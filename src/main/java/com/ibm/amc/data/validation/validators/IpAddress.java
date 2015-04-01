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
package com.ibm.amc.data.validation.validators;

import java.lang.annotation.Annotation;
import java.net.InetAddress;
import java.net.UnknownHostException;

import com.ibm.amc.Constants;
import com.ibm.amc.data.validation.Validator;
import com.ibm.amc.ras.Logger47;

/**
 * A validator that accepts only valid IP addresses (IPv4 or IPv6).
 */
public class IpAddress implements Validator
{
	// @CLASS-COPYRIGHT@

	static Logger47 logger = Logger47.get(IpAddress.class.getCanonicalName(), Constants.CWZBA_BUNDLE_NAME);

	@Override
	public boolean validate(Object value, Annotation constraints)
	{
		if (value == null) return true;
		if (!(value instanceof String)) throw new IllegalArgumentException();
		String address = (String) value;

		if (!address.matches("[0-9a-fA-F:.]+")) return false;

		try
		{
			// Normally this is used to do a DNS lookup but, according to the
			// Javadoc, if you pass in an IP address instead of a hostname, the
			// format is checked but no lookup is done, which is what we want.
			InetAddress.getByName(address);
		}
		catch (UnknownHostException e)
		{
			// Should never happen, because we're not doing a lookup.
			logger.debug("validate", "UnknownHostException while validating an IP address.", e);
			return false;
		}
		return true;
	}
}
