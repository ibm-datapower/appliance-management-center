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
import java.util.List;

/**
 * The REST representation of a Firmware Version
 */
public class Firmware extends AbstractRestData
{
	// No CLASS-COPYRIGHT because we don't want copyright text in ReST responses.

	@Hashcode
	public String primaryKey;

	public String applianceType;

	public String displayName;

	public String modelType;

	public List<String> nonStrictFeatures;

	public List<String> strictFeatures;

	public String level;

	public Date manufactureDate;

	public Date timeStamp;

	public String userComment;

	/**
	 * Set by the server when this firmware is being returned as part of a list
	 * of firmwares suitable for a particular purpose, and the server considers
	 * this one especially appropriate for some reason. Clients may wish to 
	 * offer recommended firmwares as the default option, sort them to the top 
	 * of lists, etc. Note that more than one firmware in a list may have 
	 * recommended status.
	 */
	public boolean recommended;

}
