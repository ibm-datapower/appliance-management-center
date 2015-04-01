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
package com.ibm.amc.resources.data;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;

import com.ibm.amc.ras.Logger47;

/**
 * Common function for the WAMC ReST interface data types.
 * 
 * @author mallman
 */
public abstract class AbstractRestData
{
	// No CLASS-COPYRIGHT because we don't want copyright text in ReST responses.

	/**
	 * Indicates one or more fields which should be used for computing hashcodes. 
	 * Typically this annotation will be applied to some unique name or id field.
	 * If no fields are annotated, all instances of the class will have the 
	 * same hashcode, which is valid but will not perform well in hashes.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Hashcode{}
	
	/* resource bundle is not required as only debug messages are output */
	static Logger47 logger = Logger47.get(AbstractRestData.class.getCanonicalName(), null);
	
	@Override
	public int hashCode()
	{
		Field[] fields = this.getClass().getDeclaredFields();
		
		int hashcode = 42;
		
		for (Field field : fields)
		{
			// xor together all fields marked as forming the hashcode, plus the 
			// initial seed (an arbitrary 42). 
			if(field.isAnnotationPresent(Hashcode.class))
			{
				try
				{
					if (field.get(this) != null)
					{
						hashcode ^= field.get(this).hashCode();
					}
				}
				catch (Exception e)
				{
					if(logger.isDebugEnabled()) logger.debug("toString", "Exception while computing hashcode, accessing field: " + field.getName() , e);
					// continue with other fields, if any, else behave as if none
					// were marked.
				}
			}
		}
		
		return hashcode;
	}
	
	@edu.umd.cs.findbugs.annotations.SuppressWarnings(justification="Listed as dodgy, but well unit tested", value="Eq")
	@Override
	public boolean equals(Object obj)
	{
		if (obj == null) return false;

		if (this == obj) return true;

		if (!this.getClass().isInstance(obj)) return false;

		Field[] fields = this.getClass().getDeclaredFields();
		for (Field field : fields)
		{
			try
			{
				if (notSame(field.get(obj), field.get(this)))
				{
					return false;
				}
			}
			catch (Exception e)
			{
				if(logger.isDebugEnabled()) logger.debug("toString", "Exception while computing equality, accessing field: " + field.getName() , e);
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Provide a useful toString for this object.
	 */
	@Override
	public String toString()
	{
		/* start the output string with the class name */
		StringBuilder output = new StringBuilder("[");
		output.append(this.getClass().getName());
		
		/* grab the name and value for each member */
		Field[] fields = this.getClass().getDeclaredFields();
		for (Field field : fields)
		{
			String fieldName = field.getName();
			/* don't output passwords */
			if (fieldName.contains("assword")) continue;
			
			output.append(" | ");
			output.append(fieldName);
			output.append(": ");
			try
			{
				output.append(field.get(this));
			}
			catch (IllegalAccessException e)
			{
				/* nothing can be done, just log the exception */
				if(logger.isDebugEnabled()) logger.debug("toString", "IllegalAccessException thrown while computing toString, accessing field: " + fieldName , e);
			}
		}

		/* close off the output */
		output.append("]");

		return output.toString();
	}

	private boolean notSame(Object one, Object two)
	{
		if (one == null)
		{
			return two != null;
		}
		return !one.equals(two);
	}
}
