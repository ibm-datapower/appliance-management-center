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
        "dojo/_base/lang",
        "dojo/dom-attr",
        "dijit/registry"],
	function(array,lang,domAttr,registry){
	
	var wamcUtil = {
	
		getDataKey: function(/*Node*/node){
			// summary:
			//		Get the data binding key for a wamc form node. 
			//		The data key is defined by the custom attribute data-wamc-datakey.
			//		If the node represents a widget, the value is retrieved from the attribute of the widget.
			// node: Node
			//		The node from which to get the data key.
			// return: String
			//		The value of the data binding key
			return this._getCustomAttribute("data-wamc-datakey", node);
			
		},
		
		getLabelForKey: function(/*Node*/node){
			// summary:
			//		Get the data binding key for a wamc form node. 
			//		The data key is defined by the custom attribute data-wamc-labelfor.
			//		If the node represents a widget, the value is retrieved from the attribute of the widget.
			// node: Node
			//		The node from which to get the labelfor key.
			// return: String
			//		The value of the labelfor key
			return this._getCustomAttribute("data-wamc-labelfor", node);
			
		},
		
		getNlsId: function(/*Node*/node){
			// summary:
			//		Get the nls id for a wamc form node 
			//		The data key is defined by the custom attribute data-wamc-nlsid.
			//		If the node represents a widget, the value is retrieved from the attribute of the widget.
			// node: Node
			//		The node from which to get the data key.
			// return: String
			//		The string value of the nls id
			return this._getCustomAttribute("data-wamc-nlsid", node);
		},
		
		_getCustomAttribute:function(/*String*/property,/*Node*/node){
			// summary:
			//		Get the value of a custom attribute from 
			//		The data key is defined by the custom attribute data-wamc-nlsid.
			//		If the node represents a widget, the value is retrieved from the attribute of the widget.
			// node: Node
			//		The node from which to get the data key.
			// return: String
			//		The string value of the custom property
			// tags:
			//		private			
			var result, widget = registry.byNode(node);
			
			if (widget !== undefined) {
				result = widget.get(property);
				
			} else {
				result = domAttr.get(node, property);
			}
			
			return result;
		},
		
		compareObjects:function(/*Object*/first,/*Object*/second){
			// summary:
			//		Compare the values of two objects
			// return:
			//		true if the objects are equivalent, false otherwise
			
			// If either object is null, return false
			if(!first || !second){
				return false;
			}
			
			// If both are not objects, return false
			if (!lang.isObject(first) || !lang.isObject(second)) { 
				return false;
			}
				        
			// Setup list of properties for comparison, ignoring functions
			var props = {};
			for (var prop in first) {
				if (!lang.isFunction(first[prop]))
					props[prop] = 1;
			}
			for (prop in second) {
				if (!lang.isFunction(second[prop])){
				                props[prop] = 1;
				}   
			}
			// Now, compare the collected properties
			for (prop in props) {
				

				if (first[prop] === undefined || second[prop] === undefined) {
					//Property must exist in both objects
					return false;
				} else if (typeof first[prop] == "object" && typeof second[prop] == "object") {
					// If property is an object, they must be equivalent (recursive call)
					if (!this.compareObjects(first[prop],second[prop])) {
						return false;
					}
				} else if (first[prop] !== second[prop]) {
					// Otherwise, values must be the same
					return false;
				}
			}
			return true;
		},
		
		mergeArraysUnique: function(/*Array*/first,/*Array*/second) {
			// summary:
			//		Merge two arrays together, removing any duplicates.
			// description:
			//		Returns a new array containing the result of the merge.
			//		Only the instance of each element will be preserved. 
			//		Ordering is preserved.
			// first: Array
			//		The first array to merge
			// second: Array
			//		The second array to merge
			// return: Array
			//		A new array containing the concatenated result of the two arrays
			
			var result = first.concat(second);
			for(var i = 0; i < result.length; ++i){
				for(var j = i+1; j < result.length; ++j){
					if(this.compareObjects(result[i],result[j])){
						result.splice(j, 1);
					}
				}
			}
			return result;
		},
		
		generateFirmwareFeaturesString: function(strictFeatures, nonStrictFeatures) {
			
			//combine strict and non strict features.
			var firmwareFeatures = "";
			if(strictFeatures.length) {
				firmwareFeatures+= strictFeatures.join(", ");
			}
			if(nonStrictFeatures.length) {
				if(strictFeatures.length) {
					firmwareFeatures+= ", ";
				}
				firmwareFeatures+= nonStrictFeatures.join(", ");
			}
			
			return firmwareFeatures;
			
		},
		
		containsValue: function(/*Array*/array,/*String*/value){
			// summary:
			//		does the array contain the provided string value
			// description:
			//		Return true if the string value is found in the array			
			var i = array.length;
		    while (i--){
		       if (array[i] === value){
		           return true;
		       }
		    }
		    return false;
		},
		
		invert: function(/*boolean*/bool){
			// summary:
			//		invert a boolean
			// return:
			//		the inverse of the boolean provided
			return bool === true ? false : true;
		}
	};
	
	return wamcUtil;
	
});
