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
package com.ibm.amc.demo.provider;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.ibm.amc.Constants;
import com.ibm.amc.ras.Logger47;
import com.ibm.datapower.amt.AdminStatus;
import com.ibm.datapower.amt.DeviceType;
import com.ibm.datapower.amt.ModelType;
import com.ibm.datapower.amt.OpStatus;
import com.ibm.datapower.amt.OperationStatus;
import com.ibm.datapower.amt.QuiesceStatus;
import com.ibm.datapower.amt.StringCollection;
import com.ibm.datapower.amt.OperationStatus.Enumerated;
import com.ibm.datapower.amt.amp.AMPException;
import com.ibm.datapower.amt.amp.AMPIOException;
import com.ibm.datapower.amt.amp.Commands;
import com.ibm.datapower.amt.amp.ConfigObject;
import com.ibm.datapower.amt.amp.DeleteObjectResult;
import com.ibm.datapower.amt.amp.DeviceContext;
import com.ibm.datapower.amt.amp.DeviceExecutionException;
import com.ibm.datapower.amt.amp.DeviceMetaInfo;
import com.ibm.datapower.amt.amp.DomainStatus;
import com.ibm.datapower.amt.amp.ErrorReport;
import com.ibm.datapower.amt.amp.InterDependentServiceCollection;
import com.ibm.datapower.amt.amp.InvalidCredentialsException;
import com.ibm.datapower.amt.amp.NotExistException;
import com.ibm.datapower.amt.amp.PingResponse;
import com.ibm.datapower.amt.amp.ReferencedObjectCollection;
import com.ibm.datapower.amt.amp.SubscriptionResponseCode;
import com.ibm.datapower.amt.amp.SubscriptionState;
import com.ibm.datapower.amt.clientAPI.ConfigService;
import com.ibm.datapower.amt.clientAPI.DeletedException;
import com.ibm.datapower.amt.clientAPI.DeploymentPolicy;
import com.ibm.datapower.amt.clientAPI.RuntimeService;

import org.apache.commons.codec.binary.Base64InputStream;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

public class AmcDemoCommands implements Commands
{
	// @CLASS-COPYRIGHT@

	static Logger47 logger = Logger47.get(AmcDemoCommands.class.getCanonicalName(), Constants.CWZBA_BUNDLE_NAME);

	/**
	 * Map from appliance host name to set of names of quiesced domains.
	 */

	/**
	 * Map of devices keyed by host name.
	 */
	private static Map<String, Device> devices = Collections.synchronizedMap(new HashMap<String, Device>());

	public AmcDemoCommands(String soapHelperImplementationClassName) throws AMPException
	{
		if (logger.isEntryEnabled()) logger.entry("AmcDemoCommands", soapHelperImplementationClassName);
		if (logger.isEntryEnabled()) logger.exit("AmcDemoCommands");
	}

	@Override
	public SubscriptionResponseCode subscribeToDevice(DeviceContext device, String subscriptionId, StringCollection topics, URL callback) throws InvalidCredentialsException, AMPIOException,
			DeviceExecutionException, AMPException
	{
		if (logger.isEntryEnabled()) logger.entry("subscribeToDevice", device, subscriptionId, topics, callback);
		getDevice(device);
		final SubscriptionResponseCode result = SubscriptionResponseCode.ACTIVE;
		if (logger.isEntryEnabled()) logger.exit("subscribeToDevice", result);
		return result;
	}

	@Override
	public void unsubscribeFromDevice(DeviceContext device, String subscriptionID, StringCollection topics) throws NotExistException, InvalidCredentialsException, DeviceExecutionException,
			AMPIOException, AMPException
	{
		if (logger.isEntryEnabled()) logger.entry("unsubscribeFromDevice", device, subscriptionID, topics);
		getDevice(device);
		if (logger.isEntryEnabled()) logger.exit("unsubscribeFromDevice");
	}

	@Override
	public PingResponse pingDevice(DeviceContext device, String subscriptionID) throws InvalidCredentialsException, DeviceExecutionException, AMPIOException, AMPException
	{
		if (logger.isEntryEnabled()) logger.entry("pingDevice", device, subscriptionID);
		getDevice(device);
		final PingResponse result = new PingResponse(SubscriptionState.ACTIVE);
		if (logger.isEntryEnabled()) logger.exit("pingDevice", result);
		return result;
	}

	@Override
	public DeviceMetaInfo getDeviceMetaInfo(DeviceContext deviceContext) throws InvalidCredentialsException, DeviceExecutionException, AMPIOException, AMPException
	{
		if (logger.isEntryEnabled()) logger.entry("getDeviceMetaInfo", deviceContext);
		
		Device device = getDevice(deviceContext);
		final DeviceMetaInfo metaInfo = device.getMetaInfo();
		
		if (logger.isEntryEnabled()) logger.exit("getDeviceMetaInfo", metaInfo);
		return metaInfo;
	}

