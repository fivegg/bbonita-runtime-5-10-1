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
 * Modified by Charles Souillard, Matthieu Chaffotte, Nicolas Chabanoles, Elias Ricken de Medeiros
 *  - BonitaSoft S.A.
 **/
package org.ow2.bonita.facade.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.ow2.bonita.facade.QueryRuntimeAPI;
import org.ow2.bonita.facade.def.InternalProcessDefinition;
import org.ow2.bonita.facade.exception.ActivityNotFoundException;
import org.ow2.bonita.facade.exception.DocumentNotFoundException;
import org.ow2.bonita.facade.exception.EventNotFoundException;
import org.ow2.bonita.facade.exception.InstanceNotFoundException;
import org.ow2.bonita.facade.exception.TaskNotFoundException;
import org.ow2.bonita.facade.exception.VariableNotFoundException;
import org.ow2.bonita.facade.identity.impl.GroupImpl;
import org.ow2.bonita.facade.identity.impl.RoleImpl;
import org.ow2.bonita.facade.identity.impl.UserImpl;
import org.ow2.bonita.facade.paging.ActivityInstanceCriterion;
import org.ow2.bonita.facade.paging.ProcessInstanceCriterion;
import org.ow2.bonita.facade.privilege.Rule.RuleType;
import org.ow2.bonita.facade.runtime.ActivityInstance;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.AttachmentInstance;
import org.ow2.bonita.facade.runtime.CatchingEvent;
import org.ow2.bonita.facade.runtime.CatchingEvent.Position;
import org.ow2.bonita.facade.runtime.CatchingEvent.Type;
import org.ow2.bonita.facade.runtime.Comment;
import org.ow2.bonita.facade.runtime.Document;
import org.ow2.bonita.facade.runtime.InstanceState;
import org.ow2.bonita.facade.runtime.ProcessInstance;
import org.ow2.bonita.facade.runtime.TaskInstance;
import org.ow2.bonita.facade.runtime.VariableUpdate;
import org.ow2.bonita.facade.runtime.impl.ActivityInstanceImpl;
import org.ow2.bonita.facade.runtime.impl.AttachmentInstanceImpl;
import org.ow2.bonita.facade.runtime.impl.CaseImpl;
import org.ow2.bonita.facade.runtime.impl.CatchingEventImpl;
import org.ow2.bonita.facade.runtime.impl.InternalActivityInstance;
import org.ow2.bonita.facade.runtime.impl.InternalProcessInstance;
import org.ow2.bonita.facade.runtime.impl.ProcessInstanceImpl;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.CatchingEventUUID;
import org.ow2.bonita.facade.uuid.DocumentUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.light.LightActivityInstance;
import org.ow2.bonita.light.LightProcessDefinition;
import org.ow2.bonita.light.LightProcessInstance;
import org.ow2.bonita.light.LightTaskInstance;
import org.ow2.bonita.light.impl.LightActivityInstanceImpl;
import org.ow2.bonita.light.impl.LightProcessDefinitionImpl;
import org.ow2.bonita.light.impl.LightProcessInstanceImpl;
import org.ow2.bonita.runtime.event.EventConstants;
import org.ow2.bonita.runtime.event.Job;
import org.ow2.bonita.runtime.model.Execution;
import org.ow2.bonita.search.DocumentResult;
import org.ow2.bonita.search.DocumentSearchBuilder;
import org.ow2.bonita.search.SearchQueryBuilder;
import org.ow2.bonita.search.index.Index;
import org.ow2.bonita.services.DocumentationManager;
import org.ow2.bonita.services.EventService;
import org.ow2.bonita.services.Querier;
import org.ow2.bonita.util.BonitaRuntimeException;
import org.ow2.bonita.util.CopyTool;
import org.ow2.bonita.util.DateUtil;
import org.ow2.bonita.util.DocumentService;
import org.ow2.bonita.util.EnvTool;
import org.ow2.bonita.util.ExceptionManager;
import org.ow2.bonita.util.Misc;
import org.ow2.bonita.util.TransientData;
import org.xml.sax.SAXException;

/**
 * @author Pierre Vigneras
 */
public class QueryRuntimeAPIImpl implements QueryRuntimeAPI {

  private final String queryList;

  protected QueryRuntimeAPIImpl(final String queryList) {
    this.queryList = queryList;
  }

  private String getQueryList() {
    return queryList;
  }

  @Override
  public int getNumberOfParentProcessInstances() {
    final boolean access = EnvTool.isRestrictedApplicationAcces();
    int count = 0;
    if (access) {
      final String applicationName = EnvTool.getApplicationAccessName();
      final Set<ProcessDefinitionUUID> visibleProcessUUIDs = FacadeUtil.getAllowedProcessUUIDsFor(applicationName,
          RuleType.PROCESS_READ);
      if (visibleProcessUUIDs != null) {
        count = EnvTool.getAllQueriers(getQueryList()).getNumberOfParentProcessInstances(visibleProcessUUIDs);
      }
    } else {
      count = EnvTool.getAllQueriers(getQueryList()).getNumberOfParentProcessInstances();
    }
    return count;
  }

  @Override
  public int getNumberOfParentProcessInstances(final Set<ProcessDefinitionUUID> processDefinitionUUIDs) {
    if (processDefinitionUUIDs == null || processDefinitionUUIDs.isEmpty()) {
      return 0;
    }

    int count = 0;
    final boolean access = EnvTool.isRestrictedApplicationAcces();
    if (access) {
      final String applicationName = EnvTool.getApplicationAccessName();
      if (applicationName != null) {
        Set<ProcessDefinitionUUID> visibleProcessUUIDs = FacadeUtil.getAllowedProcessUUIDsFor(applicationName,
            RuleType.PROCESS_READ);
        if (visibleProcessUUIDs == null || visibleProcessUUIDs.isEmpty()) {
          visibleProcessUUIDs = new HashSet<ProcessDefinitionUUID>();
        }
        visibleProcessUUIDs.retainAll(processDefinitionUUIDs);
        if (visibleProcessUUIDs != null && !visibleProcessUUIDs.isEmpty()) {
          count = EnvTool.getAllQueriers(getQueryList()).getNumberOfParentProcessInstances(visibleProcessUUIDs);
        }
      }
    } else {
      count = EnvTool.getAllQueriers(getQueryList()).getNumberOfParentProcessInstances(processDefinitionUUIDs);
    }
    return count;
  }

  @Override
  public int getNumberOfParentProcessInstancesExcept(final Set<ProcessDefinitionUUID> exceptions) {
    if (exceptions == null || exceptions.isEmpty()) {
      return getNumberOfParentProcessInstances();
    }

    int count = 0;
    final boolean access = EnvTool.isRestrictedApplicationAcces();
    if (access) {
      final String applicationName = EnvTool.getApplicationAccessName();
      if (applicationName != null) {
        Set<ProcessDefinitionUUID> visibleProcessUUIDs = FacadeUtil.getAllowedProcessUUIDsFor(applicationName,
            RuleType.PROCESS_READ);
        if (visibleProcessUUIDs == null || visibleProcessUUIDs.isEmpty()) {
          visibleProcessUUIDs = new HashSet<ProcessDefinitionUUID>();
        }
        visibleProcessUUIDs.removeAll(exceptions);
        if (visibleProcessUUIDs != null && !visibleProcessUUIDs.isEmpty()) {
          count = EnvTool.getAllQueriers(getQueryList()).getNumberOfParentProcessInstances(visibleProcessUUIDs);
        }
      }
    } else {
      final Set<ProcessDefinitionUUID> visibleProcessUUIDs = EnvTool.getAllQueriers(getQueryList())
          .getAllProcessDefinitionUUIDsExcept(exceptions);
      if (visibleProcessUUIDs != null && !visibleProcessUUIDs.isEmpty()) {
        count = EnvTool.getAllQueriers(getQueryList()).getNumberOfParentProcessInstances(visibleProcessUUIDs);
      }
    }
    return count;
  }

  @Override
  public int getNumberOfProcessInstances() {
    final boolean access = EnvTool.isRestrictedApplicationAcces();
    int count = 0;
    if (access) {
      final String applicationName = EnvTool.getApplicationAccessName();
      final Set<ProcessDefinitionUUID> visibleProcessUUIDs = FacadeUtil.getAllowedProcessUUIDsFor(applicationName,
          RuleType.PROCESS_READ);
      if (visibleProcessUUIDs != null && !visibleProcessUUIDs.isEmpty()) {
        count = EnvTool.getAllQueriers(getQueryList()).getNumberOfProcessInstances(visibleProcessUUIDs);
      }
    } else {
      count = EnvTool.getAllQueriers(getQueryList()).getNumberOfProcessInstances();
    }
    return count;
  }

  @Override
  public ProcessInstance getProcessInstance(final ProcessInstanceUUID instanceUUID) throws InstanceNotFoundException {
    final InternalProcessInstance instance = getInternalProcessInstanceWithAttachments(instanceUUID);
    if (instance == null) {
      throw new InstanceNotFoundException("bai_QRAPII_1", instanceUUID);
    }
    return new ProcessInstanceImpl(instance);
  }

  @Override
  public LightProcessInstance getLightProcessInstance(final ProcessInstanceUUID instanceUUID)
      throws InstanceNotFoundException {
    final ProcessInstance result = getInternalProcessInstanceWithoutAttachements(instanceUUID);
    if (result == null) {
      throw new InstanceNotFoundException("bai_QRAPII_1", instanceUUID);
    }
    return new LightProcessInstanceImpl(result);
  }

  private InternalProcessInstance getInternalProcessInstanceWithAttachments(final ProcessInstanceUUID instanceUUID)
      throws InstanceNotFoundException {
    final InternalProcessInstance internalProcessInstance = getInternalProcessInstanceWithoutAttachements(instanceUUID);
    if (internalProcessInstance != null && internalProcessInstance.getNbOfAttachments() > 0) {
      bindAttachementsToInternalProcessInstance(internalProcessInstance);
    }
    return internalProcessInstance;
  }

  private void bindAttachementsToInternalProcessInstance(final InternalProcessInstance internalProcessInstance) {
    if (internalProcessInstance != null) {
      final int nbOfAttachments = internalProcessInstance.getNbOfAttachments();
      if (nbOfAttachments > 0) {
        final DocumentationManager manager = EnvTool.getDocumentationManager();
        final List<AttachmentInstance> allAttachmentVersions = DocumentService.getAllAttachmentVersions(manager,
            internalProcessInstance.getProcessInstanceUUID());
        for (final AttachmentInstance attachmentInstance : allAttachmentVersions) {
          internalProcessInstance.addAttachment(attachmentInstance);
        }
      }
    }
  }

  private InternalProcessInstance getInternalProcessInstanceWithoutAttachements(final ProcessInstanceUUID instanceUUID) {
    FacadeUtil.checkArgsNotNull(instanceUUID);
    final Querier querier = EnvTool.getAllQueriers(getQueryList());
    return querier.getProcessInstance(instanceUUID);
  }

  @Override
  public Set<ProcessInstance> getProcessInstances() {
    final Set<InternalProcessInstance> processes = getInternalProcessInstancesWithAttachements();
    final Set<ProcessInstance> result = new HashSet<ProcessInstance>();
    for (final ProcessInstance record : processes) {
      result.add(new ProcessInstanceImpl(record));
    }
    return result;
  }

  @Override
  public Set<LightProcessInstance> getLightProcessInstances() {
    final Set<InternalProcessInstance> processes = getInternalProcessInstancesWithoutAttachements();
    final Set<LightProcessInstance> result = new HashSet<LightProcessInstance>();
    for (final ProcessInstance record : processes) {
      result.add(new LightProcessInstanceImpl(record));
    }
    return result;
  }

  private Set<InternalProcessInstance> getInternalProcessInstancesWithoutAttachements() {
    final boolean access = EnvTool.isRestrictedApplicationAcces();
    Set<InternalProcessInstance> processes = new HashSet<InternalProcessInstance>();
    if (access) {
      final String applicationName = EnvTool.getApplicationAccessName();
      if (applicationName != null) {
        final Set<ProcessDefinitionUUID> visibleProcessUUIDs = FacadeUtil.getAllowedProcessUUIDsFor(applicationName,
            RuleType.PROCESS_READ);
        if (visibleProcessUUIDs != null && !visibleProcessUUIDs.isEmpty()) {
          processes = EnvTool.getAllQueriers(getQueryList()).getProcessInstances(visibleProcessUUIDs);
        }
      }
    } else {
      processes = EnvTool.getAllQueriers(getQueryList()).getProcessInstances();
    }
    return processes;
  }

  private Set<InternalProcessInstance> getInternalProcessInstancesWithAttachements() {
    final Set<InternalProcessInstance> processes = getInternalProcessInstancesWithoutAttachements();
    bindAttachementsToInternalProcessInstances(processes);
    return processes;
  }

  private void bindAttachementsToInternalProcessInstances(final Set<InternalProcessInstance> processes) {
    for (final InternalProcessInstance internalProcessInstance : processes) {
      bindAttachementsToInternalProcessInstance(internalProcessInstance);
    }
  }

  @Override
  public List<LightProcessInstance> getLightProcessInstances(final int fromIndex, final int pageSize) {
    final List<LightProcessInstance> result = new ArrayList<LightProcessInstance>();
    if (pageSize <= 0) {
      return result;
    }
    final boolean access = EnvTool.isRestrictedApplicationAcces();
    List<InternalProcessInstance> processes = new ArrayList<InternalProcessInstance>();
    if (access) {
      final String applicationName = EnvTool.getApplicationAccessName();
      if (applicationName != null) {
        final Set<ProcessDefinitionUUID> visibleProcessUUIDs = FacadeUtil.getAllowedProcessUUIDsFor(applicationName,
            RuleType.PROCESS_READ);
        if (visibleProcessUUIDs != null && !visibleProcessUUIDs.isEmpty()) {
          processes = EnvTool.getAllQueriers(getQueryList()).getProcessInstances(visibleProcessUUIDs, fromIndex,
              pageSize);
        }
      }
    } else {
      processes = EnvTool.getAllQueriers(getQueryList()).getProcessInstances(fromIndex, pageSize);
    }

    for (final ProcessInstance record : processes) {
      result.add(new LightProcessInstanceImpl(record));
    }
    return result;
  }

  @Override
  public List<LightProcessInstance> getLightProcessInstances(final int fromIndex, final int pageSize,
      final ProcessInstanceCriterion pagingCriterion) {
    final List<LightProcessInstance> result = new ArrayList<LightProcessInstance>();
    if (pageSize <= 0) {
      return result;
    }
    final boolean access = EnvTool.isRestrictedApplicationAcces();
    List<InternalProcessInstance> processes = new ArrayList<InternalProcessInstance>();
    if (access) {
      final String applicationName = EnvTool.getApplicationAccessName();
      if (applicationName != null) {
        final Set<ProcessDefinitionUUID> visibleProcessUUIDs = FacadeUtil.getAllowedProcessUUIDsFor(applicationName,
            RuleType.PROCESS_READ);
        if (visibleProcessUUIDs != null && !visibleProcessUUIDs.isEmpty()) {
          processes = EnvTool.getAllQueriers(getQueryList()).getProcessInstances(visibleProcessUUIDs, fromIndex,
              pageSize, pagingCriterion);
        }
      }
    } else {
      processes = EnvTool.getAllQueriers(getQueryList()).getProcessInstances(fromIndex, pageSize, pagingCriterion);
    }

    for (final ProcessInstance record : processes) {
      result.add(new LightProcessInstanceImpl(record));
    }
    return result;
  }

  @Override
  public List<LightProcessInstance> getLightParentProcessInstances(final int fromIndex, final int pageSize) {
    final List<LightProcessInstance> result = new ArrayList<LightProcessInstance>();
    if (pageSize <= 0) {
      return result;
    }
    final List<InternalProcessInstance> records = EnvTool.getAllQueriers(getQueryList()).getParentProcessInstances(
        fromIndex, pageSize);
    for (final ProcessInstance record : records) {
      result.add(new LightProcessInstanceImpl(record));
    }
    return result;
  }

  @Override
  public List<LightProcessInstance> getLightParentProcessInstances(final Set<ProcessDefinitionUUID> processUUIDs,
      final int fromIndex, final int pageSize, final ProcessInstanceCriterion pagingCriterion) {
    Misc.checkArgsNotNull(processUUIDs);
    if (processUUIDs.isEmpty()) {
      return Collections.emptyList();
    }
    final List<InternalProcessInstance> processes = getParentProcessInstances(processUUIDs, fromIndex, pageSize,
        pagingCriterion);

    final List<LightProcessInstance> result = new ArrayList<LightProcessInstance>();
    for (final ProcessInstance record : processes) {
      result.add(new LightProcessInstanceImpl(record));
    }
    return result;
  }

