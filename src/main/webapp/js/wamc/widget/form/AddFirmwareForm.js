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
        "dojo/_base/json",
        "dojo/_base/lang",
        "dojo/_base/event",
        "dojo/_base/xhr",
        "dojo/aspect",
        "dojo/dom-construct",
        "dojo/on",
        "dijit/_Widget",
        "dijit/_TemplatedMixin",
        "dijit/_WidgetsInTemplateMixin",
        "dijit/registry",
        "dijit/form/Form",
        "dijit/form/RadioButton",
		"dojox/form/Uploader",
		"wamc/widget/form/UploadStandby",
		"wamc/widget/form/TextBox",
		"wamc/_MessagesMixin",
		"wamc/config/Resources",
		"wamc/validation",
		"wamc/widget/form/UploaderValueTextBox",
		"dojo/text!wamc/widget/form/templates/AddFirmwareForm.html",
		"dojo/i18n!wamc/nls/strings"],
		
		function(declare, json, lang, event, xhr, aspect, domConstruct, on, 
				_Widget, _TemplatedMixin, _WidgetsInTemplateMixin, registry, 
				Form, RadioButton, Uploader, Standby, TextBox, 
				_MessagesMixin, Resources, wamcValidation, 
				UploaderValueTextBox, template, nls) {

	var AddFirmwareForm = declare("wamc.widget.form.AddFirmwareForm", [_Widget, _TemplatedMixin, _WidgetsInTemplateMixin, _MessagesMixin], {
		// summary:
		//		A form for adding new firmware, either by uploading or using a remote file
				
		nls: nls,
		
		templateString:template,
		
		uploadAction: Resources.upload.url,
		
		startup: function(){
			// summary:
			//		Widget life-cycle method.
			
			var F = this.declaredClass + ".startup(): ";
			console.debug(F);
			
			this.inherited(arguments);

			// If the uploader is going to use an iframe (because this is IE)
			// add a hidden input to let the server know.
			if(!this.localSourceUploader.supports("multiple")){
				console.debug(F,"Adding wrapResponse input");
				domConstruct.create("input",
						{name:"wrapResponse",value:"true",type:"hidden"},
						this.id + "_form","first");
			}
			
			// Set up event handlers for form validation and submission

			// If this wizard appears in a dialog, have the standby overlay cover the entire dialog
			var parent = registry.getEnclosingWidget(this.domNode.parentNode);
			
			if(parent && parent instanceof dijit.Dialog){
				console.debug(F,"Parent is dialog");
				this.standby.set("target",parent.domNode);
			}
			
			on(this.submitButton, "click", lang.hitch(this, function(evt){

				event.stop(evt);
				
				if (this.formNode.isValid()) {
					this.submitButton.set("disabled",true);
					this.cancelButton.set("disabled",true);
					
					if (this.localSourceRadioButton.checked) {
						console.debug("Uploading firmware image");
						on(this.localSourceUploader, "progress", lang.hitch(this, function(evt) {
							var pct = (Math.floor(100*evt.bytesLoaded/evt.bytesTotal));
							this.standby.setValue(pct);
						}));
						this.standby.setupProgressBar();
						this.standby.show();
						this.localSourceUploader.upload();
						this.onSubmit();
					} else {
						this.onSubmit();
						this.post(this.formNode.value.remoteSource);
					}
				} else {
					// Show errors
					this.formNode.validate();			
				}
			}));	
			
			// POST form data on completion of file upload
			on(this.localSourceUploader, "complete", lang.hitch(this, function(evt) {
				console.debug("Upload complete: ", evt);
				this.standby.hide();

				if(evt.code){ // If we got back an error response instead of a list of files.
					this.showServerResponse(json.toJson(evt));
					this.cancelButton.set("disabled",false);
					return;
				}
				// Otherwise, it's the files.
				this.post(evt.files[0]);
			}));
			
			on(this.localSourceUploader, "error", lang.hitch(this, function(evt) {
				console.debug("Upload error: ", evt);
				this.standby.hide();
				var xhr = evt.target;
				if(xhr) { 
					this.showServerResponse(xhr.responseText);
				} else {
					this.showMessage("ERROR_NO_RESPONSE");
				}
				this.cancelButton.set("disabled",false);
			}));
			
			// Setup button connections
			on(this.cancelButton, "click", lang.hitch(this, function(evt){
				event.stop(evt);
				this.reset();
				this.onCancel();
			}));
			
			this.formNode.watch("state",
					lang.hitch(this,"_formStateChanged"));
			
			/* 
			 * Because the uploader doesn't affect the state of its parent 
			 * watch for change events separately. 
			 */
			aspect.after(this.localSourceUploader,"onChange",
					lang.partial(lang.hitch(this,"_formStateChanged"),"state"));
				
			on(this.localSourceRadioButton,"change",lang.hitch(this,function(newValue){
				console.debug("localSourceRadioButton Changed",newValue);
				
				if(newValue){
					this.localSourceUploader.set("disabled",false);
					this.localSourceUploader.set("required",true);
					this._formStateChanged("state"); // Form does not change state automatically
				}else{
					this.localSourceUploader.set("required",false);
					this.localSourceUploader.set("disabled",true);
					this.localSourceUploader.reset();
				}
			}));
			
			// When the radio button changes, set the required attributes on fields
			on(this.remoteSourceRadioButton,"change",lang.hitch(this,function(newValue){
				console.debug("remoteSourceRadioButton Changed",newValue);
				
				if(newValue){
					// This means that the remote url is now required, but the file upload is not
					this.remoteSourceDataField.set("disabled",false);
					this.remoteSourceDataField.set("required",true);
				}else{
					// Now the file is required, but the remote is not
					this.remoteSourceDataField.set("required",false);
					this.remoteSourceDataField.set("disabled",true);
					this.remoteSourceDataField.reset();
				}
			}));
			
			this.reset();
			
		},
		
		reset: function(){
			// summary:
			//		Reset the fields in this form
			var F = this.declaredClass + ".reset(): ";
			console.debug(F);
			
			this.localSourceRadioButton.set("value",true);
			this.localSourceUploader.reset();
			
			this.remoteSourceDataField.reset();
			this.remoteSourceDataField.set("disabled",true);
			
			this.userCommentsDataField.reset();
			
			this.submitButton.set("disabled",true);
			this.cancelButton.set("disabled",false);
			
			this.hideMessage();
		},
		
		post: function(uri){
			// summary:
			//		Post the form data

			var F = this.declaredClass + ".post(): ";
			
			var postObject = {"uri": uri, "userComment": this.formNode.value.userComment};
			
			xhr.post({
				url : Resources.firmware.add.url,
				headers : {
					"Content-Type" : "application/json"
				},
				handleAs : "json",
				postData : json.toJson(postObject),
				load : lang.hitch(this, function(response, ioArgs) {
					this.reset();
					this.onDone();
					return response;
				}),
		        error:lang.hitch(this,function(response,ioArgs){
					console.log(F,"Error while adding firmware " + response);
					this.showServerResponse(response.xhr.responseText);
					this.submitButton.set("disabled",false);
					this.cancelButton.set("disabled",false);
					return response;
		        })
			});
		},
		
		_formStateChanged:function(/*String*/property,/*String*/oldValue,/*String*/newValue){
			// summary:
			//		Called when the state of one of the forms changes
			// tags:
			//		protected
			var F = this.declaredClass + "._formStateChanged()";
			console.debug(F,property,"oldValue",oldValue,"newValue",newValue);
			
			var f = this.formNode.get("value"),
				valid = true;
			
			console.debug(F,f);
			
			if(f.source==="local"){
				console.log(F,"Validating Local Source");
				valid = !(this.localSourceUploader.get("value").length==0);
			}else{
				console.log(F,"Validating Remote Source");
				valid = this.formNode.isValid();
			}
			
			this.submitButton.set("disabled",!valid);
			
		},

		onSubmit: function(){
			// summary:
			//		Placeholder for more events to occur on submit
			// tags:
			//		override
		},
		
		onDone: function(){
			// summary:
			//		Placeholder for more events to occur after 
			//		task has been submitted
			// tags:
			//		override
		},
		
		onCancel: function(){
			// summary:
			//		Placeholder for more events to occur on cancel
			// tags:
			//		override
		}
	});
	return AddFirmwareForm;
});
