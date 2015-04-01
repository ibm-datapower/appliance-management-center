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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.ibm.amc.AmcRuntimeException;
import com.ibm.amc.Constants;
import com.ibm.amc.data.SvrAppliance;
import com.ibm.amc.data.SvrFirmware;
import com.ibm.amc.ras.Logger47;
import com.ibm.amc.resources.exceptions.ConcurrencyException;
import com.ibm.amc.resources.exceptions.NoSuchResourceException;
import com.ibm.amc.security.SecurityContext;
import com.ibm.amc.server.action.ActionFactory;
import com.ibm.amc.server.action.ActionStatus;
import com.ibm.datapower.amt.DeviceType;
import com.ibm.datapower.amt.ModelType;
import com.ibm.datapower.amt.StringCollection;
import com.ibm.datapower.amt.clientAPI.DeletedException;
import com.ibm.datapower.amt.clientAPI.Device;
import com.ibm.datapower.amt.clientAPI.Firmware;
import com.ibm.datapower.amt.clientAPI.FirmwareVersion;
import com.ibm.datapower.amt.clientAPI.InUseException;
import com.ibm.datapower.amt.clientAPI.InvalidParameterException;
import com.ibm.datapower.amt.clientAPI.Manager;
import com.ibm.datapower.amt.dataAPI.DatastoreException;
import com.ibm.datapower.amt.dataAPI.DirtySaveException;

/**
 * A Firmware Version backed by the Appliance Management Toolkit.
 */
public class WamtFirmware extends SvrFirmware
{
	// @CLASS-COPYRIGHT@
	
	static Logger47 logger = Logger47.get(WamtFirmware.class.getCanonicalName(), Constants.CWZBA_BUNDLE_NAME);
	
	private Manager manager;

	private Firmware wamtFirmware;
	private FirmwareVersion wamtFirmwareVersion;

	/**
	 * Get a firmware version that is already in the repository
	 * 
	 * @param primaryKey
	 *            The primary key of the firmware version
	 * @param manager
	 *            The manager to use
	 */
	public WamtFirmware(String primaryKey, Manager manager) throws NoSuchResourceException
	{
		if (logger.isEntryEnabled()) logger.entry("<init>", primaryKey, manager);
		
		this.manager = manager;

		this.wamtFirmwareVersion = FirmwareVersion.getByPrimaryKey(primaryKey);

		if (this.wamtFirmwareVersion == null)
		{
			throw new NoSuchResourceException(primaryKey);
		}

		try
		{
			this.wamtFirmware = (Firmware) this.wamtFirmwareVersion.getVersionedObject();
		}
		catch (DeletedException e)
		{
			throw new NoSuchResourceException(primaryKey);
		}
		if (logger.isEntryEnabled()) logger.exit("<init>");
	}

	public WamtFirmware(FirmwareVersion wamtFirmwareVersion, Manager manager) throws NoSuchResourceException
	{
		if (logger.isEntryEnabled()) logger.entry("<init>", wamtFirmwareVersion, manager);
		this.manager = manager;

		this.wamtFirmwareVersion = wamtFirmwareVersion;

		try
		{
			this.wamtFirmware = (Firmware) this.wamtFirmwareVersion.getVersionedObject();
		}
		catch (DeletedException e)
		{
			throw new NoSuchResourceException(wamtFirmwareVersion.getAbsoluteDisplayName());
		}
		if (logger.isEntryEnabled()) logger.exit("<init>");
	}

	@Override
	public String getPrimaryKey()
	{
		if (logger.isEntryEnabled()) logger.entry("getPrimaryKey");
		try
		{
			String result = wamtFirmwareVersion.getPrimaryKey();
			if (logger.isEntryEnabled()) logger.exit("getPrimaryKey", result);
			return result;
		}
		catch (DeletedException e)
		{
			throw new NoSuchResourceException(wamtFirmwareVersion.getAbsoluteDisplayName());
		}
	}

