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
        "dojo/_base/Deferred",
        "dojo/_base/lang",
        "dojox/widget/Wizard"],
        function(declare,Deferred,lang,Wizard){
	
	return declare("wamc.widget.Wizard",[Wizard],{
		
		_checkButtons: function(){
			// summary: override the checkbuttons method to disable the 'next'
			//		and 'done' buttons if the passFunction doesn't pass
			var F = this.declaredClass + "._checkButtons()";
			console.debug(F);
			
			var sw = this.selectedChildWidget,
				pf = sw.passFunction,
				pass = true;
			
			console.debug("Pass Function",typeof pf,pf);
			

			if(typeof pf === "function"){
				pass = pf();
			}
			
			var lastStep = sw.isLastChild;
			this.nextButton.set("disabled", lastStep || !pass);
			
			this._setButtonClass(this.nextButton,lastStep);
			if(sw.doneFunction){

				this.doneButton.domNode.style.display = "";
				this.doneButton.set("disabled",!pass);
				this._setButtonClass(this.doneButton);	
				if(lastStep){
					this.nextButton.domNode.style.display = "none";
				}
			}else{
				// #1438 issue here.
				this.doneButton.domNode.style.display = "none";
			}
			this.previousButton.set("disabled", !this.selectedChildWidget.canGoBack);
			this._setButtonClass(this.previousButton,!this.selectedChildWidget.canGoBack);
			
		},
		
		_forward: function(){
			// summary: callback when next button is clicked
			var F = this.declaredClass + "._forward()";
			console.debug(F);
			
			if(this.selectedChildWidget._checkPass()){
				
				Deferred.when(this._performAction(this.selectedChildWidget),
						lang.hitch(this,function(result){
							console.debug(F,"deferred resolved",result);
					if(result===true){
						console.debug(F,"Calling Forward");
						this.forward();
					}
				}));
			}
		},
		
		_performAction:function(/*WizardPane*/widget){
			// summary: 
			// return: Deferred
			//		A deferred that resolves when the actionFunction of the 
			//		widget has completed.
			// tags:
			//		protected
			var F = this.declaredClass + "._performAction()";
			console.debug(F,widget);
			
			if(widget.actionFunction && lang.isFunction(widget.actionFunction)){
				console.debug(F,"Performing Action Function");
				return widget.actionFunction();
			}
			console.debug(F,"No Action Function, returning true");
			return true;
		},
		
		_setButtonClass: function(button,forceHide){
			// summary:
			//		Hide buttons that shouldn't be enabled
			// description:
			//		Overriding because we always hide buttons that aren't
			//		applicable, (e.g. next on last form, done when no
			//		donefunction is set).
			//		When hideDisabled is enabled it only applies
			//		to buttons that might be appropriate.
			// button: dijit/form/Button
			//		The button to set the class of
			// hideDisabled: Boolean
			//		Force his button to be hidden
			// tags:
			//		protected
			
			button.domNode.style.display = (this.hideDisabled && button.disabled) || 
				forceHide===true ? "none" : "";
		},
		
		
	});
	
});
	
	