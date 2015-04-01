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
package com.ibm.amc;

import com.ibm.amc.nls.NLS;

/**
 * An exception indicating that an error (probably IOException) occurred 
 * while loading a resource file. It has a getLocalizedMessage, but for 
 * logging purposes you probably want to have the logger assemble the message 
 * itself, using getCause for the inserts.
 */
public class ResourceLoadException extends RuntimeException
{
	// @CLASS-COPYRIGHT@

	private static final long serialVersionUID = -2527845651178079634L;
	private static final String MESSAGE_KEY = "CWZBA0006E_RESOURCE_NOT_LOADED";
	
	public ResourceLoadException(Exception e)
	{
		super(e);
	}

	@Override
	public String getLocalizedMessage()
	{
		return NLS.formatMessageWithoutCode(NLS.SERVER_LOCALE, MESSAGE_KEY, 
				this.getCause().getClass().getSimpleName(),
				this.getCause().getLocalizedMessage());
	}
}
