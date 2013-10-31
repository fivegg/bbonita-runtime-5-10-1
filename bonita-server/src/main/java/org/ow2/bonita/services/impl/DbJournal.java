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
 * Modified by Matthieu Chaffotte, Elias Ricken de Medeiros, Charles Souillard - BonitaSoft S.A.
 **/
package org.ow2.bonita.services.impl;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.ow2.bonita.facade.def.InternalProcessDefinition;
import org.ow2.bonita.facade.def.element.MetaData;
import org.ow2.bonita.facade.def.element.impl.MetaDataImpl;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition.ProcessState;
import org.ow2.bonita.facade.runtime.ActivityInstance;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.Category;
import org.ow2.bonita.facade.runtime.InstanceState;
import org.ow2.bonita.facade.runtime.StateUpdate;
import org.ow2.bonita.facade.runtime.TaskInstance;
import org.ow2.bonita.facade.runtime.VariableUpdate;
import org.ow2.bonita.facade.runtime.impl.InternalActivityInstance;
import org.ow2.bonita.facade.runtime.impl.InternalProcessInstance;
import org.ow2.bonita.facade.runtime.impl.InternalVariableUpdate;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.persistence.JournalDbSession;
import org.ow2.bonita.runtime.VariablesOptions;
import org.ow2.bonita.runtime.event.Master;
import org.ow2.bonita.services.Journal;
import org.ow2.bonita.type.Variable;
import org.ow2.bonita.util.BonitaConstants;
import org.ow2.bonita.util.EnvTool;
import org.ow2.bonita.util.ExceptionManager;
import org.ow2.bonita.util.Misc;
import org.ow2.bonita.util.VariableUtil;

/**
 * @author Guillaume Porcher
 * 
 */
public class DbJournal extends AbstractDbQuerier implements Journal {

  public DbJournal(final String persistenceServiceName) {
    super(persistenceServiceName);
  }

  @Override
  public JournalDbSession getDbSession() {
    return EnvTool.getJournalDbSession(getPersistenceServiceName());
  }

  @Override
  public List<String> getInstanceIdsFromMetadata(final int index, final int maxResult) {
    return getDbSession().getInstanceIdsFromMetadata(index, maxResult);
  }

  @Override
  public void remove(final InternalProcessDefinition pack) {
    Misc.checkArgsNotNull(pack);
    getDbSession().delete(pack);
  }

  @Override
  public void removeExecution(final long id) {
    Misc.checkArgsNotNull(id);
    getDbSession().deleteExecution(id);
  }

  @Override
  public void remove(final InternalProcessInstance processInst) {
    Misc.checkArgsNotNull(processInst);
    processInst.removeAttachments();
    getDbSession().delete(processInst);
    // TODO
  }

  @Override
  public void recordEnterActivity(final ActivityInstance activityInstance) {
    Misc.checkArgsNotNull(activityInstance);

    final InternalProcessInstance processInstance = getProcessInstance(activityInstance.getProcessInstanceUUID());
    processInstance.addActivity(activityInstance);
  }

  @Override
  public void recordBodyStarted(final ActivityInstance activityInstance) {
    Misc.checkArgsNotNull(activityInstance);
    if (!activityInstance.isTask()) {
      ((InternalActivityInstance) activityInstance).setActivityState(ActivityState.EXECUTING,
          BonitaConstants.SYSTEM_USER);
    }
  }

  @Override
  public void recordBodyEnded(final ActivityInstance activityInstance) {
    Misc.checkArgsNotNull(activityInstance);
    if (!activityInstance.isTask()) {
      ((InternalActivityInstance) activityInstance).setActivityState(ActivityState.FINISHED,
          BonitaConstants.SYSTEM_USER);
    }
  }

  @Override
  public void recordBodyAborted(final ActivityInstance activityInstance) {
    Misc.checkArgsNotNull(activityInstance);
    ((InternalActivityInstance) activityInstance).setActivityState(ActivityState.ABORTED, BonitaConstants.SYSTEM_USER);
  }

