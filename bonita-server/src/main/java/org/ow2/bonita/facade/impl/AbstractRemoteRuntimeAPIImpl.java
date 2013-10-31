/**
 * Copyright (C) 2010-2012 BonitaSoft S.A.
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

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ow2.bonita.facade.APIAccessor;
import org.ow2.bonita.facade.RuntimeAPI;
import org.ow2.bonita.facade.exception.ActivityNotFoundException;
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
import org.ow2.bonita.facade.internal.AbstractRemoteRuntimeAPI;
import org.ow2.bonita.facade.runtime.AttachmentInstance;
import org.ow2.bonita.facade.runtime.ConnectorExecutionDescriptor;
import org.ow2.bonita.facade.runtime.Document;
import org.ow2.bonita.facade.uuid.ActivityDefinitionUUID;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.CatchingEventUUID;
import org.ow2.bonita.facade.uuid.DocumentUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.identity.auth.DomainOwner;
import org.ow2.bonita.identity.auth.UserOwner;
import org.ow2.bonita.util.GroovyException;

/**
 * 
 * @author Elias Ricken de Medeiros, Matthieu Chaffotte
 * 
 */
public abstract class AbstractRemoteRuntimeAPIImpl implements AbstractRemoteRuntimeAPI {

  protected Map<String, RuntimeAPI> apis = new HashMap<String, RuntimeAPI>();

  protected RuntimeAPI getAPI(final Map<String, String> options) {
    if (options == null) {
      throw new IllegalArgumentException("The options are null or not well set.");
    }
    final String queryList = options.get(APIAccessor.QUERYLIST_OPTION);
    final String user = options.get(APIAccessor.USER_OPTION);
    final String domain = options.get(APIAccessor.DOMAIN_OPTION);
    UserOwner.setUser(user);
    DomainOwner.setDomain(domain);

    if (!apis.containsKey(queryList)) {
      putAPI(queryList);
    }
    return apis.get(queryList);
  }

  protected void putAPI(final String queryList) {
    apis.put(queryList, new StandardAPIAccessorImpl().getRuntimeAPI(queryList));
  }

  @Override
  public void enableEventsInFailure(final ProcessInstanceUUID instanceUUID, final String activityName,
      final Map<String, String> options) {
    getAPI(options).enableEventsInFailure(instanceUUID, activityName);
  }

  @Override
  public void enableEventsInFailure(final ActivityInstanceUUID activityUUID, final Map<String, String> options) {
    getAPI(options).enableEventsInFailure(activityUUID);
  }

  @Override
  public void enablePermanentEventInFailure(final ActivityDefinitionUUID activityUUID, final Map<String, String> options)
      throws RemoteException {
    getAPI(options).enablePermanentEventInFailure(activityUUID);
  }

  @Override
  public void startTask(final ActivityInstanceUUID taskUUID, final boolean assignTask, final Map<String, String> options)
      throws TaskNotFoundException, IllegalTaskStateException {
    getAPI(options).startTask(taskUUID, assignTask);
  }

  @Override
  public void finishTask(final ActivityInstanceUUID taskUUID, final boolean taskAssign,
      final Map<String, String> options) throws TaskNotFoundException, IllegalTaskStateException {
    getAPI(options).finishTask(taskUUID, taskAssign);
  }

  @Override
  public void resumeTask(final ActivityInstanceUUID taskUUID, final boolean taskAssign,
      final Map<String, String> options) throws TaskNotFoundException, IllegalTaskStateException {
    getAPI(options).resumeTask(taskUUID, taskAssign);
  }

  @Override
  public void startActivity(final ActivityInstanceUUID activityUUID, final Map<String, String> options)
      throws ActivityNotFoundException, RemoteException {
    getAPI(options).startActivity(activityUUID);
  }

  @Override
  public void suspendTask(final ActivityInstanceUUID taskUUID, final boolean assignTask,
      final Map<String, String> options) throws TaskNotFoundException, IllegalTaskStateException {
    getAPI(options).suspendTask(taskUUID, assignTask);
  }

