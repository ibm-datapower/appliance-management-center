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

import java.util.HashSet;
import java.util.Set;

import com.ibm.amc.data.validation.annotations.ValidateNotBlank;
import com.ibm.amc.data.validation.annotations.ValidateNotNull;
import com.ibm.amc.security.Permission;

/**
 * The REST representation of a role.
 */
public class Role
{
	// No CLASS-COPYRIGHT because we don't want copyright text in ReST responses.

	public Role()
	{
	}
	
	public Role(final com.ibm.amc.security.Role securityRole)
	{
		this.id = securityRole.getId();
		this.name = securityRole.getName();
		this.permissions = new HashSet<String>(securityRole.getPermissions().size());
		for (final Permission permission : securityRole.getPermissions())
			this.permissions.add(permission.toRest());
		this.isDefault = securityRole.isDefault();
	}

	public long id;

	@ValidateNotBlank
	public String name;

	@ValidateNotNull
	public Set<String> permissions;
	
	public boolean isDefault;
	
}
