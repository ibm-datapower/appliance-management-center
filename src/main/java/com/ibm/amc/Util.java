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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ibm.amc.ras.Logger47;
import com.ibm.amc.resources.data.AbstractRestData;
import com.ibm.amc.resources.data.HasRest;
import com.ibm.datapower.amt.StringCollection;

/**
 * Container class for self-contained utility methods. Any method added to this class should be
 * static and have no side-effects beyond its arguments and return value.
 */
public class Util
{
	// @CLASS-COPYRIGHT@

	static Logger47 logger = Logger47.get(Util.class.getCanonicalName(), Constants.CWZBA_BUNDLE_NAME);

	/**
	 * Copy the contents of WAMT's "StringCollection" type into an actual Java Collection.
	 * 
	 * @return A list of the strings from the StringCollection, or an empty list if a null
	 *         StringCollection is given.
	 */
	public static List<String> asList(final StringCollection strings)
	{
		ArrayList<String> list = new ArrayList<String>();
		if (strings == null) return list;

		int size = strings.size();
		for (int i = 0; i < size; i++)
		{
			list.add(strings.get(i));
		}
		return list;
	}

	/**
	 * Take a list of server-side objects, invoke each one's toRest method, and return the results
	 * in a new list.
	 * 
	 * @param <T>
	 *            The expected type of the ReST objects (eg Domain, Appliance, etc). This is a
	 *            return type parameter, so you don't specify it directly. The compiler infers it
	 *            from what you try to assign the result to. Don't be scared, it generally just
	 *            works :-).
	 * @param svrItems
	 *            A list of objects that implement the HasRest interface, ie have a toRest() method
	 *            that returns an AbstractRestData representing the parts of themselves they wish to
	 *            expose via ReST.
	 * @return A list of the ReST objects (of type T) obtained from the input objects.
	 */
	/*
	 * An alternative way to provide this functionality (prototyped and rejected) would be to
	 * implement the AbstractListDecorator from commons.collections. This would avoid copying into a
	 * second list, instead maintaining a single backing list of server items and calling toRest on
	 * the fly whenever accessor methods on the list were called. Rejected as overcomplicated and
	 * not helping much with performance given the likely use patterns.
	 */
	@SuppressWarnings("unchecked")
	// Can't avoid doing an unsafe cast, but the situation is unlikely.
	public static <T> List<T> listAsRest(List<? extends HasRest> svrItems)
	{
		List<T> result = new ArrayList<T>(svrItems.size());

		for (HasRest item : svrItems)
		{
			AbstractRestData rest = item.toRest();
			try
			{
				result.add((T) rest);
			}
			catch (ClassCastException e)
			{
				throw new AmcRuntimeException(new RuntimeException("Incorrect cast in Util.listAsRest: " + e.getMessage() + "\n" + "The type you declare for the result list must match what "
						+ "is returned from the input objects' toRest methods."));
			}
		}
		return result;
	}

	/**
	 * Log a list of String inserts via the provided logger
	 * 
	 * @param inserts
	 *            the list of inserts to log
	 */
	public static void logInserts(Logger47 logger, String... inserts)
	{
		if (logger.isDebugEnabled() && inserts != null)
		{
			for (String insert : inserts)
			{
				logger.debug("Insert:", insert);
			}
		}
	}

	/**
	 * <p>
	 * If e is a WAMT exception, extract its error code. Some WAMT exceptions (for example,
	 * AMPException) don't expose the error code except as part of their message. This method
	 * provides a centralised place to parse that message string as cleanly as possible.
	 * </p>
	 * <p>
	 * Because this method returns an empty string if no code is found, it is always safe to do
	 * <code>if(Util.getWamtExceptionCode(ex).equals("WAMT0001")</code> - this will simply return
	 * false if ex is not the exception you're looking for.
	 * </p>
	 * <p>
	 * Note that the error codes returned by this method do not include the severity character (the
	 * "E", "I", etc at the end of the code). In theory at least, those might change, while the
	 * numeric code should remain constant for all time.
	 * </p>
	 * 
	 * @param e
	 *            The exception to get the code for.
	 * @return The error code (for example WAMT0805) if present, or the empty string if none found
	 *         (including because e was null or not a WAMT exception).
	 */
	public static String getWamtExceptionCode(Throwable e)
	{
		if (e == null) return "";

		String message = e.getMessage();
		if (message == null) return "";

		Matcher matcher = pattern.matcher(message);
		if (matcher.find())
		{
			return matcher.group(1);
		}
		else
			return "";
	}

	private static Pattern pattern = Pattern.compile("^(WAMT\\d\\d\\d\\d)[EIWS]:");

	/**
	 * Truncates a string at the given length, outputting an information message if it is truncated.
	 * 
	 * @param in
	 *            the input string
	 * @param length
	 *            the maximum length
	 * @return the input string truncated at the maximum length
	 */
	public static String truncateString(final String in, final int length)
	{
		final String out;
		if (in == null || in.length() <= length)
		{
			out = in;
		}
		else
		{
			out = in.substring(0, length);
			if (logger.isInfoEnabled()) logger.info("CWZBA1035I_TRUNCATED_STRING", in, out);
		}
		return out;
	}

	/**
	 * Returns a list of strings from the given array, each truncated at the given length.
	 * 
	 * @param in
	 *            an array of strings
	 * @param length
	 *            the maximum length
	 * @return a list of strings from the original array truncated at the maximum string length
	 */
	public static List<String> arrayToTruncatedList(final String[] in, final int length)
	{
		if (in == null) return Collections.emptyList();
		final List<String> out = new ArrayList<String>(in.length);
		for (String s : in)
		{
			out.add(truncateString(s, length));
		}
		return out;
	}

}
