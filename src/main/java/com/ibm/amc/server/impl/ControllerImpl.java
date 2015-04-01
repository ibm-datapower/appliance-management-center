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
package com.ibm.amc.server.impl;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.Response.Status;

import com.ibm.amc.AmcRuntimeException;
import com.ibm.amc.Constants;
import com.ibm.amc.Util;
import com.ibm.amc.data.SvrAppliance;
import com.ibm.amc.data.SvrDomain;
import com.ibm.amc.data.SvrFirmware;
import com.ibm.amc.data.SvrService;
import com.ibm.amc.data.filter.QueryFilterEngine;
import com.ibm.amc.ras.Logger47;
import com.ibm.amc.resources.data.Appliance;
import com.ibm.amc.resources.data.Domain;
import com.ibm.amc.resources.data.DomainDeploymentConfiguration;
import com.ibm.amc.resources.data.Firmware;
import com.ibm.amc.resources.data.Group;
import com.ibm.amc.resources.data.Service;
import com.ibm.amc.resources.data.ServiceDeploymentConfiguration;
import com.ibm.amc.resources.data.ServiceDescriptor;
import com.ibm.amc.resources.data.ServiceImpactDescriptor;
import com.ibm.amc.resources.data.ServiceObject;
import com.ibm.amc.resources.exceptions.ApplianceConnectionException;
import com.ibm.amc.resources.exceptions.NoSuchResourceException;
import com.ibm.amc.server.ApplianceManager;
import com.ibm.amc.server.Controller;

/**
 * @see com.ibm.amc.server.Controller
 * 
 * @author mallman
 */
public class ControllerImpl implements Controller
{
	// @CLASS-COPYRIGHT@

	static Logger47 logger = Logger47.get(ControllerImpl.class.getCanonicalName(), Constants.CWZBA_BUNDLE_NAME);

	/* access a appliance manager to use */
	private ApplianceManager manager = ApplianceManagerFactory.getInstance();

	/**
	 * Creation is via the factory class.
	 * 
	 * @see com.ibm.amc.server.impl.ControllerFactory
	 */
	ControllerImpl()
	{
	}

	/**
	 * @see com.ibm.amc.server.Controller#addAppliance(Appliance)
	 */
	@Override
	public Appliance addAppliance(Appliance applianceConnection) throws ApplianceConnectionException
	{
		if (logger.isEntryEnabled()) logger.entry("addAppliance", applianceConnection);

		Appliance result = manager.addAppliance(applianceConnection).toRest();

		if (logger.isEntryEnabled()) logger.exit("addAppliance", result);
		return result;
	}

	/**
	 * @see com.ibm.amc.server.Controller#getAppliances(Set)
	 */
	@Override
	public List<Appliance> getAppliances(Set<Map.Entry<String, List<String>>> queryParams)
	{
		if (logger.isEntryEnabled()) logger.entry("getAppliances", queryParams);

		List<Appliance> result = Util.listAsRest(manager.getAppliances());
		
		/* filter the results */
		if (queryParams != null && !queryParams.isEmpty())
		{
			if (logger.isDebugEnabled()) logger.debug("getAppliances", "Process query parameters");
			result = (List<Appliance>) QueryFilterEngine.filter(result, queryParams);
		}

		if (logger.isEntryEnabled()) logger.exit("getAppliances", result);
		return result;
	}

	/**
	 * @see com.ibm.amc.server.Controller#removeAppliance(String)
	 * @throws NoSuchResourceException
	 */
	@Override
	public void removeAppliance(String applianceId) throws NoSuchResourceException
	{
		if (logger.isEntryEnabled()) logger.entry("removeAppliance", applianceId);

		manager.removeAppliance(applianceId);

		if (logger.isEntryEnabled()) logger.exit("removeAppliance");
	}

