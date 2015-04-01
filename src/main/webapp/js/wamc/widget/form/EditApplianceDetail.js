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
        "wamc/widget/form/_DataForm",
        "dojo/text!wamc/widget/form/templates/EditApplianceDetail.html",
        "dojo/i18n!wamc/nls/strings"],
		function(declare, lang, topic, _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, NumberTextBox, TextBox, _MessagesMixin, _HelpLabelsMixin, gridFormat, _DataDisplay, _DataForm, template, nls){
	
	var ApplianceDetail = declare("wamc.widget.form.EditApplianceDetail",[_Widget, _TemplatedMixin, _WidgetsInTemplateMixin, _DataDisplay, _DataForm, _MessagesMixin, _HelpLabelsMixin],{
		
		nls: nls,
		
		templateString:template,
		
		name: "editApplianceDetail",
		
		defaultDataModel: {"adminUserPassword": "xxxxxxxxxxxxxxxx"},
		
		refresh: function(){
			this.hideMessage();
			this.inherited(arguments);
		},
		
		overrideModel:function(/*Object*/model){
			var F = this.declaredClass + ".overrideModel():";
			console.debug(F, model);

			// Add identifier from model
			model.id = this.model.id;

			// remove password if not modified
			if(model.adminUserPassword === this.defaultDataModel.adminUserPassword){
				delete model.adminUserPassword;
			}

			// Don't try to set appliance status
			delete model.status;

			return model;
		}
	});
	
	return ApplianceDetail;
	
});
