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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ow2.bonita.facade.APIAccessor;
import org.ow2.bonita.facade.QueryDefinitionAPI;
import org.ow2.bonita.facade.def.element.AttachmentDefinition;
import org.ow2.bonita.facade.def.element.BusinessArchive;
import org.ow2.bonita.facade.def.majorElement.ActivityDefinition;
import org.ow2.bonita.facade.def.majorElement.DataFieldDefinition;
import org.ow2.bonita.facade.def.majorElement.ParticipantDefinition;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition.ProcessState;
import org.ow2.bonita.facade.exception.ActivityDefNotFoundException;
import org.ow2.bonita.facade.exception.ActivityNotFoundException;
import org.ow2.bonita.facade.exception.DataFieldNotFoundException;
import org.ow2.bonita.facade.exception.ParticipantNotFoundException;
import org.ow2.bonita.facade.exception.ProcessNotFoundException;
import org.ow2.bonita.facade.internal.RemoteQueryDefinitionAPI;
import org.ow2.bonita.facade.paging.ProcessDefinitionCriterion;
import org.ow2.bonita.facade.runtime.InitialAttachment;
import org.ow2.bonita.facade.uuid.ActivityDefinitionUUID;
import org.ow2.bonita.facade.uuid.ParticipantDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.identity.auth.DomainOwner;
import org.ow2.bonita.identity.auth.PasswordOwner;
import org.ow2.bonita.identity.auth.RESTUserOwner;
import org.ow2.bonita.identity.auth.UserOwner;
import org.ow2.bonita.light.LightProcessDefinition;

/**
 * 
 * @author Elias Ricken de Medeiros, Matthieu Chaffotte
 * 
 */
public class RemoteQueryDefinitionAPIImpl implements RemoteQueryDefinitionAPI {

  protected Map<String, QueryDefinitionAPI> apis = new HashMap<String, QueryDefinitionAPI>();

