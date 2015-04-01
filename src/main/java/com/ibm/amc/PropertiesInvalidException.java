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

import java.util.Locale;
import com.ibm.amc.nls.NLS;

/**
 * An exception thrown when a required property is missing, but the server is
 * still running, and that property is requested. This should never happen in
 * production, because the server is shut down by the start-wamc script if
 * properties are missing, but there's no way to enforce this shutdown in a 
 * development environment.
 */
public class PropertiesInvalidException extends RuntimeException
{
	// @CLASS-COPYRIGHT@
	
	public PropertiesInvalidException(String propertyName)
	{
		// Null locale means "format for server logs".
		super(NLS.formatMessageWithoutCode((Locale)null, "CWZBA0511E_CONFIG_PROPERTY_MISSING", propertyName));
	}

	private static final long serialVersionUID = 1L;
	
	
}
