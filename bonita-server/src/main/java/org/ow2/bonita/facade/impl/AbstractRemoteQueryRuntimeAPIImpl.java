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
import org.ow2.bonita.facade.QueryRuntimeAPI;
import org.ow2.bonita.facade.exception.ActivityNotFoundException;
import org.ow2.bonita.facade.exception.DocumentNotFoundException;
import org.ow2.bonita.facade.exception.EventNotFoundException;
import org.ow2.bonita.facade.exception.InstanceNotFoundException;
import org.ow2.bonita.facade.exception.ProcessNotFoundException;
import org.ow2.bonita.facade.exception.TaskNotFoundException;
import org.ow2.bonita.facade.exception.VariableNotFoundException;
import org.ow2.bonita.facade.internal.AbstractRemoteQueryRuntimeAPI;
import org.ow2.bonita.facade.paging.ActivityInstanceCriterion;
import org.ow2.bonita.facade.paging.ProcessInstanceCriterion;
import org.ow2.bonita.facade.runtime.ActivityInstance;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.AttachmentInstance;
import org.ow2.bonita.facade.runtime.CatchingEvent;
import org.ow2.bonita.facade.runtime.Comment;
import org.ow2.bonita.facade.runtime.Document;
import org.ow2.bonita.facade.runtime.ProcessInstance;
import org.ow2.bonita.facade.runtime.TaskInstance;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.CatchingEventUUID;
import org.ow2.bonita.facade.uuid.DocumentUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.identity.auth.DomainOwner;
import org.ow2.bonita.identity.auth.PasswordOwner;
import org.ow2.bonita.identity.auth.RESTUserOwner;
import org.ow2.bonita.identity.auth.UserOwner;
import org.ow2.bonita.light.LightActivityInstance;
import org.ow2.bonita.light.LightProcessInstance;
import org.ow2.bonita.light.LightTaskInstance;
import org.ow2.bonita.search.DocumentResult;
import org.ow2.bonita.search.DocumentSearchBuilder;
import org.ow2.bonita.search.SearchQueryBuilder;

/**
 * 
 * @author Elias Ricken de Medeiros, Nicolas Chabanoles, Matthieu Chaffotte
 * 
 */
public abstract class AbstractRemoteQueryRuntimeAPIImpl implements AbstractRemoteQueryRuntimeAPI {

  protected Map<String, QueryRuntimeAPI> apis = new HashMap<String, QueryRuntimeAPI>();

  protected QueryRuntimeAPI getAPI(final Map<String, String> options) {
    if (options == null) {
      throw new IllegalArgumentException("The options are null or not well set.");
    }
    final String queryList = options.get(APIAccessor.QUERYLIST_OPTION);
    final String user = options.get(APIAccessor.USER_OPTION);
    final String domain = options.get(APIAccessor.DOMAIN_OPTION);
    UserOwner.setUser(user);
    DomainOwner.setDomain(domain);

    final String restUser = options.get(APIAccessor.REST_USER_OPTION);
    if (restUser != null) {
      RESTUserOwner.setUser(restUser);
      final String restPswd = options.get(APIAccessor.PASSWORD_HASH_OPTION);
      PasswordOwner.setPassword(restPswd);
    }

    if (!apis.containsKey(queryList)) {
      putAPI(queryList);
    }
    return apis.get(queryList);
  }

  protected void putAPI(final String queryList) {
    apis.put(queryList, new StandardAPIAccessorImpl().getQueryRuntimeAPI(queryList));
  }

  @Override
  public Set<String> getTaskCandidates(final ActivityInstanceUUID taskUUID, final Map<String, String> options)
      throws RemoteException, TaskNotFoundException {
    return getAPI(options).getTaskCandidates(taskUUID);
  }

  @Override
  public Map<ActivityInstanceUUID, Set<String>> getTaskCandidates(final Set<ActivityInstanceUUID> taskUUIDs,
      final Map<String, String> options) throws RemoteException, TaskNotFoundException {
    return getAPI(options).getTaskCandidates(taskUUIDs);
  }

  @Override
  public List<LightProcessInstance> getLightParentProcessInstances(final int fromIndex, final int pageSize,
      final Map<String, String> options) throws RemoteException {
    return getAPI(options).getLightParentProcessInstances(fromIndex, pageSize);
  }

  @Override
  public List<LightProcessInstance> getLightParentProcessInstances(final int fromIndex, final int pageSize,
      final ProcessInstanceCriterion pagingCriterion, final Map<String, String> options) throws RemoteException {
    return getAPI(options).getLightParentProcessInstances(fromIndex, pageSize, pagingCriterion);
  }

  @Override
  public List<LightProcessInstance> getLightParentProcessInstances(final Set<ProcessDefinitionUUID> processUUIDs,
      final int fromIndex, final int pageSize, final ProcessInstanceCriterion pagingCriterion,
      final Map<String, String> options) throws RemoteException {
    return getAPI(options).getLightParentProcessInstances(processUUIDs, fromIndex, pageSize, pagingCriterion);
  }

  @Override
  public List<LightProcessInstance> getLightParentProcessInstancesExcept(final Set<ProcessDefinitionUUID> exceptions,
      final int fromIndex, final int pageSize, final ProcessInstanceCriterion pagingCriterion,
      final Map<String, String> options) throws RemoteException {
    return getAPI(options).getLightParentProcessInstancesExcept(exceptions, fromIndex, pageSize, pagingCriterion);
  }

  @Override
  public int getNumberOfParentProcessInstances(final Map<String, String> options) throws RemoteException {
    return getAPI(options).getNumberOfParentProcessInstances();
  }

  @Override
  public int getNumberOfParentProcessInstances(final Set<ProcessDefinitionUUID> processDefinitionUUIDs,
      final Map<String, String> options) throws RemoteException {
    return getAPI(options).getNumberOfParentProcessInstances(processDefinitionUUIDs);
  }