  @Override
  public void recordBodyCancelled(final ActivityInstance activityInstance) {
    Misc.checkArgsNotNull(activityInstance);
    ((InternalActivityInstance) activityInstance).setActivityState(ActivityState.CANCELLED, EnvTool.getUserId());
  }

  @Override
  public void recordActivityFailed(final ActivityInstance activityInstance) {
    Misc.checkArgsNotNull(activityInstance);
    ((InternalActivityInstance) activityInstance).setActivityState(ActivityState.FAILED, BonitaConstants.SYSTEM_USER);
  }

  @Override
  public void recordActivitySkipped(final ActivityInstance activityInstance, final String loggedInUserId) {
    Misc.checkArgsNotNull(activityInstance, loggedInUserId);
    ((InternalActivityInstance) activityInstance).setActivityState(ActivityState.SKIPPED, loggedInUserId);
  }

  @Override
  public void recordInstanceEnded(final ProcessInstanceUUID instanceUUID, final String loggedInUserId) {
    Misc.checkArgsNotNull(instanceUUID);

    final InternalProcessInstance instanceRecord = getProcessInstance(instanceUUID);

    final String message = ExceptionManager.getInstance().getFullMessage("bsi_DJ_1", instanceUUID);
    Misc.badStateIfNull(instanceRecord, message);
    instanceRecord.setInstanceState(InstanceState.FINISHED, loggedInUserId);

  }

  @Override
  public void recordInstanceAborted(final ProcessInstanceUUID instanceUUID, final String loggedInUserId) {
    Misc.checkArgsNotNull(instanceUUID);

    final InternalProcessInstance instanceRecord = getProcessInstance(instanceUUID);

    final String message = ExceptionManager.getInstance().getFullMessage("bsi_DJ_2", instanceUUID);
    Misc.badStateIfNull(instanceRecord, message);
    instanceRecord.setInstanceState(InstanceState.ABORTED, loggedInUserId);

  }

  @Override
  public void recordInstanceCancelled(final ProcessInstanceUUID instanceUUID, final String loggedInUserId) {
    Misc.checkArgsNotNull(instanceUUID);

    final InternalProcessInstance instanceRecord = getProcessInstance(instanceUUID);

    final String message = ExceptionManager.getInstance().getFullMessage("bsi_DJ_3", instanceUUID);
    Misc.badStateIfNull(instanceRecord, message);
    instanceRecord.setInstanceState(InstanceState.CANCELLED, loggedInUserId);
  }

  @Override
  public void recordInstanceStarted(final InternalProcessInstance instance, final String loggedInUserId) {
    Misc.checkArgsNotNull(instance);

    if (instance.getParentInstanceUUID() != null) {
      final InternalProcessInstance parentInstance = getProcessInstance(instance.getParentInstanceUUID());
      parentInstance.addChildInstance(instance.getUUID());
    }

    final String message = ExceptionManager.getInstance().getFullMessage("bsi_DJ_4");
    Misc.badStateIfNull(loggedInUserId, message);
    instance.setInstanceState(InstanceState.STARTED, loggedInUserId);
    getDbSession().save(instance);
  }

  @Override
  public void recordTaskFinished(final ActivityInstanceUUID taskUUID, final String loggedInUserId) {
    Misc.checkArgsNotNull(taskUUID);

    final TaskInstance activity = getTaskInstance(taskUUID);
    final String message = ExceptionManager.getInstance().getFullMessage("bsi_DJ_5", taskUUID);
    Misc.badStateIfNull(activity, message);
    ((InternalActivityInstance) activity).setActivityState(ActivityState.FINISHED, loggedInUserId);
  }

