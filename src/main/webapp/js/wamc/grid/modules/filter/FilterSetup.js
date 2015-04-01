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
define([
	"dojo/_base/declare",
	"dojo/_base/lang",
	"dojo/aspect",
	"dojo/when",
	"gridx/core/_Module"],
	function(declare,lang,aspect,when,_Module){
	
	return declare(/*===== "wamc.grid.modules.filter.FilterSetup", =====*/_Module,{
		// summary:
		//		This module initialises the grid filter from a query string,
		//		and sets up connections between the Model, Filter and FilterBar
		
		name: "filterSetup",
		
		optional: ["filterBar"],
		
		getAPIPath: function(){
			return {
				filterSetup: this
			};
		},
		
		load: function(args, deferStartup){
			var t = this,
				g = t.grid,
				m = g.model,
				done = function(){
					t.loaded.callback();
				},
				filterData;
			
			if(!!g.filterBar){
				// First, setup connections between the model and filterBar
				t.batchConnect(
						[m,"onNew","refresh",g.filterBar],
						[m,"onSet","refresh",g.filterBar],
						[m,"onDelete","refresh",g.filterBar]);
				// Second, refresh the filter bar when the filter refreshes.
				// filterBar requires that filter be loaded, no need to check.
				t.aspect(g.filter,"refresh","_refreshFilterBar",t,"after",false);
				

				// Third, set an initial filter for the grid
				filterData = this.arg("filterData");
				
				console.debug("!!!!filterData",filterData);
				
				if(!!filterData && filterData.conditions){
					g.filterBar.applyFilter(filterData);
				}
				g.when().then(done);
			}else{
				done();
			}
		},
		
		aspect: function(obj, e, method, scope, pos, rcvArgs){
			// summary:
			//		Override _Module aspect to allow us to set rcvArgs
			var cnnt = aspect[pos || 'after'](obj, e, lang.hitch(scope || this, method), !!rcvArgs);
			this._cnnts.push(cnnt);
			return cnnt;
		},
		
		_refreshFilterBar:function(/*Deferred|null*/deferred){
			// summary:
			//		Sometimes refreshing the filter bar returns a deferred, but
			//		it isn't consistent about it. This function normalises
			// deferred: Deferred | null
			//		The deferred indicating that the filter has been refreshed
			var t = this, d = deferred || true;
			
			return when(d,function(){
				t.grid.filterBar.refresh();
			});
		}
		
	});
	
	
});
