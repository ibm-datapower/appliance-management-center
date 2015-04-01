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
        "dojo/_base/array",
        "dojo/_base/json",
        "wamc/config/Capabilities"],
	function(declare,array,json,Capabilities){
	
	var _CapabilityMixin = declare("wamc._CapabilityMixin",null,{		
		
		_applyCapabilities:function(/*map*/result, /*Appliance[]*/ appliances) {
			// summary:
			//		Using the capabilities of one or more appliances (typically the
			// 		appliances either selected in a main page grid, or hosting the
			// 		objects selected in such a grid), modify an enabled/disabled map
			// 		so that actions not supported by all the selected appliances are
			// 		forced to "disabled".
			// result:
			//		The set of enablement states for buttons
			//		e.g. {addAppliance:"enabled",removeAppliance:"disabled"}			
			//		with the enablement state as calculated so far, according
			//		to role permissions, number of objects selected, etc. This 
			//		method will not re-enable anything, but will disable actions
			// 		that the selected appliances don't support.
			//		This map must contain all the buttons to be modified; no new
			//		entries will be added.
			// appliances: 
			//		A list of objects representing the appliances involved, which
			//		must at minimum have a "capabilities" parameter indicating
			//		which capabilities are supported. If this is missing, that 
			// 		appliance is not used in the decision to disable anything.
			// return: 
			//		The set of enablement states for buttons
			//		e.g. {addAppliance:"enabled",removeAppliance:"disabled"} ,
			//		modified to account for appliance capability.
			var F = this.declaredClass + "._applyCapabilities(): ";
			console.debug(F, json.fromJson(json.toJson(result)), appliances);
			
			if(appliances == null || appliances.length === 0) return result;
			
			var localCapabilityMap = this.context ? 
										Capabilities[this.context][this.id] 
										: Capabilities[this.id];
										
			for(var button in result){ if(result.hasOwnProperty(button)){ // like doing foreach on a Java map
				var capability = localCapabilityMap[button];
				console.debug("Button: ", button, ", required capability: ", capability);
				if(capability == null) continue;
				
				// "If some appliances are missing the capability..."
				if(array.some(appliances,function(appliance){
					if(appliance.capabilities == null) return false; // If capabilities has got lost, ignore this appliance (pretend it has the capability)
					return array.indexOf(appliance.capabilities, capability) == -1;
				})){
					result[button] = "disabled";
				}
			}}
			console.debug(F+" EXIT: ", result);
			return result;
		}
	});
	
	return _CapabilityMixin;
});
