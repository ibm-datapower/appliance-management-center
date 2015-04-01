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
        "dojo/_base/Deferred",
        "dojo/_base/lang",
        "dojo/DeferredList",
        "dojo/store/util/QueryResults"],
        function(array,declare,Deferred,lang,DeferredList,QueryResults){

	return declare("wamc.store.Gateway",[],{
		// summary:
		//		A store that acts as an aggregating gateway for several stores
		
		// idProperty: String
		//		This is the id for this store. It must not be the same as
		//		any of the of the stores behind this gateway.
		//		The value for the id will be the mapped store name concatenated 
		//		with the store id.
		idProperty: "id",
		
		// typeProperty: String
		//		The property that will be added to results to differentiate
		//		results from each store.
		typeProperty: "type",
		
		idSeparator: "_",
		
		// stores: Object
		//		A map of string names to stores. The String name will the value
		//		of typeProperty for objects returned from each store.
		stores:null,
		
		constructor: function(/*wamc.store.Gateway*/ options){
			// summary:
			//		
			declare.safeMixin(this, options);
		},
		
		getIdentity: function(object){
			// summary:
			//		Returns an object's identity
			// object: Object
			//		The object to get the identity from
			// returns: String|Number
			return object[this.idProperty];
		},
		
		getType: function(object){
			// summary:
			//		Returns an object's type
			// object: Object
			//		The object to get the type from
			// returns: String|Number
			return object[this.typeProperty];
		},
		
		_guessType:function(/*String*/id){
			// tags:
			//		protected
			return id.split(this.idSeparator)[0];
		},
		
		_guessOrigId:function(/*String*/id){
			// tags:
			//		protected
			return id.split(this.idSeparator)[1];
		},
		
		_getStoreById:function(/*String*/id){
			// tags:
			//		protected
			var type = this._guessType(id),
				s = this.stores(type);
			
			return s;
		},
		
		_getStoreByType:function(/*String*/type){
			// tags:
			//		protected
			
			return this.stores[type];
		},
		
		_createId:function(/*String*/type,/*String/number*/origId){
			return [type,this.idSeparator,origId].join("");
		},
		
		wrapObject:function(/*String*/type,/*Object*/object){
			// summary:
			//		Add idProperty and typeProperty attributes to an object
			
			if(typeof object === "object"){
				// If it's an object, wrap it
				var origId = this.stores[type].getIdentity(object);
				
				object[this.idProperty] = this._createId(type,origId);
				object[this.typeProperty] = type;
				
				return object;
			}else if(typeof object === "string" || typeof object === "number"){
				// If an id was passed by mistake, generate a new one
				return this._createId(type,object);
			}else{
				// No idea. Return the object as is.
				return object;
			}
		},
		
		unwrapObject:function(/*Object*/object){
			if(typeof object === "object"){
				delete object[this.idProperty];
				delete object[this.typeProperty];
			}
			return object;
		},
		
		get: function(id){
			// summary:
			//		Retrieves an object by its identity
			// id: Number
			//		The identity to use to lookup the object
			// returns: Object
			//		The object in the store that matches the given id.
			
			var t = this._guessType(id),
				s = this._getStoreByType(t),
				origId = this._guessOrigId(id),
				result = s.get(origId);
			return Deferred.when(result,lang.hitch(this,"wrapObject",t));
		},
		
		put: function(object, directives){
			// summary:
			//		Stores an object
			// object: Object
			//		The object to store.
			// directives: dojo.store.api.Store.PutDirectives?
			//		Additional directives for storing objects.
			// returns: Number|String
			var t = object[this.typeProperty],
				s = this._getStoreByType(t),
				o = this.unwrapObject(object),
				result = s.put(o,directives);
			return Deferred.when(result,lang.hitch(this,"wrapObject",t));
		},
		
		add: function(object, directives){
			// summary:
			//		Creates an object, throws an error if the object already exists
			// object: Object
			//		The object to store.
			// directives: dojo.store.api.Store.PutDirectives?
			//		Additional directives for creating objects.
			// returns: Number|String
			var t = object[this.typeProperty],
				s = this._getStoreByType(t),
				o = this.unwrapObject(object),
				result = s.add(o,directives);
			return Deferred.when(result,lang.hitch(this,"wrapObject",t));
		},
		
		remove: function(id){
			// summary:
			//		Deletes an object by its identity
			// id: Number
			//		The identity to use to delete the object
			var t = object[this.typeProperty],
				s = this._getStoreByType(t),
				origId = this._guessOrigId(id);
			
			s.remove(origId);
		},
		
		query: function(query, options){
			// summary:
			//		Queries all the stores and returns a normalized,
			//		consolidated QueryResult.
			//		Limitations: 
			//		Sends the query to all stores regardless of type
			//		Same options are sent to all stores (limit etc)
			//		
			// query: String|Object|Function
			//		The query to use for retrieving objects from the store.
			// options: dojo.store.api.Store.QueryOptions
			//		The optional arguments to apply to the resultset.
			// returns: dojo.store.api.Store.QueryResults
			//		The results of the query, extended with iterative methods.

			var t = this,
				resultList = [], // The list of results from calling query on each store
				deferredList = null, // A DeferredList to wait for the queries to return
				types = [], // An index for resultList 
				d,i,j; // Array index variables
			
			// Query Each of the stores
			for(var s in this.stores){
				types.push(s);
				var queryResult = this.stores[s].query(query,options);
				
				if(queryResult.then){
					// If the store is async, this willresultListe a promise
					resultList.push(queryResult);
				}else{
					// If the store is sync, it will be an actual result
					var queryPromise = new Deferred();
					resultList.push(queryPromise);
					queryPromise.resolve(queryResult); // This may have to be done later
				}
			}
					
			deferredList = new DeferredList(resultList);
			
			d = Deferred.when(deferredList,function(results){
				var x = [];
				
				for(i=0; i<results.length; i++){
					var a = results[i][1];
					
					for(j=0;j<a.length;j++){
						x.push(t.wrapObject(types[i],a[j]));
					}
				}
				return x;
			});
			
			return QueryResults(d);
		}
	});

});