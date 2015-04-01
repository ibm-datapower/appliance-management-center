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
package com.ibm.amc.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.ibm.amc.utils.JsonTools;

/**
 * Test the JsonTools class.
 * 
 * @author mallman
 */
public class JsonMappingTests
{
	// @CLASS-COPYRIGHT@

	@Test
	public void testStringToObject() throws JsonParseException, JsonMappingException, IOException
	{
		String json = "{\"username\":\"Annette\",\"password\":\"Curtain\",\"number\":555,\"list\":[\"greedy\",\"little\",\"tike\"]}";

		Object obj = JsonTools.jsonToObject(json, TestObject.class);
		assertTrue("wrong object type", obj instanceof TestObject);

		TestObject to = (TestObject) obj;
		assertEquals("incorrect username", "Annette", to.username);
		assertEquals("incorrect password", "Curtain", to.password);
		assertEquals("incorrect number", 555, to.number);
		assertEquals("incorrect list length", 3, to.list.size());
		assertEquals("incorrect list item 0", "greedy", to.list.get(0));
		assertEquals("incorrect list item 1", "little", to.list.get(1));
		assertEquals("incorrect list item 2", "tike", to.list.get(2));
	}

	@Test
	public void testObjectToString() throws JsonGenerationException, JsonMappingException, IOException
	{
		List<String> list = new ArrayList<String>();
		list.add("hello");
		list.add("bob");

		TestObject dc = new TestObject("user", "pass", 1, list);
		String actual = JsonTools.objectToJson(dc);
		String expected = "{\"username\":\"user\",\"password\":\"pass\",\"number\":1,\"list\":[\"hello\",\"bob\"]}";
		assertEquals("incorrect string", expected, actual);
	}
}

/**
 * Class to use in the above tests.
 * 
 * @author mallman
 */
class TestObject
{
	String username;
	String password;
	int number;
	List<String> list;

	TestObject()
	{
	}

	TestObject(String username, String password, int number, List<String> list)
	{
		this.username = username;
		this.password = password;
		this.number = number;
		this.list = list;
	}

	public String getUsername()
	{
		return username;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public int getNumber()
	{
		return number;
	}

	public void setNumber(int number)
	{
		this.number = number;
	}

	public List<String> getlist()
	{
		return list;
	}

	public void setList(List<String> list)
	{
		this.list = list;
	}
}
