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
package com.ibm.amc.resources.exceptions;

import java.net.URI;

import javax.ws.rs.core.Response.Status;

import com.ibm.amc.AmcException;
import com.ibm.amc.nls.NLS;
import com.ibm.amc.security.SecurityContext;

/**
 * An exception indicating that a remote HTTP server could not find a file. The
 * associated user message implies an HTTP 404 situation, but doesn't use the 
 * code, so it would be possible to also use this exception for closely-related
 * cases. However, something like a "host not found" should not use this 
 * exception as the user may need to know specifically that his server has gone 
 * down. 
 */
public class HttpNotFoundException extends AmcException
{
	// @CLASS-COPYRIGHT@

	private static final long serialVersionUID = 1L;

	public HttpNotFoundException(String url)
	{
		super(Status.BAD_REQUEST, "CWZBA0538E_HTTP_NOT_FOUND", url);
	}

	public HttpNotFoundException(URI url)
	{
		this(url.toString());
	}

	public HttpNotFoundException(String url, String activityKey, String... activityInserts)
	{
		super(Status.BAD_REQUEST, "CWZBA0537E_HTTP_NOT_FOUND_WITH_ACTION", url, 
				NLS.formatMessage(SecurityContext.getContext().getUser(), activityKey, activityInserts));
	}

}
