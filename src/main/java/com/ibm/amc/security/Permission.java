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

/**
 * Care should be taken when modifying this enum. An existing config DB
 * will contain all of the values, therefore if one is removed and the
 * new server code is used with and existing config DB runtime 
 * exceptions will be thrown at runtime.
 */
public enum Permission
{

	FIRMWARE_ADD, FIRMWARE_REMOVE, FIRMWARE_VIEW, FIRMWARE_EDIT_PROPERTIES, 
	
	APPLIANCE_VIEW, APPLIANCE_ADD, APPLIANCE_REMOVE, APPLIANCE_REBOOT, APPLIANCE_BACKUP, 
	APPLIANCE_RESTORE, APPLIANCE_DEPLOY_FIRMWARE, APPLIANCE_EDIT_PROPERTIES, APPLIANCE_QUIESCE, 
	APPLIANCE_UNQUIESCE, APPLIANCE_CREATE_DOMAIN, APPLIANCE_ASSIGN_GROUPS,
	
	DOMAIN_VIEW, DOMAIN_UPDATE_CONFIGURATION, DOMAIN_DELETE, DOMAIN_QUIESCE, DOMAIN_UNQUIESCE, 
	DOMAIN_RESTART, DOMAIN_UPLOAD_FILE, DOMAIN_EDIT_PROPERTIES, DOMAIN_ASSIGN_GROUPS,
	
	HISTORY_VIEW;

	// @CLASS-COPYRIGHT@

	public String toRest()
	{
		return name().toLowerCase().replace('_', '-');
	}
	
	public static Permission fromRest(String rest)
	{
		return valueOf(rest.toUpperCase().replace('-', '_'));
	}
	
}
