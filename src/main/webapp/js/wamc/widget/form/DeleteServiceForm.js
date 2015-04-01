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
		"dojo/text!wamc/widget/form/templates/DeleteServiceForm.html",
		"dojo/i18n!wamc/nls/strings"],
		
		function(array,declare,lang,domClass,on,string,Memory,_Widget,_TemplatedMixin,_WidgetsInTemplateMixin,registry,Grid,Cache,gridFormat,gridModules,template,nls){

		var DeleteServiceForm = declare("wamc.widget.form.DeleteServiceForm",[_Widget,_TemplatedMixin,_WidgetsInTemplateMixin],{	

		nls: nls,
		
		templateString: template,
		
		postCreate: function(){
			// summary:
			//		Widget life-cycle method.
			var F = this.declaredClass + ".postCreate():";
			console.debug(F);
			
			this.inherited(arguments);

			var deleteServiceGrid = new Grid({
				id:"deleteServiceGrid",
				store: new Memory({data: []}),
				cacheClass: Cache,
				modules:[
				         gridModules.VScroller,
				         gridModules.IndirectSelect,
				         gridModules.Focus,
				         gridModules.ColumnResizer,
				         gridModules.RowHeader,
				         {moduleClass: gridModules.ExtendedSelectRow, triggerOnCell: true},
				         {moduleClass:gridModules.Focus}
				],			
				structure:[
					        { id:"objectType", name: nls.deleteService.objectType, field: "type", dataType: "string", width: "45%" },
					        { id:"objectName", name: nls.deleteService.objectName, field: "name", dataType: "string", width: "auto" }],
				"aria-label": nls.deleteService.gridTitle
				},"deleteServiceGrid");
			
			domClass.add(deleteServiceGrid.domNode,"compact");

			// attach the grid of services at the template marker
			deleteServiceGrid.placeAt(this.serviceTableContainer);
			deleteServiceGrid.startup();
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
			console.debug(F, this.orphans);
			
			this.inherited(arguments);

			registry.byId("deleteServiceGrid").setStore(new Memory({"data": this.service.orphans, "idProperty": "id"}));
		},
		
		reset: function(){
			// summary:
			//		Reset the fields in this form
			var F = this.declaredClass + ".reset():";
			console.debug(F);
			
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
		},
		
		populate: function(service){
			this.service = service;
			registry.getEnclosingWidget(this.domNode.parentNode).set("title", string.substitute(nls.deleteService.title, service));
		}
	});
	return DeleteServiceForm;
});
