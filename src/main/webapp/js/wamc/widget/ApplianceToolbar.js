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
	
	var ApplianceToolbar = declare("wamc.widget.ApplianceToolbar",[_ActionToolbar,_CapabilityMixin],{
		
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
		
		addAppliance:function(){
			// summary:
			//		Called when the user clicks the Add Appliance button
			// tags:
			//		callback
			
			var F = this.declaredClass + ".addAppliance(): ";
			console.debug(F);
		},
		
		removeAppliance:function(){
			// summary:
			//		Called when the user clicks the Remove Appliance button
			// tags:
			//		callback

			var F = this.declaredClass + ".removeAppliance(): ";
			console.debug(F);
		},
		
		viewDomains:function(){
			// summary:
			//		Called when the user clicks the View Domains button
			// tags:
			//		callback

			var F = this.declaredClass + ".viewDomains(): ";
			console.debug(F);
		},
		
		quiesce:function(){
			// summary:
			//		Called when the user clicks the View Domains button
			// tags:
			//		callback

			var F = this.declaredClass + ".quiesce(): ";
			console.debug(F);
		},
		
		unquiesce:function(){
			// summary:
			//		Called when the user clicks the View Domains button
			// tags:
			//		callback

			var F = this.declaredClass + ".unquiesce(): ";
			console.debug(F);
		},
		
		backup:function(){
			// summary:
			//		Called when the user clicks the Backup button
			// tags:
			//		callback

			var F = this.declaredClass + ".backup(): ";
			console.debug(F);
		},
		
		restore:function(){
			// summary:
			//		Called when the user clicks the Restore button
			// tags:
			//		callback

			var F = this.declaredClass + ".restore(): ";
			console.debug(F);
		},
		
		deployFirmware:function(){
			// summary:
			//		Called when the user clicks the Deploy Firmware button
			// tags:
			//		callback

			var F = this.declaredClass + ".deployFirmware(): ";
			console.debug(F);
		},
		
		createDomain:function(){
			// summary:
			//		Called when the user clicks the Create Domain button
			// tags:
			//		callback

			var F = this.declaredClass + ".createDomain(): ";
			console.debug(F);
		},
		
		reboot:function(){
			// summary:
			//		Called when the user clicks the Reboot button
			// tags:
			//		callback

			var F = this.declaredClass + ".reboot(): ";
			console.debug(F);
		},
		
		assignGroups:function(){
			// summary:
			//		Called when the user clicks the Assign Groups button
			// tags:
			//		callback

			var F = this.declaredClass + ".assignGroups(): ";
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
			
			if(selection.length===0){
				result = {addAppliance:"enabled",
						removeAppliance:"disabled",
						assignGroups:"disabled",
						viewDomains:"disabled",
						quiesce:"disabled",
						unquiesce:"disabled",
						backup:"disabled",
						restore:"disabled",
						deployFirmware:"disabled",
						createDomain:"disabled",
						reboot:"disabled"};
			}else{
				
				var allUp=true,allDown=true;
				
				array.forEach(selection,function(appliance){
					if(appliance.status.toLowerCase()!="up"){
						allUp=false;
					}
					if(appliance.status.toLowerCase()!="down"){
						allDown=false;
					}
				});
				
				result = {
						addAppliance:"enabled",
						removeAppliance:"enabled",
						assignGroups:"enabled",
						viewDomains:"enabled",
						quiesce: allDown ? "disabled" : "enabled",
						unquiesce: allUp ? "disabled" : "enabled",
						backup: (selection.length===1) ? "enabled" : "disabled",
						restore: (selection.length===1) ? "enabled" : "disabled",
						deployFirmware:"enabled",
						createDomain:"enabled",
						reboot:"enabled"};
			}
			
			result = this._applyCapabilities(result, selection);
			
			return result;	
		}
	});
	
	return ApplianceToolbar;
	
});
