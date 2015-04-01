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

import com.ibm.amc.ras.Logger47;

/**
 * Special case exception to be able to throw a checked exception
 * 
 * @author mallman
 */
public class AmcRuntimeomiserException extends RuntimeException
{
	// @CLASS-COPYRIGHT@
	
	static Logger47 logger = Logger47.get(AmcRuntimeomiserException.class.getCanonicalName(), Constants.CWZBA_BUNDLE_NAME);

	final AmcException amcException;
	
	private static final long serialVersionUID = 3242721778327754398L;

	public AmcRuntimeomiserException(final AmcException amcException)
	{
		super();
		
		if (logger.isEntryEnabled()) logger.entry("<init>", amcException);
		
		this.amcException = amcException;

		if (logger.isEntryEnabled()) logger.exit("<init>");
	}

	public AmcException getAmcException()
	{
		return amcException;
	}
}
