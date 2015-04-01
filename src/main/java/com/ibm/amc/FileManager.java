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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.FileUtils;

import com.ibm.amc.ras.Logger47;
import com.ibm.amc.resources.exceptions.AmcIllegalArgumentException;
import com.ibm.amc.security.SecurityContext;

public class FileManager
{
	// @CLASS-COPYRIGHT@

	static Logger47 logger = Logger47.get(FileManager.class.getCanonicalName(), Constants.CWZBA_BUNDLE_NAME);

	/** Count of how many actions are sharing a particular file (provided they told us) */
	private static Map<URI, Integer> references = new HashMap<URI, Integer>();

	/** Prefix given to temporary files */
	private static final String FILE_PREFIX = "wamc";

	/** Suffix given to temporary files */
	private static final String FILE_SUFFIX = ".temp";

	/** Directory under WAMT repository used for uploads */
	private static final String UPLOAD_DIR = "upload";

	/** Directory under WAMT repository used for downloads */
	private static final String DOWNLOAD_DIR = "download";

	/** The URI scheme used for temporary files */
	public static final String SCHEME = "wamctmp";

	/** Buffer size for ZIP operations */
	private static final int ZIP_BUFFER = 8192;

	private static final String ZIP_EXTENSION = ".zip";

	/**
	 * @return The root upload directory, in which each user has their own
	 * subdirectory.
	 */
	public static File getUploadRoot()
	{
		String repoDir = WamcProperties.instance().getWamtRepository();
		if (!repoDir.endsWith(File.separator)) repoDir += File.separator;

		return new File(repoDir + UPLOAD_DIR);
	}

	/**
	 * @return The root download directory, in which each user has their own
	 * subdirectory.
	 */
	public static File getDownloadRoot()
	{
		String repoDir = WamcProperties.instance().getWamtRepository();
		if (!repoDir.endsWith(File.separator)) repoDir += File.separator;

		return new File(repoDir + DOWNLOAD_DIR);
	}
	
	/**
	 * @return The upload directory for the current user.
	 */
	private static File getUploadDirectory()
	{
		return new File(getUploadRoot(), SecurityContext.getContext().getUser());
	}

	private static File getDownloadDirectory()
	{
		return new File(getDownloadRoot(), SecurityContext.getContext().getUser());
	}

	public static File getDownloadFile(final String fileName)
	{
		return new File(getDownloadDirectory(), fileName);
	}

	/**
	 * Converts a temporary file name to a URI.
	 * 
	 * @param file
	 *            the temporary file
	 * @return the URI
	 */
	public static URI getUriForTemporaryFile(File file)
	{
		try
		{
			final String fileName = file.getName();
			return new URI(SCHEME, fileName.substring(FILE_PREFIX.length(), fileName.length() - FILE_SUFFIX.length()), null);
		}
		catch (final URISyntaxException e)
		{
			// Unexpected exception
			throw new AmcRuntimeException(e);
		}
	}

	/**
	 * Resolves an input URI to a URL suitable for WAMT. If the URI represents a temporary file,
	 * converts it to the real file location.
	 * 
	 * @param uri
	 *            the input URI
	 * @return the output URL
	 */
	public static URL resolveUriToUrl(URI uri)
	{
		// if a null is passed in, send one back
		if (uri == null) return null;
		
		try
		{
			return resolveUri(uri).toURL();
		}
		catch (MalformedURLException e)
		{
			if (logger.isDebugEnabled()) 
			{
				logger.debug("resolveUriToUrl()", "MalformedURLException:", e);
				logger.stacktrace(e);
			}
			if (uri.getScheme().equals(SCHEME))
			{
				throw new AmcIllegalArgumentException(e, "CWZBA2002E_TEMPORARY_FILE_FAILED", uri.toString());
			}
			else
			{
				throw new AmcIllegalArgumentException("CWZBA2003E_INVALID_URI", uri.toString());
			}
		}
	}

	/**
	 * Resolve an input URI to one suitable for WAMT. If the URI represents a temporary file,
	 * converts it to the real file location.
	 * 
	 * @param uri
	 *            the input URI
	 * @return the output URI
	 */
	public static URI resolveUri(URI uri)
	{
		return (SCHEME.equals(uri.getScheme())) ? getFileForUri(uri).toURI() : uri;
	}

	/**
	 * If the URI represents a temporary file, attempt to delete the corresponding file.
	 * 
	 * @param uri
	 */
	public static void deleteIfTemporaryFile(URI uri)
	{
		if (uri != null && uri.getScheme().equals(SCHEME))
		{
			if (getFileForUri(uri).delete())
			{
				if (logger.isDebugEnabled()) logger.debug("deleteIfTemporaryFile", "Deleted temporary file " + uri.toString());
			}
			else
			{
				if (logger.isDebugEnabled()) logger.debug("deleteIfTemporaryFile", "Failed to delete temporary file " + uri.toString());
			}

			// Check for unzip directory
			final File unzipDirectory = new File(getDownloadDirectory(), uri.getSchemeSpecificPart());
			if (unzipDirectory.exists())
			{
				try
				{
					FileUtils.deleteDirectory(unzipDirectory);
					if (logger.isDebugEnabled()) logger.debug("deleteIfTemporaryFile", "Deleted temporary directory " + unzipDirectory);
				}
				catch (final IOException e)
				{
					if (logger.isDebugEnabled()) logger.debug("deleteIfTemporaryFile", "Failed to delete temporary directory " + unzipDirectory);
				}
			}

		}
	}

