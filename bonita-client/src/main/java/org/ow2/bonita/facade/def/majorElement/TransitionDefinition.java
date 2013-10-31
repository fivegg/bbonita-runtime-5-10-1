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

import org.ow2.bonita.facade.uuid.TransitionDefinitionUUID;

/**
 * Activities are linked themselves using transition. A transition links only two activities.
 * Three kinf of tranistion exists:
 * <ul>
 *    <li>Normal transition</li>
 *    <li>Default transition</li>
 *    <li>Exception transition</li>
 * </ul>
 * A normal transition is a transition which can contains a condition. If the condition is true, the transition is taken.<br/>
 * A default transition is a transition which is taken when no other transitions can be taken.<br/>
 * And an exception transition links a boundary event of an activity to another activity.
 */
public interface TransitionDefinition extends ProcessElement, Serializable {

  /**
   * Returns the UUID for the TransitionDefinition.
   * @return the UUID for the TransitionDefinition
   */
  TransitionDefinitionUUID getUUID();

  /**
   * Returns the activity name from which the transition goes out.
   * @return the activity name from which the transition goes out
   */
  String getFrom();

  /**
   * Returns the activity to which the transition goes in.
   * @return the activity to which the transition goes in
   */
  String getTo();

  /**
   * Gets the condition of a normal transition or null for default or exception transitions.
   * Returns the condition or null
   */
  String getCondition();

  /**
   * Returns true if this transition is a default one.
   * @return true if this transition is a one: false otherwise
   */
  boolean isDefault();

  /**
   * Returns the boundary event name or null if this transition is a normal one.
   * @return the boundary event name or null if this transition is a normal one
   */
  String getFromBoundaryEvent();

}
