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

import java.util.Arrays;

import org.junit.BeforeClass;
import org.junit.Test;

public class AppliancePasswordTest
{
	// @CLASS-COPYRIGHT@

	@BeforeClass
	public static void create()
	{
		validator = new AppliancePassword();
	}

	private static AppliancePassword validator;

	@Test
	public void testValidPasswords()
	{

		String[] validPasswords = { "foo", "a", "=-[]#';,./<>?:@~}+_", " ", stringLength(255) };
		for (String password : validPasswords)
		{
			assertTrue("Appliance password validator claimed that " + password + " is not a valid password.", validator.validate(password, null));
		}
	}

	@Test
	public void testInvalidPassword()
	{
		assertFalse("Appliance password validator allowed an excessively long password.", validator.validate(stringLength(256), null));
	}

	@Test
	public void testNull()
	{
		assertTrue("Validator didn't allow empty values.", validator.validate(null, null));
	}

	private String stringLength(int length)
	{

		char[] buffer = new char[length];
		Arrays.fill(buffer, 'a');
		return new String(buffer);
	}
}
