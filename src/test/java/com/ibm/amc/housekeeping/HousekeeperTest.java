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

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;


public class HousekeeperTest
{
	@Before
	public void stopExisting()
	{
		Housekeeper.reset();
		DummyService.calls.clear();
		Housekeeper.activeServices = new Class[]{DummyService.class};
	}
	
	@Test
	public void basicTest() throws InterruptedException
	{
		DummyService.schedule = new Schedule(100, TimeUnit.MILLISECONDS);
		Housekeeper.init();
		Thread.sleep(1000);
		Housekeeper.reset();
		assertEquals("Number of calls to dummy service:", 10, DummyService.calls.size());
	}
	
	@Test
	public void twoServices() throws InterruptedException
	{
		DummyService.schedule = new Schedule(300, TimeUnit.MILLISECONDS);
		Housekeeper.activeServices = new Class[]{DummyService.class, TenTimesASecond.class};
		Housekeeper.init();
		Thread.sleep(1000);
		Housekeeper.reset();
		assertEquals("Number of calls to dummy service:", 3, DummyService.calls.size());
		assertTrue("Number of calls to 10-times-a-second service:", TenTimesASecond.calls.size() >= 9); // allow a bit of leeway for slow build machines
	}
	
	
	
}

class DummyService implements Service
{
	static Schedule schedule;
	static List<Date> calls = new ArrayList<Date>();
	public void execute()
	{
		calls.add(new Date());
	}
	public Schedule getSchedule()
	{
		return schedule;
	}

	public void init(){}
}

class TenTimesASecond implements Service
{
	static List<Date> calls = new ArrayList<Date>();
	public void execute()
	{
		calls.add(new Date());
	}
	public Schedule getSchedule()
	{
		return new Schedule(100, TimeUnit.MILLISECONDS);
	}

	public void init(){}
}
