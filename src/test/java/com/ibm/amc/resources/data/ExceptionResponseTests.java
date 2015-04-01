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
import static org.junit.Assert.assertNotNull;

import java.util.Date;

import javax.ws.rs.core.Response.Status;

import org.junit.BeforeClass;
import org.junit.Test;

import com.ibm.amc.AmcRuntimeException;
import com.ibm.amc.security.SecurityContext;
import com.ibm.datapower.amt.DMgrException;

public class ExceptionResponseTests
{
	// @CLASS-COPYRIGHT@

	@BeforeClass
	public static void setContext()
	{
		SecurityContext.setContext("user");
	}
	
	@Test
	public void testConstructorMessage()
	{
		Date timeStamp = new Date();
		ExceptionResponse response = new ExceptionResponse("userId", timeStamp, "CWZBA1002E_ACTION_FAILED");

		assertEquals(timeStamp, response.timeStamp);
		assertEquals("CWZBA1002E", response.code);
		assertEquals("Failed to complete successfully.", response.message);
	}

	@Test
	public void testConstructorGenericException()
	{
		ExceptionResponse response = new ExceptionResponse(new Throwable("TEST_EXCEPTION"));

		assertEquals("java.lang.Throwable: TEST_EXCEPTION", response.message);
	}

	@Test
	public void testConstructorLocalizedException()
	{
		ExceptionResponse response = new ExceptionResponse(new AmcRuntimeException(Status.INTERNAL_SERVER_ERROR, "CWZBA0509E_APPLIANCE_CONNECTION_ERROR", new String[] { "APPLIANCE", "5550" }));

		assertEquals("Failed to connect to the appliance APPLIANCE on port 5550.", response.message);
	}

	@Test
	public void testConstructorNestedException()
	{
		ExceptionResponse response = new ExceptionResponse(new Throwable("TEST_EXCEPTION", new Throwable("TEST_CAUSE")));

		assertNotNull(response.cause);
		assertEquals("java.lang.Throwable: TEST_CAUSE", response.cause.message);
	}

	@Test
	public void testConstructorDmgrException()
	{
		ExceptionResponse response = new ExceptionResponse(new DMgrException("WAMT0002E: The INSERT option was missing when instantiating the DataPower appliance manager.",
				"wamt.clientAPI.Manager.missingOpt", new String[] { "INSERT" }));

		assertEquals("The INSERT option was missing when instantiating the DataPower appliance manager.", response.message);
		assertEquals("WAMT0002E", response.code);
	}
}
