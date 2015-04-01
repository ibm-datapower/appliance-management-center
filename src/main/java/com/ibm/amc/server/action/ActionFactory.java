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

import com.ibm.amc.server.action.impl.ActionControllerImpl;
import com.ibm.amc.server.action.impl.ActionLogImpl;

/**
 * Factory class for obtaining instances of the action controller and log.
 */
public class ActionFactory
{
	// @CLASS-COPYRIGHT@

	/**
	 * The action controller singleton.
	 */
	private static ActionController CONTROLLER = new ActionControllerImpl();

	/**
	 * The action log singleton.
	 */
	private static ActionLog LOG = new ActionLogImpl();

	/**
	 * Factory method to get the action controller singleton.
	 * 
	 * @return the action controller
	 */
	public static ActionController getActionController()
	{
		return CONTROLLER;
	}

	/**
	 * Factory method to get the action log singleton.
	 * 
	 * @return the action log
	 */
	public static ActionLog getActionLog()
	{
		return LOG;
	}

}
