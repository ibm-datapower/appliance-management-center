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
import java.io.FileFilter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.FileUtils;

import com.ibm.amc.AmcRuntimeException;
import com.ibm.amc.Constants;
import com.ibm.amc.FileManager;
import com.ibm.amc.Util;
import com.ibm.amc.data.SvrAppliance;
import com.ibm.amc.data.SvrDomain;
import com.ibm.amc.data.SvrFirmware;
import com.ibm.amc.resources.TemporaryFileResource;
import com.ibm.amc.resources.data.Appliance;
import com.ibm.amc.resources.exceptions.AmcIllegalArgumentException;
import com.ibm.amc.resources.exceptions.ApplianceConnectionException;
import com.ibm.amc.resources.exceptions.ConcurrencyException;
import com.ibm.amc.resources.exceptions.InvalidCredentialsException;
import com.ibm.amc.resources.exceptions.NoSuchResourceException;
import com.ibm.amc.security.SecurityContext;
import com.ibm.amc.server.action.ActionFactory;
import com.ibm.amc.server.action.ActionStatus;
import com.ibm.amc.server.action.impl.AbstractAction;
import com.ibm.amc.server.action.impl.AbstractProgressContainerAction;
import com.ibm.datapower.amt.DMgrException;
import com.ibm.datapower.amt.DeviceType;
import com.ibm.datapower.amt.ModelType;
import com.ibm.datapower.amt.amp.AMPException;
import com.ibm.datapower.amt.amp.AMPIOException;
import com.ibm.datapower.amt.clientAPI.AlreadyExistsException;
import com.ibm.datapower.amt.clientAPI.DeletedException;
import com.ibm.datapower.amt.clientAPI.Device;
import com.ibm.datapower.amt.clientAPI.Firmware;
import com.ibm.datapower.amt.clientAPI.FirmwareVersion;
import com.ibm.datapower.amt.clientAPI.FullException;
import com.ibm.datapower.amt.clientAPI.InUseException;
import com.ibm.datapower.amt.clientAPI.InvalidParameterException;
import com.ibm.datapower.amt.clientAPI.LockBusyException;
import com.ibm.datapower.amt.clientAPI.ManagedSet;
import com.ibm.datapower.amt.clientAPI.ManagementOperations;
import com.ibm.datapower.amt.clientAPI.ManagementStatus;
import com.ibm.datapower.amt.clientAPI.Manager;
import com.ibm.datapower.amt.clientAPI.MinimumFirmwareLevel;
import com.ibm.datapower.amt.clientAPI.MissingFeaturesInFirmwareException;
import com.ibm.datapower.amt.clientAPI.NotEmptyException;
import com.ibm.datapower.amt.clientAPI.NotExistException;
import com.ibm.datapower.amt.clientAPI.ProgressContainer;
import com.ibm.datapower.amt.clientAPI.URLSource;
import com.ibm.datapower.amt.clientAPI.UnsuccessfulOperationException;
import com.ibm.datapower.amt.clientAPI.Version;
import com.ibm.datapower.amt.dataAPI.AlreadyExistsInRepositoryException;
import com.ibm.datapower.amt.dataAPI.DatastoreException;
import com.ibm.datapower.amt.dataAPI.DirtySaveException;

/**
 * An Appliance that is backed by the Appliance Management Toolkit.
 */
public class WamtAppliance extends SvrAppliance
{
	// @CLASS-COPYRIGHT@

	// WAMT manager to use with this appliance
	private Manager manager;
	
	// a copy of the WAMT version of this object
	private Device wamtDevice;

	// We pull out a copy of just the symbolic name, for use in error messages
	// when we find we can't access any other data about the appliance.
	private String applianceSymbolicName;

	private static String BACKUP_MANIFEST_FILE_NAME = "backupmanifest.xml";

	public WamtAppliance(Manager manager, Device wamtDevice) throws DeletedException
	{
		if (logger.isEntryEnabled()) logger.entry("<init>", manager, wamtDevice);

		this.manager = manager;
		this.wamtDevice = wamtDevice;
		applianceSymbolicName = wamtDevice.getSymbolicName();

		ensureDeviceManaged();

		if (logger.isEntryEnabled()) logger.exit("<init>");
	}

