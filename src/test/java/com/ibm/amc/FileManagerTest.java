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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;

import org.junit.BeforeClass;
import org.junit.Test;

import com.ibm.amc.ras.Logger47;
import com.ibm.amc.security.SecurityContext;


public class FileManagerTest
{
	@BeforeClass
	public static void enableLogging()
	{
		ConsoleHandler handler = new ConsoleHandler();
		Logger47 logger = Logger47.get(FileManager.class.getCanonicalName(), Constants.CWZBA_BUNDLE_NAME);
		logger.logger.addHandler(handler);		
		logger.logger.setLevel(Level.ALL);
		handler.setLevel(Level.ALL);
	}
	
	@BeforeClass
	public static void setUpProperties() throws IOException
	{
		Properties props = new Properties();
		// Make the repository be whatever Java thinks is the tmp directory.
		File marker = File.createTempFile("foo", "tmp");
		props.setProperty("com.ibm.amc.wamtRepository", marker.getParent());
		marker.delete();
		WamcProperties.newInstance(props);
		
		// FileManager uses the current user name in file paths.
		SecurityContext.setContext("test");
	}
	
	@Test
	public void testReferenceCount() throws IOException, InterruptedException, URISyntaxException
	{
		File file = FileManager.createUploadFile();
		URI fileUrl = FileManager.getUriForTemporaryFile(file);
		file.createNewFile();
		
		FileManager.incrementReferenceCount(fileUrl);
		FileManager.incrementReferenceCount(fileUrl);
		assertTrue(file.exists());
		
		FileManager.decrementReferenceCount(fileUrl);
		assertTrue(file.exists());
		
		FileManager.decrementReferenceCount(fileUrl);
		assertFalse(file.exists());
	}
}
