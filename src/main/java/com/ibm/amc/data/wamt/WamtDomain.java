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
package com.ibm.amc.data.wamt;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.core.Response.Status;

import com.ibm.amc.AmcRuntimeException;
import com.ibm.amc.Constants;
import com.ibm.amc.FileManager;
import com.ibm.amc.data.SvrDomain;
import com.ibm.amc.data.SvrService;
import com.ibm.amc.data.SvrServiceImpactDescriptor;
import com.ibm.amc.resources.data.DomainDeploymentConfiguration;
import com.ibm.amc.resources.data.ServiceDeploymentConfiguration;
import com.ibm.amc.resources.data.ServiceImpactDescriptor.ObjectDescriptor;
import com.ibm.amc.resources.exceptions.AmcIllegalArgumentException;
import com.ibm.amc.resources.exceptions.ConcurrencyException;
import com.ibm.amc.resources.exceptions.HttpNotFoundException;
import com.ibm.amc.resources.exceptions.NoSuchResourceException;
import com.ibm.amc.security.SecurityContext;
import com.ibm.amc.server.action.ActionFactory;
import com.ibm.amc.server.action.ActionStatus;
import com.ibm.amc.server.action.AsyncAction;
import com.ibm.amc.server.action.impl.AbstractAction;
import com.ibm.amc.server.action.impl.AbstractProgressContainerAction;
import com.ibm.amc.server.action.impl.AbstractSyncAdapterAction;
import com.ibm.amc.server.impl.ApplianceManagerFactory;
import com.ibm.datapower.amt.DMgrException;
import com.ibm.datapower.amt.OperationStatus;
import com.ibm.datapower.amt.StringCollection;
import com.ibm.datapower.amt.amp.AMPException;
import com.ibm.datapower.amt.amp.ConfigObject;
import com.ibm.datapower.amt.clientAPI.ConfigService;
import com.ibm.datapower.amt.clientAPI.DeletedException;
import com.ibm.datapower.amt.clientAPI.DeploymentPolicy;
import com.ibm.datapower.amt.clientAPI.Device;
import com.ibm.datapower.amt.clientAPI.Domain;
import com.ibm.datapower.amt.clientAPI.DomainSynchronizationMode;
import com.ibm.datapower.amt.clientAPI.InUseException;
import com.ibm.datapower.amt.clientAPI.InvalidParameterException;
import com.ibm.datapower.amt.clientAPI.LockBusyException;
import com.ibm.datapower.amt.clientAPI.Manager;
import com.ibm.datapower.amt.clientAPI.MissingFeaturesInFirmwareException;
import com.ibm.datapower.amt.clientAPI.NotEmptyException;
import com.ibm.datapower.amt.clientAPI.NotExistException;
import com.ibm.datapower.amt.clientAPI.ProgressContainer;
import com.ibm.datapower.amt.clientAPI.RuntimeService;
import com.ibm.datapower.amt.clientAPI.ServiceConfiguration;
import com.ibm.datapower.amt.clientAPI.ServiceDeployment;
import com.ibm.datapower.amt.clientAPI.URLSource;
import com.ibm.datapower.amt.clientAPI.UnsuccessfulOperationException;
import com.ibm.datapower.amt.dataAPI.AlreadyExistsInRepositoryException;
import com.ibm.datapower.amt.dataAPI.DatastoreException;
import com.ibm.datapower.amt.dataAPI.DirtySaveException;

/**
 * The server-side representation of a Domain, that is backed by the Appliance Management
 * Toolkit.
 */
public class WamtDomain extends SvrDomain
{
	// @CLASS-COPYRIGHT@

	private com.ibm.datapower.amt.clientAPI.Domain wamtDomain;
	
	// The human-readable name of the domain, mostly for error messages.
	private String domainName;

	private ServiceDeployment serviceDeployment;

	// We're not using manager yet, but we may need to in future so make sure we have it.
	/**
	 * Create a WamtDomain that may or may not represent an existing real domain. If the given
	 * device doesn't contain a domain by this name, it will be created.
	 */
	public WamtDomain(Manager manager, Device wamtDevice, String domainName) throws DMgrException
	{
		if (logger.isEntryEnabled()) logger.entry("<init>", manager, wamtDevice, domainName);

		wamtDomain = wamtDevice.getManagedDomain(domainName);
		if (wamtDomain == null) wamtDomain = wamtDevice.createManagedDomain(domainName);
		
		this.domainName = domainName;

		if (logger.isEntryEnabled()) logger.exit("<init>");
	}

	/**
	 * Create a WamtDomain that represents a domain that already exists on a Datapower appliance,
	 * throwing NoSuchResourceException if it doesn't exist.
	 */
	public WamtDomain(Manager manager, String domainId)
	{
		if (logger.isEntryEnabled()) logger.entry("<init>", manager, domainId);

		wamtDomain = Domain.getByPrimaryKey(domainId);
		if (wamtDomain == null) throw new NoSuchResourceException(domainId);
		
		try
		{
			domainName = wamtDomain.getName();
		}
		catch (DeletedException e)
		{
			throw new NoSuchResourceException(domainId);
		}

		if (logger.isEntryEnabled()) logger.exit("<init>");
	}

