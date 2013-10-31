/**
 * Copyright (C) 2006 Bull S. A. S.
 * Bull, Rue Jean Jaures, B.P.68, 78340, Les Clayes-sous-Bois
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 * 
 * Modified by Matthieu Chaffotte, Elias Ricken de Medeiros - BonitaSoft S.A.
 **/
package org.ow2.bonita.facade.impl;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.ow2.bonita.connector.core.Connector;
import org.ow2.bonita.connector.core.Filter;
import org.ow2.bonita.connector.core.Mapper;
import org.ow2.bonita.connector.core.RoleResolver;
import org.ow2.bonita.definition.activity.ConnectorExecutor;
import org.ow2.bonita.facade.APIAccessor;
import org.ow2.bonita.facade.QueryDefinitionAPI;
import org.ow2.bonita.facade.QueryRuntimeAPI;
import org.ow2.bonita.facade.RuntimeAPI;
import org.ow2.bonita.facade.def.InternalProcessDefinition;
import org.ow2.bonita.facade.def.majorElement.DataFieldDefinition;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition.ProcessState;
import org.ow2.bonita.facade.exception.ActivityDefNotFoundException;
import org.ow2.bonita.facade.exception.ActivityNotFoundException;
import org.ow2.bonita.facade.exception.BonitaInternalException;
import org.ow2.bonita.facade.exception.DataFieldNotFoundException;
import org.ow2.bonita.facade.exception.DocumentNotFoundException;
import org.ow2.bonita.facade.exception.DocumentationCreationException;
import org.ow2.bonita.facade.exception.EventNotFoundException;
import org.ow2.bonita.facade.exception.IllegalTaskStateException;
import org.ow2.bonita.facade.exception.InstanceNotFoundException;
import org.ow2.bonita.facade.exception.ProcessNotFoundException;
import org.ow2.bonita.facade.exception.TaskNotFoundException;
import org.ow2.bonita.facade.exception.UncancellableInstanceException;
import org.ow2.bonita.facade.exception.UndeletableInstanceException;
import org.ow2.bonita.facade.exception.VariableNotFoundException;
import org.ow2.bonita.facade.runtime.ActivityInstance;
import org.ow2.bonita.facade.runtime.AttachmentInstance;
import org.ow2.bonita.facade.runtime.Comment;
import org.ow2.bonita.facade.runtime.ConnectorExecutionDescriptor;
import org.ow2.bonita.facade.runtime.InitialAttachment;
import org.ow2.bonita.facade.runtime.InstanceState;
import org.ow2.bonita.facade.runtime.ProcessInstance;
import org.ow2.bonita.facade.runtime.impl.CommentImpl;
import org.ow2.bonita.facade.runtime.impl.InternalActivityInstance;
import org.ow2.bonita.facade.runtime.impl.InternalProcessInstance;
import org.ow2.bonita.facade.uuid.AbstractUUID;
import org.ow2.bonita.facade.uuid.ActivityDefinitionUUID;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.CatchingEventUUID;
import org.ow2.bonita.facade.uuid.DocumentUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.runtime.ActivityManager;
import org.ow2.bonita.runtime.ClassDataLoader;
import org.ow2.bonita.runtime.TaskManager;
import org.ow2.bonita.runtime.event.EventConstants;
import org.ow2.bonita.runtime.event.Job;
import org.ow2.bonita.runtime.event.OutgoingEventInstance;
import org.ow2.bonita.runtime.model.Execution;
import org.ow2.bonita.services.Archiver;
import org.ow2.bonita.services.DocumentationManager;
import org.ow2.bonita.services.EventService;
import org.ow2.bonita.services.Querier;
import org.ow2.bonita.services.Recorder;
import org.ow2.bonita.util.AccessorUtil;
import org.ow2.bonita.util.BonitaConstants;
import org.ow2.bonita.util.BonitaException;
import org.ow2.bonita.util.BonitaRuntimeException;
import org.ow2.bonita.util.DocumentService;
import org.ow2.bonita.util.EnvTool;
import org.ow2.bonita.util.ExceptionManager;
import org.ow2.bonita.util.GroovyBindingBuilder;
import org.ow2.bonita.util.GroovyBindingBuilder.PropagateBinding;
import org.ow2.bonita.util.GroovyException;
import org.ow2.bonita.util.GroovyUtil;
import org.ow2.bonita.util.Misc;
import org.ow2.bonita.util.ProcessUtil;
import org.ow2.bonita.util.TransientData;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

/**
 * @author Marc Blachon, Guillaume Porcher, Charles Souillard, Miguel Valdes,
 *         Pierre Vigneras
 */
public class RuntimeAPIImpl implements RuntimeAPI {

  /**
   * @author Baptiste Mesta
   * 
   */
  private abstract class ExecuteInClassLoader {

    abstract Map<String, Object> executeInClassLoader(ClassLoader classLoaderToUse, ClassDataLoader classDataLoader)
        throws Exception;

    public Map<String, Object> execute(final ClassLoader classLoader, final ProcessDefinitionUUID definitionUUID)
        throws Exception {
      final ClassLoader baseClassLoader = Thread.currentThread().getContextClassLoader();
      try {
        ClassLoader classLoaderToUse = null;
        ClassDataLoader classDataLoader = null;
        if (classLoader != null) {
          classLoaderToUse = classLoader;
        } else {
          if (definitionUUID == null) {
            classDataLoader = EnvTool.getClassDataLoader();
            // no need to set the classloader here (using the global
            // classloader)
          } else {
            classLoaderToUse = EnvTool.getClassDataLoader().getProcessClassLoader(definitionUUID);
          }
        }
        if (classLoaderToUse != null) {
          Thread.currentThread().setContextClassLoader(classLoaderToUse);
        }
        return executeInClassLoader(classLoaderToUse, classDataLoader);
      } finally {
        Thread.currentThread().setContextClassLoader(baseClassLoader);
      }
    }

  }

  private static final Logger LOG = Logger.getLogger(RuntimeAPIImpl.class.getName());

  private final String queryList;

  protected RuntimeAPIImpl(final String queryList) {
    this.queryList = queryList;
  }

  private String getQueryList() {
    return queryList;
  }

  @Override
  public void enableEventsInFailure(final ActivityInstanceUUID activityUUID) {
    final EventService eventService = EnvTool.getEventService();
    eventService.enableEventsInFailureIncomingEvents(activityUUID);
  }

  @Override
  public void enableEventsInFailure(final ProcessInstanceUUID instanceUUID, final String activityName) {
    final Set<InternalActivityInstance> activities = EnvTool.getAllQueriers().getActivityInstances(instanceUUID,
        activityName);
    for (final InternalActivityInstance activity : activities) {
      enableEventsInFailure(activity.getUUID());
    }
  }

  @Override
  public void enablePermanentEventInFailure(final ActivityDefinitionUUID activityUUID) {
    final EventService eventService = EnvTool.getEventService();
    eventService.enablePermanentEventsInFailure(activityUUID);
  }

  /**
   * Create an instance of the specified process and return the processUUID
   */
  @Override
  public ProcessInstanceUUID instantiateProcess(final ProcessDefinitionUUID processUUID)
      throws ProcessNotFoundException {
    try {
      return instantiateProcess(processUUID, null, null);
    } catch (final VariableNotFoundException e) {
      // must never occur
      throw new BonitaRuntimeException(e);
    }
  }

  @Override
  public ProcessInstanceUUID instantiateProcess(final ProcessDefinitionUUID processUUID,
      final ActivityDefinitionUUID activityUUID) throws ProcessNotFoundException {
    try {
      return instantiateProcess(processUUID, null, null, activityUUID);
    } catch (final VariableNotFoundException e) {
      // must never occur
      throw new BonitaRuntimeException(e);
    }
  }

  @Override
  public ProcessInstanceUUID instantiateProcess(final ProcessDefinitionUUID processUUID,
      final Map<String, Object> variables) throws ProcessNotFoundException, VariableNotFoundException {
    return instantiateProcess(processUUID, variables, null);
  }

  @Override
  public ProcessInstanceUUID instantiateProcess(final ProcessDefinitionUUID processUUID,
      final Map<String, Object> variables, final Collection<InitialAttachment> attachments)
      throws ProcessNotFoundException, VariableNotFoundException {
    return instantiateProcess(processUUID, variables, attachments, null);
  }

  private ProcessInstanceUUID instantiateProcess(final ProcessDefinitionUUID processUUID,
      final Map<String, Object> variables, final Collection<InitialAttachment> attachments,
      final ActivityDefinitionUUID activityUUID) throws ProcessNotFoundException, VariableNotFoundException {
    FacadeUtil.checkArgsNotNull(processUUID);
    final ProcessState state = new QueryDefinitionAPIImpl(queryList).getProcess(processUUID).getState();
    if (ProcessState.DISABLED.equals(state)) {
      final String message = ExceptionManager.getInstance().getFullMessage("bai_RAPII_36", processUUID);
      throw new BonitaRuntimeException(message);
    }
    if (LOG.isLoggable(Level.FINE)) {
      LOG.fine("Starting a new instance of process : " + processUUID);
    }
    final Execution rootExecution = ProcessUtil.createProcessInstance(processUUID, variables, attachments, null, null,
        activityUUID, null);
    if (LOG.isLoggable(Level.FINE)) {
      LOG.fine("Started: " + rootExecution.getInstance());
    }
    final ProcessInstance instance = rootExecution.getInstance();
    final ProcessInstanceUUID instanceUUID = instance.getUUID();
    ProcessUtil.startEventSubProcesses(instance);
    rootExecution.getInstance().begin(activityUUID);
    return instanceUUID;
  }

