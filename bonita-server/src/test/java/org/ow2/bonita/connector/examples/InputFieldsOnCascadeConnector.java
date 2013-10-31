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
package org.ow2.bonita.connector.examples;

import java.util.List;

import org.ow2.bonita.connector.core.ConnectorError;

public class InputFieldsOnCascadeConnector extends InputFieldsConnector {
  
  private String address;

  /**
   * @return the address
   */
  public String getAddress() {
    return address;
  }

  /**
   * @param address the address to set
   */
  public void setAddress(String address) {
    this.address = address;
  }

  @Override
  protected List<ConnectorError> validateValues() {
    List<ConnectorError> errors = super.validateValues();
    if(getAddress() == null) {
      ConnectorError error = new ConnectorError("address",
          new IllegalArgumentException("The address cannot be null"));
      errors.add(error);
    }
    return errors;
  }
}
