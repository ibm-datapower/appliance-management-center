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
package com.ibm.amc;

import static org.junit.Assert.*;

import org.junit.Test;

import com.ibm.datapower.amt.Messages;
import com.ibm.datapower.amt.amp.AMPException;


public class UtilTest
{
	@Test
	public void testWamtExceptionValid()
	{
		// No signficance to "invalidTopic", just the first bit of WAMT code 
		// that came to hand.
		String message = Messages.getString("wamt.amp.defaultProvider.CommandsImpl.invalidTopic","foo");
        AMPException e = new AMPException(message,"wamt.amp.defaultProvider.CommandsImpl.invalidTopic","foo");
        
		assertEquals("WAMT0239", Util.getWamtExceptionCode(e));
	}
	
	@Test
	public void testWamtExceptionOtherExceptionType()
	{
		assertEquals("", Util.getWamtExceptionCode(new IllegalStateException("foo")));
	}
	
	@Test
	public void testWamtExceptionSneakyMessages()
	{
		assertEquals("", Util.getWamtExceptionCode(new Exception("Please look for WAMT0239 errors in the logs.")));
		assertEquals("", Util.getWamtExceptionCode(new Exception("WAMT0239 is a nasty error")));
		assertEquals("", Util.getWamtExceptionCode(new Exception("WAMT0239E is too.")));
		assertEquals("", Util.getWamtExceptionCode(new Exception("If you see \"WAMT0239E: cheese\" you should run away.")));
	}
	
	@Test
	public void testWamtExceptionNoMessage()
	{
		assertEquals("", Util.getWamtExceptionCode(new IllegalStateException()));
	}
}