  @Override
  public int getNumberOfParentProcessInstancesExcept(final Set<ProcessDefinitionUUID> exceptions,
      final Map<String, String> options) throws RemoteException {
    return getAPI(options).getNumberOfParentProcessInstancesExcept(exceptions);
  }

  @Override
  public int getNumberOfProcessInstances(final Map<String, String> options) throws RemoteException {
    return getAPI(options).getNumberOfProcessInstances();
  }

  @Override
  public List<LightActivityInstance> getLightActivityInstancesFromRoot(final ProcessInstanceUUID rootInstanceUUID,
      final Map<String, String> options) throws RemoteException {
    return getAPI(options).getLightActivityInstancesFromRoot(rootInstanceUUID);
  }

  @Override
  public Map<ProcessInstanceUUID, List<LightActivityInstance>> getLightActivityInstancesFromRoot(
      final Set<ProcessInstanceUUID> rootInstanceUUIDs, final Map<String, String> options) throws RemoteException {
    return getAPI(options).getLightActivityInstancesFromRoot(rootInstanceUUIDs);
  }

  @Override
  public List<LightTaskInstance> getLightTaskInstancesFromRoot(final ProcessInstanceUUID rootInstanceUUID,
      final Map<String, String> options) throws RemoteException {
    return getAPI(options).getLightTaskInstancesFromRoot(rootInstanceUUID);
  }

  @Override
  public Map<ProcessInstanceUUID, List<LightTaskInstance>> getLightTaskInstancesFromRoot(
      final Set<ProcessInstanceUUID> rootInstanceUUIDs, final Map<String, String> options) throws RemoteException {
    return getAPI(options).getLightTaskInstancesFromRoot(rootInstanceUUIDs);
  }

  @Override
  public ActivityInstanceUUID getOneTask(final ProcessDefinitionUUID processUUID, final ActivityState taskState,
      final Map<String, String> options) throws RemoteException {
    return getAPI(options).getOneTask(processUUID, taskState);
  }

  @Override
  public ActivityInstanceUUID getOneTask(final ProcessInstanceUUID instanceUUID, final ActivityState taskState,
      final Map<String, String> options) throws RemoteException {
    return getAPI(options).getOneTask(instanceUUID, taskState);
  }

  @Override
  public ActivityInstanceUUID getOneTask(final ActivityState taskState, final Map<String, String> options)
      throws RemoteException {
    return getAPI(options).getOneTask(taskState);
  }

  @Override
  public Set<ActivityInstance> getActivityInstances(final ProcessInstanceUUID instanceUUID,
      final Map<String, String> options) throws InstanceNotFoundException, RemoteException {
    return getAPI(options).getActivityInstances(instanceUUID);
  }

  @Override
  public Set<ActivityInstance> getActivityInstances(final ProcessInstanceUUID instanceUUID, final String activityId,
      final Map<String, String> options) throws InstanceNotFoundException, ActivityNotFoundException, RemoteException {
    return getAPI(options).getActivityInstances(instanceUUID, activityId);
  }

  @Override
  public ActivityInstance getActivityInstance(final ActivityInstanceUUID activityUUID, final Map<String, String> options)
      throws ActivityNotFoundException, RemoteException {
    return getAPI(options).getActivityInstance(activityUUID);
  }

  @Override
  public ProcessInstance getProcessInstance(final ProcessInstanceUUID instanceUUID, final Map<String, String> options)
      throws InstanceNotFoundException, RemoteException {
    return getAPI(options).getProcessInstance(instanceUUID);
  }

  @Override
  public Set<ProcessInstance> getProcessInstances(final Map<String, String> options) throws RemoteException {
    return getAPI(options).getProcessInstances();
  }

  @Override
  public Boolean canExecuteTask(final ActivityInstanceUUID taskUUID, final Map<String, String> options)
      throws TaskNotFoundException, RemoteException {
    return getAPI(options).canExecuteTask(taskUUID);
  }

  @Override
  public Set<ProcessInstance> getProcessInstances(final ProcessDefinitionUUID processUUID,
      final Map<String, String> options) throws ProcessNotFoundException, RemoteException {
    return getAPI(options).getProcessInstances(processUUID);
  }

  @Override
  public TaskInstance getTask(final ActivityInstanceUUID taskUUID, final Map<String, String> options)
      throws TaskNotFoundException, RemoteException {
    return getAPI(options).getTask(taskUUID);
  }

  @Override
  public Collection<TaskInstance> getTaskList(final ProcessInstanceUUID instanceUUID, final ActivityState taskState,
      final Map<String, String> options) throws InstanceNotFoundException, RemoteException {
    return getAPI(options).getTaskList(instanceUUID, taskState);
  }

  @Override
  public Collection<TaskInstance> getTaskList(final ProcessInstanceUUID instanceUUID, final String userId,
      final ActivityState taskState, final Map<String, String> options) throws InstanceNotFoundException,
      RemoteException {
    return getAPI(options).getTaskList(instanceUUID, userId, taskState);
  }

  @Override
  public Collection<TaskInstance> getTaskList(final ActivityState taskState, final Map<String, String> options)
      throws RemoteException {
    return getAPI(options).getTaskList(taskState);
  }

  @Override
  public Collection<TaskInstance> getTaskList(final String userId, final ActivityState taskState,
      final Map<String, String> options) throws RemoteException {
    return getAPI(options).getTaskList(userId, taskState);
  }

  @Override
  public Set<TaskInstance> getTasks(final ProcessInstanceUUID instanceUUID, final Map<String, String> options)
      throws InstanceNotFoundException, RemoteException {
    return getAPI(options).getTasks(instanceUUID);
  }

  @Override
  public Object getActivityInstanceVariable(final ActivityInstanceUUID activityUUID, final String variableId,
      final Map<String, String> options) throws ActivityNotFoundException, VariableNotFoundException, RemoteException {
    return getAPI(options).getActivityInstanceVariable(activityUUID, variableId);
  }

