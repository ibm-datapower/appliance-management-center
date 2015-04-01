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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.List;
import java.util.logging.Level;

import javax.persistence.EntityManager;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.ibm.amc.Constants;
import com.ibm.amc.data.SvrFirmware;
import com.ibm.amc.ras.Logger47;
import com.ibm.amc.resources.data.Appliance;
import com.ibm.amc.resources.exceptions.AmcIllegalArgumentException;
import com.ibm.amc.resources.exceptions.ApplianceConnectionException;
import com.ibm.amc.resources.exceptions.InvalidCredentialsException;
import com.ibm.amc.security.SecurityContext;
import com.ibm.amc.server.action.impl.ActionStatusImpl;
import com.ibm.amc.server.impl.ApplianceManagerImplTests;
import com.ibm.amc.utils.PersistenceTools;
import com.ibm.datapower.amt.amp.AMPException;
import com.ibm.datapower.amt.amp.AMPIOException;
import com.ibm.datapower.amt.clientAPI.DeletedException;
import com.ibm.datapower.amt.clientAPI.Device;
import com.ibm.datapower.amt.clientAPI.Firmware;
import com.ibm.datapower.amt.clientAPI.FirmwareVersion;
import com.ibm.datapower.amt.clientAPI.ManagedSet;
import com.ibm.datapower.amt.clientAPI.Manager;
import com.ibm.datapower.amt.clientAPI.ProgressContainer;
import com.ibm.datapower.amt.clientAPI.UnsuccessfulOperationException;
import com.ibm.datapower.amt.dataAPI.AlreadyExistsInRepositoryException;

public class WamtApplianceTests
{
	// @CLASS-COPYRIGHT@

	/* logger for output during test run */
	static Logger47 logger = Logger47.get(ApplianceManagerImplTests.class.getCanonicalName(), Constants.CWZBA_BUNDLE_NAME);

	public Device device;
	public WamtAppliance appliance;
	public Manager manager;

	@Before
	public void setUp() throws Exception
	{
		device = mock(Device.class);
		manager = mock(Manager.class);
		ManagedSet defaultManagedSet = mock(ManagedSet.class);

		when(manager.getManagedSet(Constants.DEFAULT_MANAGED_SET)).thenReturn(defaultManagedSet);

		appliance = new WamtAppliance(manager, device);
		SecurityContext.setContext("test-user");

		EntityManager manager = PersistenceTools.setPersistenceContext();
		ActionStatusImpl status = mock(ActionStatusImpl.class);
		when(manager.find(ActionStatusImpl.class, new Long(0))).thenReturn(status);
	}

	@Test
	public void testAddAppliance() throws Exception
	{
		logger.entry("\n\ntestAddAppliance");

		Appliance applianceConnection = new Appliance();
		applianceConnection.name = "wamcdp3";
		applianceConnection.hostName = "wamcdp3.hursley.ibm.com";
		applianceConnection.ampPort = 5550;
		applianceConnection.adminUserId = "admin";
		applianceConnection.adminUserPassword = "wamcdp3";
		when(device.getSymbolicName()).thenReturn("wamcdp3");

		WamtAppliance appliance = new TestWamtAppliance(manager, applianceConnection);

		assertEquals("incorrect appliance name", "wamcdp3", appliance.getName());

		logger.exit("testAddAppliance");
	}

	@Test(expected = ApplianceConnectionException.class)
	public void testAddApplianceBadConnectionDetails() throws ApplianceConnectionException
	{
		logger.entry("\n\ntestAddApplianceBadConnectionDetails");

		Appliance applianceConnection = new Appliance();
		applianceConnection.name = "wamcdp5";
		applianceConnection.hostName = "blurb";
		applianceConnection.ampPort = 5550;
		applianceConnection.adminUserId = "admin";
		applianceConnection.adminUserPassword = "wamcdp5";

		new TestWamtAppliance(manager, applianceConnection);

		logger.exit("testAddApplianceBadConnectionDetails");
	}

	@Test(expected = InvalidCredentialsException.class)
	public void testAddApplianceBadPassword() throws ApplianceConnectionException
	{
		logger.entry("\n\ntestAddApplianceBadPassword");

		Appliance applianceConnection = new Appliance();
		applianceConnection.name = "wamcdp5";
		applianceConnection.hostName = "wamcdp5.hursley.ibm.com";
		applianceConnection.ampPort = 5550;
		applianceConnection.adminUserId = "admin";
		applianceConnection.adminUserPassword = "notright";

		new TestWamtAppliance(manager, applianceConnection);

		logger.exit("testAddApplianceBadPassword");
	}

