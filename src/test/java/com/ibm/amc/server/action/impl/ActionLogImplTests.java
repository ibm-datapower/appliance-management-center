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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

import com.ibm.amc.server.action.ActionFactory;
import com.ibm.amc.server.action.ActionLog;
import com.ibm.amc.server.action.ActionStatus;
import com.ibm.amc.server.action.ActionStatusListener;
import com.ibm.amc.server.action.ActionStatusUpdate;
import com.ibm.amc.server.action.AsyncAction;
import com.ibm.amc.utils.PersistenceTools;

public class ActionLogImplTests
{
	// @CLASS-COPYRIGHT@

	private ActionLog log;

	@Before
	public void setUp() throws Exception
	{
		log = ActionFactory.getActionLog();
		PersistenceTools.setPersistenceContext();
	}

	@Test
	public void testActionSubmitted() throws InterruptedException
	{
		AsyncAction action = mock(AsyncAction.class);
		when(action.getSubjectId()).thenReturn("APPLIANCE_ID");
		when(action.getDescriptionInserts()).thenReturn(new String[] { "INSERT1" });
		when(action.getDescriptionKey()).thenReturn("KEY");
		when(action.getName()).thenReturn("NAME");

		ActionStatusListenerImpl listener = new ActionStatusListenerImpl();
		log.addActionListener(listener);

		log.actionSubmitted("USER", action);

		assertTrue("Action listener called", listener.called);
		
		log.removeActionListener(listener);

	}

	@Test
	public void testActionStatusStarted()
	{
		ActionStatus status = new ActionStatusImpl();

		status.started();

		assertEquals(ActionStatus.State.STARTED, status.getState());
		assertEquals(1, status.getUpdates().size());
		ActionStatusUpdate update = status.getUpdates().get(0);
		assertEquals("CWZBA1001I_ACTION_STARTED", update.getMessageKey());
		assertNotNull(update.getTimeStamp());
	}

	@Test
	public void testActionStatusSucceeded()
	{
		ActionStatus status = new ActionStatusImpl();

		status.succeeded();

		assertEquals(ActionStatus.State.SUCCEEDED, status.getState());
		assertEquals(1, status.getUpdates().size());
		ActionStatusUpdate update = status.getUpdates().get(0);
		assertEquals("CWZBA1003I_ACTION_SUCCEEDED", update.getMessageKey());
		assertNotNull(update.getTimeStamp());
	}

	@Test
	public void testActionStatusFailed()
	{
		ActionStatus status = new ActionStatusImpl("tim", "12345", "name", "key", "insert");

		status.failed(new Throwable("TEST"));

		assertEquals(ActionStatus.State.FAILED, status.getState());
		assertEquals(2, status.getUpdates().size());
		ActionStatusUpdate update = status.getUpdates().get(0);
		assertEquals("CWZBA1001I_ACTION_STARTED", update.getMessageKey());
		update = status.getUpdates().get(1);
		assertEquals("CWZBA1002E_ACTION_FAILED", update.getMessageKey());
		assertNotNull(update.getTimeStamp());
		assertNotNull(update.getCause());
		assertEquals("java.lang.Throwable: TEST", update.getCause().getMessage(Locale.getDefault()));
	}

	class ActionStatusListenerImpl implements ActionStatusListener
	{
		boolean called = false;

		@Override
		public void actionStatusUpdated(ActionStatus status)
		{
			assertNotNull(status.getSubmitted());
			assertEquals("APPLIANCE_ID", status.getApplianceId());
			assertArrayEquals(new String[] { "INSERT1" }, status.getDescriptionInserts());
			assertEquals("KEY", status.getDescriptionKey());
			assertEquals("NAME", status.getName());
			called = true;
		}

	};

}
