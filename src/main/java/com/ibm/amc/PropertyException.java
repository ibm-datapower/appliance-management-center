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

import java.util.HashMap;
import java.util.Map;

import com.ibm.amc.ras.Logger47;


/**
 * An exception indicating that one or more WAMC properties are invalid or 
 * missing. It encapsulates a series of errors (so that a user can fix them
 * all at once) so it offers a logErrors method to log all of them.
 */
@SuppressWarnings("serial")
public class PropertyException extends Exception
{
	// @CLASS-COPYRIGHT@

	public enum Error
	{
		MISSING("CWZBA0511E_CONFIG_PROPERTY_MISSING"),
		NOT_INTEGER("CWZBA0512E_CONFIG_PROPERTY_NOT_INTEGER"),
		NOT_BOOLEAN("CWZBA0513E_CONFIG_PROPERTY_NOT_BOOLEAN");
		
		private String messageKey;

		Error(String messageKey)
		{
			this.messageKey = messageKey;
		}
	}

	private Map<String, Error> errors = new HashMap<String, Error>();

	/**
	 * Record the fact that a property is in error.
	 * @param propertyName The name (as used in the properties file) of the 
	 * erroneous property.
	 * @param errorType What is wrong with it.
	 */
	public void addPropertyError(String propertyName, Error errorType)
	{
		errors.put(propertyName, errorType);
	}

	/**
	 * @return true if there has been at least one call to addPropertyError, ie
	 * if at least one property is invalid.
	 */
	public boolean hasErrors()
	{
		return !errors.isEmpty();
	};
	
	/**
	 * Get a list (actually a Map) of the properties that have errors.
	 * @return a Map of property names to Errors, for example
	 * <pre>com.ibm.amc.colourOfProgrammersSocks -> Error.MISSING,
	 *com.ibm.amc.numberOfSandwiches -> Error.NOT_INTEGER</pre>
	 */
	public Map<String, Error> getErrors()
	{
		return errors;
	}
	
	/**
	 * Output error messages to the log describing the problem(s) with the 
	 * properties. If there are none, this method returns immediately without
	 * logging anything.
	 * @param logger the logger to which the messages should be sent.
	 */
	public void logErrors(Logger47 logger)
	{
		if(hasErrors())
		{
			logger.error("CWZBA0510E_CONFIG_PROPERTY_PROBLEM");
			for (String error_property : errors.keySet())
			{
				logger.error(errors.get(error_property).messageKey, error_property);
			}
		}
	}
}
