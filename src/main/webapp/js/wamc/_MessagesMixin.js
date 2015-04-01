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
        "dojo/dom-construct",
        "dojo/string",
        "wamc/config/Messages",
        "wamc/widget/messaging/MessageArea",
        "dojo/i18n!wamc/nls/strings"],
        function(declare,json,domConstruct,string,Messages,MessageArea,nls){

	var _MessagesMixin = declare("wamc._MessagesMixin", [], {
		// summary:
		//		This mixin adds a message area and showMessage/hideMessage methods to custom widgets.
		//		It allows them to display messages from the application NLS bundle to provide relevant
		//		feedback.
		//		Widgets using this mixin need to call initMessageArea in their postCreate method.
		
		_feedbackBar: null,
		
		_messageArea: null,
		
		postCreate:function(){
			this.inherited(arguments);
			
			this._feedbackBar = domConstruct.create("div",{id:this.id + "_FeedbackBar","aria-live":"polite"},this.domNode,"first");
		},
		
		showMessage:function(/*String*/messageKey,/*Array*/substitutes,/*boolean*/closable){
			// summary:
			//		Show the message area with the specified message
			// messageKey: String
			//		The key for the message
			// substitutes: Array
			//		The list of substitute values to use
			// closable: boolean
			//		Should the message be closable
			
			var F = this.declaredClass + ".showMessage()";
			console.debug(F,messageKey,substitutes,closable);
			
			this.hideMessage();
			
			var message = Messages[messageKey],
				args = {type:message.level,
						title:nls.level[message.level],
						message:string.substitute(nls[messageKey],substitutes),
						closable:closable},
				n = domConstruct.create("div",{id:this.id + "_MessageArea"},this._feedbackBar,"first");
			
			this._messageArea = MessageArea.showMessage(n,args);
		},
		
		showServerResponse:function(/*String*/serverResponse){
			// summary:
			//		Show the message area, populated with a multi-part message
			//		as returned by the server (generally when an error occurs)
			// serverResponse:
			//		The JSON response from the server.
			var F = this.declaredClass + ".showServerResponse(): ";
			console.debug(F, serverResponse);
			
			this.hideMessage();
			
			if(serverResponse === null || serverResponse === ""){
				// Show a 'no server response' error
				console.debug(F, "Response is Null or empty string");
				this.showMessage("ERROR_NO_RESPONSE");
			}else{
				if(typeof serverResponse === "string"){
					console.debug(F, "Response is string",serverResponse);
					//If this is a string, turn it into a response object				
					serverResponse = json.fromJson(serverResponse);
				}
				
				var errorLevel = MessageArea.computeErrorLevel(serverResponse.code),
					args = {type:errorLevel,
							title:nls.level[errorLevel],
							message:serverResponse.message,
							explanation:serverResponse.explanation,
							userAction:serverResponse.userAction,
							code:serverResponse.code,
							timestamp:serverResponse.timeStamp,
							cause:serverResponse.cause,
							closable:true},
					n = domConstruct.create("div",{id:this.id + "_MessageArea"},this._feedbackBar,"first");
				
				this._messageArea = MessageArea.showMessage(n,args);
			}
		},
		
		hideMessage:function(){
			// summary:
			//		Hide the message area.
			
			var F = this.declaredClass + ".hideMessage(): ";
			console.debug(F);
			
			if(this._messageArea){
				this._messageArea.close();
			}
		}
	
	});
	
	return _MessagesMixin;
});
