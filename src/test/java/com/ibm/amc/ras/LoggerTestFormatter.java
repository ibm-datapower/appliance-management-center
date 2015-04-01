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
package com.ibm.amc.ras;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * java.util.logging formatter providing simplified output to allow for 
 * automated comparison (ie no thread ids or timestamps etc).
 * 
 * It is difficult to compare parameters as these could be objects so 
 * have different object ids, but they can be counted and compared if 
 * they are sent as Strings.
 * 
 * @author mallman
 */
public class LoggerTestFormatter extends Formatter
{
	// @CLASS-COPYRIGHT@

	public LoggerTestFormatter()
	{
		super();
	}

	public String format(LogRecord record)
	{
		StringBuffer buffer = new StringBuffer();
		
		buffer.append(record.getSourceMethodName());
		buffer.append(spacer());
		buffer.append(record.getMessage());
		buffer.append(spacer());
		buffer.append(parmContent(record.getParameters()));
		buffer.append(spacer());
		buffer.append(record.getThrown());
		buffer.append("\n");
		
		return buffer.toString();
	}
	
	
	// ------------------------------------------------------------------------
	// private helpers
	// ------------------------------------------------------------------------
	/**
	 * Common spacer for the formatting
	 * 
	 * @return a spacer
	 */
	private String spacer()
	{
		return "  ";
	}
	
	/**
	 * Serialise the items in an Object array.
	 * 
	 * @param parms the array to count
	 * @return the number of items contained, or null
	 */
	private String parmContent(Object[] parms)
	{
		StringBuffer result = new StringBuffer();
		
		if (parms == null)
		{
			result.append("NULL");
		}
		else
		{
			int counter = parms.length;
			result.append("[parm count: " + Integer.toString(counter));
			
			for (int index = 0; index < counter; index++)
			{
				result.append(spacer());
				result.append(parms[index]);
			}
			result.append("]");
		}
		return result.toString();
	}
}


