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
package com.ibm.amc.resources;

import static com.ibm.amc.security.Permission.APPLIANCE_ADD;
import static com.ibm.amc.security.Permission.APPLIANCE_ASSIGN_GROUPS;
import static com.ibm.amc.security.Permission.APPLIANCE_BACKUP;
import static com.ibm.amc.security.Permission.APPLIANCE_DEPLOY_FIRMWARE;
import static com.ibm.amc.security.Permission.APPLIANCE_EDIT_PROPERTIES;
import static com.ibm.amc.security.Permission.APPLIANCE_QUIESCE;
import static com.ibm.amc.security.Permission.APPLIANCE_REBOOT;
import static com.ibm.amc.security.Permission.APPLIANCE_REMOVE;
import static com.ibm.amc.security.Permission.APPLIANCE_RESTORE;
import static com.ibm.amc.security.Permission.APPLIANCE_UNQUIESCE;
import static com.ibm.amc.security.Permission.APPLIANCE_VIEW;
import static com.ibm.amc.security.Permission.DOMAIN_ASSIGN_GROUPS;
import static com.ibm.amc.security.Permission.DOMAIN_DELETE;
import static com.ibm.amc.security.Permission.DOMAIN_EDIT_PROPERTIES;
import static com.ibm.amc.security.Permission.DOMAIN_QUIESCE;
import static com.ibm.amc.security.Permission.DOMAIN_RESTART;
import static com.ibm.amc.security.Permission.DOMAIN_UNQUIESCE;
import static com.ibm.amc.security.Permission.DOMAIN_UPDATE_CONFIGURATION;
import static com.ibm.amc.security.Permission.DOMAIN_UPLOAD_FILE;
import static com.ibm.amc.security.Permission.DOMAIN_VIEW;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import com.ibm.amc.AmcRuntimeException;
import com.ibm.amc.Constants;
import com.ibm.amc.FileManager;
import com.ibm.amc.data.validation.ValidationEngine;
import com.ibm.amc.ras.Logger47;
import com.ibm.amc.resources.data.Appliance;
import com.ibm.amc.resources.data.BackupRequest;
import com.ibm.amc.resources.data.Domain;
import com.ibm.amc.resources.data.DomainDeploymentConfiguration;
import com.ibm.amc.resources.data.FileUploadDefinition;
import com.ibm.amc.resources.data.Firmware;
import com.ibm.amc.resources.data.FirmwareDeployRequest;
import com.ibm.amc.resources.data.RestoreRequest;
import com.ibm.amc.resources.data.Service;
import com.ibm.amc.resources.data.ServiceDeploymentConfiguration;
import com.ibm.amc.resources.data.ServiceImpactDescriptor;
import com.ibm.amc.resources.data.ServiceObject;
import com.ibm.amc.resources.exceptions.AmcIllegalArgumentException;
import com.ibm.amc.resources.exceptions.ApplianceConnectionException;
import com.ibm.amc.resources.exceptions.NoSuchResourceException;
import com.ibm.amc.security.Permissions;
import com.ibm.amc.server.Controller;
import com.ibm.datapower.amt.DMgrException;

/**
 * ReST resource for appliances. All ReSTfullness should be encapsulated here.
 * 
 * @author mallman
 */
@Path("/appliances")
@Produces(MediaType.APPLICATION_JSON)
public final class ApplianceResource extends AbstractResource
{
	// @CLASS-COPYRIGHT@

	static Logger47 logger = Logger47.get(ApplianceResource.class.getCanonicalName(), Constants.CWZBA_BUNDLE_NAME);

	public ApplianceResource()
	{
	}

	public ApplianceResource(final Controller controller)
	{
		super(controller);
	}

	/**
	 * Add a new appliance to the repository.
	 * 
	 * @param applianceConnection
	 *            properties required to connect to a new appliance
	 * @return
	 * @throws ApplianceConnectionException
	 */
	@POST
	@Permissions(APPLIANCE_ADD)
	public Response addAppliance(Appliance applianceConnection) throws ApplianceConnectionException
	{
		if (logger.isEntryEnabled()) logger.entry("addAppliance", applianceConnection);
		ResponseBuilder response;

		ValidationEngine.getInstance().validate(applianceConnection);

		/* try to add the appliance */
		Appliance appliance = controller.addAppliance(applianceConnection);

		/* populate the response with the new resource location */
		URI location = getResourceUri(appliance.id);
		response = Response.created(location).entity(appliance);
		Response result = response.build();

		if (logger.isEntryEnabled()) logger.exit("addAppliance", result);
		return result;
	}