	@Override
	public void reboot(DeviceContext device) throws InvalidCredentialsException, DeviceExecutionException, AMPIOException, AMPException
	{
		if (logger.isEntryEnabled()) logger.entry("reboot", device);
		getDevice(device);
		unquiesceDevice(device);
		if (logger.isEntryEnabled()) logger.exit("reboot");
	}

	@Override
	public String[] getDomainList(DeviceContext deviceContext) throws InvalidCredentialsException, DeviceExecutionException, AMPIOException, AMPException
	{
		if (logger.isEntryEnabled()) logger.entry("getDomainList", deviceContext);
		final String[] result = getDevice(deviceContext).getDomainNames();
		if (logger.isEntryEnabled()) logger.exit("getDomainList", result);
		return result;
	}

	@Override
	public byte[] getDomain(DeviceContext device, String domainName) throws NotExistException, InvalidCredentialsException, DeviceExecutionException, AMPIOException, AMPException
	{
		if (logger.isEntryEnabled()) logger.entry("getDomain", device, domainName);
		getDevice(device);
		final byte[] result = new byte[] {};
		if (logger.isEntryEnabled()) logger.exit("getDomain", result);
		return result;
	}

	@Override
	public void setDomain(DeviceContext device, String domainName, byte[] domainImage, DeploymentPolicy policy) throws InvalidCredentialsException, DeviceExecutionException, AMPIOException,
			AMPException, DeletedException
	{
		if (logger.isEntryEnabled()) logger.entry("setDomain", device, domainName, domainImage, policy);
		getDevice(device).createDomain(domainName);
		if (logger.isEntryEnabled()) logger.exit("setDomain");
	}

	@Override
	public void deleteDomain(DeviceContext deviceContext, String domainName) throws NotExistException, InvalidCredentialsException, DeviceExecutionException, AMPIOException, AMPException
	{
		if (logger.isEntryEnabled()) logger.entry("deleteDomain", deviceContext, domainName);
		getDevice(deviceContext).deleteDomain(domainName);
		if (logger.isEntryEnabled()) logger.exit("deleteDomain");
	}

	@Override
	public DomainStatus getDomainStatus(DeviceContext deviceContext, String domainName) throws NotExistException, InvalidCredentialsException, DeviceExecutionException, AMPIOException
	{
		if (logger.isEntryEnabled()) logger.entry("getDomainStatus", deviceContext, domainName);
		Device.Domain domain = getDevice(deviceContext).getDomain(domainName);
		if (domain == null) throw new NotExistException();
		final DomainStatus status = domain.getStatus();
		if (logger.isEntryEnabled()) logger.exit("getDomainStatus", status);
		return status;
	}

	@Override
	public void startDomain(DeviceContext device, String domainName) throws NotExistException, InvalidCredentialsException, DeviceExecutionException, AMPIOException, AMPException
	{
		if (logger.isEntryEnabled()) logger.entry("startDomain", device, domainName);
		getDevice(device);
		if (logger.isEntryEnabled()) logger.exit("startDomain");
	}

	@Override
	public void stopDomain(DeviceContext device, String domainName) throws NotExistException, InvalidCredentialsException, DeviceExecutionException, AMPIOException, AMPException
	{
		if (logger.isEntryEnabled()) logger.entry("stopDomain", device, domainName);
		getDevice(device);
		if (logger.isEntryEnabled()) logger.exit("stopDomain");
	}

	@Override
	public void restartDomain(DeviceContext device, String domainName) throws NotExistException, InvalidCredentialsException, DeviceExecutionException, AMPIOException, AMPException
	{
		if (logger.isEntryEnabled()) logger.entry("restartDomain", device, domainName);
		getDevice(device);
		if (logger.isEntryEnabled()) logger.exit("restartDomain");
	}

	@Override
	public void setFirmware(DeviceContext device, byte[] firmwareImage) throws InvalidCredentialsException, DeviceExecutionException, AMPIOException, AMPException
	{
		if (logger.isEntryEnabled()) logger.entry("setFirmware", device, firmwareImage);
		getDevice(device);
		if (logger.isEntryEnabled()) logger.exit("setFirmware");
	}

