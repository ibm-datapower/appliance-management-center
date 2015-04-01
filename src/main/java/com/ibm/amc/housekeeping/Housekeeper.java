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
package com.ibm.amc.housekeeping;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import com.ibm.amc.Constants;
import com.ibm.amc.IShutdown;
import com.ibm.amc.ShutdownListener;
import com.ibm.amc.ras.Logger47;


/**
 * A class which operates more or less autonomously from the rest of the system,
 * performing periodic file cleanup and similar activities. It has an init 
 * method which should be called once to start it running; if multiple calls
 * are made they will harmlessly return.
 */
public class Housekeeper implements IShutdown
{
	// @CLASS-COPYRIGHT@
	
	static Logger47 logger = Logger47.get(Housekeeper.class.getCanonicalName(), Constants.CWZBA_BUNDLE_NAME);
	
	/** Singleton Housekeeper */
	private static Housekeeper instance;

	/**
	 * Initialise the Housekeeper system. This must be called once during system
	 * startup (or shortly thereafter). Multiple calls will not hurt.
	 */
	public static synchronized void init()
	{
		if(instance != null) return;
		
		instance = new Housekeeper();
	}

	/**
	 * The list of Service classes which should be run. To add a new 
	 * housekeeping service, implement the Service interface and add your 
	 * class to this list.
	 */
	static Class<?>[] activeServices = {
		CleanUploads.class,
		CleanDownloads.class,
		RefreshProperties.class
	};
	
	private ScheduledExecutorService scheduler;
	
	
	private Housekeeper()
	{
		scheduler = Executors.newSingleThreadScheduledExecutor();				ShutdownListener.addShutdownListener(this);
		
		nextService: for (Class<?> serviceClass : activeServices)
		{
			final Service service = instantiateService(serviceClass);
			if(service == null) continue nextService;
			
			service.init();
			Schedule schedule = service.getSchedule();
			
			scheduler.scheduleAtFixedRate(new Runnable()
			{
				public void run()
				{
					try
					{
						service.execute();
					}
					catch(Throwable t)
					{
						logger.warning("CWZBA1025W_HOUSEKEEPER_EXCEPTION", t.getClass().getSimpleName(), t.getLocalizedMessage());
						if (logger.isDebugEnabled()) 
						{
							logger.debug("Housekeeper()", "Exception details:", t);
							logger.stacktrace(t);
						}
					}
				}
			}, schedule.interval, schedule.interval, schedule.unit);
		}
	}

	private void stop()
	{
		scheduler.shutdownNow();
	}			public void shutdown() {		if(scheduler != null)			scheduler.shutdown();	}
	
	/**
	 * @param serviceClass A Class object which must implement the Service
	 * interface. (Due to the limitations of erasure-based generics in respect
	 * of arrays like activeServices, we can't enforce this at compile-time.)
	 * @return the corresponding Service instance, or null if an error occurs.
	 * Errors are logged to debug, but no exceptions are thrown.
	 */
	private Service instantiateService(Class<?> serviceClass)
	{
		try
		{
			return (Service) serviceClass.newInstance();
		}
		catch (ClassCastException e)
		{
			if (logger.isDebugEnabled()) logger.debug("instantiateService()", "The class "+serviceClass.getName()+" in the Housekeeper's list of services doesn't implement the Service interface.");
		}
		catch (Exception e)
		{
			if (logger.isDebugEnabled()) logger.debug("instantiateService()", "Error instantiating Housekeeper service "+serviceClass.getSimpleName(), e);
		}
		return null;
	}
	
	/**
	 * Shutdown the thread pool, (attempt to) stop any current running tasks,
	 * remove the reference to the current instance. Intended for unit test only
	 * at this point.
	 */
	static synchronized void reset()
	{
		if(instance == null) return;
		instance.stop();
		instance = null;
	}
}
