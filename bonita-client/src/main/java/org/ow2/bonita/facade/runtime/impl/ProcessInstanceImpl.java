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
 * Modified by Charles Souillard, BonitaSoft
 * Modified by Matthieu Chaffotte - BonitaSoft S.A.
 **/
package org.ow2.bonita.facade.runtime.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ow2.bonita.facade.runtime.ActivityInstance;
import org.ow2.bonita.facade.runtime.AttachmentInstance;
import org.ow2.bonita.facade.runtime.Comment;
import org.ow2.bonita.facade.runtime.InstanceStateUpdate;
import org.ow2.bonita.facade.runtime.ProcessInstance;
import org.ow2.bonita.facade.runtime.TaskInstance;
import org.ow2.bonita.facade.runtime.VariableUpdate;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.light.impl.LightProcessInstanceImpl;
import org.ow2.bonita.util.CopyTool;
import org.ow2.bonita.util.Misc;

/**
 * @author Pierre Vigneras, Charles Souillard
 */
public class ProcessInstanceImpl extends LightProcessInstanceImpl implements ProcessInstance {

  private static final long serialVersionUID = 8366284714927360659L;

  protected Set<ProcessInstanceUUID> childrenInstanceUUID;
  protected Map<String, Object> clientVariables;

  protected List<VariableUpdate> variableUpdates = new ArrayList<VariableUpdate>();
  protected List<InstanceStateUpdate> instanceStateUpdates = new ArrayList<InstanceStateUpdate>();
  protected List<Comment> commentFeed;
  protected Set<ActivityInstance> activities;
  protected List<AttachmentInstance> attachments;
  protected Set<String> involvedUsers;
  protected Set<String> activeUsers;

  // mandatory for hibernate
  protected ProcessInstanceImpl() {
    super();
  }

  protected ProcessInstanceImpl(final ProcessDefinitionUUID processUUID, final ProcessInstanceUUID instanceUUID,
      final ProcessInstanceUUID rootInstanceUUID, final long iterationId) {
    super(processUUID, instanceUUID, rootInstanceUUID, iterationId);
    commentFeed = new ArrayList<Comment>();
  }

  public ProcessInstanceImpl(final ProcessInstance processInstance) {
    super(processInstance);
    final Set<ProcessInstanceUUID> children = processInstance.getChildrenInstanceUUID();
    if (children != null && !children.isEmpty()) {
      childrenInstanceUUID = new HashSet<ProcessInstanceUUID>();
      for (final ProcessInstanceUUID child : children) {
        childrenInstanceUUID.add(child);
      }
    }

    clientVariables = processInstance.getInitialVariableValues();

    final List<VariableUpdate> vu = processInstance.getVariableUpdates();
    if (vu != null && !vu.isEmpty()) {
      variableUpdates = new ArrayList<VariableUpdate>();
      for (final VariableUpdate varUpdate : vu) {
        variableUpdates.add(new VariableUpdateImpl(varUpdate));
      }
    }

    final List<InstanceStateUpdate> lisu = processInstance.getInstanceStateUpdates();
    if (lisu != null && !lisu.isEmpty()) {
      instanceStateUpdates = new ArrayList<InstanceStateUpdate>();
      for (final InstanceStateUpdate isu : lisu) {
        instanceStateUpdates.add(new InstanceStateUpdateImpl(isu));
      }
    }

    final List<AttachmentInstance> lai = processInstance.getAttachments();
    if (lai != null && !lai.isEmpty()) {
      attachments = new ArrayList<AttachmentInstance>();
      for (final AttachmentInstance ai : lai) {
        attachments.add(new AttachmentInstanceImpl(ai));
      }
    }

    final List<Comment> comments = processInstance.getCommentFeed();
    commentFeed = new ArrayList<Comment>();
    for (final Comment comment : comments) {
      commentFeed.add(new CommentImpl(comment));
    }

    final Set<ActivityInstance> acts = processInstance.getActivities();
    if (acts != null) {
      activities = new HashSet<ActivityInstance>();
      for (final ActivityInstance activity : acts) {
        activities.add(new ActivityInstanceImpl(activity));
      }
    }

    involvedUsers = CopyTool.copy(processInstance.getInvolvedUsers());

    if (processInstance.getActiveUsers() != null) {
      activeUsers = new HashSet<String>();
      for (final String activeUser : processInstance.getActiveUsers()) {
        activeUsers.add(activeUser);
      }
    }
  }

  @Override
  public Set<String> getInvolvedUsers() {
    return involvedUsers;
  }

  @Override
  public Map<String, Object> getInitialVariableValues() {
    return clientVariables;
  }

  @Override
  public Set<ProcessInstanceUUID> getChildrenInstanceUUID() {
    if (childrenInstanceUUID == null) {
      return new HashSet<ProcessInstanceUUID>();
    }
    return childrenInstanceUUID;
  }

  @Override
  public Object getInitialVariableValue(final String variableId) {
    return getInitialVariableValues().get(variableId);
  }

  @Override
  public Map<String, Object> getLastKnownVariableValues() {
    Map<String, Object> var = getInitialVariableValues();
    if (var != null) {
      var = new HashMap<String, Object>(var);
    } else {
      var = new HashMap<String, Object>();
    }
    for (final VariableUpdate varUp : getVariableUpdates()) {
      var.put(varUp.getName(), varUp.getValue());
    }
    return var;
  }

  @Override
  public List<VariableUpdate> getVariableUpdates() {
    if (variableUpdates == null) {
      return Collections.emptyList();
    }
    return variableUpdates;
  }

