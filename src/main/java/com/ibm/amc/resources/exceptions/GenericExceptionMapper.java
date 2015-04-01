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

import java.util.Date;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.ibm.amc.AmcRuntimeException;
import com.ibm.amc.AmcRuntimeomiserException;
import com.ibm.amc.Constants;
import com.ibm.amc.ras.Logger47;
import com.ibm.amc.resources.data.ExceptionResponse;
import com.ibm.amc.security.SecurityContext;

@Provider
public class GenericExceptionMapper extends AbstractExceptionMapper<Throwable> implements ExceptionMapper<Throwable>
{
	// @CLASS-COPYRIGHT@

	static Logger47 logger = Logger47.get(GenericExceptionMapper.class.getCanonicalName(), Constants.CWZBA_BUNDLE_NAME);
	
	@Override
	public Response toResponse(Throwable exception)
	{
		if (logger.isEntryEnabled()) logger.entry("toResponse", exception);
		
		// Notwithstanding the efforts below to only log genuine error conditions
		// in normal production use, unconditionally stacktrace all exceptions 
		// if trace is on.
		if(logger.isDebugEnabled()) logger.stacktrace(exception);
		
		ResponseBuilder responseBuilder;
		
		/* check if this is our special case of a wrapped, checked exception */
		if (exception instanceof AmcRuntimeomiserException)
		{
			exception = ((AmcRuntimeomiserException) exception).getAmcException();
		}
		
		if (exception instanceof LocalizedException)
		{
			responseBuilder = Response.status(((LocalizedException) exception).getHttpStatusCode());
			responseBuilder.type(MediaType.APPLICATION_JSON_TYPE);
			responseBuilder.entity(new ExceptionResponse(exception));
			
			if(exception instanceof AmcRuntimeException && ((AmcRuntimeException) exception).getMessageCode().equals("CWZBA0504E"))
			{
				// Stacktrace if this exception didn't have a specific message.
				// Exceptions with messages represent exceptional conditions in
				// WAMC's environment (eg, appliance gone away) rather than 
				// errors in WAMC itself, and they generally don't require a 
				// stacktrace to figure out what happened. 
				logError(exception.getCause() == null? exception : exception.getCause());
			}
		}
		else
		{
			responseBuilder = Response.status(Status.INTERNAL_SERVER_ERROR);
			responseBuilder.type(MediaType.APPLICATION_JSON_TYPE);
			final String message = (exception.getLocalizedMessage() == null) ? exception.getClass().getCanonicalName() : exception.getClass().getName()+": "+exception.getLocalizedMessage();
			responseBuilder.entity(new ExceptionResponse(SecurityContext.getContext().getUser(), new Date(), "CWZBA0504E_UNEXPECTED_ERROR", message));
			
			// Always stacktrace un-wrapped exceptions, as they always represent 
			// an unexpected error.
			logError(exception);
		}
		
		Response response = responseBuilder.build();
		
		if (logger.isEntryEnabled()) logger.exit("toResponse", response);
		return response;
	}

	private void logError(Throwable exception)
	{
		String message = (exception.getLocalizedMessage() == null) ? exception.getClass().getCanonicalName() : exception.getClass().getName()+": "+exception.getLocalizedMessage();
		// Write in console.log to say there's been an error
		logger.error("CWZBA0528E_LOG_UNEXPECTED_ERROR", message);
		// Write the full details into messages.log
		logger.stacktrace(exception);
	}
}