	/**
	 * Get the list of known appliances. If provided, filter the list based on the query parameters.
	 * 
	 * @return a list of filtered appliances
	 * @throws DMgrException
	 */
	@GET
	@Permissions(APPLIANCE_VIEW)
	public Response getAppliances() throws DMgrException
	{
		if (logger.isEntryEnabled()) logger.entry("getAppliances");

		List<Appliance> appliances = controller.getAppliances(uriInfo.getQueryParameters().entrySet());

		Response result = Response.ok(appliances).header("Content-Range", "items */" + appliances.size()).build();

		if (logger.isEntryEnabled()) logger.exit("getAppliances", result);
		return result;
	}

	/**
	 * Removes a appliance from the repository.
	 * 
	 * @param applianceId
	 *            the identifier for the appliance to remove
	 * @throws DMgrException
	 * @throws NoSuchResourceException
	 */
	@DELETE
	@Path("{applianceId}")
	@Permissions(APPLIANCE_REMOVE)
	public void removeAppliance(@PathParam("applianceId") String applianceId) throws DMgrException, NoSuchResourceException
	{
		if (logger.isEntryEnabled()) logger.entry("removeAppliance", applianceId);

		controller.removeAppliance(applianceId);

		if (logger.isEntryEnabled()) logger.exit("removeAppliance");
	}

	/**
	 * Retrieves an appliance by identifier. Required for both viewing appliances and also
	 * domains/services where the appliance capabilities are required to determine the available
	 * actions.
	 * 
	 * @param applianceId
	 * @return
	 * @throws DMgrException
	 * @throws NoSuchResourceException
	 */
	@GET
	@Path("{applianceId}")
	@Permissions(value = { APPLIANCE_VIEW, DOMAIN_VIEW })
	public Appliance getApplianceById(@PathParam("applianceId") String applianceId) throws DMgrException, NoSuchResourceException
	{
		if (logger.isEntryEnabled()) logger.entry("getApplianceById", applianceId);

		Appliance result = controller.getApplianceById(applianceId);

		if (logger.isEntryEnabled()) logger.exit("getApplianceById", result);
		return result;
	}

	/**
	 * Update the connection properties of an already known appliance
	 * 
	 * @param applianceId
	 * @return
	 * @throws NoSuchResourceException
	 */
	@PUT
	@Path("{applianceId}")
	@Permissions(APPLIANCE_EDIT_PROPERTIES)
	public Appliance updateAppliance(@PathParam("applianceId") String applianceId, Appliance applianceConnection) throws NoSuchResourceException
	{
		if (logger.isEntryEnabled()) logger.entry("updateAppliance", applianceId, applianceConnection);

		ValidationEngine.getInstance().validate(applianceConnection);

		Appliance result = controller.updateAppliance(applianceId, applianceConnection);

		if (logger.isEntryEnabled()) logger.exit("updateAppliance", result);
		return result;
	}

	/**
	 * Update the group assignment for an appliance.
	 * 
	 * @param applianceId
	 *            the appliance to update
	 * @param groups
	 *            the list of groups
	 */
	@PUT
	@Path("{applianceId}/groups")
	@Permissions(APPLIANCE_ASSIGN_GROUPS)
	public void updateApplianceGroups(@PathParam("applianceId") String applianceId, List<String> groups)
	{
		if (logger.isEntryEnabled()) logger.entry("updateApplianceGroups", applianceId, groups);

		controller.updateApplianceGroups(applianceId, groups);

		if (logger.isEntryEnabled()) logger.exit("updateApplianceGroups");
	}

	@GET
	@Path("{applianceId}/domains")
	@Permissions(DOMAIN_VIEW)
	public List<Domain> getDomainsFromAppliance(@PathParam("applianceId") String applianceId) throws DMgrException, NoSuchResourceException
	{
		if (logger.isEntryEnabled()) logger.entry("getDomainFromAppliance", applianceId);

		List<Domain> result = controller.getDomainsFromAppliance(applianceId);

		if (logger.isEntryEnabled()) logger.exit("getDomainFromAppliance", result);
		return result;
	}

