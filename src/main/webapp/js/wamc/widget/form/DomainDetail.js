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
        "dijit/form/Form",
        "wamc/widget/form/NumberTextBox",
        "wamc/_MessagesMixin",
        "wamc/_HelpLabelsMixin",
        "wamc/grid/format",
        "wamc/widget/form/_DataDisplay",
        "wamc/widget/form/ValueDisplay",
        "dojo/i18n!wamc/nls/strings",
        "dojo/text!wamc/widget/form/templates/DomainDetail.html"],
		function(declare,lang,string,topic,_Widget,_TemplatedMixin,_WidgetsInTemplateMixin,Form,NumberTextBox,_MessagesMixin,_HelpLabelsMixin,gridFormat,_DataDisplay,ValueDisplay,nls,template){
	
	var DomainDetail = declare("wamc.widget.form.DomainDetail",[_Widget, _TemplatedMixin, _WidgetsInTemplateMixin, _DataDisplay, _MessagesMixin, _HelpLabelsMixin],{
		
		nls:nls,
		
		name:"domainDetail",
		
		templateString:template,
		
		defaultModel:{},
		
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
			
			data.status = gridFormat.decorateDomainDetailStatus(data.status);
			data.groups = gridFormat.decorateGroupsList(data.groups);
			
			if(typeof data.sourceConfigurationUrl === "string"){
				if(data.sourceConfigurationUrl === "file://") {
					data.sourceConfigurationUrl = nls.domainDetail.configSourceUploadedFile;
				}else if(data.sourceConfigurationUrl.indexOf("device://") === 0){
					var ad = data.sourceConfigurationUrl.substring(9).split("/"),
						a = ad[0], d = ad[1];
					data.sourceConfigurationUrl = string.substitute(nls.domainDetail.applianceDomain,{appliance:a,domain:d}); 
				}
			}
			
			if(typeof data.deploymentPolicyUrl === "string"){
				var template="",replacements={};
				
				// We should always know the object name
				replacements.object = data.deploymentPolicyObjectName;
				
				if(data.deploymentPolicyUrl === "file://") {
					// If the file is local
					if(typeof data.deploymentPolicyDomainName === "string"){
						// If we know the domain name
						template = nls.domainDetail.deploymentPolicyFileDomain; 
						replacements.domain = data.deploymentPolicyDomainName;
						replacements.source = nls.domainDetail.uploadedFile;
					}else {
						// If we only know that it is from an uploaded file
						template=nls.domainDetail.deploymentPolicyFile;
						replacements.source = nls.domainDetail.deploymentPolicyUploadedFile;
					}
				}else if(data.deploymentPolicyUrl.indexOf("device://") === 0){
					// Source is a domain on another appliance
					var ad = data.deploymentPolicyUrl.substring(9).split("/"),
						a = ad[0], d = ad[1];
					template = nls.domainDetail.deploymentPolicyApplianceDomain;
					replacements.appliance = a;
					replacements.domain = d;
				}else{
					// Otherwise, this is a remote source
					if(typeof data.deploymentPolicyDomainName === "string"){
						// If we know the domain name
						template = nls.domainDetail.deploymentPolicyFileDomain; 
						replacements.domain = data.deploymentPolicyDomainName;
						replacements.source = data.deploymentPolicyUrl;
					}else {
						// If we only know that it is from an uploaded file
						template=nls.domainDetail.deploymentPolicyFile;
						replacements.source = data.deploymentPolicyUrl;
					}
				}
				
				data.deploymentPolicy = string.substitute(template,replacements);
			}
			
			if(typeof data.automaticSynchronization === "boolean"){
				if(data.automaticSynchronization){
					data.automaticSynchronizationDisplay = nls.domainDetail.autoSyncEnabled;
				}else{
					data.automaticSynchronizationDisplay = nls.domainDetail.autoSyncDisabled;
				}
			}else{
				data.automaticSynchronizationDisplay = nls.domainDetail.autoSyncUnknown;
			}
			
			return data;
		}
		
	});
	
	return DomainDetail;
	
});
