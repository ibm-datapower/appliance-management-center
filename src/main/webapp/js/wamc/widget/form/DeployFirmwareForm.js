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
        "dojo/_base/json",
        "dojo/_base/lang",
        "dojo/_base/event",
        "dojo/_base/xhr",
        "dojo/Deferred",
        "dojo/dom",
        "dojo/dom-attr",
        "dojo/dom-class",
        "dojo/on",
        "dojo/promise/all",
        "dojo/string",
        "dojo/store/Memory",
        "dojo/query",	// required by tooltip selector
        "dijit/_Widget",
        "dijit/_TemplatedMixin",
        "dijit/_WidgetsInTemplateMixin",
        "dijit/registry",
        "gridx/Grid",
        "gridx/core/model/cache/Async",
        "dijit/Tooltip",
        "wamc/_MessagesMixin",
        "wamc/_HelpLabelsMixin",
		"wamc/config/Resources",
        "wamc/grid/format",
        "wamc/grid/modules",
        "wamc/util",
		"dojo/text!wamc/widget/form/templates/DeployFirmwareForm.html",
		"dojo/text!wamc/widget/form/templates/_DeployFirmwareFormDetails.html",
		"dojo/i18n!wamc/nls/strings",
		"dojo/NodeList-traverse",
		"dijit/form/Form",
		"dijit/form/RadioButton",
		"wamc/widget/form/CheckBox",
		"wamc/widget/form/Select"],
		
		function(array, declare, json, lang, event, xhr, Deferred, dom, domAttr,
				domClass, on, all, string, Memory,query, _Widget, _TemplatedMixin,
				_WidgetsInTemplateMixin, registry, Grid,Cache, HoverHelp,
				_MessagesMixin, _HelpLabelsMixin, Resources, gridFormat,
				gridModules, wamcUtil, template, detailsTemplate, nls) {

	var DeployFirmwareForm = declare("wamc.widget.form.DeployFirmwareForm",
			[_Widget,_TemplatedMixin, _WidgetsInTemplateMixin, _MessagesMixin, _HelpLabelsMixin],{	

		nls: nls,
		
		templateString: template,
		
		// appliances: Object[]
		//		Array of appliance objects to deploy firmware for
		appliances: null,
		
		// applianceFirmwareLevels: Object
		//		Map of appliance serial numbers to arrays of applicable firmware objects
		//		{"wamcdp5":{
		//			"5.0.0.1":{
		//					... firmware object ...
		//				}
		//			}
		//		}
		applianceFirmwareLevels: null,
		
		// allLevels: String[]
		//		Array of all firmware version numbers applicable to the appliances  
		allLevels: null,
		
		// upgradeLevels: String[]
		//		Array of applicable firmware version numbers that represent upgrades to at least one appliance and a downgrade to none
		upgradeLevels: null,
		
		postMixInProperties:function(){
			this.inherited(arguments);
			this.applianceFirmwareLevels = {};
			this.allLevels = [];
			this.upgradeLevels = [];
		},
		
		buildRendering:function(){
			this.inherited(arguments);
			
			this.applianceGrid = new Grid({
				store: new Memory({data: []}),
				cacheClass: Cache,
				columnWidthPercentage:true,
				columnWidthAutoResize: true,
				modules:[{moduleClass:gridModules.VScroller},
				         {moduleClass:gridModules.Focus}],
				structure: [
				            { id: "applianceName", name: nls.deployFirmware.applianceName, field: "name", dataType: "string", width: "24%"},
				            { id: "applianceType", name: nls.deployFirmware.applianceType, field: "type", dataType: "string", width: "12%"},
				            { id: "applianceModel", name: nls.deployFirmware.applianceModel, field: "model", dataType: "string", width: "12%"},
				            { id: "currentFirmware", name: nls.deployFirmware.currentFirmware, field: "current", dataType: "string", width: "26%"},
				            { id: "targetFirmware", name: nls.deployFirmware.targetFirmware, field: "target", dataType: "object",  decorator:gridFormat.decorateFirmwareAvailability, width: "26%"}],
				'aria-labelledby':this.id + "_summaryHeading"
			}).placeAt(this.firmwareApplianceTableContainer);
			
			// Create a HoverHelp for the firmware details column
			this.detailsHoverHelp = new HoverHelp({connectId:this.applianceGrid.domNode,
				selector:".firmwareAvailabilityDetails",
				getContent:lang.hitch(this,"_getFirmwareDetailsContent"),
				position:["after"]
			});

		},
		
		postCreate:function(){
			// summary:
			//		Widget life-cycle method.
			var F = this.declaredClass + ".postCreate(): ";
			console.debug(F);
			
			this.inherited(arguments);
			
			// Set up event handlers for form validation and submission
			this.own(on(this.formNode, "submit",
					lang.hitch(this, "_onSubmit")));
			
			// Setup button connections
			this.own(on(this.cancelButton, "click",
					lang.hitch(this, "_onCancel")));
			
			// When the radio button changes, update the options that are displayed
			this.own(on(this.upgradesOnlyRadioButton,"change",
					lang.hitch(this,"_toggleUpgradesOnly")));
			
			// When the version selection changes, update the summary
			this.own(on(this.versionList,"change",
					lang.hitch(this,"_versionSelectionChanged")));
			
			// When the licence check box is changed, should deploy be enabled
			this.own(on(this.licenceCheck,"change",
					lang.hitch(this,"_enableDeployButton")));
			
		},
		
		reset: function(){
			// summary:
			//		Reset the fields in this form
			var F = this.declaredClass + ".reset(): ";
			console.debug(F);
			
			// Reset the form
			this.formNode.reset();
			
			// Hide Warning
			domClass.add(this.warningNode,"dijitHidden");
		},
		
		_setAppliancesAttr:function(appliances){
			// tags:
			//		protected
			
			var F = this.declaredClass + "._setAppliancesAttr(): ";
			console.debug(F,appliances);
			
			// Reset related place holders
			this.applianceFirmwareLevels = {};
			this.allLevels = [];
			this.upgradeLevels = [];
			
			this._set("appliances",appliances);
			
			this.reset();
			
			this._updateAvailableFirmware(appliances).then(
					lang.hitch(this,"_updateDisplay"));
		},
		
		_updateAvailableFirmware:function(appliances){
			// appliances: Array
			//		The appliances to show firmware for
			// tags:
			//		protected
			
			var i = 0, defs = {}, appliance = null, result = null;
			
			result = new Deferred();
			
			if(typeof appliances.sort !== "function"){
				// If there are no appliances to show, return
				result.resolve();
			}else{
				for(i=0;i<appliances.length;i++){
					appliance = appliances[i];
					
					defs[appliance.id] = xhr.get({
						url:string.substitute(Resources.availableFirmware.url, {applianceId:appliance.id}),
						handleAs:"json",
						load: lang.hitch(this,"_filterRecommendedFirmwareVersions"),
						error: lang.hitch(this,function(error,ioArgs){
							console.debug("Error while getting available firmware for appliance",appliance.id);
							this.showServerResponse(error.xhr.responseText);
						})
					});
				}
				
				all(defs).then(lang.hitch(this,function(results){
						// Determine the list of firmware levels for all of the appliances
						var applianceId = null, applianceLevels = null, allLevels = [], levelId = null,j=0, k = 0;
						
						for (j=0;j<appliances.length;j++) {
							
							applianceId = appliances[j].id;
							applianceLevels = results[applianceId] || [];
							
							// Store the appliances levels
							this.applianceFirmwareLevels[applianceId] = {};
							
							for(k=0;k<applianceLevels.length;k++){
								levelId = applianceLevels[k].level;
								this.applianceFirmwareLevels[applianceId][levelId] = applianceLevels[k];
							}
							
							// Add these levels to the all levels array
							allLevels = allLevels.concat(array.map(applianceLevels,function(firmware){return firmware.level;}));
						}
						
						this.allLevels = this._firmwareLevelsArraySortAndDedupe(allLevels);
						
						this.upgradeLevels = this._filterUpgradeFirmwareLevels(appliances,allLevels);
						
						result.resolve();
					}),function(error){
						result.reject(error);
					});
			
			}
			
			return result;
		},
		
		_filterRecommendedFirmwareVersions:function(/*Array*/firmwareVersions){
			// firmwareVersions: Array
			//		The available firmware levels for the appliance
			// return: Array
			//		An Array containing only firmware for each level 
			// tags:
			//		protected
			
			// Determine the list of firmware levels for all of the appliances
			var filtered = {}, i = 0, firmware = null, level = null, result = [];
			
			var F = this.declaredClass + "._filterRecommendedfirmwareVersions()";
			console.debug(F,firmwareVersions);
			
			for (i=0;i<firmwareVersions.length;i++) {
				
				firmware = firmwareVersions[i];
				
				if(!filtered[firmware.level] || firmware.recommeded){
					filtered[firmware.level] = firmware;
				}
			}
			
			for(level in filtered){
				result.push(filtered[level]);
			}
			
			console.debug(F,"EXIT",result);
			
			return result;
		},
		
		_filterUpgradeFirmwareLevels:function(/*Array*/appliances,/*Array*/firmwareLevels){
			// appliances:Array
			//		An array of the appliances to find levels for
			// firmwareLevels:Array
			//		An array of available firmwareLevels
			// return: Array
			//		An array of firmware levels classified as upgrades
			// tags:
			//		protected
			var F = this.declaredClass + "._filterUpgradeFirmwareLevels()";
			console.debug(F,appliances,firmwareLevels);
			
			var sortedAppliances = null, maxLevel = null, minLevel = null, upgrades = null;
			
			// Determine the maximum and minimum current firmware versions
			sortedAppliances = appliances.sort(lang.hitch(this,"_compareApplianceFirmwareLevels"));
			
			console.debug(sortedAppliances.length,sortedAppliances);
			
			minLevel = sortedAppliances[0].firmwareLevel;
			maxLevel = sortedAppliances[sortedAppliances.length-1].firmwareLevel;
			
			// Filter firmware levels based on current appliance levels to determine upgrades
			upgrades = array.filter(firmwareLevels, lang.hitch(this, function(level) {
				if (maxLevel == minLevel) {
					return this._compareFirmwareLevels(level, maxLevel) > 0;
				} else {
					return this._compareFirmwareLevels(level, maxLevel) >= 0;
				}
			}));
			
			return upgrades;
		},
		
		_onSubmit:function(evt){
			// summary:
			//		Handler for form submit event
			// tags:
			//		protected
			
			var level = null, defs = [];
			
			event.stop(evt);
			
			if (this.formNode.isValid()) {
				
				// Disable the form buttons
				this.deployButton.set("disabled",true);
				this.cancelButton.set("disabled",true);
				
				// Get the selected level
				level = this.versionList.get('value');
				
				array.forEach(this.appliances, lang.hitch(this, function(appliance) {
					var applianceLevels = this.applianceFirmwareLevels[appliance.id],
						firmware = applianceLevels[level],
						putObject = {};
					
					if (firmware) {
						console.debug("Deploying firmware for appliance " + appliance.id + ": " + firmware.primaryKey);
					
						putObject.firmwarePrimaryKey = firmware.primaryKey;
						putObject.licenceAccepted = this.licenceCheck.checked;
						
						defs.push(xhr.put({
							url:string.substitute(Resources.currentFirmware.url, {applianceId:appliance.id}),
							putData:json.toJson(putObject),
							headers:{"Content-Type":"application/json"},
							handleAs:"json",
							load: function(response,ioArgs) {
								console.debug("Initiated deployment of firmware for appliance " + appliance.id);
							},
							error: lang.hitch(this,function(response,ioArgs){
								console.debug("Error while initiating deployment of firmware for appliance " + appliance.id);
								this.showServerResponse(response.xhr.responseText);
							})
						}));
					}
				}));
				
				all(defs).then(lang.hitch(this,function(results){
					console.debug("Initiated deployment of firmware for appliances");
					this._enableDeployButton();
					this.cancelButton.set("disabled",false);
					
					this.onDeploy();
				}),lang.hitch(this,function(error){
					console.log("Error initializing firmware deployment",error);
					// Show the message from the last response in the message area
					this.showServerResponse(error.xhr.responseText);
					this._enableDeployButton();
					this.cancelButton.set("disabled",false);
				}));
				
				
			} else {
				// Shown errors
				this.formNode.validate();
			}
		},
		
		_onCancel:function(evt){
			// tags:
			//		protected
			event.stop(evt);
			this.reset();
			this.onCancel();
		},
		
		_toggleUpgradesOnly:function(/*Boolean*/showUpgradesOnly){
			// tags:
			//		protected
			this._updateDisplay();
			this._enableDeployButton();
		},
		
		_versionSelectionChanged:function(/*String*/newValue){
			// tags:
			//		protected
			this.licenceCheck.set("checked",false);
			this._displaySummary();
		},
		
		_updateDisplay:function(){
			// tags:
			//		protected
			this._displayOptions();
			this._displaySummary();
		},
		
		_displayOptions: function() {
			// tags:
			//		protected
			var F = this.declaredClass + "._displayOptions(): ";
			console.debug(F);
			
			var levels = (this.upgradesOnlyRadioButton.checked) ? this.upgradeLevels : this.allLevels,
				options = [];
			
			// Populate options
			if (levels.length === 0) {
				options.push({"label":this.nls.deployFirmware.noneAvailable});
			} else {
				array.forEach(levels, function(level) {
					options.push({"label":level,"value":level});
				});
			}
			//this._enableDeployButton();
			this.versionList.options = [];
			this.versionList.addOption(options);
		},
		
		_displaySummary: function() {
			// tags:
			//		protected
			var F = this.declaredClass + "._displaySummary(): ";
			console.debug(F);
			
			var selectedLevel = null, data = [], matches = 0, grid = null;
			
			selectedLevel = this.versionList.get("value");
			
			console.debug(F,"Selected Level",selectedLevel);
			
			array.forEach(this.appliances, lang.hitch(this, function(appliance) {
				
				var applianceLevels = null, applianceFirmware = null;
				
				applianceLevels = this.applianceFirmwareLevels[appliance.id] || {};
				applianceFirmware = applianceLevels[selectedLevel];
				
				if (applianceFirmware){
					matches++;
				}
				data.push({id: appliance.id,
						name: appliance.name,
						model: appliance.model,
						type: appliance.applianceType,
						current: appliance.firmwareLevel,
						target: applianceFirmware});
			}));
			
			console.debug(F,"New grid data:",data);
			
			grid = this.applianceGrid;

			grid.setStore(new Memory({"data": data}));
			
			// Force the hoverhelp to refresh its attach points
			this.detailsHoverHelp.set("connectId",grid.domNode.id);
			
			this._displayWarning(matches);

		},
		
		_displayWarning:function(/*number*/matches){
			// summary:
			//		If the number of matches specified does not match the number
			//		of selected appliances, show a warning
			// matches:
			//		The number of firmware matches detected
			// tags:
			//		protected
			var F = this.declaredClass + "._displayWarning(): ";
			console.debug(F,matches);
			
			var message = "",showMessage = false;
			
			if (matches > 0 && (matches != this.appliances.length)) {
				showMessage = true;
				if (matches == 1) {
					message = string.substitute(nls.deployFirmware.warningSingular, {total: this.appliances.length});
				}else {
					message = string.substitute(this.nls.deployFirmware.warningPlural, {total: this.appliances.length, updated: matches});
				}
			}
			
			domAttr.set(this.warningMessage,"innerHTML",message);
			domClass.toggle(this.warningNode,"dijitHidden",!showMessage);
		},
		
		_firmwareLevelsArraySortAndDedupe: function(levels) {
			// tags:
			//		protected
			var F = this.declaredClass + "._firmwareLevelsArraySortAndDedupe(): ";
			console.debug(F,levels);
			
			var levelSet = [];
			array.forEach(levels, function(level, i) {
				 if(array.indexOf(levelSet, level)  == -1) {
					 levelSet.push(level);
				 }
			});
			return levelSet.sort(lang.hitch(this, this._compareFirmwareLevels)).reverse();
		},
		
		_compareApplianceFirmwareLevels:function(applianceA,applianceB){
			// tags:
			//		protected
			var F = this.declaredClass + "._compareApplianceFirmwareLevels(): ";
			console.debug(F,applianceA,applianceB);
			return this._compareFirmwareLevels(applianceA.firmwareLevel,applianceB.firmwareLevel);
		},
		
		_compareFirmwareLevels: function(levelA, levelB){
			// tags:
			//		protected
			var F = this.declaredClass + "._compareFirmwareLevels(): ";
			console.debug(F,levelA,levelB);
			
			return this._firmwareLevelToInteger(levelA) - this._firmwareLevelToInteger(levelB);
		},
		
		_firmwareLevelToInteger: function(level) {
			// tags:
			//		protected
			var F = this.declaredClass + "._firmwareLevelToInteger(): ";
			console.debug(F,level);
			
			var parts = level.split(".");
			// Make sure there are at least four parts (even if empty);
			for(var i=0; i<4; i++){
				if(!parts[i]) parts[i] = "";
			}
			
			// Massage each part into a number (albeit in string form), zero if we don't like the format.
			parts = array.map(parts, function(part){
				if(!part) return "0";
				if(isNaN(part)){ // don't waste time doing regexes if it's already a number.
					var numbers = /^\d+/.exec(part); // Pick up digits at start of string.
					return numbers? numbers+"" : "0";
				}
				return part; // if it was already a number just send it back.
			});

			var result = 0;
			for (i=0; i<4; i++){
				result = (result * 100) + parseInt(parts[i], 10);
			}
			console.log(F, "Integer level: ", result);
			return result;
		},
		
		_enableDeployButton: function(){
			// summary
			//		Determine if the deploy button should be enabled
			// tags:
			//		protected
			
			// has the user accepted the licence
			if (this.licenceCheck.checked){
				
				// are there viable levels that can be deployed
				var levels = (this.upgradesOnlyRadioButton.checked) ? this.upgradeLevels : this.allLevels;
				if (levels.length > 0){
					this.deployButton.set("disabled",false);
				}
				else{
					this.deployButton.set("disabled",true);
				}
			}
			else{
				this.deployButton.set("disabled",true);
			}
		},
		
		_getFirmwareDetailsContent:function(node){
			// summary:
			//		Get the detail to put in the hover help for a firmware details link
			// tags:
			//		protected
			var F = this.declaredClass + "._getFirmwareDetailsContent(): ";
			console.debug(F,arguments);
			
			var content = "", 
				nl = null, row = null, rowId = null,
				level = null,
				firmware = null, firmwareFeatures = null, substitutions = null;
			
			// Create a node list so we can find the parent
			nl = new query.NodeList();
			nl.push(dom.byId(node));
			
			// Now get the parent rowId (appliance id)
			row = nl.closest(".gridxRow")[0];
			rowId = domAttr.get(row,"rowid");
			
			// Now get the firmware info for the appliance
			level = this.versionList.get("value");
			firmware = this.applianceFirmwareLevels[rowId][level];
			
			if (firmware) {
				firmwareFeatures = wamcUtil.generateFirmwareFeaturesString(firmware.strictFeatures, firmware.nonStrictFeatures);
				substitutions = {firmwareVersion: firmware.level, 
						firmwareFeatures: firmwareFeatures.length > 0 ? firmwareFeatures : this.nls.deployFirmware.noFeatures,
						userComment: firmware.userComment,
						nls: this.nls};
				content = string.substitute(detailsTemplate, substitutions);
			} 
			
			return content;
		},
		
		onCancel: function(){
			// summary:
			//		Placeholder for more events to occur on cancel
			// tags:
			//		override
			var F = this.declaredClass + ".onCancel(): ";
			console.debug(F);
		},
		
		onDeploy: function(){
			// summary:
			//		Placeholder for more events to occur on submit
			// tags:
			//		override
			var F = this.declaredClass + ".onDeploy(): ";
			console.debug(F);
		},
		
		onError: function(){
			// summary:
			//		Placeholder for more events to occur when something goes wrong
			// tags:
			//		override
			var F = this.declaredClass + ".onError(): ";
			console.debug(F);
		}
	});
		
	return DeployFirmwareForm;
	
});
