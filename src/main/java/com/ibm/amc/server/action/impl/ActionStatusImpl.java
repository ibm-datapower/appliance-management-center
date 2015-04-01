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

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.persistence.Version;

import com.ibm.amc.AmcRuntimeException;
import com.ibm.amc.Constants;
import com.ibm.amc.FileManager;
import com.ibm.amc.Util;
import com.ibm.amc.nls.NLS;
import com.ibm.amc.ras.Logger47;
import com.ibm.amc.resources.TemporaryFileResource;
import com.ibm.amc.server.action.ActionStatus;
import com.ibm.amc.server.action.ActionStatusUpdate;
import com.ibm.amc.server.action.AsyncAction;
import com.ibm.amc.server.impl.PersistenceContext;

/**
 * Action status implementation. Does not need to be thread safe as an action should only be running
 * on a single thread.
 */
@Entity(name = "ActionStatus")
public class ActionStatusImpl implements ActionStatus
{
	// @CLASS-COPYRIGHT@

	@Transient
	static Logger47 logger = Logger47.get(ActionStatusImpl.class.getCanonicalName(), Constants.CWZBA_BUNDLE_NAME);

	/* provided only for improved persistence performance */
//	@SuppressWarnings("unused")
	@Version
	private long version;

	private static final int COLUMN_LENGTH = 255;

	/* entity fields */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long actionId;
	private String name;
	private String descriptionKey;
	@ElementCollection
	@Column(length = COLUMN_LENGTH)
	private List<String> descriptionInserts;
	@Column(length = COLUMN_LENGTH)
	private String applianceId;
	private Date submitted;
	private Date updated;
	private String userId;
	private State state;
	private boolean sync;
	private URI result;
	@OneToMany(targetEntity = ActionStatusUpdateImpl.class, orphanRemoval = true, cascade = CascadeType.ALL)
	private List<ActionStatusUpdate> updates;

	/**
	 * Default constructor for use by JPA.
	 */
	public ActionStatusImpl()
	{
		if (logger.isEntryEnabled()) logger.entry("ActionStatusImpl");

		updates = new ArrayList<ActionStatusUpdate>();

		if (logger.isEntryEnabled()) logger.exit("ActionStatusImpl");
	}

	/**
	 * Constructor for asynchronous action submission.
	 * 
	 * @param userId
	 *            the ID of the user initiating the action
	 * @param action
	 *            the asynchronous action
	 */
	public ActionStatusImpl(final String userId, final AsyncAction action)
	{
		this();
		if (logger.isEntryEnabled()) logger.entry("ActionStatusImpl", userId, action);

		name = action.getName();
		descriptionKey = action.getDescriptionKey();
		descriptionInserts = arrayToTruncatedList(action.getDescriptionInserts(), COLUMN_LENGTH);
		applianceId = Util.truncateString(action.getSubjectId(), COLUMN_LENGTH);
		this.userId = userId;
		state = State.SUBMITTED;
		sync = false;
		final ActionStatusUpdate update = new ActionStatusUpdateImpl("CWZBA1012I_ACTION_SUBMITTED");
		updates.add(update);
		submitted = update.getTimeStamp();
		updated = submitted;
		persist();

		if (logger.isEntryEnabled()) logger.exit("ActionStatusImpl");
	}

	/**
	 * Constructor for synchronous action starting.
	 * 
	 * @param userId
	 *            the ID of the user initiating the action
	 */
	public ActionStatusImpl(final String userId, final String applianceId, final String name, final String descriptionKey, final String... descriptionInserts)
	{
		this();
		if (logger.isEntryEnabled()) logger.entry("ActionStatusImpl", userId, applianceId, name, descriptionKey, descriptionInserts);

		this.name = name;
		this.descriptionKey = descriptionKey;
		this.descriptionInserts = arrayToTruncatedList(descriptionInserts, COLUMN_LENGTH);
		this.applianceId = Util.truncateString(applianceId, COLUMN_LENGTH);
		this.userId = userId;
		state = State.STARTED;
		sync = true;
		final ActionStatusUpdate update = new ActionStatusUpdateImpl("CWZBA1001I_ACTION_STARTED");
		updates.add(update);
		submitted = update.getTimeStamp();
		updated = submitted;
		persist();

		if (logger.isEntryEnabled()) logger.exit("ActionStatusImpl");
	}

