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
import java.util.List;

import com.ibm.amc.AmcRuntimeException;
import com.ibm.amc.data.SvrServiceImpactDescriptor;
import com.ibm.amc.resources.data.ServiceImpactDescriptor.ObjectDescriptor;
import com.ibm.datapower.amt.amp.NotExistException;
import com.ibm.datapower.amt.clientAPI.DeletedException;
import com.ibm.datapower.amt.clientAPI.RuntimeService;

/**
 * The server-side representation that describes the impact of deploying 
 * a service. Content  is backed by the Appliance Management Toolkit.
 */
public class WamtServiceImpactDescriptor extends SvrServiceImpactDescriptor
{
	// @CLASS-COPYRIGHT@

	private final RuntimeService service;
	private List<String> files;
	private List<ObjectDescriptor> objectDescriptors;
	
	public WamtServiceImpactDescriptor(final RuntimeService service)
	{
		this.service = service;
		this.files = new ArrayList<String>();
		this.objectDescriptors = new ArrayList<ObjectDescriptor>();
	}
	
	@Override
	public String getApplianceName()
	{
		try
		{
			return service.getDomain().getDevice().getSymbolicName();
		}
		catch (DeletedException e)
		{
			throw new AmcRuntimeException(e);
		}
		catch (NotExistException e)
		{
			throw new AmcRuntimeException(e);
		}
	}

	@Override
	public String getDomainName()
	{
		try
		{
			return service.getDomain().getName();
		}
		catch (DeletedException e)
		{
			throw new AmcRuntimeException(e);
		}
		catch (NotExistException e)
		{
			throw new AmcRuntimeException(e);
		}
	}

	@Override
	public String getClassDisplayName()
	{
		return service.getClassDisplayName();
	}

	@Override
	public String getName()
	{
		return service.getName();
	}

	@Override
	public List<String> getFiles()
	{
		return files;
	}
	
	void addFile(String file)
	{
		files.add(file);
	}

	@Override
	public List<ObjectDescriptor> getObjects()
	{
		return objectDescriptors;
	}
	
	void addObjectDescriptor(ObjectDescriptor objectDecriptor)
	{
		objectDescriptors.add(objectDecriptor);
	}
}
