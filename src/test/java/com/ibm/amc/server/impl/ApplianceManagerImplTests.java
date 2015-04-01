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
package com.ibm.amc.server.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.List;

import javax.naming.NamingException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.ibm.amc.Constants;
import com.ibm.amc.data.SvrAppliance;
import com.ibm.amc.ras.Logger47;
import com.ibm.amc.resources.data.Appliance;
import com.ibm.amc.resources.exceptions.ApplianceConnectionException;
import com.ibm.amc.resources.exceptions.NoSuchResourceException;
import com.ibm.amc.security.SecurityContext;
import com.ibm.amc.utils.PersistenceTools;
import com.ibm.datapower.amt.DMgrException;
import com.ibm.datapower.amt.DeviceType;
import com.ibm.datapower.amt.ModelType;
import com.ibm.datapower.amt.StringCollection;
import com.ibm.datapower.amt.clientAPI.Device;
import com.ibm.datapower.amt.clientAPI.ManagedSet;
import com.ibm.datapower.amt.clientAPI.ManagementOperations;
import com.ibm.datapower.amt.clientAPI.Manager;


/**
 * Tests for the interaction with WAMT.
 * 
 * @see com.ibm.amc.ApplianceManagerImpl
 * @author mallman
 */
public class ApplianceManagerImplTests
{
	// @CLASS-COPYRIGHT@

	/* logger for output during test run */
	static Logger47 logger = Logger47.get(ApplianceManagerImplTests.class.getCanonicalName(), Constants.CWZBA_BUNDLE_NAME);

	private TestApplianceManager manager;

	private Manager wamtManager;

	private Device[] devices;

	@Before
	public void setupPersistenceContext() throws IllegalStateException, SecurityException, NamingException
	{
		PersistenceTools.setPersistenceContext();
	}

	@Before
	public void setupSecurityContext()
	{
		SecurityContext.setContext("user");
	}

	@Before
	public void initialiseManagers() throws Exception
	{
		wamtManager = mock(Manager.class);

		devices = new Device[2];
		devices[0] = mockDevice("dp1");
		devices[1] = mockDevice("dp2");

		when(wamtManager.getAllDevices()).thenReturn(devices);
		when(wamtManager.getDeviceBySerialNumber("dp1")).thenReturn(devices[0]);
		when(wamtManager.getDeviceBySerialNumber("dp2")).thenReturn(devices[1]);
		ManagedSet defaultManagedSet = mock(ManagedSet.class);
		when(wamtManager.getManagedSet(Constants.DEFAULT_MANAGED_SET)).thenReturn(defaultManagedSet);
		
		Field field = Manager.class.getDeclaredField("singleton");
		field.setAccessible(true);
		field.set(null, wamtManager);
		
		manager = new TestApplianceManager();
	}

	/**
	 * get the list of currently defined appliances.
	 * 
	 * @throws DMgrException
	 * @throws ApplianceConnectionException
	 */
	@Test
	public void testGetAppliances() throws DMgrException, ApplianceConnectionException
	{
		logger.entry("\n\ngetAppliances");

		List<SvrAppliance> appliances = manager.getAppliances();

		assertEquals("incorrect number of appliances found in repo", 2, appliances.size());

		for (SvrAppliance appliance : appliances)
		{
			if (appliance.getName() != "dp1" && appliance.getName() != "dp2")
			{
				/* we should have found something... */
				fail("incorrect appliance found");
			}
		}
		logger.exit("getAppliances");
	}

	@Test
	public void testGetApplianceById() throws DMgrException, NoSuchResourceException, ApplianceConnectionException
	{
		SvrAppliance appliance = manager.getApplianceById("dp2");
		assertEquals("Asked for wamcdp2 in test repository but got something else:", "dp2", appliance.getName());
	}

	@Test(expected = NoSuchResourceException.class)
	public void testGetApplianceByIdNoSuchResource() throws DMgrException, NoSuchResourceException, ApplianceConnectionException
	{
		manager.getApplianceById("no-such-appliance");
	}

	@Test
	public void testRemoveAppliance() throws DMgrException, ApplianceConnectionException
	{
		manager.removeAppliance("dp2");

		ArgumentCaptor<Device> device = ArgumentCaptor.forClass(Device.class);
		verify(wamtManager).remove(device.capture());
		assertEquals("dp2", device.getValue().getSymbolicName());
	}

	@Test(expected = NoSuchResourceException.class)
	public void testRemoveApplianceNoSuchResource() throws DMgrException
	{
		manager.removeAppliance("no-such-appliance");
	}

	@Test(expected = NoSuchResourceException.class)
	public void testUpdateApplianceNoSuchResource()
	{
		manager.updateAppliance("no-such-appliance", null);
	}

	@Test
	public void testUpdateHostname() throws Exception
	{

		Appliance appliance = manager.getApplianceById("dp1").toRest();
		appliance.hostName = "rubbish";
		manager.updateAppliance("dp1", appliance);

		verify(devices[0]).setHostname("rubbish");
	}

	@Test
	public void testUpdateAmpPort() throws Exception
	{

		Appliance appliance = manager.getApplianceById("dp1").toRest();
		appliance.ampPort = 7777;
		manager.updateAppliance("dp1", appliance);

		verify(devices[0]).setHLMPort(7777);
	}

	@Test
	public void testUpdateAdminUserId() throws Exception
	{

		Appliance appliance = manager.getApplianceById("dp1").toRest();
		appliance.adminUserId = "tosh";
		manager.updateAppliance("dp1", appliance);

		verify(devices[0]).setUserId("tosh");
	}

	@Test
	public void testUpdateQuiesceTimeout() throws Exception
	{
		logger.entry("testNegativeQuiesceTimeout");

		Appliance appliance = manager.getApplianceById("dp1").toRest();
		appliance.quiesceTimeout = 1234;
		manager.updateAppliance("dp1", appliance);

		verify(devices[0]).setQuiesceTimeout(1234);

		logger.exit("testNegativeQuiesceTimeout");
	}

	public class TestApplianceManager extends ApplianceManagerImpl
	{

		Exception createDeviceException;

		public TestApplianceManager() {
			super(wamtManager);
		}

		@Override
		ManagedSet getDefaultManagedSet()
		{
			return mock(ManagedSet.class);
		}

	}

	public static Device mockDevice(String name) throws Exception
	{
		Device device = mock(Device.class);
		when(device.getSymbolicName()).thenReturn(name);
		when(device.getSerialNumber()).thenReturn(name);
		when(device.getDeviceType()).thenReturn(DeviceType.XI52);
		when(device.getModelType()).thenReturn(ModelType.TYPE_9005);
		when(device.getAllDomainNames()).thenReturn(new StringCollection());
		when(device.getSupportedOperations()).thenReturn(new ManagementOperations[]{ManagementOperations.BACKUP_RESTORE, ManagementOperations.DOMAIN_CONFIG_MANAGEMENT});
		when(device.getPrimaryKey()).thenReturn(name);
		return device;
	}

}
