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
package com.ibm.amc.server;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ibm.amc.resources.data.Appliance;
import com.ibm.amc.resources.data.Domain;
import com.ibm.amc.resources.data.DomainDeploymentConfiguration;
import com.ibm.amc.resources.data.Firmware;
import com.ibm.amc.resources.data.Group;
import com.ibm.amc.resources.data.Service;
import com.ibm.amc.resources.data.ServiceDeploymentConfiguration;
import com.ibm.amc.resources.data.ServiceDescriptor;
import com.ibm.amc.resources.data.ServiceImpactDescriptor;
import com.ibm.amc.resources.data.ServiceObject;
import com.ibm.amc.resources.exceptions.ApplianceConnectionException;
import com.ibm.amc.resources.exceptions.NoSuchResourceException;

/**
 * Main WAMC logic will reside in the Controller (eg any memory caching will go here).
 * 
 * @author mallman
 */
public interface Controller
{
	// @CLASS-COPYRIGHT@

	/**
	 * Add a new appliance
	 * 
	 * @param applianceConnection
	 *            connection details for the appliance
	 * @return the full details of the appliance
	 * @throws ApplianceConnectionException
	 *             if a connection to the appliance could not be obtained
	 */
	public Appliance addAppliance(Appliance applianceConnection) throws ApplianceConnectionException;

	/**
	 * Removes an existing appliance.
	 * 
	 * @param applianceId
	 *            the identifier for the appliance
	 * @throws NoSuchResourceException
	 *             if the specified applianceId does not refer to a known appliance
	 */
	public void removeAppliance(String applianceId) throws NoSuchResourceException;

	/**
	 * Obtain a list of appliances that have been defined. If query parameters
	 * are provided, filter the results using these parameters.
	 * 
	 * @param queryParams filter the list based on query parameters. Null is permitted
	 * 		filtering is not required
	 * @return the list of appliances
	 */
	public List<Appliance> getAppliances(Set<Map.Entry<String, List<String>>> queryParams);

	/**
	 * Obtain a particular appliance based on its ID ("serial number" in the case of WAMT)
	 * 
	 * @return the requested appliance
	 * @throws NoSuchResourceException
	 *             if the specified applianceId does not refer to a known appliance.
	 */
	public Appliance getApplianceById(String applianceId) throws NoSuchResourceException;

	/**
	 * Update the connection properties of a known appliance. If the appliance is not known, an
	 * exception will be thrown.
	 * 
	 * <p>
	 * Only properties that are valid to update will be processed, if additional properties are
	 * provided they will be ignored. The valid set is limited to:
	 * <ul>
	 * <li>Host name</li>
	 * <li>AMP port</li>
	 * <li>Admin user ID</li>
	 * <li>Admin user password</li>
	 * <li>quiesce timeout</li>
	 * </ul>
	 * 
	 * <p>
	 * If a valid property is not provided it will be ignored during the update (it will not be
	 * unset). To do this the property should be present in the request with a value of empty
	 * string/zero etc.
	 * 
	 * @param applianceId
	 *            the ID of the appliance to update
	 * @param applianceConnection
	 *            the properties to update
	 * @return the details of the appliance that the connection properties have been updated
	 * @throws NoSuchResourceException
	 *             if the supplied ID is not known
	 */
	public Appliance updateAppliance(String applianceId, Appliance applianceConnection) throws NoSuchResourceException;

	/**
	 * Submits an asynchronous request to quiesce an appliance.
	 * 
	 * @param applianceId
	 *            the ID of the appliance to quiesce
	 * @return the ID of the asynchronous action
	 */
	public String quiesceAppliance(String applianceId);

	/**
	 * Submits an asynchronous request to unquiesce an appliance.
	 * 
	 * @param applianceId
	 *            the ID of the appliance to unquiesce
	 * @return the ID of the asynchronous action
	 */
	public String unquiesceAppliance(String applianceId);

	/**
	 * Submits an asynchronous request to backup an appliance using a certificate on the appliance
	 * for encryption.
	 * 
	 * @param applianceId
	 *            the ID of the appliance to backup
	 * @param certificateName
	 *            the name of the certificate to use for the backup
	 * @param backupDestination
	 *            the destination folder for the backup
	 * @param includeIsci
	 *            whether iSCSI data should be included in the backup
	 * @param includRaid
	 *            whether RAID data should be included in the backup
	 * @return the ID of the asynchronous action
	 */
	public String backupAppliance(String applianceId, String certificateName, URI backupDestination, boolean includeIscsi, boolean includeRaid);

