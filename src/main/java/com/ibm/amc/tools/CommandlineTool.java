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
package com.ibm.amc.tools;

import java.util.HashMap;
import java.util.Map;

public abstract class CommandlineTool
{
	/** A constant indicating that a command-line argument was used, but
	 has no associated value */
	protected static final String NO_PARAMETER_VALUE = "NO_VALUE";

	/**
	 * @return A fixed string providing basic command-line help for the command.
	 * It will be word-wrapped to 80 characters automatically.
	 */
	protected abstract String getHelpMessage();
	
	/**
	 * Process commandline arguments into a map. We only allow --long-options 
	 * (with or without values) which is simple to parse, hence this method
	 * rather than commons.cli.
	 * @param args The standard args parameter to a main method.
	 * @return A map of argument names (still with their -- fronting) to their
	 * values. Arguments with no value are valid and map to null.
	 */
	protected Map<String, String> parseArgs(String[] args)
	{
		Map<String, String> params = new HashMap<String, String>();
		for (int i = 0; i < args.length; i++)
		{
			String token = args[i];
			
			// Each item in the loop should be an arg name; we skip over values
			// manually. So if we hit something without a -- the format is wrong
			// and we should bail.
			if(!token.startsWith("--"))
			{
				showHelp();
			}
	
			String value = NO_PARAMETER_VALUE;
			if(i < args.length-1 && !args[i+1].startsWith("--"))
			{
				i++;
				value = args[i];
			}
			params.put(token, value);
			
		}
		return params;
	}

	/** Show help message and exit. */
	@edu.umd.cs.findbugs.annotations.SuppressWarnings(value="DM_EXIT", 
			justification="We deliberately exit the JVM after showing help.")
	protected void showHelp()
	{
		// Shouldn't need wordwrapping as the message text has inherent formatting,
		// but translators are notorious for breaking that :-(
		System.out.println(wordwrap(this.getHelpMessage()));
		System.exit(1);
	}

	private static final int LINE_LENGTH = 80;
	protected String wordwrap(String message)
	{
		if(message.length() <= LINE_LENGTH) return message;
		
		int breakPlace;
		if(message.substring(0, LINE_LENGTH).contains("\n"))
		{
			 breakPlace = message.indexOf("\n");			
		} else
		{
			breakPlace = message.lastIndexOf(" ", LINE_LENGTH);
		}

		String line = message.substring(0, breakPlace);
		String rest = message.substring(breakPlace+1);
		return line+"\n"+wordwrap(rest);
	}

}
