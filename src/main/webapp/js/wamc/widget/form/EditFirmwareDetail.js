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
        "dojo/_base/json",
        "dojo/_base/lang",
        "dojo/_base/xhr",
        "dojo/_base/event",
        "dojo/on",
        "dojo/topic",
        "dojo/string",
        "dijit/_Widget",
        "dijit/_TemplatedMixin",
        "dijit/_WidgetsInTemplateMixin",
        "wamc/widget/form/TextBox",
        "wamc/config/Resources",
        "wamc/widget/form/_DataDisplay",
        "wamc/widget/form/_DataForm",
        "wamc/widget/form/ValueDisplay",
        "wamc/util",
        "dojo/i18n!wamc/nls/strings",
        "dojo/text!wamc/widget/form/templates/EditFirmwareDetail.html"],
		function(declare, json, lang, xhr, event, on, topic, string, _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, TextBox, Resources, _DataDisplay, _DataForm, ValueDisplay, wamcUtil, nls, template){
	
	var EditFirmwareDetail = declare("wamc.widget.form.EditFirmwareDetail",[_Widget, _TemplatedMixin, _WidgetsInTemplateMixin, _DataDisplay, _DataForm],{
		
		nls: nls,

		templateString: template,
	
	});
	
	return EditFirmwareDetail;
});
