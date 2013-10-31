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
 **/
package org.ow2.bonita.services.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.ow2.bonita.facade.runtime.ActivityInstance;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.TaskInstance;

public final class QuerierUtil {

  private QuerierUtil() { }

  public static Set<TaskInstance> getUserTasks(Set<ActivityInstance> activities,
      String userId, ActivityState taskState) {
    Collection<ActivityState> taskStates = new HashSet<ActivityState>();
    taskStates.add(taskState);
    return getUserTasks(activities, userId, taskStates);
  }
  public static Set<TaskInstance> getUserTasks(Set<ActivityInstance> activities,
      String userId, Collection<ActivityState> taskStates) {
    Set<TaskInstance> result = new HashSet<TaskInstance>();
    if (activities != null) {
      for (ActivityInstance activity : activities) {
        if (activity.isTask() && taskStates.contains(activity.getTask().getState())) {
          TaskInstance taskInstance = activity.getTask();
          String taskUserId = null;
          boolean userIdOk = false;
          boolean candidatesOk = false;
          try {
            taskUserId = taskInstance.getTaskUser();
            userIdOk = taskUserId != null && taskUserId.equals(userId);
          } catch (IllegalStateException e) {
            // nothing
          }
          if (!userIdOk) {
            try {
              Set<String> taskCandidates = taskInstance.getTaskCandidates();
              candidatesOk = taskCandidates != null && taskCandidates.contains(userId);
            } catch (IllegalStateException e) {
              // nothing
            }
          }
          if (userIdOk || (taskUserId == null && candidatesOk)) {
            result.add(taskInstance);
          }
        }
      }
    }
    return result;
  }
}


