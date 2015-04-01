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

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet filter to indicate to the server when responses should be compressed.
 */
public class CompressionFilter implements Filter
{
	// @CLASS-COPYRIGHT@

	private static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
	private static final String ENCODING_METHOD = "gzip";
	private static final String CONTAINER_ENCODING_HINT = "$WSZIP";
	private static final String[] EXCLUDES = { ".png", ".zip" };

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
			final HttpServletResponse httpResponse = (HttpServletResponse) response;

			// Ensure client handles encoded response
			final String enc = httpRequest.getHeader(HEADER_ACCEPT_ENCODING);
			if (enc != null)
			{
				// Exclude files that are already compressed
				boolean compress = true;
				final String uri = httpRequest.getRequestURI().toLowerCase();
				for (String exclude : EXCLUDES)
				{
					if (uri.endsWith(exclude))
					{
						compress = false;
						break;
					}
				}

				if (compress)
				{
					// Check for supported encoding method
					final String[] methods = enc.split(",");
					for (String key : methods)
					{
						if (ENCODING_METHOD.equalsIgnoreCase(key.trim()))
						{
							httpResponse.addHeader(CONTAINER_ENCODING_HINT, ENCODING_METHOD);
							break;
						}
					}
				}
			}
		}

		chain.doFilter(request, response);
	}

	@Override
	public void init(FilterConfig config) throws ServletException
	{
	}

}
