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

import java.util.Date;
import java.util.List;

import com.ibm.amc.Constants;
import com.ibm.amc.ras.Logger47;
import com.ibm.amc.resources.data.Firmware;
import com.ibm.amc.resources.data.HasRest;

public abstract class SvrFirmware implements HasRest
{
	// @CLASS-COPYRIGHT@

	@edu.umd.cs.findbugs.annotations.SuppressWarnings(justification = "Findbugs wants this to be package-protected, but subclasses outside the package use it.", value = "MS")
	protected static Logger47 logger = Logger47.get(SvrFirmware.class.getCanonicalName(), Constants.CWZBA_BUNDLE_NAME);

	@Override
	public Firmware toRest()
	{
		if (logger.isEntryEnabled()) logger.entry("toRest");

		Firmware firmware = new Firmware();

		firmware.primaryKey = this.getPrimaryKey();
		firmware.applianceType = this.getApplianceType();
		firmware.displayName = this.getDisplayName();
		firmware.modelType = this.getModelType();
		firmware.nonStrictFeatures = this.getNonStrictFeatures();
		firmware.strictFeatures = this.getStrictFeatures();
		firmware.level = this.getLevel();
		firmware.manufactureDate = this.getManufactureDate();
		firmware.timeStamp = this.getTimeStamp();
		firmware.userComment = this.getUserComment();

		if (logger.isEntryEnabled()) logger.exit("toRest", firmware);
		return firmware;
	}

	@Override
	public String toString()
	{
		return this.toRest().toString();
	}

	@Override
	public boolean equals(Object o)
	{
		if(o == null) return false;
		if(!(o instanceof SvrFirmware)) return false;
		return ((SvrFirmware)o).getPrimaryKey().equals(this.getPrimaryKey());
	}
	
	@Override
	public int hashCode()
	{
		return getPrimaryKey().hashCode();
	}
	
	public abstract String getPrimaryKey();

	public abstract String getApplianceType();

	public abstract String getDisplayName();

	public abstract String getModelType();

	public abstract List<String> getNonStrictFeatures();

	public abstract List<String> getStrictFeatures();

	public abstract String getLevel();

	public abstract Date getManufactureDate();

	public abstract Date getTimeStamp();

	public abstract String getUserComment();

	public abstract void setUserComment(String userComment);
	
	public abstract void remove();

	/** @see com.ibm.datapower.amt.clientAPI.Firmware.isCompatibleWith(Device) */
	public abstract boolean isCompatibleWith(SvrAppliance appliance);
	
}
