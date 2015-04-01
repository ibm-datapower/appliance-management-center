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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.ws.rs.core.Response.Status;

import org.apache.commons.collections.Predicate;

import com.ibm.amc.Constants;
import com.ibm.amc.data.filter.QueryFilter.Operator;
import com.ibm.amc.data.filter.predicates.Contains;
import com.ibm.amc.data.filter.predicates.Empty;
import com.ibm.amc.data.filter.predicates.EndsWith;
import com.ibm.amc.data.filter.predicates.Equals;
import com.ibm.amc.data.filter.predicates.NotContains;
import com.ibm.amc.data.filter.predicates.NotEndsWith;
import com.ibm.amc.data.filter.predicates.NotEquals;
import com.ibm.amc.data.filter.predicates.NotStartsWith;
import com.ibm.amc.data.filter.predicates.StartsWith;
import com.ibm.amc.ras.Logger47;
import com.ibm.amc.resources.exceptions.AmcIllegalArgumentException;

/**
 * A factory to build query filters.
 * 
 * @author mallman
 */
public class QueryFilterFactory
{
	// @CLASS-COPYRIGHT@

	static Logger47 logger = Logger47.get(QueryFilterFactory.class.getCanonicalName(), Constants.CWZBA_BUNDLE_NAME);
	
	/**
	 * Build a query filter based on a set of provided query components. Each
	 * components has to specify its clause in the query value delimited by a 
	 * colon (:).
	 * 
	 * @param components a set of components that describe the query
	 * @return the constructed filter object
	 */
	public static QueryFilter build(Set<Map.Entry<String, List<String>>> components)
	{
		if (logger.isEntryEnabled()) logger.entry("build", components);
		
		/* one operator per query */
		Operator operator = null;
		
		/* multiple clauses per query */
		List<Predicate> clauses = new ArrayList<Predicate>();
		
		/* if components have been supplied, build a list of QueryFilter objects */
		if (components != null)
		{	
			for (Entry<String, List<String>> entry : components)
			{
				String key = entry.getKey();
				
				/* remove the filter operator from the filter request */
				if ("filterOperator".equalsIgnoreCase(key))
				{
					/* only supporting one operator */
					if (entry.getValue().size() > 1)
					{
						throw new AmcIllegalArgumentException(Status.BAD_REQUEST, "CWZBA0530E_TOO_MANY_FILTER_OPERATORS", Integer.toString(entry.getValue().size()));
					}
					String value = entry.getValue().get(0);
					
					/* when a value has been provided, try and use it */
					if (value != null && !"".equalsIgnoreCase(value))
					{
						operator = Operator.fromValue(value);
					}
				}
				/* process the clauses */
				else
				{
					/* each key can have multiple values */
					for (String value : entry.getValue())
					{
						Predicate predicate = null;
						
						if (value == null)
						{
							/* all we can do here is "equals" */
							predicate = new Equals(key, null);
						}
						else
						{
							/* separate the query clause from the data. Clause tokens are required */
							int delimiter = value.indexOf(":");
							if (delimiter == -1)
							{
								throw new AmcIllegalArgumentException(Status.BAD_REQUEST, "CWZBA0531E_NO_MATCHING_ACTION", key, value);
							}
							String action = value.substring(0, delimiter);
							String data = value.substring(delimiter + 1);
							
							/* create the correct predicate */
							if ("equal".equalsIgnoreCase(action))
							{
								predicate = new Equals(key, data);
							}
							else if ("contain".equalsIgnoreCase(action))
							{
								predicate = new Contains(key, data);
							}
							else if ("start".equalsIgnoreCase(action))
							{
								predicate = new StartsWith(key, data);
							}
							else if ("end".equalsIgnoreCase(action))
							{
								predicate = new EndsWith(key, data);
							}
							else if ("empty".equalsIgnoreCase(action))
							{
								predicate = new Empty(key, data);
							}	
							else if ("!equal".equalsIgnoreCase(action))
							{
								predicate = new NotEquals(key, data);
							}
							else if ("!contain".equalsIgnoreCase(action))
							{
								predicate = new NotContains(key, data);
							}
							else if ("!start".equalsIgnoreCase(action))
							{
								predicate = new NotStartsWith(key, data);
							}
							else if ("!end".equalsIgnoreCase(action))
							{
								predicate = new NotEndsWith(key, data);
							}
							else
							{
								/* there is no default. If we don't recognise it, send it back */
								throw new AmcIllegalArgumentException(Status.BAD_REQUEST, "CWZBA0532E_INVALID_MATCHING_ACTION", action, key, data);
							}
						}
						
						/* store the predicate */
						if (predicate != null)
						{
							clauses.add(predicate);
						}
					}
				}
			}
		}
		
		QueryFilter result = new QueryFilter(operator, clauses);
		
		if (logger.isEntryEnabled()) logger.exit("build", result);
		return result;
	}
}
