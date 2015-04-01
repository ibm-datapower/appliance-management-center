/**
 * Copyright 2014 IBM Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
package com.ibm.amc.feedback;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.AsyncContext;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.ibm.amc.Constants;
import com.ibm.amc.IShutdown;
import com.ibm.amc.ShutdownListener;
import com.ibm.amc.ras.Logger47;
import com.ibm.amc.resources.data.ActionStatusResponse;
import com.ibm.amc.server.action.ActionFactory;
import com.ibm.amc.server.action.ActionStatus;
import com.ibm.amc.server.action.ActionStatusListener;

public class FeedbackHandler implements ActionStatusListener, IShutdown
{
	// @CLASS-COPYRIGHT@

	private static final int POLLING_TIMEOUT = 20;

	private static final int ACTION_STATUS_TIMEOUT = 60 * 1000;

	static Logger47 logger = Logger47.get(FeedbackHandler.class.getCanonicalName(), Constants.CWZBA_BUNDLE_NAME);

	private final Map<String, Queue<ActionStatusResponse>> statuses = Collections.synchronizedMap(new HashMap<String, Queue<ActionStatusResponse>>());

	private ScheduledExecutorService executor = Executors.newScheduledThreadPool(10);

	private ObjectMapper mapper;

	/**
	 * Map of feedback listeners keyed off user name. Populated by <code>FeedbackServlet</code>.
	 */
	final Map<String, Set<AsyncContext>> feedbackListeners = Collections.synchronizedMap(new HashMap<String, Set<AsyncContext>>());

	public FeedbackHandler()
	{
		if (logger.isEntryEnabled()) logger.entry("FeedbackHandler");

		mapper = new ObjectMapper();
		mapper.getFactory().disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);				ShutdownListener.addShutdownListener(this);

		ActionFactory.getActionLog().addActionListener(this);

		// Check for action statuses that are too old and remove them
		executor.scheduleAtFixedRate(new Runnable()
		{

			@Override
			public void run()
			{
				final Date date = new Date(System.currentTimeMillis() - ACTION_STATUS_TIMEOUT);
				synchronized (statuses)
				{
					for (Queue<ActionStatusResponse> queue : statuses.values())
					{
						while (queue.peek().updated.before(date))
						{
							final ActionStatusResponse response = queue.poll();
							if (logger.isDebugEnabled()) logger.debug("FeedbackHandler$Runnable.run", "Expiring action feedback response for action " + response.actionId);
						}
					}
				}

			}
		}, ACTION_STATUS_TIMEOUT, ACTION_STATUS_TIMEOUT, TimeUnit.MILLISECONDS);

		if (logger.isEntryEnabled()) logger.exit("FeedbackHandler");
	}		public void shutdown()	{		if(executor != null)			executor.shutdown();	}

	@Override
	public void actionStatusUpdated(final ActionStatus status)
	{
		if (logger.isEntryEnabled()) logger.entry("actionStatusUpdated", status);

		if (!status.isSynchronous())
		{
			final String user = status.getUserId();

			final Set<AsyncContext> contexts = feedbackListeners.remove(user);
			if (contexts == null)
			{
				// No one waiting - queue status
				if (logger.isDebugEnabled()) logger.debug("actionStatusUpdated", "Queuing status update for action " + status.getActionId());
				queueActionStatus(status);
			}
			else
			{
				// Request waiting - send response immediately
				if (logger.isDebugEnabled()) logger.debug("actionStatusUpdated", "Sending status update for action " + status.getActionId());
				final Queue<ActionStatusResponse> queue = new LinkedList<ActionStatusResponse>();
				queue.offer(new ActionStatusResponse(status));
				for (AsyncContext ctx : contexts)
				{
					writeResponse(ctx.getResponse(), queue);
					ctx.complete();
				}
			}
		}

		if (logger.isEntryEnabled()) logger.exit("actionStatusUpdated");
	}

	public void handleRequest(final HttpServletRequest request, final HttpServletResponse response)
	{
		if (logger.isEntryEnabled()) logger.entry("handleRequest", request, response);

		final String user = request.getRemoteUser();
		final Queue<ActionStatusResponse> statuses = getActionStatuses(user);
		if (statuses.isEmpty())
		{
			// No updates pending - register listener
			final AsyncContext asyncContext = request.startAsync(request, response);
			asyncContext.setTimeout(900000000L);
			logger.debug("handleRequest", "Registering new listener for user " + user);
			synchronized (feedbackListeners)
			{
				Set<AsyncContext> contexts = feedbackListeners.get(user);
				if (contexts == null)
				{
					contexts = new HashSet<AsyncContext>();
					feedbackListeners.put(user, contexts);
				}
				contexts.add(asyncContext);
			}

			// Timeout listener
			executor.schedule(new Runnable()
			{

				@Override
				public void run()
				{
					synchronized (feedbackListeners)
					{
						final Set<AsyncContext> contexts = feedbackListeners.get(user);
						if (contexts.remove(asyncContext))
						{
							if (logger.isDebugEnabled()) logger.debug("handleRequest$Runnable.run", "Timing out listener for user " + user);
							writeResponse(asyncContext.getResponse(), new LinkedList<ActionStatusResponse>());
							asyncContext.complete();
							if (contexts.isEmpty()) feedbackListeners.remove(user);
						}
					}
				}

			}, POLLING_TIMEOUT, TimeUnit.SECONDS);
		}
		else
		{
			// Update pending - send response immediately
			writeResponse(response, statuses);
		}

		if (logger.isEntryEnabled()) logger.exit("handleRequest");
	}

	private void writeResponse(final ServletResponse response, final Queue<ActionStatusResponse> statuses)
	{
		try
		{
			mapper.writeValue(response.getOutputStream(), statuses);
		}
		catch (Exception e)
		{
			logger.debug("writeResponse", "Exception writing response", e);
		}
	}

	private Queue<ActionStatusResponse> getActionStatuses(final String user)
	{
		if (logger.isEntryEnabled()) logger.entry("getActionStatuses", user);
		Queue<ActionStatusResponse> userStatuses;
		synchronized (statuses)
		{
			userStatuses = statuses.remove(user);
			if (userStatuses == null)
			{
				userStatuses = new LinkedList<ActionStatusResponse>();
			}
		}
		if (logger.isEntryEnabled()) logger.exit("getActionStatuses", userStatuses);
		return userStatuses;
	}

	private void queueActionStatus(final ActionStatus status)
	{
		if (logger.isEntryEnabled()) logger.entry("queueActionStatus", status);
		if (logger.isDebugEnabled()) logger.debug("queueActionStatus", "", status.getUserId() + " " + status.getState());
		synchronized (statuses)
		{
			Queue<ActionStatusResponse> userStatuses = statuses.get(status.getUserId());
			if (userStatuses == null)
			{
				userStatuses = new LinkedList<ActionStatusResponse>();
				statuses.put(status.getUserId(), userStatuses);
			}
			userStatuses.add(new ActionStatusResponse(status));
		}
		if (logger.isEntryEnabled()) logger.exit("queueActionStatus");
	}

}
