/**
 * Copyright (C) 2006  Bull S. A. S.
 * Bull, Rue Jean Jaures, B.P.68, 78340, Les Clayes-sous-Bois
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA  02110-1301, USA.
 * 
 * Modified by Matthieu Chaffotte, Elias Ricken de Medeiros - BonitaSoft S.A.
 **/
package org.ow2.bonita.facade.internal;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.ow2.bonita.facade.def.element.AttachmentDefinition;
import org.ow2.bonita.facade.def.element.BusinessArchive;
import org.ow2.bonita.facade.def.majorElement.ActivityDefinition;
import org.ow2.bonita.facade.def.majorElement.DataFieldDefinition;
import org.ow2.bonita.facade.def.majorElement.ParticipantDefinition;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.exception.ActivityDefNotFoundException;
import org.ow2.bonita.facade.exception.ActivityNotFoundException;
import org.ow2.bonita.facade.exception.DataFieldNotFoundException;
import org.ow2.bonita.facade.exception.ParticipantNotFoundException;
import org.ow2.bonita.facade.exception.ProcessNotFoundException;
import org.ow2.bonita.facade.paging.ProcessDefinitionCriterion;
import org.ow2.bonita.facade.runtime.InitialAttachment;
import org.ow2.bonita.facade.uuid.ActivityDefinitionUUID;
import org.ow2.bonita.facade.uuid.ParticipantDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.light.LightProcessDefinition;

/**
 * For internal use only.
 */
@Path("/API/queryDefinitionAPI/")
@Produces({"text/*","application/xml"})
@Consumes({"application/x-www-form-urlencoded","text/*", "application/xml" })
public interface RemoteQueryDefinitionAPI extends Remote {

	/**
	 * Returns the process definition for the specified process name and process version.
	 * @param processId the process name.
   * @param processVersion the process version.
	 * @param options the options map (domain, queryList, user)
	 * @return the process definition for the specified process name and process version.
	 * @throws ProcessNotFoundException
	 * @throws RemoteException
	 */
	@POST @Path("getProcess/{processId}/{processVersion}")
  ProcessDefinition getProcess(
      @PathParam("processId") String processId,
      @PathParam("processVersion") String processVersion, 
  		@FormParam("options") final Map<String, String> options)
	throws ProcessNotFoundException, RemoteException;

	/**
	 * Returns the set of definition information of all deployed processes.
	 * @param options the options map (domain, queryList, user)
	 * @return the set of ProcessDefinition of all deployed processes.
	 * @throws RemoteException
	 */
	@POST @Path("getProcesses")
  Set<ProcessDefinition> getProcesses(
      @FormParam("options") final Map<String, String> options)
  throws RemoteException;

	/**
	 * Returns the set of definition information of all deployed processes. A lightProcessDefinition
	 * is a ProcessDefinition containing only required information.
	 * @param options the options map (domain, queryList, user)
	 * @return the set of LightProcessDefinition of all deployed processes.
	 * @throws RemoteException
	 */
	@POST @Path("getLightProcesses")
  Set<LightProcessDefinition> getLightProcesses(
      @FormParam("options") final Map<String, String> options)
  throws RemoteException;

	/**
	 * Gets the number of deployed processes.
	 * @param options the options map (domain, queryList, user)
	 * @return the number of deployed processes.
	 * @throws RemoteException
	 */
	@POST @Path("getNumberOfProcesses")
  int getNumberOfProcesses(
      @FormParam("options") final Map<String, String> options)
	throws RemoteException;

	/**
	 * Returns the set of definition informations for the process with the specified process name.
   * These process information are searched into the current recorded information and into the archived informations.
   * A process with the given process name could have been deployed, enabled and disabled several times.
   * @param processId the process name
	 * @param options the options map (domain, queryList, user)
	 * @return set of ProcessDefinition of the specified process name.
	 * @throws RemoteException
	 */
	@POST @Path("getProcessesByProcessId/{processId}")
  Set<ProcessDefinition> getProcesses(
      @PathParam("processId") String processId,
      @FormParam("options") final Map<String, String> options)
  throws RemoteException;

