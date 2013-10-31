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
package org.ow2.bonita.env.operation;

import java.io.Serializable;

import org.ow2.bonita.env.WireContext;

/**
 * any field update or method invocation after the construction of an object.
 */
public interface Operation extends Serializable {

  /**
   * Apply this operation to the specified object, defined in the specified
   * {@link WireContext}.
   * 
   * @param target
   *          object on which the operation should be performed.
   * @param wireContext
   *          context in which the operation is applied.
   */
  void apply(Object target, WireContext wireContext);
}