  private List<InternalProcessInstance> getParentProcessInstances(final Set<ProcessDefinitionUUID> processUUIDs,
      final int startingIndex, final int pageSize, final ProcessInstanceCriterion pagingCriterion) {
    final boolean access = EnvTool.isRestrictedApplicationAcces();
    List<InternalProcessInstance> processes = new ArrayList<InternalProcessInstance>();
    if (access) {
      final String applicationName = EnvTool.getApplicationAccessName();
      if (applicationName != null) {
        Set<ProcessDefinitionUUID> visibleProcessUUIDs = FacadeUtil.getAllowedProcessUUIDsFor(applicationName,
            RuleType.PROCESS_READ);
        if (visibleProcessUUIDs == null || visibleProcessUUIDs.isEmpty()) {
          visibleProcessUUIDs = new HashSet<ProcessDefinitionUUID>();
        }
        visibleProcessUUIDs.retainAll(processUUIDs);
        if (visibleProcessUUIDs != null && !visibleProcessUUIDs.isEmpty()) {
          processes = EnvTool.getAllQueriers(getQueryList()).getParentProcessInstances(visibleProcessUUIDs,
              startingIndex, pageSize, pagingCriterion);
        }
      }
    } else {
      processes = EnvTool.getAllQueriers(getQueryList()).getParentProcessInstances(processUUIDs, startingIndex,
          pageSize, pagingCriterion);
    }
    return processes;
  }

  @Override
  public List<LightProcessInstance> getLightParentProcessInstancesExcept(final Set<ProcessDefinitionUUID> exceptions,
      final int fromIndex, final int pageSize, final ProcessInstanceCriterion pagingCriterion) {
    if (exceptions == null || exceptions.isEmpty()) {
      return getLightParentProcessInstances(fromIndex, pageSize, pagingCriterion);
    }

    final boolean access = EnvTool.isRestrictedApplicationAcces();
    List<InternalProcessInstance> processes = new ArrayList<InternalProcessInstance>();
    if (access) {
      final String applicationName = EnvTool.getApplicationAccessName();
      if (applicationName != null) {
        Set<ProcessDefinitionUUID> visibleProcessUUIDs = FacadeUtil.getAllowedProcessUUIDsFor(applicationName,
            RuleType.PROCESS_READ);
        if (visibleProcessUUIDs == null || visibleProcessUUIDs.isEmpty()) {
          visibleProcessUUIDs = new HashSet<ProcessDefinitionUUID>();
        }
        visibleProcessUUIDs.removeAll(exceptions);
        if (visibleProcessUUIDs != null && !visibleProcessUUIDs.isEmpty()) {
          processes = EnvTool.getAllQueriers(getQueryList()).getParentProcessInstances(visibleProcessUUIDs, fromIndex,
              pageSize, pagingCriterion);
        }
      }
    } else {
      processes = EnvTool.getAllQueriers(getQueryList()).getParentProcessInstancesExcept(exceptions, fromIndex,
          pageSize, pagingCriterion);
    }

    final List<LightProcessInstance> result = new ArrayList<LightProcessInstance>();
    for (final ProcessInstance record : processes) {
      result.add(new LightProcessInstanceImpl(record));
    }
    return result;
  }

  @Override
  public List<LightProcessInstance> getLightParentProcessInstances(final int fromIndex, final int pageSize,
      final ProcessInstanceCriterion pagingCriterion) {
    final List<LightProcessInstance> result = new ArrayList<LightProcessInstance>();
    if (pageSize <= 0) {
      return result;
    }
    final List<InternalProcessInstance> records = EnvTool.getAllQueriers(getQueryList()).getParentProcessInstances(
        fromIndex, pageSize, pagingCriterion);
    for (final ProcessInstance record : records) {
      result.add(new LightProcessInstanceImpl(record));
    }
    return result;
  }

  @Override
  public Set<ProcessInstance> getUserInstances() {
    final Set<InternalProcessInstance> processes = getUserProcessInstances();
    // bind attachements from document service
    bindAttachementsToInternalProcessInstances(processes);

    final Set<ProcessInstance> result = new HashSet<ProcessInstance>();
    for (final ProcessInstance record : processes) {
      result.add(new ProcessInstanceImpl(record));
    }
    return result;
  }

  @Override
  public Set<LightProcessInstance> getLightUserInstances() {
    final Set<InternalProcessInstance> processes = getUserProcessInstances();
    final Set<LightProcessInstance> result = new HashSet<LightProcessInstance>();
    for (final ProcessInstance record : processes) {
      result.add(new LightProcessInstanceImpl(record));
    }
    return result;
  }

  @Override
  public List<LightProcessInstance> getLightParentUserInstances(final int startingIndex, final int pageSize) {
    final List<InternalProcessInstance> processes = getUserParentProcessInstances(startingIndex, pageSize);
    final List<LightProcessInstance> result = new ArrayList<LightProcessInstance>();
    for (final ProcessInstance record : processes) {
      result.add(new LightProcessInstanceImpl(record));
    }
    return result;
  }

  @Override
  public List<LightProcessInstance> getLightParentUserInstances(final int fromIndex, final int pageSize,
      final ProcessInstanceCriterion pagingCriterion) {
    final List<InternalProcessInstance> processes = getUserParentProcessInstances(fromIndex, pageSize, pagingCriterion);
    final List<LightProcessInstance> result = new ArrayList<LightProcessInstance>();
    for (final ProcessInstance record : processes) {
      result.add(new LightProcessInstanceImpl(record));
    }
    return result;
  }

  @Override
  public List<LightProcessInstance> getLightParentProcessInstancesWithActiveUser(final String userId,
      final int fromIndex, final int pageSize) {
    final List<InternalProcessInstance> processes = getParentProcessInstancesWithActiveUser(userId, fromIndex, pageSize);
    final List<LightProcessInstance> result = new ArrayList<LightProcessInstance>();
    for (final ProcessInstance record : processes) {
      result.add(new LightProcessInstanceImpl(record));
    }
    return result;
  }

  @Override
  public List<LightProcessInstance> getLightParentProcessInstancesWithActiveUser(final String userId,
      final int fromIndex, final int pageSize, final ProcessInstanceCriterion pagingCriterion) {
    final List<InternalProcessInstance> processes = getParentProcessInstancesWithActiveUser(userId, fromIndex,
        pageSize, pagingCriterion);
    final List<LightProcessInstance> result = new ArrayList<LightProcessInstance>();
    for (final ProcessInstance record : processes) {
      result.add(new LightProcessInstanceImpl(record));
    }
    return result;
  }

  @Override
  public List<LightProcessInstance> getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(
      final String userId, final int remainingDays, final int fromIndex, final int pageSize) {
    final List<InternalProcessInstance> processes = getParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(
        userId, remainingDays, fromIndex, pageSize);
    final List<LightProcessInstance> result = new ArrayList<LightProcessInstance>();
    for (final ProcessInstance record : processes) {
      result.add(new LightProcessInstanceImpl(record));
    }
    return result;
  }

  @Override
  public List<LightProcessInstance> getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(
      final String userId, final int remainingDays, final int fromIndex, final int pageSize,
      final ProcessInstanceCriterion pagingCriterion) {
    final List<InternalProcessInstance> processes = getParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(
        userId, remainingDays, fromIndex, pageSize, pagingCriterion);
    final List<LightProcessInstance> result = new ArrayList<LightProcessInstance>();
    for (final ProcessInstance record : processes) {
      result.add(new LightProcessInstanceImpl(record));
    }
    return result;
  }

  @Override
  public List<LightProcessInstance> getLightParentProcessInstancesWithOverdueTasks(final String userId,
      final int fromIndex, final int pageSize) {
    final List<InternalProcessInstance> processes = getParentProcessInstancesWithOverdueTasks(userId, fromIndex,
        pageSize, ProcessInstanceCriterion.DEFAULT);
    final List<LightProcessInstance> result = new ArrayList<LightProcessInstance>();
    for (final ProcessInstance record : processes) {
      result.add(new LightProcessInstanceImpl(record));
    }
    return result;
  }

  @Override
  public List<LightProcessInstance> getLightParentProcessInstancesWithOverdueTasks(final String userId,
      final int fromIndex, final int pageSize, final ProcessInstanceCriterion pagingCriterion) {
    final List<InternalProcessInstance> processes = getParentProcessInstancesWithOverdueTasks(userId, fromIndex,
        pageSize, pagingCriterion);
    final List<LightProcessInstance> result = new ArrayList<LightProcessInstance>();
    for (final ProcessInstance record : processes) {
      result.add(new LightProcessInstanceImpl(record));
    }
    return result;
  }

  private List<InternalProcessInstance> getParentProcessInstancesWithOverdueTasks(final String userId,
      final int fromIndex, final int pageSize, final ProcessInstanceCriterion pagingCriterion) {
    final boolean access = EnvTool.isRestrictedApplicationAcces();
    List<InternalProcessInstance> processes = new ArrayList<InternalProcessInstance>();
    final Date currentDate = new Date();
    if (access) {
      final String applicationName = EnvTool.getApplicationAccessName();
      if (applicationName != null) {
        final Set<ProcessDefinitionUUID> visibleProcessUUIDs = FacadeUtil.getAllowedProcessUUIDsFor(applicationName,
            RuleType.PROCESS_READ);
        if (visibleProcessUUIDs != null && !visibleProcessUUIDs.isEmpty()) {
          processes = EnvTool.getAllQueriers(getQueryList()).getParentProcessInstancesWithOverdueTasks(userId,
              currentDate, fromIndex, pageSize, visibleProcessUUIDs, pagingCriterion);
        }
      }
    } else {
      processes = EnvTool.getAllQueriers(getQueryList()).getParentProcessInstancesWithOverdueTasks(userId, currentDate,
          fromIndex, pageSize, pagingCriterion);
    }
    return processes;
  }

  private List<InternalProcessInstance> getParentProcessInstancesWithOverdueTasks(final String userId,
      final int fromIndex, final int pageSize, final Set<ProcessDefinitionUUID> processUUIDs,
      final ProcessInstanceCriterion pagingCriterion) {
    final boolean access = EnvTool.isRestrictedApplicationAcces();
    List<InternalProcessInstance> processes = new ArrayList<InternalProcessInstance>();
    final Date currentDate = new Date();
    if (access) {
      final String applicationName = EnvTool.getApplicationAccessName();
      if (applicationName != null) {
        Set<ProcessDefinitionUUID> visibleProcessUUIDs = FacadeUtil.getAllowedProcessUUIDsFor(applicationName,
            RuleType.PROCESS_READ);
        if (visibleProcessUUIDs == null) {
          visibleProcessUUIDs = new HashSet<ProcessDefinitionUUID>();
        }
        visibleProcessUUIDs.retainAll(processUUIDs);
        if (visibleProcessUUIDs != null && !visibleProcessUUIDs.isEmpty()) {
          processes = EnvTool.getAllQueriers(getQueryList()).getParentProcessInstancesWithOverdueTasks(userId,
              currentDate, fromIndex, pageSize, visibleProcessUUIDs, pagingCriterion);
        }
      }
    } else {
      processes = EnvTool.getAllQueriers(getQueryList()).getParentProcessInstancesWithOverdueTasks(userId, currentDate,
          fromIndex, pageSize, processUUIDs, pagingCriterion);
    }
    return processes;
  }

  private List<InternalProcessInstance> getParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(
      final String userId, final int remainingDays, final int startingIndex, final int pageSize) {
    final boolean access = EnvTool.isRestrictedApplicationAcces();
    List<InternalProcessInstance> processes = new ArrayList<InternalProcessInstance>();
    final Date currentDate = new Date();
    final Date beginningOfTheDay = DateUtil.getBeginningOfTheDay(currentDate);
    final Date atRisk = DateUtil.backTo(beginningOfTheDay, -(remainingDays + 1));
    if (access) {
      final String applicationName = EnvTool.getApplicationAccessName();
      if (applicationName != null) {
        final Set<ProcessDefinitionUUID> visibleProcessUUIDs = FacadeUtil.getAllowedProcessUUIDsFor(applicationName,
            RuleType.PROCESS_READ);
        if (visibleProcessUUIDs != null && !visibleProcessUUIDs.isEmpty()) {
          processes = EnvTool.getAllQueriers(getQueryList())
              .getParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(userId, currentDate, atRisk,
                  startingIndex, pageSize, visibleProcessUUIDs);
        }
      }
    } else {
      processes = EnvTool.getAllQueriers(getQueryList())
          .getParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(userId, currentDate, atRisk,
              startingIndex, pageSize);
    }
    return processes;
  }

  private List<InternalProcessInstance> getParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(
      final String userId, final int remainingDays, final int startingIndex, final int pageSize,
      final ProcessInstanceCriterion pagingCriterion) {
    final boolean access = EnvTool.isRestrictedApplicationAcces();
    List<InternalProcessInstance> processes = new ArrayList<InternalProcessInstance>();
    final Date currentDate = new Date();
    final Date beginningOfTheDay = DateUtil.getBeginningOfTheDay(currentDate);
    final Date atRisk = DateUtil.backTo(beginningOfTheDay, -(remainingDays + 1));
    if (access) {
      final String applicationName = EnvTool.getApplicationAccessName();
      if (applicationName != null) {
        final Set<ProcessDefinitionUUID> visibleProcessUUIDs = FacadeUtil.getAllowedProcessUUIDsFor(applicationName,
            RuleType.PROCESS_READ);
        if (visibleProcessUUIDs != null && !visibleProcessUUIDs.isEmpty()) {
          processes = EnvTool.getAllQueriers(getQueryList())
              .getParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(userId, currentDate, atRisk,
                  startingIndex, pageSize, visibleProcessUUIDs, pagingCriterion);
        }
      }
    } else {
      processes = EnvTool.getAllQueriers(getQueryList())
          .getParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(userId, currentDate, atRisk,
              startingIndex, pageSize, pagingCriterion);
    }
    return processes;
  }

  private List<InternalProcessInstance> getParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(
      final String userId, final int remainingDays, final int startingIndex, final int pageSize,
      final Set<ProcessDefinitionUUID> processUUIDs) {
    final boolean access = EnvTool.isRestrictedApplicationAcces();
    List<InternalProcessInstance> processes = new ArrayList<InternalProcessInstance>();
    final Date currentDate = new Date();
    final Date beginningOfTheDay = DateUtil.getBeginningOfTheDay(currentDate);
    final Date atRisk = DateUtil.backTo(beginningOfTheDay, -(remainingDays + 1));
    if (access) {
      final String applicationName = EnvTool.getApplicationAccessName();
      if (applicationName != null) {
        Set<ProcessDefinitionUUID> visibleProcessUUIDs = FacadeUtil.getAllowedProcessUUIDsFor(applicationName,
            RuleType.PROCESS_READ);
        if (visibleProcessUUIDs == null) {
          visibleProcessUUIDs = new HashSet<ProcessDefinitionUUID>();
        }
        visibleProcessUUIDs.retainAll(processUUIDs);
        if (visibleProcessUUIDs != null && !visibleProcessUUIDs.isEmpty()) {
          processes = EnvTool.getAllQueriers(getQueryList())
              .getParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(userId, currentDate, atRisk,
                  startingIndex, pageSize, visibleProcessUUIDs);
        }
      }
    } else {
      processes = EnvTool.getAllQueriers(getQueryList())
          .getParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(userId, currentDate, atRisk,
              startingIndex, pageSize, processUUIDs);
    }
    return processes;
  }

  private List<InternalProcessInstance> getParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(
      final String userId, final int remainingDays, final int startingIndex, final int pageSize,
      final Set<ProcessDefinitionUUID> processUUIDs, final ProcessInstanceCriterion pagingCriterion) {
    final boolean access = EnvTool.isRestrictedApplicationAcces();
    List<InternalProcessInstance> processes = new ArrayList<InternalProcessInstance>();
    final Date currentDate = new Date();
    final Date beginningOfTheDay = DateUtil.getBeginningOfTheDay(currentDate);
    final Date atRisk = DateUtil.backTo(beginningOfTheDay, -(remainingDays + 1));
    if (access) {
      final String applicationName = EnvTool.getApplicationAccessName();
      if (applicationName != null) {
        Set<ProcessDefinitionUUID> visibleProcessUUIDs = FacadeUtil.getAllowedProcessUUIDsFor(applicationName,
            RuleType.PROCESS_READ);
        if (visibleProcessUUIDs == null) {
          visibleProcessUUIDs = new HashSet<ProcessDefinitionUUID>();
        }
        visibleProcessUUIDs.retainAll(processUUIDs);
        if (visibleProcessUUIDs != null && !visibleProcessUUIDs.isEmpty()) {
          processes = EnvTool.getAllQueriers(getQueryList())
              .getParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(userId, currentDate, atRisk,
                  startingIndex, pageSize, visibleProcessUUIDs, pagingCriterion);
        }
      }
    } else {
      processes = EnvTool.getAllQueriers(getQueryList())
          .getParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(userId, currentDate, atRisk,
              startingIndex, pageSize, processUUIDs, pagingCriterion);
    }
    return processes;
  }

  @Override
  public List<LightProcessInstance> getLightParentProcessInstancesWithInvolvedUser(final String userId,
      final int fromIndex, final int pageSize) {
    final List<InternalProcessInstance> processes = getParentProcessInstancesWithInvolvedUser(userId, fromIndex,
        pageSize);
    final List<LightProcessInstance> result = new ArrayList<LightProcessInstance>();
    for (final ProcessInstance record : processes) {
      result.add(new LightProcessInstanceImpl(record));
    }
    return result;
  }

