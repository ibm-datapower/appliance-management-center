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

import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Set;

import com.ibm.amc.data.SvrAppliance;
import com.ibm.amc.data.SvrDomain;
import com.ibm.amc.data.SvrFirmware;
import com.ibm.amc.data.SvrService;
import com.ibm.amc.data.SvrServiceDescriptor;
import com.ibm.amc.resources.data.Appliance;
import com.ibm.amc.resources.data.Domain;
import com.ibm.amc.resources.data.Firmware;
import com.ibm.amc.resources.data.GroupMember;
import com.ibm.amc.resources.exceptions.ApplianceConnectionException;
import com.ibm.amc.resources.exceptions.NoSuchResourceException;

/**
 * Interface with a ApplianceManager. Initially, the only appliance manager will be WAMT.
 * 
 * @author mallman
 */
public interface ApplianceManager
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
	public SvrAppliance addAppliance(Appliance applianceConnection) throws ApplianceConnectionException;

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
	 * Get the list of appliances that have been added to the appliance manager
	 * 
	 * @return the list of appliances
	 */
	public List<SvrAppliance> getAppliances();

	/**
	 * Obtain a particular appliance based on its ID ("serial number" in the case of WAMT)
	 * 
	 * @return the requested appliance
	 * @throws NoSuchResourceException
	 *             if the specified applianceId does not refer to a known appliance.
	 */
	public SvrAppliance getApplianceById(String applianceId) throws NoSuchResourceException;

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
	public SvrAppliance updateAppliance(String applianceId, Appliance applianceConnection) throws NoSuchResourceException;

	/**
	 * @return a list of all domains on all known appliances.
	 */
	public List<SvrDomain> getDomains();
	
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
	public SvrDomain updateDomain(String applianceId, String domainName, Domain domain) throws NoSuchResourceException;
	
	/**
	 * Retreive all of the services for all known devices.
	 * 
	 * @return the list of services
	 */
	public List<SvrService> getServices();
	
	/**
	 * Retrieve descriptions of services contained in a in a service export zip. Source 
	 * files are processed by each appliance in the list, and the common subset
	 * of services that can be deployed to all appliances is returned.
	 * 
	 * @param applianceIds a list of appliance identifiers to check for configurable services
	 * @param serviceSourceUri the URI of a source file to query
	 * @return a list of service descriptions
	 */
	public List<SvrServiceDescriptor> getContainedServices(List<String> applianceIds, URI serviceSourceUri);

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
	 * Get a list of all the firmware versions that have been defined
	 * 
	 * @return The list of all the firmware versions
	 */
	public List<SvrFirmware> getFirmware();

	/**
	 * Get a specific firmware version by ID
	 * 
	 * @param firmwareId
	 *            The ID of the firmware version
	 * @return The specified firmware version
	 */
	public SvrFirmware getFirmwareById(String firmwareId);

	/**
	 * Update editable fields of a firmware object.
	 * 
	 * @param firmwareId
	 *            the identifier of the firmware object to update
	 * @param firmware
	 *            the new firmware details
	 * @return the updated firmware details
	 */
	public SvrFirmware updateFirmware(String firmwareId, Firmware firmware);
	
	/**
	 * Get the names all of the groups currently used by the known resources.
	 * 
	 * @return the list of names of groups in use
	 */
	public List<String> getGroupNames();
	
	/**
	 * Get the members of a named group.
	 * 
	 * @param name the name of the group
	 * @return a set containing the members of the group
	 */
	public Set<GroupMember> getGroupMembers(String name);
}
