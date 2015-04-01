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
package com.ibm.amc.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Various test utilities
 * 
 * @author mallman
 */
public class FileTools
{
	// @CLASS-COPYRIGHT@

	/**
	 * Load a file into a list of Strings
	 * 
	 * @param path location of the file to load
	 * @return a list of Strings, each String being a line in the file
	 * @throws FileNotFoundException if the file to load isn't available
	 */
	public static List<String> loadFileToList(String path) throws FileNotFoundException
	{	
		List<String> list = new ArrayList<String>();
		
		Scanner scanner = new Scanner(new File(path));
		
		while (scanner.hasNextLine())
		{
			list.add(scanner.nextLine());
		}

		return list;
	}
}