  @Override
  public void executeTask(final ActivityInstanceUUID taskUUID, final boolean assignTask) throws TaskNotFoundException,
      IllegalTaskStateException {
    startTask(taskUUID, assignTask);
    finishTask(taskUUID, assignTask);
  }

  @Override
  public void cancelProcessInstance(final ProcessInstanceUUID instanceUUID) throws InstanceNotFoundException,
      UncancellableInstanceException {
    // if this instance is a child execution, throw an exception
    FacadeUtil.checkArgsNotNull(instanceUUID);
    final InternalProcessInstance instance = FacadeUtil.getInstance(instanceUUID, null);
    if (instance == null) {
      throw new InstanceNotFoundException("bai_RAPII_1", instanceUUID);
    }
    // if this instance is a child execution, throw an exception
    if (instance.getParentInstanceUUID() != null || !instance.getInstanceState().equals(InstanceState.STARTED)) {
      throw new UncancellableInstanceException("bai_RAPII_2", instanceUUID, instance.getParentInstanceUUID(),
          instance.getInstanceState());
    }
    instance.cancel();
  }

  @Override
  public void cancelProcessInstances(final Collection<ProcessInstanceUUID> instanceUUIDs)
      throws InstanceNotFoundException, UncancellableInstanceException {
    FacadeUtil.checkArgsNotNull(instanceUUIDs);
    for (final ProcessInstanceUUID instanceUUID : instanceUUIDs) {
      cancelProcessInstance(instanceUUID);
    }
  }

  @Override
  public void deleteProcessInstances(final Collection<ProcessInstanceUUID> instanceUUIDs)
      throws InstanceNotFoundException, UndeletableInstanceException {
    if (instanceUUIDs != null) {
      for (final ProcessInstanceUUID instanceUUID : instanceUUIDs) {
        deleteProcessInstance(instanceUUID);
      }
    }
  }

  @Override
  public void deleteProcessInstance(final ProcessInstanceUUID instanceUUID) throws InstanceNotFoundException,
      UndeletableInstanceException {
    // if this instance is a child execution, throw an exception
    // if this instance has children, delete them
    FacadeUtil.checkArgsNotNull(instanceUUID);

    final Querier allQueriers = EnvTool.getAllQueriers();
    final Querier journal = EnvTool.getJournalQueriers();
    final Querier history = EnvTool.getHistoryQueriers();

    InternalProcessInstance processInst = journal.getProcessInstance(instanceUUID);

    boolean inHistory = false;
    final boolean inJournal = processInst != null;
    if (!inJournal) {
      processInst = history.getProcessInstance(instanceUUID);
      inHistory = processInst != null;
    }

    if (processInst == null) {
      throw new InstanceNotFoundException("bai_RAPII_3", instanceUUID);
    }
    final ProcessInstanceUUID parentInstanceUUID = processInst.getParentInstanceUUID();
    // check that the parent instance does not exist anymore, else, throw an
    // exception
    if (parentInstanceUUID != null && allQueriers.getProcessInstance(parentInstanceUUID) != null) {
      throw new UndeletableInstanceException("bai_RAPII_4", instanceUUID, parentInstanceUUID);
    }

    EnvTool.getLargeDataRepository().deleteData(Misc.getAttachmentCategories(instanceUUID));
    ProcessUtil.removeAllInstanceEvents(processInst);

    if (inJournal) {
      final Recorder recorder = EnvTool.getRecorder();
      recorder.remove(processInst);
    } else if (inHistory) {
      final Archiver archiver = EnvTool.getArchiver();
      archiver.remove(processInst);
    }
    final Set<ProcessInstanceUUID> children = processInst.getChildrenInstanceUUID();
    for (final ProcessInstanceUUID child : children) {
      deleteProcessInstance(child);
    }
  }

  @Override
  public void deleteAllProcessInstances(final Collection<ProcessDefinitionUUID> processUUIDs)
      throws ProcessNotFoundException, UndeletableInstanceException {
    FacadeUtil.checkArgsNotNull(processUUIDs);
    for (final ProcessDefinitionUUID processUUID : processUUIDs) {
      deleteAllProcessInstances(processUUID);
    }
  }

  @Override
  public void deleteAllProcessInstances(final ProcessDefinitionUUID processUUID) throws ProcessNotFoundException,
      UndeletableInstanceException {
    FacadeUtil.checkArgsNotNull(processUUID);
    final Querier querier = EnvTool.getAllQueriers();
    final ProcessDefinition process = querier.getProcess(processUUID);
    if (process == null) {
      throw new ProcessNotFoundException("bai_RAPII_5", processUUID);
    }
    deleteAllProcessInstances(process);
  }

  private void deleteAllProcessInstances(final ProcessDefinition process) throws ProcessNotFoundException,
      UndeletableInstanceException {
    FacadeUtil.checkArgsNotNull(process);
    final ProcessDefinitionUUID processUUID = process.getUUID();
    final Querier querier = EnvTool.getAllQueriers();

    List<InternalProcessInstance> parentProcessInstances = querier.getParentProcessInstances(processUUID, 0, 200);
    do {
      for (final InternalProcessInstance parentProcessInstance : parentProcessInstances) {
        try {
          deleteProcessInstance(parentProcessInstance.getUUID());
        } catch (final InstanceNotFoundException infe) {
          final String message = ExceptionManager.getInstance().getFullMessage("bai_RAPII_6");
          throw new BonitaInternalException(message, infe);
        }
      }
      parentProcessInstances = querier.getParentProcessInstances(processUUID, 0, 200);
    } while (!parentProcessInstances.isEmpty());

    final Set<InternalProcessInstance> instances = querier.getProcessInstances(processUUID);
    if (instances != null && !instances.isEmpty()) {
      final ProcessInstance first = instances.iterator().next();
      throw new UndeletableInstanceException("bai_RAPII_7", first.getUUID(), first.getParentInstanceUUID());
    }
  }

  @Override
  public void startTask(final ActivityInstanceUUID taskUUID, final boolean assignTask) throws TaskNotFoundException,
      IllegalTaskStateException {
    FacadeUtil.checkArgsNotNull(taskUUID);
    TaskManager.start(taskUUID, assignTask);
  }

  @Override
  public void startActivity(final ActivityInstanceUUID activityUUID) throws ActivityNotFoundException {
    // nothing
  }

  @Override
  public void finishTask(final ActivityInstanceUUID taskUUID, final boolean assignTask) throws TaskNotFoundException,
      IllegalTaskStateException {
    FacadeUtil.checkArgsNotNull(taskUUID);
    TaskManager.finish(taskUUID, assignTask);
  }

  @Override
  public void suspendTask(final ActivityInstanceUUID taskUUID, final boolean assignTask) throws TaskNotFoundException,
      IllegalTaskStateException {
    FacadeUtil.checkArgsNotNull(taskUUID);
    TaskManager.suspend(taskUUID, assignTask);
  }

  @Override
  public void resumeTask(final ActivityInstanceUUID taskUUID, final boolean taskAssign) throws TaskNotFoundException,
      IllegalTaskStateException {
    FacadeUtil.checkArgsNotNull(taskUUID);
    TaskManager.resume(taskUUID, taskAssign);
  }

  @Override
  public void assignTask(final ActivityInstanceUUID taskUUID) throws TaskNotFoundException {
    FacadeUtil.checkArgsNotNull(taskUUID);
    TaskManager.assign(taskUUID);
  }

  @Override
  public void assignTask(final ActivityInstanceUUID taskUUID, final String userId) throws TaskNotFoundException {
    FacadeUtil.checkArgsNotNull(taskUUID, userId);
    TaskManager.assign(taskUUID, userId);
  }

  @Override
  public void assignTask(final ActivityInstanceUUID taskUUID, final Set<String> candidates)
      throws TaskNotFoundException {
    FacadeUtil.checkArgsNotNull(taskUUID, candidates);
    TaskManager.assign(taskUUID, candidates);
  }

  @Override
  public void unassignTask(final ActivityInstanceUUID taskUUID) throws TaskNotFoundException {
    FacadeUtil.checkArgsNotNull(taskUUID);
    TaskManager.unAssign(taskUUID);
  }

  private String getDataTypeClassName(final String variableId, final Object variableValue, final AbstractUUID uuid) {
    final boolean mayBeAnXmlDocument = mayBeAnXMLDocument(variableValue);
    if (mayBeAnXmlDocument) {
      String dataTypeClassName = null;
      DataFieldDefinition dataFieldDefinition = null;
      final String dataFieldName = Misc.getVariableName(variableId);
      final APIAccessor accessor = new StandardAPIAccessorImpl();
      final QueryDefinitionAPI queryDefinitionAPI = accessor.getQueryDefinitionAPI(AccessorUtil.QUERYLIST_JOURNAL_KEY);

      try {
        if (uuid instanceof ProcessDefinitionUUID) {
          dataFieldDefinition = queryDefinitionAPI.getProcessDataField((ProcessDefinitionUUID) uuid, dataFieldName);
        } else if (uuid instanceof ActivityDefinitionUUID) {
          dataFieldDefinition = queryDefinitionAPI.getActivityDataField((ActivityDefinitionUUID) uuid, dataFieldName);
        }
        // do that only if the string may be a Document
        dataTypeClassName = dataFieldDefinition.getDataTypeClassName();
      } catch (final Exception e) {
        throw new BonitaRuntimeException("unable to find datafield with name: " + dataFieldName + " in " + uuid);
      }
      return dataTypeClassName;
    }
    return null;
  }