	@GET
	@Path("{applianceId}/status")
	@Permissions(APPLIANCE_VIEW)
	public String getApplianceStatus(@PathParam("applianceId") String applianceId) throws DMgrException, NoSuchResourceException
	{
		if (logger.isEntryEnabled()) logger.entry("getDomainFromAppliance", applianceId);

		String result = controller.getApplianceStatus(applianceId);

		if (logger.isEntryEnabled()) logger.exit("getDomainFromAppliance", result);
		return result;
	}

	@POST
	@Path("{applianceId}/actions/quiesce")
	@Permissions(APPLIANCE_QUIESCE)
	public Response quiesce(@PathParam("applianceId") String applianceId)
	{
		if (logger.isEntryEnabled()) logger.entry("quiesce", applianceId);

		final String actionId = controller.quiesceAppliance(applianceId);
		Response result = createAsyncActionResponse(actionId);

		if (logger.isEntryEnabled()) logger.exit("quiesce", result);
		return result;
	}

	@POST
	@Path("{applianceId}/actions/unquiesce")
	@Permissions(APPLIANCE_UNQUIESCE)
	public Response unquiesce(@PathParam("applianceId") String applianceId)
	{
		if (logger.isEntryEnabled()) logger.entry("unquiesce", applianceId);

		final String actionId = controller.unquiesceAppliance(applianceId);
		Response result = createAsyncActionResponse(actionId);

		if (logger.isEntryEnabled()) logger.exit("unquiesce", result);
		return result;
	}

	@POST
	@Path("{applianceId}/actions/backup")
	@Permissions(APPLIANCE_BACKUP)
	public Response backup(@PathParam("applianceId") String applianceId, BackupRequest request)
	{
		if (logger.isEntryEnabled()) logger.entry("backup", applianceId);

		ValidationEngine.getInstance().validate(request);

		final String actionId;
		if (request.certificateName != null)
		{
			actionId = controller.backupAppliance(applianceId, request.certificateName, request.backupDestination, request.includeIscsi, request.includeRaid);
		}
		else
		{
			actionId = controller.backupAppliance(applianceId, request.certificateLocation, request.backupDestination, request.includeIscsi, request.includeRaid);
		}
		Response result = createAsyncActionResponse(actionId);

		if (logger.isEntryEnabled()) logger.exit("backup", result);
		return result;
	}

	@POST
	@Path("{applianceId}/actions/restore")
	@Permissions(APPLIANCE_RESTORE)
	public Response restore(@PathParam("applianceId") String applianceId, RestoreRequest request)
	{
		if (logger.isEntryEnabled()) logger.entry("restore", applianceId);

		ValidationEngine.getInstance().validate(request);

		final String actionId = controller.restoreAppliance(applianceId, request.credentialName, request.backupSource);
		Response result = createAsyncActionResponse(actionId);

		if (logger.isEntryEnabled()) logger.exit("restore", result);
		return result;
	}

	@POST
	@Path("{applianceId}/actions/reboot")
	@Permissions(APPLIANCE_REBOOT)
	public Response reboot(@PathParam("applianceId") String applianceId)
	{
		if (logger.isEntryEnabled()) logger.entry("reboot", applianceId);

		final String actionId = controller.rebootAppliance(applianceId);
		Response result = createAsyncActionResponse(actionId);

		if (logger.isEntryEnabled()) logger.exit("reboot", result);
		return result;
	}

	@GET
	@Path("{applianceId}/firmware/available")
	@Permissions(APPLIANCE_DEPLOY_FIRMWARE)
	public List<Firmware> getAvailableFirmware(@PathParam("applianceId") String applianceId)
	{
		if (logger.isEntryEnabled()) logger.entry("getAvailableFirmware", applianceId);

		List<Firmware> result = controller.getAvailableFirmware(applianceId);
		if (logger.isEntryEnabled()) logger.exit("getAvailableFirmware", result);
		return result;
	}

	@PUT
	@Path("{applianceId}/firmware/current")
	@Permissions(APPLIANCE_DEPLOY_FIRMWARE)
	public Response deployFirmware(@PathParam("applianceId") String applianceId, FirmwareDeployRequest request)
	{
		if (logger.isEntryEnabled()) logger.entry("deployFirmware", applianceId, request);

		/* we only go further if the box is checked */
		if (!request.licenceAccepted) throw new AmcIllegalArgumentException(Status.BAD_REQUEST, "CWZBA1501E_LICENCE_NOT_ACCEPTED");

		String actionId = controller.deployFirmware(applianceId, request.firmwarePrimaryKey, request.licenceAccepted);
		Response result = createAsyncActionResponse(actionId);

		if (logger.isEntryEnabled()) logger.exit("deployFirmware", result);
		return result;
	}

