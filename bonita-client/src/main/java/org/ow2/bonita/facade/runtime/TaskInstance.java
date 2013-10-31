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
 **/
package org.ow2.bonita.facade.runtime;

import java.util.List;
import java.util.Set;

import org.ow2.bonita.light.LightTaskInstance;

/**
 * Interface of task activity (aka Manual activity).
 */
public interface TaskInstance extends LightTaskInstance, ActivityInstance {

  /**
   * Returns the user Id performing the update ({@link StateUpdate state update} or {@link AssignUpdate assign update}).
   * @return the user Id performing the update ({@link StateUpdate state update} or {@link AssignUpdate assign update}).
   */
  String getUpdatedBy();
  
  /**
   * Returns the list of recorded {@link AssignUpdate assign changes}.
   * @return The list of recorded {@link AssignUpdate assign changes}.
   */
  List<AssignUpdate> getAssignUpdates();

  /**
   * Returns the current set of candidate users.<br>
   * This list depends on:<br>
   * <ul>
   * <li>the set of candidate users when the task has been initially created.
   * The set could be empty if the performer has role type and no role mapper has been defined.</li>
   * <li>the call(s) to assign method (that could execute the role mapper defined for the activity).
   * Update list contains all information on these {@link AssignUpdate assign update}.</li>
   * </ul>
   * @return A set of user id.
   */
  Set<String> getTaskCandidates();
}
