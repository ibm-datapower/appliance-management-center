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
package com.ibm.amc.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.ibm.amc.Constants;
import com.ibm.amc.data.validation.InvalidDataException;
import com.ibm.amc.data.validation.ValidationEngine;
import com.ibm.amc.ras.Logger47;
import com.ibm.amc.resources.data.Appliance;

/**
 * @see com.ibm.amc.resources.data.Appliance
 * @author mallman
 */
public class ApplianceTests
{
	// @CLASS-COPYRIGHT@

	/* logger for output during test run. No resource bundle is needed */
	static Logger47 logger = Logger47.get(ApplianceTests.class.getCanonicalName(), null);

	/**
	 * A bit low level. If this doesn't work we are in trouble, but as an exercise in getting some
	 * unit tests written...
	 */
	@Test
	public void construction()
	{
		logger.entry("\n\nconstruction");

		/* create an appliance */
		Appliance appliance = new Appliance();
		appliance.name = "name";
		appliance.applianceType = "model";
		appliance.firmwareLevel = "version";
		appliance.hostName = "ip";

		/* check the toString matches what we expect */
		logger.debug("construction", "Output is: " + appliance);

		/* check attributes individually */
		assertEquals("incorrect appliance name", "name", appliance.name);
		assertEquals("incorrect appliance type", "model", appliance.applianceType);
		assertEquals("incorrect appliance version", "version", appliance.firmwareLevel);
		assertEquals("incorrect appliance ip", "ip", appliance.hostName);

		logger.exit("\n\nconstruction");
	}

	@Test
	public void validationNameTests()
	{
		logger.entry("\n\nvalidationNameTests");

		Appliance appliance = new Appliance();

		assertNull("name field should be null", appliance.name);

		/* null name */
		try
		{
			ValidationEngine.getInstance().validate(appliance);
			fail("should have had an exception for null name");
		}
		catch (InvalidDataException ide)
		{
			logger.debug("validationNameTests", "Caught exception for null name");
		}

		/* blank name */
		appliance.name = "";
		try
		{
			ValidationEngine.getInstance().validate(appliance);
			fail("should have had an exception for blank name");
		}
		catch (InvalidDataException ide)
		{
			logger.debug("validationNameTests", "Caught exception for blank name");
		}

		/* name set */
		appliance.name = "name";
		try
		{
			ValidationEngine.getInstance().validate(appliance);
			logger.debug("validationNameTests", "validated successfully with the name set");
		}
		catch (InvalidDataException ide)
		{
			fail("error, not expecting exception");
		}

		/* return to blank name */
		appliance.name = "";
		try
		{
			ValidationEngine.getInstance().validate(appliance);
			fail("should have had an exception for blank name");
		}
		catch (InvalidDataException ide)
		{
			logger.debug("validationNameTests", "Caught exception for blank name");
		}

		/* return to null name */
		appliance.name = null;
		try
		{
			ValidationEngine.getInstance().validate(appliance);
			fail("should have had an exception for null name");
		}
		catch (InvalidDataException ide)
		{
			logger.debug("validationNameTests", "Caught exception for null name");
		}

		logger.exit("\n\nvalidationNameTests");
	}

	@Test
	public void validatePortTests()
	{
		logger.entry("\n\nvalidatePortTests");

		Appliance appliance = new Appliance();
		appliance.name = "name";

		/* assume amp port is null if not set */
		assertNull("amp port should be null", appliance.ampPort);

		/* negative amp port */
		appliance.ampPort = -23;
		try
		{
			ValidationEngine.getInstance().validate(appliance);
			fail("should have had an exception for negative amp port");
		}
		catch (InvalidDataException ide)
		{
			logger.debug("validatePortTests", "Caught exception for negative amp port");
		}

		/* port of zero */
		appliance.ampPort = 0;
		try
		{
			ValidationEngine.getInstance().validate(appliance);
			logger.debug("validatePortTests", "validated successfully with a port of zero");
		}
		catch (InvalidDataException ide)
		{
			fail("error, not expecting exception");
		}

		/* amp port below min */
		appliance.ampPort = Constants.PORT_NUMBER_MIN - 1;
		try
		{
			ValidationEngine.getInstance().validate(appliance);
			fail("should have had an exception for amp port below min");
		}
		catch (InvalidDataException ide)
		{
			logger.debug("validatePortTests", "Caught exception for amp port below min");
		}

		/* port of min */
		appliance.ampPort = Constants.PORT_NUMBER_MIN;
		try
		{
			ValidationEngine.getInstance().validate(appliance);
			logger.debug("validatePortTests", "validated successfully with a port of min");
		}
		catch (InvalidDataException ide)
		{
			fail("error, not expecting exception");
		}

		/* port of 1234 */
		appliance.ampPort = 1234;
		try
		{
			ValidationEngine.getInstance().validate(appliance);
			logger.debug("validatePortTests", "validated successfully with a port of 1234");
		}
		catch (InvalidDataException ide)
		{
			fail("error, not expecting exception");
		}

		/* port of max */
		appliance.ampPort = Constants.PORT_NUMBER_MAX;
		try
		{
			ValidationEngine.getInstance().validate(appliance);
			logger.debug("validatePortTests", "validated successfully with a port of max");
		}
		catch (InvalidDataException ide)
		{
			fail("error, not expecting exception");
		}

		/* amp port above max */
		appliance.ampPort = Constants.PORT_NUMBER_MAX + 1;
		try
		{
			ValidationEngine.getInstance().validate(appliance);
			fail("should have had an exception for amp port beyond max");
		}
		catch (InvalidDataException ide)
		{
			logger.debug("validatePortTests", "Caught exception for amp port beyond max");
		}

		logger.exit("\n\nvalidatePortTests");
	}