	@Override
	public DomainStatus getStatus()
	{
		if (logger.isEntryEnabled()) logger.entry("getStatus");
		try
		{
			OperationStatus status = null;
			try
			{
				// Trying to get the status of a domain during deployment results in
				// a NullPointerException.
				status = wamtDomain.getDevice().getOperationStatusOfDomain(wamtDomain);
			}
			catch (NullPointerException npe)
			{
				// Do nothing, handled below
			}

			if (status == null)
			{
				if (logger.isDebugEnabled()) logger.debug("getStatus", "Returning unknown status because getOperationStatusOfDomain " + "returned null.", wamtDomain);
				if (logger.isEntryEnabled()) logger.exit("getStatus", DomainStatus.UNKNOWN);
				return DomainStatus.UNKNOWN;
			}
			DomainStatus result = DomainStatus.UNKNOWN;
			if (status.isDown()) result = DomainStatus.DOWN;
			if (status.isPartiallyUp()) result = DomainStatus.PARTIAL;
			if (status.isUp()) result = DomainStatus.UP;

			if (logger.isEntryEnabled()) logger.exit("getStatus", result);
			return result;
		}
		catch (DeletedException e)
		{
			throw new NoSuchResourceException(domainName);
		}
	}

	@Override
	public String getName()
	{
		try
		{
			return wamtDomain.getName();
		}
		catch (DeletedException e)
		{
			throw new NoSuchResourceException(domainName);
		}
	}

	@Override
	public String getPrimaryKey()
	{
		try
		{
			return wamtDomain.getPrimaryKey();
		}
		catch (DeletedException e)
		{
			throw new NoSuchResourceException(domainName);
		}
	}

	@Override
	public String getSourceConfigurationUrl()
	{
		URLSource sc;
		try
		{
			String result = null;
			sc = wamtDomain.getSourceConfiguration();
			if (sc != null) result = sc.getURL();

			// Don't reveal full filesystem paths. Send "file://" on the ReST
			// API, which the GUI will turn into something friendlier.
			// String.startsWith seems a bit loose compared to URL.getScheme, but
			// really they're exactly equivalent.
			if (result != null && result.startsWith("file:")) result = "file://";

			return result;
		}
		catch (DeletedException e)
		{
			throw new NoSuchResourceException(domainName);
		}
	}

	@Override
	public int getQuiesceTimeout()
	{
		return wamtDomain.getQuiesceTimeout();
	}

	@Override
	public String getDisplayName()
	{
		return wamtDomain.getRelativeDisplayName();
	}

	@Override
	public String getDeploymentPolicy()
	{
		try
		{
			String result = null;
			DeploymentPolicy dp = wamtDomain.getDeploymentPolicy();
			if (dp != null) result = dp.getPolicyObjectName();

			return result;
		}
		catch (DeletedException e)
		{
			throw new NoSuchResourceException(domainName);
		}
	}

	@Override
	public String getApplianceName()
	{
		try
		{
			return wamtDomain.getDevice().getSymbolicName();
		}
		catch (DeletedException e)
		{
			throw new NoSuchResourceException(domainName);
		}
	}

	@Override
	public String getApplianceId()
	{
		try
		{
			return wamtDomain.getDevice().getPrimaryKey();
		}
		catch (DeletedException e)
		{
			throw new NoSuchResourceException(domainName);
		}
	}

	@Override
	public SynchronizationMode getSynchronizationMode()
	{
		DomainSynchronizationMode syncMode = null;

		try
		{
			syncMode = wamtDomain.getSynchronizationMode();

			SynchronizationMode result = SynchronizationMode.MANUAL;

			if (syncMode == DomainSynchronizationMode.AUTO)
			{
				result = SynchronizationMode.AUTO;
			}

			if (logger.isEntryEnabled()) logger.exit("getSynchronizationMode", result);

			return result;

		}
		catch (DeletedException e)
		{
			throw new NoSuchResourceException(domainName);
		}
	}

	@Override
	public String getDeploymentPolicyUrl()
	{
		try
		{
			String result = null;
			DeploymentPolicy dp = wamtDomain.getDeploymentPolicy();
			if (dp != null)
			{
				URLSource src = dp.getPolicyURLSource();
				if (src != null) result = src.getURL();

				// Don't reveal full filesystem paths.
				if (result != null && result.startsWith("file:")) result = "file://";
			}

			return result;
		}
		catch (DeletedException e)
		{
			throw new NoSuchResourceException(domainName);
		}
	}
	
	

	@Override
	public String getDeploymentPolicyDomainName()
	{
		try
		{
			String result = null;
			DeploymentPolicy dp = wamtDomain.getDeploymentPolicy();
			if (dp != null)
			{
				result = dp.getPolicyDomainName();
			}

			return result;
		}
		catch (DeletedException e)
		{
			throw new NoSuchResourceException(domainName);
		}
	}

	@Override
	public String getDeploymentPolicyObjectName()
	{
		try
		{
			String result = null;
			DeploymentPolicy dp = wamtDomain.getDeploymentPolicy();
			if (dp != null)
			{
				result = dp.getPolicyObjectName();
			}

			return result;
		}
		catch (DeletedException e)
		{
			throw new NoSuchResourceException(domainName);
		}
	}

	@Override
	public String quiesce()
	{
		if (logger.isEntryEnabled()) logger.entry("quiesce");

		final AsyncAction quiesceAction = new AbstractSyncAdapterAction("quiesce", domainName, "CWZBA1027I_QUIESCE_DOMAIN_ACTION_DESCRIPTION", domainName, getApplianceName())
		{
			@Override
			public void execute() throws Throwable
			{
				try
				{
					wamtDomain.quiesce();
				}
				catch (DeletedException e)
				{
					throw new NoSuchResourceException(domainName);
				}
				catch (NotExistException e)
				{
					throw new NoSuchResourceException(domainName);
				}
				catch (AMPException e)
				{
					throw new AmcRuntimeException(e);
				}
				catch (UnsuccessfulOperationException e)
				{
					throw new AmcRuntimeException(e);
				}
			}
		};
		final String actionId = ActionFactory.getActionController().submitAction(quiesceAction);

		if (logger.isEntryEnabled()) logger.exit("quiesce", actionId);
		return actionId;
	}

