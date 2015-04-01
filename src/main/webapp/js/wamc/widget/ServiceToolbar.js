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
        "./_ActionToolbar",
        "wamc/_CapabilityMixin"],
	function(array,declare,topic,_ActionToolbar,_CapabilityMixin){
	
	var ServiceToolbar = declare("wamc.widget.ServiceToolbar",[_ActionToolbar,_CapabilityMixin],{
	
		grid:"",
		
		startup:function(){
			// summary:
			//		Lifecycle method, called when widget is started
			// tags:
			//		override
			var F = this.declaredClass + ".startup(): ";
			console.debug(F);
			
			this.inherited(arguments);
			
			var toolbar = this,
				g = this.grid,
				gridId = typeof g === "string" ? g : g.id,
				topicId = gridId + "_selectionChanged";
			
			topic.subscribe(topicId, function(newSelection){
				toolbar.setActionState(toolbar._calculateActionState(newSelection));
			});
			
		},
		
		viewDomain:function(){
			// summary:
			//		Called when the user clicks the View Domain button
			// tags:
			//		callback
			
			var F = this.declaredClass + ".viewDomain(): ";
			console.debug(F);
		},
		
		deleteService: function(){
			// summary:
			//		Called when the user clicks the delete button
			// tags:
			//		callback
			var F = this.declaredClass + ".deleteService(): ";
			console.debug(F);			
		},
		
		deployConfiguration: function(){
			// summary:
			//		Called when the user clicks the Deploy Configuration button
			// tags:
			//		callback
			var F = this.declaredClass + ".deployConfiguration(): ";
			console.debug(F);			
		},
		
		quiesce: function(){
			// summary:
			//		Called when the user clicks the Quiesce button
			// tags:
			//		callback
			var F = this.declaredClass + ".quiesce(): ";
			console.debug(F);			
		},
		
		unquiesce: function(){
			// summary:
			//		Called when the user clicks the Unquiesce button
			// tags:
			//		callback
			var F = this.declaredClass + ".unquiesce(): ";
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
			
			selection = array.filter(selection,function(item){return item !== null;});
			
			var result = {};
			
			if(selection.length === 0){
				result = {
						viewDomain:"disabled",
						quiesce: "disabled",
						unquiesce:"disabled",
						deleteService: "disabled",
						deployConfiguration: "disabled"
						};
			}else{
			
				var sameName=true, sameClass=true, lastName=null, lastClass=null, 
					allUp=true, allDown=true;
				
				array.forEach(selection,function(service){
					
					if(service.status.toLowerCase()!="up"){
						allUp=false;
					}
					if(service.status.toLowerCase()!="down"){
						allDown=false;
					}
					
					if(lastName && lastName !== service.name){
						sameName=false;
					}
					if(lastClass && lastClass !== service.serviceClass){
						sameClass=false;
					}
					
					lastName = service.name;
					lastClass = service.serviceClass;
				});
				
				result = {viewDomain:(selection.length===1) ? "enabled" : "disabled",
					quiesce: allDown ? "disabled" : "enabled",
					unquiesce: allUp ? "disabled" : "enabled",
					deleteService:(selection.length===1) ? "enabled" : "disabled",
					deployConfiguration:(sameName && sameClass) ? "enabled" : "disabled"};
			
			}
			var applianceCapabilities = array.map(selection, function(service){
				return {capabilities: service.applianceCapabilities};
			});

			result = this._applyCapabilities(result, applianceCapabilities);
			
			return result;
		}
		
	});
	
	return ServiceToolbar;
	
});
