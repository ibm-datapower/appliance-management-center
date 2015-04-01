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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.ibm.amc.data.filter.QueryFilter.Operator;
import com.ibm.amc.data.filter.predicates.EndsWith;
import com.ibm.amc.data.filter.predicates.Equals;
import com.ibm.amc.data.filter.predicates.NotContains;
import com.ibm.amc.resources.exceptions.AmcIllegalArgumentException;

public class QueryFilterFactoryTests
{
	// @CLASS-COPYRIGHT@
	
	private HashSet<Map.Entry<String, List<String>>> filterSet;
	
	@Before
	public void setup()
	{
		filterSet = new HashSet<Map.Entry<String, List<String>>>();
	}
	
	@Test
	public void nullArguments()
	{
		System.out.println("\nnullArguments()");
		
		QueryFilter filter = QueryFilterFactory.build(null);
		
		assertNotNull(filter);
		assertNotNull(filter.getOperator());
		assertNotNull(filter.getClauses());
		assertEquals("operator is incorrect", Operator.OR, filter.getOperator());
		assertEquals("unexpected clauses found", 0, filter.getClauses().size());
	}
	
	@Test
	public void validOperators()
	{
		System.out.println("\nvalidOperators()");
		
		filterSet.add(Util.buildClause("filterOperator", "or"));
		QueryFilter filter = QueryFilterFactory.build(filterSet);
		assertEquals("operator is incorrect", Operator.OR, filter.getOperator());
		System.out.println(filter);
		
		filterSet.clear();
		filterSet.add(Util.buildClause("filterOperator", "OR"));
		filter = QueryFilterFactory.build(filterSet);
		assertEquals("operator is incorrect", Operator.OR, filter.getOperator());
		System.out.println(filter);
		
		filterSet.clear();
		filterSet.add(Util.buildClause("filterOperator", "oR"));
		filter = QueryFilterFactory.build(filterSet);
		assertEquals("operator is incorrect", Operator.OR, filter.getOperator());
		System.out.println(filter);
		
		filterSet.clear();
		filterSet.add(Util.buildClause("filterOperator", "and"));
		filter = QueryFilterFactory.build(filterSet);
		assertEquals("operator is incorrect", Operator.AND, filter.getOperator());
		System.out.println(filter);
		
		filterSet.clear();
		filterSet.add(Util.buildClause("filterOperator", "AND"));
		filter = QueryFilterFactory.build(filterSet);
		assertEquals("operator is incorrect", Operator.AND, filter.getOperator());
		System.out.println(filter);
		
		filterSet.clear();
		filterSet.add(Util.buildClause("filterOperator", "aNd"));
		filter = QueryFilterFactory.build(filterSet);
		assertEquals("operator is incorrect", Operator.AND, filter.getOperator());
		System.out.println(filter);
	}
	
	@Test
	public void invalidOperators()
	{
		System.out.println("\ninvalidOperators()");
		
		try
		{
			filterSet.add(Util.buildClause("filterOperator", "spoon"));
			QueryFilterFactory.build(filterSet);
			fail("expected exception not thrown");
			
		}
		catch (AmcIllegalArgumentException e)
		{
			assertEquals("incorrect exception message key", "CWZBA0529E_INVALID_FILTER_OPERATOR", e.getMessageKey());
		}

		try
		{
			filterSet.clear();
			filterSet.add(Util.buildClause("filterOperator", "oscar", "the", "grouch"));
			QueryFilterFactory.build(filterSet);
			fail("expected exception not thrown");
		}
		catch (AmcIllegalArgumentException e)
		{
			assertEquals("incorrect exception message key", "CWZBA0530E_TOO_MANY_FILTER_OPERATORS", e.getMessageKey());
		}
		
		try
		{
			filterSet.clear();
			filterSet.add(Util.buildClause("filterOperator", "OR", "garbage"));
			QueryFilterFactory.build(filterSet);
			fail("expected exception not thrown");
		}
		catch (AmcIllegalArgumentException e)
		{
			assertEquals("incorrect exception message key", "CWZBA0530E_TOO_MANY_FILTER_OPERATORS", e.getMessageKey());
		}
	}
	
	@Test
	public void invalidMatchingClauses()
	{
		System.out.println("\ninvalidMatchingClauses()");
		
		try
		{
			filterSet.add(Util.buildClause("tim", ""));
			QueryFilterFactory.build(filterSet);
			fail("expected exception not thrown");
		}
		catch (AmcIllegalArgumentException e)
		{
			assertEquals("incorrect exception message key", "CWZBA0531E_NO_MATCHING_ACTION", e.getMessageKey());
		}
		
		try
		{
			filterSet.add(Util.buildClause("tim", "end"));
			QueryFilterFactory.build(filterSet);
			fail("expected exception not thrown");
		}
		catch (AmcIllegalArgumentException e)
		{
			assertEquals("incorrect exception message key", "CWZBA0531E_NO_MATCHING_ACTION", e.getMessageKey());
		}
	
		try
		{
			filterSet.clear();
			filterSet.add(Util.buildClause("name", "spoon"));
			QueryFilterFactory.build(filterSet);
			fail("expected exception not thrown");
		}
		catch (AmcIllegalArgumentException e)
		{
			assertEquals("incorrect exception message key", "CWZBA0531E_NO_MATCHING_ACTION", e.getMessageKey());
		}
		
		try
		{
			filterSet.clear();
			filterSet.add(Util.buildClause("name", "spoon:beans"));
			QueryFilterFactory.build(filterSet);
			fail("expected exception not thrown");	
		}
		catch (AmcIllegalArgumentException e)
		{
			assertEquals("incorrect exception message key", "CWZBA0532E_INVALID_MATCHING_ACTION", e.getMessageKey());
		}
	}
	
