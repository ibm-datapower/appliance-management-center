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
package com.ibm.amc;

public class Constants
{
	// @CLASS-COPYRIGHT@

	// server message bundle
	public static final String CWZBA_BUNDLE_NAME = "com.ibm.amc.nls.CWZBAMessages";
	
	// validation
	public static final int QUIESCE_TIMEOUT_MIN = 0;
	public static final int QUIESCE_TIMEOUT_MAX = Integer.MAX_VALUE;
	public static final int PORT_NUMBER_MIN = 0;
	public static final int PORT_NUMBER_MAX = 65535;
	
	// for auto-management, until WAMT manages everything by default.
	public static final String DEFAULT_MANAGED_SET = "WAMC";

	public static final String DEFAULT_DOMAIN = "default";
	
	// Action persistence
	public static final String JNDI_USER_TRANSACTION = "java:comp/UserTransaction";
	public static final String JNDI_ENTITY_MANAGER = "java:comp/env/jpa/entitymanager";
	public static final String QUERY_ACTION_STATUS_ALL = "SELECT s FROM ActionStatus s ORDER BY s.updated DESC";
	public static final String QUERY_ACTION_STATUS_FOR_USER = "SELECT s FROM ActionStatus s WHERE s.userId = :userId ORDER BY s.updated DESC";
	public static final String QUERY_ROLES_ALL = "SELECT r, LOWER(r.name) as orderField FROM Role r ORDER BY orderField ASC";
	public static final String QUERY_ROLES_FOR_USER = "SELECT r, LOWER(r.name) as orderField FROM Role r WHERE :user MEMBER OF r.users ORDER BY orderField ASC";
	public static final String QUERY_ROLE_BY_NAME = "SELECT r FROM Role r WHERE r.name = :name";
	public static final String QUERY_ROLES_FOR_USER_GROUP = "SELECT r, LOWER(r.name) as orderField FROM Role r WHERE :group MEMBER OF r.userGroups ORDER BY orderField ASC";
	public static final String QUERY_UNIQUENESS_CHECK = "SELECT x FROM %s AS x WHERE x.%s = :value";
	
	// Groups
	public static final String GROUPS_TAG_KEY = "com.ibm.amc.tags.group";
}
