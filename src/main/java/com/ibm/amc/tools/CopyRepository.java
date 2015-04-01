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
package com.ibm.amc.tools;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import com.ibm.amc.nls.NLS;

public class CopyRepository extends CommandlineTool implements ErrorListener
{
	public static void main(String[] args)
	{
		new CopyRepository().start(args);
	}
	
	/**
	 * Perform commandline handling and run the command. This method will invoke
	 * System.exit() so should not be called from within a wider Java process.
	 */
	@edu.umd.cs.findbugs.annotations.SuppressWarnings(value="DM_EXIT", 
			justification="Exiting the JVM is appropriate for a commandline tool. Call the run method if you don't want that.")
	private void start(String[] args)
	{
		Map<String, String> params = parseArgs(args);
		if(params.size() == 0 || params.get("--help") != null)
		{
			showHelp();
		}
		
		try
		{
			run(params);
		}
		catch (InvalidCmdArgsException e)
		{
			System.out.println(wordwrap(e.getMessage()));
			System.exit(2);
		}
		catch (IOException e)
		{
			System.out.println(wordwrap(e.getMessage()));
			System.exit(3);
		}
		
		System.out.println(wordwrap(NLS.formatMessageWithCode(null, "CWZBA2052I_SUCCESS")));
		System.exit(0);
	}

	/**
	 * Run the command. This method throws exceptions instead of exiting in the
	 * event of trouble, so may be called as part of other processing.
	 * @param params A map of command-line arguments, from names (eg "--toRepo")
	 * to values (eg "/test/new/WAMTRepository"). If an argument is to be set
	 * but has no value (eg the imaginary "--doItFaster") then the name should
	 * map to CommandLineTool.NO_PARAMETER_VALUE. 
	 * @throws InvalidCmdArgsException if there is something invalid about the
	 * arguments provided (exception message will reveal what was wrong).
	 * @throws IOException if an error occurs while copying. 
	 */
	private void run(Map<String, String> params) throws InvalidCmdArgsException, IOException
	{
		// Check that inputs are valid, as much as possible.
		File sourceRepository = validateSourceRepo(params.get("--fromRepo"));
		File destinationRepository = validateDestinationRepo(params.get("--toRepo"));
		checkSpace(sourceRepository, destinationRepository);
		
		// Copy files which don't need modifying, and find those that do.
		List<File> specialFiles = copyNormalFiles(sourceRepository, destinationRepository, ".+\\.xml$");

		// Copy files that need on-the-fly modification.
		String fromPrefix = sourceRepository.toURI().toString();
		String toPrefix = destinationRepository.toURI().toString();
		for (File xmlFile : specialFiles)
		{
			File destinationFile = new File(xmlFile.getCanonicalPath().replace(
					sourceRepository.getCanonicalPath(), 
					destinationRepository.getCanonicalPath()));
			copyXmlFileReplacingAttributeValues(xmlFile, destinationFile, fromPrefix, toPrefix);
		}
	}

	private void checkSpace(File sourceRepository, File destinationRepository) throws InvalidCmdArgsException
	{
		long availableBytes = destinationRepository.getUsableSpace();
		long requiredBytes = computeDirectorySize(sourceRepository);
		
		if(availableBytes < requiredBytes)
		{
			throw new InvalidCmdArgsException("CWZBA2053E_INSUFFICIENT_DEST_SPACE",
					destinationRepository.getPath(),
					requiredBytes+"", forHumans(requiredBytes),
					availableBytes+"", forHumans(availableBytes));
		}
	}

	/**
	 * Format a file size in bytes using "K", "M" or "G" so that non-AIX-using 
	 * human beings can read them comfortably.
	 */
	private static String forHumans(long bytes)
	{
		DecimalFormat format = new DecimalFormat("0.0");
		int k = 1024;
		int m = 1024 * k;
		int g = 1024 * m;
		
		
		if(bytes >= g)
		{
			return format.format(((double)bytes / g))+"G";
		}
		if(bytes >= 1024*1024)
		{
			return format.format(((double)bytes / m))+"M";
		}
		if(bytes >= 1024)
		{
			return format.format(((double)bytes / k))+"K";
		}
		return bytes+"bytes";
	}