	@Override
	public void setFirmware(DeviceContext deviceContext, InputStream inputStream) throws InvalidCredentialsException, DeviceExecutionException, AMPIOException, AMPException
	{
		if (logger.isEntryEnabled()) logger.entry("setFirmware", deviceContext, inputStream);
		final Device device = getDevice(deviceContext);

		String contents = null;
		String tagname = "firmwareRev";

		InputStreamReader inputStreamReader = null;
		BufferedReader bufferedReader = null;
		try
		{
			try
			{
				String openTag = "<" + tagname + ">";
				String closeTag = "</" + tagname + ">";

				inputStreamReader = new InputStreamReader(new Base64InputStream(inputStream), "ISO-8859-1");
				bufferedReader = new BufferedReader(inputStreamReader);
				while (bufferedReader.ready() && (contents == null))
				{
					String line = bufferedReader.readLine();
					if (line != null)
					{
						if (line.indexOf("-----BEGIN ") > -1)
						{
							break;
						}
						int openTagIndex = line.indexOf(openTag);
						if (openTagIndex > -1)
						{
							int closeTagIndex = line.lastIndexOf(closeTag);
							int beginIndex = openTagIndex + openTag.length();
							int endIndex = closeTagIndex;
							contents = line.substring(beginIndex, endIndex);
						}
					}
				}
			}
			finally
			{
				if (bufferedReader != null)
				{
					bufferedReader.close();
				}
				else if (inputStreamReader != null)
				{
					inputStreamReader.close();
				}
				else if (inputStream != null)
				{
					inputStream.close();
				}
			}
		}
		catch (Throwable e)
		{
			throw new AMPException(e);
		}

		if (contents == null)
		{
			throw new AMPException();
		}

		int periodIndex = contents.indexOf(".");
		String version = contents.substring(periodIndex + 1);
		
		device.setFirmwareLevel(version);
		unquiesceDevice(deviceContext);

		if (logger.isEntryEnabled()) logger.exit("setFirmware");
	}

	@Override
	public String[] getKeyFilenames(DeviceContext device, String domainName) throws InvalidCredentialsException, DeviceExecutionException, AMPIOException, AMPException
	{
		if (logger.isEntryEnabled()) logger.entry("getKeyFilenames", device, domainName);
		getDevice(device);
		final String[] result = new String[] {};
		if (logger.isEntryEnabled()) logger.exit("getKeyFilenames", result);
		return result;
	}

	@Override
	public void setFile(DeviceContext device, String domainName, String filenameOnDevice, byte[] contents) throws NotExistException, InvalidCredentialsException, DeviceExecutionException,
			AMPIOException, AMPException
	{
		if (logger.isEntryEnabled()) logger.entry("setFile", device, domainName, filenameOnDevice, contents);
		getDevice(device);
		if (logger.isEntryEnabled()) logger.exit("setFile");
	}

	@Override
	public boolean isDomainDifferent(String domainName, byte[] configImage1, byte[] configImage2, DeviceContext device) throws InvalidCredentialsException, DeviceExecutionException, AMPIOException,
			AMPException
	{
		if (logger.isEntryEnabled()) logger.entry("isDomainDifferent", configImage1, configImage2, device);
		getDevice(device);
		final boolean result = Arrays.equals(configImage1, configImage2);
		if (logger.isEntryEnabled()) logger.exit("isDomainDifferent", result);
		return result;
	}

	@Override
	public URL getDomainDifferences(String domainName, byte[] configImage1, byte[] configImage2, DeviceContext device) throws InvalidCredentialsException, DeviceExecutionException, AMPIOException,
			AMPException
	{
		if (logger.isEntryEnabled()) logger.entry("getDomainDifferences", domainName, configImage1, configImage2, device);
		getDevice(device);
		URL result = null;
		try
		{
			result = new URL("http://www.ibm.com");
		}
		catch (MalformedURLException e)
		{
		}
		if (logger.isEntryEnabled()) logger.exit("getDomainDifferences", result);
		return result;
	}

	@Override
	public ErrorReport getErrorReport(DeviceContext device) throws InvalidCredentialsException, DeviceExecutionException, AMPIOException, AMPException
	{
		if (logger.isEntryEnabled()) logger.entry("getErrorReport", device);
		getDevice(device);
		final ErrorReport result = null;
		if (logger.isEntryEnabled()) logger.exit("getErrorReport", result);
		return null;
	}

	@Override
	public String getSAMLToken(DeviceContext device, String domainName) throws InvalidCredentialsException, DeviceExecutionException, AMPIOException, AMPException
	{
		if (logger.isEntryEnabled()) logger.entry("device", domainName);
		getDevice(device);
		final String result = "";
		if (logger.isEntryEnabled()) logger.exit("device", result);
		return result;
	}

	@Override
	public Hashtable<String, byte[]> backupDevice(DeviceContext device, String cryptoCertificateName, byte[] cryptoImage, String secureBackupDestination, boolean includeISCSI, boolean includeRaid)
			throws AMPIOException, InvalidCredentialsException, AMPException
	{
		if (logger.isEntryEnabled()) logger.entry("backupDevice", device, cryptoCertificateName, cryptoImage, secureBackupDestination, includeISCSI, includeRaid);
		getDevice(device);
		final Hashtable<String, byte[]> result = new Hashtable<String, byte[]>();
		if (secureBackupDestination != null)
		{
			for (String domain : getDomainList(device))
			{
				result.put(domain + ".tgz", new byte[] {});
			}
			result.put("backupmanifest.xml", new byte[] {});
		}
		if (logger.isEntryEnabled()) logger.exit("backupDevice", result);
		return result;
	}

