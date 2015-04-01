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

import static com.ibm.amc.security.Permission.HISTORY_VIEW;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.ibm.amc.Constants;
import com.ibm.amc.data.filter.QueryFilterEngine;
import com.ibm.amc.ras.Logger47;
import com.ibm.amc.resources.data.ActionStatusResponse;
import com.ibm.amc.resources.exceptions.PermissionDeniedException;
import com.ibm.amc.security.Permission;
import com.ibm.amc.security.SecurityContext;
import com.ibm.amc.server.Controller;
import com.ibm.amc.server.action.ActionFactory;
import com.ibm.amc.server.action.ActionLog;
import com.ibm.amc.server.action.ActionStatus;

/**
 * REST resource for actions.
 */
@Path("/actions")
@Produces(MediaType.APPLICATION_JSON)
public class ActionResource extends AbstractResource
{
	// @CLASS-COPYRIGHT@
	static Logger47 logger = Logger47.get(ActionResource.class.getCanonicalName(), Constants.CWZBA_BUNDLE_NAME);

	public ActionResource()
	{
		super();
	}

	public ActionResource(Controller controller)
	{
		super(controller);
	}

	/**
	 * Request the action status for all stored actions. If query parameters are provided, filter
	 * the results using these parameters.
	 * 
	 * @return a list of all stored statuses
	 */
	@GET
	public Response getAllActions()
	{
		if (logger.isEntryEnabled()) logger.entry("getAllActions");

		List<ActionStatusResponse> resultList = new ArrayList<ActionStatusResponse>();

		ActionLog log = ActionFactory.getActionLog();

		final String userName = SecurityContext.getContext().getUser();
		final Set<Permission> permissions = SecurityContext.getContext().getUserPermissions();

		List<ActionStatus> statuses = (permissions.contains(HISTORY_VIEW)) ? log.getActionStatuses() : log.getActionStatusesForUser(userName);

		for (ActionStatus status : statuses)
		{
			resultList.add(new ActionStatusResponse(status));
		}

		/* filter the results */
		Set<Entry<String, List<String>>> queryParams = uriInfo.getQueryParameters().entrySet();
		if (queryParams != null && !queryParams.isEmpty())
		{
			if (logger.isDebugEnabled()) logger.debug("getAllActions", "Process query parameters");
			resultList = (List<ActionStatusResponse>) QueryFilterEngine.filter(resultList, queryParams);
		}

		Response result = Response.ok(resultList).header("Content-Range", "items */" + resultList.size()).build();

		if (logger.isEntryEnabled()) logger.exit("getAllActions", result);
		return result;
	}

	/**
	 * Get an action's status.
	 * 
	 * @param actionId
	 *            the identifier for the action
	 */
	@GET
	@Path("{actionId}")
	public ActionStatusResponse getAction(@PathParam("actionId") String actionId)
	{
		if (logger.isEntryEnabled()) logger.entry("getAction");

		ActionLog log = ActionFactory.getActionLog();
		ActionStatus status = log.getActionStatus(actionId);

		// Check user has permission to view action
		final String userName = SecurityContext.getContext().getUser();
		if (!userName.equals(status.getUserId()))
		{
			final Set<Permission> permissions = SecurityContext.getContext().getUserPermissions();
			if (!permissions.contains(HISTORY_VIEW))
			{
				logger.audit("CWZBA1023A_PERMISSION_DENIED", userName, "GET", uriInfo.getAbsolutePath().getPath(), HISTORY_VIEW.toRest());
				throw new PermissionDeniedException();
			}
		}

		ActionStatusResponse result = new ActionStatusResponse(status);

		if (logger.isEntryEnabled()) logger.exit("getAction", result);
		return result;
	}
}