  private boolean mayBeAnXMLDocument(final Object variableValue) {
    final boolean mayBeAnXmlDocument = variableValue instanceof String
        && (((String) variableValue).trim().startsWith("<") || ((String) variableValue).trim().startsWith("&lt;"));
    return mayBeAnXmlDocument;
  }

  @Override
  public void setProcessInstanceVariable(final ProcessInstanceUUID instanceUUID, final String variableId,
      final Object variableValue) throws InstanceNotFoundException, VariableNotFoundException {
    final InternalProcessInstance instance = EnvTool.getJournalQueriers().getProcessInstance(instanceUUID);
    AttachmentInstance attachment = null;
    if (instance == null) {
      throw new InstanceNotFoundException("bai_RAPII_20", instanceUUID);
    }
    final String variableName = Misc.getVariableName(variableId);
    if (!instance.getLastKnownVariableValues().containsKey(variableName)) {
      final DocumentationManager manager = EnvTool.getDocumentationManager();
      List<AttachmentInstance> attachments = new ArrayList<AttachmentInstance>();
      if (instance.getNbOfAttachments() > 0) {
        attachments = DocumentService.getAllAttachmentVersions(manager, instanceUUID, variableId);
      }
      if (attachments.size() == 0) {
        throw new VariableNotFoundException("bai_RAPII_21", instanceUUID, variableName);
      } else {
        attachment = attachments.get(attachments.size() - 1);
      }
    }

    Object newValue = variableValue;
    String targetVariable = variableId;
    final String dataTypeClassName = getDataTypeClassName(variableId, variableValue,
        instance.getProcessDefinitionUUID());
    if (attachment == null) {
      if (variableId.contains(BonitaConstants.XPATH_VAR_SEPARATOR)) {
        try {
          targetVariable = Misc.getVariableName(variableId);
          newValue = getXMLValueXPath(variableId, variableValue, null, instanceUUID);
        } catch (final Exception e) {
          throw new VariableNotFoundException("bai_RAPII_32", instanceUUID, variableId);
        }
      } else if (variableId.contains(BonitaConstants.JAVA_VAR_SEPARATOR)) {
        try {
          targetVariable = Misc.getVariableName(variableId);
          newValue = getModifiedJavaObject(variableId, variableValue, null, instance);
        } catch (final Exception ex) {
          throw new VariableNotFoundException("bai_RAPII_34", instanceUUID, null, variableId);
        }
      } else if (Document.class.getName().equals(dataTypeClassName) && variableValue instanceof String) {
        try {
          newValue = Misc.generateDocument((String) variableValue);
        } catch (final Exception e) {
          throw new BonitaRuntimeException("Unable to build a DOM Document from String: " + variableValue);
        }
      }
      EnvTool.getRecorder().recordInstanceVariableUpdated(targetVariable, newValue, instance.getUUID(),
          EnvTool.getUserId());
    } else {
      if (variableValue instanceof byte[]) {
        addAttachment(instanceUUID, attachment.getName(), attachment.getFileName(), (byte[]) variableValue);
      } else if (variableValue instanceof AttachmentInstance) {
        final AttachmentInstance newAttachment = (AttachmentInstance) variableValue;
        byte[] attachmentValue;
        try {
          final DocumentationManager manager = EnvTool.getDocumentationManager();
          final org.ow2.bonita.services.Document document = manager.getDocument(newAttachment.getUUID().getValue());
          attachmentValue = manager.getContent(document);
        } catch (final DocumentNotFoundException e) {
          throw new BonitaRuntimeException(e);
        }
        addAttachment(instanceUUID, attachment.getName(), newAttachment.getFileName(), attachmentValue);
      } else {
        final String message = ExceptionManager.getInstance().getMessage("bai_RAPII_37");
        throw new IllegalArgumentException(message);
      }
    }
  }

  @Override
  public void setProcessInstanceVariables(final ProcessInstanceUUID instanceUUID, final Map<String, Object> variables)
      throws InstanceNotFoundException, VariableNotFoundException {
    for (final Entry<String, Object> variable : variables.entrySet()) {
      setProcessInstanceVariable(instanceUUID, variable.getKey(), variable.getValue());
    }
  }

  @Override
  public void setActivityInstanceVariable(final ActivityInstanceUUID activityUUID, final String variableId,
      final Object variableValue) throws ActivityNotFoundException, VariableNotFoundException {
    final ActivityInstance activity = EnvTool.getJournalQueriers().getActivityInstance(activityUUID);
    if (activity == null) {
      throw new ActivityNotFoundException("bai_RAPII_22", activityUUID);
    }
    final ProcessInstanceUUID instanceUUID = activity.getProcessInstanceUUID();
    final String activityId = activity.getActivityName();

    Object newValue = variableValue;
    String targetVariable = variableId;
    final String dataTypeClassName = getDataTypeClassName(variableId, variableValue,
        activity.getActivityDefinitionUUID());
    if (variableId.contains(BonitaConstants.XPATH_VAR_SEPARATOR)) {
      try {
        targetVariable = Misc.getVariableName(variableId);
        newValue = getXMLValueXPath(variableId, variableValue, activityUUID, null);
      } catch (final Exception e) {
        throw new VariableNotFoundException("bai_RAPII_31", instanceUUID, activityId, variableId);
      }
    } else if (variableId.contains(BonitaConstants.JAVA_VAR_SEPARATOR)) {
      try {
        targetVariable = Misc.getVariableName(variableId);
        newValue = getModifiedJavaObject(variableId, variableValue, activity, null);
      } catch (final Exception ex) {
        throw new VariableNotFoundException("bai_RAPII_35", instanceUUID, activityId, variableId);
      }
    } else if (Document.class.getName().equals(dataTypeClassName) && variableValue instanceof String) {
      try {
        newValue = Misc.generateDocument((String) variableValue);
      } catch (final Exception e) {
        throw new BonitaRuntimeException("Unable to build a DOM Document from String: " + variableValue);
      }
    }

    // search the variable in the transient variables
    final Map<String, Object> transientVariables = TransientData.getActivityTransientVariables(activityUUID);
    if (transientVariables != null && transientVariables.containsKey(targetVariable)) {
      TransientData.updateActivityTransientVariableValue(activityUUID, targetVariable, newValue);
    } else {
      // search in the database
      if (!activity.getLastKnownVariableValues().containsKey(targetVariable)) {
        throw new VariableNotFoundException("bai_RAPII_24", instanceUUID, activityId, targetVariable);
      }

      // local variable updated -> update only current activity
      final Recorder recorder = EnvTool.getRecorder();
      recorder.recordActivityVariableUpdated(targetVariable, newValue, activityUUID, EnvTool.getUserId());
    }
  }

  @Override
  public void setActivityInstanceVariables(final ActivityInstanceUUID activityUUID, final Map<String, Object> variables)
      throws ActivityNotFoundException, VariableNotFoundException {
    for (final Entry<String, Object> variable : variables.entrySet()) {
      setActivityInstanceVariable(activityUUID, variable.getKey(), variable.getValue());
    }
  }

  private Object getModifiedJavaObject(final String variableExpression, final Object attributeValue,
      final ActivityInstance activity, final ProcessInstance processInstance) throws ActivityNotFoundException,
      VariableNotFoundException, InstanceNotFoundException {
    final String variableName = Misc.getVariableName(variableExpression);
    ActivityInstanceUUID activityUUID = null;
    if (activity != null) {
      activityUUID = activity.getUUID();
    }
    ProcessInstanceUUID processInstanceUUID = null;
    if (processInstance != null) {
      processInstanceUUID = processInstance.getUUID();
    }
    final Object data = getVariable(variableName, activityUUID, processInstanceUUID);
    final ProcessDefinitionUUID processDefUUID = getProcessDefinitionUUID(activity, processInstance);
    return getModifiedJavaObject(processDefUUID, variableExpression, data, attributeValue);
  }

  private ProcessDefinitionUUID getProcessDefinitionUUID(final ActivityInstance activity,
      final ProcessInstance processInstance) {
    ProcessDefinitionUUID processDefUUID = null;
    if (processInstance != null) {
      processDefUUID = processInstance.getProcessDefinitionUUID();
    } else {
      processDefUUID = activity.getProcessDefinitionUUID();
    }
    return processDefUUID;
  }

  @Override
  public Object getModifiedJavaObject(final ProcessDefinitionUUID processUUID, final String variableExpression,
      final Object variableValue, final Object attributeValue) {
    final String variableName = Misc.getVariableName(variableExpression);
    final String groovyPlaceholderAccessExpression = Misc.getGroovyPlaceholderAccessExpression(variableExpression);
    final String setterName = Misc.getSetterName(variableExpression);
    final ClassLoader processClassLoader = EnvTool.getClassDataLoader().getProcessClassLoader(processUUID);
    return modifyJavaObject(variableValue, variableName, groovyPlaceholderAccessExpression, setterName, attributeValue,
        processClassLoader);
  }

