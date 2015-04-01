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
        "dojo/when",
        "dojo/store/Cache",
        "dojo/store/Memory",
        "dojo/store/Observable",
        "wamc/config/Resources",
        "wamc/store/JsonRest",
        "wamc/store/DomainJsonRest",
        "wamc/store/ServiceJsonRest"],
		function(lang,when,Cache,Memory,Observable,Resources,JsonRest,
				DomainJsonRest,ServiceJsonRest){
	// summary:
	//		wamc/stores is an access point for ObjectStores used by grids
	//		in WAMC.
	// description:
	//		In this first version, we'll create all the stores up front.
	//		Later, we can add lazy instantiation and pre-caching.
	
	var stores = lang.getObject("wamc.stores",true),
	
	// This is a map of all the stores used by the application
	_stores = {};
	
	_stores.appliance = Observable(
			new JsonRest({target:Resources.appliances.url,
					idProperty: "id"}));
	
	_stores.domain = Observable(
			new DomainJsonRest({target:Resources.domains.url,
					instanceTarget:Resources.domain.url,
					idProperty: "primaryKey"}));
	
	_stores.service = Observable(
			new ServiceJsonRest({target:Resources.services.url,
					instanceTarget:Resources.service.url,
					idProperty: "primaryKey"}));
	
	_stores.firmware = Observable(
			new JsonRest({target:Resources.firmwares.url,
					idProperty: "primaryKey"}));
	
	_stores.action = new JsonRest({target:Resources.actions.url,
			idProperty: "actionId"});
	
	// More stores go here...
	
	stores.get = function(/*String*/name){
		return _stores[name];
	};
	
	stores.notifyUpdate = function(store,id,object){
		// summary:
		//		Trigger updates on an Observable store when an object has
		//		been updated. If the object is omitted, it is refreshed from 
		//		the store source
		// store: dojo/store
		//		The store to update
		// id: 
		//		The id of the object
		// object:
		//		The object that has been updated
		var F = "wamc.stores.notifyUpdate()";
		console.debug(F,store,id,object);
		
		if(!store || !id) return;
		
		if (typeof store === "string"){
			store = stores.get(store);
		}
		
		if(!object){
			if(typeof store.evict === "function"){
				store.evict(id);
			}
			object = store.get(id);
		}
		
		when(object,function(o){
			store.notify(o,id);
		});
	};
	
	return stores;
});