  @Override
  public void assignTask(final ActivityInstanceUUID taskUUID, final Map<String, String> options)
      throws TaskNotFoundException {
    getAPI(options).assignTask(taskUUID);
  }

  @Override
  public void assignTask(final ActivityInstanceUUID taskUUID, final String actorId, final Map<String, String> options)
      throws TaskNotFoundException {
    getAPI(options).assignTask(taskUUID, actorId);
  }

  @Override
  public void assignTask(final ActivityInstanceUUID taskUUID, final Set<String> candidates,
      final Map<String, String> options) throws TaskNotFoundException {
    getAPI(options).assignTask(taskUUID, candidates);
  }

  @Override
  public void unassignTask(final ActivityInstanceUUID taskUUID, final Map<String, String> options)
      throws TaskNotFoundException {
    getAPI(options).unassignTask(taskUUID);
  }

  @Override
  public void deleteAllProcessInstances(final ProcessDefinitionUUID processUUID, final Map<String, String> options)
      throws ProcessNotFoundException, UndeletableInstanceException {
    getAPI(options).deleteAllProcessInstances(processUUID);
  }

  @Override
  public void executeTask(final ActivityInstanceUUID taskUUID, final boolean assignTask,
      final Map<String, String> options) throws TaskNotFoundException, IllegalTaskStateException, RemoteException {
    getAPI(options).executeTask(taskUUID, assignTask);
  }

  @Override
  public void deleteProcessInstance(final ProcessInstanceUUID instanceUUID, final Map<String, String> options)
      throws InstanceNotFoundException, UndeletableInstanceException {
    getAPI(options).deleteProcessInstance(instanceUUID);
  }

  @Override
  public void cancelProcessInstance(final ProcessInstanceUUID instanceUUID, final Map<String, String> options)
      throws InstanceNotFoundException, UncancellableInstanceException {
    getAPI(options).cancelProcessInstance(instanceUUID);
  }

  @Override
  public ProcessInstanceUUID instantiateProcess(final ProcessDefinitionUUID processUUID,
      final Map<String, String> options) throws ProcessNotFoundException {
    return getAPI(options).instantiateProcess(processUUID);
  }

  @Override
  public ProcessInstanceUUID instantiateProcess(final ProcessDefinitionUUID processUUID,
      final ActivityDefinitionUUID activityUUID, final Map<String, String> options) throws ProcessNotFoundException {
    return getAPI(options).instantiateProcess(processUUID, activityUUID);
  }

  @Override
  public ProcessInstanceUUID instantiateProcess(final ProcessDefinitionUUID processUUID,
      final Map<String, Object> variables, final Map<String, String> options) throws ProcessNotFoundException,
      VariableNotFoundException {
    return getAPI(options).instantiateProcess(processUUID, variables);
  }

  @Override
  public void setProcessInstanceVariable(final ProcessInstanceUUID instanceUUID, final String variableId,
      final Object variableValue, final Map<String, String> options) throws InstanceNotFoundException,
      VariableNotFoundException {
    getAPI(options).setProcessInstanceVariable(instanceUUID, variableId, variableValue);
  }

  @Override
  public void setProcessInstanceVariables(final ProcessInstanceUUID instanceUUID, final Map<String, Object> variables,
      final Map<String, String> options) throws InstanceNotFoundException, VariableNotFoundException {
    getAPI(options).setProcessInstanceVariables(instanceUUID, variables);
  }

  @Override
  public void setActivityInstanceVariable(final ActivityInstanceUUID activityUUID, final String variableId,
      final Object variableValue, final Map<String, String> options) throws ActivityNotFoundException,
      VariableNotFoundException {
    getAPI(options).setActivityInstanceVariable(activityUUID, variableId, variableValue);
  }

  @Override
  public void setActivityInstanceVariables(final ActivityInstanceUUID activityUUID,
      final Map<String, Object> variables, final Map<String, String> options) throws ActivityNotFoundException,
      VariableNotFoundException {
    getAPI(options).setActivityInstanceVariables(activityUUID, variables);
  }

