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
define({ root:
({
	// ------------------------------------------------------------------------
	// Grid Column titles must be capitalised, labels can be sentence case.
	// ------------------------------------------------------------------------
	
	// ------------------------------------------------------------------------
	// Global Definitions
	// ------------------------------------------------------------------------
	global: {
		productName : "IBM Appliance Management Center",
		appliance: "Appliance",
		appliances: "Appliances",
		domain: "Domain",
		domains: "Domains",
		services: "Services",
		repository: "Repository",
		history: "History",
		users: "Users",
		userGroups: "User Groups",
		user: "User",
		group: "Group",
		userRoles: "Permissions",
		radioLabel: "Radio button for ",
		error: "Error",
		skipLink: "Skip to Main Content",
		standbyDefault:"Working . . .",
		hasFilterMessage:"${0} ${2} shown",
		labelSeparator: ":",
		arraySeparator: ", "
	},
	
	navigation: {
		management:"Management",
		settings:"Settings"
	},
	
	regions: {
		general:{
			feedbackBar:"Feedback Bar"
		},
		appliances:{
			grid:"Appliance Grid",
			properties:"Appliance Properties"
		},
		domains:{
			grid:"Domains Grid",
			properties:"Domain Properties"
		},
		services:{
			grid:"Services Grid",
			properties:"Service Properties"
		},
		repository:{
			grid:"Firmware Grid",
			properties:"Firmware Properties"
		},
		history:{
			grid:"Action History",
			properties:"Action Detail"
		},
		permissions:{
			userRoleList:"User Roles",
			userRolePermissions:"Permission Detail"
		},
		users:{
			grid:"Users"
		},
		userGroups:{
			grid:"User Groups"
		}
	},
	
	// ------------------------------------------------------------------------
	// About Box
	// ------------------------------------------------------------------------
	aboutBox: {
		title: "IBM Appliance Management Center",
		ibmCopyright: 'IBM&reg;, the IBM logo, and ibm.com&reg; are trademarks or registered trademarks of International Business Machines Corp., registered in many jurisdictions worldwide. Other product and service names might be trademarks of IBM or other companies. A current list of IBM trademarks is available on the web at Copyright and trademark information at <a href="http://www.ibm.com/legal/copytrade.shtml" target="_blank">http://www.ibm.com/legal/copytrade.shtml</a>.',
		releaseDateLabel: "Release: ",
		buildNumberLabel: "Build Number:"
	},	
	
	// ------------------------------------------------------------------------
	// Login
	// ------------------------------------------------------------------------
	loginForm: {
		title: "Log in",
		subTitle: "Please enter your information",
		userId: "User ID",
		password: "Password",
		logIn: "Log in",
		cancel: "Cancel",
		errorIconAltText:"Error",
		error: "Log in failed.",
		invalidMessage:"Log in failed.",
		inactivityMessage:"Please note, after some time of inactivity, the system will log you out automatically and ask you to log in again.",
		copyright: "&copy; Copyright IBM Corporation 2012-2014. All Rights Reserved."
	},
	
	
	// ------------------------------------------------------------------------
	// Appliance
	// ------------------------------------------------------------------------
	appliance: {
		title: "Appliances"
	},
	
	applianceGrid: {
		title:"Appliance Grid",
		name: "Name",
		hostName: "Host Name",
		model: "Model",
		applianceType: "Type",
		firmwareLevel: "Firmware",
		groups: "Groups",
		status: "Status"
	},
	
	applianceDetail: {
		name: "Name:",
		applianceType: "Type:",
		firmwareVersion: "Firmware:",
		groups: "Groups:",
		hostName: "Host name:",
		ampVersion: "AMP version:",
		model: "Model:",
		featureList: "Feature list:",
		ampPort: "AMP port:",
		firmwareManagementStatus: "Firmware management status:",
		status: "Status:",
		serialNumber: "Serial number:",
		adminUserId: "Appliance administrator ID:",
		adminUserPassword: "Appliance administrator password:",
		quiesceTimeout: "Quiesce timeout (seconds):",
		create: "Create",
		update: "Save",
		cancel: "Cancel"
	},

	addApplianceForm: {
		name: "Name:",
		hostName: "Host name:",
		ampPort: "AMP port:",
		adminUserId: "Appliance administrator ID:",
		adminUserPassword: "Appliance administrator password:",
		add: "Add",
		cancel: "Cancel"
	},
	
	backupApplianceWizard:{
		title:"Backup Appliance",
		submit: "Backup",
		cancel: "Cancel",
		next:"Next",
		previous:"Previous",
		cancel:"Cancel",
		done:"Backup",
		standbyText:"Uploading . . .",
		crypto:{
			title: "Provide the crypto certficate:",
			specifyBackupCryptoDetails: "Crypto certificates contain a public key that is used to encrypt the secure backup file.<br/>Use one of the following options to specify where the crypto certificate is located.",
			certificateNameRadio:"On the appliance",
			certificateName: "Crypto certificate object name:",
			linktoAppliance: "Link to the appliance web GUI",
			localFileRadio: "From a local file",
			localFile: "Select file:",
			browse: "Browse",
			remoteSourceRadio: "From a remote location (URL)",
			remoteSource: "Specify URL:"
		},
		location:{
			title: "Specify where to save the backup:",
			applianceLocalFileRadio: "Appliance local file directory",
			applianceLocalFile: "Specify the directory:",
			applianceTemporaryFileRadio: "Appliance temporary file directory",
			applianceTemporaryFile: "Specify the directory:",
			createZipFileRadio: "File for download",
			ftpRadio: "Remote FTP location:",
			ftpHostName: "Host name:",
			ftpPort: "Port:",
			ftpPath: "Filepath:",
			ftpUserId: "FTP user ID:",
			ftpUserPassword: "FTP user password:",	
		},
		options:{
			title: "Additional options:",
			additionalOptionsDetails: "You can include additional data in the secure backup:",
			includeiSCSI: "Include iSCSI data",
			includeRAID: "Include RAID data",
			information:"The time it takes for a backup to complete varies according to how much configuration and data exists on an appliance. Backups of appliances with RAID or iSCSI storage drives or backups of heavily used appliances might require significant amounts of time."
		},
		hoverHelp:{
			cryptoCertificate: "Specify the crypto certificate object name as it is shown in the appliance web GUI. <br/>To check the name, click the link to the appliance web GUI.",
			cryptoRemoteLocation: "If you exported the crypto certificate object to a .pem file, specify the remote location.<br/>Only HTTP and HTTPS protocols are supported. Retrieving the crypto certificate must not require authentication.",
			fileForDownload: "If you select this option, when the backup has completed you can download the backup file by using the link in either the Feedback bar or the History page.",
			iSCSI: "If the appliance that you are backing up contains an iSCSI storage drive, you can include data from this storage drive in the backup.",
			raid: "If the appliance that you are backing up contains RAID storage drive, you can include data from this storage drive in the backup.",
			ftpHostName: "Either the host name or IP address of the remote FTP server."
		}
	},
	
	restoreApplianceWizard:{
		title:"Restore Appliance",
		next:"Next",
		previous:"Previous",
		cancel:"Cancel",
		done:"Restore",
		standbyText:"Uploading . . .",
		information:{
			title: "Information about the secure restore operation:",
			details: "During a secure restore operation, the appliance is unavailable and it can take some time for it to come back online. The password for the administrative user on the appliance is reset to 'admin'.",
			action1: "When the appliance web GUI or command-line interface become available, you must change the administrative password.",
			action2: "To continue managing the appliance, you must ensure that the administrative password on the appliance and the password that is held by Appliance Management Center match."
		},
		crypto:{
			title: "Provide the crypto identification credential:",
			specifyBackupCryptoDetails: "Crypto certificates contain a public key that is used to encrypt a secure backup file. You must provide the corresponding crypto identification credential to perform this secure restore.",
			certificateName: "Crypto identification<br/>credential object name:",
			linktoAppliance: "Link to the appliance web GUI"
		},
		location:{
			title: "Specify where the backup is located:",
			browse: "Browse",
			applianceLocalFileRadio: "Appliance local file directory",
			applianceLocalFile: "Specify the directory:",
			applianceTemporaryFileRadio: "Appliance temporary file directory",
			applianceTemporaryFile: "Specify the directory:",
			zipFileRadio: "Local file",
			zipFile: "Select file:",
			ftpRadio: "Remote FTP location",
			ftpHostName: "Host name:",
			ftpPort: "Port:",
			ftpPath: "Filepath:",
			ftpUserId: "FTP user ID:",
			ftpUserPassword: "FTP user password:",	
		},
		hoverHelp:{
			cryptoCertificate: "Specify the crypto identification credential object name as it is shown in the appliance web GUI. <br/>To check the name, click the link to the appliance web GUI.",
			ftpHostName: "Either the host name or IP address of the remote FTP server."
		}
	},
	
	applianceGridToolbar: {
		title: "Appliance Toolbar",
		addAppliance: "Add Appliance",
		removeAppliance: "Remove Appliance",
		assignGroups: "Assign Groups",
		viewDomains: "View Domains",
		quiesce: "Quiesce Appliance",
		unquiesce: "Unquiesce Appliance",
		backup: "Backup Appliance",
		restore: "Restore Appliance",
		deployFirmware: "Deploy Firmware",
		createDomain: "Create Domain",
		reboot: "Reboot Appliance",
		moreActions:"Other Actions"
	},
	
	applianceDetailToolbar: {
		title: "Properties:",
		editProperties: "Edit Properties"
	},
	
	dialog: {
		edit_properties: "Edit Properties",
		add_appliance: "Add Appliance",
		backup_appliance: "Backup Appliance",
		restore_appliance: "Restore Appliance",
		assign_groups: "Assign Groups",
		add_firmware: "Add Firmware",
		deploy_firmware: "Deploy Firmware",
		create_domain: "Create Domain",
		domain_detail: "Domain Detail",
		firmware_detail: "Firmware Detail",
		service_detail: "Service Detail",
		appliance_detail: "Appliance Detail",
		cause: "Cause",
		explanation: "Explanation",
		userAction: "User Action",
		editDomain: "Edit Domain",
		deleteDomains: "Delete Domains",
		deployConfiguration: "Update Domain Configuration",
		createService: "Create Service",
		deleteService: "Delete Service",
		applianceProperties: "Appliance Properties",
		domainProperties: "Domain Properties",
		historyProperties: "History Properties",
		serviceProperties: "Service Properties",
		repositoryProperties: "Repository Properties",
		editFirmware: "Edit Firmware",
		applianceList: "Appliance List",
		domainList: "Domain List",
		historyList: "History List",
		serviceList: "Service List",
		repositoryList: "Repository List",
		appliancePaging: "Appliances Paging",
		domainPaging: "Domains Paging",
		servicePaging: "Services Paging",
		deployServiceConfiguration:"Update Service Configuration",
		uploadFile:"Upload File",
		addUserRole:"Create User Role"
	},
	
	firmwareAvailability: {
		available: "Firmware available",
		notAvailable: "No firmware available",
		details: "details"
	},
	
	deployFirmware: {
		setTargetFirmware: "Set the target firmware:",
		upgradesOnly: "List upgrades only",
		allFirmwareVersions: "List all compatible firmware versions",
		specifyFirmwareVersion: "Specify the firmware version:",
		mustUploadFirmware: "New firmware images must be added to the Repository page before they can be deployed to appliances.",
		summary: "Summary of selected appliances:",
		deploy: "Deploy",
		cancel: "Cancel",
		applianceName: "Name",
		applianceType: "Type",
		applianceModel: "Model",
		currentFirmware: "Current Firmware",
		targetFirmware: "Target Firmware",
		warning: "Warning",
		warningSingular: "The repository contains matching firmware images for 1 of the ${total} appliances selected. If you continue, only this appliance will be updated.",
		warningPlural: "The repository contains matching firmware images for ${updated} of the ${total} appliances selected. If you continue, these ${updated} appliances only will be updated.",
		note: "<b>Note:</b> After you click <b>Deploy</b>, you cannot stop this operation.", 
		noneAvailable: "(None available)", 
		firmwareVersion: "Firmware version:",
		firmwareFeatures: "Firmware features:",
		userComment: "User comment:",
		noFeatures: "(None)",
		acceptLicence: "I accept the terms in the license agreements."
	},

	applianceStatusTooltip: {
		up: "Up: All domains on this appliance are up",
		partial: "Partial: One or more domains on this appliance are down",
		down: "Down: All domains on this appliance, except the default domain, are down",
		unknown: "Unknown: The status of this appliance is unavailable",
		reachable: "Online: This appliance is connected; it has no domains"
	},	
	
	applianceDetailsHoverHelp: {
		name: "The name must be unique and consist of only alphanumeric, hyphen (-), period (.), or underscore (_) characters, and must begin with an alphanumeric character.",
		hostName: "Either the host name or IP address of the appliance.",
		featureList: "Features that are supported by the appliance.",
		quiesceTimeout: "The time, in seconds, that the appliance waits for in-flight transactions to complete during a quiesce operation. <br/>The default and minimum quiesce timeout period is 60 seconds.",
		ampPort: "The port number of the appliance on which the XML management interface is running with the Appliance Management Protocol (AMP) enabled."
	},

	
	backup: {
		download: " <a href=\"${uri}\">Download backup</a>" // Only translate "Download backup"
	},
	
	feedback: {
		historyLink: 'More information&#8230;',
		historyHover: 'Select this action on the History page for more information about the failure.'
	},
	
	
	// ------------------------------------------------------------------------
	// Domain
	// ------------------------------------------------------------------------
	domain: {
		title: "Domain"
	},
	
	domainGrid: {
		title: "Domain Grid",
		name: "Name",
		appliance: "Appliance",
		groups: "Groups",
		status: "Status"
	},
	
	domainGridToolbar: {
		title:"Domain Toolbar",
		assignGroups: "Assign Groups",
		viewAppliances: "View Appliances",
		viewServices: "View Services",
		quiesce: "Quiesce Domain",
		unquiesce: "Unquiesce Domain",
		deleteDomain: "Delete Domain",
		deployConfiguration: "Update Domain Configuration",
		createService: "Create Service",
		uploadFile: "Upload File",
		moreActions: "Other Actions",
		restart: "Restart Domain"
	},
	
	domainDetailToolbar: {
		title: "Properties:",
		editProperties: "Edit Properties"
	},
	
	domainDetail: {
		displayName: "Name:",
		deploymentPolicy: "Deployment policy:",
		appliance: "Appliance:",
		groups: "Groups:",
		status: "Status:",
		quiesceTimeout: "Quiesce timeout (seconds):",
		sourceConfiguration: "Configuration source:",
		autoSync:"Automatic synchronization:",
		enableAutoSync:"Enable automatic synchronization",
		create: "Create",
		update: "Save",
		cancel: "Cancel",
		label: "Properties:",
		configSourceUploadedFile:"Uploaded file",
		deploymentPolicyFileDomain: "'${object}' in domain '${domain}' from ${source}",
		deploymentPolicyFile: "'${object}' in domain '${domain}' from ${source}",
		deploymentPolicyApplianceDomain: "'${object}' in domain '${domain}' from appliance '${appliance}'",
		uploadedFile: "an uploaded file",
		autoSyncEnabled:"Enabled",
		autoSyncDisabled:"Not enabled",
		autoSyncUnknown:"Unknown",
		applianceDomain:"'${domain}' domain from appliance '${appliance}'"
	},
	
	deleteDomain: {
		description: "If you proceed, the domains and all services in the domains are permanently deleted from the specified appliances.",
		question: "Are you sure you want to delete the following domains?",
		gridTitle:"Domains to be deleted",
		name: "Name",
		appliance: "Appliance",
		status: "Status",
		warning: "Warning",
		defaultOne: "1 of the ${total} domains specified is the default domain.",
		defaultMany: "${count} of the ${total} domains specified are default domains.",
		defaultSingular: "A default domain cannot be deleted. If you proceed, the default domain is not included in the delete action.",
		defaultPlural: "A default domain cannot be deleted. If you proceed, the default domains are not included in the delete action.",
		quiesceOne: "1 of the ${total} domains that you can delete is up (unquiesced).",
		quiesceOneOfOne: "The domain specified is up (unquiesced).",
		quiesceMany: "${count} of the ${total} domains that you can delete are up (unquiesced).",
		quiesceSingular: "If you want to quiesce this domain before deleting it, cancel this action.",
		quiescePlural: "If you want to quiesce these domains before deleting them, cancel this action.",
		deleteButton: "Delete",
		cancel: "Cancel"
	},
	
	domainStatusTooltip: {
		up: "Up: The domain is unquiesced and its administrative state is enabled",
		partial: "Partial: One or more services on this domain are down",
		down: "Down: The domain is quiesced or its administrative state is not enabled",
		unknown: "Unknown: Unable to connect to this domain"
	},		
	
	domainDetailsHoverHelp: {
		quiesceTimeout: "The time, in seconds, that the domain waits for in-flight transactions to complete during a quiesce operation. <br/>The default and minimum quiesce timeout period is 60 seconds.",
		autoSync: "Enable this feature if you want Appliance Management Center to automatically synchronize the domain with any changes detected in the configuration source files or deployment policy files."
	},
	
	createDomainWizard:{
		title:"Create Domain",
		createDomainTitle:"Create Domain",
		configurationSourceTitle:"Configuration Source",
		deploymentPolicyTitle:"Deployment Policy",
		syncModeTitle:"Automatic Syncronization",
		next:"Next",
		previous:"Previous",
		cancel:"Cancel",
		done:"Create",
		standbyText:"Uploading . . .",
		standbyWorking:"Working . . .",
		createDomain:{
			header:"Summary of appliances selected:",
			name:"Specify the name for this new domain:",
			grid:{
				name:"Name",
				applianceType:"Appliance Type"
			},
			explanation:"New domains are created by using existing configuration. You can either use a backup that contains a domain of the same name, or a configuration export that contains objects to use in the new domain.",
			noteHeader:"Note:",
			noteText:"After a domain is created, you cannot change its name."
			
		},	
		configurationSource:{
			header:"Specify the configuration source for this domain:",
			explanation:{
				1: "The configuration source can be: ",
				2:		"A backup that contains a domain of the same name, or a configuration export that contains " +
						"objects to include in this domain, loaded from one of the following locations:",
				3:			"A local file",
				4:			"A remote location accessed through HTTP or HTTPS",
				5:		"An existing domain of the same name on another appliance"
			},
			localSourceRadio:"Local file",
			localSource:"Select file:",
			remoteSourceRadio:"Remote location (URL)",
			remoteSource:"Specify URL:",
			browse:"Browse",
			existingSourceRadio:"An existing domain of the same name",
			selectDomain:"Select domain:",
			nullDomain: " &lt;Select Domain&gt; ",
			existingDomainOption:"Appliance ${applianceName}, Domain ${domainName}",
			hoverHelp:{
				remoteSource: "Only HTTP and HTTPS protocols are supported. Retrieving the configuration source must not require authentication."
			}
		},
		deploymentPolicy:{
			header:"Specify a deployment policy (optional):",
			intro:"In the following table, you can select one or more domains and then specify a deployment policy.",
			gridHeader:"Summary:",
			grid:{
				name:"Appliance",
				policy:"Policy"
			},
			sourceType:{
				label:"Deployment policy option:",
				none:"No policy",
				local:"Policy from a local file",
				remote:"Policy from a remote location",
				existingDomain:"Policy from another domain"
			},
			hoverHelp:{
				localFileUploader:"Browse to the backup or configuration export containing the policy.",
				remoteLocation:"Only HTTP and HTTPS protocols are supported. Retrieving the deployment policy must not require authentication.",
				domainName:"The name of the domain in the backup or configuration export that contains the policy."
			},
			localFileUploader:"Specify the file:",
			remoteLocation:"Specify the URL:",
			domainName:"Policy domain name:",
			existingAppliance:"Specify the appliance:",
			existingDomain:"Specify the domain:",
			policyName:"Policy name:",
			userComment:"User comment:",
			nullAppliance: " &lt;Select Appliance&gt; ",
			nullDomain: " &lt;Select Domain&gt; ",
			apply:"Apply",
			cancel:"Cancel",
			browse:"Browse",
			standbyText:"Uploading . . ."
		},
		syncMode:{
			header:"Future domain synchronization:",
			intro:"You are about to create a new domain.",
			intro2:"Do you want Appliance Management Center to automatically synchronize the domain with any changes detected in the configuration source files or deployment policy files?",
			intro3:"If so, select <strong>Enable automatic synchronization</strong>.",
			enableAutoSync:"Enable automatic synchronization"
		}
	},
	
	domainConfigurationWizard:{
		title:"Update Domain Configuration",
		configurationSourceTitle:"Configuration Source",
		deploymentPolicyTitle:"Deployment Policy",
		syncModeTitle:"Automatic Syncronization",
		next:"Next",
		previous:"Previous",
		cancel:"Cancel",
		done:"Finish",
		standbyText:"Uploading . . .",
		standbyWorking:"Working . . .",
		configurationSource:{
			header:"Specify the configuration source for this domain:",
			explanation:{
				1: "The configuration source can be: ",
				2:		"A backup that contains a domain of the same name, or a configuration export that contains " +
						"objects to include in this domain, loaded from one of the following locations:",
				3:			"A local file",
				4:			"A remote location accessed through HTTP or HTTPS",
				5:		"An existing domain of the same name on another appliance"
			},
			localSourceRadio:"Local file",
			localSource:"Select file:",
			remoteSourceRadio:"Remote location (URL)",
			remoteSource:"Specify URL:",
			browse:"Browse",
			existingSourceRadio:"An existing domain of the same name",
			selectDomain:"Select domain",
			nullDomain: " &lt;Select Domain&gt; ",
			existingDomainOption:"Appliance ${applianceName}, Domain ${domainName}",
			hoverHelp:{
				remoteSource: "Only HTTP and HTTPS protocols are supported. Retrieving the configuration source must not require authentication."
			}
		},
		deploymentPolicy:{
			header:"Specify a deployment policy (optional):",
			intro:"In the following table, you can select one or more domains and then specify a deployment policy.",
			gridHeader:"Summary:",
			grid:{
				name:"Appliance",
				policy:"Policy"
			},
			sourceType:{
				label:"Deployment policy option:",
				none:"No policy",
				local:"Policy from a local file",
				remote:"Policy from a remote location",
				existingDomain:"Policy from another domain"
			},
			hoverHelp:{
				localFileUploader:"Browse to the backup or configuration export containing the policy.",
				remoteLocation:"Only HTTP and HTTPS protocols are supported. Retrieving the deployment policy must not require authentication.",
				domainName:"The name of the domain in the backup or configuration export that contains the policy."
			},
			localFileUploader:"Specify the file:",
			remoteLocation:"Specify the URL:",
			domainName:"Policy domain name:",
			existingAppliance:"Specify the appliance:",
			existingDomain:"Specify the domain:",
			policyName:"Policy name:",
			userComment:"User comment:",
			nullAppliance: " &lt;Select Appliance&gt; ",
			nullDomain: " &lt;Select Domain&gt; ",
			apply:"Apply",
			cancel:"Cancel",
			browse:"Browse",
			standbyText:"Uploading . . ."
		},
		syncMode:{
			header:"Future domain synchronization:",
			intro:"You are about to deploy new configuration to this domain.",
			introPlural:"You are about to deploy new configuration to these domans.",
			intro2:"Do you want Appliance Management Center to automatically synchronize the domain with any changes detected in the configuration source files or deployment policy files?",
			intro3:"If so, select <strong>Enable automatic synchronization</strong>.",
			enableAutoSync:"Enable automatic synchronization"
		}
	},
	
	uploadFileWizard:{
		title: "Upload File",
		sourceTitle:"Upload File Source",
		destinationTitle:"Upload File Destination",
		next:"Next",
		previous:"Previous",
		cancel:"Cancel",
		done:"Upload",
		standbyText:"Uploading . . .",
		source:{
			header:"Specify the source of the file that you want to upload:",
			localRadio:"Local file",
			remoteRadio:"Remote location (URL)",
			localSource:"Select file:",
			remoteSource:"Specify URL:",
			browse:"Browse",
			hoverHelp:{
				remoteSource:"Only HTTP and HTTPS protocols are supported. Retrieving the file must not require authentication."
			}
		},
		destination:{
			header:"Specify the target folder:",
			intro:"Specify where to save the file on the appliance or appliances.<br /><strong>Note:</strong> The folder must already exist.",
			header2:"Specify the name for this file:",
			folder:"Appliance folder path:",
			fileName:"Save file as:",
			hoverHelp:{
				folder:"For example, cert:/ or local:/"
			}
				
		}
	},
	
	// ------------------------------------------------------------------------
	// Service
	// ------------------------------------------------------------------------
	serviceGrid: {
		title: "Service Grid",
		name: "Name",
		type: "Type",
		domain: "Domain",
		appliance: "Appliance",
		status: "Status"
	},
	
	serviceGridToolbar: {
		title: "Service Toolbar",
		viewDomain: "View Domain",
		deleteService: "Delete Service",
		deployConfiguration: "Update Service Configuration",
		quiesce: "Quiesce Service",
		unquiesce: "Unquiesce Service",
		moreActions: "Other Actions"
	},
	
	serviceDetailToolbar: {
		title: "Properties:"
	},
	
	serviceDetail: {
		name: "Name:",
		type: "Type:",
		domain: "Domain:",
		appliance: "Appliance:",
		status: "Status:",
		label: "Properties:"
	},
	
	deleteService: {
		title: "Delete Service \"${name}\"",
		description: "The following table shows any child objects that are used by this service only. Select any child objects that you want to delete when this service is deleted.",
		gridTitle: "Child objects to delete",
		objectType: "Object Type",
		objectName: "Object Name",
		warning: "Warning",
		warningNoReferences: "No references can be found to the listed objects, however these objects might be referenced from within a custom style sheet. If you are sure that these objects are not referenced in a style sheet, you can delete them.",
		deleteButton: "Delete",
		cancel: "Cancel"
	},

	createServiceWizard:{
		title:"Create Service",
		configurationSourceTitle:"Configuration Source",
		selectServiceTitle:"Select Service",
		deploymentPolicyTitle:"Deployment Policy",
		serviceImpactTitle:"Service Impact",
		next:"Next",
		previous:"Previous",
		cancel:"Cancel",
		done:"Create",
		standbyText:"Uploading . . .",
		configurationSource:{
			header:"Summary of domains selected:",
			grid:{
				name:"Name",
				applianceName:"Appliance Name"
			},
			configSourceLegend:"Specify the configuration source for this service",
			localSourceRadio:"Local file",
			localSource:"Select file:",
			remoteSourceRadio:"Remote location (URL)",
			remoteSource:"Specify URL:",
			browse:"Browse",
			hoverHelp:{
				remoteSource:"Only HTTP and HTTPS protocols are supported. Retrieving the configuration source must not require authentication."
			}
		},
		selectService:{
			header:"Summary of domains selected:",
			serviceSelect:"Select the service name and type:",
			grid:{
				name:"Name",
				applianceName:"Appliance Name"
			}
		},
		deploymentPolicy:{
			header:"Specify a deployment policy (optional):",
			intro:"In the following table, you can select one or more service and then specify a deployment policy.",
			grid:{
				title:"Summary",
				name:"Domain",
				applianceName:"Appliance",
				policy:"Policy"
			},
			sourceType:{
				label:"Deployment policy option:",
				none:"No policy",
				local:"Policy from a local file",
				remote:"Policy from a remote location"
			},
			localFileUploader:"Specify the file:",
			remoteLocation:"Specify the URL:",
			domainName:"Policy domain name:",
			policyName:"Policy name:",
			userComment:"User comment:",
			apply:"Apply",
			cancel:"Cancel",
			browse:"Browse",
			standbyText:"Uploading . . .",
			hoverHelp:{
				localFileUploader:"Browse to the backup or configuration export containing the policy.",
				remoteLocation:"Only HTTP and HTTPS protocols are supported. Retrieving the deployment policy must not require authentication.",
				domainName:"The name of the domain in the backup or configuration export that contains the policy."
			}
		},
		serviceImpact:{
			header:"Services affected by this deployment:",
			intro:"The following services have dependencies on resources that will be updated by this configuration deployment.",
			warning:"If you proceed, the services are quiesced while the new configuration is deployed to service \"${name}\"",
			noImpact:"No other services impacted.",
			file:"File",
			serviceTooltip:"Service \"${serviceName}\" (${serviceType}) running on:",
			serviceTooltipListItem:"appliance \"${appliance}\", domain \"${domain}\""
		},
	},
	
	serviceConfigurationWizard:{
		title:"Update Service Configuration",
		configurationSourceTitle:"Configuration Source",
		deploymentPolicyTitle:"Deployment Policy",
		serviceImpactTitle:"Service Impact",
		next:"Next",
		previous:"Previous",
		cancel:"Cancel",
		done:"Finish",
		standbyText:"Uploading . . .",
		configurationSource:{
			configSourceLegend:"Specify the configuration source for this service",
			localSourceRadio:"Local file",
			localSource:"Select file:",
			remoteSourceRadio:"Remote location (URL)",
			remoteSource:"Specify URL:",
			browse:"Browse",
			hoverHelp:{
				remoteSource:"Only HTTP and HTTPS protocols are supported. Retrieving the configuration source must not require authentication."
			}
		},
		deploymentPolicy:{
			header:"Specify a deployment policy (optional):",
			intro:"In the following table, you can select one or more service and then specify a deployment policy.",
			gridHeader:"Summary",
			grid:{
				name:"Domain",
				applianceName:"Appliance",
				policy:"Policy",
				noPolicy: "None",
				updatedPolicy: "Updated from local file"
			},
			sourceType:{
				label:"Deployment policy option:",
				none:"No policy",
				local:"Policy from a local file",
				remote:"Policy from a remote location"
			},
			localFileUploader:"Specify the file:",
			remoteLocation:"Specify the URL:",
			domainName:"Policy domain name:",
			policyName:"Policy name:",
			userComment:"User comment:",
			apply:"Apply",
			cancel:"Cancel",
			browse:"Browse",
			standbyText:"Uploading . . .",
			hoverHelp:{
				localFileUploader:"Browse to the backup or configuration export containing the policy.",
				remoteLocation:"Only HTTP and HTTPS protocols are supported. Retrieving the deployment policy must not require authentication.",
				domainName:"The name of the domain in the backup or configuration export that contains the policy."
			}
		},
		serviceImpact:{
			header:"Services affected by this deployment:",
			intro:"The following services have dependencies on resources that will be updated by this configuration deployment.",
			warning:"If you proceed, the services are quiesced while the new configuration is deployed to service \"${name}\"",
			noImpact:"No other services impacted.",
			file:"File",
			serviceTooltip:"Service \"${serviceName}\" (${serviceType}) running on:",
			serviceTooltipListItem:"appliance \"${appliance}\", domain \"${domain}\""
		},
	},
	
	deploymentPolicy:{
		grid:{
			name:"Domain",
			applianceName:"Appliance",
			policy:"Policy",
			noPolicy: "None",
			updatedPolicyFile: "Updated from local file",
			updatedPolicyDomain: "From domain \"${domain}\""
		},
	},
	
	serviceStatusTooltip: {
		up: "Up: This service is up",
		down: "Down: This service is down.",
		unknown: "Unknown: Unable to determine this service's status."
	},
	
	
	// ------------------------------------------------------------------------
	// Repository
	// ------------------------------------------------------------------------
	repository: {
		title: "Repository"
	},
	
	repositoryGridToolbar: {
		title: "Repository Toolbar",
		addArtefact: "Add Firmware",
		removeArtefact: "Remove Firmware"
	},
	
	repositoryGrid: {
		title: "Repository Grid",
		applianceType: "Appliance Type",
		modelType: "Model",
		firmwareVersion: "Firmware Version",
		userComment: "User Comment"
	},
	
	firmwareDetail: {
		applianceType: "Appliance type:",
		modelType: "Model:",
		firmwareVersion: "Firmware version:",
		userComment: "User comment:",
		firmwareFeatures: "Firmware<br/>features:",
		manufactureDate: "Manufacture date:",
		uploadDate: "Upload date:",
		label: "Properties:",
		edit: "Edit",
		title: "Upload To Repository",
		update: "Save",
		cancel: "Cancel",
		noFeatures:"None"
	},
	
	repositoryDetailToolbar: {
		title: "Properties:",
		editProperties: "Edit Properties"
	},

	repositoryDetailsHoverHelp: {
		features: "The list of features contained in the firmware image. <br/>Note that not all of the features listed will necessarily be installed on to the appliance if you use this firmware image. The set of features installed on the appliance is determined by the licenses contained on that appliance."
	},
	
	
	// ------------------------------------------------------------------------
	// Firmware
	// ------------------------------------------------------------------------

	addFirmwareForm: {
		specifySource: "Specify the source:",
		addComment: "Add a comment to this firmware image (optional):",
		remoteSourceRadio: "Remote location (URL)",
		remoteSource: "Specify URL:",
		localSourceRadio: "Local file",
		localSource: "Select file:",
		userComments: "User comment:",
		browse: "Browse",
		submit: "Add",
		cancel: "Cancel",
		standbyText:"Uploading . . ."
	},
	
	
	// ------------------------------------------------------------------------
	// History
	// ------------------------------------------------------------------------
	history: {
		title: "History"
	},
	
	actionGrid: {
		title: "Action History Grid",
		state: "Status",
		description: "Action",
		userId: "User ID",
		submitted: "Submitted",
		updated: "Updated"
		
	},
	
	actionState: {
		success: "Success",
		inProgress: "In progress",
		failure: "Failure"
	},
	
	actionDetail: {
		label: "Details:"
	},
	
	
	// ------------------------------------------------------------------------
	// Permissions
	// ------------------------------------------------------------------------
	
	permissions:{
		userRoleNameLabel: "User role name:",
		save: "Save",
		cancel: "Cancel",
		editMode: " ",
		appliance:{
			title: "Appliance Permissions",
			list: "Further appliance permissions",
			view: "View appliances",
			add: "Add appliance",
			remove: "Remove appliance",
			groupAssign: "Assign appliance groups",
			editProperties: "Edit properties",
			backup: "Backup appliance",
			restore: "Restore appliance",
			deployFirmware: "Deploy firmware",
			createDomain: "Create domain",
			quiesce: "Quiesce appliance",
			unquiesce: "Unquiesce appliance",
			reboot: "Reboot appliance"
		},
		domain:{
			title: "Domain and Service Permissions",
			list: "Further domain and service permissions",
			view: "View domains or services",
			editProperties: "Edit properties",
			groupAssign: "Assign domain groups",
			updateConfiguration: "Update domain configuration and create, update, and delete service configuration",
			uploadFile: "Upload file",
			remove: "Delete domain",
			quiesce: "Quiesce domain or service",
			unquiesce: "Unquiesce domain or service",
			restart: "Restart domain"
		},
		repository:{
			title: "Repository Permissions",
			list: "Further repository permissions",
			view: "View repository",
			add: "Add firmware",
			remove: "Remove firmware",
			editProperties: "Edit properties"
		},
		history:{
			title: "History Permissions",
			note: "Note: All users can access the History page and can view " +
				"entries for actions they initiated.",
			view: "View all actions (performed by any user)"
		},
		security:{
			title: "Security Administration Permissions",
			user:{
				list: "Further user permissions"
			},
			role:{
				list: "Further user role permissions"
			},
			viewUser: "View users and user groups",
			editUser: "Edit user role and user group role mappings",
			viewRole: "View user roles and permissions",
			editRole: "Create, edit, and delete user roles and permissions"
		}
	},
	
	// ------------------------------------------------------------------------
	// Groups
	// ------------------------------------------------------------------------
	assignGroupsForm: {
		existingGroups:{
			title:"Existing groups",
			intro:"Use the check boxes to add members to or remove members from a group.",
			noGroups:"No groups exist yet.",
			groupLabel:"${name} (${count})"
		},
		newGroup:{
			title:"Create a new group",
			name:"Group name",
			hoverHelp: "The group name must be unique and consist of only alphanumeric, hyphen (-), period (.), or underscore (_) characters, and must begin with an alphanumeric character.",
			add:"Add"
		},
		tooltip:"<strong>Group name:</strong> ${name}<br/><strong>Members (appliances):</strong> ${appliances}<br/><strong>Members (domains):</strong> ${domains}",
		none: "(None)",
		submit:"Apply",
		cancel:"Cancel"
	},
	
	groupsHoverHelp: {
		header:"${resource} is assigned to the following groups:"
	},
	
	// ------------------------------------------------------------------------
	// Forms
	// ------------------------------------------------------------------------
	_dataDisplayForm: {
		create: "Create",
		update: "Update"
	},
	
	userMenu: {
		label:"User Menu",
		logOut: "Log Out"
	},
	
	helpMenu: {
		label:"Help Menu",
		help: "Help",
		about: "About"
	},
	
	
	// ------------------------------------------------------------------------
	// Message Level Descriptions
	// ------------------------------------------------------------------------
	level: {
		Information: "Information",
		Warning: "Warning",
		Error: "Error",
		Confirmation: "Confirmation",
		Success: "Success"
	},

	action: {
		RemoveAppliance: "Remove",
		RemoveAppliances: "Remove Appliances",
		QuiesceAppliance: "Quiesce",
		QuiesceAppliances: "Quiesce Appliances",
		UnquiesceAppliance: "Unquiesce",
		UnquiesceAppliances: "Unquiesce Appliances",
		RebootAppliance: "Reboot",
		QuiesceDomain: "Quiesce",
		QuiesceDomains: "Quiesce Domains",
		UnquiesceDomain: "Unquiesce",
		UnquiesceDomains: "Unquiesce Domains",
		RestartDomain: "Restart",
		RemoveFirmware: "Remove",
		RemoveFirmwares: "Remove",
		DeleteService: "Delete",
		QuiesceService: "Quiesce",
		QuiesceServices: "Quiesce Services",
		UnquiesceService: "Unquiesce",
		UnquiesceServices: "Unquiesce Services",
		DeleteUserRole: "Delete",
		Ok: "Ok",
		Close: "Close",
		Cancel: "Cancel"
	},
	
	
	// ------------------------------------------------------------------------
	// General Messages
	// ------------------------------------------------------------------------
	CONFIRM_REMOVE_APPLIANCE: "Are you sure you want to remove the appliance ${name}?",
	CONFIRM_REMOVE_APPLIANCE_summary: "Removing an appliance results in all data relating to the appliance being removed from Appliance Management Center.",

	CONFIRM_REMOVE_APPLIANCES: "Are you sure you want to remove these ${quantity} appliances?",
	CONFIRM_REMOVE_APPLIANCES_summary: "Removing an appliance results in all data relating to the appliance being removed from Appliance Management Center.",

	CONFIRM_QUIESCE_APPLIANCE: "Are you sure you want to quiesce the appliance ${name}?",
	CONFIRM_QUIESCE_APPLIANCE_summary: "Quiesce an appliance to modify its firmware or to troubleshoot without affecting data traffic. When you quiesce an appliance, all the domains on the appliance are quiesced after the timeout period set for the appliance. <br/><b>Note:</b> You can quiesce individual domains from the Domains page.",
	
	CONFIRM_QUIESCE_APPLIANCES: "Are you sure you want to quiesce these ${quantity} appliances?",
	CONFIRM_QUIESCE_APPLIANCES_summary: "Quiesce an appliance to modify its firmware or to troubleshoot without affecting data traffic. When you quiesce an appliance, all the domains on the appliance are quiesced after the timeout period set for the appliance. <br/><b>Note:</b> You can quiesce individual domains from the Domains page.",
	
	CONFIRM_UNQUIESCE_APPLIANCE: "Are you sure you want to unquiesce the appliance ${name}?",
	CONFIRM_UNQUIESCE_APPLIANCE_summary: "When you unquiesce an appliance, all the domains on the appliance are unquiesced. <br/><b>Note:</b> You can unquiesce individual domains from the Domains page.",
	
	CONFIRM_UNQUIESCE_APPLIANCES: "Are you sure you want to unquiesce these ${quantity} appliances?",
	CONFIRM_UNQUIESCE_APPLIANCES_summary: "When you unquiesce an appliance, all the domains on the appliance are unquiesced. <br/><b>Note:</b> You can unquiesce individual domains from the Domains page.",
	
	CONFIRM_REBOOT_APPLIANCE: "Are you sure you want to reboot the appliance ${name}?",
	CONFIRM_REBOOT_APPLIANCE_summary: "When you reboot an appliance, all the domains on the appliance are restarted. Data traffic will not be processed while the appliance is rebooting.",
	
	CONFIRM_REBOOT_APPLIANCES: "Are you sure you want to reboot these ${quantity} appliances?",
	CONFIRM_REBOOT_APPLIANCES_summary: "When you reboot an appliance, all the domains on the appliance are restarted. Data traffic will not be processed while the appliance is rebooting.",

	
	CONFIRM_QUIESCE_DOMAIN: "Are you sure you want to quiesce the domain ${name}?",
	CONFIRM_QUIESCE_DOMAIN_summary: "",
	
	CONFIRM_QUIESCE_DOMAINS: "Are you sure you want to quiesce these ${quantity} domains?",
	CONFIRM_QUIESCE_DOMAINS_summary: "",
	
	CONFIRM_UNQUIESCE_DOMAIN: "Are you sure you want to unquiesce the domain ${name}?",
	CONFIRM_UNQUIESCE_DOMAIN_summary: "",
	
	CONFIRM_UNQUIESCE_DOMAINS: "Are you sure you want to unquiesce these ${quantity} domains?",
	CONFIRM_UNQUIESCE_DOMAINS_summary: "",

	CONFIRM_RESTART_DOMAIN: "Are you sure you want to restart the domain ${name}?",
	CONFIRM_RESTART_DOMAIN_summary: "Data traffic to this domain will not be processed while the domain is restarting.",
	
	CONFIRM_RESTART_DOMAINS: "Are you sure you want to restart these ${quantity} domains?",
	CONFIRM_RESTART_DOMAINS_summary: "Data traffic to these domain will not be processed while the domains are restarting.",

	
	CONFIRM_REMOVE_FIRMWARE: "Are you sure you want to remove this firmware image from the repository?",
	CONFIRM_REMOVE_FIRMWARE_summary: "",
	
	CONFIRM_REMOVE_FIRMWARES: "Are you sure you want to remove these ${quantity} firmware images from the repository?",
	CONFIRM_REMOVE_FIRMWARES_summary: "",
	
	CONFIRM_QUIESCE_SERVICE: "Are you sure you want to quiesce the service ${name} of type ${classDisplayName}?",
	CONFIRM_QUIESCE_SERVICE_summary: "",
	
	CONFIRM_QUIESCE_SERVICES: "Are you sure you want to quiesce these ${quantity} services?",
	CONFIRM_QUIESCE_SERVICES_summary: "",
	
	CONFIRM_UNQUIESCE_SERVICE: "Are you sure you want to unquiesce the service ${name} of type ${classDisplayName}?",
	CONFIRM_UNQUIESCE_SERVICE_summary: "",
	
	CONFIRM_UNQUIESCE_SERVICES: "Are you sure you want to unquiesce these ${quantity} services?",
	CONFIRM_UNQUIESCE_SERVICES_summary: "",
	
	CONFIRM_DELETE_SERVICE_NO_CHILDREN: "Are you sure you want to delete the service \"${name}\"?",
	CONFIRM_DELETE_SERVICE_NO_CHILDREN_summary: "<strong>Service:</strong> ${name}<br/><strong>Type:</strong> ${classDisplayName}<br/><strong>Domain:</strong> ${domainName}<br/><strong>Appliance:</strong> ${applianceName}.",
	
	CONFIRM_DELETE_ROLE: "Are you sure you want to remove the user role '${name}'?",
	CONFIRM_DELETE_ROLE_summary: "There are no users or groups associated with this user role.",
	
	CONFIRM_DELETE_ROLE_MEMBERS: "Are you sure you want to remove the user role '${name}'?",
	CONFIRM_DELETE_ROLE_MEMBERS_summary: "${count} users or user groups are still associated with this user role. If you proceed, these users or user groups will lose the access defined by this user role.<br/>To find out which users or user groups are associated with this user role, go to the Users page.",
	
	SUCCESS_ADD_APPLIANCE: "Appliance ${name} successfully added",
	SUCCESS_UPDATE_APPLIANCE: "Appliance ${name} successfully updated",
	SUCCESS_REMOVE_APPLIANCE: "Appliance ${name} successfully removed",
	SUCCESS_REMOVE_APPLIANCES: "Successfully removed ${quantity} appliances",
	SUCCESS_DELETE_DOMAIN: "Domain ${name} on appliance ${appliance} successfully deleted",
	SUCCESS_DELETE_SERVICE: "Service ${name} on appliance ${applianceName} successfully deleted",
	SUCCESS_UPDATE_DOMAIN: "Domain ${name} on appliance ${appliance} successfully updated",
	SUCCESS_ASSIGN_GROUP: "Successfully updated group assignment for ${type} '${name}'",
	SUCCESS_ASSIGN_GROUPS: "Successfully updated group assignment for ${count} ${type}",
	SUCCESS_UPDATE_FIRMWARE: "Successfully updated firmware",
	SUCCESS_UPDATE_FIRMWARE_COMMENT: "Successfully updated firmware '${comment}'",
	SUCCESS_REMOVE_FIRMWARE: "Successfully removed firmware",
	SUCCESS_REMOVE_FIRMWARE_COMMENT: "Successfully removed firmware '${comment}'",
	SUCCESS_UPDATE_USER: "Successfully updated user '${name}'",
	SUCCESS_UPDATE_USER_GROUP: "Successfully updated group '${name}'",
	SUCCESS_CREATE_ROLE: "User role '${name}' successfully created",
	SUCCESS_UPDATE_ROLE: "User role '${name}' successfully updated",
	SUCCESS_DELETE_ROLE: "User role '${name}' successfully deleted",
	
	ERROR_ADD_APPLIANCE: "Unable to add appliance",
	ERROR_UPDATE_APPLIANCE: "Unable to update appliance",
	ERROR_REMOVE_APPLIANCE: "Unable to remove appliance",
	ERROR_NO_APPLIANCE_SELECTED: "No appliance selected",
	ERROR_QUIESCE_APPLIANCE: "Unable to quiesce appliance",
	ERROR_BACKUP_APPLIANCE: "Unable to backup appliance",
	ERROR_RESTORE_APPLIANCE: "Unable to restore appliance",
	ERROR_NO_DOMAIN_SELECTED: "No domain selected",
	ERROR_NO_SERVICE_SELECTED: "No service selected",
	ERROR_MULTIPLE_SERVICES_SELECTED: "This action can only be performed on a single service.",
	ERROR_NO_MATCHING_SERVICES: "The configuration source contains no configuration for service ${name}.",
	ERROR_NO_CONTAINED_SERVICES: "The configuation source does not contain any services applicable to the selected appliances.",
	ERROR_SERVICE_IMPACT: "Unable to retrieve impact of deploying service configuration",
	ERROR_UNQUIESCE_APPLIANCE: "Unable to unquiesce appliance",
	ERROR_QUIESCE_DOMAIN: "Unable to quiesce domain",
	ERROR_UNQUIESCE_DOMAIN: "Unable to unquiesce domain",
	ERROR_ADD_FIRMWARE: "Unable to add firmware",
	ERROR_REMOVE_FIRMWARE: "Unable to remove firmware",
	ERROR_QUIESCE_SERVICE: "Unable to quiesce service",
	ERROR_UNQUIESCE_SERVICE: "Unable to unquiesce service",
	ERROR_NO_RESPONSE: "No response from server",
	ERROR_NO_RESPONSE_summary: "Communication with the server failed. You might have become disconnected from the network.",
	ERROR_GROUP_EXISTS: "A group with the name \"${group}\" already exists",
	ERROR_NO_ROLE_SELECTED: "No role selected",
	
	ERROR_RETRIEVE_DATA: "Unable to retrieve data for display",
	
	ERROR_NO_ARTEFACT_SELECTED: "No artefact selected",
	CONFIRM_REMOVE_ARTEFACT: "Are you sure you want to remove the artefact ${name}?",
	
	ERROR_UPLOADING_FILE: "Error uploading file.",
	
	// ------------------------------------------------------------------------
	// Server Error Messages
	// ------------------------------------------------------------------------
	CWZGUE0001E_GENERAL_WAMT_ERROR: "CWZGUE0001E: The subsystem used for communicating with appliances reported an error.",
	CWZGUE0002E_NO_SUCH_RESOURCE: "CWZGUE0002E: The resource with ID {0} was not found."
})
});
