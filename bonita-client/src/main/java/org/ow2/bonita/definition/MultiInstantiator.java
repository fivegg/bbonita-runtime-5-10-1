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
package org.ow2.bonita.definition;

import org.ow2.bonita.facade.QueryAPIAccessor;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;

/**
 * Implementing this interface is required to get multi-instantiation feature.<br>
 * This feature allows to get multiple instantiations of an activity.
 * <p>
 * Into the xpdl file, this feature is specified within an external attributes of the Activity element.<br>
 * The name of the external attribute is MultiInstantiation.<br>
 * There is two child elements defining the MultiInstantiation:
 * <ul>
 * <li>The variable element containing the <b>name of the variable</b> that is set at multi-instantiation execution
 * for each activity instance. This variable must be defined as local to the activity.<br>
 * For instance this activity variable can be used into hooks or performer assignments defined within the activity.
 * Notices this activity variable cannot be used into the condition of the outgoing transition.</li>
 * <li>MultiInstantiator element containing the <b>name of the class that implements this interface</b></li>
 * </ul>
 * </p>
 */
public interface MultiInstantiator {
  /**
   * Method of the interface to be implemented.<br>
   * Put in all your required user-defined operations.<br>
   * <p>
   * This method must return an MultiInstantiatorDescriptor.<br>
   * Parameters to construct this returned object must respect the following conditions:
   * <ul>
   * <li>parameters cannot be null</li>
   * <li>joinNumber greater than 0</li>
   * <li>(*)joinNumber must be lesser than or equal to the variableValues size</li>
   * </ul>
   * Otherwise a runtime exception will be raised at execution.<br>
   * (*) Temporally restriction has be added ! : <b>joinNumber must be equal to the variableValues size</b>.
   *
   * </p>
   * @param accessor The InternalQueryAPIAccessor interface to access: QueryRuntimeAPI or QueryDefinitionAPI.
   * @param instanceUUID Id of the instance.
   * @param activityId Id of the activity.
   * @param iterationId Id of the iteration.
   * @return an {@link MultiInstantiatorDescriptor} that describes the number of activities to instantiate.
   * @throws Exception If an Exception has occurred.
   */
  MultiInstantiatorDescriptor execute(QueryAPIAccessor accessor, ProcessInstanceUUID instanceUUID,
      String activityId, String iterationId) throws Exception;

}
