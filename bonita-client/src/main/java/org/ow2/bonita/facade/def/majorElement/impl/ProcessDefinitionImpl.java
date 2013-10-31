/**
 * Copyright (C) 2006  Bull S. A. S.
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
 * Modified by Matthieu Chaffotte - BonitaSoft S.A.
 **/
package org.ow2.bonita.facade.def.majorElement.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.ow2.bonita.facade.def.element.AttachmentDefinition;
import org.ow2.bonita.facade.def.element.ConnectorDefinition;
import org.ow2.bonita.facade.def.element.HookDefinition;
import org.ow2.bonita.facade.def.element.impl.AttachmentDefinitionImpl;
import org.ow2.bonita.facade.def.element.impl.ConnectorDefinitionImpl;
import org.ow2.bonita.facade.def.element.impl.IterationDescriptor;
import org.ow2.bonita.facade.def.majorElement.ActivityDefinition;
import org.ow2.bonita.facade.def.majorElement.DataFieldDefinition;
import org.ow2.bonita.facade.def.majorElement.EventProcessDefinition;
import org.ow2.bonita.facade.def.majorElement.ParticipantDefinition;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.def.majorElement.TransitionDefinition;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.light.impl.LightProcessDefinitionImpl;
import org.ow2.bonita.util.CopyTool;
import org.ow2.bonita.util.Misc;

public class ProcessDefinitionImpl extends LightProcessDefinitionImpl implements ProcessDefinition {

  private static final long serialVersionUID = -572795239631090498L;

  protected Set<DataFieldDefinition> dataFields;
  protected Set<ParticipantDefinition> participants;
  protected Set<ActivityDefinition> activities;
  protected Set<AttachmentDefinition> attachments;
  protected Set<String> subProcesses;
  protected List<EventProcessDefinition> eventSubProcesses;

  protected Map<String, String> metadata;
  protected List<HookDefinition> connectors;
  protected Set<IterationDescriptor> iterationDescriptors;

  protected ProcessDefinitionImpl() {
    super();
  }

  public ProcessDefinitionImpl(final String name, final String version) {
    super(name, version);
  }

  public ProcessDefinitionImpl(final ProcessDefinition src) {
    super(src);

    final ClassLoader current = Thread.currentThread().getContextClassLoader();
    try {
      final ProcessDefinitionImpl srcImpl = (ProcessDefinitionImpl) src;
      Thread.currentThread().setContextClassLoader(srcImpl.getClassLoader(src.getUUID()));

      final Set<DataFieldDefinition> dataFields = src.getDataFields();
      if (dataFields != null) {
        this.dataFields = new HashSet<DataFieldDefinition>();
        for (final DataFieldDefinition d : dataFields) {
          this.dataFields.add(new DataFieldDefinitionImpl(d));
        }
      }
      final List<HookDefinition> connectors = src.getConnectors();
      if (connectors != null) {
        this.connectors = new ArrayList<HookDefinition>();
        for (final HookDefinition d : connectors) {
          this.connectors.add(new ConnectorDefinitionImpl(d));
        }
      }
      final Set<ParticipantDefinition> participants = src.getParticipants();
      if (participants != null) {
        this.participants = new HashSet<ParticipantDefinition>();
        for (final ParticipantDefinition d : participants) {
          this.participants.add(new ParticipantDefinitionImpl(d));
        }
      }
      final Set<ActivityDefinition> activities = src.getActivities();
      if (activities != null) {
        this.activities = new HashSet<ActivityDefinition>();
        for (final ActivityDefinition d : activities) {
          this.activities.add(new ActivityDefinitionImpl(d));
        }
      }

      metadata = new HashMap<String, String>();
      final Map<String, String> meta = src.getMetaData();
      for (final Entry<String, String> entry : meta.entrySet()) {
        metadata.put(entry.getKey(), entry.getValue());
      }

      final Map<String, AttachmentDefinition> other = src.getAttachments();
      if (!other.isEmpty()) {
        attachments = new HashSet<AttachmentDefinition>();
        for (final AttachmentDefinition attach : other.values()) {
          attachments.add(new AttachmentDefinitionImpl(attach));
        }
      }
      if (src.getIterationDescriptors() != null) {
        iterationDescriptors = new HashSet<IterationDescriptor>();
        for (final IterationDescriptor id : src.getIterationDescriptors()) {
          iterationDescriptors.add(new IterationDescriptor(id));
        }
      }

      final List<EventProcessDefinition> eventSubProcesses = src.getEventSubProcesses();
      this.eventSubProcesses = new ArrayList<EventProcessDefinition>();
      for (final EventProcessDefinition d : eventSubProcesses) {
        this.eventSubProcesses.add(new EventProcessDefinitionImpl(d));
      }

      subProcesses = CopyTool.copy(src.getSubProcesses());
    } finally {
      Thread.currentThread().setContextClassLoader(current);
    }
  }

