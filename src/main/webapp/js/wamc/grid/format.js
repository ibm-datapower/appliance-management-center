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
define(["dojo/_base/lang",
        "dojo/string",
        "wamc/date/format",
        "dojo/i18n!wamc/nls/strings"],
	function(lang,string,dateFormat,nls){
	
	var source = "wamc.grid.format",
		gridFormat = lang.getObject(source,true);
	
	gridFormat.decorateApplianceGridStatus = function(/*Object*/cellData, /*number*/ rowId){
			// summary:
			//		Front doStatusDecoration providing some additional input
			//		parameters
			// cellData: Object
			//		The formatted (not raw) data for a gridx cell
			// rowId: number
			//		The id of the row being decorated
			// return: String
			//		The decorated action state value, in this case an img tag
			return gridFormat.doStatusDecoration(cellData, "appliance", "grid");
		};
		
	gridFormat.decorateApplianceDetailStatus = function(/*String*/data){
			// summary:
			//		Front doStatusDecoration providing some additional input
			//		parameters
			// data: String
			//		The data requiring formatting
			// return: String
			//		The decorated action state value, in this case an img tag
			return gridFormat.doStatusDecoration(data, "appliance", "detail");
		};
		
	gridFormat.decorateDomainGridStatus = function(/*Object*/cellData, /*number*/ rowId){
			// summary:
			//		Front doStatusDecoration providing some additional input
			//		parameters
			// cellData: Object
			//		The formatted (not raw) data for a gridx cell
			// rowId: number
			//		The id of the row being decorated
			// return: String
			//		The decorated action state value, in this case an img tag
			return gridFormat.doStatusDecoration(cellData, "domain", "grid");
		};
		
	gridFormat.decorateDomainDetailStatus = function(/*String*/data){
			// summary:
			//		Front doStatusDecoration providing some additional input
			//		parameters
			// data: String
			//		The data requiring formatting
			// return: String
			//		The decorated action state value, in this case an img tag
			return gridFormat.doStatusDecoration(data, "domain", "detail");
		};
		
	gridFormat.decorateServiceGridStatus = function(/*Object*/cellData, /*number*/ rowId){
			// summary:
			//		Front doStatusDecoration providing some additional input
			//		parameters
			// cellData: Object
			//		The formatted (not raw) data for a gridx cell
			// rowId: number
			//		The id of the row being decorated
			// return: String
			//		The decorated action state value, in this case an img tag
			return gridFormat.doStatusDecoration(cellData, "service", "grid");
		};
		
	gridFormat.decorateServiceDetailStatus = function(/*String*/data){
			// summary:
			//		Front doStatusDecoration providing some additional input
			//		parameters
			// data: String
			//		The data requiring formatting
			// return: String
			//		The decorated action state value, in this case an img tag
			return gridFormat.doStatusDecoration(data, "service", "detail");
		};
		
	gridFormat.decorateGroupsList = function(/*Object*/cellData){
			// summary:
			//		Decorate the groups membership data displayed in a grid,
			//		comma separating the list and adding spaces
			// data: Array
			//		The raw cellData
			// return: String
			//		The formatted groups list
			if (cellData instanceof Array && cellData.length > 0) {
				// sort the groups alphabetically
				cellData.sort();
				
				var buffer = cellData[0];
				for (var i = 1; i < cellData.length; i++) {
					buffer = buffer + ", " + cellData[i];
				}
				cellData = buffer;
			}
			return cellData;
		};

	gridFormat.decorateDateTime = function(/*Object*/cellData, /*number*/rowId){
			// summary:
			//		Decorator for datetime vlaues. For use with Gridx
			// description:
			//		Decorates the value of a datetime for display in a grid.
			//		Importing wamc/date/function causes dojo to use an overriden
			//		timestamp format that allows it to parse the timestamps produced
			//		by Jackson
			// cellData: Object
			//		The formatted (not raw) data for a gridx cell
			// rowId: number
			//		The id of the row being decorated
			// return: String
			//		The decorated date time value			
			
			return dateFormat.reformat(cellData,{formatLength:"long",timePattern: dateFormat.timePattern});
		};
		
	gridFormat.decorateDateTimeNoMillis = function(/*Object*/cellData, /*number*/rowId){
			// summary:
			//		Decorator for datetime vlaues. For use with Gridx
			// description:
			//		Decorates the value of a datetime for display in a grid.
			//		Importing wamc/date/function causes dojo to use an overriden
			//		timestamp format that allows it to parse the timestamps produced
			//		by Jackson
			// cellData: Object
			//		The formatted (not raw) data for a gridx cell
			// rowId: number
			//		The id of the row being decorated
			// return: String
			//		The decorated date time value			
			return dateFormat.reformat(cellData,{formatLength:"medium",timePattern: dateFormat.timePatternNoMillis});
		};
		
	gridFormat.decorateActionState = function(/*Object*/cellData, /*number*/ rowId){
			 // summary:
			 //		Decorator for Action Status State values. For use with Gridx
			 // description:
			 //		Decorates the value of status in Action Status grid rows.
			 //		The following values are valid:
			 //		SUBMITTED, STARTED, SUCCEEDED, FAILED
			 // cellData: Object
			 //		The formatted (not raw) data for a gridx cell
			 // rowId: number
			 //		The id of the row being decorated
			 // return: String
			 //		The decorated action state value, in this case an img tag
			 
			 var img = "js/wamc/themes/claro/images/",altText="";
				
				switch(cellData.toLowerCase()){
					case "succeeded": 
						img += "msgSuccess16.png";
						altText=nls.actionState.success;
						break;
					case "failed": 
						img += "msgError16.png";
						altText=nls.actionState.failure;
						break;
					default /*submitted, started*/: 
						img += "progress.gif";
						altText=nls.actionState.inProgress;
						break;
				}
				
				var status = "<div class=\"gridStatusImage\"><img src=\"" + img + "\" title=\"" + altText + "\" alt=\"" + altText + "\"/></div>";
				
				console.debug("Format status",status);
				
				return status;
		 };
		
	gridFormat.decorateFirmwareAvailability = function(/*Object*/firmware, /*number*/ rowId){
			// summary:
			//		Decorator for firmware availability in a gridx table.
			// description:
			//		Provides an indication of the availability of firmware for an appliance. Either the
			//		the version and a pop-up with details followed by a green tick or, if none is 
			//     available, a red cross.
			// firmware: Object
			//		The firmware to decorate
			// rowId: number
			//		The id of the row being decorated
			// return: String
			//		The decorated cell content
			var F = "gridFormat.decorateFirmwareAvailability(): ";
			
			console.debug(F,firmware,rowId);

			
			var result, imgUrl;
			if (firmware) {
				imgUrl = require.toUrl("wamc/themes/claro/images/msgSuccess16.png");
				
				result = "<img src=\"" + imgUrl + "\" style=\"float:right\" alt=\"" + nls.firmwareAvailability.available + "\" title=\"" + nls.firmwareAvailability.available + "\"/>" + firmware.level + "&nbsp;&nbsp;<span class=\"firmwareAvailabilityDetails\">" + nls.firmwareAvailability.details + "</a>";
			} else {
				imgUrl = require.toUrl("wamc/themes/claro/images/msgError16.png");
				result = "<img src=\"" + imgUrl + "\" style=\"float:right\" alt=\"" + nls.firmwareAvailability.notAvailable + "\" title=\"" + nls.firmwareAvailability.notAvailable + "\"/>" + nls.deployFirmware.noneAvailable;
			}
			
			console.debug("Format availability",result);
			
			return result;
		 };
		 
	gridFormat.doStatusDecoration = function(/*String*/value, /*String*/artefact, /*String*/displayStyle){
			// summary:
			//		Perform the formatting for a number of artefacts and UI locations.
			// value: String
			//		The input value to be formatted. Valid values are:
			//			UP, PARTIAL, DOWN, UNKNOWN, REACHABLE
			// artefact: String
			//		What is the artefact to be formatted. Valid values are:
			//			appliance, domain
			// displayStyle: String
			//		What formatting is required, aka where is the status
			//		being displayed. Valid values are:
			//			grid, detail
			// return: String
			//		Formatted representation of status
			var F = source + ".doStatusDecoration()";
			console.debug(F, "value:", value, "arteface:", artefact, "displayStyle", displayStyle);

			/* determine the image and alt text */
			var img = "js/wamc/themes/claro/images/", altText = "";
			
			switch(value.toLowerCase()){
				case "up": 
					img += "statusUp16.png";
					if (artefact == "appliance") altText=nls.applianceStatusTooltip.up;
					if (artefact == "domain") altText=nls.domainStatusTooltip.up;
					if (artefact == "service") altText=nls.serviceStatusTooltip.up;
					break;
				case "partial": 
					img += "statusPartial16.png";
					if (artefact == "appliance") altText=nls.applianceStatusTooltip.partial;
					if (artefact == "domain") altText=nls.domainStatusTooltip.partial; 
					break;
				case "down": 
					img += "statusDown16.png";
					if (artefact == "appliance") altText=nls.applianceStatusTooltip.down;
					if (artefact == "domain") altText=nls.domainStatusTooltip.down;
					if (artefact == "service") altText=nls.serviceStatusTooltip.down;
					break;
				case "reachable": 
					img += "statusUp16.png";
					if (artefact == "appliance") altText=nls.applianceStatusTooltip.reachable;
					break;
				default /*unknown*/: 
					img += "statusUnknown16.png";
					if (artefact == "appliance") altText=nls.applianceStatusTooltip.unknown;
					if (artefact == "domain") altText=nls.domainStatusTooltip.unknown;
					if (artefact == "service") altText=nls.serviceStatusTooltip.down;
					break;
			}

			/* determine the class to use */
			var requiredClass = "";
			
			switch(displayStyle){
				case "grid":
					requiredClass = "gridStatusImage";
					break;
				case "detail":
					requiredClass = "detailStatusImage";
					break;
			}

			/* build the final status string */
			var status =  "<div class=\"" + requiredClass + "\"><img src=\"" + img + "\" title=\"" + altText + "\" alt=\"" + altText + "\"/></div>";
			
			console.debug("Formatted status:", status);
			return status;
		};
		
	gridFormat.formatDeploymentPolicy = function(/*Object*/rawData){
			// summary:
			//		Formatter function to get the deployment policy uri from an appliance
			// description:
			//		This is a formatter (rather than a decorator) for a gridx cell. The difference is that
			//		a formatter modifies the data in the grid row (but not the store) whereas a decorator
			//		only updates the displayed data.
			//		This formatter is for the Appliance Grid on the Domain Deployment Policy Wizard
			// return: Object
			//		The formatted deploymentPolicy URI
			var F = source + ".formatDeploymentPolicy()";
			console.debug(F, rawData);
			
			if(!rawData){
				console.debug(F, "No Raw Data",rawData);
				return "";
			}
			else if(!rawData.deploymentPolicy){
				console.debug(F, "No Deployment Policy",rawData);
				return nls.deploymentPolicy.grid.noPolicy;
			}
			else if(!rawData.deploymentPolicy.uri){
				console.debug(F, "No Deployment Policy URI",rawData);
				return nls.deploymentPolicy.grid.noPolicy;
			}
			
			if(rawData.deploymentPolicy.uri.substring(0,8)==="wamctmp:"){
				return nls.deploymentPolicy.grid.updatedPolicyFile;
			}else if(rawData.deploymentPolicy.uri.substring(0,9)==="device://"){
				var ad = rawData.deploymentPolicy.uri.substring(9).split("/"),
					a = ad[0], d = ad[1];
				return string.substitute(nls.deploymentPolicy.grid.updatedPolicyDomain,{domain:d}); 
			}else{
				return rawData.deploymentPolicy.uri;
			}	
		};

	return gridFormat;
});