	/*
	 * Domain resources that hang off a particular appliance:
	 */

	@GET
	@Path("{applianceId}/domains/{domainName}")
	@Permissions(DOMAIN_VIEW)
	/**
	 * Get the domain with a particular name that exists on this appliance.
	 */
	public Domain getDomainFromAppliance(@PathParam("applianceId") String applianceId, @PathParam("domainName") String domainName) throws DMgrException, NoSuchResourceException
	{
		if (logger.isEntryEnabled()) logger.entry("getDomainFromAppliance", applianceId, domainName);

		Domain result = controller.getDomainFromAppliance(applianceId, domainName);

		if (logger.isEntryEnabled()) logger.exit("getDomainFromAppliance", result);
		return result;
	}

	@PUT
	@Path("{applianceId}/domains/{domainName}/config")
	@Permissions(DOMAIN_UPDATE_CONFIGURATION)
	/**
	 * Deploy a new configuration to a domain on this appliance. If no domain
	 * with the given name exists, a new one is created.
	 */
	public Response deployDomain(@PathParam("applianceId") String applianceId, @PathParam("domainName") String domainName, DomainDeploymentConfiguration deploymentConfig)
	{
		if (logger.isEntryEnabled()) logger.entry("deployDomain", domainName, deploymentConfig);

		ValidationEngine.getInstance().validate(deploymentConfig);

		String actionId = controller.deployDomain(applianceId, domainName, deploymentConfig);
		Response result = createAsyncActionResponse(actionId);

		if (logger.isEntryEnabled()) logger.exit("deployDomain", result);
		return result;
	}

	@PUT
	@Path("{applianceId}/domains/{domainName}")
	@Permissions(DOMAIN_EDIT_PROPERTIES)
	public Domain updateDomain(@PathParam("applianceId") String applianceId, @PathParam("domainName") String domainName, Domain domain)
	{
		if (logger.isEntryEnabled()) logger.entry("updateDomain", applianceId, domainName, domain);

		ValidationEngine.getInstance().validate(domain);

		Domain result = controller.updateDomain(applianceId, domainName, domain);

		if (logger.isEntryEnabled()) logger.exit("updateDomain", result);
		return result;
	}

	@DELETE
	@Path("{applianceId}/domains/{domainName}")
	@Permissions(DOMAIN_DELETE)
	/**
	 * Delete a particular domain on this appliance.
	 */
	public void deleteDomain(@PathParam("applianceId") String applianceId, @PathParam("domainName") String domainName)
	{
		if (logger.isEntryEnabled()) logger.entry("delete", applianceId, domainName);

		controller.deleteDomain(applianceId, domainName);

		if (logger.isEntryEnabled()) logger.exit("delete");
	}

	/**
	 * Update the group assignment for a domain.
	 * 
	 * @param applianceId
	 *            the appliance identifier
	 * @param domainName
	 *            the name of the domain to update
	 * @param groups
	 *            the list of groups
	 */
	@PUT
	@Path("{applianceId}/domains/{domainName}/groups")
	@Permissions(DOMAIN_ASSIGN_GROUPS)
	public void updateDomainGroups(@PathParam("applianceId") String applianceId, @PathParam("domainName") String domainName, List<String> groups)
	{
		if (logger.isEntryEnabled()) logger.entry("updateDomainGroups", applianceId, domainName, groups);

		controller.updateDomainGroups(applianceId, domainName, groups);

		if (logger.isEntryEnabled()) logger.exit("updateDomainGroups");
	}

	@POST
	@Path("{applianceId}/domains/{domainName}/actions/quiesce")
	@Permissions(DOMAIN_QUIESCE)
	/**
	 * Quiesce a particular domain on this appliance.
	 */
	public Response quiesceDomain(@PathParam("applianceId") String applianceId, @PathParam("domainName") String domainName)
	{
		if (logger.isEntryEnabled()) logger.entry("quiesceDomain", domainName);

		final String actionId = controller.quiesceDomain(applianceId, domainName);
		Response result = createAsyncActionResponse(actionId);

		if (logger.isEntryEnabled()) logger.exit("quiesceDomain", result);
		return result;
	}

