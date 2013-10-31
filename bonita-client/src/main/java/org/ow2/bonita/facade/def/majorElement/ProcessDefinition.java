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
 * Modified by Matthieu Chaffotte - BonitaSoft S.A.
 **/
package org.ow2.bonita.facade.def.majorElement;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ow2.bonita.facade.def.element.AttachmentDefinition;
import org.ow2.bonita.facade.def.element.HookDefinition;
import org.ow2.bonita.facade.def.element.impl.IterationDescriptor;
import org.ow2.bonita.light.LightProcessDefinition;

/**
 * This interface represents the Process definition.
 * It's derived from the WorkflowProcess of XPDL.
 */
public interface ProcessDefinition extends LightProcessDefinition {

  /**
   * Process state are used to define the process life cycle.<br>
   * A process can be enable, disable several times. But when a process from a disable state goes
   * to archived state, its life cycle is done.
   */
  public static enum ProcessState {
    ENABLED, DISABLED, ARCHIVED
  }

  /**
   * Gets all meta-data of the process.
   * @return all meta-data of the process
   */
  Map<String, String> getMetaData();

  /**
   * Gets a meta-data of the process according to its key.
   * @param key the key of the meta-data
   * @return the meta-data
   */
  String getAMetaData(String key);

  /**
   * Gets the attachments of the process
   * @return the attachments of the process
   */
  Map<String, AttachmentDefinition> getAttachments();

  /**
   * Gets an attachment of the process according to its name
   * @param name the attachment name
   * @return an attachment of the process
   */
  AttachmentDefinition getAttachment(String name);

  /**
   * Obtains process connectors.
   * @return the process connector list
   */
  List<HookDefinition> getConnectors();

  /**
   * Gets process dataFields.
   * @return the process dataFields.
   */
  Set<DataFieldDefinition> getDataFields();

  /**
   * Gets the participants of the process.
   * @return the participant list
   */
  Set<ParticipantDefinition> getParticipants();

  /**
   * Gets the process activities.
   * @return the process activities.
   */
  Set<ActivityDefinition> getActivities();

  /**
   * Gets the process transitions.
   * @return the process transitions
   */
  Set<TransitionDefinition> getTransitions();

  /**
   * Gets the process sub-processes.
   * @return the process sub-processes
   */
  Set<String> getSubProcesses();

  /**
   * Gets the process dependencies (sub-processes) of this process.<br>
   * Note: If a sub-process of this process is defined in another process, it is not possible
   * to undeploy these sub-processes until this process is undeployed.
   * @return The set of processDefinitionUUID depending on this process definition.
   */
  Set<String> getProcessDependencies();

  /**
   * Returns class dependencies (java classes used by connectors).
   * @return the class names which connectors of this process depend.
   */
  Set<String> getClassDependencies();

  /**
   * Returns the activity with the given name. Null if no activity exists within the process with the given name.
   * @param name the activity name
   * @return the activity
   */
  ActivityDefinition getActivity(String name);

  /**
   * Gets a dataField according to its name
   * @param name the dataField name
   * @return the dataField
   */
  DataFieldDefinition getDatafield(String name);

  /**
   * Gets the initial activities.
   * @return the initial activities
   */
  Map<String, ActivityDefinition> getInitialActivities();

  /**
   * Gets the final activities.
   * @return the final activities
   */
  Map<String, ActivityDefinition> getFinalActivities();

  /**
   * Gets the iteration descriptors.
   * @return the iteration descriptors
   */
  Set<IterationDescriptor> getIterationDescriptors();

  /**
   * Gets iteration descriptors according to the activity name
   * @param activityName the activity name
   * @return the iteration descriptors of an activity
   */
  Set<IterationDescriptor> getIterationDescriptors(String activityName);

  /**
   * Gets the list of event sub-processes.
   * @return the list of event sub-processes
   */
  List<EventProcessDefinition> getEventSubProcesses();

}
