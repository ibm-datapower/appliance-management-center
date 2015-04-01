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
package com.ibm.amc.resources;

import java.net.URI;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.ibm.amc.Constants;
import com.ibm.amc.ras.Logger47;
import com.ibm.amc.resources.data.Service;
import com.ibm.amc.resources.data.ServiceDescriptor;
import com.ibm.amc.security.Permissions;

import static com.ibm.amc.security.Permission.*;

import com.ibm.amc.server.Controller;
import com.ibm.datapower.amt.DMgrException;

/**
 * ReST resource for services.
 */
@Produces(MediaType.APPLICATION_JSON)
@Path("/services")
public final class ServiceResource extends AbstractResource
{
	// @CLASS-COPYRIGHT@

	static Logger47 logger = Logger47.get(ServiceResource.class.getCanonicalName(), Constants.CWZBA_BUNDLE_NAME);

	public ServiceResource()
	{
		if (logger.isEntryEnabled()) logger.entry("<init>");
		if (logger.isEntryEnabled()) logger.exit("<init>");
	}

	public ServiceResource(final Controller controller)
	{
		super(controller);
		if (logger.isEntryEnabled()) logger.entry("<init>", controller);
		if (logger.isEntryEnabled()) logger.exit("<init>");
	}

	@GET
	@Permissions(DOMAIN_VIEW)
	/**
	 * Get all services known to WAMC.
	 */
	public Response getServices() throws DMgrException
	{
		if (logger.isEntryEnabled()) logger.entry("getServices");

		List<Service> services = controller.getServices(uriInfo.getQueryParameters().entrySet());
		
		Response result = Response.ok(services).header("Content-Range", "items */" + services.size()).build();

		if (logger.isEntryEnabled()) logger.exit("getServices", result);
		return result;
	}
	
	@GET
	@Path("contained")
	@Permissions(DOMAIN_UPDATE_CONFIGURATION)
	/**
	 * Get the services contained in a service export zip 
	 */
	public Response getContainedServices(
			@QueryParam("applianceId") List<String> applianceIds, 
			@QueryParam("serviceSource") URI serviceSourceUri) throws DMgrException
			
	{
		if (logger.isEntryEnabled()) logger.entry("getContainedServices", applianceIds, serviceSourceUri);
		
		List<ServiceDescriptor> services = controller.getContainedServices(applianceIds, serviceSourceUri);
		
		Response result = Response.ok(services).header("Content-Range", "items */" + services.size()).build();
		
		if (logger.isEntryEnabled()) logger.exit("getContainedServices", result);
		return result;
	}
}