  @Override
  public List<InstanceStateUpdate> getInstanceStateUpdates() {
    return instanceStateUpdates;
  }

  @Override
  public List<Comment> getCommentFeed() {
    return commentFeed;
  }

  @Override
  public Set<ActivityInstance> getActivities() {
    return activities;
  }

  @Override
  public List<AttachmentInstance> getAttachments() {
    if (attachments == null) {
      return Collections.emptyList();
    }
    return attachments;
  }

  @Override
  public List<AttachmentInstance> getAttachments(final String attachmentName) {
    final List<AttachmentInstance> matchingAttachments = new ArrayList<AttachmentInstance>();
    for (final AttachmentInstance attachmentInstance : getAttachments()) {
      if (attachmentName.equals(attachmentInstance.getName())) {
        matchingAttachments.add(attachmentInstance);
      }
    }
    return matchingAttachments;
  }

  @Override
  public Set<TaskInstance> getTasks() {
    final Set<TaskInstance> matchingActivities = new HashSet<TaskInstance>();
    if (getActivities() != null) {
      for (final ActivityInstance activity : getActivities()) {
        if (activity.isTask()) {
          matchingActivities.add(activity.getTask());
        }
      }
    }
    return matchingActivities;
  }

  @Override
  public Set<ActivityInstance> getActivities(final String activityId) {
    final Set<ActivityInstance> matchingActivities = new HashSet<ActivityInstance>();
    if (getActivities() != null) {
      for (final ActivityInstance activity : getActivities()) {
        if (activity.getActivityName().equals(activityId)) {
          matchingActivities.add(activity);
        }
      }
    }
    return matchingActivities;
  }

  @Override
  public ActivityInstance getActivity(final String activityId, final String iterationId, final String activityInstanceId) {
    if (getActivities() != null) {
      for (final ActivityInstance activity : getActivities()) {
        if (activity.getActivityName().equals(activityId)
            && (activity.getIterationId() == null && iterationId == null || activity.getIterationId().equals(
                iterationId))
            && (activity.getActivityInstanceId() == null && activityInstanceId == null || activity
                .getActivityInstanceId().equals(activityInstanceId))) {
          return activity;
        }
      }
    }
    return null;
  }

  @Override
  public ActivityInstance getActivity(final ActivityInstanceUUID activityUUID) {
    if (getActivities() != null) {
      for (final ActivityInstance activity : getActivities()) {
        if (activity.getUUID().equals(activityUUID)) {
          return activity;
        }
      }
    }
    return null;
  }

  @Override
  public long getNb() {
    return nb;
  }

  public void setStartedDate(final Date date) {
    startedDate = Misc.getTime(date);
  }

  public void setEndedDate(final Date date) {
    endedDate = Misc.getTime(date);
  }

  @Override
  public ProcessInstanceUUID getRootInstanceUUID() {
    return rootInstanceUUID;
  }

  public void addInvolvedUser(final String user) {
    if (user != null) {
      if (involvedUsers == null) {
        involvedUsers = new HashSet<String>();
      }
      involvedUsers.add(user);
    }
  }

  public void addInvolvedUsers(final Set<String> users) {
    if (users != null) {
      if (involvedUsers == null) {
        involvedUsers = new HashSet<String>();
      }
      involvedUsers.addAll(users);
    }
  }

  public void setIsArchived(final boolean isArchived) {
    this.isArchived = isArchived;
  }

  @Override
  public Set<String> getActiveUsers() {
    if (activeUsers != null) {
      return new HashSet<String>(activeUsers);
    } else {
      return Collections.emptySet();
    }
  }

  public void addActiveUser(final String user) {
    if (user != null) {
      if (activeUsers == null) {
        activeUsers = new HashSet<String>();
      }
      activeUsers.add(user);
    }
  }

  public void addActiveUsers(final Set<String> users) {
    if (users != null) {
      if (activeUsers == null) {
        activeUsers = new HashSet<String>();
      }
      activeUsers.addAll(users);
    }
  }

  public void removeActiveUser(final String user) {
    if (user != null) {
      final HashSet<String> usersToBeRemoved = new HashSet<String>();
      usersToBeRemoved.add(user);
      removeActiveUsers(usersToBeRemoved);
    }
  }

  public void removeActiveUsers(final Set<String> users) {
    if (users != null && !users.isEmpty()) {
      final Set<String> usersToRemove = new HashSet<String>(users);
      if (activeUsers != null && activeUsers.size() > 0) {
        for (final String userToBeRemoved : usersToRemove) {
          if (activeUsers.contains(userToBeRemoved)) {
            activeUsers.remove(userToBeRemoved);
          }
        }
      }
    }
  }

  @Override
  public String toString() {
    return this.getClass().getName() + "[uuid: " + getUUID() + ", processDefinitionUUID: " + getProcessDefinitionUUID()
        + ", processUUID: " + getProcessInstanceUUID() + ", parentInstanceUUID: " + getParentInstanceUUID()
        + ", rootInstanceUUID: " + getRootInstanceUUID() + ", childrenInstanceUUID: " + getChildrenInstanceUUID()
        + ", startedBy: " + getStartedBy() + ", endedBy: " + getEndedBy() + ", startedDate: " + getStartedDate()
        + ", endedDate: " + getEndedDate() + ", initialVariableValues: " + getInitialVariableValues()
        + ", activeUsers: " + (getActiveUsers() == null ? 0 : getActiveUsers().size()) + "]";
  }

}
