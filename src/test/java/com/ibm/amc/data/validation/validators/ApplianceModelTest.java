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
package com.ibm.amc.data.validation.validators;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

public class ApplianceModelTest
{
	// @CLASS-COPYRIGHT@

	@BeforeClass
	public static void create()
	{
		validator = new ApplianceType();
	}

	private static ApplianceType validator;

	@Test
	public void testValidNames()
	{
		String[] validApplianceModels = { "XB60", "XB62", "XE82", "XI50", "XI52", "XI50B", "XM70", "XG45", "XA35", "XS40" };
		for (String model : validApplianceModels)
		{
			assertTrue("ApplianceModel validator claimed that " + model + " is not a valid appliance model.", validator.validate(model, null));
		}
	}

	@Test
	public void testInvalidName()
	{
		// An appliance we don't support.
		assertFalse("ApplianceModel validator claimed we support dishwashers.", validator.validate("Dishwasher", null));
	}

	@Test
	public void testNull()
	{
		assertTrue("Validator didn't allow empty values.", validator.validate(null, null));
	}
}