	/**
	 * @see com.ibm.amc.server.Controller#getApplianceById(String)
	 * @throws NoSuchResourceException
	 */
	@Override
	public Appliance getApplianceById(String applianceId) throws NoSuchResourceException
	{
		if (logger.isEntryEnabled()) logger.entry("getApplianceById", applianceId);

		Appliance result = manager.getApplianceById(applianceId).toRest();

		if (logger.isEntryEnabled()) logger.exit("getApplianceById", result);
		return result;
	}

	/**
	 * @see com.ibm.amc.server.Controller#updateAppliance(String, Appliance)
	 * @throws NoSuchResourceException
	 */
	@Override
	public Appliance updateAppliance(String applianceId, Appliance applianceConnection) throws NoSuchResourceException
	{
		if (logger.isEntryEnabled()) logger.entry("updateAppliance", applianceId);

		Appliance result = manager.updateAppliance(applianceId, applianceConnection).toRest();

		if (logger.isEntryEnabled()) logger.exit("updateAppliance", result);
		return result;
	}

	/**
	 * @see com.ibm.amc.server.Controller#getDomains(Set)
	 * @throws NoSuchResourceException
	 */
	@Override
	public List<Domain> getDomains(Set<Map.Entry<String, List<String>>> queryParams)
	{
		if (logger.isEntryEnabled()) logger.entry("getDomains", queryParams);

		List<Domain> allKnownDomains = Util.listAsRest(manager.getDomains());

		/* filter the results */
		if (queryParams != null && !queryParams.isEmpty())
		{
			if (logger.isDebugEnabled()) logger.debug("getDomains", "Process query parameters");
			allKnownDomains = (List<Domain>) QueryFilterEngine.filter(allKnownDomains, queryParams);
		}
		
		if (logger.isEntryEnabled()) logger.exit("getDomains", allKnownDomains);
		return allKnownDomains;
	}

	@Override
	public List<Domain> getDomainsByName(String domainName, List<String> applianceIds) throws NoSuchResourceException
	{
		if (logger.isEntryEnabled()) logger.entry("getDomainsByName", domainName);

		List<SvrAppliance> appliances;
		if(applianceIds == null)
		{
			appliances = manager.getAppliances();
		}
		else
		{
			appliances = new ArrayList<SvrAppliance>();
			for (String id : applianceIds)
			{
				appliances.add(manager.getApplianceById(id));
			}
		}

		List<Domain> result = new ArrayList<Domain>();
		for (SvrAppliance appliance : appliances)
		{
			try
			{
				result.add(appliance.getDomain(domainName).toRest());
			} 
			catch(NoSuchResourceException e) {} // Fine, no domain with this name on that appliance.
		}

		if (logger.isEntryEnabled()) logger.exit("getDomainsByName", result);
		return result;
	}
	
	@Override
	public String quiesceDomain(String applianceId, String domainName)
	{
		if (logger.isEntryEnabled()) logger.entry("quiesceDomain", applianceId, domainName);

		final String actionId = manager.getApplianceById(applianceId).getDomain(domainName).quiesce();

		if (logger.isEntryEnabled()) logger.exit("quiesceDomain", actionId);
		return actionId;
	}
	
	@Override
	public String unquiesceDomain(String applianceId, String domainName)
	{
		if (logger.isEntryEnabled()) logger.entry("unquiesceDomain", applianceId, domainName);

		final String actionId = manager.getApplianceById(applianceId).getDomain(domainName).unquiesce();

		if (logger.isEntryEnabled()) logger.exit("unquiesceDomain", actionId);
		return actionId;
	}
	
	@Override
	public String restartDomain(String applianceId, String domainName)
	{
		if (logger.isEntryEnabled()) logger.entry("restartDomain", applianceId, domainName);

		final String actionId = manager.getApplianceById(applianceId).getDomain(domainName).restart();

		if (logger.isEntryEnabled()) logger.exit("restartDomain", actionId);
		return actionId;
	}
	
	@Override
	public String rebootAppliance(String applianceId)
	{
		if (logger.isEntryEnabled()) logger.entry("rebootAppliance", applianceId);

		String result = manager.getApplianceById(applianceId).reboot();

		if (logger.isEntryEnabled()) logger.exit("rebootAppliance", result);
		return result;
	}
	
