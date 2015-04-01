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

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.ibm.amc.AmcRuntimeException;
import com.ibm.amc.Constants;
import com.ibm.amc.FileManager;
import com.ibm.amc.data.validation.ValidationEngine;
import com.ibm.amc.ras.Logger47;
import com.ibm.amc.resources.data.Firmware;
import com.ibm.amc.resources.data.FirmwareDefiniton;
import com.ibm.amc.resources.exceptions.AmcIllegalArgumentException;
import com.ibm.amc.resources.exceptions.NoSuchResourceException;
import com.ibm.amc.security.Permissions;

import static com.ibm.amc.security.Permission.*;

import com.ibm.amc.server.Controller;
import com.ibm.datapower.amt.DMgrException;

/**
 * ReST resource for Firmware. The representation of Firmware in AMC is a flattened version of both
 * the versioned Firmware entity and a single Firmware Version.
 * 
 */
@Path("/firmware")
@Produces(MediaType.APPLICATION_JSON)
public class FirmwareResource extends AbstractResource
{
	// @CLASS-COPYRIGHT@

	static Logger47 logger = Logger47.get(FirmwareResource.class.getCanonicalName(), Constants.CWZBA_BUNDLE_NAME);

	public FirmwareResource()
	{
		super();
	}

	public FirmwareResource(final Controller controller)
	{
		super(controller);
	}

	@POST
	@Permissions(FIRMWARE_ADD)
	public Response addFirmware(FirmwareDefiniton firmware)
	{
		if (logger.isEntryEnabled()) logger.entry("addFirmware", firmware);

		ValidationEngine.getInstance().validate(firmware);

		// Check uploaded file is not zero length
		if (FileManager.SCHEME.equals(firmware.uri.getScheme()))
		{
			if (new File(FileManager.resolveUri(firmware.uri)).length() == 0)
			{
				throw new AmcIllegalArgumentException("CWZBA1502E_EMPTY_FILE");
			}
		}

		final URL url = FileManager.resolveUriToUrl(firmware.uri);

		String actionId = controller.addFirmware(url, firmware.userComment);
		final URI actionUri;
		try
		{
			actionUri = new URI("/actions/" + actionId);
		}
		catch (final URISyntaxException e)
		{
			throw new AmcRuntimeException(e);
		}

		Response result = Response.status(Response.Status.ACCEPTED).location(actionUri).build();

		if (logger.isEntryEnabled()) logger.exit("addFirmware", result);
		return result;
	}

	@DELETE
	@Path("{firmwareId}")
	@Permissions(FIRMWARE_REMOVE)
	public void removeFirmware(@PathParam("firmwareId") String firmwareId)
	{
		if (logger.isEntryEnabled()) logger.entry("removeFirmware", firmwareId);

		controller.removeFirmware(firmwareId);

		if (logger.isEntryEnabled()) logger.exit("removeFirmware");
	}

	@PUT
	@Path("{firmwareId}")
	@Permissions(FIRMWARE_EDIT_PROPERTIES)
	public Firmware updateFirmware(@PathParam("firmwareId") String firmwareId, Firmware firmware)
	{
		if (logger.isEntryEnabled()) logger.entry("updateFirmware", firmwareId, firmware);

		Firmware result = controller.updateFirmware(firmwareId, firmware);

		if (logger.isEntryEnabled()) logger.exit("updateFirmware", result);
		return result;
	}

	@GET
	@Permissions(FIRMWARE_VIEW)
	public Response getFirmware() throws DMgrException, NoSuchResourceException
	{
		if (logger.isEntryEnabled()) logger.entry("getFirmware");

		List<Firmware> firmware = controller.getFirmware(uriInfo.getQueryParameters().entrySet());
		
		Response result = Response.ok(firmware).header("Content-Range", "items */" + firmware.size()).build();

		if (logger.isEntryEnabled()) logger.exit("getFirmware", result);
		return result;
	}

	@GET
	@Path("{firmwareId}")
	@Permissions(FIRMWARE_VIEW)
	public Firmware getFirmwareById(@PathParam("firmwareId") String firmwareId) throws DMgrException, NoSuchResourceException
	{
		if (logger.isEntryEnabled()) logger.entry("getFirmwareById", firmwareId);

		Firmware firmware = controller.getFirmwareById(firmwareId);

		if (logger.isEntryEnabled()) logger.exit("getFirmwareById", firmware);
		return firmware;
	}

}
