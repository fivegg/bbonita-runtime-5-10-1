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

import org.ow2.bonita.connector.core.Connector;
import org.ow2.bonita.connector.core.ConnectorError;

public class SingleInputFieldConnector extends Connector {

  private String inputField;
  private String field;
  private int number;

  /**
   * @return the inputField
   */
  public String getInputField() {
    return inputField;
  }

  /**
   * @return the field
   */
  public String getField() {
    return field;
  }

  /**
   * @return the number
   */
  public int getNumber() {
    return number;
  }

  /**
   * @param inputField the inputField to set
   */
  public void setInputField(String inputField) {
    this.inputField = inputField;
  }

  /**
   * @param field the field to set
   */
  public void setField(String field) {
    this.field = field;
  }

  /**
   * @param number the number to set
   */
  public void setNumber(int number) {
    this.number = number;
  }

  @Override
  protected void executeConnector() {
    
  }

  @Override
  protected List<ConnectorError> validateValues() {
    getNumber();
    getField();
    getInputField();
    return null;
  }
}
