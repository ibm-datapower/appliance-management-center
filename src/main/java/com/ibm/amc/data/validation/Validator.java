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

import java.lang.annotation.Annotation;

/**
 * An interface which is implemented by things which can validate that a value
 * conforms to a rule.
 */
public interface Validator 
{
	// @CLASS-COPYRIGHT@

	/**
	 * Test whether a given value is valid according to the rules of this 
	 * validator.
	 * 
	 * @param value The value to test
	 * @param constraints An annotation instance, generally obtained by calling
	 * the getAnnotation method on a Method object, which adds some constraints
	 * to the validator. For example, the NumberValidator accepts a ValidNumber
	 * annotation that specifies the maximum and minimum allowable values. 
	 * <strong>Validators specified by the ValidatedAs annotation don't use 
	 * this.</strong>
	 * 
	 * @return true if the value is considered valid, otherwise false.
	 */
	public abstract boolean validate(Object value, Annotation constraints);

}
