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
        "dojo/request/xhr",
        "dojo/string",
        "dojo/topic",
        "dojo/when",
        "dojo/promise/all",
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
        "wamc/widget/PropertiesToolbar",
        "wamc/widget/ServiceToolbar",
        "wamc/widget/form/DeleteServiceForm",
        "wamc/widget/form/ServiceDetail",
        "wamc/widget/wizard/ServiceConfigurationWizard",
        "dojo/i18n!wamc/nls/strings"],
		function(array,declare,json,lang,aspect,xhr,string,topic,when,all,
				Dialog,BorderContainer,ContentPane,registry,Grid,Async,
				Tooltip,dialogs,messages,stores,standby,Resources,
				_ContentController,masterDetail,gridFormat,gridModules,gridUtil,
				PropertiesToolbar,ServiceToolbar,DeleteServiceForm,
				ServiceDetail,ServiceConfigurationWizard,nls){
	
	var _module = "wamc.controller.services",

	viewDomain = function(){
		// summary:
		//		Build filter criteria and redirect to the 
		//		domains page.
		var F = _module + ".viewDomain()";
		console.debug(F);
		
		var args = {"type":"all","conditions":[]},
			grid = registry.byId("serviceGrid"),
			selection = grid.select.row.getSelected();
		
		if(selection.length > 0){
			// Only show services for one domain
			var data = grid.row(selection[0]).data();
			
			args.conditions.push({
				"colId":"displayName",
				"condition":"equal",
				"value":data.domainName});
			
			args.conditions.push({
				"colId":"applianceName",
				"condition":"equal",
				"value":data.applianceName});
			
			console.debug(F,args);
			
			// publish message
			topic.publish("setFilter","domains",args);
		}
	},
	
	deleteService = function(){
		var F = _module + ".deleteService()";
		console.debug(F);
		
		var grid = registry.byId("serviceGrid"),
			serviceToDelete = registry.byId("deleteServiceForm").service,
			objectsToDelete = registry.byId("deleteServiceGrid").model.getMarkedIds(),
			def;
		
		console.debug(F, "Service to delete: ", serviceToDelete, "Child objects to delete: ", objectsToDelete);
		
		def = stores.get("service").remove(serviceToDelete.primaryKey,objectsToDelete);
		
		def.then(function(response){
			messages.showMessage("SUCCESS_DELETE_SERVICE", serviceToDelete);
			grid.select.row.clear();
		},function(error){
			console.debug(F,"Error while deleting service");
			dialogs.showServerResponse(error.response.text);
		});
	},
	
	deployConfiguration = function(/*Object*/serviceConfig){
		var F = _module + ".deployConfiguration()";
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
				console.debug(F,"Deploy Configuration for " + serviceConfig.name + " started");
			},function(error){
				console.log(F,"Error while deploying configuration for " + serviceConfig.name);
				dialogs.showServerResponse(error.response.text);
			});
		});	
	},
	
	_serviceConfigurationModel = function(/*Array*/services){
		// summary:
		//		Build a model for the serviceConfig wizard
		// tags:
		//		protected
		var F = _module + "._serviceConfigurationModel()";
		console.debug(F,services);
		
		var m = {
				name:services[0].name,
				serviceClass:services[0].className,
				domains:[]};
		
		array.forEach(services,function(service){
			m.domains.push({
				primaryKey:service.applianceId + ":" + service.domainName,
				applianceId:service.applianceId,
				applianceName:service.applianceName,
				name:service.domainName});
		});
		
		return m;
	},
	
	quiesceService = function(/*Object*/service){
		// summary:
		//		Quiesce a service
		var F = _module + ".quiesceService():";
		console.debug(F,service);
		
		var url = string.substitute(Resources.quiesceService.url,service),
			xhrArgs = {
				handleAs:"json"
		};
		
		xhr.post(url,xhrArgs).then(function(data){
			console.debug(F,"Quiesce service " + service + " submitted");
		},function(error){
			dialogs.showServerResponse(error.response.text);
		});
	},
	
	quiesce = function(){
		// summary:
		//		Quiesce currently selected services
		var F = _module + ".quiesce()";
		console.debug(F);
		
		var grid = registry.byId("serviceGrid");
		
		gridUtil.withSelectedData(grid,function(selection){
			if(selection.length){
				array.forEach(selection,function(service){
					quiesceService(service);
				});
			}
		});
	},
	
	unquiesceService = function(/*Object*/service){
		// summary:
		//		Unquiesce a service
		var F = _module + ".unquiesceService()";
		console.debug(F,service);
		
		var url = string.substitute(Resources.unquiesceService.url,service),
			xhrArgs = {
				handleAs:"json"
			};
	
		xhr.post(url,xhrArgs).then(function(data){
			console.debug(F,"Unquiesce service " + service + " submitted");
		},function(error){
			dialogs.showServerResponse(error.response.text);
		});
	},
	
	unquiesce = function(){
		// summary:
		//		Unquiesce currently selected services
		var F = _module + ".unquiesce()";
		console.debug(F);
		
		var grid = registry.byId("serviceGrid");
		
		gridUtil.withSelectedData(grid,function(selection){
			if(selection.length){
				array.forEach(selection,function(service){
					unquiesceService(service);
				});
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
				function(serviceId){ // Hit the server for more information about what's selected
					var applianceDef = masterDetail.getCachedAppliance(serviceId),
						serviceDef = stores.get("service").get(serviceId);
					return all([serviceDef, applianceDef]).then(
							function(results){
								// Add the appliance capabilities to the service.
								// It would be possible to add the whole appliance here, 
								// if needed, but note that it's cached so things like
								// status could go out of date.
								console.debug(F,"LOOP",results);
								var service = results[0];
								service.applianceCapabilities = results[1].capabilities;
								return service;
							});
					return combinedDeferred;
				},
				function(selection){ // Publish what came back.
					topic.publish(topicId,selection);
				});
		});
	},
	
	/******************************************/
	
	showDeleteDialog = function(service){
		var F = _module + ".showDeleteDialog()";
		console.debug(F, service);
		
		var dlg = registry.byId("deleteServiceDialog"),
			widget = registry.byId("deleteServiceForm"),
			url,svc,xhrArgs;
		
		if(!dlg){
			dlg = new Dialog({
				id:"deleteServiceDialog",
			title:nls.dialog.deleteService});
			widget = new DeleteServiceForm({id:"deleteServiceForm"});
			dlg.setContent(widget);
			dlg.startup();
		
			// when someone clicks on the cancel button of the delete service dialog
			aspect.after(widget, "onCancel", function(){
				dlg.hide();
				widget.reset();
			});
			
			// when someone clicks on the delete button of the delete service dialog
			aspect.after(widget, "onDelete", function(){
				dlg.hide();
				deleteService();
			});
		}
		
		// We only support deletion of one service at a time. Check this is
		// a one-element array, then convert to just the element.
		if (service.length < 1) {
			dialogs.showDialog("ERROR_NO_SERVICE_SELECTED");
			return;
		} 
		if (service.length > 1) {
			dialogs.showDialog("ERROR_MULTIPLE_SERVICES_SELECTED");
			return;
		} 
		
		svc = service[0];
		
		url = string.substitute(Resources.serviceOrphans.url, svc);
		
		xhrArgs = {
				method: Resources.serviceOrphans.list.method,
				handleAs: "json",
				headers: {
					Accept:"application/javascript, application/json"
				}
		};
		
		xhr(url,xhrArgs).then(function(orphans){
			console.debug("Orphans: ", orphans);
			svc.orphans = orphans;
			registry.byId("deleteServiceForm").populate(svc);
			
			if(svc.orphans) {
				registry.byId("deleteServiceDialog").show();
			} else { // empty orphans array
				dialogs.showDialog("CONFIRM_DELETE_SERVICE_NO_CHILDREN", svc, deleteService);
			}
		});
	},
	
	showDeployConfigurationDialog = function(){
		var grid = registry.byId("serviceGrid");
			dlg = registry.byId("serviceConfigurationDialog"),
			widget = registry.byId("serviceConfigurationWizard");
			
		if(!dlg){
			dlg = new Dialog({
				id:"serviceConfigurationDialog",
			title:nls.dialog.deployServiceConfiguration});
			widget = new ServiceConfigurationWizard({id:"serviceConfigurationWizard"});
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
				deployConfiguration(config);
			});
		}
		
		gridUtil.withSelectedData(grid,function(selection){
			var m = _serviceConfigurationModel(selection);
			widget.reset();
			widget.set("model",m);
			dlg.show();
		});
	},
	
	/******************************************/
	
	confirmQuiesceService = function(){
		var grid = registry.byId("serviceGrid"),
			services = grid.select.row.getSelected();
		
		switch(services.length){
			case 0:
				dialogs.showDialog("ERROR_NO_SERVICE_SELECTED");
				break;
			case 1:
				var service = grid.row(services[0]).data();
				dialogs.showDialog("CONFIRM_QUIESCE_SERVICE",{name:service.name,classDisplayName:service.type},quiesce);
				break;
			default:
				dialogs.showDialog("CONFIRM_QUIESCE_SERVICES",{quantity:services.length},quiesce);
				break;
		}
	},
	
	confirmUnquiesceService = function(){
		var grid = registry.byId("serviceGrid"),
			services = grid.select.row.getSelected();
		
		switch(services.length){
			case 0:
				dialogs.showDialog("ERROR_NO_SERVICE_SELECTED");
				break;
			case 1:
				var service = grid.row(services[0]).data();
				dialogs.showDialog("CONFIRM_UNQUIESCE_SERVICE",{name:service.name,classDisplayName:service.type},unquiesce);
				break;
			default:
				dialogs.showDialog("CONFIRM_UNQUIESCE_SERVICES",{quantity:services.length},unquiesce);
				break;
		}
	},
	
	/******************************************/
	
	Services = declare(_ContentController,{
		
		initUI:function(){
			// summary:
			//		Build the services page UI
			var container = new BorderContainer({
					//"class":"wamcPageContainer"
				},"serviceContainer"),
				gridPane = new BorderContainer({
						region:"center",
						gutters:false,
						'aria-label':nls.regions.services.grid,
						role:"region"},
					"serviceGridPane"),
				gridToolbar = new ServiceToolbar({
						region:"top",context:"services",grid:"serviceGrid"},
					"serviceGridToolbar"),
				grid = new Grid({
						region:"center",
						store:stores.get("service"),
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
					structure: [{id:'name', name: nls.serviceGrid.name, field: 'name', dataType: 'string', width: '20%' },
								{id:'type', name: nls.serviceGrid.type, field: 'classDisplayName', dataType: 'string', width: '20%' },
								{id:'domainName', name: nls.serviceGrid.domain, field: 'domainName', dataType: 'string', width: '20%' },
								{id:'applianceName', name: nls.serviceGrid.appliance, field: 'applianceName', dataType: 'string', width: '20%' },
								{id:'status', name: nls.serviceGrid.status, field: 'status', width: '10%', dataType: 'string', decorator:wamc.grid.format.decorateServiceGridStatus}],
					"aria-label":nls.serviceGrid.title,
					"class":"compact"},
					"serviceGrid"),
				detailPane = new ContentPane({
						region:"trailing",
						"aria-label":nls.regions.services.properties,
						role:"region",
						"class":"wamcPropertiesPanel"},"serviceDetailPane"),
				detailToolbar = new PropertiesToolbar({context:"services",master:"serviceGrid"},
						"serviceDetailToolbar"),
				detail = new ServiceDetail({viewOnly:true,master:"serviceGrid"},
						"serviceDetail");
			
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
			
			var grid = registry.byId("serviceGrid"),
				gridToolbar = registry.byId("serviceGridToolbar"),
				detailToolbar = registry.byId("serviceDetailToolbar"), 
				detail = registry.byId("serviceDetail");
			
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
			
			aspect.after(gridToolbar,"viewDomain",function(evt){
				viewDomain();
			});
			
			// when someone clicks on the delete button, display the confirmation dialog
			aspect.after(gridToolbar,"deleteService",function(evt){
				gridUtil.withSelectedData(grid,showDeleteDialog);
			});
			
			aspect.after(gridToolbar,"quiesce",function(evt){
				confirmQuiesceService();
			});
			
			aspect.after(gridToolbar,"unquiesce",function(evt){
				confirmUnquiesceService();
			});
			
			aspect.after(gridToolbar,"deployConfiguration",function(evt){
				showDeployConfigurationDialog();
			});
			
			_selectionChanged(grid,[]);
		},
		
		init:function(){
			this.initUI();
			this.setupEvents();
			this.inherited(arguments);
		},
		
		setFilter:function(/*Object*/filter){
			var grid = registry.byId("serviceGrid");
			
			this._filter = filter;
			
			grid && grid.filterBar.applyFilter(filter);
		},
		
		refreshContent:function(){
			var grid;
			if(this.isLoaded()){
				grid= registry.byId("serviceGrid");
				gridUtil.refreshGrid(grid).then(function(){
					gridUtil.selectDefault(grid);
				});
			}
		}
		
	});
	
	return new Services();
});
