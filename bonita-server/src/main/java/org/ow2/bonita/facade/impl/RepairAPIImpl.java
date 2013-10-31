/**
 * Copyright (C) 2010  BonitaSoft S.A..
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
package org.ow2.bonita.facade.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ow2.bonita.facade.RepairAPI;
import org.ow2.bonita.facade.def.InternalActivityDefinition;
import org.ow2.bonita.facade.def.InternalProcessDefinition;
import org.ow2.bonita.facade.def.element.impl.AttachmentDefinitionImpl;
import org.ow2.bonita.facade.def.majorElement.ActivityDefinition;
import org.ow2.bonita.facade.def.majorElement.TransitionDefinition;
import org.ow2.bonita.facade.exception.ActivityNotFoundException;
import org.ow2.bonita.facade.exception.DocumentNotFoundException;
import org.ow2.bonita.facade.exception.InstanceNotFoundException;
import org.ow2.bonita.facade.exception.ProcessNotFoundException;
import org.ow2.bonita.facade.exception.VariableNotFoundException;
import org.ow2.bonita.facade.runtime.AttachmentInstance;
import org.ow2.bonita.facade.runtime.InitialAttachment;
import org.ow2.bonita.facade.runtime.VariableUpdate;
import org.ow2.bonita.facade.runtime.impl.InitialAttachmentImpl;
import org.ow2.bonita.facade.runtime.impl.InternalProcessInstance;
import org.ow2.bonita.facade.runtime.impl.InternalProcessInstance.TransitionState;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.runtime.model.ExecuteNode;
import org.ow2.bonita.runtime.model.Execution;
import org.ow2.bonita.services.Document;
import org.ow2.bonita.services.DocumentationManager;
import org.ow2.bonita.services.Recorder;
import org.ow2.bonita.util.BonitaRuntimeException;
import org.ow2.bonita.util.DocumentService;
import org.ow2.bonita.util.EnvTool;
import org.ow2.bonita.util.Misc;
import org.ow2.bonita.util.ProcessUtil;

/**
 * @author Anthony Birembaut, Matthieu Chaffotte
 * 
 */
public class RepairAPIImpl implements RepairAPI {

  private static final Logger LOG = Logger.getLogger(RepairAPIImpl.class.getName());

  protected RepairAPIImpl(final String queryList) {
  }

  @Override
  public ProcessInstanceUUID copyProcessInstance(final ProcessInstanceUUID instanceUUID,
      final Map<String, Object> processVariables, final Collection<InitialAttachment> attachments)
      throws InstanceNotFoundException, VariableNotFoundException {
    return copyProcessInstance(instanceUUID, processVariables, attachments, null);
  }

