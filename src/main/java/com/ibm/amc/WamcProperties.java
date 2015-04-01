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

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Response.Status;

import com.ibm.amc.ras.Logger47;

/**
 * A centralised handler of WAMC server-side properties, as (usually) defined in the wamc.properties
 * config file. This may be called from anywhere in the application (it will be initialised on first
 * use) but it is recommended that the check method be called at startup so that invalid properties
 * can be detected and reported.
 */
public class WamcProperties implements InvocationHandler
{
	// @CLASS-COPYRIGHT@
	static Logger47 logger = Logger47.get(WamcProperties.class.getCanonicalName(), Constants.CWZBA_BUNDLE_NAME);

	/**
	 * <p>
	 * A list of the available WAMC properties. Calls to these methods will be automatically
	 * resolved into lookups of the matching named property in the properties file.
	 * </p>
	 * <p>
	 * "Matching named" means that the "get" prefix is removed from the method name, the next
	 * character is changed to lowercase, and the string "com.ibm.amc." is inserted at the
	 * beginning. The resulting name is looked up in the properties file.
	 * </p>
	 * <p>
	 * The "Required" annotation is enforced when the containing WamcProperties is check()ed,
	 * presumably at application startup. The value specified in the "Default" annotation is used if
	 * the property is unset at the time it is requested.
	 * </p>
	 */
	public interface Props
	{
		/* *********************************************************************
		 *  Start of properties to be documented
		 * ********************************************************************/
		/**
		 * The fully-qualified path to a directory in which WAMT should create and maintain its
		 * repository. If used, this includes the <i>WAMTRepository</i> part of the path. 
		 */
		@Required
		String getWamtRepository();

		/**
		 * The port on which WAMT should listen for incoming notifications from appliances. If
		 * multiple WAMTs are run on one system (whether embedded in WAMC or not) each one must have
		 * this set differently. The corresponding WAMT property (if setting up standalone WAMTs) is
		 * OPTION_NOTIFICATION_CATCHER_PORT.
		 */
		@Required
		@Default("5555")
		int getWamtNotificationPort();
		
		/**
		 * The IP address that appliances should use to contact WAMC. This will normally be configured
		 * automatically to the WAMC server's own address, but some firewall configurations might mean
		 * that appliances need to use a different address to reach the server.
		 */
		String getWamtNotificationAddress();

		/**
		 * The network interface on which WAMC should listen for incoming notifications from appliances.
		 * This will normally be configured automatically, but systems with multiple interfaces on
		 * different networks might need to set it manually.
		 */
		String getWamtNotificationInterface();
		
		/**
		 * The port on which WAMC should listen for requests from XC10 appliances to retrieve firmware.
		 * For most appliances, WAMC uploads the firmware image directly to the appliance. For XC10,
		 * it sends a command to the appliance which then connects back to WAMC to download the image.
		 * This port is only needed if you manage XC10 appliances using WAMC.
		 */
		@Required
		@Default("5556")
		int getWamtHttpPort();
		
		/**
		 * The IP address that XC10 appliances should use to contact WAMC to retrieve firmware (see 
		 * the explanation for wamtHttpPort). This will normally be configured automatically to the 
		 * WAMC server's own address, but some firewall configurations might mean that appliances 
		 * need to use a different address to reach the server.
		 */
		String getWamtHttpAddress();

		/**
		 * The network interface on which WAMT should listen for incoming connections from XC10 
		 * appliances (see the explanation for wamtHttpPort). This will normally be configured 
		 * automatically, but systems with multiple interfaces on different networks might need 
		 * to set it manually.
		 */
		String getWamtHttpInterface();

		/**
		 * The fully-qualified path to a properties file containing arbitrary WAMT properties. The
		 * properties file must be called WAMT.properties; this sets the directory in which to find
		 * it. Valid property names are documented as constants in
		 * com.ibm.datapower.amt.clientAPI.Configuration. This will be read by WAMT and used to set
		 * any properties not explicitly set by WAMC.
		 */
		String getWamtConfigFilePath();

		/**
		 * A boolean value indicating whether WAMC should run in demo mode where a dummy WAMT
		 * command provider is used removing the requirement for interaction with real appliances.
		 */
		@Required
		@Default("false")
		boolean getDemoMode();
		
		/**
		 * String path for history database directory. Default value is relative to Liberty server
		 * directory and, in a full install, is in the WAMC logs directory.
		 */
		@Required
		@Default("db/history")
		String getHistoryDirectoryPath();
		
		/* *********************************************************************
		 *  End of properties to be documented
		 * ********************************************************************/
		
		// "Secret" internal / debug properties could be added at this point.
		
		/**
		 * Check that all properties have valid values (are strings/ints/booleans as appropriate,
		 * are present if Required, etc).
		 * 
		 * @throws PropertyException
		 *             if one or more properties are invalid.
		 */
		public void check() throws PropertyException;

	}