  protected ClassLoader getClassLoader(final ProcessDefinitionUUID processUUID) {
    return Thread.currentThread().getContextClassLoader();
  }

  @Override
  public String toString() {
    String st = this.getClass().getName() + "[uuid: " + getUUID() + ", name:" + getName() + ", description:"
        + getDescription() + ", version:" + getVersion();
    st += "]";
    return st;
  }

  @Override
  public Set<String> getClassDependencies() {
    final Set<String> classDependencies = new HashSet<String>();
    for (final ParticipantDefinition participant : getParticipants()) {
      if (participant.getRoleMapper() != null) {
        classDependencies.add(participant.getRoleMapper().getClassName());
      }
    }
    for (final ConnectorDefinition connector : getConnectors()) {
      classDependencies.add(connector.getClassName());
    }
    for (final ActivityDefinition activity : getActivities()) {
      classDependencies.addAll(activity.getClassDependencies());
    }

    return classDependencies;
  }

  @Override
  public Set<String> getProcessDependencies() {
    final Set<String> processDependencies = new HashSet<String>();
    for (final ActivityDefinition activity : getActivities()) {
      if (activity.getSubflowProcessName() != null) {
        processDependencies.add(activity.getSubflowProcessName());
      }
    }
    return processDependencies;
  }

  @Override
  public Set<DataFieldDefinition> getDataFields() {
    if (dataFields == null) {
      return Collections.emptySet();
    }
    return dataFields;
  }

  @Override
  public Set<String> getSubProcesses() {
    if (subProcesses == null) {
      return Collections.emptySet();
    }
    return subProcesses;
  }

  public void setSubProcesses(final Set<String> subProcesses) {
    this.subProcesses = subProcesses;
  }

  @Override
  public Set<ParticipantDefinition> getParticipants() {
    if (participants == null) {
      return Collections.emptySet();
    }
    return participants;
  }

  @Override
  public Set<ActivityDefinition> getActivities() {
    if (activities == null) {
      return Collections.emptySet();
    }
    return activities;
  }

  @Override
  public Set<TransitionDefinition> getTransitions() {
    if (activities == null) {
      return Collections.emptySet();
    }
    final Set<TransitionDefinition> transitions = new HashSet<TransitionDefinition>();
    for (final ActivityDefinition activity : getActivities()) {
      final Set<TransitionDefinition> activityTransitions = activity.getOutgoingTransitions();
      for (final TransitionDefinition transition : activityTransitions) {
        transitions.add(transition);
      }
    }
    return transitions;
  }

  @Override
  public Map<String, String> getMetaData() {
    if (metadata == null) {
      return Collections.emptyMap();
    }
    return metadata;
  }

  @Override
  public String getAMetaData(final String key) {
    Misc.checkArgsNotNull(key);
    if (metadata == null) {
      return null;
    }
    return metadata.get(key);
  }

  @Override
  public List<HookDefinition> getConnectors() {
    if (connectors == null) {
      return Collections.emptyList();
    }
    return connectors;
  }

  /**
   * SETTERS
   */

  public void setState(final ProcessState state) {
    this.state = state;
  }

  public void setType(final ProcessType type) {
    this.type = type;
  }

  public void setUndeployedDate(final Date undeployedDate) {
    this.undeployedDate = Misc.getTime(undeployedDate);
  }

  public void setUndeployedBy(final String undeployedBy) {
    this.undeployedBy = undeployedBy;
  }

  public void addData(final DataFieldDefinition data) {
    if (dataFields == null) {
      dataFields = new HashSet<DataFieldDefinition>();
    }
    dataFields.add(data);
  }

  public void addGroup(final ParticipantDefinitionImpl group) {
    if (participants == null) {
      participants = new HashSet<ParticipantDefinition>();
    }
    participants.add(group);
  }

  public void addActivity(final ActivityDefinition activity) {
    if (activities == null) {
      activities = new HashSet<ActivityDefinition>();
    }
    activities.add(activity);
  }

