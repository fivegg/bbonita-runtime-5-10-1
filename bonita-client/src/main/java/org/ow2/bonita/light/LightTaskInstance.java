/**
 * Copyright (C) 2009  BonitaSoft S.A.
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
package org.ow2.bonita.light;

import java.util.Date;

import org.ow2.bonita.facade.runtime.AssignUpdate;
import org.ow2.bonita.facade.runtime.StateUpdate;

/**
 * Interface of task activity (aka Manual activity).
 */
public interface LightTaskInstance extends LightActivityInstance {
  
  /**
   * Returns the user assigned for the task.<br>
   * Check before calling this method if a user has been assigned with {@link #isTaskAssigned()} method.
   * The assigned user depends on:
   * <ul>
   * <li>the user assigned for the task when the task has been initially created</li>
   * <li>actions performed on the task such as start, suspend, resume, assign.
   * Update list contains all information on these recorded updates
   * ({@link AssignUpdate assign update}, {@link StateUpdate state update}).</li>
   * </ul>
   * @throws IllegalStateException if the task is not assigned.
   * @return The user assigned for the task.
   */
  String getTaskUser();
  
  /**
   * Returns the user starting the task.
   * @return The user starting the task.
   */
  String getStartedBy();
  
  /**
   * Returns the user finishing the activity.
   * @return The user finishing the activity.
   */
  String getEndedBy();
  
  /**
   * Returns the date recorded when the task is created.
   * The task is created when the execution flow enters into the activity
   * node defining the task.
   * @return The date recorded when the task is created.
   */
  Date getCreatedDate();
  
  /**
   * Return true if the task is assigned.
   * @return true if the task is assigned.
   */
  boolean isTaskAssigned();

}