  @Override
  public ActivityState getActivityInstanceState(final ActivityInstanceUUID activityUUID,
      final Map<String, String> options) throws ActivityNotFoundException, RemoteException {
    return getAPI(options).getActivityInstanceState(activityUUID);
  }

  @Override
  public Object getProcessInstanceVariable(final ProcessInstanceUUID instanceUUID, final String variableId,
      final Map<String, String> options) throws InstanceNotFoundException, VariableNotFoundException, RemoteException {
    return getAPI(options).getProcessInstanceVariable(instanceUUID, variableId);
  }

  @Override
  public Map<String, Object> getActivityInstanceVariables(final ActivityInstanceUUID activityUUID,
      final Map<String, String> options) throws ActivityNotFoundException, RemoteException {
    return getAPI(options).getActivityInstanceVariables(activityUUID);
  }

  @Override
  public Map<String, Object> getProcessInstanceVariables(final ProcessInstanceUUID instanceUUID,
      final Map<String, String> options) throws InstanceNotFoundException, RemoteException {
    return getAPI(options).getProcessInstanceVariables(instanceUUID);
  }

  @Override
  public Map<String, Object> getProcessInstanceVariables(final ProcessInstanceUUID instanceUUID, final Date maxDate,
      final Map<String, String> options) throws RemoteException, InstanceNotFoundException {
    return getAPI(options).getProcessInstanceVariables(instanceUUID, maxDate);
  }

  @Override
  public Object getVariable(final ActivityInstanceUUID activityUUID, final String variableId,
      final Map<String, String> options) throws ActivityNotFoundException, VariableNotFoundException, RemoteException {
    return getAPI(options).getVariable(activityUUID, variableId);
  }

  @Override
  public Map<String, Object> getVariables(final ActivityInstanceUUID activityUUID, final Map<String, String> options)
      throws InstanceNotFoundException, ActivityNotFoundException, RemoteException {
    return getAPI(options).getVariables(activityUUID);
  }

  @Override
  public List<Comment> getCommentFeed(final ProcessInstanceUUID instanceUUID, final Map<String, String> options)
      throws InstanceNotFoundException, RemoteException {
    return getAPI(options).getCommentFeed(instanceUUID);
  }

  @Override
  public Set<ProcessInstance> getUserInstances(final Map<String, String> options) throws RemoteException {
    return getAPI(options).getUserInstances();
  }

  @Override
  @Deprecated
  public Set<String> getAttachmentNames(final ProcessInstanceUUID instanceUUID, final Map<String, String> options)
      throws RemoteException {
    return getAPI(options).getAttachmentNames(instanceUUID);
  }

  @Override
  @Deprecated
  public List<AttachmentInstance> getAttachments(final ProcessInstanceUUID instanceUUID, final String attachmentName,
      final Map<String, String> options) throws RemoteException {
    return getAPI(options).getAttachments(instanceUUID, attachmentName);
  }

  @Override
  @Deprecated
  public AttachmentInstance getLastAttachment(final ProcessInstanceUUID instanceUUID, final String attachmentName,
      final Map<String, String> options) throws RemoteException {
    return getAPI(options).getLastAttachment(instanceUUID, attachmentName);
  }

  @Override
  @Deprecated
  public Collection<AttachmentInstance> getLastAttachments(final ProcessInstanceUUID instanceUUID,
      final Set<String> attachmentNames, final Map<String, String> options) throws RemoteException {
    return getAPI(options).getLastAttachments(instanceUUID, attachmentNames);
  }

  @Override
  @Deprecated
  public AttachmentInstance getLastAttachment(final ProcessInstanceUUID instanceUUID, final String attachmentName,
      final ActivityInstanceUUID activityUUID, final Map<String, String> options) throws ActivityNotFoundException,
      RemoteException {
    return getAPI(options).getLastAttachment(instanceUUID, attachmentName, activityUUID);
  }

  @Override
  @Deprecated
  public AttachmentInstance getLastAttachment(final ProcessInstanceUUID instanceUUID, final String attachmentName,
      final Date date, final Map<String, String> options) throws RemoteException {
    return getAPI(options).getLastAttachment(instanceUUID, attachmentName, date);
  }

  @Override
  @Deprecated
  public Collection<AttachmentInstance> getLastAttachments(final ProcessInstanceUUID instanceUUID, final String regex,
      final Map<String, String> options) throws RemoteException {
    return getAPI(options).getLastAttachments(instanceUUID, regex);
  }

  @Override
  @Deprecated
  public byte[] getAttachmentValue(final AttachmentInstance attachmentInstance, final Map<String, String> options)
      throws RemoteException {
    return getAPI(options).getAttachmentValue(attachmentInstance);
  }

  @Override
  public Set<LightProcessInstance> getLightUserInstances(final Map<String, String> options) throws RemoteException {
    return getAPI(options).getLightUserInstances();
  }

  @Override
  public List<LightProcessInstance> getLightParentUserInstances(final int startingIndex, final int pageSize,
      final Map<String, String> options) throws RemoteException {
    return getAPI(options).getLightParentUserInstances(startingIndex, pageSize);
  }

  @Override
  public LightProcessInstance getLightProcessInstance(final ProcessInstanceUUID instanceUUID,
      final Map<String, String> options) throws InstanceNotFoundException, RemoteException {
    return getAPI(options).getLightProcessInstance(instanceUUID);
  }

  @Override
  public Set<LightProcessInstance> getLightProcessInstances(final ProcessDefinitionUUID processUUID,
      final Map<String, String> options) throws RemoteException {
    return getAPI(options).getLightProcessInstances(processUUID);
  }

  @Override
  public List<LightProcessInstance> getLightProcessInstances(final Set<ProcessInstanceUUID> instanceUUIDs,
      final int fromIndex, final int pageSize, final ProcessInstanceCriterion pagingCriterion,
      final Map<String, String> options) throws RemoteException {
    return getAPI(options).getLightProcessInstances(instanceUUIDs, fromIndex, pageSize, pagingCriterion);
  }