  @Override
  public List<LightProcessInstance> getLightParentProcessInstancesWithInvolvedUser(final String userId,
      final int fromIndex, final int pageSize, final ProcessInstanceCriterion pagingCriterion) {
    final List<InternalProcessInstance> processes = getParentProcessInstancesWithInvolvedUser(userId, fromIndex,
        pageSize, pagingCriterion);
    final List<LightProcessInstance> result = new ArrayList<LightProcessInstance>();
    for (final ProcessInstance record : processes) {
      result.add(new LightProcessInstanceImpl(record));
    }
    return result;
  }

  @Override
  public List<LightProcessInstance> getLightParentProcessInstancesWithInvolvedUser(final String userId,
      final int fromIndex, final int pageSize, final Set<ProcessDefinitionUUID> processUUIDs) {
    if (processUUIDs == null || processUUIDs.isEmpty()) {
      return Collections.emptyList();
    }
    final List<InternalProcessInstance> internalProcesses = EnvTool.getAllQueriers(getQueryList())
        .getParentProcessInstancesWithInvolvedUser(userId, fromIndex, pageSize, processUUIDs);
    final List<LightProcessInstance> result = new ArrayList<LightProcessInstance>();
    for (final ProcessInstance record : internalProcesses) {
      result.add(new LightProcessInstanceImpl(record));
    }
    return result;
  }

  @Override
  public List<LightProcessInstance> getLightParentProcessInstancesWithInvolvedUser(final String userId,
      final int fromIndex, final int pageSize, final Set<ProcessDefinitionUUID> processUUIDs,
      final ProcessInstanceCriterion pagingCriterion) {
    if (processUUIDs == null || processUUIDs.isEmpty()) {
      return Collections.emptyList();
    }
    final List<InternalProcessInstance> internalProcesses = EnvTool.getAllQueriers(getQueryList())
        .getParentProcessInstancesWithInvolvedUser(userId, fromIndex, pageSize, processUUIDs, pagingCriterion);

    final List<LightProcessInstance> result = new ArrayList<LightProcessInstance>();
    for (final ProcessInstance record : internalProcesses) {
      result.add(new LightProcessInstanceImpl(record));
    }
    return result;
  }

  @Override
  public Integer getNumberOfParentProcessInstancesWithActiveUser(final String userId) {
    final boolean access = EnvTool.isRestrictedApplicationAcces();
    if (access) {
      final String applicationName = EnvTool.getApplicationAccessName();
      if (applicationName != null) {
        final Set<ProcessDefinitionUUID> visibleProcessUUIDs = FacadeUtil.getAllowedProcessUUIDsFor(applicationName,
            RuleType.PROCESS_READ);
        if (visibleProcessUUIDs != null && !visibleProcessUUIDs.isEmpty()) {
          return EnvTool.getAllQueriers(getQueryList()).getNumberOfParentProcessInstancesWithActiveUser(userId,
              visibleProcessUUIDs);
        }
      }
      return 0;
    } else {
      return EnvTool.getAllQueriers(getQueryList()).getNumberOfParentProcessInstancesWithActiveUser(userId);
    }
  }

  @Override
  public Integer getNumberOfParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(final String userId,
      final int remainingDays) {
    final boolean access = EnvTool.isRestrictedApplicationAcces();
    final Date currentDate = new Date();
    final Date beginningOfTheDay = DateUtil.getBeginningOfTheDay(currentDate);
    final Date atRisk = DateUtil.backTo(beginningOfTheDay, -(remainingDays + 1));
    if (access) {
      final String applicationName = EnvTool.getApplicationAccessName();
      if (applicationName != null) {
        final Set<ProcessDefinitionUUID> visibleProcessUUIDs = FacadeUtil.getAllowedProcessUUIDsFor(applicationName,
            RuleType.PROCESS_READ);
        if (visibleProcessUUIDs != null && !visibleProcessUUIDs.isEmpty()) {
          return EnvTool.getAllQueriers(getQueryList())
              .getNumberOfParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(userId, currentDate,
                  atRisk, visibleProcessUUIDs);
        }
      }
      return 0;
    } else {
      return EnvTool.getAllQueriers(getQueryList())
          .getNumberOfParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(userId, currentDate,
              atRisk);
    }
  }

  @Override
  public Integer getNumberOfParentProcessInstancesWithOverdueTasks(final String userId) {
    final boolean access = EnvTool.isRestrictedApplicationAcces();
    final Date currentDate = new Date();
    if (access) {
      final String applicationName = EnvTool.getApplicationAccessName();
      if (applicationName != null) {
        final Set<ProcessDefinitionUUID> visibleProcessUUIDs = FacadeUtil.getAllowedProcessUUIDsFor(applicationName,
            RuleType.PROCESS_READ);
        if (visibleProcessUUIDs != null && !visibleProcessUUIDs.isEmpty()) {
          return EnvTool.getAllQueriers(getQueryList()).getNumberOfParentProcessInstancesWithOverdueTasks(userId,
              currentDate, visibleProcessUUIDs);
        }
      }
      return 0;
    } else {
      return EnvTool.getAllQueriers(getQueryList()).getNumberOfParentProcessInstancesWithOverdueTasks(userId,
          currentDate);
    }
  }

  @Override
  public Integer getNumberOfParentProcessInstancesWithInvolvedUser(final String userId) {
    final boolean access = EnvTool.isRestrictedApplicationAcces();
    if (access) {
      final String applicationName = EnvTool.getApplicationAccessName();
      if (applicationName != null) {
        final Set<ProcessDefinitionUUID> visibleProcessUUIDs = FacadeUtil.getAllowedProcessUUIDsFor(applicationName,
            RuleType.PROCESS_READ);
        if (visibleProcessUUIDs != null && !visibleProcessUUIDs.isEmpty()) {
          return EnvTool.getAllQueriers(getQueryList()).getNumberOfParentProcessInstancesWithInvolvedUser(userId,
              visibleProcessUUIDs);
        }
      }
      return 0;
    } else {
      return EnvTool.getAllQueriers(getQueryList()).getNumberOfParentProcessInstancesWithInvolvedUser(userId);
    }
  }

  @Override
  public Integer getNumberOfParentProcessInstancesWithInvolvedUserAndCategory(final String userId, final String category) {
    final boolean access = EnvTool.isRestrictedApplicationAcces();
    final Set<ProcessDefinitionUUID> targetedProcesses = EnvTool.getAllQueriers(getQueryList())
        .getProcessUUIDsFromCategory(category);
    if (access) {
      final String applicationName = EnvTool.getApplicationAccessName();
      if (applicationName != null) {
        final Set<ProcessDefinitionUUID> visibleProcessUUIDs = FacadeUtil.getAllowedProcessUUIDsFor(applicationName,
            RuleType.PROCESS_READ);
        visibleProcessUUIDs.retainAll(targetedProcesses);
        if (!visibleProcessUUIDs.isEmpty()) {
          return EnvTool.getAllQueriers(getQueryList()).getNumberOfParentProcessInstancesWithInvolvedUser(userId,
              visibleProcessUUIDs);
        }
      }
      return 0;
    } else if (targetedProcesses.isEmpty()) {
      return 0;
    } else {
      return EnvTool.getAllQueriers(getQueryList()).getNumberOfParentProcessInstancesWithInvolvedUser(userId,
          targetedProcesses);
    }
  }

  @Override
  public Integer getNumberOfParentProcessInstancesWithStartedBy(final String userId) {
    final boolean access = EnvTool.isRestrictedApplicationAcces();
    if (access) {
      final String applicationName = EnvTool.getApplicationAccessName();
      if (applicationName != null) {
        final Set<ProcessDefinitionUUID> visibleProcessUUIDs = FacadeUtil.getAllowedProcessUUIDsFor(applicationName,
            RuleType.PROCESS_READ);
        if (visibleProcessUUIDs != null && !visibleProcessUUIDs.isEmpty()) {
          return EnvTool.getAllQueriers(getQueryList()).getNumberOfParentProcessInstancesWithStartedBy(userId,
              visibleProcessUUIDs);
        }
      }
      return 0;
    } else {
      return EnvTool.getAllQueriers(getQueryList()).getNumberOfParentProcessInstancesWithStartedBy(userId);
    }
  }

  private List<InternalProcessInstance> getParentProcessInstancesWithActiveUser(final String userId,
      final int startingIndex, final int pageSize) {
    final boolean access = EnvTool.isRestrictedApplicationAcces();
    List<InternalProcessInstance> processes = new ArrayList<InternalProcessInstance>();
    if (access) {
      final String applicationName = EnvTool.getApplicationAccessName();
      if (applicationName != null) {
        final Set<ProcessDefinitionUUID> visibleProcessUUIDs = FacadeUtil.getAllowedProcessUUIDsFor(applicationName,
            RuleType.PROCESS_READ);
        if (visibleProcessUUIDs != null && !visibleProcessUUIDs.isEmpty()) {
          processes = EnvTool.getAllQueriers(getQueryList()).getParentProcessInstancesWithActiveUser(userId,
              startingIndex, pageSize, visibleProcessUUIDs);
        }
      }
    } else {
      processes = EnvTool.getAllQueriers(getQueryList()).getParentProcessInstancesWithActiveUser(userId, startingIndex,
          pageSize);
    }
    return processes;
  }

  private List<InternalProcessInstance> getParentProcessInstancesWithActiveUser(final String userId,
      final int startingIndex, final int pageSize, final ProcessInstanceCriterion pagingCriterion) {
    final boolean access = EnvTool.isRestrictedApplicationAcces();
    List<InternalProcessInstance> processes = new ArrayList<InternalProcessInstance>();
    if (access) {
      final String applicationName = EnvTool.getApplicationAccessName();
      if (applicationName != null) {
        final Set<ProcessDefinitionUUID> visibleProcessUUIDs = FacadeUtil.getAllowedProcessUUIDsFor(applicationName,
            RuleType.PROCESS_READ);
        if (visibleProcessUUIDs != null && !visibleProcessUUIDs.isEmpty()) {
          processes = EnvTool.getAllQueriers(getQueryList()).getParentProcessInstancesWithActiveUser(userId,
              startingIndex, pageSize, visibleProcessUUIDs, pagingCriterion);
        }
      }
    } else {
      processes = EnvTool.getAllQueriers(getQueryList()).getParentProcessInstancesWithActiveUser(userId, startingIndex,
          pageSize, pagingCriterion);
    }
    return processes;
  }

  private List<InternalProcessInstance> getParentProcessInstancesWithActiveUser(final String userId,
      final int startingIndex, final int pageSize, final Set<ProcessDefinitionUUID> processUUIDs) {
    final boolean access = EnvTool.isRestrictedApplicationAcces();
    List<InternalProcessInstance> processes = new ArrayList<InternalProcessInstance>();
    if (access) {
      final String applicationName = EnvTool.getApplicationAccessName();
      if (applicationName != null) {
        Set<ProcessDefinitionUUID> visibleProcessUUIDs = FacadeUtil.getAllowedProcessUUIDsFor(applicationName,
            RuleType.PROCESS_READ);
        if (visibleProcessUUIDs == null || visibleProcessUUIDs.isEmpty()) {
          visibleProcessUUIDs = new HashSet<ProcessDefinitionUUID>();
        }
        visibleProcessUUIDs.retainAll(processUUIDs);
        if (visibleProcessUUIDs != null && !visibleProcessUUIDs.isEmpty()) {
          processes = EnvTool.getAllQueriers(getQueryList()).getParentProcessInstancesWithActiveUser(userId,
              startingIndex, pageSize, visibleProcessUUIDs);
        }
      }
    } else {
      processes = EnvTool.getAllQueriers(getQueryList()).getParentProcessInstancesWithActiveUser(userId, startingIndex,
          pageSize, processUUIDs);
    }
    return processes;
  }

  private List<InternalProcessInstance> getParentProcessInstancesWithActiveUser(final String userId,
      final int startingIndex, final int pageSize, final Set<ProcessDefinitionUUID> processUUIDs,
      final ProcessInstanceCriterion pagingCriterion) {
    final boolean access = EnvTool.isRestrictedApplicationAcces();
    List<InternalProcessInstance> processes = new ArrayList<InternalProcessInstance>();
    if (access) {
      final String applicationName = EnvTool.getApplicationAccessName();
      if (applicationName != null) {
        Set<ProcessDefinitionUUID> visibleProcessUUIDs = FacadeUtil.getAllowedProcessUUIDsFor(applicationName,
            RuleType.PROCESS_READ);
        if (visibleProcessUUIDs == null || visibleProcessUUIDs.isEmpty()) {
          visibleProcessUUIDs = new HashSet<ProcessDefinitionUUID>();
        }
        visibleProcessUUIDs.retainAll(processUUIDs);
        if (visibleProcessUUIDs != null && !visibleProcessUUIDs.isEmpty()) {
          processes = EnvTool.getAllQueriers(getQueryList()).getParentProcessInstancesWithActiveUser(userId,
              startingIndex, pageSize, visibleProcessUUIDs, pagingCriterion);
        }
      }
    } else {
      processes = EnvTool.getAllQueriers(getQueryList()).getParentProcessInstancesWithActiveUser(userId, startingIndex,
          pageSize, processUUIDs, pagingCriterion);
    }
    return processes;
  }

  private List<InternalProcessInstance> getParentProcessInstancesWithInvolvedUser(final String userId,
      final int startingIndex, final int pageSize) {
    final boolean access = EnvTool.isRestrictedApplicationAcces();
    List<InternalProcessInstance> processes = new ArrayList<InternalProcessInstance>();
    if (access) {
      final String applicationName = EnvTool.getApplicationAccessName();
      if (applicationName != null) {
        final Set<ProcessDefinitionUUID> visibleProcessUUIDs = FacadeUtil.getAllowedProcessUUIDsFor(applicationName,
            RuleType.PROCESS_READ);
        if (visibleProcessUUIDs != null && !visibleProcessUUIDs.isEmpty()) {
          processes = EnvTool.getAllQueriers(getQueryList()).getParentProcessInstancesWithInvolvedUser(userId,
              startingIndex, pageSize, visibleProcessUUIDs);
        }
      }
    } else {
      processes = EnvTool.getAllQueriers(getQueryList()).getParentProcessInstancesWithInvolvedUser(userId,
          startingIndex, pageSize);
    }
    return processes;
  }

  private List<InternalProcessInstance> getParentProcessInstancesWithInvolvedUser(final String userId,
      final int startingIndex, final int pageSize, final ProcessInstanceCriterion pagingCriterion) {
    final boolean access = EnvTool.isRestrictedApplicationAcces();
    List<InternalProcessInstance> processes = new ArrayList<InternalProcessInstance>();
    if (access) {
      final String applicationName = EnvTool.getApplicationAccessName();
      if (applicationName != null) {
        final Set<ProcessDefinitionUUID> visibleProcessUUIDs = FacadeUtil.getAllowedProcessUUIDsFor(applicationName,
            RuleType.PROCESS_READ);
        if (visibleProcessUUIDs != null && !visibleProcessUUIDs.isEmpty()) {
          processes = EnvTool.getAllQueriers(getQueryList()).getParentProcessInstancesWithInvolvedUser(userId,
              startingIndex, pageSize, visibleProcessUUIDs, pagingCriterion);
        }
      }
    } else {
      processes = EnvTool.getAllQueriers(getQueryList()).getParentProcessInstancesWithInvolvedUser(userId,
          startingIndex, pageSize, pagingCriterion);
    }
    return processes;
  }

  private List<InternalProcessInstance> getUserParentProcessInstances(final int startingIndex, final int pageSize) {
    final boolean access = EnvTool.isRestrictedApplicationAcces();
    List<InternalProcessInstance> processes = new ArrayList<InternalProcessInstance>();
    if (access) {
      final String applicationName = EnvTool.getApplicationAccessName();
      if (applicationName != null) {
        final Set<ProcessDefinitionUUID> visibleProcessUUIDs = FacadeUtil.getAllowedProcessUUIDsFor(applicationName,
            RuleType.PROCESS_READ);
        if (visibleProcessUUIDs != null && !visibleProcessUUIDs.isEmpty()) {
          processes = EnvTool.getAllQueriers(getQueryList()).getParentUserInstances(EnvTool.getUserId(), startingIndex,
              pageSize, visibleProcessUUIDs);
        }
      }
    } else {
      processes = EnvTool.getAllQueriers(getQueryList()).getParentUserInstances(EnvTool.getUserId(), startingIndex,
          pageSize);
    }
    return processes;
  }

