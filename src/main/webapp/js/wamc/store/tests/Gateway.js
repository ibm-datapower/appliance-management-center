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
define(["doh",
        "dojo/_base/array",
        "dojo/_base/Deferred",
        "dojo/store/Memory",
        "wamc/store/Gateway"],
        function(doh,array,Deferred,Memory,Gateway){
		
	var userData = [{name:"Anne Redwood",roles:[{id:1,name:"System Administrator"},{id:2,name:"Solution Deployer"}]},
	                {name:"Roshan Nichani",roles:[{id:3,name:"System Operator"}]},
	                {name:"Sue Graham",roles:[{id:2,name:"Solution Deployer"}]},
	                {name:"Tom Waterton",roles:[{id:1,name:"System Administrator"}]}],
		groupData = [{name:"WAMC Testers",roles:[{id:2,name:"Solution Deployer"}]},
		             {name:"WAMC Developers",roles:[{id:1,name:"System Administrator"}]},
		             {name:"WAMC Super Users",roles:[{id:1,name:"System Administrator"},{id:2,name:"Solution Deployer"},{id:3,name:"System Operator"}]}],
		userStore = new Memory({idProperty:"name",data:userData}),
		groupStore = new Memory({idProperty:"name",data:groupData}),
		gatewayStore = new Gateway({
			stores:{
				user:userStore,
				group:groupStore
			}
		});
		
		doh.register("testGet",
				[function getUser(){
					var id = "user_Roshan Nichani",
						result = gatewayStore.get(id);
						
					Deferred.when(result,function(o){
						doh.assertTrue(!!o);
						doh.assertTrue(o.name ==="Roshan Nichani");
					});
				},function getGroup(){
					var id = "group_WAMC Developers",
					result = gatewayStore.get(id);
					
					Deferred.when(result,function(o){
						doh.assertTrue(!!o);
						doh.assertTrue(o.name ==="WAMC Developers");
					});
			}]);
		
		doh.register("testAdd",
				[function addUser(){
					var n = {name:"Vincent Furnier",type:"user",roles:[{id:1,name:"System Administrator"},{id:2,name:"Solution Deployer"},{id:3,name:"System Operator"}]};
					result = gatewayStore.add(n);
					Deferred.when(result,function(id){
						// Check the generated id
						doh.assertEqual("user_Vincent Furnier",id);
					});
				},function addGroup(){
					var n = {name:"Aerosmith",type:"group",roles:[{id:1,name:"System Administrator"}]};
					result = gatewayStore.add(n);
					Deferred.when(result,function(id){
						// Check the generated id
						doh.assertEqual("group_Aerosmith",id);
					});
				}]);
		
		doh.register("testPut",
				[function putUser(){
					var u = {name:"Vincent Furnier",type:"user",roles:[{id:2,name:"Solution Deployer"}]};
					result = gatewayStore.put(u);
					
					Deferred.when(result,function(id){
						Deferred.when(gatewayStore.get(id),function(o){
							doh.assertTrue(o.roles.length===1);
						});
					});
				},function putGroup(){
					var u = {name:"Aerosmith",type:"group",roles:[{id:1,name:"System Administrator"},{id:2,name:"Solution Deployer"},{id:3,name:"System Operator"}]};
					result = gatewayStore.put(u);
					
					Deferred.when(result,function(id){
						Deferred.when(gatewayStore.get(id),function(o){
							doh.assertTrue(o.roles.length===3);
						});
					});
				}]);
		
		doh.register("testQuery",
				[function queryAll(){
					var result = gatewayStore.query();
					
					Deferred.when(result,function(arr){
						doh.assertTrue(arr.length===9);
					});
				},function queryGroup(){
					var result = gatewayStore.query({name:"Aerosmith"});
					
					Deferred.when(result,function(arr){
						doh.assertTrue(arr.length===1);
					});
				},function queryNoMatches(){
					var result = gatewayStore.query({name:"Sherlock Holmes"});
					
					Deferred.when(result,function(arr){
						doh.assertTrue(arr.length===0);
					});
				}]);
});