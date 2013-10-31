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

public class WrappedPrimitiveDataTypesConnector extends Connector {

  @SuppressWarnings("unused")
  private Byte small;
  @SuppressWarnings("unused")
  private Short number;
  @SuppressWarnings("unused")
  private Integer count;
  @SuppressWarnings("unused")
  private Long credit;
  @SuppressWarnings("unused")
  private Float average;
  @SuppressWarnings("unused")
  private Double account;
  @SuppressWarnings("unused")
  private Boolean condition;
  @SuppressWarnings("unused")
  private Character character;
  @SuppressWarnings("unused")
  private String string;

  public void setSmall(Byte small) {
    this.small = small;
  }

  public void setNumber(Short number) {
    this.number = number;
  }

  public void setCount(Integer count) {
    this.count = count;
  }

  public void setCredit(Long credit) {
    this.credit = credit;
  }

  public void setAverage(Float average) {
    this.average = average;
  }

  public void setAccount(Double account) {
    this.account = account;
  }

  public void setCondition(Boolean condition) {
    this.condition = condition;
  }

  public void setCharacter(Character character) {
    this.character = character;
  }

  public void setString(String string) {
    this.string = string;
  }

  @Override
  protected void executeConnector() throws Exception {
  }

  @Override
  protected List<ConnectorError> validateValues() {
    return null;
  }
}
