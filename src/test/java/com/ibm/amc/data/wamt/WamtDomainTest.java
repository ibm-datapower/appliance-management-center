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

import java.io.File;
import java.net.URISyntaxException;
import java.util.List;

import org.junit.Test;

import com.ibm.datapower.amt.DMgrException;
import com.ibm.datapower.amt.clientAPI.DeploymentPolicy;
import com.ibm.datapower.amt.clientAPI.Device;
import com.ibm.datapower.amt.clientAPI.Domain;
import com.ibm.datapower.amt.clientAPI.DomainSynchronizationMode;
import com.ibm.datapower.amt.clientAPI.URLSource;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

public class WamtDomainTest
{
	@Test
	public void testBothFilesInUse() throws DMgrException, URISyntaxException
	{
		WamtDomain domain = createDummyDomain(DomainSynchronizationMode.AUTO, "file:///policy", "file:///source");
		List<File> filesInUse = domain.getLocalFilesInUse();
		
		assertTrue(filesInUse.size() == 2);
		assertTrue(filesInUse.contains(new File("/policy")));
		assertTrue(filesInUse.contains(new File("/source")));
	}
	
	@Test
	public void testSourceInUse() throws DMgrException, URISyntaxException
	{
		WamtDomain domain = createDummyDomain(DomainSynchronizationMode.AUTO, null, "file:///source");
		List<File> filesInUse = domain.getLocalFilesInUse();

		assertTrue(filesInUse.size() == 1);
		assertTrue(filesInUse.contains(new File("/source")));
	}
	
	@Test
	public void testPolicyInUse() throws DMgrException, URISyntaxException
	{
		WamtDomain domain = createDummyDomain(DomainSynchronizationMode.AUTO, "file:///policy", null);
		List<File> filesInUse = domain.getLocalFilesInUse();

		assertTrue(filesInUse.size() == 1);
		assertTrue(filesInUse.contains(new File("/policy")));
	}
	
	@Test
	public void testNeitherInUse() throws DMgrException, URISyntaxException
	{
		WamtDomain domain = createDummyDomain(DomainSynchronizationMode.AUTO, null, null);
		List<File> filesInUse = domain.getLocalFilesInUse();

		assertTrue(filesInUse.size() == 0);
	}
	
	@Test
	public void testNotInUseWhenNotSynching() throws DMgrException, URISyntaxException
	{
		WamtDomain domain = createDummyDomain(DomainSynchronizationMode.MANUAL, "file:///policy", null);
		List<File> filesInUse = domain.getLocalFilesInUse();

		assertTrue(filesInUse.size() == 0);
	}
	
	private WamtDomain createDummyDomain(DomainSynchronizationMode synchMode, String policyUrl, String sourceUrl) throws URISyntaxException, DMgrException
	{
		
		DeploymentPolicy dp = null;
		if(policyUrl != null) 
		{
			dp = mock(DeploymentPolicy.class);
			when(dp.getPolicyURLSource()).thenReturn(new URLSource(policyUrl));
		}
		
		Domain mockDomain = mock(Domain.class);
		when(mockDomain.getSynchronizationMode()).thenReturn(synchMode);
		when(mockDomain.getDeploymentPolicy()).thenReturn(dp);
		when(mockDomain.getSourceConfiguration()).thenReturn(sourceUrl == null? null : new URLSource(sourceUrl));
		
		Device mockDevice = mock(Device.class);
		when(mockDevice.getManagedDomain(anyString())).thenReturn(mockDomain);
		WamtDomain domain = new WamtDomain(null, mockDevice, "dummy");
		
		return domain;
	}
}