	@Override
	public String unquiesce()
	{
		if (logger.isEntryEnabled()) logger.entry("unquiesce");

		final AsyncAction unquiesceAction = new AbstractSyncAdapterAction("quiesce", domainName, "CWZBA1028I_UNQUIESCE_DOMAIN_ACTION_DESCRIPTION", domainName, getApplianceName())
		{
			@Override
			public void execute() throws Throwable
			{
				try
				{
					wamtDomain.unquiesce();
				}
				catch (DeletedException e)
				{
					throw new NoSuchResourceException(domainName);
				}
				catch (NotExistException e)
				{
					throw new NoSuchResourceException(domainName);
				}
				catch (AMPException e)
				{
					throw new AmcRuntimeException(e);
				}
				catch (UnsuccessfulOperationException e)
				{
					throw new AmcRuntimeException(e);
				}
			}
		};

		final String actionId = ActionFactory.getActionController().submitAction(unquiesceAction);

		if (logger.isEntryEnabled()) logger.exit("unquiesce", actionId);
		return actionId;
	}

	@Override
	public String deploy(final DomainDeploymentConfiguration deploymentSource)
	{
		if (logger.isEntryEnabled()) logger.entry("deploy", deploymentSource);
		
		try
		{
			// Set synch mode to manual while setting deployment source and policy,
			// as documented in WAMT javadoc.
			final DomainSynchronizationMode oldSynchMode = wamtDomain.getSynchronizationMode();
			wamtDomain.setSynchronizationMode(DomainSynchronizationMode.MANUAL);
			
			final DeploymentPolicy oldDeploymentPolicy = wamtDomain.getDeploymentPolicy();
			final URLSource oldDeploymentPolicySource = (oldDeploymentPolicy == null) ? null : oldDeploymentPolicy.getPolicyURLSource();
			final String oldDeploymentPolicyDomain = (oldDeploymentPolicy == null) ? null : oldDeploymentPolicy.getPolicyDomainName();
			final String oldDeploymentPolicyName = (oldDeploymentPolicy == null) ? null : oldDeploymentPolicy.getPolicyObjectName();
			final URLSource oldConfigSource = wamtDomain.getSourceConfiguration();

			URI depPolUrl = deploymentSource.deploymentPolicyLocation;

			if (depPolUrl != null)
			{
				wamtDomain.setDeploymentPolicy(new URLSource(FileManager.resolveUri(depPolUrl).toString()), deploymentSource.domainName, deploymentSource.policyName);
			}
			else
			{
				wamtDomain.setDeploymentPolicy(null, null, null);
			}

			URI configSourceUrl = deploymentSource.sourceConfigLocation;

			configSourceUrl = FileManager.resolveUri(configSourceUrl);

			if (logger.isDebugEnabled()) logger.debug("deploy", "Setting source configuration URL to: " + configSourceUrl);
			wamtDomain.setSourceConfiguration(new URLSource(configSourceUrl.toString()));

			final AbstractAction deployAction = new AbstractProgressContainerAction("deployDomain", getPrimaryKey(), "CWZBA1029I_DEPLOY_DOMAIN_ACTION_DESCRIPTION", domainName, getApplianceName())
			{
				@Override
				public ProgressContainer submit(final String id) throws Exception
				{
					try
					{
						return wamtDomain.deployConfiguration();
					}
					catch (final DeletedException e)
					{
						throw new NoSuchResourceException(domainName);
					}
				}

				@Override
				public void succeeded(ActionStatus statusHandle)
				{
					super.succeeded(statusHandle);

					Boolean autoSync = deploymentSource.automaticSynchronization;
					if (autoSync != null)
					{
						if (autoSync.equals(Boolean.TRUE))
						{
							setSynchronizationMode(DomainSynchronizationMode.AUTO);
						}
						else
						{
							setSynchronizationMode(DomainSynchronizationMode.MANUAL);
						}
					}
					else
					{
						setSynchronizationMode(oldSynchMode);
					}

				}

				@Override
				public void failed(ActionStatus statusHandle, Throwable exception)
				{
					if (exception instanceof FileNotFoundException)
					{
						exception = new HttpNotFoundException(exception.getMessage(), "CWZBA1029I_DEPLOY_DOMAIN_ACTION_DESCRIPTION", domainName, getApplianceName());
					}
					
					super.failed(statusHandle, exception);

					// Reset original values
					try
					{
						wamtDomain.setDeploymentPolicy(oldDeploymentPolicySource, oldDeploymentPolicyDomain, oldDeploymentPolicyName);
						wamtDomain.setSourceConfiguration(oldConfigSource);
					}
					catch (Throwable t)
					{
						if (logger.isDebugEnabled()) logger.debug("failed", "Failed to reset original domain source properties:" + t);
					}
					setSynchronizationMode(oldSynchMode);
				}

			};
			final String actionId = ActionFactory.getActionController().submitAction(deployAction);

			if (logger.isEntryEnabled()) logger.exit("deploy", actionId);
			return actionId;
		}
		catch (DeletedException e)
		{
			throw new NoSuchResourceException(domainName);
		}
		catch (DirtySaveException e)
		{
			throw new ConcurrencyException(e, domainName);
		}
		catch (MissingFeaturesInFirmwareException e)
		{
			throw new AmcRuntimeException(Status.BAD_REQUEST, e);
		}
		catch (Exception e)
		{
			throw new AmcRuntimeException(e);
		}
	}

