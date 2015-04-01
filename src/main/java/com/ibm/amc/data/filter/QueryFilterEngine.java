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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;

import com.ibm.amc.Constants;
import com.ibm.amc.data.filter.QueryFilter.Operator;
import com.ibm.amc.ras.Logger47;

/**
 * Perform filtering action.
 * 
 * @author mallman
 */
public class QueryFilterEngine
{
	// @CLASS-COPYRIGHT@

	static Logger47 logger = Logger47.get(QueryFilterEngine.class.getCanonicalName(), Constants.CWZBA_BUNDLE_NAME);
		
	@SuppressWarnings("unchecked")
	public static <T> List<T> filter(List<T> objects, Set<Map.Entry<String, List<String>>> queryParams)
	{
		if (logger.isEntryEnabled()) logger.entry("filter", objects, queryParams);
		
		/* use a HashSet to remove duplicates */
		HashSet<T> filteredObjects = new HashSet<T>();
		
		QueryFilter filter = QueryFilterFactory.build(queryParams);
		
		if (filter.getOperator().equals(Operator.AND))
		{
			if (logger.isDebugEnabled()) logger.debug("filter", "Filter operator: AND");
			for (Predicate predicate : filter.getClauses())
			{
				CollectionUtils.filter(objects, predicate);
			}
			
			/* need to muck about to get compatible types */
			ArrayList<T> temp;
			if (objects instanceof ArrayList)
			{
				temp = (ArrayList<T>)objects;
			}
			else
			{
				temp = new ArrayList<T>(objects);
			}
			
			filteredObjects.addAll(temp);
		}
		else
		{
			if (logger.isDebugEnabled()) logger.debug("filter", "Filter operator: OR");
			for (Predicate predicate : filter.getClauses())
			{
				filteredObjects.addAll(CollectionUtils.select(objects, predicate));
			}
		}
		
		if (logger.isEntryEnabled()) logger.exit("filter", filteredObjects);
		return new ArrayList<T>(filteredObjects);
	}
}
