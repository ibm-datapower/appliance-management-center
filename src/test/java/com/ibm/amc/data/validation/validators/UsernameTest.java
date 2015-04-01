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

public class UsernameTest
{
	// @CLASS-COPYRIGHT@

	@BeforeClass
	public static void create()
	{
		validator = new UserName();
	}

	private static UserName validator;

	@Test
	public void testValidNames()
	{
		// "\u6C34" is a unicode character which is considered to be a letter (it's the ideograph
		// for "water" in one or more east-Asian languages)
		String[] validUserNames = { "abc", "a", "WaterWater\u6C34\u6C34", "a.b", "dp_42" };
		for (String name : validUserNames)
		{
			assertTrue("Username validator claimed that " + name + " is not a valid username.", validator.validate(name, null));
		}
	}

	@Test
	public void testInvalidNames()
	{
		// "\uD834\uDD1E" is a unicode character that is not a letter (actually it's a musical
		// notation symbol, but most fonts can't render it anyway).
		String[] invalidUserNames = { "<script src\"nastysite.com\">", "\uD834\uDD1E" };
		for (String name : invalidUserNames)
		{
			assertFalse("Username validator claimed that " + name + " is a valid username.", validator.validate(name, null));
		}
	}

	@Test
	public void testNull()
	{
		assertTrue("Validator didn't allow empty values.", validator.validate(null, null));
	}
}
