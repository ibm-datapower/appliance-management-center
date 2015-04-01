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

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

public class IpAddressTest
{
	// @CLASS-COPYRIGHT@

	@BeforeClass
	public static void create()
	{
		validator = new IpAddress();
	}

	private static IpAddress validator;

	@Test
	public void testSimpleValidAddress()
	{
		assertTrue("IP address validator said that 127.0.0.1 wasn't a valid address.", validator.validate("127.0.0.1", null));
	}

	@Test
	public void testValidIpv6Addresses()
	{
		assertTrue("IP address validator said that 1080:0:0:0:8:800:200C:417A wasn't a valid address.", validator.validate("1080:0:0:0:8:800:200C:417A", null));
		assertTrue("IP address validator said that 1080::8:800:200C:417A wasn't a valid address.", validator.validate("1080::8:800:200C:417A", null));
		assertTrue("IP address validator said that ::FFFF:129.144.52.38 wasn't a valid address.", validator.validate("::FFFF:129.144.52.38", null));
	}

	@Test
	public void testInvalidAddresses()
	{
		assertFalse("IP address validator said that cheese was a valid address.", validator.validate("cheese", null));
		assertFalse("IP address validator said that hursley.ibm.com was a valid *IP* address.", validator.validate("hursley.ibm.com", null));
		assertFalse("IP address validator said that 'a' was a valid address.", validator.validate("a", null));
		assertFalse("IP address validator said that 192.168.23.45.73 was a valid address.", validator.validate("192.168.23.45.73", null));
		assertFalse("IP address validator said that 427.168.23.45 was a valid address.", validator.validate("427.168.23.45.73", null));
		assertFalse("IP address validator said that 1080::8:800::200C:417A was a valid address.", validator.validate("1080::8:800::200C:417A", null));
	}

	@Test
	public void testNull()
	{
		assertTrue("Validator didn't allow empty values.", validator.validate(null, null));
	}
}