	protected void setSynchronizationMode(DomainSynchronizationMode syncronizationMode)
	{
		try
		{
			wamtDomain.setSynchronizationMode(syncronizationMode);
		}
		catch (DeletedException e)
		{
			throw new NoSuchResourceException(domainName);
		}
		catch (DirtySaveException e)
		{
			throw new ConcurrencyException(e, domainName);
		}
		catch (Exception e)
		{
			throw new AmcRuntimeException(e);
		}
	}

	@Override
	public void delete()
	{
		if (logger.isEntryEnabled()) logger.entry("delete");

		ActionStatus status = ActionFactory.getActionLog().actionStarted(SecurityContext.getContext().getUser(), domainName, "delete domain", "CWZBA1026I_DELETE_DOMAIN_ACTION_DESCRIPTION",
				domainName, getApplianceName());

		try
		{
			try
			{
				Device device = wamtDomain.getDevice();
				unmanageDomain(device, 1);
				device.deleteDomain(domainName);
			}
			catch (DeletedException e)
			{
				throw new NoSuchResourceException(domainName);
			}
			catch (NotExistException e)
			{
				throw new NoSuchResourceException(domainName);
			}
			catch (InUseException e)
			{
				throw new AmcRuntimeException(e);
			}
			catch (InvalidParameterException e)
			{
				throw new AmcRuntimeException(e);
			}
			catch (NotEmptyException e)
			{
				throw new AmcRuntimeException(e);
			}
			catch (DatastoreException e)
			{
				throw new AmcRuntimeException(e);
			}
			catch (AMPException e)
			{
				throw new AmcRuntimeException(e);
			}
		}
		catch (RuntimeException e)
		{
			status.failed(e);
			throw e;
		}
		status.succeeded();

		if (logger.isEntryEnabled()) logger.exit("delete");
	}

	@Override
	public void updateProperties(com.ibm.amc.resources.data.Domain domain)
	{
		if (logger.isEntryEnabled()) logger.entry("updateProperties", domain);

		final ActionStatus status = ActionFactory.getActionLog().actionStarted(SecurityContext.getContext().getUser(), getPrimaryKey(), "update", "CWZBA1036I_UPDATE_DOMAIN_ACTION_DESCRIPTION", domainName,
				getApplianceName());

		try
		{
			Integer timeout = domain.quiesceTimeout;
			if (timeout != null && timeout.intValue() != wamtDomain.getQuiesceTimeout())
			{
				try
				{
					wamtDomain.setQuiesceTimeout(timeout);
				}
				catch (DeletedException e)
				{
					throw new NoSuchResourceException(domainName);
				}
				catch (AlreadyExistsInRepositoryException e)
				{
					// This throw doesn't make sense: We want the domain to
					// exist in order to change properties. Using general
					// exception for this case
					throw new AmcRuntimeException(e);
				}
				catch (InvalidParameterException e)
				{
					throw new AmcIllegalArgumentException(e, "CWZBA0539E_INVALID_QUIESCE_TIMEOUT", Integer.toString(timeout), Integer.toString(Constants.QUIESCE_TIMEOUT_MIN),
							Integer.toString(Constants.QUIESCE_TIMEOUT_MAX));
				}
				catch (DatastoreException e)
				{
					throw new AmcRuntimeException(e);
				}
			}

			Boolean autoSync = domain.automaticSynchronization;
			if (autoSync != null)
			{
				if (autoSync.equals(Boolean.TRUE))
				{
					setSynchronizationMode(DomainSynchronizationMode.AUTO);
				}
				else
				{
					setSynchronizationMode(DomainSynchronizationMode.MANUAL);
				}
			}
		}
		catch (final RuntimeException ex)
		{
			status.failed(ex);
			throw ex;
		}

		status.succeeded();

		if (logger.isEntryEnabled()) logger.exit("updateProperties");
	}

	@Override
	public List<SvrService> getServices()
	{
		List<SvrService> services = new ArrayList<SvrService>();
		try
		{
			for (RuntimeService wamtService : wamtDomain.getServices())
			{
				services.add(new WamtService(this, wamtService));
			}
		}
		catch (final DeletedException e)
		{
			throw new NoSuchResourceException(domainName);
		}
		catch (final UnsuccessfulOperationException e)
		{
			throw new AmcRuntimeException(e);
		}
		return services;
	}