	@Override
	public String getApplianceType()
	{
		if (logger.isEntryEnabled()) logger.entry("getApplianceType");
		try
		{
			DeviceType dt = wamtFirmware.getDeviceType();
			if (dt != null)
			{
				String result = dt.getDisplayName();
				if (logger.isEntryEnabled()) logger.exit("getApplianceType", result);
				return result;
			}
		}
		catch (DeletedException e)
		{
			throw new NoSuchResourceException(wamtFirmwareVersion.getAbsoluteDisplayName());
		}

		if (logger.isEntryEnabled()) logger.exit("getApplianceType", null);
		return null;
	}

	@Override
	public String getDisplayName()
	{
		return wamtFirmwareVersion.getAbsoluteDisplayName();
	}

	@Override
	public String getModelType()
	{
		if (logger.isEntryEnabled()) logger.entry("getModelType");
		try
		{
			ModelType mt = wamtFirmware.getModelType();
			if (mt != null)
			{
				String result = mt.getDisplayName();
				if (logger.isEntryEnabled()) logger.exit("getModelType", result);
				return result;
			}
		}
		catch (DeletedException e)
		{
			throw new NoSuchResourceException(wamtFirmwareVersion.getAbsoluteDisplayName());
		}
		if (logger.isEntryEnabled()) logger.exit("getModelType");
		return null;
	}

	@Override
	public List<String> getNonStrictFeatures()
	{
		if (logger.isEntryEnabled()) logger.entry("getNonStrictFeatures");
		try
		{
			StringCollection features = wamtFirmware.getNonstrictFeatures();
			if (features == null) return null;

			int count = features.size();
			List<String> result = new ArrayList<String>();
			for (int i = 0; i < count; i++)
			{
				result.add(features.get(i));
			}
			if (logger.isEntryEnabled()) logger.exit("getNonStrictFeatures", result);
			return result;
		}
		catch (DeletedException e)
		{
			throw new NoSuchResourceException(wamtFirmwareVersion.getAbsoluteDisplayName());
		}

	}

	@Override
	public List<String> getStrictFeatures()
	{
		if (logger.isEntryEnabled()) logger.entry("getStrictFeatures");
		try
		{
			StringCollection features = wamtFirmware.getStrictFeatures();
			if (features == null) return null;

			int count = features.size();
			List<String> result = new ArrayList<String>();
			for (int i = 0; i < count; i++)
			{
				result.add(features.get(i));
			}
			if (logger.isEntryEnabled()) logger.exit("getStrictFeatures", result);
			return result;
		}
		catch (DeletedException e)
		{
			throw new NoSuchResourceException(wamtFirmwareVersion.getAbsoluteDisplayName());
		}

	}

	@Override
	public String getLevel()
	{
		if (logger.isEntryEnabled()) logger.entry("getLevel");
		try
		{
			String result = wamtFirmwareVersion.getLevel();
			if (logger.isEntryEnabled()) logger.exit("getLevel", result);
			return result;
		}
		catch (DeletedException e)
		{
			throw new NoSuchResourceException(wamtFirmwareVersion.getAbsoluteDisplayName());
		}
	}

	@Override
	public Date getManufactureDate()
	{
		if (logger.isEntryEnabled()) logger.entry("getManufactureDate");
		try
		{
			Date result = wamtFirmwareVersion.getManufactureDate();
			if (logger.isEntryEnabled()) logger.exit("getManufactureDate", result);
			return result;
		}
		catch (DeletedException e)
		{
			throw new NoSuchResourceException(wamtFirmwareVersion.getAbsoluteDisplayName());
		}
	}

	@Override
	public Date getTimeStamp()
	{
		if (logger.isEntryEnabled()) logger.entry("getTimeStamp");
		try
		{
			Date result = wamtFirmwareVersion.getTimestamp();
			if (logger.isEntryEnabled()) logger.exit("getTimeStamp", result);
			return result;
		}
		catch (DeletedException e)
		{
			throw new NoSuchResourceException(wamtFirmwareVersion.getAbsoluteDisplayName());
		}
	}

