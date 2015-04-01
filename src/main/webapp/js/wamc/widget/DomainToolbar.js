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
	
	var DomainToolbar = declare("wamc.widget.DomainToolbar",[_ActionToolbar,_CapabilityMixin],{
	
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
		
		assignGroups:function(){
			// summary:
			//		Called when the user clicks the Assign Groups button
			// tags:
			//		callback

			var F = this.declaredClass + ".assignGroups(): ";
			console.debug(F);
		},
		
		viewAppliances:function(){
			// summary:
			//		Called when the user clicks the View Appliances button
			// tags:
			//		callback
			
			var F = this.declaredClass + ".viewAppliances(): ";
			console.debug(F);
		},
		
		viewServices:function(){
			// summary:
			//		Called when the user clicks the View Services button
			// tags:
			//		callback
			
			var F = this.declaredClass + ".viewServices(): ";
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
		
		restart: function(){
			// summary:
			//		Called when the user clicks the Restart button
			// tags:
			//		callback
			var F = this.declaredClass + ".restart(): ";
			console.debug(F);			
		},
		
		deleteDomain: function(){
			// summary:
			//		Called when the user clicks the delete button
			// tags:
			//		callback
			var F = this.declaredClass + ".deleteDomain(): ";
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
		
		createService: function(){
			// summary:
			//		Called when the user clicks the Create Service button
			// tags:
			//		callback
			var F = this.declaredClass + ".createService(): ";
			console.debug(F);			
		},
		
		uploadFile: function(){
			// summary:
			//		Called when the user clicks the Upload File button
			// tags:
			//		callback
			var F = this.declaredClass + ".uploadFile(): ";
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
			
			if(selection.length===0){
				result = {assignGroups:"disabled",
						viewAppliances:"disabled",
						viewServices:"disabled",
						quiesce:"disabled",
						unquiesce:"disabled",
						deleteDomain:"disabled",
						deployConfiguration:"disabled",
						createService:"disabled",
						uploadFile:"disabled",
						restart:"disabled"};
			}else{
				
				var allUp=true, allDown=true, allDefault=true, sameName=true,
				lastName=null;
				
				array.forEach(selection,function(domain){
					if(domain.status.toLowerCase()!="up"){
						allUp=false;
					}
					if(domain.status.toLowerCase()!="down"){
						allDown=false;
					}
					if(domain.displayName.toLowerCase()!="default"){
						allDefault=false;
					}
					if(lastName && lastName !== domain.name){
						sameName=false;
					}
					lastName = domain.name;
				});
				
				result = {assignGroups:"enabled",
						viewAppliances: "enabled",
						viewServices: selection.length === 1 ? "enabled" : "disabled",
						quiesce: allDown ? "disabled" : "enabled",
						unquiesce: allUp ? "disabled" : "enabled",
						deleteDomain: allDefault ? "disabled" : "enabled",
						deployConfiguration : sameName ? "enabled" : "disabled",
						createService: "enabled",
						uploadFile: "enabled",
						restart: "enabled"
				};
			}
			
			var applianceCapabilities = array.map(selection, function(domain){
				return {capabilities: domain.applianceCapabilities};
			});

			result = this._applyCapabilities(result, applianceCapabilities);
			
			return result;
		}
		
	});
	
	return DomainToolbar;
	
});
