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

import java.util.Date;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ibm.amc.security.SecurityContext;

public class ActionResponseTests
{
	// @CLASS-COPYRIGHT@

	private ActionResponse response;
	private Date timeStamp = new Date();

	@BeforeClass
	public static void setContext()
	{
		SecurityContext.setContext("user");
	}
	
	@Before
	public void setUp() throws Exception
	{
		response = new ActionResponse("userId", timeStamp, "CWZBA1003I_ACTION_SUCCEEDED");
	}

	@Test
	public void testConstructor()
	{
		assertEquals(timeStamp, response.timeStamp);
		assertEquals("CWZBA1003I", response.code);
		assertEquals("Completed successfully", response.message);
	}

}
