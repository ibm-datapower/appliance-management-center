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

public class AmcIllegalArgumentException extends AmcRuntimeException implements LocalizedException
{
	// @CLASS-COPYRIGHT@

	private static final long serialVersionUID = -6645231665570393553L;

	public AmcIllegalArgumentException(final String messageKey, final String... inserts)
	{
		this(Status.BAD_REQUEST, messageKey, inserts);
	}

	public AmcIllegalArgumentException(Throwable clause, final String messageKey, final String... inserts)
	{
		this(Status.BAD_REQUEST, clause, messageKey, inserts);
	}
	
	public AmcIllegalArgumentException(final Status httpStatus, final String messageKey, final String... inserts)
	{
		super(httpStatus, messageKey, inserts);
	}

	public AmcIllegalArgumentException(final Status httpStatus, Throwable cause, final String messageKey, final String... inserts)
	{
		super(httpStatus, cause, messageKey, inserts);
	}

}
