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
 **/
package org.ow2.bonita.facade;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.ow2.bonita.facade.def.element.AttachmentDefinition;
import org.ow2.bonita.facade.def.element.BusinessArchive;
import org.ow2.bonita.facade.def.majorElement.ActivityDefinition;
import org.ow2.bonita.facade.def.majorElement.DataFieldDefinition;
import org.ow2.bonita.facade.def.majorElement.ParticipantDefinition;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.exception.ActivityDefNotFoundException;
import org.ow2.bonita.facade.exception.ActivityNotFoundException;
import org.ow2.bonita.facade.exception.BonitaInternalException;
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
 * Getters on the workflow definition data for:<br>
 * <ul>
 * <li>processes (full and light-weight)</li>
 * <li>activities</li>
 * <li>participants</li>
 * <li>attachments</li>
 * </ul>
 * 
 * As indicated by its prefix: Query, this interface could be seen as complementary to the
 * {@link org.ow2.bonita.facade.QueryRuntimeAPI} interface.<br>
 * This interface deals with the static part of the data managed by the wokflow.<br>
 * <br>
 * 
 * Workflow data can be retrieved with both entities IDs or names.
 */
public interface QueryDefinitionAPI {

  // process
  /**
   * Returns the set of definition information of all deployed processes.
   * 
   * @return the set of ProcessDefinition of all deployed processes.
   * @throws BonitaInternalException
   *           if an exception occurs.
   */
  Set<ProcessDefinition> getProcesses();

  /**
   * Returns the set of definition information of all deployed processes. A lightProcessDefinition is a
   * ProcessDefinition containing only required information.
   * 
   * @return the set of LightProcessDefinition of all deployed processes.
   */
  Set<LightProcessDefinition> getLightProcesses();

  /**
   * Gets the number of deployed processes.
   * 
   * @return the number of deployed processes.
   */
  int getNumberOfProcesses();

  /**
   * Returns the set of definition informations for the process with the specified process name. These process
   * information are searched into the current recorded information and into the archived informations. A process with
   * the given process name could have been deployed, enabled and disabled several times.
   * 
   * @param processName
   *          the process name
   * @return set of ProcessDefinition of the specified process name.
   * @throws BonitaInternalException
   *           if an exception occurs.
   */
  Set<ProcessDefinition> getProcesses(String processName);

  /**
   * Returns the process definition for the specified processDefinition UUID.
   * 
   * @param processDefinitionUUID
   *          the process UUID.
   * @return the process definition of the specified processDefinition UUID.
   * @throws ProcessNotFoundException
   *           if the process with the given UUID does not exist.
   * @throws BonitaInternalException
   *           if an exception occurs.
   */
  ProcessDefinition getProcess(ProcessDefinitionUUID processDefinitionUUID) throws ProcessNotFoundException;

  /**
   * Returns the light-weight process definition for the specified processDefinition UUID.
   * 
   * @param processDefinitionUUID
   *          the process UUID.
   * @return the light-weight process definition of the specified processDefinition UUID.
   * @throws ProcessNotFoundException
   *           if the process with the given UUID does not exist.
   * @throws BonitaInternalException
   *           if an exception occurs.
   */
  LightProcessDefinition getLightProcess(ProcessDefinitionUUID processDefinitionUUID) throws ProcessNotFoundException;

  /**
   * Returns a "page" (i.e. a set) of processes ordered by label.
   * 
   * @param fromIndex
   *          from which index the page is built
   * @param pageSize
   *          the number of page elements
   * @return an ordered list of LightProcessDefinition
   */
  List<LightProcessDefinition> getLightProcesses(int fromIndex, int pageSize);

  /**
   * Returns a "page" (i.e. a set) of processes ordered by the given criterion.
   * 
   * @param fromIndex
   *          from which index the page is built
   * @param pageSize
   *          the number of page elements
   * @param pagingCriterion
   *          the criterion used to sort the retrieved process
   * @return an ordered list of LightProcessDefinition
   */
  List<LightProcessDefinition> getLightProcesses(int fromIndex, int pageSize, ProcessDefinitionCriterion pagingCriterion);

  /**
   * Returns a "page" (i.e. a set) of processes ordered by label.
   * 
   * @param fromIndex
   *          from which index the page is built
   * @param pageSize
   *          the number of page elements
   * @return an ordered list of ProcessDefinition
   */
  List<ProcessDefinition> getProcesses(int fromIndex, int pageSize);

  /**
   * Returns the process definition for the specified process name and process version.
   * 
   * @param processName
   *          the process name.
   * @param processVersion
   *          the process version.
   * @return the process definition for the specified process name and process version.
   * @throws ProcessNotFoundException
   *           if the process with the given parameters does not exist.
   * @throws BonitaInternalException
   *           if an exception occurs.
   */
  ProcessDefinition getProcess(String processName, String processVersion) throws ProcessNotFoundException;

