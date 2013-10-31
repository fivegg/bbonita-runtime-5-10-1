/**
 * Copyright (C) 2009  Bull S. A. S.
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
package org.ow2.bonita.connector.core;

import java.util.List;

/**
 * 
 * @author Matthieu Chaffotte
 *
 */
public class ConnectorException extends Exception {

  private static final long serialVersionUID = 6590527374786234777L;
  private List<ConnectorError> errors;
  private String connectorId;
  private String connectorClassName;

  public ConnectorException(final String message, final String connectorId, final String connectorClassName, final List<ConnectorError> errors) {
    super(message);
    this.connectorId = connectorId;
    this.errors = errors;
    this.connectorClassName = connectorClassName;
  }
  
  public ConnectorException(final Throwable t, final String connectorId, final List<ConnectorError> errors) {
    super(t);
    this.connectorId = connectorId;
    this.errors = errors;
  }

  public List<ConnectorError> getErrors() {
    return errors;
  }

  public String getConnectorId() {
    return connectorId;
  }

  public String getConnectorClassName() {
    return connectorClassName;
  }

}