  private Object modifyJavaObject(final Object data, final String variableName,
      final String groovyPlaceholderAccessExpression, final String setterName, final Object variableValue,
      final ClassLoader classLoader) {
    final GroovyShell shell = new GroovyShell(classLoader);
    shell.setProperty(variableName, data);
    shell.setProperty("__variableValue__", variableValue);
    final StringBuilder script = new StringBuilder();
    script.append("def __tmp__ =");
    if (groovyPlaceholderAccessExpression != null && groovyPlaceholderAccessExpression.trim().length() > 0) {
      script.append(groovyPlaceholderAccessExpression);
    } else {
      script.append(variableName);
    }
    script.append(";\n");
    script.append("__tmp__.");
    script.append(setterName);
    script.append("(__variableValue__);\n");
    script.append(variableName);
    return shell.evaluate(script.toString());
  }

  private Object getXMLValueXPath(final String variableId, final Object variableValue,
      final ActivityInstanceUUID activityUUID, final ProcessInstanceUUID processInstanceUUID) throws Exception {
    final String variableName = Misc.getVariableName(variableId);
    final Document doc = (Document) getVariable(variableName, activityUUID, processInstanceUUID);
    return getXMLValueXPath(variableId, variableValue, doc);
  }

  private Object getXMLValueXPath(final String variableId, final Object variableValue, final Document doc)
      throws Exception {
    final String xpathExpression = Misc.getXPath(variableId);
    final boolean isAppend = Misc.isXMLAppend(variableId);
    return evaluateXPath(doc, xpathExpression, isAppend, variableValue);
  }

  private Object getVariable(final String variableName, final ActivityInstanceUUID activityUUID,
      final ProcessInstanceUUID processInstanceUUID) throws ActivityNotFoundException, VariableNotFoundException,
      InstanceNotFoundException {
    Object oldValue = null;
    final QueryRuntimeAPI queryAPI = new StandardAPIAccessorImpl().getQueryRuntimeAPI();
    if (activityUUID != null) {
      oldValue = queryAPI.getVariable(activityUUID, variableName);
    } else {
      oldValue = queryAPI.getProcessInstanceVariable(processInstanceUUID, variableName);
    }
    return oldValue;
  }

  private Document evaluateXPath(final Document doc, final String xpathExpression, final boolean isAppend,
      final Object variableValue) throws XPathExpressionException, ParserConfigurationException, SAXException,
      IOException {
    final XPath xpath = XPathFactory.newInstance().newXPath();
    final Node node = (Node) xpath.compile(xpathExpression).evaluate(doc, XPathConstants.NODE);
    if (isSetAttribute(xpathExpression, variableValue)) {
      if (node == null) { // Create the attribute
        final String parentPath = xpathExpression.substring(0, xpathExpression.lastIndexOf('/'));
        final String attributeName = xpathExpression.substring(xpathExpression.lastIndexOf('/') + 2); // +1
                                                                                                      // for
                                                                                                      // @
        final Node parentNode = (Node) xpath.compile(parentPath).evaluate(doc, XPathConstants.NODE);
        if (parentNode instanceof Element) {
          final Element element = (Element) parentNode;
          if (variableValue instanceof String) {
            element.setAttribute(attributeName, (String) variableValue);
          } else if (variableValue instanceof Attr) {
            element.setAttribute(((Attr) variableValue).getName(), ((Attr) variableValue).getTextContent());
          }
        }
      } else if (node instanceof Attr) { // Set an existing attribute
        if (variableValue instanceof String) {
          node.setTextContent((String) variableValue);
        } else if (variableValue instanceof Attr) {
          node.setTextContent(((Attr) variableValue).getTextContent());
        }
      } else if (node instanceof Element) { // add attribute to an element
        final Attr attr = (Attr) variableValue;
        ((Element) node).setAttribute(attr.getName(), attr.getValue());
      }
    } else if (node instanceof Text) {
      node.setTextContent((String) variableValue);
    } else if (node instanceof Element) {
      Node newNode = null;
      if (variableValue instanceof Node) {
        newNode = doc.importNode((Node) variableValue, true);
      } else if (variableValue instanceof String) {
        newNode = doc.importNode(Misc.generateDocument((String) variableValue).getDocumentElement(), true);
      }

      if (isAppend) {
        node.appendChild(newNode);
      } else { // replace
        final Node parentNode = node.getParentNode();
        parentNode.removeChild(node);
        parentNode.appendChild(newNode);
      }
    } else if (node == null && xpathExpression.endsWith("/text()") && variableValue instanceof String) {
      final String parentPath = xpathExpression.substring(0, xpathExpression.lastIndexOf('/'));
      final Node parentNode = (Node) xpath.compile(parentPath).evaluate(doc, XPathConstants.NODE);
      parentNode.appendChild(doc.createTextNode((String) variableValue));
    }
    return doc;
  }

  private boolean isSetAttribute(final String xpathExpression, final Object variableValue) {
    if (variableValue instanceof Attr) {
      return true;
    } else {
      final String[] segments = xpathExpression.split("/");
      return segments[segments.length - 1].startsWith("@");
    }
  }

  @Override
  public void setVariable(final ActivityInstanceUUID activityUUID, final String variableId, final Object variableValue)
      throws ActivityNotFoundException, VariableNotFoundException {
    try {
      setActivityInstanceVariable(activityUUID, variableId, variableValue);
    } catch (final Throwable e) {
      final ActivityInstance activity = EnvTool.getAllQueriers().getActivityInstance(activityUUID);
      if (activity == null) {
        throw new ActivityNotFoundException("bai_RAPII_25", activityUUID);
      }
      try {
        setProcessInstanceVariable(activity.getProcessInstanceUUID(), variableId, variableValue);
      } catch (final InstanceNotFoundException e1) {
        // If activity exists, the process instance must exist too.
        Misc.unreachableStatement();
      }
    }
  }

  @Override
  public void addComment(final ProcessInstanceUUID instanceUUID, final String message, final String userId)
      throws InstanceNotFoundException {
    final CommentImpl comment = new CommentImpl(userId, message, instanceUUID);
    addComment(comment, instanceUUID);
  }

  @Override
  public void addComment(final ActivityInstanceUUID activityUUID, final String message, final String userId)
      throws ActivityNotFoundException, InstanceNotFoundException {
    final ActivityInstance activity = EnvTool.getJournalQueriers().getActivityInstance(activityUUID);
    if (activity == null) {
      throw new ActivityNotFoundException("bai_RAPII_28", activityUUID);
    }
    final CommentImpl comment = new CommentImpl(userId, message, activityUUID, activity.getProcessInstanceUUID());
    addComment(comment, activity.getProcessInstanceUUID());
  }

  private void addComment(final Comment comment, final ProcessInstanceUUID instanceUUID)
      throws InstanceNotFoundException {
    final InternalProcessInstance instance = EnvTool.getJournalQueriers().getProcessInstance(instanceUUID);
    if (instance == null) {
      throw new InstanceNotFoundException("bai_RAPII_27", instanceUUID);
    }
    instance.addComment(comment);
  }

  @Override
  @Deprecated
  public void addComment(final ProcessInstanceUUID instanceUUID, final ActivityInstanceUUID activityUUID,
      final String message, final String userId) throws InstanceNotFoundException, ActivityNotFoundException {
    final InternalProcessInstance instance = EnvTool.getJournalQueriers().getProcessInstance(instanceUUID);
    if (instance == null) {
      throw new InstanceNotFoundException("bai_RAPII_27", instanceUUID);
    }
    if (activityUUID != null) {
      final ActivityInstance activity = EnvTool.getAllQueriers().getActivityInstance(activityUUID);
      if (activity == null) {
        throw new ActivityNotFoundException("bai_RAPII_28", activityUUID);
      }
    }
    CommentImpl comment;
    if (activityUUID != null) {
      comment = new CommentImpl(userId, message, activityUUID, instanceUUID);
    } else {
      comment = new CommentImpl(userId, message, instanceUUID);
    }
    instance.addComment(comment);
  }

  @Override
  public void addProcessMetaData(final ProcessDefinitionUUID uuid, final String key, final String value)
      throws ProcessNotFoundException {
    FacadeUtil.checkArgsNotNull(uuid, key, value);
    final InternalProcessDefinition process = EnvTool.getAllQueriers().getProcess(uuid);
    if (process == null) {
      throw new ProcessNotFoundException("bai_RAPII_29", uuid);
    }
    process.addAMetaData(key, value);
  }

  @Override
  public void deleteProcessMetaData(final ProcessDefinitionUUID uuid, final String key) throws ProcessNotFoundException {
    FacadeUtil.checkArgsNotNull(uuid, key);
    final InternalProcessDefinition process = EnvTool.getAllQueriers().getProcess(uuid);
    if (process == null) {
      throw new ProcessNotFoundException("bai_RAPII_29", uuid);
    }
    process.deleteAMetaData(key);
  }

  @Override
  public Object evaluateGroovyExpression(final String expression, final ProcessInstanceUUID instanceUUID,
      final boolean propagate) throws InstanceNotFoundException, GroovyException {
    return evaluateGroovyExpression(expression, instanceUUID, null, propagate);
  }

