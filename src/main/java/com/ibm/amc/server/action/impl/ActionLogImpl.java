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
package com.ibm.amc.server.action.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.ibm.amc.AmcRuntimeException;
import com.ibm.amc.Constants;
import com.ibm.amc.ras.Logger47;
import com.ibm.amc.resources.exceptions.NoSuchResourceException;
import com.ibm.amc.server.action.ActionLog;
import com.ibm.amc.server.action.ActionStatus;
import com.ibm.amc.server.action.ActionStatusListener;
import com.ibm.amc.server.action.AsyncAction;
import com.ibm.amc.server.impl.PersistenceContext;

/**
 * Derby based log of actions.
 */
public class ActionLogImpl implements ActionLog
{
	// @CLASS-COPYRIGHT@
	static Logger47 logger = Logger47.get(ActionLogImpl.class.getCanonicalName(), Constants.CWZBA_BUNDLE_NAME);

	private static Set<ActionStatusListener> actionStatusListeners = Collections.synchronizedSet(new HashSet<ActionStatusListener>());

	@Override
	public ActionStatus getActionStatus(final String actionId)
	{
		if (logger.isEntryEnabled()) logger.entry("getActionStatus", actionId);

		ActionStatus result = null;

		try
		{
			long id = Long.parseLong(actionId.trim());
			result = getEntityManager().find(ActionStatusImpl.class, id);
		}
		catch (NumberFormatException e)
		{
			throw new NoSuchResourceException(actionId);
		}
		catch (Exception e)
		{
			logger.error("Failed to retrieve action status", e);
			logger.debug("getActionStatus", "Action ID: ", actionId);
			throw new AmcRuntimeException(e);
		}

		if (result == null)
		{
			throw new NoSuchResourceException(actionId);
		}

		if (logger.isEntryEnabled()) logger.exit("getActionStatus", result);
		return result;
	}

	@Override
	public List<ActionStatus> getActionStatuses()
	{
		if (logger.isEntryEnabled()) logger.entry("getActionStatuses");

		/* construct the query and execute */
		Query q = getEntityManager().createQuery(Constants.QUERY_ACTION_STATUS_ALL);
		@SuppressWarnings("unchecked")
		List<ActionStatus> result = q.getResultList();
		
		if (result == null)
		{
			result = new ArrayList<ActionStatus>();
		}

		if (logger.isEntryEnabled()) logger.exit("getActionStatuses", result);
		return result;
	}

	@Override
	public String actionSubmitted(String userId, AsyncAction action)
	{
		if (logger.isEntryEnabled()) logger.entry("actionSubmitted", userId, action);

		final ActionStatus status = new ActionStatusImpl(userId, action);
		notifyListeners(status);
		final String actionId = status.getActionId();		

		if (logger.isEntryEnabled()) logger.exit("actionSubmitted", actionId);
		return actionId;
	}

	@Override
	public ActionStatus actionStarted(final String userId, final String applianceId, final String name, final String descriptionKey, final String... descriptionInserts)
	{
		if (logger.isEntryEnabled()) logger.entry("actionStarted", userId);

		final ActionStatus status = new ActionStatusImpl(userId, applianceId, name, descriptionKey, descriptionInserts);
		notifyListeners(status);

		if (logger.isEntryEnabled()) logger.exit("actionSubmitted", status);
		return status;
	}

	@Override
	public void addActionListener(final ActionStatusListener listener)
	{
		actionStatusListeners.add(listener);
	}

	@Override
	public void removeActionListener(final ActionStatusListener listener)
	{
		actionStatusListeners.remove(listener);
	}

	static void notifyListeners(final ActionStatus status)
	{
		final Set<ActionStatusListener> listeners; // Copy for iterating over
		synchronized (actionStatusListeners)
		{
			listeners = new HashSet<ActionStatusListener>(actionStatusListeners);
		}

		for (ActionStatusListener listener : listeners)
		{
			listener.actionStatusUpdated(status);
		}
	}

	private EntityManager getEntityManager()
	{
		return PersistenceContext.getContext().getActionHistoryEntityManager();
	}

	@Override
	public List<ActionStatus> getActionStatusesForUser(String userName)
	{
		if (logger.isEntryEnabled()) logger.entry("getActionStatusesForUser", userName);

		/* construct the query and execute */
		Query q = getEntityManager().createQuery(Constants.QUERY_ACTION_STATUS_FOR_USER);
		q.setParameter("userId", userName);
		@SuppressWarnings("unchecked")
		List<ActionStatus> result = q.getResultList();
		
		if (result == null)
		{
			result = new ArrayList<ActionStatus>();
		}

		if (logger.isEntryEnabled()) logger.exit("getActionStatusesForUser", result);
		return result;
	}

}