	/**
	 * Returns a "page" (i.e. a set) of processes ordered by label.
   * @param fromIndex from which index the page is built
   * @param pageSize the number of page elements 
	 * @param options the options map (domain, queryList, user)
	 * @return an ordered list of LightProcessDefinition
	 * @throws RemoteException
	 */
	@POST @Path("getLightProcessesByIndexAndPageSize")
  List<LightProcessDefinition> getLightProcesses(
      @QueryParam("fromIndex") int fromIndex,
      @QueryParam("pageSize") int pageSize, 
  		@FormParam("options") final Map<String, String> options)
  throws RemoteException;
	
	/**
   * Returns a "page" (i.e. a set) of processes ordered by the given criterion.
   * @param fromIndex from which index the page is built
   * @param pageSize the number of page elements 
   * @param pagingCriterion the criterion used to sort the retrieved process
   * @param options the options map (domain, queryList, user)
   * @return an ordered list of LightProcessDefinition
   */
	@POST @Path("getLightProcessesByIndexAndPageSizeWithPagingCriterion")
  List<LightProcessDefinition> getLightProcesses(
      @QueryParam("fromIndex") int fromIndex, 
      @QueryParam("pageSize") int pageSize, 
      @QueryParam("pagingCriterion") ProcessDefinitionCriterion pagingCriterion, 
      @FormParam("options") final Map<String, String> options)
  throws RemoteException;

	/**
	 * Returns a "page" (i.e. a set) of processes ordered by label.
   * @param fromIndex from which index the page is built
   * @param pageSize the number of page elements 
	 * @param options the options map (domain, queryList, user)
	 * @return an ordered list of ProcessDefinition
	 * @throws RemoteException
	 */
	@POST @Path("getProcessesByIndexAndPageSize")
  List<ProcessDefinition> getProcesses(
      @QueryParam("fromIndex") int fromIndex,
      @QueryParam("pageSize") int pageSize, 
  		@FormParam("options") final Map<String, String> options)
  throws RemoteException;

	/**
	 * Returns the process definition for the specified processDefinition UUID.
   * @param processDefinitionUUID the process UUID.
	 * @param options the options map (domain, queryList, user)
	 * @return the process definition of the specified processDefinition UUID.
	 * @throws RemoteException
	 * @throws ProcessNotFoundException
	 */
	@POST  @Path("getProcess/{processDefinitionUUID}")
  ProcessDefinition getProcess(
      @PathParam("processDefinitionUUID") ProcessDefinitionUUID processDefinitionUUID, 
  		@FormParam("options") final Map<String, String> options)
	throws RemoteException, ProcessNotFoundException;

	/**
	 * Returns the light-weight process definition for the specified processDefinition UUID.
   * @param processDefinitionUUID the process UUID.
	 * @param options the options map (domain, queryList, user)
	 * @return the light-weight process definition of the specified processDefinition UUID.
	 * @throws RemoteException
	 * @throws ProcessNotFoundException
	 */
	@POST @Path("getLightProcess/{processDefinitionUUID}")
  LightProcessDefinition getLightProcess(
      @PathParam("processDefinitionUUID") ProcessDefinitionUUID processDefinitionUUID,
  		@FormParam("options") final Map<String, String> options)
	throws RemoteException, ProcessNotFoundException;

	/**
	 * Returns the set of definition informations for the processes with the specified process state.
   * @param processState the {@link org.ow2.bonita.facade.def.majorElement.ProcessDefinition.ProcessState state} of the process.
	 * @param options the options map (domain, queryList, user)
	 * @return the set of ProcessDefinition for the processes with the specified process state.
	 * @throws RemoteException
	 */
	@POST @Path("getProcessesByState/{processState}")
  Set<ProcessDefinition> getProcesses(
      @PathParam("processState")ProcessDefinition.ProcessState processState, 
  		@FormParam("options") final Map<String, String> options)
  throws RemoteException;