  @Override
  public Set<LightProcessInstance> getLightWeightProcessInstances(final Set<ProcessDefinitionUUID> processUUIDs,
      final Map<String, String> options) throws RemoteException {
    return getAPI(options).getLightWeightProcessInstances(processUUIDs);
  }

  @Override
  public Set<LightProcessInstance> getLightProcessInstances(final Map<String, String> options) throws RemoteException {
    return getAPI(options).getLightProcessInstances();
  }

  @Override
  public List<LightProcessInstance> getLightProcessInstances(final int fromIndex, final int pageSize,
      final Map<String, String> options) throws RemoteException {
    return getAPI(options).getLightProcessInstances(fromIndex, pageSize);
  }

  @Override
  public List<LightProcessInstance> getLightParentUserInstances(final int fromIndex, final int pageSize,
      final ProcessInstanceCriterion pagingCriterion, final Map<String, String> options) throws RemoteException {
    return getAPI(options).getLightParentUserInstances(fromIndex, pageSize, pagingCriterion);
  }

  @Override
  public List<LightProcessInstance> getLightProcessInstances(final int fromIndex, final int pageSize,
      final ProcessInstanceCriterion pagingCriterion, final Map<String, String> options) throws RemoteException {
    return getAPI(options).getLightProcessInstances(fromIndex, pageSize, pagingCriterion);
  }

  @Override
  public List<LightProcessInstance> getLightParentProcessInstancesWithActiveUser(final String userId,
      final int fromIndex, final int pageSize, final Map<String, String> options) throws RemoteException {
    return getAPI(options).getLightParentProcessInstancesWithActiveUser(userId, fromIndex, pageSize);
  }

  @Override
  public List<LightProcessInstance> getLightParentProcessInstancesWithActiveUser(final String userId,
      final int fromIndex, final int pageSize, final ProcessInstanceCriterion pagingCriterion,
      final Map<String, String> options) throws RemoteException {
    return getAPI(options).getLightParentProcessInstancesWithActiveUser(userId, fromIndex, pageSize, pagingCriterion);
  }

  @Override
  public List<LightProcessInstance> getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(
      final String userId, final int remainingDays, final int fromIndex, final int pageSize,
      final Map<String, String> options) throws RemoteException {
    return getAPI(options).getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(userId,
        remainingDays, fromIndex, pageSize);
  }

  @Override
  public List<LightProcessInstance> getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(
      final String userId, final int remainingDays, final int fromIndex, final int pageSize,
      final ProcessInstanceCriterion pagingCriterion, final Map<String, String> options) throws RemoteException {
    return getAPI(options).getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(userId,
        remainingDays, fromIndex, pageSize, pagingCriterion);
  }

  @Override
  public List<LightProcessInstance> getLightParentProcessInstancesWithOverdueTasks(final String userId,
      final int fromIndex, final int pageSize, final Map<String, String> options) throws RemoteException {
    return getAPI(options).getLightParentProcessInstancesWithOverdueTasks(userId, fromIndex, pageSize);
  }

  @Override
  public List<LightProcessInstance> getLightParentProcessInstancesWithOverdueTasks(final String userId,
      final int fromIndex, final int pageSize, final ProcessInstanceCriterion pagingCriterion,
      final Map<String, String> options) throws RemoteException {
    return getAPI(options).getLightParentProcessInstancesWithOverdueTasks(userId, fromIndex, pageSize, pagingCriterion);
  }

  @Override
  public List<LightProcessInstance> getLightParentProcessInstancesWithInvolvedUser(final String userId,
      final int fromIndex, final int pageSize, final Map<String, String> options) throws RemoteException {
    return getAPI(options).getLightParentProcessInstancesWithInvolvedUser(userId, fromIndex, pageSize);
  }

  @Override
  public List<LightProcessInstance> getLightParentProcessInstancesWithInvolvedUser(final String userId,
      final int fromIndex, final int pageSize, final ProcessInstanceCriterion pagingCriterion,
      final Map<String, String> options) throws RemoteException {
    return getAPI(options).getLightParentProcessInstancesWithInvolvedUser(userId, fromIndex, pageSize, pagingCriterion);
  }

  @Override
  public Set<LightActivityInstance> getLightActivityInstances(final ProcessInstanceUUID instanceUUID,
      final Map<String, String> options) throws InstanceNotFoundException, RemoteException {
    return getAPI(options).getLightActivityInstances(instanceUUID);
  }

  @Override
  public Set<LightTaskInstance> getLightTasks(final ProcessInstanceUUID instanceUUID, final Map<String, String> options)
      throws InstanceNotFoundException, RemoteException {
    return getAPI(options).getLightTasks(instanceUUID);
  }

  @Override
  public Set<LightTaskInstance> getLightTasks(final ProcessInstanceUUID instanceUUID, final Set<String> taskNames,
      final Map<String, String> options) throws RemoteException {
    return getAPI(options).getLightTasks(instanceUUID, taskNames);
  }

  @Override
  public List<Comment> getActivityInstanceCommentFeed(final ActivityInstanceUUID activityUUID,
      final Map<String, String> options) throws InstanceNotFoundException, RemoteException {
    return getAPI(options).getActivityInstanceCommentFeed(activityUUID);
  }

  @Override
  public List<Comment> getProcessInstanceCommentFeed(final ProcessInstanceUUID instanceUUID,
      final Map<String, String> options) throws InstanceNotFoundException, RemoteException {
    return getAPI(options).getProcessInstanceCommentFeed(instanceUUID);
  }

  @Override
  public int getNumberOfActivityInstanceComments(final ActivityInstanceUUID activityUUID,
      final Map<String, String> options) throws InstanceNotFoundException, RemoteException {
    return getAPI(options).getNumberOfActivityInstanceComments(activityUUID);
  }

