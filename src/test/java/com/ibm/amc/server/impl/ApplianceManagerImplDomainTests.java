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
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.ibm.amc.Constants;
import com.ibm.amc.data.SvrDomain;
import com.ibm.amc.server.ApplianceManager;
import com.ibm.datapower.amt.StringCollection;
import com.ibm.datapower.amt.clientAPI.Device;
import com.ibm.datapower.amt.clientAPI.Domain;
import com.ibm.datapower.amt.clientAPI.ManagedSet;
import com.ibm.datapower.amt.clientAPI.ManagementOperations;
import com.ibm.datapower.amt.clientAPI.Manager;

public class ApplianceManagerImplDomainTests
{
	// @CLASS-COPYRIGHT@

	private Manager manager;
	private ApplianceManager applianceManager;
	private ManagedSet mockDefaultSet;
	private Device[] mockDevices;

	@Before
	public void setup() throws Exception
	{
		mockDefaultSet = mock(ManagedSet.class);
		manager = mock(Manager.class);
		when(manager.getManagedSet(Constants.DEFAULT_MANAGED_SET)).thenReturn(mockDefaultSet);
	}

	/**
	 * Test that multiple domains and multiple devices are correctly collected.
	 */
	@Test
	public void testGetAllDomains() throws Exception
	{
		// Rather than define lots of mock domains (which we could, but it's a
		// lot of copy-paste code) this Answer creates them on the fly.
		Answer<Domain> domainAnswer = new Answer<Domain>()
		{
			@Override
			public Domain answer(InvocationOnMock invocation) throws Throwable
			{
				String domainName = (String) invocation.getArguments()[0];
				Domain domain = mock(Domain.class);
				when(domain.getName()).thenReturn(domainName);

				Device device = mock(Device.class);
				when(device.getSymbolicName()).thenReturn("DeviceFor_" + domainName);
				when(domain.getDevice()).thenReturn(device);

				return domain;
			}
		};

		// Set up a structure of mock devices and domains. All are managed.
		Device noDomainsDevice = mock(Device.class);
		when(noDomainsDevice.getSymbolicName()).thenReturn("noDomainsDevice");
		when(noDomainsDevice.getAllDomainNames()).thenReturn(new StringCollection(new String[] {}));
		when(noDomainsDevice.getSerialNumber()).thenReturn("1");
		when(noDomainsDevice.getSupportedOperations()).thenReturn(new ManagementOperations[0]);
		when(noDomainsDevice.getPrimaryKey()).thenReturn("1");

		Device oneDomainDevice = mock(Device.class);
		when(oneDomainDevice.getSymbolicName()).thenReturn("oneDomainDevice");
		when(oneDomainDevice.getAllDomainNames()).thenReturn(new StringCollection(new String[] { "onlyDomain" }));
		when(oneDomainDevice.getManagedDomain("onlyDomain")).thenAnswer(domainAnswer);
		when(oneDomainDevice.getSerialNumber()).thenReturn("2");
		when(oneDomainDevice.getSupportedOperations()).thenReturn(new ManagementOperations[0]);
		when(oneDomainDevice.getPrimaryKey()).thenReturn("2");

		Device threeDomainsDevice = mock(Device.class);
		when(threeDomainsDevice.getSymbolicName()).thenReturn("threeDomainsDevice");
		when(threeDomainsDevice.getAllDomainNames()).thenReturn(new StringCollection(new String[] { "domainOne", "domainTwo", "domainThree" }));
		when(threeDomainsDevice.getManagedDomain("domainOne")).thenAnswer(domainAnswer);
		when(threeDomainsDevice.getManagedDomain("domainTwo")).thenAnswer(domainAnswer);
		when(threeDomainsDevice.getManagedDomain("domainThree")).thenAnswer(domainAnswer);
		when(threeDomainsDevice.getSerialNumber()).thenReturn("3");
		when(threeDomainsDevice.getSupportedOperations()).thenReturn(new ManagementOperations[0]);
		when(threeDomainsDevice.getPrimaryKey()).thenReturn("3");

		mockDevices = new Device[] { noDomainsDevice, oneDomainDevice, threeDomainsDevice };
		when(manager.getAllDevices()).thenReturn(mockDevices);
		applianceManager = new ApplianceManagerImpl(manager);

		// Ask the appliance manager under test to get all the domains
		List<SvrDomain> domains = applianceManager.getDomains();

		// Check it didn't ask the noDomains device for the domains it doesn't have.
		verify(noDomainsDevice, never()).getManagedDomain(anyString());

		assertEquals("Wrong number of domains found.", 4, domains.size());

		// devices and domains were defined via arrays, so order should be stable.
		assertEquals("onlyDomain", domains.get(0).getName());
		assertEquals("DeviceFor_domainOne", domains.get(1).getApplianceName());

	}

	/**
	 * Test that managed domains are created to replace unmanaged domains if a device has them.
	 */
	@Test
	public void testGetUnmanagedDomains() throws Exception
	{
		Device oneDomainDevice = mock(Device.class);
		when(oneDomainDevice.getSymbolicName()).thenReturn("oneDomainDevice");
		when(oneDomainDevice.getDisplayName()).thenReturn("oneDomainDevice");
		when(oneDomainDevice.getAllDomainNames()).thenReturn(new StringCollection(new String[] { "unmanagedDomain" }));
		when(oneDomainDevice.getSupportedOperations()).thenReturn(new ManagementOperations[0]);

		Domain newManagedDomain = mock(Domain.class);
		when(newManagedDomain.getName()).thenReturn("managedDomain");
		Device device = mock(Device.class);
		when(device.getSymbolicName()).thenReturn("device");
		when(device.getDisplayName()).thenReturn("device");
		when(newManagedDomain.getDevice()).thenReturn(device);

		when(oneDomainDevice.getManagedDomain("unmanagedDomain")).thenReturn(newManagedDomain);

		mockDevices = new Device[] { oneDomainDevice };
		when(manager.getAllDevices()).thenReturn(mockDevices);
		applianceManager = new ApplianceManagerImpl(manager);

		// Ask the appliance manager under test to get all the domains
		List<SvrDomain> domains = applianceManager.getDomains();

		assertEquals("Wrong number of domains found.", 1, domains.size());

		assertEquals("managedDomain", domains.get(0).getName());
		assertEquals("device", domains.get(0).getApplianceName());

	}
	
}
