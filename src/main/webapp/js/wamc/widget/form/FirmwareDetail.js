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
        "wamc/widget/form/TextBox",
        "wamc/_HelpLabelsMixin",
        "wamc/date/format",
        "wamc/widget/form/_DataDisplay",
        "wamc/widget/form/ValueDisplay",
        "dojo/i18n!wamc/nls/strings",
        "dojo/text!wamc/widget/form/templates/FirmwareDetail.html"],
		function(declare, lang, topic, _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, TextBox, _HelpLabelsMixin, dateFormat, _DataDisplay, ValueDisplay, nls, template){
	
	var FirmwareDetail = declare("wamc.widget.form.FirmwareDetail",[_Widget, _TemplatedMixin, _WidgetsInTemplateMixin, _HelpLabelsMixin, _DataDisplay],{
		
		nls:nls,

		templateString:template,
		
		master:"",
		
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
	
		formatData: function(/*Object*/data){
			// summary:
			//		Update the model to include derived firmware fields
			// data: Object
			//		The data model to be formatted
			// return:
			//		A formatted copy of the data model
			// tags:
			//		override
			var F = this.declaredClass + ".formatData():";
			console.debug(F, data);
			
			data = this.inherited(arguments);
			
			var ff = [];
			
			ff = data.strictFeatures ? ff.concat(data.strictFeatures) : ff;
			
			ff = data.nonStrictFeatures ? ff.concat(data.nonStrictFeatures) : ff;
			
			data.firmwareFeatures = ff.length ? ff : nls.firmwareDetail.noFeatures;
			
			data.manufactureDateDisplay = dateFormat.reformat(data.manufactureDate,{formatLength:"long",timePattern: dateFormat.timePatternNoMillis});
			data.timeStampDisplay = dateFormat.reformat(data.timeStamp,{formatLength:"long",timePattern: dateFormat.timePatternNoMillis});
			
			return data;
		},
		
		overrideModel: function(/*Object*/model){
			// summary:
			//		Remove the derives firmwareFeatures from the Model
			// model: Object
			//		The data model to be overriden
			// return:
			//		The overriden model
			// tags:
			//		override
			var F = this.declaredClass + ".overrideModel():";
			console.debug(F, model);
			
			model = this.inherited(arguments);
			
			delete model.firmwareFeatures;
			delete model.manufactureDateDisplay;
			delete model.timestampDisplay;
			
			return model;
		}

	});
	return FirmwareDetail;
});
