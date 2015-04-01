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
        "dojo/_base/lang",
        "dojo/aspect",
        "dojo/promise/all",
        "dojo/topic",
        "dojo/when",
        "dijit/Dialog",
        "dijit/layout/BorderContainer",
        "dijit/layout/ContentPane",
        "dijit/registry",
        "gridx/Grid",
        "gridx/core/model/cache/Async",
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
        "wamc/widget/RepositoryToolbar",
        "wamc/widget/form/AddFirmwareForm",
        "wamc/widget/form/EditFirmwareDetail",
        "wamc/widget/form/FirmwareDetail",
        "dojo/i18n!wamc/nls/strings"],
		function(array,declare,lang,aspect,all,topic,when,Dialog,BorderContainer,ContentPane,registry,Grid,Async,dialogs,messages,stores,standby,Resources,_ContentController,masterDetail,gridFormat,gridModules,gridUtil,PropertiesToolbar,RepositoryToolbar,AddFirmwareForm,EditFirmwareDetail,FirmwareDetail,nls){
	
	var _module = "wamc.controller.repository",
	
	removeFirmware = function(/*String*/primaryKey,/*String*/userComment){
		var F = _module + ".removeFirmware";
		console.debug(F,primaryKey,userComment);
		
		var msg = userComment? "SUCCESS_REMOVE_FIRMWARE_COMMENT" : "SUCCESS_REMOVE_FIRMWARE";
		
		var def = stores.get("firmware").remove(primaryKey);
		
		when(def,function(result){
			console.debug(F,"Firmware " + primaryKey + " removed");
			messages.showMessage(msg,{"comment":userComment});
			return result;
		},function(error){
			console.log(F,"Error while removing firmware " + primaryKey);
			dialogs.showServerResponse(error.xhr.responseText);
			return error;
		});
		
		return def;
	},
	
	removeSelectedArtefacts = function(){
		// summary:
		//		Remove any artefacts selected in the grid
		var F = _module + ".removeSelectedArtefacts()";
		console.debug(F);

		var dl = [],
			grid = registry.byId("repositoryGrid"),
			selection = grid.select.row.getSelected();
		
		array.forEach(selection,function(primaryKey){
			var data = grid.row(primaryKey).data(),
				userComment = data ? data.userComment : null;
			dl.push(removeFirmware(primaryKey,userComment));		
		},this);
		
		all(dl).then(function(result){
			grid.select.row.clear();
		},function(error){
			grid.select.row.clear();
		});
	},
	
	editFirmware = function(firmware){
		var F = _module + ".editFirmware()";
		console.debug(F,firmware);
		
		return stores.get("firmware").put(firmware,{overwrite:true}).then(function(result){
			console.debug(F,"Firmware " + firmware.userComment + " updated");
			if(firmware.userComment){
				messages.showMessage("SUCCESS_UPDATE_FIRMWARE_COMMENT",{"comment":firmware.userComment});
			}else{
				messages.showMessage("SUCCESS_UPDATE_FIRMWARE");
			}
		});
		
	},
	
	/*************************************/
	
	showAddFirmwareDialog = function(){
		var dlg = registry.byId("addFirmwareDialog"),
			widget = registry.byId("addFirmwareForm");

		if(!dlg){
			dlg = new Dialog({
				id:"addFirmwareDialog",
				title:nls.dialog.add_firmware});
			widget = new AddFirmwareForm({id:"addFirmwareForm"});
			dlg.setContent(widget);
			dlg.startup();
			
			// When the AddFirmwareForm closes, hide the dialog
			aspect.after(widget,"onDone",function(arg){
				dlg.hide();
			});
			
			// when someone clicks on the cancel button of an update appliance management properties form
			aspect.after(widget, "onCancel", function(){
				dlg.hide();
			});
		}
		
		widget.reset();
		dlg.show();
	},
	
	showEditFirmwareDialog = function(){
		var grid = registry.byId("repositoryGrid"),
			dlg = registry.byId("editFirmwareDialog"),
			widget = registry.byId("editFirmwareForm");

		if(!dlg){
			dlg = new Dialog({
				id:"editFirmwareDialog",
				title:nls.dialog.add_firmware});
			widget = new EditFirmwareDetail({id:"editFirmwareForm"});
			dlg.setContent(widget);
			dlg.startup();
			
			// Setup events
			aspect.after(widget,"onSubmit",function(arg){
				var model = widget.get("model");
				
				editFirmware(model).then(function(result){
					console.debug("FOO!!!");
					dlg.hide();
					gridUtil.publishSelection(grid);
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
	
	/*************************************/
	
	confirmRemoveFirmware = function(){
		var grid = registry.byId("repositoryGrid"),
			artefacts = grid.select.row.getSelected();
		
		switch(artefacts.length){
			case 0:
				dialogs.showDialog("ERROR_NO_ARTEFACT_SELECTED");
				break;
			case 1:
				dialogs.showDialog("CONFIRM_REMOVE_FIRMWARE",null,removeSelectedArtefacts);
				break;
			default:
				dialogs.showDialog("CONFIRM_REMOVE_FIRMWARES",{quantity:artefacts.length},removeSelectedArtefacts);
				break;
		}
	},
	
	/*************************************/
	

	
	Repository = declare(_ContentController,{
		
		initUI:function(){
			// summary:
			//		Build the repository page UI
			var container = new BorderContainer({
					//"class":"wamcPageContainer"
				},"repositoryContainer"),
				gridPane = new BorderContainer({
						region:"center",
						gutters:false,
						'aria-label':nls.regions.repository.grid,
						role:"region"},
					"repositoryGridPane"),
				gridToolbar = new RepositoryToolbar({
						region:"top",context:"repository",grid:"repositoryGrid"},
					"repositoryGridToolbar"),
				grid = new Grid({
						region:"center",
						store:stores.get("firmware"),
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
								{id: 'applianceType', name: nls.repositoryGrid.applianceType,   field: 'applianceType', dataType: 'string', width: 'auto'},
								{id: 'modelType',     name: nls.repositoryGrid.modelType,       field: 'modelType',     dataType: 'string', width: 'auto'},
								{id: 'level',         name: nls.repositoryGrid.firmwareVersion, field: 'level',         dataType: 'string', width: 'auto'},
								{id: 'userComment',   name: nls.repositoryGrid.userComment,     field: 'userComment',   dataType: 'string', width: 'auto'}],
					"aria-label":nls.repositoryGrid.title,
					"class":"compact"},
					"repositoryGrid"),
				detailPane = new ContentPane({
						region:"trailing",
						"aria-label":nls.regions.repository.properties,
						role:"region",
						"class":"wamcPropertiesPanel"},"repositoryDetailPane"),
				detailToolbar = new PropertiesToolbar({context:"repository",master:"repositoryGrid"},
						"repositoryDetailToolbar"),
				detail = new FirmwareDetail({viewOnly:true,master:"repositoryGrid"},
						"repositoryDetail");
			
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
			
			var grid = registry.byId("repositoryGrid"),
				gridToolbar = registry.byId("repositoryGridToolbar"),
				detailToolbar = registry.byId("repositoryDetailToolbar"), 
				detail = registry.byId("repositoryDetail");
			
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
						HoverHelpTooltip.show(content,evt.cellNode,["below","after"]);	
					}
				}
			},true);
			
			// hide hover help for the groups column
			aspect.after(grid, "onCellMouseOut",function(evt){
				if (evt.columnId === "groups"){
					HoverHelpTooltip.hide(evt.cellNode);
				}
			},true);
			
			// ActionStatus refreshes
			topic.subscribe("actionStatus",
					lang.partial(masterDetail.publishActionStatus,grid));
			
			// When addArtefact is clicked in the toolbar, show the addArtefactForm widget
			aspect.after(gridToolbar,"addArtefact",function(evt){
				showAddFirmwareDialog();
			});
			
			// When removeArtefact is clicked in the toolbar, remove the artefact from the store
			aspect.after(gridToolbar,"removeArtefact",function(evt){
				confirmRemoveFirmware();
			});
			
			// when the edit properties button is pressed, display the edit dialog
			aspect.after(detailToolbar,"editProperties",function(evt){
				showEditFirmwareDialog();
			});
			
			// Finally, fire a selection event to configure the toolbars
			masterDetail.selectionChanged(grid,[]);
		},
		
		init:function(){
			this.initUI();
			this.setupEvents();
			this.inherited(arguments);
		},
		
		setFilter:function(/*Object*/filter){
			var grid = registry.byId("repositoryGrid");
			
			this._filter = filter;
			
			grid && grid.filterBar.applyFilter(filter);
		},
		
		refreshContent:function(){
			var grid;
			if(this.isLoaded()){
				grid= registry.byId("repositoryGrid");
				gridUtil.refreshGrid(grid).then(function(){
					gridUtil.selectDefault(grid);
				});
			}
		}
	});
	
	return new Repository();
});
