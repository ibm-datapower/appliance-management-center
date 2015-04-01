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

import java.io.File;
import java.net.URI;
import java.util.List;

import com.ibm.amc.Constants;
import com.ibm.amc.ras.Logger47;
import com.ibm.amc.resources.data.Domain;
import com.ibm.amc.resources.data.DomainDeploymentConfiguration;
import com.ibm.amc.resources.data.HasRest;
import com.ibm.amc.resources.data.ServiceDeploymentConfiguration;

public abstract class SvrDomain implements HasRest
{
	// @CLASS-COPYRIGHT@

	@edu.umd.cs.findbugs.annotations.SuppressWarnings(justification="Findbugs wants this to be package-protected, but subclasses outside the package use it.", value="MS")
	protected static Logger47 logger = Logger47.get(SvrDomain.class.getCanonicalName(), Constants.CWZBA_BUNDLE_NAME);
	
	public Domain toRest()
	{
		if(logger.isEntryEnabled()) logger.entry("toRest");
		
		Domain result = new Domain();
		
		result.primaryKey = this.getPrimaryKey();
		result.name = this.getName();
		result.displayName = this.getName();
		result.applianceName = this.getApplianceName();
		result.applianceId = this.getApplianceId();
		result.deploymentPolicy = this.getDeploymentPolicy();
		result.quiesceTimeout = this.getQuiesceTimeout();
		result.sourceConfigurationUrl = this.getSourceConfigurationUrl();
		result.automaticSynchronization = 
				this.getSynchronizationMode() == SynchronizationMode.AUTO;
		result.status = this.getStatus();
		result.deploymentPolicyUrl = this.getDeploymentPolicyUrl();
		result.deploymentPolicyDomainName = this.getDeploymentPolicyDomainName();
		result.deploymentPolicyObjectName = this.getDeploymentPolicyObjectName();
		result.groups = this.getGroups();
		
		if(logger.isEntryEnabled()) logger.exit("toRest", result);
		return result;
	}

	public abstract String getName();

	public abstract String getPrimaryKey();

	public abstract String getSourceConfigurationUrl();

	public abstract int getQuiesceTimeout();

	public abstract String getDisplayName();

	public abstract String getDeploymentPolicy();

	public abstract String getApplianceName();
	
	public abstract String getApplianceId();

	public abstract DomainStatus getStatus();
	
	public abstract String getDeploymentPolicyUrl();
	
	public abstract String getDeploymentPolicyDomainName();
	
	public abstract String getDeploymentPolicyObjectName();
	
	public abstract List<String> getGroups();
	
	public abstract void updateGroups(List<String> groups);
	
	/**
	 * A transport-neutral representation of a domain's possible status.
	 * However, see the JavaDoc for com.ibm.datapower.amt.OperationStatus to
	 * understand the precise meaning - although a domain does have its own
	 * explicit up/down op-state, this is not it.
	 */
	public enum DomainStatus
	{
		/** All services in the domain are down. */
		DOWN, 
		/** Some services in the domain are up and some are down. */
		PARTIAL, 
		/** All services in the domain are up */
		UP, 
		/** The status couldn't be found, eg comms error. */
		UNKNOWN;

		@Override
		public String toString()
		{
			return this.name().toLowerCase();
		}
	}
	
	public abstract SynchronizationMode getSynchronizationMode();
	
	public enum SynchronizationMode
	{
		/** Configuration deployment is synchronized automatically. */
		AUTO,
		/** Configuration deployment is done manually. This is the default. */
		MANUAL;
		
		@Override
		public String toString()
		{
			return this.name().toLowerCase();
		}
	}

	public abstract String quiesce();
	
	public abstract String unquiesce();
	
	public abstract void delete();
	
	public abstract void updateProperties(Domain domain);
	
	public abstract String deploy(DomainDeploymentConfiguration deploymentSource);

	public abstract List<SvrService> getServices();

	public abstract SvrService getService(String type, String name);
	
	public abstract String deployService(String type, String name, ServiceDeploymentConfiguration deploymentSource);
	
	public abstract List<SvrServiceImpactDescriptor> deployServiceImpact(String type, String name, ServiceDeploymentConfiguration deploymentSource);

	/**
	 * @return Any files on the local system (where WAMC is running) that are
	 * relevant to this domain and should not be moved or deleted.
	 */
	public abstract List<? extends File> getLocalFilesInUse();
	
	public abstract String uploadFile(URI source, String fileName);

	public abstract String restart();
}
