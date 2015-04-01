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
        "dojo/_base/event",
        "dojo/_base/lang",
        "dojo/dom-geometry",
        "dojo/dom-style",
        "dijit/registry",
        "dijit/MenuSeparator",
        "dijit/Menu"],
		function(array,event,lang,domGeom,domStyle,registry,MenuSeparator,
				iMenu){
	
	var wamcMenus = lang.getObject("wamc.menus", true);
	
	// Add a Child Selector to idx/widget/Menu to make it compatible with dojo 1.8.2
	lang.extend(iMenu,{
		// define a child selector that finds the menu items in any of our columns
		childSelector: function(/*DOMNode*/ node){
			var widget = registry.byNode(node);
			return (array.indexOf(this._containerNodes, node.parentNode) >= 0) && widget && widget.focus;
		}
	});
	
	
	return wamcMenus;
});