	@POST
	@Path("{applianceId}/domains/{domainName}/actions/unquiesce")
	@Permissions(DOMAIN_UNQUIESCE)
	/**
	 * Unquiesce a particular domain on this appliance.
	 */
	public Response unquiesceDomain(@PathParam("applianceId") String applianceId, @PathParam("domainName") String domainName)
	{
		if (logger.isEntryEnabled()) logger.entry("unquiesceDomain", domainName);

		final String actionId = controller.unquiesceDomain(applianceId, domainName);
		Response result = createAsyncActionResponse(actionId);

		if (logger.isEntryEnabled()) logger.exit("unquiesceDomain", result);
		return result;
	}

	@POST
	@Path("{applianceId}/domains/{domainName}/actions/restart")
	@Permissions(DOMAIN_RESTART)
	/**
	 * Restart a particular domain on this appliance.
	 */
	public Response restartDomain(@PathParam("applianceId") String applianceId, @PathParam("domainName") String domainName)
	{
		if (logger.isEntryEnabled()) logger.entry("restartDomain", domainName);

		final String actionId = controller.restartDomain(applianceId, domainName);
		final Response result = createAsyncActionResponse(actionId);

		if (logger.isEntryEnabled()) logger.exit("restartDomain", result);
		return result;
	}

	/*
	 * Service resources that hang off a particular appliance:
	 */

	@GET
	@Path("{applianceId}/domains/{domainName}/services/{type}/{name}")
	@Permissions(DOMAIN_VIEW)
	/**
	 * Get the service with a particular name that exists on this appliance.
	 */
	public Service getServiceFromAppliance(@PathParam("applianceId") String applianceId, @PathParam("domainName") String domainName, @PathParam("type") String type, @PathParam("name") String name)
			throws DMgrException, NoSuchResourceException
	{
		if (logger.isEntryEnabled()) logger.entry("getServiceFromAppliance", applianceId, domainName, type, name);

		Service result = controller.getService(applianceId, domainName, type, name);

		if (logger.isEntryEnabled()) logger.exit("getServiceFromAppliance", result);
		return result;
	}

	@PUT
	@Path("{applianceId}/domains/{domainName}/services/{type}/{name}")
	@Permissions(DOMAIN_UPDATE_CONFIGURATION)
	/**
	 * Deploy a service, either create new or updated existing.
	 * 
	 * @param applianceId the target appliance identifier
	 * @param domainName the domain name
	 * @param deploymentSource a definition of the service to be deployed
	 * @return a response object referencing the action resource
	 * @throws DMgrException
	 */
	public Response deployService(@PathParam("applianceId") String applianceId, @PathParam("domainName") String domainName, @PathParam("type") String type, @PathParam("name") String name,
			ServiceDeploymentConfiguration deploymentSource) throws DMgrException
	{
		if (logger.isEntryEnabled()) logger.entry("deployService", applianceId, domainName, type, name, deploymentSource);

		FileManager.incrementReferenceCount(deploymentSource.configLocation);
		FileManager.incrementReferenceCount(deploymentSource.policyLocation);
		String actionId = controller.deployService(applianceId, domainName, type, name, deploymentSource);
		Response result = createAsyncActionResponse(actionId);

		if (logger.isEntryEnabled()) logger.exit("deployService", result);
		return result;
	}

	@POST
	@Path("{applianceId}/domains/{domainName}/services/{type}/{name}/impact")
	@Permissions(DOMAIN_UPDATE_CONFIGURATION)
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
	 * @throws DMgrException
	 */
	public List<ServiceImpactDescriptor> deployServiceImpact(@PathParam("applianceId") String applianceId, @PathParam("domainName") String domainName, @PathParam("type") String type,
			@PathParam("name") String name, ServiceDeploymentConfiguration deploymentSource) throws DMgrException
	{
		if (logger.isEntryEnabled()) logger.entry("deployServiceImpact", applianceId, domainName, type, name, deploymentSource);

		List<ServiceImpactDescriptor> result = controller.deployServiceImpact(applianceId, domainName, type, name, deploymentSource);

		if (logger.isEntryEnabled()) logger.exit("deployServiceImpact", result);
		return result;
	}

