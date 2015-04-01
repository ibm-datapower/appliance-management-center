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

import static com.ibm.amc.security.Permission.APPLIANCE_ASSIGN_GROUPS;
import static com.ibm.amc.security.Permission.DOMAIN_ASSIGN_GROUPS;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.ibm.amc.Constants;
import com.ibm.amc.ras.Logger47;
import com.ibm.amc.resources.data.Group;
import com.ibm.amc.security.Permissions;
import com.ibm.amc.server.Controller;

/**
 * ReST resource for services.
 */
@Produces(MediaType.APPLICATION_JSON)
@Path("/groups")
public final class GroupResource extends AbstractResource
{
	// @CLASS-COPYRIGHT@

	static Logger47 logger = Logger47.get(GroupResource.class.getCanonicalName(), Constants.CWZBA_BUNDLE_NAME);

	public GroupResource()
	{
		if (logger.isEntryEnabled()) logger.entry("<init>");
		if (logger.isEntryEnabled()) logger.exit("<init>");
	}

	public GroupResource(final Controller controller)
	{
		super(controller);
		if (logger.isEntryEnabled()) logger.entry("<init>", controller);
		if (logger.isEntryEnabled()) logger.exit("<init>");
	}

	/**
	 * Get all groups in use by WAMC.
	 */
	@GET
	@Permissions(value = { APPLIANCE_ASSIGN_GROUPS, DOMAIN_ASSIGN_GROUPS })
	public List<Group> getGroups()
	{
		if (logger.isEntryEnabled()) logger.entry("getGroups");

		List<Group> result = controller.getGroups();

		if (result == null)
		{
			result = new ArrayList<Group>();
		}

		if (logger.isEntryEnabled()) logger.exit("getGroups", result);
		return result;
	}
}
