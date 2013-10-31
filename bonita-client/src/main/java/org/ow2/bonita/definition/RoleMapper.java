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
package org.ow2.bonita.definition;

import java.util.Set;

import org.ow2.bonita.facade.QueryAPIAccessor;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;

/**
 * Role mapper is dedicated to manual activity (ie. activity with startMode=Manual, aka human task).<br>
 * Implementing this interface allows to resolve the role by executing the {@link #searchMembers searchMembers() method}
 * that returns the list of users matching with the role.<br>
 * Within xpdl definition, Role Mapper element is specified within a role (ie. XPDL participant element with type=role)
 * and this role is specified within the XPDL Performer element of a manual activity
 * (ie. activity with startMode=Manual, aka human task).<br>
 * There is two parameters to define a role mapper:
 * <ul>
 * <li>the type of the role mapper (ie. custom)</li>
 * <li>the name of the class that implements this interface</li>
 * </ul>
 * The {@link #searchMembers searchMembers() method} to be implemented is called when the execution
 * enters into the node of the activity after the creation of the human task.<br>
 * Candidates list of the task will be filled-in with returned set of users assuming
 * that only these members of the list can get the task.<br>
 * In other word, this task is returned by {@link org.ow2.bonita.facade.QueryRuntimeAPI#getTaskList getTaskList() method}
 * (with state=READY) if executed by the users of the candidates list (being authenticated).
 *
 * @author Marc Blachon, Guillaume Porcher, Charles Souillard, Miguel Valdes, Pierre Vigneras
 */

public interface RoleMapper {

  /**
   * This method of the interface is intended to return an unsorted set of users that
   * match the specified role.
   * good practice is to call an identity module (ie. user data base, Ldap server).
   * Returned users become the candidates to execute the task.
   * @param accessor The QueryAPIAccessor interface to access: QueryRuntimeAPI or QueryDefinitionAPI.
   * @param instanceUUID Id of the instance.
   * @param roleId Value of Id attribute of the Participant element (with role type) defined into the xpdl definition.
   * @return The set of unordered list of user Id that match the role in the user base.
   * @throws Exception If an Exception has occurred.
   */
  Set<String> searchMembers(QueryAPIAccessor accessor, ProcessInstanceUUID instanceUUID, String roleId) throws Exception;

}