	@POST
	@Path("{applianceId}/domains/{domainName}/services/{type}/{name}/actions/unquiesce")
	@Permissions(DOMAIN_UNQUIESCE)
	/**
	 * Unquiesce a particular service on this appliance.
	 */
	public void unquiesceService(@PathParam("applianceId") String applianceId, @PathParam("domainName") String domainName, @PathParam("type") String type, @PathParam("name") String name)
	{
		if (logger.isEntryEnabled()) logger.entry("unquiesceService", applianceId, domainName, type, name);

		controller.unquiesceService(applianceId, domainName, type, name);

		if (logger.isEntryEnabled()) logger.exit("unquiesceService");
	}

	@POST
	@Path("{applianceId}/domains/{domainName}/services/{type}/{name}/actions/quiesce")
	@Permissions(DOMAIN_QUIESCE)
	/**
	 * Quiesce a particular service on this appliance.
	 */
	public void quiesceService(@PathParam("applianceId") String applianceId, @PathParam("domainName") String domainName, @PathParam("type") String type, @PathParam("name") String name)
	{
		if (logger.isEntryEnabled()) logger.entry("quiesceService", applianceId, domainName, type, name);

		controller.quiesceService(applianceId, domainName, type, name);

		if (logger.isEntryEnabled()) logger.exit("quiesceService");
	}

	@DELETE
	@Path("{applianceId}/domains/{domainName}/services/{type}/{name}")
	@Permissions(DOMAIN_UPDATE_CONFIGURATION)
	/**
	 * Delete the service with a particular name that exists on this appliance.
	 */
	public void deleteServiceFromAppliance(@PathParam("applianceId") String applianceId, @PathParam("domainName") String domainName, @PathParam("type") String type, @PathParam("name") String name,
			List<String> orphansToDelete) throws DMgrException, NoSuchResourceException
	{
		if (logger.isEntryEnabled()) logger.entry("deleteServiceFromAppliance", applianceId, domainName, type, name, orphansToDelete);

		controller.deleteService(applianceId, domainName, type, name, orphansToDelete);

		if (logger.isEntryEnabled()) logger.exit("deleteServiceFromAppliance");
	}

	@GET
	@Path("{applianceId}/domains/{domainName}/services/{type}/{name}/orphans")
	@Permissions(DOMAIN_UPDATE_CONFIGURATION)
	/**
	 * Considering the service with a particular name that exists on this 
	 * appliance, list the config objects which it (and only it) references, 
	 * such that those config objects would become orphans if this service were
	 * to be deleted.
	 */
	public List<ServiceObject> listOrphansIfServiceDeleted(@PathParam("applianceId") String applianceId, @PathParam("domainName") String domainName, @PathParam("type") String type,
			@PathParam("name") String name) throws DMgrException, NoSuchResourceException
	{
		if (logger.isEntryEnabled()) logger.entry("listOrphansIfServiceDeleted", applianceId, domainName, type, name);

		List<ServiceObject> result = controller.listOrphansIfServiceDeleted(applianceId, domainName, type, name);

		if (logger.isEntryEnabled()) logger.exit("listOrphansIfServiceDeleted", result);
		return result;
	}

	@POST
	@Path("{applianceId}/domains/{domainName}/files")
	@Permissions(DOMAIN_UPLOAD_FILE)
	public Response uploadFile(@PathParam("applianceId") String applianceId, @PathParam("domainName") String domainName, FileUploadDefinition fileUploadDefinition)
	{
		if (logger.isEntryEnabled()) logger.entry("uploadFile", applianceId, domainName, fileUploadDefinition);

		ValidationEngine.getInstance().validate(fileUploadDefinition);

		final String actionId = controller.uploadFile(applianceId, domainName, fileUploadDefinition.source, fileUploadDefinition.fileName);
		final Response result = createAsyncActionResponse(actionId);

		if (logger.isEntryEnabled()) logger.exit("uploadFile", result);
		return result;
	}

	/**
	 * Creates a response object that directs the user to the REST resource for an asynchronous
	 * action.
	 * 
	 * @param actionId
	 *            the identifier for the action
	 * @return the response object
	 */
	private Response createAsyncActionResponse(String actionId)
	{
		final URI actionUri;
		try
		{
			actionUri = new URI("/actions/" + actionId);
		}
		catch (final URISyntaxException e)
		{
			throw new AmcRuntimeException(e);
		}
		return Response.status(Response.Status.ACCEPTED).location(actionUri).build();
	}

}
