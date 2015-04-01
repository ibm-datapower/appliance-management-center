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

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.ibm.amc.Constants;
import com.ibm.amc.data.validation.ValidationEngine;
import com.ibm.amc.ras.Logger47;
import com.ibm.amc.resources.data.Domain;
import com.ibm.amc.resources.data.DomainDeploymentConfiguration;
import com.ibm.amc.security.Permissions;

import static com.ibm.amc.security.Permission.*;

import com.ibm.amc.server.Controller;
import com.ibm.datapower.amt.DMgrException;

/**
 * ReST resource for domains.
 */
@Produces(MediaType.APPLICATION_JSON)
@Path("/domains")
public final class DomainResource extends AbstractResource
{
	// @CLASS-COPYRIGHT@

	static Logger47 logger = Logger47.get(DomainResource.class.getCanonicalName(), Constants.CWZBA_BUNDLE_NAME);

	public DomainResource()
	{
		if (logger.isEntryEnabled()) logger.entry("<init>");
		if (logger.isEntryEnabled()) logger.exit("<init>");
	}

	public DomainResource(final Controller controller)
	{
		super(controller);
		if (logger.isEntryEnabled()) logger.entry("<init>", controller);
		if (logger.isEntryEnabled()) logger.exit("<init>");
	}

	@GET
	@Permissions(DOMAIN_VIEW)
	/**
	 * Get all domains known to WAMC.
	 */
	public Response getDomains() throws DMgrException
	{
		if (logger.isEntryEnabled()) logger.entry("getDomains");

		List<Domain> domains = controller.getDomains(uriInfo.getQueryParameters().entrySet());

		Response result = Response.ok(domains).header("Content-Range", "items */" + domains.size()).build();
		
		if (logger.isEntryEnabled()) logger.exit("getDomains", result);
		return result;
	}
	
	@GET
	@Path("{domainName}")
	@Permissions(DOMAIN_VIEW)
	/**
	 * Get all domains with a given name, across all appliances. Or if a query
	 * string is provided, a subset of these (typically limiting the set of 
	 * appliances we're interested in).
	 */
	public Response getDomains(@PathParam("domainName") String domainName, @QueryParam("appliance") List<String> applianceIds) 
	{
		if (logger.isEntryEnabled()) logger.entry("getDomains", domainName, applianceIds);
		
		// Wink splits "appliance=111111111&appliance=2222222222&appliance=3333333333" into a list all 
		// on its own. But we want to (also) support "appliance=1111111111,2222222222,3333333333" (or a 
		// mix of the two).
		splitOnCommas(applianceIds);
		
		List<Domain> domains = controller.getDomainsByName(domainName, applianceIds);
		
		Response result = Response.ok(domains).header("Content-Range", "items */" + domains.size()).build();

		if (logger.isEntryEnabled()) logger.exit("getDomains", result);
		return result;
	}
	
	@POST
	@Path("{domainName}/config")
	@Permissions(DOMAIN_UPDATE_CONFIGURATION)
	/**
	 * Deploy a configuration to all domains with a given name, across all 
	 * appliances. Or if a query string is provided, a subset of these 
	 * (typically limiting the set of appliances we're interested in).
	 */
	public List<String> multiDeployDomains(@PathParam("domainName") String domainName, 
											@QueryParam("appliance") List<String> applianceIds,
											DomainDeploymentConfiguration deploymentConfig) 
	{
		if (logger.isEntryEnabled()) logger.entry("multiDeployDomains", domainName, applianceIds, deploymentConfig);
		
		ValidationEngine.getInstance().validate(deploymentConfig);
		
		List<String> actionIds = new ArrayList<String>();
		for (String applianceId : applianceIds)
		{
			actionIds.add(controller.deployDomain(applianceId, domainName, deploymentConfig));
		}
		
		if (logger.isEntryEnabled()) logger.exit("multiDeployDomains", actionIds);
		return actionIds;
	}
	
	/**
	 * Take one or more values (from a key-value pair like "appliance=1111111111")
	 * and, if any values contain un-escaped commas, expand the list so that 
	 * each comma-separated ID is its own item in the list.
	 * <br>
	 * This method operates on the list in-place, and does not preserve order.
	 */
	private void splitOnCommas(List<String> applianceIds)
	{
		int originalLength = applianceIds.size();
		for(int i = 0; i < originalLength; i++)
		{
			String value = applianceIds.get(i);
			String[] ids = value.split(",");
			if(ids.length > 1)
			{
				applianceIds.set(i, ids[0]);
				for (int j = 1; j < ids.length; j++)
				{
					applianceIds.add(ids[j]);
				}
			}
		}
	}
}
