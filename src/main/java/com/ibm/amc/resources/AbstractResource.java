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

import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.ibm.amc.Constants;
import com.ibm.amc.ras.Logger47;
import com.ibm.amc.server.Controller;
import com.ibm.amc.server.impl.ControllerFactory;

/**
 * Parent class for all Resources.
 * 
 * @author mallman
 */
public abstract class AbstractResource
{
	// @CLASS-COPYRIGHT@

	static Logger47 logger = Logger47.get(AbstractResource.class.getCanonicalName(), Constants.CWZBA_BUNDLE_NAME);
	
	static String CONTEXT_ROOT = "rest";

	/* gain access to the request URI info */
	@Context
	UriInfo uriInfo;

	/* grab a controller to use */
	protected final Controller controller;

	public AbstractResource()
	{
		controller = ControllerFactory.getInstance();
	}

	public AbstractResource(Controller controller)
	{
		this.controller = controller;
	}

	/**
	 * Construct the Location URI for a new resource.
	 * 
	 * @param resource
	 *            the identifier of the new resource
	 * @return a build URI for the new resource location
	 * @throws URISyntaxException
	 */
	URI getResourceUri(String resource)
	{
		if (logger.isEntryEnabled()) logger.entry("getResourceUri", resource);

		URI result = null;

		if (uriInfo != null)
		{
			UriBuilder builder = uriInfo.getRequestUriBuilder();
			result = builder.path(resource).build();
		}
		else
		{
			/* bad news, log the situation */
			if (logger.isDebugEnabled()) logger.debug("getResourceUri", "The provided uriInfo is null");
		}

		if (logger.isEntryEnabled()) logger.exit("getResourceUri", result);
		return result;
	}
}
