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

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.ibm.amc.resources.data.ExceptionResponse;
import com.ibm.amc.security.SecurityContext;

@Provider
public class UnrecognizedPropertyExceptionMapper extends AbstractExceptionMapper<UnrecognizedPropertyException> implements ExceptionMapper<UnrecognizedPropertyException>
{
	// @CLASS-COPYRIGHT@

	@Override
	public Response toResponse(UnrecognizedPropertyException exception)
	{
		ResponseBuilder response = Response.status(Status.BAD_REQUEST);
		response.type(MediaType.APPLICATION_JSON_TYPE);
		response.entity(new ExceptionResponse(SecurityContext.getContext().getUser(), new Date(), "CWZBA0506E_UNRECOGNIZED_PROPERTY", exception.getPropertyName()));
		return response.build();
	}
}