  @Override
  public void recordTaskReady(final ActivityInstanceUUID taskUUID, final Set<String> candidates, final String userId) {
    Misc.checkArgsNotNull(taskUUID);

    final TaskInstance activity = getTaskInstance(taskUUID);
    final String message = ExceptionManager.getInstance().getFullMessage("bsi_DJ_6", taskUUID);
    Misc.badStateIfNull(activity, message);
    if (userId != null) {
      ((InternalActivityInstance) activity).setTaskAssign(ActivityState.READY, BonitaConstants.SYSTEM_USER, userId);
    } else {
      ((InternalActivityInstance) activity).setTaskAssign(ActivityState.READY, BonitaConstants.SYSTEM_USER, candidates);
    }

  }

  @Override
  public void recordTaskStarted(final ActivityInstanceUUID taskUUID, final String loggedInUserId) {
    Misc.checkArgsNotNull(taskUUID);

    final TaskInstance activity = getTaskInstance(taskUUID);
    final String message = ExceptionManager.getInstance().getFullMessage("bsi_DJ_7", taskUUID);
    Misc.badStateIfNull(activity, message);
    ((InternalActivityInstance) activity).setActivityState(ActivityState.EXECUTING, loggedInUserId);
  }

  @Override
  public void recordTaskResumed(final ActivityInstanceUUID taskUUID, final String loggedInUserId) {
    Misc.checkArgsNotNull(taskUUID);

    final TaskInstance task = getTaskInstance(taskUUID);
    ActivityState stateBeforeSuspend = null;
    for (final StateUpdate su : task.getStateUpdates()) {
      if (su.getActivityState().equals(ActivityState.SUSPENDED)) {
        stateBeforeSuspend = su.getInitialState();
      }
    }

    ((InternalActivityInstance) task).setActivityState(stateBeforeSuspend, loggedInUserId);
  }

  @Override
  public void recordTaskSuspended(final ActivityInstanceUUID taskUUID, final String loggedInUserId) {
    Misc.checkArgsNotNull(taskUUID);

    final TaskInstance activity = getTaskInstance(taskUUID);
    ((InternalActivityInstance) activity).setActivityState(ActivityState.SUSPENDED, loggedInUserId);
  }

  @Override
  public void recordTaskSkipped(final ActivityInstanceUUID taskUUID, final String loggedInUserId) {
    Misc.checkArgsNotNull(taskUUID);
    final TaskInstance activity = getTaskInstance(taskUUID);
    ((InternalActivityInstance) activity).setActivityState(ActivityState.SKIPPED, loggedInUserId);

  }

  @Override
  public void recordTaskAssigned(final ActivityInstanceUUID taskUUID, final ActivityState taskState,
      final String loggedInUserId, final Set<String> candidates, final String assignedUserId) {
    Misc.checkArgsNotNull(taskUUID);

    final TaskInstance activity = getTaskInstance(taskUUID);
    if (assignedUserId != null) {
      ((InternalActivityInstance) activity).setTaskAssign(taskState, loggedInUserId, assignedUserId);
    } else {
      ((InternalActivityInstance) activity).setTaskAssign(taskState, loggedInUserId, candidates);
    }
  }

  @Override
  public void recordProcessDeployed(final InternalProcessDefinition processDef, final String userId) {
    Misc.checkArgsNotNull(processDef);
    processDef.setState(ProcessState.ENABLED);
    processDef.setDeployedDate(new Date());
    processDef.setDeployedBy(userId);
    getDbSession().save(processDef);
  }

  @Override
  public void recordProcessEnable(final InternalProcessDefinition processDef) {
    Misc.checkArgsNotNull(processDef);
    processDef.setState(ProcessState.ENABLED);
  }

  @Override
  public void recordProcessDisable(final InternalProcessDefinition processDef) {
    Misc.checkArgsNotNull(processDef);
    processDef.setState(ProcessState.DISABLED);
  }

  @Override
  public void recordProcessArchive(final InternalProcessDefinition processDef, final String userId) {
    Misc.checkArgsNotNull(processDef);
    processDef.setUndeployedBy(userId);
    processDef.setUndeployedDate(new Date());
    processDef.setState(ProcessState.ARCHIVED);
  }

  public void recordProcessUndeployed(final InternalProcessDefinition processDef) {
    recordProcessDisable(processDef);
  }

