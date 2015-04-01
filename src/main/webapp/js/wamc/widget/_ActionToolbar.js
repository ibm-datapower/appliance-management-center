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
define(["dijit/layout/_LayoutWidget",
        "dijit/form/Button",
        "dijit/form/DropDownButton",
        "dijit/DropDownMenu",
        "dijit/MenuItem",
        "dijit/Toolbar",
        "dojo/_base/array",
        "dojo/_base/declare",
        "dojo/_base/lang",
        "dojo/dom-attr",
        "dojo/dom-class",
        "dojo/dom-construct",
        "wamc/config/Toolbars",
        "wamc/user",
        "dojo/i18n!wamc/nls/strings"],
	function(_LayoutWidget,Button,DropDownButton,DropDownMenu,MenuItem,Toolbar,array,declare,lang,domAttr,domClass,domConstruct,Toolbars,user,nls){
	
	var _ActionToolbar = declare("wamc.widget._ActionToolbar",[_LayoutWidget],{

		nls: nls,
		
		buildRendering:function(){
			this.inherited(arguments);
			var F = this.declaredClass + ".buildRendering()";
			console.debug(F);
			
			var t = this,
				definition = this.context ? Toolbars[this.context][this.id] : Toolbars[this.id],
				toolbar,
				showToolbar = false;
			if(definition.header){
				domClass.add(this.domNode,"actionToolbar");
				var headerId = this.id+'_header';
				domConstruct.create("h2",{id:headerId,innerHTML:nls[this.id].title},this.domNode);
				toolbar = new Toolbar({"class":"actionToolbarButtons","aria-labelledby":headerId});
				showToolbar = true;
			}else{
				toolbar = new Toolbar({"aria-label":nls[this.id].title});
			}
			array.forEach(definition.items, function(item){
				if(item.items){ // Drop-down menu
					var showMenu = false;
					var menu = new DropDownMenu({id:t.id + "_" + item.name + "Menu","aria-label":nls[t.id][item.name]});
					array.forEach(item.items, function(subItem){
						if(!subItem.permission||user.hasPermission(subItem.permission)){
							var menuItem = new MenuItem({id:menu.id + "_" + subItem.name,label:nls[t.id][subItem.name],onClick:lang.hitch(t, subItem.name),disabled:"disabled"});
							t[subItem.name+'Button'] = menuItem;
							menu.addChild(menuItem);
							showMenu = true;
						}
					});
					if(showMenu){
						var dropDown = new DropDownButton({id:t.id + "_" + item.name,label:nls[t.id][item.name],dropDown:menu});
						domAttr.set(dropDown.domNode,"role","menu");
						toolbar.addChild(dropDown);
						showToolbar = true;
					}
				}else{ // Button
					if(!item.permission||user.hasPermission(item.permission)){
						var button = new Button({id:t.id + "_" + item.name,label:nls[t.id][item.name],iconClass:item.iconClass,showLabel:false,onClick:lang.hitch(t, item.name),disabled:"disabled"});
						t[item.name+'Button'] = button;
						toolbar.addChild(button);
						showToolbar = true;
					}else{
						console.debug(F,"User does not have permission " + item.permission + " for item " + item.name);
					}
				}
			});
			if(showToolbar){
				this.addChild(toolbar);
			}
		},
		
		setActionState:function(/*Object*/params){
			// summary:
			//		Set the state of actions in the toolbar
			//		{addAppliance:"enabled",removeAppliance:"disabled"}
			// params: Object
			//		The state for each action. The value for each action should be
			//		"enabled" or "disabled"
			var F = this.declaredClass + ".setActionState(): ";
			console.debug(F,params);
			
			for(var name in params){
				
				var button = this[name+"Button"];
				
				if(button){
					button.set("disabled",(params[name] == "disabled"));
				}
			}
		}
		
	});
	return _ActionToolbar;
	
});