	@Override
	public String deployService(String type, String name, final ServiceDeploymentConfiguration deploymentSource)
	{
		try
		{
			if (logger.isEntryEnabled()) logger.entry("deployService", type, name, deploymentSource);

			/* extract all the services contained in the configuration source */
			ServiceConfiguration serviceConfig = new ServiceConfiguration(new URLSource(FileManager.resolveUriToUrl(deploymentSource.configLocation).toString()));
			ConfigService[] availableServices = serviceConfig.getAvailableServices(wamtDomain.getDevice());

			/* find the service that is to be deployed */
			ConfigService service = null;

			for (ConfigService cs : availableServices)
			{
				if (cs.getClassName().equals(type) && cs.getName().equals(name))
				{
					if (logger.isDebugEnabled()) logger.debug("deployService", "Service configuration found for " + type + ":" + name);
					service = cs;
					break;
				}
			}

			/* provided we found the service to be deployed, we continue... */
			if (service == null)
			{
				throw new NoSuchResourceException(type + ":" + name);
			}

			/* mark the service for deployment */
			ConfigService[] serviceDeploy = new ConfigService[] { service };
			serviceConfig.setServicesForDeployment(serviceDeploy);

			/* create the service deployment object, with or without a deployment policy */
			if (deploymentSource.policyLocation == null)
			{
				if (logger.isDebugEnabled()) logger.debug("deployService", "No deployment policy will be applied");
				serviceDeployment = wamtDomain.createServiceDeployment(serviceConfig, deploymentSource.importAllFiles);
			}
			else
			{
				if (logger.isDebugEnabled()) logger.debug("deployService", "Applying deployment policy");
				serviceDeployment = wamtDomain.createServiceDeployment(serviceConfig, new URLSource(FileManager.resolveUriToUrl(deploymentSource.policyLocation).toString()),
						deploymentSource.policyDomain, deploymentSource.policyName, deploymentSource.importAllFiles);
			}

			/* perform the deploy */
			final AbstractAction deployAction = new AbstractProgressContainerAction("deployService", getPrimaryKey(), "CWZBA1030I_DEPLOY_SERVICE_ACTION_DESCRIPTION", service.getName(),
					service.getClassDisplayName(), domainName, getApplianceName())
			{
				@Override
				public ProgressContainer submit(final String id) throws Exception
				{
					try
					{
						return serviceDeployment.deployServiceConfiguration();
					}
					catch (final DeletedException e)
					{
						throw new NoSuchResourceException(domainName);
					}
				}

				@Override
				public void succeeded(ActionStatus statusHandle)
				{
					super.succeeded(statusHandle);
					FileManager.decrementReferenceCount(deploymentSource.configLocation);
					FileManager.decrementReferenceCount(deploymentSource.policyLocation);
				}

				@Override
				public void failed(ActionStatus statusHandle, Throwable exception)
				{
					super.failed(statusHandle, exception);
					FileManager.decrementReferenceCount(deploymentSource.configLocation);
					FileManager.decrementReferenceCount(deploymentSource.policyLocation);
				}
			};

			final String actionId = ActionFactory.getActionController().submitAction(deployAction);

			if (logger.isEntryEnabled()) logger.exit("deployService", actionId);
			return actionId;
		}
		catch (URISyntaxException e)
		{
			throw new AmcIllegalArgumentException(Status.BAD_REQUEST, e, "CWZBA2004E_INVALID_URI_STRING", deploymentSource.configLocation.toString());
		}
		catch (DirtySaveException e)
		{
			throw new ConcurrencyException(e, domainName);
		}
		catch (MissingFeaturesInFirmwareException e)
		{
			throw new AmcRuntimeException(Status.BAD_REQUEST, e);
		}
		catch (Exception e)
		{
			throw new AmcRuntimeException(e);
		}
	}

