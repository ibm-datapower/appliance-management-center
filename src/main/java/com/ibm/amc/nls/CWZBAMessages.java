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
// @start_prolog@
// @end_prolog@

// ------------------------------------------------------------------------------------------------
//
// Important information - do not remove or modify information in this block
//
// Messages in this file will be formatted as follows to enable translation and problem determination
//
//    // ----------------------------------------------------------------------------------------
//    // {0} description of String insert field 0
//    // {1} description of String insert field 1
//    // ...etc
//	  { "pppppnnnnX_MSG_DESCRIPTIVE_NAME",             "This is a message with String inserts {0} and {1}." },
//	  { "pppppnnnnX_MSG_DESCRIPTIVE_NAME.explanation", "Explanation text for the message " +
//	                                                   "which may be split across several lines." },
//	  { "pppppnnnnX_MSG_DESCRIPTIVE_NAME.useraction",  "User action text for the message." },
//
// where pppppnnnn is a unique message identifier. ppppp is the component identifier and nnnn is a unique
// number within the component identifier name space. X is the message severity being one of:
//
//    I - Informational
//    A - Audit
//    W - Warning
//    E - Error
//
// Inserts can not be used in the explanation or useraction messages.
//
// Instructions to translators will precede the line to which it applies. Messages that do 
// not require translation should be wrapped with the following tags:
//    
//    // START NON-TRANSLATABLE 
//    // END NON-TRANSLATABLE
//
// Some of the messages in this file are processed as "java.text.MessageFormat" patterns that
// use the apostrophe "'" and open brace "{" as special characters.
//
// If you are a translator use the IBMJDK21 markup table to translate this file, type apostrophe characters
// as you wish them to appear to the user. The translation tools will automatically handle whether one or two
// apostrophes characters are required when the translated file is exported. See the following page for more
// details:
//
//    http://w3-03.ibm.com/globalization/page/2089#IBMJDK21
//
// If you are a developer working with this file in a text editor, use the following rules for apostrophe
// characters:
//
//  a) If there are inserts in the message, for example "{0}", then type two apostrophe 
//     characters, that is "''".
//  b) If there are no inserts in the message, then just type a single apostrophe.
//  c) When using a double quote character """, it needs to be escaped with a \ in .java 
//     files.
//
// Examples:
//
//  a) "UDUT0036E_message_xml_parserError_prefix", "Erreur de l''analyseur syntaxique : {0}" 
//  b) "UDUT0078E_error_getAuthInfoFailed", "Impossible d'obtenir authinfo."
//  c) "UDUT0099E_INSTALL_PATH", "The installation path is \"{0}\"."
//
// ------------------------------------------------------------------------------------------------
// NLS_ENCODING=UTF-8
// NLS_MESSAGEFORMAT_VAR
// FILEPATHNAME wamc/src/com/ibm/amc/nls/CWZBAMessages.java
// COMPONENTPREFIX CWZBA
// ISMESSAGEFILE TRUE
// ISMESSAGEFILE_SHIPPED TRUE
// ------------------------------------------------------------------------------------------------
// Messages CWZBA0001 - CWZBA0500 allocated to WAMT ApplianceManager implementation
// Messages CWZBA0501 - CWZBA1000 allocated to AMC Controller/ApplianceManager interfaces
// Messages CWZBA1001 - CWZBA1500 allocated to actions
// Messages CWZBA1501 - CWZBA2000 allocated to ReST resources
// Messages CWZBA2001 - CWZBA2050 allocated to file management
// Messages CWZBA2051 - CWZBA2250 allocated to command-line tools
// Messages CWZBA2251 - CWZBA2300 allocated to server framework
// ------------------------------------------------------------------------------------------------
package com.ibm.amc.nls;

import java.util.ListResourceBundle;

public class CWZBAMessages extends ListResourceBundle
{
	// @CLASS-COPYRIGHT@

	@Override
	protected Object[][] getContents()
	{
		return contents;
	}