  private List<InternalProcessInstance> getUserParentProcessInstances(final int startingIndex, final int pageSize,
      final ProcessInstanceCriterion pagingCriterion) {
    final boolean access = EnvTool.isRestrictedApplicationAcces();
    List<InternalProcessInstance> processes = new ArrayList<InternalProcessInstance>();
    if (access) {
      final String applicationName = EnvTool.getApplicationAccessName();
      if (applicationName != null) {
        final Set<ProcessDefinitionUUID> visibleProcessUUIDs = FacadeUtil.getAllowedProcessUUIDsFor(applicationName,
            RuleType.PROCESS_READ);
        if (visibleProcessUUIDs != null && !visibleProcessUUIDs.isEmpty()) {
          processes = EnvTool.getAllQueriers(getQueryList()).getParentUserInstances(EnvTool.getUserId(), startingIndex,
              pageSize, visibleProcessUUIDs, pagingCriterion);
        }
      }
    } else {
      processes = EnvTool.getAllQueriers(getQueryList()).getParentUserInstances(EnvTool.getUserId(), startingIndex,
          pageSize, pagingCriterion);
    }
    return processes;
  }

  private List<InternalProcessInstance> getUserParentProcessInstances(final int startingIndex, final int pageSize,
      final Set<ProcessDefinitionUUID> processUUIDs) {
    final boolean access = EnvTool.isRestrictedApplicationAcces();
    List<InternalProcessInstance> processes = new ArrayList<InternalProcessInstance>();
    if (access) {
      final String applicationName = EnvTool.getApplicationAccessName();
      if (applicationName != null) {
        Set<ProcessDefinitionUUID> visibleProcessUUIDs = FacadeUtil.getAllowedProcessUUIDsFor(applicationName,
            RuleType.PROCESS_READ);
        if (visibleProcessUUIDs == null) {
          visibleProcessUUIDs = new HashSet<ProcessDefinitionUUID>();
        }
        visibleProcessUUIDs.retainAll(processUUIDs);
        if (visibleProcessUUIDs != null && !visibleProcessUUIDs.isEmpty()) {
          processes = EnvTool.getAllQueriers(getQueryList()).getParentUserInstances(EnvTool.getUserId(), startingIndex,
              pageSize, visibleProcessUUIDs);
        }
      }
    } else {
      processes = EnvTool.getAllQueriers(getQueryList()).getParentUserInstances(EnvTool.getUserId(), startingIndex,
          pageSize, processUUIDs);
    }
    return processes;
  }

  private List<InternalProcessInstance> getUserParentProcessInstances(final int startingIndex, final int pageSize,
      final Set<ProcessDefinitionUUID> processUUIDs, final ProcessInstanceCriterion pagingCriterion) {
    final boolean access = EnvTool.isRestrictedApplicationAcces();
    List<InternalProcessInstance> processes = new ArrayList<InternalProcessInstance>();
    if (access) {
      final String applicationName = EnvTool.getApplicationAccessName();
      if (applicationName != null) {
        Set<ProcessDefinitionUUID> visibleProcessUUIDs = FacadeUtil.getAllowedProcessUUIDsFor(applicationName,
            RuleType.PROCESS_READ);
        if (visibleProcessUUIDs == null) {
          visibleProcessUUIDs = new HashSet<ProcessDefinitionUUID>();
        }
        visibleProcessUUIDs.retainAll(processUUIDs);
        if (visibleProcessUUIDs != null && !visibleProcessUUIDs.isEmpty()) {
          processes = EnvTool.getAllQueriers(getQueryList()).getParentUserInstances(EnvTool.getUserId(), startingIndex,
              pageSize, visibleProcessUUIDs, pagingCriterion);
        }
      }
    } else {
      processes = EnvTool.getAllQueriers(getQueryList()).getParentUserInstances(EnvTool.getUserId(), startingIndex,
          pageSize, processUUIDs, pagingCriterion);
    }
    return processes;
  }

  private Set<InternalProcessInstance> getUserProcessInstances() {
    final boolean access = EnvTool.isRestrictedApplicationAcces();
    Set<InternalProcessInstance> processes = new HashSet<InternalProcessInstance>();
    if (access) {
      final String applicationName = EnvTool.getApplicationAccessName();
      if (applicationName != null) {
        final Set<ProcessDefinitionUUID> visibleProcessUUIDs = FacadeUtil.getAllowedProcessUUIDsFor(applicationName,
            RuleType.PROCESS_READ);
        if (visibleProcessUUIDs != null && !visibleProcessUUIDs.isEmpty()) {
          processes = EnvTool.getAllQueriers(getQueryList()).getUserInstances(EnvTool.getUserId(), visibleProcessUUIDs);
        }
      }
    } else {
      processes = EnvTool.getAllQueriers(getQueryList()).getUserInstances(EnvTool.getUserId());
    }
    return processes;
  }

  @Override
  public Set<ProcessInstance> getProcessInstances(final Collection<ProcessInstanceUUID> instanceUUIDs) {
    final Set<ProcessInstance> result = new HashSet<ProcessInstance>();
    if (instanceUUIDs == null || instanceUUIDs.isEmpty()) {
      return result;
    }
    for (final InternalProcessInstance record : EnvTool.getAllQueriers(getQueryList()).getProcessInstances(
        instanceUUIDs)) {
      bindAttachementsToInternalProcessInstance(record);
      result.add(new ProcessInstanceImpl(record));
    }
    return result;
  }

  @Override
  public Set<LightProcessInstance> getLightProcessInstances(final Collection<ProcessInstanceUUID> instanceUUIDs) {
    final Set<LightProcessInstance> result = new HashSet<LightProcessInstance>();
    if (instanceUUIDs == null || instanceUUIDs.isEmpty()) {
      return result;
    }
    for (final ProcessInstance record : EnvTool.getAllQueriers(getQueryList()).getProcessInstances(instanceUUIDs)) {
      result.add(new LightProcessInstanceImpl(record));
    }
    return result;
  }

  @Override
  public List<LightProcessInstance> getLightProcessInstances(final Collection<ProcessInstanceUUID> instanceUUIDs,
      final int fromIndex, final int pageSize) {
    final List<LightProcessInstance> result = new ArrayList<LightProcessInstance>();
    if (pageSize <= 0) {
      return result;
    }
    if (instanceUUIDs == null || instanceUUIDs.isEmpty()) {
      return result;
    }
    for (final ProcessInstance record : EnvTool.getAllQueriers(getQueryList()).getProcessInstances(instanceUUIDs,
        fromIndex, pageSize)) {
      result.add(new LightProcessInstanceImpl(record));
    }
    return result;
  }

  @Override
  public List<LightProcessInstance> getLightProcessInstances(final Set<ProcessInstanceUUID> instanceUUIDs,
      final int fromIndex, final int pageSize, final ProcessInstanceCriterion pagingCriterion) {
    final List<LightProcessInstance> result = new ArrayList<LightProcessInstance>();
    if (pageSize <= 0) {
      return result;
    }
    if (instanceUUIDs == null || instanceUUIDs.isEmpty()) {
      return result;
    }
    for (final ProcessInstance record : EnvTool.getAllQueriers(getQueryList()).getProcessInstancesWithInstanceUUIDs(
        instanceUUIDs, fromIndex, pageSize, pagingCriterion)) {
      result.add(new LightProcessInstanceImpl(record));
    }
    return result;
  }

  @Override
  public Set<ProcessInstance> getProcessInstancesWithTaskState(final Collection<ActivityState> activityStates) {
    final Set<ProcessInstance> result = new HashSet<ProcessInstance>();
    if (activityStates == null || activityStates.isEmpty()) {
      return result;
    }

    final boolean access = EnvTool.isRestrictedApplicationAcces();
    Set<InternalProcessInstance> processes = new HashSet<InternalProcessInstance>();
    if (access) {
      final String applicationName = EnvTool.getApplicationAccessName();
      if (applicationName != null) {
        final Set<ProcessDefinitionUUID> visibleProcessUUIDs = FacadeUtil.getAllowedProcessUUIDsFor(applicationName,
            RuleType.PROCESS_READ);
        if (visibleProcessUUIDs != null && !visibleProcessUUIDs.isEmpty()) {
          processes = EnvTool.getAllQueriers(getQueryList()).getProcessInstancesWithTaskState(activityStates,
              visibleProcessUUIDs);
        }
      }
    } else {
      processes = EnvTool.getAllQueriers(getQueryList()).getProcessInstancesWithTaskState(activityStates);
    }
    for (final InternalProcessInstance record : processes) {
      bindAttachementsToInternalProcessInstance(record);
      result.add(new ProcessInstanceImpl(record));
    }
    return result;
  }

  @Override
  public Set<ProcessInstance> getProcessInstancesWithInstanceStates(final Collection<InstanceState> instanceStates) {
    Misc.checkArgsNotNull(instanceStates);
    if (instanceStates.isEmpty()) {
      throw new IllegalArgumentException(ExceptionManager.getInstance().getMessage("bai_QRAPII_15"));
    }
    final Set<ProcessInstance> result = new HashSet<ProcessInstance>();
    final boolean access = EnvTool.isRestrictedApplicationAcces();
    Set<InternalProcessInstance> processes = new HashSet<InternalProcessInstance>();
    if (access) {
      final String applicationName = EnvTool.getApplicationAccessName();
      if (applicationName != null) {
        final Set<ProcessDefinitionUUID> visibleProcessUUIDs = FacadeUtil.getAllowedProcessUUIDsFor(applicationName,
            RuleType.PROCESS_READ);
        if (visibleProcessUUIDs != null && !visibleProcessUUIDs.isEmpty()) {
          processes = EnvTool.getAllQueriers(getQueryList()).getProcessInstancesWithInstanceStates(instanceStates,
              visibleProcessUUIDs);
        }
      }
    } else {
      processes = EnvTool.getAllQueriers(getQueryList()).getProcessInstancesWithInstanceStates(instanceStates);
    }
    for (final InternalProcessInstance record : processes) {
      bindAttachementsToInternalProcessInstance(record);
      result.add(new ProcessInstanceImpl(record));
    }
    return result;
  }

  @Override
  public Set<ProcessInstance> getProcessInstances(final ProcessDefinitionUUID processUUID) {
    final Set<ProcessInstance> result = new HashSet<ProcessInstance>();
    for (final InternalProcessInstance record : EnvTool.getAllQueriers(getQueryList()).getProcessInstances(processUUID)) {
      bindAttachementsToInternalProcessInstance(record);
      result.add(new ProcessInstanceImpl(record));
    }
    return result;
  }

  @Override
  public Set<LightProcessInstance> getLightProcessInstances(final ProcessDefinitionUUID processUUID) {
    final Set<LightProcessInstance> result = new HashSet<LightProcessInstance>();
    for (final ProcessInstance record : EnvTool.getAllQueriers(getQueryList()).getProcessInstances(processUUID)) {
      result.add(new LightProcessInstanceImpl(record));
    }
    return result;
  }

  @Override
  public Set<LightProcessInstance> getLightWeightProcessInstances(final Set<ProcessDefinitionUUID> processUUIDs) {
    final Set<LightProcessInstance> result = new HashSet<LightProcessInstance>();
    for (final ProcessInstance record : EnvTool.getAllQueriers(getQueryList()).getProcessInstances(processUUIDs)) {
      result.add(new LightProcessInstanceImpl(record));
    }
    return result;
  }

  public ActivityInstance getActivityInstance(final ProcessInstanceUUID instanceUUID, final String activityId,
      final String iterationId, final String activityInstanceId, final String loopId) throws ActivityNotFoundException,
      InstanceNotFoundException {
    final ActivityInstance result = EnvTool.getAllQueriers(getQueryList()).getActivityInstance(instanceUUID,
        activityId, iterationId, activityInstanceId, loopId);
    if (result == null) {
      if (EnvTool.getAllQueriers(getQueryList()).getProcessInstance(instanceUUID) == null) {
        throw new InstanceNotFoundException("bai_QRAPII_2", instanceUUID);
      }
      throw new ActivityNotFoundException("bai_QRAPII_3", instanceUUID, activityId, iterationId);
    }
    return new ActivityInstanceImpl(result);
  }

  @Override
  public boolean canExecuteTask(final ActivityInstanceUUID taskUUID) throws TaskNotFoundException {
    final TaskInstance task = getTask(taskUUID);
    if (!task.getState().equals(ActivityState.READY)) {
      return false;
    }
    final String userId = EnvTool.getUserId();
    if (task.isTaskAssigned()) {
      return task.getTaskUser().equals(userId);
    }
    return task.getTaskCandidates().contains(userId);
  }

  @Override
  public Set<ActivityInstance> getActivityInstances(final ProcessInstanceUUID instanceUUID) {
    final Set<ActivityInstance> result = new HashSet<ActivityInstance>();
    for (final ActivityInstance record : EnvTool.getAllQueriers(getQueryList()).getActivityInstances(instanceUUID)) {
      result.add(new ActivityInstanceImpl(record));
    }
    return result;
  }

  @Override
  public List<LightActivityInstance> getLightActivityInstancesFromRoot(final ProcessInstanceUUID rootInstanceUUID) {
    final List<LightActivityInstance> result = new ArrayList<LightActivityInstance>();
    final List<InternalActivityInstance> activities = EnvTool.getAllQueriers(getQueryList())
        .getActivityInstancesFromRoot(rootInstanceUUID);
    for (final InternalActivityInstance record : activities) {
      result.add(new LightActivityInstanceImpl(record));
    }
    return result;
  }

  @Override
  public Map<ProcessInstanceUUID, List<LightActivityInstance>> getLightActivityInstancesFromRoot(
      final Set<ProcessInstanceUUID> rootInstanceUUIDs, final ActivityState state) {
    final Map<ProcessInstanceUUID, List<LightActivityInstance>> result = new HashMap<ProcessInstanceUUID, List<LightActivityInstance>>();
    final List<InternalActivityInstance> activities = EnvTool.getAllQueriers(getQueryList())
        .getActivityInstancesFromRoot(rootInstanceUUIDs, state);
    for (final InternalActivityInstance activity : activities) {
      final ProcessInstanceUUID instanceUUID = activity.getRootInstanceUUID();
      if (!result.containsKey(instanceUUID)) {
        result.put(instanceUUID, new ArrayList<LightActivityInstance>());
      }
      result.get(instanceUUID).add(new LightActivityInstanceImpl(activity));
    }
    return result;
  }

  @Override
  public Map<ProcessInstanceUUID, List<LightActivityInstance>> getLightActivityInstancesFromRoot(
      final Set<ProcessInstanceUUID> rootInstanceUUIDs) {
    final Map<ProcessInstanceUUID, List<LightActivityInstance>> result = new HashMap<ProcessInstanceUUID, List<LightActivityInstance>>();
    final List<InternalActivityInstance> activities = EnvTool.getAllQueriers(getQueryList())
        .getActivityInstancesFromRoot(rootInstanceUUIDs);
    for (final InternalActivityInstance activity : activities) {
      final ProcessInstanceUUID instanceUUID = activity.getRootInstanceUUID();
      if (!result.containsKey(instanceUUID)) {
        result.put(instanceUUID, new ArrayList<LightActivityInstance>());
      }
      result.get(instanceUUID).add(new LightActivityInstanceImpl(activity));
    }
    return result;
  }

  @Override
  public Map<ProcessInstanceUUID, LightActivityInstance> getLightLastUpdatedActivityInstanceFromRoot(
      final Set<ProcessInstanceUUID> rootInstanceUUIDs, final boolean considerSystemTaks) {
    final Map<ProcessInstanceUUID, LightActivityInstance> result = new HashMap<ProcessInstanceUUID, LightActivityInstance>();
    final Map<ProcessInstanceUUID, InternalActivityInstance> temp = EnvTool.getAllQueriers(getQueryList())
        .getLastUpdatedActivityInstanceFromRoot(rootInstanceUUIDs, considerSystemTaks);
    for (final Map.Entry<ProcessInstanceUUID, InternalActivityInstance> entry : temp.entrySet()) {
      result.put(entry.getKey(), new LightActivityInstanceImpl(entry.getValue()));
    }
    return result;
  }

  @Override
  public List<LightTaskInstance> getLightTaskInstancesFromRoot(final ProcessInstanceUUID rootInstanceUUID) {
    final List<LightTaskInstance> result = new ArrayList<LightTaskInstance>();
    for (final InternalActivityInstance record : EnvTool.getAllQueriers(getQueryList()).getActivityInstancesFromRoot(
        rootInstanceUUID)) {
      if (record.isTask()) {
        result.add(new LightActivityInstanceImpl(record));
      }
    }
    return result;
  }

  @Override
  public Map<ProcessInstanceUUID, List<LightTaskInstance>> getLightTaskInstancesFromRoot(
      final Set<ProcessInstanceUUID> rootInstanceUUIDs) {
    final Map<ProcessInstanceUUID, List<LightTaskInstance>> result = new HashMap<ProcessInstanceUUID, List<LightTaskInstance>>();
    for (final ProcessInstanceUUID instanceUUID : rootInstanceUUIDs) {
      result.put(instanceUUID, getLightTaskInstancesFromRoot(instanceUUID));
    }
    return result;
  }

  @Override
  public Set<LightActivityInstance> getLightActivityInstances(final ProcessInstanceUUID instanceUUID)
      throws InstanceNotFoundException {
    final Set<LightActivityInstance> result = new HashSet<LightActivityInstance>();
    final Set<InternalActivityInstance> activities = EnvTool.getAllQueriers(getQueryList()).getActivityInstances(
        instanceUUID);
    for (final ActivityInstance record : activities) {
      result.add(new LightActivityInstanceImpl(record));
    }
    return result;
  }