	@Override
	public void restoreDevice(DeviceContext device, String cryptoCredentialName, boolean validate, URI secureBackupSource, Hashtable<String, byte[]> backupFilesTable) throws AMPIOException,
			InvalidCredentialsException, AMPException
	{
		if (logger.isEntryEnabled()) logger.entry("restoreDevice", device, cryptoCredentialName, validate, secureBackupSource, backupFilesTable);
		getDevice(device);
		if (logger.isEntryEnabled()) logger.exit("restoreDevice");
	}

	@Override
	public void quiesceDomain(DeviceContext deviceContext, String domain, int timeout) throws AMPIOException, InvalidCredentialsException, AMPException
	{
		if (logger.isEntryEnabled()) logger.entry("quiesceDomain", deviceContext, domain, timeout);
		getDevice(deviceContext).getDomain(domain).quiesce();
		if (logger.isEntryEnabled()) logger.exit("quiesceDomain");
	}

	@Override
	public void unquiesceDomain(DeviceContext deviceContext, String domain) throws AMPIOException, InvalidCredentialsException, AMPException
	{
		if (logger.isEntryEnabled()) logger.entry("unquiesceDomain", deviceContext, domain);
		getDevice(deviceContext).getDomain(domain).unquiesce();
		if (logger.isEntryEnabled()) logger.exit("unquiesceDomain");
	}

	@Override
	public void quiesceDevice(DeviceContext device, int timeout) throws AMPIOException, InvalidCredentialsException, AMPException
	{
		if (logger.isEntryEnabled()) logger.entry("quiesceDevice", device, timeout);
		getDevice(device);
		for (String domain : getDomainList(device))
		{
			quiesceDomain(device, domain, timeout);
		}
		if (logger.isEntryEnabled()) logger.exit("quiesceDevice");
	}

	@Override
	public void unquiesceDevice(DeviceContext device) throws AMPIOException, InvalidCredentialsException, AMPException
	{
		if (logger.isEntryEnabled()) logger.entry("unquiesceDevice", device);
		getDevice(device);
		for (String domain : getDomainList(device))
		{
			unquiesceDomain(device, domain);
		}
		if (logger.isEntryEnabled()) logger.exit("unquiesceDevice");
	}

	private Device getDevice(DeviceContext deviceContext) throws InvalidCredentialsException, AMPIOException
	{
		if ("invalid".equals(deviceContext.getPassword()))
		{
			throw new InvalidCredentialsException();
		}
		if ("unknown".equals(deviceContext.getHostname()))
		{
			throw new AMPIOException(new UnknownHostException());
		}
		Device device = devices.get(deviceContext.getHostname()); 
		if (device == null)
		{
			device = new Device(deviceContext);
			devices.put(deviceContext.getHostname(), device);
		}
		return device;
	}

	private String getProperty(DeviceContext device, String name, String defaultValue)
	{
		for (String commas : device.getPassword().split(","))
		{
			String[] parts = commas.split("=");
			if (parts.length == 2)
			{
				if (name.equals(parts[0]))
				{
					return parts[1];
				}
			}
		}
		return defaultValue;
	}

	@Override
	public void deleteFile(DeviceContext device, String domainName, String fileNameOnDevice) throws InvalidCredentialsException, DeviceExecutionException, AMPIOException, AMPException
	{
		if (logger.isEntryEnabled()) logger.entry("deleteFile", device, domainName, fileNameOnDevice);
		if (logger.isEntryEnabled()) logger.exit("deleteFile");
	}

	@Override
	public ConfigService[] getServiceListFromExport(DeviceContext device, byte[] packageImage) throws NotExistException, InvalidCredentialsException, DeviceExecutionException, AMPIOException,
			AMPException
	{
		if (logger.isEntryEnabled()) logger.entry("getServiceListFromExport", device, packageImage);
		final ConfigService[] result = new ConfigService[4];
		result[0] = new ConfigService("serviceA", "WSGateway", "Web Service Proxy", "");
		result[1] = new ConfigService("serviceB", "WSGateway", "Web Service Proxy", "");
		result[2] = new ConfigService("serviceC", "WSGateway", "Web Service Proxy", "");
		result[3] = new ConfigService("serviceD", "WSGateway", "Web Service Proxy", "");
		if (logger.isEntryEnabled()) logger.exit("getServiceListFromExport", result);
		return result;
	}