  @Override
  public Map<ActivityInstanceUUID, Integer> getNumberOfActivityInstanceComments(
      final Set<ActivityInstanceUUID> activityUUIDs, final Map<String, String> options)
      throws InstanceNotFoundException, RemoteException {
    return getAPI(options).getNumberOfActivityInstanceComments(activityUUIDs);
  }

  @Override
  public int getNumberOfComments(final ProcessInstanceUUID instanceUUID, final Map<String, String> options)
      throws InstanceNotFoundException, RemoteException {
    return getAPI(options).getNumberOfComments(instanceUUID);
  }

  @Override
  public int getNumberOfProcessInstanceComments(final ProcessInstanceUUID instanceUUID,
      final Map<String, String> options) throws InstanceNotFoundException, RemoteException {
    return getAPI(options).getNumberOfProcessInstanceComments(instanceUUID);
  }

  @Override
  public LightTaskInstance getLightTaskInstance(final ActivityInstanceUUID taskUUID, final Map<String, String> options)
      throws TaskNotFoundException, RemoteException {
    return getAPI(options).getLightTaskInstance(taskUUID);
  }

  @Override
  public Set<LightActivityInstance> getLightActivityInstances(final ProcessInstanceUUID instanceUUID,
      final String activityName, final Map<String, String> options) throws InstanceNotFoundException,
      ActivityNotFoundException, RemoteException {
    return getAPI(options).getLightActivityInstances(instanceUUID, activityName);
  }

  @Override
  public Set<LightActivityInstance> getLightActivityInstances(final ProcessInstanceUUID instanceUUID,
      final String activityName, final String iterationId, final Map<String, String> options) throws RemoteException {
    return getAPI(options).getLightActivityInstances(instanceUUID, activityName, iterationId);
  }

  @Override
  public LightActivityInstance getLightActivityInstance(final ActivityInstanceUUID activityInstanceUUID,
      final Map<String, String> options) throws ActivityNotFoundException, RemoteException {
    return getAPI(options).getLightActivityInstance(activityInstanceUUID);
  }

  @Override
  public Collection<LightTaskInstance> getLightTaskList(final ActivityState taskState, final Map<String, String> options)
      throws RemoteException {
    return getAPI(options).getLightTaskList(taskState);
  }

  @Override
  public Collection<LightTaskInstance> getLightTaskList(final ProcessInstanceUUID instanceUUID,
      final ActivityState taskState, final Map<String, String> options) throws InstanceNotFoundException,
      RemoteException {
    return getAPI(options).getLightTaskList(instanceUUID, taskState);
  }

  public Collection<LightTaskInstance> getLightTaskList(final ProcessInstanceUUID instanceUUID,
      final Collection<ActivityState> taskStates, final Map<String, String> options) throws InstanceNotFoundException,
      RemoteException {
    return getAPI(options).getLightTaskList(instanceUUID, taskStates);
  }

  @Override
  public Collection<LightTaskInstance> getLightTaskList(final ProcessInstanceUUID instanceUUID, final String userId,
      final ActivityState taskState, final Map<String, String> options) throws InstanceNotFoundException,
      RemoteException {
    return getAPI(options).getLightTaskList(instanceUUID, userId, taskState);
  }

  @Override
  public Collection<LightTaskInstance> getLightTaskList(final String userId, final ActivityState taskState,
      final Map<String, String> options) throws RemoteException {
    return getAPI(options).getLightTaskList(userId, taskState);
  }

  @Override
  public Map<ProcessInstanceUUID, List<LightActivityInstance>> getLightActivityInstancesFromRoot(
      final Set<ProcessInstanceUUID> rootInstanceUUIDs, final ActivityState state, final Map<String, String> options)
      throws RemoteException {
    return getAPI(options).getLightActivityInstancesFromRoot(rootInstanceUUIDs, state);
  }

  @Override
  public Map<ProcessInstanceUUID, LightActivityInstance> getLightLastUpdatedActivityInstanceFromRoot(
      final Set<ProcessInstanceUUID> rootInstanceUUIDs, final boolean considerSystemTaks,
      final Map<String, String> options) throws RemoteException {
    return getAPI(options).getLightLastUpdatedActivityInstanceFromRoot(rootInstanceUUIDs, considerSystemTaks);
  }

  @Override
  public Integer getNumberOfParentProcessInstancesWithActiveUser(final String userId, final Map<String, String> options)
      throws RemoteException {
    return getAPI(options).getNumberOfParentProcessInstancesWithActiveUser(userId);
  }

  @Override
  public Integer getNumberOfParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(final String userId,
      final int remainingDays, final Map<String, String> options) throws RemoteException {
    return getAPI(options).getNumberOfParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(userId,
        remainingDays);
  }

  @Override
  public Integer getNumberOfParentProcessInstancesWithOverdueTasks(final String userId,
      final Map<String, String> options) throws RemoteException {
    return getAPI(options).getNumberOfParentProcessInstancesWithOverdueTasks(userId);
  }

  @Override
  public Integer getNumberOfParentProcessInstancesWithInvolvedUser(final String userId,
      final Map<String, String> options) throws RemoteException {
    return getAPI(options).getNumberOfParentProcessInstancesWithInvolvedUser(userId);
  }

  @Override
  public Integer getNumberOfParentProcessInstancesWithStartedBy(final String userId, final Map<String, String> options)
      throws RemoteException {
    return getAPI(options).getNumberOfParentProcessInstancesWithStartedBy(userId);
  }

  @Override
  public Integer getNumberOfParentProcessInstancesWithInvolvedUserAndCategory(final String userId,
      final String category, final Map<String, String> options) throws RemoteException {
    return getAPI(options).getNumberOfParentProcessInstancesWithInvolvedUserAndCategory(userId, category);
  }