  @Override
  public void setVariable(final ActivityInstanceUUID activityUUID, final String variableId, final Object variableValue,
      final Map<String, String> options) throws ActivityNotFoundException, VariableNotFoundException {
    getAPI(options).setVariable(activityUUID, variableId, variableValue);
  }

  @Override
  @Deprecated
  public void addComment(final ProcessInstanceUUID instanceUUID, final ActivityInstanceUUID activityUUID,
      final String message, final String userId, final Map<String, String> options) throws InstanceNotFoundException,
      ActivityNotFoundException {
    getAPI(options).addComment(instanceUUID, activityUUID, message, userId);
  }

  @Override
  public void addComment(final ProcessInstanceUUID instanceUUID, final String message, final String userId,
      final Map<String, String> options) throws InstanceNotFoundException, RemoteException {
    getAPI(options).addComment(instanceUUID, message, userId);
  }

  @Override
  public void addComment(final ActivityInstanceUUID activityUUID, final String message, final String userId,
      final Map<String, String> options) throws ActivityNotFoundException, InstanceNotFoundException, RemoteException {
    getAPI(options).addComment(activityUUID, message, userId);
  }

  @Override
  public void addProcessMetaData(final ProcessDefinitionUUID uuid, final String key, final String value,
      final Map<String, String> options) throws ProcessNotFoundException {
    getAPI(options).addProcessMetaData(uuid, key, value);
  }

  @Override
  public void deleteProcessMetaData(final ProcessDefinitionUUID uuid, final String key,
      final Map<String, String> options) throws ProcessNotFoundException {
    getAPI(options).deleteProcessMetaData(uuid, key);
  }

  @Override
  public Object evaluateGroovyExpression(final String expression, final ProcessInstanceUUID instanceUUID,
      final boolean propagate, final Map<String, String> options) throws InstanceNotFoundException, GroovyException {
    return getAPI(options).evaluateGroovyExpression(expression, instanceUUID, propagate);
  }

  @Override
  public Object evaluateGroovyExpression(final String expression, final ProcessInstanceUUID processInstanceUUID,
      final Map<String, Object> context, final boolean propagate, final Map<String, String> options)
      throws InstanceNotFoundException, GroovyException, RemoteException {
    return getAPI(options).evaluateGroovyExpression(expression, processInstanceUUID, context, propagate);
  }

  @Override
  public Object evaluateGroovyExpression(final String expression, final ActivityInstanceUUID activityUUID,
      final boolean useActivityScope, final boolean propagate, final Map<String, String> options)
      throws InstanceNotFoundException, ActivityNotFoundException, GroovyException {
    return getAPI(options).evaluateGroovyExpression(expression, activityUUID, useActivityScope, propagate);
  }

  @Override
  public Object evaluateGroovyExpression(final String expression, final ActivityInstanceUUID activityInstanceUUID,
      final Map<String, Object> context, final boolean useActivityScope, final boolean propagate,
      final Map<String, String> options) throws InstanceNotFoundException, ActivityNotFoundException, GroovyException,
      RemoteException {
    return getAPI(options).evaluateGroovyExpression(expression, activityInstanceUUID, context, useActivityScope,
        propagate);
  }

  @Override
  public Object evaluateGroovyExpression(final String expression, final ProcessDefinitionUUID processDefinitionUUID,
      final Map<String, String> options) throws ProcessNotFoundException, GroovyException, RemoteException {
    return getAPI(options).evaluateGroovyExpression(expression, processDefinitionUUID);
  }

  @Override
  public Object evaluateGroovyExpression(final String expression, final ProcessDefinitionUUID processDefinitionUUID,
      final Map<String, Object> context, final Map<String, String> options) throws ProcessNotFoundException,
      GroovyException, RemoteException {
    return getAPI(options).evaluateGroovyExpression(expression, processDefinitionUUID, context);
  }

  @Override
  public Map<String, Object> evaluateGroovyExpressions(final Map<String, String> expressions,
      final ActivityInstanceUUID activityUUID, final Map<String, Object> context, final boolean useActivityScope,
      final boolean propagate, final Map<String, String> options) throws InstanceNotFoundException,
      ActivityNotFoundException, GroovyException {
    return getAPI(options).evaluateGroovyExpressions(expressions, activityUUID, context, useActivityScope, propagate);
  }

