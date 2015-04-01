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

import java.io.File;

import com.ibm.amc.Constants;
import com.ibm.amc.ras.Logger47;

/**
 * A common superclass for Housekeeper Services that delete old files. 
 * Subclasses should provide their own Schedule, and in their init methods
 * should set the fields to control what gets cleaned and when.
 */
public abstract class FileCleaningService implements Service
{
	// @CLASS-COPYRIGHT@


	static Logger47 logger = Logger47.get(FileCleaningService.class.getCanonicalName(), Constants.CWZBA_BUNDLE_NAME);
	
	// Set these fields in the subclass init method.
	/** The directory from which files should be deleted (including subdirectories) */
	File directoryToClean;
	/** The maximum age a file is allowed to grow to; files older than this are deleted. */
	Schedule maxFileAge;

	// Implement these methods
	public abstract Schedule getSchedule();
	public abstract void init();
	
	/** 
	 * Indicate that a file should not be deleted. Subclasses should optionally 
	 * override this if some files will be exempt from their processing. The
	 * default implementation exempts no files. 
	 */
	public boolean isExempt(File file)
	{
		return false;
	}

	// Don't implement your own execute method, let this one be used.
	public void execute()
	{
		long earliestPermittedFileTime = System.currentTimeMillis() - maxFileAge.unit.toMillis(maxFileAge.interval);
		
		purgeDirectory(directoryToClean, earliestPermittedFileTime);
	}
	
	/**
	 * Recurse through the directory tree, deleting empty directories and files
	 * older than the earliestPermittedFileTime
	 * @param currentdirectory The directory to start in
	 * @param earliestPermittedFileTime The minimum age, in milliseconds since 
	 * the epoch, that a file can be and survive. 
	 */
	private void purgeDirectory(File currentdirectory, long earliestPermittedFileTime)
	{
		File[] children = currentdirectory.listFiles();
		if(children == null || children.length == 0) 
		{
			delete(currentdirectory);
			return;
		}
		
		for (File child : children)
		{
			if(child.isDirectory()) purgeDirectory(child, earliestPermittedFileTime);
			if(child.lastModified() < earliestPermittedFileTime) delete(child);
		}
	}
	
	private void delete(File file)
	{
		if(isExempt(file))
		{
			if (logger.isDebugEnabled()) logger.debug("delete()", "Would have deleted "+file+", but keeping it because it's exempt.");
			return;
		}
		if(file.delete()) return;
		if (logger.isDebugEnabled()) logger.debug("purgeDirectory()", "Error deleting: ", file);		
	}
}
