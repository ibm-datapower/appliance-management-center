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
package com.ibm.amc.resources;

import static org.junit.Assert.assertEquals;

import javax.naming.NamingException;

import org.junit.Before;
import org.junit.Test;

import com.ibm.amc.security.SecurityContext;
import com.ibm.amc.utils.PersistenceTools;

public class CurrentUserResourceTests
{
	// @CLASS-COPYRIGHT@

	@Before
	public void setupPersistenceContext() throws IllegalStateException, SecurityException, NamingException
	{
		PersistenceTools.setPersistenceContext();
	}

	@Test
	public void testGetCurrent() throws Exception
	{
		SecurityContext.setContext("dummy user");
		CurrentUserResource resource = new CurrentUserResource();

		assertEquals("CurrentUserResource created a user with a different name to the current thread", "dummy user", resource.getCurrent().name);
	}

}
