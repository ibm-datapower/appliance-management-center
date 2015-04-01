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
        "dojo/_base/connect",
        "dojo/_base/declare",
        "dojo/topic",
        "./_ActionToolbar"],
	function(array,connect,declare,topic,_ActionToolbar){
	
	var PropertiesToolbar = declare("wamc.widget.PropertiesToolbar",[_ActionToolbar],{
		
		master:"",
	
		startup: function(){
			this.inherited(arguments);	
			
			var toolbar = this,
				m = this.master,
				masterId = typeof m === "string" ? m : m.id,
				topicId = masterId + "_selectionChanged";
			
			topic.subscribe(topicId, function(newSelection){
				toolbar.setActionState(toolbar._calculateActionState(newSelection));
			});
		},
		
		editProperties: function(){
			// summary:
			//		Called when the user clicks the Edit Properties button
			// tags:
			//		callback
			var F = this.declaredClass + ".editProperties(): ";
			console.debug(F);
		},
		
		_calculateActionState:function(/*Array*/selection){
			// summary:
			//		Calculates the state of toolbar actions based an array of selected objects
			// selection: array
			//		The new object selection
			// return: Object
			//		The set of enablement states for buttons
			//		e.g. {addAppliance:"enabled",removeAppliance:"disabled"}
			var F = this.declaredClass + "._calculateActionState(): ";
			console.debug(F,selection);
			
			var result = {};
			
			selection = array.filter(selection,function(item){return item !== null;});

			if(selection.length === 1){
				result = {"editProperties":"enabled"};
			}else{
				result = {"editProperties":"disabled"};
			}
			
			return result;
		}
	});
	
	return PropertiesToolbar;
});
