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
package org.ow2.bonita.facade.runtime;

import java.io.Serializable;
import java.util.Date;

/**
 * Interface providing methods to get informations about state changes of the process instance.
 */
public interface InstanceStateUpdate extends Serializable {
  /**
   * Returns the date of the update.
   * @return The date of the update.
   */
  Date getUpdatedDate();

  /**
   * Returns the user Id who made the update.
   * @return The assigned user Id.
   */
  String getUpdatedBy();

  /**
   * Returns the  state at assignment.
   * @return The  state at assignment.
   */
  InstanceState getInstanceState();

  /**
   * Returns the initial state.
   * @return The initial state.
   */
  InstanceState getInitialInstanceState();

}
