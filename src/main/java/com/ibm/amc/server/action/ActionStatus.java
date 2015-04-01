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

import java.net.URI;
import java.util.Date;
import java.util.List;

/**
 * Represents the status of an individual action.
 */
public interface ActionStatus
{
	// @CLASS-COPYRIGHT@

	/**
	 * Action states.
	 */
	enum State
	{
		SUBMITTED, STARTED, SUCCEEDED, FAILED
	}

	/**
	 * Returns the identifier for the action.
	 * 
	 * @return the action identifier
	 */
	String getActionId();

	/**
	 * Returns the name of the action (non-translated machine readable).
	 * 
	 * @return the name
	 */
	String getName();

	/**
	 * Returns the NLS key for the action description.
	 * 
	 * @return the description key
	 */
	String getDescriptionKey();

	/**
	 * Returns the NLS inserts for the action description.
	 * 
	 * @return the description inserts
	 */
	String[] getDescriptionInserts();

	/**
	 * Returns the identifier for the appliance the action is being performed on.
	 * 
	 * @return the appliance ID
	 */
	String getApplianceId();

	/**
	 * Returns the time stamp for when the action was submitted.
	 * 
	 * @return the submission time stamp
	 */
	Date getSubmitted();

	/**
	 * Returns the time stamp for the action's last update.
	 * 
	 * @return the time stamp for the last update
	 */
	Date getUpdated();

	/**
	 * Returns the current state of the action.
	 * 
	 * @return the action state
	 */
	State getState();

	/**
	 * The ID of the user that submitted the action.
	 * 
	 * @return the ID of the user that submitted the action
	 */
	String getUserId();

	/**
	 * Optionally returns a URI representing the result of the action.
	 * 
	 * @return the URI of the result
	 */
	URI getResult();

	/**
	 * Indicates whether the action is being performed synchronously.
	 * 
	 * @return <code>true</code> if synchronous, otherwise <code>false</code>
	 */
	boolean isSynchronous();

	/**
	 * Returns the historic updates that have been made to the action status.
	 * 
	 * @return the status updates
	 */
	List<ActionStatusUpdate> getUpdates();

	/**
	 * Updates the state of the action status to <code>STARTED</code>.
	 */
	void started();

	/**
	 * Updates the state of the action status to <code>FAILED</code>.
	 * 
	 * @param exception
	 *            the exception that caused the failure of the action
	 */
	void failed(Throwable exception);

	/**
	 * Updates the state of the action status to <code>SUCCEEDED</code>.
	 */
	void succeeded();

	/**
	 * Updates the state of the action status to <code>SUCCEEDED</code>.
	 * 
	 * @param result
	 *            a URI representing the result of the action
	 */
	void succeeded(URI result);

	/**
	 * Add an update to the action status. The state will not be changed.
	 * 
	 * @param messageKey
	 *            the key of the update message
	 * @param messageInserts
	 *            any message inserts
	 */
	void updateStatus(final String messageKey, final String... messageInserts);
}