	@Override
	public List<SvrServiceImpactDescriptor> deployServiceImpact(String type, String name, ServiceDeploymentConfiguration deploymentSource)
	{
		try
		{
			if (logger.isEntryEnabled()) logger.entry("deployServiceImpact", type, name, deploymentSource);

			/* extract all the services contained in the configuration source */
			ServiceConfiguration serviceConfig = new ServiceConfiguration(new URLSource(FileManager.resolveUriToUrl(deploymentSource.configLocation).toString()));
			ConfigService[] availableServices = serviceConfig.getAvailableServices(wamtDomain.getDevice());

			/* find the service that is to be deployed */
			ConfigService service = null;

			for (ConfigService cs : availableServices)
			{
				if (cs.getClassName().equals(type) && cs.getName().equals(name))
				{
					if (logger.isDebugEnabled()) logger.debug("deployServiceImpact", "Service configuration found: " + type + ":" + name + ":" + cs.getUserComment());
					service = cs;
					break;
				}
			}

			/* provided we found the service to determine the deployment impact of, we continue... */
			if (service == null)
			{
				throw new NoSuchResourceException(type + ":" + name);
			}
			else
			{
				/* mark the service for deployment */
				ConfigService[] serviceDeploy = new ConfigService[] { service };
				serviceConfig.setServicesForDeployment(serviceDeploy);

				/* create the service deployment object, with or without a deployment policy */
				if (deploymentSource.policyLocation == null)
				{
					if (logger.isDebugEnabled()) logger.debug("deployServiceImpact", "No deployment policy will be applied");
					serviceDeployment = wamtDomain.createServiceDeployment(serviceConfig, deploymentSource.importAllFiles);
				}
				else
				{
					if (logger.isDebugEnabled()) logger.debug("deployServiceImpact", "Applying deployment policy");
					serviceDeployment = wamtDomain.createServiceDeployment(serviceConfig, new URLSource(FileManager.resolveUriToUrl(deploymentSource.policyLocation).toString()),
							deploymentSource.policyDomain, deploymentSource.policyName, deploymentSource.importAllFiles);
				}

				/* create the empty result object */
				ArrayList<SvrServiceImpactDescriptor> result = new ArrayList<SvrServiceImpactDescriptor>();

				/* get hold of the files and objects to be overwritten */
				StringCollection filesToBeOverwritten = serviceDeployment.getFilesToBeOverwritten();
				ConfigObject[] objectsToBeOverwritten = serviceDeployment.getObjectsToBeOverwritten();

				if (logger.isDebugEnabled())
				{
					if (filesToBeOverwritten != null) logger.debug("deployServiceImpact", "Files to be overwritten: " + filesToBeOverwritten.size());
					if (objectsToBeOverwritten != null) logger.debug("deployServiceImpact", "Objects to be overwritten: " + objectsToBeOverwritten.length);
				}

				/* get hold of the interdependent services */
				RuntimeService[] interDepServices = serviceDeployment.getInterDependentServices();

				/*
				 * if we find some interdependent services, get any objects or files to be
				 * overwritten
				 */
				if (interDepServices != null && interDepServices.length > 0)
				{
					if (logger.isDebugEnabled()) logger.debug("deployServiceImpact", "Interdependent services found: " + interDepServices.length);

					/* now iterate through and build up the impact data */
					for (RuntimeService runtimeService : interDepServices)
					{
						if (logger.isDebugEnabled()) logger.debug("deployServiceImpact", "\tInterdependent service - Class: " + runtimeService.getClassName() + " - Name: " + runtimeService.getName()
								+ " - User comment: " + runtimeService.getUserComment());

						WamtServiceImpactDescriptor impactDescriptor = new WamtServiceImpactDescriptor(runtimeService);

						/* get the referenced files and objects */
						StringCollection fileList = runtimeService.getReferencedObjectsAndFiles().getReferencedFiles();
						ConfigObject[] referencedObjects = runtimeService.getReferencedObjectsAndFiles().getReferencedObjects();

						/* process the listed files */
						if (fileList != null && fileList.size() > 0)
						{
							if (logger.isDebugEnabled()) logger.debug("deployServiceImpact", "\tNumber of listed files: " + fileList.size());

							/* step through each file listed by the current interdependent service */
							for (int i = 0; i < fileList.size(); i++)
							{
								if (logger.isDebugEnabled()) logger.debug("deployServiceImpact", "\t\tListed file: " + fileList.get(i));

								/*
								 * and check if the referenced object matches up with an object to
								 * be overwritten
								 */
								for (int j = 0; j < filesToBeOverwritten.size(); j++)
								{
									if (logger.isDebugEnabled()) logger.debug("deployServiceImpact", "\t\t\tFile to be overwritten: " + filesToBeOverwritten.get(j));

									if (filesToBeOverwritten.get(j).equals(fileList.get(i)))
									{
										/* scurry away any matches */
										impactDescriptor.addFile("");
									}
								}
							}
						}
						else
						{
							if (logger.isDebugEnabled()) logger.debug("deployServiceImpact", "\tNo files listed for service " + runtimeService.getPrimaryKey());
						}

						/* process the referenced objects */
						if (referencedObjects != null && referencedObjects.length > 0)
						{
							if (logger.isDebugEnabled()) logger.debug("deployServiceImpact", "\tNumber of referenced objects: " + referencedObjects.length);

							/*
							 * step through each object referenced from the current interdependent
							 * service
							 */
							for (ConfigObject referencedObject : referencedObjects)
							{
								if (logger.isDebugEnabled()) logger.debug("deployServiceImpact", "\t\tReferenced object: " + referencedObject);

								/*
								 * and check if the referenced object matches up with an object to
								 * be overwritten
								 */
								for (ConfigObject objectToBeOverwritten : objectsToBeOverwritten)
								{
									if (logger.isDebugEnabled()) logger.debug("deployServiceImpact", "\t\t\tObject to be overwritten: " + objectToBeOverwritten);

									if (objectToBeOverwritten.getClassName().equals(referencedObject.getClassName()) && objectToBeOverwritten.getName().equals(referencedObject.getName()))
									{
										/* scurry away any matches */
										impactDescriptor.addObjectDescriptor(new ObjectDescriptor(referencedObject.getClassDisplayName(), referencedObject.getName()));
									}
								}
							}
						}
						else
						{
							if (logger.isDebugEnabled()) logger.debug("deployServiceImpact", "\tNo referenced objects for service " + runtimeService.getPrimaryKey());
						}

						/* finished processing the current runtime service, save it in the result */
						result.add(impactDescriptor);
					}
				}
				else
				{
					if (logger.isDebugEnabled()) logger.debug("deployServiceImpact", "No interdependent services found");
				}

				if (logger.isEntryEnabled()) logger.exit("deployServiceImpact", result);
				return result;
			}
		}
		catch (URISyntaxException e)
		{
			throw new AmcIllegalArgumentException(Status.BAD_REQUEST, e, "CWZBA2004E_INVALID_URI_STRING", deploymentSource.configLocation.toString());
		}
		catch (DirtySaveException e)
		{
			throw new ConcurrencyException(e, domainName);
		}
		catch (MissingFeaturesInFirmwareException e)
		{
			throw new AmcRuntimeException(Status.BAD_REQUEST, e);
		}
		catch (Exception e)
		{
			throw new AmcRuntimeException(e);
		}
	}

	@Override
	public List<String> getGroups()
	{
		if (logger.isEntryEnabled()) logger.entry("getGroups");
		List<String> result = new ArrayList<String>();

		Set<String> groups = null;

		try
		{
			groups = wamtDomain.getTagValues(Constants.GROUPS_TAG_KEY);
		}
		catch (DeletedException e)
		{
			throw new NoSuchResourceException(domainName);
		}
		catch (InvalidParameterException e)
		{
			throw new AmcRuntimeException(e);
		}

		if (groups != null)
		{
			result.addAll(groups);
		}

		if (logger.isEntryEnabled()) logger.exit("getGroups", result);
		return result;
	}

