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
define(["dojo/_base/window",
        "dijit/registry",
        "dojox/widget/Standby",
        "dojo/i18n!wamc/nls/strings"],
        function(win,registry,Standby,nls){

	var _module = "wamc.standby",
	
	getStandby = function(){
		
		var s = registry.byId("standby");
		
		if(!s) {
			s = new Standby({
				id:"standby",
				centerIndicator:"text",
				color:"#F7F7F7"
			});
			s.placeAt(win.body());
			s.startup();
		}
		
		return s;
	},
	
	markBusy = function(/*dijit._Widget*/widget,/*String*/message){
		// summary:
		//		Mark a widget as busy while an action occurs
		// widget: dijit._Widget
		//		The widget to mark as busy
		// message: String (optional)
		//		The message to display on the overlay
		var F = _module + ".markBusy()";
		console.debug(F,widget,message || "No message");
		
		var s = getStandby();
		s.set("target",widget.domNode);	
		s.set("text", message || nls.global.standbyDefault);		
		s.show();
	},
	
	markAvailable = function(/*dijit._Widget*/widget){
		// summary:
		//		Mark a widget that was previously busy as available
		// widget: dijit._Widget
		//		The widget to mark available
		var F = _module + ".markAvailable()";
		console.debug(F,widget);
		
		var s = getStandby();
		
		if(widget.domNode == s.get("target")){
			s.hide();
		}
	},
	
	standby = {
		markBusy:markBusy,
		markAvailable:markAvailable
	};
	
	return standby;
});
