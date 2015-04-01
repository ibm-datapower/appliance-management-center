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

import java.util.concurrent.TimeUnit;

/**
 * Each service run by the housekeeper is expressed in terms of a class that
 * implements this interface.
 */
interface Service
{
	// @CLASS-COPYRIGHT@
	/**
	 * The method that will be called every time the scheduler fires, to perform
	 * the service's raison d'etre.
	 */
	void execute();
	
	/**
	 * @return a Schedule specifying how often the service should be executed.
	 */
	Schedule getSchedule();
	
	/**
	 * Will be called when the service is first created, to do any one-time
	 * setup such as finding out where to clean. Can be empty if no setup is
	 * needed.
	 */
	void init();
}

class Schedule
{
	public int interval;
	public TimeUnit unit;
	
	public Schedule(int interval, TimeUnit unit)
	{
		this.interval = interval;
		this.unit = unit;
	}
}