  @Override
  public ProcessInstanceUUID copyProcessInstance(final ProcessInstanceUUID instanceUUID,
      final Map<String, Object> processVariables, final Collection<InitialAttachment> attachments,
      final Date restoreVariableValuesAtDate) throws InstanceNotFoundException, VariableNotFoundException {

    FacadeUtil.checkArgsNotNull(instanceUUID);

    final InternalProcessInstance processInstance = EnvTool.getJournalQueriers().getProcessInstance(instanceUUID);
    if (processInstance == null) {
      throw new InstanceNotFoundException("bai_REAPII_1", instanceUUID);
    }

    final InternalProcessDefinition processDefinition = EnvTool.getJournalQueriers().getProcess(
        processInstance.getProcessDefinitionUUID());

    final List<String> activitiesNames = new ArrayList<String>();
    final Set<Execution> activiesExecutions = searchActiveExecutions(processInstance.getRootExecution(), null,
        Execution.STATE_ACTIVE);
    for (final Execution execution : activiesExecutions) {
      if (!processInstance.getActivities(execution.getNodeName()).isEmpty()) {
        activitiesNames.add(execution.getNodeName());
      }
    }

    Map<String, Object> variablesToSet = null;
    if (restoreVariableValuesAtDate == null) {
      variablesToSet = processInstance.getLastKnownVariableValues();
    } else {
      variablesToSet = processInstance.getInitialVariableValues();
      final List<VariableUpdate> VariablesUpdates = processInstance.getVariableUpdates();
      for (final VariableUpdate variableUpdate : VariablesUpdates) {
        if (variableUpdate.getDate().getTime() <= restoreVariableValuesAtDate.getTime()) {
          variablesToSet.put(variableUpdate.getName(), variableUpdate.getValue());
        }
      }
    }
    if (processVariables != null) {
      variablesToSet.putAll(processVariables);
    }

    final Map<String, InitialAttachment> attachmentsToSet = new HashMap<String, InitialAttachment>();
    if (processInstance.getNbOfAttachments() > 0) {
      final DocumentationManager manager = EnvTool.getDocumentationManager();
      final List<AttachmentInstance> attachmentInstances = DocumentService.getAllAttachmentVersions(manager,
          instanceUUID);

      for (final AttachmentInstance attachmentInstance : attachmentInstances) {
        if (restoreVariableValuesAtDate == null
            || attachmentInstance.getVersionDate().getTime() <= restoreVariableValuesAtDate.getTime()) {
          final ProcessDefinitionUUID definitionUUID = processDefinition.getUUID();
          final String attachmentName = attachmentInstance.getName();
          final AttachmentDefinitionImpl attachmentDefinition = new AttachmentDefinitionImpl(definitionUUID,
              attachmentName);
          attachmentDefinition.setDescription(attachmentInstance.getDescription());
          attachmentDefinition.setFileName(attachmentInstance.getFileName());
          attachmentDefinition.setLabel(attachmentInstance.getLabel());

          final List<Document> documents = DocumentService.getDocuments(manager, definitionUUID, attachmentName);
          final Document document = documents.get(0);
          byte[] attachmentValue;
          try {
            attachmentValue = manager.getContent(document);
          } catch (final DocumentNotFoundException e) {
            throw new BonitaRuntimeException(e);
          }
          final InitialAttachment initialAttachment = new InitialAttachmentImpl(attachmentDefinition, attachmentValue);
          attachmentsToSet.put(attachmentDefinition.getName(), initialAttachment);
        }
      }
      if (attachments != null) {
        for (final InitialAttachment attachment : attachments) {
          attachmentsToSet.put(attachment.getName(), attachment);
        }
      }
    }

    final String instanceInitiator = processInstance.getStartedBy();
    ProcessInstanceUUID processInstanceCopyUUID = null;
    try {
      processInstanceCopyUUID = instantiateProcess(processInstance.getProcessDefinitionUUID(), variablesToSet,
          attachmentsToSet.values(), activitiesNames, instanceInitiator);
    } catch (final ProcessNotFoundException e) {
      LOG.severe("Process instance " + processDefinition.getUUID() + " not found.");
    } catch (final ActivityNotFoundException e) {
      LOG.severe("Unable to start an execution of activity. Activity " + e.getActivityId() + " not found.");
    }
    return processInstanceCopyUUID;
  }

  @Override
  public ProcessInstanceUUID instantiateProcess(final ProcessDefinitionUUID processDefinitionUUID,
      final Map<String, Object> processVariables, final Collection<InitialAttachment> attachments,
      final List<String> startActivitiesNames) throws ProcessNotFoundException, VariableNotFoundException,
      ActivityNotFoundException {
    return instantiateProcess(processDefinitionUUID, processVariables, attachments, startActivitiesNames,
        EnvTool.getUserId());
  }

  @Override
  public ProcessInstanceUUID instantiateProcess(final ProcessDefinitionUUID processDefinitionUUID,
      final Map<String, Object> processVariables, final Collection<InitialAttachment> attachments,
      final List<String> startActivitiesNames, final String instanceInitiator) throws ProcessNotFoundException,
      VariableNotFoundException, ActivityNotFoundException {
    FacadeUtil.checkArgsNotNull(processDefinitionUUID);
    if (LOG.isLoggable(Level.FINE)) {
      LOG.fine("Starting a new instance of process : " + processDefinitionUUID);
    }
    final Execution rootExecution = ProcessUtil.createProcessInstance(processDefinitionUUID, processVariables,
        attachments, null, null, null, null);
    if (LOG.isLoggable(Level.FINE)) {
      LOG.fine("Started: " + rootExecution.getInstance());
    }

    final Recorder recorder = EnvTool.getRecorder();
    recorder.recordInstanceStarted(rootExecution.getInstance(), instanceInitiator);

    final ProcessInstanceUUID processInstanceUUID = rootExecution.getInstance().getUUID();
    if (startActivitiesNames != null) {
      final Set<String> executionsAlreadyStarted = new HashSet<String>();
      for (final String activityName : startActivitiesNames) {
        // make sure there are no attempts to start several executions of the
        // same activity
        if (!executionsAlreadyStarted.contains(activityName)) {
          try {
            startExecution(processInstanceUUID, activityName);
            executionsAlreadyStarted.add(activityName);
          } catch (final InstanceNotFoundException e) {
            LOG.severe("Unable to start an execution of " + activityName + " in process instance "
                + processInstanceUUID + ". Process instance not found.");
          }
        } else {
          LOG.warning("Unable to start a second execution of " + activityName + " in process instance "
              + processInstanceUUID + ". Another execution of this activity is already active.");
        }
      }
    }
    return processInstanceUUID;
  }

