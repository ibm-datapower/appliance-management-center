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
package com.ibm.amc.data.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.lang.annotation.Annotation;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.ibm.amc.data.validation.annotations.ValidCharacters;
import com.ibm.amc.data.validation.annotations.ValidLength;
import com.ibm.amc.data.validation.annotations.ValidNumber;
import com.ibm.amc.data.validation.annotations.ValidRegex;
import com.ibm.amc.data.validation.annotations.ValidUri;
import com.ibm.amc.data.validation.annotations.ValidateNotBlank;
import com.ibm.amc.data.validation.annotations.ValidatedAs;
import com.ibm.amc.resources.data.AbstractRestData;
import com.ibm.amc.resources.exceptions.AmcIllegalArgumentException;

@SuppressWarnings("unused")
// None of the dummy getters are called directly.
public class ValidationEngineTest
{
	// @CLASS-COPYRIGHT@

	// Avoid the need for a hashCode method in every test.
	static class DummyResource extends AbstractRestData
	{
	}

	static abstract class SelfValidatingDummyResource extends AbstractRestData implements SelfValidating
	{
	}

	// Incantation that enables tests to specify that an exception must be thrown.
	@Rule
	public ExpectedException exception = ExpectedException.none();

	@BeforeClass
	public static void create()
	{
		engine = ValidationEngine.getInstance();
	}

	private static ValidationEngine engine;

	@Test
	public void testValidNumber() throws InvalidDataException
	{
		engine.validate(new DummyResource()
		{
			@ValidNumber(max = 10)
			public int foo = 8;
		});
	}

	@Test
	public void testTooBigNumber() throws InvalidDataException
	{
		exception.expect(InvalidDataException.class);
		engine.validate(new DummyResource()
		{
			@ValidNumber(max = 10)
			public int foo = 12;
		});
	}

	@Test
	public void testTooSmallNumber() throws InvalidDataException
	{
		exception.expect(InvalidDataException.class);
		engine.validate(new DummyResource()
		{
			@ValidNumber(max = 10)
			public int foo = -3;
		});
	}

	@Test
	public void testValidNegativeNumber() throws InvalidDataException
	{
		engine.validate(new DummyResource()
		{
			@ValidNumber(max = 10, min = -10)
			public int foo = -3;
		});
	}

	@Test
	public void testNonIntegerNumber() throws InvalidDataException
	{
		exception.expect(InvalidDataException.class);
		engine.validate(new DummyResource()
		{
			@ValidNumber(max = 10, min = -10)
			public double foo = -2.3;
		});
	}

	@Test
	public void testValidNonIntegerNumber() throws InvalidDataException
	{
		engine.validate(new DummyResource()
		{
			@ValidNumber(max = 10, integer = false)
			public double foo = 2.3;
		});
	}

	@Test
	public void testZeroIsValid() throws InvalidDataException
	{
		engine.validate(new DummyResource()
		{
			@ValidNumber(max = 10)
			public double foo = 0;
		});
	}

	@Test
	public void testStringValidChars() throws InvalidDataException
	{
		engine.validate(new DummyResource()
		{
			@ValidCharacters({ "A", "B" })
			public String foo = "ABBA";
		});
	}

	@Test
	public void testStringInvalidChars() throws InvalidDataException
	{
		exception.expect(InvalidDataException.class);
		engine.validate(new DummyResource()
		{
			@ValidCharacters({ "A", "B" })
			public String foo = "No more ABBA!";
		});
	}

	@Test
	public void testStringWordChars() throws InvalidDataException
	{
		engine.validate(new DummyResource()
		{
			@ValidCharacters({ ValidCharacters.ASCII_WORD_CHARS, "!", " " })
			public String foo = "No more ABBA!";
		});
	}

	@Test
	public void testStringWordCharsOnly() throws InvalidDataException
	{
		exception.expect(InvalidDataException.class);
		engine.validate(new DummyResource()
		{
			@ValidCharacters(ValidCharacters.ASCII_WORD_CHARS)
			public String foo = "No more ABBA!";
		});
	}

	@Test
	public void testValidRegex() throws InvalidDataException
	{
		engine.validate(new DummyResource()
		{
			@ValidRegex("H\\w+ [a-z]+@*!")
			public String foo = "Hello world!";
		});
	}