	/**
	 * Recursively copy directory contents from one place to another, omitting
	 * any files whose names identify them as "special".
	 * @param sourceDirectory The 
	 * @param destinationDirectory
	 * @param specialFilenamePattern A regex; any files whose simple names (not 
	 * paths) match this are shunted into the special list instead of being 
	 * copied.
	 * @return a list of Files that weren't copied because they are special.
	 * @throws IOException if a copy fails at any point. 
	 */
	private List<File> copyNormalFiles(File sourceDirectory, File destinationDirectory, String specialFilenamePattern) throws IOException
	{
		if(!sourceDirectory.isDirectory() || !destinationDirectory.isDirectory()) throw new IllegalArgumentException();
		
		List<File> specialFiles = new ArrayList<File>();
		File[] children = sourceDirectory.listFiles();
		
		for (File child : children)
		{
			// The equivalent (not yet extant) file or directory on the other side
			File destinationChild = new File(destinationDirectory, child.getName());

			if(child.isDirectory()) // Create it, then copy its contents
			{
				if(!destinationChild.mkdir()) throw new IOException(NLS.formatMessageWithCode(null, "CWZBA2054E_CREATE_DIR_FAILED", destinationChild.getAbsolutePath())); 
				specialFiles.addAll(copyNormalFiles(child, destinationChild, specialFilenamePattern));
			}
			
			if(child.isFile()) // Copy it, unless it's special.
			{
				if(child.getName().matches(specialFilenamePattern))
				{
					specialFiles.add(child);
				}
				else
				{
					copyFile(child, destinationChild);
				}
			}
		}
		return specialFiles;
	}

	/**
	 * Check that a given directory path is valid, and either does not exist or
	 * does not contain any children. If it does not exist, it is created 
	 * (including parents if required).
	 * @throws InvalidCmdArgsException if given path is not usable.
	 */
	@edu.umd.cs.findbugs.annotations.SuppressWarnings(value={"OS_OPEN_STREAM", "OBL_UNSATISFIED_OBLIGATION"}, 
			justification="False positive; findbugs doesn't realise that the return from FileWriter.append is the same Writer.")
	private File validateDestinationRepo(String destinationRepositoryPath) throws InvalidCmdArgsException
	{
		if(destinationRepositoryPath == null) throw new InvalidCmdArgsException("CWZBA2055E_DEST_REPO_NOT_SPECIFIED");
		
		File destinationRepository = new File(destinationRepositoryPath);
		
		if(!destinationRepository.isAbsolute()){ throw new InvalidCmdArgsException("CWZBA2056E_DEST_REPO_NOT_ABSOLUTE", destinationRepositoryPath);}
		
		try
		{
			destinationRepository = destinationRepository.getCanonicalFile();
		}
		catch (IOException e)
		{
			throw new InvalidCmdArgsException("CWZBA2057E_DEST_REPO_NOT_VALID", destinationRepository.getPath(), 
					e.getClass().getSimpleName()+": "+e.getLocalizedMessage());
		}
		
		if(!destinationRepository.exists()) 
		{
			if(!destinationRepository.mkdirs()) throw new InvalidCmdArgsException("CWZBA2058E_CREATE_DEST_FAILED", destinationRepository.getPath());
		}

		if(!destinationRepository.isDirectory()) throw new InvalidCmdArgsException("CWZBA2059E_DEST_REPO_NOT_DIR", destinationRepository.getPath());
		
		String[] childFiles = destinationRepository.list();
		
		// File.list() returns null if not a directory (already accounted for above) or if "an I/O error occurred".
		// Sadly no way of knowing what actually went wrong...
		if(childFiles == null) throw new InvalidCmdArgsException("CWZBA2061E_DEST_REPO_NOT_ACCESSIBLE", destinationRepository.getPath());
		
		if(childFiles.length != 0) throw new InvalidCmdArgsException("CWZBA2059E_DEST_REPO_NOT_EMPTY", destinationRepository.getPath());
		
		try
		{
			// Write (then remove) a test file to ensure we can write to this directory.
			File testFile = new File(destinationRepository, "testFile");
			FileWriter writer = new FileWriter(testFile);
			try {
				writer.append("File writing test");
			}
			finally {
				writer.close();
			}
			if(!testFile.delete()) throw new InvalidCmdArgsException("CWZBA2062E_DEST_DELETE_FAILED", testFile.getAbsolutePath());
		}
		catch (IOException e)
		{
			throw new InvalidCmdArgsException("CWZBA2063E_DEST_WRITE_FAILED", 
					destinationRepository.getPath(),
					e.getClass().getSimpleName()+": "+e.getLocalizedMessage());
		}
		
		return destinationRepository;
	}