	@Override
	public ConfigService[] getServiceListFromExport(DeviceContext device, String fileDomainName, String fileNameOnDevice) throws NotExistException, InvalidCredentialsException,
			DeviceExecutionException, AMPIOException, AMPException
	{
		if (logger.isEntryEnabled()) logger.entry("getServiceListFromExport", device, fileDomainName, fileNameOnDevice);
		final ConfigService[] result = getServiceListFromExport(device, new byte[0]);
		if (logger.isEntryEnabled()) logger.exit("getServiceListFromExport", result);
		return result;
	}

	@Override
	public void quiesceService(DeviceContext device, String domainName, ConfigObject[] objects, int timeout) throws InvalidCredentialsException, DeviceExecutionException, AMPIOException, AMPException
	{
		if (logger.isEntryEnabled()) logger.entry("quiesceService", device, domainName, objects, timeout);
		Device.Domain domain = getDevice(device).getDomain(domainName);
		for  (ConfigObject object: objects)
		{
			domain.getService(object.getName()).quiesce();
		}
		if (logger.isEntryEnabled()) logger.exit("quiesceService");
	}

	@Override
	public void setDomainByService(DeviceContext device, String domainName, ConfigObject[] objects, byte[] domainImage, DeploymentPolicy policy, boolean importAllFiles)
			throws InvalidCredentialsException, DeviceExecutionException, AMPIOException, AMPException, DeletedException
	{
		if (logger.isEntryEnabled()) logger.entry("setDomainByService", device, domainName, objects, domainImage, policy, importAllFiles);
		Device.Domain domain = getDevice(device).getDomain(domainName);
		for  (ConfigObject object: objects)
		{
			domain.createService(object.getName());
		}
		if (logger.isEntryEnabled()) logger.exit("setDomainByService");
	}

	@Override
	public void setDomainByService(DeviceContext device, String domainName, ConfigObject[] objects, String fileDomainName, String fileNameOnDevice, DeploymentPolicy policy, boolean importAllFiles)
			throws InvalidCredentialsException, DeviceExecutionException, AMPIOException, AMPException, DeletedException
	{
		if (logger.isEntryEnabled()) logger.entry("setDomainByService", device, domainName, objects, fileDomainName, fileNameOnDevice, policy, importAllFiles);
		setDomainByService(device, domainName, objects, new byte[0], policy, importAllFiles);
		if (logger.isEntryEnabled()) logger.exit("setDomainByService");
	}

	@Override
	public void setFirmware(DeviceContext device, byte[] firmwareImage, boolean acceptLicense) throws InvalidCredentialsException, DeviceExecutionException, AMPIOException, AMPException
	{
		if (logger.isEntryEnabled()) logger.entry("setFirmware", device, firmwareImage, acceptLicense);
		this.setFirmware(device, firmwareImage);
		if (logger.isEntryEnabled()) logger.exit("setFirmware");
	}

	@Override
	public void setFirmware(DeviceContext device, InputStream inputStream, boolean acceptLicense) throws InvalidCredentialsException, DeviceExecutionException, AMPIOException, AMPException
	{
		if (logger.isEntryEnabled()) logger.entry("setFirmware", device, inputStream, acceptLicense);
		this.setFirmware(device, inputStream);
		if (logger.isEntryEnabled()) logger.exit("setFirmware");

	}

	@Override
	public void startService(DeviceContext device, String domainName, ConfigObject[] objects) throws InvalidCredentialsException, DeviceExecutionException, AMPIOException, AMPException
	{
		if (logger.isEntryEnabled()) logger.entry("startService", device, domainName, objects);
		if (logger.isEntryEnabled()) logger.exit("startService");
	}

	@Override
	public void stopService(DeviceContext device, String domainName, ConfigObject[] objects) throws InvalidCredentialsException, DeviceExecutionException, AMPIOException, AMPException
	{
		if (logger.isEntryEnabled()) logger.entry("stopService", device, domainName, objects);
		if (logger.isEntryEnabled()) logger.exit("stopService");
	}

	@Override
	public void unquiesceService(DeviceContext device, String domainName, ConfigObject[] objects) throws InvalidCredentialsException, DeviceExecutionException, AMPIOException, AMPException
	{
		if (logger.isEntryEnabled()) logger.entry("unquiesceService", device, domainName, objects);
		Device.Domain domain = getDevice(device).getDomain(domainName);
		for  (ConfigObject object: objects)
		{
			domain.getService(object.getName()).unquiesce();
		}
		if (logger.isEntryEnabled()) logger.exit("unquiesceService");
	}

	@Override
	public DeleteObjectResult[] deleteService(DeviceContext device, String domainName, String objectName, String objectClassName, ConfigObject[] excludeObjects, boolean deleteReferencedFiles)
			throws NotExistException, InvalidCredentialsException, DeviceExecutionException, AMPIOException, AMPException
	{
		if (logger.isEntryEnabled()) logger.entry("deleteService", device, domainName, objectName, objectClassName, excludeObjects, deleteReferencedFiles);
		getDevice(device).getDomain(domainName).deleteService(objectName);
		final DeleteObjectResult[] result = new DeleteObjectResult[0];
		if (logger.isEntryEnabled()) logger.exit("deleteService", result);
		return result;
	}

