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

import static com.ibm.amc.security.Permission.APPLIANCE_ADD;
import static com.ibm.amc.security.Permission.APPLIANCE_ASSIGN_GROUPS;
import static com.ibm.amc.security.Permission.APPLIANCE_BACKUP;
import static com.ibm.amc.security.Permission.APPLIANCE_CREATE_DOMAIN;
import static com.ibm.amc.security.Permission.APPLIANCE_DEPLOY_FIRMWARE;
import static com.ibm.amc.security.Permission.APPLIANCE_EDIT_PROPERTIES;
import static com.ibm.amc.security.Permission.APPLIANCE_QUIESCE;
import static com.ibm.amc.security.Permission.APPLIANCE_REBOOT;
import static com.ibm.amc.security.Permission.APPLIANCE_REMOVE;
import static com.ibm.amc.security.Permission.APPLIANCE_RESTORE;
import static com.ibm.amc.security.Permission.APPLIANCE_UNQUIESCE;
import static com.ibm.amc.security.Permission.APPLIANCE_VIEW;
import static com.ibm.amc.security.Permission.DOMAIN_ASSIGN_GROUPS;
import static com.ibm.amc.security.Permission.DOMAIN_DELETE;
import static com.ibm.amc.security.Permission.DOMAIN_EDIT_PROPERTIES;
import static com.ibm.amc.security.Permission.DOMAIN_QUIESCE;
import static com.ibm.amc.security.Permission.DOMAIN_RESTART;
import static com.ibm.amc.security.Permission.DOMAIN_UNQUIESCE;
import static com.ibm.amc.security.Permission.DOMAIN_UPDATE_CONFIGURATION;
import static com.ibm.amc.security.Permission.DOMAIN_UPLOAD_FILE;
import static com.ibm.amc.security.Permission.DOMAIN_VIEW;
import static com.ibm.amc.security.Permission.FIRMWARE_ADD;
import static com.ibm.amc.security.Permission.FIRMWARE_EDIT_PROPERTIES;
import static com.ibm.amc.security.Permission.FIRMWARE_REMOVE;
import static com.ibm.amc.security.Permission.FIRMWARE_VIEW;
import static com.ibm.amc.security.Permission.HISTORY_VIEW;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.ibm.amc.Constants;
import com.ibm.amc.ras.Logger47;

public class SecurityManager
{
	// @CLASS-COPYRIGHT@

	static Logger47 logger = Logger47.get(SecurityManager.class.getCanonicalName(), Constants.CWZBA_BUNDLE_NAME);

	private static class SecurityManagerHolder
	{
		public static SecurityManager INSTANCE = new SecurityManager();
	}

	static String APPLIANCE_SOLUTION_DEPLOYER = "Appliance Solution Deployer";
	static String APPLIANCE_SYSTEM_ADMINISTRATOR = "Appliance System Administrator";
	static String APPLIANCE_SYSTEM_OPERATOR = "Appliance System Operator";
	static String MANAGEMENT_CENTER_ADMINISTRATOR = "Management Center Administrator";

	static List<String> DEFAULT_ROLES = new ArrayList<String>();
	static
	{
		DEFAULT_ROLES.add(MANAGEMENT_CENTER_ADMINISTRATOR);
		DEFAULT_ROLES.add(APPLIANCE_SYSTEM_ADMINISTRATOR);
		DEFAULT_ROLES.add(APPLIANCE_SOLUTION_DEPLOYER);
		DEFAULT_ROLES.add(APPLIANCE_SYSTEM_OPERATOR);
	}

