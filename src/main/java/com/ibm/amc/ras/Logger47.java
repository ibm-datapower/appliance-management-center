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
package com.ibm.amc.ras;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JSR047 Logger wrapper
 * 
 * @author mallman
 */
public class Logger47
{
	// @CLASS-COPYRIGHT@

	/* a list of known Logger wrappers using the source class as a key */
	static HashMap<String, Logger47> logger47s = new HashMap<String, Logger47>();

	private static final int MESSAGE_CODE_LENGTH = 10;
	private static final String MESSAGE_CODE_SEPARATOR = ": ";

	/* instance members */
	String sourceClass;
	public Logger logger;
	ResourceBundle resourceBundle;

	// ------------------------------------------------------------------------
	// wrapper construction and access
	// ------------------------------------------------------------------------
	/**
	 * Create a new wrapper for a particular source class. The wrapper remembers its source class
	 * and creates a JSR047 Logger. This class should be used to create loggers that have an
	 * associated ResourceBundle, we globalise everything, right?
	 * 
	 * @param sourceClass
	 *            the name of the class that will use this logger
	 * @param resourceBundleName
	 */
	private Logger47(String sourceClass, String resourceBundleName)
	{
		/* store away the source class, this will be used by many of the log methods */
		this.sourceClass = sourceClass;

		/* create a java.util.logger for this wrapper */
		logger = Logger.getLogger(sourceClass, resourceBundleName);

		/* load the resource bundle for this logger for the default locale */
		resourceBundle = logger.getResourceBundle();
	}

	/**
	 * Obtain a logger for a given source class
	 * 
	 * @param sourceClass
	 *            of the required Logger
	 * @return the existing or newly created Logger for the provided source class
	 */
	public static Logger47 get(String sourceClass, String resourceBundleName)
	{
		Logger47 logger47 = null;

		/* check to see if this wrapper has already been created */
		if (logger47s.containsKey(sourceClass))
		{
			logger47 = logger47s.get(sourceClass);
		}
		else
		{
			/* create a new Logger and stash it */
			logger47 = new Logger47(sourceClass, resourceBundleName);
			logger47s.put(sourceClass, logger47);
		}
		return logger47;
	}

	// ------------------------------------------------------------------------
	// Logging output guards
	//
	// Guards are not provided for audit, warning or error as these are
	// expected to always be output.
	// ------------------------------------------------------------------------
	/**
	 * Determine if debug logging should be processed
	 * 
	 * @return the result of the test
	 */
	public boolean isDebugEnabled()
	{
		return logger.isLoggable(Level.FINEST);
	}

	/**
	 * Determine if entry/exit logging should be processed
	 * 
	 * @return the result of the test
	 */
	public boolean isEntryEnabled()
	{
		return logger.isLoggable(Level.FINER);
	}

	/**
	 * Determine if info logging should be processed
	 * 
	 * @return the result of the test
	 */
	public boolean isInfoEnabled()
	{
		return logger.isLoggable(Level.INFO);
	}

	// ------------------------------------------------------------------------
	// Logging output methods
	// ------------------------------------------------------------------------
	/**
	 * Output level: finest Translatable: no
	 * 
	 * @param sourceMethod
	 *            name of the originating method
	 * @param msg
	 *            a non-translated message
	 */
	public void debug(String sourceMethod, String msg)
	{
		logger.logp(Level.FINEST, sourceClass, sourceMethod, msg);
	}

	/**
	 * Output level: finest Translatable: no
	 * 
	 * @param sourceMethod
	 *            name of the originating method
	 * @param msg
	 *            a non-translated message
	 * @param obj
	 *            object to include in the message
	 */
	public void debug(String sourceMethod, String msg, Object obj)
	{
		logger.logp(Level.FINEST, sourceClass, sourceMethod, msg, obj);
	}

	/**
	 * Output level: finer Translatable: no
	 * 
	 * @param sourceMethod
	 *            name of the originating method
	 * @param params
	 *            array of method entry parameters
	 */
	public void entry(String sourceMethod, Object... params)
	{
		logger.entering(sourceClass, sourceMethod, params);
	}

	/**
	 * Output level: finer Translatable: no
	 * 
	 * @param sourceMethod
	 *            name of the originating method
	 */
	public void exit(String sourceMethod)
	{
		logger.exiting(sourceClass, sourceMethod);
	}

	/**
	 * Output level: finer Translatable: no
	 * 
	 * @param sourceMethod
	 *            name of the originating method
	 * @param result
	 */
	public void exit(String sourceMethod, Object result)
	{
		logger.exiting(sourceClass, sourceMethod, result);
	}