	private static final String PROPERTY_PREFIX = "com.ibm.amc.";

	/**
	 * The singleton instance of the properties. Although typed as a Props, behind that facade this
	 * is actually an instance of WamcProperties, via the invocation-handler mechanism rather than
	 * simple inheritance. Callers see it as a Props, with its simple methods to access properties;
	 * within this class, think of it as a WamcProperties following the normal singleton pattern.
	 */
	private static Props instance;		private static String location;

	private Properties properties;

	/**
	 * Obtain a Props instance populated from the wamc.properties file. This is the normal way for
	 * WAMC code to access its properties, eg:
	 * 
	 * <pre>
	 * WamcProperties.instance().getMyFunkyProperty();
	 * // or
	 * Props props = WamcProperties.instance();
	 * 
	 * doStuff(things, props.getConfigPropertyOne());
	 * doMoreStuff(props.getConfigPropertyTwo());
	 * if (props.getSomeBooleanProperty()) return &quot;foo&quot;;
	 * </pre>
	 * 
	 * It is recommended that Props objects not be kept around long-term, in case the values of
	 * properties change (the properties system is not the place for rapidly-changing pub/sub-style
	 * data, but still...). Generally, callers should either get a new instance each time they need
	 * to check a value (first example above) or get a Props, do a bunch of related stuff, and throw
	 * it away again (second example).
	 * 
	 * @return an implementation of the Props interface, which both defines the available properties
	 *         and their semantics, and provides access to them.
	 * @throws PropertyLoadException
	 *             if the properties have not already been loaded, and an attempt to load them for
	 *             this request fails.
	 */
	public static Props instance() throws PropertyLoadException
	{
		if (instance == null)
		{
			instance = (Props) Proxy.newProxyInstance(WamcProperties.class.getClassLoader(), new Class[] { Props.class }, new WamcProperties(null));
		}
		return instance;
	}

	/**
	 * <p>
	 * Override the properties mechanism with a specified set of values. Any subsequent calls to
	 * instance() will pick up the new values (pre-existing Props objects will not). This is
	 * intended primarily for unit testing of classes that use properties.
	 * </p>
	 * <p>
	 * Creating a new instance does not cause a check() to happen, so the Properties passed in need
	 * not include all Required properties.
	 * </p>
	 * 
	 * @param values
	 *            a Properties configured with the values required. If null, creates a new instance
	 *            that loads from a file as normal.
	 * @return a Props object providing access to the properties.
	 */
	public static Props newInstance(Properties values)
	{
		instance = (Props) Proxy.newProxyInstance(WamcProperties.class.getClassLoader(), new Class[] { Props.class }, new WamcProperties(values));
		return instance;
	}

	/**
	 * Private constructor; singleton pattern.
	 * 
	 * @param values
	 *            Null if values are to be loaded by reading the config file (the usual case). If
	 *            not null, the contents of this Properties are copied and used to populate this
	 *            WamcProperties.
	 * @throws PropertyLoadException
	 */
	private WamcProperties(Properties values) throws PropertyLoadException
	{
		if (values != null)
		{
			properties = (Properties) values.clone();
		}
		else
		{
			properties = new Properties();
			load();
		}
	}

	/**
	 * This method is called by the dynamic proxy system whenever the property methods in Props are
	 * invoked. Other code should not call this.
	 */
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
	{
		String methodName = method.getName();

		// Special-case for the check method. All others are get-property methods.
		if (methodName.equals("check"))
		{
			check();
			return null;
		}

		String propertyName = toPropertyName(methodName);

		String defaultValue = method.isAnnotationPresent(Default.class) ? method.getAnnotation(Default.class).value() : null;

		String result = properties.getProperty(propertyName, defaultValue);
		
		if(result == null && method.isAnnotationPresent(Required.class))
		{
			throw new PropertiesInvalidException(propertyName);
		}

		if (logger.isDebugEnabled()) logger.debug("invoke()", "Accessing WAMC property \""+propertyName+"\" with value:", result);

		if (method.getReturnType() == String.class)
		{
			return result;
		}

		if (method.getReturnType() == int.class || method.getReturnType() == Integer.class) // Might be Integer if we want to support nulls.
		{
			if(result == null) return null;
			
			// value will have been checked by check(), so no need to handle
			// NumberFormatExceptions here.
			return Integer.parseInt(result);
		}

		if (method.getReturnType() == boolean.class)
		{
			return Boolean.parseBoolean(result);
		}

		// Failure here is a programming error which should not happen in the field.
		throw new AmcRuntimeException(Status.INTERNAL_SERVER_ERROR, "CWZBA0514E_UNKNOWN_CONFIG_PROPERTY_TYPE", propertyName, method.getReturnType().getName());
	}

