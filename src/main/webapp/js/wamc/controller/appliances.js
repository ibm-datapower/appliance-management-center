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
define(["dojo/_base/array",
        "dojo/_base/declare",
        "dojo/_base/json",
        "dojo/_base/lang",
        "dojo/aspect",
        "dojo/promise/all",
        "dojo/string",
        "dojo/topic",
        "dojo/when",
        "dojo/request/xhr",
        "dijit/Dialog",
        "dijit/layout/BorderContainer",
        "dijit/layout/ContentPane",
        "dijit/registry",
        "gridx/Grid",
        "gridx/core/model/cache/Async",
        "dijit/Tooltip",
        "wamc/dialogs",
        "wamc/messages",
        "wamc/stores",
        "wamc/standby",
        "wamc/config/Resources",
        "wamc/controller/_ContentController",
        "wamc/controller/master-detail",
        "wamc/grid/format",
        "wamc/grid/modules",
        "wamc/grid/util",
        "wamc/widget/form/AddApplianceForm",
        "wamc/widget/form/ApplianceDetail",
        "wamc/widget/form/AssignGroupsForm",
        "wamc/widget/form/DeployFirmwareForm",
        "wamc/widget/form/EditApplianceDetail",
        "wamc/widget/ApplianceToolbar",
        "wamc/widget/PropertiesToolbar",
        "wamc/widget/wizard/BackupApplianceWizard",
        "wamc/widget/wizard/CreateDomainWizard",
        "wamc/widget/wizard/RestoreApplianceWizard",
        "dojo/text!wamc/controller/templates/_GroupsHoverHelp.html",
        "dojo/i18n!wamc/nls/strings"],
		function(array,declare,json,lang,aspect,all,string,topic,when,xhr,Dialog,BorderContainer,
				ContentPane,registry,Grid,Async,Tooltip,dialogs,
				messages,stores,standby,Resources,_ContentController,masterDetail,gridFormat,
				gridModules,gridUtil,
				AddApplianceForm,ApplianceDetail,AssignGroupsForm,DeployFirmwareForm,
				EditApplianceDetail,ApplianceToolbar,PropertiesToolbar,
				BackupApplianceWizard,CreateDomainWizard,RestoreApplianceWizard,
				groupsHoverHelpTemplate,nls){
	
	var addAppliance = function(/*Object*/appliance){
		// summary:
		//		Add an appliance to the store
		// appliance: Object
		//		The appliance to add
		// return: Deferred
		//		A deferred that resolves when the appliance is added 
		var F = "wamc.controller.appliances.addAppliance()";
		console.debug(F,appliance);
		
		var grid = registry.byId("applianceGrid"),
			store = stores.get("appliance");
			def = store.add(appliance);
		
		def.then(function(result){
				messages.showMessage("SUCCESS_ADD_APPLIANCE",{"name":result.name});
				when(grid.model.when(),function(){
					// Clear the grid silently
					grid.select.row.clear(true);
					// Now try and select the new row
					grid.select.row.selectById(result.id);
					// hack to avoid duplicate items in grid
					grid.model.clearCache();
					grid.body.refresh();
				});
			});
		
		return def;
	},
	
	editAppliance = function(/*Object*/appliance){
		// summary:
		//		Update an appliance in the store
		// appliance: Object
		//		The appliance to update
		// return: Deferred
		//		A deferred that resolves when the appliance has been updated
		var F="wamc.controller.appliances.editAppliance()";
		console.debug(F);
		
		var grid = registry.byId("applianceGrid"),
			store = stores.get("appliance"),
			def = store.put(appliance,{overwrite:true});
		
		def.then(function(result){
			messages.showMessage("SUCCESS_UPDATE_APPLIANCE",{"name":result.name});
			
			when(grid.model.when(),function(){
				// Clear the grid silently
				grid.select.row.clear(true);
				// Now try and select the new row
				grid.select.row.selectById(result.id);
			});
		});
		
		return def;
	},
	
	reboot = function(){
		// summary:
		//		Reboot selected appliances
		var F = "wamc.controller.appliances.reboot()";
		console.debug(F);
		
		var grid = registry.byId("applianceGrid");
		
		gridUtil.withSelectedData(grid,function(selection){
			if(selection.length){
				array.forEach(selection,function(appliance){
					rebootAppliance(appliance);
				});
			}	
		});
	},
	
	rebootAppliance = function(/*Appliance*/appliance){
		// summary:
		//		Reboot a managed appliance
		// appliance:
		//		The appliance to reboot
		
		var F = "wamc.controller.appliances.rebootAppliance()";
		console.debug(F,appliance);

		var pName = appliance.name,
			url = string.substitute(Resources.rebootAppliance.url,{applianceId:appliance.id}),
			xhrArgs = {
				method:Resources.rebootAppliance.method,
				handleAs:"json"
		};
		
		xhr(url,xhrArgs).then(function(data){
			console.debug(F,"Reboot appliance " + pName + " started");
		},
		function(err){
			console.log(F,"Error while rebooting appliance " + pName);
			controller.showServerResponse(err.response.text);
		});
	},
	
	createDomains = function(/*Object*/domainConfig){
		var F = "wamc.controller.appliances.createDomains()";
		console.debug(F,domainConfig);
		
		if(!domainConfig) return;
		
		array.forEach(domainConfig.appliances,function(appliance){
			
			// Create the postData
			var pd = {
					sourceConfigLocation:domainConfig.uri
				},
				url = string.substitute(Resources.domainConfig.url,{applianceId:appliance.id,name:domainConfig.name}),
				xhrArgs;
			
			if(appliance.deploymentPolicy){
				if(appliance.deploymentPolicy.sourceType !=="none"){
					pd.deploymentPolicyLocation = appliance.deploymentPolicy.uri;
					pd.domainName = appliance.deploymentPolicy.domainName;
					pd.policyName = appliance.deploymentPolicy.policyName;
				}
			}
			
			pd.automaticSynchronization = domainConfig.automaticSynchronization;
			
			xhrArgs = {
					handleAs:"json",
					data:json.toJson(pd),
					headers:{
						"Content-Type": "application/json",
						Accept: "application/javascript, application/json"
					}
			};
			
			xhr.put(url,xhrArgs).then(function(data){
				console.debug(F,"Create " + domainConfig.name + " started");
			},
			function(err){
				console.log(F,"Error while creatin domain appliance " + domainConfig.name);
				dialogs.showServerResponse(err.response.text);
			});
		},this);	
	},
	
	updateApplianceGroups = function(/*Appliance*/appliance){
		// summary:
		//		Update group membership for an appliance
		// appliance:Object
		//		The appliance to update
		var F = "wamc.controller.appliances.updateApplianceGroups()";
		console.debug(F,appliance);
		
		var url = string.substitute(Resources.applianceGroups.url,{applianceId:appliance.id}),
			xhrArgs = {
				data:json.toJson(appliance.groups),
				handleAs:"json",
				headers: {
					"Content-Type":"application/json",
					Accept: "application/javascript, application/json"
				}
			};
		
		return xhr.put(url,xhrArgs).then(function(response){
			stores.notifyUpdate("appliance",appliance.id);
		});
	},
	
	updateGroups = function(/*Array/Object*/appliance){
		// summary:
		//		Update group membership for an appliance
		// appliance:Array/Object
		//		The appliance or appliances to update
		
		if(lang.isArray(appliance)){
			var dl = array.map(appliance,updateApplianceGroups);
			return all(dl);
		}else{
			return updateApplianceGroups(appliance);
		}
	},
	
	listGroups = function(){
		// summary:
		//		Get a list of the available groups
		// return: Deferred
		//		A deferred that resolves to an array of strings representing the available groups
		console.debug("wamc.controller.appliances.listGroups()",Resources.groups.url);
		return xhr.get(Resources.groups.url,{
			handleAs: "json",
			headers: {
				"Content-Type":"application/json",
				Accept: "application/javascript, application/json"
			}
		});
	},
	
	
	removeSelectedAppliances = function(){
		// summary:
		//		Remove appliances currently selected in the appliance table from WAMC 
		var grid = registry.byId("applianceGrid");
		
		gridUtil.withSelectedData(grid,function(selection){
			var dl = [];
			
			if(selection.length){
				array.forEach(selection,function(appliance){
					dl.push(removeAppliance(appliance));
				});
				
				all(dl).then(function(result){
					grid.select.row.clear();
				},function(error){
					grid.select.row.clear();
				});
			}else{
				dialogs.showDialog("ERROR_NO_APPLIANCE_SELECTED");
			}	
		});
	},
	
	removeAppliance = function(/*Appliance*/appliance){
		// summary:
		//		Remove an appliance from WAMC
		// appliance:
		//		The appliance to remove
		
		var F = "wamc.controller.appliances.removeAppliance()";
		console.debug(F,appliance);
		
		var def = stores.get("appliance").remove(appliance.id);
		
		when(def,function(result){
			console.debug(F,"Appliance " + appliance.name + " removed");
			messages.showMessage("SUCCESS_REMOVE_APPLIANCE",{"name":appliance.name});
		},function(error){
			console.log(F,"Error while removing appliance " + appliance.name);
			dialogs.showServerResponse(error.response.text);
		});
		
		return def;
	},
	
	viewDomains = function(){
		// summary:
		//		View the domains on the currently selected appliances
		
		var F = "wamc.controller.appliances.viewDomains()";
		console.debug(F);
		
		var args = {"type":"any","conditions":[]},
			grid = registry.byId("applianceGrid"),
			selection = grid.select.row.getSelected();
		
		if(selection.length > 0){
			
			array.forEach(selection,function(appliance){
				var nextName = grid.row(appliance).data().name;
				
				args.conditions.push({
					"colId":"applianceName",
					"condition":"equal",
					"value":nextName});
			});
			
			console.debug(F,args);
			
			// publish message
			topic.publish("setFilter","domains",args);
		}
	},
	
	quiesceAppliance = function(/*Appliance*/appliance){
		// summary:
		//		Quiesce a managed appliance
		// appliance:
		//		The appliance to quiesce
		
		var F = "wamc.controller.appliances.quiesceAppliance()";
		console.debug(F,appliance);

		var pName = appliance.name,
			url = string.substitute(Resources.quiesceAppliance.url,{applianceId:appliance.id}),
			xhrArgs = {
				method:Resources.quiesceAppliance.add.method,
				handleAs:"json"
		};
		
		xhr(url,xhrArgs).then(function(data){
			console.debug(F,"Quiesce appliance " + pName + " started");
		},
		function(err){
			console.log(F,"Error while quiescing appliance " + pName);
			dialogs.showServerResponse(err.response.text);
		});
	},
	
	quiesceAppliances = function(){
		// summary:
		//		Quiesce selected appliances
		var F = "wamc.controller.appliances.quiesce()";
		console.debug(F);
		
		var grid = registry.byId("applianceGrid");
		
		gridUtil.withSelectedData(grid,function(selection){
			if(selection.length){
				array.forEach(selection,function(appliance){
					quiesceAppliance(appliance);
				});
			}	
		});
	},
	
	unquiesceAppliance = function(/*Appliance*/appliance){
		// summary:
		//		Unquiesce a managed appliance
		// pName:
		//		The name (identifier) of the appliance to unquiesce
		
		var F = "wamc.controller.appliances.unquiesceAppliance()";
		console.debug(F,appliance);
		
		var pName = appliance.name,
			url = string.substitute(Resources.unquiesceAppliance.url,{applianceId:appliance.id}),
			xhrArgs = {
				method:Resources.unquiesceAppliance.add.method,
				handleAs:"json"
		};
		
		xhr(url,xhrArgs).then(function(data){
			console.debug(F,"Appliance " + pName + " unquiescing");
		},
		function(err){
			console.debug(F,"Error while unquiescing appliance " + pName);
			dialogs.showServerResponse(err.response.text);
		});
	},
	
	unquiesceAppliances = function(){
		// summary:
		//		Get selected appliances and post messages to server
		var F = "wamc.controller.appliances.unquiesce()";
		console.debug(F);
		
		var grid = registry.byId("applianceGrid");
		
		gridUtil.withSelectedData(grid,function(selection){
			if(selection.length){
				array.forEach(selection,function(appliance){
					unquiesceAppliance(appliance);
				});
			}	
		});
	},
	
	backupAppliance = function(/*Object*/backupConfig){
		// summary:
		//		Backup an appliance using the config object supplied.
		// backupConfig: Object
		//		Uses a config object that looks like this:
		//			{
		//				appliance: {
		//					id:"",	//String, will not be sent to server
		//				},
		//				certificateName: "", // String, required if certificateLocation is not provided
		//				certificateLocation: "", //String, required if certificateName is not provided. For local or remote certs
		//				backupDestination: "" // String. Format depends on location type
		//				includeIscsi: true //Boolean, depends on value of checkbox
		//				includeRaid: true //Boolean, depends on value of checkbox
		//				
		//			}

		var F = "wamc.controller.appliances.backupAppliance()";
		console.debug(F,backupConfig);
		
		var applianceId = backupConfig.appliance.id,
			url = string.substitute(Resources.backupAppliance.url,{applianceId:applianceId});
		
		delete backupConfig.appliance;
		
		xhr.post(url,{
			handleAs:"json",
			data:json.toJson(backupConfig),
			headers:{
				"Content-Type":"application/json",
				Accept: "application/javascript, application/json"
			}
		}).then(function(response){
			console.debug(F,"Successfully initiated backup for appliance",applianceId);
			return response;
		},
		function(err){
			console.log(F,"Error while backing up appliance",applianceId);
			dialogs.showServerResponse(err.response.text);
			return err;
		});
	},
	
	restoreAppliance = function(/*Object*/restoreConfig){
		// summary:
		//		Restore an appliance using the config object supplied.
		// restoreConfig: Object
		//		Uses a config object that looks like this:
		//			{
		//				appliance: {
		//					id:"",	//String, will not be sent to server
		//				},
		//				credentialName: "", // String, required, the name of the crypto certificate
		//				backupSource: "", //String, required, url of backup file
		//				
		//			}

		var F = "wamc.controller.appliances.restoreAppliance()";
		console.debug(F,restoreConfig);
		
		var applianceId = restoreConfig.appliance.id,
			url = string.substitute(Resources.restoreAppliance.url,{applianceId:applianceId});
		
		delete restoreConfig.appliance;
		
		xhr.post(url,{
			handleAs:"json",
			data:json.toJson(restoreConfig),
			headers:{
				"Content-Type":"application/json",
				Accept: "application/javascript, application/json"
			}
		}).then(function(response){
			console.debug(F,"Successfully initiated restore for appliance",applianceId);
			return response;
		},function(err){
			console.log(F,"Error while restoring up appliance",applianceId);
			dialogs.showServerResponse(err.response.text);
			return err;
		});
	},
	
	showAddApplianceDialog = function(){
		var dlg = registry.byId("addApplianceDialog"),
			widget = registry.byId("addApplianceForm");

		if(!dlg){
			dlg = new Dialog({
				id:"addApplianceDialog",
				title:nls.dialog.add_appliance});
			widget = new AddApplianceForm({id:"addApplianceForm"});
			dlg.setContent(widget);
			dlg.startup();
			
			// When the Add Appliance form submit button is pressed, close the dialog
			aspect.after(widget,"onSubmit",function(arg){
				
				var model = widget.get("model");
				
				standby.markBusy(dlg);
				
				var def = addAppliance(model);
				
				when(def,function(result){
					standby.markAvailable(dlg);
					dlg.hide().then(lang.hitch(widget,widget.reset));
				},
				function(error){
					standby.markAvailable(dlg);
					widget.showServerResponse(error.response.text);
					widget.enable();
				});
				
			});
			
			// when someone clicks on the cancel button of an add appliance form
			aspect.after(widget, "onCancel", function(){
				dlg.hide().then(lang.hitch(widget,widget.reset));
			});
		}
		
		dlg.show();
	},
	
	showEditApplianceDialog = function(){
		var grid = registry.byId("applianceGrid"),
			dlg = registry.byId("editApplianceDialog"),
			widget = registry.byId("editApplianceForm");
		
		if(!dlg){
			dlg = new Dialog({
				id:"editApplianceDialog",
				title:nls.dialog.edit_properties});
			widget = new EditApplianceDetail({id:"editApplianceForm"});
			dlg.setContent(widget);
			dlg.startup();
			
			// Setup events
			aspect.after(widget,"onSubmit",function(arg){
				var model = widget.get("formData");
				
				editAppliance(model).then(function(result){
					dlg.hide();
				},function(error){
					widget.showServerResponse(error.response.text);
					widget.enable();
				});
				
			});
			
			// when someone clicks on the cancel button of an update appliance management properties form
			aspect.after(widget, "onCancel", function(){
				widget._updateDisplay(widget.model); // Reset fields
				dlg.hide();
			});
			
		}
		
		gridUtil.withSelectedData(grid,function(selection){
			var selected = selection.length==1 ? selection[0] : null;
			widget.reset();
			widget.set("model",selected);
			dlg.show();
		});
	},
	
	showBackupApplianceDialog = function(){
		var grid = registry.byId("applianceGrid");
			dlg = registry.byId("backupApplianceDialog"),
			widget = registry.byId("backupApplianceWizard");
		
		if(!dlg){
			dlg = new Dialog({
				id:"backupApplianceDialog",
				title:nls.dialog.backup_appliance});
			widget = new BackupApplianceWizard({id:"backupApplianceWizard"});
			dlg.setContent(widget);
			dlg.startup();
			
			// When the backup appliance form submit button is pressed, close the dialog
			aspect.after(widget,"onDone",function(){
				var config = widget.get("model");
				dlg.hide();
				backupAppliance(config);
			});
			
			// when someone clicks on the cancel button of an backup appliance form
			aspect.after(widget, "onCancel", function(){
				dlg.hide();
			});
			
		}
		
		gridUtil.withSelectedData(grid,function(appliances){
			widget.set("model",{"appliance":appliances[0]});
			dlg.show();
		});
	},
	
	showRestoreApplianceDialog = function(){
		var grid = registry.byId("applianceGrid");
			dlg = registry.byId("restoreApplianceDialog"),
			widget = registry.byId("restoreApplianceWizard");
		
		if(!dlg){
			dlg = new Dialog({
				id:"restoreApplianceDialog",
				title:nls.dialog.restore_appliance});
			widget = new RestoreApplianceWizard({id:"restoreApplianceWizard"});
			dlg.setContent(widget);
			dlg.startup();
			
			// When the remove appliance form submit button is pressed, close the dialog
			aspect.after(widget,"onDone",function(){
				var config = widget.get("model");
				dlg.hide();
				restoreAppliance(config);
			});
			
			// when someone clicks on the cancel button of an backup appliance form
			aspect.after(widget, "onCancel", function(){
				dlg.hide();
			});
			
		}
		
		gridUtil.withSelectedData(grid,function(appliances){
			widget.set("model",{"appliance":appliances[0]});
			dlg.show();
		});
	},
	
	showDeployFirmwareDialog = function(){
		var grid = registry.byId("applianceGrid");
			dlg = registry.byId("deployFirmwareDialog"),
			widget = registry.byId("deployFirmwareForm");
	
		if(!dlg){
			dlg = new Dialog({
				id:"deployFirmwareDialog",
				title:nls.dialog.deploy_firmware});
			widget = new DeployFirmwareForm({id:"deployFirmwareForm"});
			dlg.setContent(widget);
			dlg.startup();
			
			// when someone clicks on the cancel button of the deploy firmware form
			aspect.after(widget, "onCancel", function(){
				dlg.hide();
			});
			
			// when someone clicks on the submit button of the deploy firmware form
			aspect.after(widget, "onDeploy", function(){
				dlg.hide();
			});
		}
		
		gridUtil.withSelectedData(grid,function(appliances){
			widget.set("appliances",appliances);
			dlg.show();
		});
	},
	
	showCreateDomainDialog = function(){
		var grid = registry.byId("applianceGrid");
			dlg = registry.byId("createDomainDialog"),
			widget = registry.byId("createDomainWizard");

		if(!dlg){
			dlg = new Dialog({
				id:"createDomainDialog",
				title:nls.dialog.create_domain});
			widget = new CreateDomainWizard({id:"createDomainWizard",
					store:stores.get("domain")});
			dlg.setContent(widget);
			dlg.startup();
			
			// when someone clicks on the cancel button of an Create Domain Wizard
			aspect.after(widget, "onCancel", function(){
				dlg.hide();
			});
			
			// when someone clicks on the Done button of an Create Domain Wizard
			aspect.after(widget, "onDone", function(){
				var config = widget.get("model");
				dlg.hide();
				createDomains(config);
			});
		}
		
		gridUtil.withSelectedData(grid,function(appliances){
			widget.set("model",{appliances:appliances});
			dlg.show();
		});
	},
	
	showAssignGroupsDialog = function(){
		var grid = registry.byId("applianceGrid");
			dlg = registry.byId("assignApplianceGroupsDialog"),
			widget = registry.byId("assignApplianceGroupsForm");

		if(!dlg){
			dlg = new Dialog({
				id:"assignApplianceGroupsDialog",
				title:nls.dialog.assign_groups});
			widget = new AssignGroupsForm({id:"assignApplianceGroupsForm"});
			dlg.setContent(widget);
			dlg.startup();
			
			aspect.after(widget,"onSubmit",function(appliances){
				var def = updateGroups(appliances);
				
				standby.markBusy(dlg);
				
				def.then(function(result){
						standby.markAvailable(dlg);
						dlg.hide().then(lang.hitch(widget,widget.reset));
						if(appliances.length === 1){
							messages.showMessage("SUCCESS_ASSIGN_GROUP",{type:nls.global.appliance.toLowerCase(),name:appliances[0].name});
						}else{
							messages.showMessage("SUCCESS_ASSIGN_GROUPS",{type:nls.global.appliances.toLowerCase(),count:appliances.length});
						}
						gridUtil.publishSelection(grid);
					},function(error){
						standby.markAvailable(dlg);
						widget.showServerResponse(error.xhr.responseText);
					});
			},true);
			
			aspect.after(widget,"onCancel",function(){
				dlg.hide();
			});
		}
		
		gridUtil.withSelectedData(grid,function(appliances){
			when(listGroups(),function(groups){
				widget.reset();
				widget.set("groups",groups);
				widget.set("objects",appliances);
				dlg.show();	
			});
		});
	},
	
	confirmRemoveAppliances = function(){
		var grid = registry.byId("applianceGrid");
			appliances = grid.select.row.getSelected();
		
		switch(appliances.length){
			case 0:
				dialogs.showDialog("ERROR_NO_APPLIANCE_SELECTED");
				break;
			case 1:
				var appliance = grid.row(appliances[0]).data();
				dialogs.showDialog("CONFIRM_REMOVE_APPLIANCE",{name:appliance.name},removeSelectedAppliances);
				break;
			default:
				dialogs.showDialog("CONFIRM_REMOVE_APPLIANCES",{quantity:appliances.length},removeSelectedAppliances);
				break;
		}
	},
	
	confirmQuiesceAppliances = function(){
		var grid = registry.byId("applianceGrid"),
			appliances = grid.select.row.getSelected();
		
		switch(appliances.length){
			case 0:
				dialogs.showDialog("ERROR_NO_APPLIANCE_SELECTED");
				break;
			case 1:
				var appliance = grid.row(appliances[0]).data();
				dialogs.showDialog("CONFIRM_QUIESCE_APPLIANCE",{name:appliance.name},quiesceAppliances);
				break;
			default:
				dialogs.showDialog("CONFIRM_QUIESCE_APPLIANCES",{quantity:appliances.length},quiesceAppliances);
				break;
		}
	},
	
	confirmUnquiesceAppliances = function(){
		var grid = registry.byId("applianceGrid"),
			appliances = grid.select.row.getSelected();
		
		switch(appliances.length){
		case 0:
			dialogs.showDialog("ERROR_NO_APPLIANCE_SELECTED");
			break;
		case 1:
			var appliance = grid.row(appliances[0]).data();
			dialogs.showDialog("CONFIRM_UNQUIESCE_APPLIANCE",{name:appliance.name},unquiesceAppliances);
			break;
		default:
			dialogs.showDialog("CONFIRM_UNQUIESCE_APPLIANCES",{quantity:appliances.length},unquiesceAppliances);
			break;
		}
	},
	
	confirmReboot = function(){
		var grid = registry.byId("applianceGrid"),
			appliances = grid.select.row.getSelected();
		
		switch(appliances.length){
			case 0:
				dialogs.showDialog("ERROR_NO_APPLIANCE_SELECTED");
				break;
			case 1:
				var appliance = grid.row(appliances[0]).data();
				dialogs.showDialog("CONFIRM_REBOOT_APPLIANCE",{name:appliance.name},reboot);
				break;
			default:
				dialogs.showDialog("CONFIRM_REBOOT_APPLIANCES",{quantity:appliances.length},reboot);
				break;
		}
	},
	

	
	Appliances = declare(_ContentController,{
		
		initUI:function(){
			// summary:
			//		Build the appliances page UI
			var container = new BorderContainer({
					//"class":"wamcPageContainer"
				},"applianceContainer"),
				gridPane = new BorderContainer({
						region:"center",
						gutters:false,
						'aria-label':nls.regions.appliances.grid,
						role:"region"},
					"applianceGridPane"),
				gridToolbar = new ApplianceToolbar({
						region:"top",context:"appliances",grid:"applianceGrid"},
					"applianceGridToolbar"),
				grid = new Grid({
						region:"center",
						store:stores.get("appliance"),
						cacheClass:"gridx/core/model/cache/Async",
							modules:[{moduleClass:"gridx/modules/ColumnResizer"},
							{moduleClass:"gridx/modules/extendedSelect/Row",triggerOnCell:true,enabled:true},
							{moduleClass:"gridx/modules/Filter",serverMode:false},
							{moduleClass:"gridx/modules/filter/FilterBar",closeButton:false,ruleCountToConfirmClearFilter:0,hasFilterMessage:nls.global.hasFilterMessage},
							{moduleClass:"gridx/modules/VirtualVScroller"},
							{moduleClass:"gridx/modules/Focus"},
							{moduleClass:"wamc/grid/modules/filter/FilterSetup",filterData:this._filter}],
					columnWidthPercentage:true,
					columnWidthAutoResize: true,
					structure: [
							{ id: "name", name: nls.applianceGrid.name, field: "name", dataType: "string", width: "15%" },
							{ id: "hostName",name: nls.applianceGrid.hostName, field: "hostName", dataType: "string",width: "20%" },
							{ id: "applianceType", name: nls.applianceGrid.applianceType, field: "applianceType", dataType: "string", width: "8%" },
							{ id: "model", name: nls.applianceGrid.model, field: "model", dataType: "string", width: "8%" },
							{ id: "firmwareLevel", name: nls.applianceGrid.firmwareLevel, field: "firmwareLevel", dataType: "string", width: "8%" },
							{ id: "groups", name: nls.applianceGrid.groups, field: "groups", dataType: "string", width: "33%", decorator:wamc.grid.format.decorateGroupsList},
							{ id: "status", name: nls.applianceGrid.status, field: "status", dataType: "string", width: "8%", decorator:wamc.grid.format.decorateApplianceGridStatus}],
					"aria-label":nls.applianceGrid.title,
					"class":"compact"},
					"applianceGrid"),
				detailPane = new ContentPane({
						region:"trailing",
						"aria-label":nls.regions.appliances.properties,
						role:"region",
						"class":"wamcPropertiesPanel"},"applianceDetailPane"),
				detailToolbar = new PropertiesToolbar({context:"appliances",master:"applianceGrid"},
						"applianceDetailToolbar"),
				detail = new ApplianceDetail({viewOnly:true,master:"applianceGrid"},
						"applianceDetail");
			
			grid.onModulesLoaded = function(){
				gridUtil.refreshFilterBar(grid);
				masterDetail.filterChanged(grid);
			};
			
			gridPane.addChild(gridToolbar);
			gridPane.addChild(grid);
			
			container.addChild(gridPane);
			container.addChild(detailPane);
			
			// Start everything
			container.startup();
		},
		
		setupEvents:function(){
			// summary:
			//		Connect widgets for the appliances tab
			
			var grid = registry.byId("applianceGrid"),
				gridToolbar = registry.byId("applianceGridToolbar"),
				detailToolbar = registry.byId("applianceDetailToolbar"), 
				detail = registry.byId("applianceDetail");
			
			// Any time the table selection changes, update the available actions
			aspect.after(grid.select.row,"onSelectionChange",
					lang.partial(masterDetail.selectionChanged,grid),true);
			
			// Whenever a grid filter is applied, make sure a visible row is selected
			aspect.after(grid.filter,"setFilter",
					lang.partial(masterDetail.filterChanged,grid));
			
			// show hover help for the groups column 
			aspect.after(grid, "onCellMouseOver",function(evt){
				if (evt.columnId === "groups"){
					var cell = grid.cell(evt.rowIndex, evt.columnIndex),
						cellData = cell ? cell.data() : null;
					if (cellData && cellData.length > 0){
						var header = string.substitute(nls.groupsHoverHelp.header,
								{resource: grid.cell(evt.rowIndex, 0).data()});
						var content = string.substitute(groupsHoverHelpTemplate,
								{header: header, list: gridFormat.decorateGroupsList(cellData)});
						Tooltip.show(content,evt.cellNode,["below","after"]);	
					}
				}
			},true);
			
			// hide hover help for the groups column
			aspect.after(grid, "onCellMouseOut",function(evt){
				if (evt.columnId === "groups"){
					Tooltip.hide(evt.cellNode);
				}
			},true);
			
			// ActionStatus refreshes
			topic.subscribe("actionStatus",
					lang.partial(masterDetail.publishActionStatus,grid));
			
			// When addAppliance is clicked in the toolbar, show the addApplianceForm widget
			aspect.after(gridToolbar,"addAppliance",function(evt){
				showAddApplianceDialog();
			});
			
			// When backupAppliance is clicked in the toolbar, show the backupApplianceForm widget
			aspect.after(gridToolbar,"backup",function(evt){
				showBackupApplianceDialog();
			});
			
			// When restoreAppliance is clicked in the toolbar, show the backupApplianceForm widget
			aspect.after(gridToolbar,"restore",function(evt){
				showRestoreApplianceDialog();
			});
			
			// When removeAppliance is clicked in the toolbar, remove the appliance from the store
			aspect.after(gridToolbar,"removeAppliance",function(evt){
				confirmRemoveAppliances();
			});
			
			aspect.after(gridToolbar,"viewDomains",function(evt){
				viewDomains();
			});
			
			aspect.after(gridToolbar,"quiesce",function(evt){
				confirmQuiesceAppliances();
			});
			
			aspect.after(gridToolbar,"unquiesce",function(evt){
				confirmUnquiesceAppliances();
			});
			
			aspect.after(gridToolbar,"deployFirmware",function(evt){
				showDeployFirmwareDialog();
			});
			
			aspect.after(gridToolbar,"createDomain",function(evt){
				showCreateDomainDialog();
			});
			
			aspect.after(gridToolbar,"assignGroups",function(evt){
				showAssignGroupsDialog();
			});
			
			aspect.after(gridToolbar,"reboot",function(evt){
				confirmReboot();
			});
			
			// when the edit properties button is pressed, display the edit dialog
			aspect.after(detailToolbar,"editProperties",function(evt){
				showEditApplianceDialog();
			});
			
			// Finally, fire an event to configure the grid
			masterDetail.selectionChanged(grid,[]);
		},
		
		init:function(){
			this.initUI();
			this.setupEvents();
			this.inherited(arguments);
		},
		
		setFilter:function(/*Object*/filter){
			var grid = registry.byId("applianceGrid");
			
			this._filter = filter;
			
			grid && grid.filterBar.applyFilter(filter);
		},
		
		refreshContent:function(){
			var grid;
			if(this.isLoaded()){
				grid= registry.byId("applianceGrid");
				gridUtil.refreshGrid(grid).then(function(){
					gridUtil.selectDefault(grid);
					gridUtil.publishSelection(grid);
				});
			}
		}
	});
	
	return new Appliances();
});