  /**
   * Returns the set of definition informations for the processes with the specified process state.
   * 
   * @param processState
   *          the {@link org.ow2.bonita.facade.def.majorElement.ProcessDefinition.ProcessState state} of the process.
   * @return the set of ProcessDefinition for the processes with the specified process state.
   * @throws BonitaInternalException
   *           if an exception occurs.
   */
  Set<ProcessDefinition> getProcesses(ProcessDefinition.ProcessState processState);

  /**
   * Returns the set of definition informations for the processes with the specified process state.
   * 
   * @param processState
   *          the {@link org.ow2.bonita.facade.def.majorElement.ProcessDefinition.ProcessState state} of the process.
   * @return the set of ProcessDefinition for the processes with the specified process state.
   * @throws BonitaInternalException
   *           if an exception occurs.
   */
  Set<LightProcessDefinition> getLightProcesses(ProcessDefinition.ProcessState processState);

  /**
   * Returns the set of definition informations for the processes with the specified process name and process state.
   * 
   * @param processName
   *          the process name.
   * @param processState
   *          the {@link org.ow2.bonita.facade.def.majorElement.ProcessDefinition.ProcessState state} of the process.
   * @return the set of ProcessDefinition for the processes with the specified process name and process state.
   * @throws BonitaInternalException
   *           if an exception occurs.
   */
  Set<ProcessDefinition> getProcesses(String processName, ProcessDefinition.ProcessState processState);

  /**
   * Returns the businessArchive used to deploy the corresponding process.
   * 
   * @param processDefinitionUUID
   *          the process definition UUID
   * @return the businessArchive used to deploy the corresponding process.
   * @throws ProcessNotFoundException
   *           if no process exists with the given processDefinition UUID.
   */
  BusinessArchive getBusinessArchive(ProcessDefinitionUUID processDefinitionUUID) throws ProcessNotFoundException;

  // process dataFields
  /**
   * Returns the set of dataField definitions defined within the given processDefinition UUID.
   * 
   * @param processDefinitionUUID
   *          the processDefinition UUID.
   * @return the set of DataFieldDefinition of the given processDefinition UUID.
   * @throws ProcessNotFoundException
   *           if no process exists with the given processDefinition UUID.
   * @throws BonitaInternalException
   *           if another exception occurs.
   */
  Set<DataFieldDefinition> getProcessDataFields(ProcessDefinitionUUID processDefinitionUUID)
      throws ProcessNotFoundException;

  /**
   * Returns the DataField definition defined within the specified processDefinition UUID for the given dataField name
   * 
   * @param processDefinitionUUID
   *          the processDefinition UUID.
   * @param dataFieldName
   *          the dataField name.
   * @return the DataFieldDefinition for the specified processDefinition UUID and dataField Id.
   * @throws ProcessNotFoundException
   *           if the process with the given processDefinition UUID does not exist.
   * @throws DataFieldNotFoundException
   *           if the dataField does not exist in process.
   * @throws BonitaInternalException
   *           if another exception occurs.
   */
  DataFieldDefinition getProcessDataField(ProcessDefinitionUUID processDefinitionUUID, String dataFieldName)
      throws ProcessNotFoundException, DataFieldNotFoundException;

  // process participants
  /**
   * Returns the set of participant definitions for the specified processDefinition UUID.
   * 
   * @param processDefinitionUUID
   *          the processDefinition UUID.
   * @return the set of ParticipantDefinition for the specified processDefinition UUID.
   * @throws ProcessNotFoundException
   *           if no process exists with the given processDefinition UUID.
   * @throws BonitaInternalException
   *           if another exception occurs.
   */
  Set<ParticipantDefinition> getProcessParticipants(ProcessDefinitionUUID processDefinitionUUID)
      throws ProcessNotFoundException;

  /**
   * Returns the participant definition for the specified processDefinition UUID and participant Id.
   * 
   * @param processDefinitionUUID
   *          the processDefinition UUID.
   * @param participantId
   *          the participant ID.
   * @return the ParticipantDefinition for the specified processDefinition UUID and participant ID.
   * @throws ProcessNotFoundException
   *           if the process with the given processDefinition UUID does not exist.
   * @throws ParticipantNotFoundException
   *           if the participant does not exist in process.
   * @throws BonitaInternalException
   *           if another exception occurs.
   */
  ParticipantDefinition getProcessParticipant(ProcessDefinitionUUID processDefinitionUUID, String participantId)
      throws ProcessNotFoundException, ParticipantNotFoundException;

