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
        "dijit/_TemplatedMixin",
        "dijit/form/_FormWidgetMixin",
        "dijit/form/Select",
        "dijit/form/_FormSelectWidget",
        "dojo/text!wamc/widget/form/templates/Select.html",
        "wamc/_LabelMixin"],
        function(declare,domAttr,domClass,query,_Widget,_TemplatedMixin,_FormWidgetMixin,Select,_FormSelectWidget,template,_LabelMixin){

	var SelectWidget = declare("wamc.widget.form.Select",[_Widget,_TemplatedMixin,_FormWidgetMixin,Select,_FormSelectWidget,_LabelMixin],{
		templateString:template,

		postCreate: function() {
			this.inherited(arguments);
			domClass.remove(this.domNode, "dijitSelect");
		}
	});

	return SelectWidget;
});
