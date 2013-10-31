/**
 * Copyright (C) 2010  BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
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

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.ow2.bonita.facade.exception.ActivityNotFoundException;
import org.ow2.bonita.facade.exception.InstanceNotFoundException;
import org.ow2.bonita.facade.exception.ProcessNotFoundException;
import org.ow2.bonita.facade.exception.VariableNotFoundException;
import org.ow2.bonita.facade.runtime.InitialAttachment;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;

/**
 * Bonita Repair API. 
 * This API's purpose is to repair and adjust process instances.
 * @author Anthony Birembaut
 *
 */
public interface RepairAPI {
  
  /**
   * Starts a new execution in a process instance at a specific activity.
   * All connectors of specified activity will be executed normally (include connectors OnReady).
   * @param instanceUUID the process instance UUID
   * @param activityName the activity to execute
   * @return the {@link ActivityInstanceUUID} of the activity
   * @throws InstanceNotFoundException
   * @throws ActivityNotFoundException
   * @throws VariableNotFoundException
   */
  ActivityInstanceUUID startExecution(ProcessInstanceUUID instanceUUID, String activityName) throws InstanceNotFoundException, ActivityNotFoundException, VariableNotFoundException;
  
  /**
   * Stops all the active executions for an activity of a given instance.
   * @param instanceUUID the process instance UUID
   * @param activityName the name of the activity whose executions should be stopped stops
   * @throws InstanceNotFoundException
   * @throws ActivityNotFoundException
   */
  void stopExecution(ProcessInstanceUUID instanceUUID, String activityName) throws InstanceNotFoundException, ActivityNotFoundException;

  /**
   * Create a new instance based on an existing one, being able to set its variables values. The activities located before the current activities are not executed.
   * @param instanceUUID the process instance UUID
   * @param processVariables the process variables in this Map will override the current process variable values
   * @param attachments the attachments in this Collection override the current attachments values
   * @return the {@link ProcessInstanceUUID} of the copy
   * @throws InstanceNotFoundException
   * @throws VariableNotFoundException
   */
  ProcessInstanceUUID copyProcessInstance(ProcessInstanceUUID instanceUUID, Map<String, Object> processVariables, Collection<InitialAttachment> attachments) throws InstanceNotFoundException, VariableNotFoundException;
  
  /**
   * Create a new instance based on an existing one, being able to set its variables values. The activities located before the current activities are not executed.
   * @param instanceUUID the process instance UUID
   * @param processVariables the process variables in this Map will override the current process variable values
   * @param attachments the attachments in this Collection override the current attachments values
   * @param restoreVariableValuesAtDate restore the process variables and attachment values at this date
   * @return the {@link ProcessInstanceUUID} of the copy
   * @throws InstanceNotFoundException
   * @throws VariableNotFoundException
   */
  ProcessInstanceUUID copyProcessInstance(ProcessInstanceUUID instanceUUID, Map<String, Object> processVariables, Collection<InitialAttachment> attachments, Date restoreVariableValuesAtDate) throws InstanceNotFoundException, VariableNotFoundException;

  /**
   * Starts a new process instance, positioned at specific activities, with a map of instance's variable values. The activities located before the start activities are not executed.
   * All connectors of specified activities will be executed normally (include connectors OnReady).
   * @param processDefinitionUUID the process definition UUID
   * @param processVariables the process variables in this Map will override the current process variable values
   * @param attachments the attachments in this Collection override the current attachments values
   * @param startActivitiesNames the list of activities on which to start an execution
   * @return the {@link ProcessInstanceUUID} of the started instance
   * @throws ProcessNotFoundException
   * @throws VariableNotFoundException
   * @throws ActivityNotFoundException
   */
  ProcessInstanceUUID instantiateProcess(ProcessDefinitionUUID processDefinitionUUID, Map<String, Object> processVariables, Collection<InitialAttachment> attachments, List<String> startActivitiesNames) throws ProcessNotFoundException, VariableNotFoundException, ActivityNotFoundException;

  /**
   * Starts a new process instance, positioned at specific activities, with a map of instance's variable values. The activities located before the start activities are not executed.
   * All connectors of specified activities will be executed normally (include connectors OnReady).
   * @param processDefinitionUUID the process definition UUID
   * @param processVariables the process variables in this Map will override the current process variable values
   * @param attachments the attachments in this Collection override the current attachments values
   * @param startActivitiesNames the list of activities on which to start an execution
   * @param instanceInitiator the instance initiator of the process
   * @return the {@link ProcessInstanceUUID} of the started instance
   * @throws ProcessNotFoundException
   * @throws VariableNotFoundException
   * @throws ActivityNotFoundException
   */
  ProcessInstanceUUID instantiateProcess(ProcessDefinitionUUID processDefinitionUUID, Map<String, Object> processVariables, Collection<InitialAttachment> attachments, List<String> startActivitiesNames, String instanceInitiator) throws ProcessNotFoundException, VariableNotFoundException, ActivityNotFoundException;
}