  protected QueryDefinitionAPI getAPI(final Map<String, String> options) {
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
      apis.put(queryList, new StandardAPIAccessorImpl().getQueryDefinitionAPI(queryList));
    }
    return apis.get(queryList);
  }

  @Override
  public int getNumberOfProcesses(final Map<String, String> options) throws RemoteException {
    return getAPI(options).getNumberOfProcesses();
  }

  @Override
  public ProcessDefinition getProcess(final String processId, final String version, final Map<String, String> options)
      throws ProcessNotFoundException, RemoteException {
    return getAPI(options).getProcess(processId, version);
  }

  @Override
  public ProcessDefinition getLastProcess(final String processId, final Map<String, String> options)
      throws ProcessNotFoundException, RemoteException {
    return getAPI(options).getLastProcess(processId);
  }

  @Override
  public LightProcessDefinition getLastLightProcess(final String processId, final Map<String, String> options)
      throws ProcessNotFoundException, RemoteException {
    return getAPI(options).getLastLightProcess(processId);
  }

  @Override
  public List<LightProcessDefinition> getLightProcesses(final int fromIndex, final int pageSize,
      final Map<String, String> options) throws RemoteException {
    return getAPI(options).getLightProcesses(fromIndex, pageSize);
  }

  @Override
  public List<LightProcessDefinition> getLightProcesses(final int fromIndex, final int pageSize,
      final ProcessDefinitionCriterion pagingCriterion, final Map<String, String> options) throws RemoteException {
    return getAPI(options).getLightProcesses(fromIndex, pageSize, pagingCriterion);
  }

  @Override
  public BusinessArchive getBusinessArchive(final ProcessDefinitionUUID processDefinitionUUID,
      final Map<String, String> options) throws ProcessNotFoundException, RemoteException {
    return getAPI(options).getBusinessArchive(processDefinitionUUID);
  }

  @Override
  public ProcessDefinition getProcess(final ProcessDefinitionUUID processDefinitionUUID,
      final Map<String, String> options) throws ProcessNotFoundException, RemoteException {
    return getAPI(options).getProcess(processDefinitionUUID);
  }

  @Override
  public List<ProcessDefinition> getProcesses(final int fromIndex, final int pageSize, final Map<String, String> options)
      throws RemoteException {
    return getAPI(options).getProcesses(fromIndex, pageSize);
  }

  @Override
  public Set<ActivityDefinition> getProcessActivities(final ProcessDefinitionUUID processDefinitionUUID,
      final Map<String, String> options) throws ProcessNotFoundException, RemoteException {
    return getAPI(options).getProcessActivities(processDefinitionUUID);
  }

  @Override
  public ActivityDefinition getProcessActivity(final ProcessDefinitionUUID processDefinitionUUID,
      final String activityId, final Map<String, String> options) throws ProcessNotFoundException,
      ActivityNotFoundException, RemoteException {
    return getAPI(options).getProcessActivity(processDefinitionUUID, activityId);
  }

  @Override
  public ActivityDefinitionUUID getProcessActivityId(final ProcessDefinitionUUID processDefinitionUUID,
      final String activityName, final Map<String, String> options) throws ProcessNotFoundException, RemoteException {
    return getAPI(options).getProcessActivityId(processDefinitionUUID, activityName);
  }

  @Override
  public ParticipantDefinition getProcessParticipant(final ProcessDefinitionUUID processDefinitionUUID,
      final String participantId, final Map<String, String> options) throws ProcessNotFoundException,
      ParticipantNotFoundException, RemoteException {
    return getAPI(options).getProcessParticipant(processDefinitionUUID, participantId);
  }

  @Override
  public ParticipantDefinitionUUID getProcessParticipantId(final ProcessDefinitionUUID processDefinitionUUID,
      final String participantName, final Map<String, String> options) throws ProcessNotFoundException, RemoteException {
    return getAPI(options).getProcessParticipantId(processDefinitionUUID, participantName);
  }

  @Override
  public Set<ParticipantDefinition> getProcessParticipants(final ProcessDefinitionUUID processDefinitionUUID,
      final Map<String, String> options) throws ProcessNotFoundException, RemoteException {
    return getAPI(options).getProcessParticipants(processDefinitionUUID);
  }

  @Override
  public Set<ProcessDefinition> getProcesses(final Map<String, String> options) throws RemoteException {
    return getAPI(options).getProcesses();
  }

  @Override
  public Set<ProcessDefinition> getProcesses(final String processId, final Map<String, String> options)
      throws RemoteException {
    return getAPI(options).getProcesses(processId);
  }

  @Override
  public Set<ProcessDefinition> getProcesses(final ProcessDefinition.ProcessState processState,
      final Map<String, String> options) throws RemoteException {
    return getAPI(options).getProcesses(processState);
  }

  @Override
  public Set<ProcessDefinition> getProcesses(final String processId, final ProcessDefinition.ProcessState processState,
      final Map<String, String> options) throws RemoteException {
    return getAPI(options).getProcesses(processId, processState);
  }

  @Override
  public DataFieldDefinition getProcessDataField(final ProcessDefinitionUUID processDefinitionUUID,
      final String dataFieldId, final Map<String, String> options) throws ProcessNotFoundException,
      DataFieldNotFoundException, RemoteException {
    return getAPI(options).getProcessDataField(processDefinitionUUID, dataFieldId);
  }

  @Override
  public Set<DataFieldDefinition> getProcessDataFields(final ProcessDefinitionUUID processDefinitionUUID,
      final Map<String, String> options) throws ProcessNotFoundException, RemoteException {
    return getAPI(options).getProcessDataFields(processDefinitionUUID);
  }

  @Override
  public Set<DataFieldDefinition> getActivityDataFields(final ActivityDefinitionUUID activityDefinitionUUID,
      final Map<String, String> options) throws ActivityDefNotFoundException, RemoteException {
    return getAPI(options).getActivityDataFields(activityDefinitionUUID);
  }

  @Override
  public DataFieldDefinition getActivityDataField(final ActivityDefinitionUUID activityDefinitionUUID,
      final String dataFieldId, final Map<String, String> options) throws ActivityDefNotFoundException,
      DataFieldNotFoundException, RemoteException {
    return getAPI(options).getActivityDataField(activityDefinitionUUID, dataFieldId);
  }

  @Override
  public String getProcessMetaData(final ProcessDefinitionUUID uuid, final String key, final Map<String, String> options)
      throws RemoteException, ProcessNotFoundException {
    return getAPI(options).getProcessMetaData(uuid, key);
  }

  @Override
  public InitialAttachment getProcessAttachment(final ProcessDefinitionUUID processUUID, final String attachmentName,
      final Map<String, String> options) throws ProcessNotFoundException, RemoteException {
    return getAPI(options).getProcessAttachment(processUUID, attachmentName);
  }

  @Override
  public Set<InitialAttachment> getProcessAttachments(final ProcessDefinitionUUID processUUID,
      final Map<String, String> options) throws ProcessNotFoundException, RemoteException {
    return getAPI(options).getProcessAttachments(processUUID);
  }

  @Override
  public AttachmentDefinition getAttachmentDefinition(final ProcessDefinitionUUID processUUID,
      final String attachmentName, final Map<String, String> options) throws ProcessNotFoundException, RemoteException {
    return getAPI(options).getAttachmentDefinition(processUUID, attachmentName);
  }

  @Override
  public Set<AttachmentDefinition> getAttachmentDefinitions(final ProcessDefinitionUUID processUUID,
      final Map<String, String> options) throws ProcessNotFoundException, RemoteException {
    return getAPI(options).getAttachmentDefinitions(processUUID);
  }

  @Override
  public Set<LightProcessDefinition> getLightProcesses(final ProcessState processState,
      final Map<String, String> options) throws RemoteException {
    return getAPI(options).getLightProcesses(processState);
  }

  @Override
  public Set<LightProcessDefinition> getLightProcesses(final Map<String, String> options) throws RemoteException {
    return getAPI(options).getLightProcesses();
  }

  @Override
  public LightProcessDefinition getLightProcess(final ProcessDefinitionUUID processDefinitionUUID,
      final Map<String, String> options) throws RemoteException, ProcessNotFoundException {
    return getAPI(options).getLightProcess(processDefinitionUUID);
  }

  @Override
  public byte[] getResource(final ProcessDefinitionUUID definitionUUID, final String resourcePath,
      final Map<String, String> options) throws ProcessNotFoundException, RemoteException {
    return getAPI(options).getResource(definitionUUID, resourcePath);
  }

  @Override
  public Set<LightProcessDefinition> getLightProcesses(final Set<ProcessDefinitionUUID> processUUIDs,
      final Map<String, String> options) throws ProcessNotFoundException, RemoteException {
    return getAPI(options).getLightProcesses(processUUIDs);
  }

  @Override
  public List<LightProcessDefinition> getLightProcesses(final Set<ProcessDefinitionUUID> processUUIDs,
      final int fromIndex, final int pageSize, final ProcessDefinitionCriterion pagingCriterion,
      final Map<String, String> options) throws ProcessNotFoundException, RemoteException {
    return getAPI(options).getLightProcesses(processUUIDs, fromIndex, pageSize, pagingCriterion);
  }

  @Override
  public List<LightProcessDefinition> getAllLightProcessesExcept(final Set<ProcessDefinitionUUID> processUUIDs,
      final int fromIndex, final int pageSize, final Map<String, String> options) throws RemoteException {
    return getAPI(options).getAllLightProcessesExcept(processUUIDs, fromIndex, pageSize);
  }

  @Override
  public List<LightProcessDefinition> getAllLightProcessesExcept(final Set<ProcessDefinitionUUID> processUUIDs,
      final int fromIndex, final int pageSize, final ProcessDefinitionCriterion pagingCriterion,
      final Map<String, String> options) throws RemoteException {
    return getAPI(options).getAllLightProcessesExcept(processUUIDs, fromIndex, pageSize, pagingCriterion);
  }

  @Override
  public Set<ProcessDefinitionUUID> getProcessUUIDs(final String category, final Map<String, String> options)
      throws RemoteException {
    return getAPI(options).getProcessUUIDs(category);
  }

  @Override
  public Set<ActivityDefinitionUUID> getProcessTaskUUIDs(final ProcessDefinitionUUID processsUUID,
      final Map<String, String> options) throws ProcessNotFoundException, RemoteException {
    return getAPI(options).getProcessTaskUUIDs(processsUUID);
  }

  @Override
  public Date getMigrationDate(final ProcessDefinitionUUID processUUID, final Map<String, String> options)
      throws ProcessNotFoundException, RemoteException {
    return getAPI(options).getMigrationDate(processUUID);
  }

}
