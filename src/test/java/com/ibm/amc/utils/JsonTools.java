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
package com.ibm.amc.utils;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Helpers for JSON serialisation and deserialisation using Jackson.
 * 
 * @author mallman
 */
public class JsonTools
{
	// @CLASS-COPYRIGHT@

	static ObjectMapper mapper = new ObjectMapper();

	/**
	 * Convert a string into a java object.
	 * 
	 * @param json a JSON string
	 * @param valueType the class that the JSON represents
	 * @return an object described by the json
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public static Object jsonToObject(String json, Class<?> valueType) throws JsonParseException, JsonMappingException, IOException
	{
		return mapper.readValue(json, valueType);
	}

	/**
	 * Convert an object into a JSON string.
	 * 
	 * @param obj the object to convert to JSON
	 * @return a string representing the object provided
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public static String objectToJson(Object obj) throws JsonGenerationException, JsonMappingException, IOException
	{
		return mapper.writeValueAsString(obj);
	}
}