	@Test(expected = InvalidCredentialsException.class)
	public void testAddApplianceBadUsername() throws ApplianceConnectionException
	{
		logger.entry("\n\ntestAddApplianceBadUsername");

		Appliance applianceConnection = new Appliance();
		applianceConnection.name = "wamcdp5";
		applianceConnection.hostName = "wamcdp5.hursley.ibm.com";
		applianceConnection.ampPort = 5550;
		applianceConnection.adminUserId = "notright";
		applianceConnection.adminUserPassword = "wamcdp5";

		new TestWamtAppliance(manager, applianceConnection);

		logger.exit("testAddApplianceBadUsername");
	}

	@Test(expected = AmcIllegalArgumentException.class)
	public void testAddApplianceAlreadyExists() throws ApplianceConnectionException
	{
		logger.entry("\n\ntestAddApplianceAlreadyExists");

		Appliance applianceConnection = new Appliance();
		applianceConnection.name = "dp3";
		applianceConnection.hostName = "wamcdp3.hursley.ibm.com";
		applianceConnection.ampPort = 5550;
		applianceConnection.adminUserId = "admin";
		applianceConnection.adminUserPassword = "wamcdp3";

		new TestWamtAppliance(manager, applianceConnection);

		logger.exit("testAddApplianceAlreadyExists");
	}

	public void testUpdateConnection() throws Exception
	{
		logger.entry("\n\ntestUpdateConnection");

		Appliance applianceConnection = new Appliance();
		applianceConnection.name = "wamcdp3";
		applianceConnection.hostName = "wamcdp3.hursley.ibm.com";
		applianceConnection.ampPort = 5550;
		applianceConnection.adminUserId = "admin";
		applianceConnection.adminUserPassword = "wamcdp3";
		when(device.getSymbolicName()).thenReturn("wamcdp3");

		WamtAppliance appliance = new TestWamtAppliance(manager, applianceConnection);

		Appliance updatedConnection = new Appliance();
		updatedConnection.name = "wamcdp4";
		updatedConnection.hostName = "wamcdp4.hursley.ibm.com";
		updatedConnection.ampPort = 5551;
		updatedConnection.adminUserId = "admins";
		updatedConnection.adminUserPassword = "wamcdp4";
		when(device.getSymbolicName()).thenReturn("wamcdp4");

		appliance.updateConnection(updatedConnection);

		assertEquals("incorrect appliance name", "wamcdp4", appliance.getName());
		assertEquals("incorrect host name", "wamcdp4.hursley.ibm.com", appliance.getHostname());
		assertEquals("incorrect AMP port", "5551", appliance.getAmpPort());
		assertEquals("incorrect user ID", "admins", appliance.getUserId());

		logger.exit("testUpdateConnection");
	}

	@Test(expected = AmcIllegalArgumentException.class)
	public void testUpdateConnectionAlreadyExists() throws Exception
	{
		logger.entry("\n\ntestUpdateConnectionAlreadyExists");

		Appliance applianceConnection = new Appliance();
		applianceConnection.name = "wamcdp3";
		applianceConnection.hostName = "wamcdp3.hursley.ibm.com";
		applianceConnection.ampPort = 5550;
		applianceConnection.adminUserId = "admin";
		applianceConnection.adminUserPassword = "wamcdp3";
		when(device.getSymbolicName()).thenReturn("wamcdp3");

		WamtAppliance appliance = new TestWamtAppliance(manager, applianceConnection);

		Appliance updatedConnection = new Appliance();
		updatedConnection.name = "wamcdp4";
		updatedConnection.hostName = "wamcdp4.hursley.ibm.com";
		updatedConnection.ampPort = 5551;
		updatedConnection.adminUserId = "admins";
		updatedConnection.adminUserPassword = "wamcdp4";

		doThrow(new AlreadyExistsInRepositoryException()).when(device).setSymbolicName("wamcdp4");

		appliance.updateConnection(updatedConnection);

		logger.exit("testUpdateConnectionAlreadyExists");
	}

	@Test
	public void testQuiesce() throws DeletedException, UnsuccessfulOperationException, AMPException
	{
		appliance.quiesce();
		try
		{
			Thread.sleep(10);
		}
		catch (InterruptedException e)
		{
		}
		verify(device).quiesce();
	}

	@Test
	public void testUnquiesce() throws DeletedException, UnsuccessfulOperationException, AMPException
	{
		appliance.unquiesce();
		try
		{
			Thread.sleep(500);
		}
		catch (InterruptedException e)
		{
		}
		verify(device).unquiesce();
	}

	@Test
	public void testBackupApplianceCertLocalDestination() throws Exception
	{
		appliance.backup("CERT_NAME", new URI("local://backup"), false, false);
		try
		{
			Thread.sleep(500);
		}
		catch (InterruptedException e)
		{
		}
		verify(device).backup("CERT_NAME", null, new URI("local://backup"), false, false);
	}

	@Test
	public void testRestoreApplianceCertLocalSource() throws Exception
	{
		appliance.restore("CERT_NAME", new URI("local://backup"));
		try
		{
			Thread.sleep(500);
		}
		catch (InterruptedException e)
		{
		}
		verify(device).restore("CERT_NAME", new URI("local://backup"), false);
	}

