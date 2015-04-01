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
package com.ibm.amc.resources;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.ibm.amc.ras.Logger47;
import com.ibm.amc.server.Controller;

/**
 * Create mock ReST Resource classes for use when testing.
 * 
 * @author mallman
 */
public class ResourceUtils
{
	// @CLASS-COPYRIGHT@

	/* resource bundle not required */
	static Logger47 logger = Logger47.get(ResourceUtils.class.getCanonicalName(), null);

	// ------------------------------------------------------------------------
	// getters for the mock resources that are available
	// ------------------------------------------------------------------------
	public static ApplianceResource getConfiguredAppliancesResource(Controller controller)
	{
		ApplianceResource resource = new ApplianceResource(controller);
		configureAbstractResource(resource);
		return resource;
	}

	/**
	 * configure the appropriate flavour of mock, including any methods overrides
	 * 
	 * @param resource
	 */
	private static void configureAbstractResource(AbstractResource resource)
	{
		/* prevent complaints that implementations are not used (invocation is via reflection) */
		@SuppressWarnings("unused")
		InvocationHandler invocationHandler = new InvocationHandler()
		{
			@Override
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
			{
				Object result = null;
				String methodName = method.getName();

				try
				{
					// Find out what method has been requested to be called, and see if we have
					// implemented it on this proxy. If we have then delegate the invocation 
					// directly through.
					Method targetMethod = this.getClass().getMethod(methodName, method.getParameterTypes());

					if (targetMethod != null)
					{
						result = targetMethod.invoke(this, args);
					}
				}
				catch (NoSuchMethodException nme)
				{
					// It may be useful to throw this up to the test framework while testing or
					// writing the code, however in some cases returning null here may be 
					// reasonable (if the returned object is not used for anything in that code 
					// path). Leaving this throw here in order to see how things go.
					throw nme;
				}
				return result;
			}

			// ------------------------------------------------------------------------
			// method overrides
			// ------------------------------------------------------------------------
			public UriBuilder getRequestUriBuilder()
			{
				if (logger.isEntryEnabled()) logger.entry("getRequestUriBuilder");

				String path = "https://my.dummy.host:9443/amc/rest/context";

				UriBuilder builder = UriBuilder.fromPath(path);
				
				if (logger.isEntryEnabled()) logger.exit("getRequestUriBuilder", builder);
				return builder;
			}
		};

		// ------------------------------------------------------------------------
		// create any required objects
		// ------------------------------------------------------------------------
		UriInfo uriInfoObj = (UriInfo) Proxy.newProxyInstance(UriInfo.class.getClassLoader(), new Class[] { UriInfo.class }, invocationHandler);

		// ------------------------------------------------------------------------
		// configure the required objects/properties onto the resource
		// ------------------------------------------------------------------------
		resource.uriInfo = uriInfoObj;
	}
}
