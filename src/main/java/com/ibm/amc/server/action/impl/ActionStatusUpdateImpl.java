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
package com.ibm.amc.server.action.impl;

import static com.ibm.amc.Util.arrayToTruncatedList;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.OrderColumn;
import javax.persistence.Transient;
import javax.persistence.Version;

import com.ibm.amc.Constants;
import com.ibm.amc.ras.Logger47;
import com.ibm.amc.server.action.ActionException;
import com.ibm.amc.server.action.ActionStatusUpdate;

/**
 * Implementation of action status update.
 */
@Entity(name = "ActionStatusUpdate")
public class ActionStatusUpdateImpl implements ActionStatusUpdate
{
	// @CLASS-COPYRIGHT@

	@Transient
	static Logger47 logger = Logger47.get(ActionStatusUpdateImpl.class.getCanonicalName(), Constants.CWZBA_BUNDLE_NAME);

	/* provided only for improved persistence performance */
//	@SuppressWarnings("unused")
	@Version
	private long version;

	private static final int COLUMN_LENGTH = 255;

	/* entity fields */
	@OneToOne(targetEntity = ActionExceptionImpl.class, cascade = CascadeType.ALL)
	private ActionException exception;
	private String messageKey;
	@ElementCollection
	@Column(length = COLUMN_LENGTH)
	private List<String> messageInserts;
	@OrderColumn
	private Date timeStamp = new Date();

	/**
	 * Default constructor for use by JPA.
	 */
	public ActionStatusUpdateImpl()
	{
	}

	/**
	 * Constructor for failure updates.
	 * 
	 * @param exception
	 *            the exception that caused the failure
	 */
	public ActionStatusUpdateImpl(final Throwable exception, final String messageKey, final String... messageInserts)
	{
		if (logger.isEntryEnabled()) logger.entry("ActionStatusUpdateImpl", exception, messageKey, messageInserts);

		this.messageKey = messageKey;
		this.messageInserts = arrayToTruncatedList(messageInserts, COLUMN_LENGTH);
		this.exception = new ActionExceptionImpl(exception);

		if (logger.isEntryEnabled()) logger.exit("ActionStatusUpdateImpl");
	}

	/**
	 * Constructor for generic updates.
	 * 
	 * @param messageKey
	 *            the message key
	 * @param messageInserts
	 *            the message inserts
	 */
	public ActionStatusUpdateImpl(final String messageKey, final String... messageInserts)
	{
		if (logger.isEntryEnabled()) logger.entry("ActionStatusUpdateImpl", messageKey, messageInserts);

		this.messageKey = messageKey;
		this.messageInserts = arrayToTruncatedList(messageInserts, COLUMN_LENGTH);

		if (logger.isEntryEnabled()) logger.exit("ActionStatusUpdateImpl");
	}

	@Override
	public ActionException getCause()
	{
		return exception;
	}

	@Override
	public String getMessageKey()
	{
		return messageKey;
	}

	@Override
	public String[] getMessageInserts()
	{
		return (messageInserts == null) ? null : messageInserts.toArray(new String[messageInserts.size()]);
	}

	@Override
	public Date getTimeStamp()
	{
		return (timeStamp == null) ? null : new Date(timeStamp.getTime());
	}
}
