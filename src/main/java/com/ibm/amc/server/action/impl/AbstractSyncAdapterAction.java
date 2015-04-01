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

import com.ibm.amc.Constants;
import com.ibm.amc.ras.Logger47;
import com.ibm.amc.server.action.ActionFactory;
import com.ibm.amc.server.action.ActionStatus;

/**
 * Adapter to convert a synchronous WAMT operation in to an asynchronous WAMC action.
 */
abstract public class AbstractSyncAdapterAction extends AbstractAction
{
	// @CLASS-COPYRIGHT@
	static Logger47 logger = Logger47.get(AbstractSyncAdapterAction.class.getCanonicalName(), Constants.CWZBA_BUNDLE_NAME);

	public AbstractSyncAdapterAction(String name, String subjectId, String descriptionKey, String... descriptionInserts)
	{
		super(name, subjectId, descriptionKey, descriptionInserts);
	}

	@Override
	public void start(String actionId)
	{
		final ActionStatus statusHandle = ActionFactory.getActionLog().getActionStatus(actionId);
		try
		{
			execute();
		}
		catch (Throwable t)
		{
			statusHandle.failed(t);
			return;
		}
		statusHandle.succeeded();
	}
	
	abstract public void execute() throws Throwable;

}
