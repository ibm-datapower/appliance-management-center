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
package com.ibm.amc.data.wamt;

import com.ibm.amc.data.SvrServiceObject;
import com.ibm.datapower.amt.amp.ConfigObject;

public class WamtServiceObject extends SvrServiceObject
{

	private ConfigObject wamtObject;

	public WamtServiceObject(ConfigObject configObject)
	{
		this.wamtObject = configObject;
	}

	@Override
	public String getPrimaryKey()
	{
		return wamtObject.getPrimaryKey();
	}

	@Override
	public String getName()
	{
		return wamtObject.getName();
	}

	@Override
	public String getType()
	{
		return wamtObject.getClassDisplayName();
	}

}
