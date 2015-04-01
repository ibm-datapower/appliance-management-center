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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import com.ibm.amc.server.action.ActionStatus;
import com.ibm.amc.server.action.ActionStatus.State;

public class ActionStatusResponseTests
{
	// @CLASS-COPYRIGHT@

	private ActionStatusResponse response;
	private Date submitted = new Date();
	private Date updated = new Date();

	@Before
	public void setUp() throws Exception
	{
		ActionStatus status = mock(ActionStatus.class);
		when(status.getActionId()).thenReturn("ACTION_ID");
		when(status.getApplianceId()).thenReturn("APPLIANCE_ID");
		when(status.getDescriptionInserts()).thenReturn(new String[] { "ACTION_INSERT" });
		when(status.getDescriptionKey()).thenReturn("CWZBA1010I_QUIESCE_APPLIANCE_ACTION_DESCRIPTION");
		when(status.getName()).thenReturn("ACTION");
		when(status.getSubmitted()).thenReturn(submitted);
		when(status.getUpdated()).thenReturn(updated);
		when(status.getState()).thenReturn(State.SUCCEEDED);
		when(status.getUserId()).thenReturn("user");
		response = new ActionStatusResponse(status);
	}

	@Test
	public void testConstructor() throws URISyntaxException
	{
		assertEquals("ACTION", response.action);
		assertEquals("ACTION_ID", response.actionId);
		assertEquals("Quiesce appliance 'ACTION_INSERT'", response.description);
		assertEquals(new URI("/amc/rest/appliances/APPLIANCE_ID"), response.resource);
		assertEquals("SUCCEEDED", response.state);
		assertEquals(submitted, response.submitted);
		assertEquals(updated, response.updated);
	}

}
