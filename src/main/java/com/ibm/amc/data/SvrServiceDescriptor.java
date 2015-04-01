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

import com.ibm.amc.Constants;
import com.ibm.amc.ras.Logger47;
import com.ibm.amc.resources.data.HasRest;
import com.ibm.amc.resources.data.ServiceDescriptor;

public abstract class SvrServiceDescriptor implements HasRest
{
	// @CLASS-COPYRIGHT@

	@edu.umd.cs.findbugs.annotations.SuppressWarnings(justification = "Findbugs wants this to be package-protected, but subclasses outside the package use it.", value = "MS")
	protected static Logger47 logger = Logger47.get(SvrServiceDescriptor.class.getCanonicalName(), Constants.CWZBA_BUNDLE_NAME);

	public ServiceDescriptor toRest()
	{
		if (logger.isEntryEnabled()) logger.entry("toRest");

		ServiceDescriptor result = new ServiceDescriptor();
		result.name = getName();
		result.className = getClassName();
		result.classDisplayName = getClassDisplayName();
		result.userComment = getUserComment();

		if (logger.isEntryEnabled()) logger.exit("toRest", result);
		return result;
	}

	public abstract String getName();

	public abstract String getClassName();

	public abstract String getClassDisplayName();

	public abstract String getUserComment();
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getClassDisplayName() == null) ? 0 : getClassDisplayName().hashCode());
		result = prime * result + ((getClassName() == null) ? 0 : getClassName().hashCode());
		result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
		result = prime * result + ((getUserComment() == null) ? 0 : getUserComment().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		SvrServiceDescriptor other = (SvrServiceDescriptor) obj;
		if (getClassDisplayName() == null)
		{
			if (other.getClassDisplayName() != null) return false;
		}
		else if (!getClassDisplayName().equals(other.getClassDisplayName())) return false;
		if (getClassName() == null)
		{
			if (other.getClassName() != null) return false;
		}
		else if (!getClassName().equals(other.getClassName())) return false;
		if (getName() == null)
		{
			if (other.getName() != null) return false;
		}
		else if (!getName().equals(other.getName())) return false;
		if (getUserComment() == null)
		{
			if (other.getUserComment() != null) return false;
		}
		else if (!getUserComment().equals(other.getUserComment())) return false;
		return true;
	}
}
