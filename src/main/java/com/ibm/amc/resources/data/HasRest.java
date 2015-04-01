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

/**
 * A server-side data object which has an accompanying ReST representation. That
 * is, it has a toRest method which returns a data structure (implementing 
 * AbstractRestData) suitable for automatically generating a ReST string.
 */
public interface HasRest
{
	// No CLASS-COPYRIGHT because we don't want copyright text in ReST responses.

	public abstract AbstractRestData toRest(); 
}
