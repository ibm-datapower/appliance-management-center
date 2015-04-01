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
package com.ibm.amc.data.validation;

import com.ibm.amc.resources.exceptions.AmcIllegalArgumentException;

/**
 * Interface implemented by resources that want to perform their own custom validation (in addition
 * to any field level validation specified via annotations). For example, if validation requires
 * knowledge of multiple fields.
 */
public interface SelfValidating
{
	public void validate() throws AmcIllegalArgumentException;
}
