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

import java.lang.reflect.Field;
import java.util.List;

import com.ibm.amc.AmcRuntimeException;
import com.ibm.amc.Constants;
import com.ibm.amc.ras.Logger47;

public abstract class AbstractPredicate
{
	// @CLASS-COPYRIGHT@

	static Logger47 logger = Logger47.get(AbstractPredicate.class.getCanonicalName(), Constants.CWZBA_BUNDLE_NAME);
	
	String name;
	String value;
	
	AbstractPredicate(String name, String value)
	{
		this.name = name;
		this.value = value;
	}
	
	/**
	 * For a given object instance get the string value of a named field. The 
	 * string provided for a given field is based on the toString method for 
	 * the field object type. In the case of a List, this method will remove 
	 * any wrapping square brackets from the string representation.
	 * 
	 * @param fieldName the name of the required field
	 * @param object the object to query
	 * @return the value of the field as a string, or null if the field doesn't exist
	 */
	String getStringValue(String fieldName, Object object)
	{
		if (logger.isEntryEnabled()) logger.entry("getStringValue", fieldName, object);
		
		String result = null;
		
		try
		{
			Field field = object.getClass().getField(fieldName);
			
			result = field.get(object).toString();
			
			/* if we have a list, remove the outer most square brackets */
			if (field.getType() == List.class)
			{
				if (result.startsWith("[") && result.endsWith("]"))
				{
					result = result.substring(1, result.length() - 1);
				}
			}
		}
		catch (IllegalArgumentException e)
		{
			/* the same object is used to get the field and the value, so this shouldn't(!?) occur */
			throw new AmcRuntimeException(e);
		}
		catch (IllegalAccessException e)
		{
			throw new AmcRuntimeException(e);
		}
		catch (NoSuchFieldException e)
		{
			/* if the field doesn't exist, the predicate doesn't match. Simply log... */
			logger.debug("evaluate", "Matching field '" + fieldName + "' is not a member of object '" + object.getClass().getName() + "'", e);
		}
		catch (SecurityException e)
		{
			throw new AmcRuntimeException(e);
		}
		
		if (logger.isEntryEnabled()) logger.exit("getStringValue", result);
		return result;
	}
	
	/**
	 * Facilitate readable logging. This method will return name and value
	 * of the predicate separated by a colon.
	 */
	 @Override
	public String toString()
	{
		 StringBuilder sb = new StringBuilder(name);
		 sb.append(" ");
		 sb.append(this.getClass().getSimpleName());
		 sb.append(" ");
		 sb.append(value);
		 return sb.toString();
	}
}
