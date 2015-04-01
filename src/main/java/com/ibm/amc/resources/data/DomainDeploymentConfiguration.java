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

import com.ibm.amc.data.validation.annotations.ValidUri;
import com.ibm.amc.data.validation.annotations.ValidateNotBlank;

/**
 * The information necessary to do a domain deployment, encapsulating both the
 * new configuration and (optionally) the policy that will modify it during
 * deployment.
 */
public class DomainDeploymentConfiguration extends AbstractRestData
{
	/**
	 * The location of a deployment policy that will be applied to modify the
	 * domain configuration as it is applied. Optional. May be an HTTP(S) URL
	 * for a remotely-located policy, or a wamctmp: URL representing a file
	 * uploaded to WAMC in a preceding ReST request. 
	 */
	@ValidUri
	public URI deploymentPolicyLocation;

	/**
	 * The location of the domain configuration. May be an HTTP(S) URL
	 * for a remotely-located policy, a wamctmp: URL representing a file
	 * uploaded to WAMC in a preceding ReST request, or a URL like
	 * device://deviceHost/domainName indicating that config should be copied
	 * from another domain.
	 */
	@ValidUri
	@ValidateNotBlank
	public URI sourceConfigLocation;

	/**
	 * If a deployment policy is provided, domainName and policyName indicate
	 * where it is to be found within the file bundle downloaded from 
	 * deploymentPolicyLocation. The type of file provided is a backup or export
	 * of a whole appliance, so it is necessary to specify which domain on the 
	 * appliance and which policy in the domain is to be used.
	 */
	public String policyName;

	/**
	 * If a deployment policy is provided, domainName and policyName indicate
	 * where it is to be found within the file bundle downloaded from 
	 * deploymentPolicyLocation. The type of file provided is a backup or export
	 * of a whole appliance, so it is necessary to specify which domain on the 
	 * appliance and which policy in the domain is to be used.
	 */
	public String domainName;
	
	
	public Boolean automaticSynchronization;
}
