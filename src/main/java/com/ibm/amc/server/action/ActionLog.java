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
package com.ibm.amc.server.action;

import java.util.List;

/**
 * Provides a log of asynchronous actions, past and present.
 */
public interface ActionLog
{
	// @CLASS-COPYRIGHT@

	/**
	 * Retrieves the status for an action.
	 * 
	 * @param actionId
	 *            the action identifier
	 * @return the action status
	 */
	ActionStatus getActionStatus(String actionId);

	/**
	 * Retrieves status for all actions.
	 * 
	 * @return a list of all action status objects
	 */
	List<ActionStatus> getActionStatuses();

	/**
	 * Create a new status record for an asynchronous action on submission.
	 * 
	 * @param userId
	 *            the ID of the user submitting the action
	 * @param action
	 *            the action that has been submitted
	 * @return the action identifier
	 */
	String actionSubmitted(String userId, AsyncAction action);

	/**
	 * Create a new status record for a synchronous action on starting.
	 * 
	 * @param userId
	 *            the ID of the user that submitted the action
	 * @param applianceId
	 *            the identifier of the appliance the action is being performed upon
	 * @param name
	 *            the name of the action being performed
	 * @param descriptionKey
	 *            the NLS key for the description of the action
	 * @param descriptionInserts
	 *            the inserts for the description NLS message
	 * @return the action status
	 */
	ActionStatus actionStarted(final String userId, final String applianceId, final String name, final String descriptionKey, final String... descriptionInserts);

	/**
	 * Adds a listener to receive notification of action updates.
	 * 
	 * @param listener
	 *            the listener to register
	 */
	void addActionListener(ActionStatusListener listener);

	/**
	 * Remove a listener to stop receiving notification of action updates.
	 * 
	 * @param listener
	 *            the listener to unregister
	 */
	void removeActionListener(ActionStatusListener listener);

	/**
	 * Retrieves status for actions for a given user
	 * 
	 * @param userName
	 *            the name of the user to retrieve actions for
	 * @return a list of action status objects for the given user
	 */
	List<ActionStatus> getActionStatusesForUser(String userName);
}
