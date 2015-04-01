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
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import javax.ws.rs.core.Response.Status;

import com.ibm.amc.AmcRuntimeException;
import com.ibm.amc.Constants;
import com.ibm.amc.FileManager;
import com.ibm.amc.ResourceLoadException;
import com.ibm.amc.Util;
import com.ibm.amc.WamcProperties;
import com.ibm.amc.WamcProperties.Props;
import com.ibm.amc.data.SvrAppliance;
import com.ibm.amc.data.SvrDomain;
import com.ibm.amc.data.SvrFirmware;
import com.ibm.amc.data.SvrService;
import com.ibm.amc.data.SvrServiceDescriptor;
import com.ibm.amc.data.wamt.WamtAppliance;
import com.ibm.amc.data.wamt.WamtConfigService;
import com.ibm.amc.data.wamt.WamtDomain;
import com.ibm.amc.data.wamt.WamtFirmware;
import com.ibm.amc.demo.provider.AmcDemoCommands;
import com.ibm.amc.ras.Logger47;
import com.ibm.amc.resources.data.Appliance;
import com.ibm.amc.resources.data.Domain;
import com.ibm.amc.resources.data.GroupMember;
import com.ibm.amc.resources.exceptions.AmcIllegalArgumentException;
import com.ibm.amc.resources.exceptions.ApplianceConnectionException;
import com.ibm.amc.resources.exceptions.NoSuchResourceException;
import com.ibm.amc.server.ApplianceManager;
import com.ibm.amc.server.action.ActionFactory;
import com.ibm.amc.server.action.impl.AbstractAction;
import com.ibm.datapower.amt.Credential;
import com.ibm.datapower.amt.DMgrException;
import com.ibm.datapower.amt.amp.AMPException;
import com.ibm.datapower.amt.amp.DeviceExecutionException;
import com.ibm.datapower.amt.clientAPI.Blob;
import com.ibm.datapower.amt.clientAPI.ConfigService;
import com.ibm.datapower.amt.clientAPI.DeletedException;
import com.ibm.datapower.amt.clientAPI.Device;
import com.ibm.datapower.amt.clientAPI.Firmware;
import com.ibm.datapower.amt.clientAPI.FirmwareVersion;
import com.ibm.datapower.amt.clientAPI.FullException;
import com.ibm.datapower.amt.clientAPI.InUseException;
import com.ibm.datapower.amt.clientAPI.InvalidParameterException;
import com.ibm.datapower.amt.clientAPI.ManagedSet;
import com.ibm.datapower.amt.clientAPI.Manager;
import com.ibm.datapower.amt.clientAPI.NotExistException;
import com.ibm.datapower.amt.clientAPI.ProgressContainer;
import com.ibm.datapower.amt.clientAPI.ServiceConfiguration;
import com.ibm.datapower.amt.clientAPI.Taggable;
import com.ibm.datapower.amt.clientAPI.URLSource;
import com.ibm.datapower.amt.clientAPI.UnsuccessfulOperationException;
import com.ibm.datapower.amt.clientAPI.Version;
import com.ibm.datapower.amt.dataAPI.AlreadyExistsInRepositoryException;
import com.ibm.datapower.amt.dataAPI.DatastoreException;

/**
 * ApplianceManager implementation that will use WAMT to communicate with appliances.
 * 
 * @see com.ibm.amc.server.ApplianceManager
 * @author mallman
 */
public class ApplianceManagerImpl implements ApplianceManager
{
	// @CLASS-COPYRIGHT@

	static Logger47 logger = Logger47.get(ApplianceManagerImpl.class.getCanonicalName(), Constants.CWZBA_BUNDLE_NAME);

	/**
	 * Instance of WAMT manager populated on construction.
	 */
	private Manager manager;

	/**
	 * Lock used to control access to WAMT operations that operate on the default WAMC managed set
	 * and which would otherwise fail with a WAMT lock exception.
	 */
	private ReentrantLock managedSetLock = new ReentrantLock();

	/**
	 * Map of serial numbers to appliances. Cache primarily exists so that
	 * <code>getAppliances</code> does not return a new appliance until <code>addAppliance</code>
	 * has completed.
	 */
	private Map<String, SvrAppliance> appliances = Collections.synchronizedMap(new HashMap<String, SvrAppliance>());