	/** Convert from "getFooProperty" to "com.ibm.amc.fooProperty" */
	private String toPropertyName(String methodName)
	{
		// character after the "get" must be lowercased
		char firstChar = Character.toLowerCase(methodName.charAt(3));
		// substring lops off the preceding "get"
		String propertyName = PROPERTY_PREFIX + firstChar + methodName.substring(4);
		return propertyName;
	}

	private void check() throws PropertyException
	{
		Method[] propertyDefinitions = Props.class.getDeclaredMethods();

		// Accumulate all errors in this exception, then throw it if necessary.
		PropertyException errors = new PropertyException();

		// Check each property in turn.
		for (Method propertyDefinition : propertyDefinitions)
		{
			String methodName = propertyDefinition.getName();

			// Ignore any non-property methods.
			if (!methodName.startsWith("get")) continue;
			// The com.ibm.amc.fooBarBaz style.
			String nameInFile = toPropertyName(methodName);

			// First check - if required (and has no default) is it present in
			// the file?
			if (propertyDefinition.isAnnotationPresent(Required.class) && !propertyDefinition.isAnnotationPresent(Default.class))
			{
				if (properties.getProperty(nameInFile) == null)
				{
					errors.addPropertyError(nameInFile, PropertyException.Error.MISSING);
					continue;
				}
			}

			// Further checks need a value to look at. If validly not specified,
			// stop checking this property and move on to the next.
			String value = properties.getProperty(nameInFile);
			if (value == null) continue;

			// If it's an integer, can we parse it?
			if (propertyDefinition.getReturnType().equals(int.class))
			{
				try
				{
					Integer.parseInt(value);
				}
				catch (NumberFormatException e)
				{
					errors.addPropertyError(nameInFile, PropertyException.Error.NOT_INTEGER);
				}
			}

			// Likewise for booleans.
			if (propertyDefinition.getReturnType().equals(boolean.class))
			{
				if (!(value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")))
				{
					errors.addPropertyError(nameInFile, PropertyException.Error.NOT_BOOLEAN);
				}
			}
		}

		if (errors.hasErrors()) throw errors;
	}

	private void load() throws PropertyLoadException
	{
		try
		{
			URL propertiesFile;			if(location != null)				propertiesFile = new File(location).toURI().toURL();			else				propertiesFile = findPropertiesFile().toURI().toURL();
			
			if(logger.isInfoEnabled()) logger.info("CWZBA0526I_LOADING_PROPS_FROM", propertiesFile.toString());
			properties.load(propertiesFile.openStream());
		}
		catch (IOException e)
		{
			throw new PropertyLoadException(e);
		}
		
	}
	public static void initialize(ServletContext sc)	{		location = sc.getInitParameter("com.ibm.amc.wamtPropertiesLocation");
		if(location == null)
			location="conf/wamc.properties";	}	
	/**
	 * Find the properties file. Strategy is to first load wamc.properties from 
	 * anywhere accessible to the current classloader. This is the original 
	 * behaviour, and will find files in dev environments etc. If this is not 
	 * found, we locate index.html as a "beacon" in the filesystem, and 
	 * backtrack from there up the known production-install tree to the config 
	 * directory. Icky, but if you've got a better idea let's hear it.
	 */
	public static File findPropertiesFile()
	{
		return new File(location);
	}
	
	/**
	 * <p>Load the properties, and check that their values are valid. This method
	 * should be called at startup; it can also be called while the application
	 * is running to re-read the file for new values.</p>
	 * 
	 * <p>In the re-read at runtime case, any existing Props instances held by 
	 * client code will keep their existing values (so sets of values that need 
	 * to vary together will remain atomic, provided the client code reads them
	 * together from the same Props) but any new instances requested will have 
	 * the new values.</p>
	 * 
	 * <p>If a re-read fails, the existing values remain in place.</p>
	 * 
	 * @throws PropertyLoadException
	 *             if there is an error reading the properties file
	 * @throws PropertyException
	 *             if the file is read, but one or more properties are invalid.
	 */
	public static void loadAndCheck() throws PropertyLoadException, PropertyException
	{
		Props newInstance = (Props) Proxy.newProxyInstance(WamcProperties.class.getClassLoader(), new Class[] { Props.class }, new WamcProperties(null));
		newInstance.check();
		// Only if check doesn't throw an exception:
		instance = newInstance;
	}

	/**
	 * Indicate that this property is required for WAMC to operate, and an error should be generated
	 * unless it is either set explicitly or has a default.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	private @interface Required
	{
	}

	/**
	 * Specify the default value of this property, which will be used if it is not set in the
	 * properties file. Properties do not need to have a default. Defaults must be specified as a
	 * String (as they would appear in the properties file) even if their logical type is Integer
	 * etc.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	private @interface Default
	{
		String value();
	}
}