	/**
	 * Returns the set of definition informations for the processes with the specified process state.
   * @param processState the {@link org.ow2.bonita.facade.def.majorElement.ProcessDefinition.ProcessState state} of the process.
	 * @param options the options map (domain, queryList, user)
	 * @return the set of ProcessDefinition for the processes with the specified process state.
	 * @throws RemoteException
	 */
	@POST @Path("getLightProcesses/{processState}")
  Set<LightProcessDefinition> getLightProcesses(
      @PathParam("processState") ProcessDefinition.ProcessState processState, 
  		@FormParam("options") final Map<String, String> options)
  throws RemoteException;

	/**
	 * Returns the set of definition informations for the processes with the specified process name and process state.
   * @param processId the process name.
   * @param processState the {@link org.ow2.bonita.facade.def.majorElement.ProcessDefinition.ProcessState state} of the process.
	 * @param options the options map (domain, queryList, user)
	 * @return the set of ProcessDefinition for the processes with the specified process name and process state.
	 * @throws RemoteException
	 */
	@POST @Path("getProcessesByProcessIdAndState/{processId}/{processState}")
  Set<ProcessDefinition> getProcesses(
      @PathParam("processId") String processId,
      @PathParam("processState")ProcessDefinition.ProcessState processState, 
  		@FormParam("options") final Map<String, String> options) 
  throws RemoteException;

	/**
	 * @param processDefinitionUUID
	 * @param options the options map (domain, queryList, user)
	 * @return
	 * @throws ProcessNotFoundException
	 * @throws RemoteException
	 */
	@Produces("application/octet-stream")
	@POST @Path("getBusinessArchive/{processDefinitionUUID}")
  BusinessArchive getBusinessArchive(
      @PathParam("processDefinitionUUID") ProcessDefinitionUUID processDefinitionUUID, 
  		@FormParam("options") final Map<String, String> options)
	throws ProcessNotFoundException, RemoteException;

	/**
	 * Returns the businessArchive used to deploy the corresponding process.
   * @param processDefinitionUUID the process definition UUID
	 * @param options the options map (domain, queryList, user)
	 * @return the businessArchive used to deploy the corresponding process.
	 * @throws RemoteException
	 * @throws ProcessNotFoundException
	 */
	@POST @Path("getProcessParticipants/{processDefinitionUUID}")
  Set<ParticipantDefinition> getProcessParticipants(
      @PathParam("processDefinitionUUID") ProcessDefinitionUUID processDefinitionUUID, 
  		@FormParam("options") final Map<String, String> options)
  throws RemoteException, ProcessNotFoundException;

	/**
	 * Returns the participant definition for the specified processDefinition UUID and participant Id.
   * @param processDefinitionUUID the processDefinition UUID.
   * @param participantId the participant ID.
	 * @param options the options map (domain, queryList, user)
	 * @return the ParticipantDefinition for the specified processDefinition UUID and participant ID.
	 * @throws RemoteException
	 * @throws ProcessNotFoundException
	 * @throws ParticipantNotFoundException
	 */
	@POST @Path("getProcessParticipant/{processDefinitionUUID}/{participantId}")
  ParticipantDefinition getProcessParticipant(
      @PathParam("processDefinitionUUID") ProcessDefinitionUUID processDefinitionUUID,
      @PathParam("participantId") String participantId,
      @FormParam("options") final Map<String, String> options)
	throws RemoteException, ProcessNotFoundException, ParticipantNotFoundException;

