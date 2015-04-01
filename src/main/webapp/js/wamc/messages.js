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
define(["dojo/string",
        "dijit/registry",
        "wamc/config/Messages",
        "dojo/i18n!wamc/nls/strings"],
        function(string,registry,Messages,nls){
	
	var showMessage = function(/*String*/messageKey,/*Array*/substitutes){
		// summary:
		//		Show the message area with the specified message
		// messageKey:
		//		The key for the message
		// substitutes:
		//		The list of substitute values to use
		
		var F = "wamc.messages.showMessage()";
		console.debug(F);
		
		var feedbackBar = registry.byId("feedbackBar"),
			message = Messages[messageKey];
		
		if(feedbackBar){
			feedbackBar.showMessage({
				type:message.level,
				title:nls.level[message.level],
				message:string.substitute(nls[messageKey],substitutes)
			});
		}
	},
	
	messages = {
		showMessage:showMessage
	};
	
	return messages;

});
