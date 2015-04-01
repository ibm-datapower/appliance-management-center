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
        "dojo/Deferred",
        "dojo/promise/all",
        "dojo/topic",
        "dojo/when",
        "dijit/registry"],
		function(array,declare,lang,Deferred,all,topic,when,registry){
	
	var refreshGrid = function(/*gridx/Grid*/grid){
		// summary:
		//		Refresh a grid. If the grid uses the filter plugin, use
		//		use that. Otherwise just refresh the body.
		// grid: gridx/Grid
		//		The grid to refresh
		// return: Deferred
		//		A deferred that resolves when the refresh is complete
		var result;
		
		console.debug("wamc.masterDetail.refreshGrid()");
		
		if(grid.filter){
			result = grid.filter.refresh();
		}else{
			grid.model.clearCache();
			result = grid.body.refresh();
		}
		
		return result;
	},
	
	refreshFilterBar = function(/*gridx/Grid*/grid){
		// summary:
		//		Refresh the filterbar of a grid
		// grid: gridx/Grid
		//		The grid to refresh

		var fb = grid.filterBar;
		
		if(!fb) return;
		
		grid.when().then(function(){
			fb.refresh();
		});
	},
	
	withSelectedData = function(/*gridx/Grid*/grid,/*Callback*/callback, /*Callback*/errBack){
		// summary:
		//		Perform an action with the data currently selected in the grid
		// grid: gridx/Grid
		//		The grid to get the selected data for
		// callback: Callback
		//		The function to call once the selected data has been retrieved.
		//		Function match the signature of Deferred.when();
		// errBack: Callback
		//		The function to call if the retrieval fails
		// return: Deferred
		//		A deferred that resolves when the callback has completed
		return withSelected(grid,
				function(id){
					return grid.store.get(id);
				},callback,errBack);
	},
	
	withSelectedRows = function(/*gridx/Grid*/grid, /*Callback*/callback, /*Callback*/errBack){
		// summary:
		//		Perform an action with the data of rows currently selected in the grid.
		// description:
		//		This function differs from withSelectedData in that only the data held in the grid model
		//		is passed to the callback. No rows are retrieved from the store,
		//		which means less network traffic in the case of a JsonRest store.
		//		The disadvantage is that only columns in the grid are passed to the function.
		// grid: gridx/Grid
		//		The grid to get the selected data for		
		// callback: Callback
		//		The function to call once the selected data has been retrieved.
		//		Function match the signature of Deferred.when();
		// errBack: Callback
		//		The function to call if the retrieval fails
		// return: Deferred
		//		A deferred that resolves when the callback has completed
		return withSelected(grid,function(id){
					var def = new Deferred();
					def.resolve(grid.row(id).data());
					return def;
				},callback,errBack);
	},
	
	withSelected = function(/*gridx/Grid*/grid, /*function*/getSelected,/*Callback*/callback, /*Callback*/errBack){
		// summary:
		//		Perform an action with the data currently selected in the grid
		// getSelected: Function
		//		A function that accepts a single string parameter, and returns a row matching that identifier.
		// grid: gridx/Grid
		//		The grid to get the selected data for
		// callback: Callback
		//		The function to call once the selected data has been retrieved.
		//		Function match the signature of Deferred.when();
		// errBack: Callback
		//		The function to call if the retrieval fails
		// return: Deferred
		//		A deferred that resolves when the callback has completed
		
		var ids = grid.select.row.getSelected(),
			defs = [], def=null;
		
		// First, get the data for each item from the store
		array.forEach(ids,function(id){
			defs.push(when(getSelected(id)));
		},this);
		
		if(defs.length > 0){
			// If any deferreds have been added to the list, create a DeferredList
			def = all(defs);
		}else{
			//Otherwise create a deferred that returns an empty list of results
			def = new Deferred();
			def.resolve([]);
		}
		def.then(callback,errBack);
	},
	
	publishSelection = function(/*gridx/Grid*/grid){
		// summary:
		//		Publish the items currently selected in the grid.
		//		The topic will be the id of the grid widget 
		//		plus "_selectionChanged"
		// grid: gridx/Grid
		//		The grid to publish the selection of
		var F = "wamc.grid.util.publishSelection()";
		console.debug(F,grid);
		
		return grid.when().then(function(){
			return withSelectedData(grid,function(selection){
				topic.publish(grid.id + "_selectionChanged",selection);
			});
		});
		
	},
	
	fixSelection = function(/*gridx/Grid*/grid){
		// summary:
		//		Correct the current selection in a grid by making sure that the 
		//		currently selected rows still exist and are selectable
		// return: Array
		//		an array containing the ids of the current selection
		var F = "wamc.masterDetail.fixSelection()";
		console.debug(F,grid);
		
		var selection;
		
		// First, make sure that the grid has row select
		if(!grid || !grid.select || !grid.select.row) return;
		
		selection = grid.select.row.getSelected();
		
		array.forEach(selection,function(selected){
			var row = grid.row(selected);
			if(!row){
				grid.select.row.deselectById(selected);
			}
		});
		
		return grid.select.row.getSelected();
		
	},
	
	selectDefault = function(/*gridx/Grid*/grid, /*Array*/newSelectedIds,/*Array*/oldSelectedIds){
		// summary:
		//		Perform default selection on the grid, making sure that 
		//		a record is always selected.
		// description:
		//		If newIds are passed and some can be selected, select them again
		//		Else, if a record was previously selected and still exists, select that
		//		Else, if the grid has at least one row, select the first
		//		Else, leave it
		// grid: gridx/Grid
		//		The grid to select in
		// return: Array
		//		The new array of selected ids
		// tags:
		//		protected
		var F = "wamc.masterDetail.selectDefault()";
		console.debug(F,grid,newSelectedIds,oldSelectedIds);
		
		var newSelection = [],
			selectIds = function(ids,byId){
				var i,id,row;
				for(i=0;i<ids.length;i++){
					id = ids[i];
					row = grid.row(id,!!byId);
					if(row){
						row.select();
						row.isSelected() && newSelection.push(row.id);
					}
				}
			};
		
		newSelection = fixSelection(grid);
		
		if(newSelection.length === 0 && (lang.isArray(oldSelectedIds) &&
				oldSelectedIds.length > 0)){
			console.debug(F,"Selecting old ids");
			selectIds(oldSelectedIds);
		}
		
		console.debug(F,"After trying to select old ids",newSelection);
		
		if(newSelection.length === 0 && grid.rowCount() > 0){
			console.debug(F,"Selecting first row");
			selectIds([0],false);
		}
		
		return newSelection;
	};
	
	return {
		refreshGrid:refreshGrid,
		refreshFilterBar:refreshFilterBar,
		withSelectedData:withSelectedData,
		withSelectedRows:withSelectedRows,
		withSelected:withSelected,
		publishSelection:publishSelection,
		selectDefault:selectDefault,
		fixSelection:fixSelection
	};

});