  public List<LightProcessInstance> getLightParentProcessInstancesWithInvolvedUser(final String userId,
      final Set<ProcessDefinitionUUID> processes, final int fromIndex, final int pageSize,
      final Map<String, String> options) throws RemoteException {
    return getAPI(options).getLightParentProcessInstancesWithInvolvedUser(userId, fromIndex, pageSize);
  }

  @Override
  public <T> List<T> search(final SearchQueryBuilder query, final int firstResult, final int maxResults,
      final Map<String, String> options) throws RemoteException {
    return getAPI(options).search(query, firstResult, maxResults);
  }

  @Override
  public int search(final SearchQueryBuilder query, final Map<String, String> options) throws RemoteException {
    return getAPI(options).search(query);
  }

  @Override
  public List<LightProcessInstance> getLightParentProcessInstancesWithActiveUser(final String userId,
      final int fromIndex, final int pageSize, final Set<ProcessDefinitionUUID> processes,
      final Map<String, String> options) throws RemoteException {
    return getAPI(options).getLightParentProcessInstancesWithActiveUser(userId, fromIndex, pageSize, processes);
  }

  @Override
  public List<LightProcessInstance> getLightParentProcessInstancesWithActiveUser(final String username,
      final int fromIndex, final int pageSize, final Set<ProcessDefinitionUUID> processUUIDs,
      final ProcessInstanceCriterion pagingCriterion, final Map<String, String> options) throws RemoteException {
    return getAPI(options).getLightParentProcessInstancesWithActiveUser(username, fromIndex, pageSize, processUUIDs,
        pagingCriterion);
  }

  @Override
  public List<LightProcessInstance> getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(
      final String userId, final int remainingDays, final int fromIndex, final int pageSize,
      final Set<ProcessDefinitionUUID> processes, final Map<String, String> options) throws RemoteException {
    return getAPI(options).getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(userId,
        remainingDays, fromIndex, pageSize, processes);
  }

  @Override
  public List<LightProcessInstance> getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(
      final String username, final int remainingDays, final int fromIndex, final int pageSize,
      final Set<ProcessDefinitionUUID> processUUIDs, final ProcessInstanceCriterion pagingCriterion,
      final Map<String, String> options) throws RemoteException {
    return getAPI(options).getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(username,
        remainingDays, fromIndex, pageSize, processUUIDs, pagingCriterion);
  }

  @Override
  public List<LightProcessInstance> getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDateExcept(
      final String userId, final int remainingDays, final int fromIndex, final int pageSize,
      final Set<ProcessDefinitionUUID> processes, final Map<String, String> options) throws RemoteException {
    return getAPI(options).getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDateExcept(userId,
        remainingDays, fromIndex, pageSize, processes);
  }

  @Override
  public List<LightProcessInstance> getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDateExcept(
      final String username, final int remainingDays, final int fromIndex, final int pageSize,
      final Set<ProcessDefinitionUUID> processUUIDs, final ProcessInstanceCriterion pagingCriterion,
      final Map<String, String> options) throws RemoteException {
    return getAPI(options).getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDateExcept(
        username, remainingDays, fromIndex, pageSize, processUUIDs, pagingCriterion);
  }

  @Override
  public List<LightProcessInstance> getLightParentProcessInstancesWithActiveUserExcept(final String userId,
      final int fromIndex, final int pageSize, final Set<ProcessDefinitionUUID> processes,
      final Map<String, String> options) throws RemoteException {
    return getAPI(options).getLightParentProcessInstancesWithActiveUserExcept(userId, fromIndex, pageSize, processes);
  }

  @Override
  public List<LightProcessInstance> getLightParentProcessInstancesWithActiveUserExcept(final String userId,
      final int fromIndex, final int pageSize, final Set<ProcessDefinitionUUID> processUUIDs,
      final ProcessInstanceCriterion pagingCriterion, final Map<String, String> options) throws RemoteException {
    return getAPI(options).getLightParentProcessInstancesWithActiveUserExcept(userId, fromIndex, pageSize,
        processUUIDs, pagingCriterion);
  }

  @Override
  public List<LightProcessInstance> getLightParentProcessInstancesWithInvolvedUser(final String userId,
      final int fromIndex, final int pageSize, final Set<ProcessDefinitionUUID> processUUIDs,
      final Map<String, String> options) throws RemoteException {
    return getAPI(options).getLightParentProcessInstancesWithInvolvedUser(userId, fromIndex, pageSize, processUUIDs);
  }

  @Override
  public List<LightProcessInstance> getLightParentProcessInstancesWithInvolvedUser(final String userId,
      final int fromIndex, final int pageSize, final Set<ProcessDefinitionUUID> processUUIDs,
      final ProcessInstanceCriterion pagingCriterion, final Map<String, String> options) throws RemoteException {
    return getAPI(options).getLightParentProcessInstancesWithInvolvedUser(userId, fromIndex, pageSize, processUUIDs,
        pagingCriterion);
  }

  @Override
  public List<LightProcessInstance> getLightParentProcessInstancesWithInvolvedUserExcept(final String userId,
      final int fromIndex, final int pageSize, final Set<ProcessDefinitionUUID> processes,
      final Map<String, String> options) throws RemoteException {
    return getAPI(options).getLightParentProcessInstancesWithInvolvedUserExcept(userId, fromIndex, pageSize, processes);
  }

  @Override
  public List<LightProcessInstance> getLightParentProcessInstancesWithInvolvedUserExcept(final String username,
      final int fromIndex, final int pageSize, final Set<ProcessDefinitionUUID> processUUIDs,
      final ProcessInstanceCriterion pagingCriterion, final Map<String, String> options) throws RemoteException {
    return getAPI(options).getLightParentProcessInstancesWithInvolvedUserExcept(username, fromIndex, pageSize,
        processUUIDs, pagingCriterion);
  }