  @Override
  public List<LightActivityInstance> getLightActivityInstances(final ProcessInstanceUUID instanceUUID,
      final int fromIdex, final int pageSize, final ActivityInstanceCriterion pagingCriterion)
      throws InstanceNotFoundException {
    final List<LightActivityInstance> result = new ArrayList<LightActivityInstance>();
    final List<InternalActivityInstance> activities = EnvTool.getAllQueriers(getQueryList()).getActivityInstances(
        instanceUUID, fromIdex, pageSize, pagingCriterion);
    for (final ActivityInstance record : activities) {
      result.add(new LightActivityInstanceImpl(record));
    }
    return result;
  }

  @Override
  public Set<ActivityInstance> getActivityInstances(final ProcessInstanceUUID instanceUUID, final String activityId)
      throws ActivityNotFoundException {
    final Set<ActivityInstance> result = new HashSet<ActivityInstance>();
    for (final ActivityInstance record : EnvTool.getAllQueriers(getQueryList()).getActivityInstances(instanceUUID)) {
      if (record.getActivityName().equals(activityId)) {
        result.add(new ActivityInstanceImpl(record));
      }
    }
    if (result.isEmpty()) {
      throw new ActivityNotFoundException("bai_QRAPII_4", instanceUUID, activityId);
    }
    return result;
  }

  @Override
  public Set<LightActivityInstance> getLightActivityInstances(final ProcessInstanceUUID instanceUUID,
      final String activityName) throws InstanceNotFoundException, ActivityNotFoundException {
    final Set<LightActivityInstance> result = new HashSet<LightActivityInstance>();
    final Set<InternalActivityInstance> activities = EnvTool.getAllQueriers(getQueryList()).getActivityInstances(
        instanceUUID);
    for (final InternalActivityInstance record : activities) {
      if (record.getActivityName().equals(activityName)) {
        result.add(new LightActivityInstanceImpl(record));
      }
    }
    if (result.isEmpty()) {
      throw new ActivityNotFoundException("bai_QRAPII_4", instanceUUID, activityName);
    }
    return result;
  }

  @Override
  public Set<LightActivityInstance> getLightActivityInstances(final ProcessInstanceUUID instanceUUID,
      final String activityName, final String iterationId) {
    final Set<LightActivityInstance> result = new HashSet<LightActivityInstance>();
    final Set<InternalActivityInstance> activities = EnvTool.getAllQueriers(getQueryList()).getActivityInstances(
        instanceUUID, activityName, iterationId);
    for (final ActivityInstance record : activities) {
      result.add(new LightActivityInstanceImpl(record));
    }
    return result;
  }

  @Override
  public Set<TaskInstance> getTasks(final ProcessInstanceUUID instanceUUID) {
    final Set<TaskInstance> result = new HashSet<TaskInstance>();
    for (final TaskInstance record : EnvTool.getAllQueriers(getQueryList()).getTaskInstances(instanceUUID)) {
      result.add(new ActivityInstanceImpl(record));
    }
    return result;
  }

  @Override
  public Set<LightTaskInstance> getLightTasks(final ProcessInstanceUUID instanceUUID) throws InstanceNotFoundException {
    final Set<LightTaskInstance> result = new HashSet<LightTaskInstance>();
    for (final TaskInstance record : EnvTool.getAllQueriers(getQueryList()).getTaskInstances(instanceUUID)) {
      result.add(new LightActivityInstanceImpl(record));
    }
    return result;
  }

  @Override
  public Set<LightTaskInstance> getLightTasks(final ProcessInstanceUUID instanceUUID, final Set<String> taskNames) {
    final Set<LightTaskInstance> result = new HashSet<LightTaskInstance>();
    if (taskNames != null && !taskNames.isEmpty()) {
      for (final TaskInstance record : EnvTool.getAllQueriers(getQueryList()).getTaskInstances(instanceUUID, taskNames)) {
        result.add(new LightActivityInstanceImpl(record));
      }
    }
    return result;
  }

  @Override
  public Collection<TaskInstance> getTaskList(final ProcessInstanceUUID instanceUUID, final ActivityState taskState)
      throws InstanceNotFoundException {
    return getTaskListUser(instanceUUID, EnvTool.getUserId(), taskState);
  }

  @Override
  public Collection<LightTaskInstance> getLightTaskList(final ProcessInstanceUUID instanceUUID,
      final ActivityState taskState) throws InstanceNotFoundException {
    return getLightTaskListUser(instanceUUID, EnvTool.getUserId(), taskState);
  }

  @Override
  public Collection<TaskInstance> getTaskList(final ProcessInstanceUUID instanceUUID,
      final Collection<ActivityState> taskStates) throws InstanceNotFoundException {
    FacadeUtil.checkArgsNotNull(instanceUUID, taskStates);
    final Collection<TaskInstance> todos = new HashSet<TaskInstance>();
    for (final ActivityState taskState : taskStates) {
      final Collection<TaskInstance> tasks = getTaskListUser(instanceUUID, EnvTool.getUserId(), taskState);
      if (tasks != null) {
        todos.addAll(tasks);
      }
    }
    return todos;
  }

  @Override
  public Collection<LightTaskInstance> getLightTaskList(final ProcessInstanceUUID instanceUUID,
      final Collection<ActivityState> taskStates) throws InstanceNotFoundException {
    FacadeUtil.checkArgsNotNull(instanceUUID, taskStates);
    final Collection<LightTaskInstance> todos = new HashSet<LightTaskInstance>();
    for (final ActivityState taskState : taskStates) {
      final Collection<LightTaskInstance> tasks = getLightTaskListUser(instanceUUID, EnvTool.getUserId(), taskState);
      if (tasks != null) {
        todos.addAll(tasks);
      }
    }
    return todos;
  }

  @Override
  public Collection<TaskInstance> getTaskList(final ActivityState taskState) {
    return getTaskListUser(EnvTool.getUserId(), taskState);
  }

  @Override
  public Collection<LightTaskInstance> getLightTaskList(final ActivityState taskState) {
    return getLightTaskListUser(EnvTool.getUserId(), taskState);
  }

  @Override
  public TaskInstance getTask(final ActivityInstanceUUID taskUUID) throws TaskNotFoundException {
    final TaskInstance taskInstance = EnvTool.getAllQueriers(getQueryList()).getTaskInstance(taskUUID);
    if (taskInstance == null) {
      throw new TaskNotFoundException("bai_QRAPII_5", taskUUID);
    }
    return new ActivityInstanceImpl(taskInstance);
  }

  @Override
  public Set<String> getTaskCandidates(final ActivityInstanceUUID taskUUID) throws TaskNotFoundException {
    final TaskInstance taskInstance = EnvTool.getAllQueriers(getQueryList()).getTaskInstance(taskUUID);
    if (taskInstance == null) {
      throw new TaskNotFoundException("bai_QRAPII_5", taskUUID);
    }
    return CopyTool.copy(taskInstance.getTaskCandidates());
  }

  @Override
  public Map<ActivityInstanceUUID, Set<String>> getTaskCandidates(final Set<ActivityInstanceUUID> taskUUIDs)
      throws TaskNotFoundException {
    final Map<ActivityInstanceUUID, Set<String>> result = new HashMap<ActivityInstanceUUID, Set<String>>();
    for (final ActivityInstanceUUID taskUUID : taskUUIDs) {
      final TaskInstance taskInstance = EnvTool.getAllQueriers(getQueryList()).getTaskInstance(taskUUID);
      if (taskInstance == null) {
        throw new TaskNotFoundException("bai_QRAPII_5", taskUUID);
      }
      result.put(taskUUID, CopyTool.copy(taskInstance.getTaskCandidates()));
    }
    return result;
  }

  public Map<String, Object> getActivityInstanceVariables(final ProcessInstanceUUID instanceUUID,
      final String activityId, final String iterationId, final String activityInstanceId, final String loopId)
      throws ActivityNotFoundException, InstanceNotFoundException {

    final ActivityInstance activityInst = EnvTool.getAllQueriers().getActivityInstance(instanceUUID, activityId,
        iterationId, activityInstanceId, loopId);

    if (activityInst == null) {
      throw new ActivityNotFoundException("bai_QRAPII_6", instanceUUID, activityId);
    }
    return activityInst.getLastKnownVariableValues();
  }

  public Object getActivityInstanceVariable(final ProcessInstanceUUID instanceUUID, final String activityId,
      final String iterationId, final String activityInstanceId, final String loopId, final String variableId)
      throws InstanceNotFoundException, ActivityNotFoundException, VariableNotFoundException {

    final Map<String, Object> variables = getActivityInstanceVariables(instanceUUID, activityId, iterationId,
        activityInstanceId, loopId);
    if (variables == null || !variables.containsKey(variableId)) {
      throw new VariableNotFoundException("bai_QRAPII_7", instanceUUID, activityId, variableId);
    }
    return variables.get(variableId);
  }

  @Override
  public Map<String, Object> getProcessInstanceVariables(final ProcessInstanceUUID instanceUUID)
      throws InstanceNotFoundException {
    final ProcessInstance processInstance = getInternalProcessInstanceWithoutAttachements(instanceUUID);
    if (processInstance == null) {
      throw new InstanceNotFoundException("bai_QRAPII_8", instanceUUID);
    }
    return processInstance.getLastKnownVariableValues();
  }

  @Override
  public Map<String, Object> getProcessInstanceVariables(final ProcessInstanceUUID instanceUUID, final Date maxDate)
      throws InstanceNotFoundException {
    // take all initial instance var and for each varupdate being proceed before
    // max date, replace the initial value by
    // the new one
    final ProcessInstance processInstance = getInternalProcessInstanceWithoutAttachements(instanceUUID);
    final Map<String, Object> instanceInitialVars = processInstance.getInitialVariableValues();
    final Map<String, Object> instanceVarBeforeMaxDate = new HashMap<String, Object>();
    instanceVarBeforeMaxDate.putAll(instanceInitialVars);
    final Map<String, VariableUpdate> maxVarUpdates = new HashMap<String, VariableUpdate>();

    for (final VariableUpdate varUpdate : processInstance.getVariableUpdates()) {
      if (varUpdate.getDate().getTime() <= maxDate.getTime()) {
        final VariableUpdate currentMax = maxVarUpdates.get(varUpdate.getName());
        if (currentMax == null || currentMax.getDate().getTime() <= varUpdate.getDate().getTime()) {
          maxVarUpdates.put(varUpdate.getName(), varUpdate);
          instanceVarBeforeMaxDate.put(varUpdate.getName(), varUpdate.getValue());
        }
      }
    }
    return instanceVarBeforeMaxDate;
  }

  private Object getProcessInstanceVariable(final ProcessInstanceUUID instanceUUID, final String variableId,
      final Date maxDate) throws InstanceNotFoundException, VariableNotFoundException {
    final String variableName = Misc.getVariableName(variableId);
    final String xpath = Misc.getXPath(variableId);

    final Map<String, Object> variables = getProcessInstanceVariables(instanceUUID, maxDate);
    if (variables == null || !variables.containsKey(variableName)) {
      throw new VariableNotFoundException("bai_QRAPII_9", instanceUUID, variableName);
    }
    final Object value = variables.get(variableName);
    if (xpath != null && xpath.length() > 0) {
      try {
        return evaluateXPath(xpath, (org.w3c.dom.Document) value);
      } catch (final Exception ex) {
        throw new VariableNotFoundException("bai_QRAPII_17", instanceUUID, variableName);
      }
    } else {
      return value;
    }
  }

  @Override
  public Object getProcessInstanceVariable(final ProcessInstanceUUID instanceUUID, final String variableId)
      throws InstanceNotFoundException, VariableNotFoundException {
    final String variableName = Misc.getVariableName(variableId);
    final String xpath = Misc.getXPath(variableId);
    final Map<String, Object> variables = getProcessInstanceVariables(instanceUUID);
    if (variables == null || !variables.containsKey(variableName)) {
      throw new VariableNotFoundException("bai_QRAPII_10", instanceUUID, variableName);
    }
    final Object value = variables.get(variableName);
    if (xpath != null && xpath.length() > 0) {
      try {
        return evaluateXPath(xpath, (org.w3c.dom.Document) value);
      } catch (final Exception ex) {
        ex.printStackTrace();
        throw new VariableNotFoundException("bai_QRAPII_17", instanceUUID, variableId);
      }
    } else {
      return value;
    }
  }

  @Override
  public ActivityInstance getActivityInstance(final ActivityInstanceUUID activityUUID) throws ActivityNotFoundException {
    final ActivityInstance activity = EnvTool.getAllQueriers(getQueryList()).getActivityInstance(activityUUID);
    if (activity == null) {
      throw new ActivityNotFoundException("bai_QRAPII_11", activityUUID);
    }
    return new ActivityInstanceImpl(activity);
  }

  @Override
  public ActivityState getActivityInstanceState(final ActivityInstanceUUID activityUUID)
      throws ActivityNotFoundException {
    FacadeUtil.checkArgsNotNull(activityUUID);
    final Querier querier = EnvTool.getAllQueriers(getQueryList());
    final ActivityState state = querier.getActivityInstanceState(activityUUID);
    if (state == null) {
      final ActivityInstance activity = querier.getActivityInstance(activityUUID);
      throw new ActivityNotFoundException("bai_QRAPII_3", activity.getProcessInstanceUUID(), activity.getActivityName());
    }
    return state;
  }

  @Override
  public Object getActivityInstanceVariable(final ActivityInstanceUUID activityUUID, final String variableId)
      throws ActivityNotFoundException, VariableNotFoundException {
    // search in transient variables
    final Map<String, Object> transientVariables = TransientData.getActivityTransientVariables(activityUUID);
    if (transientVariables != null && transientVariables.containsKey(variableId)) {
      return transientVariables.get(variableId);
    }
    // search in the database persisted variables
    final String variableName = Misc.getVariableName(variableId);
    final String xpath = Misc.getXPath(variableId);
    final ActivityInstance activity = EnvTool.getAllQueriers().getActivityInstance(activityUUID);
    if (activity == null) {
      throw new ActivityNotFoundException("bai_QRAPII_11", activityUUID);
    }
    final Map<String, Object> variableValues = activity.getLastKnownVariableValues();

    if (!variableValues.containsKey(variableName)) {
      throw new VariableNotFoundException("bai_QRAPII_12", activityUUID, variableName);
    }
    final Object value = activity.getLastKnownVariableValues().get(variableName);
    if (xpath != null && xpath.length() > 0) {
      try {
        return evaluateXPath(xpath, (org.w3c.dom.Document) value);
      } catch (final Exception ex) {
        throw new VariableNotFoundException("bai_QRAPII_16", activityUUID, variableName);
      }
    } else {
      return value;
    }
  }

  private Object evaluateXPath(final String xpath, final org.w3c.dom.Document doc) throws ParserConfigurationException,
      SAXException, IOException, XPathExpressionException {
    final XPath xpathEval = XPathFactory.newInstance().newXPath();
    if (isTextExpected(xpath)) {
      return xpathEval.evaluate(xpath, doc);
    } else {
      return xpathEval.evaluate(xpath, doc, XPathConstants.NODE);
    }
  }

  private boolean isTextExpected(final String xpath) {
    final String[] segments = xpath.split("/");
    final String lastSegment = segments[segments.length - 1];
    return "text()".equals(lastSegment) || lastSegment.startsWith("@");
  }

  @Override
  public Map<String, Object> getActivityInstanceVariables(final ActivityInstanceUUID activityUUID)
      throws ActivityNotFoundException {
    final Map<String, Object> variables = new HashMap<String, Object>();
    final Map<String, Object> transientVariables = TransientData.getActivityTransientVariables(activityUUID);
    if (transientVariables != null) {
      variables.putAll(transientVariables);
    }
    final ActivityInstance activity = EnvTool.getAllQueriers().getActivityInstance(activityUUID);
    if (activity == null) {
      throw new ActivityNotFoundException("bai_QRAPII_11", activityUUID);
    }
    final Map<String, Object> lastKnownVariables = activity.getLastKnownVariableValues();
    if (lastKnownVariables != null) {
      variables.putAll(lastKnownVariables);
    }
    return variables;
  }

  @Override
  public Collection<TaskInstance> getTaskList(final ProcessInstanceUUID instanceUUID, final String userId,
      final ActivityState taskState) throws InstanceNotFoundException {
    return getTaskListUser(instanceUUID, userId, taskState);
  }

  @Override
  public Collection<LightTaskInstance> getLightTaskList(final ProcessInstanceUUID instanceUUID, final String userId,
      final ActivityState taskState) throws InstanceNotFoundException {
    return getLightTaskListUser(instanceUUID, userId, taskState);
  }

  @Override
  public Collection<TaskInstance> getTaskList(final String userId, final ActivityState taskState) {
    return getTaskListUser(userId, taskState);
  }

  @Override
  public Collection<LightTaskInstance> getLightTaskList(final String userId, final ActivityState taskState) {
    return getLightTaskListUser(userId, taskState);
  }

