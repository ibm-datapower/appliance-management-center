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

import java.util.Arrays;

import com.ibm.amc.server.action.AsyncAction;

/**
 * Base class for action implementations.
 */
public abstract class AbstractAction implements AsyncAction
{
	// @CLASS-COPYRIGHT@
	private String name;
	private String descriptionKey;
	private String[] descriptionInserts;
	private String subjectId;

	/**
	 * @param name A short string identifying the type of action being performed.
	 * This is not displayed to users.
	 * @param subjectId An identifier for the subject of the action, the thing
	 * it is being performed upon. For example, when an appliance is quiesced,
	 * the subject of the action is the appliance, and this argument is the 
	 * appliance ID.
	 * @param descriptionKey The NLS key for a human-readable description of the
	 * action.
	 * @param descriptionInserts Any inserts used by the NLS key.
	 */
	public AbstractAction(String name, String subjectId, String descriptionKey, String... descriptionInserts)
	{
		this.name = name;
		this.subjectId = subjectId;
		this.descriptionKey = descriptionKey;
		this.descriptionInserts = descriptionInserts;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public String getDescriptionKey()
	{
		return descriptionKey;
	}

	@Override
	public String[] getDescriptionInserts()
	{
		return Arrays.copyOf(descriptionInserts, descriptionInserts.length); // Defensive copy
	}

	@Override
	public String getSubjectId()
	{
		return subjectId;
	}
}
