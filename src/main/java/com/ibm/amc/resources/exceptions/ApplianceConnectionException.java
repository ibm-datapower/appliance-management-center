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

import com.ibm.amc.AmcException;


public class ApplianceConnectionException extends AmcException
{
	// @CLASS-COPYRIGHT@

	private static final long serialVersionUID = 7428733975180760474L;

	public ApplianceConnectionException(Throwable cause, String host, int port)
	{
		super(Status.BAD_REQUEST, cause, "CWZBA0509E_APPLIANCE_CONNECTION_ERROR", host, Integer.toString(port));
	}

}
