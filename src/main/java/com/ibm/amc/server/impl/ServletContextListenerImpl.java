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
package com.ibm.amc.server.impl;

import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.ws.rs.core.Response.Status;

import com.ibm.amc.AmcException;
import com.ibm.amc.Constants;
import com.ibm.amc.PropertyException;
import com.ibm.amc.PropertyLoadException;
import com.ibm.amc.WamcProperties;
import com.ibm.amc.housekeeping.Housekeeper;
import com.ibm.amc.ras.Logger47;
import com.ibm.amc.server.action.ActionFactory;
import com.ibm.amc.server.action.ActionStatus;
import com.ibm.amc.server.action.ActionStatus.State;

/**
 * Context listener to handle life-cycle of WAMT manager, pre-initialise JPA and put out a message
 * at application startup.
 */
@WebListener
public final class ServletContextListenerImpl implements ServletContextListener
{
	// @CLASS-COPYRIGHT@

	private static final Logger47 logger = Logger47.get(ServletContextListenerImpl.class.getCanonicalName(), Constants.CWZBA_BUNDLE_NAME);

	private static String PRODUCT_RELEASE = "Product-Release";

	private static String BUILD_NUMBER = "Build-Number";

	private ExecutorService executor;

	@Override
	public void contextInitialized(final ServletContextEvent event)
	{
		if (logger.isEntryEnabled()) logger.entry("contextInitialized", event);

		if (logger.isInfoEnabled())
		{
			final ServletContext context = event.getServletContext();
			logger.info("CWZBA2251I_RELEASE_BUILD", getManifestAttribute(context, PRODUCT_RELEASE), getManifestAttribute(context, BUILD_NUMBER));
		}

		try
		{
			WamcProperties.initialize(event.getServletContext());
			WamcProperties.loadAndCheck();
		}
		catch (PropertyLoadException e)
		{
			logger.error(e.getLocalizedMessage(), true);
			return;
		}
		catch (PropertyException e)
		{
			e.logErrors(logger);
			return;
		}

		executor = Executors.newCachedThreadPool();

		// Initialise WAMT in the background
		executor.submit(new Runnable()
		{
			public void run()
			{
				long start = System.currentTimeMillis();
				ApplianceManagerFactory.getInstance().getAppliances();
				if (logger.isDebugEnabled()) logger.debug("run", "WAMT initialised in " + (System.currentTimeMillis() - start) / 1000 + " seconds");
			}
		});

		// Fail any actions that were in progress when the server
		// stopped running. This also ensures the history database is initialised.
		executor.submit(new Runnable()
		{
			public void run()
			{
				long start = System.currentTimeMillis();
				PersistenceContext.setContext();
				try
				{
					List<ActionStatus> actions = ActionFactory.getActionLog().getActionStatuses();
					for (ActionStatus action : actions)
					{
						State state = action.getState();
						if (State.STARTED == state || State.SUBMITTED == state)
						{
							action.failed(new AmcException(Status.INTERNAL_SERVER_ERROR, "CWZBA1039W_SERVER_STOPPED"));
						}
					}
				}
				finally
				{
					PersistenceContext.destroy();
				}
				if (logger.isDebugEnabled()) logger.debug("run", "History DB initialised in " + (System.currentTimeMillis() - start) / 1000 + " seconds");
			}
		});

		Housekeeper.init();

		if (logger.isEntryEnabled()) logger.exit("contextInitialized");
	}

	@Override
	public void contextDestroyed(final ServletContextEvent event)
	{
		if (logger.isEntryEnabled()) logger.entry("contextDestroyed", event);

		// Attempt to shutdown WAMT manager to cleanup daemon threads, close listener port and
		// write out repository
		((ApplianceManagerImpl) ApplianceManagerFactory.getInstance()).shutdownManager();
		if(executor != null)
			executor.shutdown();

		if (logger.isEntryEnabled()) logger.exit("contextDestroyed");
	}

	private static String getManifestAttribute(ServletContext context, String name)
	{
		String value = "?";
		try
		{
			final Properties properties = new Properties();
			properties.load(context.getResourceAsStream("META-INF/MANIFEST.MF"));
			if (properties.containsKey(name)) value = properties.getProperty(name);
		}
		catch (IOException e)
		{
			if (logger.isDebugEnabled()) logger.debug("getManifestAttribute", "Failed to open manifest: " + e.getLocalizedMessage());
		}
		return value;
	}

}