	/**
	 * Generate the name of a working file to upload to. Working files are located in the WAMT
	 * Repository in a folder corresponding to the requester's user ID under the 'upload' folder.
	 * 
	 * @return The working File
	 * @throws IOException
	 *             if something bad occurs while creating a the working file
	 */
	public static File createUploadFile() throws IOException
	{
		if (logger.isEntryEnabled()) logger.entry("createUploadFile");

		File uploadDir = getUploadDirectory();

		if (!uploadDir.exists())
		{
			if (!uploadDir.mkdirs())
			{
				throw new AmcRuntimeException(Status.INTERNAL_SERVER_ERROR, "CWZBA2001E_DIRECTORY_CREATION_FAILED", uploadDir.getPath());
			}
		}

		File f = File.createTempFile(FILE_PREFIX, FILE_SUFFIX, uploadDir);
		if (logger.isEntryEnabled()) logger.exit("createUploadFile", f.getCanonicalPath());
		return f;
	}

	/**
	 * Creates a download directory with the given name.
	 * 
	 * @return a file object representing the directory
	 */
	public static File createDownloadDirectory(final String directoryName)
	{
		if (logger.isEntryEnabled()) logger.entry("createDownloadDirectory");

		File parentDirectory = getDownloadDirectory();
		File downloadDirectory = new File(parentDirectory, directoryName);

		if (!downloadDirectory.exists())
		{
			if (!downloadDirectory.mkdirs())
			{
				throw new AmcRuntimeException(Status.INTERNAL_SERVER_ERROR, "CWZBA2001E_DIRECTORY_CREATION_FAILED", downloadDirectory.getPath());
			}
		}

		if (logger.isEntryEnabled()) logger.exit("createDownloadDirectory", downloadDirectory);
		return downloadDirectory;
	}

	private static File getFileForUri(URI uri)
	{
		return new File(getUploadDirectory(), FILE_PREFIX + uri.getSchemeSpecificPart() + FILE_SUFFIX);
	}

	public static File decompress(URI temporaryFileUri)
	{
		final File destination = new File(getUploadDirectory(), temporaryFileUri.getSchemeSpecificPart());
		if (!destination.mkdirs())
		{
			throw new AmcRuntimeException(Status.INTERNAL_SERVER_ERROR, "CWZBA2001E_DIRECTORY_CREATION_FAILED", destination.getPath());
		}

		ZipFile zipFile = null;
		try
		{
			zipFile = new ZipFile(getFileForUri(temporaryFileUri));

			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while (entries.hasMoreElements())
			{
				ZipEntry entry = entries.nextElement();
				File newDirOrFile = new File(destination, entry.getName());
				if (newDirOrFile.getParentFile() != null && !newDirOrFile.getParentFile().exists())
				{
					if (!newDirOrFile.getParentFile().mkdirs())
					{
						throw new AmcRuntimeException(Status.INTERNAL_SERVER_ERROR, "CWZBA2001E_DIRECTORY_CREATION_FAILED", newDirOrFile.getParentFile().getPath());
					}
				}
				if (entry.isDirectory())
				{
					if (!newDirOrFile.mkdir())
					{
						throw new AmcRuntimeException(Status.INTERNAL_SERVER_ERROR, "CWZBA2001E_DIRECTORY_CREATION_FAILED", newDirOrFile.getPath());
					}
				}
				else
				{
					BufferedInputStream bis = new BufferedInputStream(zipFile.getInputStream(entry));
					int size;
					byte[] buffer = new byte[ZIP_BUFFER];
					BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(newDirOrFile), ZIP_BUFFER);
					while ((size = bis.read(buffer, 0, ZIP_BUFFER)) != -1)
					{
						bos.write(buffer, 0, size);
					}
					bos.flush();
					bos.close();
					bis.close();
				}
			}
		}
		catch (Exception e)
		{
			throw new AmcRuntimeException(e);
		}
		finally
		{
			if (zipFile != null)
			{
				try
				{
					zipFile.close();
				}
				catch (IOException e)
				{
					logger.debug("decompress", "close failed with " + e);
				}
			}
		}

