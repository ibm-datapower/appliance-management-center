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
import java.util.Arrays;
import java.util.List;

import com.ibm.amc.AmcRuntimeException;
import com.ibm.amc.data.SvrService;
import com.ibm.amc.data.SvrServiceObject;
import com.ibm.amc.resources.exceptions.NoSuchResourceException;
import com.ibm.amc.security.SecurityContext;
import com.ibm.amc.server.action.ActionFactory;
import com.ibm.amc.server.action.ActionStatus;
import com.ibm.amc.server.action.AsyncAction;
import com.ibm.amc.server.action.impl.AbstractSyncAdapterAction;
import com.ibm.datapower.amt.amp.AMPException;
import com.ibm.datapower.amt.amp.ConfigObject;
import com.ibm.datapower.amt.amp.NotExistException;
import com.ibm.datapower.amt.clientAPI.DeletedException;
import com.ibm.datapower.amt.clientAPI.Domain;
import com.ibm.datapower.amt.clientAPI.RuntimeService;
import com.ibm.datapower.amt.clientAPI.UnsuccessfulOperationException;

/**
 * The server-side representation of a service, that is backed by the Appliance Management
 * Toolkit.
 */
public class WamtService extends SvrService
{
	// @CLASS-COPYRIGHT@

	private final WamtDomain domain;

	private final RuntimeService wamtService;

	private Domain wamtDomain;

	public WamtService(final WamtDomain domain, final RuntimeService wamtService)
	{
		this.domain = domain;
		this.wamtService = wamtService;
		try
		{
			this.wamtDomain = wamtService.getDomain();
		}
		catch (NotExistException e)
		{
			throw new NoSuchResourceException(getDomainName());
		}
	}

	public String getName()
	{
		return wamtService.getName();
	}

	public String getPrimaryKey()
	{
		// WAMT primary key key currently only consists of service name and type
		return domain.getApplianceId() + ":" + getDomainName() + ":" + wamtService.getPrimaryKey();
	}

	public String getApplianceName()
	{
		return domain.getApplianceName();
	}

	public String getApplianceId()
	{
		return domain.getApplianceId();
	}

	public String getDomainName()
	{
		return domain.getName();
	}

	public String getClassDisplayName()
	{
		return wamtService.getClassDisplayName();
	}

	public String getClassName()
	{
		return wamtService.getClassName();
	}

	public ServiceStatus getStatus()
	{
		return ServiceStatus.valueOf(wamtService.getOpStatus().name());
	}
	
	@Override
	public String unquiesce()
	{
		final AsyncAction unquiesceAction = new AbstractSyncAdapterAction("unquiesce", getPrimaryKey(), "CWZBA1038I_UNQUIESCE_SERVICE_ACTION_DESCRIPTION", getName(), getClassDisplayName(),
				getDomainName(), getApplianceName())
		{
			@Override
			public void execute() throws Throwable
			{
				try
				{
					wamtService.unquiesce();
				}
				catch (DeletedException e)
				{
					throw new NoSuchResourceException(WamtService.this.getName());
				}
				catch (NotExistException e)
				{
					throw new NoSuchResourceException(WamtService.this.getName());
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
		
		return actionId;
	}
	
	@Override
	public String quiesce()
	{
		final AsyncAction quiesceAction = new AbstractSyncAdapterAction("quiesce", getPrimaryKey(), "CWZBA1037I_QUIESCE_SERVICE_ACTION_DESCRIPTION", getName(), getClassDisplayName(), getDomainName(),
				getApplianceName())
		{
			@Override
			public void execute() throws Throwable
			{
				try
				{
					wamtService.quiesce();
				}
				catch (DeletedException e)
				{
					throw new NoSuchResourceException(WamtService.this.getName());
				}
				catch (NotExistException e)
				{
					throw new NoSuchResourceException(WamtService.this.getName());
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

		return actionId;
	}

	@Override
	public void delete(List<String> orphansToDelete)
	{
		if (logger.isEntryEnabled()) logger.entry("delete", orphansToDelete);

		final ActionStatus status = ActionFactory.getActionLog().actionStarted(SecurityContext.getContext().getUser(), wamtService.getPrimaryKey(), "delete",
				"CWZBA1034I_DELETE_SERVICE_ACTION_DESCRIPTION", wamtService.getName(), wamtService.getClassDisplayName(), domain.getName(), domain.getApplianceName());

		try
		{
			try
			{
				// Keep all referenced objects, except for orphans whose deletion has
				// been explicitly requested.
				ConfigObject[] refs = wamtService.getReferencedObjectsAndFiles().getReferencedObjects();

				List<ConfigObject> childrenToKeep = new ArrayList<ConfigObject>();
				
				if (refs != null)
				{
					childrenToKeep.addAll(Arrays.asList(refs));
				}
				
				if (orphansToDelete != null)
				{
					for (int i = 0; i < childrenToKeep.size(); i++)
					{
						ConfigObject child = childrenToKeep.get(i);
						if (orphansToDelete.contains(child.getPrimaryKey())) childrenToKeep.remove(i);
					}
				}

				if (logger.isDebugEnabled()) debugDelete(orphansToDelete, refs, childrenToKeep);
				wamtDomain.deleteService(wamtService, childrenToKeep.toArray(new ConfigObject[] {}), true);
			}
			catch (DeletedException e)
			{
				throw new NoSuchResourceException(getName());
			}
			catch (UnsuccessfulOperationException e)
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
	public List<SvrServiceObject> getOrphansIfDeleted()
	{
		if (logger.isEntryEnabled()) logger.entry("getOrphansIfDeleted");

		List<SvrServiceObject> result = new ArrayList<SvrServiceObject>();
	
		ConfigObject[] allObjects = wamtService.getReferencedObjectsAndFiles().getReferencedObjects();
		
		if (allObjects != null)
		{
			for (ConfigObject configObject : allObjects)
			{
				if (!configObject.isReferencedExternally())
				{
					result.add(new WamtServiceObject(configObject));
				}
			}
		}

		if (logger.isEntryEnabled()) logger.exit("getOrphansIfDeleted", result);
		return result;
	}

	private void debugDelete(List<String> orphansToDelete, ConfigObject[] refs, List<ConfigObject> childrenToKeep)
	{
		logger.debug("delete()", "Deleting", getName());

		StringBuilder buffer = new StringBuilder("|");
		for (ConfigObject configObject : childrenToKeep)
		{
			buffer.append(configObject.getName() + "|");
		}
		String keepers = buffer.toString();

		String orphans = "[null]";
		if (orphansToDelete != null)
		{
			buffer = new StringBuilder("|");
			for (String orphan : orphansToDelete)
			{
				buffer.append(orphan + "|");
			}
			orphans = buffer.toString();
		}

		logger.debug("delete()", "Orphans to delete were: " + orphans);
		logger.debug("delete()", "Objects to  keep  were: " + keepers);
	}
}