	@Override
	public InterDependentServiceCollection getInterDependentServices(DeviceContext device, String domainName, byte[] packageImage, ConfigObject[] objectArray) throws NotExistException,
			InvalidCredentialsException, DeviceExecutionException, AMPIOException, AMPException
	{
		if (logger.isEntryEnabled()) logger.entry("getInterDependentServices", device, domainName, packageImage, objectArray);
		List<RuntimeService> services = new ArrayList<RuntimeService>();
		Map<String, ConfigObject> objects = new HashMap<String, ConfigObject>();
		for (Device.Domain.Service service: getDevice(device).getDomain(domainName).getServices())
		{
			services.add(service.getRuntimeService());
			for (ConfigObject object: service.getObjects())
			{
				if (!object.getName().startsWith(service.getName())) objects.put(object.getName() + ":" + object.getClassName(), object);
			}
		}
		
		final InterDependentServiceCollection result = new InterDependentServiceCollection(services, new ArrayList<ConfigObject>(objects.values()), StringCollection.EMPTY);
		if (logger.isEntryEnabled()) logger.exit("getInterDependentServices", result);
		return result;
	}

	@Override
	public InterDependentServiceCollection getInterDependentServices(DeviceContext device, String domainName, String fileDomainName, String fileNameOnDevice, ConfigObject[] objects)
			throws NotExistException, InvalidCredentialsException, DeviceExecutionException, AMPIOException, AMPException
	{
		if (logger.isEntryEnabled()) logger.entry("getInterDependentServices", device, domainName, fileDomainName, fileNameOnDevice, objects);
		final InterDependentServiceCollection result = getInterDependentServices(device, domainName, new byte[0], objects);
		if (logger.isEntryEnabled()) logger.exit("getInterDependentServices", result);
		return result;
	}

	@Override
	public ReferencedObjectCollection getReferencedObjects(DeviceContext device, String domainName, String objectName, String objectClassName) throws NotExistException, InvalidCredentialsException,
			DeviceExecutionException, AMPIOException, AMPException
	{
		if (logger.isEntryEnabled()) logger.entry("getReferencedObjects", device, domainName, objectName, objectClassName);
		final Device.Domain.Service service = getDevice(device).getDomain(domainName).getService(objectName);
		@java.lang.SuppressWarnings({ "rawtypes", "unchecked" }) // WAMT oddness
		final HashMap<String, ArrayList<ConfigObject>> referencedObjects = new HashMap(service.getReferencedObjects());
		if (logger.isDebugEnabled()) logger.debug("getReferencedObjects", "Referenced objects: {0}", referencedObjects);
		final ReferencedObjectCollection result = new ReferencedObjectCollection(referencedObjects, StringCollection.EMPTY, objectName + ":" + objectClassName);
		if (logger.isEntryEnabled()) logger.exit("getReferencedObjects", result);
		return result;
	}

	@Override
	public RuntimeService[] getServiceListFromDomain(DeviceContext deviceContext, String domainName) throws DeviceExecutionException, AMPIOException, AMPException
	{
		if (logger.isEntryEnabled()) logger.entry("getServiceListFromDomain", deviceContext, domainName);
		final RuntimeService[] result;
		if ("default".equals(domainName))
		{
			result = null;
		}
		else
		{
			Collection<Device.Domain.Service> services = getDevice(deviceContext).getDomain(domainName).getServices();
			result = new RuntimeService[services.size()];
			Iterator<Device.Domain.Service> iterator = services.iterator();
			for (int i = 0; iterator.hasNext(); i++)
			{
				result[i] = iterator.next().getRuntimeService();
			}
		}
		if (logger.isEntryEnabled()) logger.exit("getServiceListFromDomain", result);
		return result;
	}
	
	private class Device
	{
		
		private final String hostname;
		private final String serialNumber;
		@SuppressWarnings(justification = "Fixed string now but may change in future", value = {"SS"})
		private final String hardwareOptions = "";
		private final DeviceType type;
		private final ModelType model;
		private final int[] supportedCommands;
		private final String secureBackup;
		private final String[] features;
		private String firmwareLevel;
		private String ampVersion;
		
		private final Map<String, Domain> domains = Collections.synchronizedMap(new HashMap<String, Domain>());
		
