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
 * Indicate that an accessor method should be validated to ensure it is not
 * the empty string or null. This validator is intended for String data; if
 * used on anything else, only the null check is performed. 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ValidateNotBlank 
{
	// @CLASS-COPYRIGHT@

	static class NotBlankValidator implements Validator
	{
		@Override
		public boolean validate(Object value, Annotation constraints)
		{
			if(value == null) return false;
			if (!(value instanceof String)) return true;
			
			return ! value.equals("");
		}
	}
}