	@Override
	public Domain updateDomain(String applianceId, String domainName, Domain domain) throws NoSuchResourceException
	{
		if (logger.isEntryEnabled()) logger.entry("updateDomain", applianceId, domainName, domain);
		
		Domain result = manager.updateDomain(applianceId, domainName, domain).toRest();
		
		if (logger.isEntryEnabled()) logger.exit("updateDomain", result);
		return result;
	}
	
	@Override
	public void updateDomainGroups(String applianceId, String domainName, List<String> groups)
	{
		if (logger.isEntryEnabled()) logger.entry("updateDomainGroups", applianceId, domainName, groups);

		checkGroupsList(groups);
		
		manager.getApplianceById(applianceId).getDomain(domainName).updateGroups(groups);
		
		if (logger.isEntryEnabled()) logger.exit("updateDomainGroups");
	}

	@Override
	public void deleteDomain(String applianceId, String domainName)
	{
		if (logger.isEntryEnabled()) logger.entry("deleteDomain", domainName);

		manager.getApplianceById(applianceId).getDomain(domainName).delete();

		if (logger.isEntryEnabled()) logger.exit("deleteDomain");
	}

	@Override
	public Domain getDomainFromAppliance(String applianceId, String domainName)
	{
		if (logger.isEntryEnabled()) logger.entry("getDomainFromAppliance", applianceId, domainName);

		Domain result = manager.getApplianceById(applianceId).getDomain(domainName).toRest();

		if (logger.isEntryEnabled()) logger.exit("getDomainFromAppliance", result);
		return result;
	}

	@Override
	public List<Domain> getDomainsFromAppliance(String applianceId)
	{
		if (logger.isEntryEnabled()) logger.entry("getDomainsFromAppliance");
		List<Domain> result = Util.listAsRest(manager.getApplianceById(applianceId).getDomains());
		if (logger.isEntryEnabled()) logger.exit("getDomainsFromAppliance", result);
		return result;
	}

	@Override
	public List<Service> getServices(Set<Map.Entry<String, List<String>>> queryParams)
	{
		if (logger.isEntryEnabled()) logger.entry("getServices", queryParams);

		List<Service> allKnownServices = Util.listAsRest(manager.getServices());

		/* filter the results */
		if (queryParams != null && !queryParams.isEmpty())
		{
			if (logger.isDebugEnabled()) logger.debug("getServices", "Process query parameters");
			allKnownServices = (List<Service>) QueryFilterEngine.filter(allKnownServices, queryParams);
		}
		
		if (logger.isEntryEnabled()) logger.exit("getServices", allKnownServices);
		return allKnownServices;
	}
	
	@Override
	public String deployService(String applianceId, String domainName, String type, String name, ServiceDeploymentConfiguration deploymentSource)
	{
		if (logger.isEntryEnabled()) logger.entry("deployService", applianceId, domainName, type, name, deploymentSource);
		
		String actionId = manager.getApplianceById(applianceId).getDomain(domainName).deployService(type, name, deploymentSource);
			
		if (logger.isEntryEnabled()) logger.exit("deployService", actionId);
		return actionId;
	}

	@Override
	public String quiesceAppliance(String applianceId)
	{
		if (logger.isEntryEnabled()) logger.entry("quiesceAppliance", applianceId);

		String result = manager.getApplianceById(applianceId).quiesce();

		if (logger.isEntryEnabled()) logger.exit("quiesceAppliance", result);
		return result;
	}

	@Override
	public String unquiesceAppliance(String applianceId)
	{
		if (logger.isEntryEnabled()) logger.entry("unquiesceAppliance", applianceId);

		String result = manager.getApplianceById(applianceId).unquiesce();

		if (logger.isEntryEnabled()) logger.exit("unquiesceAppliance", result);
		return result;
	}

