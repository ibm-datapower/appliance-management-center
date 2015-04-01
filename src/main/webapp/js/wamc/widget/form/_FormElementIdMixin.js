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
	
	var _FormElementIdMixin = declare("wamc.widget.form._FormElementIdMixin",null,{
		// summary:
		//		A mixin that provides labels the 'for' ids that are generated dynamically.
				
		startup: function () {
			// summary:
			//		Reset all dataFields
			var F = this.declaredClass + ".startup(): ";
			console.debug(F);
			
			this.inherited(arguments);
						
			this._updateLabels();			
			
		},
		
		_updateLabels: function () {
			// summary: 
			//		Update labels in this form with the id of the input given by the labelfor key.
			// tags:
			//	private
			var F = this.declaredClass + "._updateLabels():";
			console.debug(F);						
			
			var queryString = "#" + this.domNode.id + " label";
						
			query(/*".dataField"*/queryString).forEach(function(node) {

				var labelForKey;

				labelForKey = wamcUtil.getLabelForKey(node);		
				
				if (labelForKey !== null) {
					var generatedId = this[labelForKey].id;
					this._updateLabel(node, generatedId);
					domAttr.remove(node, "data-wamc-labelfor");
				} 
			}, this);
		},
		
		_updateLabel : function(node, value) {
			// summary: 
			//		Update the label with for attribute with a value.
			// tags:
			//	private
			var F = this.declaredClass + ".updateLabel():";
			console.debug(F);		

			domAttr.set(node, "for", value);
		}
		
				
	});
	
	return _FormElementIdMixin;
	
});
