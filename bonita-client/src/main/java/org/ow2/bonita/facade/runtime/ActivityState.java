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
 * Modified by Elias Ricken de Medeiros - BonitaSoft S.A.
 **/
package org.ow2.bonita.facade.runtime;

import java.util.Collection;
import java.util.HashSet;

/**
 * Possible states of task.
 */
public enum ActivityState {
  /**
   * Activity has been created and initialized.
   */
  READY,
  /**
   * Activity is executing.
   */
  EXECUTING,
  /**
   * Activity (is a task) has been suspended (the task that was READY or EXECUTING).
   */
  SUSPENDED,
  /**
   * Activity is finished.
   */
  FINISHED,
  /**
   * Activity has been cancelled by a user.
   */
  CANCELLED,
  /**
   * Activity has been aborted by the system.
   */
  ABORTED,
  /**
   * Activity has been skipped by a user. 
   */
  SKIPPED,
  
  /**
   *Activity has failed 
   */
  FAILED;
  
  public static Collection<ActivityState> getAllStates() {
    Collection<ActivityState> taskStates = new HashSet<ActivityState>();
    taskStates.add(ActivityState.READY);
    taskStates.add(ActivityState.EXECUTING);
    taskStates.add(ActivityState.FINISHED);
    taskStates.add(ActivityState.SUSPENDED);
    taskStates.add(ActivityState.CANCELLED);
    taskStates.add(ActivityState.ABORTED);
    taskStates.add(ActivityState.SKIPPED);
    return taskStates;
  }
}

