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
        "dojo/_base/event",
        "dojo/_base/lang",
        "dojo/_base/window",
        "dojo/aspect",
        "dojo/on",
        "dojo/dom-attr",
        "dojo/dom-class",
        "dojo/dom-geometry",
        "dojo/dom-style",
        "dojo/dom",
        "dojo/fx",
        "dojo/keys",
        "dojo/string",
        "dijit/_Widget",
        "dijit/_TemplatedMixin",
        "wamc/date/format",
        "wamc/config/Messages",
        "dojo/i18n!wamc/widget/messaging/nls/MessageArea",
        "dojo/text!wamc/widget/messaging/templates/MessageArea.html",
        "dojo/text!wamc/widget/messaging/templates/MessageAreaCause.html"],
        function(declare,event,lang,win,aspect,on,domAttr,domClass,domGeom,
        		domStyle,dom,coreFx,keys,string,_Widget,_TemplatedMixin,
        		dateFormat,Messages,nls,template,causeTemplate){
	
	var _buildCauseText = function(/*Object*/cause,/*boolean*/rootCause){
		console.debug("wamc.widget.messaging.MessageArea._buildCauseText",cause,rootCause);
		var c, hasExplanation, hasUserAction;
		
		hasExplanation = (cause.explanation &&
				cause.explanation != "!null.explanation!");
		
		hasUserAction = (cause.userAction &&
				cause.userAction != "!null.useraction!");
		
		c = {
			explanationHeader:nls.messageArea.explanationHeader,
			userActionHeader:nls.messageArea.userActionHeader
		};
		
		c.causeClass = rootCause ? "rootCause" : "nestedCause";
		c.code = cause.code? cause.code+": " : "";
		c.message = cause.message ? cause.message : "";
		c.explanationClass = hasExplanation ? "" : "dijitHidden";
		c.explanation = hasExplanation ? cause.explanation : "";
		c.userActionClass = hasUserAction ? "" : "dijitHidden";
		c.userAction = hasUserAction ? cause.userAction : "";
		c.subCause = cause.cause ? _buildCauseText(cause.cause) : "";
		c.tabIndex = 0;
		
		console.debug("wamc.widget.messaging.MessageArea._buildCauseText","built object",c);
		
		return string.substitute(causeTemplate,c);
	},
	
	showMessage = function(/*Node/String*/node,/*Object*/args){
		// summary:
		//		Show a message area with the given arguments at a node.
		// node: Node
		//		The dom node or reference at which to show the message
		// args: Object
		//		Arguments for the message area.
		// return: MessageArea
		//		A reference to the message area that has been created
		console.debug("wamc.widget.messaging.MessageArea.showMessage()",node,args);
		
		var n = dom.byId(node), ma;
		
		if(args.code === "CWZBA1002E" && args.cause){
			// Promote cause to top-level message for generic action failure message
			args.message = args.cause.message;
			args.userAction = args.cause.userAction;
			args.explanation = args.cause.explanation;
			args.code = args.cause.code;
			args.cause = args.cause.cause;
		}
		
		ma = new MessageArea(args,n);
		
		ma.startup();
		
		return ma;
	},
	
	computeErrorLevel = function(/*String*/CWZCode){
		var F = "wamc.widget.messaging.MessageArea..computeErrorLevel()";
		console.debug(F, CWZCode);
		
		var levelChar = CWZCode.charAt(9);
		return {I:"Information",
				A:"Warning",
				W:"Warning",
				E:"Error"}[levelChar];
	},
	
	MessageArea = declare("wamc.widget.messaging.MessageArea",
			[_Widget,_TemplatedMixin],{	

		baseClass: "wamcMessage",
		
		nls: nls.messageArea,
		
		templateString: template,
		
		tabIndex: 0,
		
		// type: String
		//		The type of message to display. 
		//		One of Information, Error, Warning, Confirmation
		type:"Error",
		
		// message: String
		//		The message to display
		message:"Default message",
		
		// explanation: String
		//		The explanation for the error.
		//		May be null, especially for Information messages.
		explanation: null,
		
		// userAction: String
		//		The action to take to correct the error.
		//		May be null, especially for Information messages.		
		userAction:null,
		
		// code: String
		//		The message code for reference in the docs and for support requests.
		//		Optional for client-generated messages.
		code:null,
		
		// timestamp:
		//		The time (in the server's timezone) that the message was created. For tying up with server logs, 
		//		hence not applicable to client-generated messages.
		timestamp:null,
		
		// cause: Object
		//		A linked exception object, with its own message, code, explanation and userAction properties, and 
		//		potentially (and recursively) a further nested cause. Optional.
		cause:null,
		
		// closeable:
		//		Provide a close icon to remove the message.
		//		Also determines placement of the messageFooter.
		closable: true,
		
		startup:function(){
			this.inherited(arguments);
			
			var parent = this.getParent && this.getParent();
			
			if(!(parent && parent.isLayoutContainer)){
				this.resize();

				// If I'm not part of a layout container, resize when the window does
				this.connect(win.global, 'onresize', function(){
					// Using function(){} closure to ensure no arguments passed to resize().
					this.resize();
				});
			}
		},
		
		_setTypeAttr: function(/*String*/pType){
			var oldClass = "wamc" + this.type + "Message",
				newClass = "wamc" + pType + "Message";
			domClass.toggle(this.domNode, oldClass, false);
			domClass.toggle(this.domNode, newClass, true);
			domAttr.set(this.typeNode,"innerHTML",pType);
			this._set("type",pType);
		},
		
		_setMessageAttr: function(/*String*/pMessage){
			this._set("message",pMessage);
			domAttr.set(this.messageNode,"innerHTML",pMessage);
		},
		
		_setExplanationAttr: function(/*String*/explanation){
			var hasExplanation = (typeof explanation === "string" &&
					explanation !== "!null.explanation!");
			
			this._set("explanation",explanation);
			domAttr.set(this.explanationNode,"innerHTML",explanation);
			domClass.toggle(this.explanationSectionNode,"dijitHidden",!hasExplanation);
			
			this._toggleDetailLinks();
			
		},
		
		_setUserActionAttr: function(/*String*/userAction){
			var hasUserAction = (typeof userAction === "string" && 
					userAction !== "!null.useraction!");
			
			this._set("userAction",userAction);
			domAttr.set(this.userActionNode,"innerHTML",userAction);
			domClass.toggle(this.userActionSectionNode,"dijitHidden",!hasUserAction);
			
			this._toggleDetailLinks();
			
		},
		
		_toggleDetailLinks:function(){
			var hasExplanation = (typeof this.explanation === "string" &&
					this.explanation !== "!null.explanation!"),
				hasUserAction = (typeof this.userAction === "string" && 
					this.userAction != "!null.useraction!");
			domClass.toggle(this.detailsLinks,"dijitHidden",
					!(hasExplanation || hasUserAction));
			
		},
		
		_setClosableAttr:function(/*boolean*/closable){
			var c = typeof closable === "boolean" && closable;
			this._set("closable",c);
			
			domClass.toggle(this.headlineRight,"dijitHidden",!c);
			
			this._toggleCodeSection();
		},
		
		_setCodeAttr: function(/*String*/code){
			var hasCode = (typeof code === "string" &&
					code !== "hidden"),
				displayCode = hasCode ? code : "";
			
			this._set("code",code);
			
			domAttr.set(this.codeNode,"innerHTML",displayCode);
			domAttr.set(this.codeTopNode,"innerHTML",displayCode);
			
			this._toggleCodeSection();
		},
		
		_setTimestampAttr: function(/*String*/timestamp){
			var hasValue = (typeof timestamp =="string"),
				formattedTimestamp = "";
			
			this._set("timestamp",timestamp);
			
			if(hasValue){
			
				formattedTimestamp = dateFormat.reformat(timestamp,{formatLength:"medium", timePattern:dateFormat.timePattern});
			}
			
			domAttr.set(this.timestampNode,"innerHTML",formattedTimestamp);
			domAttr.set(this.timestampTopNode,"innerHTML",formattedTimestamp);
			
			this._toggleCodeSection();
		},
		
		_toggleCodeSection:function(){
			var hasCode = (typeof this.code === "string" &&
					this.code !== "hidden"),
				hasTimestamp = (typeof this.timestamp =="string"),
				hideTopSection = !(hasCode || hasTimestamp) || this.closable,
				hideLowerSection = !(hasCode || hasTimestamp) || !this.closable;
			
			// For some reason, if the message is not closable, the code and 
			// timestamp appear at the top
			domClass.toggle(this.codeSectionNode,"dijitHidden",hideLowerSection);
			domClass.toggle(this.codeSectionTopNode,"dijitHidden",hideTopSection);
		},
		
		_setCauseAttr: function(/*Object*/cause){
			var hasValue = (cause != null);	// TODO test for required object values
			
			this._set("cause",cause);
			
			if(hasValue){
				domAttr.set(this.causeNode,"innerHTML",_buildCauseText(cause,true));
			}
			
			domClass.toggle(this.causeSectionNode,"dijitHidden",!hasValue);
		},
		
		collapseDetails: function(){
			// summary:
			//		Collapse the details section of the message area.
			domAttr.set(this.expandCollapseNode,"innerHTML",this.nls.expand);
			domAttr.set(this.expandCollapseNode,"aria-expanded",false);
			
			var anim = coreFx.wipeOut({node: this.details});
			
			aspect.after(anim,"onEnd",lang.hitch(this,function(){
				this.onResize();
			}));
			
			anim.play();
		},
		
		expandDetails: function(){
			// summary:
			//		Expand the details section of the message area.
			domAttr.set(this.expandCollapseNode,"innerHTML",this.nls.collapse);
			domAttr.set(this.expandCollapseNode,"aria-expanded",true);
			var anim = coreFx.wipeIn({node: this.details});
			
			aspect.after(anim,"onEnd",lang.hitch(this,function(){
				this.onResize();
			}));
			
			anim.play();
		},
		
		toggleDetails:function(){
			var cs = domStyle.getComputedStyle(this.details),
				collapsed = cs.display === "none";
			
			if(collapsed){
				this.expandDetails();
			}else{
				this.collapseDetails();
			}
		},
		
		expandCause: function() {
			// summary:
			//		Show the cause area.
			// Change Icon Style
			domClass.remove(this.toggleCauseIcon,"wamcExpandIcon");
			domClass.add(this.toggleCauseIcon,"wamcCollapseIcon");
			
			// Change Icon Alt Text
			domAttr.set(this.toggleCauseIconAltText,"innerHTML",this.nls.collapse);
			
			// Change Expanded state
			// Do Animation
			domAttr.set(this.toggleCauseIcon,"aria-expanded",true);
			
			var anim = coreFx.wipeIn({node: this.causeNode});
			
			aspect.after(anim,"onEnd",lang.hitch(this,function(){
				this.onResize();
			}));
			
			anim.play();
		},
		
		collapseCause: function(){
			// summary:
			//		Hide the cause area.
			
			domClass.remove(this.toggleCauseIcon,"wamcCollapseIcon");
			domClass.add(this.toggleCauseIcon,"wamcExpandIcon");
			domAttr.set(this.toggleCauseIconAltText,"innerHTML",this.nls.expand);

			domAttr.set(this.toggleCauseIcon,"aria-expanded",false);
			
			var anim = coreFx.wipeOut({node: this.causeNode});
			
			aspect.after(anim,"onEnd",lang.hitch(this,function(){
				this.onResize();
			}));
			anim.play();
		},
		
		toggleCause:function(){
			var cs = domStyle.getComputedStyle(this.causeNode),
				collapsed = cs.display === "none";
			
			if(collapsed){
				this.expandCause();
			}else{
				this.collapseCause();
			}
		},
		
		close:function(){
			var F = this.declaredClass + ".close()";
			console.debug(F);
			this.destroyRecursive();
		},
		
		clear:function(){
			// summary:
			//		Remove all content from the message area, and reset the 
			//		visibility of the sections to their initial states, so that 
			//		the message area can be used again.
			var F = this.declaredClass + ".clear(): ";
			console.debug(F);
			
			// These elements should have all child nodes and text content removed.
			var elementsToEmpty = [this.messageNode, this.explanationNode, this.userActionNode, this.timestampNode, this.codeNode, this.causeNode];
			for (var i = 0; i < elementsToEmpty.length; i++)
			{
				domAttr.set(elementsToEmpty[i],"innerHTML","");
			}
			
			// These elements should start off hidden.
			var elementsToHide = [this.detailsLinks, this.explanationSectionNode, this.userActionSectionNode, this.causeSectionNode];
			for (i = 0; i < elementsToHide.length; i++)
			{
				domClass.add(elementsToHide[i],"hidden");
			}
			
			// Set the various things needed for collapsed state.
			this.collapseDetails();
			this.collapseCause();
		},
		
		onResize:function(){
			// summary:
			//		Called when the size of this widget is changed
			//		e.g. When the cause is expanded/collapsed
			// tags:
			//		callback
			var F = this.declaredClass + ".onResize()";
			console.debug(F);
		},
		
		_onKey:function(evt){
			// summary:
			//		Control function for key navigation on this widget
			// tags:
			//		protected
			var F = this.declaredClass + "_onKey()";
			console.debug(F,evt);
			
			var src=evt.target,
				charOrCode = evt.charCode || evt.keyCode;
			
			switch(charOrCode){
			case keys.SPACE:
			case keys.ENTER:
				switch(src){
				case this.expandCollapseNode:
					event.stop(evt);
					this.toggleDetails();
					break;
				case this.toggleCauseLink:
				case this.toggleCauseIcon:
					event.stop(evt);
					this.toggleCause();
					break;
				case this.closeIcon:
					event.stop(evt);
					this.close();
					break;
				default:
					// Do nothing
				}
				
				// Handle expand event
				break;
			default:
				// Do nothing
			}
		},
		
		resize:function(){
			var F = this.declaredClass + "resize()";
			console.debug(F);
			
			var iconWidth = domGeom.getMarginBox(this.headlineLeft).w,
				closeWidth = domGeom.getMarginBox(this.headlineRight).w,
				targetWidth = domGeom.getContentBox(this.domNode).w - (iconWidth + closeWidth);
			
			if(targetWidth < 0){
				targetWidth = 0;
			}
			
			domStyle.set(this.headlineCenter,"width",targetWidth + "px");
		}
		
	});
	
	// Export these functions as static functions of the module
	MessageArea.showMessage = showMessage;
	
	MessageArea.computeErrorLevel = computeErrorLevel;
		
	return MessageArea;
	
});
