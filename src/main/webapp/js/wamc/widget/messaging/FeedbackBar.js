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
        "dojo/aspect",
        "dojo/dom-attr",
        "dojo/dom-geometry",
        "dojo/dom-style",
        "dojo/string",
        "dojo/topic",
        "dijit/_Container",
        "dijit/_Widget",
        "dijit/_TemplatedMixin",
        "dijit/registry",
        "wamc/widget/messaging/MessageArea",
        "dojo/i18n!wamc/nls/strings",
        "dojo/text!wamc/widget/messaging/templates/FeedbackBar.html"],
        function(array,declare,lang,aspect,domAttr,domGeometry,domStyle,string,
        		topic,_Container,_Widget,_TemplatedMixin,registry,MessageArea,
        		nls,template){
	
	var FeedbackBar = declare("wamc.widget.messaging.FeedbackBar",
				[_Widget,_Container,_TemplatedMixin],{
		// summary:
		//		Bar to be placed on each page to display messages and Server feedback
		// description:
		//		The FeedbackBar appears on every page in WAMC. It listens for messages on
		//		the 'feedback' topic and displays them to the user.
		//		Multiple messages will stack on top of each other.
		//		There is the option to have informational messages automatically disappear 
		//		after a configurable interval.
		
		baseClass:"wamcFeedbackBar",
		
		templateString:template,
		
		// status: Object
		//		A map of actionStatus values to message types
		status:{
			SUBMITTED: "Information",
			STARTED: "Information",
			SUCCEEDED: "Success",
			FAILED: "Error"
		},
		
		startup:function(){
			var F = this.declaredClass + ".startup()";
			console.log(F);
			this.inherited(arguments);
			
			topic.subscribe("actionStatus",lang.hitch(this,function(actionStatus){
				console.debug("FeedbackBar received msg",actionStatus);
				this.displayMessage(actionStatus.actionId, this._messageFromActionStatus(actionStatus));
			}));
			
			var parent = registry.getEnclosingWidget(this.domNode.parentNode);
			
			if(parent && parent instanceof dijit.layout.BorderContainer){
				aspect.after(this,"_resize",function(){parent.resize();});
			}
		},
	
		displayMessage: function(/*String*/actionId, /*Object*/message){
			// summary:
			//		Display a message in the feedback bar
			var F = this.declaredClass + ".displayMessage()";
			console.log(F,actionId,message);
			
			var t = this,
				msgId = this.id + "_MessageArea_" + actionId,
				msg = lang.mixin({id:msgId},message),
				ma;
			
			t.hideMessage(actionId);
			
			ma = new MessageArea(msg);
			
			aspect.after(ma,"onResize",lang.hitch(t,"_resize"));
			
			aspect.before(ma,"close",function(){
				t.removeChild(ma);
			});
			
			this.addChild(ma);
		},
		
		hideMessage:function(/*String*/actionId){
			// summary:
			//		Hide a message with the specified actionId
			// actionId:
			//		String
			var F = this.declaredClass + ".hideMessage()";
			console.log(F,actionId);
			
			var msgId = this.id + "_MessageArea_" + actionId,
				ma = registry.byId(msgId);
			
			if(ma){
				ma.destroyRecursive();
			}
		},
		
		showMessage:function(/*Object*/args){
			// summary:
			//		Display a message in the feedback bar.
			// args: Object
			//		The same arguments object as a message area
			var ma = new MessageArea(args);
			
			this.addChild(ma);

			aspect.after(ma,"onResize",lang.hitch(this, function(){
				this._resize();
			}));
			aspect.before(ma,"close",lang.hitch(this,function(){
				this.removeChild(ma);
			}));
			
			this._resize();
		},
		
		_messageFromActionStatus: function(/*Object*/actionStatus){
			// summary:
			//		Return a message object formatted from an ActionStatus update
			// tags:
			//		protected
			var F = this.declaredClass + "._messageFromActionStatus()";
			console.log(F,actionStatus);
			
			var lastUpdate = actionStatus.updates[actionStatus.updates.length-1];

			// Promote cause to top-level message for generic action failure message
			if (lastUpdate.code === "CWZBA1002E" && lastUpdate.cause) {
				lastUpdate = lastUpdate.cause;
			}
			
			var message = actionStatus.description + (lastUpdate ? " | " + lastUpdate.message : "");
			
			if(actionStatus.result){ // Add backup link
				message = message + string.substitute(nls.backup.download,{uri:actionStatus.result});
			}
			
			if(actionStatus.state === "FAILED" &&
					(lastUpdate.explanation || lastUpdate.userAction || lastUpdate.cause)) {
				message = message + '&nbsp;&nbsp;| <a href="/amc/#history" title="'
					+ string.substitute(nls.feedback.historyHover) +'">'
					+ string.substitute(nls.feedback.historyLink) + '</a>';
			}
			
			var msg = {
				type: this.status[actionStatus.state],
				title:nls.level[this.status[actionStatus.state]],
				message:message,
				timestamp:actionStatus.updated
				};
			
			return msg;
		},
		
		addChild: function(/*dijit._Widget*/ child, /*Integer?*/ insertIndex){
			// Overrides _Container.addChild() to call _setupChild()
			this.inherited(arguments);
			if(this._started){
				this._resize();
			}
		},
		
		removeChild: function(/*dijit._Widget*/ child){
			this.inherited(arguments);
			if(this._started){
				this._resize();
			}
		},
		
		_resize:function(){
			var F = this.declaredClass + "._resize()";
			console.log(F);
			
			var s = this._calculateSize();
			console.debug(F,"Calculated Size",s);
			domGeometry.setMarginBox(this.domNode,s);

		},
		
		_calculateSize:function(){
			var F = this.declaredClass + "._calculateSize()";
			console.log(F);
			
			var mb = domGeometry.getMarginBox(this.domNode),
				newBox = lang.mixin(mb,{h:0,w:0});
			
			array.forEach(this.getChildren(),function(child){
				mb = domGeometry.getMarginBox(child.domNode);
				// TODO incorporate top and left into calculations
				newBox.h += mb.h;
				newBox.w = mb.w;
			});
			
			return newBox;
		}
		
	});
	
	return FeedbackBar;
});