  @Override
  public Object evaluateGroovyExpression(final String expression, final ProcessInstanceUUID instanceUUID,
      final Map<String, Object> context, final boolean propagate) throws InstanceNotFoundException, GroovyException {
    return GroovyUtil.evaluate(expression, context, instanceUUID, false, propagate);
  }

  @Override
  public Object evaluateGroovyExpression(final String expression, final ProcessInstanceUUID instanceUUID,
      final Map<String, Object> context, final boolean useInitialVariableValues, final boolean propagate)
      throws InstanceNotFoundException, GroovyException {
    return GroovyUtil.evaluate(expression, context, instanceUUID, useInitialVariableValues, propagate);
  }

  @Override
  public Object evaluateGroovyExpression(final String expression, final ActivityInstanceUUID activityUUID,
      final boolean useActivityScope, final boolean propagate) throws InstanceNotFoundException,
      ActivityNotFoundException, GroovyException {
    return evaluateGroovyExpression(expression, activityUUID, null, useActivityScope, propagate);
  }

  @Override
  public Object evaluateGroovyExpression(final String expression, final ActivityInstanceUUID activityUUID,
      final Map<String, Object> context, final boolean useActivityScope, final boolean propagate)
      throws InstanceNotFoundException, ActivityNotFoundException, GroovyException {
    return GroovyUtil.evaluate(expression, context, activityUUID, useActivityScope, propagate);
  }

  @Override
  public Object evaluateGroovyExpression(final String expression, final ProcessDefinitionUUID processDefinitionUUID)
      throws ProcessNotFoundException, GroovyException {
    return evaluateGroovyExpression(expression, processDefinitionUUID, null);
  }

  @Override
  public Object evaluateGroovyExpression(final String expression, final ProcessDefinitionUUID processDefinitionUUID,
      final Map<String, Object> context) throws ProcessNotFoundException, GroovyException {
    return GroovyUtil.evaluate(expression, context, processDefinitionUUID, false);
  }

  @Override
  public Map<String, Object> evaluateGroovyExpressions(final Map<String, String> expressions,
      final ProcessDefinitionUUID processDefinitionUUID, final Map<String, Object> context)
      throws ProcessNotFoundException, GroovyException {
    Misc.checkArgsNotNull(processDefinitionUUID);
    if (expressions == null || expressions.isEmpty()) {
      return Collections.emptyMap();
    }
    final ClassLoader ori = Thread.currentThread().getContextClassLoader();
    try {
      final ClassLoader processClassLoader = EnvTool.getClassDataLoader().getProcessClassLoader(processDefinitionUUID);
      Thread.currentThread().setContextClassLoader(processClassLoader);
      final Binding simpleBinding = GroovyBindingBuilder.getSimpleBinding(processDefinitionUUID, null, null, context,
          true, true);
      return evaluateGroovyExpressions(expressions, simpleBinding);
    } catch (final Exception e) {
      throw new GroovyException("Exception while getting binding. ProcessDefinitionUUID: " + processDefinitionUUID, e);
    } finally {
      if (ori != null && ori != Thread.currentThread().getContextClassLoader()) {
        Thread.currentThread().setContextClassLoader(ori);
      }
    }
  }

  @Override
  public Map<String, Object> evaluateGroovyExpressions(final Map<String, String> expressions,
      final ActivityInstanceUUID activityUUID, final Map<String, Object> context, final boolean useActivityScope,
      final boolean propagate) throws InstanceNotFoundException, ActivityNotFoundException, GroovyException {
    Misc.checkArgsNotNull(activityUUID);
    if (expressions == null || expressions.isEmpty()) {
      return Collections.emptyMap();
    }
    final ClassLoader ori = Thread.currentThread().getContextClassLoader();
    try {
      final InternalActivityInstance activityInstance = EnvTool.getAllQueriers().getActivityInstance(activityUUID);
      final ProcessDefinitionUUID definitionUUID = activityInstance.getProcessDefinitionUUID();
      final ClassLoader processClassLoader = EnvTool.getClassDataLoader().getProcessClassLoader(definitionUUID);
      Thread.currentThread().setContextClassLoader(processClassLoader);
      final Binding binding = getBinding(definitionUUID, activityInstance.getProcessInstanceUUID(), activityUUID,
          context, useActivityScope, false, propagate);
      final Map<String, Object> results = evaluateGroovyExpressions(expressions, binding);
      propagateVariablesIfNecessary(activityUUID, null, propagate, binding);
      return results;
    } catch (final Exception e) {
      throw new GroovyException("Exception while evaluating expression. ActivityInstanceUUID: " + activityUUID, e);
    } finally {
      if (ori != null && ori != Thread.currentThread().getContextClassLoader()) {
        Thread.currentThread().setContextClassLoader(ori);
      }
    }
  }

  private void propagateVariablesIfNecessary(final ActivityInstanceUUID activityUUID,
      final ProcessInstanceUUID instanceUUID, final boolean propagate, final Binding binding) throws GroovyException {
    if (propagate) {
      try {
        GroovyUtil.propagateVariables(((PropagateBinding) binding).getVariablesToPropagate(), activityUUID,
            instanceUUID);
      } catch (final Exception e) {
        throw new GroovyException("Exception while propagating variables", e);
      }
    }
  }

  private Binding getBinding(final ProcessDefinitionUUID processUUID, final ProcessInstanceUUID instanceUUID,
      final ActivityInstanceUUID activityUUID, final Map<String, Object> context, final boolean useActiveScope,
      final boolean useInitialVariableValues, final boolean propagate) throws GroovyException {
    Binding binding = null;
    try {
      if (propagate) {
        binding = GroovyBindingBuilder.getPropagateBinding(processUUID, instanceUUID, activityUUID, context,
            useActiveScope, useInitialVariableValues);
      } else {
        binding = GroovyBindingBuilder.getSimpleBinding(processUUID, instanceUUID, activityUUID, context,
            useActiveScope, useInitialVariableValues);
      }
    } catch (final Exception e) {
      throw new GroovyException("Exception while getting binding", e);
    }
    return binding;
  }

  private Map<String, Object> evaluateGroovyExpressions(final Map<String, String> expressions, final Binding binding)
      throws GroovyException, NotSerializableException, ActivityDefNotFoundException, DataFieldNotFoundException,
      ProcessNotFoundException, IOException, ClassNotFoundException {
    final Map<String, Object> results = new HashMap<String, Object>();
    for (final Entry<String, String> expr : expressions.entrySet()) {
      final String expressionName = expr.getKey();
      final Object result = GroovyUtil.evaluate(expr.getValue(), binding);
      results.put(expressionName, result);
    }
    return results;
  }

  @Override
  public Map<String, Object> evaluateGroovyExpressions(final Map<String, String> expressions,
      final ProcessInstanceUUID processInstanceUUID, final Map<String, Object> context,
      final boolean useInitialVariableValues, final boolean propagate) throws InstanceNotFoundException,
      GroovyException {
    Misc.checkArgsNotNull(processInstanceUUID);
    if (expressions == null || expressions.isEmpty()) {
      return Collections.emptyMap();
    }

    final ClassLoader ori = Thread.currentThread().getContextClassLoader();
    try {
      final InternalProcessInstance instance = EnvTool.getAllQueriers().getProcessInstance(processInstanceUUID);
      final ProcessDefinitionUUID definitionUUID = instance.getProcessDefinitionUUID();
      final ClassLoader processClassLoader = EnvTool.getClassDataLoader().getProcessClassLoader(definitionUUID);
      Thread.currentThread().setContextClassLoader(processClassLoader);
      final Binding binding = getBinding(definitionUUID, processInstanceUUID, null, context, false,
          useInitialVariableValues, propagate);
      final Map<String, Object> results = evaluateGroovyExpressions(expressions, binding);
      propagateVariablesIfNecessary(null, processInstanceUUID, propagate, binding);
      return results;
    } catch (final Exception e) {
      throw new GroovyException("Exception while evaluating expression. ProcessInstanceUUID: " + processInstanceUUID, e);
    } finally {
      if (ori != null && ori != Thread.currentThread().getContextClassLoader()) {
        Thread.currentThread().setContextClassLoader(ori);
      }
    }
  }

  @Override
  public void addAttachment(final ProcessInstanceUUID instanceUUID, final String name, final String fileName,
      final byte[] value) {
    if (value == null && fileName != null) {
      throw new BonitaRuntimeException("The content of the attachment cannot be null");
    }
    createDocumentOrVersion(instanceUUID, name, fileName, value, DocumentService.DEFAULT_MIME_TYPE);
  }

  @Override
  public void createDocumentOrAddDocumentVersion(final ProcessInstanceUUID instanceUUID, final String name,
      final String fileName, final String mimeType, final byte[] value) {
    if (value == null && fileName != null) {
      throw new BonitaRuntimeException("The content of the attachment cannot be null");
    }
    createDocumentOrVersion(instanceUUID, name, fileName, value, mimeType);
  }

  @Override
  public void addAttachment(final ProcessInstanceUUID instanceUUID, final String name, final String label,
      final String description, final String fileName, final Map<String, String> metadata, final byte[] value) {
    if (value == null && fileName != null) {
      throw new BonitaRuntimeException("The content of the attachment cannot be null");
    }
    String mimeType = metadata.get("content-type");
    if (mimeType == null) {
      mimeType = DocumentService.DEFAULT_MIME_TYPE;
    }

    createDocumentOrVersion(instanceUUID, name, fileName, value, mimeType);
  }

