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
        "dojo/_base/lang",
        "dojo/dom-construct",
        "dojo/string",
        "dojo/topic",
        "dijit/_Widget",
        "dijit/_TemplatedMixin",
        "wamc/widget/messaging/MessageArea",
        "dojo/text!wamc/widget/templates/ActionDetail.html",
        "dojo/i18n!wamc/nls/strings"],
        function(array,declare,lang,domConstruct,string,topic,_Widget,_TemplatedMixin,MessageArea,template,nls){
	
	var ActionStatus = declare("wamc.widget.ActionDetail",[_Widget,_TemplatedMixin],{
		
		templateString:template,
		
		nls: nls,
		
		messageAreas: null,
		
		master:"master",
		
		startup:function(){
			this.inherited(arguments);
			
			var m = this.master,
				masterId = typeof m ==="string" ? m : m.id,
				topicId = masterId + "_selectionChanged";
			
			topic.subscribe(topicId,lang.hitch(this,function(selection){
				var selected = selection.length==1 ? selection[0] : null;
				
				this.set("actionStatus",selected);
			}));
			
		},
		
		_setActionStatusAttr: function(/*Object*/actionStatus){
			var F = this.declaredClass + "._setActionStatusAttr()";
			console.log(F,actionStatus);
			this._set("actionStatus",actionStatus);
			if(actionStatus){
				this.refresh();
			}else{
				this.reset();
			}
		},
		
		refresh:function(){
			// summary:
			//		Render with most up to date version action status
			var F = this.declaredClass + ".refresh()";
			console.log(F);
			
			this.reset();
			
			if(this.actionStatus){
				array.forEach(this.actionStatus.updates,lang.hitch(this,function(update, index, array){
					
					var type = "Information";
					var message = update.message;
					
					if(index==(array.length-1)){ // Last update
						switch(this.actionStatus.state){
						case "SUCCEEDED":
							type = "Success";
							break;
						case "FAILED":
							type = "Error";
							break;
						}
						
						if(this.actionStatus.result){ // Add backup link
							message = message + string.substitute(this.nls.backup.download,{uri:this.actionStatus.result});
						}
					}
						
					var args, messageDiv, messageArea;
					
					args = {type:type,
							title:this.nls.level[type],
							message:message,
							explanation:update.explanation,
							userAction:update.userAction,
							code:update.code,
							timestamp:update.timeStamp,
							cause:update.cause,
							closable:false
							};
					
					messageDiv = domConstruct.create("div",null,this.domNode,"first");
					messageArea = MessageArea.showMessage(messageDiv,args);
					
					this.messageAreas.push(messageArea);
				}));
			}
		},
	
		reset:function(){
			// summary:
			//		Reset this widget to blank by destroying all the message areas
			var F = this.declaredClass + ".reset()";
			console.log(F);
			
			if(this.messageAreas) {
				for(var i=0;i<this.messageAreas.length;i++){
					this.messageAreas[i].destroy();
				}
			}
			this.messageAreas = [];
			
		}
		
	});
	

	return ActionStatus;
});
