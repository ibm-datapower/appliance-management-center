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
package com.ibm.amc.utils;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceProviderResolver;
import javax.persistence.spi.PersistenceProviderResolverHolder;

import com.ibm.amc.server.impl.PersistenceContext;

/**
 * Utility methods for dealing with persistence.
 */
public final class PersistenceTools
{
	// @CLASS-COPYRIGHT@

	private static EntityManager manager;
	static
	{
		PersistenceProviderResolver resolver = mock(PersistenceProviderResolver.class);
		PersistenceProvider provider = mock(PersistenceProvider.class);
		when(resolver.getPersistenceProviders()).thenReturn(Collections.singletonList(provider));
		EntityManagerFactory factory = mock(EntityManagerFactory.class);
		when(provider.createEntityManagerFactory(anyString(), any(Map.class))).thenReturn(factory);
		PersistenceProviderResolverHolder.setPersistenceProviderResolver(resolver);
		manager = mock(EntityManager.class);
		when(factory.createEntityManager()).thenReturn(manager);
		EntityTransaction tran = mock(EntityTransaction.class);
		when(manager.getTransaction()).thenReturn(tran);
		Query query = mock(Query.class);
		when(manager.createQuery(anyString())).thenReturn(query);
		PersistenceContext.setContext();
	}

	private PersistenceTools()
	{
	}

	/**
	 * Associates a persistence context with the thread.
	 * 
	 * @return the entity manager for the persistence context
	 */
	public static EntityManager setPersistenceContext()
	{
		return manager;
	}

}
