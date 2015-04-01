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
package com.ibm.amc.resources.data;

import com.ibm.amc.data.validation.annotations.ValidateNotBlank;

/**
 * The REST representation of a request to deploy firmware.
 */
public class FirmwareDeployRequest extends AbstractRestData
{
	// No CLASS-COPYRIGHT because we don't want copyright text in ReST responses.

	@ValidateNotBlank
	public String firmwarePrimaryKey;
	
	@ValidateNotBlank
	public boolean licenceAccepted;
}