	/**
	 * Check that a given directory path is valid and appears to contain a
	 * WAMT repository.
	 * @return 
	 * @throws InvalidCmdArgsException if given path is not usable.
	 * @throws IOException 
	 */
	@edu.umd.cs.findbugs.annotations.SuppressWarnings(value="RR_NOT_CHECKED", 
			justification="Check for bytes read is inherent in the test we're already doing.")
	private File validateSourceRepo(String sourceRepositoryPath) throws InvalidCmdArgsException, IOException
	{
		if(sourceRepositoryPath == null) throw new InvalidCmdArgsException("CWZBA2064E_SRC_REPO_NOT_SPECIFIED");
		
		File sourceRepository = new File(sourceRepositoryPath);
		
		if(!sourceRepository.isAbsolute()){ throw new InvalidCmdArgsException("CWZBA2065E_SRC_REPO_NOT_ABSOLUTE", sourceRepositoryPath);}
		
		try
		{
			sourceRepository = sourceRepository.getCanonicalFile();
		}
		catch (IOException e)
		{
			throw new InvalidCmdArgsException("CWZBA2066E_SRC_REPO_NOT_VALID", 
					sourceRepository.getPath(),
					e.getClass().getSimpleName()+": "+e.getLocalizedMessage());
		}

		if(!sourceRepository.isDirectory()) 
		{
			// It seems that on Windows, if you don't have read permission then
			// you can't even tell if it's a directory. Disambiguate here.
			if(System.getProperty("os.name").startsWith("Windows") && !sourceRepository.canRead())
			{
				throw new InvalidCmdArgsException("CWZBA2069E_SRC_REPO_NOT_READABLE", sourceRepository.getPath());
			}
			throw new InvalidCmdArgsException("CWZBA2067E_SRC_REPO_NOT_DIR", sourceRepository.getPath());
		}
		
		String[] childFiles = sourceRepository.list();
		
		// File.list() returns null if not a directory (already accounted for above) or if "an I/O error occurred".
		// Sadly no way of knowing what actually went wrong...
		if(childFiles == null) throw new InvalidCmdArgsException("CWZBA2068E_SRC_REPO_NOT_ACCESSIBLE", sourceRepository.getPath());
		
		String infoFileName = "WAMT.repository.xml";
		File infoFile = new File(sourceRepository, infoFileName);
		FileReader reader = null;
		try
		{
			String firstFewChars = "<?xml";
			char[] buffer = new char[firstFewChars.length()];
			reader = new FileReader(infoFile);
			reader.read(buffer);
			if(!new String(buffer).equals(firstFewChars)) throw new InvalidCmdArgsException("CWZBA2070E_INFO_FILE_NO_MATCH", infoFile.getAbsolutePath());
		}
		catch (FileNotFoundException e)
		{
			throw new InvalidCmdArgsException("CWZBA2071E_INFO_FILE_NOT_FOUND", infoFileName, sourceRepository.getPath());
		}
		catch (IOException e)
		{
			throw new InvalidCmdArgsException("CWZBA2072E_INFO_FILE_READ_FAILED", 
					infoFile.getAbsolutePath(),
					e.getClass().getSimpleName()+": "+e.getLocalizedMessage());
		}
		finally
		{
			if(reader != null) reader.close();
		}
		
		return sourceRepository;
	}

	private long computeDirectorySize(File fileOrDirectory)
	{
		if(fileOrDirectory.isFile()) return fileOrDirectory.length();
		
		File[] children = fileOrDirectory.listFiles();
		long size = 0L;
		for (File child : children)
		{
			size += computeDirectorySize(child);
		}
		return size;
	}

