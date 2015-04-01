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

import java.util.List;

import com.ibm.amc.Constants;
import com.ibm.amc.data.SvrAppliance.ApplianceStatus;
import com.ibm.amc.data.validation.annotations.ValidNumber;
import com.ibm.amc.data.validation.annotations.ValidateNotBlank;
import com.ibm.amc.data.validation.annotations.ValidatedAs;
import com.ibm.amc.data.validation.validators.ApplianceName;
import com.ibm.amc.data.validation.validators.AppliancePassword;
import com.ibm.amc.data.validation.validators.ApplianceType;
import com.ibm.amc.data.validation.validators.HostnameOrIp;
import com.ibm.amc.data.validation.validators.UserName;

/**
 * The REST representation of a device or appliance.
 */
public class Appliance extends AbstractRestData
{
	// No CLASS-COPYRIGHT because we don't want copyright text in ReST responses.

	public String id;
	
	@ValidatedAs(ApplianceName.class)
	@ValidateNotBlank
	@Hashcode
	public String name;
	
	@ValidatedAs(HostnameOrIp.class)
	public String hostName;
	
	@ValidNumber(max = Constants.PORT_NUMBER_MAX)
	public Integer ampPort;
	
	@ValidatedAs(UserName.class)
	public String adminUserId;
	
//	@JsonIgnore
	@ValidatedAs(AppliancePassword.class)
	public String adminUserPassword;
	// Override the JsonIgnore for setting.
	public void setAdminUserPassword(String adminUserPassword)
	{
		this.adminUserPassword = adminUserPassword;
	}

	@ValidatedAs(ApplianceType.class)
	public String applianceType;
	
	public String firmwareLevel;
	
	public String ampVersion;
	
	public String model;
	
	public List<String> featureLicenses;
	
	@ValidNumber(max = Constants.PORT_NUMBER_MAX)
	public int guiPort;
	
	public String firmwareManagementStatus;
	
	@ValidNumber(min = Constants.QUIESCE_TIMEOUT_MIN, max = Constants.QUIESCE_TIMEOUT_MAX)
	public Integer quiesceTimeout;
	
	public ApplianceStatus status;
	
	public String serialNumber;
	
	public List<String> capabilities;
	
	public List<String> groups;
}
