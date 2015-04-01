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
import com.ibm.amc.data.validation.Validator;

/**
 * A validator that accepts only valid hostnames. The definition of validity is 
 * taken from http://en.wikipedia.org/w/index.php?title=Hostname&oldid=461907048#Restrictions_on_valid_host_names
 * , permitting the technically-invalid Windowsisms mentioned there.
 */
public class Hostname implements Validator 
{
	// @CLASS-COPYRIGHT@

	@Override
	public boolean validate(Object value, Annotation constraints) 
	{
		if(value == null) return true;
		if(!(value instanceof String)) throw new IllegalArgumentException();
		String hostname = (String) value;
		
		if(hostname.length() > 255) return false;
		if(hostname.startsWith(".") || hostname.endsWith(".")) return false;
		
		String[] labels = hostname.split("\\.");
		if(labels.length < 1) return false;
		for (String label : labels) {
			if(label.length() > 63) return false;
			if(!label.matches("[a-zA-Z0-9\\-_]+")) return false;
			if(label.startsWith("-") || label.endsWith("-")) return false;
		}
		return true;
	}
}
