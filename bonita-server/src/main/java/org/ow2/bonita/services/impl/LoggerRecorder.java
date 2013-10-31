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
 * 
 * Modified by Matthieu Chaffotte, Elias Ricken de Medeiros - BonitaSoft S..A
 **/
package org.ow2.bonita.services.impl;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ow2.bonita.facade.def.InternalProcessDefinition;
import org.ow2.bonita.facade.runtime.ActivityInstance;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.Category;
import org.ow2.bonita.facade.runtime.impl.InternalProcessInstance;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.runtime.event.Master;
import org.ow2.bonita.services.Recorder;
import org.ow2.bonita.type.lob.Lob;

/**
 * @author Pierre Vigneras
 */
public class LoggerRecorder implements Recorder {

  private static final Logger LOG = Logger.getLogger(LoggerRecorder.class.getName());
  private final Level level;

  public LoggerRecorder() {
    this(Level.FINE);
  }

  public LoggerRecorder(final String levelName) {
    this(Level.parse(levelName));
  }

  public LoggerRecorder(final Level level) {
    this.level = level;
  }

  public void recordProcessDeployed(final InternalProcessDefinition processDef, String userId) {
    if (LOG.isLoggable(this.level)) {
      LOG.log(this.level, "Deployed process: processDefinitionUUID: " + processDef.getUUID()
          + ", processId: " + processDef.getName() + ", userId: "
          + userId + ", description: " + processDef.getDescription());
    }
  }
  
  public void recordProcessEnable(final InternalProcessDefinition processDef) {
    if (LOG.isLoggable(this.level)) {
      LOG.log(this.level, "Enable process: processDefinitionUUID: " + processDef.getUUID()
          + ", processId: " + processDef.getName() + ", userId: "
          + processDef.getDeployedBy() + ", description: " + processDef.getDescription());
    }
  }

  public void recordProcessDisable(final InternalProcessDefinition processDef) {
    if (LOG.isLoggable(this.level)) {
      LOG.log(this.level, "Disable process: processDefinitionUUID: "
          + processDef.getUUID());
    }
  }
  
  public void recordProcessArchive(final InternalProcessDefinition processDef, final String userId) {
    if (LOG.isLoggable(this.level)) {
      LOG.log(this.level, "Archive process: processDefinitionUUID: "
          + processDef.getUUID() + ", userId = " + userId);
    }
  }

  public void recordInstanceVariableUpdated(final String variableId, final Object variableValue,
      final ProcessInstanceUUID instanceUUID, final String userId) {
    if (LOG.isLoggable(this.level)) {
      LOG.log(this.level, "Global variable updated: processUUID: "
          + instanceUUID + ", variableId: " + variableId + ", userId = " + userId);
    }
  }

  public void recordActivityVariableUpdated(final String variableId, final Object variableValue,
      final ActivityInstanceUUID activityUUID, final String userId) {
    if (LOG.isLoggable(this.level)) {
      LOG.log(this.level, "Local variable updated: activityUUID: "
          + activityUUID + ", variableId: " + variableId + ", userId = " + userId);
    }
  }

  public void remove(final InternalProcessDefinition processDef) {
    if (LOG.isLoggable(this.level)) {
      LOG.log(this.level, "ProcessDefinition " + processDef.getUUID() + " removed.");
    }
  }

  public void remove(final InternalProcessInstance processInst) {
    if (LOG.isLoggable(this.level)) {
      LOG.log(this.level, "ProcessInstance " + processInst.getUUID() + " removed.");
    }
  }
  public void clear() {
    if (LOG.isLoggable(this.level)) {
      LOG.log(this.level, "Clearing recorder");
    }
  }

  public void recordBodyEnded(final ActivityInstance activityInstance) {
    if (LOG.isLoggable(this.level)) {
      LOG.log(this.level, "Activity body ended: " + activityInstance.getActivityName()
          + " (" + activityInstance.getProcessInstanceUUID() + ")");
    }
  }

  public void recordBodyAborted(final ActivityInstance activityInstance) {
    if (LOG.isLoggable(this.level)) {
      LOG.log(this.level, "Activity body aborted: " + activityInstance.getActivityName()
          + " (" + activityInstance.getProcessInstanceUUID() + ")");
    }
  }

  public void recordBodyCancelled(final ActivityInstance activityInstance) {
    if (LOG.isLoggable(this.level)) {
      LOG.log(this.level, "Activity body cancelled: " + activityInstance.getActivityName()
          + " (" + activityInstance.getProcessInstanceUUID() + ")");
    }
  }

  public void recordBodyStarted(final ActivityInstance activityInstance) {
    if (LOG.isLoggable(this.level)) {
      LOG.log(this.level, "Activity body started: " + activityInstance.getActivityName()
          + " (" + activityInstance.getProcessInstanceUUID() + ")");
    }
  }

