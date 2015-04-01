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

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Util
{
	// @CLASS-COPYRIGHT@
	
	public static Map.Entry<String, List<String>> buildClause(String key, String... values)
	{
		List<String> clauseValues = new ArrayList<String>();
		
		if (values != null) 
		{
			for (String value : values)
			{
				clauseValues.add(value);
			}
		}
		else
		{
			clauseValues.add(null);
		}

		return new AbstractMap.SimpleEntry<String, List<String>>(key, clauseValues);
	}
}
