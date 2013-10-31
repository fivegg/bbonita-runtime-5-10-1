/**
 * Copyright (C) 2007  Bull S. A. S.
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
package org.ow2.bonita.facade.runtime;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.light.LightProcessInstance;

/**
 * Interface for getting recorded (runtime) information on process instance.
 */
public interface ProcessInstance extends LightProcessInstance {

  /**
   * Returns all global variables (for the process instance) defined  within the xml definition file
   * and also optionally added as parameter (variable map) of the instantiateProcess() method.
   * Variable values are ones recorded at instance creation.
   * Map Key is the variable processDefinitionUUID.
   * Map Object is the variable value.
   * An empty map is returned if no variable is found.
   * @return the map containing global (for process instance) variables recorded at instance creation.
   * The variable object (can be: a plain {@link String}, a {@link Boolean}, a {@link Date}, a {@link Long} or a {@link Double}).
   */
  Map<String, Object> getInitialVariableValues();
  /**
   * Returns the value of the variable with the specified key recorded at the instance creation.
   * @param variableId the variable processDefinitionUUID.
   * @return the value of the variable with the specified key recorded at the instance creation.
   * (can be: a plain {@link String}, a {@link Boolean}, a {@link Date}, a {@link Long} or a {@link Double}).
   */
  Object getInitialVariableValue(String variableId);
  /**
   * Returns the map containing all variables with the last updated value.
   * @return The map containing all variables with the last updated value.
   * the variable object can be:
   * a plain {@link String}, a {@link Boolean}, a {@link Date}, a {@link Long} or a {@link Double}.
   */
  Map<String, Object> getLastKnownVariableValues();
  
  /**
   * Returns the list of recorded {@link InstanceStateUpdate state changes}.
   * @return The list of recorded {@link InstanceStateUpdate state changes}.
   */
  List<InstanceStateUpdate> getInstanceStateUpdates();
  /**
   * Gives access to the historic of variables updates.
   * @return The list of recorded informations for the variable updates.
   */
  List<VariableUpdate> getVariableUpdates();
  
  List<Comment> getCommentFeed();
  
  ActivityInstance getActivity(ActivityInstanceUUID activityUUID);

  ActivityInstance getActivity(String activityId, String iterationId, String activityInstanceId);
  
  Set<ActivityInstance> getActivities(String name);
  
  Set<TaskInstance> getTasks();

  
  /**
   * @deprecated replaced by {@link QueryRuntimeAPI.#searchDocuments(org.ow2.bonita.search.DocumentSearchBuilder, int, int)}
   * @return the attachment instances
   */
  @Deprecated
  List<AttachmentInstance> getAttachments();

  /**
   * @deprecated replaced by {@link QueryRuntimeAPI.#searchDocuments(org.ow2.bonita.search.DocumentSearchBuilder, int, int)}
   * @param attachmentName the attachment name
   * @return the attachment instances with the given name
   */
  @Deprecated
  List<AttachmentInstance> getAttachments(String attachmentName);

  Set<String> getInvolvedUsers();

  /**
   * If this instance is a parent execution, it returns UUID of children instances
   * otherwise an empty set
   * @return  If this instance is a parent execution, it returns UUID of children instances
   * otherwise an empty set
   */
  Set<ProcessInstanceUUID> getChildrenInstanceUUID();
  
  Set<ActivityInstance> getActivities();
  
  /**
   * Returns the set of active users. Active users are users having at least one open task in the instance.
   */
  Set<String> getActiveUsers();
  
}