  @Override
  public List<LightProcessInstance> getLightParentProcessInstancesWithOverdueTasks(final String userId,
      final int fromIndex, final int pageSize, final Set<ProcessDefinitionUUID> processes,
      final Map<String, String> options) throws RemoteException {
    return getAPI(options).getLightParentProcessInstancesWithOverdueTasks(userId, fromIndex, pageSize, processes);
  }

  @Override
  public List<LightProcessInstance> getLightParentProcessInstancesWithOverdueTasks(final String username,
      final int fromIndex, final int pageSize, final Set<ProcessDefinitionUUID> processUUIDs,
      final ProcessInstanceCriterion pagingCriterion, final Map<String, String> options) throws RemoteException {
    return getAPI(options).getLightParentProcessInstancesWithOverdueTasks(username, fromIndex, pageSize, processUUIDs,
        pagingCriterion);
  }

  @Override
  public List<LightProcessInstance> getLightParentProcessInstancesWithOverdueTasksExcept(final String userId,
      final int fromIndex, final int pageSize, final Set<ProcessDefinitionUUID> processes,
      final Map<String, String> options) throws RemoteException {
    return getAPI(options).getLightParentProcessInstancesWithOverdueTasksExcept(userId, fromIndex, pageSize, processes);
  }

  @Override
  public List<LightProcessInstance> getLightParentProcessInstancesWithOverdueTasksExcept(final String username,
      final int fromIndex, final int pageSize, final Set<ProcessDefinitionUUID> processUUIDs,
      final ProcessInstanceCriterion pagingCriterion, final Map<String, String> options) throws RemoteException {
    return getAPI(options).getLightParentProcessInstancesWithOverdueTasksExcept(username, fromIndex, pageSize,
        processUUIDs, pagingCriterion);
  }

  @Override
  public Integer getNumberOfParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(final String userId,
      final int remainingDays, final Set<ProcessDefinitionUUID> processes, final Map<String, String> options)
      throws RemoteException {
    return getAPI(options).getNumberOfParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(userId,
        remainingDays, processes);
  }

  @Override
  public Integer getNumberOfParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDateExcept(
      final String userId, final int remainingDays, final Set<ProcessDefinitionUUID> processes,
      final Map<String, String> options) throws RemoteException {
    return getAPI(options).getNumberOfParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDateExcept(
        userId, remainingDays, processes);
  }

  public Integer getNumberOfParentProcessInstancesWithActiveUserAndProcessUUIDs(final String userId,
      final Set<ProcessDefinitionUUID> processes, final Map<String, String> options) throws RemoteException {
    return getAPI(options).getNumberOfParentProcessInstancesWithActiveUser(userId, processes);
  }

  @Override
  public Integer getNumberOfParentProcessInstancesWithActiveUserExcept(final String userId,
      final Set<ProcessDefinitionUUID> processes, final Map<String, String> options) throws RemoteException {
    return getAPI(options).getNumberOfParentProcessInstancesWithActiveUserExcept(userId, processes);
  }

  @Override
  public Integer getNumberOfParentProcessInstancesWithInvolvedUser(final String userId,
      final Set<ProcessDefinitionUUID> processUUIDs, final Map<String, String> options) throws RemoteException {
    return getAPI(options).getNumberOfParentProcessInstancesWithInvolvedUser(userId, processUUIDs);
  }

  @Override
  public Integer getNumberOfParentProcessInstancesWithInvolvedUserAndCategory(final String userId,
      final String category, final Set<ProcessDefinitionUUID> processUUIDs, final Map<String, String> options)
      throws RemoteException {
    return getAPI(options).getNumberOfParentProcessInstancesWithInvolvedUserAndCategory(userId, category, processUUIDs);
  }

  @Override
  public Integer getNumberOfParentProcessInstancesWithInvolvedUserAndCategoryExcept(final String userId,
      final String category, final Set<ProcessDefinitionUUID> processUUIDs, final Map<String, String> options)
      throws RemoteException {
    return getAPI(options).getNumberOfParentProcessInstancesWithInvolvedUserAndCategoryExcept(userId, category,
        processUUIDs);
  }

  @Override
  public Integer getNumberOfParentProcessInstancesWithInvolvedUserExcept(final String userId,
      final Set<ProcessDefinitionUUID> processUUIDs, final Map<String, String> options) throws RemoteException {
    return getAPI(options).getNumberOfParentProcessInstancesWithInvolvedUserExcept(userId, processUUIDs);
  }

  @Override
  public Integer getNumberOfParentProcessInstancesWithOverdueTasks(final String userId,
      final Set<ProcessDefinitionUUID> processUUIDs, final Map<String, String> options) throws RemoteException {
    return getAPI(options).getNumberOfParentProcessInstancesWithOverdueTasks(userId, processUUIDs);
  }

  @Override
  public Integer getNumberOfParentProcessInstancesWithOverdueTasksExcept(final String userId,
      final Set<ProcessDefinitionUUID> processUUIDs, final Map<String, String> options) throws RemoteException {
    return getAPI(options).getNumberOfParentProcessInstancesWithOverdueTasksExcept(userId, processUUIDs);
  }

  @Override
  public Integer getNumberOfParentProcessInstancesWithStartedBy(final String userId,
      final Set<ProcessDefinitionUUID> processUUIDs, final Map<String, String> options) throws RemoteException {
    return getAPI(options).getNumberOfParentProcessInstancesWithStartedBy(userId, processUUIDs);
  }

  @Override
  public Integer getNumberOfParentProcessInstancesWithStartedByExcept(final String userId,
      final Set<ProcessDefinitionUUID> processUUIDs, final Map<String, String> options) throws RemoteException {
    return getAPI(options).getNumberOfParentProcessInstancesWithStartedByExcept(userId, processUUIDs);
  }

  @Override
  public List<LightProcessInstance> getLightParentUserInstances(final int fromIndex, final int pageSize,
      final Set<ProcessDefinitionUUID> processUUIDs, final Map<String, String> options) throws RemoteException {
    return getAPI(options).getLightParentUserInstances(fromIndex, pageSize, processUUIDs);
  }

