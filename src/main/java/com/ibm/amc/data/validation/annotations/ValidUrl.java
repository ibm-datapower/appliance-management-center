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
import java.net.URL;

import com.ibm.amc.data.validation.Validator;

/**
 * Indicate that a URL is of a supported protocol.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ValidUrl
{
	// @CLASS-COPYRIGHT@

	String[] protocols() default {};

	class ValidUrlValidator implements Validator
	{
		public boolean validate(Object value, Annotation constraints)
		{
			if (!(constraints instanceof ValidUrl)) throw new IllegalArgumentException();
			if (value == null) return true;
			if (!(value instanceof URL)) throw new IllegalArgumentException();
			
			URL url = (URL)value;
			for (String scheme: ((ValidUrl) constraints).protocols())
			{
				if (scheme.equals(url.getProtocol())) return true;
			}
			return false;
		}
	}
}