  // process activities
  /**
   * Returns the set of definitions for process activities of the specified processDefinition UUID.
   * 
   * @param processDefinitionUUID
   *          the process UUID.
   * @return the set of ActivityDefinition for the specified processDefinition UUID.
   * @throws ProcessNotFoundException
   *           if the process with the given processDefinition UUID does not exist.
   * @throws BonitaInternalException
   *           if another exception occurs.
   */
  Set<ActivityDefinition> getProcessActivities(ProcessDefinitionUUID processDefinitionUUID)
      throws ProcessNotFoundException;

  /**
   * Returns the activity definition of the specified process and activity name.
   * 
   * @param processDefinitionUUID
   *          the process UUID.
   * @param activityName
   *          the activity name.
   * @return the ActivityDefinition for the specified processDefinition UUID and activity name.
   * @throws ProcessNotFoundException
   *           if the process with the given UUID does not exist.
   * @throws ActivityNotFoundException
   *           if the activity with the given id does not exist in the process.
   * @throws BonitaInternalException
   *           if an exception occurs.
   */
  ActivityDefinition getProcessActivity(ProcessDefinitionUUID processDefinitionUUID, String activityName)
      throws ProcessNotFoundException, ActivityNotFoundException;

  /**
   * Returns the DataField definition that has been defined as local to the given activity for the given
   * activityDefinition UUID and dataField name.
   * 
   * @param activityDefinitionUUID
   *          the ActivityDefinition UUID.
   * @param dataFieldName
   *          the dataField name.
   * @return the DataFieldDefinition for the specified activityDefinition UUID and dataField name.
   * @throws ActivityNotFoundException
   *           if the activity with the given activityDefinition UUID does not exist.
   * @throws DataFieldNotFoundException
   *           if the dataField does not exist within the activity.
   * @throws BonitaInternalException
   *           if an exception occurs.
   */
  DataFieldDefinition getActivityDataField(ActivityDefinitionUUID activityDefinitionUUID, String dataFieldName)
      throws ActivityDefNotFoundException, DataFieldNotFoundException;

  // activity dataFields
  /**
   * Returns the set of dataField definitions that have been defined as local to the given activity for the given
   * activityDefinition UUID.
   * 
   * @param activityDefinitionUUID
   *          the activityDefinition UUID.
   * @return the set of DataFieldDefinition for the specified activityDefinition UUID.
   * @throws ActivityDefNotFoundException
   *           if no activity exists with the given activityDefinition UUID.
   * @throws BonitaInternalException
   *           if an exception occurs.
   */
  Set<DataFieldDefinition> getActivityDataFields(ActivityDefinitionUUID activityDefinitionUUID)
      throws ActivityDefNotFoundException;

  /**
   * Returns the activity definition UUID of the given process definition UUID and activity name.
   * 
   * @param processDefinitionUUID
   *          the process UUID.
   * @param activityName
   *          the activity name
   * @return the activity UUID of the given process UUID and activity name.
   * @throws ProcessNotFoundException
   *           if the process with the given UUID does not exist.
   * @throws BonitaInternalException
   *           if an exception occurs.
   */
  ActivityDefinitionUUID getProcessActivityId(ProcessDefinitionUUID processDefinitionUUID, String activityName)
      throws ProcessNotFoundException;

  /**
   * Returns the participant definition UUID of the given process definition UUID and participant name.
   * 
   * @param processDefinitionUUID
   *          the process definition UUID.
   * @param participantName
   *          the participant name.
   * @return the participant definition UUID of the given process definition UUID and participant name.
   * @throws ProcessNotFoundException
   *           if the process with the given UUID does not exist
   * @throws BonitaInternalException
   *           if an exception occurs.
   */
  ParticipantDefinitionUUID getProcessParticipantId(ProcessDefinitionUUID processDefinitionUUID, String participantName)
      throws ProcessNotFoundException;

  /**
   * Returns the process definition of the most recently deployed process of the given process name.
   * 
   * @param processName
   *          the process name.
   * @return the process definition of the last deployed process of the given process name.
   * @throws ProcessNotFoundException
   *           if the process with the given process name does not exist.
   * @throws BonitaInternalException
   *           if an exception occurs.
   */
  ProcessDefinition getLastProcess(String processName) throws ProcessNotFoundException;

  /**
   * Returns the light process definition of the most recently deployed process of the given process name.
   * 
   * @param processName
   *          the process name.
   * @return the light process definition of the last deployed process of the given process name.
   * @throws ProcessNotFoundException
   *           if the process with the given process name does not exist.
   * @throws BonitaInternalException
   *           if an exception occurs.
   */
  LightProcessDefinition getLastLightProcess(String processName) throws ProcessNotFoundException;

  /**
   * Obtains the value of a process meta data. If no meta data matches with the key, null is returned.
   * 
   * @param uuid
   *          the process definition UUID
   * @param key
   *          the key of the meta data
   * @return the value of the meta data
   */
  String getProcessMetaData(ProcessDefinitionUUID uuid, String key) throws ProcessNotFoundException;

