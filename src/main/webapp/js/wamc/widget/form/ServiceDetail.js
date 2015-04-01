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
        "dijit/form/Form",
        "wamc/_MessagesMixin",
        "wamc/_HelpLabelsMixin",
        "wamc/grid/format",
        "wamc/widget/form/_DataDisplay",
        "wamc/widget/form/ValueDisplay",
        "dojo/i18n!wamc/nls/strings",
        "dojo/text!wamc/widget/form/templates/ServiceDetail.html"],
		function(declare,lang,topic,_Widget,_TemplatedMixin,_WidgetsInTemplateMixin,Form,_MessagesMixin,_HelpLabelsMixin,gridFormat,_DataDisplay,ValueDisplay,nls,template){
	
	var ServiceDetail = declare("wamc.widget.form.ServiceDetail",[_Widget, _TemplatedMixin, _WidgetsInTemplateMixin, _DataDisplay, _MessagesMixin, _HelpLabelsMixin],{
		
		nls:nls,
		
		name:"serviceDetail",
		
		templateString:template,
		
		defaultModel:{},
		
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
		
		formatData:function(/*Object*/data){
			var F = this.declaredClass + ".formatFormData():";
			console.debug(F, data);
			
			 data.status = gridFormat.decorateServiceDetailStatus(data.status);
			
			return data;
		}
		
	});
	
	return ServiceDetail;
	
});
