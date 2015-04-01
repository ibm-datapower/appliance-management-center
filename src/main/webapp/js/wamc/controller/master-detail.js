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
        "dojo/_base/lang",
        "dojo/store/Cache",
        "dojo/store/Memory",
        "wamc/stores",
        "wamc/grid/util"],
		function(array,lang,Cache,Memory,stores,gridUtil){
	
	var _module = "wamc.controller.masterDetail",
	
	_applianceCache = new Cache(stores.get("appliance"),
			new Memory({idProperty: "id"})),
	
	selectionChanged = function(/*gridx/Grid*/grid, /*Array*/newSelectedIds,/*Array*/oldSelectedIds){
		// summary:
		//		Handle changes in grid row selection by attempting to select the
		//		default rows, and then publishing the current selection
		// grid: gridx/Grid
		//		The grid to publish the selection of

		var F = _module + ".selectionChanged()";
		console.debug(F,grid,newSelectedIds,oldSelectedIds);
		
		grid.when().then(function(){
			var newSelection = null;
			if(lang.isArray(newSelectedIds) &&
					newSelectedIds.length === 0){
				// If nothing is selected try and select the first row in the grid
				 newSelection = gridUtil.selectDefault(grid,newSelectedIds, oldSelectedIds);
				 
				 //if(newSelection.length > 0) return;
			}
			
			// If there was a selection, or default selection was unsuccessful
			console.debug(F,"publishing selection");
			gridUtil.publishSelection(grid);
		});
	},
	
	filterChanged = function(/*gridx/Grid*/grid){
		// summary:
		//		Handle changes in grid filter by attempting to select the
		//		default rows, and then publishing the current selection
		// grid: gridx/Grid
		//		The grid to publish the selection of

		var F = _module + ".filterChanged()";
		console.debug(F,grid);
		
		grid.when().then(function(){
			var ids = grid.select.row.getSelected();
			selectionChanged(grid,ids);
		});
	},
	
	publishActionStatus = function(grid,actionStatus){
		var F = _module + ".publishActionStatus()";
		console.debug(F,grid,actionStatus);
		
		if(actionStatus.state==="SUCCEEDED" || actionStatus.state==="FAILED"){
			gridUtil.refreshGrid(grid).then(lang.partial(gridUtil.publishSelection,grid));
		}
	},
	
	getCachedAppliance = function(/*string*/objectId){
		// summary:
		//		Get the appliance associated with a particular domain or
		//		service, via a non-expiring cache (ie, once an appliance is
		//		retrieved it is never refreshed).
		// objectId:
		//		A primary key that starts "<applianceId>:"
		// return:
		//		A Deferred which resolves to the ReST Appliance associated
		//		with the given domain.
		var F = _module + ".getCachedAppliance()";
		console.debug(F, objectId);
		
		// Decomposing the domain ID is distinctly yucky, but any other 
		// approach requires XHRs to be serialised and doubles the response
		// time whenever you click on a domain - not good. 
		// _uriFromPrimaryKey started the evil :-)
		var applianceId = objectId.split(":")[0];
		
		return _applianceCache.get(applianceId);
	};
	
	return {
		selectionChanged:selectionChanged,
		filterChanged:filterChanged,
		publishActionStatus:publishActionStatus,
		getCachedAppliance:getCachedAppliance
	};

});
