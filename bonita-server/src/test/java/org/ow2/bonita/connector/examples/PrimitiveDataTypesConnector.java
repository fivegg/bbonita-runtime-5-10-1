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

public class PrimitiveDataTypesConnector extends Connector {

  @SuppressWarnings("unused")
  private byte small;
  @SuppressWarnings("unused")
  private short number;
  @SuppressWarnings("unused")
  private int count;
  @SuppressWarnings("unused")
  private long credit;
  @SuppressWarnings("unused")
  private float average;
  @SuppressWarnings("unused")
  private double account;
  @SuppressWarnings("unused")
  private boolean condition;
  @SuppressWarnings("unused")
  private char character;

  public void setSmall(byte small) {
    this.small = small;
  }

  public void setNumber(short number) {
    this.number = number;
  }

  public void setCount(int count) {
    this.count = count;
  }

  public void setCredit(long credit) {
    this.credit = credit;
  }

  public void setAverage(float average) {
    this.average = average;
  }

  public void setAccount(double account) {
    this.account = account;
  }

  public void setCondition(boolean condition) {
    this.condition = condition;
  }

  public void setCharacter(char character) {
    this.character = character;
  }

  @Override
  protected void executeConnector() throws Exception {
    System.out.println("Done");
  }

  @Override
  protected List<ConnectorError> validateValues() {
    return null;
  }
}
