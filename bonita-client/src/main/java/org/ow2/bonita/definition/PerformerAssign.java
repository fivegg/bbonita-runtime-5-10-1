/**
 * Copyright (C) 2010  BonitaSoft S.A.
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
package org.ow2.bonita.definition;

import java.util.Set;

import org.ow2.bonita.facade.QueryAPIAccessor;
import org.ow2.bonita.facade.runtime.ActivityInstance;

/**
 * Performer assignment is dedicated to manual activity (ie. activity with startMode=Manual, aka human task).<br>
 * Implementing this interface allows to assign the activity to a user of a group.<br>
 * Within xpdl definition, the performer assignment feature is specified within
 * external attributes of the Activity element.<br>
 * There is two parameters to define a performer assignment:
 * <ul>
 * <li>the type of the performer assignment (ie. custom)</li>
 * <li>the name of the class that implements this interface</li>
 * </ul>
 * <p>
 * The {@link #selectUser selectUser()} method to be implemented is called when the execution enters into the node of
 * the activity after the creation of the human task and after the execution of
 * the {@link RoleMapper role mapper} if there is one.
 * <p>
 * This task will be assign to the returned user and only this user can get the task.<br>
 * In other word, this task is returned by {@link org.ow2.bonita.facade.QueryRuntimeAPI#getTaskList getTaskList() method}
 * (with state=READY) only if executed by the assigned user (being authenticated).
 */
public interface PerformerAssign {
  /**
   * Selecting one user from the candidates list is the good practice.
   * Candidates list results from the execution of a role mapper.
   * @param accessor The QueryAPIAccessor interface to access: QueryRuntimeAPI or QueryDefinitionAPI.
   * @param activityInstance the activity.
   * @param candidates The unordered set of user uuid that are candidate to start the task.
   * @return The user Id that can start the task.
   * @throws Exception If an Exception has occurred.
   */
  String selectUser(QueryAPIAccessor accessor, ActivityInstance activityInstance, 
      Set<String> candidates) throws Exception;
}
