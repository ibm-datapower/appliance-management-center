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

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import com.ibm.amc.AmcRequestFilter;
import com.ibm.amc.Constants;
import com.ibm.amc.ras.Logger47;
import com.ibm.amc.resources.exceptions.GenericExceptionMapper;
import com.ibm.amc.resources.exceptions.MalformedURLExceptionMapper;
import com.ibm.amc.resources.exceptions.UnrecognizedPropertyExceptionMapper;
import com.ibm.amc.resources.exceptions.WebApplicationExceptionMapper;

/**
 * Add all of the ReSt resources to this implementation class that itself is registered with the
 * servlet container.
 * 
 * @author mallman
 */
public class ResourcesApplication extends Application
{
	// @CLASS-COPYRIGHT@
	static Logger47 logger = Logger47.get(ResourcesApplication.class.getCanonicalName(), Constants.CWZBA_BUNDLE_NAME);

	/**
	 * JAX-RS uses this method to obtain details about all the Resource classes that are supported
	 * by this application.
	 * 
	 * A new instance of these classes will be created for each request in which they are used.
	 */
	public Set<Class<?>> getClasses()
	{
		if (logger.isEntryEnabled()) logger.entry("getClasses");

		Set<Class<?>> classes = new HashSet<Class<?>>();

		/* add each ReST resource to the list below */
		classes.add(ApplianceResource.class);
		classes.add(CurrentUserResource.class);
		classes.add(DomainResource.class);
		classes.add(ServiceResource.class);
		classes.add(GroupResource.class);
		classes.add(ActionResource.class);
		classes.add(FirmwareResource.class);
		classes.add(TemporaryFileResource.class);
		
		/* add each exception mapper to the list below */
		classes.add(UnrecognizedPropertyExceptionMapper.class);
		classes.add(WebApplicationExceptionMapper.class);
		classes.add(MalformedURLExceptionMapper.class);
		classes.add(GenericExceptionMapper.class);

		if (logger.isEntryEnabled()) logger.exit("getClasses", classes);
		return classes;
	}

	/**
	 * Configure Jackson to format dates.
	 */
	public Set<Object> getSingletons()
	{
		HashSet<Object> singletons = new HashSet<Object>();
		singletons.add(new AmcRequestFilter());
		return singletons;
	}
}
