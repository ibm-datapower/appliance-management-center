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
package com.ibm.amc.data.validation.annotations;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.ibm.amc.data.validation.Validator;

/**
 * Indicate that a value should be validated against a specific list of characters. The parameter to
 * this annotation is an array of the valid characters, expressed either literally as strings, or
 * using one of the constants in this class.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ValidCharacters
{
	// @CLASS-COPYRIGHT@

	/** Mixed-case alphanumerics and the underscore character */
	// Random-looking order is frequency of use in English, so on scanning
	// through we find more likely letters earlier. Premature optimisation, moi?
	public static final String ASCII_WORD_CHARS = "eEtTaAoOiInNsShHrRdDlLcCuUmMwWfFgGyYpPbBvVkKjJxXqQzZ0123456789_";
	// Note that we're probably going to need to support a non-ascii equivalent
	// of the above, eg using the Character.isLetterOrDigit method.

	/** Digits 0 - 9 */
	public static final String DIGITS = "0123456789";

	String[] value();

	class ValidCharacterValidator implements Validator
	{
		public boolean validate(Object value, Annotation constraints)
		{
			if (!(constraints instanceof ValidCharacters)) throw new IllegalArgumentException();
			if (value == null) return true;
			if (!(value instanceof String)) throw new IllegalArgumentException();

			// Assemble a unified array of permitted characters.
			String[] specifiedCharacterSets = ((ValidCharacters) constraints).value();
			StringBuilder buffer = new StringBuilder();
			for (String set : specifiedCharacterSets)
			{
				buffer.append(set);
			}
			char[] allowedCharacters = buffer.toString().toCharArray();

			// Express the value to be checked as an array of characters.
			char[] text = ((String) value).toCharArray();

			// Compare the two.
			nextChar: for (char letter : text)
			{
				for (char c : allowedCharacters)
				{
					if (letter == c) continue nextChar;
				}
				return false;
			}

			return true;
		}
	}
}
