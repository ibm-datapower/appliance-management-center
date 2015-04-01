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
        "dojo/query",
        "dojo/dom-attr",
        "dijit/registry",
        "wamc/util"],
		function(declare,lang,query,domAttr,registry,wamcUtil){
	
	var _DataDisplay = declare("wamc.widget.form._DataDisplay",null,{
		// summary:
		//		A mixin that provides behaviour for displaying a model in a set of fields annotated with wamc-datakey
		
		// defaultModel: Object
		//		Values that should be used as defaults in the form, if no values are specified
		defaultModel:{},
		
		// model: Object
		//		The model being bound to the display elements
		model:{},
		
		_setModelAttr: function(/*Object*/model){
			var m = model ? lang.mixin(lang.clone(this.defaultDataModel),model) : model;
			
			this._set("model",m);
			
			if(this._started){
				this._updateDisplay(this.model);
			}
		},
		
		_getModelAttr: function(){
			if(this._started){
				this._updateModel();	
			}
			
			return this.model;
			
		},
		
		_getFormDataAttr: function(){
			return this._started ? this._getFormData() : {};
		},
		
		reset: function () {
			// summary:
			//		Reset all dataFields
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
		},
		
		formatData: function(/*Object*/data){
			// summary:
			//		Perform any formatting actions on the model before populating form fields.
			// data: Object
			//		The data model to be formatted
			// return:
			//		A formatted copy of the data model
			// tags:
			//		override
			var F = this.declaredClass + ".formatData():";
			console.debug(F, data);
			
			return lang.clone(data);
		},
		
		overrideModel: function(/*Object*/model){
			// summary:
			//		Perform any overrides on the model being retrieved from the form
			// model: Object
			//		The data model to be overridden
			// return:
			//		The overridden model
			// tags:
			//		override
			var F = this.declaredClass + ".overrideModel():";
			console.debug(F, model);
			
			return model;
		},
		
		_updateDisplay: function (/*Object*/data) {
			// summary: 
			//		Update  fields in this widget with the object specified by data
			// data:
			//		The object to update the form fields with
			// tags:
			//	private
			//this.dataModel = this._clean(data);
			var F = this.declaredClass + "._updateDisplay():";
			console.debug(F, data);
			
			this.reset();
			
			data = data ? this.formatData(data) : data;
			
			var queryString = "#" + this.domNode.id + " .dataField";
						
			query(/*".dataField"*/queryString).forEach(function(node) {

				var dataKey;

				dataKey = wamcUtil.getDataKey(node);
				
				if (dataKey !== null) {
					if (this._hasProperty(dataKey, data)) {
						this._updateField(node, data[dataKey]);
					}
				} 
			}, this);
		},
		
		_updateModel:function(){
			// summary:
			//		Get updated model with the most recent data from any widgets that are enabled
			// return: Object
			//		The updated model, for invocation chaining
			// tags:
			//		private
			var F = this.declaredClass + "._updateModel():";
			console.debug(F);

			var newModel = this._getFormData();
			
			this.model=lang.mixin(this.model,newModel);
				
			console.debug(F, "Exiting",this.model);
			return this.model;
		},
		
		_getFormData:function(){
			// summary:
			//		Get form data from any widgets that are enabled
			// return: Object
			//		The updated form data, overridden with any mixins from the default model
			// tags:
			//		private
			var newModel = {};
			
			var queryString = "#" + this.domNode.id + " .dataField";
						
			query(/*".dataField"*/queryString).forEach(function(node) {
				var dataKey;

				var widget = registry.byNode(node);
				
				if (widget !== undefined){
					dataKey = wamcUtil.getDataKey(node);
					
					if (dataKey !== null && !widget.get("readOnly")) {
						newModel[dataKey] = widget.get("value");
					} 
				}
			}, this);
			
			newModel = newModel ? this.overrideModel(newModel) : newModel;
			
			return newModel;
		},
		
			
		_hasProperty:function(/*String*/property,/*Object*/obj){
			return (obj !== null && property !== null && obj[property] !== null && obj[property] !== undefined);
		},
		
		_updateField : function(node, value) {
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
		}
	});
	
	return _DataDisplay;
	
});
