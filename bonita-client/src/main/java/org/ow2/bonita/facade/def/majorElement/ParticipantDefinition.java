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
package org.ow2.bonita.facade.def.majorElement;

import java.io.Serializable;

import org.ow2.bonita.facade.def.element.RoleMapperDefinition;
import org.ow2.bonita.facade.uuid.ParticipantDefinitionUUID;
/**
 * This interface represents the Participant definition.<br>
 * It's derived from the Workflow Participant of XPDL.<br>
 * The Participant entity type attribute characterises the participant to be an individual,
 * an organisational unit or an abstract resource such as a machine.
 * <ul>
 * <li>RESOURCE_SET A set of resources (Not supported).</li>
 * <li>RESOURCE A specific resource agent (Not supported).
 * <li>ROLE This type allows performer addressing by a role or skill set.
 * A role in this context is a function a human has within an organization.
 * As a function is not necessarily unique, a coordinator may be defined
 * (for administrative purposes or in case of exception handling) and a list of
 * humans the role is related to.
 * <li>HUMAN A human interacting with the system via an application presenting a user interface to
 * the participant.
 * <li>SYSTEM An automatic agent.
 * <ul>
 */
public interface ParticipantDefinition extends ProcessElement, Serializable {

  /**
   * Returns the ParticipantDefinition UUID.
   * @return the ParticipantDefinition UUID
   */
  ParticipantDefinitionUUID getUUID();

  /**
   * The role mappers feature permits automatic definition of the Bonita roles.
   * A call to a java class is performed when the task is created.
   * @return RoleMapperDefinition the RoleMapper.
   */
  RoleMapperDefinition getRoleMapper();

}