		public Device(DeviceContext deviceContext)
		{
			hostname = deviceContext.getHostname();
			type = DeviceType.fromString(getProperty(deviceContext, "t", DeviceType.XI52.getDisplayName()));
			model = ModelType.fromString(getProperty(deviceContext, "m", ModelType.TYPE_7199.getDisplayName()));
			serialNumber = model.equals(ModelType.TYPE_5725) ? "0000000" : String.format("%07d", hostname.hashCode());

			if (type.equals(DeviceType.XC10))
			{
				setFirmwareLevel(getProperty(deviceContext, "l", "2.1.0.0"));
				supportedCommands = new int[] { Commands.GET_DEVICE_METAINFO, Commands.SET_FIRMWARE_IMAGE };
				secureBackup = null;
				features = new String[] {};
			}
			else
			{
				setFirmwareLevel(getProperty(deviceContext, "l", "5.0.0.0"));
				supportedCommands = new int[] { Commands.BACKUP_DEVICE, Commands.DELETE_DOMAIN, Commands.DELETE_FILE, Commands.DELETE_SERVICE, Commands.GET_DEVICE_METAINFO, Commands.GET_DOMAIN,
						Commands.GET_DOMAIN_DIFFERENCES, Commands.GET_DOMAIN_LIST, Commands.GET_DOMAIN_STATUS, Commands.GET_ERROR_REPORT, Commands.GET_INTERDEPENDENT_SERVICES_FILE,
						Commands.GET_INTERDEPENDENT_SERVICES_IMAGE, Commands.GET_KEY_FILENAMES, Commands.GET_REFERENCED_OBJECTS, Commands.GET_SAML_TOKEN, Commands.GET_SERVICE_LIST_FROM_DOMAIN,
						Commands.GET_SERVICE_LIST_FROM_EXPORT_IMAGE, Commands.GET_SERVICE_LIST_FROM_EXPORT_FILE, Commands.IS_DOMAIN_DIFFERENT, Commands.PING_DEVICE, Commands.QUIESCE_DEVICE,
						Commands.QUIESCE_DOMAIN, Commands.QUIESCE_SERVICE, Commands.REBOOT, Commands.RESTART_DOMAIN, Commands.RESTORE_DEVICE, Commands.SET_DOMAIN, Commands.SET_DOMAIN_BY_SERVICE_FILE,
						Commands.SET_DOMAIN_BY_SERVICE_IMAGE, Commands.SET_FILE, Commands.SET_FIRMWARE_IMAGE, Commands.SET_FIRMWARE_IMAGE_ACCEPT_LICENSE, Commands.SET_FIRMWARE_STREAM,
						Commands.SET_FIRMWARE_STREAM_ACCEPT_LICENSE, Commands.START_DOMAIN, Commands.START_SERVICE, Commands.STOP_DOMAIN, Commands.STOP_SERVICE, Commands.SUBSCRIBE_TO_DEVICE,
						Commands.UNSUBSCRIBE_FROM_DEVICE, Commands.UNQUIESCE_DEVICE, Commands.UNQUIESCE_DOMAIN, Commands.UNQUIESCE_SERVICE };
				secureBackup = "enabled";
				features = new String[] { "MQ", "DataGlue", "JAXP-API", "PKCS7-SMIME", "WebSphere-JMS", "RaidVolume", "iSCSI", "LocateLED", "IPMI", "RaidVolumeSr", "IntrusionDetection", "IPMI-LAN" };

				createDomain("default");
				createDomain("crm");
				createDomain("payroll");
				createDomain("sales");
			}
		}

		public void deleteDomain(String domainName)
		{
			domains.remove(domainName);
		}
		
		public void createDomain(String domainName)
		{
			domains.put(domainName, new Domain(domainName));
		}

		public String[] getDomainNames()
		{
			return domains.keySet().toArray(new String[domains.size()]);
		}

		public Domain getDomain(String domainName)
		{
			return domains.get(domainName);
		}

		public DeviceMetaInfo getMetaInfo()
		{
			return new DeviceMetaInfo(hostname, serialNumber, ampVersion, model, hardwareOptions, 9090, type, firmwareLevel, new StringCollection(features),
					supportedCommands, secureBackup);
		}
		
		public void setFirmwareLevel(String level)
		{
			firmwareLevel = level;
			ampVersion = type.equals(DeviceType.XC10) ? "" : (firmwareLevel.startsWith("5.") ? "3.0" : "2.0");
		}
		
		public class Domain
		{
			private final Map<String, Service> services = Collections.synchronizedMap(new HashMap<String, Service>());
			private QuiesceStatus quiesceStatus = QuiesceStatus.NORMAL;
			private Enumerated opStatus = Enumerated.UP;
			
			private Domain(String name)
			{
				if (!"default".equals(name) && "3.0".equals(ampVersion))
				{
					createService("serviceA");
					createService("serviceB");
					createService("serviceC");
				}
			}
			
			public Service getService(String name) {
				return services.get(name);
			}

			public void createService(String name)
			{
				services.put(name, new Service(name));
			}

			public void deleteService(String name)
			{
				services.remove(name);
			}