	private static final Object[][] contents =
	{
		// ========================================================================================
		// Messages CWZBA0001 - CWZBA0500 allocated to WAMT ApplianceManager implementation
		// ========================================================================================
		// {0} the path of the repository
		{ "CWZBA0001I_WAMT_REPO_PATH",								"The Appliance Management Center repository path is ''{0}''." },
		{ "CWZBA0001I_WAMT_REPO_PATH.explanation",					"The location of the Appliance Management Center repository is " +
																	"provided in the message." },
		{ "CWZBA0001I_WAMT_REPO_PATH.useraction",					"No user action is required." },
		
		// ----------------------------------------------------------------------------------------
		// {0} exception message
		{ "CWZBA0002I_WAMT_ERROR_ADD_APPLIANCE",             		"An error occurred while trying to add an appliance. The message is ''{0}''." },
		{ "CWZBA0002I_WAMT_ERROR_ADD_APPLIANCE.explanation",		"The subsystem that is used for communicating with appliances reported an " +
																	"error." },
		{ "CWZBA0002I_WAMT_ERROR_ADD_APPLIANCE.useraction",  		"Review the error message." },
		
		// ----------------------------------------------------------------------------------------
		// {0} the name of the environment variable, eg WAMT_REPO
		{ "CWZBA0003I_WAMT_REPO_PATH_FROM_ENV",             		"The repository path is being loaded from the environment variable ''{0}''." },
		{ "CWZBA0003I_WAMT_REPO_PATH_FROM_ENV.explanation",			"The location of the Appliance Management Center repository is " +
																	"normally specified in the property file for the application, but for " +
																	"problem determination it can be specified by an environment variable " +
																	"instead. The environment variable is being used instead of the property " +
																	"file." },
		{ "CWZBA0003I_WAMT_REPO_PATH_FROM_ENV.useraction",  		"No user action is required." },
		
		// ----------------------------------------------------------------------------------------
		// {0} is the name of an exception (eg FileNotFoundException)
		// {1} is the already-translated message from that exception describing the details of the problem.
		{ "CWZBA0004E_WAMC_PROPERTIES_NOT_LOADED",					"The property file for the application cannot be loaded. The reported error " +
																	"is ''{0}: {1}''." },
		{ "CWZBA0004E_WAMC_PROPERTIES_NOT_LOADED.explanation",		"Configuration properties for Appliance Management Center are " +
																	"loaded from a property file. An error occurred while trying to locate or " +
																	"read the property file." },
		{ "CWZBA0004E_WAMC_PROPERTIES_NOT_LOADED.useraction",		"Review the reported error message and correct the problem it describes. " +
																	"Contact IBM Support for further assistance." },

		// ----------------------------------------------------------------------------------------
		// {0} is a message key
		{ "CWZBA0005W_MESSAGE_KEY_NOT_FOUND",						"The message for key {0} cannot be found." },
		{ "CWZBA0005W_MESSAGE_KEY_NOT_FOUND.explanation",			"Messages are represented by keys to enable translation into multiple " +
																	"languages. A key was used for which no corresponding message can be " +
																	"found. The message is displayed in an alternative form, consisting " +
																	"of the key (for example, CWZBA0042E_ERROR_RETICULATING_SPLINES) and " +
																	"any associated data." },
		{ "CWZBA0005W_MESSAGE_KEY_NOT_FOUND.useraction",			"Contact IBM Support to report this problem." },

		// ----------------------------------------------------------------------------------------
		// {0} is the name of an exception (eg FileNotFoundException)
		// {1} is the already-translated message from that exception describing the details of the problem.
		{ "CWZBA0006E_RESOURCE_NOT_LOADED",							"The resource file cannot be loaded. The reported error is ''{0}: {1}''." },
		{ "CWZBA0006E_RESOURCE_NOT_LOADED.explanation",				"Resources are loaded from files. An error occurred while trying to locate or " +
																	"read the resource file." },
		{ "CWZBA0006E_RESOURCE_NOT_LOADED.useraction",				"Review the reported error message and correct the problem it describes. " +
																	"Contact IBM Support for further assistance." },
		
																	
																	
		// ========================================================================================
		// Messages CWZBA0501 - CWZBA1000 allocated to AMC Controller/ApplianceManager interfaces
		// ========================================================================================
		// {0} the error message from WAMT
		{ "CWZBA0501E_WAMT_GENERAL_WAMT_ERROR",						"The subsystem that is used for communicating with appliances reported an " +
																	"error. The message is ''{0}''." },
		{ "CWZBA0501E_WAMT_GENERAL_WAMT_ERROR.explanation",			"An error occurred while attempting to communicate with an appliance." },
		{ "CWZBA0501E_WAMT_GENERAL_WAMT_ERROR.useraction",			"Review the error message." },

		// ----------------------------------------------------------------------------------------
		// {0} the identifier for the resource that could not be found
		{ "CWZBA0502E_WAMT_NO_SUCH_RESOURCE",						"The resource with identifier ''{0}'' was not found." },
		{ "CWZBA0502E_WAMT_NO_SUCH_RESOURCE.explanation",			"The requested resource was not found." },
		{ "CWZBA0502E_WAMT_NO_SUCH_RESOURCE.useraction",			"Ensure that the correct resource identifier was specified." },

		// ----------------------------------------------------------------------------------------
		// {0} the error message received on trying to add an appliance
		{ "CWZBA0503E_ERROR_ADD_APPLIANCE",							"The appliance was not added successfully. The message is ''{0}''." },
		{ "CWZBA0503E_ERROR_ADD_APPLIANCE.explanation",				"An error occurred while attempting to add a appliance." },
		{ "CWZBA0503E_ERROR_ADD_APPLIANCE.useraction",				"Review the error message." },

		// ----------------------------------------------------------------------------------------
		// {0} the error message received on performing the operation
		{ "CWZBA0504E_UNEXPECTED_ERROR",							"The operation failed unexpectedly. The message is ''{0}''." },
		{ "CWZBA0504E_UNEXPECTED_ERROR.explanation",				"An error occurred while attempting to perform the operation." },
		{ "CWZBA0504E_UNEXPECTED_ERROR.useraction",					"Review the error message." },

		// ----------------------------------------------------------------------------------------
		// {0} the name of the property with the invalid data
		{ "CWZBA0505E_ERROR_INVALID_DATA",							"The supplied property ''{0}'' contains invalid data." },
		{ "CWZBA0505E_ERROR_INVALID_DATA.explanation",				"The data supplied for the named property is not valid." },
		{ "CWZBA0505E_ERROR_INVALID_DATA.useraction",				"Check the data supplied in the named property for validity." },

		// ----------------------------------------------------------------------------------------
		// {0} the name of the unrecognized property
		{ "CWZBA0506E_UNRECOGNIZED_PROPERTY",						"The supplied property ''{0}'' is not recognized for this type of resource." },
		{ "CWZBA0506E_UNRECOGNIZED_PROPERTY.explanation",			"The resource provided with the request contained a property that is not " +
																	"recognized." },
		{ "CWZBA0506E_UNRECOGNIZED_PROPERTY.useraction",			"Check the format of the supplied resource and either remove this property " +
																	"or correct its name." },

		// ----------------------------------------------------------------------------------------
		{ "CWZBA0507E_INVALID_APPLIANCE_CREDENTIALS",				"The user details supplied with the request are not valid for the specified " +
																	"appliance." },
		{ "CWZBA0507E_INVALID_APPLIANCE_CREDENTIALS.explanation",	"The appliance rejected the user details specified with the request." },
		{ "CWZBA0507E_INVALID_APPLIANCE_CREDENTIALS.useraction",	"Ensure that the user details given are correct for the specified appliance." },

		// ----------------------------------------------------------------------------------------
		// {0} the name given for the appliance
		// {1} the host name or IP address of the appliance
		{ "CWZBA0508E_APPLIANCE_ALREADY_EXISTS",					"The name ''{0}'' is already in use or the appliance {1} is already " +
																	"added to Appliance Management Center." },
		{ "CWZBA0508E_APPLIANCE_ALREADY_EXISTS.explanation",		"Either the name specified for the appliance is already in use or the " +
																	"connection details specified in the request are for an appliance that is " +
																	"already added to Appliance Management Center." },
		{ "CWZBA0508E_APPLIANCE_ALREADY_EXISTS.useraction",			"See the linked exception to determine the cause." },

		// ----------------------------------------------------------------------------------------
		// {0} the host name or IP address of the appliance
		// {1} the AMP port number of the appliance
		{ "CWZBA0509E_APPLIANCE_CONNECTION_ERROR",					"Failed to connect to the appliance {0} on port {1}." },
		{ "CWZBA0509E_APPLIANCE_CONNECTION_ERROR.explanation",		"A failure occurred when trying to connect by using the Appliance Management " +
																	"Protocol that is using the given host and port." },
		{ "CWZBA0509E_APPLIANCE_CONNECTION_ERROR.useraction",		"Ensure that the host and port are correct, that the Appliance Management " +
																	"Protocol is enabled on the XML Management Interface, and that the appliance " +
																	"is available." },
		
		// ----------------------------------------------------------------------------------------
		{ "CWZBA0510E_CONFIG_PROPERTY_PROBLEM",             		"The configuration properties for the application are incorrect. Check the " +
																	"following messages for details." },
		{ "CWZBA0510E_CONFIG_PROPERTY_PROBLEM.explanation", 		"Configuration properties for the Appliance Management Center " +
																	"server are loaded from a property file. The values of these properties are " +
																	"checked when the server starts, and any problems are reported." },
		{ "CWZBA0510E_CONFIG_PROPERTY_PROBLEM.useraction",  		"Review the following messages to correct each of the problems." },
		
		// ----------------------------------------------------------------------------------------
		// {0} is the name of the missing property.
		{ "CWZBA0511E_CONFIG_PROPERTY_MISSING",						"''{0}'' is not set. It is a required property." },
		{ "CWZBA0511E_CONFIG_PROPERTY_MISSING.explanation",			"Configuration properties for the Appliance Management Center " +
																	"server are loaded from a property file. Some of these properties are " +
																	"mandatory, and the server does not start if one or more mandatory " +
																	"properties are missing." },																	
		{ "CWZBA0511E_CONFIG_PROPERTY_MISSING.useraction",			"Ensure that this property is included in the properties file. Consult the " +
																	"product documentation to find out the correct value." },
				
		// ----------------------------------------------------------------------------------------
		// {0} is the name of the problem property.
		{ "CWZBA0512E_CONFIG_PROPERTY_NOT_INTEGER",					"''{0}'' is invalid. It must be an integer." },
		{ "CWZBA0512E_CONFIG_PROPERTY_NOT_INTEGER.explanation",		"The property is set, but it must be a number and the value that it " +
																	"currently holds is not numeric." },
		{ "CWZBA0512E_CONFIG_PROPERTY_NOT_INTEGER.useraction",		"Ensure that this property is correctly set in the properties file. Consult " +
																	"the product documentation to find out the correct value." },
		
		// ----------------------------------------------------------------------------------------
		// {0} is the name of the problem property.
		{ "CWZBA0513E_CONFIG_PROPERTY_NOT_BOOLEAN",					"''{0}'' is invalid. It must be ''true'' or ''false''." },
		{ "CWZBA0513E_CONFIG_PROPERTY_NOT_BOOLEAN.explanation",		"The property is set, but it must be Boolean (represented by the " +
																	"case-insensitive strings ''true'' and ''false'') and the value that it " +
																	"currently holds is not ''true'' or ''false''." },
		{ "CWZBA0513E_CONFIG_PROPERTY_NOT_BOOLEAN.useraction",		"Ensure that this property is correctly set in the properties file. Consult " +
																	"the product documentation to find out the correct value." },		
				
		// ----------------------------------------------------------------------------------------
		// {0} is the name of the problem property.
		// {1} is the name of a Java type, for example java.lang.Double
		{ "CWZBA0514E_UNKNOWN_CONFIG_PROPERTY_TYPE",				"The Appliance Management Center property {0} has the type {1}, " +
																	"which is not known to WamcProperties. Handling for this new type must be " +
																	"added to the WamcProperties.invoke method." },
		{ "CWZBA0514E_UNKNOWN_CONFIG_PROPERTY_TYPE.explanation",	"A defect is detected in Appliance Management Center." },
		{ "CWZBA0514E_UNKNOWN_CONFIG_PROPERTY_TYPE.useraction",		"Contact IBM Support." },	
				
		// ----------------------------------------------------------------------------------------
		// {0} is the name of an object which was being worked with.
		{ "CWZBA0515E_CONCURRENCY_WITH_RESOURCE",					"The operation on ''{0}'' cannot complete because resources are already in " +
																	"use. For more details, expand ''Additional technical information''." },
		{ "CWZBA0515E_CONCURRENCY_WITH_RESOURCE.explanation",		"Appliance Management Center can be used by multiple users " +
																	"simultaneously. Normally the actions of different users do not affect each " +
																	"other, but, in some circumstances, resource locks can coincide." },
		{ "CWZBA0515E_CONCURRENCY_WITH_RESOURCE.useraction",  		"Try the operation again. If the problem persists, stop and then restart " +
																	"the Appliance Management Center server program. If the problem " +
																	"still persists, contact IBM Support." },	
				
		// ----------------------------------------------------------------------------------------
		{ "CWZBA0516E_CONCURRENCY_WITHOUT_RESOURCE",				"The operation cannot complete because resources are already in use. For " +
																	"more details, expand ''Additional technical information''." },
		{ "CWZBA0516E_CONCURRENCY_WITHOUT_RESOURCE.explanation",	"Appliance Management Center can be used by multiple users " +
																	"simultaneously. Normally the actions of different users do not affect " +
																	"each other, but, in some circumstances, resource locks can coincide." },
		{ "CWZBA0516E_CONCURRENCY_WITHOUT_RESOURCE.useraction",		"Try the operation again. If the problem persists, stop and then restart " +
																	"the Appliance Management Center server program. If the problem " +
																	"still persists, contact IBM Support." },			
				
		// ----------------------------------------------------------------------------------------
		// {0} is a directory name.
		{ "CWZBA0517I_EXTRA_WAMT_PROPERTIES",						"Additional properties for Appliance Management Toolkit are being " +
																	"loaded from WAMT.properties in ''{0}''." },
		{ "CWZBA0517I_EXTRA_WAMT_PROPERTIES.explanation",			"Appliance Management Center uses Appliance Management " +
																	"Toolkit to control appliances. Normally, Appliance Management Center " +
																	"completes all the Appliance Management Toolkit configuration that is " +
																	"required, but for problem determination purposes you can set additional " +
																	"configuration properties manually." },
		{ "CWZBA0517I_EXTRA_WAMT_PROPERTIES.useraction",			"If you expect the server to use extra properties, no action is required. If " +
																	"you do not know what extra properties are being set, read the file named in " +
																	"the message. To stop using extra properties, remove the property " +
																	"''com.ibm.amc.wamtConfigFilePath'' from the Appliance Management " +
																	"Center configuration file at " +
																	"<install_dir>/config/wamc.properties." },	
				
		// ----------------------------------------------------------------------------------------
		{ "CWZBA0518I_DEMO_MODE",									"Appliance Management Center is running in demonstration mode." },
		{ "CWZBA0518I_DEMO_MODE.explanation",						"Appliance Management Center is running in a mode provided for " +
																	"demonstration purposes only, connections are not created to real appliances." },
		{ "CWZBA0518I_DEMO_MODE.useraction",						"If you do not want to run in demonstration mode, remove the property " +
																	"''com.ibm.amc.demoMode'' from the Appliance Management Center " +
																	"configuration file at " +
																	"<install_dir>/config/wamc.properties." },
																	
		// ----------------------------------------------------------------------------------------
		// {0} the name of the first property
		// {1} the name of the second property
		{ "CWZBA0519E_ONE_AND_ONLY_ONE_PROPERTY",					"The resource must specify either the property ''{0}'' or the property ''{1}''." },
		{ "CWZBA0519E_ONE_AND_ONLY_ONE_PROPERTY.explanation",		"The resource must have only one of the two named properties." },
		{ "CWZBA0519E_ONE_AND_ONLY_ONE_PROPERTY.useraction",		"Check that the resource has one of the two properties." },	
		
		// ----------------------------------------------------------------------------------------
		// {0} the symbolic name of the appliance
		// {1} the appliance firmware level
		// {2} the required firmware level
		{ "CWZBA0520E_BACKUP_NOT_SUPPORTED",						"The appliance ''{0}'' has firmware level {1}, which does not meet the " +
																	"minimum level of {2} required for secure backup." },
		{ "CWZBA0520E_BACKUP_NOT_SUPPORTED.explanation",			"Secure backup is supported only for appliances running a particular " +
																	"firmware level or higher." },
		{ "CWZBA0520E_BACKUP_NOT_SUPPORTED.useraction",				"Deploy firmware to the appliance of at least the minimum required level " +
																	"and then perform the backup." },
		
		// ----------------------------------------------------------------------------------------
		// {0} the symbolic name of the appliance
		// {1} the appliance firmware level
		// {2} the required firmware level
		{ "CWZBA0521E_RESTORE_NOT_SUPPORTED",						"The appliance ''{0}'' has firmware level {1}, which does not meet the " +
																	"minimum level of {2} required for secure restore." },
		{ "CWZBA0521E_RESTORE_NOT_SUPPORTED.explanation",			"Secure restore is supported only for appliances running a particular " +
																	"firmware level or higher." },
		{ "CWZBA0521E_RESTORE_NOT_SUPPORTED.useraction",			"Deploy firmware to the appliance of at least the minimum required level and " +
																	"then perform the restore." },
		
		// ----------------------------------------------------------------------------------------
		// {0} the location of the backup file
		{ "CWZBA0522E_BACKUP_FILE_IOEXCEPTION",						"An error occurred trying to read the backup file ''{0}''." },
		{ "CWZBA0522E_BACKUP_FILE_IOEXCEPTION.explanation",			"An error occurred trying to read the backup file for a restore operation." },
		{ "CWZBA0522E_BACKUP_FILE_IOEXCEPTION.useraction",			"See the linked exception to determine the cause." },
				
		// ----------------------------------------------------------------------------------------
		// {0} the exception message
		{ "CWZBA0523E_MALFORMED_URL",								"The URL is not valid. The error message is ''{0}''." },
		{ "CWZBA0523E_MALFORMED_URL.explanation",					"The data provided for the URL is not correctly formed." },
		{ "CWZBA0523E_MALFORMED_URL.useraction",					"Check that the data provided is a valid URL." },
		
		// ----------------------------------------------------------------------------------------
		// {0} the domain identifier
		// {1} the domain display name
		// {2) the error message
		{ "CWZBA0524E_INVALID_DOMAIN_IDENTIFIER",					"The domain identifier ''{0}'' is not valid for the domain named ''{1}''. The " +
																	"error message is ''{2}''." },
		{ "CWZBA0524E_INVALID_DOMAIN_IDENTIFIER.explanation",		"The data provided for the domain identifier is not valid." },
		{ "CWZBA0524E_INVALID_DOMAIN_IDENTIFIER.useraction",		"Check the provided data." },
		
		// ----------------------------------------------------------------------------------------
		// {0} A file path
		{ "CWZBA0525E_CANT_FIND_INSTALL_ROOT",						"Error while locating the Appliance Management Center installation " +
																	"directory. The location of index.html is ''{0}''." },
		{ "CWZBA0525E_CANT_FIND_INSTALL_ROOT.explanation",			"While generating the location of the installation directory, the application queries " +
																	"the application server for the location of index.html. This message indicates that " +
																	"the query returned either a null response, or an unexpected location." },
		{ "CWZBA0525E_CANT_FIND_INSTALL_ROOT.useraction",			"Contact IBM Support to report this problem." },
		
		// ----------------------------------------------------------------------------------------
		// {0} A file path
		{ "CWZBA0526I_LOADING_PROPS_FROM",							"Configuration properties are being loaded from ''{0}''." },
		{ "CWZBA0526I_LOADING_PROPS_FROM.explanation",				"Configuration properties can change the behavior of Appliance " +
																	"Management Center. This message indicates the file from which the properties " +
																	"are loaded." },
		{ "CWZBA0526I_LOADING_PROPS_FROM.useraction",				"No action is required." },		
		
		// ----------------------------------------------------------------------------------------
		// {0} An appliance name
		{ "CWZBA0527E_ERROR_DISSECTING_SERVICES",					"The appliance ''{0}'' cannot extract any services from the provided file." },
		{ "CWZBA0527E_ERROR_DISSECTING_SERVICES.explanation",		"Appliance Management Center uses facilities on an appliance to decode " +
																	"configuration source files. The provided file cannot be decoded by the named " +
																	"appliance. The most likely cause is that it is not a valid configuration source " +
																	"file." },
		{ "CWZBA0527E_ERROR_DISSECTING_SERVICES.useraction",		"Check that the provided file is valid. If it is valid, for more information " +
																	"examine the system logs on the named appliance." },
		
		// ----------------------------------------------------------------------------------------
		// This message is the server-log equivalent of displaying CWZBA0504E_UNEXPECTED_ERROR to the user. That code is included
		// in the message text so that people searching for that error code will find it.
		// {0} the exception message
		{ "CWZBA0528E_LOG_UNEXPECTED_ERROR",						"An unexpected error occurred (CWZBA0504E). Message: ''{0}''" },
		{ "CWZBA0528E_LOG_UNEXPECTED_ERROR.explanation",			"An error occurred for which there is no predefined response." },
		{ "CWZBA0528E_LOG_UNEXPECTED_ERROR.useraction",				"Review the error message, and the diagnostic information in " +
																	"<install_dir>/logs/messages.log. Contact IBM Support for further assistance." },
																														
		// ----------------------------------------------------------------------------------------
		// {0} the value of the filter operator in the request
		{ "CWZBA0529E_INVALID_FILTER_OPERATOR",						"The filter operator value of ''{0}'' is not valid." },
		{ "CWZBA0529E_INVALID_FILTER_OPERATOR.explanation",			"The value provided for the filter operator is not a valid value." },
		{ "CWZBA0529E_INVALID_FILTER_OPERATOR.useraction",			"Check the value provided is valid." },		
		
		// ----------------------------------------------------------------------------------------
		// {0} the number of filter operators provided
		{ "CWZBA0530E_TOO_MANY_FILTER_OPERATORS",					"The number of filter operators provided ({0}) is not valid." },
		{ "CWZBA0530E_TOO_MANY_FILTER_OPERATORS.explanation",		"Filtering supports one filter operator only." },
		{ "CWZBA0530E_TOO_MANY_FILTER_OPERATORS.useraction",		"Use one filter operator when supplying filtering criteria." },	
																																
		// ----------------------------------------------------------------------------------------
		// {0} filter key
		// {1} filter value
		{ "CWZBA0531E_NO_MATCHING_ACTION",							"The matching action for key ''{0}'' and value ''{1}'' is not present." },
		{ "CWZBA0531E_NO_MATCHING_ACTION.explanation",				"Filter clauses must contain the required matching action." },
		{ "CWZBA0531E_NO_MATCHING_ACTION.useraction",				"Include a matching action for each clause when supplying filtering criteria." },	
		
		// ----------------------------------------------------------------------------------------
		// {0} matching action 
		// {1} filter key
		// {2} filter value
		{ "CWZBA0532E_INVALID_MATCHING_ACTION",						"The matching action ''{0}'' for key ''{1}'' and value ''{2}'' is not valid." },
		{ "CWZBA0532E_INVALID_MATCHING_ACTION.explanation",			"Filter clauses must contain a valid matching action." },
		{ "CWZBA0532E_INVALID_MATCHING_ACTION.useraction",			"Include a valid matching action for each clause when supplying filtering criteria." },
		
		// ----------------------------------------------------------------------------------------
		// {0} HTTP error code, eg 404 
		// {1} Short message explaining what the code means (eg "Not Authorized")
		// {2} A URL
		{ "CWZBA0533E_REMOTE_FILE_HTTP_ERROR",						"The request specified a remote file accessed through HTTP. The remote server reported HTTP " +
																	"error {0} ({1}) for the URL {2}" },
		{ "CWZBA0533E_REMOTE_FILE_HTTP_ERROR.explanation",			"For some operations, Appliance Management Center allows you to use a file located " +
																	"on a remote server instead of uploading a file from your local workstation. Such files are " +
																	"transferred to the Appliance Management Center server using HTTP. This message indicates " +
																	"that the remote server responded to the transfer request with an error." },
		{ "CWZBA0533E_REMOTE_FILE_HTTP_ERROR.useraction",			"Ensure that the URL you specified for the remote file is correct, and that the remote file server is " +
																	"configured to accept requests from the Appliance Management Center server. If necessary, " +
																	"examine the HTTP server logs on the remote file server to determine why the transfer request was rejected." },
																	
		// ----------------------------------------------------------------------------------------
		// {0} parameter name 
		// {1} parameter value
	    { "CWZBA0534E_INVALID_PARAMETER_VALUE",						"The parameter ''{0}'' with value ''{1}'' is not valid." },
	    { "CWZBA0534E_INVALID_PARAMETER_VALUE.explanation",			"The parameter must contain a valid value." },
		{ "CWZBA0534E_INVALID_PARAMETER_VALUE.useraction",			"Include a valid value when supplying the parameter." },
																	
		// ----------------------------------------------------------------------------------------
		// {0} The file that wasn't found. 
		// {1} The name of a domain.
		{ "CWZBA0535E_UPLOAD_FILE_NOT_FOUND",						"Error while uploading to the domain ''{1}''. The file ''{0}'' was not in temporary storage." },
		{ "CWZBA0535E_UPLOAD_FILE_NOT_FOUND.explanation",			"Files that you upload from your local workstation are stored temporarily on the Appliance " +
																	"Management Center (WAMC) server before being transferred to the appliance. The WAMC web client performs " +
																	"both of these actions for you; clients that use the REST API must first upload the file to the server and " +
																	"then request its transfer to the appliance. This message indicates that the file was not in the temporary " +
																	"store when the transfer was requested." },
		{ "CWZBA0535E_UPLOAD_FILE_NOT_FOUND.useraction",			"If you are not using the WAMC web client, ensure that the file is uploaded to the temporary store " +
																	"(/files resource) before you request the transfer to the appliance. If you are using the WAMC web client, " +
																	"report this error to IBM Support; as a workaround, try performing the upload again." },
																	
		// ----------------------------------------------------------------------------------------
		// {0} The file name that wasn't found 
		// {1} A domain's name.
		{ "CWZBA0536E_REMOTE_UPLOAD_FILE_NOT_FOUND",				"The file could not be uploaded to the domain ''{1}''. The file was not found at ''{0}''." },
		{ "CWZBA0536E_REMOTE_UPLOAD_FILE_NOT_FOUND.explanation",	"You chose to upload a file to the appliance from a remote location. The file was not found." },
		{ "CWZBA0536E_REMOTE_UPLOAD_FILE_NOT_FOUND.useraction",		"Check the URL supplied in the request, and ensure that the file is present on the remote server." },
		
		// ----------------------------------------------------------------------------------------
		// {0} The url that wasn't found 
		// {1} The action that was being attempted when the error occurred.
		{ "CWZBA0537E_HTTP_NOT_FOUND_WITH_ACTION",					"No file was found at the URL ''{0}'' while performing the following action: ''{1}''" },
		{ "CWZBA0537E_HTTP_NOT_FOUND_WITH_ACTION.explanation",		"For some operations, Appliance Management Center allows you to use a file located " +
																	"on a remote server instead of uploading a file from your local workstation. Such files are " +
																	"transferred to the Appliance Management Center server using HTTP. This message indicates " +
																	"that the remote server could not find the file at the specified address." },
		{ "CWZBA0537E_HTTP_NOT_FOUND_WITH_ACTION.useraction",		"Ensure that the URL you specified for the remote file is correct, and that the remote file server is " +
																	"configured to accept requests from the Appliance Management Center server. If necessary, " +
																	"examine the HTTP server logs on the remote file server to determine why the transfer request was rejected." },

		// ----------------------------------------------------------------------------------------
		// {0} The url that wasn't found 
		{ "CWZBA0538E_HTTP_NOT_FOUND",								"No file was found at the URL ''{0}''." },
		{ "CWZBA0538E_HTTP_NOT_FOUND.explanation",					"For some operations, Appliance Management Center allows you to use a file located " +
																	"on a remote server instead of uploading a file from your local workstation. Such files are " +
																	"transferred to the Appliance Management Center server using HTTP. This message indicates " +
																	"that the remote server could not find the file at the specified address." },
		{ "CWZBA0538E_HTTP_NOT_FOUND.useraction",					"Ensure that the URL you specified for the remote file is correct, and that the remote file server is " +
																	"configured to accept requests from the Appliance Management Center server. If necessary, " +
																	"examine the HTTP server logs on the remote file server to determine why the transfer request was rejected." },																	

		// ----------------------------------------------------------------------------------------
		// {0} the quiesce timeout provided
		// {1} the min value
		// {2} the max value
		{ "CWZBA0539E_INVALID_QUIESCE_TIMEOUT",						"The provided quiesce timeout of {0} is invalid. The quiesce timeout " +
																	"must be within the range {1} - {2}." },
		{ "CWZBA0539E_INVALID_QUIESCE_TIMEOUT.explanation",			"The quiesce timeout must be within the valid range." },
		{ "CWZBA0539E_INVALID_QUIESCE_TIMEOUT.useraction",			"Ensure the quiesce timeout provided is within the valid range." },
																	
		// ========================================================================================
		// Messages CWZBA1001 - CWZBA1500 allocated to actions
		// ========================================================================================
		{ "CWZBA1001I_ACTION_STARTED",								"Started" },
		
		// ----------------------------------------------------------------------------------------
		{ "CWZBA1002E_ACTION_FAILED",								"Failed to complete successfully." },
		{ "CWZBA1002E_ACTION_FAILED.explanation",					"An action failed to complete successfully." },
		{ "CWZBA1002E_ACTION_FAILED.useraction",					"For information about why the action failed, expand ''Additional technical " +
																	"information''." },
		
		// ----------------------------------------------------------------------------------------
		// {0} the (translated) description of the action
		{ "CWZBA1003I_ACTION_SUCCEEDED",							"Completed successfully" },
		
		// ----------------------------------------------------------------------------------------
		// nested message, hence apostrophe type and no full stop
		// {0} is the identifier for the appliance
		{ "CWZBA1010I_QUIESCE_APPLIANCE_ACTION_DESCRIPTION",		"Quiesce appliance ''{0}''" },

		// ----------------------------------------------------------------------------------------
		// nested message, hence apostrophe type and no full stop
		// {0} is the identifier for the appliance
		{ "CWZBA1011I_UNQUIESCE_APPLIANCE_ACTION_DESCRIPTION",		"Unquiesce appliance ''{0}''" },
		
		// ----------------------------------------------------------------------------------------
		{ "CWZBA1012I_ACTION_SUBMITTED",							"Submitted" },

		// ----------------------------------------------------------------------------------------
		// nested message, hence apostrophe type and no full stop
		// {0} is the user comment for the firmware
		{ "CWZBA1013I_ADD_FIRMWARE_ACTION_DESCRIPTION",				"Add firmware ''{0}''" },

		// ----------------------------------------------------------------------------------------
		// {0} is the identifier for the appliance
		{ "CWZBA1014I_REMOVE_APPLIANCE_ACTION_DESCRIPTION",			"Remove appliance ''{0}''" },

		// ----------------------------------------------------------------------------------------
		// {0} is the identifier for the appliance
		{ "CWZBA1015I_ADD_APPLIANCE_ACTION_DESCRIPTION",			"Add appliance ''{0}''" },

		// ----------------------------------------------------------------------------------------
		// {0} is the identifier for the appliance
		{ "CWZBA1016I_UPDATE_APPLIANCE_ACTION_DESCRIPTION",			"Update properties for appliance ''{0}''" },

		// ----------------------------------------------------------------------------------------
		// {0} is the identifier for the appliance
		{ "CWZBA1017I_DEPLOY_FIRMWARE_ACTION_DESCRIPTION",			"Deploy firmware to appliance ''{0}''" },

		// ----------------------------------------------------------------------------------------
		// {0} is a step description from WAMT (pre-translated)
		{ "CWZBA1018I_DEPLOY_FIRMWARE_UPDATE",						"Firmware deployment in progress. {0}" },

		// ----------------------------------------------------------------------------------------
		// nested message, hence apostrophe type and no full stop
		// {0} is the user comment for the firmware
		{ "CWZBA1019I_REMOVE_FIRMWARE_ACTION_DESCRIPTION",			"Remove firmware ''{0}''" },
		
		// ----------------------------------------------------------------------------------------
		// {0} is the identifier for the appliance
		{ "CWZBA1020I_BACKUP_APPLIANCE_ACTION_DESCRIPTION",			"Backup appliance ''{0}''" },

		// ----------------------------------------------------------------------------------------
		// {0} is the identifier for the appliance
		{ "CWZBA1021I_RESTORE_APPLIANCE_ACTION_DESCRIPTION",		"Initiate restore appliance ''{0}''" },

		// ----------------------------------------------------------------------------------------
		{ "CWZBA1022E_PERMISSION_DENIED",							"The current user does not have permission to perform this action." },
		{ "CWZBA1022E_PERMISSION_DENIED.explanation",				"The current user and the groups of which they are a member do not have the " + 
																	"required permission to perform the requested action." },
		{ "CWZBA1022E_PERMISSION_DENIED.useraction",				"Ask your system administrator to grant you the required permission." },
														
		// ----------------------------------------------------------------------------------------
		// {0} is a username
		// {1} the HTTP method e.g. GET
		// {2} the URL path e.g. /amc/rest/appliances
		// {2} a list of permissions that apply to the resource e.g. [view-appliance]
		{ "CWZBA1023A_PERMISSION_DENIED",							"The user ''{0}'' does not have one of the required permissions {3} to perform " +
																	"the action ''{1}'' on the resource ''{2}''." },
		{ "CWZBA1023A_PERMISSION_DENIED.explanation",				"A REST request was sent to the server by a logged-in user, but the user does " +
																	"not have permission to access the resource." },
		{ "CWZBA1023A_PERMISSION_DENIED.useraction",				"No action is required, because the request was denied. If unauthorized " +
																	"requests are received, you should investigate them." },
																	
		// ----------------------------------------------------------------------------------------
		{ "CWZBA1024E_INVALID_BACKUP",								"The compressed backup file provided does not contain a manifest file." },
		{ "CWZBA1024E_INVALID_BACKUP.explanation",					"The compressed backup file provided for a restore operation does not contain " +
																	"a manifest file."},
		{ "CWZBA1024E_INVALID_BACKUP.useraction",					"Try the restore operation again with a valid compressed backup file." },
		
		// ----------------------------------------------------------------------------------------
		// {0} an exception classname, for example NullPointerException 
		// {1} a localised exception message
		{ "CWZBA1025W_HOUSEKEEPER_EXCEPTION",						"An error occurred in a background maintenance task. The error message is " +
																	"''{0}: {1}''." },
		{ "CWZBA1025W_HOUSEKEEPER_EXCEPTION.explanation",			"The server runs background tasks to perform maintenance actions, such as " +
																	"deleting files that are not required. One of these tasks encountered an " +
																	"error." },
		{ "CWZBA1025W_HOUSEKEEPER_EXCEPTION.useraction",			"Review the error message. For more information, enable debug tracing." },
																	
		// ----------------------------------------------------------------------------------------
		// nested message, hence apostrophe type and no full stop
		// {0} is the identifier for the domain
		// {1} the symbolic name of the appliance
		{ "CWZBA1026I_DELETE_DOMAIN_ACTION_DESCRIPTION",			"Delete domain ''{0}'' from appliance ''{1}''" },
		
		// ----------------------------------------------------------------------------------------
		// nested message, hence apostrophe type and no full stop
		// {0} is the name of the domain
		// {1} the symbolic name of the appliance
		{ "CWZBA1027I_QUIESCE_DOMAIN_ACTION_DESCRIPTION",			"Quiesce domain ''{0}'' on appliance ''{1}''" },

		// ----------------------------------------------------------------------------------------
		// nested message, hence apostrophe type and no full stop
		// {0} is the name of the domain
		// {1} the symbolic name of the appliance
		{ "CWZBA1028I_UNQUIESCE_DOMAIN_ACTION_DESCRIPTION",			"Unquiesce domain ''{0}'' on appliance ''{1}''" },
		
		// ----------------------------------------------------------------------------------------
		// nested message, hence apostrophe type and no full stop
		// {0} is the name of the domain
		// {1} the symbolic name of the appliance
		{ "CWZBA1029I_DEPLOY_DOMAIN_ACTION_DESCRIPTION",			"Deploy domain configuration to ''{0}'' on appliance ''{1}''" },
		
		// ----------------------------------------------------------------------------------------
		// nested message, hence apostrophe type and no full stop
		// {0} the name of the service
		// {1} the class of the service
		// {2} is the name of the domain
		// {3} the symbolic name of the appliance
		{ "CWZBA1030I_DEPLOY_SERVICE_ACTION_DESCRIPTION",			"Deploy service ''{0}'' of type ''{1}'' to domain ''{2}'' on appliance ''{3}''" },

		// ----------------------------------------------------------------------------------------
		// nested message, hence apostrophe type and no full stop
		{ "CWZBA1031I_ADD_FIRMWARE_ACTION_NO_COMMENT_DESCRIPTION",	"Add firmware" },

		// ----------------------------------------------------------------------------------------
		// nested message, hence apostrophe type and no full stop
		{ "CWZBA1032I_REMOVE_FIRMWARE_ACTION_NO_COMMENT_DESCRIPTION",	"Remove firmware" },
		
		// ----------------------------------------------------------------------------------------
		// {0} is the translated description of the action
		{ "CWZBA1033E_ACTION_FAILED_LOG",							"Action ''{0}'' failed to complete successfully." },
		{ "CWZBA1033E_ACTION_FAILED_LOG.explanation",				"An action failed to complete successfully." },
		{ "CWZBA1033E_ACTION_FAILED_LOG.useraction",				"For information about why the action failed, expand ''Additional technical " +
																	"information''." },
																	
		// ----------------------------------------------------------------------------------------
		// nested message, hence apostrophe type and no full stop
		// {0} the name of the service
		// {1} the class of the service
		// {2} is the name of the domain
		// {3} the symbolic name of the appliance
		{ "CWZBA1034I_DELETE_SERVICE_ACTION_DESCRIPTION",			"Delete service ''{0}'' of type ''{1}'' from domain ''{2}'' on appliance ''{3}''" },
				
		// ----------------------------------------------------------------------------------------
		// {0} the original string
		// {1} the truncated string
		{ "CWZBA1035I_TRUNCATED_STRING",							"The string ''{0}'' was truncated to ''{1}'' before being stored in the action "+
																	"history database." },
		{ "CWZBA1035I_TRUNCATED_STRING.explanation",				"The database column is not wide enough to contain the given string. The truncation " +
																	"does not have any functional implications." },
		{ "CWZBA1035I_TRUNCATED_STRING.useraction",					"No user action is required." },
		
		// ----------------------------------------------------------------------------------------
		// {0} is the name of the domain
		// {1} is the symbolic name of the appliance
		{ "CWZBA1036I_UPDATE_DOMAIN_ACTION_DESCRIPTION",			"Update properties for domain ''{0}'' on appliance ''{1}''" },
		
		// ----------------------------------------------------------------------------------------
		// nested message, hence apostrophe type and no full stop
		// {0} is the name of the service
		// {1} is the type of the service
		// {2} is name of the domain
		// {3} the symbolic name of the appliance
		{ "CWZBA1037I_QUIESCE_SERVICE_ACTION_DESCRIPTION",			"Quiesce service ''{0}'' of type ''{1}'' on domain ''{2}'' and appliance ''{3}''" },
		
		// ----------------------------------------------------------------------------------------
		// nested message, hence apostrophe type and no full stop
		// {0} is the name of the service
		// {1} is the type of the service
		// {2} is name of the domain
		// {3} the symbolic name of the appliance
		{ "CWZBA1038I_UNQUIESCE_SERVICE_ACTION_DESCRIPTION",		"Unquiesce service ''{0}'' of type ''{1}'' on domain ''{2}'' and appliance ''{3}''" },

		// ----------------------------------------------------------------------------------------
		{ "CWZBA1039W_SERVER_STOPPED",								"The server was stopped before the action finished running." },
		{ "CWZBA1039W_SERVER_STOPPED.explanation",					"Actions can take place only while the server is running, and cannot " +
																	"be resumed after the server stops. The server might stop because of a " +
																	"serious error, or an administrator can stop it deliberately." },
		{ "CWZBA1039W_SERVER_STOPPED.useraction",					"Try the action again." },		

		// ----------------------------------------------------------------------------------------
		// nested message, hence apostrophe type and no full stop
		// {0} is the name of the destination file
		// {1} is the name of the domain
		// {2} the symbolic name of the appliance
		{ "CWZBA1040I_UPLOAD_FILE_ACTION_DESCRIPTION",				"Upload of file ''{0}'' to domain ''{1}'' on appliance ''{2}''" },
		
		// ----------------------------------------------------------------------------------------
		// {0} is the identifier for the appliance
		{ "CWZBA1041I_UPDATE_APPLIANCE_GROUPS_ACTION",				"Update group assignment for appliance ''{0}''" },
		
		// ----------------------------------------------------------------------------------------
		// {0} is the identifier for the domain
		{ "CWZBA1042I_UPDATE_DOMAIN_GROUPS_ACTION",					"Update group assignment for domain ''{0}''" },
		
		// ----------------------------------------------------------------------------------------
		// nested message, hence apostrophe type and no full stop
		// {0} is the name of the domain
		// {1} the symbolic name of the appliance
		{ "CWZBA1043I_RESTART_DOMAIN_ACTION_DESCRIPTION",				"Restart domain ''{0}'' on appliance ''{1}''" },
		
		// ----------------------------------------------------------------------------------------
		// nested message, hence apostrophe type and no full stop
		// {0} the symbolic name of the appliance
		{ "CWZBA1044I_REBOOT_APPLIANCE_ACTION_DESCRIPTION",				"Reboot appliance ''{0}''" },		
		
		// ----------------------------------------------------------------------------------------
		// nested message, hence apostrophe type and no full stop
		// {0} the user comment for the firmware
		{ "CWZBA1045I_UPDATE_FIRMWARE_ACTION_DESCRIPTION",				"Update properties for firmware ''{0}''" },		
		
		// ----------------------------------------------------------------------------------------
		// nested message, hence apostrophe type and no full stop
		// {0} the user comment for the firmware
		{ "CWZBA1046I_UPDATE_FIRMWARE_NO_COMMENT_ACTION_DESCRIPTION",	"Update properties for firmware" },		
		
		// ----------------------------------------------------------------------------------------
		// {0} is the name of the role
		{ "CWZBA1047I_CREATE_ROLE_ACTION_DESCRIPTION",					"Create user role ''{0}''" },	
		
		// ----------------------------------------------------------------------------------------
		// {0} is the name of the role
		{ "CWZBA1048I_DELETE_ROLE_ACTION_DESCRIPTION",					"Delete user role ''{0}''" },
		
		// ----------------------------------------------------------------------------------------
		// {0} is the name of the role
		{ "CWZBA1049I_UPDATE_ROLE_ACTION_DESCRIPTION",					"Update user role ''{0}''" },
		
		// ----------------------------------------------------------------------------------------
		// {0} is the name of the user group
		{ "CWZBA1050I_UPDATE_USER_GROUP_ACTION_DESCRIPTION",			"Update user group ''{0}''" },
		
		// ----------------------------------------------------------------------------------------
		// {0} is the name of the user 
		{ "CWZBA1051I_UPDATE_USER_ACTION_DESCRIPTION",					"Update user ''{0}''" },
		
		// ========================================================================================
		// Messages CWZBA1501 - CWZBA2000 allocated to ReST resources
		// ========================================================================================
		{ "CWZBA1501E_LICENCE_NOT_ACCEPTED",						"The license agreement was not accepted." },
		{ "CWZBA1501E_LICENCE_NOT_ACCEPTED.explanation",			"To undertake this action, the license agreement must be accepted." },
		{ "CWZBA1501E_LICENCE_NOT_ACCEPTED.useraction",				"Accept the license agreement." },		
		
		// ----------------------------------------------------------------------------------------
		{ "CWZBA1502E_EMPTY_FILE",									"The uploaded file was empty." },
		{ "CWZBA1502E_EMPTY_FILE.explanation",						"The file uploaded as part of this action was empty." },
		{ "CWZBA1502E_EMPTY_FILE.useraction",						"Ensure that the original file is valid. If you are using Firefox 3.6 on Windows, " +
																	"use a different browser to upload large files." },						
		
		// ----------------------------------------------------------------------------------------
		{ "CWZBA1503E_NAME_IN_USE",									"The {0} name ''{1}'' is already in use." },
		{ "CWZBA1503E_NAME_IN_USE.explanation",						"An object with this name already exists. Every object of the same type must have a unique name." },
		{ "CWZBA1503E_NAME_IN_USE.useraction",						"Choose a different name and try the operation again." },						
		
		// ----------------------------------------------------------------------------------------
		// nested message, hence no caps or full-stop.
		{ "CWZBA1504I_USER_ROLE",									"user role" },

		// ----------------------------------------------------------------------------------------
		// nested message, hence no caps or full-stop.
		{ "CWZBA1505I_USER",										"user" },

		// ----------------------------------------------------------------------------------------
		// nested message, hence no caps or full-stop.
		{ "CWZBA1506I_USER_GROUP",									"user group" },

		
		// ========================================================================================
		// Messages CWZBA2001 - CWZBA2050 allocated to file management
		// ========================================================================================											
		// {0} the path of the directory that could not be created
		{ "CWZBA2001E_DIRECTORY_CREATION_FAILED",					"Failed to create a temporary directory with path ''{0}''." },
		{ "CWZBA2001E_DIRECTORY_CREATION_FAILED.explanation",		"An attempt to create a temporary directory under the Appliance " +
																	"Management Toolkit repository directory failed." },
		{ "CWZBA2001E_DIRECTORY_CREATION_FAILED.useraction",		"Ensure that the user under which the Appliance Management Server " +
																	"is running has sufficient privileges to create the named directory." },		

		// ----------------------------------------------------------------------------------------
		// {0} the temporary file URI
		{ "CWZBA2002E_TEMPORARY_FILE_FAILED",						"Failed to create path to temporary file from URI ''{0}''." },
		{ "CWZBA2002E_TEMPORARY_FILE_FAILED.explanation",			"An attempt to create a path to a temporary file in the Appliance " +
																	"Management Toolkit repository based on the given URI failed." },
		{ "CWZBA2002E_TEMPORARY_FILE_FAILED.useraction",			"Ensure that the URI specified is the same as that returned when the " +
																	"temporary file was uploaded." },		

		// ----------------------------------------------------------------------------------------
		// {0} the remote file URI
		{ "CWZBA2003E_INVALID_URI",									"Failed to convert the URI ''{0}'' for a remote file to a valid URL." },
		{ "CWZBA2003E_INVALID_URI.explanation",						"An attempt to convert the URI given for a remote file into a URL failed." },
		{ "CWZBA2003E_INVALID_URI.useraction",						"Ensure that the URI represents a valid URL." },
		
		// ----------------------------------------------------------------------------------------
		// {0} the remote file URI
		{ "CWZBA2004E_INVALID_URI_STRING",							"Failed to convert the string ''{0}'' to a valid URI." },
		{ "CWZBA2004E_INVALID_URI_STRING.explanation",				"The syntax of the string provided is not a valid URI." },
		{ "CWZBA2004E_INVALID_URI_STRING.useraction",				"Ensure the string represents a valid URI." },

							
		
		// ========================================================================================
		// Messages CWZBA2051 - CWZBA2250 allocated to command-line tools
		// ========================================================================================			
		{ "CWZBA2051I_COPY_REPO_HELP",								"Appliance Management Center         (C) Copyright IBM Corporation 2014\n" +
																	"\n" +
																	"NAME\n" +
																	"    copy-repository - copy a Appliance Management Toolkit repository.\n" +
																	"\n" +
																	"SYNOPSIS\n" +
																	"    copy-repository --fromRepo <DIRECTORY> --toRepo <DIRECTORY>\n" +
																	"\n" +
																	"DESCRIPTION\n" +
																	"    Copy a repository from one location in the file system to another. Because\n" +
																	"    repositories can contain absolute paths referring to members within\n" +
																	"    themselves, they should never be moved using normal operating system tools.\n" +
																	"    This utility copies a repository and updates these internal references to\n" +
																	"    use the new location.\n" +
																	"\n" +
																	"    --fromRepo\n" +
																	"        The absolute path to a directory, as referred to in wamc.properties, \n" +
																	"        that forms a WAMT repository. This is the repository that will be\n" +
																	"        copied, and must exist.\n" +
																	"\n" +
																	"    --toRepo\n" +
																	"        The absolute path to a directory which will become the new WAMT\n" +
																	"        repository. This need not already exist (it and any parent directories\n" +
																	"        will be created if needed) but if it does exist then it must be empty.\n" +
																	"\n" +
																	"EXIT STATUS\n" +
																	"    0   Successful completion\n" +
																	"    1   Displayed help message (possibly due to incorrect commandline syntax)\n" +
																	"    2   Input not valid (for example, specifying a non-writable destination)\n" +
																	"    3   Runtime failure\n" +
																	"\n" },
		
		// ----------------------------------------------------------------------------------------
		{ "CWZBA2052I_SUCCESS",										"The repository was copied successfully." },
		{ "CWZBA2052I_SUCCESS.explanation",							"The repository-copy tool has finished working." },

		// ----------------------------------------------------------------------------------------
		// {0} 
		// {1} 
		// {2} 
		// {3}
		// {4} 
		{ "CWZBA2053E_INSUFFICIENT_DEST_SPACE",						"The destination directory does not have enough usable disk space.\n" +
																	"Destination:                  {0}\n" +
																	"Source repository size:       {1} ({2})\n" +
																	"Destination repository size: {3} ({4})" },
		{ "CWZBA2053E_INSUFFICIENT_DEST_SPACE.explanation",			"The tool calculates the current size of the source repository, and " +
																	"queries the system for the amount of usable space on the partition that holds " +
																	"the destination. If the usable space is less than the current size, " +
																	"the copy operation is not started." },
		{ "CWZBA2053E_INSUFFICIENT_DEST_SPACE.useraction",			"Make more space available at the destination directory and run the " +
																	"command again, or choose a different destination." },
		
		// ----------------------------------------------------------------------------------------
		// {0} 
		{ "CWZBA2054E_CREATE_DIR_FAILED",							"Failed to create directory ''{0}''." },
		{ "CWZBA2054E_CREATE_DIR_FAILED.explanation",				"While copying the repository, the tool attempted to create the named " +
																	"directory. The system reported that it was not created, but did not " +
																	"indicate why." },
		{ "CWZBA2054E_CREATE_DIR_FAILED.useraction",				"Check that the user that is running the tool can create directories in " +
																	"the destination repository." },
		
		// ----------------------------------------------------------------------------------------
		{ "CWZBA2055E_DEST_REPO_NOT_SPECIFIED",						"The destination repository is not specified. To receive usage information, run the command with " +
																	"a --help parameter." },
		{ "CWZBA2055E_DEST_REPO_NOT_SPECIFIED.explanation",			"The command ran without a --toRepo parameter." },
		{ "CWZBA2055E_DEST_REPO_NOT_SPECIFIED.useraction",			"Run the command again with the appropriate parameters." },

		// ----------------------------------------------------------------------------------------
		// {0} 
		{ "CWZBA2056E_DEST_REPO_NOT_ABSOLUTE",						"The specified destination repository path, ''{0}'', is not absolute. On Linux and UNIX platforms, " +
																	"paths must start with a forward slash (/);  on Windows, paths must start with a drive letter or network path." },
		{ "CWZBA2056E_DEST_REPO_NOT_ABSOLUTE.explanation",			"The command accepts absolute file system paths only." },
		{ "CWZBA2056E_DEST_REPO_NOT_ABSOLUTE.useraction",			"Run the command again with the appropriate parameters." },

		// ----------------------------------------------------------------------------------------
		// {0} 
		// {1} 
		{ "CWZBA2057E_DEST_REPO_NOT_VALID",							"The system reported an error when trying to use the destination repository path ''{0}''. " +
																	"The error was ''{1}''"},
		{ "CWZBA2057E_DEST_REPO_NOT_VALID.explanation",				"An error was reported by the system. The error is included as part of this message. " +
																	"The path specified for the destination repository might not be valid." },
		{ "CWZBA2057E_DEST_REPO_NOT_VALID.useraction",				"Review the system error message. Run the command again with a different destination path." },
		
		// ----------------------------------------------------------------------------------------
		// {0} 
		{ "CWZBA2058E_CREATE_DEST_FAILED",							"Failed to create the destination repository directory ''{0}''." },
		{ "CWZBA2058E_CREATE_DEST_FAILED.explanation",				"The specified destination directory does not exist, therefore the tool attempted to create " +
																	"it. The system reported that the directory was not created, but did not indicate why." },
		{ "CWZBA2058E_CREATE_DEST_FAILED.useraction",				"Check whether the user that is running the tool can create the destination repository manually." },
		
		// ----------------------------------------------------------------------------------------
		// {0} 
		{ "CWZBA2059E_DEST_REPO_NOT_DIR",							"The specified destination repository path, ''{0}'', is not a directory." },
		{ "CWZBA2059E_DEST_REPO_NOT_DIR.explanation",				"The destination repository must either not exist, or be an empty directory." },
		{ "CWZBA2059E_DEST_REPO_NOT_DIR.useraction",				"Choose a different destination repository directory, or delete the file that " +
																	"is currently at this location." },

		// ----------------------------------------------------------------------------------------
		// {0} 
		{ "CWZBA2060E_DEST_REPO_NOT_EMPTY",							"The specified destination repository directory, ''{0}'', is not empty." },
		{ "CWZBA2060E_DEST_REPO_NOT_EMPTY.explanation",				"The destination repository must either not exist, or be an empty directory." },
		{ "CWZBA2060E_DEST_REPO_NOT_EMPTY.useraction",				"Choose a different destination repository directory, or delete the existing " +
																	"contents of the directory." },

		// ----------------------------------------------------------------------------------------
		// {0} 													
		{ "CWZBA2061E_DEST_REPO_NOT_ACCESSIBLE",					"The destination repository directory, ''{0}'', cannot be accessed." },
		{ "CWZBA2061E_DEST_REPO_NOT_ACCESSIBLE.explanation",		"The tool attempted to list the contents of the repository directory. The " +
																	"system reported that this operation failed, but did not indicate why." },
		{ "CWZBA2061E_DEST_REPO_NOT_ACCESSIBLE.useraction",			"Check whether the user that is running the tool can access the destination repository manually." },
							
		// ----------------------------------------------------------------------------------------
		// {0} 
		{ "CWZBA2062E_DEST_DELETE_FAILED",							"Failed to delete test file ''{0}'' from the destination repository directory." },
		{ "CWZBA2062E_DEST_DELETE_FAILED.explanation",				"Before starting to copy the repository, the tool writes a small test file to " +
																	"the destination to check that all required operations are available. Deletion of " +
																	"this test file failed, but the system did not indicate why." },
		{ "CWZBA2062E_DEST_DELETE_FAILED.useraction",				"Ensure that the user that is running the tool has full read and write access to the " +
																	"destination repository directory." },

		// ----------------------------------------------------------------------------------------	
		// {0}
		// {1} 
		{ "CWZBA2063E_DEST_WRITE_FAILED",							"Unable to write to the destination repository directory, ''{0}''. The following " +
																	"error was reported: ''{1}''." },
		{ "CWZBA2063E_DEST_WRITE_FAILED.explanation",				"Before starting to copy the repository, the tool writes a small test file to " +
																	"the destination to check that all required operations are available. An error " +
																	"was reported while attempting to write this file." },
		{ "CWZBA2063E_DEST_WRITE_FAILED.useraction",				"Ensure that the user that is running the tool has full read and write access to the " +
																	"destination repository directory." },		

		// ----------------------------------------------------------------------------------------
		{ "CWZBA2064E_SRC_REPO_NOT_SPECIFIED",						"No source repository is specified. To receive usage information, run the command with " +
																	"a --help parameter." },
		{ "CWZBA2064E_SRC_REPO_NOT_SPECIFIED.explanation",			"The command ran without a --fromRepo parameter." },
		{ "CWZBA2064E_SRC_REPO_NOT_SPECIFIED.useraction",			"Run the command again with the appropriate parameters." },

		// ----------------------------------------------------------------------------------------
		// {0} 
		{ "CWZBA2065E_SRC_REPO_NOT_ABSOLUTE",						"The specified source repository path, ''{0}'', is not absolute. On Linux and UNIX platforms, " +
																	"paths must start with ''/''; on Windows, paths must start with a drive letter or network path." },
		{ "CWZBA2065E_SRC_REPO_NOT_ABSOLUTE.explanation",			"The command accepts absolute file system paths only." },
		{ "CWZBA2065E_SRC_REPO_NOT_ABSOLUTE.useraction",			"Run the command again with the appropriate parameters." },

		// ----------------------------------------------------------------------------------------
		// {0} 
		// {1} 
		{ "CWZBA2066E_SRC_REPO_NOT_VALID",							"The system reported an error when trying to use the source repository path ''{0}''. " +
																	"The error was ''{1}''"},
		{ "CWZBA2066E_SRC_REPO_NOT_VALID.explanation",				"An error was reported by the system. The error is included as part of this message. " +
																	"The path specified for the source repository might not be valid." },
		{ "CWZBA2066E_SRC_REPO_NOT_VALID.useraction",				"Review the system error message. Run the command again with a different source path." },
		
		// ----------------------------------------------------------------------------------------
		// {0} 
		{ "CWZBA2067E_SRC_REPO_NOT_DIR",							"The specified source repository path, ''{0}'', is not a directory." },
		{ "CWZBA2067E_SRC_REPO_NOT_DIR.explanation",				"The source repository must be an existing Appliance Management Toolkit " +
																	"repository, and the --fromRepo parameter must point to the top-level directory of " +
																	"the repository that contains the WAMT.repository.xml file." },
		{ "CWZBA2067E_SRC_REPO_NOT_DIR.useraction",					"Run the command again by using the --fromRepo parameter to identify an existing " +
																	"repository." },

		// ----------------------------------------------------------------------------------------
		// {0} 
		{ "CWZBA2068E_SRC_REPO_NOT_ACCESSIBLE",						"The source repository directory, ''{0}'', cannot be accessed." },
		{ "CWZBA2068E_SRC_REPO_NOT_ACCESSIBLE.explanation",			"The tool attempted to list the contents of the source directory. The " +
																	"system reported that this operation failed, but did not indicate why." },
		{ "CWZBA2068E_SRC_REPO_NOT_ACCESSIBLE.useraction",			"Check whether the user that is running the tool can access the source repository manually." },

		// ----------------------------------------------------------------------------------------
		// {0} 
		{ "CWZBA2069E_SRC_REPO_NOT_READABLE",						"The source repository directory, ''{0}'', is not readable." },
		{ "CWZBA2069E_SRC_REPO_NOT_READABLE.explanation",			"The system reported that the directory cannot be read by the current user." },
		{ "CWZBA2069E_SRC_REPO_NOT_READABLE.useraction",			"Check whether the user that is running the tool can access the source repository manually." },
						
		// ----------------------------------------------------------------------------------------
		// {0} 
		{ "CWZBA2070E_INFO_FILE_NO_MATCH",							"Unable to correctly read the source repository information file, ''{0}''. " },
		{ "CWZBA2070E_INFO_FILE_NO_MATCH.explanation",				"Before starting to copy the file, the tool checks the first characters of the repository " +
																	"information file to verify that it can read the file correctly. " +
																	"The contents of the file do not match what was expected." },
		{ "CWZBA2070E_INFO_FILE_NO_MATCH.useraction",				"Check that the repository information file is valid and readable." },

		// ----------------------------------------------------------------------------------------
		// {0} 
		// {1} 
		{ "CWZBA2071E_INFO_FILE_NOT_FOUND",							"Repository information file ({0}) was not found in the specified source directory, ''{1}''. " },
		{ "CWZBA2071E_INFO_FILE_NOT_FOUND.explanation",				"The information file stores the appliances, domains, and other resources known to " +
																	"Appliance Management Center. No information file was found in the specified " +
																	"directory, therefore it is not a valid repository." },
		{ "CWZBA2071E_INFO_FILE_NOT_FOUND.useraction",				"Specify a valid source repository path." },

		// ----------------------------------------------------------------------------------------
		// {0} 
		// {1} 
		{ "CWZBA2072E_INFO_FILE_READ_FAILED",						"An error occurred while reading the source repository information file, ''{0}''. " +
																	"The error was: ''{1}''." },
		{ "CWZBA2072E_INFO_FILE_READ_FAILED.explanation",			"Before starting to copy this file, the tool checks that it can read the repository information " +
																	"file. When the tool attempted to read the file, an error was reported by the system." },
		{ "CWZBA2072E_INFO_FILE_READ_FAILED.useraction",			"Review the system error message." },

		// ----------------------------------------------------------------------------------------
		// {0} 
		// {1} 
		// {2} 
		{ "CWZBA2073E_FILE_COPY_ERROR",								"An error occurred while copying ''{0}'' to ''{1}''. The error was: ''{2}''." },
		{ "CWZBA2073E_FILE_COPY_ERROR.explanation",					"An error was reported by the system. The error is included as part of this message." },
		{ "CWZBA2073E_FILE_COPY_ERROR.useraction",					"Review the system error message." },	
		
		
		// ========================================================================================
		// Messages CWZBA2251 - CWZBA2300 allocated to server framework
		// ========================================================================================
		// {0} the product release
		// {1} the build number
		{ "CWZBA2251I_RELEASE_BUILD",								"IBM Appliance Management Center Release {0}, Build Number {1}." },

		
		// ========================================================================================
		// Messages CWZBA2301 - CWZBA2350 allocated to security manager
		// ========================================================================================
		// {0} the role name
		{ "CWZBA2301E_DEFAULT_ROLE_IMMUTABLE",						"The default user role ''{0}'' cannot be modified." },
		{ "CWZBA2301E_DEFAULT_ROLE_IMMUTABLE.explanation",			"You cannot modify the default roles that are provided with " +
																	"the product." },
		{ "CWZBA2301E_DEFAULT_ROLE_IMMUTABLE.useraction",			"Copy the default user role and modify the copy." },

		
		// ========================================================================================
		// Emergency Message - should remain the last message in this file
		// ========================================================================================
		// {0} The emergency message content
		{ "CWZBA9999E_EMERGENCY_MSG",								"{0}" },
		{ "CWZBA9999E_EMERGENCY_MSG.explanation",					"If this message does not provide the required information, check the " +
																	"accompanying messages." },
		{ "CWZBA9999E_EMERGENCY_MSG.useraction",					"For further information about resolving this error, see the product " +
																	"information center." }
	};
}