	/**
	 * Get an appliance that is already in the WAMT repository.
	 * 
	 * @throws NoSuchResourceException
	 *             if the appliance is not known to WAMT
	 */
	public WamtAppliance(Manager manager, String applianceId) throws NoSuchResourceException
	{
		if (logger.isEntryEnabled()) logger.entry("<init>", manager, applianceId);

		this.manager = manager;
		wamtDevice = getDeviceFromManager(applianceId, manager);

		try
		{
			applianceSymbolicName = this.wamtDevice.getSymbolicName();
		}
		catch (DeletedException e)
		{
			/* we have not be able to retrieve the name, so use the only identifier we have */
			throw new NoSuchResourceException(applianceId);
		}

		ensureDeviceManaged();

		if (logger.isEntryEnabled()) logger.exit("<init>");
	}

	/**
	 * Create a new appliance in the WAMT repository, using the connection details to access the
	 * appliance hardware.
	 * 
	 * @param manager
	 *            WAMT
	 * @param applianceConnection
	 *            The ReST object containing the properties needed to connect to the device.
	 * @throws ApplianceConnectionException
	 *             if the appliance can't be contacted with the given hostname and port
	 * @throws InvalidCredentialsException
	 *             if the username and password are not accepted
	 * @throws AmcIllegalArgumentException
	 *             if the appliance already exists in the WAMT repository
	 */
	public WamtAppliance(Manager manager, Appliance applianceConnection) throws ApplianceConnectionException, InvalidCredentialsException, AmcIllegalArgumentException
	{
		if (logger.isEntryEnabled()) logger.entry("<init>", manager, applianceConnection);

		this.manager = manager;
		applianceSymbolicName = applianceConnection.name;

		final ActionStatus status = ActionFactory.getActionLog().actionStarted(SecurityContext.getContext().getUser(), null, "add", "CWZBA1015I_ADD_APPLIANCE_ACTION_DESCRIPTION",
				applianceSymbolicName);

		try
		{
			ProgressContainer progressContainer;
			try
			{
				progressContainer = createDevice(applianceConnection);
			}
			catch (final FullException e)
			{
				throw new AmcRuntimeException(e);
			}

			try
			{
				progressContainer.blockAndTrace(Level.FINER);
			}
			catch (final Exception e)
			{
				// Remove the generic progress container wrapper exception if possible
				final Throwable cause = e.getCause();
				if (cause != null)
				{
					if (cause instanceof com.ibm.datapower.amt.amp.InvalidCredentialsException)
					{
						throw new InvalidCredentialsException();
					}
					if (cause instanceof AlreadyExistsInRepositoryException)
					{
						throw new AmcIllegalArgumentException(cause, "CWZBA0508E_APPLIANCE_ALREADY_EXISTS", applianceConnection.name, applianceConnection.hostName);
					}
					if (cause instanceof AMPIOException)
					{
						throw new ApplianceConnectionException(cause.getCause(), applianceConnection.hostName, applianceConnection.ampPort);
					}
					if (Util.getWamtExceptionCode(cause).equals("WAMT0805"))
					{
						throw new ApplianceConnectionException(cause.getCause(), applianceConnection.hostName, applianceConnection.ampPort);
					}
					throw new AmcRuntimeException(cause);
				}
				throw new AmcRuntimeException(e);
			}

			if (progressContainer.hasError())
			{
				Exception exception = progressContainer.getError();
				logger.error("CWZBA0002I_WAMT_ERROR_ADD_APPLIANCE", exception, exception.getMessage());
				throw new AmcRuntimeException(exception);
			}

			wamtDevice = (Device) progressContainer.getResult();
			
			try
			{
				ensureDeviceManaged();
			}
			catch (RuntimeException exc)
			{
				// Attempt to remove appliance again
				try
				{
					manager.remove(wamtDevice);
				}
				catch (Throwable t)
				{
					if (logger.isDebugEnabled()) logger.debug("<init>", "Failed to remove appliance: " + t);
				}
				throw exc;
			}

			// Retrieve domains now so that they are all managed before the appliance is made
			// available to competing threads
			getDomains();
		}
		catch (final ApplianceConnectionException exc)
		{
			status.failed(exc);
			throw exc;
		}
		catch (final RuntimeException exc)
		{
			status.failed(exc);
			throw exc;
		}

		status.succeeded();

		if (logger.isEntryEnabled()) logger.exit("<init>");
	}

