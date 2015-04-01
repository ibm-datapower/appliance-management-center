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
        "dojo/_base/json",
        "dojo/_base/lang",
        "dojo/_base/xhr",
        "dojo/aspect",
        "dojo/data/ItemFileReadStore",
        "dojo/DeferredList",
        "dojo/dom-attr",
        "dojo/dom-class",
        "dojo/dom-construct",
        "dojo/dom-style",
        "dojo/fx",
        "dojo/on",
        "dojo/store/Memory",
        "dojo/string",
        "dojo/topic",
        "dijit/form/Button",
        "dijit/form/Form",
        "dijit/form/RadioButton",
        "dijit/layout/ContentPane",
        "dijit/registry",
        "dijit/Tree",
        "dijit/tree/ForestStoreModel",
        "dijit/_TemplatedMixin",
        "dijit/_Widget",
        "dijit/_WidgetsInTemplateMixin",
        "dojox/form/Uploader",
        "dojox/widget/Standby",        "dojox/widget/WizardPane",
        "gridx/core/model/cache/Async",
        "gridx/Grid",
        "wamc/widget/form/Select",
        "wamc/widget/form/TextBox",
        "wamc/config/Resources",
        "wamc/grid/format",
        "wamc/grid/modules",
        "wamc/validation",
        "wamc/widget/Wizard",
        "wamc/widget/form/_FormElementIdMixin",
        "wamc/widget/form/UploaderValueTextBox",
        "wamc/_HelpLabelsMixin",
        "wamc/_MessagesMixin",
        "dojo/text!wamc/widget/wizard/templates/CreateServiceWizard.html",
        "dojo/i18n!wamc/nls/strings"],
        function(array,declare,Deferred,event,json,lang,xhr,aspect,
        		ItemFileReadStore,DeferredList,domAttr,domClass,domConstruct,
        		domStyle,fx,on,MemoryStore,string,topic,Button,Form,RadioButton,
        		ContentPane,registry,Tree,ForestStoreModel,_TemplatedMixin,
        		_Widget,_WidgetsInTemplateMixin,Uploader,Standby,WizardPane,Cache,
        		Grid,Select,TextBox,Resources,gridFormat,gridModules,
        		validation,Wizard,_FormElementIdMixin,UploaderValueTextBox,
        		_HelpLabelsMixin,_MessagesMixin,template,nls){
	
	var CreateServiceWizard = declare("wamc.widget.wizard.CreateServiceWizard",
			[_Widget,_TemplatedMixin,_WidgetsInTemplateMixin,_MessagesMixin,_HelpLabelsMixin,_FormElementIdMixin],{
		
		templateString:template,
		
		nls: nls,
		
		target:Resources.upload.url,
		
		postCreate:function(){
			// summary:
			//		Widget life-cycle function
			
			var F = this.declaredClass + ".postCreate()";
			console.debug(F);
			
			this.inherited(arguments);
			
			var wizard = this.wizard;
			
			console.debug(F, "Setup Widgets: configSourcePane");
			
			var configSourceGrid = this.configSourceGrid = new Grid({
				store: new MemoryStore({data:null}),
				cacheClass: Cache,
				structure:[
				           { id: "name", name: nls.createServiceWizard.configurationSource.grid.name, field: "name", width:"50%" },			        
				           { id: "applianceName", name: nls.createServiceWizard.configurationSource.grid.applianceName, field: "applianceName",width:"50%" }
				           ],
				modules:[{moduleClass:gridModules.Focus}],
				"aria-labelledby":this.id + "_configSource_heading"
				});
			configSourceGrid.placeAt(this.configSourceGridContainer);
			
			domClass.add(configSourceGrid.domNode,"compact");
			
			configSourceGrid.startup();
			
			console.debug(F, "Setup Widgets: selectServicePane");
			
			var selectServiceGrid = this.selectServiceGrid = new Grid({
				store: new MemoryStore({data:null}),
				cacheClass: Cache,
				structure:[
				           { id: "name", name: nls.createServiceWizard.selectService.grid.name, field: "name", width:"50%" },			        
				           { id: "applianceName", name: nls.createServiceWizard.selectService.grid.applianceName, field: "applianceName",width:"50%" }
				           ],
				modules:[{moduleClass:gridModules.Focus}],
				"aria-labelledby":this.id + "_selectService_heading"
				});
			selectServiceGrid.placeAt(this.selectServiceGridContainer);
			
			domClass.add(selectServiceGrid.domNode,"compact");
			
			selectServiceGrid.startup();
			
			console.debug(F, "Setup Widgets: deploymentPolicyPane");
			
			var deploymentPolicyGrid = this.deploymentPolicyGrid = new Grid({
				store: new MemoryStore({data:[]}),
				cacheClass: Cache,
				modules:[
				         {moduleClass:gridModules.ExtendedSelectRow,
				        	 triggerOnCell:true},
				        {moduleClass:gridModules.Focus},
				         ],
				structure:[
				           { id: "applianceName", name: nls.createServiceWizard.deploymentPolicy.grid.applianceName, field:"applianceName", width:"30%"},
				           { id: "name", name:nls.createServiceWizard.deploymentPolicy.grid.name, field:"name", width:"30%"},
				           { id: "policy", name: nls.createServiceWizard.deploymentPolicy.grid.policy, field: "deploymentPolicy",width:"40%",formatter:gridFormat.formatDeploymentPolicy}],
				'aria-label':nls.createServiceWizard.deploymentPolicy.grid.title
				});
			deploymentPolicyGrid.placeAt(this.deploymentPolicyGridContainer);
			this._setupGridSelectionEvents(deploymentPolicyGrid);
			
			domClass.add(deploymentPolicyGrid.domNode,"compact");
			
			deploymentPolicyGrid.startup();
			
			console.debug(F, "Setup Events: configSourcePane");
			
			this.configSourceForm.watch("state",function(property,oldValue,newValue){
				console.debug("Form State Changed",property,oldValue,newValue);
				wizard._checkButtons();
			});
			
			/* 
			 * Because the uploader doesn't affect the state of its parent 
			 * watch for change events separately. 
			 */
			aspect.after(this.configSourceLocalSourceUploader,"onChange",
					function(){
						wizard._checkButtons();
					}
			);
			
			// When the radio button changes, set the required attributes on fields
			on(this.configSourceLocalSourceRadioButton,"change",
					lang.hitch(this,function(newValue){
			
						if(newValue){
							this.configSourceLocalSourceUploader.set("disabled",false);
							this.configSourceLocalSourceUploader.set("required",true);
						}else{
							this.configSourceLocalSourceUploader.set("required",false);
							this.configSourceLocalSourceUploader.set("disabled",true);
							this.configSourceLocalSourceUploader.reset();
						}
						wizard._checkButtons();
					}
			));
			
			on(this.configSourceRemoteSourceRadioButton,"change",
					lang.hitch(this,function(newValue){	
						if(newValue){
							this.configSourceRemoteSourceDataField.set("disabled",false);
							this.configSourceRemoteSourceDataField.set("required",true);
						}else{
							this.configSourceRemoteSourceDataField.set("required",false);
							this.configSourceRemoteSourceDataField.set("disabled",true);
							this.configSourceRemoteSourceDataField.reset();
						}
					}
			));
			
			on(this.configSourceLocalSourceUploader,"complete",
					lang.hitch(this,"_configSourceUploadComplete"));
			
			console.debug(F, "Setup Events: selectServicePane");
			
			aspect.after(this.selectServicePane,"onShow",
					lang.hitch(this,function(){
						this._resetGrid(this.selectServiceGrid);
					}
			));
			
			console.debug(F, "Setup Events: deploymentPolicyPane");
			
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
			
			on(this.deploymentPolicyLocalFileDataField,"complete",
					lang.hitch(this,"_deploymentPolicyUploadComplete"));
			
			aspect.after(this.deploymentPolicyPane,"onShow",
					lang.hitch(this,function(){
						this._resetGrid(this.deploymentPolicyGrid);
					}
			));
			
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
			
			// Set up events for the wizard
			this.wizard.set("cancelFunction",
					lang.hitch(this,"cancel"));
	
			// Validation for Configuration Source Panel
			this.configSourcePane.set("passFunction",
					lang.hitch(this,"configSourcePassFunction"));

			this.configSourcePane.set("actionFunction",
					lang.hitch(this,"configSourceProcessForm"));
			
			// Validation for Select Service Panel
			this.selectServicePane.set("passFunction",
					lang.hitch(this,"selectServicePassFunction"));
			
			this.selectServicePane.set("actionFunction",
					lang.hitch(this,"selectServiceProcessForm"));
			
			// Validation for Deployment Policy Panel
			this.deploymentPolicyPane.set("passFunction",
					lang.hitch(this,"deploymentPolicyPassFunction"));

			this.deploymentPolicyPane.set("actionFunction",
					lang.hitch(this,"_deploymentPolicyActionFunction"));
			
			//Wizard 'done' function
			
			this.serviceImpactPane.set("doneFunction",
					lang.hitch(this,"done"));
			
			
			// Clear any error meessages before moving to next panel
			aspect.before(wizard,"forward",lang.hitch(this,"hideMessage"));
			
		},
		
		startup:function(){
			var F = this.declaredClass + ".startup()";
			console.debug(F);
			
			this.inherited(arguments);
			
			// If this wizard appears in a dialog, have the overlay cover the entire dialog
			var parent = registry.getEnclosingWidget(this.domNode.parentNode);
			
			if(parent && parent instanceof dijit.Dialog){
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
			
			// Config Source Pane
			this._resetGrid(this.configSourceGrid,
					new MemoryStore({data:model.domains,idProperty:"primaryKey"}));
			
			// Select Service Pane
			this._resetGrid(this.selectServiceGrid,
					new MemoryStore({data:model.domains,idProperty:"primaryKey"}));
			
			this._selectServiceShowSelection();
			
			// Deployment Policy
			this._resetGrid(this.deploymentPolicyGrid,
					new MemoryStore({data:model.domains,idProperty:"primaryKey"}));
			
			// Service Impact
			this._serviceImpactUpdateImpact();
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
			
			// Config Source Pane
			var form = this.configSourceForm.get("value");
			
			delete form.localFile;
			
			if(form.sourceType==="local"){
				delete form.uri;
			}
			
			lang.mixin(this.model,form);
			
			
			// Select Service Pane
			var name = this.selectServiceServiceSelect.get("value");
			
			// Filter should result in a single service from the model
			var sa = array.filter(this.model.services,function(item,index){
				return item.name === name;
			});
			
			this.model.name = sa[0].name;
			this.model.serviceClass = sa[0].className;
			
			// No updates necessary for Deployment Policy or Service Impact
			
		},
		
		passFunction:function(){
			// summary:
			//		Validation function to check that the wizard is ok
			var F = this.declaredClass + ".passFunction()";
			console.debug(F);
			
			return (this.selectServicePassFunction()===true &&
					this.deploymentPolicyPassFunction()===true);
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
			
			this.onDone();

		},

		reset:function(){
			// summary:
			//		Reset all panes of this wizard
			var F = this.declaredClass + ".reset()";
			console.debug(F);
			
			
			this.hideMessage();
			this.configSourceLocalSourceRadioButton.set("value",true);
			this.configSourceRemoteSourceRadioButton.set("value",false);
			this.configSourceLocalSourceUploader.reset();
			this.configSourceRemoteSourceDataField.reset();
			
			this.selectServiceServiceSelect.reset();
			
			
			//this.hideMessage();
			this.deploymentPolicySourceTypeSelect.reset();
			this._deploymentPolicyHideDetails();
			this._deploymentPolicyConfigureDetails("none");
			
			if(this.serviceImpactTree){
				this.serviceImpactTree.destroyRecursive();
			}
			
			domAttr.set(this.serviceImpactNoImpactNode,"innerHTML","");
			
			this._serviceImpactHideWarning();
						this.wizard.selectChild(this.selectServicePane);
			this.wizard.selectChild(this.configSourcePane);
		},
		
		resize:function(){
			// summary:
			//		Overridden to make sure that grids are resized whenever this
			//		widget is notified of a size change
			
			var F = this.declaredClass + ".resize()";
			console.debug(F);
			
			this.inherited(arguments);
			this.wizard.resize(arguments);
			this._resetGrid(this.configSourceGrid);
			this._resetGrid(this.selectServiceGrid);
			this._resetGrid(this.deploymentPolicyGrid);
			
		},
		
		cancel:function(){
			var F = this.declaredClass + ".cancel()";
			console.debug(F);
			
			this.onCancel();
		},
		
		
		disable: function(){
			var F = this.declaredClass + ".disable()";
			console.debug(F);
			
			// Disable buttons
			this.wizard.doneButton.set("disabled",true);
			this.wizard.cancelButton.set("disabled",true);
			this.wizard.previousButton.set("disabled",true);
			this.wizard.nextButton.set("disabled",true);
		},
		
		enable: function(){
			var F = this.declaredClass + ".enable()";
			console.debug(F);
			
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

		configSourcePassFunction:function(){
			// summary:
			//		Check that the user should be allowed to proceed to the next page
			// description:
			//		Because this form uses inputs and radio buttons, form.validate is
			//		not sufficient, and we need to write our own custom check
			// return: Boolean
			//		True if the user can continue
			
			var F = this.declaredClass + ".configSourcePassFunction()";
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
			
			return valid;
		},
		
		configSourceProcessForm:function(){
			// summary:
			//		Process the submitted form
			// description:
			//		Starts the upload process and disables the form until
			//		upload is complete and the model is updated.
			// return: Deferred
			//		A deferred that resolves to success/failure
			//		of upload process
			var F = this.declaredClass + ".configSourceProcessForm()";
			console.debug(F);
			
			// Trigger upload and retrieval of services
			this.standby.show();
			
			// create a deferred that will track the upload/service retrieval.
			this._uploadDeferred = new Deferred();
			
			// If local, do the upload
			if(this.configSourceLocalSourceRadioButton.get("value")){
				console.debug(F,"Using Local File");
				this.configSourceUpload();
			}else{
				var url = this.configSourceRemoteSourceDataField.get("value");
				console.debug(F,"Using Remote File",url);
				// For remote files, we already have the file url
				this._configSourceUploadComplete({files:[url]});
			}
			return this._uploadDeferred;
		},
		
		configSourceUpload:function(){
			// summary:
			//		Upload the file specified by the local uploader
			var F = this.declaredClass + ".configSourceUpload()";
			console.debug(F);
			
			this.configSourceLocalSourceUploader.upload();
		},
		
		_configSourceUploadComplete:function(/*Object*/result){
			// summary:
			//		Upload the model with the urls of uploaded local file
			// result: Object
			//		Object containineg the list of files returned by the server
			// tags:
			//		protected
			var F = this.declaredClass + "._configSourceUploadComplete()";
			console.debug(F,result);			
			
			console.debug(F,"Model at start",this.model);
			
			var uri = this.model.uri = result.files[0],
				applianceIds = array.map(this.model.domains,
						function(d){
							return d.applianceId;
				}),
				success=false;
			
			// Now that that we have a uri, use it to get the list of services
			// in the file.
			Deferred.when(this._configSourceGetServiceList(applianceIds, uri),
					lang.hitch(this,function(services){
					
						if(services.length === 0){
							this.showMessage("ERROR_NO_CONTAINED_SERVICES",{},true);
							success = false;
							this.wizard._checkButtons();
						}else{
							// Update the model
							this.model.services = services;
							
							// Populate the service list
							this._selectServiceShowSelection();
							
							success=true;
						}
						
						// Resolve the deferred
						this.standby.hide();
						
						if(this._uploadDeferred){
							this._uploadDeferred.resolve(success);
							delete this._uploadDeferred;
						}
					}),
					lang.hitch(this,function(error){
						this.showServerResponse(error.responseText);
						
						success=false;
						
						if(this._uploadDeferred){
							this._uploadDeferred.resolve(success);
							delete this._uploadDeferred;
						}
						
						this.wizard._checkButtons();
						
						this.standby.hide();
					})
				);
		},
		
		_configSourceGetServiceList:function(/*Array*/applianceIds,/*String*/uri){
			// summary:
			//		Get the list of service configurations in a file
			//		At a given uri
			// return: Deferred
			//		A deferred that resolves with the list of services in a file
			// tags: 
			//		protected
			var F = this.declaredClass + "._configSourceGetServiceList()";
			console.debug(F,applianceIds,uri);
			
			var xhrArgs = {
					url:Resources.serviceContents.url,
					handleAs:"json",
					content:{
						applianceId:applianceIds,
						serviceSource:uri
					}
			};
			
			return xhr.get(xhrArgs);
		},
		
		selectServicePassFunction:function(){
			var F = this.declaredClass + ".selectServicePassFunction()";
			console.debug(F);
			
			if (this.selectServiceForm.isValid()) {
				console.debug(F,"Form Valid");
				return true;
			} else {
				console.debug(F,"Form Not Valid");
				return false;
			}
		},
		
		selectServiceProcessForm:function(){
			// summary:
			//		Update the model once
			// return: Boolean
			//		true if update was successful
			var F = this.declaredClass + ".selectServiceProcessForm()";
			console.debug(F);
			
			var name = this.selectServiceServiceSelect.get("value");
			
			// Filter should result in a single service from the model
			var sa = array.filter(this.model.services,function(item,index){
				return item.name === name;
			});
			
			this.model.name = sa[0].name;
			this.model.serviceClass = sa[0].className;
			
			return true;
		},
		
		_selectServiceShowSelection:function(){
			// summary:
			//		Populate the serviceSelect with
			//		Options from the model
			// tags:
			//		protected
			var F = this.declaredClass + "_selectServiceShowSelection()";
			console.debug(F);
			
			var model = this.model;
			
			if(model.services){
				var options = [];
				array.forEach(model.services,function(service){
					options.push({"label":service.name + ", " +service.classDisplayName ,"value":service.name});
				});
				this.selectServiceServiceSelect.set("options",[]);
				this.selectServiceServiceSelect.addOption(options);
				this.selectServiceServiceSelect.validate(false);
			}
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
			var F = this.declaredClass + "._deploymentPolicyValidate()";
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
				
				var enable = [], disable=[], form = "deploymentPolicy";
				
				switch (value) {
				case "local":
					enable = ["LocalFile",
					          "DomainName",
					          "PolicyName"];
					
					disable = ["RemoteLocation"];
					break;
				case "remote":
					enable = ["RemoteLocation",
					          "DomainName",
					          "PolicyName"];
					
					disable = ["LocalFile"];
					break;
				default /*None*/:
					
					disable = ["LocalFile",
					           "RemoteLocation",
					           "DomainName",
					           "PolicyName"];
					break;
				}
				
				array.forEach(enable,function(n){
					console.debug(F,"Enabling",n);
					var row = this[form + n + "Row"],
						input = this[form + n +"DataField"];
					
					domClass.remove(row,"hidden");
					input.set("required",true);
				},this);
				
				array.forEach(disable,function(n){
					console.debug(F,"Disabling",n);
					var row = this[form + n + "Row"],
					input = this[form + n +"DataField"];
					
					input.set("required",false);
					domClass.add(row,"hidden");
				},this);
				
				// 3 - Reset fields
				this.deploymentPolicyLocalFileDataField.reset();
				this.deploymentPolicyRemoteLocationDataField.reset();
				this.deploymentPolicyDomainNameDataField.reset();
				this.deploymentPolicyPolicyNameDataField.reset();
				
				fx.wipeIn(wipeArgs).play();
			}));
			
			anim.play();
			
		},
		
		deploymentPolicyUpload:function(){
			// summary:
			//		Upload the file specified by the local uploader
			var F = this.declaredClass + ".deploymentPolicyUpload()";
			console.debug(F);
			
			this.standby.show();
			
			this.deploymentPolicyLocalFileDataField.upload();
			
		},
		
		_deploymentPolicyUploadComplete:function(/*Object*/result){
			// summary:
			//		Update the model with the urls of policy file.
			// description:
			//		If no result is provided, the deployment policy is unset
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
			}
			
			// Update Selected Appliances			
			array.forEach(grid.select.row.getSelected(),function(id){
				console.debug(F,"Creating deploymentPolicy for domain",id);
				var domain = grid.store.get(id);
				domain.deploymentPolicy = lang.clone(deploymentPolicy);
			},this);
			
			
			// Finally, update the model with the contents of the store
			this.model.domains = grid.store.data;
			
			console.debug(F,"Updated model",this.model,this);
			
			this._resetGrid(grid);
			
			// Grid is reset, so let it know that it has no rows selcted.
			grid.select.row.clear();

			this.enable();
			
			this.standby.hide();
			
		},
		
		_deploymentPolicyActionFunction:function(){
			// summary:
			//		Action to perform when moving between deploymentPolicy
			//		and serviceImpact Panes
			// tags:
			//		protected
			var F = this.declaredClass + "._deploymentPolicyActionFunction()";
			console.debug(F);
			
			// Show the standby
			this.standby.show();
			
			// Now, get the impact of deploying the service with this config
			var self = this, defs = [], model = this.model, failed=false;
			
			array.forEach(model.domains,function(domain){
				defs.push(self._deploymentPolicyGetServiceImpact(domain.applianceId,
						model.serviceClass,model.name,model.uri,domain));
			});
			
			var dl = new DeferredList(defs);
			
			// Update the model, and hide the standby
			
			var def =  Deferred.when(dl,function(results){				
				return array.map(results,function(result){
					if(!result[0]){
						console.debug(F,"Result failed!");
						failed=true;
					}
					return result[1];
				});
			});
			
			var d2 = Deferred.when(def,function(results){
				
				if(failed){
					
					self.showMessage("ERROR_SERVICE_IMPACT");
					self.standby.hide();
					return false;
				}
				
				var impact = [];
				array.forEach(results,function(result){
					impact = impact.concat(result);
				});
				
				self.model.impacts = impact;
				self._serviceImpactUpdateImpact();
				self.standby.hide();
				return true;
			});
			
			return d2;
		},
		
		_deploymentPolicyGetServiceImpact:function(/*String*/applianceId,
				/*String*/serviceClass,/*String*/serviceName,
				/*String*/configUri,/*String*/domain){
			// summary:
			//		Get the impact of deploying a service configuration on
			//		an appliance with a given serial number
			// applianceId: String
			//		The identifier of the appliance
			// serviceClass: String
			//		The type of service
			// serviceName: String
			//		The given name of the service
			// configUri: String
			//		The URI of the configuration source
			// domain: String
			//		The name of the domain being deployed to.
			// tags:
			//		protected
			var F = this.declaredClass + "._deploymentPolicyGetServiceImpact()";
			console.debug(F,applianceId,serviceClass,serviceName,configUri,domain);
			
			// Construct the URL
			var url = string.substitute(
					Resources.serviceImpact.url,{
						applianceId:applianceId,
						domainName:domain.name,
						className:serviceClass,
						name:serviceName
					});
			
			console.debug(F,"URL",url);
			
			var pd = {
					configLocation:configUri,
					importAllFiles:true
				};
			
			if(domain.deploymentPolicy){
				pd.policyLocation=domain.deploymentPolicy.uri;
				pd.policyDomain=domain.deploymentPolicy.domainName;
				pd.policyName=domain.deploymentPolicy.policyName;
			}
			
			// Construct XHR args
			var xhrArgs = {
					url:url,
					handleAs:"json",
					postData:json.toJson(pd),
					headers:{
						"Content-Type": "application/json",
						Accept: "application/javascript, application/json"
					}
			};
			
			// Return the deferred for the calling function
			return xhr.post(xhrArgs);
			
		},
		
		_serviceImpactUpdateImpact:function(){
			// summary: 
			//		Update the service impact display based on the current model
			// tags:
			//		protected
			var F = this.declaredClass + "._serviceImpactUpdateImpact()";
			console.debug(F);
			
			// Hide no impact node
			domAttr.set(this.serviceImpactNoImpactNode,"innerHTML","");
			
			// Destroy any existing impact tree(s)
			array.forEach(registry.findWidgets(this.serviceImpactTreeNode),
					function(tree){
						tree.destroyRecursive();
					});
			
			this._serviceImpactHideWarning();
			
			if(this.model.impacts && this.model.impacts.length > 0){
				
				// Copy the model: we don't want it ending up in the store where
				// it can be modified
				var m = lang.clone(this.model);
				
				this._serviceImpactShowImpact(m);
			
			}else{
				this._serviceImpactShowNoImpact();
			}
		},
		
		_serviceImpactShowImpact:function(/*Object*/data){
			// summary: 
			//		Display the impact tree
			// data:
			//		The model to show the impact of
			// tags:
			//		protected
			var F = this.declaredClass + "._serviceImpactShowImpact():";
			console.debug(F, data);
			
			var newData = this._serviceImpactStructureTreeData(
					this._serviceImpactConsolidateServiceData(data.impacts));
			
			var store = new ItemFileReadStore({
				data:{
					items:newData,
					label:"name",
					identifier:"id"
				}
			});
			
			var treeModel = new ForestStoreModel({
		        store: store,
		        rootId: "root",
		        rootLabel: "Impact",
		        childrenAttrs: ["files","objects"]
		    });
			
			// Create a new Tree
			var t = this.tree = new Tree({
				model:treeModel,
				showRoot:false,
				getIconClass: this.serviceImpactGetIconClass,
				_onNodeMouseEnter:this.serviceImpactOnNodeMouseEnter,
				_onNodeMouseLeave:this.serviceImpactOnNodeMouseLeave
			});
			
			t.placeAt(this.serviceImpactTreeNode);
			
			t.startup();
			
			this._serviceImpactShowWarning(data.name);
		},
		
		_serviceImpactShowNoImpact:function(){
			// summary: 
			// 		Display no-impact messages
			// tags:
			//		protected
			var F = this.declaredClass + "._serviceImpactShowNoImpact()";
			console.debug(F);
			
			domAttr.set(this.serviceImpactNoImpactNode,"innerHTML",nls.createServiceWizard.serviceImpact.noImpact);
		},
		
		_serviceImpactConsolidateServiceData:function(/*Array*/data){
			// summary:
			//		Goes through a list of impact data and consolidate it by
			//		service name and type. The list of appliances and domains
			//		that the service is deployed to goes into an deployments 
			//		property.
			// return: Array
			//		The consolidated data
			// tags:
			//		protected
			var F = this.declaredClass + "._serviceImpactConsolidateServiceData()";
			console.debug(F, data);
			
			var consolidated = [];
			
			array.forEach(data,function(item){
				//check to see if this service is already consolidated
				if(array.some(consolidated,function(ci){
					return ci.name===item.name && ci.type === item.type;
				})){
					//ignore this item
				}else{
					//Get an array of the services with the same name on all 
					var thisService = array.filter(data,function(ti){
						return ti.name===item.name && ti.type === item.type;
					});
					
					var oldItem = thisService [0],
						newItem = {
							name: oldItem.name,
							type: oldItem.type,
							files: oldItem.files,
							objects: oldItem.objects,
							deployments: []
					};
					
					array.forEach(thisService,function(it){
						newItem.deployments.push({
							applianceName:it.applianceName,
							domainName:it.domainName});
					},this);
					
					consolidated.push(newItem);
					
				}
				
			},this);
			
			return consolidated;
		},
		
		_serviceImpactStructureTreeData:function(/*Array*/ data){
			// summary:
			//		Restructure data for display in the tree grid
			// tags:
			//		protected
			var F = this.declaredClass + "._serviceImpactStructureTreeData():";
			console.debug(F, data);

			var i = 0;
			
			var newImpacts = array.map(data,function(impact){
				
				var ni = {
						id:i++,
						type:"service",
						name:impact.name + " (" + impact.type +")",
						serviceName:impact.name,
						serviceType:impact.type,
						deployments:impact.deployments
					};

				ni.files = array.map(impact.files,function(file){
					return {
						id:i++,
						type:"file",
						name:file + " (" + nls.createServiceWizard.serviceImpact.file + ")"
					};
				},this);
				
				ni.objects = array.map(impact.objects,function(obj){
					return{
						id:i++,
						type:"object",
						name:obj.name + " ("+ obj.type +")",
						objectName:obj.name,
						objectType:obj.type
					};
				},this);
				
				ni.deployments = array.map(impact.deployments,function(dep){
					return{
						id:i++,
						type:"deployment",
						name:dep.applianceName,
						applianceName:dep.applianceName,
						domainName:dep.domainName
					};
				},this);
				
				return ni;
				
			},this);
			
			console.debug(F,"exiting",newImpacts);
			
			return newImpacts;
		},
		
		_serviceImpactShowWarning:function(/*String*/serviceName){
			// summary:
			//		Show the warning message for this service
			// serviceName: String
			//		The name of the service to put in the warning message
			// tags:
			//		protected
			var F = this.declaredClass + "._serviceImpactShowWarning()";
			console.debug(F, serviceName);
			
			//setup warning message
			var msg = string.substitute(nls.createServiceWizard.serviceImpact.warning,{name:serviceName});
			domAttr.set(this.serviceImpactWarningMessage,"innerHTML",msg);
			domClass.remove(this.serviceImpactWarningNode,"hidden");
		},
		
		_serviceImpactHideWarning:function(/*String*/serviceName){
			// summary:
			//		Hide the warning message
			// tags:
			//		protected
			var F = this.declaredClass + "._serviceImpactHideWarning()";
			console.debug(F);			
			
			domClass.add(this.serviceImpactWarningNode,"hidden");
			domAttr.set(this.serviceImpactWarningMessage,"innerHTML","");
		},
		
		serviceImpactGetIconClass: function(/*dojo.data.Item*/ item, /*Boolean*/ opened){
			//  summary:
			//		Override function for styling tree nodes
			//	item: dojo.data.Item
			//		The item from the trees model store
			//	opened: Boolean
			//		Is this item expanded?
			
			// Is this a root node?
			if (!item || this.model.mayHaveChildren(item)){
				// If so, check whether it is expanded
				return "wamcServiceNone";
			}else {
				// Otherwise, check the type and return the appropriate icon
				
				var iconClass = "", type = this.model.store.getValue(item,"type");
				switch (type){
					case "file" : 
						iconClass = "dijitIconFile";
						break;
					case "object":
						iconClass = "dijitIconPackage";
						break;
					default: 
						iconClass = "dijitLeaf";
				}
				return iconClass;
			}
		},
		
		serviceImpactOnNodeMouseEnter: function(/*dijit._Widget*/ node){
			// summary:
			//		Use to override dijit.Tree method to show HoverHelp for
			//		service nodes
			var item = node.item, type = this.model.store.getValue(item,"type");
			if(type==="service"){
				var tooltip = string.substitute(nls.createServiceWizard.serviceImpact.serviceTooltip,{serviceName:item.serviceName,serviceType:item.serviceType});
				tooltip += "<ul>";
				array.forEach(item.deployments,function(deployment){
					tooltip += "<li>";
					tooltip += string.substitute(nls.createServiceWizard.serviceImpact.serviceTooltipListItem,{appliance:deployment.applianceName,domain:deployment.domainName});
				});
				tooltip += "</ul>";
				dijit.showTooltip(tooltip,node.domNode);
			}
		},

		serviceImpactOnNodeMouseLeave: function(/*dijit._Widget*/ node){
			// summary:
			//		For overriding the tree method and removing hoverhelp
			dijit.hideTooltip(node.domNode);
		}
		
	});

	return CreateServiceWizard;
});
