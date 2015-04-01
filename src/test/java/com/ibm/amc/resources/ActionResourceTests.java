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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import com.ibm.amc.resources.data.ActionResponse;
import com.ibm.amc.resources.data.ActionStatusResponse;
import com.ibm.amc.resources.data.ExceptionResponse;
import com.ibm.amc.resources.exceptions.NoSuchResourceException;
import com.ibm.amc.server.Controller;
import com.ibm.amc.server.action.ActionFactory;
import com.ibm.amc.server.action.ActionStatus;
import com.ibm.amc.server.action.AsyncAction;

public class ActionResourceTests
{
	// @CLASS-COPYRIGHT@

	private ActionResource resource;

	@Before
	public void setUp() throws Exception
	{
		resource = new ActionResource(mock(Controller.class));
	}

	@Test(expected = NoSuchResourceException.class)
	public void testGetActionNoSuchResourceException()
	{
		resource.getAction("UNKNOWN_ACTION_ID");
	}

	public void testGetAction()
	{
		AsyncAction action = mock(AsyncAction.class);
		when(action.getSubjectId()).thenReturn("APPLIANCE_ID");
		when(action.getDescriptionInserts()).thenReturn(new String[] { "INSERT1" });
		when(action.getDescriptionKey()).thenReturn("CWZBA1010I_QUIESCE_APPLIANCE_ACTION_DESCRIPTION");
		when(action.getName()).thenReturn("NAME");

		String actionId = ActionFactory.getActionLog().actionSubmitted("USER", action);
		ActionFactory.getActionLog().getActionStatus(actionId).started();
		ActionFactory.getActionLog().getActionStatus(actionId).failed(new Throwable("TEST_EXCEPTION"));

		ActionStatusResponse response = resource.getAction(actionId);

		assertEquals("NAME", response.action);
		assertEquals(actionId, response.actionId);
		assertEquals("Quiesce appliance INSERT1", response.description);
		assertEquals("/amc/rest/appliances/APPLIANCE_ID", response.resource);
		assertEquals(ActionStatus.State.FAILED.toString(), response.state);
		assertNotNull(response.submitted);

		// Two updates
		assertEquals(2, response.updates.size());

		// First is for start of action
		ActionResponse firstUpdate = response.updates.get(0);
		assertEquals("CWZBA1001I", firstUpdate.code);
		assertEquals("Action \"Quiesce appliance INSERT1\" started.", firstUpdate.message);

		// Second is for failure
		ActionResponse secondUpdate = response.updates.get(0);
		assertTrue(secondUpdate instanceof ExceptionResponse);
		assertEquals("CWZBA1002E", secondUpdate.code);
		assertEquals("Action \"Quiesce appliance INSERT1\" failed to complete successfully.", secondUpdate.message);
		assertEquals("TEST_EXCEPTION", ((ExceptionResponse) secondUpdate).cause);

	}
}