	/**
	 * Submits an asynchronous request to backup an appliance using a certificate remote from the
	 * appliance for encryption.
	 * 
	 * @param applianceId
	 *            the ID of the appliance to backup
	 * @param certificateLocation
	 *            the location of the certificate to use for the backup
	 * @param backupDestination
	 *            the destination folder for the backup
	 * @param includeIsci
	 *            whether iSCSI data should be included in the backup
	 * @param includRaid
	 *            whether RAID data should be included in the backup
	 * @return the ID of the asynchronous action
	 */
	public String backupAppliance(String applianceId, URI certificateLocation, URI backupDestination, boolean includeIscsi, boolean includeRaid);

	/**
	 * Submits an asynchronous request to restore an appliance.
	 * 
	 * @param applianceId
	 *            the ID of the appliance to restore
	 * @param credentialName
	 *            the name of the credential on the appliance to decrypt the backup
	 * @param backupSource
	 *            the location of the backup to restore
	 * @return the ID of the asynchronous action
	 */
	public String restoreAppliance(String applianceId, String credentialName, URI backupSource);

	/**
	 * Get a list of all domains on all known devices. If query parameters
	 * are provided, filter the results using these parameters.
	 * 
	 * @param queryParams filter the list based on query parameters. Null is permitted
	 * 		filtering is not required
	 * @return the list of domains
	 */
	public List<Domain> getDomains(Set<Map.Entry<String, List<String>>> queryParams);

	/**
	 * Get all domains that have the given name. This may be across all 
	 * appliances, or only a subset.
	 * 
	 * @param applianceIds If null, return domains from all appliances. If not
	 * null, a list of appliance IDs from which the desired domains must come.
	 * @param domainName The name that the desired domains share.
	 * @return the requested domains. If no domains match, an empty list.
	 */
	public List<Domain> getDomainsByName(String domainName, List<String> applianceIds) throws NoSuchResourceException;
	
	/**
	 * Quiesce a domain.
	 */
	public String quiesceDomain(String applianceId, String domainName);
	
	/**
	 * Unquiesce a domain.
	 */
	public String unquiesceDomain(String applianceId, String domainName);
	
	/**
	 * Update a domain. The appliance must also be specified because domain names are only
	 * unique on a single appliance.
	 * 
	 * <p>
	 * Only properties that are valid to update will be processed, if additional properties are
	 * provided they will be ignored. The valid set is limited to:
	 * <ul>
	 * <li>Synchronization Mode</li>
	 * <li>Quiesce Timeout</li>
	 * </ul>
	 * 
	 * <p>
	 * If a valid property is not provided it will be ignored during the update (it will not be
	 * unset). To do this the property should be present in the request with a value of empty
	 * string/zero etc.
	 * 
	 * @param applianceId
	 * 			The identifier for the appliance on which the domain resides
	 * @param domainName
	 * 			The simple name of the domain to be updated
	 * @param domain
	 * 			The domain to be updated
	 * @return
	 * 			The domain following the update operation
	 * @throws NoSuchResourceException
	 * 			In the event that a domain with the supplied identifiers is now known
	 */
	public Domain updateDomain(String applianceId, String domainName, Domain domain) throws NoSuchResourceException;
	
	/**
	 * Delete a domain from an appliance.
	 */
	public void deleteDomain(String applianceId, String domainName);

	/**
	 * Get a domain by name. The appliance must also be specified because domain names are only
	 * unique on a single appliance. Only managed domains can be retrieved by this method.
	 * 
	 * @param applianceId
	 *            The identifier for the appliance on which the required domain resides.
	 * @param domainName
	 *            The simple name of the required domain.
	 * @return The specified domain.
	 */
	public Domain getDomainFromAppliance(String applianceId, String domainName) throws NoSuchResourceException;

	/**
	 * Get the list of domains for a specific appliance.
	 * 
	 * @param applianceId
	 *            the identifier of the appliance to obtain the list of domains
	 * @return the list of domains
	 */
	public List<Domain> getDomainsFromAppliance(String applianceId);
	
