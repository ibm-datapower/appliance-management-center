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
define({
	// This map defines which appliance capabilities are needed to
	// enable the various action buttons. For each "context" (roughly,
	// page-name, usually set by the controller and injected into each
	// toolbar or other widget when it creates them) and widget 
	// (identified by its page id) there is a list of button names. 
	// Where a button has a string after it, this means the button will
	// be disabled unless the current appliance has that capability.
	// Omitting buttons from this list (where they don't need a 
	// capability) is valid, but including them with a null capability
	// value is encouraged for clarity.
	
	appliances:{
		applianceGridToolbar:{
			addAppliance:null,
			removeAppliance:null,
			assignGroups:null,
			viewDomains:"domain_config_management",
			quiesce:"device_quiesce_unquiesce",
			unquiesce:"device_quiesce_unquiesce",
			backup:"backup_restore",
			restore:"backup_restore",
			deployFirmware:"firmware_update",
			createDomain: "domain_config_management",
			reboot:null
		}
	},
	domains:{
		domainGridToolbar:{
			assignGroups:null,
			viewAppliances:null,
			viewServices:"service_config_management",
			quiesce:"domain_quiesce_unquiesce",
			unquiesce:"domain_quiesce_unquiesce",
			deleteDomain:null,
			deployConfiguration:"domain_config_management",
			createService:"service_config_management",
			uploadFile:"domain_config_management",
			restart:null
		}
	},
	services:{
		serviceGridToolbar:{
			viewDomains:null,
			deleteService:"service_config_management",
			deployConfiguration:"service_config_management"
		}
	}
});
