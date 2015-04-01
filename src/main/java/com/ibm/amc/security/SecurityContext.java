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
package com.ibm.amc.security;

import java.util.HashSet;
import java.util.Set;

/**
 * Provides security information in the context of a REST service request.
 */
public final class SecurityContext
{
	// @CLASS-COPYRIGHT@

	/**
	 * The security name of the user associated with the thread.
	 */
	private final String userName;

	/**
	 * The user permissions for the user associated with the thread (for synchronous invocations).
	 */
	private Set<Permission> userPermissions;

	/**
	 * Thread local containing contexts.
	 */
	private static final ThreadLocal<SecurityContext> CONTEXT = new ThreadLocal<SecurityContext>();

	/**
	 * Constructor.
	 * 
	 * @param user
	 *            the name of the user to associate with the thread
	 * @param permissions
	 *            optionally, the permissions associated with the user
	 */
	private SecurityContext(final String userName, final Set<Permission> userPermissions)
	{
		this.userName = userName;
		this.userPermissions = userPermissions;
	}

	/**
	 * Sets the context for the current thread based on an HTTP request.
	 */
	public static void setContext(HashSet<String> userRoles, String userName)
	{
		String securityName = userName;
		Set<Role> roles = new HashSet<Role>();

		// Get roles for user
		roles.addAll(SecurityManager.getInstance().getRoles(userRoles));

		final Set<Permission> permissions = new HashSet<Permission>();
		for (Role role : roles)
			permissions.addAll(role.getPermissions());

		CONTEXT.set(new SecurityContext(securityName, permissions));
	}

	/**
	 * Sets the context for the current thread.
	 * 
	 * @param user
	 *            the name of the user
	 */
	public static void setContext(final String user)
	{
		CONTEXT.set(new SecurityContext(user, new HashSet<Permission>()));
	}

	/**
	 * Gets the context for the current thread.
	 * 
	 * @return the security context for the current thread, or <code>null</code> if the current
	 *         thread does not represent a REST service request
	 */
	public static SecurityContext getContext()
	{
		return CONTEXT.get();
	}

	/**
	 * Destroys the context associated with the current thread.
	 */
	protected static void destroy()
	{
		CONTEXT.remove();
	}

	/**
	 * The user associated with the current thread of execution.
	 * 
	 * @return the user
	 */
	public String getUser()
	{
		return userName;
	}

	public Set<Permission> getUserPermissions()
	{
		return userPermissions;
	}

}