	@Test
	public void validateQuiesceTimeoutTests()
	{
		logger.entry("\n\nvalidateQuiesceTimeoutTests");

		Appliance appliance = new Appliance();
		appliance.name = "name";

		/* assume timeout is null if not set */
		assertNull("timeout should be null", appliance.quiesceTimeout);

		/* negative timeout */
		appliance.quiesceTimeout = -23;
		try
		{
			ValidationEngine.getInstance().validate(appliance);
			fail("should have had an exception for negative timeout");
		}
		catch (InvalidDataException ide)
		{
			logger.debug("validateQuiesceTimeoutTests", "Caught exception for negative timeout");
		}

		/* timeout below min */
		appliance.quiesceTimeout = Constants.QUIESCE_TIMEOUT_MIN - 30;
		try
		{
			ValidationEngine.getInstance().validate(appliance);
			fail("should have had an exception for timeout below min");
		}
		catch (InvalidDataException ide)
		{
			logger.debug("validateQuiesceTimeoutTests", "Caught exception for timeout below min");
		}

		/* timeout below min */
		appliance.quiesceTimeout = Constants.QUIESCE_TIMEOUT_MIN - 1;
		try
		{
			ValidationEngine.getInstance().validate(appliance);
			fail("should have had an exception for timeout below min");
		}
		catch (InvalidDataException ide)
		{
			logger.debug("validateQuiesceTimeoutTests", "Caught exception for timeout below min");
		}

		/* timeout of zero */
		appliance.quiesceTimeout = 0;
		try
		{
			ValidationEngine.getInstance().validate(appliance);
			logger.debug("validateQuiesceTimeoutTests", "validated successfully with a timeout of 0");
		}
		catch (InvalidDataException ide)
		{
			fail("error, not expecting exception");
		}

		
		/* timeout of min */
		appliance.quiesceTimeout = Constants.QUIESCE_TIMEOUT_MIN;
		try
		{
			ValidationEngine.getInstance().validate(appliance);
			logger.debug("validateQuiesceTimeoutTests", "validated successfully with a timeout of min");
		}
		catch (InvalidDataException ide)
		{
			fail("error, not expecting exception");
		}

		/* timeout of 1234 */
		appliance.quiesceTimeout = 1234;
		try
		{
			ValidationEngine.getInstance().validate(appliance);
			logger.debug("validateQuiesceTimeoutTests", "validated successfully with a timeout of 1234");
		}
		catch (InvalidDataException ide)
		{
			fail("error, not expecting exception");
		}

		/* timeout of max */
		appliance.quiesceTimeout = Constants.QUIESCE_TIMEOUT_MAX;
		try
		{
			ValidationEngine.getInstance().validate(appliance);
			logger.debug("validateQuiesceTimeoutTests", "validated successfully with a timeout of max");
		}
		catch (InvalidDataException ide)
		{
			fail("error, not expecting exception");
		}

		/* timeout above max */
		appliance.quiesceTimeout = Constants.QUIESCE_TIMEOUT_MAX + 1;
		try
		{
			ValidationEngine.getInstance().validate(appliance);
			fail("should have had an exception for amp port beyond max");
		}
		catch (InvalidDataException ide)
		{
			logger.debug("validateQuiesceTimeoutTests", "Caught exception for timeout beyond max");
		}

		logger.entry("\n\nvalidateQuiesceTimeoutTests");
	}

}
