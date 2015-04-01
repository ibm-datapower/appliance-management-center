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

public class FilenameOnApplianceTest
{
	// @CLASS-COPYRIGHT@

	@BeforeClass
	public static void create()
	{
		validator = new FilenameOnAppliance();
	}

	private static FilenameOnAppliance validator;

	@Test
	public void testValidNames()
	{
		String[] validFilenames = { 
				"local:/abc", 
				"cert:/a",
				"store:/a.b", 
				"temporary:/dp_42", 
				"foo:/my-boring_Policy.file_27",
				"bar:/several/directories/down"};
		for (String name : validFilenames)
		{
			assertTrue("Filename validator claimed that [" + name + "] is not a valid filename.", validator.validate(name, null));
		}
	}

	@Test
	public void testInvalidNames()
	{
		String[] invalidFileNames = {
				"no.filesystem",
				"local:/ends/with/slash/",
				"local:/<script src\"nastysite.com\">",  
				"temporary:/UnicodeText\u6C34\u6C34", 
				"cert:/spaces are not allowed", 
				"",
				"foo:/.",
				"bar:/..",
				"baz:/i||egal:ch@r@cte¬s",
				"quux:/foo/.."};
		
		for (String name : invalidFileNames)
		{
			assertFalse("Filename validator claimed that [" + name + "] is a valid Filename.", validator.validate(name, null));
		}
	}

	@Test
	public void testLengths()
	{
		assertFalse("Filename validator allowed a filename longer than 128 characters.", validator.validate(longName(129), null));
		assertTrue("Filename validator rejected a filename 128 characters long, which should be allowed.", validator.validate(longName(128), null));
	}
	
	private String longName(int length)
	{
		String prefix = "local:/";
		char[] buffer = new char[length - prefix.length()];
		Arrays.fill(buffer, 'x');
		return prefix+new String(buffer);
	}

	@Test
	public void testNull()
	{
		assertTrue("Validator didn't allow empty values.", validator.validate(null, null));
	}
}
