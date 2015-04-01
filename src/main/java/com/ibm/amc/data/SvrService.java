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
import com.ibm.amc.resources.data.Service;

public abstract class SvrService implements HasRest
{
	// @CLASS-COPYRIGHT@

	@edu.umd.cs.findbugs.annotations.SuppressWarnings(justification = "Findbugs wants this to be package-protected, but subclasses outside the package use it.", value = "MS")
	protected static Logger47 logger = Logger47.get(SvrService.class.getCanonicalName(), Constants.CWZBA_BUNDLE_NAME);

	public Service toRest()
	{
		if (logger.isEntryEnabled()) logger.entry("toRest");

		Service result = new Service();
		result.name = getName();
		result.primaryKey = getPrimaryKey();
		result.domainName = getDomainName();
		result.applianceName = getApplianceName();
		result.applianceId = getApplianceId();
		result.className = getClassName();
		result.classDisplayName = getClassDisplayName();
		result.status = getStatus();

		if (logger.isEntryEnabled()) logger.exit("toRest", result);
		return result;
	}

	public abstract String getName();

	public abstract String getPrimaryKey();

	public abstract String getDomainName();

	public abstract String getApplianceName();

	public abstract String getApplianceId();

	public abstract String getClassName();

	public abstract String getClassDisplayName();
	
	public abstract ServiceStatus getStatus();

	/**
	 * A transport-neutral representation of a service's possible status.
	 */
	public enum ServiceStatus
	{
		DOWN, UP, UNKNOWN;

		@Override
		public String toString()
		{
			return this.name().toLowerCase();
		}
	}

	/**
	 * Unquiesce this service
	 * @return the action identifier
	 */
	public abstract String unquiesce();
	
	/**
	 * Quiesce this service
	 * @return the action identifier
	 */
	public abstract String quiesce();
	
	/**
	 * Delete this service.
	 * @param orphansToDelete A list of IDs, referring to ServiceObjects 
	 * originally obtained from getOrphansIfDeleted, indicating which of those
	 * config objects the user wishes to delete along with the Service itself.
	 */
	public abstract void delete(List<String> orphansToDelete);

	/**
	 * @return the list of config objects which are used by this service and no
	 * other, and hence would be orphans if this service were to be deleted. 
	 */
	public abstract List<SvrServiceObject> getOrphansIfDeleted();
}
