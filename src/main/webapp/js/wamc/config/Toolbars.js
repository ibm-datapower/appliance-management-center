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
	appliances:{
		applianceGridToolbar:{
			items:[
				{name:"addAppliance",iconClass:"wamcIconAdd",permission:"appliance-add"},
				{name:"removeAppliance",iconClass:"wamcIconRemove",permission:"appliance-remove"},
				{name:"assignGroups",iconClass:"wamcIconAssignGroups",permission:"appliance-assign-groups"},
				{name:"viewDomains",iconClass:"wamcIconShowChild",permission:"domain-view"},
				{name:"moreActions",
				items:[
					{name:"quiesce",permission:"appliance-quiesce"},
					{name:"unquiesce",permission:"appliance-unquiesce"},
					{name:"backup",permission:"appliance-backup"},
					{name:"restore",permission:"appliance-restore"},
					{name:"deployFirmware",permission:"appliance-deploy-firmware"},
					{name:"createDomain",permission:"appliance-create-domain"},
					{name:"reboot",permission:"appliance-reboot"}
				]}
			]
		},
		applianceDetailToolbar:{
			header:true,
			items:[
			    {name:"editProperties",iconClass:"wamcIconEdit",permission:"appliance-edit-properties"}
			]
		}
	},
	repository:{
		repositoryGridToolbar:{
			items:[
			    {name:"addArtefact",iconClass:"wamcIconAdd",permission:"firmware-add"},
			    {name:"removeArtefact",iconClass:"wamcIconRemove",permission:"firmware-remove"}
			]
		},
		repositoryDetailToolbar:{
			header:true,
			items:[
			    {name:"editProperties",iconClass:"wamcIconEdit",permission:"firmware-edit-properties"}
			]
		}
	},
	domains:{
		domainGridToolbar:{
			items:[
				{name:"assignGroups",iconClass:"wamcIconAssignGroups",permission:"domain-assign-groups"},
				{name:"viewAppliances",iconClass:"wamcIconShowParent",permission:"appliance-view"},
				{name:"viewServices",iconClass:"wamcIconShowChild",permission:"domain-view"},
				{name:"moreActions",
				items:[
					{name:"quiesce",permission:"domain-quiesce"},
					{name:"unquiesce",permission:"domain-unquiesce"},
					{name:"deleteDomain",permission:"domain-delete"},
					{name:"createService",permission:"domain-update-configuration"},
					{name:"deployConfiguration",permission:"domain-update-configuration"},
					{name:"uploadFile",permission:"domain-upload-file"},
					{name:"restart",permission:"domain-restart"}
				]}
			]
		},
		domainDetailToolbar:{
			header:true,
			items:[
			    {name:"editProperties",iconClass:"wamcIconEdit",permission:"domain-edit-properties"}
			]
		}
	},
	services:{
		serviceGridToolbar:{
			items:[
				{name:"viewDomain",iconClass:"wamcIconShowParent",permission:"domain-view"},
				{name:"moreActions",
				items:[
					{name:"quiesce",permission:"domain-quiesce"},
					{name:"unquiesce",permission:"domain-unquiesce"},
					{name:"deleteService",permission:"domain-update-configuration"},
					{name:"deployConfiguration",permission:"domain-update-configuration"}
				]}
			]
		},
		serviceDetailToolbar:{
			header:true,
			items:[]
		}
	}
});