	/**
	 * Copy a file.
	 * @param sourceFile 
	 * @param destinationFile
	 * @throws IOException
	 */
	@edu.umd.cs.findbugs.annotations.SuppressWarnings(value="OBL_UNSATISFIED_OBLIGATION", 
			justification="Findbugs doesn't know that the NIO close method also closes the parent stream")
	protected void copyFile(File sourceFile, File destinationFile) throws IOException
	{
		try
		{
			// I found some tests suggesting that old-IO buffers are faster for
			// small files, but NIO is faster for big ones. We're most interested in
			// slinging big fat firmwares around, so NIO it is.
			FileInputStream fis = new FileInputStream(sourceFile);
			FileOutputStream fos = new FileOutputStream(destinationFile);
			FileChannel in = fis.getChannel();
			FileChannel out = fos.getChannel();
			try {
				ByteBuffer buffer = ByteBuffer.allocate(512 * 1024);

				while(true) // escape using break.
				{
					// Fill the buffer, or escape if there are no bytes left to read.
					if(in.read(buffer) == -1) break;

					// Get the buffer ready to write.
					buffer.flip();

					// Write, and get ready for next read.
					out.write(buffer);
					buffer.clear();
				}
			}
			finally {
				fis.close();
				fos.close();
				in.close();
				out.close();
			}
		} 
		catch(IOException e)
		{
			// Make sure error messages include which file was being copied.
			throw new IOException(NLS.formatMessageWithCode(null, "CWZBA2073E_FILE_COPY_ERROR", 
					sourceFile.getAbsolutePath(), destinationFile.getAbsolutePath(),
					e.getClass().getSimpleName()+": "+e.getLocalizedMessage()));
		}
	}
	
	/**
	 * Copy an XML file from one place to another, and modify some attribute
	 * values while copying.
	 * @param sourceFile The file to copy.
	 * @param destinationFile The path and filename for the new copy.
	 * @param fromPrefix The string to search for in attribute values. This will
	 * be found in any attribute, as long as it starts at the beginning of the 
	 * attribute value (ie, is a prefix).
	 * @param toPrefix The new value, which fromPrefix will be replaced with.
	 * @throws FileNotFoundException
	 */
	void copyXmlFileReplacingAttributeValues(File sourceFile, File destinationFile, String fromPrefix, String toPrefix) throws FileNotFoundException
	{
		Source inputXml = new StreamSource(new BufferedInputStream(new FileInputStream(sourceFile), 16000));
		Result outputXml = new StreamResult(destinationFile);
		
		Source inputXslt = new StreamSource(new StringReader(
				// XSLT boilerplate, and copy everything unchanged unless otherwise noted
				"<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" + 
				"<xsl:transform version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">\n\n"+
				"<xsl:template match=\"@*|node()\">\n" + 
				"  <xsl:copy>\n" + 
				"    <xsl:apply-templates select=\"@*|node()\"/>\n" + 
				"  </xsl:copy>\n" + 
				"</xsl:template>\n" +
				
				// Interesting bit. Match any attributes whose values start with the old path...
				"<xsl:template match=\"@*[starts-with(., '"+fromPrefix+"')]\">\n" + 
				// ...copy them with name unchanged...
				"  <xsl:attribute name=\"{name()}\">\n" +
				// ...but value replaced with a string that has new path in place of old path.
				"    <xsl:value-of select=\"concat('"+toPrefix+"', substring-after(., '"+fromPrefix+"'))\"/>\n"+
				"  </xsl:attribute>\n" + 
				
				// Closing boilerplate
				"</xsl:template>\n" +
				"</xsl:transform>\n"	
			));

		try
		{
			TransformerFactory factory = TransformerFactory.newInstance();
			factory.setErrorListener(this);
			Transformer transformer = factory.newTransformer(inputXslt);
			transformer.setErrorListener(this);
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.transform(inputXml, outputXml);
		}
		catch (TransformerConfigurationException e)
		{
			handleXmlError(e);
		}
		catch (TransformerFactoryConfigurationError e)
		{
			handleXmlError(e);
		}
		catch (TransformerException e)
		{
			handleXmlError(e);
		}
			
	}

	@Override
	protected String getHelpMessage()
	{
		return NLS.formatMessageWithoutCode(null, "CWZBA2051I_COPY_REPO_HELP");
	}

	private void handleXmlError(Throwable t)
	{
		//TODO better handling of XML errors
		t.printStackTrace();
	}
	
	@Override
	public void error(TransformerException e) throws TransformerException
	{
		handleXmlError(e);
	}

	@Override
	public void fatalError(TransformerException e) throws TransformerException
	{
		handleXmlError(e);		
	}

	@Override
	public void warning(TransformerException e) throws TransformerException
	{
		handleXmlError(e);
	}

}
