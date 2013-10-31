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
package org.ow2.bonita.facade.def.element;

import java.util.Map;

import org.ow2.bonita.facade.def.majorElement.DescriptionElement;

/**
 * Connector definition within Activity or ProcessInstance element.
 */
public interface ConnectorDefinition extends DescriptionElement {

  /**
   * Gets the class name of the connector.
   * @return the class name of the connector
   */
  String getClassName();

  /**
   * Obtains the connector parameters.
   * @return the connector parameters
   */
  Map<String, Object[]> getParameters();

  /**
   * Defines whether the connector throws exceptions when executing.
   * @return true if the connector throws exception; false otherwise
   */
  boolean isThrowingException();

  /**
   * Defines whether the connector throws a error event using this error code.
   * @return the error code or null
   */
  String getErrorCode();

}
