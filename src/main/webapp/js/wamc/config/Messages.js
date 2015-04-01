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

	// Appliance messages
	CONFIRM_REMOVE_APPLIANCE: {
		code:null,
		level:"Confirmation",
		link:null,
		action:"RemoveAppliance",
		close:"Cancel"
	},

	CONFIRM_REMOVE_APPLIANCES: {
		code:null,
		level:"Confirmation",
		link:null,
		action:"RemoveAppliances",
		close:"Cancel"
	},
	
	CONFIRM_QUIESCE_APPLIANCE: {
		code:null,
		level:"Confirmation",
		link:null,
		action:"QuiesceAppliance",
		close:"Cancel"
	},

	CONFIRM_QUIESCE_APPLIANCES: {
		code:null,
		level:"Confirmation",
		link:null,
		action:"QuiesceAppliances",
		close:"Cancel"
	},
	
	CONFIRM_UNQUIESCE_APPLIANCE: {
		code:null,
		level:"Confirmation",
		link:null,
		action:"UnquiesceAppliance",
		close:"Cancel"
	},

	CONFIRM_UNQUIESCE_APPLIANCES: {
		code:null,
		level:"Confirmation",
		link:null,
		action:"UnquiesceAppliances",
		close:"Cancel"
	},
	
	CONFIRM_REBOOT_APPLIANCE: {
		code:null,
		level:"Confirmation",
		link:null,
		action:"RebootAppliance",
		close:"Cancel"
	},

	CONFIRM_REBOOT_APPLIANCES: {
		code:null,
		level:"Confirmation",
		link:null,
		action:"RebootAppliance",
		close:"Cancel"
	},
	
	CONFIRM_RESTART_DOMAIN: {
		code:null,
		level:"Confirmation",
		link:null,
		action:"RestartDomain",
		close:"Cancel"
	},

	CONFIRM_RESTART_DOMAINS: {
		code:null,
		level:"Confirmation",
		link:null,
		action:"RestartDomain",
		close:"Cancel"
	},
	
	
	CONFIRM_REMOVE_FIRMWARE: {
		code:null,
		level:"Confirmation",
		link:null,
		action:"RemoveFirmware",
		close:"Cancel"
	},
	
	CONFIRM_REMOVE_FIRMWARES: {
		code:null,
		level:"Confirmation",
		link:null,
		action:"RemoveFirmwares",
		close:"Cancel"
	},
	
	CONFIRM_DELETE_SERVICE_NO_CHILDREN: {
		code:null,
		level:"Confirmation",
		link:null,
		action:"DeleteService",
		close:"Cancel"
	},
	
	SUCCESS_REMOVE_APPLIANCE: {
		code:null,
		level:"Success",
		link:null,
		close:"Close"
	},
	
	SUCCESS_ADD_APPLIANCE: {
		code:null,
		level:"Success",
		link:null,
		close:"Close"
	},
	
	SUCCESS_UPDATE_APPLIANCE: {
		code:null,
		level:"Success",
		link:null,
		close:"Close"
	},
	
	SUCCESS_UPDATE_FIRMWARE: {
		code:null,
		level:"Success",
		link:null,
		close:"Close"
	},
	
	SUCCESS_UPDATE_FIRMWARE_COMMENT: {
		code:null,
		level:"Success",
		link:null,
		close:"Close"
	},
	
	SUCCESS_REMOVE_FIRMWARE: {
		code:null,
		level:"Success",
		link:null,
		close:"Close"
	},
	
	SUCCESS_REMOVE_FIRMWARE_COMMENT: {
		code:null,
		level:"Success",
		link:null,
		close:"Close"
	},
	
	ERROR_NO_APPLIANCE_SELECTED:{
		code:null,
		level:"Error",
		link:null,
		close:"Close"
	},
	
	ERROR_ADD_APPLIANCE: {
		code:null,
		level:"Error",
		link:null,
		close:"Close"
	},
	
	ERROR_UPDATE_APPLIANCE: {
		code:null,
		level:"Error",
		link:null,
		close:"Close"
	},
	
	ERROR_REMOVE_APPLIANCE: {
		code:null,
		level:"Error",
		link:null,
		close:"Close"
	},
	
	ERROR_RETRIEVE_DATA: {
		code:null,
		level:"Error",
		link:null,
		close:"Close"
	},
	
	ERROR_NO_DOMAIN_SELECTED: {
		code:null,
		level:"Error",
		link:null,
		close:"Close"
	},
	
	ERROR_QUIESCE_APPLIANCE: {
		code:null,
		level:"Error",
		link:null,
		close:"Close"
	},
	
	ERROR_UNQUIESCE_APPLIANCE: {
		code:null,
		level:"Error",
		link:null,
		close:"Close"
	},
	
	// Domain Messages
	CONFIRM_QUIESCE_DOMAIN: {
		code:null,
		level:"Confirmation",
		link:null,
		action:"QuiesceDomain",
		close:"Cancel"
	},

	CONFIRM_QUIESCE_DOMAINS: {
		code:null,
		level:"Confirmation",
		link:null,
		action:"QuiesceDomains",
		close:"Cancel"
	},
	
	CONFIRM_UNQUIESCE_DOMAIN: {
		code:null,
		level:"Confirmation",
		link:null,
		action:"UnquiesceDomain",
		close:"Cancel"
	},

	CONFIRM_UNQUIESCE_DOMAINS: {
		code:null,
		level:"Confirmation",
		link:null,
		action:"UnquiesceDomains",
		close:"Cancel"
	},
	
	SUCCESS_DELETE_DOMAIN: {
		code:null,
		level:"Success",
		link:null,
		close:"Close"
	},
	
	SUCCESS_UPDATE_DOMAIN: {
		code:null,
		level:"Success",
		link:null,
		close:"Close"
	},
	
	ERROR_QUIESCE_DOMAIN: {
		code:null,
		level:"Error",
		link:null,
		close:"Close"
	},
	
	ERROR_UNQUIESCE_DOMAIN: {
		code:null,
		level:"Error",
		link:null,
		close:"Close"
	},
	
	// Firmware Messages
	ERROR_ADD_FIRMWARE: {
		code:null,
		level:"Error",
		link:null,
		close:"Close"
	},
	
	ERROR_REMOVE_FIRMWARE: {
		code:null,
		level:"Error",
		link:null,
		close:"Close"
	},
	
	ERROR_UPLOADING_FILE: {
		code:null,
		level:"Error",
		link:null,
		close:"Close"
	},
	
	// Service messages
	CONFIRM_QUIESCE_SERVICE: {
		code:null,
		level:"Confirmation",
		link:null,
		action:"QuiesceService",
		close:"Cancel"
	},

	CONFIRM_QUIESCE_SERVICES: {
		code:null,
		level:"Confirmation",
		link:null,
		action:"QuiesceServices",
		close:"Cancel"
	},
	
	CONFIRM_UNQUIESCE_SERVICE: {
		code:null,
		level:"Confirmation",
		link:null,
		action:"UnquiesceService",
		close:"Cancel"
	},

	CONFIRM_UNQUIESCE_SERVICES: {
		code:null,
		level:"Confirmation",
		link:null,
		action:"UnquiesceServices",
		close:"Cancel"
	},
	
	ERROR_NO_SERVICE_SELECTED: {
		code:null,
		level:"Error",
		link:null,
		close:"Close"
	},
	
	ERROR_MULTIPLE_SERVICES_SELECTED: {
		code:null,
		level:"Error",
		link:null,
		close:"Close"
	},
	
	SUCCESS_DELETE_SERVICE: {
		code:null,
		level:"Success",
		link:null,
		close:"Close"
	},
	
	ERROR_SERVICE_IMPACT:{
		code:null,
		level:"Error",
		link:null,
		close:"Close"
	},
	
	ERROR_NO_MATCHING_SERVICES:{
		code:null,
		level:"Error",
		link:null,
		close:"Close"
	},
	
	ERROR_NO_CONTAINED_SERVICES:{
		code:null,
		level:"Error",
		link:null,
		close:"Close"
	},
	
	ERROR_QUIESCE_SERVICE: {
		code:null,
		level:"Error",
		link:null,
		close:"Close"
	},
	
	ERROR_UNQUIESCE_SERVICE: {
		code:null,
		level:"Error",
		link:null,
		close:"Close"
	},
	
	SUCCESS_ASSIGN_GROUPS: {
		code:null,
		level:"Success",
		link:null,
		close:"Close"
	},
	
	SUCCESS_ASSIGN_GROUP: {
		code:null,
		level:"Success",
		link:null,
		close:"Close"
	},
	
	ERROR_GROUP_EXISTS: {
		code:null,
		level:"Error",
		link:null,
		close:"Close"
	},
	
	SUCCESS_UPDATE_USER:{
		code:null,
		level:"Success",
		link:null,
		close:"Close"
	},
	
	SUCCESS_UPDATE_USER_GROUP:{
		code:null,
		level:"Success",
		link:null,
		close:"Close"
	},
	
	CONFIRM_DELETE_ROLE: {
		code:null,
		level:"Confirmation",
		link:null,
		action:"DeleteUserRole",
		close:"Cancel"
	},
	
	CONFIRM_DELETE_ROLE_MEMBERS: {
		code:null,
		level:"Confirmation",
		link:null,
		action:"DeleteUserRole",
		close:"Cancel"
	},
	
	SUCCESS_CREATE_ROLE:{
		code:null,
		level:"Success",
		link:null,
		close:"Close"
	},
	
	SUCCESS_UPDATE_ROLE:{
		code:null,
		level:"Success",
		link:null,
		close:"Close"
	},
	
	SUCCESS_DELETE_ROLE:{
		code:null,
		level:"Success",
		link:null,
		close:"Close"
	},
	
	ERROR_NO_ROLE_SELECTED:{
		code:null,
		level:"Error",
		link:null,
		close:"Close"
	},
	
	// Error Messages with codes
	CWZGUE0001E_GENERAL_WAMT_ERROR: {
		code:"CWZGUE0001E",
		level:"Error",
		link:"https://ibm.com"
	},

	CWZGUE0002E_NO_SUCH_RESOURCE: { 
		code:"CWZGUE0002E",
		level:"Error",
		link:"https://ibm.com"
	},

	ABOUT_TEXT: {
		code:null,
		level:"Information",
		link:null,
		close:"Close"
	},
	
	ERROR_NO_RESPONSE: {
		code:null,
		level:"Error",
		link:null,
		close:"Close"
		
	},
	
	BACKUP_UPLOADING: {
		code:null,
		level:"Information",
		link:null,
		close:"Close"
	}
});
