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

import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import javax.ws.rs.core.Response.Status;

import com.ibm.amc.nls.NLS;
import com.ibm.amc.ras.Logger47;
import com.ibm.amc.resources.exceptions.LocalizedException;

public class AmcException extends Exception implements LocalizedException
{
	// @CLASS-COPYRIGHT@

	static Logger47 logger = Logger47.get(AmcException.class.getCanonicalName(), Constants.CWZBA_BUNDLE_NAME);

	final Status httpStatus;

	final String messageKey;

	final String[] inserts;

	final Date timeStamp = new Date();

	private static final long serialVersionUID = -6645231665570393553L;

	public AmcException(final Status httpStatus, final String messageKey, final String... inserts)
	{
		super();

		if (logger.isEntryEnabled()) logger.entry("<init>", httpStatus, messageKey, inserts);
		Util.logInserts(logger, inserts);

		this.httpStatus = httpStatus;
		this.messageKey = messageKey;
		this.inserts = inserts;

		if (logger.isEntryEnabled()) logger.exit("<init>");
	}

	public AmcException(final Status httpStatus, final Throwable cause, final String messageKey, final String... inserts)
	{
		super(cause);

		if (logger.isEntryEnabled()) logger.entry("<init>", httpStatus, cause, messageKey, inserts);
		Util.logInserts(logger, inserts);

		this.httpStatus = httpStatus;
		this.messageKey = messageKey;
		this.inserts = inserts;

		if (logger.isEntryEnabled()) logger.exit("<init>");
	}

	public String getMessage()
	{
		return getMessage(Locale.getDefault());
	}

	@Override
	public String getMessage(Locale locale)
	{
		return NLS.formatMessageWithoutCode(locale, messageKey, inserts);
	}

	@Override
	public String getExplanation(Locale locale)
	{
		return NLS.formatMessageWithoutCode(locale, messageKey + ".explanation");
	}

	@Override
	public String getUserAction(Locale locale)
	{
		return NLS.formatMessageWithoutCode(locale, messageKey + ".useraction");
	}

	@Override
	public String getMessageCode()
	{
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
