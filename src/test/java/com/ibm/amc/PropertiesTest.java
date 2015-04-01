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
package com.ibm.amc;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Properties;
import java.util.ResourceBundle;

import org.junit.AfterClass;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.ibm.amc.WamcProperties.Props;
import com.ibm.amc.ras.Logger47;

public class PropertiesTest
{
	// @CLASS-COPYRIGHT@

	protected ResourceBundle resourceBundle = ResourceBundle.getBundle(Constants.CWZBA_BUNDLE_NAME);

	@Test
	public void simple()
	{
		write("com.ibm.amc.wamtRepository=wamt repository");
		WamcProperties.newInstance(null);
		
		assertEquals("wamt repository", WamcProperties.instance().getWamtRepository());
	}
	
	@Test
	public void useDefault()
	{
		write("com.ibm.amc.wamtRepository=wamt repository");
		WamcProperties.newInstance(null);
		
		assertEquals(5555, WamcProperties.instance().getWamtNotificationPort());
	}
	
	@Test
	public void missing()
	{
		write(); // Write an empty file.
		WamcProperties.newInstance(null);

		try
		{
			WamcProperties.instance().check();
		}
		catch (PropertyException e)
		{
			assertEquals(PropertyException.Error.MISSING, e.getErrors().get("com.ibm.amc.wamtRepository"));
			return;
		}
		fail("Called check() on a file with missing mandatory property, but no exception was thrown.");
	}
	
	@Test
	public void notInt()
	{
		write("com.ibm.amc.wamtRepository=wamt repository",
				"com.ibm.amc.wamtNotificationPort=not a number"); 
		WamcProperties.newInstance(null);

		try
		{
			WamcProperties.instance().check();
		}
		catch (PropertyException e)
		{
			assertEquals(PropertyException.Error.NOT_INTEGER, e.getErrors().get("com.ibm.amc.wamtNotificationPort"));
			return;
		}
		fail("Called check() with a non-numeric value for an int property, but no exception was thrown.");
	}

	@Test
	public void injectedValuesForUnitTesting()
	{
		Properties customValues = new Properties();
		customValues.setProperty("com.ibm.amc.wamtRepository", "Custom Repo Location");
		WamcProperties.newInstance(customValues);
		
		assertEquals("Custom Repo Location", WamcProperties.instance().getWamtRepository());
	}
	
	@SuppressWarnings("rawtypes") // cannot type Answer because it would have to be Answer<void> and that's not legal syntax.
	@Test
	public void errorLogMessage()
	{
		write("com.ibm.amc.wamtNotificationPort=not a number"); 
		// and com.ibm.amc.wamtRepository missing. 
		
		WamcProperties.newInstance(null);

		try
		{
			WamcProperties.instance().check();
		}
		catch (PropertyException e)
		{
			// Set up a mock logger that captures what is sent to it.
			final StringBuilder capturedLog = new StringBuilder();
			Logger47 logger = mock(Logger47.class);
			doAnswer(new Answer(){
				public Object answer(InvocationOnMock invocation) throws Throwable
				{
					Object[] args = invocation.getArguments();
					String msgKey = (String) args[0];
					String[] inserts = new String[args.length-1];
					if(inserts.length > 0)
					{
						for (int i = 0; i < inserts.length; i++)
						{
							inserts[i] = (String)args[i+1];
						}
					}
					
					capturedLog.append(new MessageFormat(resourceBundle.getString(msgKey)).format(inserts)+"\n");
					return null;
				}})
				.when(logger).error(anyString(), (String[]) anyVararg());

			// Get the errors into the log from the exception.
			e.logErrors(logger);
		
			// Compare.
			assertEquals("The configuration properties for the application are incorrect. Check the following messages for details.\n" + 
					"'com.ibm.amc.wamtRepository' is not set. It is a required property.\n" + 
					"'com.ibm.amc.wamtNotificationPort' is invalid. It must be an integer.\n",
					capturedLog.toString());
			return;
		}
		fail("Called check() with a non-numeric value for an int property, but no exception was thrown.");
	}
	
	@Test
	public void runtimeUpdate() throws PropertyLoadException, PropertyException
	{
		write("com.ibm.amc.wamtRepository=foo");
		WamcProperties.loadAndCheck();

		assertEquals("foo", WamcProperties.instance().getWamtRepository());
		
		Props oldProps = WamcProperties.instance();
		
		deleteTemporaryPropsFile();
		write("com.ibm.amc.wamtRepository=bar");
		
		// Doesn't change instantly...
		assertEquals("foo", WamcProperties.instance().getWamtRepository());

		// But should do when we tell it to re-read
		WamcProperties.loadAndCheck();
		assertEquals("bar", WamcProperties.instance().getWamtRepository());

		// Old instances keep their old values, for atomicity. Don't hang on to
		// Props instances for longer than you need!
		assertEquals("foo", oldProps.getWamtRepository());
	}
	
	@Test
	public void badRuntimeUpdate() throws PropertyLoadException, PropertyException
	{
		write("com.ibm.amc.wamtRepository=foo", "com.ibm.amc.wamtHttpPort=12");
		WamcProperties.loadAndCheck();

		assertEquals("foo", WamcProperties.instance().getWamtRepository());
		
		deleteTemporaryPropsFile();
		write("com.ibm.amc.wamtRepository=bar", "com.ibm.amc.wamtHttpPort=not a number");

		// Will fail.
		try {
			WamcProperties.loadAndCheck();
		} catch(PropertyException e){/* fine */ }
			
		// Now check that we can still load valid (old) values.
		assertEquals("foo", WamcProperties.instance().getWamtRepository());
		assertEquals(12, WamcProperties.instance().getWamtHttpPort());

	}
	
	/**
	 * Write a properties file with certain properties.
	 * @param properties one or more strings, each of the format "name=value".
	 */
	private void write(String... properties)
	{
		Properties newProps = new Properties();
		for (String propertyString : properties)
		{
			String[] halves = propertyString.split("=", 2);
			String name = halves[0];
			String value = halves[1];
			newProps.setProperty(name, value);
		}
		
		try
		{
			File propertiesFile = getPropsFileName();
			
			newProps.store(new FileOutputStream(propertiesFile), 
					"Temporary properties file used for unit test "+this.getClass().getSimpleName());	
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail("Exception while writing temporary properties file. See stderr.");
		}
	}


//	@AfterClass
	public static void deleteTemporaryPropsFile()
	{
		getPropsFileName().delete(); // delete is so old it doesn't throw exceptions when it fails...
	}

	private static File getPropsFileName()
	{
		try
		{
			URL baseDir = PropertiesTest.class.getProtectionDomain().getCodeSource().getLocation();
			File propertiesFile;
			propertiesFile = new File(new URL(baseDir, "wamc.properties").toURI());
			return propertiesFile;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail("Exception while writing temporary properties file. See stderr.");
			return null; 
		}
	}
}
