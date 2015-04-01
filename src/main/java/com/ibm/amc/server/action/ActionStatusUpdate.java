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

import java.util.Date;

/**
 * Represents an historic update to the status for an action.
 */
public interface ActionStatusUpdate
{
	// @CLASS-COPYRIGHT@
	/**
	 * If the status update represents a failure, returns the exception that caused the failure,
	 * otherwise returns <code>null</code>.
	 * 
	 * @return the cause of a failure, otherwise <code>null</code>
	 */
	ActionException getCause();

	/**
	 * The time stamp for the update.
	 * 
	 * @return the time stamp
	 */
	Date getTimeStamp();

	/**
	 * The NLS key for the update message.
	 * 
	 * @return the message key
	 */
	String getMessageKey();

	/**
	 * The NLS inserts for the update message.
	 * 
	 * @return the message inserts
	 */
	String[] getMessageInserts();
}