	/**
	 * Creation is via the factory class.
	 * 
	 * @see com.ibm.amc.server.impl.ApplianceManagerFactory
	 */
	ApplianceManagerImpl()
	{
		this(getManager());
	}

	/**
	 * Separate constructor exposed for unit test purposes.
	 * 
	 * @param manager
	 *            the WAMT manager
	 */
	ApplianceManagerImpl(Manager manager)
	{
		this.manager = manager;

		// Populate appliance map
		Device[] devices = manager.getAllDevices();
		for (Device device : devices)
		{
			try
			{
				WamtAppliance appliance = new WamtAppliance(manager, device);
				if (logger.isDebugEnabled()) logger.debug("<init>", "Adding appliance to WAMC appliance list: " + appliance);
				appliances.put(appliance.getPrimaryKey(), appliance);
			}
			catch (DeletedException e)
			{
				continue; /* ignore deleted devices. */
			}
		}
	}

	/**
	 * @see com.ibm.amc.server.ApplianceManager#addAppliance(Appliance)
	 */
	@Override
	public SvrAppliance addAppliance(Appliance applianceConnection) throws ApplianceConnectionException
	{
		if (logger.isEntryEnabled()) logger.entry("addAppliance", applianceConnection);

		SvrAppliance result;
		managedSetLock.lock();
		try
		{
			result = new WamtAppliance(manager, applianceConnection);
		}
		finally
		{
			managedSetLock.unlock();
		}
		appliances.put(result.getPrimaryKey(), result);

		if (logger.isEntryEnabled()) logger.exit("addAppliance", result);
		return result;
	}

	@Override
	synchronized public void removeAppliance(String applianceId) throws NoSuchResourceException
	{
		if (logger.isEntryEnabled()) logger.entry("removeAppliance", applianceId);

		SvrAppliance appliance = getApplianceById(applianceId);
		managedSetLock.lock();
		try
		{
			appliance.remove();
		}
		finally
		{
			managedSetLock.unlock();
		}
		appliances.remove(applianceId);

		if (logger.isEntryEnabled()) logger.exit("removeAppliance");
	}

	/**
	 * @see com.ibm.amc.server.ApplianceManager#getAppliances()
	 */
	@Override
	public List<SvrAppliance> getAppliances()
	{
		if (logger.isEntryEnabled()) logger.entry("getAppliances");

		List<SvrAppliance> result = new ArrayList<SvrAppliance>(appliances.values());

		// Sort list by appliance name
		Collections.sort(result, new Comparator<SvrAppliance>()
		{
			@Override
			public int compare(final SvrAppliance appliance1, final SvrAppliance appliance2)
			{
				return appliance1.getName().compareTo(appliance2.getName());
			}
		});

		if (logger.isEntryEnabled()) logger.exit("getAppliances", result);
		return result;
	}

	/**
	 * @see com.ibm.amc.server.ApplianceManager#getApplianceById(String)
	 * @throws NoSuchResourceException
	 */
	@Override
	public SvrAppliance getApplianceById(String applianceId) throws NoSuchResourceException
	{
		if (logger.isEntryEnabled()) logger.entry("getApplianceById", applianceId);

		SvrAppliance wamcAppliance = appliances.get(applianceId);
		if (wamcAppliance != null)
		{
			if (logger.isEntryEnabled()) logger.exit("getApplianceById", wamcAppliance);
			return wamcAppliance;
		}

		wamcAppliance = new WamtAppliance(manager, applianceId);
		appliances.put(applianceId, wamcAppliance);

		if (logger.isEntryEnabled()) logger.exit("getApplianceById", wamcAppliance);
		return wamcAppliance;
	}

	/**
	 * @see com.ibm.amc.server.ApplianceManager#updateAppliance(String, Appliance)
	 * @throws NoSuchResourceException
	 */
	@Override
	public SvrAppliance updateAppliance(String applianceId, Appliance applianceConnection) throws NoSuchResourceException
	{
		if (logger.isEntryEnabled()) logger.entry("updateAppliance", applianceId, applianceConnection);

		/* grab the existing appliance. */
		SvrAppliance appliance = appliances.get(applianceId);
		if (appliance == null) throw new NoSuchResourceException(applianceId);

		appliance.updateConnection(applianceConnection);

		if (logger.isEntryEnabled()) logger.exit("updateAppliance", appliance);
		return appliance;
	}