	@SuppressWarnings("all")
	@Test
	public void noOperatorOneClauseNullValue()
	{
		System.out.println("\nnoOperatorOneClauseNullValue()");
		
		/* SuppressWarnings annotation added as the compiler doesn't like using 
		 * null for varargs, but this is explicitly testing what happens. Eclipse
		 * supports a subset of values, only "all" actually suppressed the warning */
		filterSet.add(Util.buildClause("bob", null));
		QueryFilter filter = QueryFilterFactory.build(filterSet);

		assertEquals("operator is incorrect", Operator.OR, filter.getOperator());
		assertEquals("clause count is wrong", 1, filter.getClauses().size());
		
		System.out.println(filter);
	}
	
	@Test
	public void noOperatorOneClauseEmptyValue()
	{
		System.out.println("\nnoOperatorOneClauseEmptyValue()");
		
		filterSet.add(Util.buildClause("bob", "equal:"));
		QueryFilter filter = QueryFilterFactory.build(filterSet);
		
		System.out.println(filter);
		
		assertEquals("operator is incorrect", Operator.OR, filter.getOperator());
		assertEquals("clause count is wrong", 1, filter.getClauses().size());
		assertTrue("wrong predicate object", filter.getClauses().get(0) instanceof Equals);
	}
	
	@SuppressWarnings("all")
	@Test
	public void andOperatorMultiClauseMixed()
	{
		System.out.println("\nandOperatorMultiClauseMixed()");
		
		filterSet.add(Util.buildClause("bob", "end:"));
		/* SuppressWarnings annotation added as the compiler doesn't like using 
		 * null for varargs, but this is explicitly testing what happens. Eclipse
		 * supports a subset of values, only "all" actually suppressed the warning */
		filterSet.add(Util.buildClause("null1", null));
		filterSet.add(Util.buildClause("bob", "end:1"));
		filterSet.add(Util.buildClause("bob", "end:2"));
		filterSet.add(Util.buildClause("bob", "end:the value"));
		filterSet.add(Util.buildClause("bob", "end:1", "end:22", "end:333"));
		/* SuppressWarnings annotation added as the compiler doesn't like using 
		 * null for varargs, but this is explicitly testing what happens. Eclipse
		 * supports a subset of values, only "all" actually suppressed the warning */
		filterSet.add(Util.buildClause("null2", null));
		filterSet.add(Util.buildClause("filterOperator", "and"));
		filterSet.add(Util.buildClause("charlie", "end:charlie"));
		filterSet.add(Util.buildClause("nullList1","end:nullList1_1", null, "end:nullList1_2"));
		filterSet.add(Util.buildClause("nullList2", null, "end:nullList2"));
		filterSet.add(Util.buildClause("nullList3", "end:nullList3", null));
		filterSet.add(Util.buildClause("nullList4", null, "end:nullList4", null));
		filterSet.add(Util.buildClause("nullLIst5", null, null, null));
		QueryFilter filter = QueryFilterFactory.build(filterSet);
		
		System.out.println(filter);
		
		assertEquals("operator is incorrect", Operator.AND, filter.getOperator());
		assertEquals("clause count is wrong", 23, filter.getClauses().size());
	}
	
	@Test
	public void noOperatorOneClause()
	{
		System.out.println("\nnoOperatorOneClause()");
		
		filterSet.add(Util.buildClause("name", "end:name"));
		QueryFilter filter = QueryFilterFactory.build(filterSet);
		
		System.out.println(filter);
		
		assertEquals("clause count is wrong", 1, filter.getClauses().size());
		assertEquals("operator is incorrect", Operator.OR, filter.getOperator());
		assertTrue("wrong predicate object", filter.getClauses().get(0) instanceof EndsWith);
	}
	
	@Test
	public void noOperatorMultiClause()
	{
		System.out.println("\nnoOperatorMultiClause()");
		
		filterSet.add(Util.buildClause("name", "!contain:name"));
		filterSet.add(Util.buildClause("host", "!contain:host"));
		filterSet.add(Util.buildClause("port", "!contain:port"));
		QueryFilter filter = QueryFilterFactory.build(filterSet);
		
		System.out.println(filter);
		
		assertEquals("clause count is wrong", 3, filter.getClauses().size());
		assertEquals("operator is incorrect", Operator.OR, filter.getOperator());
		assertTrue("wrong predicate object", filter.getClauses().get(0) instanceof NotContains);
	}
}
