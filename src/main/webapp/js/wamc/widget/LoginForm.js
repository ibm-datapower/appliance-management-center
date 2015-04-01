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
		"dojo/cookie",
		"dojo/dom",
		"dojo/dom-style",
		"dojo/io-query",
		"dojo/query",
		"dojo/NodeList-manipulate",
		"dijit/_TemplatedMixin",
		"dijit/_WidgetsInTemplateMixin",
		"dijit/_Widget",
		"wamc/validation",
		"dojo/i18n!wamc/nls/strings",
		"dojo/text!wamc/widget/templates/LoginForm.html",
		"dijit/form/Button",
		"dijit/form/Form",
		"wamc/widget/form/TextBox"],
		function(declare,lang,cookie,dom,domStyle,ioquery,query,manipulate,
				_TemplatedMixin,_WidgetsInTemplateMixin,_Widget,
				wamcValidation, nls,template){

	var LoginForm = declare("wamc.widget.LoginForm",
				[_Widget, _TemplatedMixin, _WidgetsInTemplateMixin], {
		
		nls: nls,
		
		templateString : template,
		
		baseClass: "wamcLoginFrame",	// For CSS
		
		// isContainer: [readonly] String
		//		Allow users to add other components (e.g. hidden fields) under password field
		isContainer: true,
		
		// loginTitle: String
		//		Title to be displayed above the login form.
		loginTitle: nls.loginForm.title,
		
		// loginSubTitle: String
		//		Subtitle to be displayed immediately beneath {@link idx.app.LoginFrame#loginTitle}
		loginSubTitle: nls.loginForm.subTitle,
		
		// labelSeparator: String
		//		The character sequence to use as a label separator.  Typically a colon (":") in the en_us locale.
		labelSeparator: nls.global.labelSeparator,
		
		_setLabelSeparatorAttr: function(value) {
			// tags:
			//		protected
			this._set("labelSeparator",value);
			
			var separator = this.labelSeparator;
			if (this.domNode) {
				query(".wamcLoginSeparator",this.domNode).forEach(function(node,index,nodeList){
					node.innerHTML = separator;
				});
			}
		},
		
		// labelUserName: String
		//		Label that corresponds to the first text field in the form.
		labelUserName: nls.loginForm.userId,
		
		// labelPassword: String
		//		Label that corresponds to the second text field in the form.
		labelPassword: nls.loginForm.password,
		
		// Map the label attributes.
		attributeMap: lang.delegate(_Widget.prototype.attributeMap, {
				inactivityMessage: {node: "inactivityMessageNode", type: "innerHTML"},
				loginTitle: {node: "loginTitleNode", type: "innerHTML"},
				loginSubTitle: {node: "loginSubtitleNode", type: "innerHTML"},
				loginCopyright: {node: "copyrightNode", type: "innerHTML"},
				invalidMessage: {node: "invalidMessageNode", type: "innerHTML"}
		}),
		
		// inactivityMessage: String
		//		Informational message to be displayed directly above the form's buttons.
		inactivityMessage: nls.loginForm.inactivityMessage,
		
		// loginCopyright: String
		//		Copyright statement to be displayed below the form.
		loginCopyright: nls.loginForm.copyright,
		
		
		// labelSubmitButton: String
		//		Label to be displayed on the submission/login button.
		labelSubmitButton: nls.loginForm.logIn,
		
		_setLabelSubmitButtonAttr: function(value) {
			// tags:
			//		protected
			this._set("labelSubmitButton",value);
			this.loginButton.set("label", this.labelSubmitButton);
		},
		
		// invalidMessage: String
		//		Error message to be displayed when required input 
		//		user name or password is empty or blank.
		invalidMessage: nls.loginForm.invalidMessage,
		
		// invalidMessageTitle: String
		//		Error message dialog title when login button clicked 
		//		with invalid username or password.
		invalidMessageTitle: nls.loginForm.invalidMessageTitle,
		
		// labelCancelButton: String
		//		Message to be displayed on the cancel button.
		labelCancelButton: nls.loginForm.cancel,
		
		_setLabelCancelButtonAttr: function(value) {
			// tags:
			//		protected
			this._set("labelCancelButton",value);
			this.cancelButton.set("label", this.labelCancelButton);
		},
		
		// showCancelButton: String
		//		Specifies whether this LoginFrame should include a Cancel button
		showCancelButton: false,

		_setShowCancelButtonAttr: function(b){
			// tags:
			//		protected
			this._set("showCancelButton",b);
			if(b){
				domStyle.set(this.cancelButton.domNode,{visibility:"visible",display:"inline"});
			}
			else{
				domStyle.set(this.cancelButton.domNode,{visibility:"hidden",display:"none"});
			}
		},
		
		startup: function() {
			var F = this.declaredClass + ".startup()";
			console.log(F);

			// Call Parent startup
			this.inherited(arguments);
			
			var uri = window.location.href,
				queryString = uri.substring(uri.indexOf("?") + 1, uri.length),
				wasCookie = cookie("WASReqURL"); 
			
			if (typeof(ioquery.queryToObject(queryString).error) != "undefined") {
				domStyle.set(this.loginErrorNode,"display","block");
			}
			
			// Set redirect URL to main page if not provided or to the logout URL
			if (wasCookie === undefined || 
					(wasCookie.search("/amc/login") != -1) ||
					(wasCookie.search("/amc/ibm_security_logout$") != -1)) {
				// Use base JavaScript as WAS can't cope with Dojo URL encoding the value
				document.cookie = "WASReqURL=/amc; Path=/";
			}
			
			this.loginUserName.focus();
		},
		
		_onSubmit: function(/*Event*/e){
			// tags:
			//		protected
			if(this.loginForm.validate()){
				return this.onSubmit(e);
			}else{
				return false;
			}
		},

		onSubmit:function(/*Event*/ e){
			// summary:
			//		Callback function when the end-user clicks the 
			//		submit/login button. Users of this class should 
			//		override this function to provide intended submission behavior
			return true;
		},
		
		_onCancel: function(/*Event*/ e){
			// tags:
			//		protected
			return this.onCancel(e);
		},

		onCancel: function(/*Event*/ e){
			// summary:
			//		Callback function when the end-user clicks the 
			//		cancel button. Users of this class should 
			//		override this function to provide intended cancel behavior
			return true;
		}
	});
	return LoginForm;
});
