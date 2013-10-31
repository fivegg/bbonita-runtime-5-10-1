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
 * Modified by Elias Ricken de Medeiros, MAtthieu Chaffotte - BonitaSoft S.A.
 **/
package org.ow2.bonita.services;

import java.util.Set;

import org.ow2.bonita.env.binding.JournalBinding;
import org.ow2.bonita.facade.def.InternalProcessDefinition;
import org.ow2.bonita.facade.runtime.ActivityInstance;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.Category;
import org.ow2.bonita.facade.runtime.impl.InternalProcessInstance;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.runtime.event.Master;

/**
 * A Recorder is responsible for storing <strong>runtime</strong> instances
 * such as currently deployed processes, running instances,
 * activities and tasks.
 *
 * Usually, recorded records are available using queries on a {@link JournalBinding}.
 *
 * @author Pierre Vigneras
 * @see JournalBinding
 */
public interface Recorder {

  String DEFAULT_KEY = "recorder";

  /*
   * ACTIVITY
   */
  void recordEnterActivity(ActivityInstance activityInstance);
  void recordBodyStarted(ActivityInstance activityInstance);
  void recordBodyEnded(ActivityInstance activityInstance);  
  void recordBodyAborted(ActivityInstance activityInstance);
  void recordBodyCancelled(ActivityInstance activityInstance);
  void recordActivityFailed(ActivityInstance activityInstance);
  void recordActivitySkipped(ActivityInstance activityInstance, String loggedInUserId);


  /*
   * INSTANCE
   */
  void recordInstanceStarted(InternalProcessInstance instance, String loggedInUserId);
  void recordInstanceEnded(ProcessInstanceUUID instanceUUID, String loggedInUserId);
  void recordInstanceAborted(ProcessInstanceUUID instanceUUID, String loggedInUserId);
  void recordInstanceCancelled(ProcessInstanceUUID instanceUUID, String loggedInUserId);

  /*
   * TASK
   */
  void recordTaskReady(ActivityInstanceUUID taskUUID, Set<String> candidates, String userId);
  void recordTaskStarted(ActivityInstanceUUID taskUUID, String loggedInUserId);
  void recordTaskFinished(ActivityInstanceUUID taskUUID, String loggedInUserId);
  void recordTaskSuspended(ActivityInstanceUUID taskUUID, String loggedInUserId);
  void recordTaskResumed(ActivityInstanceUUID taskUUID, String loggedInUserId);
  void recordTaskAssigned(ActivityInstanceUUID taskUUID, ActivityState taskState, String loggedInUserId, Set<String> candidates, String assignedUserId);
  void recordTaskSkipped(ActivityInstanceUUID taskUUID, String loggedInUserId);
  /*
   * DEFINITION
   */
  void recordProcessDeployed(InternalProcessDefinition processDef, String userId);
  void recordProcessEnable(InternalProcessDefinition processDef);
  void recordProcessDisable(InternalProcessDefinition processDef);
  void recordProcessArchive(InternalProcessDefinition processDef, String userId);

  /*
   * VARIABLE UPDATES
   */

  void recordInstanceVariableUpdated(String variableId, Object variableValue, ProcessInstanceUUID instanceUUID, String userId);
  void recordActivityVariableUpdated(String variableId, Object variableValue, ActivityInstanceUUID activityUUID, String userId);

  /*
   * PRIORITY
   */
  void recordActivityPriorityUpdated(ActivityInstanceUUID activityUUID, int priority);
  
  /**
   * Generic method for removing a journal record.
   *
   * Implementation may delegate to overloaded methods according to record type.
   * @param processInst the record to archive
   */
  void remove(InternalProcessInstance processInst);
  void remove(InternalProcessDefinition processDef);

  /*
   * Categories
   */
  /**
   * @param aName
   * @return
   */
  void recordNewCategory(Category category);
  
  void recordNewMaster(Master master);
}
