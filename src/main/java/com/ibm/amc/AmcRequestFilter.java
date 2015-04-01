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
package com.ibm.amc;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.Provider;

import org.apache.cxf.jaxrs.ext.RequestHandler;
import org.apache.cxf.jaxrs.model.ClassResourceInfo;
import org.apache.cxf.message.Message;

import com.ibm.amc.ras.Logger47;
import com.ibm.amc.resources.data.ExceptionResponse;
import com.ibm.amc.security.Permission;
import com.ibm.amc.security.Permissions;
import com.ibm.amc.security.SecurityContext;

@Provider
public class AmcRequestFilter implements RequestHandler {
	
	static Logger47 logger = Logger47.get(AmcRequestFilter.class.getCanonicalName(), Constants.CWZBA_BUNDLE_NAME);

	
	public AmcRequestFilter()
	{
	}

	@Override
	public Response handleRequest(Message message, ClassResourceInfo cri) {
		try
		{
			if (logger.isEntryEnabled()) logger.entry("handleRequest", message, cri);

			if (SecurityContext.getContext() == null || SecurityContext.getContext().getUser() == null)
			{
				ResponseBuilder response = Response.status(Response.Status.UNAUTHORIZED);
				return response.build();
			}

			// targetMethod is the method (for example, ApplianceResource.getAppliances)
			// that this incoming request will be directed to
			Method targetMethod = (Method)message.get("org.apache.cxf.resource.method");
			
			Permissions permissions = targetMethod.getAnnotation(Permissions.class);
			if (permissions == null)
			{
				// No permissions indicates that a method is accessible by all authenticated
				// users
				if (logger.isEntryEnabled()) logger.exit("handleRequest");
				return null;
			}
			else
			{
				// Retrieve permissions for user
				final Set<Permission> userPermissions = SecurityContext.getContext().getUserPermissions();

				// Check user has at least one of the permissions associated with the method
				for (Permission permission : permissions.value())
				{
					// This user is allowed access; our work here is done.
					if (userPermissions.contains(permission))
					{
						// Call the next handler in the chain (mandatory for all
						// RequestHandler implementations.)
						if (logger.isEntryEnabled()) logger.exit("handleRequest");
						return null;
					}
				}
			}

			Response res = denyAccess(message, permissions, cri);
			if (logger.isEntryEnabled()) logger.exit("handleRequest", res);
			return res;

		}
		/* if this is already an AmcRuntimeException, re-throw */
		catch (AmcRuntimeException are)
		{
			throw are;
		}
		/* else wrap in an AmcRuntimeException */
		catch (Throwable t)
		{
			throw new AmcRuntimeException(t);
		}
	}
	
	/**
	 * Modify the request context so that a permission-denied response will be sent to the
	 * client. After calling this method, the handleRequest method should immediately
	 * return. <br>
	 * <br>
	 * Also logs an Audit message about the unauthorised request.
	 */
	private Response denyAccess(Message context, Permissions permissions, ClassResourceInfo cri)
	{
		String userName = SecurityContext.getContext().getUser();

		Request req = context.get(Request.class);
		
		final Set<String> permissionNames = new HashSet<String>(permissions.value().length);
		for (final Permission permission : permissions.value())
			permissionNames.add(permission.toRest());
		logger.audit("CWZBA1023A_PERMISSION_DENIED", userName, req.getMethod(), cri.getPath().toString(), permissionNames.toString());

		ResponseBuilder response = Response.status(Response.Status.FORBIDDEN);
		response.type(MediaType.APPLICATION_JSON_TYPE);
		response.entity(new ExceptionResponse(userName, new Date(), "CWZBA1022E_PERMISSION_DENIED"));
		return response.build();
	}

}
