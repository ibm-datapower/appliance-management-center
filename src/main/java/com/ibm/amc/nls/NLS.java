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
package com.ibm.amc.nls;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import com.ibm.amc.Constants;
import com.ibm.amc.ras.Logger47;
import com.ibm.amc.security.SecurityContext;
import com.ibm.datapower.amt.Messages;

public class NLS
{
	// @CLASS-COPYRIGHT@
	
	private static Logger47 logger = Logger47.get(NLS.class.getCanonicalName(), Constants.CWZBA_BUNDLE_NAME);
	
	/* If and when we add elements (translated text that is not a message), I
	 * anticipate adding formatElement(...) methods and perhaps a format(...)
	 * method that detects which is applicable and does the right thing, for 
	 * when keys are eg pulled out of the database and we don't know which 
	 * bundle they're from. That might work by knowledge of the key formats, or 
	 * by trying one and then doing the other if the first fails to find a match.
	 */
	
	/** 
	 * Special value for use in place of a user id, to indicate that the 
	 * NLS mechanism should do its own lookup on SecurityContext to find the
	 * current user. <strong>This will not work on threads which are not
	 * associated with a REST request, in particular AsyncAction worker threads.
	 * </strong>
	 */
	public static final String CURRENT_REST_USER = "current user";

	/**
	 * Special value to indicate that text should be localised for the server
	 * (for example, because it will be written to a log file) rather than for a
	 * client user.
	 */
	public static final Locale SERVER_LOCALE = Locale.getDefault();

	/**
	 * Format a message, i.e. a CWZBA0123W type utterance.
	 * @param locale The locale to format the message for. May be null, in 
	 * which case the server locale is used, but make sure that an appropriate
	 * user locale is always used for anything returned to a browser.
	 * @param key The message key
	 * @param inserts Data to be inserted into the message.
	 * @return A formatted string.
	 */
	public static String formatMessageWithoutCode(Locale locale, String key, String... inserts)
	{
		if(locale == null) locale = Locale.getDefault();
		
		try
		{
			final ResourceBundle bundle = ResourceBundle.getBundle(Constants.CWZBA_BUNDLE_NAME, locale);
			return new MessageFormat(bundle.getString(key)).format(inserts);
		}
		catch(MissingResourceException e)
		{
			logger.warning("CWZBA0005W_MESSAGE_KEY_NOT_FOUND", key);
			
			if(inserts.length == 0)
			{
				return "["+key+"]";
			}
			
			StringBuilder buffer = new StringBuilder();
			for (String insert : inserts)
			{
				buffer.append(insert+", ");
			}
			String insertList = buffer.toString();
			insertList = insertList.substring(0, insertList.length()-2);
			
			return "["+key+": "+insertList+"]";
		}
	}

	/**
	 * Format a message, i.e. a CWZBA0123W type utterance.
	 * @param locale The locale to format the message for. May be null, in 
	 * which case the server locale is used, but make sure that an appropriate
	 * user locale is always used for anything returned to a browser.
	 * @param key The message key
	 * @param inserts Data to be inserted into the message.
	 * @return A formatted string.
	 */
	public static String formatMessageWithCode(Locale locale, String key, String... inserts)
	{
		if(locale == null) locale = Locale.getDefault();
		
		try
		{
			final ResourceBundle bundle = ResourceBundle.getBundle(Constants.CWZBA_BUNDLE_NAME, locale);
			String message = key.substring(0, 10)+": "+bundle.getString(key);
			return new MessageFormat(message).format(inserts);
		}
		catch(MissingResourceException e)
		{
			logger.warning("CWZBA0005W_MESSAGE_KEY_NOT_FOUND", key);
			StringBuilder buffer = new StringBuilder();
			for (String insert : inserts)
			{
				buffer.append(insert+", ");
			}
			String insertList = buffer.toString();
			insertList = insertList.substring(0, insertList.length()-2);
			
			return "["+key+": "+insertList+"]";
		}
	}
	
	/**
	 * Format a message, i.e. a CWZBA0123W type utterance, in a particular 
	 * user's chosen language.
	 * @param userId The user to format the message for. 
	 * @param key The message key
	 * @param inserts Data to be inserted into the message.
	 * @return A formatted string.
	 */
	public static String formatMessage(String userId, String key, String... inserts)
	{
		return formatMessageWithoutCode(getUserLocale(userId), key, inserts);
	}
	
	/**
	 * Find the preferred locale of a particular user. 
	 * This method is guaranteed always to return a usable locale; if for some
	 * reason the user's locale can't be obtained, it will return the server 
	 * locale as a fallback.
	 * @param userId The ID of the user whose preferred locale we want. The 
	 * special value NLS.CURRENT_REST_USER can be used to perform the lookup
	 * internally, but it is important to note that this <strong>will not work
	 * </strong> on threads which are not servicing a REST request, in 
	 * particular AsyncAction worker threads. 
	 * @return An appropriate locale.
	 */
	/* The intention is for preferred language to be a user-configurable item, 
	 * because what browsers send isn't always what users prefer to read, and 
	 * their preference for WAMC may be different to their preference for the 
	 * Web at large. By centralising all calls for user locale here, we can
	 * easily swap in the preference mechanism when it becomes available. For 
	 * now, everything uses the server default.
	 */
	@edu.umd.cs.findbugs.annotations.SuppressWarnings(
			value=			{"ES_COMPARING_PARAMETER_STRING_WITH_EQ", "DLS_DEAD_LOCAL_STORE"}, 
			justification=	"Comparison: Yup, I meant to do that." +
							"Dead Store: We're not actually using userId yet.")
	public static Locale getUserLocale(String userId)
	{
		// While we're untranslated, we don't want to miss code being written that 
		// erroneously passes null userIds. Without this it would be undetectable.
		if(userId == null) throw new IllegalArgumentException("Null userId");
		
		if(userId == CURRENT_REST_USER) // == is deliberate, we want same object not string equality.
		{
			SecurityContext context = SecurityContext.getContext();
			if(context == null) throw new IllegalArgumentException("Tried to use CURRENT_REST_USER in a non-REST thread.");
			userId = context.getUser();
		}
		
		return Locale.getDefault();
	}
	
	/**
	 * Localise a message (or other string) from the WAMT bundles into the 
	 * preferred locale of the user.
	 * @return A displayable message. If the WAMT message can't be located, the
	 * key and inserts are concatenated nicely and returned.
	 */
	public static String wamtMessageToUserLocale(String userId, String wamtMessageKey, Object... wamtMessageInserts)
	{
		Locale locale = getUserLocale(userId);
		
		// WAMT has two message bundles. Rather than worry about which is which
		// everywhere, simply try both.
		
		String message = Messages.getNonMsgString(wamtMessageKey, locale, wamtMessageInserts);
		if(message.equals("!"+wamtMessageKey+"!")) // Key not found
		{
			message = Messages.getString(wamtMessageKey, locale, wamtMessageInserts);
		}
		if(message.equals("!"+wamtMessageKey+"!")) // Key still not found
		{
			StringBuilder buffer = new StringBuilder("["+wamtMessageKey+": ");
			for (Object insert : wamtMessageInserts)
			{
				buffer.append(insert+", ");
			}
			message = buffer.toString().substring(0, buffer.length()-2)+"]";
		}
		
		return message;
	}
}
