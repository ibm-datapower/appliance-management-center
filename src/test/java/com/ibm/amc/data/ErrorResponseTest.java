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

import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.ResourceBundle;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.amc.Constants;
import com.ibm.amc.resources.data.ExceptionResponse;
import com.ibm.amc.security.SecurityContext;

public class ErrorResponseTest
{
	// @CLASS-COPYRIGHT@

	private ExceptionResponse responseObject;
	private String responseJson;

	private static String INSERT = "INSERT";
	private static String KEY = "CWZBA9999E_EMERGENCY_MSG";

	@BeforeClass
	public static void setContext()
	{
		SecurityContext.setContext("userId");
	}
	
	@Before
	public void setupResponse() throws Exception
	{
		responseObject = new ExceptionResponse("userId", new Date(), KEY, INSERT);
		responseJson = new ObjectMapper().writeValueAsString(responseObject);
	}

	@Test
	public void testMessage()
	{
		assertTrue(responseJson.contains("\"code\":\"CWZBA9999E\""));
		assertTrue(responseJson.contains("\"message\":\"" + INSERT + "\""));
	}

	@Test
	public void testExplanation()
	{
		ResourceBundle bundle = ResourceBundle.getBundle(Constants.CWZBA_BUNDLE_NAME);
		assertTrue(responseJson.contains("\"explanation\":\"" + bundle.getString(KEY + ".explanation") + "\""));
	}

	@Test
	public void testUserAction()
	{
		ResourceBundle bundle = ResourceBundle.getBundle(Constants.CWZBA_BUNDLE_NAME);
		assertTrue(responseJson.contains("\"userAction\":\"" + bundle.getString(KEY + ".useraction") + "\""));
	}

}
