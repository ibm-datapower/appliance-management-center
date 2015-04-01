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

/**
 * Interface implemented for asynchronous actions.
 */
public interface AsyncAction
{
	// @CLASS-COPYRIGHT@
	/**
	 * Starts execution of the action and then returns.
	 * 
	 * @param the
	 *            identifier allocated to the action
	 */
	void start(String actionId);

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
	String getSubjectId();
}
