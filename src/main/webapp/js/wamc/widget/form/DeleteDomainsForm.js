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
        "dojo/dom-class",
        "dojo/on",
        "dojo/string",
        "dojo/store/Memory",
        "dijit/_Widget",
        "dijit/_TemplatedMixin",
        "dijit/_WidgetsInTemplateMixin",
        "dijit/registry",
        "gridx/Grid",
        "gridx/core/model/cache/Async",
        "wamc/grid/format",
        "wamc/grid/modules",
		"dojo/text!wamc/widget/form/templates/DeleteDomainsForm.html",
		"dojo/i18n!wamc/nls/strings"],
		
		function(array,declare,lang,domClass,on,string,Memory,_Widget,_TemplatedMixin,_WidgetsInTemplateMixin,registry,Grid,Cache,gridFormat,gridModules,template,nls){

		var DeleteDomainsForm = declare("wamc.widget.form.DeleteDomainsForm",[_Widget,_TemplatedMixin,_WidgetsInTemplateMixin],{	

		nls: nls,
		
		templateString: template,
		
		// domains: Object[]
		//		Array of domain objects to delete
		domains: [],
		
		postCreate: function(){
			// summary:
			//		Widget life-cycle method.
			var F = this.declaredClass + ".postCreate():";
			console.debug(F);
			
			this.inherited(arguments);

			var deleteDomainGrid = new Grid({
				id:"deleteDomainGrid",
				store: new Memory({data: []}),
				cacheClass: Cache,
				modules:[
				         {moduleClass:gridModules.VScroller},
				         {moduleClass:gridModules.Focus}
				],			
				structure:[
					        { id:"displayName", name: nls.deleteDomain.name, field: "displayName", dataType: "string", width: "45%" },
					        { id:"applianceName", name: nls.deleteDomain.appliance, field: "applianceName", dataType: "string", width: "45%" },
					        { id:"status", name: nls.deleteDomain.status, field: "status", dataType: "string", width: "auto", decorator:gridFormat.decorateDomainGridStatus}],
				"aria-label": nls.deleteDomain.gridTitle
				},"deleteDomainGrid");
			
			domClass.add(deleteDomainGrid.domNode,"compact");
			
			// attach the grid of domains at the template marker
			deleteDomainGrid.placeAt(this.domainsTableContainer);
			deleteDomainGrid.startup();
		},
		
		startup: function(){
			// summary:
			//		Widget life-cycle method.
			var F = this.declaredClass + ".startup():";
			console.debug(F);
			
			this.inherited(arguments);
			
			// Setup button connections
			on(this.deleteButton,"click",lang.hitch(this, function(evt){
				this.onDelete();
			}));

			on(this.cancelButton,"click",lang.hitch(this, function(evt){
				this.onCancel();
			}));
			
			this.reset();
		},
		
		resize: function(){
			// summary:
			//		Over-ride the resize function to make it possible
			//		to correctly display a grid with content
			// tags:
			//		override			
			var F = this.declaredClass + ".resize():";
			console.debug(F);
			
			this.inherited(arguments);

			registry.byId("deleteDomainGrid").setStore(new Memory({"data": this.domains, "idProperty": "id"}));
		},
		
		reset: function(){
			// summary:
			//		Reset the fields in this form
			var F = this.declaredClass + ".reset():";
			console.debug(F);
			
			// ensure any warning messages are removed from view
			domClass.add(this.defaultWarning,"hidden");
			domClass.add(this.quiesceWarning,"hidden");
		},
		
		processDomains: function(/*Domain[]*/domains){
			// summary:
			//		Determine what messages need to be displayed
			//		to the user when confirming to delete domains
			// domains:
			//		An array of domains to display
			var F = this.declaredClass + ".processDomains():";
			console.debug(F,domains);
			
			this.reset();
			
			// gather some numbers, only count unquiesed domains that 
			// are not default domains and give them all a sudo ID to
			// facilitate display in the confirmation grid
			var defaults=0, unquiesced=0, sudoId=1;

			array.forEach(domains, lang.hitch(this, function(domain){
				if (domain.displayName.toLowerCase() == "default") defaults++;
				else if (domain.status.toLowerCase() == "up") unquiesced++;
				domain.id=sudoId++;
			}));

			// save the domains array for use by resize()
			this.domains = domains;
			
			// do we need the default domain warning message
			if (defaults > 0){
				// make the warning area visible
				domClass.remove(this.defaultWarning,"hidden");
				
				if (defaults==1){
					this.defaultMessage.innerHTML = string.substitute(this.nls.deleteDomain.defaultOne, {total: domains.length});
					this.defaultExplaination.innerHTML = this.nls.deleteDomain.defaultSingular;
				}else{
					this.defaultMessage.innerHTML = string.substitute(this.nls.deleteDomain.defaultMany, {total: domains.length, count: defaults});
					this.defaultExplaination.innerHTML = this.nls.deleteDomain.defaultPlural;
				} 
			}else{
				domClass.add(this.defaultWarning,"hidden");
			}
			
			// calculate how many domains there are that we can work with
			var deletableDomains = domains.length - defaults;
			
			// is the quiesce warning message (3 versions available) about 
			// unquiesced domains required, if so pick the correct one
			if (unquiesced > 0){
				// make the warning area visible
				domClass.remove(this.quiesceWarning,"hidden");
				
				if (deletableDomains == 1){
					this.quiesceMessage.innerHTML = this.nls.deleteDomain.quiesceOneOfOne;
					this.quiesceExplaination.innerHTML = this.nls.deleteDomain.quiesceSingular;
				}else if (unquiesced == 1){
					this.quiesceMessage.innerHTML = string.substitute(this.nls.deleteDomain.quiesceOne, {total: deletableDomains});
					this.quiesceExplaination.innerHTML = this.nls.deleteDomain.quiesceSingular;
				}else{
					this.quiesceMessage.innerHTML = string.substitute(this.nls.deleteDomain.quiesceMany, {total: deletableDomains, count: unquiesced});
					this.quiesceExplaination.innerHTML = this.nls.deleteDomain.quiescePlural;
				}
			}else{
				domClass.add(this.quiesceWarning,"hidden");
			}
		},
						
		onDelete: function(){
			// summary:
			//		Placeholder for more events to occur on delete
			// tags:
			//		override
			var F = this.declaredClass + ".onDelete():";
			console.debug(F);
		},
		
		onCancel: function(){
			// summary:
			//		Placeholder for more events to occur on cancel
			// tags:
			//		override
			var F = this.declaredClass + ".onCancel():";
			console.debug(F);
			
			this.reset();
		}
	});
	return DeleteDomainsForm;
});