	/**
	 * Update the groups for a domain.
	 * 
	 * @param applianceId the appliance hosting the domain
	 * @param domainName the domain name
	 * @param groups the groups the domain is a member of
	 */
	public void updateDomainGroups(String applianceId, String domainName, List<String> groups);
	
	/**
	 * Get the status for a specific appliance.
	 * 
	 * @param applianceId
	 *            the identifier for the appliance
	 * @return the status of the applaince
	 */
	public String getApplianceStatus(String applianceId);

	/**
	 * Update the groups for a specific appliance
	 * 
	 * @param applianceId
	 *            the identifier for the appliance
	 * @param groups
	 *            A list of groups the appliance belongs to
	 */
	public void updateApplianceGroups(String applianceId, List<String> groups);
	
	/**
	 * Submit an asynchronous request to add a firmware version.
	 * 
	 * @param url
	 *            The URL of the firmware image
	 * @param userComment
	 *            Any user comments to be attached to the firmware version
	 * @return The ID of the asynchronous action
	 */
	public String addFirmware(URL url, String userComments);

	/**
	 * Remove a firmware version
	 * 
	 * @param firmwareId
	 *            The ID of the firmware version to remove
	 */
	public void removeFirmware(String firmwareId);

	/**
	 * Get a list of all the firmware versions that have been defined. If query parameters
	 * are provided, filter the results using these parameters.
	 * 
	 * @param queryParams filter the list based on query parameters. Null is permitted
	 * 		filtering is not required
	 * @return The list of all the firmware versions
	 */
	public List<Firmware> getFirmware(Set<Map.Entry<String, List<String>>> queryParams);
	
	/**
	 * Get a specific firmware version by ID
	 * 
	 * @param firmwareId
	 *            The ID of the firmware version
	 * @return The specified firmware version
	 */
	public Firmware getFirmwareById(String firmwareId);

	/**
	 * Update editable fields of a firmware object.
	 * 
	 * @param firmwareId
	 *            the identifier of the firmware object to update
	 * @param firmware
	 *            the new firmware details
	 * @return the updated firmware details
	 */
	public Firmware updateFirmware(String firmwareId, Firmware firmware);

	/**
	 * Get a list of firmwares which are available in the inventory and are applicable to a given
	 * appliance.
	 * 
	 * @param applianceId
	 *            The ID of the appliance for which a firmware must be applicable to be included in
	 *            the list.
	 * @return List of applicable firmware.
	 */
	public List<Firmware> getAvailableFirmware(String applianceId);

	/**
	 * Deploy the firmware described by the given ID (which must already exist in the repository).
	 * 
	 * @param applianceId
	 *            The ID of the device to deploy to.
	 * @param targetFirmwareId
	 *            The primary key of the firmware to deploy.
	 * @param licenceAccepted
	 *            a flag to indicate if the licence has been accepted
	 * @return The ID of the Action used to deploy the firmware.
	 */
	public String deployFirmware(String applianceId, String targetFirmwareId, boolean licenceAccepted);

	/**
	 * Deploy a domain configuration on a particular appliance. If a domain of
	 * the given name already exists then it is overwritten, otherwise it is
	 * created.
	 * @param deploymentSource The domain configuration source and deployment 
	 * policy needed to do the deployment.
	 * @return An action ID for the deployment operation.
	 */
	public String deployDomain(String applianceId, String domainName, DomainDeploymentConfiguration deploymentSource);

	/**
	 * Get a list of all services on all known devices. If query parameters
	 * are provided, filter the results using these parameters.
	 * 
	 * @param queryParams filter the list based on query parameters. Null is permitted
	 * 		filtering is not required
	 */
	public List<Service> getServices(Set<Map.Entry<String, List<String>>> queryParams);
	
	/**
	 * Retrieves a service on a particular appliance.
	 * 
	 * @param applianceId the appliance identifier
	 * @param domainName the domain name
	 * @param type the type of the service
	 * @param name the name of the service
	 * @return the service
	 */
	public Service getService(String applianceId, String domainName, String type, String name);
	
