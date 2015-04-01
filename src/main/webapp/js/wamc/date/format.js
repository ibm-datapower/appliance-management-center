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
define(["dojo/date/stamp",
        "dojo/date/locale"],
	function(stamp,locale){

	// dojo.date.stamp._isoRegExp: String
	//		Override _isoRegExp from dojo.date.stamp.
	//		This is to allow parsing of UTC date stamps as produced by Jackson and Java date format
	dojo.date.stamp._isoRegExp =
		/^(?:(\d{4})(?:-(\d{2})(?:-(\d{2}))?)?)?(?:T(\d{2}):(\d{2})(?::(\d{2})(.\d+)?)?((?:[+-](\d{2})(\d{2}))|Z)?)?$/;
	
	var dateFormat = {

		// timePattern: String
		//		defines string to be used for timestamp formatting
		timePattern:"HH:mm:ss:SSS",
		timePatternNoMillis:"HH:mm:ss",
			
		reformat: function(/*String*/dateString, /*object*/options){
			// summary:
			//		Reformat an ISO 8601 date string with locale specific formatting
			// description:
			//		This function consumes an ISO 8601 date string and returns a formatted
			//		string based on locale specific settings.
			//		This module modifies the normal ISO regexp used by dojo.date.stamp so
			//		that it can parse the dates produced by Java REST services
			
			var dateObj;
			if(typeof dateString == "string")
				dateObj = stamp.fromISOString(dateString);
			else
				dateObj = new Date(dateString);  // workaround if dateString comes as number from JSON
			
			return locale.format(dateObj,options);
		}
	
	};
	
	return dateFormat;
	
});
