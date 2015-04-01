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

public class HostnameTest
{
	// @CLASS-COPYRIGHT@

	@BeforeClass
	public static void create()
	{
		validator = new Hostname();
	}

	private static Hostname validator;

	@Test
	public void testSimpleValidHostname()
	{
		assertTrue("Hostname validator said that w3.hursley.ibm.com wasn't a valid hostname.", validator.validate("w3.hursley.ibm.com", null));
	}

	@Test
	public void testOneWordHostname()
	{
		assertTrue("Hostname validator said that busybox was not a valid hostname.", validator.validate("busybox", null));
	}

	@Test
	public void testMaxLengthHostname()
	{
		String longSegment = stringLength(63);
		String longName = longSegment + "." + longSegment + "." + longSegment + "." + longSegment;
		assertTrue("Hostname validator didn't permit a long (but valid) hostname.", validator.validate(longName, null));
	}

	@Test
	public void testTooLongHostname()
	{
		String longSegment = stringLength(63);
		String longName = longSegment + "." + longSegment + "." + longSegment + "." + longSegment + "b";
		assertFalse("Hostname validator allowed an excessively long hostname.", validator.validate(longName, null));
	}

	@Test
	public void testTooLongSegment()
	{
		String longSegment = stringLength(65);
		String longName = longSegment + "." + longSegment;
		assertFalse("Hostname validator allowed a hostname with an excessively long segment.", validator.validate(longName, null));
	}

	@Test
	public void testDotAsHostname()
	{
		assertFalse("Hostname validator allowed a dot as a hostname.", validator.validate(".", null));
	}

	@Test
	public void testHostnameEndingWithDot()
	{
		assertFalse("Hostname validator allowed a dot at the end of a hostname.", validator.validate("foo.bar.baz.", null));
	}

	@Test
	public void testSegmentStartingWithHyphen()
	{
		assertFalse("Hostname validator allowed a hyphen at the start of a hostname segment.", validator.validate("foo.-bar.baz", null));
	}

	@Test
	public void testInvalidCharacters()
	{
		assertFalse("Hostname validator allowed a * in a hostname.", validator.validate("this.is.wrong.*", null));
		assertFalse("Hostname validator allowed a # in a hostname.", validator.validate("this.is.wrong#", null));
		assertFalse("Hostname validator allowed a @ in a hostname.", validator.validate("this.is.wr@ng", null));
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
