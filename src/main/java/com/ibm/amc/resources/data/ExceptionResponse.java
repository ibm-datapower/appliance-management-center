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

import java.util.Date;
import java.util.Locale;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize.Inclusion;
import com.ibm.amc.nls.NLS;
import com.ibm.amc.resources.exceptions.LocalizedException;
import com.ibm.amc.server.action.ActionException;
import com.ibm.datapower.amt.DMgrException;
import com.ibm.datapower.amt.clientAPI.Configuration;

/**
 * The response that the server uses to indicate to a ReST client that an error occurred. It should
 * be accompanied by an appropriate HTTP response code.
 */
public class ExceptionResponse extends ActionResponse
{
	// No CLASS-COPYRIGHT because we don't want copyright text in ReST responses.

	/**
	 * @param userId
	 *            the ID of the user that submitted the action
	 * @param timeStamp
	 *            time stamp for the message
	 * @param messageKey
	 *            A globalisation message key
	 * @param inserts
	 *            Any strings that should be inserted into the message.
	 */
	public ExceptionResponse(final String userId, final Date timeStamp, final String messageKey, final String... inserts)
	{
		super(userId, timeStamp, messageKey, inserts);
		
		userAction = NLS.formatMessage(userId, messageKey + ".useraction");
		explanation =  NLS.formatMessage(userId, messageKey + ".explanation");
	}

	public ExceptionResponse(final ActionException exception)
	{
		super();

		// TODO the locale should be taken from user preferences
		final Locale locale = Locale.getDefault();

		code = exception.getMessageCode();
		message = exception.getMessage(locale);
		userAction = exception.getUserAction(locale);
		explanation = exception.getExplanation(locale);
		timeStamp = exception.getTimeStamp();
		if (exception.getCause() != null) cause = new ExceptionResponse(exception.getCause());

	}

	public ExceptionResponse(final Throwable exception)
	{
		super();
		// TODO the locale should be taken from user preferences
		final Locale locale = Locale.getDefault();
		if (exception instanceof LocalizedException)
		{
			final LocalizedException localizedException = (LocalizedException) exception;
			code = localizedException.getMessageCode();
			message = localizedException.getMessage(locale);
			userAction = localizedException.getUserAction(locale);
			explanation = localizedException.getExplanation(locale);
			timeStamp = localizedException.getTimeStamp();
		}
		else if (exception instanceof DMgrException)
		{
			final DMgrException dmgrException = (DMgrException) exception;
			message = dmgrException.getMessage(locale);
			if (message != null && message.startsWith(Configuration.DEFAULT_MESSAGE_PREFIX))
			{
				// Extract message code
				code = message.substring(0, Configuration.DEFAULT_MESSAGE_PREFIX.length() + 5);
				message = message.substring(Configuration.DEFAULT_MESSAGE_PREFIX.length() + 7);
			}
			userAction = dmgrException.getMessageUseraction(locale);
			explanation = dmgrException.getMessageExplanation(locale);
		}
		else
		{
			final StringBuffer buffer = new StringBuffer();
			buffer.append(exception.getClass().getCanonicalName());
			if (exception.getLocalizedMessage() != null)
			{
				buffer.append(": ");
				buffer.append(exception.getLocalizedMessage());
			}
			message = buffer.toString();
		}

		if (exception.getCause() != null)
		{
			cause = new ExceptionResponse(exception.getCause());
		}
	}

	// ------------------------------------------------------------------------
	// Boilerplate methods for Jackson purposes.
	// ------------------------------------------------------------------------

	public ExceptionResponse()
	{
		super();
	}

	@JsonSerialize(include = Inclusion.NON_NULL)
	public ExceptionResponse cause;
	@JsonSerialize(include = Inclusion.NON_NULL)
	public String explanation;
	@JsonSerialize(include = Inclusion.NON_NULL)
	public String userAction;

}