	ProgressContainer createDevice(Appliance applianceConnection) throws FullException
	{
		return Device.createDevice(applianceConnection.name, applianceConnection.hostName, applianceConnection.adminUserId, applianceConnection.adminUserPassword, applianceConnection.ampPort);
	}

	/**
	 * Single entry point for obtaining appliances from WAMT
	 */
	private Device getDeviceFromManager(String applianceId, Manager manager) throws NoSuchResourceException
	{
		if (applianceId == null) throw new NoSuchResourceException(null);
		
		Device device = Device.getByPrimaryKey(applianceId);
		
		if (device == null)
		{
			throw new NoSuchResourceException(applianceId);
		}
		return device;
	}

	private void ensureDeviceManaged()
	{
		if (logger.isEntryEnabled()) logger.entry("ensureDeviceManaged", wamtDevice);
		try
		{
			ManagedSet defaultSet = manager.getManagedSet(Constants.DEFAULT_MANAGED_SET);

			if (defaultSet == null)
			{
				// sets are implicitly added to the manager when created.
				defaultSet = new ManagedSet(Constants.DEFAULT_MANAGED_SET);
			}
			ManagedSet existingSet = wamtDevice.getManagedSet();

			// Any set will do so only add to default set if not already in one
			if (existingSet == null)
			{
				defaultSet.addDevice(wamtDevice);
			}
		}
		catch (LockBusyException e)
		{
			throw new ConcurrencyException(e, applianceSymbolicName);
		}
		// The add rather rudely throws Exception when it should probably be a DMgrException.
		catch (Exception e)
		{
			throw new AmcRuntimeException(e);
		}
		if (logger.isEntryEnabled()) logger.exit("ensureDeviceManaged");
	}
	
	public Device getWamtDevice()
	{
		return wamtDevice;
	}

	@Override
	public String getPrimaryKey()
	{
		try
		{
			return wamtDevice.getPrimaryKey();
		}
		catch (DeletedException e)
		{
			throw new NoSuchResourceException(applianceSymbolicName);
		}
	}
	
	@Override
	public List<SvrDomain> getDomains() throws NoSuchResourceException
	{
		if (logger.isEntryEnabled()) logger.entry("getDomains");

		List<com.ibm.amc.data.SvrDomain> wamcDomains = new ArrayList<SvrDomain>();

		try
		{
			List<String> wamtDomains = Util.asList(wamtDevice.getAllDomainNames());

			for (String domainName : wamtDomains)
			{
				wamcDomains.add(new WamtDomain(manager, wamtDevice, domainName));
			}
		}
		catch (DMgrException e)
		{
			throw new AmcRuntimeException(e);
		}

		if (logger.isEntryEnabled()) logger.exit("getDomains", wamcDomains);
		return wamcDomains;
	}

	@Override
	public String getSerialNumber()
	{
		try
		{
			return wamtDevice.getSerialNumber();
		}
		catch (DeletedException e)
		{
			throw new NoSuchResourceException(applianceSymbolicName);
		}
	}

	@Override
	public int getQuiesceTimeout()
	{
		return wamtDevice.getQuiesceTimeout();
	}

	@Override
	public String getManagementStatusOfFirmware()
	{
		ManagementStatus fw = wamtDevice.getManagementStatusOfFirmware();
		if (fw != null) return fw.getDisplayName();
		return null;
	}

	@Override
	public String getApplianceType()
	{
		try
		{
			DeviceType dt = wamtDevice.getDeviceType();
			if (dt != null) return dt.getDisplayName();
			return null;
		}
		catch (DeletedException e)
		{
			throw new NoSuchResourceException(applianceSymbolicName);
		}
	}

	@Override
	public int getGUIPort()
	{
		try
		{
			return wamtDevice.getGUIPort();
		}
		catch (DeletedException e)
		{
			throw new NoSuchResourceException(applianceSymbolicName);
		}
	}

	@Override
	public List<String> getFeatureLicenses()
	{
		try
		{
			return Util.asList(wamtDevice.getFeatureLicenses());
		}
		catch (DeletedException e)
		{
			throw new NoSuchResourceException(applianceSymbolicName);
		}
	}

	@Override
	public String getModel()
	{
		try
		{
			ModelType mt = wamtDevice.getModelType();
			if (mt != null) return mt.getDisplayName();
			return null;
		}
		catch (DeletedException e)
		{
			throw new NoSuchResourceException(applianceSymbolicName);
		}
	}