	/**
	 * Info logging should not be used for 'business as usual' messages, but can be used for server
	 * startup or exceptional circumstances that are not warnings or errors.
	 * 
	 * Output level: info Translatable: yes
	 * 
	 * @param msgKey
	 *            message key to use from the G11N bundle
	 * @param inserts
	 *            a list of String inserts to be placed into the message
	 */
	public void info(String msgKey, String... inserts)
	{
		logger.logp(Level.INFO, sourceClass, "", infuse(msgKey, inserts));
	}

	/**
	 * Output level: audit Translatable: yes
	 * 
	 * @param msgKey
	 *            message key to use from the G11N bundle
	 * @param inserts
	 *            a list of String inserts to be placed into the message
	 */
	public void audit(String msgKey, String... inserts)
	{
		logger.logp(Level.INFO, sourceClass, "", infuse(msgKey, inserts));
	}

	/**
	 * Output level: warning Translatable: yes
	 * 
	 * @param msgKey
	 *            message key to use from the G11N bundle
	 * @param inserts
	 *            a list of String inserts to be placed into the message
	 */
	public void warning(String msgKey, String... inserts)
	{
		logger.logp(Level.WARNING, sourceClass, "", infuse(msgKey, inserts));
	}

	/**
	 * Use error conditions where there is no throwable.
	 * 
	 * Output level: severe Translatable: yes
	 * 
	 * @param msgKey
	 *            message key to use from the G11N bundle
	 * @param inserts
	 *            a list of String inserts to be placed into the message
	 */
	public void error(String msgKey, String... inserts)
	{
		logger.logp(Level.SEVERE, sourceClass, "", infuse(msgKey, inserts));
	}
	
	/**
	 * Use when a message has already been localised.
	 * 
	 * Output level: severe Translatable: yes
	 * 
	 * @param message
	 *            The already-translated message.
	 * @param alreadyTranslated True to confirm that the message is already
	 * translated (nefarious purpose is to distinguish this method's signature
	 * from the varargs version with inserts).
	 */
	public void error(String message, boolean alreadyTranslated)
	{
		logger.logp(Level.SEVERE, sourceClass, "", message);
	}

	/**
	 * Use within exception handling logic.
	 * 
	 * Output level: severe Translatable: yes
	 * 
	 * @param msgKey
	 *            message key to use from the G11N bundle
	 * @param inserts
	 *            a list of String inserts to be placed into the message
	 * @param thrown
	 *            throwable causing this message to be logged
	 */
	public void error(String msgKey, Throwable thrown, String... inserts)
	{
		logger.logp(Level.SEVERE, sourceClass, "", infuse(msgKey, inserts), thrown);
	}
	
	/**
	 * Log an exception's stacktrace. This is an explicit log call to prevent
	 * the logs being filled with the same stacktrace multiple times as the
	 * exception percolates up the stack. It is logged at the INFO level so that
	 * it does not appear in console.log, which is a higher-level record of 
	 * server activity not to be polluted with dump data.
	 */
	public void stacktrace(Throwable exception)
	{
		logger.logp(Level.INFO, sourceClass, "", getStackTrace(exception));
	}

	// ------------------------------------------------------------------------
	// private helpers
	// ------------------------------------------------------------------------
	/**
	 * Infuse a message from a resource bundle with a list of inserts.
	 * 
	 * @param msgKey
	 *            key of the message to grab in the resource bundle
	 * @param inserts
	 *            an array of string inserts
	 * @return the infused message
	 */
	private String infuse(String msgKey, String[] inserts)
	{
		String message;

		try
		{
			/* grab the message from the bundle */
			message = resourceBundle.getString(msgKey);

			/* prepend message code */
			StringBuffer buffer = new StringBuffer(MESSAGE_CODE_LENGTH + MESSAGE_CODE_SEPARATOR.length() + message.length());
			buffer.append(getMessageCode(msgKey));
			buffer.append(MESSAGE_CODE_SEPARATOR);
			buffer.append(message);
			message = buffer.toString();
		}
		catch (MissingResourceException e)
		{
			/* if we don't find the key, use it literally */
			message = msgKey;
		}

		/* shove in the inserts */
		message = new MessageFormat(message).format(inserts);

		return message;
	}

	static public String getMessageCode(final String messageKey)
	{
		return messageKey.substring(0, MESSAGE_CODE_LENGTH);
	}

	String getStackTrace(Throwable e)
	{
		StringWriter buffer = new StringWriter();
		e.printStackTrace(new PrintWriter(buffer));
		return buffer.toString();
	}
}
