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
 * An exception indicating that an error (probably an IOException) occurred 
 * while loading the properties file. It has a getLocalizedMessage, but for 
 * logging purposes you probably want to have the logger assemble the message 
 * itself, using getCause for the inserts.
 */
@SuppressWarnings("serial")
public class PropertyLoadException extends RuntimeException
{
	// @CLASS-COPYRIGHT@

	private static final String DEFAULT_MESSAGE_KEY = "CWZBA0004E_WAMC_PROPERTIES_NOT_LOADED";
	private String customMessageKey;
	private String[] insertsForCustomMessage;
	
	/**
	 * Create a PropertyLoadException, explaining the reason in terms of a cause
	 * exception  
	 * @param e The cause
	 */
	public PropertyLoadException(Exception e)
	{
		super(e);
	}

	/**
	 * Create a PropertyLoadException, explaining the reason in text.
	 * @param messageKey 
	 * @param inserts
	 */
	public PropertyLoadException(String messageKey, String... inserts)
	{
		this.customMessageKey = messageKey;
		this.insertsForCustomMessage = inserts;
	}

	@Override
	public String getLocalizedMessage()
	{
		if(customMessageKey != null)
		{
			return NLS.formatMessageWithCode(NLS.SERVER_LOCALE, customMessageKey, insertsForCustomMessage);
		}
		return NLS.formatMessageWithoutCode(NLS.SERVER_LOCALE, DEFAULT_MESSAGE_KEY, 
				this.getCause().getClass().getSimpleName(),
				this.getCause().getLocalizedMessage());
	}
}
