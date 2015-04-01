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
package com.ibm.amc.resources.data;

import java.util.Date;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize.Inclusion;
import com.ibm.amc.nls.NLS;
import com.ibm.amc.ras.Logger47;

/**
 * Represents the REST service serialization of an action status.
 */
public class ActionResponse extends AbstractRestData
{
	// No CLASS-COPYRIGHT because we don't want copyright text in ReST responses.

	/**
	 * Constructor.
	 * 
	 * @param userId
	 *            the ID of the user that submitted the action
	 * @param timeStamp
	 *            time stamp for the action response
	 * @param messageKey
	 *            a globalisation message key
	 * @param inserts
	 *            any strings that should be inserted into the message.
	 */
	public ActionResponse(final String userId, final Date timeStamp, final String messageKey, final String... inserts)
	{
		this.timeStamp = (timeStamp == null) ? null : new Date(timeStamp.getTime());

		code = Logger47.getMessageCode(messageKey);
		message = NLS.formatMessage(userId, messageKey, inserts);
	}

	public ActionResponse()
	{
	}

	@Hashcode
	@JsonSerialize(include = Inclusion.NON_NULL)
	public Date timeStamp;
	public String code;
	public String message;

}