	/**
	 * Deploy a service of a particular type to a named domain on a specific
	 * appliance. If the service already exists it will be overwritten otherwise
	 * it will be created.
	 *
	 * @param applianceId the appliance identifier
	 * @param domainName the name of the target domain
	 * @param type the class of service to be deployed
	 * @param name the name of the service to be deployed
	 * @param deploymentSource the source of the service and deployment policy (optional)
	 * @return the action ID for the deployment operation
	 */
	public String deployService(String applianceId, String domainName, String type, String name, ServiceDeploymentConfiguration deploymentSource);

	/**
	 * If a service is deployed, detail what impact that will have on existing
	 * objects.
	 * 
	 * @param applianceId the appliance identifier
	 * @param domainName the name of the target domain
	 * @param type the class of service to be deployed
	 * @param name the name of the service to be deployed
	 * @param deploymentSource the source of the service and deployment policy (optional)
	 * @return the impacted objects
	 */
	public List<ServiceImpactDescriptor> deployServiceImpact(String applianceId, String domainName, String type, String name, ServiceDeploymentConfiguration deploymentSource);
	
	/**
	 * Unquiesce a service.
	 * 
	 * @param applianceId the appliance identifier
	 * @param domainName the domain name
	 * @param type the type of the service
	 * @param name the name of the service
	 * @return the action identifier
	 */
	public String unquiesceService(String applianceId, String domainName, String type, String name);
	
	/**
	 * Quiesce a service.
	 * 
	 * @param applianceId the appliance identifier
	 * @param domainName the domain name
	 * @param type the type of the service
	 * @param name the name of the service
	 * @return the action identifier
	 */
	public String quiesceService(String applianceId, String domainName, String type, String name);
	
	/**
	 * Delete a service. 
	 * @param applianceId the appliance identifier
	 * @param domainName the domain name
	 * @param type the type of the service
	 * @param name the name of the service
	 * @param orphansToDelete A list of IDs of config objects, which will be
	 * orphaned when the service is deleted but which are nevertheless to be 
	 * kept. 
	 */
	public void deleteService(String applianceId, String domainName, String type, String name, List<String> orphansToDelete);
	
	/**
	 * List the config objects which a specified service (and only that 
	 * service) references, such that those config objects would become orphans
	 * if the service were to be deleted.
	 * @param applianceId The ID of the appliance the service is on
	 * @param domainName The name of the domain the service is part of
	 * @param type The type of service this is
	 * @param name The name of the service
	 * @return A list of potentially-orphaned config objects.
	 */
	public List<ServiceObject> listOrphansIfServiceDeleted(String applianceId, String domainName, String type, String name);

	/**
	 * Retrieve descriptions of services contained in a in a service export zip. Source 
	 * files are processed by each appliance in the list, and the common subset
	 * of services that can be deployed to all appliances is returned.
	 * 
	 * @param applianceIds a list of appliance identifiers to check for configurable services
	 * @param serviceSourceUri the URI for a source file to query
	 * @return a list of service descriptions
	 */
	public List<ServiceDescriptor> getContainedServices(List<String> applianceIds, URI serviceSourceUri);

	/**
	 * Determine the set of files which are "in use". An in-use file is one 
	 * which is currently the "configuration source" or "deployment policy" for
	 * a domain (possibly also for services in future?) and the domain has 
	 * automatic synchronisation turned on. With auto synch, WAMT may need the 
	 * file at any time for a redeployment, so it is in use. This method 
	 * examines the WAMT records for all domains, so should not be called in 
	 * tight loops. 
	 */
	public List<File> getInUseDeploymentFiles();
	
	/**
	 * Upload a file to the appliance of a domain. The file can be a file that 
	 * has been uploaded to WAMC, or a file at a remote (http/https) location.
	 * @param applianceId The identifier of the appliance that the domain is on
	 * @param domainName The name of the domain 
	 * @param source The source of the file that will be copied to the appliance
	 * @param fileName The name of the file in the format used by the appliance, 
	 * 			e.g., store:///myfile. The folder must exist in the appliance
	 * @return the action identifier
	 */
	public String uploadFile(String applianceId, String domainName, URI source, String fileName);
	
	/**
	 * Get all of the groups currently used by the known resources.
	 * 
	 * @return the list of groups in use
	 */
	public List<Group> getGroups();

	/**
	 * Restart a domain.
	 */
	public String restartDomain(String applianceId, String domainName);

	/**
	 * Reboot an appliance.
	 */
	public String rebootAppliance(String applianceId);
}
