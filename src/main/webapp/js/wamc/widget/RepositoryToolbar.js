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
        "dojo/topic",
        "./_ActionToolbar"],
	function(array,declare,topic,_ActionToolbar){
	
	var RepositoryToolbar = declare("wamc.widget.RepositoryToolbar",[_ActionToolbar],{
	
		grid:"",
		
		startup:function(){
			// summary:
			//		Lifecycle method, called when widget is started
			// tags:
			//		override
			var F = this.declaredClass + ".startup(): ";
			console.debug(F);
			
			var toolbar = this,
				g = this.grid,
				gridId = typeof g === "string" ? g : g.id,
				topicId = gridId + "_selectionChanged";
			
			topic.subscribe(topicId, function(newSelection){
				toolbar.setActionState(toolbar._calculateActionState(newSelection));
			});
		},
		
		addArtefact:function(){
			// summary:
			//		Called when the user clicks the Add Appliance button
			// tags:
			//		callback
			
			var F = this.declaredClass + ".addArtefact(): ";
			console.debug(F);
		},
		
		removeArtefact:function(){
			// summary:
			//		Called when the user clicks the Remove Appliance button
			// tags:
			//		callback

			var F = this.declaredClass + ".removeArtefact(): ";
			console.debug(F);
		},
		
		_calculateActionState:function(/*Array*/selection){
			// summary:
			//		Calculates the state of toolbar actions based an array of selected objects
			// selection: array
			//		The new object selection
			// return: Object
			//		The set of enablement states for buttons
			//		e.g. {addArtefact: "enabled",removeArtefact: "disabled"}
			var F = this.declaredClass + "._calculateActionState(): ";
			console.debug(F,selection);
			
			var result = {};
			
			selection = array.filter(selection,function(item){return item !== null;});
			
			if(selection.length===0){
				result = {addArtefact: "enabled",
						removeArtefact: "disabled"};
			}else{
				result = {
						addArtefact: "enabled",
						removeArtefact: "enabled"
				};
			}
			return result;	
		}
	});
	
	return RepositoryToolbar;
});
