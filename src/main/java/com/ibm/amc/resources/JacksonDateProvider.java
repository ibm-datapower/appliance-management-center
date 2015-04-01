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
package com.ibm.amc.resources;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@Provider 
@Produces(MediaType.APPLICATION_JSON) 
public class JacksonDateProvider implements ContextResolver<ObjectMapper> { 
	private ObjectMapper objectMapper; 

	public JacksonDateProvider()
	{ 
		objectMapper = new ObjectMapper();
	}

	public ObjectMapper getContext(Class<?> objectType) {
		objectMapper.getFactory().disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
		objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false); 
		return objectMapper; 
	} 
} 