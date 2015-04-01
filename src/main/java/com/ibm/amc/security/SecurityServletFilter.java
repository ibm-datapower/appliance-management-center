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
package com.ibm.amc.security;

import java.io.IOException;
import java.util.HashSet;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * Servlet filter to setup and destroy security context.
 */
public class SecurityServletFilter implements Filter
{
	// @CLASS-COPYRIGHT@

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
	{
		if (request instanceof HttpServletRequest)
		{
			HttpServletRequest httpRequest = (HttpServletRequest) request;
			HashSet<String> roles = new HashSet<String>();
			if(httpRequest.isUserInRole(SecurityManager.MANAGEMENT_CENTER_ADMINISTRATORS))
				roles.add(SecurityManager.MANAGEMENT_CENTER_ADMINISTRATORS);
			if(httpRequest.isUserInRole(SecurityManager.SOLUTION_DEPLOYERS))
				roles.add(SecurityManager.SOLUTION_DEPLOYERS);
			if(httpRequest.isUserInRole(SecurityManager.SYSTEM_ADMINISTRATORS))
				roles.add(SecurityManager.SYSTEM_ADMINISTRATORS);
			if(httpRequest.isUserInRole(SecurityManager.SYSTEM_OPERATORS))
				roles.add(SecurityManager.SYSTEM_OPERATORS);
			if(httpRequest.getUserPrincipal() != null)
				SecurityContext.setContext(roles, httpRequest.getUserPrincipal().getName());
		}
		chain.doFilter(request, response);
		if (request instanceof HttpServletRequest)
		{
			SecurityContext.destroy();
		}
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException
	{
	}

	@Override
	public void destroy()
	{
	}

}