	@Override
	public String getActionId()
	{
		return String.valueOf(actionId);
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public String getDescriptionKey()
	{
		return descriptionKey;
	}

	@Override
	public String[] getDescriptionInserts()
	{
		return (descriptionInserts == null) ? null : descriptionInserts.toArray(new String[descriptionInserts.size()]);
	}

	@Override
	public String getApplianceId()
	{
		return applianceId;
	}

	@Override
	public Date getSubmitted()
	{
		return (submitted == null) ? null : new Date(submitted.getTime());
	}

	@Override
	public State getState()
	{
		return state;
	}

	@Override
	public Date getUpdated()
	{
		return (updated == null) ? null : new Date(updated.getTime());
	}

	@Override
	public String getUserId()
	{
		return userId;
	}

	@Override
	public boolean isSynchronous()
	{
		return sync;
	}

	@Override
	public List<ActionStatusUpdate> getUpdates()
	{
		return updates;
	}

	@Override
	public void started()
	{
		if (logger.isEntryEnabled()) logger.entry("started");
		update(State.STARTED, new ActionStatusUpdateImpl("CWZBA1001I_ACTION_STARTED"));
		if (logger.isEntryEnabled()) logger.exit("started");
	}

	@Override
	public void failed(Throwable exception)
	{
		if (logger.isEntryEnabled()) logger.entry("failed", exception);
		update(State.FAILED, new ActionStatusUpdateImpl(exception, "CWZBA1002E_ACTION_FAILED"));
		logger.error("CWZBA1033E_ACTION_FAILED_LOG", exception, NLS.formatMessage(userId, descriptionKey, descriptionInserts.toArray(new String[descriptionInserts.size()])));
		logger.stacktrace(exception);
		if (logger.isEntryEnabled()) logger.exit("failed");
	}

	@Override
	public void succeeded()
	{
		if (logger.isEntryEnabled()) logger.entry("succeeded");
		update(State.SUCCEEDED, new ActionStatusUpdateImpl("CWZBA1003I_ACTION_SUCCEEDED"));
		if (logger.isEntryEnabled()) logger.exit("succeeded");
	}

	@Override
	public void succeeded(final URI result)
	{
		if (logger.isEntryEnabled()) logger.entry("succeeded", result);
		this.result = result;
		succeeded();
		if (logger.isEntryEnabled()) logger.exit("succeeded");
	}

	@Override
	public void updateStatus(final String messageKey, final String... messageInserts)
	{
		if (logger.isEntryEnabled()) logger.entry("updateStatus", messageKey, messageInserts);
		update(null, new ActionStatusUpdateImpl(messageKey, messageInserts));
		if (logger.isEntryEnabled()) logger.exit("updateStatus");
	}

	/**
	 * Persist an update to the action status
	 * 
	 * @param state
	 *            the new state. <code>Null</code> if there is no state change.
	 * @param update
	 *            the action status update
	 */
	private void update(State state, ActionStatusUpdate update)
	{
		if (logger.isEntryEnabled()) logger.entry("update", state, update);

		try
		{
			/* update this action */
			this.updates.add(update);
			this.updated = update.getTimeStamp();

			if (state != null)
			{
				this.state = state;
			}

			persist();

			ActionLogImpl.notifyListeners(this);
		}
		catch (Exception e)
		{
			logger.error("Failed adding update to action status", e, (state == null) ? null : state.toString(), (update.getCause() == null) ? null : update.getCause().getMessage(Locale.getDefault()),
					update.getMessageKey(), Arrays.toString(update.getMessageInserts()));
			logger.debug("update", "action status: ", this.actionId);
			throw new AmcRuntimeException(e);
		}

		if (logger.isEntryEnabled()) logger.exit("update");
	}

	private void persist()
	{
		try
		{
			final EntityManager em = PersistenceContext.getContext().getActionHistoryEntityManager();
			final EntityTransaction tran = em.getTransaction();
			tran.begin();
			em.persist(this);
			tran.commit();
		}
		catch (Exception e)
		{
			logger.error("Failed storing creating action status", e);
			logger.debug("actionSubmitted", "Action status: ", getActionId());
			throw new AmcRuntimeException(e);
		}
	}

	@Override
	public URI getResult()
	{
		// Only return temporary file URI if the file still exists
		if (result != null && result.getPath().startsWith(TemporaryFileResource.PATH))
		{
			return FileManager.getDownloadFile(result.getPath().substring(TemporaryFileResource.PATH.length() + 1)).exists() ? result : null;
		}

		return result;
	}

}
