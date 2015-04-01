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

import static com.ibm.amc.security.Permission.APPLIANCE_BACKUP;
import static com.ibm.amc.security.Permission.APPLIANCE_CREATE_DOMAIN;
import static com.ibm.amc.security.Permission.DOMAIN_UPDATE_CONFIGURATION;
import static com.ibm.amc.security.Permission.DOMAIN_UPLOAD_FILE;
import static com.ibm.amc.security.Permission.FIRMWARE_ADD;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.MultipartBody;

import com.ibm.amc.Constants;
import com.ibm.amc.FileManager;
import com.ibm.amc.ras.Logger47;
import com.ibm.amc.security.Permissions;
import com.ibm.amc.server.Controller;

/**
 * REST resource for temporary file handling (upload and download).
 * 
 */
@Path(TemporaryFileResource.RELATIVE_PATH)
@Produces(MediaType.APPLICATION_JSON)
public class TemporaryFileResource extends AbstractResource
{
	// @CLASS-COPYRIGHT@

	static Logger47 logger = Logger47.get(TemporaryFileResource.class.getCanonicalName(), Constants.CWZBA_BUNDLE_NAME);

	static final String RELATIVE_PATH = "/files";

	public static final String PATH = CONTEXT_ROOT + RELATIVE_PATH;

	public TemporaryFileResource()
	{
		super();
	}

	public TemporaryFileResource(final Controller controller)
	{
		super(controller);
	}

	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	@Permissions({APPLIANCE_BACKUP, FIRMWARE_ADD, DOMAIN_UPDATE_CONFIGURATION, APPLIANCE_CREATE_DOMAIN, DOMAIN_UPLOAD_FILE})
	public String uploadFile(MultipartBody body) throws IOException
	{
		// review if a generic solution can be made to work
		final String METHOD = "uploadFile";
		if (logger.isEntryEnabled()) logger.entry(METHOD, body);

		final List<URI> uris = new ArrayList<URI>();
		String referenceCount = null;
		boolean wrapResponse = false;

		List<Attachment> attachments = body.getAllAttachments();
		// process the InMultiPart, gather out the useful bits
		for(Attachment part : attachments)
		{
			final String name = part.getContentDisposition().getParameter("name");
			if ("referenceCount".equals(name)) referenceCount = getString(part);
			else if ("wrapResponse".equals(name)) wrapResponse = Boolean.valueOf(getString(part));
			else
			{
				String fileName = part.getContentDisposition().getParameter("filename");
				if (fileName != null)
				{
					// If the part is a file, write it to a working file
					if (logger.isDebugEnabled()) logger.debug(METHOD, "Part is a file");
					uris.add(FileManager.getUriForTemporaryFile(writeToFile(part.getDataHandler().getInputStream())));
				}
			}
		}
		
		// If provided, set initial reference count
		if (referenceCount != null)
		{	
			if (logger.isDebugEnabled()) logger.debug(METHOD, "Setting reference count to " + referenceCount);
			for (URI uri:uris) FileManager.setReferenceCount(uri, Integer.parseInt(referenceCount));
		}
			
		StringBuffer sb = new StringBuffer();
		if (wrapResponse) sb.append("<textarea>");
		sb.append("{\"files\":[");
		if (!uris.isEmpty())
		{
			sb.append("\"");
			sb.append(StringUtils.join(uris, "\",\""));
			sb.append("\"");
		}
		sb.append("]}");
		if (wrapResponse) sb.append("</textarea>");

		if (logger.isEntryEnabled()) logger.exit(METHOD, sb.toString());
		return sb.toString();
	}
	
	private static String getString(Attachment part) throws IOException
	{
		return part.getObject(String.class);
	}

	@GET
	@Path("{fileName}")
	@Permissions(APPLIANCE_BACKUP)
	public Response downloadFile(@PathParam("fileName") String fileName)
	{
		final String METHOD = "downloadFile";
		if (logger.isEntryEnabled()) logger.entry(METHOD, fileName);

		final File file = FileManager.getDownloadFile(fileName);
		final StreamingOutput output = new StreamingOutput()
		{
			@Override
			public void write(OutputStream os) throws IOException
			{
				IOUtils.copy(new FileInputStream(file), os);
			}
		};
		final ResponseBuilder builder;
		if (file.getName().endsWith(".zip"))
		{
			builder = Response.ok(output, "application/zip");
		}
		else
		{
			builder = Response.ok(output, MediaType.APPLICATION_OCTET_STREAM_TYPE);
		}
		final Response response = builder.build();
		if (logger.isEntryEnabled()) logger.exit(METHOD, response);
		return response;
	}

	@DELETE
	@Path("{fileName}")
	@Permissions(APPLIANCE_BACKUP)
	public void deleteFile(@PathParam("fileName") String fileName)
	{
		if (!FileManager.getDownloadFile(fileName).delete())
		{
			if (logger.isDebugEnabled()) logger.debug("deleteFile", "Failed to delete file: " + fileName);
		}
	}

	// -------------------------------------------------------------------------
	// private methods
	// -------------------------------------------------------------------------
	/**
	 * Write an InputStream to a local file
	 * 
	 * @param stream
	 *            contains the file
	 * @return a File object from the input stream
	 * @throws IOException
	 */
	private File writeToFile(InputStream stream) throws IOException
	{
		if (logger.isEntryEnabled()) logger.entry("writeToFile", stream);
		File file = FileManager.createUploadFile();

		BufferedInputStream in = null;
		BufferedOutputStream out = null;

		try
		{
			in = new BufferedInputStream(stream);
			out = new BufferedOutputStream(new FileOutputStream(file));

			IOUtils.copy(in, out);
		}
		finally
		{
			if (in != null)
			{
				try
				{
					in.close();
				}
				catch (IOException e)
				{
					logger.error("ERROR: IOException thrown attempting to close the input stream", e);
				}
			}
			if (out != null)
			{
				try
				{
					out.close();
				}
				catch (IOException e)
				{
					logger.error("ERROR: IOException thrown attempting to close the output stream", e);
				}
			}
		}
		if (logger.isEntryEnabled()) logger.exit("writeToFile", file);
		return file;
	}

}