		return destination;
	}

	public static File compress(File pathToCompress)
	{
		try
		{
			File zipFile = new File(pathToCompress.getCanonicalPath() + ZIP_EXTENSION);
			FileOutputStream fileOutputStream = new FileOutputStream(zipFile);
			CheckedOutputStream cos = new CheckedOutputStream(fileOutputStream, new CRC32());
			ZipOutputStream out = new ZipOutputStream(cos);
			String basedir = "";
			compress(pathToCompress, out, basedir);
			out.close();
			return zipFile;
		}
		catch (Exception e)
		{
			throw new AmcRuntimeException(e);
		}
	}

	private static void compress(File file, ZipOutputStream out, String basedir)
	{
		if (file.isDirectory())
		{
			compressDirectory(file, out, basedir);
		}
		else
		{
			compressFile(file, out, basedir);
		}
	}

	private static void compressDirectory(File dir, ZipOutputStream out, String basedir)
	{
		try
		{
			File[] files = dir.listFiles();
			for (int i = 0; i < files.length; i++)
			{
				compress(files[i], out, basedir + dir.getName() + "/");
			}
		}
		catch (Exception e)
		{
			throw new AmcRuntimeException(e);
		}
	}

	private static void compressFile(File file, ZipOutputStream out, String basedir)
	{
		try
		{
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
			ZipEntry entry = new ZipEntry(basedir + file.getName());
			out.putNextEntry(entry);
			int length = Long.valueOf(file.length()).intValue();
			int buffer = ZIP_BUFFER;
			if (length != 0)
			{
				buffer = length;
			}
			int count;
			byte data[] = new byte[buffer];
			while ((count = bis.read(data, 0, buffer)) != -1)
			{
				out.write(data, 0, count);
			}
			bis.close();
		}
		catch (Exception e)
		{
			throw new AmcRuntimeException(e);
		}
	}

	/**
	 * Engage the reference-counting function for a temporary upload file. This
	 * is for use when a series of actions is to take place in quick succession
	 * using the same temporary file. By "quick succession", we mean as part of
	 * the same ReST call. This is not intended for use between calls from the 
	 * client.
	 * <br><br> 
	 * Each action calls this method as early as possible in its execution, to 
	 * register its interest in the file. When the action has finished with the 
	 * file, it calls the corresponding decrement method. Once the number of 
	 * decrements matches the number of increments, everybody has finished with 
	 * the file and it will be immediately deleted (before the last decrement 
	 * call returns).
	 * <br><br>
	 * Files using this mechanism are not immune from deletion by the 
	 * housekeeper even while they still have references, but this should not 
	 * cause a problem unless your actions are taking a significant portion of 
	 * a day to complete. 
	 * @param file The file to count references for. Non-FileManager URIs can
	 * be passed, but they will be silently ignored and no references maintained
	 * for them; we wouldn't be able to delete them anyway.
	 */
	// This is all lost if we get restarted. That's fine, the actions will be
	// lost too, and the housekeeper will tidy it all up eventually.
	public static synchronized void incrementReferenceCount(URI file)
	{
		if (logger.isEntryEnabled()) logger.entry("incrementReferenceCount", file);
		if(file==null) return;
		if(!file.getScheme().equalsIgnoreCase(SCHEME)) return; // Only manage our own files.
		
		Integer count = references.get(file);
		if(count == null) count = 0;
		references.put(file, ++count);

		if (logger.isDebugEnabled()) logger.debug("incrementReferenceCount()", "Reference count increased to "+count+" for "+file);
		if (logger.isEntryEnabled()) logger.exit("incrementReferenceCount");
	}
	
	public static synchronized void setReferenceCount(URI file, int referenceCount)
	{
		if (logger.isEntryEnabled()) logger.entry("setReferenceCount", file, referenceCount);
		if(file==null) return;
		if(!file.getScheme().equalsIgnoreCase(SCHEME)) return; // Only manage our own files.
		
		references.put(file, referenceCount);

		if (logger.isEntryEnabled()) logger.exit("setReferenceCount");
	}
	
	/**
	 * Indicate that the caller no longer needs the file. If nobody else needs 
	 * it either, it will be deleted. See the incrementReferenceCount method for
	 * details.
	 * @param file The file to count references for. Non-FileManager URIs can
	 * be passed, but they will be silently ignored and no references maintained
	 * for them; we wouldn't be able to delete them anyway.
	 */
	public static synchronized void decrementReferenceCount(URI file)
	{
		if (logger.isEntryEnabled()) logger.entry("decrementReferenceCount", file);
		if(file==null) return;
		
		if(!file.getScheme().equalsIgnoreCase(SCHEME)) return; // Only manage our own files.
		
		Integer count = references.get(file);
		if (count != null)
		{
			count--;
			references.put(file, count);

			if (logger.isDebugEnabled()) logger.debug("decrementReferenceCount()", "Reference count decreased to " + count + " for " + file);
			if (count < 1)
			{
				if (logger.isDebugEnabled()) logger.debug("decrementReferenceCount()", "No more references to " + file + "; deleting.");
				deleteIfTemporaryFile(file);
			}
		}
		
		if (logger.isEntryEnabled()) logger.exit("decrementReferenceCount");
	}
}
