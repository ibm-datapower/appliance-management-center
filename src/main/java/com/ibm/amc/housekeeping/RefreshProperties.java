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
package com.ibm.amc.housekeeping;

import java.io.File;
import java.util.concurrent.TimeUnit;

import com.ibm.amc.Constants;
import com.ibm.amc.PropertyException;
import com.ibm.amc.PropertyLoadException;
import com.ibm.amc.WamcProperties;
import com.ibm.amc.ras.Logger47;

/**
 * Service to observe the properties file and reload it if it changes.
 */
public class RefreshProperties implements Service 
{
	// @CLASS-COPYRIGHT@
	
	static Logger47 logger = Logger47.get(RefreshProperties.class.getCanonicalName(), Constants.CWZBA_BUNDLE_NAME);
	
	private File propertiesFile;
	private long lastModified;

	@Override
	public void execute() 
	{
		// Quick check, and return if unchanged.
		if(propertiesFile.lastModified() == lastModified){return;}
		
		// File has changed. Don't fire again until it changes again.
		lastModified = propertiesFile.lastModified();
		
		try
		{
			WamcProperties.loadAndCheck();
		}
		catch (PropertyLoadException e)
		{
			logger.error(e.getLocalizedMessage(), true);
		}
		catch (PropertyException e)
		{
			e.logErrors(logger);
		}
	}

	@Override
	public Schedule getSchedule() 
	{
		return new Schedule(10, TimeUnit.SECONDS);
	}

	@Override
	public void init() 
	{
		try
		{
			propertiesFile = WamcProperties.findPropertiesFile();
			lastModified = propertiesFile.lastModified();
		} 
		catch(PropertyLoadException e)
		{
			// No need to really do anything, as the main startup load and check
			// will report it.
			logger.debug("init", "Exception looking for wamc.properties while " +
					"initialising the refresh service.", e);
		}
	}

}
