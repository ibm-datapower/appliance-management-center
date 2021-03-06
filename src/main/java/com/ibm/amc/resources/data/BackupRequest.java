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

import java.net.URI;

import com.ibm.amc.data.validation.SelfValidating;
import com.ibm.amc.data.validation.annotations.ValidUri;
import com.ibm.amc.data.validation.annotations.ValidateNotBlank;
import com.ibm.amc.resources.exceptions.AmcIllegalArgumentException;

/**
 * The REST representation of a backup request.
 */
public class BackupRequest extends AbstractRestData implements SelfValidating
{
	// No CLASS-COPYRIGHT because we don't want copyright text in ReST responses.

	public String certificateName;
	@ValidUri(schemes = { "http", "https", "wamctmp" })
	public URI certificateLocation;
	@ValidateNotBlank
	@ValidUri(schemes = { "local", "temporary", "file", "ftp" })
	@Hashcode
	public URI backupDestination;
	public Boolean includeRaid = Boolean.FALSE;
	public Boolean includeIscsi = Boolean.FALSE;

	@Override
	public void validate() throws AmcIllegalArgumentException
	{
		if (((certificateLocation == null) && (certificateName == null)) || ((certificateLocation != null) && (certificateName != null)))
		{
			throw new AmcIllegalArgumentException("CWZBA0519E_ONE_AND_ONLY_ONE_PROPERTY", "certificateLocation", "certificateName");
		}
	}

}
