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
import java.util.HashSet;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ibm.amc.Constants;
import com.ibm.amc.ras.Logger47;

/**
 * Filter to disable caching.
 */
public class CacheFilter implements Filter
{
	// @CLASS-COPYRIGHT@

	private static final Logger47 logger = Logger47.get(CacheFilter.class.getCanonicalName(), Constants.CWZBA_BUNDLE_NAME);
	
	private static final Set<String> excludePatterns = new HashSet<String>();
	static {
		excludePatterns.add("^/files/.*");
	}

	@Override
	public void destroy()
	{
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
	{
		if (request instanceof HttpServletRequest)
		{
			final HttpServletRequest httpRequest = (HttpServletRequest) request;
			final String path = httpRequest.getPathInfo();
			boolean disableCaching = true;
			for (String pattern: excludePatterns) {
				if (path.matches(pattern))
				{
					disableCaching = false;
					break;
				}
			}
			if (disableCaching)
			{
				final HttpServletResponse httpResponse = (HttpServletResponse) response;
				if (logger.isDebugEnabled()) logger.debug("doFilter", "Disable caching of response for path " + path);
				httpResponse.setHeader("Pragma", "No-cache");
				httpResponse.setHeader("Cache-Control", "no-cache,no-store,max-age=0");
				httpResponse.setDateHeader("Expires", 1);
			}
		}
		chain.doFilter(request, response);
	}

	@Override
	public void init(FilterConfig config) throws ServletException
	{
	}

}
