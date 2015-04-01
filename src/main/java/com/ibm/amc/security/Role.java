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

import java.util.Set;

import com.ibm.amc.Constants;
import com.ibm.amc.Util;
import com.ibm.amc.ras.Logger47;

/**
 * Represents a role and associated permissions
 */
public class Role
{
	// @CLASS-COPYRIGHT@

	static Logger47 logger = Logger47.get(Role.class.getCanonicalName(), Constants.CWZBA_BUNDLE_NAME);

	private static final int COLUMN_LENGTH = 255;

	private long id;
	private String name;
	private Set<Permission> permissions;

	public Role()
	{
		if (logger.isEntryEnabled()) logger.entry("Role");
		if (logger.isEntryEnabled()) logger.exit("Role");
	}

	public Role(final String name, final Set<Permission> permissions)
	{
		if (logger.isEntryEnabled()) logger.entry("Role", name, permissions);
		this.name = Util.truncateString(name, COLUMN_LENGTH);

		this.permissions = permissions;
		if (logger.isEntryEnabled()) logger.exit("Role");
	}

	public long getId()
	{
		return id;
	}

	public String getName()
	{
		return name;
	}

	public Set<Permission> getPermissions()
	{
		return permissions;
	}

	public void setName(final String name)
	{
		this.name = name;
	}

	public void setPermissions(final Set<Permission> permissions)
	{
		this.permissions = permissions;
	}

	public String toString()
	{
		StringBuilder builder = new StringBuilder("[");
		builder.append(this.getClass().getName());
		builder.append(" | name: ");
		builder.append(name);
		builder.append(" | permissions: ");
		builder.append(permissions);
		builder.append("]");
		return builder.toString();
	}

	public boolean isDefault()
	{
		return SecurityManager.DEFAULT_ROLES.contains(name);
	}

}
