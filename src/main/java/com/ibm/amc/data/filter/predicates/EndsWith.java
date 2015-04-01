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
package com.ibm.amc.data.filter.predicates;

import org.apache.commons.collections.Predicate;

import com.ibm.amc.Constants;
import com.ibm.amc.ras.Logger47;

/**
 * A predicate to determine if a supplied object contains 
 * a named field with a provided value.
 * 
 * This predicate performs a "ends with" match.
 * 
 * Comparisons take place as strings and are not case sensitive.
 * 
 * @author mallman
 */
public class EndsWith extends AbstractPredicate implements Predicate
{
	// @CLASS-COPYRIGHT@

	static Logger47 logger = Logger47.get(EndsWith.class.getCanonicalName(), Constants.CWZBA_BUNDLE_NAME);
	
	/**
	 * Constructor
	 * 
	 * @param name the name of the field to match
	 * @param value the value of the field to match
	 */
	public EndsWith(String name, String value)
	{
		super(name, value);
	}

	/**
	 * Evaluate this predicates matching criteria with the 
	 * supplied object. Matching is not case sensitive.
	 */
	public boolean evaluate(Object object)
	{
		if (logger.isEntryEnabled()) logger.entry("evaluate", object);
		
		boolean result = false;
		
		String objectFieldValue = getStringValue(name, object);
		if (logger.isDebugEnabled()) logger.debug("evaluate", "Required: '" + name + "' ends with '" + value + "'. Found: '" + name + "' = '" + objectFieldValue + "'");
		
		if (objectFieldValue != null && objectFieldValue.toLowerCase().endsWith(value.toLowerCase()))
		{
			result = true;
		}
				
		if (logger.isEntryEnabled()) logger.exit("evaluate", result);
		return result;
	}
}