	/**
	 * Returns the set of definitions for process activities of the specified processDefinition UUID.
   * @param processDefinitionUUID the process UUID.
	 * @param options the options map (domain, queryList, user)
	 * @return the set of ActivityDefinition for the specified processDefinition UUID.
	 * @throws RemoteException
	 * @throws ProcessNotFoundException
	 */
	@POST @Path("getProcessActivities/{processDefinitionUUID}")
  Set<ActivityDefinition> getProcessActivities(
      @PathParam("processDefinitionUUID") ProcessDefinitionUUID processDefinitionUUID, 
  		@FormParam("options") final Map<String, String> options)
  throws RemoteException, ProcessNotFoundException;
  
	/**
	 * Returns the activity definition of the specified process and activity name.
   * @param processDefinitionUUID the process UUID.
   * @param activityId the activity name.
	 * @param options the options map (domain, queryList, user)
	 * @return the ActivityDefinition for the specified processDefinition UUID and activity name.
	 * @throws RemoteException
	 * @throws ProcessNotFoundException
	 * @throws ActivityNotFoundException
	 */
	@POST @Path("getProcessActivity/{processDefinitionUUID}/{activityId}")
  ActivityDefinition getProcessActivity(
      @PathParam("processDefinitionUUID") ProcessDefinitionUUID processDefinitionUUID, 
  		@PathParam("activityId") String activityId,
  		@FormParam("options") final Map<String, String> options)
	throws RemoteException, ProcessNotFoundException, ActivityNotFoundException;

	/**
	 * Returns the activity definition UUID of the given process definition UUID and activity name.
   * @param processDefinitionUUID the process UUID.
   * @param activityName the activity name
	 * @param options the options map (domain, queryList, user)
	 * @return the activity UUID of the given process UUID and activity name.
	 * @throws RemoteException
	 * @throws ProcessNotFoundException
	 */
	@POST @Path("getProcessActivityId/{processDefinitionUUID}/{activityName}")
  ActivityDefinitionUUID getProcessActivityId(
      @PathParam("processDefinitionUUID") ProcessDefinitionUUID processDefinitionUUID,
      @PathParam("activityName") String activityName,
      @FormParam("options") final Map<String, String> options)
	throws RemoteException, ProcessNotFoundException;

	/**
	 * Returns the participant definition UUID of the given process definition UUID and participant name.
   * @param processDefinitionUUID the process definition UUID.
   * @param participantName the participant name.
	 * @param options the options map (domain, queryList, user)
	 * @return the participant definition UUID of the given process definition UUID and participant name.
	 * @throws RemoteException
	 * @throws ProcessNotFoundException
	 */
	@POST @Path("getProcessParticipantId/{processDefinitionUUID}/{participantName}")
  ParticipantDefinitionUUID getProcessParticipantId(
      @PathParam("processDefinitionUUID") ProcessDefinitionUUID processDefinitionUUID,
  		@PathParam("participantName") String participantName,
  		@FormParam("options") final Map<String, String> options) 
	throws RemoteException, ProcessNotFoundException;
    
	/**
	 * Returns the process definition of the most recently deployed process of the given process name.
   * @param processName the process name.
	 * @param options the options map (domain, queryList, user)
	 * @return the process definition of the last deployed process of the given process name.
	 * @throws RemoteException
	 * @throws ProcessNotFoundException
	 */
	@POST @Path("getLastProcess/{processId}")
  ProcessDefinition getLastProcess(
      @PathParam("processId") String processId, 
      @FormParam("options") final Map<String, String> options)
	throws RemoteException, ProcessNotFoundException;
  
	/**
	 * Returns the light process definition of the most recently deployed process of the given process name.
   * @param processName the process name.
	 * @param options the options map (domain, queryList, user)
	 * @return the light process definition of the last deployed process of the given process name.
	 * @throws RemoteException
	 * @throws ProcessNotFoundException
	 */
	@POST @Path("getLastLightProcess/{processName}")
  LightProcessDefinition getLastLightProcess(
      @PathParam("processName") String processName, 
      @FormParam("options") final Map<String, String> options)
	throws RemoteException, ProcessNotFoundException;