  @Override
  public List<LightProcessInstance> getLightParentUserInstances(final int fromIndex, final int pageSize,
      final Set<ProcessDefinitionUUID> processUUIDs, final ProcessInstanceCriterion pagingCriterion,
      final Map<String, String> options) throws RemoteException {
    return getAPI(options).getLightParentUserInstances(fromIndex, pageSize, processUUIDs, pagingCriterion);
  }

  @Override
  public List<LightProcessInstance> getLightParentUserInstancesExcept(final int fromIndex, final int pageSize,
      final Set<ProcessDefinitionUUID> processUUIDs, final Map<String, String> options) throws RemoteException {
    return getAPI(options).getLightParentUserInstancesExcept(fromIndex, pageSize, processUUIDs);
  }

  @Override
  public List<LightProcessInstance> getLightParentUserInstancesExcept(final int fromIndex, final int pageSize,
      final Set<ProcessDefinitionUUID> processUUIDs, final ProcessInstanceCriterion pagingCriterion,
      final Map<String, String> options) throws RemoteException {
    return getAPI(options).getLightParentUserInstancesExcept(fromIndex, pageSize, processUUIDs, pagingCriterion);
  }

  @Override
  public Integer getNumberOfParentProcessInstancesWithActiveUser(final String username,
      final Set<ProcessDefinitionUUID> processUUIDs, final Map<String, String> options) throws RemoteException {
    return getAPI(options).getNumberOfParentProcessInstancesWithActiveUser(username, processUUIDs);
  }

  @Override
  public Set<String> getActiveUsersOfProcessInstance(final ProcessInstanceUUID uuid, final Map<String, String> options)
      throws RemoteException, InstanceNotFoundException {
    return getAPI(options).getActiveUsersOfProcessInstance(uuid);
  }

  @Override
  public Map<ProcessInstanceUUID, Set<String>> getActiveUsersOfProcessInstances(
      final Set<ProcessInstanceUUID> instanceUUIDs, final Map<String, String> options) throws RemoteException,
      InstanceNotFoundException {
    return getAPI(options).getActiveUsersOfProcessInstances(instanceUUIDs);
  }

  @Override
  public List<LightActivityInstance> getLightActivityInstances(final ProcessInstanceUUID instanceUUID,
      final int fromIdex, final int pageSize, final ActivityInstanceCriterion pagingCriterion,
      final Map<String, String> options) throws InstanceNotFoundException, RemoteException {
    return getAPI(options).getLightActivityInstances(instanceUUID, fromIdex, pageSize, pagingCriterion);
  }

  @Override
  public CatchingEvent getEvent(final CatchingEventUUID eventUUID, final Map<String, String> options)
      throws RemoteException, EventNotFoundException {
    return getAPI(options).getEvent(eventUUID);
  }

  @Override
  public Set<CatchingEvent> getEvents(final Map<String, String> options) throws RemoteException {
    return getAPI(options).getEvents();
  }

  @Override
  public Set<CatchingEvent> getEvents(final ProcessInstanceUUID instanceUUID, final Map<String, String> options)
      throws RemoteException {
    return getAPI(options).getEvents(instanceUUID);
  }

  @Override
  public Set<CatchingEvent> getEvents(final ActivityInstanceUUID activityUUID, final Map<String, String> options)
      throws RemoteException {
    return getAPI(options).getEvents(activityUUID);
  }

  @Override
  public byte[] getDocumentContent(final DocumentUUID documentUUID, final Map<String, String> options)
      throws RemoteException, DocumentNotFoundException {
    return getAPI(options).getDocumentContent(documentUUID);
  }

  @Override
  public DocumentResult searchDocuments(final DocumentSearchBuilder builder, final int fromResult,
      final int maxResults, final Map<String, String> options) throws RemoteException {
    return getAPI(options).searchDocuments(builder, fromResult, maxResults);
  }

  @Override
  public Document getDocument(final DocumentUUID documentUUID, final Map<String, String> options)
      throws RemoteException, DocumentNotFoundException {
    return getAPI(options).getDocument(documentUUID);
  }

  @Override
  public List<Document> getDocuments(final List<DocumentUUID> documentUUIDs, final Map<String, String> options)
      throws RemoteException, DocumentNotFoundException {
    return getAPI(options).getDocuments(documentUUIDs);
  }

  @Override
  public List<Document> getDocumentVersions(final DocumentUUID documentUUID, final Map<String, String> options)
      throws RemoteException, DocumentNotFoundException {
    return getAPI(options).getDocumentVersions(documentUUID);
  }

  @Override
  public Set<String> getInvolvedUsersOfProcessInstance(final ProcessInstanceUUID instanceUUID,
      final Map<String, String> options) throws RemoteException, InstanceNotFoundException {
    return getAPI(options).getInvolvedUsersOfProcessInstance(instanceUUID);
  }

  @Override
  public Set<ProcessInstanceUUID> getChildrenInstanceUUIDsOfProcessInstance(final ProcessInstanceUUID instanceUUID,
      final Map<String, String> options) throws RemoteException, InstanceNotFoundException {
    return getAPI(options).getChildrenInstanceUUIDsOfProcessInstance(instanceUUID);
  }

  @Override
  public Document getLastDocument(final ProcessInstanceUUID instanceUUID, final String documentName, final Date date, final Map<String, String> options) throws RemoteException {
    return getAPI(options).getLastDocument(instanceUUID, documentName, date);
  }

  @Override
  public Document getLastDocument(final ProcessInstanceUUID instanceUUID, final String documentName, final ActivityInstanceUUID activityUUID, final Map<String, String> options)
      throws ActivityNotFoundException, RemoteException {
    return getAPI(options).getLastDocument(instanceUUID, documentName, activityUUID);
  }
}
