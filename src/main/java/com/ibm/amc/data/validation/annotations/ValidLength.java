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
 * Indicate that a string value's length should lie within a specific range.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ValidLength
{
	// @CLASS-COPYRIGHT@

	int max() default Integer.MAX_VALUE;
	int min() default 0;

	class ValidLengthValidator implements Validator
	{
		public boolean validate(Object value, Annotation constraints)
		{
			if (!(constraints instanceof ValidLength)) throw new IllegalArgumentException();
			if (value == null) return true;
			if (!(value instanceof String)) throw new IllegalArgumentException();

			int length = ((String) value).length();
			ValidLength range = (ValidLength) constraints;
			
			return length >= range.min() && length <= range.max();
		}
	}
}
