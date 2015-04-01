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
package com.ibm.amc.data.filter;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.ibm.amc.resources.data.Appliance;

public class QueryFilterEngineTests
{
	// @CLASS-COPYRIGHT@
	
	private HashSet<Map.Entry<String, List<String>>> filterSet;
	private List<Appliance> appliances;
	
	@Before
	public void setup()
	{
		filterSet = new HashSet<Map.Entry<String, List<String>>>();
		appliances = new ArrayList<Appliance>();
	}
	
	/**
	 * filtering uses the toString() method of the field object type 
	 * and if this is a list it is wrapped in square brackets. The filtering
	 * code removes these brackets if the field type is a list and the 
	 * string representation starts and ends with a opening and closing 
	 * square bracket.
	 * 
	 * @see com.ibm.amc.data.filter.predicates.AbstractPredicate#getStringValue(String, Object)
	 */
	@Test
	public void filterOnList()
	{
		/* construct the necessary inputs */
		Appliance appliance = new Appliance();
		appliance.groups = new ArrayList<String>();
		appliance.groups.add("test");
		appliances.add(appliance);
	
		/* filter the data */
		filterSet.add(Util.buildClause("groups", "equal:test"));
		List<Appliance> result = QueryFilterEngine.filter(appliances, filterSet);
		
		/* check it */
		assertEquals("incorrect number of applianced", 1, result.size());
		
		/* update the data */
		appliance.groups.add("dev");
		
		/* filter again */
		filterSet.add(Util.buildClause("groups", "equal:test"));
		result = QueryFilterEngine.filter(appliances, filterSet);
		
		/* re-check */
		assertEquals("incorrect number of applianced", 0, result.size());
		
		/* udpate the filter and try again */
		filterSet.clear();
		filterSet.add(Util.buildClause("groups", "contain:test"));
		result = QueryFilterEngine.filter(appliances, filterSet);
		
		/* check again */
		assertEquals("incorrect number of applianced", 1, result.size());
	}
}
