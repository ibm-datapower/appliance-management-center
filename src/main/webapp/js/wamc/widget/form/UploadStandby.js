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
        "dojo/dom-attr",
        "dojo/dom-class",
        "dojo/query",
        "dijit/_Widget",
        "dijit/ProgressBar",
        "dojox/widget/Standby",
        "dojo/i18n!wamc/nls/strings"],
        function(declare,domAttr,domClass,query,_Widget,ProgressBar,Standby,nls){

	var UploadStandby = declare("wamc.widget.form.UploadStandby",[_Widget,Standby],{
		message:nls.global.standbyDefault,
		
		progressBar: null,

		setupProgressBar: function() {
			this.inherited(arguments);
			this.set("text", "<div style=\"text-align: center\">" + this.message + "</div>");
			this.progressBar = new ProgressBar({style: "width: 200px", maximum: 100});
			this.progressBar.placeAt(this._textNode, "last");
			this.progressBar.startup();
		},
		
		setValue: function(val) {
			this.progressBar.set("value", val);
		}
	});

	return UploadStandby;
});
