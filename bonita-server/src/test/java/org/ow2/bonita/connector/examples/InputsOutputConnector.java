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

import java.util.Date;
import java.util.List;

import org.ow2.bonita.connector.core.Connector;
import org.ow2.bonita.connector.core.ConnectorError;

public class InputsOutputConnector extends Connector {

  private int firstInput;
  private String secondInput;
  private double thirdInput;
  private Date fourthInput;
  private String output;

  public String getOutput() {
    return output;
  }

  public void setFirstInput(int firstInput) {
    this.firstInput = firstInput;
  }

  public void setSecondInput(String secondInput) {
    this.secondInput = secondInput;
  }

  public void setThirdInput(double thirdInput) {
    this.thirdInput = thirdInput;
  }

  public void setThirdInput(Double thirdInput) {
    this.thirdInput = thirdInput;
  }

  public void setFourthInput(Date fourthInput) {
    this.fourthInput = fourthInput;
  }

  @Override
  protected void executeConnector() throws Exception {
    output = firstInput + " " + secondInput + " " + thirdInput + " " + fourthInput;
  }

  @Override
  protected List<ConnectorError> validateValues() {
    return null;
  }
}