	/**
	 * Returns the set of dataField definitions defined within the given processDefinition UUID.
   * @param processDefinitionUUID the processDefinition UUID.
	 * @param options the options map (domain, queryList, user)
	 * @return the set of DataFieldDefinition of the given processDefinition UUID.
	 * @throws RemoteException
	 * @throws ProcessNotFoundException
	 */
	@POST @Path("getProcessDataFields/{processDefinitionUUID}")
  Set<DataFieldDefinition> getProcessDataFields(
      @PathParam("processDefinitionUUID") ProcessDefinitionUUID processDefinitionUUID, 
  		@FormParam("options") final Map<String, String> options)
  throws RemoteException, ProcessNotFoundException;

	/**
	 Returns the DataField definition defined within the specified processDefinition UUID
   * for the given dataField name
   * @param processDefinitionUUID the processDefinition UUID.
   * @param dataFieldId the dataField name.
	 * @param options the options map (domain, queryList, user)
	 * @return the DataFieldDefinition for the specified processDefinition UUID and dataField Id.
	 * @throws RemoteException
	 * @throws ProcessNotFoundException
	 * @throws DataFieldNotFoundException
	 */
	@POST @Path("getProcessDataField/{processDefinitionUUID}/{dataFieldId}")
  DataFieldDefinition getProcessDataField(
      @PathParam("processDefinitionUUID") ProcessDefinitionUUID processDefinitionUUID, 
  		@PathParam("dataFieldId") String dataFieldId,
  		@FormParam("options") final Map<String, String> options)
	throws RemoteException, ProcessNotFoundException, DataFieldNotFoundException;

	/**
	 * Returns the set of dataField definitions that have been defined as local to the given activity
   * for the given activityDefinition UUID.
   * @param uuid the activityDefinition UUID.
	 * @param options the options map (domain, queryList, user)
	 * @return the set of DataFieldDefinition for the specified activityDefinition UUID.
	 * @throws RemoteException
	 * @throws ActivityDefNotFoundException
	 */
	@POST @Path("getActivityDataFields/{uuid}")
  Set<DataFieldDefinition> getActivityDataFields(
      @PathParam("uuid") ActivityDefinitionUUID uuid,
      @FormParam("options") final Map<String, String> options)
  throws RemoteException, ActivityDefNotFoundException;

	/**
	 * Returns the DataField definition that has been defined as local to the given activity
   * for the given activityDefinition UUID and dataField name.
   * @param activityDefinitionUUID the ActivityDefinition UUID.
   * @param dataFieldId the dataField name.
	 * @param options the options map (domain, queryList, user)
	 * @return the DataFieldDefinition for the specified activityDefinition UUID and dataField name.
	 * @throws RemoteException
	 * @throws ActivityDefNotFoundException
	 * @throws DataFieldNotFoundException
	 */
	@POST @Path("getActivityDataField/{activityDefinitionUUID}/{dataFieldId}")
  DataFieldDefinition getActivityDataField(
      @PathParam("activityDefinitionUUID") ActivityDefinitionUUID activityDefinitionUUID, 
  		@PathParam("dataFieldId") String dataFieldId,
  		@FormParam("options") final Map<String, String> options)
	throws RemoteException, ActivityDefNotFoundException, DataFieldNotFoundException;

	/**
	 * Obtains the value of a process meta data. If no meta data matches with the key, null is returned.
   * @param uuid the process definition UUID
   * @param key the key of the meta data
	 * @param options the options map (domain, queryList, user)
	 * @return the value of the meta data
	 * @throws RemoteException
	 * @throws ProcessNotFoundException
	 */
	@POST @Path("getProcessMetaData/{uuid}/{key}")
  String getProcessMetaData(
      @PathParam("uuid") ProcessDefinitionUUID uuid,
      @PathParam("key") String key, 
  		@FormParam("options") final Map<String, String> options)
	throws RemoteException, ProcessNotFoundException;