	@Override
	public void updateGroups(List<String> groups)
	{
		if (logger.isEntryEnabled()) logger.entry("updateGroups", groups);

		final ActionStatus status = ActionFactory.getActionLog().actionStarted(SecurityContext.getContext().getUser(), getPrimaryKey(), "updateGroups", "CWZBA1042I_UPDATE_DOMAIN_GROUPS_ACTION", domainName);

		try
		{
			try
			{
				/* remove the old groups */
				wamtDomain.removeTag(Constants.GROUPS_TAG_KEY);

				/* add the new groups */
				for (String group : groups)
				{
					wamtDomain.addTag(Constants.GROUPS_TAG_KEY, group);
				}
			}
			catch (DeletedException e)
			{
				throw new NoSuchResourceException(domainName);
			}
			catch (DirtySaveException e)
			{
				throw new ConcurrencyException(e, domainName);
			}
			catch (DatastoreException e)
			{
				throw new AmcRuntimeException(e);
			}
			catch (InvalidParameterException e)
			{
				throw new AmcRuntimeException(e);
			}
		}
		catch (final RuntimeException exc)
		{
			status.failed(exc);
			throw exc;
		}

		status.succeeded();

		if (logger.isEntryEnabled()) logger.exit("updateGroups");
	}

	@Override
	public SvrService getService(final String type, final String name)
	{
		try
		{
			for (RuntimeService wamtService : wamtDomain.getServices())
			{
				if (wamtService.getName().equals(name) && wamtService.getClassName().equals(type))
				{
					return new WamtService(this, wamtService);
				}
			}
		}
		catch (final DeletedException e)
		{
			throw new NoSuchResourceException(domainName);
		}
		catch (final UnsuccessfulOperationException e)
		{
			throw new AmcRuntimeException(e);
		}
		throw new NoSuchResourceException(type + "/" + name);
	}

	/*
	 * TODO - temporary workaround until WAMT provides a new locking mechanism with a wait
	 * 
	 * One of the exceptions thrown by device.removeManagedDomain() is LockBusyException. It is
	 * feasible to retry the action if this exception is caught. That is the purpose of separating
	 * this method out of the existing delete() method.
	 * 
	 * Five attempts will be made, allowing a short sleep between each. If the exception is still
	 * thrown, re-throw wrapped in an AmcRuntimeException.
	 * 
	 * @param device the device to remove the managed domain from
	 * 
	 * @param attemptCount remember how many attempts have been made
	 * 
	 * @throws DeletedException
	 * 
	 * @throws NotExistException
	 * 
	 * @throws InUseException
	 * 
	 * @throws InvalidParameterException
	 * 
	 * @throws NotEmptyException
	 * 
	 * @throws DatastoreException
	 */
	private void unmanageDomain(Device device, int attemptCount) throws DeletedException, NotExistException, InUseException, InvalidParameterException, NotEmptyException, DatastoreException
	{
		try
		{
			device.removeManagedDomain(domainName);
		}
		catch (LockBusyException e)
		{
			if (logger.isDebugEnabled()) logger.debug("unmanageDomain", "Caught a LockBusyException on attempt " + attemptCount);

			/* wait half a second and retry up to 5 times */
			if (attemptCount <= 5)
			{
				try
				{
					Thread.sleep(500);
				}
				catch (InterruptedException ie)
				{
					logger.error("InterruptedException thrown during retry while trying to remove a managed domain", ie);
				}
				/* recursive call */
				unmanageDomain(device, ++attemptCount);
			}
			/* if still failing, re-throw the exception */
			else
			{
				throw new AmcRuntimeException(e);
			}
		}
	}

	@Override
	public List<File> getLocalFilesInUse()
	{
		if (logger.isEntryEnabled()) logger.entry("getLocalFilesInUse");

		String deploymentPolicy = null;
		String configurationSource = null;

		try
		{
			if (wamtDomain.getSynchronizationMode() == DomainSynchronizationMode.MANUAL)
			{
				if (logger.isDebugEnabled()) logger.debug("getLocalFilesInUse()", "Synch mode for " + domainName + " is manual, so no files are 'in use'.");
				if (logger.isEntryEnabled()) logger.exit("getLocalFilesInUse");
				return Collections.emptyList();
			}

			DeploymentPolicy policy = wamtDomain.getDeploymentPolicy();
			if (policy != null && policy.getPolicyURLSource() != null)
			{
				deploymentPolicy = policy.getPolicyURLSource().getURL();
				if (!deploymentPolicy.startsWith("file://")) deploymentPolicy = null; // Only
																						// interested
																						// in local
																						// files.
			}

			URLSource source = wamtDomain.getSourceConfiguration();
			if (source != null)
			{
				configurationSource = source.getURL();
				if (!configurationSource.startsWith("file://")) configurationSource = null; // Only
																							// interested
																							// in
																							// local
																							// files.
			}
		}
		catch (DeletedException e)
		{
			throw new NoSuchResourceException(domainName);
		}

		if (deploymentPolicy == null && configurationSource == null)
		{
			if (logger.isDebugEnabled()) logger.debug("getLocalFilesInUse()", "DeploymentPolicy and ConfigurationSource both unset or non-local.");
			if (logger.isEntryEnabled()) logger.exit("getLocalFilesInUse");
			return Collections.emptyList();
		}

		ArrayList<File> files = new ArrayList<File>();
		try
		{
			if (deploymentPolicy != null) files.add(new File(new URI(deploymentPolicy)));
			if (configurationSource != null) files.add(new File(new URI(configurationSource)));
		}
		catch (URISyntaxException e)
		{
			throw new AmcRuntimeException(e); // URLs obtained from WAMT so *should* be valid.
		}

		if (logger.isEntryEnabled()) logger.exit("getLocalFilesInUse", files);
		return files;
	}

