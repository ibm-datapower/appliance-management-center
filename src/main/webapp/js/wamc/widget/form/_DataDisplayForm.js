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
        "dojo/_base/xhr",
        "dojo/_base/event",
        "dojo/query",
        "dojo/dom-attr",
        "dojo/dom-style",
        "dojo/on",
        "dojo/string",
        "dijit/_Widget",
        "dijit/_TemplatedMixin",
        "dijit/_WidgetsInTemplateMixin",
        "dijit/registry",
        "dijit/form/Button",
        "dijit/form/Form",
        "wamc/util",
        "wamc/config/Messages",
        "wamc/config/Resources",
        "wamc/validation",
        "dojo/text!wamc/widget/form/templates/_DataDisplayForm.html"],
		function(declare,lang,xhr,event,query,domAttr,domStyle,on,string,_Widget,_TemplatedMixin,_WidgetsInTemplateMixin,registry,Button,Form,wamcUtil,Messages,Resources,wamcValidation,template){
	
	var _DataDisplayForm = declare("wamc.widget.form._DataDisplayForm",[_Widget,_TemplatedMixin,_WidgetsInTemplateMixin],{
		
		nls:{},
		
		templateString:template,
		
		name: "_dataDisplayForm",
		
		// resourceEndpoint: String
		//		The key for this resource in the wamc/config/Resources bundle. e.g. 'domains'
		resourceEndpoint : null,
		
		// idKey: String
		//		The name of the property in the dataModel that represents the primaryKey. e.g. 'primaryKey'
		idKey: null,
		
		// identifiers: Object
		//		The values of key values for this resource
		//		The properties of this object will be substituted into the resourceEndpoint URL template to make the final URL
		//		e.g. {"id":"23","secondId:"default"} into /amc/appliances/${id}/domains/${secondId} 
		//		becomes /amc/appliances/23/domains/default
		identifiers:{},
		
		// viewOnly: boolean
		//		Is this form for viewing only? If so, all child widgets will be made read only
		//		default false
		viewOnly:false,
		
		
		// defaultModel: Object
		//		Values that should be used as defaults in the form, if no values are specified
		defaultModel:{},
				
		// initialise: boolean
		//		Has this widget been initialised? 
		initialised:null,
		
		// _resourceEndpointURL: String
		//		The resource endpoint resolved from wamc/config/Resources
		// tags:
		//		private
		_resourceEndpointURL : null,
		
		postCreate: function(){
			// summary:
			//		Widget life-cycle method, called after the widget has been created and added to the page.
			
			this.inherited(arguments);
			
			//If the form is read only, set all form widgets to be read only as well
			if(this.viewOnly){
				this._setDataFieldAttribute("readOnly",true);
				
				// Disable and hide the Submit and Cancel Buttons
				if (this.submitButton) {
					this.submitButton.set("disabled",true);
					domStyle.set(this.submitButton.domNode,"visibility","hidden");
				}
				if(this.cancelButton){
					this.cancelButton.set("disabled",true);
					domStyle.set(this.cancelButton.domNode,"visibility","hidden");
				}
			}
			
			//Set Cancel Button Label
			
		},
		
		startup: function(){
			this.inherited(arguments);
			// summary:
			//		Widget life-cycle method.
			
			// Set up event handlers for form validation and submission
			on(this.formNode, "submit", lang.hitch(this, function(evt){
				// Stop default event propagating up.
				event.stop(evt);
				if (this.formNode.isValid()) {
					this.disable();
					if (this.identifiers === null || this.identifiers[this.idKey] === null) {
						// If no primary identifier, create a new resource
						this._create();	
					} else {
						// Otherwise, update the existing resource
						this._update();
					}	
				} else {
					// Shown errors
					this.formNode.validate();
					
				}
			}));	
			
			// Setup button connections
			on(this.cancelButton, "click", lang.hitch(this, function(evt){
				event.stop(evt);
				this._cancel();
			}));
			
			this.initialised=true;
		},
		
		
		_setIdentifiersAttr: function (/*Object*/identifiers){
			// summary:
			//		Set the 'identifiers' property of this widget, retrieve resource data
			//		based on the new identifiers, and update the form with the returned values.
			// tags:
			//		
			var F = this.declaredClass + "._setIdentifiersAttr: ";
			console.debug(F,identifiers);
			
			// Set the identifiers attribute of this widget
			this.identifiers = identifiers;					

			this.refresh();
			
		},
		
		reset: function () {
			// summary:
			//		Reset all dataFields on this form
			var F = this.declaredClass + ".reset(): ";
			console.debug(F);
			
			var queryString = "#" + this.domNode.id + " .dataField";
			
			// Reset the state of all individual user input fields
			query(/*".dataField"*/queryString).forEach(function(node) {
				if (domAttr.get(node, "widgetid") !== null) {
					// If a widget, use reset
					registry.byNode(node).reset();
				} else {
					// Otherwise set the node innerHTML to a blank value
					domAttr.set(node,"innerHTML","");
				}
			});
			
			this.enable();			
		},
		
		refresh: function(){
			// summary:
			//		Repopulate form with most current data
			var F = this.declaredClass + ".refresh(): ";
			console.debug(F);
			
			if (this.initialised && this.resourceEndpoint !== null) {

				// Reset all form fields
				this.reset();
				
				if (this._resourceEndpointURL === null) {
					this._resourceEndpointURL = Resources[this.resourceEndpoint].url;
				}
				
				// If this.identifiers is set, populate form
				if(this.identifiers){
					this._retrieveDataModel();
				}
				
			} else {
				console.debug("Resource End Point is null, can't update form component");
			}			
		},
		
		_retrieveDataModel:function(){
			var F = this.declaredClass + "._retrieveDataModel: ";
			console.debug(F);

			if (this.idKey !==null && this.identifiers[this.idKey] === null) {
				
				this._updateFormData(this.defaultModel);
				
				this.submitButton.set("label", this.nls[this.name].create);	
				
			} else {				
				var url = string.substitute(this._resourceEndpointURL, this.identifiers);
				console.debug(this.declaredClass + "._retrieveDataModel: URL: " + url);
				
				xhr.get({
					url:url,
					handleAs:"json",
					load:lang.hitch(this,function(response,ioArgs){
							
							if(response[this.idKey]===this.identifiers[this.idKey]){
								this._updateFormData(lang.mixin(lang.clone(this.defaultDataModel),response));
							}else{
								console.debug("Ignoring response for response " + response[this.idKey]);
							}
							return response;
						}),
					error:lang.hitch(this,function(response,ioArgs){
							this.showServerResponse(response.xhr.responseText);
							return response;
						})
					}
				);
				
				this.submitButton.set("label", this.nls[this.name].update);
			}	
		},
		
		formatFormData: function(/*Object*/data){
			// summary:
			//		Perform any formatting actions on the data model before populating form fields.
			// tags:
			//		override
			var F = this.declaredClass + ".formatFormData():";
			console.debug(F, data);
				return data;
			
		},
		
		_updateFormData: function (/*Object*/data) {
			// summary: 
			//		Update form fields in this widget with the object specified by data
			// data:
			//		The object to update the form fields with
			// tags:
			//	private
			//this.dataModel = this._clean(data);
			var F = this.declaredClass + "._updateFormData():";
			console.debug(F, data);
			
			data = this.formatFormData(data);
			
			var queryString = "#" + this.domNode.id + " .dataField";
						
			query(/*".dataField"*/queryString).forEach(function(node) {
				var dataKey;

				dataKey = wamcUtil.getDataKey(node);
				
				if (dataKey !== null) {
					if (this._hasProperty(dataKey, data)) {
						this._updateFormField(node, data[dataKey]);
					}
				} 
			}, this);
	
		},
		
		_getFormData: function () {
			// summary: 
			//		Update form fields in this widget with the object specified by data
			// data:
			//		The object to update the form fields with
			// tags:
			//	private
			//this.dataModel = this._clean(data);
			var F = this.declaredClass + "._getFormData():";
			console.debug(F);
			
			var newModel = {};
			
			var queryString = "#" + this.domNode.id + " .dataField";
						
			query(/*".dataField"*/queryString).forEach(function(node) {
				var dataKey;

				var widget = registry.byNode(node);
				
				if (widget !== undefined){
					dataKey = wamcUtil.getDataKey(node);
					
					if ((dataKey !== null) && !widget.get("readOnly")) {
						newModel[dataKey] = widget.get("value");
					} 
				}
			}, this);
			
			console.debug(F, "Exiting",newModel);
			return newModel;
		},
			
		_hasProperty:function(/*String*/property,/*Object*/obj){
			return (obj !== null && property !== null && obj[property] !== null && obj[property] !== undefined);
		},
		
		_updateFormField : function(node, value) {
			var widget = registry.byNode(node);	
			
			if(value instanceof Array){
				var buffer = value[0];
				for (var i = 1; i < value.length; i++) {
					buffer = buffer + ", " + value[i];
				}
				value = buffer;
			}
			
			// If we still have a reference to a dijit update that, otherwise
			// put value in read-only field.
			if (widget !== undefined) {								
				widget.set("value", value);
			} else {
				domAttr.set(node, "innerHTML", value);
			}
		},
		
		_setDataFieldAttribute:function(/*String*/name,/*Object*/value){
			var F = this.declaredClass + "._setDataFieldAttribute: ";
			console.debug(F,name,value);
			
			var queryString = "#" + this.domNode.id + " .dataField";
			
			query(/*".dataField"*/queryString).forEach(function(node) {
				var widget = registry.byNode(node);
				
				if(widget !== undefined){
					widget.set(name,value);
				}
			});
		},
		
		enable: function(){
			if (!this.viewOnly) {
				if (this.submitButton) {
					this.submitButton.set("disabled",false);
				}
				if(this.cancelButton){
					this.cancelButton.set("disabled",false);
				}
				this._setDataFieldAttribute("disabled",false);
			}
		},
		
		disable: function(){
			if (this.submitButton) {
				this.submitButton.set("disabled",true);
			}
			if(this.cancelButton){
				this.cancelButton.set("disabled",true);
			}
			this._setDataFieldAttribute("disabled",true);
		},
		
		onCreate: function(){
			
		},
		
		onUpdate: function(){
			return this.identifiers[this.idKey];
		},
		
		onCancel: function(){
			
		},
		
		_create: function(){
			// summary:
			//		Children should implement this method to provide create functionality 
			// tags:
			//		override
			this.onCreate();
		},
		
		_update: function(){
			// summary:
			//		Children should implement this method to provide update functionality 
			// tags:
			//		override
			this.onUpdate();
		},
		
		_cancel: function(){
			// summary:
			//		Children should implement this method to provide cancel 
			// tags:
			//		override
			this.onCancel();
		},
		
		showMessage:function(/*String*/messageKey,/*Array*/substitutes){
			// summary:
			//		Display a message with the appropriate error. Overridden by _MessagesMixin
			// messageKey:
			//		The key for the message
			// substitutes:
			//		The list of substitute values to use
			var F = this.declaredClass + ".showMessage: ";
			console.debug(F,messageKey,substitutes);
			
			var message = string.substitute(Messages.messageKey,substitutes);
			
			alert(message);
		}
	});
	
	return _DataDisplayForm;
	
});
