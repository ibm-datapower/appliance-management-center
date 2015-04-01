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
package com.ibm.amc.resources.data;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize.Inclusion;
import com.ibm.amc.nls.NLS;
import com.ibm.amc.server.action.ActionStatus;
import com.ibm.amc.server.action.ActionStatusUpdate;

/**
 * Represents the REST service serialization of an action status.
 */
public class ActionStatusResponse extends AbstractRestData
{
	// No CLASS-COPYRIGHT because we don't want copyright text in ReST responses.

	/**
	 * Constructor.
	 * 
	 * @param status
	 *            the action status
	 */
	public ActionStatusResponse(ActionStatus status)
	{
		actionId = status.getActionId();
		userId = status.getUserId();
		if (status.getApplianceId() != null)
		{
			try
			{
				resource = new URI("rest/appliances/" + status.getApplianceId());
			}
			catch (URISyntaxException e)
			{
			}
		}
		action = status.getName();
		description = NLS.formatMessage(userId, status.getDescriptionKey(), status.getDescriptionInserts());
		state = status.getState().toString();
		submitted = new Date(status.getSubmitted().getTime());
		updated = new Date(status.getUpdated().getTime());
		result = status.getResult();

		if (status.getUpdates() != null)
		{
			updates = new ArrayList<ActionResponse>(status.getUpdates().size());

			for (ActionStatusUpdate statusUpdate : status.getUpdates())
			{
				if (statusUpdate.getCause() == null)
				{
					updates.add(new ActionResponse(userId, statusUpdate.getTimeStamp(), statusUpdate.getMessageKey(), statusUpdate.getMessageInserts()));
				}
				else
				{
					final ExceptionResponse response = new ExceptionResponse(userId, statusUpdate.getTimeStamp(), statusUpdate.getMessageKey(), statusUpdate.getMessageInserts());
					response.cause = new ExceptionResponse(statusUpdate.getCause());
					updates.add(response);
				}
			}
		}
	}

	@Hashcode
	public String actionId;
	public String userId;
	@JsonSerialize(include = Inclusion.NON_NULL)
	public URI resource;
	public String action;
	public String description;
	public String state;
	public Date submitted;
	public Date updated;
	@JsonSerialize(include = Inclusion.NON_NULL)
	public URI result;
	public List<ActionResponse> updates;
}
