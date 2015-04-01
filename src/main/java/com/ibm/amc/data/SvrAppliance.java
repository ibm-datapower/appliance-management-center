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

import java.net.URI;
import java.util.List;

import com.ibm.amc.Constants;
import com.ibm.amc.ras.Logger47;
import com.ibm.amc.resources.data.Appliance;
import com.ibm.amc.resources.data.HasRest;
import com.ibm.amc.resources.exceptions.NoSuchResourceException;

public abstract class SvrAppliance implements HasRest
{
	// @CLASS-COPYRIGHT@

	public enum ApplianceStatus
	{
		/** all enabled domains are up */
		UP, 
		/** all enabled domains are down (excluding default domain which is never down) */
		DOWN, 
		/** one or more enabled domains are down (quiescing, quiesced or simply down) */
		PARTIAL,
		/** unable to connect to appliance */
		UNKNOWN, 
		/** Unable to compute domain-based up/down status, but the appliance is active on the network. */
		REACHABLE;
		
		@Override
		public String toString()
		{
			return this.name().toLowerCase();
		}
	}
	
	@edu.umd.cs.findbugs.annotations.SuppressWarnings(justification="Findbugs wants this to be package-protected, but subclasses outside the package use it.", value="MS")
	protected static Logger47 logger = Logger47.get(SvrAppliance.class.getCanonicalName(), Constants.CWZBA_BUNDLE_NAME);
	
	public Appliance toRest()
	{
		if (logger.isEntryEnabled()) logger.entry("toRest");
		
		Appliance appliance = new Appliance();
		
		appliance.id = this.getPrimaryKey();
		appliance.name = this.getName();
		appliance.hostName = this.getHostname();
		appliance.ampPort = this.getAmpPort();
		appliance.adminUserId = this.getUserId();
		appliance.applianceType = this.getApplianceType();
		appliance.firmwareLevel = this.getActualFirmwareLevel();
		appliance.ampVersion = this.getCurrentAMPVersion();
		appliance.model = this.getModel();
		appliance.featureLicenses = this.getFeatureLicenses();
		appliance.guiPort = this.getGUIPort();
		appliance.firmwareManagementStatus = this.getManagementStatusOfFirmware();
		appliance.quiesceTimeout = this.getQuiesceTimeout();
		appliance.status = this.getStatus();
		appliance.serialNumber = this.getSerialNumber();
		appliance.capabilities = this.getCapabilities();
		appliance.groups = this.getGroups();

		if (logger.isEntryEnabled()) logger.exit("toRest", appliance);
		return appliance;
	}

	public ApplianceStatus getStatus()
	{
		if (logger.isEntryEnabled()) logger.entry("getStatus");
		
		List<SvrDomain> domains;
		try
		{
			domains = this.getDomains();
		}
		catch (final Throwable t)
		{
			// In particular, required for AmcRuntimeException due to LockException
			if (logger.isDebugEnabled()) logger.debug("getStatus", "getDomains() failed: " + t.getLocalizedMessage());
			return ApplianceStatus.UNKNOWN;
		}
		
		if(domains.size() < 1) 
		{
			// If no domains (eg XC10), base status on reachability rather than domain state.
			if(this.isReachable()) return ApplianceStatus.REACHABLE;
			return ApplianceStatus.DOWN;
		}
		
		boolean allUp = true;
		boolean noneUp = true;
		for (SvrDomain domain : domains)
		{
			switch (domain.getStatus())
			{
				case UP:
					if(!Constants.DEFAULT_DOMAIN.equals(domain.getName())) noneUp = false;
					break;
	
				case DOWN:
					allUp = false;
					break;
	
				case PARTIAL:
					allUp = false;
					break;
				
				case UNKNOWN:
					// WAMT docs don't really define what it means to get UNKNOWN
					// for a domain status, but it seems to get returned when
					// WAMT hasn't queried the appliance yet. UNKNOWN for the 
					// whole appliance seems a reasonable response.
					return ApplianceStatus.UNKNOWN;
			}
		}
		if(allUp) return ApplianceStatus.UP;
		if(noneUp) return ApplianceStatus.DOWN;
		return ApplianceStatus.PARTIAL;
	}
	

	@Override
	public String toString()
	{
		return toRest().toString();
	}

	/**
	 * Update the connection details for the appliance.
	 * @param applianceConnection ReST representation of the new details. Only
	 * attributes that are set in the ReST are changed, others are left 
	 * unchanged not blanked.
	 */
	public abstract void updateConnection(Appliance applianceConnection);

	public abstract String getPrimaryKey();
	
	public abstract boolean isReachable();

	public abstract List<SvrDomain> getDomains();

	public abstract String getSerialNumber();

	public abstract int getQuiesceTimeout();

	public abstract String getManagementStatusOfFirmware();

	public abstract String getApplianceType();
	
	public abstract int getGUIPort();

	public abstract List<String> getFeatureLicenses();

	public abstract String getModel();

	public abstract String getCurrentAMPVersion();

	public abstract String getActualFirmwareLevel();

	public abstract String getUserId();

	public abstract int getAmpPort();

	public abstract String getHostname();

	public abstract String getName();
	
	public abstract List<String> getCapabilities();
	
	public abstract List<String> getGroups();
	
	public abstract void updateGroups(List<String> groups);

	/**
	 * @throws NoSuchResourceException if there is no domain on this appliance
	 * with the given name.
	 */
	public abstract SvrDomain getDomain(String domainName) throws NoSuchResourceException;

	public abstract void remove();

	public abstract String quiesce();

	public abstract String unquiesce();

	public abstract String backup(String certificateName, URI backupDestination, boolean includeIscsi, boolean includeRaid);
	
	public abstract String backup(URI certificateLocation, URI backupDestination, boolean includeIscsi, boolean includeRaid);

	public abstract String restore(String credentialName, URI backupSource);

	/**
	 * Determine which of the available firmware items is considered best for 
	 * this appliance, and return all available versions of it.
	 */
	public abstract List<SvrFirmware> getBestFirmwareVersions();

	/**
	 * Deploy the firm ware with the given ID (which must already exist in the 
	 * repository) onto this appliance.
	 * 
	 * @param targetFirmwareId
	 *            The primary key of the firmware to deploy.
	 * @param licenceAccepted
	 *            a flag to indicate if the licence has been accepted
	 * @return an Action ID.
	 */
	public abstract String deployFirmware(String targetFirmwareId, boolean licenceAccepted);

	public abstract SvrDomain createDomain(String domainName);

	public abstract String reboot();
}
