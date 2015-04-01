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
package com.ibm.amc.housekeeping;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class FileCleaningServiceTest
{
	private static File tmpdir;
	private File testDir;

	@BeforeClass
	public static void findRoot() throws IOException
	{
		File probe = File.createTempFile(FileCleaningServiceTest.class.getSimpleName(), "");
		tmpdir = probe.getParentFile();
		probe.delete();
	}
	
	@Before
	public void createEmptyTestDir()
	{
		testDir = new File(tmpdir, this.getClass().getSimpleName());
		testDir.mkdir();
	}
	
	@After
	public void deleteTestDir()
	{
		delete(testDir);
	}
	
	
	@Test
	public void basicTest() throws InterruptedException, IOException
	{
		/* The timeouts in this test are uncomfortably long, but trying
		 * to squeeze them together reduces the tolerance for slower machines.
		 */

		FileCleaningService cleaner = new FileCleaningService(){
			public Schedule getSchedule(){return null;}
			public void init()
			{
				directoryToClean = testDir;
				maxFileAge = new Schedule(1800, TimeUnit.MILLISECONDS);
				
			}};
		cleaner.init();
			
		File.createTempFile("old_", null, testDir);
		File.createTempFile("old_", null, testDir);
		File.createTempFile("old_", null, testDir);
		Thread.sleep(2000); // Let the old files mature.
		File.createTempFile("new_", null, testDir);
		File.createTempFile("new_", null, testDir);

		cleaner.execute();
		
		// Max file age is 1800 millis, but the old ones are now at least 2000 
		// millis old. So they should be deleted. Provided we ran the cleaner
		// less than 1800 millis after creating the new files, they should have
		// survived. Check:
		
		String[] oldFiles = testDir.list(new FilenameFilter()
		{
			public boolean accept(File dir, String name)
			{
				return name.startsWith("old");
			}
		});
		assertEquals("Old files weren't deleted.", 0, oldFiles.length);
		
		String[] newFiles = testDir.list(new FilenameFilter()
		{
			public boolean accept(File dir, String name)
			{
				return name.startsWith("new");
			}
		});
		
		assertEquals("New files were deleted.", 2, newFiles.length);
	}
	
	@Test
	public void testExemptions() throws IOException
	{
		/* The timeouts in this test are uncomfortably long, but trying
		 * to squeeze them together reduces the tolerance for slower machines.
		 */

		FileCleaningService cleaner = new FileCleaningService(){
			public Schedule getSchedule(){return null;}
			public void init()
			{
				directoryToClean = testDir;
				
				// Everything will always be older than this, so deletion is 
				// purely down to exemption.
				maxFileAge = new Schedule(1, TimeUnit.MILLISECONDS);
			}
			public boolean isExempt(File file)
			{
				return file.getName().startsWith("E");
			}
		};
		cleaner.init();
			
		File.createTempFile("foo", null, testDir);
		File.createTempFile("E_bar", null, testDir);
		File.createTempFile("baz", null, testDir);
		File.createTempFile("E_quux", null, testDir);

		cleaner.execute();
		
		String[] normalFiles = testDir.list(new FilenameFilter()
		{
			public boolean accept(File dir, String name)
			{
				return !name.startsWith("E");
			}
		});
		assertEquals("Normal files weren't deleted.", 0, normalFiles.length);
		
		String[] exemptFiles = testDir.list(new FilenameFilter()
		{
			public boolean accept(File dir, String name)
			{
				return name.startsWith("E");
			}
		});
		
		assertEquals("Exempt files were deleted.", 2, exemptFiles.length);
	}
	
	/**
	 * Utility recursive-delete. 
	 */
	public void delete(File file)
	{
		File[] children = file.listFiles();
		if(children == null || children.length == 0) 
		{
			file.delete();
			return;
		}
		
		for (File child : children)
		{
			delete(child);
		}
	}
}
