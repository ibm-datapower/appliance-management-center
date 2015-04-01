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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import java.io.IOException;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Level;

import org.junit.Ignore;
import org.junit.Test;

import com.ibm.amc.Constants;
import com.ibm.amc.TestConstants;
import com.ibm.amc.data.ApplianceTests;
import com.ibm.amc.server.impl.ApplianceManagerImplTests;
import com.ibm.amc.utils.FileTools;

/**
 * @see com.ibm.amc.ras.Logger47
 * @author mallman
 */
public class Logger47Tests
{	
	// @CLASS-COPYRIGHT@

	/* logger for output during test run. No resource bundle is needed */
	static Logger47 logger = Logger47.get(Logger47Tests.class.getCanonicalName(), Constants.CWZBA_BUNDLE_NAME);
	
	/**
	 * ensure we get different loggers for different source classes
	 */
	@Test
	public void notSame()
	{
		logger.entry("\n\nnotSame");
		
		// try with strings as identifiers
		Logger47 logger1 = Logger47.get("logger1", null);
		Logger47 logger2 = Logger47.get("logger2", null);
		assertNotSame("the Logger47 loggers shouldn't be the same object", logger1, logger2);
		assertNotSame("the java.util.Logger loggers shouln't be the same object", logger1.logger, logger2.logger);
		
		// try with actual class as identifiers
		Logger47 logger3 = Logger47.get(ApplianceTests.class.getCanonicalName(), null);
		Logger47 logger4 = Logger47.get(ApplianceManagerImplTests.class.getCanonicalName(), null);
		assertNotSame("the Logger47 loggers shouldn't be the same object", logger3, logger4);
		assertNotSame("the java.util.Logger loggers shouln't be the same object", logger3.logger, logger4.logger);
		
		logger.exit("notSame");
	}
	
	/**
	 * Write a pile of log messages and then check the output. Checking will be 
	 * by comparing an expected output file with the actual output file.
	 * 
	 * To do this the test uses a custom log formatter that needs to be 
	 * specified in logging.properties. Therefore, the run configuration 
	 * needs to include the following VM arugment:
	 * 
	 * 		-Djava.util.logging.config.file=resources/Logger47/logging.properties
	 * 
	 * @throws IOException if there are issues accessing the expected output file
	 * 
	 * see comment below regarding the SuppressWarnings annotation
	 */
	@SuppressWarnings("all")
	@Ignore("Logging is well-established and covered by FVT, and this test has " +
			"inconvenient setup needs.")
	@Test
	public void checkOutput() throws IOException
	{
		logger.entry("\n\ncheckOutput");
		
		// grab a different logger for this test content and modify it
		Logger47 loggerX = Logger47.get("loggerX", Constants.CWZBA_BUNDLE_NAME);
		FileHandler fileHandler = new FileHandler(TestConstants.LOGGER47_ACTUAL_OUTPUT);
		loggerX.logger.addHandler(fileHandler);
		loggerX.logger.setLevel(Level.ALL);
		
		// dump output from each of the logging methods
		loggerX.debug("debug1", "msg1");
		loggerX.debug("debug2", "msg2", "debugParm2.1");
		
		loggerX.entry("entry1");
		loggerX.entry("entry2", "parm2.1");
		loggerX.entry("entry3", "parm3.1", "parm3.2");
		// You *can* also use arrays of objects if you want to.
		loggerX.entry("entry4", new Object[]{"parm4.1", "parm4.2", "parm4.3", "parm4.4", "parm4.5", "parm4.6", "parm4.7", "parm4.8", "parm4.9", "parm4.10"});

		
		loggerX.exit("exit1");
		loggerX.exit("entry2", "result2");
		
		loggerX.info("info1");
		loggerX.info("info2", "insert2.1");
		loggerX.info("info3", "insert3.1", "insert3.2");
		
		loggerX.audit("audit1");
		loggerX.audit("audit2", "insert2.1");
		
		loggerX.warning("warning1");
		loggerX.warning("warning2", "insert2.1", "insert2.2", "insert2.2");
		
		loggerX.error("error1", new Exception("error1 exception"));
		loggerX.error("error2 {0}", new Exception("error2 exception"), "insert1");
		
		// variations around inserts
		loggerX.info("CWZBA0001I_WAMT_REPO_PATH");
		loggerX.info("CWZBA0001I_WAMT_REPO_PATH", "insert1");
		String valueThatTurnsOutToBeNull = null;
		loggerX.info("CWZBA0001I_WAMT_REPO_PATH", valueThatTurnsOutToBeNull);
		loggerX.info("CWZBA0001I_WAMT_REPO_PATH", "insert1", "insert2");
		loggerX.info("the following key will not be found");
		loggerX.info("the following key will not be found", "insert1");
		loggerX.info("the following key will not be found", "insert1", "insert2", "insert3");
		loggerX.info("the following key {0} will not be found");
		loggerX.info("the following key {0} will not be {1} found", "insert1");
		loggerX.info("the following key {0} will not be {1} found", "insert1", "insert2");
		loggerX.info("the following key {2} will not be found", "insert1", "insert2");
		loggerX.info("the following key \"{0}\" will not be found", "insert1");
		loggerX.info("the following key '{0}' will not be found", "insert1");
		loggerX.info("the following key ''{0}'' will not be found", "insert1");
		loggerX.info("the following key won''t be found", "insert1");
		loggerX.info("the following key won't be found", "insert1");
		
		// special cases
		loggerX.debug(null, null);
		loggerX.debug(null, null, null);
		loggerX.entry(null);
		loggerX.entry(null);
		loggerX.info("CWZBA0001I_WAMT_REPO_PATH");
		
		/* SuppressWarnings annotation added as the compiler doesn't like using 
		 * null for varargs, but this is explicitly testing what happens. Eclipse
		 * supports a subset of values, only "all" actually suppressed the warning */
		loggerX.error("error1", null);
		
		// read in the expected and actual logs
		List<String> expectedLines = FileTools.loadFileToList(TestConstants.LOGGER47_EXPECTED_OUTPUT);
		List<String> actualLines = FileTools.loadFileToList(TestConstants.LOGGER47_ACTUAL_OUTPUT);
		
		// check to see if the expected matches the actual
		assertEquals("file lengths do not match", expectedLines.size(), actualLines.size());
		
		int counter = 0;
		for (String expected : expectedLines)
		{
			counter++;
			assertEquals("error at line: " + counter, expected, actualLines.get(counter -1));
		}
		
		logger.exit("checkOutput");
	}
}


