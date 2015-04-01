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
        "dojo/_base/event",
        "dojo/_base/lang",
        "dojo/aspect",
        "dojo/dom-construct",
        "dojo/on",
        "dojo/string",
        "dijit/_TemplatedMixin",
        "dijit/_Widget",
        "dijit/_WidgetsInTemplateMixin",
        "dijit/form/Button",
        "dijit/form/Form",
        "dijit/form/RadioButton",
        "dijit/registry",
        "dojox/form/Uploader",
        "dojox/widget/Standby",        "dojox/widget/WizardPane",
        "wamc/widget/form/TextBox",
        "wamc/_HelpLabelsMixin",
        "wamc/config/Resources",
        "wamc/widget/Wizard",
        "wamc/widget/form/_FormElementIdMixin",
        "wamc/widget/form/UploaderValueTextBox",
        "wamc/validation",
        "dojo/i18n!wamc/nls/strings",
        "dojo/text!wamc/widget/wizard/templates/UploadFileWizard.html"],
        function(array,declare,event,lang,aspect,domConstruct,on,string,
        		_TemplatedMixin,_Widget,_WidgetsInTemplateMixin,Button,Form,
        		RadioButton,registry,Uploader,Standby,WizardPane,TextBox,
        		_HelpLabelsMixin,Resources,Wizard,
        		_FormElementIdMixin,UploaderValueTextBox,wamcValidation,nls,
        		template){
        
	var UploadFileWizard = declare("wamc.widget.wizard.UploadFileWizard",
			[_Widget,_TemplatedMixin,_WidgetsInTemplateMixin,_FormElementIdMixin,_HelpLabelsMixin],{
		
		templateString:template,
		
		nls: nls,
		
		target:Resources.upload.url,
		
		postCreate:function(){
			// summary:
			//		Widget life-cycle function
			
			var F = this.declaredClass + ".postCreate()";
			console.debug(F);
			
			this.inherited(arguments);
			
			console.debug(F, "Setup Events: sourcePane");
			
			this.sourceForm.watch("state",lang.hitch(this,"_formStateChanged"));
			
			console.debug(F, "Setup Events: destinationPane");
			
			this.destinationForm.watch("state",lang.hitch(this,"_formStateChanged"));
			
			/* 
			 * Because the uploader doesn't affect the state of its parent 
			 * watch for change events separately. 
			 */
			aspect.after(this.sourceLocalSourceUploader,"onChange",lang.hitch(this,"_formStateChanged"));
			
			// When the radio button changes, set the required attributes on fields
			on(this.sourceLocalRadioButton,"change",lang.hitch(this,function(newValue){
				console.debug("Config Source Local radio changed",newValue);
				
				if(newValue){
					// Enable this section
					this.sourceLocalSourceUploader.set("disabled",false);
					this.sourceLocalSourceUploader.set("required",true);
					this._formStateChanged();//Form doesn't change state automatically
				}else{
					this.sourceLocalSourceUploader.set("required",false);
					this.sourceLocalSourceUploader.set("disabled",true);
					this.sourceLocalSourceUploader.reset();
				}
			}));
			
			on(this.sourceRemoteRadioButton,"change",lang.hitch(this,function(newValue){
				console.debug("Config Source Remote radio changed",newValue);
				
				if(newValue){
					//Enable this section
					this.sourceRemoteSourceDataField.set("disabled",false);
					this.sourceRemoteSourceDataField.set("required",true);
				}else{
					this.sourceRemoteSourceDataField.set("required",false);
					this.sourceRemoteSourceDataField.set("disabled",true);
					this.sourceRemoteSourceDataField.reset();
				}
				
			}));
			
			// When the uploader completes, call _uploadComplete to update the model
			on(this.sourceLocalSourceUploader,"complete",lang.hitch(this,this._uploadComplete));

			console.debug(F, "Setup events: Wizard Level");
			
			this.wizard.set("cancelFunction",
					lang.hitch(this,"cancel"));
			
			// Validation for Source Panel
			this.sourcePane.set("passFunction",
					lang.hitch(this,"sourcePassFunction"));
			
			this.sourcePane.set("actionFunction",
					lang.hitch(this,"sourceActionFunction"));
			
			// Validation for Destination Panel
			this.destinationPane.set("passFunction",
					lang.hitch(this,"destinationPassFunction"));
			
			//Wizard 'done' function
			this.destinationPane.set("doneFunction",
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
			if(!this.sourceLocalSourceUploader.supports("multiple")){

				domConstruct.create("input",
						{name:"wrapResponse",value:"true",type:"hidden"},
						this.id + "_sourceForm","first");
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

			var form = this.sourceForm.get("value");
			
			if(form.sourceType==="local"){
				delete form.uri;
			}
			
			delete form.localFile;
			
			this.model = lang.mixin(this.model,form);
			
			this.model = lang.mixin(this.model,this.destinationForm.get("value"));
		},
		
		passFunction:function(){
			// summary:
			//		Validation function to check that the wizard is ok
			var F = this.declaredClass + ".passFunction()";
			console.debug(F);
			
			return (this.sourcePassFunction()===true && 
					this.destinationPassFunction()===true);
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
			
			var source = this.sourceLocalRadioButton.get("value");
			
			// If using a local file, upload
			if(source==="local"){
				console.debug(F,"Local SourceType",source);
				this.uploadStart();
				this.sourceLocalSourceUploader.upload({"referenceCount":this.model.domains.length});
			}else{
				console.debug(F,"Non-local SourceType",source);
				var remoteSource = this.sourceRemoteSourceDataField.get("value");
				this._uploadComplete({files:[remoteSource]});
				
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
			
			this.model.source = result.files[0];
			
			console.debug(F,"updated model",this.model);
			
			this.uploadComplete(result);
		},

		reset:function(){
			// summary:
			//		Reset all panes of this wizard
			
			var F = this.declaredClass + ".reset()";
			console.debug(F);
			
			this.sourceForm.reset();
			this.sourceLocalRadioButton.set("checked",true);
		
			this.destinationForm.reset();
						this.wizard.selectChild(this.destinationPane);
			this.wizard.selectChild(this.sourcePane);
		},
		
		resize:function(){
			// summary:
			//		Overridden to make sure that grids are resized whenever this
			//		widget is notified of a size change
			
			var F = this.declaredClass + ".resize()";
			console.debug(F);
			
			this.inherited(arguments);
			this.wizard.resize(arguments);
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
		
		showStandby:function(/*String*/ message){
			// summary:
			//		Show the standby overlay with the provided message
			// message: String
			//		The message to show on the standby
			// substitutes: Object
			//		the key/value pairs of subsitutes for the message
			
			var msg = message || nls.uploadFileWizard.standbyText;
			
			this.standby.set("text",msg);
			this.standby.show();
		},
		
		hideStandby:function(){
			// summary
			//		Hide the standby overlay
			
			this.standby.hide();
		},
		
		/** Functions for SourcePane **/
		
		sourcePassFunction: function(){
			// summary:
			//		Validation function for the source pane
			// description:
			//		Because this form uses inputs and radio buttons, form.validate is
			//		not sufficient, and we need to write our own custom check
			// return: Boolean
			//		True if the user can continue, otherwise false
			var F = this.declaredClass + ".sourcePassFunction()";
			console.debug(F);
			
			var f = this.sourceForm.get("value"),
				valid = true;
			
			if(f.sourceType==="local"){
				console.debug(F,"Validating Local Source");
				valid = !(this.sourceLocalSourceUploader.get("value").length===0);
			}

			else if(f.sourceType==="remote"){
				console.debug(F,"Validating Remote Source");
				//this.sourceRemoteSourceDataField.validate();
				valid = this.sourceRemoteSourceDataField.isValid();
			}
			
			return valid;
		},
		
		sourceActionFunction:function(){
			// summary:
			//		Prepopulate the filename field 
			var F = this.declaredClass + ".sourceActionFunction()";
			console.debug(F);
			
			if(this.sourceLocalRadioButton.get("value")){
				var files = this.sourceLocalSourceUploader.get("value");
				this.destinationFileNameDataField.set("value",files[0].name);
				this.destinationFileNameDataField.validate();
			}
			if(this.sourceRemoteRadioButton.get("value")){
				var url = this.sourceRemoteSourceDataField.get("value");
				// Take everything after the last slash as the filename; this
				// could pick up query and fragment parts as well, but they're
				// unlikely for a file being hosted and can always be removed 
				// on the next page if necessary.
				var filename = /\/([^\/]*)$/.exec(url)[1];
				this.destinationFileNameDataField.set("value", filename);
				this.destinationFileNameDataField.validate();
			}
			
			return true;
		},
		
		/** Functions for Destination Pane **/

		destinationPassFunction:function(){
			// summary:
			//		Validation function for the destination pane
			// return: Boolean
			//		True if the user can continue, otherwise false
			
			var F = this.declaredClass + ".destinationPassFunction()";
			console.debug(F);
			if (this.destinationForm.isValid()) {
				console.debug(F,"Form Valid");
				return true;
			} else {
				console.debug(F,"Form Not Valid");
				// Shown errors
				//this.createDomainForm.validate();
				return false;
			}
		}
	});

	return UploadFileWizard;
});
