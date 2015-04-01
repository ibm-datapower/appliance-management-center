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
package com.ibm.amc.resources.exceptions;

import javax.ws.rs.core.Response.Status;

/**
 * An exception indicating that a particular resource does not exist. This 
 * applies specifically to cases in which the client asked for a particular
 * item by name, but provided a name for which no resource can be found.
 */
public class NoSuchResourceException extends AmcIllegalArgumentException 
{
	// @CLASS-COPYRIGHT@

	private static final long serialVersionUID = 1L;
	private String unknownResourceId;

	public NoSuchResourceException(String resourceId) 
	{
		super(Status.NOT_FOUND, "CWZBA0502E_WAMT_NO_SUCH_RESOURCE", resourceId);
		this.unknownResourceId = resourceId;
	}

	public String getResourceId() 
	{
		return unknownResourceId;
	}
}
