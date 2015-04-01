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
package com.ibm.amc.data;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;

import com.ibm.amc.data.SvrAppliance.ApplianceStatus;
import com.ibm.amc.ras.Logger47;
import com.ibm.amc.resources.data.Appliance;

/**
 * @see com.ibm.amc.data.AbstractData
 * @author mallman
 */
public class AbstractDataTests
{
	// @CLASS-COPYRIGHT@

	/* logger for output during test run. No resource bundle is needed */
	static Logger47 logger = Logger47.get(AbstractDataTests.class.getCanonicalName(), null);

	@Test
	public void testHashcode()
	{
		logger.entry("\n\ntestHashcode");

		Appliance appliance1 = new Appliance();
		appliance1.name = "Appliance 1";
		appliance1.applianceType = "Skinny Model";
		appliance1.firmwareLevel = "A version to cats";
		appliance1.hostName = "127.0.0.1";

		Appliance appliance2 = new Appliance();
		appliance2.name = "Appliance 1";
		appliance2.applianceType = "Skinny Model";
		appliance2.firmwareLevel = "A version to cats";
		appliance2.hostName = "127.0.0.1";

		assertEquals("Two appliances with the same properties weren't equal", appliance1, appliance2);
		assertEquals("Two equal appliances did not have the same hashcode.", appliance1.hashCode(), appliance2.hashCode());

		logger.exit("testHashcode");
	}

	/**
	 * Test the equals override
	 * 
	 * TODO best add these as the findbugs suppress warnings annotation says they are present ;-)
	 */
	@Test
	public void testEquals()
	{
		logger.entry("\n\ntestEquals");

		logger.exit("testEquals");
	}

	/**
	 * This test may be a little fragile, as it expects an explicit string to be output from the
	 * object and this will be invalid if the Appliance class is updated due to the dynamic nature of
	 * the toString() override in AbstractData. However, it does test that dynamic logic for a known
	 * structure!
	 * 
	 * @see com.ibm.amc.resources.data.Appliance
	 */
	@Test
	public void testApplianceToString()
	{
		logger.entry("\n\ntoString");

		/* create a appliance */
		Appliance appliance = new Appliance();
		appliance.id = "id";
		appliance.adminUserId = "adminuserid";
		appliance.ampPort = 12345;
		appliance.ampVersion = "ampversion";
		appliance.firmwareLevel = "firmwarelevel";
		appliance.guiPort = 54321;
		appliance.hostName = "hostname";
		appliance.applianceType = "appliancetype";
		appliance.model = "model";
		appliance.name = "name";
		appliance.quiesceTimeout = 555;
		appliance.serialNumber = "serialnumber";
		appliance.status = ApplianceStatus.UP;
		appliance.capabilities = Arrays.asList(new String[]{"domain_config_management","update_firmware","backup_restore","service_config_management"});
		appliance.groups = Arrays.asList(new String[]{"test"});

		String actual = appliance.toString();
		String expected = "[com.ibm.amc.resources.data.Appliance | id: id | name: name | hostName: hostname | ampPort: 12345 | adminUserId: adminuserid | applianceType: appliancetype | firmwareLevel: firmwarelevel | ampVersion: ampversion | model: model | featureLicenses: null | guiPort: 54321 | firmwareManagementStatus: null | quiesceTimeout: 555 | status: up | serialNumber: serialnumber | capabilities: [domain_config_management, update_firmware, backup_restore, service_config_management] | groups: [test]]";

		assertEquals("toString didn't work as expected", expected, actual);

		logger.exit("toString");
	}
}
