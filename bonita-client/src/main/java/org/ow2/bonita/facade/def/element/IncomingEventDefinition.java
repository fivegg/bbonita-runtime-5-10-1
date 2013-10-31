/**
 * Copyright (C) 2009-2012 BonitaSoft S.A.
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
package org.ow2.bonita.facade.def.element;

import java.util.List;

/**
 * IncomingEvent definition within activity element.
 * @author Matthieu Chaffotte
 *
 */
public interface IncomingEventDefinition extends EventDefinition {

  /**
   * Obtains the connector list to execute
   * @return the connector list
   */
  List<ConnectorDefinition> getConnectors();

  /**
   * Gets the expression.
   * @return the expression
   */
  String getExpression();

}