	@Override
	public List<SvrDomain> getDomains()
	{
		if (logger.isEntryEnabled()) logger.entry("getDomains");

		List<SvrAppliance> appliances = getAppliances();
		List<SvrDomain> domains = new ArrayList<SvrDomain>();
		for (SvrAppliance appliance : appliances)
		{
			domains.addAll(appliance.getDomains());
		}

		if (logger.isEntryEnabled()) logger.exit("getDomains", domains);
		return domains;
	}

	@Override
	public SvrDomain updateDomain(String applianceId, String domainName, Domain domain) throws NoSuchResourceException
	{
		if (logger.isEntryEnabled()) logger.entry("updateDomain", applianceId, domainName, domain);

		SvrAppliance appliance = appliances.get(applianceId);
		if (appliance == null) throw new NoSuchResourceException(applianceId);

		SvrDomain svrDomain = appliance.getDomain(domainName);

		if (domain == null) throw new NoSuchResourceException(domainName);

		svrDomain.updateProperties(domain);

		if (logger.isEntryEnabled()) logger.exit("updateDomain", svrDomain);
		return svrDomain;
	}

	@Override
	public List<SvrService> getServices()
	{
		if (logger.isEntryEnabled()) logger.entry("getServices");

		List<SvrService> services = new ArrayList<SvrService>();
		for (SvrAppliance appliance : getAppliances())
		{
			// If this appliance doesn't do services, we're not interested.
			if (!appliance.getCapabilities().contains("service_config_management")) continue;

			for (SvrDomain domain : appliance.getDomains())
			{
				services.addAll(domain.getServices());
			}
		}

		if (logger.isEntryEnabled()) logger.exit("getServices", services);
		return services;
	}

	@Override
	public List<SvrServiceDescriptor> getContainedServices(List<String> applianceIds, URI serviceSourceUri)
	{
		if (logger.isEntryEnabled()) logger.entry("getContainedServices", applianceIds, serviceSourceUri);

		List<SvrServiceDescriptor> result = new ArrayList<SvrServiceDescriptor>();

		boolean first = true;

		for (String applianceId : applianceIds)
		{
			List<SvrServiceDescriptor> applianceResult = new ArrayList<SvrServiceDescriptor>();

			ConfigService[] services = getContainedServicesArray(applianceId, serviceSourceUri);

			if (services != null)
			{
				for (ConfigService cs : services)
				{
					if (logger.isDebugEnabled()) logger.debug("getContainedServices", "Appliance \"" + applianceId + "\" can deploy service:" + cs.toString());
					applianceResult.add(new WamtConfigService(cs));
				}
			}

			if (first)
			{
				/*
				 * If this is the first (or only) appliance, use it as the base of the result
				 */
				result.addAll(applianceResult);
				first = false;
			}
			else
			{
				/*
				 * Otherwise, remove any items from the result that are not compatible with this
				 * appliance
				 */
				result.retainAll(applianceResult);
			}
		}

		if (logger.isEntryEnabled()) logger.exit("getContainedServices", result);
		return result;
	}

	@Override
	public String addFirmware(final URL url, final String userComment)
	{
		if (logger.isEntryEnabled()) logger.entry("addFirmware", url, userComment);

		final AbstractAction addFirmwareAction = new AbstractAction("addFirmware", null, (userComment == null || "".equals(userComment)) ? "CWZBA1031I_ADD_FIRMWARE_ACTION_NO_COMMENT_DESCRIPTION"
				: "CWZBA1013I_ADD_FIRMWARE_ACTION_DESCRIPTION", userComment)
		{

			@Override
			public void start(String actionId)
			{
				ProgressContainer pc = null;

				try
				{
					// Workaround for WAMT issue
					final Blob blob;
					if (url.getProtocol().equalsIgnoreCase("file"))
					{
						try
						{
							blob = new Blob(new File(url.toURI()));
						}
						catch (URISyntaxException e)
						{
							throw new AmcRuntimeException(e);
						}
					}
					else
					{
						blob = new Blob(url);
					}
					pc = manager.addFirmware(blob, userComment);

				}
				catch (final FullException e)
				{
					ActionFactory.getActionLog().getActionStatus(actionId).failed(e);
					return;
				}

				try
				{
					pc.waitForEnd();
				}
				catch (InterruptedException e)
				{
					// Do nothing
					if (logger.isDebugEnabled()) logger.debug("Caught InterruptedException during addFirmware: {0}", e.getMessage());
				}

				// If the blob came from a local temp file, delete it.
				if (url.getProtocol().equalsIgnoreCase("file"))
				{
					File tempFile = new File(url.getPath());
					boolean deleted = tempFile.delete();
					if (!deleted && logger.isDebugEnabled()) logger.debug("addFirmware()", "Unable to delete temporary upload file: ", tempFile);
				}

				if (pc.hasError())
				{
					ActionFactory.getActionLog().getActionStatus(actionId).failed(pc.getError());
				}
				else
				{
					ActionFactory.getActionLog().getActionStatus(actionId).succeeded();
				}
			}
		};

		String result = ActionFactory.getActionController().submitAction(addFirmwareAction);

		if (logger.isEntryEnabled()) logger.exit("addFirmware", result);
		return result;
	}

