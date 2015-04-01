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
define(["dojo/_base/lang","dojox/validate/regexp"],
		
	function(lang,dxregexp){

	// functions for checking a String against a predefined regular or 
	// expression simply returning a predefined regular expression
	var wamc = lang.getObject("wamc",true);
	
	wamc.validation = {
		// define the various core regular expressions
		//		Note: when a "\" is required within the regular expression, it 
		//		must itself be escaped for JavaScript processing (with another "\")
		inlineUserAndPasswordRE: "([A-Za-z0-9_-]+):([A-Za-z0-9\\]\\[!\"#$%&'()*+,./:;<=>?@\\^_`{|}~-]+)@",
		pathRE: "(/(?:[^?#\\s/]+/)*(?:[^?#\\s/]+(?:\\?[^?#\\s/]*)?(?:#[A-Za-z][\\w.:-]*)?)?)?",
		anythingRE: ".*",
		// allows dash, underscore and period but must start with alpha numeric
		alphaNumericWithDashUnderscorePeriodStartAlphaNumbericRE: "([A-Za-z0-9][A-Za-z0-9-_\.]*)",
		// allows dash, underscore and period
		alphaNumericWithDashUnderscorePeriodRE: "([A-Za-z0-9-_\.]+)",
		// numbers, letters and underscore
		alphaNumericWithUnderscoreRE: "[\w]+",
		// value in seconds. Zero, or minimum of 60 up to some high number
		timeoutRE: "^([0]*)(0|[6-9][0-9]|[1-9][0-9]{2,8})$",
		folderRE: "^[a-zA-Z0-9_\\-.]+:/[a-zA-Z0-9_\\-./]*$",
		fileNameRE: "[a-zA-Z0-9_\\-.]*[a-zA-Z0-9_\\-]",
		applianceLocalFileProtocolRE: "local:\/\/",
		applianceTemporaryFileProtocolRE: "temporary:\/\/",
		applianceFileNameRE: "([A-Za-z0-9-:/_\.]+)",
		userRoleRE:"[A-Za-z0-9]+[A-Za-z0-9-_\. ]*",


		username: function(){
			// summary: Builds a regular expression for usernames
			// return:
			//		String - the requested regular expression
			return this.anythingRE;
		},
		
		password: function(){
			// summary: Builds a regular expression for passwords
			// return:
			//		String - the requested regular expression
			return this.anythingRE;
		},
		
		applianceName: function(){
			// summary: Builds a regular expression for an appliance symbolic name
			// return:
			//		String - the requested regular expression
			return this.alphaNumericWithDashUnderscorePeriodStartAlphaNumbericRE;
		},
		
		groupName: function(){
			// summary: Builds a regular expression for resource group names
			// return:
			//		String - the requested regular expression
			return this.alphaNumericWithDashUnderscorePeriodStartAlphaNumbericRE;
		},
		
		quiesceTimeout: function(){
			// summary: Builds a regular expression for quiesce timeout values
			// return:
			//		String - the requested regular expression
			return this.timeoutRE;
		},
		
		domainName: function(){
			// summary: Builds a regular expression for domain names
			// return:
			//		String - the requested regular expression
			return this.alphaNumericWithDashUnderscorePeriodRE;
		},
		
		certificateName: function(){
			// summary: Builds a regular expression for certificate names
			// return:
			//		String - the requested regular expression
			return this.alphaNumericWithDashUnderscorePeriodRE;
		},
		
		folder: function(){
			// summary: Builds a regular expression for folder names
			// return:
			//		String - the requested regular expression
			return this.folderRE;
		},
		
		fileName: function(){
			// summary: Builds a regular expression for filenames
			// return:
			//		String - the requested regular expression
			return this.fileNameRE;
		},
		
		userRoleName: function(){
			// summary: Builds a regular expression for user role
			// return:
			//		String - the requested regular expression
			return this.userRoleRE;
		},
		
		applianceLocalFile: function(){
			// summary: Builds a regular expression for local files
			//			on an appliance
			// return:
			//		String - the requested regular expression
			var F = "wamc.validation.applianceLocalFile()",
				expression = this.applianceLocalFileProtocolRE + this.applianceFileNameRE;
			
			console.debug(F,"Expression:",expression);
			
			return expression;
		},
		
		applianceTemporaryFile: function(){
			// summary: Builds a regular expression for temporary files
			//			on an appliance
			// return:
			//		String - the requested regular expression
			var F = "wamc.validation.applianceTemporaryFile()",
				expression =  this.applianceTemporaryFileProtocolRE + this.applianceFileNameRE;
			
			console.debug(F,"Expression:",expression);
			
			return expression;
		},
				
		url: function(/*Object*/flags){
			// summary: Builds a regular expression for a URL
			// flags: An object used to configure the result
			//		flags for this.protocol can be applied
			//		flags for this.inlineUserAndPassword can be applied
			//		flags for this.host can be applied
			// return:
			//		String - the requested regular expression		
			var F = "wamc.validation.url()";
			console.debug(F, "Flags:", flags);
			
			var expression = this.protocol(flags) + this.host(flags) + this.urlPath();
			
			console.debug(F,"Expression:",expression);
			return expression;
		},
		
		urlWithBasicAuth: function(/*Object*/flags){
			// summary: Builds a regular expression for a URL with basic auth parts
			// flags: An object used to configure the result
			//		flags for this.protocol can be applied
			//		flags for this.inlineUserAndPassword can be applied
			//		flags for this.host can be applied
			// return:
			//		String - the requested regular expression		
			var F = "wamc.validation.urlWithBasicAuth()";
			console.debug(F, "Flags:", flags);
			
			var expression = this.protocol(flags) + this.inlineUserAndPassword(flags) + this.host(flags) + this.urlPath();
			
			console.debug(F,"Expression:",expression);
			return expression;
		},
		
		protocol: function(/*Object*/flags){
			// summary: Builds a regular expression for the protocol section
			//		of a URL
			// flags: An object used to configure the result
			//		flags.http - is HTTP allowed? Default is true
			//		flags.https - is HTTPs allowed? Default is false
			//		flags.ftp - is ftp allowed? Default is false
			//		flags.sftp - is sftp allowed? Default is false
			//		flags.file - is file allowed? Default is false
			// return: String
			//		the requested regular expression
			var F = "wamc.validation.protocol()";
			
			// assign default values to missing parameters
			flags = (typeof flags == "object") ? flags : {};
			if(typeof flags.http != "boolean"){ flags.http = true; }
			if(typeof flags.https != "boolean"){ flags.https = false; }
			if(typeof flags.ftp != "boolean"){ flags.ftp = false; }
			if(typeof flags.sftp != "boolean"){ flags.sftp = false; }
			if(typeof flags.file != "boolean"){ flags.file = false; }
			console.debug(F, "Flags:", flags);
			
			// define the expression parts
			var httpRE = "http";
			var httpsRE = "https";
			var ftpRE = "ftp";
			var sftpRE = "sftp";
			var fileRE = "file";
			
			// gather the expression parts
			var parts = [];
			if(flags.http){ parts.push(httpRE); }
			if(flags.https){ parts.push(httpsRE); }
			if(flags.ftp){ parts.push(ftpRE); }
			if(flags.sftp){ parts.push(sftpRE); }
			if(flags.file){ parts.push(fileRE); }

			// construct the expression
			var expression = "";

			if(parts.length > 0){
				expression = "(" + parts.join("|") + ")";
				expression += "://";
			}

			console.debug(F,"Expression:",expression);
			return expression;
		},
			
		inlineUserAndPassword: function(/*Object*/flags){
			// summary: Builds a regular expression for including a user 
			//		name and password in a URL 
			// description: The following pattern is used:
			//		user:pass@
			// flags: An object used to configure the result
			//		flags.userOptional - is the user name and password optional? Default is true
			// return: String
			//		the requested regular expression
			var F = "wamc.validation.inlineUserAndPassword()";
			
			// assign default values to missing parameters
			flags = (typeof flags == "object") ? flags : {};
			if(typeof flags.userOptional != "boolean"){ flags.userOptional = true; }
			console.debug(F, "Flags:", flags);
			
			// construct the expression
			var expression = "";
			
			if (flags.userOptional) { expression += "("; }
			expression += this.inlineUserAndPasswordRE;
			if (flags.userOptional) { expression += ")?"; }
			
			console.debug(F,"Expression:",expression);
			return expression;
		},
		
		host: function(/*Object*/flags){
			// summary:
			//		Builds a regular expression for a hostname
			// description:
			//		A host is a named host (A-z0-9_- but not starting with -), 
			//		a domain name or an IP address, possibly followed by a
			//		port number.
			//		Modified from dojox/validate/regexp host() method, with a 
			//		fix to named host regex.
			// flags: An object used to configure the result
			//		flags for regexp.host can be applied
			// return: String
			//		the requested regular expression
			var F = "wamc.validation.host()";
			console.debug(F, "Flags:", flags);
			
			// assign default values to missing paramters
			flags = (typeof flags == "object") ? flags : {};

			if(typeof flags.allowIP != "boolean"){ flags.allowIP = true; }
			if(typeof flags.allowLocal != "boolean"){ flags.allowLocal = false; }
			if(typeof flags.allowPort != "boolean"){ flags.allowPort = true; }
			if(typeof flags.allowNamed != "boolean"){ flags.allowNamed = false; }

			//TODO: support unicode hostnames?
			// Domain name labels can not end with a dash.
			var domainLabelRE = "(?:[\\da-zA-Z](?:[-\\da-zA-Z]{0,61}[\\da-zA-Z])?)";
			var domainNameRE = "(?:[a-zA-Z](?:[-\\da-zA-Z]{0,6}[\\da-zA-Z])?)"; // restricted version to allow backwards compatibility with allowLocal, allowIP

			// port number RE
			var portRE = flags.allowPort ? "(\\:\\d+)?" : "";

			// build host RE
			var hostNameRE = "((?:" + domainLabelRE + "\\.)+" + domainNameRE + "\\.?)";
			if(flags.allowIP){ hostNameRE += "|" +  dxregexp.ipAddress(flags); }
			if(flags.allowLocal){ hostNameRE += "|localhost"; }
			if(flags.allowNamed){ hostNameRE += "|[^-][a-zA-Z0-9_-]*"; }
			var expression = "(" + hostNameRE + ")" + portRE; // String
			
			console.debug(F,"Expression:",expression);
			return expression;
		},
		
		urlPath: function(){
			// summary: Builds a regular expression for path section of 
			//		a URL 
			// return: String
			//		the requested regular expression
			var F = "wamc.validation.urlPath()";
			
			var expression = this.pathRE;
			
			console.debug(F,"Expression:",expression);
			return expression;
		}
	};
	return wamc.validation;
});
