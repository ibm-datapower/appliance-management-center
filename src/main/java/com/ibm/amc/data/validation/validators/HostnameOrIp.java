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
 * A validator that accepts only valid hostnames or IP addresses. The definition 
 * of validity for hostnames is taken from 
 * http://en.wikipedia.org/w/index.php?title=Hostname&oldid=461907048#Restrictions_on_valid_host_names
 * , permitting the technically-invalid Windowsisms mentioned there.
 */
public class HostnameOrIp implements Validator 
{
	// @CLASS-COPYRIGHT@

	@Override
	public boolean validate(Object value, Annotation constraints) 
	{
		return 
			new Hostname().validate(value, constraints)
			||
			new IpAddress().validate(value, constraints);
	}

	

}
