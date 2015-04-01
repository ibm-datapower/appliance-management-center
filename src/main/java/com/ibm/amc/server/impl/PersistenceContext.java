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

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import com.ibm.amc.Constants;
import com.ibm.amc.WamcProperties;
import com.ibm.amc.ras.Logger47;

/**
 * Provides a persistence context for REST and background action threads.
 */
public final class PersistenceContext
{
	// @CLASS-COPYRIGHT@
	static Logger47 logger = Logger47.get(PersistenceContext.class.getCanonicalName(), Constants.CWZBA_BUNDLE_NAME);

	/**
	 * Thread local containing contexts.
	 */
	private static final ThreadLocal<PersistenceContext> CONTEXT = new ThreadLocal<PersistenceContext>();

	private final EntityManager actionHistoryEntityManager;

	private static final EntityManagerFactory actionHistoryFactory;
	static
	{
		final Map<String, String> historyProperties = new HashMap<String, String>();
		final String historyDbPath = WamcProperties.instance().getHistoryDirectoryPath();
		if (logger.isDebugEnabled()) logger.debug("<init>", "Action history Derby database directory: " + historyDbPath);
		historyProperties.put("openjpa.ConnectionURL", "jdbc:derby:" + historyDbPath + ";create=true");
		actionHistoryFactory = Persistence.createEntityManagerFactory("history-jpa-persistence-unit", historyProperties);
	}

	/**
	 * Constructor.
	 * 
	 * @param configEntityManagerFactory
	 */
	private PersistenceContext(final EntityManager actionHistoryEntityManager)
	{
		this.actionHistoryEntityManager = actionHistoryEntityManager;
	}

	/**
	 * Sets the context for the current thread .
	 * 
	 * @param configEntityManager
	 *            the config entity manager
	 */
	public static void setContext()
	{
		final EntityManager actionHistoryEntityManager = actionHistoryFactory.createEntityManager();
		CONTEXT.set(new PersistenceContext(actionHistoryEntityManager));
	}

	/**
	 * Gets the context for the current thread.
	 * 
	 * @return the persistence context for the current thread
	 */
	public static PersistenceContext getContext()
	{
		return CONTEXT.get();
	}

	/**
	 * Destroys the context associated with the current thread.
	 */
	public static void destroy()
	{
		final PersistenceContext context = CONTEXT.get();
		if (context != null)
		{
			context.getActionHistoryEntityManager().close();
			CONTEXT.remove();
		}
	}

	/**
	 * Returns the action history entity manager.
	 * 
	 * @return the action history entity manager
	 */
	public EntityManager getActionHistoryEntityManager()
	{
		return actionHistoryEntityManager;
	}
}
