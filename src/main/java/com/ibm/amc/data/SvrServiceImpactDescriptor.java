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
package com.ibm.amc.data;

import java.util.List;

import com.ibm.amc.Constants;
import com.ibm.amc.ras.Logger47;
import com.ibm.amc.resources.data.HasRest;
import com.ibm.amc.resources.data.ServiceImpactDescriptor;
import com.ibm.amc.resources.data.ServiceImpactDescriptor.ObjectDescriptor;

public abstract class SvrServiceImpactDescriptor implements HasRest
{
	// @CLASS-COPYRIGHT@

	@edu.umd.cs.findbugs.annotations.SuppressWarnings(justification = "Findbugs wants this to be package-protected, but subclasses outside the package use it.", value = "MS")
	protected static Logger47 logger = Logger47.get(SvrServiceImpactDescriptor.class.getCanonicalName(), Constants.CWZBA_BUNDLE_NAME);

	public ServiceImpactDescriptor toRest()
	{
		if (logger.isEntryEnabled()) logger.entry("toRest");

		ServiceImpactDescriptor result = new ServiceImpactDescriptor();
		result.applianceName = getApplianceName();
		result.domainName = getDomainName();
		result.type = getClassDisplayName();
		result.name = getName();
		result.files = getFiles();
		result.objects = getObjects();

		if (logger.isEntryEnabled()) logger.exit("toRest", result);
		return result;
	}
	
	public abstract String getApplianceName();
	
	public abstract String getDomainName();

	public abstract String getClassDisplayName();

	public abstract String getName();
	
	public abstract List<String> getFiles();
	
	public abstract List<ObjectDescriptor> getObjects();
}
