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

import java.lang.annotation.Annotation;
import java.util.Arrays;

import com.ibm.amc.data.validation.Validator;

/**
 * A validator that accepts only valid usernames. Currently this means it 
 * consists of letters and digits (in any character set) plus an arbitrary (not
 * exhaustive) set of symbols. 
 */
public class UserName implements Validator 
{
	// @CLASS-COPYRIGHT@
	
	private static final char[] allowedSymbols = {'.', '_', ' ', '-', '+', '=', '@', '/', '#', '[', ']', '{', '}', '(', ')', '!', '&'};
	static { Arrays.sort(allowedSymbols);}
	
	@Override
	public boolean validate(Object value, Annotation constraints) 
	{
		if(value == null) return true;
		if(!(value instanceof String)) throw new IllegalArgumentException();
		
		char[] name = ((String) value).toCharArray();
		if(name.length > 255) return false;
		nextChar: for (char c : name) 
		{
			if(Character.isLetterOrDigit(c)) continue nextChar;
			if(isAllowed(c)) continue nextChar;
			return false;
		}
		
		return true;
	}

	private boolean isAllowed(char c) 
	{
		return Arrays.binarySearch(allowedSymbols, c) >= 0;
	}
}
