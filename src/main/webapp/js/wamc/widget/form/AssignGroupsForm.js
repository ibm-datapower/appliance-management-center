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
        "dojo/_base/event",
        "dojo/aspect",
        "dojo/dom-attr",
        "dojo/on",
        "dojo/string",
        "dijit/_Widget",
        "dijit/_TemplatedMixin",
        "dijit/_WidgetsInTemplateMixin",
        "dijit/form/Button",
        "dijit/form/Form",
        "dijit/registry",
        "dijit/Tooltip",
        "wamc/widget/form/TextBox",
        "wamc/widget/form/TriStateCheckBox",
        "wamc/_MessagesMixin",
		"wamc/_HelpLabelsMixin",
        "wamc/util",
        "dojo/text!wamc/widget/form/templates/AssignGroupsForm.html",
        "dojo/i18n!wamc/nls/strings"],
		function(array,declare,lang,event,aspect,domAttr,on,string,
				_Widget, _TemplatedMixin, _WidgetsInTemplateMixin,
				Button,Form,registry,Tooltip,TextBox,CheckBox,
				_MessagesMixin,_HelpLabelsMixin,wamcUtil,template,nls) {
	
	var _compareGroupStates = function(/*Object*/first,/*Object*/second){
		// summary:
		//		Compare two groupState objects in order to sort an array
		// first: Object
		//		The first groupState object to compare
		// second: Object
		//		The second groupState object to compare
		// return: integer
		//		The comparison result
		
		if(first.state && !second.state){
			return -1;
		}else if(second.state && !first.state){
			return 1;
		}else{
			var fg = first.group.toUpperCase(),
				sg = second.group.toUpperCase();
			
			if(fg === sg){
				if(first.group === second.group){
					return 0;
				}else{
					return first.group < second.group ? -1 : 1;
				}
			}
			else{
				return fg < sg ? -1 : 1;
			}
		}
	};

	var AssignGroupsForm = declare("wamc.widget.form.AssignGroupsForm",
				[_Widget,_TemplatedMixin, _WidgetsInTemplateMixin,_MessagesMixin,_HelpLabelsMixin],{
		// summary:
		//		A form widget that allows users to change the group assignment
		//		of WAMC entities e.g. Appliances, Domains
		// description:
		//		The user may pass in a type parameter on creation which
		//		specifies the type of object that will be managed by the form.
		//		The default is "APPLIANCE".
		//		The groups parameter is a list of all available groups and their
		//		members
		//		The objects parameter is a list of the WAMC objects to
		//		have their group assignments modified.

		nls: nls,
		
		templateString: template,
		
		modified: false,
		
		postCreate:function(){
			// summary:
			//		Widget life-cycle method.
			var F = this.declaredClass + ".postCreate()";
			console.debug(F);
			
			this.inherited(arguments);
			
			on(this.formNode, "submit", lang.hitch(this, function(evt){
				// Stop default event propagating up.
				event.stop(evt);
				this._submit();
			}));	
			
			// Setup button connections
			on(this.cancelButton, "click", lang.hitch(this, function(evt){
				event.stop(evt);
				this.reset();
				this.onCancel();
			}));
			
			on(this.addButton,"click",lang.hitch(this,function(evt){
				event.stop(evt);
				var name = this.newGroupDataField.get("value");
				if(this.validateGroup(name)){
					this.modified = true;
					this.addGroup(name);
				}
			}));
			
			aspect.after(this.newGroupDataField,"_onInput",lang.hitch(this,this._setButtonStates));
			
			aspect.after(this.newGroupDataField,"onChange",lang.hitch(this,this._setButtonStates));
		},
		
		_submit:function(){
			// summary:
			//		Called on form submission. Update the list of objects
			//		with groups from the form
			// tags:
			//		protected
			var F = this.declaredClass + "._submit()";
			console.debug(F);
			
			this._applyGroups();
			
			// Call onSubmit so that controller can hook in
			this.onSubmit(this.objects);
		},
		
		reset: function(){
			// summary:
			//		Reset the widget, including the form fields and state
			var F = this.declaredClass + ".reset()";
			console.debug(F);
		
			delete this.objects;
			delete this.groups;
			
			this.modified = false;
			this._setButtonStates();
			
			this._resetForm();
		},
		
		_resetForm:function(){
			// summary:
			//		Reset the form elements of the widget
			// tags:
			//		protected
			var F = this.declaredClass + "._resetForm()";
			console.debug(F);
			
			this.hideMessage();
			
			// Destroy the list of checkboxes
			array.forEach(registry.findWidgets(this.existingGroupsNode),function(cb){
				cb.destroy();
			});
			
			// Remove text placeholder for 'no groups'
			domAttr.set(this.existingGroupsNode,"innerHTML","");
			
			// reset the new group field
			this.formNode.reset();
		},
		
		onCancel: function(){
			// summary:
			//		Placeholder for more events to occur on cancel
			// tags:
			//		override
			var F = this.declaredClass + ".onCancel()";
			console.debug(F);
		},
		
		onSubmit: function(/*Object*/form){
			// summary:
			//		Placeholder for more events to occur on submit
			// tags:
			//		override
			var F = this.declaredClass + ".onSubmit()";
			console.debug(F,form);
		},
		
		onError: function(){
			// summary:
			//		Placeholder for more events to occur when something goes wrong
			// tags:
			//		override
			var F = this.declaredClass + ".onError()";
			console.debug(F);
		},
		
		_setGroupsAttr:function(/*Array*/groups){
			// summary:
			//		Set the list of groups displayed in the form
			// groups: Array
			//		An array of group objects containing 
			//		group names and their members
			
			var F = this.declaredClass + "._setGroupsAttr()";
			console.debug(F,groups);
			
			this.groups = groups;
			
			if(this.objects){
				this.refresh();
			}
			
		},
		
		_setObjectsAttr:function(/*Array*/objects){
			// summary:
			//		Set the list of objects (e.g.,appliances) to assign groups 
			//		to. When this method is called, the list of displayed groups
			//		gets updated
			// objects: Array
			//		An array of objects with a 'groups' property
			var F = this.declaredClass + "._setObjectsAttr()";
			console.debug(F,objects);
			
			this.objects = objects;
			
			this.refresh();
		},
		
		refresh:function(){
			// summary:
			//		Rebuild the list of groups based on the groups and object
			//		lists for the dialog
			var F = this.declaredClass + ".refresh()";
			console.debug(F);
			
			var t = this, n = this.existingGroupsNode;
			
			// First, reset the form elements
			this._resetForm();
			
			if(this.groups.length < 1){
				// If no groups, show no groups message
				domAttr.set(this.existingGroupsNode,"innerHTML","<p>" + nls.assignGroupsForm.existingGroups.noGroups + "</p>");
				return;
			}
			
			// Calculate starting states for checkboxes, based on groups
			var groupStates = array.map(this.groups,function(group){
				
				var gs = {group:group.name};
				array.forEach(this.objects,function(obj){
					var hasGroup = lang.isArray(obj.groups) && array.indexOf(obj.groups,group.name)>=0;
					
					if(gs.hasOwnProperty("state") && gs.state!== hasGroup){
						// If the state is set, but not true, set it to mixed 
						gs.state = "mixed";
					}else{
						gs.state = hasGroup;
					}
					gs.size = lang.isArray(group.members) ? group.members.length : 0;
				},this);
				
				gs.members = group.members;
				
				return gs;
			},this);
			
			// Sort the list so that groups with members appear at the top.
			groupStates.sort(_compareGroupStates);
			
			// Create Checkboxes for the sorted list
			array.forEach(groupStates,function(gs){
				var allowedStates = gs.state === "mixed" ? [true,"mixed",false] : [true,false],
						label = string.substitute(nls.assignGroupsForm.existingGroups.groupLabel,
								{name:gs.group,count:gs.size}),
						cb = new CheckBox({
							name:gs.group,
							label:label,
							states:allowedStates,
							checked:gs.state
						});
				
				cb.placeAt(n,"last");
				cb.startup();
				
				// Add the tooltip for each checkbox
				on(cb.compLabelNode,"mouseover",function(evt){
					// Prevent bubbling. For some reason onmouseenter does not
					// fire in Chrome correctly in dialogs.
					event.stop(evt);
					Tooltip.show(t.buildTooltipContent(gs),cb.compLabelNode,["below","after"]);
				});
				
				on(cb.compLabelNode,"mouseout",function(evt){
					event.stop(evt);
					Tooltip.hide(cb.compLabelNode);
				});
				
				// Mark as modified when checkbox clicked
				on(cb,"click",function(evt){
					t.modified = true;
					t._setButtonStates();
				});
				
			},this);
		},
		
		validateGroup:function(/*String*/name){
			// summary: 
			//		Check to see if a group with the specified name can be 
			//		added. Displays an error message if not.
			// name: String
			//		The name of the new group
			// return: Boolean
			//		True if the group can be added, false otherwise
			var F = this.declaredClass + ".addGroup()";
			console.debug(F,name);
			
			this.hideMessage();
			
			if(array.some(this.groups,function(group){
				return group.name === name;
			})){
				this.showMessage("ERROR_GROUP_EXISTS",{group:name});
				return false;
			}
			return true;
		},
		
		addGroup:function(/*String*/name){
			// summary: 
			//		Add a new group to the list. The new group will be added
			//		to all objects
			// name: String
			//		The name of the new group
			var F = this.declaredClass + ".addGroup()";
			console.debug(F,name);
			
			// Before adding, update the group membership
			this._applyGroups();
			
			// Make sure there is a groups array
			this.groups = lang.isArray(this.groups) ? this.groups : [];
			
			if(lang.isString(name) && name.length > 0){
				// Add the group to the groups list, if it is not already
				var newGroup = null, newMembers = null;
				
				// See if the group is already in the list
				array.some(this.groups,function(group){
					if(group.name === name){
						// If found, keep a reference to it
						newGroup = group;
						return true;
					}
					return false;
				});
				
				// If the group is not already in the list, create a new one.
				if(!newGroup){
					newGroup = {name:name,members:[]};
					this.groups.push(newGroup);
				}
				
				// Create group members for all the objects selected
				newMembers = array.map(this.objects,function(obj){
					var nm = {};
					nm.type = this.type || "APPLIANCE";
					nm.id = obj.id || obj.primaryKey || obj.name;
					nm.name = obj.displayName || obj.name;
					return nm;
				},this);
				
				// Add all the objects to the group, removing any duplicates
				newGroup.members = wamcUtil.mergeArraysUnique(newGroup.members,newMembers);
				
				// Add the group to all objects
				array.forEach(this.objects,function(obj){
					obj.groups = lang.isArray(obj.groups) ? obj.groups : [];
					if(array.indexOf(obj.groups,name)<0){
						obj.groups.push(name);
					}
				},this);
			}
			// Refresh the form
			this.refresh();
		},
		
		_applyGroups:function(){
			// summary:
			//		Apply the current group selection to groups. Called by
			//		submit, and also before adding a new group to the list
			// tags:
			//		protected
			
			var F = this.declaredClass + "._applyGroups()";
			console.debug(F);
			
			/*
			 * In an ideal world, we would get the value of the form widget and
			 * iterate through the values of the named fields. However, because
			 * the idx TriStateCheckbox can have a string value for its
			 * 'checked' property, Form gets confused and for given name returns
			 * either an array of values (for true and false) or a string (for
			 * 'mixed) it makes it difficult to process.
			 * 
			 * So we do it this way instead:
			 */
			
			// Get all the checkboxes for existing groups
			var cbs = registry.findWidgets(this.existingGroupsNode);
			
			// Create an array of group/state values
			var groupStates = array.map(cbs,function(cb){
				return {group:cb.name,state:cb.get("checked")};
			});
			
			// Update the list of groups for each object. 'mixed' state groups
			// are only included if the object was already a member.
			array.forEach(this.objects,function(obj){
				var ng = [], eg = obj.groups;
				array.forEach(groupStates,function(gs){
					if(gs.state === true || (gs.state ==="mixed" && array.indexOf(eg,gs.group) >= 0)){
						ng.push(gs.group);
					}
				});
				obj.groups = ng;
			});
		},
		
		_setButtonStates:function(){
			// summary:
			//		Disable the add group button when an invalid name appears in the
			//		group name field and disable the apply button when there is
			//		content in the group data field.
			// tags:
			//		protected
			var v = this.newGroupDataField.get("value");
			var notEmpty = (lang.isString(v) && string.trim(v).length > 0);

			this.addButton.set("disabled",!(notEmpty && this.newGroupDataField.validate()));
			this.submitButton.set("disabled", notEmpty || !this.modified);
		},
		
		buildTooltipContent:function(/*Object*/group){
			// summary:
			//		Build the tooltip content for a group
			// description:
			//		The content looks like this
			//		Group name: 'group name'
			//		Members (appliances): appliance1, appliance2
			//		Members (domains): (none)
			// group: Object
			//		The group to build the tooltip content for
			var F = this.declaredClass + ".buildTooltipContent()";
			console.debug(F,group);
			
			var appliances = [], domains = [], args = {};
			
			array.forEach(group.members,function(mbr){
				switch(mbr.type){
				case "APPLIANCE": 
					appliances.push(mbr.name); break;
				case "DOMAIN":
					domains.push(mbr.name); break;
				default:
					//do nothing
				}
			});
			
			// Sort the arrays of members with natural order
			appliances.sort();
			domains.sort();
			
			args.name = group.name || group.group;
			args.appliances = appliances.length > 0 ? appliances: nls.assignGroupsForm.none;
			args.domains = domains.length > 0 ? domains: nls.assignGroupsForm.none;
			
			return string.substitute(nls.assignGroupsForm.tooltip,args);
		}
		
	});
		
	return AssignGroupsForm;
	
});
