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
        "dojo/_base/Deferred",
        "dojo/_base/event",
        "dojo/_base/lang",
        "dojo/_base/xhr",
        "dojo/aspect",
        "dojo/dom-attr",
        "dojo/dom-class",
        "dojo/dom-construct",
        "dojo/dom-style",
        "dojo/fx",
        "dojo/on",
        "dojo/string",
        "dojo/topic",
        "dojo/store/Memory",
        "dijit/_TemplatedMixin",
        "dijit/_Widget",
        "dijit/_WidgetsInTemplateMixin",
        "dijit/form/Button",
        "dijit/form/Form",
        "dijit/form/RadioButton",
        "dijit/registry",
        "dojox/form/Uploader",
        "dojox/widget/Standby",        "dojox/widget/WizardPane",
        "gridx/Grid",
        "gridx/core/model/cache/Async",
        "wamc/widget/form/Select",
        "wamc/widget/form/TextBox",
        "wamc/_HelpLabelsMixin",
        "wamc/_MessagesMixin",
        "wamc/config/Resources",
        "wamc/grid/format",
        "wamc/grid/modules",
        "wamc/widget/Wizard",
        "wamc/widget/form/_FormElementIdMixin",
        "wamc/widget/form/UploaderValueTextBox",
        "wamc/validation",
        "dojo/i18n!wamc/nls/strings",
        "dojo/text!wamc/widget/wizard/templates/DomainConfigurationWizard.html"],
        function(array,declare,Deferred,event,lang,xhr,aspect,domAttr,domClass,
        		domConstruct,domStyle,fx,on,string,topic,MemoryStore,
        		_TemplatedMixin,_Widget,
        		_WidgetsInTemplateMixin,Button,Form,RadioButton,registry,
        		Uploader,Standby,WizardPane,Grid,Cache,Select,TextBox,
        		_HelpLabelsMixin,_MessagesMixin,Resources,gridFormat,
        		gridModules,Wizard,_FormElementIdMixin,UploaderValueTextBox,
        		wamcValidation,nls,template){
        
	var DomainConfigurationWizard = declare("wamc.widget.wizard.DomainConfigurationWizard",
			[_Widget,_TemplatedMixin,_WidgetsInTemplateMixin,_MessagesMixin,_FormElementIdMixin,_HelpLabelsMixin],{
		
		templateString:template,
		
		nls: nls,
		
		target:Resources.upload.url,
		
		// The store this wizard will use to update options on-the-fly
		store:null,
		
		postCreate:function(){
			// summary:
			//		Widget life-cycle function
			
			var F = this.declaredClass + ".postCreate()";
			console.debug(F);
			
			this.inherited(arguments);
			
			var wizard = this.wizard;
			
			console.debug(F, "Setup Widgets: DeploymentPolicyPane");
			
			var deploymentPolicyGrid = this.deploymentPolicyGrid = new Grid({
				store: new MemoryStore({data:[]}),
				cacheClass: Cache,
				modules:[
				         {moduleClass:gridModules.ExtendedSelectRow,
				        	 triggerOnCell:true},
				         {moduleClass:gridModules.Focus}
				         ],
				structure:[
				           { id: "name", name: nls.domainConfigurationWizard.deploymentPolicy.grid.name, field: "name",width:"50%"},			        
				           { id: "policy", name: nls.domainConfigurationWizard.deploymentPolicy.grid.policy, field: "deploymentPolicy",width:"50%",formatter:gridFormat.formatDeploymentPolicy}],
				'aria-label':nls.domainConfigurationWizard.deploymentPolicy.gridHeader
				});
			deploymentPolicyGrid.placeAt(this.deploymentPolicyGridContainerNode);
			this._setupGridSelectionEvents(deploymentPolicyGrid);
			
			domClass.add(deploymentPolicyGrid.domNode,"compact");
			
			deploymentPolicyGrid.startup();
			
			console.debug(F, "Setup Events: ConfigSourcePane");
			
			this.configSourceForm.watch("state",lang.hitch(this,"_formStateChanged"));
			
			/* 
			 * Because the uploader doesn't affect the state of its parent 
			 * watch for change events separately. 
			 */
			aspect.after(this.configSourceLocalSourceUploader,"onChange",
					function(){
						wizard._checkButtons();
					}
			);
			
			/* 
			 * Because idx.form.select doesn't affect the state of the parent immediately 
			 * watch for change events separately. 
			 */
			aspect.after(this.configSourceExistingDomainSelect,"onChange",
					function(){
						wizard._checkButtons();
					}
			);
			
			// When the radio button changes, set the required attributes on fields
			on(this.configSourceLocalSourceRadioButton,"change",lang.hitch(this,function(newValue){
				console.debug("Config Source Local radio changed",newValue);
				
				if(newValue){
					// Enable this section
					this.configSourceLocalSourceUploader.set("disabled",false);
					this.configSourceLocalSourceUploader.set("required",true);
				}else{
					this.configSourceLocalSourceUploader.set("required",false);
					this.configSourceLocalSourceUploader.set("disabled",true);
					this.configSourceLocalSourceUploader.reset();
				}
				wizard._checkButtons();
			}));
			
			on(this.configSourceRemoteSourceRadioButton,"change",lang.hitch(this,function(newValue){
				console.debug("Config Source Remote radio changed",newValue);
				
				if(newValue){
					//Enable this section
					this.configSourceRemoteSourceDataField.set("disabled",false);
					this.configSourceRemoteSourceDataField.set("required",true);
				}else{
					this.configSourceRemoteSourceDataField.set("required",false);
					this.configSourceRemoteSourceDataField.set("disabled",true);
					this.configSourceRemoteSourceDataField.reset();
				}
				
			}));
			
			on(this.configSourceExistingDomainRadioButton,"change",lang.hitch(this,function(newValue){
				console.debug("Config Source Existing radio changed",newValue);
				
				if(newValue){
					//Enable this section
					this.configSourceExistingDomainSelect.set("disabled",false);
					this.configSourceExistingDomainSelect.set("required",true);
				}else{
					this.configSourceExistingDomainSelect.set("disabled",true);
					this.configSourceExistingDomainSelect.set("required",false);
					this.configSourceExistingDomainSelect.reset();
				}
			}));
			
			// When the uploader completes, call _uploadComplete to update the model
			on(this.configSourceLocalSourceUploader,"complete",lang.hitch(this,this._uploadComplete));
			
			console.debug(F, "Setup events: Deployment Policy Form");
			
			this.deploymentPolicyForm.watch("state",
					lang.hitch(this,"_deploymentPolicyFormStateChanged"));
			
			/* 
			 * Because the uploader doesn't affect the state of its parent 
			 * watch for change events separately. 
			 */
			aspect.after(this.deploymentPolicyLocalFileDataField,"onChange",
					lang.hitch(this,"_deploymentPolicyFormStateChanged"));
			
			on(this.deploymentPolicySourceTypeSelect,"change",
					lang.hitch(this,"_deploymentPolicyConfigureDetails"));
			
			on(this.deploymentPolicyExistingApplianceDataField,"change",
					lang.hitch(this,"_deploymentPolicyPopulateExistingDomain"));
			
			// When the Update box gets clicked, reset and show the detail panel
			on(this.deploymentPolicyApplyButton,"click",lang.hitch(this,function(evt){
				event.stop(evt);
				this._deploymentPolicyProcessForm();
			}));
			
			on(this.deploymentPolicyCancelButton,"click",lang.hitch(this,function(evt){
				event.stop(evt);
				// Clear selection, which will reset the deployment policy form
				this.deploymentPolicyGrid.select.row.clear();
			}));
			
			// When the uploader completes, call _uploadComplete to update the model
			on(this.deploymentPolicyLocalFileDataField,"complete",
					lang.hitch(this,this._deploymentPolicyUploadComplete));
			
			aspect.after(this.deploymentPolicyPane,"onShow",lang.hitch(this,function(){
				this._resetGrid(this.deploymentPolicyGrid);
			}));
			
			topic.subscribe(deploymentPolicyGrid.id+"-selectionChanged",
					lang.hitch(this,function(newSelectedIds){
						if(newSelectedIds.length < 1){
							this._deploymentPolicyHideDetails();
							this._deploymentPolicyReset();
						}else{
							this._deploymentPolicyShowDetails();
						}
					}
			));

			console.debug(F, "Setup events: Wizard Level");
			
			this.wizard.set("cancelFunction",
					lang.hitch(this,"cancel"));

			// Validation for Configuration Source Panel
			this.configurationSourcePane.set("passFunction",
					lang.hitch(this,"configurationSourcePassFunction"));
		
			// Validation for Deployment Policy Panel
			this.deploymentPolicyPane.set("passFunction",
					lang.hitch(this,"deploymentPolicyPassFunction"));
			
			//Wizard 'done' function
			this.syncModePane.set("doneFunction",
					lang.hitch(this,"done"));
			
		},
		
		startup:function(){
			// summary:
			//		Widget life-cycle function
			
			var F = this.declaredClass + ".startup()";
			console.debug(F);
			
			this.inherited(arguments);

			// If this wizard appears in a dialog, let the wizard know it needs to layout its panels
			var parent = registry.getEnclosingWidget(this.domNode.parentNode);
			
			if(parent && parent instanceof dijit.Dialog){
				// Have the overlay cover the entire dialog
				this.standby.set("target",parent.domNode);
			}
			
			// If the uploader is going to use an iframe (because this is IE)
			// add a hidden input to let the server know.
			if(!this.configSourceLocalSourceUploader.supports("multiple")){
				
				domConstruct.create("input",
						{name:"wrapResponse",value:"true",type:"hidden"},
						this.id + "_configSourceForm","first");
				
				domConstruct.create("input",
						{name:"wrapResponse",value:"true",type:"hidden"},
						this.id + "_deploymentPolicyForm","first");
			}
			
			this.reset();

		},
		
		_setModelAttr:function(/*Object*/model){
			// summary:
			//		Set the model to be updated by the wizard
			// model: Object
			//		The model object
			
			var F = this.declaredClass + "_setModelAttr()";
			console.debug(F,model);
			
			this._set("model",lang.mixin({},model));
			
			if(this._started){
				this._updateDisplay(this.model);
			}
		},
		
		_getModelAttr:function(){
			// summary:
			//		Get the model after it has been updated by the wizard
			// return: Object
			//		The updated model object
			
			var F = this.declaredClass + "._getModelAttr()";
			console.debug(F);
			
			if(this._started){
				this._updateModel();
			}
			
			return this.model;
			
		},
		
		_updateDisplay:function(/*Object*/model){
			// summary:
			//		Update the wizard view with details of the model
			// model:
			//		The model for this wizard
			// tags:
			//		protected
			
			var F = this.declaredClass + "._updateDisplay()";
			console.debug(F,model);
			
			this.reset();
			
			/** Configuration Source Pane **/
			
			this._configurationSourceSetup();
			if(model.sourceType==="remote"){
				this.configSourceRemoteSourceRadioButton.set("value",true);
				this.configSourceRemoteSourceDataField.set("value",model.url);
			}
			
			/** Deployment Policy Pane **/
			this._resetGrid(this.deploymentPolicyGrid,new MemoryStore({data:model.appliances,idProperty:"name"}));
			
			/** Sync Mode Pane **/
			this.syncModeAutomaticSynchronizationDataField.set("checked",model.automaticSynchronization === true);
			
			this._syncModeShowIntro(model.appliances.length);
			
		},
		
		_updateModel:function(){
			// summary:
			//		Update the model prior to being retrieved
			// return: Object
			//		The model to be returned to the caller of get("model");
			// tags:
			//		protected
			
			var F = this.declaredClass + "._updateModel()";
			console.debug(F);

			/** Configuration Source Pane **/
			var form = this.configSourceForm.get("value");
			console.debug(F, "Form: ", form);
			
			
			if(form.sourceType==="local"){
				delete form.uri;
			} else if(form.sourceType==="existing"){
				form.uri = form.existingDomain; 
			}
			
			delete form.localFile;
			delete form.existingDomain;
			
			this.model = lang.mixin(this.model,form);
			
			/** Deployment Policy Pane **/
			// Model is already up to date, since it is updated every time a file is uploaded
			
			/** Sync mode Pane **/
			this.model.automaticSynchronization = this.syncModeAutomaticSynchronizationDataField.get("checked");			
		},
		
		passFunction:function(){
			// summary:
			//		Validation function to check that the wizard is ok
			var F = this.declaredClass + ".passFunction()";
			console.debug(F);
			
			return (this.deploymentPolicyPassFunction()===true &&
					this.configurationSourcePassFunction()===true);
		},
		
		done:function(){
			// summary:
			//		Called by the wizard once all actions are complete
			
			var F = this.declaredClass + ".done()";
			console.debug(F);
			
			// First, make sure all the forms pass validation
			if(!this.passFunction()){
				return;
			}
			
			var source = this.configSourceLocalSourceRadioButton.get("value");
			
			// If using a local configuration, upload the local config
			if(source==="local"){
				console.debug(F,"Local SourceType",source);
				this.uploadStart();
				this.configSourceLocalSourceUploader.upload();
			}else{
				console.debug(F,"Non-local SourceType",source);
				this.onDone();
			}
		},

		uploadStart:function(){
			// summary:
			//		Begin upload of the domain configuration
			
			var F = this.declaredClass + ".uploadStart()";
			console.debug(F);
			
			this.disable();
			this.showStandby();
		},
		
		uploadComplete:function(){
			// summary:
			//		Called to signal the completion of uploading
			
			var F = this.declaredClass + ".uploadComplete()";
			console.debug(F);
			
			this.enable();
			this.hideStandby();
			this.onDone();
		},
		
		_uploadComplete:function(/*Object*/result){
			// summary:
			//		Upload the model with the urls of uploaded local file
			// tags:
			//		protected
			var F = this.declaredClass + "._uploadComplete()";
			console.debug(F,result);			
			
			console.debug(F,this.model);
			
			this.model.uri = result.files[0];
			
			console.debug(F,"updated model",this.model,this);
			
			this.uploadComplete(result);
		},

		reset:function(){
			// summary:
			//		Reset all panes of this wizard
			
			var F = this.declaredClass + ".reset()";
			console.debug(F);
			
			delete this._availableDomains;
			
			/** Config Source Form **/
			this._configurationSourceReset();
			
			/** Deployment Policy Form **/
			this._resetGrid(this.deploymentPolicyGrid,new MemoryStore({data:[]}));
			this._deploymentPolicyReset();
			
			/** Sync Mode Form **/
			this.syncModeAutomaticSynchronizationDataField.reset();
			this._syncModeShowIntro(1);
						this.wizard.selectChild(this.deploymentPolicyPane);
			this.wizard.selectChild(this.configurationSourcePane);
		},
		
		resize:function(){
			// summary:
			//		Overridden to make sure that grids are resized whenever this
			//		widget is notified of a size change
			
			var F = this.declaredClass + ".resize()";
			console.debug(F);
			
			this.inherited(arguments);
			this.wizard.resize(arguments);
			this._resetGrid(this.deploymentPolicyGrid);
		},
		
		cancel:function(){
			// summary:
			//		Called when the cancel button is pressed.
			
			var F = this.declaredClass + ".cancel()";
			console.debug(F);
			
			this.onCancel();
		},
		
		
		disable: function(){
			// summary:
			//		Disable panes of this wizard prior to some operation
			//		being performed

			var F = this.declaredClass + ".disable()";
			console.debug(F);
			
			// Disable forms
			this.configSourceRemoteSourceDataField.set("disabled",true);
			
			// Disable buttons
			this.wizard.doneButton.set("disabled",true);
			this.wizard.cancelButton.set("disabled",true);
			this.wizard.previousButton.set("disabled",true);
			this.wizard.nextButton.set("disabled",true);
		},
		
		enable: function(){
			// summary:
			//		Enable panes of this wizard.
			
			var F = this.declaredClass + ".enable()";
			console.debug(F);
			
			if(this.configSourceLocalSourceRadioButton.get("value")){
				this.configSourceLocalSourceUploader.set("disabled",false);
				this.configSourceRemoteSourceDataField.set("disabled",true);
			}else{
				this.configSourceLocalSourceUploader.set("disabled",true);
				this.configSourceRemoteSourceDataField.set("disabled",false);
			}
			
			this.wizard.doneButton.set("disabled",false);
			this.wizard.cancelButton.set("disabled",false);
			this.wizard._checkButtons();
		},
		
		
		onDone:function(){
			// tags:
			//		callback
			var F = this.declaredClass + ".onDone()";
			console.debug(F);
		},
		
		onCancel:function(){
			// tags:
			//		callback
			var F = this.declaredClass + ".onCancel()";
			console.debug(F);
		},
		
		_formStateChanged:function(/*String*/property,/*String*/oldValue,/*String*/newValue){
			// summary:
			//		Called when the state of one of the forms changes
			// tags:
			//		protected
			var F = this.declaredClass + "._formStateChanged()";
			console.debug(F,property,oldValue,newValue);
			
			this.wizard._checkButtons();
		},
		
		_resetGrid:function(/*gridx/Grid*/grid,/*Store*/store){
			// summary:
			//		Reset a grid
			// grid: gridx/Grid or idx/gridx/Grid
			//		The grid to reset
			// store: dojo/store/MemoryStore
			//		optionally, the store to use. If omitted, the existing store is used.
			// tags:
			//		protected
			var F = this.declaredClass + "._resetGrid()";
			console.debug(F,grid,store);
			
			if(store){
				grid.setStore(store);
			}else{
				grid.model._cache.clear();
				grid.body.refresh();				
			}
			
		},
		
		_setupGridSelectionEvents:function(/*gridx/Grid*/grid){
			// summary:
			//		Setup selection events for a grid
			// grid: gridx/Grid or idx/gridx/Grid
			//		The grid to setup selection events for
			// tags:
			//		protected
			var F = this.declaredClass + "._setupGridSelectionEvents()";
			console.debug(F,grid);
			
			// When the selection in the grid changes, publish an event
			// with the new selected ids.
			if(grid.select){
				aspect.after(grid.select.row,"onSelectionChange",lang.hitch(this,function(newSelectedIds, oldSelectedIds){
					console.debug(grid.id,"Selection Changed", newSelectedIds);
					topic.publish(grid.id + "-selectionChanged",newSelectedIds);
				}),true);
			}
		},
		
		actionFunctionError:function(error){
			// summary:
			//		Called when an action function fails to display error
			//		and prevent the wizard moving forward;
			// description:
			//		Use this function as the errback when returning a deferred
			//		from a wizardpane action function
			var F = this.declaredClass + ".actionFunctionError()";
			console.debug(F);
			
			// Show error
			if(typeof error.responseText==="string"){
				this.showServerResponse(error.responseText);
			}
			
			// Hide the standby, if it was visible
			this.hideStandby();
			// Return false, so that the wizard pane knows not to advance
			return false;
		},
		
		showStandby:function(/*String*/ message){
			// summary:
			//		Show the standby overlay with the provided message
			// message: String
			//		The message to show on the standby
			// substitutes: Object
			//		the key/value pairs of subsitutes for the message
			
			var msg = message || nls.createDomainWizard.standbyText;
			
			this.standby.set("text",msg);
			this.standby.show();
		},
		
		hideStandby:function(){
			// summary
			//		Hide the standby overlay
			
			this.standby.hide();
		},
		
		/** Functions for ConfigurationSourcePane **/

		configurationSourcePassFunction:function(){
			// summary:
			//		Validation function for the configurationSource pane
			// description:
			//		Because this form uses inputs and radio buttons, form.validate is
			//		not sufficient, and we need to write our own custom check
			// return: Boolean
			//		True if the user can continue, otherwise false
			
			var F = this.declaredClass + ".configurationSourcePassFunction()";
			console.debug(F);
			
			var f = this.configSourceForm.get("value"),
				valid = true;
		
			if (f.sourceType==="local"){
				console.debug(F,"Validating local source");
				valid = !(this.configSourceLocalSourceUploader.get("value").length===0);
			}
			else if(f.sourceType==="remote"){
				console.debug(F,"Validating Remote Source");
				valid = this.configSourceRemoteSourceDataField.validate();
			}
			else if(f.sourceType==="existing"){
				console.debug(F,"Validating Existing Domain Source");
				valid = !(this.configSourceExistingDomainSelect.get("value") === "");
			}
			
			return valid;
		},
		
		_configurationSourceSetup: function(){
			// summary:
			//		Retrieve domains with the same name for the select field
			var F = this.declaredClass + "._configurationSourceSetup()";
			console.debug(F);
			
			this.showStandby(nls.domainConfigurationWizard.standbyWorking);
			
			// Retrieve all the domains with the same name,
			// then call function to populate domains list on completion
			var d1 = this.store.query({name:this.model.name}),
				d2 = Deferred.when(d1,
						lang.hitch(this,"_configurationSourcePopulateConfigSource"),
						lang.hitch(this,"actionFunctionError"));
			
			return d2;
		},
		
		_configurationSourcePopulateConfigSource:function(domains){
			// tags:
			//		protected
			var F = this.declaredClass + "._configurationSourcePopulateConfigSource()";
			console.debug(F,domains);
			
			var t = this,
				options = [],
				appliances = array.map(this.model.appliances,function(appl){
					return appl.id;
				});
			
			t._configurationSourceReset();
			
			// Filter the list so it only contains domains we want
			domains = array.filter(domains,function(domain,i){
				return domain.name===t.model.name 
					&& array.indexOf(appliances,domain.applianceId) === -1;
			});
			
			// Populate the config source lists
			console.debug(F, "Domains: ", domains);
			options.push({label: nls.domainConfigurationWizard.configurationSource.nullDomain, value: ""});
			
			array.forEach(domains,function(domain){
				var l = string.substitute(nls.domainConfigurationWizard.configurationSource.existingDomainOption,{applianceName:domain.applianceName,domainName:domain.displayName}),
					v = "device://"+domain.applianceId+"/"+domain.name; 
				options.push({label:l,value: v});
			});
			
			this.configSourceExistingDomainSelect.set("options", []);
			this.configSourceExistingDomainSelect.addOption(options);
			
			this.hideStandby();
			
			return true;
		},
		
		_configurationSourceReset:function(){
			// summary:
			//		Reset the configuration source page
			// tags:
			//		protected
			var F = this.declaredClass + "._configurationSourceReset()";
			console.debug(F);
			
			this.configSourceLocalSourceRadioButton.set("value",true);
			this.configSourceRemoteSourceRadioButton.set("value",false);
			this.configSourceExistingDomainRadioButton.set("value",false);
			this.configSourceLocalSourceUploader.reset();
			this.configSourceRemoteSourceDataField.reset();
			this.configSourceExistingDomainSelect.reset();
		},
		
		deploymentPolicyPassFunction:function(){
			// summary:
			//		Validation function for the deploymentPolicy pane
			// description:
			//		Slightly unconventional one, you're not allowed to continue
			//		while the details form is displayed.
			// return: Boolean
			//		True if the user can continue, otherwise false
			
			var F = this.declaredClass + ".deploymentPolicyPassFunction()";
			console.debug(F);
			
			var display = domStyle.get(this.deploymentPolicyDetailNode,"display");
			
			console.debug(F,"display",display,typeof display);
			
			return display === "none";
		},
		
		_deploymentPolicyValidate:function(){
			// summary:
			//		Internal function to validate the details form on 
			//		the deployment policy pane
			// return: Boolean
			//		True if the user can continue, otherwise false
			// tags:
			//		protected

			var F = this.declaredClass + "_deploymentPolicyValidate()";
			console.debug(F);
			
			var f = true, v=true;
			
			// Because Uploader doesn't have a validate method, 
			// it needs to be done separately.
			if(this.deploymentPolicySourceTypeSelect.get("value")==="local"){
				var val = this.deploymentPolicyLocalFileDataField.get("value");
				console.debug(F,"Local Value",val);
				f = (val && val.length >0);
			}

			v= this.deploymentPolicyForm.isValid();
			
			return f && v;
		},
		
		_deploymentPolicyProcessForm:function(){
			// summary:
			//		Trigger validation and upload of the deployment policy 
			//		details form (after apply is pressed)
			// tags:
			//		protected
			
			var F = this.declaredClass + "_deploymentPolicyProcessForm()";
			console.debug(F);
			
			if(!this._deploymentPolicyValidate()) return;
			
			this.disable();
			
			switch(this.deploymentPolicySourceTypeSelect.get("value")){
			case "local":
				console.debug(F,"Using Local File");
				this.deploymentPolicyUpload();
				
				break;
			case "remote":
				var url = this.deploymentPolicyRemoteLocationDataField.get("value");
				console.debug(F,"Using Remote File",url);
				// For remote files, we already have the file url
				this._deploymentPolicyUploadComplete({files:[url]});
				break;
			case "existing":
				var a = this.deploymentPolicyExistingApplianceDataField.get("value"),
					d = this.deploymentPolicyExistingDomainDataField.get("value"),
					url = "device://"+ a + "/" + d;
				
				this._deploymentPolicyUploadComplete({files:[url]});
				break;
			default: /*None*/
				console.debug(F,"Using *None*");
				// If none is selected, set the policy location to null
				this._deploymentPolicyUploadComplete(null);
				break;
			}
		},
		
		_deploymentPolicyFormStateChanged:function(property,oldValue,newValue){
			// summary:
			//		Check whether the apply button should be enabled/disabled
			// tags:
			//		protected
			
			var F = this.declaredClass + "_deploymentPolicyFormStateChanged()";
			console.debug(F,property,oldValue,newValue);
			
			var valid = this._deploymentPolicyValidate();
			
			console.debug(F,valid ? "Form is valid" : "Form is NOT valid");
			
			this.deploymentPolicyApplyButton.set("disabled",
					!valid);
		},

		_deploymentPolicyReset:function(){
			// summary:
			//		Reset the deployment policy details form
			// tags:
			//		protected
			var F = this.declaredClass + "._deploymentPolicyReset()";
			console.debug(F);
			this.deploymentPolicySourceTypeSelect.reset();
			this._deploymentPolicyHideDetails();
			this._deploymentPolicyConfigureDetails("none");
		},
		
		_deploymentPolicyShowDetails:function(){
			// summary:
			//		Show the deployment policy details form			
			// tags:
			//		protected
			var F = this.declaredClass + "._deploymentPolicyShowDetails()";
			console.debug(F);
			
			this.deploymentPolicyApplyButton.set("disabled",false);
			
			var s = domStyle.get(this.deploymentPolicyDetailNode,"display");
			
			if(s=="none"){
				var wizard = this.wizard,
					wipeArgs = {node:this.deploymentPolicyDetailNode},
					anim = fx.wipeIn(wipeArgs);
				
				var signal = aspect.after(anim,"onEnd",
						function(){
							signal.remove();
							wizard._checkButtons();
						}
				);
				
				anim.play();
			}
		},
		
		_deploymentPolicyHideDetails:function(){
			// summary:
			//		Hide the deployment policy details form
			// tags:
			//		protected
			var F = this.declaredClass + "._deploymentPolicyHideDetails()";
			console.debug(F);
			
			this.deploymentPolicyApplyButton.set("disabled",true);
			
			var s = domStyle.get(this.deploymentPolicyDetailNode,"display");
			
			if(s!="none"){
				console.debug(F,"do animation");
				var wizard = this.wizard,
					wipeArgs = {node:this.deploymentPolicyDetailNode},
					anim = fx.wipeOut(wipeArgs);
				
				var signal = aspect.after(anim,"onEnd",
						function(){
							signal.remove();
							wizard._checkButtons();
						}
				);
				
				anim.play();
			}
		},
		
		_deploymentPolicyConfigureDetails:function(/*String*/value){
			// summary:
			//		Configure the deployment policy details form, depending 
			//		on the type of source selected
			// value: String
			//		One of "none","local","remote"
			// tags:
			//		protected
			var F = this.declaredClass + "._deploymentPolicyConfigureDetails()";
			console.debug(F,value);
			
			var wipeArgs = {node:this.deploymentPolicyDetailContentNode};
			
			// 1 - Wipe out
			
			var anim = fx.wipeOut(wipeArgs);
			
			var signal = aspect.after(anim,"onEnd",lang.hitch(this,function(){
				console.debug(F,"Wipe finished");
				
				// 1 - Only run this aspect once for this animation
				signal.remove();
				
				// 2 - Show/hide fields
				
				var enable = [], disable=[], pane="deploymentPolicy", d=false;
				
				switch (value) {
				case "local":
					enable = [pane + "LocalFile",
					          pane + "DomainName",
					          pane + "PolicyName"];
					
					disable = [pane + "RemoteLocation",
					           pane + "ExistingAppliance",
					           pane + "ExistingDomain"];
					
					d=true;
					break;
				case "remote":
					enable = [pane + "RemoteLocation",
					          pane + "DomainName",
					          pane + "PolicyName"];
					
					disable = [pane + "LocalFile",
					           pane + "ExistingAppliance",
					           pane + "ExistingDomain"];
					d=true;
					break;
				case "existing":
					enable = [pane + "ExistingAppliance",
					          pane + "ExistingDomain",
					          pane + "PolicyName"];
					
					disable = [pane + "LocalFile",
					           pane + "RemoteLocation",
					           pane + "DomainName"];
					
					// Populate the appliance select list
					var availableDomains = this._availableDomains || this.store.query();
					
					d = Deferred.when(availableDomains,
							lang.hitch(this,"_deploymentPolicyPopulateExistingAppliance"),
							lang.hitch(this,"actionFunctionError"));
					
					break;
				default /*None*/:
					
					disable = [pane + "LocalFile",
					           pane + "RemoteLocation",
					           pane + "DomainName",
					           pane + "PolicyName",
					           pane + "ExistingAppliance",
					           pane + "ExistingDomain"];
					d=true;
					break;
				}
				
				array.forEach(enable,function(n){
					console.debug(F,"Enabling",n);
					var row = this["" + n + "Row"],
						input = this["" + n +"DataField"];
					
					domClass.remove(row,"hidden");
					input.set("required",true);
				},this);
				
				array.forEach(disable,function(n){
					console.debug(F,"Disabling",n);
					var row = this["" + n + "Row"],
					input = this["" + n +"DataField"];
					
					input.set("required",false);
					domClass.add(row,"hidden");
				},this);
				
				// 3 - Reset Fields
				this.deploymentPolicyLocalFileDataField.reset();
				this.deploymentPolicyRemoteLocationDataField.reset();
				this.deploymentPolicyDomainNameDataField.reset();
				this.deploymentPolicyExistingApplianceDataField.reset();
				this.deploymentPolicyExistingDomainDataField.reset();
				this.deploymentPolicyPolicyNameDataField.reset();
				
				// Wait for any fetches or calculations to complete 
				// before wiping in.
				Deferred.when(d,function(){
					fx.wipeIn(wipeArgs).play();
				});
			}));
			
			anim.play();
		},
		
		deploymentPolicyUpload:function(){
			// summary:
			//		Upload the file specified by the local uploader
			var F = this.declaredClass + ".deploymentPolicyUpload()";
			console.debug(F);
			
			this._deploymentPolicyUploadStart();
			
			this.deploymentPolicyLocalFileDataField.upload();
			
		},
		
		_deploymentPolicyUploadStart:function(){
			// summary:
			//		Mark the wizard as busy before uploading deployment policy
			// tags:
			//		protected
			var F = this.declaredClass + "._deploymentPolicyUploadStart()";
			console.debug(F);
			
			this.disable();
			
			this.showStandby();
		},
		
		
		_deploymentPolicyUploadComplete:function(/*Object*/result){
			// summary:
			//		Update the model with the urls of policy file.
			// description:
			//		If no result is provided, the deployment policy is unset
			// result: Object
			//		The result object returned by the uploader
			// tags:
			//		protected
			var F = this.declaredClass + "._deploymentPolicyUploadComplete()";
			console.debug(F,result);	
			
			var deploymentPolicy=null, grid = this.deploymentPolicyGrid;

			console.debug(F,"Model Before",this.model);
			
			if(result==null){
				console.debug(F,"Result is null");

				if(this.deploymentPolicySourceTypeSelect.get("value")==="none"){
					deploymentPolicy = {
						sourceType:	"none"
					};
				}
				
			}else{
				console.debug(F,"Creating deploymentPolicy");
				// Create a deployment Policy object
				deploymentPolicy = {
						sourceType:this.deploymentPolicySourceTypeSelect.get("value"),
						uri: result.files[0],
						domainName: this.deploymentPolicyDomainNameDataField.get("value"),
						policyName: this.deploymentPolicyPolicyNameDataField.get("value")
				};
				
				// If the deployment policy was from an existing domain, use the name from the select
				if(deploymentPolicy.sourceType==="existing"){
					deploymentPolicy.domainName = this.deploymentPolicyExistingDomainDataField.get("value");
				}
			}
			
			// Update Selected Appliances			
			array.forEach(grid.select.row.getSelected(),function(id){
				console.debug(F,"Creating deploymentPolicy for appliance",id);
				var appliance = grid.store.get(id);
				appliance.deploymentPolicy = lang.clone(deploymentPolicy);
			},this);
			
			
			// Finally, update the model with the contents of the store
			this.model.appliances = grid.store.data;
			
			console.debug(F,"Updated model",this.model,this);
			
			this._resetGrid(this.deploymentPolicyGrid);
			
			// Grid is reset, so let it know that it has no rows selcted.
			grid.select.row.clear();

			this.enable();
			
			this.hideStandby();
		},
		
		_deploymentPolicyPopulateExistingAppliance:function(domains){
			// summary:
			//		Populate the list of existing appliances
			// domains: Array
			//		The list of available domains
			// tags:
			//		protected
			var F = this.declaredClass + "._deploymentPolicyPopulateExisitingAppliance()";
			console.debug(F,domains);
			
			var appliances = [], options = [];
			
			this._availableDomains = domains;
			
			// Add placeholder option
			options.push({label: nls.domainConfigurationWizard.deploymentPolicy.nullAppliance, value: ""});
			
			array.forEach(domains,function(domain){
				if(array.indexOf(appliances,domain.applianceId)===-1){
					appliances.push(domain.applianceId);
					options.push({label:domain.applianceName,value: domain.applianceId});	
				}
			});
			
			this.deploymentPolicyExistingApplianceDataField.set("options", []);
			this.deploymentPolicyExistingApplianceDataField.addOption(options);
			
			this._deploymentPolicyPopulateExistingDomain("");
			
		},
		
		_deploymentPolicyPopulateExistingDomain:function(applianceId){
			// summary:
			//		Populate the list of existing domains from which to choose
			//		a deployment policy source
			// tags:
			//		protected
			var F = this.declaredClass + "._deploymentPolicyPopulateExisitingDomain()";
			console.debug(F,applianceId);
			
			var domains = this._availableDomains, options = [];
			
			options.push({label: nls.domainConfigurationWizard.deploymentPolicy.nullDomain, value: ""});
			
			array.forEach(domains,function(domain){
				if(domain.applianceId === applianceId){
					var l = domain.displayName,
						v = domain.name; 
					options.push({label:l,value: v});
				}
			});
			
			this.deploymentPolicyExistingDomainDataField.set("options", []);
			this.deploymentPolicyExistingDomainDataField.addOption(options);
		},
		
		_syncModeShowIntro:function(/*Number*/domainCount){
			// summary:
			//		Show the intro message for the sync mode panel, depending
			//		on how many domains are being modifid
			// tags:
			//		protected
			var F = this.declaredClass + "._syncModeShowIntro()";
			console.debug(F,domainCount);
			
			var msg = nls.domainConfigurationWizard.syncMode.intro;
			
			if(typeof domainCount==="number" && domainCount > 1){
				msg =nls.domainConfigurationWizard.syncMode.introPlural;
			}
			
			domAttr.set(this.syncModeIntroNode,"innerHTML",msg);
			
		}

	});

	return DomainConfigurationWizard;
});
