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
package com.ibm.amc.server.action.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.persistence.EntityManager;

import org.junit.Before;
import org.junit.Test;

import com.ibm.amc.security.SecurityContext;
import com.ibm.amc.server.action.ActionController;
import com.ibm.amc.server.action.ActionFactory;
import com.ibm.amc.server.action.ActionLog;
import com.ibm.amc.server.action.ActionStatus;
import com.ibm.amc.server.action.ActionStatus.State;
import com.ibm.amc.server.action.AsyncAction;
import com.ibm.amc.utils.PersistenceTools;

public class ActionControllerImplTests
{
	// @CLASS-COPYRIGHT@

	private ActionController controller;
	private ActionLog log;

	@Before
	public void setUp()
	{
		controller = ActionFactory.getActionController();
		log = ActionFactory.getActionLog();
	}

	@Test
	public void testSubmitAction() throws InterruptedException
	{
		EntityManager em = PersistenceTools.setPersistenceContext();
		AsyncAction action = mock(AsyncAction.class);
		SecurityContext.setContext("user");

		ActionStatusImpl status = mock(ActionStatusImpl.class);
		when(em.find(eq(ActionStatusImpl.class), anyLong())).thenReturn(status);
		when(status.getState()).thenReturn(State.STARTED);

		String actionId = controller.submitAction(action);

		// Action status should be created in log during submission
		assertNotNull(log.getActionStatus(actionId));

		// Sleep to give the executor a chance to run
		Thread.sleep(100);

		assertEquals(ActionStatus.State.STARTED, log.getActionStatus(actionId).getState());

		// Check that action was started
		verify(action).start(actionId);

	}

}