	/**
	 * Gets all the initial attachments of a process given by its definition UUID.
   * @param processUUID the process definition UUID
	 * @param options the options map (domain, queryList, user)
	 * @return a set of all the attachments of a process
	 * @throws ProcessNotFoundException
	 * @throws RemoteException
	 */
	@POST @Path("getProcessAttachments/{processUUID}")
  Set<InitialAttachment> getProcessAttachments(
      @PathParam("processUUID") ProcessDefinitionUUID processUUID, 
  		@FormParam("options") final Map<String, String> options)
  throws ProcessNotFoundException, RemoteException;

	/**
	 * Gets the initial attachment of a process given by its definition UUID and the attachment name.
   * @param processUUID the process definition UUID
   * @param attachmentName the attachment name
	 * @param options the options map (domain, queryList, user)
	 * @return the attachment relative to the given process UUID and attachment name
   * or null if the attachment name does not exist.
	 * @throws ProcessNotFoundException
	 * @throws RemoteException
	 */
	@POST @Path("getProcessAttachment/{processUUID}/{attachmentName}")
  InitialAttachment getProcessAttachment(
      @PathParam("processUUID") ProcessDefinitionUUID processUUID,
      @PathParam("attachmentName") String attachmentName, 
  		@FormParam("options") final Map<String, String> options)
	throws ProcessNotFoundException, RemoteException;

	/**
	 * Gets the attachment definitions of a process given by its definition UUID.
   * @param processUUID the process definition UUID
	 * @param options the options map (domain, queryList, user)
	 * @return a set of all the attachment relative to the given process UUID
	 * @throws ProcessNotFoundException
	 * @throws RemoteException
	 */
	@POST @Path("getAttachmentDefinitions/{processUUID}")
  Set<AttachmentDefinition> getAttachmentDefinitions(
      @PathParam("processUUID") ProcessDefinitionUUID processUUID, 
  		@FormParam("options") final Map<String, String> options)
  throws ProcessNotFoundException, RemoteException;
  
	/**
	  * Gets the attachment definition of a process given by its definition UUID and the attachment name.
   * @param processUUID the process definition UUID
   * @param attachmentName the attachment name
	 * @param options the options map (domain, queryList, user)
	 * @return the attachment definition or null if the attachment name does not exist
	 * @throws ProcessNotFoundException
	 * @throws RemoteException
	 */
	@POST @Path("getAttachmentDefinition/{processUUID}/{attachmentName}")
  AttachmentDefinition getAttachmentDefinition(
      @PathParam("processUUID") ProcessDefinitionUUID processUUID,
      @PathParam("attachmentName") String attachmentName, 
  		@FormParam("options") final Map<String, String> options)
	throws ProcessNotFoundException, RemoteException;

	/**
	 * Gets a resource of a process given by its definition UUID and the resource path
   * @param definitionUUID the process definition UUID
   * @param resourcePath the path to the resource
	 * @param options the options map (domain, queryList, user)
	 * @return a byte array representing the resource or null if the resource does not exist.
	 * @throws ProcessNotFoundException
	 * @throws RemoteException
	 */
	@POST @Path("getResource/{definitionUUID}")
  byte[] getResource(
      @PathParam("definitionUUID") ProcessDefinitionUUID definitionUUID,
      @QueryParam("resourcePath") String resourcePath, 
  		@FormParam("options") final Map<String, String> options)
	throws ProcessNotFoundException, RemoteException;

	/**
	 * Returns the light-weight process definitions for the specified processDefinition UUIDs.
	 * @param processUUIDs
	 * @param options the options map (domain, queryList, user)
	 * @return
	 * @throws ProcessNotFoundException
	 * @throws RemoteException
	 */
	@POST @Path("getLightProcessesByProcessDefinitionUUIDs")
  Set<LightProcessDefinition> getLightProcesses(
      @FormParam("processUUIDs") Set<ProcessDefinitionUUID> processUUIDs, 
  		@FormParam("options") final Map<String, String> options)
  throws ProcessNotFoundException, RemoteException;
	  
