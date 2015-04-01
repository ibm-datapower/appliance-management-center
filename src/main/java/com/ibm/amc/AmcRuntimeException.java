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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import javax.ws.rs.core.Response.Status;

import com.ibm.amc.nls.NLS;
import com.ibm.amc.ras.Logger47;
import com.ibm.amc.resources.exceptions.LocalizedException;
import com.ibm.datapower.amt.DMgrException;

public class AmcRuntimeException extends RuntimeException implements LocalizedException
{
	// @CLASS-COPYRIGHT@

	static Logger47 logger = Logger47.get(AmcRuntimeException.class.getCanonicalName(), Constants.CWZBA_BUNDLE_NAME);

	final Status httpStatus;

	String messageKey;

	String[] inserts;

	final Date timeStamp = new Date();

	private DMgrException wamtCause;

	private static final long serialVersionUID = -6645231665570393553L;

	/**
	 * Fail the current request with a specific HTTP status, message, and the associated explanation
	 * / useraction.
	 */
	public AmcRuntimeException(final Status httpStatus, final String messageKey, final String... inserts)
	{
		super();

		if (logger.isEntryEnabled()) logger.entry("<init>", httpStatus, messageKey, inserts);
		Util.logInserts(logger, inserts);

		this.httpStatus = httpStatus;
		this.messageKey = messageKey;
		this.inserts = inserts;

		if (logger.isEntryEnabled()) logger.exit("<init>");
	}

	/**
	 * Fail the current request with a specific HTTP status, message, and explanation / useraction,
	 * plus a subordinate "cause" object generated from the given exception. Message, action and
	 * explanation from the cause are included in the response; this works recursively down the
	 * causedBy tree.
	 */
	public AmcRuntimeException(final Status httpStatus, final Throwable cause, final String messageKey, final String... inserts)
	{
		super(cause);

		if (logger.isEntryEnabled()) logger.entry("<init>", httpStatus, cause, messageKey, inserts);
		Util.logInserts(logger, inserts);

		this.httpStatus = httpStatus;
		this.messageKey = messageKey;
		this.inserts = inserts;

		if (logger.isEntryEnabled()) logger.exit("<init>");
	}

	/**
	 * Fail the current request with an HTTP 500 and a generic message about an unexpected error,
	 * plus a cause object generated from the given exception.
	 */
	public AmcRuntimeException(final Throwable cause)
	{
		super(cause);

		if (logger.isEntryEnabled()) logger.entry("<init>", cause);

		if (logger.isDebugEnabled()) printStackTrace(cause);

		this.httpStatus = Status.INTERNAL_SERVER_ERROR;
		this.messageKey = "CWZBA0504E_UNEXPECTED_ERROR";
		final String message = (cause.getLocalizedMessage() == null) ? cause.getClass().getCanonicalName() : cause.getLocalizedMessage();
		this.inserts = new String[] { message };

		if (logger.isEntryEnabled()) logger.exit("<init>");
	}

	/**
	 * Fail the current request, using the message/explanation/useraction from the given exception
	 * directly, rather than including them as "cause" under a generic "an error occurred" message.
	 */
	public AmcRuntimeException(final Status httpStatus, final DMgrException cause)
	{
		super(cause);

		if (logger.isEntryEnabled()) logger.entry("<init>", cause);

		if (logger.isDebugEnabled()) printStackTrace(cause);

		this.httpStatus = httpStatus;
		this.wamtCause = cause;

		if (logger.isEntryEnabled()) logger.exit("<init>");
	}

	public String getMessage()
	{
		return getMessage(Locale.getDefault());
	}

	@Override
	public String getMessage(Locale locale)
	{
		if (wamtCause != null) return wamtCause.getMessage(locale);
		return NLS.formatMessageWithoutCode(locale, messageKey, inserts);
	}

	@Override
	public String getExplanation(Locale locale)
	{
		if (wamtCause != null) return wamtCause.getMessageExplanation(locale);
		return NLS.formatMessageWithoutCode(locale, messageKey + ".explanation");
	}

	@Override
	public String getUserAction(Locale locale)
	{
		if (wamtCause != null) return wamtCause.getMessageUseraction(locale);
		return NLS.formatMessageWithoutCode(locale, messageKey + ".useraction");
	}

	@Override
	public String getMessageCode()
	{
		if (wamtCause != null) return wamtCause.getMessage().substring(0, "WAMT".length() + 5);
		return Logger47.getMessageCode(messageKey);
	}

	@Override
	public Status getHttpStatusCode()
	{
		return httpStatus;
	}

	@Override
	public Date getTimeStamp()
	{
		return new Date(timeStamp.getTime());
	}

	private static void printStackTrace(Throwable cause)
	{
		final Writer writer = new StringWriter();
		final PrintWriter printWriter = new PrintWriter(writer);
		cause.printStackTrace(printWriter);
		logger.debug("printStackTrace", writer.toString());
	}

	@Override
	public String getBundleName()
	{
		return Constants.CWZBA_BUNDLE_NAME;
	}

	@Override
	public String getMessageKey()
	{
		return messageKey;
	}

	@Override
	public String[] getMessageInserts()
	{
		return Arrays.copyOf(inserts, inserts.length);
	}

	@Override
	public String getUserActionKey()
	{
		return messageKey + ".useraction";
	}

	@Override
	public String getExplanationKey()
	{
		return messageKey + ".explanation";
	}

}
