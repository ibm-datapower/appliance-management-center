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
define(["gridx/modules/CellWidget",
		"gridx/modules/ColumnResizer",
		"gridx/modules/VScroller",
		"gridx/modules/VirtualVScroller",
		"gridx/modules/IndirectSelect",
		"gridx/modules/Focus",
		"gridx/modules/RowHeader",
		"gridx/modules/extendedSelect/Row",
		"gridx/modules/extendedSelect/Column",
		"gridx/modules/extendedSelect/Cell", 
		"gridx/modules/Filter",
		"gridx/modules/filter/FilterBar",
		"wamc/grid/modules/filter/FilterSetup"], 
		function(CellWidget, 
				ColumnResizer, VScroller, VirtualVScroller, IndirectSelect,
				Focus, RowHeader, ExtendedSelectRow, ExtendedSelectColumn, 
				ExtendedSelectCell, Filter, FilterBar, FilterSetup) {
	return {
		CellWidget: CellWidget,
		ColumnResizer : ColumnResizer,
		VScroller : VScroller,
		VirtualVScroller: VirtualVScroller,
		ExtendedSelectRow : ExtendedSelectRow,
		ExtendedSelectColumn : ExtendedSelectColumn,
		ExtendedSelectCell : ExtendedSelectCell,
		Filter : Filter,
		FilterBar : FilterBar,
		IndirectSelect: IndirectSelect,
		Focus:Focus,
		RowHeader:RowHeader,
		FilterSetup:FilterSetup
	};
});