	@Test
	public void getBestFirmware() throws DeletedException
	{
		// Set up a whole menagerie of mocked firmwares and versions.
		// Two kinds of firmware, featuresABC and features BCD. Each has two
		// levels, one of which overlap (both have a v1). Each is thus the best
		// choice for the level that only it has available; a choice has to be
		// made for v1 and we arbitrarily say that featuresABC is best at that
		// level.
		final FirmwareVersion ABCv1 = mock(FirmwareVersion.class);
		when(ABCv1.getLevel()).thenReturn("v1");
		final FirmwareVersion ABCv3 = mock(FirmwareVersion.class);
		when(ABCv3.getLevel()).thenReturn("v3");

		final FirmwareVersion BCDv1 = mock(FirmwareVersion.class);
		when(BCDv1.getLevel()).thenReturn("v1");
		final FirmwareVersion BCDv2 = mock(FirmwareVersion.class);
		when(BCDv2.getLevel()).thenReturn("v2");

		Firmware featuresABC = mock(Firmware.class);
		when(featuresABC.getVersions()).thenReturn(new FirmwareVersion[] { ABCv1, ABCv3 });

		Firmware featuresBCD = mock(Firmware.class);
		when(featuresBCD.getVersions()).thenReturn(new FirmwareVersion[] { BCDv1, BCDv2 });

		when(manager.getFirmwares()).thenReturn(new Firmware[] { featuresABC, featuresBCD });

		when(manager.getBestFirmware(null, null, null, "v1")).thenReturn(featuresABC);
		when(manager.getBestFirmware(null, null, null, "v2")).thenReturn(featuresBCD);
		when(manager.getBestFirmware(null, null, null, "v3")).thenReturn(featuresABC);

		// Add other mocked methods necessary to support calls that WamtAppliance
		// and WamtFirmware will make.
		when(featuresABC.getLevel(anyString())).thenAnswer(new Answer<FirmwareVersion>()
		{
			public FirmwareVersion answer(InvocationOnMock invocation) throws Throwable
			{
				if (invocation.getArguments()[0].equals("v1")) return ABCv1;
				if (invocation.getArguments()[0].equals("v3")) return ABCv3;
				return null;
			}
		});
		when(featuresBCD.getLevel(anyString())).thenAnswer(new Answer<FirmwareVersion>()
		{
			public FirmwareVersion answer(InvocationOnMock invocation) throws Throwable
			{
				if (invocation.getArguments()[0].equals("v1")) return BCDv1;
				if (invocation.getArguments()[0].equals("v2")) return BCDv2;
				return null;
			}
		});
		when(BCDv1.getAbsoluteDisplayName()).thenReturn("featuresBCD level v1");
		when(BCDv2.getAbsoluteDisplayName()).thenReturn("featuresBCD level v2");
		when(ABCv1.getAbsoluteDisplayName()).thenReturn("featuresABC level v1");
		when(ABCv3.getAbsoluteDisplayName()).thenReturn("featuresABC level v3");

		// Now, finally, do the test. We should get exactly one best firmware
		// for each level, v1, v2, v3.
		List<SvrFirmware> bestFirmwares = appliance.getBestFirmwareVersions();

		assertTrue(bestFirmwares.size() == 3);
		assertEquals("featuresABC level v1", bestFirmwares.get(0).getDisplayName());
		assertEquals("featuresBCD level v2", bestFirmwares.get(1).getDisplayName());
		assertEquals("featuresABC level v3", bestFirmwares.get(2).getDisplayName());
	}

	class TestWamtAppliance extends WamtAppliance
	{
		public TestWamtAppliance(Manager manager, Appliance appliance) throws InvalidCredentialsException, AmcIllegalArgumentException, ApplianceConnectionException
		{
			super(manager, appliance);
		}

		ProgressContainer createDevice(Appliance appliance)
		{
			ProgressContainer pc = mock(ProgressContainer.class);

			try
			{
				if ("blurb".equals(appliance.hostName))
				{
					doThrow(new Exception(new AMPIOException())).when(pc).blockAndTrace(Level.FINER);
				}
				else if ("notright".equals(appliance.adminUserId) || "notright".equals(appliance.adminUserPassword))
				{
					doThrow(new Exception(new com.ibm.datapower.amt.amp.InvalidCredentialsException())).when(pc).blockAndTrace(Level.FINER);
				}
				else if ("dp3".equals(appliance.name))
				{
					doThrow(new Exception(new AlreadyExistsInRepositoryException())).when(pc).blockAndTrace(Level.FINER);
				}
				else
				{
					when(pc.getResult()).thenReturn(device);
				}
			}
			catch (Exception e)
			{
			}
			return pc;
		}
	}

}
