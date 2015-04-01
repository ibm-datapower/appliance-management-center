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
define(["dojo/_base/lang",
        "dojo/_base/json",
        "dojo/aspect",        "dojo/dom-construct",
        "dojo/date/locale",
        "dojo/string",
        "dijit/_Widget",
        "dijit/ConfirmDialog",        "dijit/layout/TabContainer",        "dijit/layout/ContentPane",
        "wamc/config/Messages",
        "wamc/date/format",        "dojo/text!wamc/widget/form/templates/ConfirmationDialog.html",
        "dojo/i18n!wamc/nls/strings"],
        function(lang,json,aspect,domConstruct,locale,string,_Widget,ConfirmationDialog,TabContainer,ContentPane,        		Messages,dateFormat,template,nls){

	var _getDialogProps = function(/*Message*/message,/*String*/messageKey,/*Array*/substitutes){
		// summary:
		//		Get the properties for a new dialog
		// message: Message
		//		The message object 
		// messageKey: String
		//		The key for the message in the wamc/config/Messages module
		// substitutes: Array
		//		Substitute values to use in the message string
		// return: Object
		//		The properties to create a new dialog
		// tags:
		//		private
		var F = "wamc.dialogs._getDialogProps()";
		console.debug(F,message,messageKey,substitutes);
		
		var props = {				style: "width: 416px;",
				type:message.level,
				messageId:message.code,
				messageRef:message.link,
				text:string.substitute(nls[messageKey],substitutes),
				messageTimestamp:locale.format(new Date(),{formatLength: 'medium'}),
				buttonOk:nls.action[message.action],
				nocheck:true,				autofocus: false
			},
			msgSummary = nls[messageKey + "_summary"];
		
		if(msgSummary){
			props.info = string.substitute(msgSummary,substitutes);
		} else {			props.info = "";		}
		
		console.debug(F,messageKey,nls[messageKey]);
		
		return props;
	},
	
	_causeToString = function(/*Object*/error, /*int*/ depth){
		var F = "wamc.dialogs._causeToString()";
		console.debug(F, error, depth);
		
		var result = "",
			indent = "";
		
		if(!depth){
			depth = 0;
		}
		
		for(var i=0;i<depth;i++){
			indent+="  ";	// FIXME Should use nls strings for content 
		}
		
		if(error.code != null && error.code != "!null.code!") {
			result = result+indent+error.code+": ";
		}
		
		result = result+indent+error.message+"\n\n";
		
		if(error.explanation != null && error.explanation != "!null.explanation!") {
			result = result+indent+error.explanation+"\n\n";
		}
		
		if(error.userAction != null && error.userAction != "!null.useraction!") {
			result = result+indent+error.userAction+"\n\n";
		}
		
		if(error.cause != null && error.cause != "!null.cause!") {
			// FIXME Should use nls strings for content 
			result = result+indent+"Caused by:\n"+this._causeToString(error.cause, depth+1);
		}
		return result;
	},
	
	_computeErrorLevel = function(/*String*/CWZCode){
		var F = "wamc.dialogs._computeErrorLevel()";
		console.debug(F, CWZCode);
		
		var levelChar = CWZCode.charAt(9);
		return {I:"information",
				A:"warning",
				W:"warning",
				E:"error"}[levelChar];
	},
	
	showDialog = function(/*String*/messageKey,/*Array*/substitutes,/*Callback*/callback){
		// summary:
		//		A convenience function to show a dialog for a particular message
		// messageKey: Integer
		//		The key for the message in the wamc/config/Messages module
		// substitutes: Array
		//		Substitute values to use in the message string
		// callback: Function
		//		For confirmations, the function to call when the dialog action button is clicked
		
		var F = "wamc.dialogs.showDialog()";
		console.debug(F,messageKey,substitutes,callback);
		
		var message = Messages[messageKey];
		
		props = _getDialogProps(message,messageKey,substitutes);
		
		var dlg = null;
		
		if(message.level==="Confirmation"){			props.title = nls.level.Confirmation;
			dlg = new ConfirmationDialog(props);			dlg.set("buttonOk", props.buttonOk);			var cont = string.substitute(template, dlg);			dlg.containerNode.innerHTML = cont;			aspect.after(dlg, "onExecute", callback);
			dlg.show();
		}else{			props.title = nls.level[props.type.toLowerCase()];
			dlg = new Dialog(props);			var cont = string.substitute(template, dlg);			dlg.containerNode.innerHTML = cont;
			dlg.show();	
		}
		
		//When the dialog is hidden and the animation complete, destroy it
		aspect.after(dlg,"onHide",function(){
			dlg.destroy();
		});

	},
	
	showServerResponse = function(/*String*/serverResponse){
		// summary:
		//		Show the message area, populated with a multi-part message
		//		as returned by the server (generally when an error occurs)
		// serverResponse:
		//		The JSON response from the server.
		var F = "wamc.dialogs.showServerResponse()";
		console.debug(F, serverResponse);
		
		if(serverResponse === null || serverResponse === ""){
			// Show a 'no server response' error
			console.debug(F, "Response is Null or empty string");
			showDialog("ERROR_NO_RESPONSE");
		}else{
			if(typeof serverResponse === "string"){
				console.debug(F, "Response is string",serverResponse);
				//If this is a string, turn it into a response object				
				serverResponse = json.fromJson(serverResponse);
			}
			
			var errorLevel = _computeErrorLevel(serverResponse.code);
			var props = {
					type:errorLevel,
					text: serverResponse.message,
					info:[],
					messageId: serverResponse.code,
					messageTimeStamp: dateFormat.reformat(serverResponse.timeStamp,{formatLength:"medium", timePattern:dateFormat.timePattern})
			};
			
			if(serverResponse.explanation){
				props.info.push({title:nls.dialog.explanation,content:serverResponse.explanation});
			}
			
			if(serverResponse.userAction){
				props.info.push({title:nls.dialog.userAction,content:serverResponse.userAction});
			}
			
			if(serverResponse.cause){
				var causeText = '<pre style="white-space: pre-wrap;">'+
						_causeToString(serverResponse.cause)+"</pre>";
				props.info.push({title:nls.dialog.cause,content:causeText});
			}
			
			props.title = nls.level[props.type.toLowerCase()];			dlg = new Dialog(props);			var cont = string.substitute(template, dlg);			var node = domConstruct.toDom(cont);			var messageNode = query("#" + dlg.id + "_messageContent", node)[0];						dlg.tabs = new TabContainer({				useMenu: false,				useSlider: false,				style: "height:175px"			}, this.containerNode);//			domStyle.set(this.messageWrapper, "borderTop", "0 none");			array.forEach(props.info, function(item){				var contentPane = new ContentPane({					title: item.title,					content: item.content				});				contentPane.domNode.setAttribute("role", "document");				dlg.tabs.addChild(contentPane);			}, this);			messageNode.innerHTML = tabs;						dlg.containerNode.innerHTML = node;			dlg.show();				
			//When the dialog is hidden and the animation complete, destroy it
			aspect.after(dlg,"onHide",function(){
				dlg.destroy();
			});
			
			dlg.show();
		}
		
	},
	
	dialogs = {
		showDialog:showDialog,
		showServerResponse:showServerResponse
	};
	
	return dialogs;
});