			public Collection<Service> getServices()
			{
				return services.values();
			}

			public DomainStatus getStatus()
			{
				return new DomainStatus(new OperationStatus(opStatus), false, false, quiesceStatus);
			}

			public void quiesce()
			{
				for (Service service: services.values())
				{
					service.quiesce();
				}
				quiesceStatus = QuiesceStatus.QUIESCED;
				opStatus = Enumerated.DOWN;
			}

			public void unquiesce()
			{
				for (Service service: services.values())
				{
					service.unquiesce();
				}
				quiesceStatus = QuiesceStatus.NORMAL;
				opStatus = Enumerated.UP;
			}	
			
			@SuppressWarnings(justification = "Child of domain - just doesn't make use of that right now", value = {"SIC"})
			public class Service
			{
				private final String name;
				private OpStatus opStatus = OpStatus.UP;
				private QuiesceStatus quiesceStatus = QuiesceStatus.NORMAL;
				
				private Service(String name)
				{
					this.name = name;
				}

				public String getName()
				{
					return name;
				}

				public void quiesce()
				{
					quiesceStatus = QuiesceStatus.QUIESCED;
					if (!"default".equals(name)) opStatus = OpStatus.DOWN;
				}

				public void unquiesce()
				{
					quiesceStatus = QuiesceStatus.NORMAL;
					if (!"default".equals(name)) opStatus = OpStatus.UP;
				}	

				public RuntimeService getRuntimeService()
				{
					return new RuntimeService(name, "WSGateway", "Web Service Proxy", "", AdminStatus.ENABLED, opStatus, false, quiesceStatus);
				}		
				
				public ConfigObject[] getObjects()
				{
					final ConfigObject[] objects = new ConfigObject[15];
					objects[0] = new ConfigObject(name, "WSGateway", "Web Service Proxy", "");
					objects[1] = new ConfigObject("policy", "PolicyParameters", "Policy Parameters", "");
					objects[2] = new ConfigObject(name + "_service.wsdl", "PolicyAttachments", "Policy Attachment", "");
					objects[3] = new ConfigObject(name, "WSStylePolicy", "WS-Proxy Processing Policy", "");
					objects[4] = new ConfigObject(name + "_default_response-rule", "WSStylePolicyRule", "WS-Proxy Processing Rule", "");
					objects[5] = new ConfigObject(name + "_default_response-rule_defaultaction_result", "StylePolicyAction", "Processing Action", "");
					objects[6] = new ConfigObject(name + "_default_request-rule", "WSStylePolicyRule", "WS-Proxy Processing Rule", "");
					objects[7] = new ConfigObject(name + "_default_request-rule_defaultaction_result", "StylePolicyAction", "Processing Action", "");
			        objects[8] = new ConfigObject(name + "_default_request-rule_defaultaction_slm", "StylePolicyAction", "Processing Action", "");
			        objects[9] = new ConfigObject(name, "SLMPolicy", "SLM Policy", "");
			        objects[10] = new ConfigObject(name + "_match_all", "Matching", "Matching Rule", "");
			        objects[11] = new ConfigObject(name, "WSEndpointRewritePolicy", "WS-Proxy Endpoint Rewrite", "");
			        objects[12] = new ConfigObject(name + "_fsh", "HTTPSourceProtocolHandler", "HTTP Front Side Handler", "");
			        objects[13] = new ConfigObject("default", "XMLManager", "XML Manager", "");
			        objects[14] = new ConfigObject("default", "HTTPUserAgent", "User Agent", "");
			        
			        objects[0].setReferencedObject(new ConfigObject[] {objects[13], objects[11], objects[3], objects[2], objects[1]});
			        objects[3].setReferencedObject(new ConfigObject[] {objects[10], objects[6], objects[4]});
			        objects[4].setReferencedObject(new ConfigObject[] {objects[5]});
			        objects[6].setReferencedObject(new ConfigObject[] {objects[7], objects[8]});
			        objects[8].setReferencedObject(new ConfigObject[] {objects[9]});
			        objects[11].setReferencedObject(new ConfigObject[] {objects[12]});
			        objects[13].setReferencedObject(new ConfigObject[] {objects[14]});			        
			        
			        return objects;
				}
				
				public Map<String, List<ConfigObject>> getReferencedObjects()
				{
					final Map<String, List<ConfigObject>> objects = new HashMap<String, List<ConfigObject>>();
					for (ConfigObject object: getObjects())
					{
						final List<ConfigObject> referencedObjects = (object.getReferencedObjects() == null) ? new ArrayList<ConfigObject>() : new ArrayList<ConfigObject>(Arrays.asList(object.getReferencedObjects()));
						objects.put(object.getName() + ":" + object.getClassName(), referencedObjects);
					}
					return objects;
				}
			}	
			
		}

	}
	


}
