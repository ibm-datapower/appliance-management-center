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
        "dojo/_base/window",
        "dojo/aspect",
        "dojo/promise/all",
        "dojo/request/xhr",
        "dojo/string",
        "dojo/topic",
        "dojo/when",
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
        "wamc/widget/DomainToolbar",
        "wamc/widget/PropertiesToolbar",
        "wamc/widget/form/AssignGroupsForm",
        "wamc/widget/form/DeleteDomainsForm",
        "wamc/widget/form/DomainDetail",
        "wamc/widget/form/EditDomainDetail",
        "wamc/widget/wizard/CreateServiceWizard",
        "wamc/widget/wizard/DomainConfigurationWizard",
        "wamc/widget/wizard/UploadFileWizard",
        "dojo/text!wamc/controller/templates/_GroupsHoverHelp.html",
        "dojo/i18n!wamc/nls/strings"],
		function(array,declare,json,lang,win,aspect,all,xhr,string,topic,when,
				Dialog,BorderContainer,ContentPane,registry,Grid,Async,
				Tooltip,dialogs,messages,stores,standby,Resources,
				_ContentController,masterDetail,gridFormat,gridModules,gridUtil,
				DomainToolbar,PropertiesToolbar,AssignGroupsForm,
				DeleteDomainsForm,DomainDetail,EditDomainDetail,
				CreateServiceWizard,DomainConfigurationWizard,UploadFileWizard,
				groupsHoverHelpTemplate,nls){
	
	var _module = "wamc.controller.domains",
	
	quiesceDomain = function(/*Domain*/domain){
		// summary:
		//		Quiesce a managed domain
		// domain:
		//		The domain to quiesce
		
		var F = _module + ".quiesceDomain()";
		console.debug(F,domain);

		var pName = domain.displayName,
			url = stores.get("domain").uriFromPrimaryKey(domain.primaryKey,
					Resources.quiesceDomain.url),
			xhrArgs = {
				method: Resources.quiesceDomain.add.method,
				handleAs:"json"
		};
		xhr(url,xhrArgs).then(function(data){
			console.debug(F,"Quiesce domain " + pName + " started");
		},function(error){
			console.log(F,"Error while quiescing domain " + pName);
			dialogs.showServerResponse(error.response.text);
		});
	},
	
	quiesce = function(){
		// summary:
		//		Quiesce selected domains
		var F = _module + ".quiesce()";
		console.debug(F);
		
		var grid = registry.byId("domainGrid");
		
		gridUtil.withSelectedData(grid,function(selection){
			if(selection.length){
				array.forEach(selection,function(domain){
					quiesceDomain(domain);
				});
			}	
		});
	},
	
	unquiesceDomain = function(/*Domain*/domain){
		// summary:
		//		Unquiesce a managed domain
		// domain:
		//		The the domain to unquiesce
		var F = _module + ".unquiesceDomain():";
		console.debug(F,domain);

		var pName = domain.displayName,
			url = stores.get("domain").uriFromPrimaryKey(domain.primaryKey,
					Resources.unquiesceDomain.url),
			xhrArgs = {
				method:Resources.unquiesceDomain.add.method,
				handleAs:"json"
				
		};
		xhr(url,xhrArgs).then(function(data){
			console.debug(F,"Domain " + pName + " unquiescing");
		},function(error){
			console.debug(F,"Error while unquiescing domain " + pName);
			dialogs.showServerResponse(error.response.text);
		});
	},	
	
	unquiesce = function(){
		// summary:
		//		Get selected domains and post messages to server
		var F = _module + ".unquiesce()";
		console.debug(F);
		
		var grid = registry.byId("domainGrid");
		
		gridUtil.withSelectedData(grid,function(selection){
			if(selection.length){
				array.forEach(selection,function(domain){
					unquiesceDomain(domain);
				});
			}
		});
	},
	
	restartDomain = function(/*Domain*/domain){
		// summary:
		//		Restart a managed domain
		// domain:
		//		The domain to restart
		
		var F = _module + ".restartDomain()";
		console.debug(F,domain);

		var pName = domain.displayName,
			url = stores.get("domain").uriFromPrimaryKey(domain.primaryKey, Resources.restartDomain.url);
			xhrArgs = {
				method:Resources.restartDomain.method,
				handleAs:"json"
		};
		xhr(url,xhrArgs).then(function(data){
			console.debug(F,"Restart domain " + pName + " started");
		},function(error){
			console.log(F,"Error while restarting domain " + pName);
			dialogs.showServerResponse(error.response.text);
		});
	},
	
	restart = function(){
		// summary:
		//		Restart selected domains
		var F = _module + ".restart()";
		console.debug(F);
		
		var grid = registry.byId("domainGrid");
		
		gridUtil.withSelectedData(grid,function(selection){
			if(selection.length){
				array.forEach(selection,function(domain){
					restartDomain(domain);
				});
			}
		});
	},
	
	deleteDomain = function(/*Domain*/domain){
		// summary:
		//		Delete a managed domain
		// domain:
		//		The the domain to delete
		var F = _module + ".deleteDomain()";
		console.debug(F,domain);

		var def = stores.get("domain").remove(domain.primaryKey);
		
		when(def,function(result){
			console.debug(F,"Domain " + domain.displayName + " deleted");
			messages.showMessage("SUCCESS_DELETE_DOMAIN",{"name":domain.displayName,"appliance":domain.applianceName});
		},function(error){
			console.log(F,"Error while deleting domain " + domain.displayName);
			dialogs.showServerResponse(error.response.text);
		});
		
		return def;
	},

	deleteDomains = function(){
		// summary:
		//		Delete selected domains
		var F = _module + ".deleteDomains()";
		console.debug(F);
		
		var grid = registry.byId("domainGrid");
		
		gridUtil.withSelectedData(grid,function(selection){
			if(selection.length){
				var dl = [];
				
				array.forEach(selection,function(domain){
					if (domain.displayName.toLowerCase() != "default"){
						dl.push(deleteDomain(domain));
					}
				});
				
				all(dl).then(function(result){
					grid.select.row.clear();
				},function(error){
					grid.select.row.clear();
				});
			}	
		});
	},
	
	updateDomain = function(/*Domain*/domain){
		// summary:
		//		Update a domain
		// domain:
		//		The domain being updated
		var F = _module + ".updateDomain()";
		console.debug(F,domain);

		var pd = {
			primaryKey:domain.primaryKey,
			applianceId:domain.applianceId,
			name:domain.name,
			automaticSynchronization:domain.automaticSynchronization,
			quiesceTimeout:domain.quiesceTimeout
		};
		
		return stores.get("domain").put(pd);
	},
	
	viewAppliances = function(){
		// summary:
		//		Build filter criteria and redirect to the 
		//		appliances page.
		var F = _module + ".viewAppliances()";
		console.debug(F);
		
		var args = {"type":"any","conditions":[]},
			grid = registry.byId("domainGrid"),
			nameSet = {},
			selection = grid.select.row.getSelected();
		
		if(selection.length > 0){
			
			array.forEach(selection,function(appliance){
				var nextName = grid.row(appliance).data().applianceName;
				
				if (!nameSet[nextName]) {
					nameSet[nextName] = true;
					args.conditions.push({
						"colId":"name",
						"condition":"equal",
						"value":nextName});
				}
			});
			
			console.debug(F,args);
			
			// publish message
			topic.publish("setFilter","appliances",args);
		}
	},
	
	viewServices  = function(){
		// summary:
		//		Build filter criteria and redirect to the 
		//		domains page.
		var F = _module + ".viewServices()";
		console.debug(F);
		
		var args = {"type":"all","conditions":[]},
			grid = registry.byId("domainGrid"),
			selection = grid.select.row.getSelected();
		
		if(selection.length > 0){
			// Only show services for one domain
			var data = grid.row(selection[0]).data();
			
			args.conditions.push({
				"colId":"domainName",
				"condition":"equal",
				"value":data.displayName});
			
			args.conditions.push({
				"colId":"applianceName",
				"condition":"equal",
				"value":data.applianceName});
			
			console.debug(F,args);
			
			// publish message
			topic.publish("setFilter","services",args);
		}
		
	},
	
	deployConfiguration = function(/*Object*/domainConfig){
		var F = _module + ".deployConfiguration():";
		console.debug(F,domainConfig);
		
		if(!domainConfig) return;
		
		array.forEach(domainConfig.appliances,function(appliance){
			
			// Create the postData
			
			var pd= {
					sourceConfigLocation:domainConfig.uri
				},
				url = string.substitute(Resources.domainConfig.url,{applianceId:appliance.id,name:domainConfig.name});
			
			if(appliance.deploymentPolicy){
				if(appliance.deploymentPolicy.sourceType!=="none"){
					pd.deploymentPolicyLocation = appliance.deploymentPolicy.uri;
					pd.domainName = appliance.deploymentPolicy.domainName;
					pd.policyName = appliance.deploymentPolicy.policyName;
				}
			}
			
			pd.automaticSynchronization = domainConfig.automaticSynchronization;
			var pdJson = json.toJson(pd);
			console.debug(F, "pd json: ", pdJson);
			
			var xhrArgs = {
					handleAs:"json",
					data:pdJson,
					headers:{
						"Content-Type": "application/json",
						Accept: "application/javascript, application/json"
					}
					
			};
			
			xhr.put(url,xhrArgs).then(function(data){
				console.debug(F,"Create " + domainConfig.name + " started");
			},function(error){
				console.log(F,"Error while creatin domain appliance " + domainConfig.name);
				dialogs.showServerResponse(error.response.text);
			});
		});
	},
	
	createServices = function(/*Object*/serviceConfig){
		var F = _module + ".createServices()";
		console.debug(F,serviceConfig);
		
		if(!serviceConfig) return;
		
		array.forEach(serviceConfig.domains,function(domain){
			
			// Create the putData
			var pd= {
					configLocation:serviceConfig.uri,
					importAllFiles:true
				},
				url = string.substitute(
						Resources.service.url,{
							applianceId:domain.applianceId,
							domainName:domain.name,
							className:serviceConfig.serviceClass,
							name:serviceConfig.name
						});
			
			if(domain.deploymentPolicy){
				if(domain.deploymentPolicy.sourceType==="local"){
					pd.policyLocation = domain.deploymentPolicy.uri;
				}else if (domain.deploymentPolicy.sourceType==="remote"){
					pd.policyLocation = domain.deploymentPolicy.uri;
				}
				pd.policyDomain = domain.deploymentPolicy.domainName;
				pd.policyName = domain.deploymentPolicy.policyName;
			}
			
			var xhrArgs = {
					handleAs:"json",
					data:json.toJson(pd),
					headers:{
						"Content-Type": "application/json",
						Accept: "application/javascript, application/json"
					}
			};
			
			xhr.put(url,xhrArgs).then(function(data){
				console.debug(F,"Create " + serviceConfig.name + " started");
			},function(error){
				console.log(F,"Error while creating service " + serviceConfig.name);
				dialogs.showServerResponse(error.response.text);
			});
		});	
	},
	
	uploadFile = function(/*Object*/fileDefinition){
		// summary:
		//		Trigger upload of a file to the appliance of a domain
		// fileDefintion:
		//		An object containing the source and fileName of the file, as
		//		well as the list of appliances to deploy to

		var F = _module + ".uploadFile()";
		console.debug(F,fileDefinition);
		
		var pd = {source:fileDefinition.source,fileName:fileDefinition.folder};
		
		// Add a slash if one is not already there
		if(fileDefinition.folder.charAt(fileDefinition.folder.length -1)!=="/"){
			pd.fileName += "/";
		}
		// Now add the filename
		pd.fileName += fileDefinition.fileName;
		
		// Tell each of the domains to pick up the new file.
		array.forEach(fileDefinition.domains,function(domain){
			var url = stores.get("domain").uriFromPrimaryKey(domain.primaryKey,Resources.domainFiles.url),
				xhrArgs = {
					handleAs:"json",
					data:json.toJson(pd),
					headers:{
						"Content-Type": "application/json",
						Accept: "application/javascript, application/json"
					}
			};
			xhr.post(url,xhrArgs).then(function(data){
				console.debug(F,"Uploaded file " + pd.fileName);
			},function(error){
				console.log(F,"Error while uploading file " + pd.fileName);
				dialogs.showServerResponse(error.response.text);
			});
		});
	},
	
	_domainConfigurationModel = function(/*Array*/domains){
		// summary:
		//		Build an appliance model for the domainConfig dialog
		// tags:
		//		protected
		var F = _module + "._domainConfigurationModel()";
		console.debug(F,domains);
		
		var m = {appliances:[]};
		
		array.forEach(domains,function(domain){
			m.appliances.push({id:domain.applianceId,name:domain.applianceName});
		});
		
		m.name =domains[0].name;
		
		return m;
	},
	
	updateDomainGroups = function(/*Array/Object*/domain){
		// summary:
		//		Update group membership for a domain
		// appliance:Array/Object
		//		The domain or domains to update
		var F = _module + ".updateGroups()";
		console.debug(F,domain);
		
		var store = stores.get("domain"),
			url = store.uriFromPrimaryKey(domain.primaryKey,Resources.domainGroups.url),
			xhrArgs = {
				data:json.toJson(domain.groups),
				handleAs:"json",
				headers: {
					"Content-Type":"application/json",
					Accept: "application/javascript, application/json"
				}
			};
	
		return xhr.put(url,xhrArgs).then(function(response){
			stores.notifyUpdate(store,domain.primaryKey);
		});

	},
	
	updateGroups = function(/*Array/Object*/domain){
		// summary:
		//		Update group membership for a domain
		// domain:Array/Object
		//		The doma or domains to update
		
		if(lang.isArray(domain)){
			var dl = array.map(domain,updateDomainGroups);
			return all(dl);
		}else{
			return updateDomainGroups(domain);
		}
	},
	
	listGroups = function(){
		// summary:
		//		Get a list of the available groups
		// return: /*Deferred*/
		//		A deferred that resolves to an array of strings representing the available groups
		
		return xhr.get(Resources.groups.url,{
			handleAs: "json",
			headers: {
				"Content-Type":"application/json",
				Accept: "application/javascript, application/json"
			}
		});
	},
	
	_selectionChanged = function(/*gridx/Grid*/grid,/*Array*/newSelectedIds,/*Array*/oldSelectedIds){
		// summary:
		//		Used instead of master-detail/selectionChanged
		//		to augment selected data with appliance information.
		// tags:
		//		protected
		var F = _module + "._selectionChanged()";
		console.debug(F,newSelectedIds,oldSelectedIds);
		
		var newSelection = null,
			topicId = grid.id + "_selectionChanged";
		
		grid.when().then(function(){
			if(lang.isArray(newSelectedIds) &&
					newSelectedIds.length === 0){
				// If nothing is selected try and select the first row in the grid
				newSelection = gridUtil.selectDefault(grid, newSelectedIds, oldSelectedIds);
			}
			
			gridUtil.withSelected(grid,
				function(domainId){ // Hit the server for more information about what's selected
					var applianceDef = masterDetail.getCachedAppliance(domainId),
						domainDef = stores.get("domain").get(domainId);
					return all([domainDef, applianceDef]).then(
							function(results){
								// Add the appliance capabilities to the domain.
								// It would be possible to add the whole appliance here, 
								// if needed, but note that it's cached so things like
								// status could go out of date.
								console.debug(F,"LOOP",results);
								var domain = results[0];
								domain.applianceCapabilities = results[1].capabilities;
								return domain;
							});
					return combinedDeferred;
				},
				function(selection){ // Publish what came back.
					topic.publish(topicId,selection);
				});
		});
	},
	
	/****************************************/
	
	showAssignGroupsDialog = function(){
		var grid = registry.byId("domainGrid");
			dlg = registry.byId("assignDomainGroupsDialog"),
			widget = registry.byId("assignDomainGroupsForm");

		if(!dlg){
			dlg = new Dialog({
				id:"assignDomainGroupsDialog",
				title:nls.dialog.assign_groups});
			widget = new AssignGroupsForm({id:"assignDomainGroupsForm"});
			dlg.setContent(widget);
			dlg.startup();
			
			aspect.after(widget,"onSubmit",function(domains){
				var def = updateGroups(domains);
				
				standby.markBusy(dlg);
				
				def.then(function(result){
						standby.markAvailable(dlg);
						dlg.hide().then(lang.hitch(widget,widget.reset));
						if(domains.length === 1){
							messages.showMessage("SUCCESS_ASSIGN_GROUP",{type:nls.global.domain.toLowerCase(),name:domains[0].name});
						}else{
							messages.showMessage("SUCCESS_ASSIGN_GROUPS",{type:nls.global.domains.toLowerCase(),count:domains.length});
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
		
		gridUtil.withSelectedData(grid,function(selection){
			when(listGroups(),function(groups){
				widget.reset();
				widget.set("groups",groups);
				widget.set("objects",selection);
				dlg.show();	
			});
		});
	},
	
	showDomainConfigurationDialog = function(){
		var grid = registry.byId("domainGrid");
			dlg = registry.byId("domainConfigurationDialog"),
			widget = registry.byId("domainConfigurationWizard");

		if(!dlg){
			dlg = new Dialog({
				id:"domainConfigurationDialog",
				title:nls.dialog.deployConfiguration});
			widget = new DomainConfigurationWizard({
				id:"domainConfigurationWizard",
				store:stores.get("domain")});
			dlg.setContent(widget);
			dlg.startup();
			
			aspect.after(widget,"onDone",function(){
				var config = widget.get("model");
				dlg.hide();
				deployConfiguration(config);
			});
			
			aspect.after(widget,"onCancel",function(){
				dlg.hide();
			});
		}
		
		gridUtil.withSelectedData(grid,function(selection){
			var m = _domainConfigurationModel(selection);
			
			widget.set("model",m);
			dlg.show();
		});
	},
	
	showCreateServiceDialog = function(){
		var grid = registry.byId("domainGrid");
			dlg = registry.byId("createServiceDialog"),
			widget = registry.byId("createServiceWizard");

		if(!dlg){
			dlg = new Dialog({
				id:"createServiceDialog",
				title:nls.dialog.createService});
			widget = new CreateServiceWizard({id:"createServiceWizard"});
			dlg.setContent(widget);
			dlg.startup();
			
			aspect.after(widget,"onDone",function(){
				var config = widget.get("model");
				dlg.hide();
				createServices(config);
			});
			
			aspect.after(widget,"onCancel",function(){
				dlg.hide();
			});
		}
	
		gridUtil.withSelectedData(grid,function(selection){
			widget.reset();
			widget.set("model",{domains:selection});
			dlg.show();
		});
	},
	
	showUploadFileDialog = function(){
		var grid = registry.byId("domainGrid");
			dlg = registry.byId("uploadFileDialog"),
			widget = registry.byId("uploadFileWizard");

		if(!dlg){
			dlg = new Dialog({
				id:"uploadFileDialog",
				title:nls.dialog.uploadFile});
			widget = new UploadFileWizard({id:"uploadFileWizard"});
			dlg.setContent(widget);
			dlg.startup();
			
			aspect.after(widget,"onDone",function(){
				var fd = widget.get("model");
				dlg.hide();
				uploadFile(fd);
			});
			
			aspect.after(widget,"onCancel",function(){
				dlg.hide();
			});
		}
		
		gridUtil.withSelectedData(grid,function(selection){
			widget.reset();
			widget.set("model",{domains:selection});
			dlg.show();
		});
	},
	
	showDeleteDomainsForm = function(domains){
		var dlg = registry.byId("deleteDomainsDialog"),
			widget = registry.byId("deleteDomainsForm");
		
		if(!dlg){
			dlg = new Dialog({
				id:"deleteDomainsDialog",
				title:nls.dialog.deleteDomains});
			widget = new DeleteDomainsForm({id:"deleteDomainsForm"});
			dlg.setContent(widget);
			dlg.startup();
			
			aspect.after(widget, "onDelete", function(){
				dlg.hide();
				deleteDomains();
			});
			
			aspect.after(widget,"onCancel",function(){
				dlg.hide();
			});
		}
		
		widget.processDomains(domains);
		dlg.show();
	},
	
	showEditPropertiesDialog = function(){
		
		var grid = registry.byId("domainGrid");
			dlg = registry.byId("editDomainPropertiesDialog"),
			widget = registry.byId("editDomainPropertiesForm");

		if(!dlg){
			dlg = new Dialog({
				id:"editDomainPropertiesDialog",
				title:nls.dialog.editDomain});
			widget = new EditDomainDetail({id:"editDomainPropertiesForm"});
			dlg.setContent(widget);
			dlg.startup();
			
			aspect.after(widget,"onSubmit",function(){
				var domain = widget.get("model");
				
				dlg.hide();
				updateDomain(domain).then(function(response){
					messages.showMessage("SUCCESS_UPDATE_DOMAIN",{"name":domain.displayName,"appliance":domain.applianceName});
					gridUtil.publishSelection(grid);
				},function(error){
					controller.showServerResponse(error.response.text);
					widget.enable();
				});
			});
			
			aspect.after(widget,"onCancel",function(){
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
	
	/****************************************/
	
	confirmQuiesceDomains = function(){
		var grid = registry.byId("domainGrid"),
			domains = grid.select.row.getSelected();
		
		switch(domains.length){
			case 0:
				dialogs.showDialog("ERROR_NO_DOMAIN_SELECTED");
				break;
			case 1:
				var domain = grid.row(domains[0]).data();
				dialogs.showDialog("CONFIRM_QUIESCE_DOMAIN",{name:domain.displayName},quiesce);
				break;
			default:
				dialogs.showDialog("CONFIRM_QUIESCE_DOMAINS",{quantity:domains.length},quiesce);
				break;
		}
	},
	
	confirmUnquiesceDomains = function(){
		var grid = registry.byId("domainGrid"),
			domains = grid.select.row.getSelected();
		
		switch(domains.length){
			case 0:
				dialogs.showDialog("ERROR_NO_DOMAIN_SELECTED");
				break;
			case 1:
				var domain = grid.row(domains[0]).data();
				dialogs.showDialog("CONFIRM_UNQUIESCE_DOMAIN",{name:domain.displayName},unquiesce);
				break;
			default:
				dialogs.showDialog("CONFIRM_UNQUIESCE_DOMAINS",{quantity:domains.length},unquiesce);
				break;
		}
	},
	
	confirmRestartDomains = function(){
		var grid = registry.byId("domainGrid"),
			domains = grid.select.row.getSelected();
		
		switch(domains.length){
			case 0:
				dialogs.showDialog("ERROR_NO_DOMAIN_SELECTED");
				break;
			case 1:
				var domain = grid.row(domains[0]).data();
				dialogs.showDialog("CONFIRM_RESTART_DOMAIN",{name:domain.displayName},restart);
				break;
			default:
				dialogs.showDialog("CONFIRM_RESTART_DOMAINS",{quantity:domains.length},restart);
				break;
		}
	},
	
	confirmDeleteDomains = function(){
		var grid = registry.byId("domainGrid");
		
		gridUtil.withSelectedRows(grid,function(domains){
			
			if (domains.length < 1) {
				dialogs.showDialog("ERROR_NO_DOMAIN_SELECTED");
			} else {
				showDeleteDomainsForm(domains);
			}
		});
	},
	
	Domains = declare(_ContentController,{
		
		initUI:function(){
			// summary:
			//		Build the domains page UI
			var container = new BorderContainer({
					//"class":"wamcPageContainer"
				},"domainContainer"),
				gridPane = new BorderContainer({
						region:"center",
						gutters:false,
						'aria-label':nls.regions.domains.grid,
						role:"region"},
					"domainGridPane"),
				gridToolbar = new DomainToolbar({
						region:"top",context:"domains",grid:"domainGrid"},
					"domainGridToolbar"),
				grid = new Grid({
						region:"center",
						store:stores.get("domain"),
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
					structure: [{ id:'displayName', name: nls.domainGrid.name, field: 'displayName', dataType: 'string', width: '25%' },
								{ id:'applianceName', name: nls.domainGrid.appliance, field: 'applianceName', dataType: 'string', width: '15%' },
								{ id:'groups', name: nls.domainGrid.groups, field: 'groups', dataType: 'string', width: '50%', decorator:wamc.grid.format.decorateGroupsList },
								{ id:'status', name: nls.domainGrid.status, field: 'status', width: '10%', dataType: 'string', decorator:wamc.grid.format.decorateDomainGridStatus}],
					"aria-label":nls.domainGrid.title,
					"class":"compact"},
					"domainGrid"),
				detailPane = new ContentPane({
						region:"trailing",
						"aria-label":nls.regions.domains.properties,
						role:"region",
						"class":"wamcPropertiesPanel"},"domainDetailPane"),
				detailToolbar = new PropertiesToolbar({context:"domains",master:"domainGrid"},
						"domainDetailToolbar"),
				detail = new DomainDetail({viewOnly:true,master:"domainGrid"},
						"domainDetail");
			
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
			
			var grid = registry.byId("domainGrid"),
				gridToolbar = registry.byId("domainGridToolbar");
				detailToolbar = registry.byId("domainDetailToolbar"),
				detail = registry.byId("domainDetail");
				
				// Any time the table selection changes, update the available actions
				aspect.after(grid.select.row,"onSelectionChange",
						lang.partial(_selectionChanged,grid),true);
				
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
				
				aspect.after(gridToolbar,"assignGroups",showAssignGroupsDialog);
				
				aspect.after(gridToolbar,"viewAppliances",function(evt){
					viewAppliances();
				});
				
				aspect.after(gridToolbar,"viewServices",function(evt){
					viewServices();
				});
				
				aspect.after(gridToolbar,"quiesce",function(evt){
					confirmQuiesceDomains();
				});
				
				aspect.after(gridToolbar,"unquiesce",function(evt){
					confirmUnquiesceDomains();
				});
				
				aspect.after(gridToolbar,"restart",function(evt){
					confirmRestartDomains();
				});
				
				aspect.after(gridToolbar,"deployConfiguration",function(evt){
					showDomainConfigurationDialog();
				});
				
				aspect.after(gridToolbar,"createService",function(evt){
					showCreateServiceDialog();
				});
				
				aspect.after(gridToolbar,"uploadFile",function(evt){
					showUploadFileDialog();
				});
				
				// when someone clicks on the delete domain button, display the confirmation dialog
				aspect.after(gridToolbar,"deleteDomain",function(evt){
					confirmDeleteDomains();
				});
				
				// when the edit properties button is pressed, display the edit dialog
				aspect.after(detailToolbar,"editProperties",function(evt){
					showEditPropertiesDialog();
				});
				
				_selectionChanged(grid,[]);
			
		},
		
		init:function(){
			this.initUI();
			this.setupEvents();
			this.inherited(arguments);
		},
	
		setFilter:function(/*Object*/filter){
			var grid = registry.byId("domainGrid");
			
			this._filter = filter;
			
			grid && grid.filterBar.applyFilter(filter);
		},
		
		refreshContent:function(){
			var grid;
			if(this.isLoaded()){
				grid= registry.byId("domainGrid");
				gridUtil.refreshGrid(grid).then(function(){
					gridUtil.selectDefault(grid);
				});
			}
		}
		
	});
	
	return new Domains();
});