  public void recordEnterActivity(ActivityInstance activityInstance) {
    if (LOG.isLoggable(this.level)) {
      LOG.log(this.level, "Activity ready: "
          + activityInstance.getActivityName()
          + " (" + activityInstance.getProcessInstanceUUID() + ")");
    }

  }
  
  public void recordActivityFailed(ActivityInstance activityInstance) {
    if (LOG.isLoggable(this.level)) {
      LOG.log(this.level, "Activity failed: "
          + activityInstance.getActivityName()
          + " (" + activityInstance.getProcessInstanceUUID() + ")");
    }
  }

  public void recordInstanceEnded(ProcessInstanceUUID instanceUUID, String loggedInUserId) {
    if (LOG.isLoggable(this.level)) {
      LOG.log(this.level, "Ended instance: "
          + instanceUUID
          + ", userId = " + loggedInUserId);
    }
  }

  public void recordInstanceAborted(ProcessInstanceUUID instanceUUID, String loggedInUserId) {
    if (LOG.isLoggable(this.level)) {
      LOG.log(this.level, "Aborted instance: "
          + instanceUUID
          + ", userId = " + loggedInUserId);
    }
  }

  public void recordInstanceCancelled(ProcessInstanceUUID instanceUUID, String loggedInUserId) {
    if (LOG.isLoggable(this.level)) {
      LOG.log(this.level, "Cancelled instance: "
          + instanceUUID
          + ", userId = " + loggedInUserId);
    }
  }

  public void recordInstanceStarted(InternalProcessInstance instance, String loggedInUserId) {
    if (LOG.isLoggable(this.level)) {
      LOG.log(this.level, "Started instance: "
          + instance.getUUID()
          + ", userId = " + loggedInUserId);
    }
  }

  public void recordTaskAssigned(final ActivityInstanceUUID taskUUID, final ActivityState taskState, final String loggedInUserId,
      final Set<String> candidates, final String assignedUserId) {
    if (LOG.isLoggable(this.level)) {
      LOG.log(this.level, "Task " + taskUUID
          + " Assigned by: " + loggedInUserId
          + " Assigned to: " + assignedUserId
          + "and candidates: " + candidates);
    }
  }

  public void recordTaskFinished(final ActivityInstanceUUID taskUUID, final String loggedInUserId) {
    if (LOG.isLoggable(this.level)) {
      LOG.log(this.level, "Task " + taskUUID + " finished by " + loggedInUserId);
    }
  }
  public void recordTaskReady(final ActivityInstanceUUID taskUUID, final Set<String> candidates, final String userId) {
    if (LOG.isLoggable(this.level)) {
      LOG.log(this.level, "Task ready " + taskUUID);
    }
  }

  public void recordTaskResumed(final ActivityInstanceUUID taskUUID, final String loggedInUserId) {
    if (LOG.isLoggable(this.level)) {
      LOG.log(this.level, "Task " + taskUUID + " resumed by " + loggedInUserId + ".");
    }
  }

  public void recordTaskStarted(final ActivityInstanceUUID taskUUID, final String loggedInUserId) {
    if (LOG.isLoggable(this.level)) {
      LOG.log(this.level, "Task " + taskUUID + " started by " + loggedInUserId + ".");
    }
  }

  public void recordTaskSuspended(final ActivityInstanceUUID taskUUID, final String loggedInUserId) {
    if (LOG.isLoggable(this.level)) {
      LOG.log(this.level, "Task " + taskUUID + " suspended by " + loggedInUserId + ".");
    }
  }

	public void recordTaskSkipped(ActivityInstanceUUID taskUUID, String loggedInUserId) {
		if (LOG.isLoggable(this.level)) {
			LOG.log(this.level, "Task " + taskUUID + " skipped by " + loggedInUserId + ".");
		}
	}
	
  public void recordActivitySkipped(ActivityInstance activityInstance,
      String loggedInUserId) {
    if (LOG.isLoggable(this.level)) {
      LOG.log(this.level, "Activity " + activityInstance.getUUID() + " skipped by " + loggedInUserId + ".");
    }
  }
  
  public void removeLob(Lob lob) {
    if (LOG.isLoggable(this.level)) {
      LOG.log(this.level, "Removing lob " + lob + ".");
    } 
  }

  public void recordActivityPriorityUpdated(ActivityInstanceUUID activityUUID, int priority) {
    if (LOG.isLoggable(this.level)) {
      LOG.log(this.level, "Activity " + activityUUID + " priority changed to " + priority + ".");
    }
  }

  public void recordNewCategory(Category category) {
    if (LOG.isLoggable(this.level)) {
      LOG.log(this.level, "Category " + category.getName() + " created.");
    }
  }
  
  @Override
	public void recordNewMaster(Master master) {
	  if (LOG.isLoggable(this.level)) {
	      LOG.log(this.level, "Master " + master.getNode() + " created.");
	    }
	}

 }
