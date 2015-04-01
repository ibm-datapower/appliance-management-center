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

import com.ibm.amc.Constants;
import com.ibm.amc.ras.Logger47;

/**
 * Represents a user and their associated groups.
 */
public class User
{
	// @CLASS-COPYRIGHT@

	static Logger47 logger = Logger47.get(User.class.getCanonicalName(), Constants.CWZBA_BUNDLE_NAME);

	private String name;
	private Set<Role> roles;

	public User(final String name, final Set<Role> roles)
	{
		if (logger.isEntryEnabled()) logger.entry("User", name, roles);
		this.name = name;
		this.roles = roles;
		if (logger.isEntryEnabled()) logger.exit("User");
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * Returns the roles associated directly with this user.
	 * 
	 * @return the roles for the user
	 */
	public Set<Role> getRoles()
	{
		return roles;
	}

	public String toString()
	{
		StringBuilder builder = new StringBuilder("[");
		builder.append(this.getClass().getName());
		builder.append(" | name: ");
		builder.append(name);
		if (!roles.isEmpty())
		{
			final Set<String> roleNames = new HashSet<String>();
			for (final Role role : roles)
				roleNames.add(role.getName());
			builder.append(" | role: ");
			builder.append(roleNames);
		}
		builder.append("]");
		return builder.toString();
	}
}