	static Map<String, HashSet<Permission>> DEFAULT_ROLE_PERMISSIONS = new HashMap<String, HashSet<Permission>>();
	static
	{
		DEFAULT_ROLE_PERMISSIONS.put(MANAGEMENT_CENTER_ADMINISTRATOR, new HashSet<Permission>(Arrays.asList(new Permission[] { HISTORY_VIEW })));
		DEFAULT_ROLE_PERMISSIONS.put(
				APPLIANCE_SYSTEM_ADMINISTRATOR,
				new HashSet<Permission>(Arrays.asList(new Permission[] { FIRMWARE_ADD, FIRMWARE_REMOVE, FIRMWARE_EDIT_PROPERTIES, FIRMWARE_VIEW, APPLIANCE_VIEW, APPLIANCE_ADD, APPLIANCE_REMOVE,
						APPLIANCE_REBOOT, APPLIANCE_BACKUP, APPLIANCE_RESTORE, APPLIANCE_DEPLOY_FIRMWARE, APPLIANCE_EDIT_PROPERTIES, APPLIANCE_QUIESCE, APPLIANCE_UNQUIESCE, APPLIANCE_ASSIGN_GROUPS,
						DOMAIN_VIEW })));
		DEFAULT_ROLE_PERMISSIONS.put(
				APPLIANCE_SOLUTION_DEPLOYER,
				new HashSet<Permission>(Arrays.asList(new Permission[] { FIRMWARE_VIEW, DOMAIN_VIEW, DOMAIN_UPDATE_CONFIGURATION, DOMAIN_DELETE, DOMAIN_QUIESCE, DOMAIN_UNQUIESCE, DOMAIN_RESTART,
						DOMAIN_UPLOAD_FILE, DOMAIN_EDIT_PROPERTIES, DOMAIN_ASSIGN_GROUPS, APPLIANCE_VIEW, APPLIANCE_CREATE_DOMAIN })));
		DEFAULT_ROLE_PERMISSIONS.put(APPLIANCE_SYSTEM_OPERATOR, new HashSet<Permission>(Arrays.asList(new Permission[] { DOMAIN_VIEW, FIRMWARE_VIEW, APPLIANCE_VIEW })));
	}

	static public String MANAGEMENT_CENTER_ADMINISTRATORS = "ManagementCenterAdministrators";
	static public String SYSTEM_ADMINISTRATORS = "SystemAdministrators";
	static public String SOLUTION_DEPLOYERS = "SolutionDeployers";
	static public String SYSTEM_OPERATORS = "SystemOperators";

	static Map<String, String> DEFAULT_ROLE_MAPPINGS = new HashMap<String, String>();
	static
	{
		DEFAULT_ROLE_MAPPINGS.put(MANAGEMENT_CENTER_ADMINISTRATOR, MANAGEMENT_CENTER_ADMINISTRATORS);
		DEFAULT_ROLE_MAPPINGS.put(APPLIANCE_SYSTEM_ADMINISTRATOR, SYSTEM_ADMINISTRATORS);
		DEFAULT_ROLE_MAPPINGS.put(APPLIANCE_SOLUTION_DEPLOYER, SOLUTION_DEPLOYERS);
		DEFAULT_ROLE_MAPPINGS.put(APPLIANCE_SYSTEM_OPERATOR, SYSTEM_OPERATORS);
	}
	
	private HashMap<String, Role> roleMapping = new HashMap<String, Role>();

	private SecurityManager()
	{
		if (logger.isEntryEnabled()) logger.entry("<init>");

		for (String roleName : DEFAULT_ROLES)
		{
			final Role role = new Role(roleName, DEFAULT_ROLE_PERMISSIONS.get(roleName));
			final String groupName = DEFAULT_ROLE_MAPPINGS.get(roleName);
			roleMapping.put(groupName, role);
		}


		if (logger.isEntryEnabled()) logger.exit("<init>");
	}

	public static SecurityManager getInstance()
	{
		return SecurityManagerHolder.INSTANCE;
	}
	
	public List<Role> getRoles(HashSet<String> userRoles)
	{
		if (logger.isEntryEnabled()) logger.entry("getRoles(userRoles)");

		List<Role> result = new ArrayList<Role>();
		
		for(String role : userRoles) {
			if(roleMapping.containsKey(role))
				result.add(roleMapping.get(role));
		}
		if (logger.isEntryEnabled()) logger.exit("getRoles(userRoles)", result);
		return result;
	}
}