	@Override
	public void removeFirmware(final String firmwareId)
	{
		if (logger.isEntryEnabled()) logger.entry("removeFirmware", firmwareId);

		getFirmwareById(firmwareId).remove();

		if (logger.isEntryEnabled()) logger.exit("removeFirmware");
	}

	@Override
	public List<SvrFirmware> getFirmware()
	{
		if (logger.isEntryEnabled()) logger.entry("getFirmware");

		List<SvrFirmware> result = new ArrayList<SvrFirmware>();

		Firmware[] allFirmware = manager.getFirmwares();

		for (Firmware firmware : allFirmware)
		{
			// Now get all the firmware versions for this firmware
			try
			{
				Version[] allVersions = firmware.getVersions();

				for (Version version : allVersions)
				{
					WamtFirmware wamtFirmware = new WamtFirmware((FirmwareVersion) version, manager);
					result.add(wamtFirmware);
				}
			}
			catch (DeletedException e)
			{
				// Ignore any firmware versions that no longer exist
				if (logger.isDebugEnabled()) logger.debug("getFirmware()", "Received DeletedException when retrieving versions for firmware: ", firmware);
			}

		}

		// Sort by Appliance Type (ascending) Firmware Version (descending)
		Collections.sort(result, new Comparator<SvrFirmware>()
		{
			@Override
			public int compare(final SvrFirmware firmware1, final SvrFirmware firmware2)
			{
				int result = firmware1.getApplianceType().compareTo(firmware1.getApplianceType());

				if (result == 0)
				{
					result = firmware2.getLevel().compareTo(firmware1.getLevel());
				}

				return result;
			}
		});

		if (logger.isEntryEnabled()) logger.exit("getFirmware()", result);
		return result;
	}

	@Override
	public SvrFirmware getFirmwareById(String firmwareId)
	{
		if (logger.isEntryEnabled()) logger.entry("getFirmwareById()", firmwareId);

		SvrFirmware firmware = new WamtFirmware(firmwareId, manager);

		if (logger.isEntryEnabled()) logger.exit("getFirmwareById()", firmware);
		return firmware;
	}

	/**
	 * @see com.ibm.amc.server.ApplianceManager#updateFirmware(String, Firmware)
	 */
	@Override
	public SvrFirmware updateFirmware(String firmwareId, com.ibm.amc.resources.data.Firmware firmware)
	{
		if (logger.isEntryEnabled()) logger.entry("updateFirmware", firmwareId, firmware);

		/* grab the existing firmware */
		SvrFirmware result = new WamtFirmware(firmwareId, manager);

		/* if we have been provided with a new user comment, update it */
		if (firmware.userComment != null)
		{
			result.setUserComment(firmware.userComment);
		}

		if (logger.isEntryEnabled()) logger.exit("updateFirmware", result);
		return result;
	}