	@Override
	public String getCurrentAMPVersion()
	{
		try
		{
			return wamtDevice.getCurrentAMPVersion();
		}
		catch (DeletedException e)
		{
			throw new NoSuchResourceException(applianceSymbolicName);
		}
	}

	@Override
	public String getActualFirmwareLevel()
	{
		return wamtDevice.getActualFirmwareLevel();
	}

	@Override
	public String getUserId()
	{
		try
		{
			return wamtDevice.getUserId();
		}
		catch (DeletedException e)
		{
			throw new NoSuchResourceException(applianceSymbolicName);
		}
	}

	@Override
	public int getAmpPort()
	{
		try
		{
			return wamtDevice.getHLMPort();
		}
		catch (DeletedException e)
		{
			throw new NoSuchResourceException(applianceSymbolicName);
		}
	}

	@Override
	public String getHostname()
	{
		try
		{
			return wamtDevice.getHostname();
		}
		catch (DeletedException e)
		{
			throw new NoSuchResourceException(applianceSymbolicName);
		}
	}

	@Override
	public String getName()
	{
		try
		{
			return wamtDevice.getSymbolicName();
		}
		catch (DeletedException e)
		{
			throw new NoSuchResourceException(applianceSymbolicName);
		}
	}

	@Override
	public void updateConnection(Appliance applianceConnection)
	{
		if (logger.isEntryEnabled()) logger.entry("updateConnection", applianceConnection);

		final ActionStatus status = ActionFactory.getActionLog().actionStarted(SecurityContext.getContext().getUser(), getPrimaryKey(), "update", "CWZBA1016I_UPDATE_APPLIANCE_ACTION_DESCRIPTION",
				applianceSymbolicName);

		try
		{
			try
			{
				String name = applianceConnection.name;
				if (name != null && !name.equals(wamtDevice.getSymbolicName())) wamtDevice.setSymbolicName(name);

				String hostName = applianceConnection.hostName;
				if (hostName != null && !hostName.equals(wamtDevice.getHostname())) wamtDevice.setHostname(hostName);

				Integer ampPort = applianceConnection.ampPort;
				if (ampPort != null && ampPort != wamtDevice.getHLMPort()) wamtDevice.setHLMPort(ampPort);

				String userId = applianceConnection.adminUserId;
				if (userId != null) wamtDevice.setUserId(userId);

				String password = applianceConnection.adminUserPassword;
				if (password != null) wamtDevice.setPassword(password);
			}
			catch (AlreadyExistsInRepositoryException e)
			{
				throw new AmcIllegalArgumentException(e, "CWZBA0508E_APPLIANCE_ALREADY_EXISTS", applianceConnection.name, applianceConnection.hostName);
			}
			catch (DeletedException e)
			{
				throw new NoSuchResourceException(applianceConnection.name);
			}
			catch (DatastoreException e)
			{
				throw new AmcRuntimeException(e);
			}
			catch (AlreadyExistsException e)
			{
				throw new AmcIllegalArgumentException(e, "CWZBA0508E_APPLIANCE_ALREADY_EXISTS", applianceConnection.name, applianceConnection.hostName);
			}

			Integer timeout = applianceConnection.quiesceTimeout;

			try
			{
				if (timeout != null) wamtDevice.setQuiesceTimeout(timeout);
			}
			catch (InvalidParameterException e)
			{
				throw new AmcIllegalArgumentException(e, "CWZBA0539E_INVALID_QUIESCE_TIMEOUT", Integer.toString(timeout), Integer.toString(Constants.QUIESCE_TIMEOUT_MIN),
						Integer.toString(Constants.QUIESCE_TIMEOUT_MAX));
			}
			catch (DeletedException e)
			{
				throw new NoSuchResourceException(applianceConnection.name);
			}
			catch (AlreadyExistsInRepositoryException e)
			{
				throw new AmcIllegalArgumentException(e, "CWZBA0508E_APPLIANCE_ALREADY_EXISTS", applianceConnection.name, applianceConnection.hostName);
			}
			catch (DatastoreException e)
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

		if (logger.isEntryEnabled()) logger.exit("updateConnection");
	}

	@Override
	public SvrDomain getDomain(String domainName)
	{
		if (logger.isEntryEnabled()) logger.entry("getDomain");
		SvrDomain result = null;
		try
		{
			if(wamtDevice.getManagedDomain(domainName) == null) {
				throw new NoSuchResourceException(domainName);
			}
			result = new WamtDomain(manager, wamtDevice, domainName);
		}
		catch (DMgrException e)
		{
			throw new AmcRuntimeException(e);
		}
		if (logger.isEntryEnabled()) logger.exit("getDomain", result);
		return result;
	}
	
	@Override
	// If the domain already exists this method will silently return.
	public SvrDomain createDomain(String domainName)
	{
		if (logger.isEntryEnabled()) logger.entry("createDomain", domainName);
		SvrDomain result = null;
		try
		{
			result = new WamtDomain(manager, wamtDevice, domainName);
		}
		catch (DMgrException e)
		{
			throw new AmcRuntimeException(e);
		}
		if (logger.isEntryEnabled()) logger.exit("createDomain", result);
		return result;
	}

	@Override
	public void remove()
	{
		if (logger.isEntryEnabled()) logger.entry("remove");

		final ActionStatus status = ActionFactory.getActionLog().actionStarted(SecurityContext.getContext().getUser(), getPrimaryKey(), "remove", "CWZBA1014I_REMOVE_APPLIANCE_ACTION_DESCRIPTION",
				applianceSymbolicName);

		try
		{
			try
			{
				try
				{
					// Explicitly try to remove the appliance from default managed
					// set as otherwise WAMT doesn't clean up the log target
					final ManagedSet defaultSet = manager.getManagedSet(Constants.DEFAULT_MANAGED_SET);
					defaultSet.removeDevice(wamtDevice);
				}
				catch (final Exception e)
				{
					// Not worth failing the attempt to remove the appliance for
					if (logger.isDebugEnabled()) logger.debug("removeAppliance", "Failed to remove appliance " + applianceSymbolicName + " from default managed set due to exception ", e);
				}

				manager.remove(wamtDevice);
			}
			catch (final NotExistException e)
			{
				throw new NoSuchResourceException(applianceSymbolicName);
			}
			catch (final DeletedException e)
			{
				throw new NoSuchResourceException(applianceSymbolicName);
			}
			catch (final DMgrException e)
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

		if (logger.isEntryEnabled()) logger.exit("remove");
	}

	@Override
	public String quiesce()
	{
		final AbstractAction quiesceAction = new AbstractAction("quiesce", getPrimaryKey(), "CWZBA1010I_QUIESCE_APPLIANCE_ACTION_DESCRIPTION", applianceSymbolicName)
		{
			public void start(final String id)
			{
				try
				{
					wamtDevice.quiesce();
				}
				catch (final DeletedException e)
				{
					ActionFactory.getActionLog().getActionStatus(id).failed(new NoSuchResourceException(applianceSymbolicName));
					return;
				}
				catch (final UnsuccessfulOperationException e)
				{
					ActionFactory.getActionLog().getActionStatus(id).failed(e);
					return;
				}
				catch (final AMPException e)
				{
					ActionFactory.getActionLog().getActionStatus(id).failed(e);
					return;
				}
				ActionFactory.getActionLog().getActionStatus(id).succeeded();
			};
		};
		return ActionFactory.getActionController().submitAction(quiesceAction);
	}

	@Override
	public String unquiesce()
	{
		final AbstractAction unquiesceAction = new AbstractAction("unquiesce", getPrimaryKey(), "CWZBA1011I_UNQUIESCE_APPLIANCE_ACTION_DESCRIPTION", applianceSymbolicName)
		{
			public void start(final String id)
			{
				try
				{
					wamtDevice.unquiesce();
				}
				catch (final DeletedException e)
				{
					ActionFactory.getActionLog().getActionStatus(id).failed(new NoSuchResourceException(applianceSymbolicName));
					return;
				}
				catch (final UnsuccessfulOperationException e)
				{
					ActionFactory.getActionLog().getActionStatus(id).failed(e);
					return;
				}
				catch (final AMPException e)
				{
					ActionFactory.getActionLog().getActionStatus(id).failed(e);
					return;
				}
				ActionFactory.getActionLog().getActionStatus(id).succeeded();
			};
		};
		return ActionFactory.getActionController().submitAction(unquiesceAction);
	}

	@Override
	public String backup(final String certificateName, final URI backupDestination, final boolean includeIscsi, final boolean includeRaid)
	{
		return backup(certificateName, null, backupDestination, includeIscsi, includeRaid);
	}

	@Override
	public String backup(final URI certificateLocation, final URI backupDestination, final boolean includeIscsi, final boolean includeRaid)
	{
		return backup(null, certificateLocation, backupDestination, includeIscsi, includeRaid);
	}

	private String backup(final String certificateName, final URI certificateLocation, final URI backupDestination, final boolean includeIscsi, final boolean includeRaid)
	{
		final AbstractAction backupAction = new AbstractProgressContainerAction("backup", getPrimaryKey(), "CWZBA1020I_BACKUP_APPLIANCE_ACTION_DESCRIPTION", applianceSymbolicName)
		{
			private File downloadDirectory = null;

			public ProgressContainer submit(final String id) throws FullException, InvalidParameterException, DatastoreException, URISyntaxException
			{
				try
				{
					final URI backupDestinationForWamt;
					if (backupDestination.getScheme().equals("file"))
					{
						final String directoryName = WamtAppliance.this.getName() + "-" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
						downloadDirectory = FileManager.createDownloadDirectory(directoryName);
						backupDestinationForWamt = downloadDirectory.toURI();
					}
					else
					{
						backupDestinationForWamt = backupDestination;
					}
					final URLSource certificateLocationSource = (certificateLocation == null) ? null : new URLSource(FileManager.resolveUri(certificateLocation).toString());
					return wamtDevice.backup(certificateName, certificateLocationSource, backupDestinationForWamt, includeIscsi, includeRaid);
				}
				catch (final DeletedException e)
				{
					throw new NoSuchResourceException(applianceSymbolicName);
				}
				catch (final MissingFeaturesInFirmwareException e)
				{
					throw new AmcIllegalArgumentException(e, "CWZBA0520E_BACKUP_NOT_SUPPORTED", applianceSymbolicName, wamtDevice.getActualFirmwareLevel(),
							MinimumFirmwareLevel.MINIMUM_FW_LEVEL_FOR_BACKUP);
				}
				catch (final LockBusyException e)
				{
					throw new ConcurrencyException(e);
				}
			}

			@Override
			public void succeeded(final ActionStatus statusHandle)
			{
				if (downloadDirectory != null)
				{
					final File zip = FileManager.compress(downloadDirectory);
					try
					{
						FileUtils.deleteDirectory(downloadDirectory);
					}
					catch (final IOException e)
					{
						if (logger.isDebugEnabled()) logger.debug("succeeded", "Failed to delete download directory: " + e);
					}
					try
					{
						statusHandle.succeeded(new URI(TemporaryFileResource.PATH + "/" + zip.getName()));
					}
					catch (final URISyntaxException e)
					{
						throw new AmcRuntimeException(e);
					}
				}
				else
				{
					super.succeeded(statusHandle);
				}

				FileManager.deleteIfTemporaryFile(certificateLocation);
			}

			@Override
			public void failed(final ActionStatus statusHandle, final Throwable exception)
			{
				super.failed(statusHandle, exception);

				FileManager.deleteIfTemporaryFile(certificateLocation);
			}
		};
		return ActionFactory.getActionController().submitAction(backupAction);

	}

	@Override
	public String restore(final String credentialName, final URI backupSource)
	{
		final URI backupSourceForWamt;
		if (backupSource.getScheme().equals(FileManager.SCHEME))
		{
			final File backupDirectory = locateBackupDirectory(FileManager.decompress(backupSource));
			if (backupDirectory == null)
			{
				throw new AmcIllegalArgumentException("CWZBA1024E_INVALID_BACKUP");
			}
			backupSourceForWamt = backupDirectory.toURI();
		}
		else
		{
			backupSourceForWamt = backupSource;
		}

		final AbstractAction restoreAction = new AbstractProgressContainerAction("restore", getPrimaryKey(), "CWZBA1021I_RESTORE_APPLIANCE_ACTION_DESCRIPTION", applianceSymbolicName)
		{
			public ProgressContainer submit(final String id) throws NotExistException, FullException, InvalidParameterException, InUseException, NotEmptyException, DatastoreException,
					URISyntaxException
			{
				try
				{
					return wamtDevice.restore(credentialName, backupSourceForWamt, false);
				}
				catch (final DeletedException e)
				{
					throw new NoSuchResourceException(applianceSymbolicName);
				}
				catch (MissingFeaturesInFirmwareException e)
				{
					throw new AmcIllegalArgumentException(e, "CWZBA0521E_RESTORE_NOT_SUPPORTED", applianceSymbolicName, wamtDevice.getActualFirmwareLevel(),
							MinimumFirmwareLevel.MINIMUM_FW_LEVEL_FOR_RESTORE);
				}
				catch (LockBusyException e)
				{
					throw new ConcurrencyException(e);
				}
				catch (IOException e)
				{
					throw new AmcRuntimeException(Status.INTERNAL_SERVER_ERROR, e, "CWZBA0522E_BACKUP_FILE_IOEXCEPTION", backupSource.toString());
				}
			}

			@Override
			public void succeeded(ActionStatus statusHandle)
			{
				super.succeeded(statusHandle);
				FileManager.deleteIfTemporaryFile(backupSource);
				WamtAppliance.this.ensureDeviceManaged();
			}

			@Override
			public void failed(ActionStatus statusHandle, Throwable exception)
			{
				super.failed(statusHandle, exception);
				FileManager.deleteIfTemporaryFile(backupSource);
				WamtAppliance.this.ensureDeviceManaged();
			}

		};
		return ActionFactory.getActionController().submitAction(restoreAction);
	}

	/**
	 * Recursive in to directories to locate backupmanifest.xml.
	 * 
	 * @param directory
	 *            top-level directory
	 * @return directory containing backupmanifest.xml
	 */
	private File locateBackupDirectory(File directory)
	{
		if (new File(directory, BACKUP_MANIFEST_FILE_NAME).exists())
		{
			return directory;
		}
		else
		{
			final File[] subdirectories = directory.listFiles(new FileFilter()
			{
				@Override
				public boolean accept(File path)
				{
					return path.isDirectory();
				}
			});
			for (File subdirectory : subdirectories)
			{
				File result = locateBackupDirectory(subdirectory);
				if (result != null) return result;
			}
		}
		return null;
	}

	// Package-private; should not be used at all outside this package, and as
	// little as possible inside it.
	Device getDelegate()
	{
		return wamtDevice;
	}

	@Override
	public List<SvrFirmware> getBestFirmwareVersions()
	{
		if (logger.isEntryEnabled()) logger.entry("getBestFirmwareVersions");

		List<SvrFirmware> result = new ArrayList<SvrFirmware>();
		try
		{
			// First, get a list of all the levels of firmware known to exist,
			// across all feature sets etc.
			Set<String> availableLevels = new HashSet<String>();
			Firmware[] allAvailableFirmwares = manager.getFirmwares();

			if (allAvailableFirmwares == null)
			{
				if (logger.isDebugEnabled()) logger.debug("getBestFirmwareVersions", "No firmware in the repository");
				if (logger.isEntryEnabled()) logger.exit("getBestFirmwareVersions", result);
				return result;
			}

			for (Firmware firmware : allAvailableFirmwares)
			{
				Version[] firmwareVersions = firmware.getVersions();
				for (Version version : firmwareVersions)
				{
					String level = ((FirmwareVersion) version).getLevel();
					availableLevels.add(level);
				}
			}

			// Now find the best available firmware for each level.
			for (String level : availableLevels)
			{
				Firmware firmware = manager.getBestFirmware(wamtDevice.getDeviceType(), wamtDevice.getModelType(), wamtDevice.getFeatureLicenses(), level);
				if (firmware == null)
				{
					if (logger.isDebugEnabled()) logger.debug("getBestFirmwareVersions", "No 'best' firmware for level [" + level + "] on device [" + wamtDevice + "]");
					continue;
				}

				FirmwareVersion thisLevel = firmware.getLevel(level);
				if (thisLevel == null)
				{
					if (logger.isDebugEnabled()) logger.debug("getBestFirmwareVersions", "getBestFirmware suggested a firmware for level [" + level + "] which "
							+ "does not have a version at that level.");
					continue;
				}

				WamtFirmware firmwareImage = new WamtFirmware(thisLevel, manager);
				result.add(firmwareImage);
			}

			if (logger.isEntryEnabled()) logger.exit("getBestFirmwareVersions", result);
			return result;
		}
		catch (DeletedException e)
		{
			throw new NoSuchResourceException(applianceSymbolicName);
		}
	}

	@Override
	public String deployFirmware(final String targetFirmwareId, final boolean licenceAccepted)
	{
		if (logger.isEntryEnabled()) logger.entry("deployFirmware", targetFirmwareId, licenceAccepted);

		final AbstractAction deployAction = new AbstractProgressContainerAction("deployFirmware", getPrimaryKey(), "CWZBA1017I_DEPLOY_FIRMWARE_ACTION_DESCRIPTION", applianceSymbolicName)
		{
			public ProgressContainer submit(final String id) throws Exception
			{
				try
				{
					// Submit request to WAMT.
					WamtFirmware firmware = new WamtFirmware(targetFirmwareId, manager);
					wamtDevice.setSourceFirmwareVersion(firmware.getDelegate());
					
					if (licenceAccepted)
					{
						wamtDevice.acceptLicenseForFirmware();
					}
					
					return wamtDevice.deploySourceFirmwareVersion();
				}
				catch (final DeletedException e)
				{
					throw new NoSuchResourceException(applianceSymbolicName);
				}
				catch (NotExistException e)
				{
					throw new NoSuchResourceException(applianceSymbolicName);
				}
			}
		};
		final String actionId = ActionFactory.getActionController().submitAction(deployAction);

		if (logger.isEntryEnabled()) logger.exit("deployFirmware", actionId);
		return actionId;
	}

	@Override
	public List<String> getCapabilities()
	{
		if (logger.isEntryEnabled()) logger.entry("getCapabilities");
		
		ManagementOperations[] wamtOperations = wamtDevice.getSupportedOperations();

		List<String> result = new ArrayList<String>();
		for (ManagementOperations operation : wamtOperations)
		{
			result.add(operation.name().toLowerCase());
		}
		
		// Add in here any custom capability settings that we can deduce but
		// WAMT do not provide.
		
		if (logger.isEntryEnabled()) logger.exit("getCapabilities", result);
		return result;
	}
	
	@Override
	public List<String> getGroups()
	{
		if (logger.isEntryEnabled()) logger.entry("getGroups");
		List<String> result = new ArrayList<String>();
		
		Set<String> groups = null;
		
		try
		{
			groups = wamtDevice.getTagValues(Constants.GROUPS_TAG_KEY);
		}
		catch (DeletedException e)
		{
			throw new NoSuchResourceException(applianceSymbolicName);
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
				
		final ActionStatus status = ActionFactory.getActionLog().actionStarted(SecurityContext.getContext().getUser(), 
				getPrimaryKey(), "updateGroups", "CWZBA1041I_UPDATE_APPLIANCE_GROUPS_ACTION", applianceSymbolicName);
				
		try
		{
			try
			{
				/* remove the old groups */
				wamtDevice.removeTag(Constants.GROUPS_TAG_KEY);
				
				/* add the new groups */
				for (String group : groups)
				{
					wamtDevice.addTag(Constants.GROUPS_TAG_KEY, group);
				}
			}
			catch (DeletedException e)
			{
				throw new NoSuchResourceException(applianceSymbolicName);
			}
			catch (DirtySaveException e)
			{
				throw new ConcurrencyException(e, applianceSymbolicName);
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
	public boolean isReachable() 
	{
		if (logger.isEntryEnabled()) logger.entry("isReachable");
		boolean result = wamtDevice.isDeviceReachable();
		if (logger.isEntryEnabled()) logger.exit("isReachable", result);
		return result;
		
	}
	
	public String reboot()
	{
		if (logger.isEntryEnabled()) logger.entry("reboot");
		
		final AbstractAction rebootAction = new AbstractProgressContainerAction("rebootAppliance", getPrimaryKey(), "CWZBA1044I_REBOOT_APPLIANCE_ACTION_DESCRIPTION", applianceSymbolicName)
		{
			public ProgressContainer submit(final String id) throws Exception
			{
				try
				{
					return wamtDevice.reboot();
				}
				catch (final DeletedException e)
				{
					throw new NoSuchResourceException(applianceSymbolicName);
				}
				catch (FullException e)
				{
					throw new AmcRuntimeException(e);
				}
			}
		};
		final String actionId = ActionFactory.getActionController().submitAction(rebootAction);
		if (logger.isEntryEnabled()) logger.exit("reboot", actionId);
		return actionId;
	}

}
