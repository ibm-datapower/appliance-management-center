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
package com.ibm.amc.resources.data;

import com.ibm.amc.data.validation.annotations.ValidateNotBlank;

/**
 * A server-side data object that is a member of a group
 */
public class GroupMember
{
	// No CLASS-COPYRIGHT because we don't want copyright text in ReST responses.

	/** The id or primary key of the group member. Syntax of the id will depend on its type*/
	@ValidateNotBlank
	public String id;
	
	/** The display name of the group member. */
	@ValidateNotBlank
	public String name;

	/** The type of the group member */
	@ValidateNotBlank
	public Type type;
	
	public enum Type
	{
		
		APPLIANCE,
		
		DOMAIN;
		
		@Override
		public String toString()
		{
			return this.name().toLowerCase();
		}
	}
	

}