	@Override
	public String getApplianceStatus(String applianceId)
	{
		if (logger.isEntryEnabled()) logger.entry("getApplianceStatus");
		
		String result = manager.getApplianceById(applianceId).getStatus().toString();
		
		if (logger.isEntryEnabled()) logger.exit("getApplianceStatus", result);
		return result;
	}
	
	/**
	 * @see com.ibm.amc.server.Controller#updateGroups(String, List<String>)
	 */
	@Override
	public void updateApplianceGroups(String applianceId, List<String> groups)
	{
		if (logger.isEntryEnabled()) logger.entry("updateApplianceGroups");
		
		checkGroupsList(groups);
		
		manager.getApplianceById(applianceId).updateGroups(groups);
		
		if (logger.isEntryEnabled()) logger.exit("updateApplianceGroups");
	}

	@Override
	public String addFirmware(URL url, String userComments)
	{
		if (logger.isEntryEnabled()) logger.entry("addFirmware", url, userComments);

		String result = manager.addFirmware(url, userComments);

		if (logger.isEntryEnabled()) logger.exit("addFirmware", result);
		return result;
	}

	@Override
	public void removeFirmware(String firmwareId)
	{
		if (logger.isEntryEnabled()) logger.entry("removeFirmware", firmwareId);
		
		manager.removeFirmware(firmwareId);

		if (logger.isEntryEnabled()) logger.exit("removeFirmware");
	}

	@Override
	public List<Firmware> getFirmware(Set<Map.Entry<String, List<String>>> queryParams)
	{
		if (logger.isEntryEnabled()) logger.entry("getFirmware", queryParams);

		List<Firmware> firmware = Util.listAsRest(manager.getFirmware());

		/* filter the results */
		if (queryParams != null && !queryParams.isEmpty())
		{
			if (logger.isDebugEnabled()) logger.debug("getDomains", "Process query parameters");
			firmware = (List<Firmware>) QueryFilterEngine.filter(firmware, queryParams);
		}
		
		if (logger.isEntryEnabled()) logger.exit("getFirmware", firmware);
		return firmware;
	}

	@Override
	public Firmware getFirmwareById(String firmwareId)
	{
		if (logger.isEntryEnabled()) logger.entry("getFirmwareById", firmwareId);

		Firmware firmware = manager.getFirmwareById(firmwareId).toRest();

		if (logger.isEntryEnabled()) logger.exit("getFirmwareById", firmware);
		return firmware;
	}

	/**
	 * @see com.ibm.amc.server.Controller#updateFirmware(String, Firmware)
	 */
	@Override
	public Firmware updateFirmware(String firmwareId, Firmware firmware)
	{
		if (logger.isEntryEnabled()) logger.entry("updateFirmware", firmwareId, firmware);

		Firmware result = manager.updateFirmware(firmwareId, firmware).toRest();
		
		if (logger.isEntryEnabled()) logger.exit("updateFirmware", result);
		return result;
	}

	@Override
	public List<Firmware> getAvailableFirmware(String applianceId)
	{
		if (logger.isEntryEnabled()) logger.entry("getAvailableFirmware", applianceId);
		
		List<SvrFirmware> allFirmwares = manager.getFirmware();
		SvrAppliance appliance = manager.getApplianceById(applianceId);
		List<SvrFirmware> bestFirmwares = appliance.getBestFirmwareVersions();
		List<Firmware> result = new ArrayList<Firmware>();

		for (SvrFirmware firmware : allFirmwares)
		{
			if(firmware.isCompatibleWith(appliance))
			{
				Firmware restObject = firmware.toRest();
				if(bestFirmwares.contains(firmware))
				{
					restObject.recommended = true;
				}
				result.add(restObject);
			}
		}
		if (logger.isEntryEnabled()) logger.exit("getAvailableFirmware", result);
		return result;
	}

