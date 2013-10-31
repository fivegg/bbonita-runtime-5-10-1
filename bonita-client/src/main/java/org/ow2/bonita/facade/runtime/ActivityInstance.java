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

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.ow2.bonita.light.LightActivityInstance;

/**
 * Runtime (recorded) data concerning activities.<br>
 * This interface concerns the common part for the activity.
 */
public interface ActivityInstance extends LightActivityInstance {

  /**
   * Returns all variables for the recorded activity before
   * the activity has been started.
   * If a hook with an OnReady event name has been defined,
   * this hook has been executed.
   * The map returned by this method contains only local variables (for this activity).
   * Key is the variable name and value is the variable object (can be:
   * a plain {@link String}, a {@link Boolean}, a {@link Date}, a {@link Long} or a {@link Double}).
   * An empty map is returned if no variable is found.
   * @return The map containing activity variables.
   */
  Map<String, Object> getVariablesBeforeStarted();

  /**
   * Returns the value of the variable with the specified key before
   * the activity has been started.
   * If a hook with an OnReady event name has been defined,
   * this hook has been executed.
   * @param variableId the variable name.
   * @return The value of the variable with the specified key (can be: 
   * a plain {@link String}, a {@link Boolean}, a {@link Date}, a {@link Long} or a {@link Double}).
   */
  Object getVariableValueBeforeStarted(String variableId);

  /**
   * return null if this activity is not a task. Return the corresponding task instance if it is a task.
   */
  TaskInstance getTask();

  /**
   * Gives access to the historic of variables updates.
   * @return The list of recorded informations for the variable updates.
   */
  List<VariableUpdate> getVariableUpdates();

  /**
   * Returns the map containing all variables with the last updated value.
   * @return The map containing all variables with the last updated value.
   */
  Map<String, Object> getLastKnownVariableValues();
  
  /**
   * Returns the last state update.
   * @return the last state update.
   */
  StateUpdate getLastStateUpdate();

  /**
   * Returns the list of recorded {@link StateUpdate state changes}.
   * @return The list of recorded {@link StateUpdate state changes}.
   */
  List<StateUpdate> getStateUpdates();
  
  AssignUpdate getLastAssignUpdate();

  String getActivityName();
  
  ActivityState getState();
}