  private void createDocumentOrVersion(final ProcessInstanceUUID instanceUUID, final String name,
      final String fileName, final byte[] value, final String mimeType) {
    final DocumentationManager manager = EnvTool.getDocumentationManager();
    final List<org.ow2.bonita.services.Document> documents = DocumentService.getDocuments(manager, instanceUUID, name);
    try {
      if (documents.size() == 0) {
        createDocument(name, instanceUUID, fileName, mimeType, value);
      } else {
        addDocumentVersion(documents.get(0).getId(), true, fileName, mimeType, value);
      }
    } catch (final BonitaException e) {
      throw new BonitaRuntimeException(e);
    }
  }

  @Override
  public void addAttachments(final Map<AttachmentInstance, byte[]> attachments) {
    if (attachments != null) {
      for (final Entry<AttachmentInstance, byte[]> attachment : attachments.entrySet()) {
        final AttachmentInstance attachmentInstance = attachment.getKey();
        final String name = attachmentInstance.getName();
        final ProcessInstanceUUID instanceUUID = attachmentInstance.getProcessInstanceUUID();
        final String fileName = attachmentInstance.getFileName();
        String mimeType = attachmentInstance.getMetaData().get("content-type");
        if (mimeType == null) {
          mimeType = DocumentService.DEFAULT_MIME_TYPE;
        }
        try {
          createDocument(name, instanceUUID, fileName, mimeType, attachment.getValue());
        } catch (final Exception e) {
          throw new BonitaRuntimeException(e);
        }
      }
    }
  }

  @Override
  public void removeAttachment(final ProcessInstanceUUID instanceUUID, final String name)
      throws InstanceNotFoundException {
    FacadeUtil.checkArgsNotNull(instanceUUID, name);
    final InternalProcessInstance instance = EnvTool.getJournalQueriers().getProcessInstance(instanceUUID);
    if (instance == null) {
      throw new InstanceNotFoundException("bai_RAPII_1", instanceUUID);
    }
    if (instance.getNbOfAttachments() <= 0) {
      throw new BonitaRuntimeException(new DocumentNotFoundException(name));
    }
    final DocumentationManager manager = EnvTool.getDocumentationManager();
    final List<org.ow2.bonita.services.Document> documents = DocumentService.getDocuments(manager, instanceUUID, name);
    if (!documents.isEmpty()) {
      final org.ow2.bonita.services.Document document = documents.get(0);
      try {
        manager.deleteDocument(document.getId(), true);
        // Keep mapping with number of attachments.
        instance.setNbOfAttachments(instance.getNbOfAttachments() - 1);
      } catch (final DocumentNotFoundException e) {
        throw new BonitaRuntimeException(e);
      }
    }
  }

  @Override
  public void setActivityInstancePriority(final ActivityInstanceUUID activityInstanceUUID, final int priority)
      throws ActivityNotFoundException {
    final ActivityInstance activity = EnvTool.getJournalQueriers().getActivityInstance(activityInstanceUUID);
    if (activity == null) {
      throw new ActivityNotFoundException("bai_RAPII_22", activityInstanceUUID);
    }
    final ProcessInstanceUUID instanceUUID = activity.getProcessInstanceUUID();
    final Execution execution = EnvTool.getJournalQueriers().getExecutionOnActivity(instanceUUID, activityInstanceUUID);
    if (execution == null) {
      throw new ActivityNotFoundException("bai_RAPII_23", activityInstanceUUID);
    }
    final Recorder recorder = EnvTool.getRecorder();
    recorder.recordActivityPriorityUpdated(activityInstanceUUID, priority);
  }

  @Override
  public void deleteEvents(final String eventName, final String toProcessName, final String toActivityName,
      final ActivityInstanceUUID actiivtyUUID) {
    final EventService eventService = EnvTool.getEventService();
    final Set<OutgoingEventInstance> events = eventService.getOutgoingEvents(eventName, toProcessName, toActivityName,
        actiivtyUUID);
    if (events != null) {
      for (final OutgoingEventInstance event : events) {
        eventService.removeEvent(event);
      }
    }
  }

  @Override
  public Map<String, Object> executeConnector(final String connectorClassName, final Map<String, Object[]> parameters,
      final ProcessDefinitionUUID definitionUUID) throws Exception {
    return this.executeConnector(connectorClassName, parameters, definitionUUID, null, null, null, null, true);
  }

  @Override
  public Map<String, Object> executeConnector(final String connectorClassName, final Map<String, Object[]> parameters)
      throws Exception {
    return this.executeConnector(connectorClassName, parameters, null, null, null, null, null, true);
  }

  @Override
  public Map<String, Object> executeConnector(final String connectorClassName, final Map<String, Object[]> parameters,
      final ClassLoader classLoader) throws Exception {
    return this.executeConnector(connectorClassName, parameters, null, null, null, classLoader, null, true);
  }

  private Map<String, Object[]> getInputParameters(final ConnectorExecutionDescriptor connectorDescriptor) {
    final Map<String, Object[]> inputParameters = new HashMap<String, Object[]>(connectorDescriptor
        .getInputParameters().size());
    for (final Entry<String, Serializable[]> inputParam : connectorDescriptor.getInputParameters().entrySet()) {
      inputParameters.put(inputParam.getKey(), inputParam.getValue());
    }
    return inputParameters;
  }

  private void handleOutputParameters(final ProcessDefinitionUUID processDefinitionUUID,
      final ProcessInstance processInstance, final ActivityInstance activityInstance, final Map<String, Object> output,
      final ConnectorExecutionDescriptor connectorDescriptor, final Map<String, Object> connectorOutput,
      final boolean useCurrentVariableValues) throws Exception {
    if (connectorDescriptor.getOutputParameters() != null) {
      // initialize variables
      boolean useActivityScope = false;
      boolean useInitValues = false;
      ActivityInstanceUUID activityInstanceUUID = null;
      ProcessInstanceUUID processInstanceUUID = null;
      ProcessInstanceUUID parentProcessInstanceUUID = null;
      ProcessDefinitionUUID parentProcessDefinitionUUID = null;
      if (processInstance != null) {
        useInitValues = !useCurrentVariableValues;
        processInstanceUUID = processInstance.getUUID();
        parentProcessDefinitionUUID = processInstance.getProcessDefinitionUUID();
        parentProcessInstanceUUID = processInstanceUUID;
      } else if (activityInstance != null) {
        useActivityScope = !useCurrentVariableValues;
        activityInstanceUUID = activityInstance.getUUID();
        parentProcessDefinitionUUID = activityInstance.getProcessDefinitionUUID();
        parentProcessInstanceUUID = activityInstance.getProcessInstanceUUID();
      } else if (processDefinitionUUID != null) {
        parentProcessDefinitionUUID = processDefinitionUUID;
      }
      // to get the context only one of parameters processDefinitionUUID,
      // activityInstanceUUID, processInstanceUUID must
      // be set
      final Map<String, Object> context = GroovyBindingBuilder.getContext(connectorOutput, processDefinitionUUID,
          activityInstanceUUID, processInstanceUUID, useActivityScope, useInitValues);

      // to get he binding to avoid useless access to the data base it's better
      // always to pass information about process
      // def uuid and process instance
      // uuid
      final Binding binding = GroovyBindingBuilder.getSimpleBinding(context, parentProcessDefinitionUUID,
          parentProcessInstanceUUID, activityInstanceUUID);
      for (final Entry<String, Serializable[]> outputParameter : connectorDescriptor.getOutputParameters().entrySet()) {
        final String fieldNameExpression = outputParameter.getKey();
        if (outputParameter.getValue().length > 0) {
          final Object fieldValue = outputParameter.getValue()[0];
          if (fieldValue != null) {
            final String fieldExpression = fieldValue.toString();
            Object evaluatedFieldValue = fieldExpression;
            if (fieldExpression != null && fieldExpression.length() != 0
                && Misc.containsAGroovyExpression(fieldExpression)) {
              evaluatedFieldValue = GroovyUtil.evaluate(fieldExpression, binding);
            }
            updateContext(output, parentProcessDefinitionUUID, fieldNameExpression, evaluatedFieldValue);
          }
        }
      }
    }
  }

  private void updateContext(final Map<String, Object> output, final ProcessDefinitionUUID parentProcessDefinitionUUID,
      final String fieldNameExpression, final Object evaluatedFieldValue) throws GroovyException, Exception {
    String fieldName = fieldNameExpression;
    Object newValue = evaluatedFieldValue;

    if (fieldNameExpression.contains(BonitaConstants.XPATH_VAR_SEPARATOR)
        || fieldNameExpression.contains(BonitaConstants.JAVA_VAR_SEPARATOR)) {
      fieldName = Misc.getVariableName(fieldNameExpression);
      final Object currentValue = output.get(fieldName);
      if (currentValue == null) {
        throw new GroovyException("The variable '" + fieldName + "' was not found in the context map or it's null");
      }
      if (fieldNameExpression.contains(BonitaConstants.XPATH_VAR_SEPARATOR)) {
        if (currentValue instanceof Document) {
          newValue = getXMLValueXPath(fieldNameExpression, evaluatedFieldValue, (Document) currentValue);
        } else {
          throw new GroovyException("The variable  '" + fieldName + "' is not a Document");
        }
      } else if (fieldNameExpression.contains(BonitaConstants.JAVA_VAR_SEPARATOR)) {
        newValue = getModifiedJavaObject(parentProcessDefinitionUUID, fieldNameExpression, currentValue,
            evaluatedFieldValue);
      }
    }
    output.put(fieldName, newValue);
  }

