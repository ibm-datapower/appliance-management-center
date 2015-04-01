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
        "dojo/_base/lang",
        "dojo/request/xhr",
        "wamc/config/Resources"],
        function(array,lang,xhr,Resources){
	
	var user = lang.getObject("wamc.user",true);
	
	user.getUserName = function(){
		// summary:
		//		Get the name of the current user
		// return: String
		//		The username of the current user
		return user.name;
	};
	
	user.hasPermission = function(/*String*/name){
		// summary:
		//		Determine whether the current user has a named permission
		
		var result = false;
		
		if(user.permissions && typeof user.permissions.sort === "function"){
			result = array.indexOf(user.permissions, name) >= 0;
		}
		
		return result;
	};
	
	user.logout = function(){
		window.location = Resources.pages.logout;
	},
	
	xhr.get(Resources.currentUser.url,{
		handleAs:"json",
		sync:true
	}).then(function(response){lang.mixin(user,response);});

	return user;
});