  /**
   * Gets all the initial attachments of a process given by its definition UUID.
   * 
   * @param processUUID
   *          the process definition UUID
   * @return a set of all the attachments of a process
   * @throws ProcessNotFoundException
   *           if the process with the given process name does not exist.
   */
  Set<InitialAttachment> getProcessAttachments(ProcessDefinitionUUID processUUID) throws ProcessNotFoundException;

  /**
   * Gets the initial attachment of a process given by its definition UUID and the attachment name.
   * 
   * @param processUUID
   *          the process definition UUID
   * @param attachmentName
   *          the attachment name
   * @return the attachment relative to the given process UUID and attachment name or null if the attachment name does
   *         not exist.
   * @throws ProcessNotFoundException
   *           if the process with the given process name does not exist.
   */
  InitialAttachment getProcessAttachment(ProcessDefinitionUUID processUUID, String attachmentName)
      throws ProcessNotFoundException;

  /**
   * Gets the attachment definitions of a process given by its definition UUID.
   * 
   * @param processUUID
   *          the process definition UUID
   * @return a set of all the attachment relative to the given process UUID
   * @throws ProcessNotFoundException
   *           if the process with the given process name does not exist.
   */
  Set<AttachmentDefinition> getAttachmentDefinitions(ProcessDefinitionUUID processUUID) throws ProcessNotFoundException;

  /**
   * Gets the attachment definition of a process given by its definition UUID and the attachment name.
   * 
   * @param processUUID
   *          the process definition UUID
   * @param attachmentName
   *          the attachment name
   * @return the attachment definition or null if the attachment name does not exist
   * @throws ProcessNotFoundException
   *           if the process with the given process name does not exist.
   */
  AttachmentDefinition getAttachmentDefinition(ProcessDefinitionUUID processUUID, String attachmentName)
      throws ProcessNotFoundException;

  /**
   * Gets a resource of a process given by its definition UUID and the resource path
   * 
   * @param definitionUUID
   *          the process definition UUID
   * @param resourcePath
   *          the path to the resource
   * @return a byte array representing the resource or null if the resource does not exist.
   * @throws ProcessNotFoundException
   *           if the process with the given process name does not exist.
   */
  byte[] getResource(ProcessDefinitionUUID definitionUUID, String resourcePath) throws ProcessNotFoundException;

  /**
   * Returns the light-weight process definitions for the specified processDefinition UUIDs.
   * 
   * @param processUUIDs
   * @return the set of light-weight process definitions
   */
  Set<LightProcessDefinition> getLightProcesses(Set<ProcessDefinitionUUID> processUUIDs)
      throws ProcessNotFoundException;

  /**
   * Returns a "page" (i.e. a set) of processes ordered by the given criterion.<br>
   * Only the process from the given ProcessDefinitionUUIDs will be considered.
   * 
   * @param fromIndex
   *          from which index the page is built
   * @param pageSize
   *          the number of page elements
   * @param pagingCriterion
   *          the criterion used to sort the retrieved process
   * @return an ordered list of LightProcessDefinition
   */
  List<LightProcessDefinition> getLightProcesses(Set<ProcessDefinitionUUID> processUUIDs, int fromIndex, int pageSize,
      ProcessDefinitionCriterion pagingCriterion) throws ProcessNotFoundException;

  /**
   * Returns all the light-weight process definitions except those for which processDefinition UUID is specified.
   * 
   * @param processesUUID
   * @param fromIndex
   * @param pageSize
   * @return
   */
  List<LightProcessDefinition> getAllLightProcessesExcept(Set<ProcessDefinitionUUID> processUUIDs, int fromIndex,
      int pageSize);

  /**
   * Returns all the light-weight process definitions except those for which processDefinition UUID is specified. The
   * retrieved process are ordered by the given criterion.
   * 
   * @param processesUUID
   * @param fromIndex
   * @param pageSize
   * @return
   */
  List<LightProcessDefinition> getAllLightProcessesExcept(Set<ProcessDefinitionUUID> processUUIDs, int fromIndex,
      int pageSize, ProcessDefinitionCriterion pagingCriterion);

  /**
   * Gets the processes having the given category.
   * 
   * @param category
   * @return
   */
  Set<ProcessDefinitionUUID> getProcessUUIDs(String category);

  Set<ActivityDefinitionUUID> getProcessTaskUUIDs(ProcessDefinitionUUID processsUUID) throws ProcessNotFoundException;

  /**
   * Gets the migration date for the given processUUID
   * 
   * @param processUUID
   *          the ProcessDefinitionUUID
   * @return the migration date for the given processUUID
   * @throws ProcessNotFoundException
   */
  Date getMigrationDate(final ProcessDefinitionUUID processUUID) throws ProcessNotFoundException;
}