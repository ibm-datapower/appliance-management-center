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
 * Indicate that an accessor method should be validated as a numeric value with
 * a particular range.  
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ValidNumber 
{
	// @CLASS-COPYRIGHT@

	/** The minimum allowed value. Defaults to zero. */
	int min() default 0;
	/** The maximum allowed value. Defaults to Integer.MAX_VALUE. */
	int max() default Integer.MAX_VALUE;
	/** Set to false to allow non-integer values. */
	boolean integer() default true;

	static class NumberValidator implements Validator
	{
		@Override
		public boolean validate(Object value, Annotation constraints)
		{
			if (!(constraints instanceof ValidNumber)) throw new IllegalArgumentException();
			if(value == null) return true;
			if (!Number.class.isAssignableFrom(value.getClass())) throw new IllegalArgumentException();
			
			ValidNumber range = (ValidNumber) constraints;
			Number number = (Number) value;
			
			if(range.integer())
			{
				return number.floatValue() == number.intValue() // Test it's an integer
						&& number.intValue() >= range.min() && number.intValue() <= range.max();
			}
			else
			{
				return number.floatValue() >= range.min() && number.floatValue() <= range.max();
			}
		}
	}
}