	@Override
	public String deployFirmware(String applianceId, String targetFirmwareId, boolean licenceAccepted)
	{
		if (logger.isEntryEnabled()) logger.entry("deployFirmware", applianceId, targetFirmwareId, licenceAccepted);
		String result = manager.getApplianceById(applianceId).deployFirmware(targetFirmwareId, licenceAccepted);
		if (logger.isEntryEnabled()) logger.exit("deployFirmware", result);
		return result;
	}

	@Override
	public String backupAppliance(String applianceId, String certificateName, URI backupDestination, boolean includeIscsi, boolean includeRaid)
	{
		if (logger.isEntryEnabled()) logger.entry("backupAppliance", applianceId);

		String result = manager.getApplianceById(applianceId).backup(certificateName, backupDestination, includeIscsi, includeRaid);

		if (logger.isEntryEnabled()) logger.exit("backupAppliance", result);
		return result;
	}

	@Override
	public String backupAppliance(String applianceId, URI certificateLocation, URI backupDestination, boolean includeIscsi, boolean includeRaid)
	{
		if (logger.isEntryEnabled()) logger.entry("backupAppliance", applianceId);

		String result = manager.getApplianceById(applianceId).backup(certificateLocation, backupDestination, includeIscsi, includeRaid);

		if (logger.isEntryEnabled()) logger.exit("backupAppliance", result);
		return result;
	}

	@Override
	public String restoreAppliance(String applianceId, String credentialName, URI backupSource)
	{
		if (logger.isEntryEnabled()) logger.entry("restoreAppliance", applianceId);

		String result = manager.getApplianceById(applianceId).restore(credentialName, backupSource);

		if (logger.isEntryEnabled()) logger.exit("restoreAppliance", result);
		return result;
	}

	@Override
	public String deployDomain(String applianceId, String domainName, DomainDeploymentConfiguration deploymentSource)
	{
		if (logger.isEntryEnabled()) logger.entry("deployDomain", applianceId, domainName, deploymentSource);
		SvrAppliance appliance = manager.getApplianceById(applianceId);
		SvrDomain domain; 
		try
		{
			domain = appliance.getDomain(domainName);
		}
		catch(NoSuchResourceException e)
		{
			domain = appliance.createDomain(domainName);
		}
		
		String actionId = domain.deploy(deploymentSource);
		if (logger.isEntryEnabled()) logger.exit("deployDomain", actionId);
		return actionId;
	}

	@Override
	public Service getService(String applianceId, String domainName, String type, String name)
	{
		if (logger.isEntryEnabled()) logger.entry("getService", applianceId, domainName, type, name);
		Service service = manager.getApplianceById(applianceId).getDomain(domainName).getService(type, name).toRest();
		if (logger.isEntryEnabled()) logger.exit("getService", service);
		return service;
	}

	@Override
	public List<ServiceDescriptor> getContainedServices(List<String> applianceIds, URI serviceSourceUri)
	{
		if (logger.isEntryEnabled()) logger.entry("getContainedServices", applianceIds, serviceSourceUri);
		
		List<ServiceDescriptor> result = Util.listAsRest(manager.getContainedServices(applianceIds, serviceSourceUri));

		if (logger.isEntryEnabled()) logger.exit("getContainedServices", result);
		return result;
	}
	
	@Override
	public List<ServiceImpactDescriptor> deployServiceImpact(String applianceId, String domainName, String type, String name, ServiceDeploymentConfiguration deploymentSource)
	{
		if (logger.isEntryEnabled()) logger.entry("deployServiceImpact", applianceId, domainName, type, name, deploymentSource);

		List<ServiceImpactDescriptor> result = Util.listAsRest(manager.getApplianceById(applianceId).getDomain(domainName).deployServiceImpact(type, name, deploymentSource));
		
		if (logger.isEntryEnabled()) logger.exit("deployServiceImpact", result);
		return result;
	}
	
	@Override
	public String unquiesceService(String applianceId, String domainName, String type, String name)
	{
		if (logger.isEntryEnabled()) logger.entry("unquiesceService", applianceId, domainName, type, name);
		
		SvrService service = manager.getApplianceById(applianceId).getDomain(domainName).getService(type, name);
		final String actionId = service.unquiesce();
		
		if (logger.isEntryEnabled()) logger.exit("unquiesceService", actionId);
		return actionId;
	}
	
