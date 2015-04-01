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

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class CopyRepositoryTest
{
	private File beforeFile;
	private File afterFile;

	@Test
	public void testBasicXmlReplacement() throws Exception
	{
		writeBeforeFile(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
				"<device>\n" + 
				"	<domain name=\"foo\" something=\"bar\" file=\"/home/pete/june/july/august\"></domain>\n" + 
				"</device>"
				);
		
		new CopyRepository().copyXmlFileReplacingAttributeValues(beforeFile, afterFile, "/home/pete/", "/test/");
		
		assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><device>\n" + 
				"	<domain name=\"foo\" something=\"bar\" file=\"/test/june/july/august\"/>\n" + // <-- /home/pete has become /test
				"</device>\n", readAfterFile());
	}

	@Test
	public void testWindowsPathXmlReplacement() throws Exception
	{
		writeBeforeFile(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
				"<device>\n" + 
				"	<domain name=\"foo\" something=\"bar\" file=\"C:\\Program Files (x86)\\Common Files\\Services\\foo\"></domain>\n" + 
				"</device>"
				);
		
		new CopyRepository().copyXmlFileReplacingAttributeValues(beforeFile, afterFile, "C:\\Program Files (x86)\\Common Files", "C:\\IBM\\ITIM");

		assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><device>\n" + 
				"	<domain name=\"foo\" something=\"bar\" file=\"C:\\IBM\\ITIM\\Services\\foo\"/>\n" +
				"</device>\n", readAfterFile());
	}
	
	@Test
	public void testMultipleXmlReplacement() throws Exception
	{
		writeBeforeFile(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
				"<device>\n" + 
				"	<domain name=\"foo\" something=\"bar\" file=\"/home/pete/june/july/august\"></domain>\n" +
				"	<domain name=\"foo\" something=\"bar\" file=\"/home/pete/june/stuff\"></domain>\n" + 
				"	<domain name=\"foo\" something=\"bar\" file=\"/home/pete/june/other/stuff\"></domain>\n" + 
				"</device>"
				);
		
		new CopyRepository().copyXmlFileReplacingAttributeValues(beforeFile, afterFile, "/home/pete/", "/test/");
		
		assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><device>\n" + 
				"	<domain name=\"foo\" something=\"bar\" file=\"/test/june/july/august\"/>\n" +
				"	<domain name=\"foo\" something=\"bar\" file=\"/test/june/stuff\"/>\n" +
				"	<domain name=\"foo\" something=\"bar\" file=\"/test/june/other/stuff\"/>\n" +
				"</device>\n", readAfterFile());
	}
	
	@Test
	public void testFileCopy() throws IOException
	{
		// Write a medium sized file
		String oneKbOfText = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec vestibulum, felis ut semper venenatis, dolor mi mollis metus, eu imperdiet turpis tellus blandit sem. Nunc mattis leo sit amet justo luctus a convallis nunc faucibus. Aenean arcu justo, tincidunt non suscipit non, condimentum in elit. Ut adipiscing, erat sit amet pharetra malesuada, ligula erat ultricies mi, eu vestibulum eros erat ac sem. Aenean ut nisl at sem vulputate pulvinar nec vel felis. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia Curae; Vivamus est orci, viverra id sollicitudin in, fermentum ac elit. Aenean arcu neque, pulvinar id semper ut, consectetur quis urna. Etiam consectetur eros vitae eros molestie tempor. Suspendisse potenti. Phasellus et risus metus. Duis pretium venenatis sapien ut faucibus. Phasellus ut libero ipsum.\n" + 
						"Integer urna ante, luctus et lacinia nec, ultrices in lorem. Cras dictum vehicula vestibulum. Pellentesque lobortis pretium pretium. Sed pulvinar molestie lacus, in sodales amet.";
		FileWriter tenMegFileWriter = new FileWriter(beforeFile);
		String newline = System.getProperty("line.separator");
		for (int i = 0; i < 10*1024; i++) // 10Mb
		{
			tenMegFileWriter.append(oneKbOfText+newline);
		}
		tenMegFileWriter.close();
		
		
		// Copy it.
		new CopyRepository().copyFile(beforeFile, afterFile);
		
		
		// Assemble an in-memory version that should be the same, then compare them.
		String tenMegOfText = new String(new char[10*1024]).replace("\0", oneKbOfText+"\n"); // fugly oneliner equivalent of String.repeat
		
		assertEquals(tenMegOfText, readAfterFile());
	}
	
	@Before
	public void createTempFiles() throws IOException
	{
		afterFile = File.createTempFile("to_", ".xml");
		beforeFile = File.createTempFile("from_", ".xml");
	}
	
	@After
	public void deleteTempFiles()
	{
		beforeFile.delete();
		afterFile.delete();
	}

	private void writeBeforeFile(String fileContent) throws IOException
	{
		new FileWriter(beforeFile).append(fileContent).close();
	}

	private String readAfterFile() throws IOException
	{
		StringBuilder buffer = new StringBuilder();
		BufferedReader reader = new BufferedReader(new FileReader(afterFile));
		String line;
	    while( ( line = reader.readLine() ) != null ) {
	        buffer.append(line+"\n");
	    }
	    return buffer.toString();
	}
}