  @Override
  public ActivityInstanceUUID getOneTask(final ActivityState taskState) {
    final Querier journal = EnvTool.getJournalQueriers(getQueryList());

    final boolean access = EnvTool.isRestrictedApplicationAcces();
    TaskInstance task = null;
    if (access) {
      final String applicationName = EnvTool.getApplicationAccessName();
      if (applicationName != null) {
        final Set<ProcessDefinitionUUID> visibleProcessUUIDs = FacadeUtil.getAllowedProcessUUIDsFor(applicationName,
            RuleType.PROCESS_READ);
        if (visibleProcessUUIDs != null && !visibleProcessUUIDs.isEmpty()) {
          task = journal.getOneTask(EnvTool.getUserId(), taskState, visibleProcessUUIDs);
        }
      }
    } else {
      task = journal.getOneTask(EnvTool.getUserId(), taskState);
    }
    if (task == null) {
      return null;
    }
    return new ActivityInstanceUUID(task.getUUID());
  }

  @Override
  public ActivityInstanceUUID getOneTask(final ProcessInstanceUUID instanceUUID, final ActivityState taskState) {
    final Querier journal = EnvTool.getJournalQueriers(getQueryList());
    final TaskInstance task = journal.getOneTask(EnvTool.getUserId(), instanceUUID, taskState);
    if (task == null) {
      return null;
    }
    return new ActivityInstanceUUID(task.getUUID());
  }

  @Override
  public ActivityInstanceUUID getOneTask(final ProcessDefinitionUUID processUUID, final ActivityState taskState) {
    final Querier journal = EnvTool.getJournalQueriers(getQueryList());
    final TaskInstance task = journal.getOneTask(EnvTool.getUserId(), processUUID, taskState);
    if (task == null) {
      return null;
    }
    return new ActivityInstanceUUID(task.getUUID());
  }

  private Collection<TaskInstance> getTaskListUser(final ProcessInstanceUUID instanceUUID, final String userId,
      final ActivityState taskState) throws InstanceNotFoundException {
    final Collection<TaskInstance> todos = new ArrayList<TaskInstance>();
    for (final TaskInstance taskActivity : getInternalTaskListUser(instanceUUID, userId, taskState)) {
      todos.add(new ActivityInstanceImpl(taskActivity));
    }
    return todos;
  }

  private Collection<LightTaskInstance> getLightTaskListUser(final ProcessInstanceUUID instanceUUID,
      final String userId, final ActivityState taskState) throws InstanceNotFoundException {
    final Collection<LightTaskInstance> todos = new ArrayList<LightTaskInstance>();
    for (final TaskInstance taskActivity : getInternalTaskListUser(instanceUUID, userId, taskState)) {
      todos.add(new LightActivityInstanceImpl(taskActivity));
    }
    return todos;
  }

  private Collection<TaskInstance> getInternalTaskListUser(final ProcessInstanceUUID instanceUUID, final String userId,
      final ActivityState taskState) throws InstanceNotFoundException {
    FacadeUtil.checkArgsNotNull(instanceUUID, taskState, userId);
    final ProcessInstance processInstance = getInternalProcessInstanceWithoutAttachements(instanceUUID);
    if (processInstance == null) {
      throw new InstanceNotFoundException("bai_QRAPII_13", instanceUUID);
    }
    final Querier journal = EnvTool.getAllQueriers(getQueryList());
    return journal.getUserInstanceTasks(userId, instanceUUID, taskState);
  }

  private Collection<TaskInstance> getTaskListUser(final String userId, final ActivityState taskState) {
    final Collection<TaskInstance> result = new HashSet<TaskInstance>();
    for (final TaskInstance taskInstance : getInternalTaskListUser(userId, taskState)) {
      result.add(new ActivityInstanceImpl(taskInstance));
    }
    return result;
  }

  private Collection<LightTaskInstance> getLightTaskListUser(final String userId, final ActivityState taskState) {
    final Collection<LightTaskInstance> result = new HashSet<LightTaskInstance>();
    for (final TaskInstance taskInstance : getInternalTaskListUser(userId, taskState)) {
      result.add(new LightActivityInstanceImpl(taskInstance));
    }
    return result;
  }

  private Collection<TaskInstance> getInternalTaskListUser(final String userId, final ActivityState taskState) {
    FacadeUtil.checkArgsNotNull(userId, taskState);

    final boolean access = EnvTool.isRestrictedApplicationAcces();
    Collection<TaskInstance> tasks = new HashSet<TaskInstance>();
    if (access) {
      final String applicationName = EnvTool.getApplicationAccessName();
      if (applicationName != null) {
        final Set<ProcessDefinitionUUID> visibleProcessUUIDs = FacadeUtil.getAllowedProcessUUIDsFor(applicationName,
            RuleType.PROCESS_READ);
        if (visibleProcessUUIDs != null && !visibleProcessUUIDs.isEmpty()) {
          tasks = EnvTool.getAllQueriers(getQueryList()).getUserTasks(userId, taskState, visibleProcessUUIDs);
        }
      }
    } else {
      tasks = EnvTool.getAllQueriers(getQueryList()).getUserTasks(userId, taskState);
    }
    return tasks;
  }

  @Override
  public Object getVariable(final ActivityInstanceUUID activityUUID, final String variableId)
      throws ActivityNotFoundException, VariableNotFoundException {
    try {
      return getActivityInstanceVariable(activityUUID, variableId);
    } catch (final Throwable e) {
      final ActivityInstance activity = EnvTool.getAllQueriers().getActivityInstance(activityUUID);
      if (activity == null) {
        throw new ActivityNotFoundException("bai_QRAPII_11", activityUUID);
      }
      final Date maxDate = getMaxDate(activity);
      try {
        return getProcessInstanceVariable(activity.getProcessInstanceUUID(), variableId, maxDate);
      } catch (final InstanceNotFoundException e1) {
        // If activity exists, the process instance must exist too.
        Misc.unreachableStatement();
        return null;
      }
    }
  }

  @Override
  public Map<String, Object> getVariables(final ActivityInstanceUUID activityUUID) throws ActivityNotFoundException {
    final ActivityInstance activity = EnvTool.getAllQueriers(getQueryList()).getActivityInstance(activityUUID);
    if (activity == null) {
      throw new ActivityNotFoundException("bai_QRAPII_14", activityUUID);
    }
    final Date maxDate = getMaxDate(activity);
    try {
      final Map<String, Object> allVariables = new HashMap<String, Object>();
      final Map<String, Object> localVariables = activity.getLastKnownVariableValues();
      final Map<String, Object> globalVariables = getProcessInstanceVariables(activity.getProcessInstanceUUID(),
          maxDate);
      // add global first because if some variables are in both local and global
      // we want to keep local value
      allVariables.putAll(globalVariables);
      allVariables.putAll(localVariables);
      return allVariables;
    } catch (final InstanceNotFoundException e) {
      // If activity exists, the process instance must exist too.
      Misc.unreachableStatement();
      return null;
    }
  }

  private Date getMaxDate(final ActivityInstance activity) {
    final Date endedDate = activity.getEndedDate();
    if (endedDate == null) {
      return new Date();
    }
    return endedDate;
  }

  @Override
  public Set<String> getAttachmentNames(final ProcessInstanceUUID instanceUUID) {
    FacadeUtil.checkArgsNotNull(instanceUUID);
    try {
      final InternalProcessInstance instance = getInternalProcessInstanceWithAttachments(instanceUUID);
      final Set<String> attachmentNames = new HashSet<String>();
      final List<AttachmentInstance> attachments = instance.getAttachments();
      for (final AttachmentInstance attachment : attachments) {
        attachmentNames.add(attachment.getName());
      }
      return attachmentNames;
    } catch (final InstanceNotFoundException e) {
      throw new BonitaRuntimeException(e);
    }
  }

  @Override
  public AttachmentInstance getLastAttachment(final ProcessInstanceUUID instanceUUID, final String attachmentName) {
    FacadeUtil.checkArgsNotNull(instanceUUID, attachmentName);
    final StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("getLastAttachment for process instance ");
    stringBuilder.append(instanceUUID);
    stringBuilder.append(" and name ");
    stringBuilder.append(attachmentName);
    Misc.log(Level.FINE, stringBuilder.toString());
    final DocumentationManager manager = EnvTool.getDocumentationManager();
    final List<org.ow2.bonita.services.Document> documents = DocumentService.getDocuments(manager, instanceUUID,
        attachmentName);
    if (documents.isEmpty()) {
      Misc.log(Level.FINE, "getLastAttachment end (no document found)");
      return null;
    } else {
      final AttachmentInstance attachmentFromDocument = DocumentService.getAttachmentFromDocument(manager,
          documents.get(0));
      Misc.log(Level.FINE, "getLastAttachment end");
      return attachmentFromDocument;
    }
  }

  @Override
  public AttachmentInstance getLastAttachment(final ProcessInstanceUUID instanceUUID, final String attachmentName,
      final ActivityInstanceUUID activityUUID) throws ActivityNotFoundException {
    final Date date = getLastUpdateDate(activityUUID);
    return getLastAttachment(instanceUUID, attachmentName, date);
  }

  private Date getLastUpdateDate(final ActivityInstanceUUID activityUUID) throws ActivityNotFoundException {
    final StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("getActivity for getLastAttachment ");
    stringBuilder.append(activityUUID);
    Misc.log(Level.FINE, stringBuilder.toString());
    final ActivityInstance activity = getActivityInstance(activityUUID);
    Date date = null;
    if (!activity.getState().equals(ActivityState.READY) && !activity.getState().equals(ActivityState.SUSPENDED)
        && !activity.getState().equals(ActivityState.EXECUTING)) {
      date = activity.getLastStateUpdate().getUpdatedDate();
    } else {
      date = new Date();
    }
    return date;
  }

  @Override
  public Document getLastDocument(final ProcessInstanceUUID instanceUUID, final String documentName,
      final ActivityInstanceUUID activityUUID) throws ActivityNotFoundException {
    final Date date = getLastUpdateDate(activityUUID);
    return getLastDocument(instanceUUID, documentName, date);
  }

  @Override
  public AttachmentInstance getLastAttachment(final ProcessInstanceUUID instanceUUID, final String attachmentName,
      final Date date) {
    final DocumentationManager manager = EnvTool.getDocumentationManager();
    final org.ow2.bonita.services.Document doc = getLastServiceDocument(instanceUUID, attachmentName, date, manager);
    if (doc == null) {
      Misc.log(Level.FINE, "getLastDocument end (no document found)");
      return null;
    }
    final AttachmentInstance attachmentFromDocument = DocumentService.getAttachmentFromDocument(manager, doc);
    Misc.log(Level.FINE, "getLastAttachment end");
    return attachmentFromDocument;
  }

  @Override
  public Document getLastDocument(final ProcessInstanceUUID instanceUUID, final String documentName, final Date date) {
    final DocumentationManager manager = EnvTool.getDocumentationManager();
    final org.ow2.bonita.services.Document doc = getLastServiceDocument(instanceUUID, documentName, date, manager);
    if (doc == null) {
      Misc.log(Level.FINE, "getLastDocument end (no document found)");
      return null;
    }
    return DocumentService.getClientDocument(manager, doc);
  }

  private org.ow2.bonita.services.Document getLastServiceDocument(final ProcessInstanceUUID instanceUUID,
      final String documentName, final Date date, final DocumentationManager manager) {
    FacadeUtil.checkArgsNotNull(instanceUUID, documentName, date);

    final StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("getLastDocument for process instance ");
    stringBuilder.append(instanceUUID);
    stringBuilder.append(" and name ");
    stringBuilder.append(documentName);
    stringBuilder.append(" and date ");
    stringBuilder.append(date);
    Misc.log(Level.FINE, stringBuilder.toString());
    final List<org.ow2.bonita.services.Document> documents = DocumentService.getDocuments(manager, instanceUUID,
        documentName);
    final List<org.ow2.bonita.services.Document> allDocuments = new ArrayList<org.ow2.bonita.services.Document>();
    for (final org.ow2.bonita.services.Document document : documents) {
      List<org.ow2.bonita.services.Document> documentVersions;
      try {
        documentVersions = manager.getVersionsOfDocument(document.getId());
        allDocuments.addAll(documentVersions);
      } catch (final DocumentNotFoundException e) {
        throw new BonitaRuntimeException(e);
      }
    }
    org.ow2.bonita.services.Document doc = null;
    for (int i = 0; i < allDocuments.size(); i++) {
      final org.ow2.bonita.services.Document tmp = allDocuments.get(i);
      final long tmpDate = tmp.getCreationDate().getTime();
      if (tmpDate <= date.getTime()) {
        if (doc == null) {
          doc = tmp;
        } else if (doc.getCreationDate().getTime() <= tmpDate) {
          doc = tmp;
        }
      }
    }
    return doc;
  }

  @Override
  public Collection<AttachmentInstance> getLastAttachments(final ProcessInstanceUUID instanceUUID,
      final Set<String> attachmentNames) {
    FacadeUtil.checkArgsNotNull(instanceUUID, attachmentNames);
    final Set<AttachmentInstance> result = new HashSet<AttachmentInstance>();
    for (final String attachmentName : attachmentNames) {
      final AttachmentInstance attachmentInstance = getLastAttachment(instanceUUID, attachmentName);
      if (attachmentInstance != null) {
        result.add(attachmentInstance);
      }
    }
    return result;
  }

  @Override
  public Collection<AttachmentInstance> getLastAttachments(final ProcessInstanceUUID instanceUUID, final String regex) {
    FacadeUtil.checkArgsNotNull(instanceUUID, regex);
    final DocumentationManager manager = EnvTool.getDocumentationManager();
    final List<AttachmentInstance> matchingAttachments = DocumentService
        .getAllAttachmentVersions(manager, instanceUUID);
    final Map<String, AttachmentInstance> result = new HashMap<String, AttachmentInstance>();
    for (final AttachmentInstance attachmentInstance : matchingAttachments) {
      if (attachmentInstance.getName().matches(regex)) {
        result.put(attachmentInstance.getName(), new AttachmentInstanceImpl(attachmentInstance));
      }
    }
    return result.values();
  }

  @Override
  public List<AttachmentInstance> getAttachments(final ProcessInstanceUUID instanceUUID, final String attachmentName) {
    FacadeUtil.checkArgsNotNull(instanceUUID, attachmentName);
    final DocumentationManager manager = EnvTool.getDocumentationManager();
    return DocumentService.getAllAttachmentVersions(manager, instanceUUID, attachmentName);
  }

  @Override
  public byte[] getAttachmentValue(final AttachmentInstance attachmentInstance) {
    final DocumentationManager manager = EnvTool.getDocumentationManager();
    try {
      final org.ow2.bonita.services.Document document = manager.getDocument(attachmentInstance.getUUID().getValue());
      return manager.getContent(document);
    } catch (final DocumentNotFoundException e) {
      throw new BonitaRuntimeException(e);
    }
  }

  @Override
  public List<Comment> getCommentFeed(final ProcessInstanceUUID instanceUUID) {
    final List<Comment> comments = EnvTool.getAllQueriers(getQueryList()).getCommentFeed(instanceUUID);
    return new ArrayList<Comment>(comments);
  }

  @Override
  public List<Comment> getActivityInstanceCommentFeed(final ActivityInstanceUUID activityUUID) {
    FacadeUtil.checkArgsNotNull(activityUUID);
    final List<Comment> comments = EnvTool.getAllQueriers(getQueryList()).getActivityInstanceCommentFeed(activityUUID);
    return new ArrayList<Comment>(comments);
  }

  @Override
  public int getNumberOfActivityInstanceComments(final ActivityInstanceUUID activityUUID) {
    return EnvTool.getAllQueriers(getQueryList()).getNumberOfActivityInstanceComments(activityUUID);
  }

  @Override
  public Map<ActivityInstanceUUID, Integer> getNumberOfActivityInstanceComments(
      final Set<ActivityInstanceUUID> activityUUIDs) {
    if (activityUUIDs == null || activityUUIDs.isEmpty()) {
      return Collections.emptyMap();
    }
    return EnvTool.getAllQueriers(getQueryList()).getNumberOfActivityInstanceComments(activityUUIDs);
  }

  @Override
  public int getNumberOfComments(final ProcessInstanceUUID instanceUUID) throws InstanceNotFoundException {
    return EnvTool.getAllQueriers(getQueryList()).getNumberOfComments(instanceUUID);
  }

  @Override
  public int getNumberOfProcessInstanceComments(final ProcessInstanceUUID instanceUUID) {
    return EnvTool.getAllQueriers(getQueryList()).getNumberOfProcessInstanceComments(instanceUUID);
  }

  @Override
  public List<Comment> getProcessInstanceCommentFeed(final ProcessInstanceUUID instanceUUID) {
    FacadeUtil.checkArgsNotNull(instanceUUID);
    final List<Comment> comments = EnvTool.getAllQueriers(getQueryList()).getProcessInstanceCommentFeed(instanceUUID);
    return new ArrayList<Comment>(comments);
  }