  @Override
  public Map<String, Object> evaluateGroovyExpressions(final Map<String, String> expressions,
      final ProcessDefinitionUUID processDefinitionUUID, final Map<String, Object> context,
      final Map<String, String> options) throws InstanceNotFoundException, GroovyException, ProcessNotFoundException {
    return getAPI(options).evaluateGroovyExpressions(expressions, processDefinitionUUID, context);
  }

  @Override
  public Map<String, Object> evaluateGroovyExpressions(final Map<String, String> expression,
      final ProcessInstanceUUID processInstanceUUID, final Map<String, Object> context,
      final boolean useInitialVariableValues, final boolean propagate, final Map<String, String> options)
      throws InstanceNotFoundException, GroovyException, RemoteException {
    return getAPI(options).evaluateGroovyExpressions(expression, processInstanceUUID, context,
        useInitialVariableValues, propagate);
  }

  @Override
  public void addAttachment(final ProcessInstanceUUID instanceUUID, final String name, final String fileName,
      final byte[] value, final Map<String, String> options) throws RemoteException {
    getAPI(options).addAttachment(instanceUUID, name, fileName, value);
  }

  @Override
  public void addAttachment(final ProcessInstanceUUID instanceUUID, final String name, final String label,
      final String description, final String fileName, final Map<String, String> metadata, final byte[] value,
      final Map<String, String> options) throws RemoteException {
    getAPI(options).addAttachment(instanceUUID, name, label, description, fileName, metadata, value);
  }

  @Override
  public void addAttachments(final Map<AttachmentInstance, byte[]> attachments, final Map<String, String> options)
      throws RemoteException {
    getAPI(options).addAttachments(attachments);
  }

  @Override
  public void removeAttachment(final ProcessInstanceUUID instanceUUID, final String name,
      final Map<String, String> options) throws RemoteException, InstanceNotFoundException {
    getAPI(options).removeAttachment(instanceUUID, name);
  }

  @Override
  public void deleteEvents(final String eventName, final String toProcessName, final String toActivityName,
      final ActivityInstanceUUID actiivtyUUID, final Map<String, String> options) throws RemoteException {
    getAPI(options).deleteEvents(eventName, toProcessName, toActivityName, actiivtyUUID);
  }

  @Override
  public Map<String, Object> executeConnector(final String connectorClassName, final Map<String, Object[]> parameters,
      final ProcessDefinitionUUID definitionUUID, final Map<String, String> options) throws RemoteException, Exception {
    return getAPI(options).executeConnector(connectorClassName, parameters, definitionUUID);
  }

  @Override
  public Map<String, Object> executeConnector(final String connectorClassName, final Map<String, Object[]> parameters,
      final Map<String, String> options) throws RemoteException, Exception {
    return getAPI(options).executeConnector(connectorClassName, parameters);
  }

  @Override
  public Map<String, Object> executeConnectors(final ProcessDefinitionUUID processDefinitionUUID,
      final List<ConnectorExecutionDescriptor> connectorExecutionDescriptors, final Map<String, Object> context,
      final Map<String, String> options) throws RemoteException, Exception {
    return getAPI(options).executeConnectors(processDefinitionUUID, connectorExecutionDescriptors, context);
  }

  @Override
  public Map<String, Object> executeConnectors(final ProcessInstanceUUID processInstanceUUID,
      final List<ConnectorExecutionDescriptor> connectorExecutionDescriptors, final Map<String, Object> context,
      final boolean useCurrentVariableValues, final Map<String, String> options) throws RemoteException, Exception {
    return getAPI(options).executeConnectors(processInstanceUUID, connectorExecutionDescriptors, context,
        useCurrentVariableValues);
  }

  @Override
  public Map<String, Object> executeConnectors(final ActivityInstanceUUID activityInstanceUUID,
      final List<ConnectorExecutionDescriptor> connectorExecutionDescriptors, final Map<String, Object> context,
      final boolean useCurrentVariableValues, final Map<String, String> options) throws RemoteException, Exception {
    return getAPI(options).executeConnectors(activityInstanceUUID, connectorExecutionDescriptors, context,
        useCurrentVariableValues);
  }

