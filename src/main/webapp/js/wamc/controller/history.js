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
define(["dojo/_base/declare",
        "dojo/_base/lang",
        "dojo/aspect",
        "dojo/dom",
        "dojo/dom-construct",
        "dojo/NodeList-html",
        "dojo/topic",
        "dijit/layout/BorderContainer",
        "dijit/layout/ContentPane",
        "dijit/registry",
        "gridx/Grid",
        "gridx/core/model/cache/Async",
        "wamc/config/Resources",
        "wamc/controller/_ContentController",
        "wamc/controller/master-detail",
        "wamc/grid/format",
        "wamc/grid/modules",
        "wamc/grid/util",
        "wamc/stores",
        "wamc/widget/ActionDetail",
        "dojo/i18n!wamc/nls/strings"],
		function(declare,lang,aspect,dom,domConstruct,html,topic,BorderContainer,ContentPane,registry,Grid,Async,Resources,_ContentController,masterDetail,gridFormat,gridModules,gridUtil,stores,ActionDetail,nls){
	
	var _module = "wamc.controller.history",

	History = declare(_ContentController,{
		
		initUI:function(){
			// summary:
			//		Build the history page UI
			var container = new BorderContainer({
					//"class":"wamcPageContainer"
				},"actionContainer"),
				gridPane = new BorderContainer({
						region:"center",
						gutters:false,
						'aria-label':nls.regions.history.grid,
						role:"region"},
					"actionGridPane"),
				grid = new Grid({
						region:"center",
						store:stores.get("action"),
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
								{ id: 'state', name: nls.actionGrid.state, field: 'state', dataType: 'string', width: '8%', decorator:wamc.grid.format.decorateActionState},
								{ id: 'description', name: nls.actionGrid.description, field: 'description', dataType: 'string', width: 'auto'},
								{ id: 'userId', name: nls.actionGrid.userId, field: 'userId', dataType: 'string', width: '12%'},
								{ id: 'submitted', name: nls.actionGrid.submitted, field: 'submitted', dataType: 'datetime', width: '20%', decorator:wamc.grid.format.decorateDateTimeNoMillis},
								{ id: 'updated', name: nls.actionGrid.updated, field: 'updated', dataType: 'datetime', width: '20%', decorator:wamc.grid.format.decorateDateTimeNoMillis}],
					"aria-label":nls.actionGrid.title,
					"class":"compact"},
					"actionGrid"),
				detailPane = new ContentPane({
						region:"trailing",
						"aria-label":nls.regions.history.properties,
						role:"region",
						"class":"wamcPropertiesPanel"},"actionDetailPane"),
				detailToolbar = dom.byId("actionDetailToolbar"),
				detail = new ActionDetail({viewOnly:true,master:"actionGrid"},
						"actionDetail");
			
			grid.onModulesLoaded = function(){
				gridUtil.refreshFilterBar(grid);
				masterDetail.filterChanged(grid);
			};
			
			gridPane.addChild(grid);
			
			container.addChild(gridPane);
			container.addChild(detailPane);
			
			// Start everything
			container.startup();
			
			domConstruct.create("h2",{innerHTML:nls.actionDetail.label},detailToolbar,"first");
		},
		
		setupEvents:function(){
			// summary:
			//		Connect widgets for the appliances tab
			
			var grid = registry.byId("actionGrid"),
				detail = registry.byId("actionDetail");
			
			// Any time the table selection changes, update the available actions
			aspect.after(grid.select.row,"onSelectionChange",
					lang.partial(masterDetail.selectionChanged,grid),true);
			
			// Whenever a grid filter is applied, make sure a visible row is selected
			aspect.after(grid.filter,"setFilter",
					lang.partial(masterDetail.filterChanged,grid));
			
			// When action status is received, update the action in the grid and detail panel
			topic.subscribe("actionStatus",function(msg){
				
				gridUtil.refreshGrid(grid);
				
				displayedStatus = detail.get("actionStatus");
				
				if(displayedStatus && displayedStatus.actionId == msg.actionId){
					detail.set("actionStatus",msg);
				}
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
			var grid = registry.byId("actionGrid");
			
			this._filter = filter;
			
			grid && grid.filterBar.applyFilter(filter);
		},
		
		
		refreshContent:function(){
			var grid;
			if(this.isLoaded()){
				grid= registry.byId("actionGrid");
				gridUtil.refreshGrid(grid).then(function(){
					gridUtil.selectDefault(grid);
				});
			}
		}
	});
	
	return new History();
});