	@Override
	public String uploadFile(final URI source, final String fileName)
	{
		if (logger.isEntryEnabled()) logger.entry("uploadFile", source, fileName);

		final AsyncAction uploadFile = new AbstractSyncAdapterAction("uploadFile", domainName, "CWZBA1040I_UPLOAD_FILE_ACTION_DESCRIPTION", fileName, domainName, getApplianceName())
		{

			@Override
			public void execute() throws Throwable
			{
				URI sourceFile = FileManager.resolveUri(source);
				
				try
				{
					wamtDomain.uploadFile(fileName, new URLSource(sourceFile.toString()));
				}
				catch (DeletedException e)
				{
					throw new NoSuchResourceException(domainName);
				}
				catch (AMPException e)
				{
					throw new AmcRuntimeException(e);
				}
				catch (FileNotFoundException e)
				{
					if (source.getScheme().equalsIgnoreCase("wamctmp"))
					{
						// We've somehow managed to lose the file - or possibly a rogue
						// ReST request for a non-existent one. Use the original request
						// as insert; the actual filename from the exception would reveal
						// information about the server.
						throw new AmcRuntimeException(Status.INTERNAL_SERVER_ERROR, "CWZBA0535E_UPLOAD_FILE_NOT_FOUND", source.toString(), domainName);
					}
					// File wasn't on the remote server where the user said it was.
					// Insert the filename from the exception, what it was actually
					// trying to load, just in case it differs from what was requested.
					throw new AmcRuntimeException(Status.BAD_REQUEST, "CWZBA0536E_REMOTE_UPLOAD_FILE_NOT_FOUND", e.getMessage(), domainName);
				}
				catch (IOException e)
				{
					checkForRemoteFileHttpErrors(e);

					throw new AmcRuntimeException(e);
				}
				catch (URISyntaxException e)
				{
					throw new AmcRuntimeException(e);
				}
				catch (NotExistException e)
				{
					throw new AmcRuntimeException(e);
				}
				catch (UnsuccessfulOperationException e)
				{
					throw new AmcRuntimeException(e);
				}
				finally
				{
					FileManager.decrementReferenceCount(source);
				}
			}
		};
		final String actionId = ActionFactory.getActionController().submitAction(uploadFile);

		if (logger.isEntryEnabled()) logger.exit("uploadFile", actionId);
		return actionId;
	}

	public String restart()
	{
		if (logger.isEntryEnabled()) logger.entry("restart");

		final AsyncAction restart = new AbstractSyncAdapterAction("restart", domainName, "CWZBA1043I_RESTART_DOMAIN_ACTION_DESCRIPTION", domainName, getApplianceName())
		{

			@Override
			public void execute() throws Throwable
			{
				try
				{
					// Synchronize on the appliance object as WAMT is going to try and take a lock at that level
					synchronized(ApplianceManagerFactory.getInstance().getApplianceById(wamtDomain.getDevice().getPrimaryKey()))
					{
						wamtDomain.restart();
					}
				}
				catch (DeletedException e)
				{
					throw new NoSuchResourceException(domainName);
				}
				catch (UnsuccessfulOperationException e)
				{
					throw new AmcRuntimeException(e);
				}
				catch (LockBusyException e)
				{
					throw new ConcurrencyException(e, domainName);
				}
				catch (NotExistException e)
				{
					throw new NoSuchResourceException(domainName);
				}
				catch (AMPException e)
				{
					throw new AmcRuntimeException(e);
				}
			}
		};

		final String actionId = ActionFactory.getActionController().submitAction(restart);

		if (logger.isEntryEnabled()) logger.exit("restart", actionId);
		return actionId;
	}

	/**
	 * Examine an IOException for signs that it was thrown by WAMT while loading a file from a
	 * URLSource that is remote, and represents an HTTP error in that loading operation.
	 * IOExceptions thrown from eg Domain.uploadFile or other operations that can load their input
	 * via HTTP can be passed to this method. If they represent some other error, the method
	 * silently returns and the exception should be handled as normal. If they represent an HTTP
	 * error in loading, then an AmcRuntimeException with appropriate messages, response code, etc
	 * is thrown from within this method and should be allowed to propogate.
	 */
	private void checkForRemoteFileHttpErrors(IOException e)
	{
		// Checking text and throwing class is horrible, but can't see any other
		// way of identifying this case (there must be loads of potential
		// IOExceptions in eg a file-upload method). If this check fails, the
		// client still gets an "Unexpected error" dialog with all the
		// information, it's just less pretty.

		if (!(e.getMessage().startsWith("Server returned HTTP response code:") && e.getStackTrace()[0].getClassName().equals("sun.net.www.protocol.http.HttpURLConnection")))
		{
			return;
		}

		String originalMessage = e.getMessage();
		Matcher matcher = httpErrorPattern.matcher(originalMessage);
		if (!matcher.find()) return; // Should always match, but if not return just in case.
		int errorCode = Integer.parseInt(matcher.group(1));
		String errorText = Status.fromStatusCode(errorCode).getReasonPhrase();
		String url = matcher.group(2);

		throw new AmcRuntimeException(Status.BAD_REQUEST, "CWZBA0533E_REMOTE_FILE_HTTP_ERROR", errorCode + "", errorText, url);
	}

	private static final Pattern httpErrorPattern = Pattern.compile("Server returned HTTP response code: (\\d\\d\\d) for URL: (.*)");
}