  @Override
  public Set<String> executeFilter(final String connectorClassName, final Map<String, Object[]> parameters,
      final Set<String> members, final ProcessDefinitionUUID definitionUUID, final Map<String, String> options)
      throws RemoteException, Exception {
    return getAPI(options).executeFilter(connectorClassName, parameters, members, definitionUUID);
  }

  @Override
  public Set<String> executeFilter(final String connectorClassName, final Map<String, Object[]> parameters,
      final Set<String> members, final Map<String, String> options) throws RemoteException, Exception {
    return getAPI(options).executeFilter(connectorClassName, parameters, members);
  }

  @Override
  public Set<String> executeRoleResolver(final String connectorClassName, final Map<String, Object[]> parameters,
      final ProcessDefinitionUUID definitionUUID, final Map<String, String> options) throws RemoteException, Exception {
    return getAPI(options).executeRoleResolver(connectorClassName, parameters, definitionUUID);
  }

  @Override
  public Set<String> executeRoleResolver(final String connectorClassName, final Map<String, Object[]> parameters,
      final Map<String, String> options) throws RemoteException, Exception {
    return getAPI(options).executeRoleResolver(connectorClassName, parameters);
  }

  @Override
  public Map<String, Object> executeConnector(final String connectorClassName, final Map<String, Object[]> parameters,
      final ClassLoader classLoader, final Map<String, String> options) throws RemoteException, Exception {
    return getAPI(options).executeConnector(connectorClassName, parameters, classLoader);
  }

  @Override
  public Set<String> executeFilter(final String connectorClassName, final Map<String, Object[]> parameters,
      final Set<String> members, final ClassLoader classLoader, final Map<String, String> options)
      throws RemoteException, Exception {
    return getAPI(options).executeFilter(connectorClassName, parameters, members, classLoader);
  }

  @Override
  public Set<String> executeRoleResolver(final String connectorClassName, final Map<String, Object[]> parameters,
      final ClassLoader classLoader, final Map<String, String> options) throws RemoteException, Exception {
    return getAPI(options).executeRoleResolver(connectorClassName, parameters, classLoader);
  }

  @Override
  public void setActivityInstancePriority(final ActivityInstanceUUID activityInstanceUUID, final int priority,
      final Map<String, String> options) throws ActivityNotFoundException {
    getAPI(options).setActivityInstancePriority(activityInstanceUUID, priority);
  }

  @Override
  public Map<String, Object> executeConnector(final String connectorClassName, final Map<String, Object[]> parameters,
      final ProcessDefinitionUUID definitionUUID, final Map<String, Object> context, final Map<String, String> options)
      throws RemoteException, Exception {
    return getAPI(options).executeConnector(connectorClassName, parameters, definitionUUID, context);
  }

  @Override
  public Map<String, Object> executeConnector(final String connectorClassName, final Map<String, Object[]> parameters,
      final ProcessInstanceUUID processInstanceUUID, final Map<String, Object> context,
      final boolean useCurrentVariableValues, final Map<String, String> options) throws RemoteException, Exception {
    return getAPI(options).executeConnector(connectorClassName, parameters, processInstanceUUID, context,
        useCurrentVariableValues);
  }

  @Override
  public Map<String, Object> executeConnector(final String connectorClassName, final Map<String, Object[]> parameters,
      final ActivityInstanceUUID activityInstanceUUID, final Map<String, Object> context,
      final boolean useCurrentVariableValues, final Map<String, String> options) throws RemoteException, Exception {
    return getAPI(options).executeConnector(connectorClassName, parameters, activityInstanceUUID, context,
        useCurrentVariableValues);
  }

  @Override
  public Object evaluateGroovyExpression(final String expression, final ProcessInstanceUUID processInstanceUUID,
      final Map<String, Object> context, final boolean useInitialVariableValues, final boolean propagate,
      final Map<String, String> options) throws InstanceNotFoundException, GroovyException, RemoteException {
    return getAPI(options).evaluateGroovyExpression(expression, processInstanceUUID, context, useInitialVariableValues,
        propagate);
  }