  public void setDeployedDate(final Date deployedDate) {
    this.deployedDate = Misc.getTime(deployedDate);
  }

  public void setDeployedBy(final String deployedBy) {
    this.deployedBy = deployedBy;
  }

  public void deleteAMetaData(final String key) {
    Misc.checkArgsNotNull(key);
    metadata.remove(key);
  }

  public void addAMetaData(final String key, final String value) {
    Misc.checkArgsNotNull(key, value);
    if (metadata == null) {
      metadata = new HashMap<String, String>();
    }
    metadata.put(key, value);
  }

  public void addAttachment(final AttachmentDefinition attach) {
    Misc.checkArgsNotNull(attach);
    if (attachments == null) {
      attachments = new HashSet<AttachmentDefinition>();
    }
    attachments.add(attach);
  }

  public void addConnector(final HookDefinition connector) {
    if (connectors == null) {
      connectors = new ArrayList<HookDefinition>();
    }
    connectors.add(connector);
  }

  @Override
  public ActivityDefinition getActivity(final String name) {
    for (final ActivityDefinition activity : getActivities()) {
      if (activity.getName().equals(name)) {
        return activity;
      }
    }
    return null;
  }

  @Override
  public DataFieldDefinition getDatafield(final String name) {
    for (final DataFieldDefinition datafield : getDataFields()) {
      if (datafield.getName().equals(name)) {
        return datafield;
      }
    }
    return null;
  }

  @Override
  public AttachmentDefinition getAttachment(final String name) {
    return getAttachments().get(name);
  }

  @Override
  public Map<String, AttachmentDefinition> getAttachments() {
    if (attachments == null) {
      return Collections.emptyMap();
    }
    final Map<String, AttachmentDefinition> result = new HashMap<String, AttachmentDefinition>();
    for (final AttachmentDefinition attach : attachments) {
      result.put(attach.getName(), attach);
    }
    return result;
  }

  @Override
  public Map<String, ActivityDefinition> getFinalActivities() {
    final Map<String, ActivityDefinition> result = new HashMap<String, ActivityDefinition>();
    for (final ActivityDefinition activity : getActivities()) {
      if (!activity.hasOutgoingTransitions()) {
        result.put(activity.getName(), activity);
      }
    }
    return result;
  }

  @Override
  public Map<String, ActivityDefinition> getInitialActivities() {
    final Map<String, ActivityDefinition> result = new HashMap<String, ActivityDefinition>();
    for (final ActivityDefinition activity : getActivities()) {
      if (!activity.hasIncomingTransitions()) {
        result.put(activity.getName(), activity);
      }
    }
    return result;
  }

  @Override
  public Set<IterationDescriptor> getIterationDescriptors() {
    if (iterationDescriptors == null) {
      return Collections.emptySet();
    }
    return iterationDescriptors;
  }

  public void addIterationDescriptors(final IterationDescriptor iterationDescriptor) {
    if (iterationDescriptors == null) {
      iterationDescriptors = new HashSet<IterationDescriptor>();
    }
    iterationDescriptors.add(iterationDescriptor);
  }

  public boolean containsIterationDescriptor(final IterationDescriptor itDesc) {
    for (final IterationDescriptor id : getIterationDescriptors()) {
      if (id.equals(itDesc)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public Set<IterationDescriptor> getIterationDescriptors(final String activityName) {
    final Set<IterationDescriptor> result = new HashSet<IterationDescriptor>();
    for (final IterationDescriptor id : getIterationDescriptors()) {
      if (id.containsNode(activityName)) {
        result.add(id);
      }
    }
    return result;
  }

  public void addCategory(final String categoryName) {
    if (categoryName != null && categoryName.trim().length() > 0) {
      if (categories == null) {
        categories = new HashSet<String>();
      }
      categories.add(categoryName);
    }
  }

  public void setCategories(final Set<String> categoryNames) {
    categories = categoryNames;
  }

  public void addEventSubProcess(final EventProcessDefinition eventProcess) {
    if (eventSubProcesses == null) {
      eventSubProcesses = new ArrayList<EventProcessDefinition>();
    }
    eventSubProcesses.add(eventProcess);
  }

  @Override
  public List<EventProcessDefinition> getEventSubProcesses() {
    if (eventSubProcesses == null) {
      return Collections.emptyList();
    }
    return eventSubProcesses;
  }

}