  @Override
  public Map<String, Object> executeConnectors(final ProcessDefinitionUUID processDefinitionUUID,
      final List<ConnectorExecutionDescriptor> connectorExecutionDescriptors, final Map<String, Object> context)
      throws Exception {
    return executeConnectors(processDefinitionUUID, null, null, connectorExecutionDescriptors, context, true);
  }

  @Override
  public Map<String, Object> executeConnectors(final ProcessInstanceUUID processInstanceUUID,
      final List<ConnectorExecutionDescriptor> connectorExecutionDescriptors, final Map<String, Object> context,
      final boolean useCurrentVariableValues) throws Exception {
    return executeConnectors(null, processInstanceUUID, null, connectorExecutionDescriptors, context,
        useCurrentVariableValues);
  }

  @Override
  public Map<String, Object> executeConnectors(final ActivityInstanceUUID activityInstanceUUID,
      final List<ConnectorExecutionDescriptor> connectorExecutionDescriptors, final Map<String, Object> context,
      final boolean useCurrentVariableValues) throws Exception {
    return executeConnectors(null, null, activityInstanceUUID, connectorExecutionDescriptors, context,
        useCurrentVariableValues);
  }

  private Map<String, Object> executeConnectors(final ProcessDefinitionUUID processDefinitionUUID,
      final ProcessInstanceUUID processInstanceUUID, final ActivityInstanceUUID activityInstanceUUID,
      final List<ConnectorExecutionDescriptor> connectorExecutionDescriptors, final Map<String, Object> context,
      final boolean useCurrentVariableValues) throws Exception {

    final InternalActivityInstance activity;
    final InternalProcessInstance processInstance;
    final ProcessDefinitionUUID processDefinitionUUIDToUse;
    if (processDefinitionUUID == null) {
      if (activityInstanceUUID != null) {
        activity = EnvTool.getAllQueriers().getActivityInstance(activityInstanceUUID);
        processInstance = null;
        processDefinitionUUIDToUse = activity.getProcessDefinitionUUID();
      } else if (processInstanceUUID != null) {
        activity = null;
        processInstance = EnvTool.getAllQueriers().getProcessInstance(processInstanceUUID);
        processDefinitionUUIDToUse = processInstance.getProcessDefinitionUUID();
      } else {
        activity = null;
        processInstance = null;
        processDefinitionUUIDToUse = null;
      }
    } else {
      activity = null;
      processInstance = null;
      processDefinitionUUIDToUse = processDefinitionUUID;
    }
    return new ExecuteInClassLoader() {

      @Override
      Map<String, Object> executeInClassLoader(final ClassLoader classLoaderToUse, final ClassDataLoader classDataLoader)
          throws Exception {
        final Map<String, Object> output = new HashMap<String, Object>(context.size());
        output.putAll(context);
        for (final ConnectorExecutionDescriptor connectorDescriptor : connectorExecutionDescriptors) {
          try {
            final Map<String, Object[]> inputParameters = getInputParameters(connectorDescriptor);
            final Map<String, Object> connectorOutput = executeConnectorWithClassLoaderSet(
                connectorDescriptor.getClassName(), inputParameters, processDefinitionUUIDToUse, processInstanceUUID,
                activityInstanceUUID, classLoaderToUse, output, useCurrentVariableValues, classDataLoader);
            handleOutputParameters(processDefinitionUUIDToUse, processInstance, activity, output, connectorDescriptor,
                connectorOutput, useCurrentVariableValues);
          } catch (final Exception t) {
            if (!connectorDescriptor.isThrowingException() && LOG.isLoggable(Level.SEVERE)) {
              LOG.log(Level.SEVERE,
                  "Error while executing one of connectors. The remaining connectors will be executed", t);
            } else {
              throw t;
            }
          }
        }
        return output;
      }
    }.execute(null, processDefinitionUUIDToUse);
  }

  private Map<String, Object> executeConnector(final String connectorClassName, final Map<String, Object[]> parameters,
      final ProcessDefinitionUUID definitionUUID, final ProcessInstanceUUID instanceUUID,
      final ActivityInstanceUUID activityInstanceUUID, final ClassLoader classLoader,
      final Map<String, Object> context, final boolean useCurrentVariableValues) throws Exception {
    return new ExecuteInClassLoader() {

      @Override
      Map<String, Object> executeInClassLoader(final ClassLoader classLoaderToUse, final ClassDataLoader classDataLoader)
          throws Exception {
        return executeConnectorWithClassLoaderSet(connectorClassName, parameters, definitionUUID, instanceUUID,
            activityInstanceUUID, classLoaderToUse, context, useCurrentVariableValues, classDataLoader);
      }
    }.execute(classLoader, definitionUUID);

  }

  private Map<String, Object> executeConnectorWithClassLoaderSet(final String connectorClassName,
      final Map<String, Object[]> parameters, final ProcessDefinitionUUID definitionUUID,
      final ProcessInstanceUUID instanceUUID, final ActivityInstanceUUID activityInstanceUUID,
      final ClassLoader classLoader, final Map<String, Object> context, final boolean useCurrentVariableValues,
      final ClassDataLoader classDataLoader) throws ClassNotFoundException, InstantiationException,
      IllegalAccessException, Exception {
    Connector connector = null;
    if (classDataLoader != null) {
      connector = (Connector) classDataLoader.getInstance(null, connectorClassName);
    } else {
      final Class<?> objectClass = Class.forName(connectorClassName, true, classLoader);
      connector = (Connector) objectClass.newInstance();
    }
    if (connector instanceof Mapper) {
      throw new IllegalAccessException(connectorClassName + " is a instance of RoleResolver or Filter");
    }
    return ConnectorExecutor.executeConnector(connector, definitionUUID, instanceUUID, activityInstanceUUID,
        parameters, context, useCurrentVariableValues);
  }

  @Override
  public Map<String, Object> executeConnector(final String connectorClassName, final Map<String, Object[]> parameters,
      final ProcessDefinitionUUID definitionUUID, final Map<String, Object> context) throws Exception {
    return this.executeConnector(connectorClassName, parameters, definitionUUID, null, null, null, context, true);
  }

  @Override
  public Map<String, Object> executeConnector(final String connectorClassName, final Map<String, Object[]> parameters,
      final ProcessInstanceUUID processInstanceUUID, final Map<String, Object> context,
      final boolean useCurrentVariableValues) throws Exception {
    final ProcessInstance instance = EnvTool.getJournalQueriers().getProcessInstance(processInstanceUUID);
    return this.executeConnector(connectorClassName, parameters, instance.getProcessDefinitionUUID(),
        processInstanceUUID, null, null, context, useCurrentVariableValues);
  }

  @Override
  public Map<String, Object> executeConnector(final String connectorClassName, final Map<String, Object[]> parameters,
      final ActivityInstanceUUID activityInstanceUUID, final Map<String, Object> context,
      final boolean useCurrentVariableValues) throws Exception {
    final ActivityInstance activity = EnvTool.getJournalQueriers().getActivityInstance(activityInstanceUUID);
    return this.executeConnector(connectorClassName, parameters, activity.getProcessDefinitionUUID(), null,
        activityInstanceUUID, null, context, useCurrentVariableValues);
  }

  @Override
  public Set<String> executeFilter(final String connectorClassName, final Map<String, Object[]> parameters,
      final Set<String> members) throws Exception {
    return this.executeFilter(connectorClassName, parameters, members, null, null);
  }

  @Override
  public Set<String> executeFilter(final String connectorClassName, final Map<String, Object[]> parameters,
      final Set<String> members, final ProcessDefinitionUUID definitionUUID) throws Exception {
    return this.executeFilter(connectorClassName, parameters, members, definitionUUID, null);
  }

  @Override
  public Set<String> executeFilter(final String connectorClassName, final Map<String, Object[]> parameters,
      final Set<String> members, final ClassLoader classLoader) throws Exception {
    return this.executeFilter(connectorClassName, parameters, members, null, classLoader);
  }

  private Set<String> executeFilter(final String connectorClassName, final Map<String, Object[]> parameters,
      final Set<String> members, final ProcessDefinitionUUID definitionUUID, final ClassLoader classLoader)
      throws Exception {
    FacadeUtil.checkArgsNotNull(members);
    final ClassLoader baseClassLoader = Thread.currentThread().getContextClassLoader();
    try {
      Filter connector = null;
      if (classLoader == null) {
        connector = (Filter) EnvTool.getClassDataLoader().getInstance(definitionUUID, connectorClassName);
      } else {
        Thread.currentThread().setContextClassLoader(classLoader);
        final Class<?> objectClass = Class.forName(connectorClassName, true, classLoader);
        connector = (Filter) objectClass.newInstance();
      }
      return ConnectorExecutor.executeFilter(connector, parameters, members);
    } finally {
      Thread.currentThread().setContextClassLoader(baseClassLoader);
    }
  }

