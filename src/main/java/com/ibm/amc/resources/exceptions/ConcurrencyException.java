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

import javax.ws.rs.core.Response.Status;

import com.ibm.amc.AmcRuntimeException;

/**
 * An exception indicating that some kind of resource or locking clash happened
 * inside WAMT.
 */
public class ConcurrencyException extends AmcRuntimeException implements LocalizedException
{
	// @CLASS-COPYRIGHT@

	private static final long serialVersionUID = -6645231665570393553L;
	private static final String MESSAGE_WITH_RESOURCE = "CWZBA0515E_CONCURRENCY_WITH_RESOURCE";
	private static final String MESSAGE_WITHOUT_RESOURCE = "CWZBA0516E_CONCURRENCY_WITHOUT_RESOURCE";

	/**
	 * @param cause The WAMT exception that indicated the problem.
	 * @param resourceName Some identifier for the resource which was being
	 * manipulated.
	 */
	public ConcurrencyException(Exception cause, final String resourceName)
	{
		super(Status.INTERNAL_SERVER_ERROR, cause, MESSAGE_WITH_RESOURCE, resourceName);
	}

	/**
	 * If possible, use another constructor instead of this one, so providing
	 * more information about the problem.
	 * @param cause The WAMT exception that indicated the problem.
	 */
	public ConcurrencyException(Exception cause)
	{
		super(Status.INTERNAL_SERVER_ERROR, cause, MESSAGE_WITHOUT_RESOURCE);
	}
}
