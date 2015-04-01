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

import java.util.concurrent.ExecutorService;import java.util.concurrent.Executors;import com.ibm.amc.Constants;import com.ibm.amc.IShutdown;import com.ibm.amc.ShutdownListener;
import com.ibm.amc.ras.Logger47;import com.ibm.amc.security.SecurityContext;import com.ibm.amc.server.action.ActionController;import com.ibm.amc.server.action.ActionFactory;import com.ibm.amc.server.action.AsyncAction;import com.ibm.amc.server.impl.PersistenceContext;

/**
 * Implementation of singleton action controller. Uses <code>ExecutorService</code> to spin of
 * action submissions on to another thread.
 */
public class ActionControllerImpl implements ActionController, IShutdown
{
	// @CLASS-COPYRIGHT@
	static Logger47 logger = Logger47.get(ActionControllerImpl.class.getCanonicalName(), Constants.CWZBA_BUNDLE_NAME);

	private ExecutorService executor = Executors.newCachedThreadPool(Executors.privilegedThreadFactory());
	public ActionControllerImpl()	{		ShutdownListener.addShutdownListener(this);	}	
	@Override
	public String submitAction(final AsyncAction action)
	{
		if (logger.isEntryEnabled()) logger.entry("submitAction", action);

		final String user = SecurityContext.getContext().getUser();
		final String id = ActionFactory.getActionLog().actionSubmitted(user, action);
		
		executor.submit(new Runnable()
		{
			public void run()
			{
				try
				{
					SecurityContext.setContext(user); // Re-establish security context
					PersistenceContext.setContext();
					ActionFactory.getActionLog().getActionStatus(id).started();
					action.start(id);
					PersistenceContext.destroy();
				}
				/* anything that is not explicitly caught will result in a silent exit */
				catch (Throwable t)
				{
					logger.error("Error processing action status update - Action ID: ''" + id + "'' Action name: ''" + action.getName() + "'' Appliance ID: ''" + action.getSubjectId() + "''", t);
				}
			}

		});

		if (logger.isEntryEnabled()) logger.exit("submitAction", id);
		return id;
	}		public void shutdown() {		if(executor != null)			executor.shutdown();	}
}