	@Override
	public String getUserComment()
	{
		if (logger.isEntryEnabled()) logger.entry("getUserComment");
		try
		{
			String result = wamtFirmwareVersion.getUserComment();
			if (logger.isEntryEnabled()) logger.exit("getUserComment", result);
			return result;
		}
		catch (DeletedException e)
		{
			throw new NoSuchResourceException(wamtFirmwareVersion.getAbsoluteDisplayName());
		}
	}

	@Override
	public void setUserComment(String userComment)
	{
		if (logger.isEntryEnabled()) logger.entry("setUserComment", userComment);
		
		final ActionStatus status = ActionFactory.getActionLog().actionStarted(SecurityContext.getContext().getUser(), getPrimaryKey(), "update", 
				(userComment == null || "".equals(userComment)) ? "CWZBA1046I_UPDATE_FIRMWARE_NO_COMMENT_ACTION_DESCRIPTION" : "CWZBA1045I_UPDATE_FIRMWARE_ACTION_DESCRIPTION",
				userComment);
		
		try
		{
			try
			{
				wamtFirmwareVersion.setUserComment(userComment);
			}
			catch (DeletedException e)
			{
				throw new NoSuchResourceException(wamtFirmwareVersion.getAbsoluteDisplayName());
			}
			catch (DirtySaveException e)
			{
				throw new ConcurrencyException(e, wamtFirmwareVersion.getAbsoluteDisplayName());
			}
			catch (DatastoreException e)
			{
				throw new AmcRuntimeException(e);
			}
			status.succeeded();
		}
		catch (RuntimeException e)
		{
			status.failed(e);
			throw e;
		}
		if (logger.isEntryEnabled()) logger.exit("setUserComment");
	}

	@Override
	public void remove()
	{
		if (logger.isEntryEnabled()) logger.entry("remove");
		
		String userComment = getUserComment();
		
		final ActionStatus status = ActionFactory.getActionLog().actionStarted(SecurityContext.getContext().getUser(), getPrimaryKey(), "remove", 
				(userComment == null || "".equals(userComment)) ? "CWZBA1032I_REMOVE_FIRMWARE_ACTION_NO_COMMENT_DESCRIPTION" : "CWZBA1019I_REMOVE_FIRMWARE_ACTION_DESCRIPTION",
				userComment);
		try
		{
			try
			{
				if (wamtFirmware.getVersions().length > 1)
				{
					wamtFirmware.remove(wamtFirmwareVersion);
				}
				else
				{
					manager.remove(wamtFirmware);
				}
			}
			catch (DeletedException e)
			{
				throw new NoSuchResourceException(wamtFirmwareVersion.getAbsoluteDisplayName());
			}
			catch (InUseException e)
			{
				throw new ConcurrencyException(e, wamtFirmwareVersion.getAbsoluteDisplayName());
			}
			catch (InvalidParameterException e)
			{
				throw new AmcRuntimeException(e);
			}
			catch (DatastoreException e)
			{
				throw new AmcRuntimeException(e);
			}
		}
		catch(final RuntimeException e)
		{
			status.failed(e);
			throw e;
		}
		
		status.succeeded();
		
		if (logger.isEntryEnabled()) logger.exit("remove");
	}

	@Override
	public boolean isCompatibleWith(SvrAppliance svrAppliance)
	{
		if (logger.isEntryEnabled()) logger.entry("isCompatibleWith", svrAppliance);
		if (! (svrAppliance instanceof WamtAppliance)) throw new IllegalArgumentException(svrAppliance.getClass().getName());
		
		Device device = ((WamtAppliance) svrAppliance).getDelegate();

		boolean result = wamtFirmware.isCompatibleWith(device);
		if (logger.isEntryEnabled()) logger.exit("isCompatibleWith", result);
		return result;
	}

	/**
	 * Acccess the WAMT API object that this object wraps. Use this only within
	 * the com.ibm.amc.data.wamt package, where an API on another WAMT type 
	 * needs to use this one.
	 */
	FirmwareVersion getDelegate()
	{
		if (logger.isEntryEnabled()) logger.entry("getDelegate");
		if (logger.isEntryEnabled()) logger.exit("getDelegate", wamtFirmwareVersion);
		return wamtFirmwareVersion;
	}
}
