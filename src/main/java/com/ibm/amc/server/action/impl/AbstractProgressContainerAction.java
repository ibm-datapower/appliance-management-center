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
import com.ibm.amc.nls.NLS;
import com.ibm.amc.ras.Logger47;
import com.ibm.amc.server.action.ActionFactory;
import com.ibm.amc.server.action.ActionStatus;
import com.ibm.datapower.amt.clientAPI.ProgressContainer;

abstract public class AbstractProgressContainerAction extends AbstractAction
{
	// @CLASS-COPYRIGHT@
	static Logger47 logger = Logger47.get(AbstractProgressContainerAction.class.getCanonicalName(), Constants.CWZBA_BUNDLE_NAME);

	public AbstractProgressContainerAction(String name, String subjectId, String descriptionKey, String... descriptionInserts)
	{
		super(name, subjectId, descriptionKey, descriptionInserts);
	}

	@Override
	public void start(String actionId)
	{
		ActionStatus statusHandle = ActionFactory.getActionLog().getActionStatus(actionId);

		try
		{
			ProgressContainer progress = submit(actionId);

			// Work on updates from WAMT, passing them on to the status mechanism.

			while (true) // break out when progress container reports complete or error
			{
				if (progress.isComplete())
				{
					succeeded(statusHandle);
					return;
				}

				if (progress.hasError())
				{
					failed(statusHandle, progress.getError());
					return;
				}

				if (progress.hasUpdate())
				{
					if (logger.isDebugEnabled())
					{
						String wamtMessage = NLS.wamtMessageToUserLocale(statusHandle.getUserId(), progress.getCurrentStepDescriptionKey(), progress.getCurrentStepDescriptionArgs());
						logger.debug("start", "WAMT update: " + wamtMessage);
					}
					// TODO: Add the status update back when WAMT has something useful to say
					// statusHandle.updateStatus("CWZBA1018I_DEPLOY_FIRMWARE_UPDATE", wamtMessage);
				}

				// Go round the loop again next time news arrives.
				try
				{
					progress.waitForUpdate();
				}
				catch (InterruptedException e)
				{
					// OK, interrupted, go do the checks in the loop.
					if (logger.isDebugEnabled())
					{
						logger.debug("start", "waitForUpdate interrupted: " + e);
					}
				}

			}

		}
		catch (final Throwable exception)
		{
			failed(statusHandle, exception);
		}

	}

	public void succeeded(final ActionStatus statusHandle)
	{
		statusHandle.succeeded();
	}

	public void failed(final ActionStatus statusHandle, final Throwable exception)
	{
		statusHandle.failed(exception);
	}

	abstract public ProgressContainer submit(String actionId) throws Exception;

}