  @Override
  public Set<String> executeRoleResolver(final String connectorClassName, final Map<String, Object[]> parameters,
      final ProcessDefinitionUUID definitionUUID) throws Exception {
    return this.executeRoleResolver(connectorClassName, parameters, definitionUUID, null);
  }

  @Override
  public Set<String> executeRoleResolver(final String connectorClassName, final Map<String, Object[]> parameters)
      throws Exception {
    return this.executeRoleResolver(connectorClassName, parameters, null, null);
  }

  @Override
  public Set<String> executeRoleResolver(final String connectorClassName, final Map<String, Object[]> parameters,
      final ClassLoader classLoader) throws Exception {
    return this.executeRoleResolver(connectorClassName, parameters, null, classLoader);
  }

  private Set<String> executeRoleResolver(final String connectorClassName, final Map<String, Object[]> parameters,
      final ProcessDefinitionUUID definitionUUID, final ClassLoader classLoader) throws Exception {
    final ClassLoader baseClassLoader = Thread.currentThread().getContextClassLoader();
    try {
      RoleResolver connector = null;
      if (classLoader == null) {
        connector = (RoleResolver) EnvTool.getClassDataLoader().getInstance(definitionUUID, connectorClassName);
      } else {
        Thread.currentThread().setContextClassLoader(classLoader);
        final Class<?> objectClass = Class.forName(connectorClassName, true, classLoader);
        connector = (RoleResolver) objectClass.newInstance();
      }
      return ConnectorExecutor.executeRoleResolver(connector, parameters);
    } finally {
      Thread.currentThread().setContextClassLoader(baseClassLoader);
    }
  }

  @Override
  public void skipTask(final ActivityInstanceUUID taskUUID, final Map<String, Object> variablesToUpdate)
      throws TaskNotFoundException, IllegalTaskStateException {
    FacadeUtil.checkArgsNotNull(taskUUID);
    TaskManager.skip(taskUUID, variablesToUpdate);
  }

  @Override
  public void skip(final ActivityInstanceUUID activityInstanceUUID, final Map<String, Object> variablesToUpdate)
      throws ActivityNotFoundException, IllegalTaskStateException {
    FacadeUtil.checkArgsNotNull(activityInstanceUUID);
    ActivityManager.skip(activityInstanceUUID, variablesToUpdate);
  }

  @Override
  public void executeEvent(final CatchingEventUUID eventUUID) throws EventNotFoundException {
    final EventService eventService = EnvTool.getEventService();
    final long id = Long.parseLong(eventUUID.getValue());
    final Job job = eventService.getJob(id);
    if (EventConstants.TIMER.equals(job.getEventType())) {
      updateExpirationDate(job, new Date());
    }
  }

  @Override
  public void deleteEvent(final CatchingEventUUID eventUUID) throws EventNotFoundException {
    final EventService eventService = EnvTool.getEventService();
    final long id = Long.parseLong(eventUUID.getValue());
    final Job job = eventService.getJob(id);
    if (job == null) {
      throw new EventNotFoundException("Event " + id + "does not exist.");
    }
    eventService.removeJob(job);
    EnvTool.getEventExecutor().refreshEventMatcher();
  }

  @Override
  public void deleteEvents(final Collection<CatchingEventUUID> eventUUIDs) throws EventNotFoundException {
    if (eventUUIDs != null) {
      for (final CatchingEventUUID eventUUID : eventUUIDs) {
        deleteEvent(eventUUID);
      }
    }
  }

  @Override
  public void updateExpirationDate(final CatchingEventUUID eventUUID, final Date expiration)
      throws EventNotFoundException {
    final EventService eventService = EnvTool.getEventService();
    final long id = Long.parseLong(eventUUID.getValue());
    final Job job = eventService.getJob(id);
    if (job == null) {
      throw new EventNotFoundException("Event " + id + "does not exist.");
    }
    updateExpirationDate(job, expiration);
  }

  private void updateExpirationDate(final Job job, final Date expiration) throws EventNotFoundException {
    job.setFireTime(expiration.getTime());
    EnvTool.getEventExecutor().refreshJobExecutor();
  }

  @Override
  public void updateActivityExpectedEndDate(final ActivityInstanceUUID activityUUID, final Date expectedEndDate)
      throws ActivityNotFoundException {
    final InternalActivityInstance activity = EnvTool.getAllQueriers(getQueryList()).getActivityInstance(activityUUID);
    if (activity == null) {
      throw new ActivityNotFoundException("bai_QRAPII_11", activityUUID);
    }
    activity.setExpectedEndDate(expectedEndDate);
  }

  @Override
  public org.ow2.bonita.facade.runtime.Document createDocument(final String name,
      final ProcessInstanceUUID instanceUUID, final String fileName, final String mimeType, final byte[] content)
      throws DocumentationCreationException, InstanceNotFoundException {
    final InternalProcessInstance instance = EnvTool.getJournalQueriers().getProcessInstance(instanceUUID);
    if (instance == null) {
      throw new InstanceNotFoundException("bai_RAPII_1", instanceUUID);
    } else if (content != null && (fileName == null || mimeType == null)) {
      new DocumentationCreationException("");
    }
    final DocumentationManager manager = EnvTool.getDocumentationManager();
    final ProcessDefinitionUUID definitionUUID = instance.getProcessDefinitionUUID();
    org.ow2.bonita.services.Document d = null;
    if (content != null) {
      d = manager.createDocument(name, definitionUUID, instanceUUID, fileName, mimeType, content);
    } else {
      d = manager.createDocument(name, definitionUUID, instanceUUID);
    }
    // Keep mapping of number of attachments
    final int previousNbOfAttachments = instance.getNbOfAttachments();
    if (previousNbOfAttachments <= 0) {
      instance.setNbOfAttachments(1);
    } else {
      instance.setNbOfAttachments(previousNbOfAttachments + 1);
    }

    // update lastUpdateDate date
    instance.updateLastUpdateDate();

    return DocumentService.getClientDocument(manager, d);
  }

  @Override
  public org.ow2.bonita.facade.runtime.Document createDocument(final String name,
      final ProcessDefinitionUUID processDefinitionUUID, final String fileName, final String mimeType,
      final byte[] content) throws DocumentationCreationException, ProcessNotFoundException {
    final ProcessDefinition process = EnvTool.getAllQueriers().getProcess(processDefinitionUUID);
    if (process == null) {
      throw new ProcessNotFoundException("bai_RAPII_29", processDefinitionUUID);
    } else if (content != null && (fileName == null || mimeType == null)) {
      new DocumentationCreationException("");
    }
    final DocumentationManager manager = EnvTool.getDocumentationManager();
    org.ow2.bonita.services.Document d = null;
    if (content != null) {
      d = manager.createDocument(name, processDefinitionUUID, fileName, mimeType, content);
    } else {
      d = manager.createDocument(name, processDefinitionUUID);
    }

    return DocumentService.getClientDocument(manager, d);
  }

  @Override
  public org.ow2.bonita.facade.runtime.Document addDocumentVersion(final DocumentUUID documentUUID,
      final boolean isMajorVersion, final String fileName, final String mimeType, final byte[] content)
      throws DocumentationCreationException {
    return addDocumentVersion(documentUUID.getValue(), isMajorVersion, fileName, mimeType, content);
  }

  private org.ow2.bonita.facade.runtime.Document addDocumentVersion(final String documentId,
      final boolean isMajorVersion, final String fileName, final String mimeType, final byte[] content)
      throws DocumentationCreationException {
    final DocumentationManager manager = EnvTool.getDocumentationManager();
    org.ow2.bonita.services.Document d = null;
    if (content != null) {
      d = manager.createVersion(documentId, isMajorVersion, fileName, mimeType, content);
    } else {
      d = manager.createVersion(documentId, isMajorVersion);
    }

    final ProcessInstanceUUID instanceUUID = d.getProcessInstanceUUID();
    if (instanceUUID != null) {
      InternalProcessInstance instance = EnvTool.getJournalQueriers().getProcessInstance(instanceUUID);
      if (instance == null) {
        instance = EnvTool.getHistoryQueriers().getProcessInstance(instanceUUID);
      }
      if (instance != null) {
        instance.updateLastUpdateDate();
      }
    }
    return DocumentService.getClientDocument(manager, d);
  }

  @Override
  public void deleteDocuments(final boolean allVersions, final DocumentUUID... documentUUIDs)
      throws DocumentNotFoundException {
    final DocumentationManager manager = EnvTool.getDocumentationManager();
    final Querier queriers = EnvTool.getAllQueriers();
    if (documentUUIDs != null) {
      org.ow2.bonita.services.Document doc;
      ProcessInstanceUUID processInstanceUUID;
      for (final DocumentUUID documentUUID : documentUUIDs) {
        doc = manager.getDocument(documentUUID.getValue());
        processInstanceUUID = doc.getProcessInstanceUUID();
        manager.deleteDocument(documentUUID.getValue(), allVersions);
        if (processInstanceUUID != null) {
          final InternalProcessInstance instance = queriers.getProcessInstance(processInstanceUUID);
          if (instance != null) {
            final int nbOfAttachments = instance.getNbOfAttachments() - 1;
            instance.setNbOfAttachments(nbOfAttachments);
          } else {
            LOG.info("When deleting documents, cannot update the process instance because of its deletion");
          }
        }
      }
    }
  }
}