  @Override
  public void skipTask(final ActivityInstanceUUID taskUUID, final Map<String, Object> variablesToUpdate,
      final Map<String, String> options) throws TaskNotFoundException, IllegalTaskStateException, RemoteException {
    getAPI(options).skipTask(taskUUID, variablesToUpdate);
  }

  @Override
  public void skip(final ActivityInstanceUUID activityInstanceUUID, final Map<String, Object> variablesToUpdate,
      final Map<String, String> options) throws ActivityNotFoundException, IllegalTaskStateException, RemoteException {
    getAPI(options).skip(activityInstanceUUID, variablesToUpdate);
  }

  @Override
  public void executeEvent(final CatchingEventUUID eventUUID, final Map<String, String> options)
      throws EventNotFoundException, RemoteException {
    getAPI(options).executeEvent(eventUUID);
  }

  @Override
  public void deleteEvent(final CatchingEventUUID eventUUID, final Map<String, String> options)
      throws EventNotFoundException, RemoteException {
    getAPI(options).deleteEvent(eventUUID);
  }

  public void deleteEvents(final Collection<CatchingEventUUID> eventUUIDs, final Map<String, String> options)
      throws EventNotFoundException, RemoteException {
    getAPI(options).deleteEvents(eventUUIDs);
  }

  @Override
  public void updateExpirationDate(final CatchingEventUUID eventUUID, final Date expiration,
      final Map<String, String> options) throws EventNotFoundException, RemoteException {
    getAPI(options).updateExpirationDate(eventUUID, expiration);
  }

  @Override
  public Object getModifiedJavaObject(final ProcessDefinitionUUID processUUID, final String variableExpression,
      final Object variableValue, final Object attributeValue, final Map<String, String> options)
      throws RemoteException {
    return getAPI(options).getModifiedJavaObject(processUUID, variableExpression, variableValue, attributeValue);
  }

  @Override
  public void updateActivityExpectedEndDate(final ActivityInstanceUUID activityUUID, final Date expectedEndDate,
      final Map<String, String> options) throws RemoteException, ActivityNotFoundException {
    getAPI(options).updateActivityExpectedEndDate(activityUUID, expectedEndDate);
  }

  @Override
  public Document createDocument(final String name, final ProcessDefinitionUUID processDefinitionUUID,
      final String fileName, final String mimeType, final byte[] content, final Map<String, String> options)
      throws RemoteException, DocumentationCreationException, ProcessNotFoundException {
    return getAPI(options).createDocument(name, processDefinitionUUID, fileName, mimeType, content);
  }

  @Override
  public Document createDocument(final String name, final ProcessInstanceUUID instanceUUID, final String fileName,
      final String mimeType, final byte[] content, final Map<String, String> options) throws RemoteException,
      DocumentationCreationException, InstanceNotFoundException {
    return getAPI(options).createDocument(name, instanceUUID, fileName, mimeType, content);
  }

  @Override
  public Document addDocumentVersion(final DocumentUUID documentUUID, final boolean isMajorVersion,
      final String fileName, final String mimeType, final byte[] content, final Map<String, String> options)
      throws RemoteException, DocumentationCreationException {
    return getAPI(options).addDocumentVersion(documentUUID, isMajorVersion, fileName, mimeType, content);
  }

  @Override
  public void deleteDocuments(final boolean allVersions, final DocumentUUID[] documentUUIDs,
      final Map<String, String> options) throws RemoteException, DocumentNotFoundException {
    getAPI(options).deleteDocuments(allVersions, documentUUIDs);
  }

  @Override
  public void createDocumentOrAddDocumentVersion(final ProcessInstanceUUID instanceUUID, final String name, final String fileName, final String mimeType, final byte[] value,
      final Map<String, String> options) throws RemoteException {
    getAPI(options).createDocumentOrAddDocumentVersion(instanceUUID, name, fileName, mimeType, value);
  }

}
