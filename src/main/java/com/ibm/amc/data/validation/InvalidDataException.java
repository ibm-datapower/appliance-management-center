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

import com.ibm.amc.resources.exceptions.AmcIllegalArgumentException;

@SuppressWarnings("serial")
public class InvalidDataException extends AmcIllegalArgumentException
{
	// @CLASS-COPYRIGHT@

	private String fieldName;

	/**
	 * @param fieldName
	 *            The name of the data field that was invalid.
	 */
	public InvalidDataException(String fieldName)
	{
		super ("CWZBA0505E_ERROR_INVALID_DATA", fieldName);
		this.fieldName = fieldName;
	}

	/**
	 * @return the name of the field that was invalid.
	 */
	public String getInvalidFieldName()
	{
		return fieldName;
	}
}
