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
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;

import com.ibm.amc.Constants;
import com.ibm.amc.resources.data.CurrentUser;
import com.ibm.amc.security.Permission;
import com.ibm.amc.security.Role;
import com.ibm.amc.security.SecurityContext;
import com.ibm.amc.security.SecurityManager;
import com.ibm.amc.utils.PersistenceTools;

public class CurrentUserTests
{
	// @CLASS-COPYRIGHT@

	private EntityManager em;

	@Before
	public void setupPersistenceContext() throws IllegalStateException, SecurityException, NamingException
	{
		em = PersistenceTools.setPersistenceContext();
	}

	@Test
	public void testNameTakenFromRequest() throws Exception
	{
		SecurityContext.setContext("user");
		CurrentUser user = new CurrentUser();
		assertEquals("CurrentUser object didn't use user from context", "user", user.name);
	}

	@Test
	public void testRoles() throws Exception
	{
		SecurityContext.setContext("user");
		SecurityContext.getContext().getUserPermissions().add(Permission.DOMAIN_UPDATE_CONFIGURATION);

		CurrentUser user = new CurrentUser();
		assertTrue("Wrong permissions", user.permissions.contains("domain-update-configuration"));
	}

}
