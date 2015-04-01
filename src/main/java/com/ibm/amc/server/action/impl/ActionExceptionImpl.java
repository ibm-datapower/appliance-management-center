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

import static com.ibm.amc.Util.arrayToTruncatedList;
import static com.ibm.amc.Util.truncateString;

import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

import com.ibm.amc.Constants;
import com.ibm.amc.ras.Logger47;
import com.ibm.amc.resources.exceptions.LocalizedException;
import com.ibm.amc.server.action.ActionException;
import com.ibm.datapower.amt.DMgrException;
import com.ibm.datapower.amt.Messages;
import com.ibm.datapower.amt.clientAPI.Configuration;

/**
 * Representation of an exception to avoid issues with JPA (de)serialization.
 */
@Entity(name = "ActionException")
public class ActionExceptionImpl implements ActionException
{
	// @CLASS-COPYRIGHT@

	static Logger47 logger = Logger47.get(ActionExceptionImpl.class.getCanonicalName(), Constants.CWZBA_BUNDLE_NAME);

	private static final int COLUMN_LENGTH = 512;

	private String messageCode;
	@Column(length = COLUMN_LENGTH)
	private String message;
	@Column(length = COLUMN_LENGTH)
	private String userAction;
	@Column(length = COLUMN_LENGTH)
	private String explanation;
	private String bundleName;
	private String messageKey;
	@ElementCollection
	@Column(length = COLUMN_LENGTH)
	private List<String> messageInserts;
	private String userActionKey;
	private String explanationKey;
	private Date timeStamp;
	@OneToOne(targetEntity = ActionExceptionImpl.class, cascade = CascadeType.ALL)
	private ActionException cause;

	public ActionExceptionImpl()
	{
	}

	public ActionExceptionImpl(Throwable exception)
	{

		if (exception instanceof LocalizedException)
		{
			final LocalizedException localizedException = (LocalizedException) exception;
			messageCode = localizedException.getMessageCode();
			bundleName = localizedException.getBundleName();
			messageKey = localizedException.getMessageKey();
			messageInserts = arrayToTruncatedList(localizedException.getMessageInserts(), COLUMN_LENGTH);
			userActionKey = localizedException.getUserActionKey();
			explanationKey = localizedException.getExplanationKey();
			timeStamp = localizedException.getTimeStamp();
		}
		else if (exception instanceof DMgrException)
		{
			// Nasty hacking with WAMT internals so that we don't lose any NLS capability
			final DMgrException dmgrException = (DMgrException) exception;
			messageKey = (String) getPrivateField(dmgrException, "msgKey");
			if (messageKey != null)
			{
				bundleName = Messages.BUNDLE_NAME;
				userActionKey = messageKey + ".useraction";
				explanationKey = messageKey + ".explanation";
				Object[] inserts = (Object[]) getPrivateField(dmgrException, "msgParms");
				if (inserts != null)
				{
					messageInserts = new ArrayList<String>(inserts.length);
					for (Object insert : inserts)
					{
						if (insert != null)
						{
							messageInserts.add(truncateString(insert.toString(), COLUMN_LENGTH));
						}
						else
						{
							messageInserts.add(null);
						}
					}
				}
				final String message = dmgrException.getMessage();
				if (message != null && message.startsWith(Configuration.DEFAULT_MESSAGE_PREFIX))
				{
					// Extract message code
					messageCode = message.substring(0, Configuration.DEFAULT_MESSAGE_PREFIX.length() + 5);
				}
			}
			else
			{
				setMessage(dmgrException.getMessage());
				if (message != null && message.startsWith(Configuration.DEFAULT_MESSAGE_PREFIX))
				{
					// Extract message code
					messageCode = message.substring(0, Configuration.DEFAULT_MESSAGE_PREFIX.length() + 5);
					setMessage(message.substring(Configuration.DEFAULT_MESSAGE_PREFIX.length() + 7));
				}
				setUserAction(dmgrException.getMessageUseraction());
				setExplanation(dmgrException.getMessageExplanation());
			}
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
			setMessage(buffer.toString());
		}

		cause = (exception.getCause() == null) ? null : new ActionExceptionImpl(exception.getCause());
	}

	@Override
	public ActionException getCause()
	{
		return cause;
	}

	@Override
	public String getMessage(Locale locale)
	{
		String result = null;
		if (messageKey != null)
		{
			try
			{
				final ResourceBundle bundle = ResourceBundle.getBundle(bundleName, locale);
				if (bundle != null)
				{
					final String messageString = bundle.getString(messageKey);
					if (messageString != null && messageInserts != null && messageInserts.size() > 0)
					{
						result = MessageFormat.format(messageString, messageInserts.toArray());
					}
					else
					{
						result = messageString;
					}
					// Take the message code off the front of WAMT messages
					if (Messages.BUNDLE_NAME.equals(bundleName))
					{
						result = result.substring(7);
					}
				}
			}
			catch (MissingResourceException e)
			{
			}
		}
		return (result == null) ? message : result;
	}

	@Override
	public String getUserAction(Locale locale)
	{
		String result = null;
		if (userActionKey != null)
		{
			try
			{
				result = ResourceBundle.getBundle(bundleName, locale).getString(userActionKey);
			}
			catch (MissingResourceException e)
			{
			}
		}
		return (result == null) ? userAction : result;
	}

	@Override
	public String getExplanation(Locale locale)
	{
		String result = null;
		if (explanationKey != null)
		{
			try
			{
				result = ResourceBundle.getBundle(bundleName, locale).getString(explanationKey);
			}
			catch (MissingResourceException e)
			{
			}
		}
		return (result == null) ? explanation : result;
	}

	@Override
	public String getMessageCode()
	{
		return messageCode;
	}

	@Override
	public Date getTimeStamp()
	{
		if (timeStamp == null) return null;
		return new Date(timeStamp.getTime());
	}

	private void setMessage(String message)
	{
		this.message = truncateString(message, COLUMN_LENGTH);
	}

	private void setUserAction(String userAction)
	{
		this.userAction = truncateString(userAction, COLUMN_LENGTH);
	}

	private void setExplanation(String explanation)
	{
		this.explanation = truncateString(explanation, COLUMN_LENGTH);
	}

	private static final Object getPrivateField(DMgrException object, String name)
	{
		try
		{
			Field field = DMgrException.class.getDeclaredField(name);
			field.setAccessible(true);
			return field.get(object);
		}
		catch (final Exception e)
		{
			if (logger.isDebugEnabled()) logger.debug("getPrivateField", "Failed to get field " + name + " due to exception:" + e);
			return null;
		}
	}

}
