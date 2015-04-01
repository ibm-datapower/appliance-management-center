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
        "dojo/string",
        "dojo/topic",
        "dijit/_Widget",
        "dijit/_TemplatedMixin",
        "dijit/_WidgetsInTemplateMixin",
        "wamc/widget/form/NumberTextBox",
        "wamc/widget/form/TextBox",
        "wamc/_MessagesMixin",
        "wamc/_HelpLabelsMixin",
        "wamc/grid/format",
        "wamc/widget/form/_DataDisplay",
        "wamc/widget/form/ValueDisplay",
        "dojo/text!wamc/widget/form/templates/ApplianceDetail.html",
        "dojo/i18n!wamc/nls/strings"],
		function(declare, lang, string, topic, _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, NumberTextBox, TextBox, _MessagesMixin, _HelpLabelsMixin, gridFormat, _DataDisplay, ValueDisplay, template, nls){
	
	var ApplianceDetail = declare("wamc.widget.form.ApplianceDetail",
			[_Widget, _TemplatedMixin, _WidgetsInTemplateMixin, _DataDisplay, _MessagesMixin, _HelpLabelsMixin],{
		
		nls: nls,
		
		templateString:template,
		
		name: "applianceDetail",
		
		defaultDataModel: {"adminUserPassword": "xxxxxxxxxxxxxxxx"},
		
		master: "", // The grid id that is the master for this detail
		
		startup:function(){
			this.inherited(arguments);
			
			var m = this.master,
				masterId = typeof m ==="string" ? m : m.id,
				topicId = masterId + "_selectionChanged";
			
			topic.subscribe(topicId,lang.hitch(this,function(selection){
				var selected = selection.length==1 ? selection[0] : null;
				
				this.set("model",selected);
			}));
			
		},
				
		formatData:function(/*Object*/data){
			var F = this.declaredClass + ".formatFormData():";
			console.debug(F, data);
			
			data.status = gridFormat.decorateApplianceDetailStatus(data.status);
			data.groups = gridFormat.decorateGroupsList(data.groups);
			
			return data;
		},
		
		reset: function(){
			this.hideMessage();
			this.inherited(arguments);
		}
		
	});
	
	return ApplianceDetail;
	
});
