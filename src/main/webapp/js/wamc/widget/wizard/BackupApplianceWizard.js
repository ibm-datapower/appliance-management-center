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
        "dojo/dom-class",
        "dojo/dom-construct",
        "dojo/on",
        "dojo/string",
        "dijit/_TemplatedMixin",
        "dijit/_Widget",
        "dijit/_WidgetsInTemplateMixin",
        "dijit/form/Form",
        "dijit/form/RadioButton",
        "dijit/registry",
        "dojox/form/Uploader",
        "dojox/widget/Standby",        "dojox/widget/WizardPane",        "wamc/widget/form/NumberTextBox",
        "wamc/widget/form/TextBox",
        "wamc/_HelpLabelsMixin",
        "wamc/_MessagesMixin",
        "wamc/config/Resources",
        "wamc/widget/form/UploaderValueTextBox",
        "wamc/widget/Wizard",
        "wamc/validation",
        "dojo/i18n!wamc/nls/strings",
        "dojo/text!wamc/widget/wizard/templates/BackupApplianceWizard.html"],
        function(array,declare,event,lang,aspect,domClass,domConstruct,on,string,
        		_TemplatedMixin,_Widget,_WidgetsInTemplateMixin,Form,
        		RadioButton,registry,Uploader,Standby,WizardPane,NumberTextBox,
        		TextBox,_HelpLabelsMixin,_MessagesMixin,Resources,
        		UploaderValueTextBox,Wizard,wamcValidation,nls,template){
        
	var BackupApplianceWizard = declare("wamc.widget.wizard.BackupApplianceWizard",
			[_Widget,_TemplatedMixin,_WidgetsInTemplateMixin,_MessagesMixin,_HelpLabelsMixin],{
		
		templateString:template,
		
		webGuiLinkTemplateString:"<a class=\"linktoWebGUI\" href=\"${applianceUrl}\" onclick=\"window.open('${applianceUrl}','${applianceName}','width=760,height=500,scrollbars=yes,resizable=yes,location=yes,status=yes',true);return false;\">${linkText}</a>",
		
		nls: nls,
		
		target:Resources.upload.url,
		
		postCreate:function(){
			// summary:
			//		Widget life-cycle function
			
			var F = this.declaredClass + ".postCreate()";
			console.debug(F);
			
			this.inherited(arguments);
			
			var wizard = this.wizard;
			
			console.debug(F, "Setup Events: cryptoPane");
			
			this.cryptoForm.watch("state",
					lang.hitch(this,"_formStateChanged"));
			
			/* 
			 * Because the uploader doesn't affect the state of its parent 
			 * watch for change events separately. 
			 */
			aspect.after(this.cryptoLocalFileDataField,"onChange",
					function(){
						wizard._checkButtons();
					}
			);
			
			on(this.cryptoCertificateNameRadioButton,"click",
					lang.hitch(this,"_cryptoSourceClicked"));
			
			on(this.cryptoLocalFileRadioButton,"click",
					lang.hitch(this,"_cryptoSourceClicked"));
			
			on(this.cryptoRemoteSourceRadioButton,"click",
					lang.hitch(this,"_cryptoSourceClicked"));
			
			// When the uploader completes, call _uploadComplete to update the model
			on(this.cryptoLocalFileDataField,"complete",
					lang.hitch(this,this._uploadComplete));

			on(this.cryptoLocalFileDataField,"error",
					lang.hitch(this,this._uploadError));
			
			console.debug(F, "Setup Events: locationPane");
			
			this.locationForm.watch("state",
					lang.hitch(this,"_formStateChanged"));
			
			on(this.locationZipFileRadioButton,"click",
					lang.hitch(this,"_locationDestinationClicked"));
			
			on(this.locationApplianceLocalFileRadioButton,"click",
					lang.hitch(this,"_locationDestinationClicked"));
			
			on(this.locationApplianceTemporaryFileRadioButton,"click",
					lang.hitch(this,"_locationDestinationClicked"));
			
			on(this.locationFtpRadioButton,"click",
					lang.hitch(this,"_locationDestinationClicked"));
			
			aspect.after(this.locationFtpUserPasswordDataField,"onChange",
					lang.hitch(this,"_locationFtpUserPasswordChanged"),true);
			
			console.debug(F, "Setup events: Wizard Level");
			
			this.wizard.set("cancelFunction",
					lang.hitch(this,"cancel"));
			
			// Validation for Crypto Panel
			this.cryptoPane.set("passFunction",
					lang.hitch(this,"cryptoPassFunction"));
			
			// Validation for Location Panel			
			this.locationPane.set("passFunction",
					lang.hitch(this,"locationPassFunction"));
			
			// No validation on Options Pane
			
			//Wizard 'done' function
			this.optionsPane.set("doneFunction",
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
			if(!this.cryptoLocalFileDataField.supports("multiple")){
				domConstruct.create("input",
						{name:"wrapResponse",value:"true",type:"hidden"},
						this.id + "_cryptoForm","first");
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
			
			// Calculate the device URL
			model.deviceURL = "https://" + model.appliance.hostName + ":" + model.appliance.guiPort + "/login.xml";
			console.debug(F,"Setting Web GUI Link", model.deviceURL);
			
			var link = string.substitute(this.webGuiLinkTemplateString,{applianceUrl:model.deviceURL,applianceName:model.appliance.name,linkText:nls.backupApplianceWizard.crypto.linktoAppliance});
			
			this.cryptoCertificateNameDataField.set("hint",link);
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
			
			delete this.model.deviceURL;
			
			// Crypto source
			var cryptoForm = this.cryptoForm.get("value");
			
			switch(cryptoForm.source){
				case "remoteSource":
					this.model.certificateLocation = cryptoForm.remoteSource;
					break;
				case "certficateName":
					this.model.certificateName=cryptoForm.certificateName; 
					break;
				default: // localFile
					// Do nothing, cryto set by uploadComplete
					break;
			}

			// Backup location
			var locationForm = this.locationForm.get("value");
			
			switch(locationForm.type){
				case "applianceLocalFile":
					this.model.backupDestination = locationForm.applianceLocalFile;
					break;
				case "applianceTemporaryFile":
					this.model.backupDestination = locationForm.applianceTemporaryFile;
					break;
				case "ftp":
					//ftp://user:password@host:port/path
					var url = this._buildFTPUrl(locationForm.ftpUserId,
										locationForm.ftpUserPassword,
										locationForm.ftpHostName,
										locationForm.ftpPort,
										locationForm.ftpPath);
					this.model.backupDestination = url;
					break;
				default: // zipFile
					this.model.backupDestination = "file://CREATE";
					break;
			}
			
			// Options
			this.model.includeIscsi =  this.optionsIncludeiSCSICheckBox.checked;
			this.model.includeRaid = this.optionsIncludeRAIDCheckBox.checked;
			
			return this.model; 
		},
		
		_buildFTPUrl:function(userId,userPassword,hostName,port,path){
			// tags:
			//		protected
			var F = this.declaredClass + "_buildFTPUrl()";
			console.debug(F,userId,userPassword,hostName,port,path);
			
			var userPart = "", portPart = "", pathPart = "";
			
			if(typeof userId === "string" && userId.length > 0){
				
				userPart = userId;
				
				if(typeof userPassword === "string" && userPassword.length > 0){
					userPart = userPart + ":" + userPassword;
				}
				
				userPart += "@";
			}

			if(typeof port === "number" && port >= 0){
				portPart = ":" + port;
			}
			
			if(typeof path === "string" && path.length > 0){
				pathPart = (path.charAt(0)=="/" ? path : "/" + path);
			}
			
			return "ftp://" + userPart + hostName + portPart + pathPart;

		},
		
		passFunction:function(){
			// summary:
			//		Validation function to check that the wizard is ok
			var F = this.declaredClass + ".passFunction()";
			console.debug(F);
			
			return true;
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
			
			var source = this.cryptoLocalFileRadioButton.get("value");

			// If using a local configuration, upload the local config
			if(source==="localFile"){
				console.debug(F,"Local SourceType",source);
				this.uploadStart();
				this.cryptoLocalFileDataField.upload();
			}else{
				console.debug(F,"Other SourceType",source);
				this.onDone();
			}
		},

		uploadStart:function(){
			// summary:
			//		Begin upload of the crypto file
			
			var F = this.declaredClass + ".uploadStart()";
			console.debug(F);
			
			this.disable();
			this.standby.show();
		},
		
		uploadComplete:function(){
			// summary:
			//		Called to signal the completion of uploading
			
			var F = this.declaredClass + ".uploadComplete()";
			console.debug(F);
			
			this.enable();
			this.standby.hide();
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
			
			this.model.certificateLocation = result.files[0];
			
			console.debug(F,"updated model",this.model);
			
			this.uploadComplete(result);
		},
		
		_uploadError: function(evt) {
			// summary:
			//		Called when the uploader reports an error.
			// evt:
			//		The failure event
			// tags:
			//		protected
			var F = this.declaredClass + ".uploadError()";
			console.debug(F,evt);
			
			//Disable the standby overlay
			this.enable();
			this.standby.hide();

			this.showMessage("ERROR_UPLOADING_FILE");
		},

		reset:function(){
			// summary:
			//		Reset all panes of this wizard
			
			var F = this.declaredClass + ".reset()";
			console.debug(F);
						
			/** Crypto Form **/
			this.cryptoCertificateNameDataField.reset();
			this.cryptoCertificateNameRadioButton.set("value",true);
			this._cryptoSourceChanged("certficateName");
			
			/** Location Form **/
			this.locationZipFileRadioButton.set("value",true);
			this._locationDestinationChanged("zipFile");
			
			/** Options Form **/
			this.optionsIncludeiSCSICheckBox.set("value",false);
			this.optionsIncludeRAIDCheckBox.set("value",false);
			
			/** Wizard Level Reset **/
			this.hideMessage();
			
			// hack as it seems like first page is not correctly selected
//			this.wizard.selectChild(this.locationPane);
			this.wizard.selectChild(this.cryptoPane);
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
			console.debug(F,property,"oldValue",oldValue,"newValue",newValue);
			
			this.wizard._checkButtons();
		},
		
		/** Functions for CryptoPane **/
		
		cryptoPassFunction:function(){
			// summary:
			//		Validate the CryptoForm
			// return: Boolean
			//		True if the user can continue, otherwise false
			var F = this.declaredClass + ".cryptoPassFunction()";
			console.debug(F);
			
			var f = this.cryptoForm.get("value"),
				valid = true;
			
			// If local
			if (f.source==="localFile"){
				console.debug(F,"Validating Local Source");
				valid = !(this.cryptoLocalFileDataField.get("value").length===0);
			}else{
				console.debug(F,"Validating Other Source");
				// Else, name on device or remote file use form.isValid
				valid = this.cryptoForm.isValid();
			}
			
			return valid;
		},
		
		_cryptoSourceClicked: function(evt){
			// summary:
			//		Called when the source of the crypto file is clicked.
			//		Sets field state and validity
			// evt:
			//		The onclick event
			// tags:
			//		protected
			
			var F = this.declaredClass + "._cryptoSourceClicked()";
			console.debug(F,evt);
			
			this._cryptoSourceChanged(evt.target.value);
		},
		
		_cryptoSourceChanged: function(newSource) {
			// summary:
			//		Called when the source of the crypto file changes.
			//		Sets field state and validity
			// tags:
			//		protected
			var F = this.declaredClass + "._cryptoSourceChanged()";
			console.debug(F,newSource);
			
			var enable = [], disable=[], pane="crypto";
			
			switch (newSource) {
			case "localFile":
				enable = [pane + "LocalFile"];
				
				disable = [pane + "CertificateName",
				           pane + "RemoteSource"];
				break;
			case "remoteSource":
				enable = [pane + "RemoteSource"];
				
				disable = [pane + "CertificateName",
				           pane + "LocalFile"];
				break;
			default /*Certificate Name*/:
				
				enable = [pane + "CertificateName"];
				
				disable = [pane + "RemoteSource",
				           pane + "LocalFile"];
				break;
			}
			
			array.forEach(enable,function(n){
				console.debug(F,"Enabling",n);
				var input = this["" + n +"DataField"];
				input.set("disabled",false);
				input.set("required",true);
			},this);
			
			array.forEach(disable,function(n){
				console.debug(F,"Disabling",n);
				var input = this["" + n +"DataField"];
				input.set("required",false);
				input.reset();
				input.set("disabled",true);
			},this);			
		},
		
		/** Functions for locationPane **/
		
		locationPassFunction:function(){
			// summary:
			//		Validate the locationForm
			// return: Boolean
			//		True if the user can continue, otherwise false
			var F = this.declaredClass + ".locationPassFunction()";
			console.debug(F);
			
			return this.locationForm.isValid();
		},
		
		_locationDestinationClicked: function(evt) {
			// summary:
			//		Called when the destination for the backup changes.
			//		Sets field state and validity
			// evt:
			//		The onclick event
			// tags:
			//		protected			
			this._locationDestinationChanged(evt.target.value);
		},
		
		_locationDestinationChanged: function(newValue) {
			// summary:
			//		Called when the destination for the backup changes.
			//		Sets field state and validity
			// tags:
			//		protected
			var F = this.declaredClass + "._locationDestinationChanged()";
			console.debug(F);
			
			var enable = [], disable=[], required=[], pane="location", 
				wizard = this.wizard;
			
			switch (newValue) {
			case "applianceLocalFile":
				enable = [pane + "ApplianceLocalFile"];
				
				required = [pane + "ApplianceLocalFile"];
				
				disable = [pane + "ApplianceTemporaryFile",
				           pane + "FtpHostName",
				           pane + "FtpPort",
				           pane + "FtpPath",
				           pane + "FtpUserId",
				           pane + "FtpUserPassword"];
				break;
			case "applianceTemporaryFile":
				enable = [pane + "ApplianceTemporaryFile"];
				
				required = [pane + "ApplianceTemporaryFile"];
				
				disable = [pane + "ApplianceLocalFile",
				           pane + "FtpHostName",
				           pane + "FtpPort",
				           pane + "FtpPath",
				           pane + "FtpUserId",
				           pane + "FtpUserPassword"];
				break;
				
			case "ftp":
				enable = [pane + "FtpHostName",
				           pane + "FtpPort",
				           pane + "FtpPath",
				           pane + "FtpUserId",
				           pane + "FtpUserPassword"];
				
				required = [pane + "FtpHostName"];
				
				disable = [pane + "ApplianceLocalFile",
				           pane + "ApplianceTemporaryFile"];
				
				break;
			default /*zipFile*/:
				
				disable = [pane + "ApplianceLocalFile",
				           pane + "ApplianceTemporaryFile",
				           pane + "FtpHostName",
				           pane + "FtpPort",
				           pane + "FtpPath",
				           pane + "FtpUserId",
				           pane + "FtpUserPassword"];
				break;
			}
			
			array.forEach(enable,function(n){
				console.debug(F,"Enabling",n);
				var input = this["" + n +"DataField"];
				
				input.set("disabled",false);
				
				if(array.indexOf(required,n) > -1) {
					input.set("required",true);	
				}
			},this);
			
			array.forEach(disable,function(n){
				console.debug(F,"Disabling",n);
				var input = this["" + n +"DataField"];
				
				input.set("required",false);
				input.set("disabled",true);
				input.reset();
			},this);
			
			wizard._checkButtons();
		},
		
		_locationFtpUserPasswordChanged: function(newValue) {
			// summary:
			//		Called when the user changes the FTP userPassword field
			// newValue: String
			//		The new value of the field
			// tags:
			//		protected
			var F = this.declaredClass + "._locationFtpUserPasswordChanged()";
			console.debug(F,newValue);
			
			var reqd = typeof newValue === "string" && newValue.length > 0; 
			
			this.locationFtpUserIdDataField.set("required",reqd);
			this.locationFtpUserIdDataField.validate();
			
		}

	});

	return BackupApplianceWizard;
});
