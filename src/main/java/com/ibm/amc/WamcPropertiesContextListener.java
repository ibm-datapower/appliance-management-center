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
 **/package com.ibm.amc;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import com.ibm.amc.Constants;
import com.ibm.amc.ras.Logger47;

@WebListener()
public class WamcPropertiesContextListener implements ServletContextListener
{
	// @CLASS-COPYRIGHT@

	static Logger47 logger = Logger47.get(WamcPropertiesContextListener.class.getCanonicalName(), Constants.CWZBA_BUNDLE_NAME);

	@Override
	public void contextInitialized(ServletContextEvent sce)
	{
		final String method = "contextInitialised";
		logger.entry(method, sce);

		// Pass feedback handler to servlet
		WamcProperties.initialize(sce.getServletContext());

		logger.exit(method);
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce)
	{
		final String method = "contextDestroyed";
		logger.entry(method, sce);		logger.exit(method);
	}

}
