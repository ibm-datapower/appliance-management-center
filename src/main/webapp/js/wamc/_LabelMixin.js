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
        "dojo/query"],
        function(declare,query){

	_LabelMixin = declare(null,{
		_setLabelAttr: function(/*String*/ label){
			this.compLabelNode.innerHTML = label;
			query(".wamcLabel", this.domNode).toggleClass("dijitHidden", /^\s*$/.test(label));
			this._set("label", label);
		},

		_setLabelWidthAttr: function(/*String | Integer*/width){
			if(!width){ return; }
			query(".wamcLabel", this.domNode).style("width", width);
		},
		
		_setLabelAlignmentAttr: function(/*String*/ alignment){
			var h = alignment == "horizontal";
			query(".wamcLabel", this.domNode).toggleClass("dijitInline", h);
			this._set("labelAlignment", alignment);
		}
	});

	return _LabelMixin;
});