  @Override
  public ActivityInstanceUUID startExecution(final ProcessInstanceUUID instanceUUID, final String activityName)
      throws InstanceNotFoundException, ActivityNotFoundException, VariableNotFoundException {

    FacadeUtil.checkArgsNotNull(instanceUUID, activityName);

    final InternalProcessInstance processInstance = EnvTool.getJournalQueriers().getProcessInstance(instanceUUID);
    if (processInstance == null) {
      throw new InstanceNotFoundException("bai_REAPII_1", instanceUUID);
    }

    final InternalProcessDefinition processDefinition = EnvTool.getJournalQueriers().getProcess(
        processInstance.getProcessDefinitionUUID());

    InternalActivityDefinition activityDefinition = null;
    final Set<ActivityDefinition> activities = processDefinition.getActivities();
    if (activities != null) {
      for (final ActivityDefinition activity : activities) {
        if (activity.getName().equals(activityName)) {
          activityDefinition = (InternalActivityDefinition) activity;
          break;
        }
      }
    }
    if (activityDefinition == null) {
      throw new ActivityNotFoundException("bai_REAPII_2", processDefinition.getUUID(), activityName);
    }

    if (LOG.isLoggable(Level.FINE)) {
      LOG.fine("Starting a new execution of activity " + activityName + " of process instance "
          + processInstance.getProcessDefinitionUUID());
    }

    final Set<TransitionDefinition> incomingTransitions = activityDefinition.getIncomingTransitions();
    for (final TransitionDefinition transitionDefinition : incomingTransitions) {
      final TransitionState ts = processInstance.getTransitionState(transitionDefinition.getName());
      if (ts == null || !ts.equals(TransitionState.TAKEN)) {
        processInstance.setTransitionState(transitionDefinition.getName(), TransitionState.TAKEN);
      }
    }

    final Execution execution = new Execution(activityName, processDefinition, processInstance, activityDefinition,
        Execution.STATE_CREATED, null);
    execution.setIterationId(Misc.getUniqueId("it"));
    execution.setState(Execution.STATE_ACTIVE);
    final Execution rootExecution = processInstance.getRootExecution();
    rootExecution.addExecution(execution);
    execution.performAtomicOperation(new ExecuteNode(), false);

    for (final Execution child : execution.getExecutions()) {
      if (child.getActivityInstanceUUID() != null) {
        return child.getActivityInstanceUUID();
      }
    }
    return null;
  }

  @Override
  public void stopExecution(final ProcessInstanceUUID instanceUUID, final String activityName)
      throws InstanceNotFoundException, ActivityNotFoundException {

    FacadeUtil.checkArgsNotNull(instanceUUID, activityName);

    final InternalProcessInstance processInstance = EnvTool.getJournalQueriers().getProcessInstance(instanceUUID);
    if (processInstance == null) {
      throw new InstanceNotFoundException("bai_REAPII_1", instanceUUID);
    }

    final Set<Execution> activiesExecutions = searchActiveExecutions(processInstance.getRootExecution(), activityName,
        null);
    for (final Execution execution : activiesExecutions) {
      if (!Execution.STATE_CANCELLED.equals(execution.getState())
          && !Execution.STATE_ENDED.equals(execution.getState())) {
        execution.setState(Execution.STATE_ACTIVE);
        execution.cancel();
      }
    }

  }

  private Set<Execution> searchActiveExecutions(final Execution parentExecution, final String activityName,
      final String executionState) {
    final Set<Execution> result = new HashSet<Execution>();
    for (final Execution execution : parentExecution.getExecutions()) {
      if (activityName == null || execution.getName().equals(activityName)) {
        if (executionState == null || execution.getState().equals(executionState)) {
          result.add(execution);
        }
      }
      if (execution.getExecutions() != null) {
        result.addAll(searchActiveExecutions(execution, activityName, executionState));
      }
    }
    return result;
  }

}
