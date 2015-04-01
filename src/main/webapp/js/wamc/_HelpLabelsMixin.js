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
        "dojo/dom-construct",
        "dojo/query",
        "dijit/registry",
        "dijit/Tooltip"],
        function(array,declare,domConstruct,query,registry,Tooltip){

	var addHelpIcon = function addHelpIcon(widget){
		// summary:
		//		Add a hover help icon to a widget
		var location, hoverHelp, anchor;
		
		//Locate where we want to drop this dom.
		//Most likely will the label inside the One UI Textbox. 
		location = widget.compLabelNode;
		
		//If not search for the explicitly placed hover help anchor.
		if(!location) {
			location = query(".hoverHelpAnchor",
					widget.domNode.parentNode.parentNode)[0];
		}
		
		//Add anchor to the input widget.
		anchor = domConstruct.create("div",{"class":"helpMessageIcon"},
				location,"after");
		
		//Create new Hover Help Widget.
		hoverHelp = new Tooltip({connectId:[anchor],
				label:widget.helpMessage,
				forceFocus:false});
		
		hoverHelp.startup();
	},
	
	addHelpIcons = function addHelpIcons(/*dijit._Widget[]*/widgets){
		// summary:
		//		Add help icons to a list of widgets
		// widget:
		//		The widget to add help icons to
		array.forEach(widgets,function(widget){
			
			var children;
			
			// We only want to interact with input labels that have help messages.
			if(typeof widget.helpMessage === "string"){
				addHelpIcon(widget);
			}
			
			children = widget.getChildren();
			
			if(children && children.length > 0){
				addHelpIcons(children);
			}
		});
	},
	
	_HelpLabelsMixin = declare(null,{
		// summary:
		//		This mixin adds help widgets to input fields when appropriate.
				
		startup:function(){
			this.inherited(arguments);
			addHelpIcons(registry.findWidgets(this.domNode));
		}
	});
	
	return _HelpLabelsMixin;
});