  /**
   * Returns a "page" (i.e. a set) of processes ordered by the given criterion.<br>
   * Only the process from the given ProcessDefinitionUUIDs will be considered.
   * @param fromIndex from which index the page is built
   * @param pageSize the number of page elements 
   * @param pagingCriterion the criterion used to sort the retrieved process
   * @return an ordered list of LightProcessDefinition
   */
	@POST @Path("getLightProcessesByProcessDefinitionUUIDsWithPagingCriterion")
  List<LightProcessDefinition> getLightProcesses(
      @FormParam("processUUIDs") Set<ProcessDefinitionUUID> processUUIDs, 
      @QueryParam("fromIndex") int fromIndex, 
      @QueryParam("pageSize") int pageSize, 
      @QueryParam("pagingCriterion") ProcessDefinitionCriterion pagingCriterion, 
      @FormParam("options") final Map<String, String> options)
  throws ProcessNotFoundException, RemoteException;

	/**
	 * Returns all the light-weight process definitions except those for which processDefinition UUID is specified.
	 * @param processUUIDs
	 * @param fromIndex
	 * @param pageSize
	 * @param options the options map (domain, queryList, user)
	 * @return
	 * @throws RemoteException
	 */
	@POST @Path("getAllLightProcessesExcept")
  List<LightProcessDefinition> getAllLightProcessesExcept(
      @FormParam("processUUIDs") Set<ProcessDefinitionUUID> processUUIDs, 
  		@QueryParam("fromIndex") int fromIndex,
  		@QueryParam("pageSize") int pageSize,
  		@FormParam("options") final Map<String, String> options)
  throws RemoteException;
	
	/**
   * Returns all the light-weight process definitions except those for which processDefinition UUID is specified. The retrieved process are ordered by the given criterion.
   * @param processesUUID
   * @param fromIndex
   * @param pageSize
   * @return
   */
	@POST @Path("getAllLightProcessesExceptWithPagingCriterion")
  List<LightProcessDefinition> getAllLightProcessesExcept(
      @FormParam("processUUIDs") Set<ProcessDefinitionUUID> processUUIDs, 
      @QueryParam("fromIndex") int fromIndex, 
      @QueryParam("pageSize") int pageSize, 
      @QueryParam("pagingCriterion") ProcessDefinitionCriterion pagingCriterion,
      @FormParam("options") final Map<String, String> options)
  throws RemoteException;

	/**
	 * Gets the processes having the given category.
	 * @param category
	 * @param options the options map (domain, queryList, user)
	 * @return
	 * @throws RemoteException
	 */
	@POST @Path("getProcessUUIDs/{category}")
  Set<ProcessDefinitionUUID> getProcessUUIDs(
      @PathParam("category")String category,
      @FormParam("options") final Map<String, String> options) 
  throws RemoteException;

	/**
	 * @param processsUUID
	 * @param options the options map (domain, queryList, user)
	 * @return
	 * @throws ProcessNotFoundException
	 * @throws RemoteException
	 */
	@POST @Path("getProcessTaskUUIDs/{processsUUID}")
  Set<ActivityDefinitionUUID> getProcessTaskUUIDs(
      @PathParam("processsUUID") ProcessDefinitionUUID processsUUID, 
  		@FormParam("options") final Map<String, String> options)
  throws ProcessNotFoundException, RemoteException;
	
	
	/**
  * Gets the migration date for the given processUUID
  * @param processUUID the ProcessDefinitionUUID
  * @return the migration date for the given processUUID
  * @throws ProcessNotFoundException
  * @throws RemoteException
  */
	@POST @Path("getMigrationDate/{processUUID}")
	Date getMigrationDate(
	    @PathParam("processUUID") final ProcessDefinitionUUID processUUID,
	    @FormParam("options") final Map<String, String> options) 
	throws ProcessNotFoundException, RemoteException;

}
