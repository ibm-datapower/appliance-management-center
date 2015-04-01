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
        "dojo/_base/lang",
        "dojo/aspect",
        "dojo/dom-class",
        "dijit/registry",
        "dijit/form/ValidationTextBox",
        "wamc/widget/form/TextBox"],
        function(declare,lang,aspect,domClass,registry,ValidationTextBox,TextBox){
	
	var UploaderValueTextBox = declare("wamc.widget.form.UploaderValueTextBox",[TextBox],{
		// summary:
		//		This is an idx textbox widget that displays the currently 
		//		selected value of an uploader.
		// uploader:
		//		The uploader to link this to
		uploader: null,
		
		startup:function(){
			// summary:
			//		Widget lifecycle method
			var F=this.declaredClass + ".startup()";
			console.debug(F);
			
			var t=this;
			t.inherited(arguments);
			// This call is delayed until startup to give the uploader a chance to initialize
			t._setupUploader(); 
		},
		
		postCreate:function(){
			// summary:
			//		Widget lifecycle method
			this.inherited(arguments);
			var F=this.declaredClass + ".postCreate()";
			console.debug(F);
			
			if(this.instantValidate){
				this.connect(this, "_setValueAttr", function(){
					this.validate(this.focused);
				});
			}
		},
		
		_setupUploader:function(){{
			// summary:
			//		Setup the uploader connections and events
			// tags:
			//		protected
			var F=this.declaredClass + "._setupUploader()";
			console.debug(F);
			
			var t=this,i,u;
			// First disconnect any existing uploader connections
			if(lang.isArray(t._sigs)){
				for(i=0;i<t._sigs.length;i++){
					t._sigs[i].remove();
				}
			}
			
			t._sigs = [];
			
			if(lang.isArray(t._watches)){
				for(i=0;i<t._watches.length;i++){
					t._watches[i].unwatch();
				}
			}
			
			t._watches = [];
			
			if(t.uploader){
				u = registry.byId(t.uploader);
				
				t.set("uploader",u);
				
				// Connect up uploader events
				t._sigs.push(aspect.after(t.uploader,"onChange",lang.hitch(t,"_showUploaderValue"),true));
				
				t._sigs.push(aspect.after(t.uploader,"reset",lang.hitch(t,"reset")));
				
				t._watches.push(t.uploader.watch("required",lang.hitch(t,"_mirrorAttr")));
				
				t._watches.push(t.uploader.watch("disabled",lang.hitch(t,"_mirrorAttr")));
				
			}
			
		}},
		
		_showUploaderValue:function(files){
			// tags:
			//		protected
			var F=this.declaredClass + "._showUploaderValue()";
			console.debug(F,files);
			
			var t=this,value="";

			if(files.length){
				var i=0, f, r=[];
				while((f=files[i++])){
					if(f && f.name){
						r.push(f.name);
					}
				}
				value=r.join(', ');
			}
			t.set("value",value);
		},
		
		_setRequiredAttr:function(required){
			// Override so that setting required doesn't actually cause the box
			// to be required, just add the little star
			domClass.toggle(this.stateNode,"dijitRequired",required);
			domClass.toggle(this.domNode,"dijitRequired",required);
		},
		
		_mirrorAttr:function(name,oldValue,newValue){
			// summary:
			//		Copy the boolean attribute of the uploader. Used to 
			//		mirror the 'required' and 'disabled' values of the uploader.
			// tags:
			//		protected
			var F=this.declaredClass + "._mirrorAttr()";
			console.debug(F,name,oldValue,newValue);
			
			this.set(name,newValue);
		}
		
	});
	
	return UploaderValueTextBox;
});