  @Override
  public void recordActivityVariableUpdated(final String variableId, final Object variableValue,
      final ActivityInstanceUUID activityUUID, final String userId) {
    Misc.checkArgsNotNull(variableId, activityUUID, userId);

    final ActivityInstance activityInst = getActivityInstance(activityUUID);
    final Variable v = VariableUtil.createVariable(activityInst.getProcessDefinitionUUID(), variableId, variableValue);

    final VariablesOptions variablesOptions = EnvTool.getVariablesOptions();
    if (variablesOptions.isStoreHistory()) {
      final VariableUpdate varUpdate = new InternalVariableUpdate(new Date(), userId, variableId, v);
      ((InternalActivityInstance) activityInst).addVariableUpdate(varUpdate);
      ((InternalActivityInstance) activityInst).updateLastUpdateDate();
    } else {
      ((InternalActivityInstance) activityInst).setVariableValue(variableId, v);
    }
  }

  @Override
  public void recordInstanceVariableUpdated(final String variableId, final Object variableValue,
      final ProcessInstanceUUID instanceUUID, final String userId) {
    Misc.checkArgsNotNull(variableId, instanceUUID, userId);

    final InternalProcessInstance processInst = getProcessInstance(instanceUUID);
    final Variable v = VariableUtil.createVariable(processInst.getProcessDefinitionUUID(), variableId, variableValue);

    final VariablesOptions variablesOptions = EnvTool.getVariablesOptions();
    if (variablesOptions.isStoreHistory()) {
      final VariableUpdate varUpdate = new InternalVariableUpdate(new Date(), userId, variableId, v);
      processInst.addVariableUpdate(varUpdate);
      processInst.updateLastUpdateDate();
    } else {
      processInst.setVariableValue(variableId, v);
    }
  }

  @Override
  public void recordActivityPriorityUpdated(final ActivityInstanceUUID activityUUID, final int priority) {
    Misc.checkArgsNotNull(activityUUID);
    final ActivityInstance activityInstance = getActivityInstance(activityUUID);
    ((InternalActivityInstance) activityInstance).setPriority(priority);
  }

  @Override
  public void storeMetaData(final String key, final String value) {
    Misc.checkArgsNotNull(key, value);
    MetaDataImpl data = getInternalMetaData(key);
    if (data == null) {
      data = new MetaDataImpl(key, value);
      getDbSession().save(data);
    } else {
      data.setValue(value);
    }
  }

  private MetaDataImpl getInternalMetaData(final String key) {
    Misc.checkArgsNotNull(key);
    return getDbSession().getMetaData(key);
  }

  @Override
  public String getMetaData(final String key) {
    final MetaData data = getInternalMetaData(key);
    if (data == null) {
      return null;
    }
    return data.getValue();
  }

  @Override
  public void deleteMetaData(final String key) {
    Misc.checkArgsNotNull(key);
    final MetaDataImpl data = getInternalMetaData(key);
    if (data != null) {
      getDbSession().delete(data);
    }
  }

  @Override
  public long getLockedMetadata(final String key) {
    Misc.checkArgsNotNull(key);
    return getDbSession().getLockedMetadata(key);
  }

  @Override
  public void removeLockedMetadata(final String key) {
    Misc.checkArgsNotNull(key);
    getDbSession().lockMetadata(key);
  }

  @Override
  public void lockMetadata(final String key) {
    Misc.checkArgsNotNull(key);
    getDbSession().lockMetadata(key);
  }

  @Override
  public void updateLockedMetadata(final String key, final long value) {
    Misc.checkArgsNotNull(key);
    getDbSession().updateLockedMetadata(key, value);
  }

  @Override
  public void recordNewCategory(final Category category) {
    Misc.checkArgsNotNull(category);
    getDbSession().save(category);
  }
  
  @Override
	public void recordNewMaster(Master master) {
	  Misc.checkArgsNotNull(master);
	  getDbSession().save(master);
	}

}