	/**
	 * Retrieves the WAMT manager.
	 * 
	 * @return the WAMT manager
	 */
	static Manager getManager()
	{
		/* setup the manager config options and connect */
		String repositoryPath = readRepositoryPath();
		if (logger.isInfoEnabled()) logger.info("CWZBA0001I_WAMT_REPO_PATH", repositoryPath);

		Props wamcProperties = WamcProperties.instance();

		// If extra WAMT properties are being used, set a system property
		// that will be picked up by WAMT.
		String extraPropertiesFile = wamcProperties.getWamtConfigFilePath();
		if (extraPropertiesFile != null)
		{
			System.setProperty("WAMT_CONFIGURATION_HOME", extraPropertiesFile);
			if (logger.isInfoEnabled()) logger.info("CWZBA0517I_EXTRA_WAMT_PROPERTIES", extraPropertiesFile);
		}

		Map<String, Object> options = new HashMap<String, Object>();
		Credential credential = new Credential();
		credential.setProperty("RepositoryDirectory", repositoryPath);
		options.put(Manager.OPTION_NOTIFICATION_CATCHER_PORT, wamcProperties.getWamtNotificationPort() + "");
		options.put(Manager.OPTION_HTTP_LISTENER_IP_PORT, wamcProperties.getWamtHttpPort() + "");

		if (wamcProperties.getWamtNotificationAddress() != null) options.put(Manager.OPTION_NOTIFICATION_CATCHER_IP_ADDRESS, wamcProperties.getWamtNotificationAddress());
		if (wamcProperties.getWamtNotificationInterface() != null) options.put(Manager.OPTION_NOTIFICATION_CATCHER_IP_INTERFACE, wamcProperties.getWamtNotificationInterface());
		if (wamcProperties.getWamtHttpAddress() != null) options.put(Manager.OPTION_HTTP_LISTENER_IP_ADDRESS, wamcProperties.getWamtHttpAddress());
		if (wamcProperties.getWamtHttpInterface() != null) options.put(Manager.OPTION_HTTP_LISTENER_IP_INTERFACE, wamcProperties.getWamtHttpInterface());

		if (wamcProperties.getDemoMode())
		{
			if (logger.isInfoEnabled()) logger.info("CWZBA0518I_DEMO_MODE");
			options.put(Manager.OPTION_COMMANDS_IMPL, AmcDemoCommands.class.getCanonicalName());
			options.put(Manager.OPTION_COMMANDS_V2_IMPL, AmcDemoCommands.class.getCanonicalName());
			options.put(Manager.OPTION_COMMANDS_V3_IMPL, AmcDemoCommands.class.getCanonicalName());
		}
		options.put(Manager.OPTION_CREDENTIAL, credential);
		options.put(Manager.OPTION_COLLECT_DOMAIN_SYNCH_PROGRESSES, true);
		try
		{
			return Manager.getInstance(options);
		}
		catch (final DMgrException e)
		{
			throw new AmcRuntimeException(e);
		}
	}

	/**
	 * Load the path to the WAMT repository from outside the application. Normally this will be
	 * specified in the wamc.properties file, but for special purposes (test, debug, development) it
	 * may be overridden by a WAMT_REPO environment variable.
	 */
	private static String readRepositoryPath()
	{
		if (logger.isEntryEnabled()) logger.entry("readRepositoryPath");

		String repositoryPath = System.getenv("WAMT_REPO");
		if (repositoryPath != null)
		{
			if (logger.isInfoEnabled()) logger.info("CWZBA0003I_WAMT_REPO_PATH_FROM_ENV", "WAMT_REPO");
		}
		else
		{
			repositoryPath = WamcProperties.instance().getWamtRepository();
		}

		if (logger.isEntryEnabled()) logger.exit("readRepositoryPath", repositoryPath);
		return repositoryPath;
	}

	ManagedSet getDefaultManagedSet() throws AlreadyExistsInRepositoryException, DatastoreException, DeletedException
	{
		return new ManagedSet(Constants.DEFAULT_MANAGED_SET);
	}

	void shutdownManager()
	{
		manager.shutdown();
	}