	@Test
	public void testInvalidRegex() throws InvalidDataException
	{
		exception.expect(InvalidDataException.class);
		engine.validate(new DummyResource()
		{
			@ValidRegex("H\\w+ [a-z]+@*!")
			public String foo = "Cheese!";
		});
	}

	@Test
	public void testFieldNameInException()
	{
		try
		{
			engine.validate(new DummyResource()
			{
				@ValidNumber(max = 3)
				public int someNumber = 5;
			});
			fail("No exception when numeric data out of range.");
		}
		catch (InvalidDataException e)
		{
			assertEquals("Wrong field name in InvalidDataException", "someNumber", e.getInvalidFieldName());
		}
	}

	@Test
	public void testNotBlank() throws InvalidDataException
	{
		engine.validate(new DummyResource()
		{
			@ValidCharacters(ValidCharacters.ASCII_WORD_CHARS)
			@ValidateNotBlank
			public String someString = "Hello";
		});

		exception.expect(InvalidDataException.class);
		engine.validate(new DummyResource()
		{
			@ValidCharacters(ValidCharacters.ASCII_WORD_CHARS)
			@ValidateNotBlank
			public String someString = "";
		});
	}

	@Test
	public void testNotNull() throws InvalidDataException
	{
		exception.expect(InvalidDataException.class);
		engine.validate(new DummyResource()
		{
			@ValidateNotBlank
			public String someString = null;
		});
	}

	@Test
	public void testTooShort() throws InvalidDataException
	{
		exception.expect(InvalidDataException.class);
		engine.validate(new DummyResource()
		{
			@ValidLength(min = 10)
			public String someString = "123456789";
		});
	}

	@Test
	public void testTooLong() throws InvalidDataException
	{
		exception.expect(InvalidDataException.class);
		engine.validate(new DummyResource()
		{
			@ValidLength(max = 5)
			public String someString = "123456";
		});
	}

	@Test
	public void testWithinRange() throws InvalidDataException
	{
		engine.validate(new DummyResource()
		{
			@ValidLength(min = 5, max = 8)
			public String someString = "123456";
		});
	}

	@Test
	public void testOutsideRange() throws InvalidDataException
	{
		exception.expect(InvalidDataException.class);
		engine.validate(new DummyResource()
		{
			@ValidLength(min = 5, max = 8)
			public String someString = "123";
		});
	}

	@Test
	public void testNullOk() throws InvalidDataException
	{
		engine.validate(new DummyResource()
		{
			@ValidLength(min = 5, max = 8)
			public String someString = null;
		});
	}

	@Test
	public void testUri() throws AmcIllegalArgumentException, URISyntaxException
	{
		engine.validate(new DummyResource()
		{
			@ValidUri(schemes = { "foo", "bar" })
			public URI uri = new URI("foo://ish");
		});
	}

	@Test(expected = AmcIllegalArgumentException.class)
	public void testInvalidUri() throws AmcIllegalArgumentException, URISyntaxException
	{
		engine.validate(new DummyResource()
		{
			@ValidUri(schemes = { "foo", "bar" })
			public URI uri = new URI("moo://ish");
		});
	}

	@Test
	public void testSelfValidating()
	{
		engine.validate(new SelfValidatingDummyResource()
		{

			@Override
			public void validate() throws AmcIllegalArgumentException
			{
			}
		});
	}

	@Test(expected = AmcIllegalArgumentException.class)
	public void testSelfValidatingFails()
	{
		engine.validate(new SelfValidatingDummyResource()
		{

			@Override
			public void validate() throws AmcIllegalArgumentException
			{
				throw new AmcIllegalArgumentException("MESSAGE_KEY");
			}
		});
	}

	// Full testing of individual validators should be in their own test cases;
	// this just tests that the mechanism for calling them works.

	@Test
	public void testExternalValidatorValid() throws InvalidDataException
	{
		engine.validate(new DummyResource()
		{
			@ValidatedAs(YesMan.class)
			public String foo = "YES";
		});
	}

	@Test
	public void testExternalValidatorInvalid() throws InvalidDataException
	{
		exception.expect(InvalidDataException.class);
		engine.validate(new DummyResource()
		{
			@ValidatedAs(YesMan.class)
			public String foo = "Well, maybe";
		});
	}
}

/**
 * Dummy validator for things that must only say Yes.
 */
class YesMan implements Validator
{
	@Override
	public boolean validate(Object value, Annotation constraints)
	{
		return ((String) value).equalsIgnoreCase("yes");
	}
}
