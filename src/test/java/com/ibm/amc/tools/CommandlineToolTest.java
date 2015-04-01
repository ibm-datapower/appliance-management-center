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

import static org.junit.Assert.*;

import java.security.Permission;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;


public class CommandlineToolTest
{
	private DummyTool tool;

	@Before
	public void createDummyTool()
	{
		tool = new DummyTool();
	}

	@Test
	public void testBasicArgsParsing()
	{
		String args = "--fromRepo /test/june/repo --toRepo /test/sept/repo";
		Map<String, String> parsed = tool.parseArgs(args.split(" "));
		
		assertEquals("/test/june/repo", parsed.get("--fromRepo"));
		assertEquals("/test/sept/repo", parsed.get("--toRepo"));
	}
	
	@Test
	public void testFurtherArgsParsing()
	{
		String args = "--fromRepo /test/june/repo --noValueMiddle --toRepo /test/sept/repo --noValueEnd";
		Map<String, String> parsed = tool.parseArgs(args.split(" "));
		
		assertEquals("/test/june/repo", parsed.get("--fromRepo"));
		assertEquals("/test/sept/repo", parsed.get("--toRepo"));
		
		assertEquals(CommandlineTool.NO_PARAMETER_VALUE, parsed.get("--noValueMiddle"));
		assertEquals(CommandlineTool.NO_PARAMETER_VALUE, parsed.get("--noValueEnd"));
	}
	
	@Test
	public void testInvalidArgsParsing()
	{
		disableSystemExit();
		
		String args = "foo";
		try
		{
			tool.parseArgs(args.split(" "));
		} 
		catch (BadCmdlineArgs e)
		{
			return;
		}
		finally
		{
			enableSystemExit();
		}
		fail("Did not exit when given bad args.");
		
	}
	
	private void disableSystemExit()
	{
		existingSecurityManager = System.getSecurityManager();
		
		System.setSecurityManager(new SecurityManager(){
			@Override
			public void checkPermission(Permission perm)
			{
				if(perm.getName().startsWith("exitVM"))
				{
					throw new BadCmdlineArgs();
				}
			}
		});
	}
	public class BadCmdlineArgs extends SecurityException
		{private static final long serialVersionUID = 1L;}
	private SecurityManager existingSecurityManager;
	private void enableSystemExit()
	{
		System.setSecurityManager(existingSecurityManager);
	}
	
	public static class DummyTool extends CommandlineTool
	{
		@Override
		protected String getHelpMessage()
		{
			return "How to run the Dummy Tool: DON'T!";
		}
	}
}
