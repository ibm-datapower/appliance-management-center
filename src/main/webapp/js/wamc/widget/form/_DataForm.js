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
        "dojo/_base/lang",
        "dojo/_base/event",
        "dojo/on",
        "dijit/_Widget",
        "dijit/_TemplatedMixin",
        "dijit/_WidgetsInTemplateMixin",
        "dijit/form/Button",
        "dijit/form/Form",
        "dojo/text!wamc/widget/form/templates/_DataForm.html"],
		function(declare,lang,event,on,_Widget,_TemplatedMixin,_WidgetsInTemplateMixin,Button,Form,template){
	
	var _DataForm = declare("wamc.widget.form._DataForm",[_Widget,_TemplatedMixin,_WidgetsInTemplateMixin],{
		
		templateString:template,
		
		startup: function(){
			this.inherited(arguments);
			// summary:
			//		Widget life-cycle method.
			
			// Set up event handlers for form validation and submission
			on(this.formNode, "submit", lang.hitch(this, function(evt){
				// Stop default event propagating up.
				event.stop(evt);
				if (this.formNode.validate()) {
					this.disable();
					this._submit(this.formNode.get("value"));
				} else {
					this.enable();
				}
			}));	
			
			// Setup button connections
			on(this.cancelButton, "click", lang.hitch(this, function(evt){
				event.stop(evt);
				this._cancel();
			}));
		},
		
		reset: function () {
			// summary:
			//		Reset all dataFields on this form
			var F = this.declaredClass + ".reset(): ";
			console.debug(F);
			
			this.inherited(arguments);
			
			this.enable();			
		},
	
		enable: function(){
			var F = this.declaredClass + ".enable(): ";
			console.debug(F);
			
			if (this.submitButton) {
				this.submitButton.set("disabled",false);
			}
			if(this.cancelButton){
				this.cancelButton.set("disabled",false);
			}
			if(typeof this._setDataFieldAttribute === "function"){
				this._setDataFieldAttribute("disabled",false);
			}
		},
		
		disable: function(){
			var F = this.declaredClass + ".disable(): ";
			if (this.submitButton) {
				this.submitButton.set("disabled",true);
			}
			if(this.cancelButton){
				this.cancelButton.set("disabled",true);
			}
			if(typeof this._setDataFieldAttribute === "function"){
				this._setDataFieldAttribute("disabled",true);
			}
		},
		
		onSubmit: function(/*Object*/value){

		},
		
		onCancel: function(){

		},
		
		_submit: function(/*Object*/value){
			this.onSubmit(value);
		},
		
		_cancel: function(){
			this.onCancel();
		}
		
	});
	
	return _DataForm;
	
});