  @Override
  public LightTaskInstance getLightTaskInstance(final ActivityInstanceUUID taskUUID) throws TaskNotFoundException {
    final TaskInstance taskInstance = EnvTool.getAllQueriers(getQueryList()).getTaskInstance(taskUUID);
    if (taskInstance == null) {
      throw new TaskNotFoundException("bai_QRAPII_5", taskUUID);
    }
    return new LightActivityInstanceImpl(taskInstance);
  }

  @Override
  public LightActivityInstance getLightActivityInstance(final ActivityInstanceUUID activityInstanceUUID)
      throws ActivityNotFoundException {
    final ActivityInstance activityInstance = EnvTool.getAllQueriers(getQueryList()).getActivityInstance(
        activityInstanceUUID);
    if (activityInstance == null) {
      throw new ActivityNotFoundException("bai_QRAPII_11", activityInstanceUUID);
    }
    return new LightActivityInstanceImpl(activityInstance);
  }

  @Override
  public int search(final SearchQueryBuilder query) {
    final Class<?> indexClass = getIndexedClass(query);
    return EnvTool.getAllQueriers(getQueryList()).search(query, indexClass);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> List<T> search(final SearchQueryBuilder query, final int firstResult, final int maxResults) {
    final Index index = query.getIndex();
    final Class<?> resultClass = index.getResultClass();
    final Class<?> indexClass = getIndexedClass(query);
    final List<Object> list = EnvTool.getAllQueriers(getQueryList()).search(query, firstResult, maxResults, indexClass);
    if (UserImpl.class.equals(resultClass)) {
      return (List<T>) getUsers(list);
    } else if (LightProcessInstance.class.equals(resultClass)) {
      return (List<T>) getLightProcessInstances(list);
    } else if (LightProcessDefinition.class.equals(resultClass)) {
      return (List<T>) getLightProcessDefinitions(list);
    } else if (LightActivityInstance.class.equals(resultClass)) {
      return (List<T>) getLightActivityInstances(list);
    } else if (GroupImpl.class.equals(resultClass)) {
      return (List<T>) getGroups(list);
    } else if (RoleImpl.class.equals(resultClass)) {
      return (List<T>) getRoles(list);
    } else if (CaseImpl.class.equals(resultClass)) {
      return (List<T>) getCases(list);
    } else {
      return Collections.emptyList();
    }
  }

  private Class<?> getIndexedClass(final SearchQueryBuilder query) {
    final Index index = query.getIndex();
    final Class<?> resultClass = index.getResultClass();
    Class<?> indexClass = null;
    if (UserImpl.class.equals(resultClass)) {
      indexClass = UserImpl.class;
    } else if (LightProcessInstance.class.equals(resultClass)) {
      indexClass = InternalProcessInstance.class;
    } else if (LightProcessDefinition.class.equals(resultClass)) {
      indexClass = InternalProcessDefinition.class;
    } else if (LightActivityInstance.class.equals(resultClass)) {
      indexClass = InternalActivityInstance.class;
    } else if (GroupImpl.class.equals(resultClass)) {
      indexClass = GroupImpl.class;
    } else if (RoleImpl.class.equals(resultClass)) {
      indexClass = RoleImpl.class;
    } else if (CaseImpl.class.equals(resultClass)) {
      indexClass = CaseImpl.class;
    }
    return indexClass;
  }

  private List<LightActivityInstance> getLightActivityInstances(final List<Object> list) {
    final List<LightActivityInstance> result = new ArrayList<LightActivityInstance>();
    for (final Object object : list) {
      result.add(new LightActivityInstanceImpl((InternalActivityInstance) object));
    }
    return result;
  }

  private List<LightProcessInstance> getLightProcessInstances(final List<Object> list) {
    final List<LightProcessInstance> result = new ArrayList<LightProcessInstance>();
    for (final Object object : list) {
      result.add(new LightProcessInstanceImpl((InternalProcessInstance) object));
    }
    return result;
  }

  private List<LightProcessDefinition> getLightProcessDefinitions(final List<Object> list) {
    final List<LightProcessDefinition> result = new ArrayList<LightProcessDefinition>();
    for (final Object object : list) {
      result.add(new LightProcessDefinitionImpl((InternalProcessDefinition) object));
    }
    return result;
  }

  private List<UserImpl> getUsers(final List<Object> list) {
    final List<UserImpl> result = new ArrayList<UserImpl>();
    for (final Object object : list) {
      result.add(new UserImpl((UserImpl) object));
    }
    return result;
  }

  private List<GroupImpl> getGroups(final List<Object> list) {
    final List<GroupImpl> result = new ArrayList<GroupImpl>();
    for (final Object object : list) {
      result.add(new GroupImpl((GroupImpl) object));
    }
    return result;
  }

  private List<RoleImpl> getRoles(final List<Object> list) {
    final List<RoleImpl> result = new ArrayList<RoleImpl>();
    for (final Object object : list) {
      result.add(new RoleImpl((RoleImpl) object));
    }
    return result;
  }

  private List<CaseImpl> getCases(final List<Object> list) {
    final List<CaseImpl> result = new ArrayList<CaseImpl>();
    for (final Object object : list) {
      result.add(new CaseImpl((CaseImpl) object));
    }
    return result;
  }

  @Override
  public List<LightProcessInstance> getLightParentProcessInstancesWithActiveUser(final String username,
      final int fromIndex, final int pageSize, final Set<ProcessDefinitionUUID> processUUIDs) {
    if (processUUIDs == null || processUUIDs.isEmpty()) {
      return Collections.emptyList();
    }
    final List<InternalProcessInstance> processes = getParentProcessInstancesWithActiveUser(username, fromIndex,
        pageSize, processUUIDs);
    final List<LightProcessInstance> result = new ArrayList<LightProcessInstance>();
    for (final ProcessInstance record : processes) {
      result.add(new LightProcessInstanceImpl(record));
    }
    return result;
  }

  @Override
  public List<LightProcessInstance> getLightParentProcessInstancesWithActiveUser(final String username,
      final int fromIndex, final int pageSize, final Set<ProcessDefinitionUUID> processUUIDs,
      final ProcessInstanceCriterion pagingCriterion) {
    List<InternalProcessInstance> processes = null;
    if (processUUIDs == null || processUUIDs.isEmpty()) {
      processes = new ArrayList<InternalProcessInstance>();
    } else {
      processes = getParentProcessInstancesWithActiveUser(username, fromIndex, pageSize, processUUIDs, pagingCriterion);
    }

    final List<LightProcessInstance> result = new ArrayList<LightProcessInstance>();
    for (final ProcessInstance record : processes) {
      result.add(new LightProcessInstanceImpl(record));
    }
    return result;
  }

  @Override
  public List<LightProcessInstance> getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(
      final String username, final int remainingDays, final int fromIndex, final int pageSize,
      final Set<ProcessDefinitionUUID> processUUIDs) {
    final List<InternalProcessInstance> processes = getParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(
        username, remainingDays, fromIndex, pageSize, processUUIDs);
    final List<LightProcessInstance> result = new ArrayList<LightProcessInstance>();
    for (final ProcessInstance record : processes) {
      result.add(new LightProcessInstanceImpl(record));
    }
    return result;
  }

  @Override
  public List<LightProcessInstance> getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(
      final String username, final int remainingDays, final int fromIndex, final int pageSize,
      final Set<ProcessDefinitionUUID> processUUIDs, final ProcessInstanceCriterion pagingCriterion) {
    if (processUUIDs == null || processUUIDs.isEmpty()) {
      return new ArrayList<LightProcessInstance>();
    }

    final List<InternalProcessInstance> processes = getParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(
        username, remainingDays, fromIndex, pageSize, processUUIDs, pagingCriterion);
    final List<LightProcessInstance> result = new ArrayList<LightProcessInstance>();
    for (final ProcessInstance record : processes) {
      result.add(new LightProcessInstanceImpl(record));
    }
    return result;
  }

  @Override
  public List<LightProcessInstance> getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDateExcept(
      final String username, final int remainingDays, final int fromIndex, final int pageSize,
      final Set<ProcessDefinitionUUID> processUUIDs) {
    final Set<ProcessDefinitionUUID> visibleProcesses = EnvTool.getAllQueriers().getAllProcessDefinitionUUIDsExcept(
        processUUIDs);
    return getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(username, remainingDays,
        fromIndex, pageSize, visibleProcesses);
  }

  @Override
  public List<LightProcessInstance> getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDateExcept(
      final String username, final int remainingDays, final int fromIndex, final int pageSize,
      final Set<ProcessDefinitionUUID> processUUIDs, final ProcessInstanceCriterion pagingCriterion) {
    Set<ProcessDefinitionUUID> visibleProcesses = null;
    if (processUUIDs == null || processUUIDs.isEmpty()) {
      visibleProcesses = EnvTool.getAllQueriers().getAllProcessDefinitionUUIDs();
    } else {
      visibleProcesses = EnvTool.getAllQueriers().getAllProcessDefinitionUUIDsExcept(processUUIDs);
    }

    return getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(username, remainingDays,
        fromIndex, pageSize, visibleProcesses, pagingCriterion);
  }

  @Override
  public List<LightProcessInstance> getLightParentProcessInstancesWithActiveUserExcept(final String userId,
      final int fromIndex, final int pageSize, final Set<ProcessDefinitionUUID> processUUIDs) {
    final Set<ProcessDefinitionUUID> visibleProcesses = EnvTool.getAllQueriers().getAllProcessDefinitionUUIDsExcept(
        processUUIDs);
    return getLightParentProcessInstancesWithActiveUser(userId, fromIndex, pageSize, visibleProcesses);
  }

  @Override
  public List<LightProcessInstance> getLightParentProcessInstancesWithActiveUserExcept(final String userId,
      final int fromIndex, final int pageSize, final Set<ProcessDefinitionUUID> processUUIDs,
      final ProcessInstanceCriterion pagingCriterion) {
    Set<ProcessDefinitionUUID> visibleProcesses = null;
    if (processUUIDs == null || processUUIDs.isEmpty()) {
      visibleProcesses = EnvTool.getAllQueriers().getAllProcessDefinitionUUIDs();
    } else {
      visibleProcesses = EnvTool.getAllQueriers().getAllProcessDefinitionUUIDsExcept(processUUIDs);
    }
    return getLightParentProcessInstancesWithActiveUser(userId, fromIndex, pageSize, visibleProcesses, pagingCriterion);
  }

  @Override
  public List<LightProcessInstance> getLightParentProcessInstancesWithInvolvedUserExcept(final String username,
      final int fromIndex, final int pageSize, final Set<ProcessDefinitionUUID> processUUIDs) {
    final Set<ProcessDefinitionUUID> visibleProcesses = EnvTool.getAllQueriers().getAllProcessDefinitionUUIDsExcept(
        processUUIDs);
    return getLightParentProcessInstancesWithInvolvedUser(username, fromIndex, pageSize, visibleProcesses);
  }

  @Override
  public List<LightProcessInstance> getLightParentProcessInstancesWithInvolvedUserExcept(final String username,
      final int fromIndex, final int pageSize, final Set<ProcessDefinitionUUID> processUUIDs,
      final ProcessInstanceCriterion pagingCriterion) {
    Set<ProcessDefinitionUUID> visibleProcesses = null;
    if (processUUIDs == null || processUUIDs.isEmpty()) {
      visibleProcesses = EnvTool.getAllQueriers().getAllProcessDefinitionUUIDs();
    } else {
      visibleProcesses = EnvTool.getAllQueriers().getAllProcessDefinitionUUIDsExcept(processUUIDs);
    }
    return getLightParentProcessInstancesWithInvolvedUser(username, fromIndex, pageSize, visibleProcesses,
        pagingCriterion);
  }

  @Override
  public List<LightProcessInstance> getLightParentProcessInstancesWithOverdueTasks(final String username,
      final int fromIndex, final int pageSize, final Set<ProcessDefinitionUUID> processUUIDs) {
    final List<InternalProcessInstance> processes = getParentProcessInstancesWithOverdueTasks(username, fromIndex,
        pageSize, processUUIDs, ProcessInstanceCriterion.DEFAULT);
    final List<LightProcessInstance> result = new ArrayList<LightProcessInstance>();
    for (final ProcessInstance record : processes) {
      result.add(new LightProcessInstanceImpl(record));
    }
    return result;
  }

  @Override
  public List<LightProcessInstance> getLightParentProcessInstancesWithOverdueTasks(final String username,
      final int fromIndex, final int pageSize, final Set<ProcessDefinitionUUID> processUUIDs,
      final ProcessInstanceCriterion pagingCriterion) {
    if (processUUIDs == null || processUUIDs.isEmpty()) {
      return Collections.emptyList();
    }

    final List<InternalProcessInstance> processes = getParentProcessInstancesWithOverdueTasks(username, fromIndex,
        pageSize, processUUIDs, pagingCriterion);
    final List<LightProcessInstance> result = new ArrayList<LightProcessInstance>();
    for (final ProcessInstance record : processes) {
      result.add(new LightProcessInstanceImpl(record));
    }
    return result;
  }

  @Override
  public List<LightProcessInstance> getLightParentProcessInstancesWithOverdueTasksExcept(final String username,
      final int fromIndex, final int pageSize, final Set<ProcessDefinitionUUID> processUUIDs) {
    final Set<ProcessDefinitionUUID> visibleProcesses = EnvTool.getAllQueriers().getAllProcessDefinitionUUIDsExcept(
        processUUIDs);
    return getLightParentProcessInstancesWithOverdueTasks(username, fromIndex, pageSize, visibleProcesses);
  }

  @Override
  public List<LightProcessInstance> getLightParentProcessInstancesWithOverdueTasksExcept(final String username,
      final int fromIndex, final int pageSize, final Set<ProcessDefinitionUUID> processUUIDs,
      final ProcessInstanceCriterion pagingCriterion) {
    Set<ProcessDefinitionUUID> visibleProcesses = null;
    if (processUUIDs == null || processUUIDs.isEmpty()) {
      visibleProcesses = EnvTool.getAllQueriers().getAllProcessDefinitionUUIDs();
    } else {
      visibleProcesses = EnvTool.getAllQueriers().getAllProcessDefinitionUUIDsExcept(processUUIDs);
    }
    return getLightParentProcessInstancesWithOverdueTasks(username, fromIndex, pageSize, visibleProcesses,
        pagingCriterion);
  }

  @Override
  public List<LightProcessInstance> getLightParentUserInstances(final int fromIndex, final int pageSize,
      final Set<ProcessDefinitionUUID> processUUIDs) {
    final List<InternalProcessInstance> processes = getUserParentProcessInstances(fromIndex, pageSize, processUUIDs);
    final List<LightProcessInstance> result = new ArrayList<LightProcessInstance>();
    for (final ProcessInstance record : processes) {
      result.add(new LightProcessInstanceImpl(record));
    }
    return result;
  }

  @Override
  public List<LightProcessInstance> getLightParentUserInstances(final int fromIndex, final int pageSize,
      final Set<ProcessDefinitionUUID> processUUIDs, final ProcessInstanceCriterion pagingCriterion) {
    List<InternalProcessInstance> processes = null;
    if (processUUIDs == null || processUUIDs.isEmpty()) {
      processes = new ArrayList<InternalProcessInstance>();
    } else {
      processes = getUserParentProcessInstances(fromIndex, pageSize, processUUIDs, pagingCriterion);
    }
    final List<LightProcessInstance> result = new ArrayList<LightProcessInstance>();
    for (final ProcessInstance record : processes) {
      result.add(new LightProcessInstanceImpl(record));
    }
    return result;
  }

  @Override
  public List<LightProcessInstance> getLightParentUserInstancesExcept(final int fromIndex, final int pageSize,
      final Set<ProcessDefinitionUUID> processUUIDs) {
    Set<ProcessDefinitionUUID> visibleProcesses = null;
    if (processUUIDs == null || processUUIDs.isEmpty()) {
      visibleProcesses = EnvTool.getAllQueriers().getAllProcessDefinitionUUIDs();
    } else {
      visibleProcesses = EnvTool.getAllQueriers().getAllProcessDefinitionUUIDsExcept(processUUIDs);
    }
    return getLightParentUserInstances(fromIndex, pageSize, visibleProcesses);
  }

  @Override
  public List<LightProcessInstance> getLightParentUserInstancesExcept(final int fromIndex, final int pageSize,
      final Set<ProcessDefinitionUUID> processUUIDs, final ProcessInstanceCriterion pagingCriterion) {
    final Set<ProcessDefinitionUUID> visibleProcesses = EnvTool.getAllQueriers().getAllProcessDefinitionUUIDsExcept(
        processUUIDs);
    return getLightParentUserInstances(fromIndex, pageSize, visibleProcesses, pagingCriterion);
  }

  @Override
  public Integer getNumberOfParentProcessInstancesWithActiveUser(final String username,
      final Set<ProcessDefinitionUUID> processUUIDs) {
    final boolean access = EnvTool.isRestrictedApplicationAcces();
    if (access) {
      final String applicationName = EnvTool.getApplicationAccessName();
      if (applicationName != null) {
        Set<ProcessDefinitionUUID> visibleProcessUUIDs = FacadeUtil.getAllowedProcessUUIDsFor(applicationName,
            RuleType.PROCESS_READ);
        if (visibleProcessUUIDs == null) {
          visibleProcessUUIDs = new HashSet<ProcessDefinitionUUID>();
        }
        visibleProcessUUIDs.retainAll(processUUIDs);
        if (visibleProcessUUIDs != null && !visibleProcessUUIDs.isEmpty()) {
          return EnvTool.getAllQueriers(getQueryList()).getNumberOfParentProcessInstancesWithActiveUser(username,
              visibleProcessUUIDs);
        }
      }
      return 0;
    } else {
      return EnvTool.getAllQueriers(getQueryList()).getNumberOfParentProcessInstancesWithActiveUser(username,
          processUUIDs);
    }
  }

  @Override
  public Integer getNumberOfParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(
      final String username, final int remainingDays, final Set<ProcessDefinitionUUID> processUUIDs) {
    final boolean access = EnvTool.isRestrictedApplicationAcces();
    final Date currentDate = new Date();
    final Date beginningOfTheDay = DateUtil.getBeginningOfTheDay(currentDate);
    final Date atRisk = DateUtil.backTo(beginningOfTheDay, -(remainingDays + 1));
    if (access) {
      final String applicationName = EnvTool.getApplicationAccessName();
      if (applicationName != null) {
        Set<ProcessDefinitionUUID> visibleProcessUUIDs = FacadeUtil.getAllowedProcessUUIDsFor(applicationName,
            RuleType.PROCESS_READ);
        if (visibleProcessUUIDs == null) {
          visibleProcessUUIDs = new HashSet<ProcessDefinitionUUID>();
        }
        visibleProcessUUIDs.retainAll(processUUIDs);
        if (visibleProcessUUIDs != null && !visibleProcessUUIDs.isEmpty()) {
          return EnvTool.getAllQueriers(getQueryList())
              .getNumberOfParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(username, currentDate,
                  atRisk, visibleProcessUUIDs);
        }
      }
      return 0;
    } else {
      return EnvTool.getAllQueriers(getQueryList())
          .getNumberOfParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(username, currentDate,
              atRisk, processUUIDs);
    }
  }

  @Override
  public Integer getNumberOfParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDateExcept(
      final String username, final int remainingDays, final Set<ProcessDefinitionUUID> processUUIDs) {
    final Set<ProcessDefinitionUUID> visibleProcesses = EnvTool.getAllQueriers().getAllProcessDefinitionUUIDsExcept(
        processUUIDs);
    return getNumberOfParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(username, remainingDays,
        visibleProcesses);
  }

  @Override
  public Integer getNumberOfParentProcessInstancesWithActiveUserExcept(final String username,
      final Set<ProcessDefinitionUUID> processUUIDs) {
    final Set<ProcessDefinitionUUID> visibleProcesses = EnvTool.getAllQueriers().getAllProcessDefinitionUUIDsExcept(
        processUUIDs);
    return getNumberOfParentProcessInstancesWithActiveUser(username, visibleProcesses);
  }

  @Override
  public Integer getNumberOfParentProcessInstancesWithInvolvedUser(final String username,
      final Set<ProcessDefinitionUUID> processUUIDs) {
    final boolean access = EnvTool.isRestrictedApplicationAcces();
    if (access) {
      final String applicationName = EnvTool.getApplicationAccessName();
      if (applicationName != null) {
        Set<ProcessDefinitionUUID> visibleProcessUUIDs = FacadeUtil.getAllowedProcessUUIDsFor(applicationName,
            RuleType.PROCESS_READ);
        if (visibleProcessUUIDs == null) {
          visibleProcessUUIDs = new HashSet<ProcessDefinitionUUID>();
        }
        visibleProcessUUIDs.retainAll(processUUIDs);
        if (visibleProcessUUIDs != null && !visibleProcessUUIDs.isEmpty()) {
          return EnvTool.getAllQueriers(getQueryList()).getNumberOfParentProcessInstancesWithInvolvedUser(username,
              visibleProcessUUIDs);
        }
      }
      return 0;
    } else {
      return EnvTool.getAllQueriers(getQueryList()).getNumberOfParentProcessInstancesWithInvolvedUser(username,
          processUUIDs);
    }
  }

  @Override
  public Integer getNumberOfParentProcessInstancesWithInvolvedUserExcept(final String username,
      final Set<ProcessDefinitionUUID> processUUIDs) {
    final Set<ProcessDefinitionUUID> visibleProcesses = EnvTool.getAllQueriers().getAllProcessDefinitionUUIDsExcept(
        processUUIDs);
    return getNumberOfParentProcessInstancesWithInvolvedUser(username, visibleProcesses);
  }

  @Override
  public Integer getNumberOfParentProcessInstancesWithOverdueTasks(final String username,
      final Set<ProcessDefinitionUUID> processUUIDs) {
    final boolean access = EnvTool.isRestrictedApplicationAcces();
    final Date currentDate = new Date();
    if (access) {
      final String applicationName = EnvTool.getApplicationAccessName();
      if (applicationName != null) {
        Set<ProcessDefinitionUUID> visibleProcessUUIDs = FacadeUtil.getAllowedProcessUUIDsFor(applicationName,
            RuleType.PROCESS_READ);
        if (visibleProcessUUIDs == null) {
          visibleProcessUUIDs = new HashSet<ProcessDefinitionUUID>();
        }
        visibleProcessUUIDs.retainAll(processUUIDs);
        if (visibleProcessUUIDs != null && !visibleProcessUUIDs.isEmpty()) {
          return EnvTool.getAllQueriers(getQueryList()).getNumberOfParentProcessInstancesWithOverdueTasks(username,
              currentDate, visibleProcessUUIDs);
        }
      }
      return 0;
    } else {
      return EnvTool.getAllQueriers(getQueryList()).getNumberOfParentProcessInstancesWithOverdueTasks(username,
          currentDate, processUUIDs);
    }
  }

  @Override
  public Integer getNumberOfParentProcessInstancesWithOverdueTasksExcept(final String username,
      final Set<ProcessDefinitionUUID> processUUIDs) {
    final Set<ProcessDefinitionUUID> visibleProcesses = EnvTool.getAllQueriers().getAllProcessDefinitionUUIDsExcept(
        processUUIDs);
    return getNumberOfParentProcessInstancesWithOverdueTasks(username, visibleProcesses);
  }

  @Override
  public Integer getNumberOfParentProcessInstancesWithStartedBy(final String username,
      final Set<ProcessDefinitionUUID> processUUIDs) {
    final boolean access = EnvTool.isRestrictedApplicationAcces();
    if (access) {
      final String applicationName = EnvTool.getApplicationAccessName();
      if (applicationName != null) {
        Set<ProcessDefinitionUUID> visibleProcessUUIDs = FacadeUtil.getAllowedProcessUUIDsFor(applicationName,
            RuleType.PROCESS_READ);
        if (visibleProcessUUIDs == null) {
          visibleProcessUUIDs = new HashSet<ProcessDefinitionUUID>();
        }
        visibleProcessUUIDs.retainAll(processUUIDs);
        if (visibleProcessUUIDs != null && !visibleProcessUUIDs.isEmpty()) {
          return EnvTool.getAllQueriers(getQueryList()).getNumberOfParentProcessInstancesWithStartedBy(username,
              visibleProcessUUIDs);
        }
      }
      return 0;
    } else {
      return EnvTool.getAllQueriers(getQueryList()).getNumberOfParentProcessInstancesWithStartedBy(username,
          processUUIDs);
    }
  }

  @Override
  public Integer getNumberOfParentProcessInstancesWithStartedByExcept(final String username,
      final Set<ProcessDefinitionUUID> processUUIDs) {
    final Set<ProcessDefinitionUUID> visibleProcesses = EnvTool.getAllQueriers().getAllProcessDefinitionUUIDsExcept(
        processUUIDs);
    return getNumberOfParentProcessInstancesWithStartedBy(username, visibleProcesses);
  }

  @Override
  public Integer getNumberOfParentProcessInstancesWithInvolvedUserAndCategory(final String username,
      final String category, final Set<ProcessDefinitionUUID> processUUIDs) {
    final boolean access = EnvTool.isRestrictedApplicationAcces();
    final Set<ProcessDefinitionUUID> targetedProcesses = EnvTool.getAllQueriers(getQueryList())
        .getProcessUUIDsFromCategory(category);
    if (access) {
      final String applicationName = EnvTool.getApplicationAccessName();
      if (applicationName != null) {
        final Set<ProcessDefinitionUUID> visibleProcessUUIDs = FacadeUtil.getAllowedProcessUUIDsFor(applicationName,
            RuleType.PROCESS_READ);
        visibleProcessUUIDs.retainAll(targetedProcesses);
        visibleProcessUUIDs.retainAll(processUUIDs);
        if (!visibleProcessUUIDs.isEmpty()) {
          return EnvTool.getAllQueriers(getQueryList()).getNumberOfParentProcessInstancesWithInvolvedUser(username,
              visibleProcessUUIDs);
        }
      }
      return 0;
    } else {
      targetedProcesses.retainAll(processUUIDs);
      if (targetedProcesses.isEmpty()) {
        return 0;
      }
      return EnvTool.getAllQueriers(getQueryList()).getNumberOfParentProcessInstancesWithInvolvedUser(username,
          targetedProcesses);
    }
  }

  @Override
  public Integer getNumberOfParentProcessInstancesWithInvolvedUserAndCategoryExcept(final String username,
      final String category, final Set<ProcessDefinitionUUID> processUUIDs) {
    final Set<ProcessDefinitionUUID> visibleProcesses = EnvTool.getAllQueriers().getAllProcessDefinitionUUIDsExcept(
        processUUIDs);
    return getNumberOfParentProcessInstancesWithInvolvedUserAndCategory(username, category, visibleProcesses);
  }

  @Override
  public Set<String> getActiveUsersOfProcessInstance(final ProcessInstanceUUID uuid) throws InstanceNotFoundException {
    final ProcessInstance instance = getInternalProcessInstanceWithoutAttachements(uuid);
    if (instance == null) {
      throw new InstanceNotFoundException("bai_QRAPII_1", uuid);
    }
    return instance.getActiveUsers();
  }

  @Override
  public Map<ProcessInstanceUUID, Set<String>> getActiveUsersOfProcessInstances(
      final Set<ProcessInstanceUUID> instanceUUIDs) throws InstanceNotFoundException {
    FacadeUtil.checkArgsNotNull(instanceUUIDs);
    final HashMap<ProcessInstanceUUID, Set<String>> result = new HashMap<ProcessInstanceUUID, Set<String>>();
    for (final ProcessInstanceUUID processInstanceUUID : instanceUUIDs) {
      result.put(processInstanceUUID, getActiveUsersOfProcessInstance(processInstanceUUID));
    }
    return result;
  }

  @Override
  public CatchingEvent getEvent(final CatchingEventUUID eventUUID) throws EventNotFoundException {
    final EventService eventService = EnvTool.getEventService();
    final long jobId = Long.parseLong(eventUUID.getValue());
    final Job job = eventService.getJob(jobId);
    if (job == null) {
      throw new EventNotFoundException("Event " + jobId + "does not exist.");
    }
    return getEvent(job);
  }

  @Override
  public Set<CatchingEvent> getEvents() {
    final EventService eventService = EnvTool.getEventService();
    final List<Job> jobs = eventService.getTimerJobs();
    return getEvents(jobs);
  }

  @Override
  public Set<CatchingEvent> getEvents(final ProcessInstanceUUID instanceUUID) {
    final EventService eventService = EnvTool.getEventService();
    final List<Job> jobs = eventService.getTimerJobs(instanceUUID);
    return getEvents(jobs);
  }

  @Override
  public Set<CatchingEvent> getEvents(final ActivityInstanceUUID activityUUID) {
    final EventService eventService = EnvTool.getEventService();
    final Execution exec = EnvTool.getJournal().getExecutionOnActivity(activityUUID.getProcessInstanceUUID(),
        activityUUID);
    if (exec == null) {
      return Collections.emptySet();
    }
    final List<Job> jobs = eventService.getTimerJobs(exec.getEventUUID());
    return getEvents(jobs);
  }

  private Set<CatchingEvent> getEvents(final List<Job> jobs) {
    final Set<CatchingEvent> events = new HashSet<CatchingEvent>();
    for (final Job job : jobs) {
      final CatchingEvent event = getEvent(job);
      if (event != null) {
        events.add(event);
      }
    }
    return events;
  }

  private CatchingEvent getEvent(final Job job) {
    final String eventPosition = job.getEventPosition();
    CatchingEventImpl event = null;
    final CatchingEventUUID uuid = new CatchingEventUUID(String.valueOf(job.getId()));
    Position position = null;
    if (EventConstants.START.equals(eventPosition)) {
      position = Position.START;
    } else if (EventConstants.INTERMEDIATE.equals(eventPosition)) {
      position = Position.INTERMEDIATE;
    } else if (EventConstants.BOUNDARY.equals(eventPosition)) {
      position = Position.BOUNDARY;
    } else if (EventConstants.DEADLINE.equals(eventPosition)) {
      position = Position.DEADLINE;
    }
    event = new CatchingEventImpl(uuid, position, Type.TIMER, job.getFireTime(), job.getActivityDefinitionUUID(), null,
        job.getInstanceUUID(), null, null);
    return event;
  }

  @Override
  public byte[] getDocumentContent(final DocumentUUID documentUUID) throws DocumentNotFoundException {
    final DocumentationManager manager = EnvTool.getDocumentationManager();
    final String documentId = documentUUID.getValue();
    final org.ow2.bonita.services.Document document = manager.getDocument(documentId);
    if (document == null) {
      throw new DocumentNotFoundException(documentId);
    }
    return manager.getContent(document);
  }

  @Override
  public DocumentResult searchDocuments(final DocumentSearchBuilder builder, final int fromResult, final int MaxResults) {
    final DocumentationManager manager = EnvTool.getDocumentationManager();
    final SearchResult searchResult = manager.search(builder, fromResult, MaxResults);

    final List<org.ow2.bonita.services.Document> searchDocuments = searchResult.getDocuments();
    final List<Document> documents = new ArrayList<Document>();
    for (int i = 0; i < searchDocuments.size(); i++) {
      final org.ow2.bonita.services.Document searchDocument = searchDocuments.get(i);
      documents.add(DocumentService.getClientDocument(manager, searchDocument));
    }
    final int count = searchResult.getCount();
    return new DocumentResult(count, documents);
  }

  @Override
  public Document getDocument(final DocumentUUID documentUUID) throws DocumentNotFoundException {
    FacadeUtil.checkArgsNotNull(documentUUID);
    final DocumentationManager manager = EnvTool.getDocumentationManager();
    return getDocument(documentUUID, manager);
  }

  @Override
  public List<Document> getDocuments(final List<DocumentUUID> documentUUIDs) throws DocumentNotFoundException {
    FacadeUtil.checkArgsNotNull(documentUUIDs);
    final List<Document> documents = new ArrayList<Document>();
    final DocumentationManager manager = EnvTool.getDocumentationManager();
    for (int i = 0; i < documentUUIDs.size(); i++) {
      final DocumentUUID documentUUID = documentUUIDs.get(i);
      final Document document = getDocument(documentUUID, manager);
      documents.add(document);
    }
    return documents;
  }

  public Document getDocument(final DocumentUUID documentUUID, final DocumentationManager manager)
      throws DocumentNotFoundException {
    final org.ow2.bonita.services.Document document = manager.getDocument(documentUUID.getValue());
    return DocumentService.getClientDocument(manager, document);
  }

  @Override
  public List<Document> getDocumentVersions(final DocumentUUID documentUUID) throws DocumentNotFoundException {
    FacadeUtil.checkArgsNotNull(documentUUID);
    final DocumentationManager manager = EnvTool.getDocumentationManager();
    final List<org.ow2.bonita.services.Document> documentVersions = manager.getVersionsOfDocument(documentUUID
        .getValue());
    final List<Document> documents = new ArrayList<Document>();
    for (int i = 0; i < documentVersions.size(); i++) {
      final org.ow2.bonita.services.Document documentVersion = documentVersions.get(i);
      documents.add(DocumentService.getClientDocument(manager, documentVersion));
    }
    return documents;
  }

  @Override
  public Set<String> getInvolvedUsersOfProcessInstance(final ProcessInstanceUUID instanceUUID)
      throws InstanceNotFoundException {
    final ProcessInstance instance = getInternalProcessInstanceWithoutAttachements(instanceUUID);
    if (instance == null) {
      throw new InstanceNotFoundException("bai_QRAPII_1", instanceUUID);
    }
    return new HashSet<String>(instance.getInvolvedUsers());
  }

  @Override
  public Set<ProcessInstanceUUID> getChildrenInstanceUUIDsOfProcessInstance(final ProcessInstanceUUID instanceUUID)
      throws InstanceNotFoundException {
    final ProcessInstance instance = getInternalProcessInstanceWithoutAttachements(instanceUUID);
    if (instance == null) {
      throw new InstanceNotFoundException("bai_QRAPII_1", instanceUUID);
    }
    return new HashSet<ProcessInstanceUUID>(instance.getChildrenInstanceUUID());
  }

}
