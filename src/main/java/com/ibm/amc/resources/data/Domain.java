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

import com.ibm.amc.data.SvrDomain.DomainStatus;
import com.ibm.amc.data.validation.annotations.ValidLength;
import com.ibm.amc.data.validation.annotations.ValidatedAs;
import com.ibm.amc.data.validation.validators.ApplianceName;
import com.ibm.amc.data.validation.validators.URI;

public class Domain extends AbstractRestData
{
	// No CLASS-COPYRIGHT because we don't want copyright text in ReST responses.

	@ValidLength(max = 40)
	public String name;

	@ValidLength(max = 40)
	public String displayName;

	@ValidatedAs(ApplianceName.class)
	public String applianceName;
	
	public String applianceId;

	@ValidatedAs(URI.class)
	public String deploymentPolicy;

	public String primaryKey;

	// Any number is valid
	public Integer quiesceTimeout;

	@ValidatedAs(URI.class)
	public String sourceConfigurationUrl;
	
	@ValidatedAs(URI.class)
	public String deploymentPolicyUrl;
	
	public String deploymentPolicyDomainName;
	
	public String deploymentPolicyObjectName;
	
	public Boolean automaticSynchronization;
	
	public DomainStatus status;
	
	public List<String> groups;
}