	@Override
	public String quiesceService(String applianceId, String domainName, String type, String name)
	{
		if (logger.isEntryEnabled()) logger.entry("quiesceService", applianceId, domainName, type, name);
		
		SvrService service = manager.getApplianceById(applianceId).getDomain(domainName).getService(type, name);
		final String actionId = service.quiesce();
		
		if (logger.isEntryEnabled()) logger.exit("quiesceService", actionId);
		return actionId;
	}

	@Override
	public void deleteService(String applianceId, String domainName, String type, String name, List<String> orphansToDelete)
	{
		if (logger.isEntryEnabled()) logger.entry("deleteService", applianceId, domainName, type, name, orphansToDelete);
		
		SvrService service = manager.getApplianceById(applianceId).getDomain(domainName).getService(type, name);
		service.delete(orphansToDelete);
		
		if (logger.isEntryEnabled()) logger.exit("deleteService");
	}

	@Override
	public List<ServiceObject> listOrphansIfServiceDeleted(String applianceId, String domainName, String type, String name)
	{
		if (logger.isEntryEnabled()) logger.entry("listOrphansIfServiceDeleted", applianceId, domainName, type, name);
		
		SvrService service = manager.getApplianceById(applianceId).getDomain(domainName).getService(type, name);
		List<ServiceObject> result = Util.listAsRest(service.getOrphansIfDeleted());
		
		if (logger.isEntryEnabled()) logger.exit("listOrphansIfServiceDeleted", result);
		return result;
	}

	@Override
	public List<File> getInUseDeploymentFiles()
	{
		if (logger.isEntryEnabled()) logger.entry("getInUseDeploymentFiles");
		ArrayList<File> files = new ArrayList<File>();
		List<SvrDomain> allDomains = manager.getDomains();
		for (SvrDomain domain : allDomains)
		{
			files.addAll(domain.getLocalFilesInUse());
		}
		if (logger.isEntryEnabled()) logger.exit("getInUseDeploymentFiles", files);
		return files;
	}

	@Override
	public String uploadFile(String applianceId, String domainName, URI source, String fileName)
	{
		if (logger.isEntryEnabled()) logger.entry("uploadFile", applianceId, domainName, source, fileName);
		
		final String actionId = manager.getApplianceById(applianceId).getDomain(domainName).uploadFile(source, fileName);
		
		if (logger.isEntryEnabled()) logger.exit("uploadFile", actionId);
		return actionId;
	}
	
	/**
	 * @see com.ibm.amc.server.Controller#getGroups()
	 */
	@Override
	public List<Group> getGroups()
	{
		if (logger.isEntryEnabled()) logger.entry("getGroups");
		List<Group> result = new ArrayList<Group>();
		
		List<String> names = manager.getGroupNames();
		
		for(String name : names)
		{
			Group group = new Group();
			group.name = name;
			group.members = manager.getGroupMembers(name);
			result.add(group);
		}
		
		if (logger.isEntryEnabled()) logger.exit("getGroups", result);
		return result;
	}
	
	/**
	 * Check to see if the provided list is a null or if any entries in 
	 * the list are null or an empty string.
	 *  
	 * @param groups the list of Strings to check
	 */
	private void checkGroupsList(List<String> groups)
	{
		/* weed out any empty strings or nulls */
		if (groups != null)
		{
			for (String group : groups)
			{
				if (group == null || "".equals(group))
				{
					throw new AmcRuntimeException(Status.BAD_REQUEST, "CWZBA0534E_INVALID_PARAMETER_VALUE", Constants.GROUPS_TAG_KEY, group);
				}
			}
		}
		else
		{
			throw new AmcRuntimeException(Status.BAD_REQUEST, "CWZBA0534E_INVALID_PARAMETER_VALUE", "groups", null);
		}
	}
}
