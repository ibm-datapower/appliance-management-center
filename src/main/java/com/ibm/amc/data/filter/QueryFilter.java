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

import javax.ws.rs.core.Response.Status;

import org.apache.commons.collections.Predicate;

import com.ibm.amc.resources.exceptions.AmcIllegalArgumentException;

public class QueryFilter
{
	/* the default operator is OR */
	private Operator operator = Operator.OR;
	
	/* the list of clauses for this filter */
	private List<Predicate> clauses;
	
	/**
	 * Supported operators. The same operator applies to all clauses 
	 * in the QueryFilter
	 */
	public enum Operator
	{
		OR("or"),
		AND("and");
		
		private final String value;
		
		Operator(String value)
		{
	        this.value = value;
	    }

	    public String value()
	    {
	        return value;
	    }
	    
	    public static Operator fromValue(String value)
	    {
	        for (Operator current : Operator.values())
	        {
	        	if (current.value.equalsIgnoreCase(value))
	            {
	                return current;
	            }
	        }
	        throw new AmcIllegalArgumentException(Status.BAD_REQUEST, "CWZBA0529E_INVALID_FILTER_OPERATOR", value);
	    }
	}
	
	/**
	 * Constructor
	 * 
	 * @param operator
	 * @param clauses
	 */
	QueryFilter(Operator operator, List<Predicate> clauses)
	{
		if (operator != null)
		{
			this.operator = operator;
		}
		if (clauses != null)
		{
			this.clauses = clauses;
		}
		else
		{
			this.clauses = new ArrayList<Predicate>();
		}
		
	}

	/**
	 * Get the operator for this filter
	 * 
	 * @return the operator
	 */
	public Operator getOperator()
	{
		return operator;
	}

	/**
	 * Get the list of clauses for this filter 
	 * 
	 * @return a list of clauses. If none have been specified an empty list will be returned
	 */
	public List<Predicate> getClauses()
	{
		if (clauses == null)
		{
			clauses = new ArrayList<Predicate>();
		}
		return clauses;
	}
	
	/**
	 * Provide a better toString() for logging
	 */
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder("Operator=");
		sb.append(operator.toString());
		sb.append(" Clauses=");
		for (Predicate predicate : clauses)
		{
			sb.append("[");
			if (predicate != null)
			{
				sb.append(predicate.toString());
			}
			else
			{
				sb.append("null");
			}
			sb.append("]");
		}
		
		return sb.toString();
	}
}