	private ConfigService[] getContainedServicesArray(String applianceId, URI serviceSourceUri)
	{
		if (logger.isEntryEnabled()) logger.entry("getContainedServicesArray", applianceId, serviceSourceUri);

		ServiceConfiguration serviceConfig = null;

		/* create the service configuration from the referenced source file */
		try
		{
			serviceConfig = new ServiceConfiguration(new URLSource(FileManager.resolveUriToUrl(serviceSourceUri).toString()));
		}
		catch (URISyntaxException e)
		{
			throw new AmcIllegalArgumentException(Status.BAD_REQUEST, e, "CWZBA2004E_INVALID_URI_STRING", serviceSourceUri.toString());
		}

		/* extract the configurable services from the service configuration */
		ConfigService[] services = null;

		/* WAMT treats service configuration as a blob. Pass it to an appliance to decode */
		WamtAppliance appliance = new WamtAppliance(manager, applianceId);

		try
		{
			services = serviceConfig.getAvailableServices(appliance.getWamtDevice());
		}
		catch (DeviceExecutionException e)
		{
			if (Util.getWamtExceptionCode(e).equals("WAMT0705")) throw new AmcRuntimeException(Status.BAD_REQUEST, "CWZBA0527E_ERROR_DISSECTING_SERVICES", appliance.getName());
			else
				throw new AmcRuntimeException(e);
		}
		catch (DeletedException e)
		{
			throw new AmcRuntimeException(e);
		}
		catch (UnsuccessfulOperationException e)
		{
			throw new AmcRuntimeException(e);
		}
		catch (NotExistException e)
		{
			throw new AmcRuntimeException(e);
		}
		catch (InUseException e)
		{
			throw new AmcRuntimeException(e);
		}
		catch (InvalidParameterException e)
		{
			throw new AmcRuntimeException(e);
		}
		catch (AMPException e)
		{
			throw new AmcRuntimeException(e);
		}
		catch (IOException e)
		{
			throw new ResourceLoadException(e);
		}

		if (services != null && logger.isDebugEnabled()) logger.debug("getContainedServicesArray", "Number of services found: " + services.length);

		if (logger.isEntryEnabled()) logger.exit("getContainedServicesArray", services);
		return services;
	}

	@Override
	public List<String> getGroupNames()
	{
		if (logger.isEntryEnabled()) logger.entry("getGroupNames");
		List<String> result = new ArrayList<String>();

		Set<String> groups = null;

		try
		{
			groups = manager.getTagValues(Constants.GROUPS_TAG_KEY);
		}
		catch (InvalidParameterException e)
		{
			throw new AmcRuntimeException(e);
		}

		if (groups != null)
		{
			result.addAll(groups);
		}

		if (logger.isEntryEnabled()) logger.exit("getGroupNames", result);
		return result;
	}

	@Override
	public Set<GroupMember> getGroupMembers(String name)
	{
		if (logger.isEntryEnabled()) logger.entry("getGroupMembers", name);

		Set<GroupMember> result = new HashSet<GroupMember>();

		Set<Taggable> taggables = null;

		try
		{
			taggables = manager.getTaggedByNameValue(Constants.GROUPS_TAG_KEY, name);
		}
		catch (InvalidParameterException e)
		{
			throw new AmcRuntimeException(e);
		}

		if (taggables != null)
		{
			for (Taggable taggable : taggables)
			{

				@SuppressWarnings("rawtypes")
				Class clazz = taggable.getClass();

				if (clazz.equals(Device.class))
				{
					try
					{
						WamtAppliance wamtAppliance = new WamtAppliance(manager, (Device) taggable);
						GroupMember groupMember = new GroupMember();
						groupMember.id = wamtAppliance.getPrimaryKey();
						groupMember.name = wamtAppliance.getName();
						groupMember.type = GroupMember.Type.APPLIANCE;

						result.add(groupMember);
					}
					catch (DeletedException e)
					{
						/*
						 * If the appliance has been deleted, it won't be in the group any more.
						 */
					}
				}
				else if (clazz.equals(com.ibm.datapower.amt.clientAPI.Domain.class))
				{
					com.ibm.datapower.amt.clientAPI.Domain domain = (com.ibm.datapower.amt.clientAPI.Domain) taggable;
					try
					{
						WamtDomain wamtDomain = new WamtDomain(manager, domain.getPrimaryKey());

						GroupMember groupMember = new GroupMember();
						groupMember.id = wamtDomain.getPrimaryKey();
						groupMember.name = wamtDomain.getDisplayName();
						groupMember.type = GroupMember.Type.DOMAIN;

						result.add(groupMember);
					}
					catch (DeletedException e)
					{
						/*
						 * If the domain has been deleted, it won't be in the group any more.
						 */
					}
				}
				else
				{
					if (logger.isDebugEnabled()) logger.debug("getGroupMembers", "Could not convert class for tagged entity: " + clazz.getName());
				}
			}
		}

		if (logger.isEntryEnabled()) logger.exit("getGroupMembers", result);
		return result;
	}
}
