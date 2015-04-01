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
        "dojo/_base/xhr",
        "dojo/json",
        "dojo/string",
        "./JsonRest"],
        function(declare,xhr,JSON,string,JsonRest){
	
	var WAMCJsonRestStore = declare("wamc/store/DomainJsonRest",[JsonRest],{
		
		// instanceTarget: String
		//		The template resource to use to access single instances
		instanceTarget: null,
		
		put: function(object, options){
			// summary:
			//		Stores an object. This will trigger a PUT request to the server
			//		if the object has an id, otherwise it will trigger a POST request.
			// object: Object
			//		The object to store.
			// options: dojo.store.api.Store.PutDirectives?
			//		Additional metadata for storing the data.  Includes an "id"
			//		property if a specific id is to be used.
			//	returns: Number
			options = options || {};
			var id = ("id" in options) ? options.id : this.getIdentity(object);
			var hasId = typeof id != "undefined";
			return xhr(hasId && !options.incremental ? "PUT" : "POST", {
					url: hasId ? this.uriFromPrimaryKey(id,this.instanceTarget) : this.target,
					postData: JSON.stringify(object),
					handleAs: "json",
					headers:{
						"Content-Type": "application/json",
						Accept: this.accepts,
						"If-Match": options.overwrite === true ? "*" : null,
						"If-None-Match": options.overwrite === false ? "*" : null
					}
				});
		},
		
		add: function(object, options){
			// summary:
			//		Stores an object. Unlike JsonRestStore, this will always
			//		trigger a POST to the server to append to the collection
			// object: Object
			//		The object to store.
			// options: dojo.store.api.Store.PutDirectives?
			//		Additional metadata for storing the data.
			//	returns: Number
			options = options || {};
			return xhr("POST", {
					url: this.target,
					postData: JSON.stringify(object),
					handleAs: "json",
					headers:{
						"Content-Type": "application/json",
						Accept: this.accepts,
						"If-Match": options.overwrite === true ? "*" : null,
						"If-None-Match": options.overwrite === false ? "*" : null
					}
				});
		},
		
		remove: function(id){
			// summary:
			//		Deletes an object by its identity. This will trigger a DELETE request to the server.
			// id: Number
			//		The identity to use to delete the object
			return xhr("DELETE",{
				url:this.uriFromPrimaryKey(id,this.instanceTarget)
			});
		},
		
		get: function(id, options){
			//	summary:
			//		Retrieves an object by its identity. This will trigger a GET request to the server using
			//		the url `this.target + id`.
			//	id: Number
			//		The identity to use to lookup the object
			//	returns: Object
			//		The object in the store that matches the given id.
			var headers = options || {};
			headers.Accept = this.accepts;
			return xhr("GET", {
				url:this.uriFromPrimaryKey(id,this.instanceTarget),
				handleAs: "json",
				headers: headers
			});
		},
		
		uriFromPrimaryKey:function(/*String*/primaryKey,/*String*/resource){
			// summary:
			//		Build the URI for a domain from its unique primaryKey
			// primaryKey: String
			//		The primaryKey for the domain
			// resource: String
			//		The template string for the resource that will be called,
			//		containing 'applianceId' and 'name' tokens
			// return: String
			//		The template
			
			var F = this.declaredClass + ".uriFromPrimaryKey()";
			console.debug(F,primaryKey,resource);
			
			if(!primaryKey || primaryKey.length === 0) return primaryKey;
			
			// Split the id into an appliance serialNumber and domain name
			var parts = primaryKey.split(":");
			
			console.debug(F,parts);
			
			if (parts.length < 2){
				return primaryKey;
			}else{
				return string.substitute(resource,{applianceId:encodeURIComponent(parts[0]),name:encodeURIComponent(parts[1])});
			}
		}
		
	});
	
	return WAMCJsonRestStore;
});
