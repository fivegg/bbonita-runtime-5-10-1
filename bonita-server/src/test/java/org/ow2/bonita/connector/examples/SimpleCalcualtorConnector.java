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

import java.util.ArrayList;
import java.util.List;

import org.ow2.bonita.connector.core.Connector;
import org.ow2.bonita.connector.core.ConnectorError;

public class SimpleCalcualtorConnector extends Connector {
  
  private double firstOperand;
  private double secondOperand;
  private boolean plus;
  private boolean minus;
  private boolean times;
  private boolean dividedBy;
  private double result;
  
  public double getFirstOperand() {
    return firstOperand;
  }

  public double getSecondOperand() {
    return secondOperand;
  }

  public boolean isPlus() {
    return plus;
  }

  public boolean isMinus() {
    return minus;
  }

  public boolean isTimes() {
    return times;
  }

  public boolean isDividedBy() {
    return dividedBy;
  }

  
  public double getdoubleResult() {
    return result;
  }

  public Double getResult() {
    return result;
  }

  public void setFirstOperand(double firstOperand) {
    this.firstOperand = firstOperand;
  }

  public void setFirstOperand(Double firstOperand) {
    if (firstOperand == null) {
      this.firstOperand = 0;
    } else {
      this.firstOperand = firstOperand;
    }
  }

  public void setSecondOperand(double secondOperand) {
    this.secondOperand = secondOperand;
  }

  public void setSecondOperand(Double secondOperand) {
    if (secondOperand == null) {
      this.secondOperand = 1;
    } else {
      this.secondOperand = secondOperand;
    }
  }

  public void setPlus(boolean plus) {
    this.plus = plus;
  }
  
  public void setPlus(Boolean plus) {
    if (plus == null) {
      this.plus = false;
    } else {
      this.plus = plus;
    }
  }

  public void setMinus(boolean minus) {
    this.minus = minus;
  }
  
  public void setMinus(Boolean minus) {
    if (minus == null) {
      this.minus = false;
    } else {
      this.minus = minus;
    }
  }

  public void setTimes(boolean times) {
    this.times = times;
  }
  
  public void setTimes(Boolean times) {
    if (times == null) {
      this.times = false;
    } else {
      this.times = times;
    }
  }

  public void setDividedBy(boolean dividedBy) {
    this.dividedBy = dividedBy;
  }
  
  public void setDividedBy(Boolean dividedBy) {
    if (dividedBy == null) {
      this.dividedBy = false;
    } else {
      this.dividedBy = dividedBy;
    }
  }

  public void setResult(double result) {
    this.result = result;
  }

  @Override
  protected void executeConnector() throws Exception {
    if (isPlus()) {
      result = firstOperand + secondOperand;
    } else if (isMinus()) {
      result = firstOperand - secondOperand;
    } else if (isTimes()) {
      result = firstOperand * secondOperand;
    } else if (isDividedBy()) {
      result = firstOperand / secondOperand;
    }
  }
  
  @Override
  protected List<ConnectorError> validateValues() {
    List<ConnectorError> errors = new ArrayList<ConnectorError>();
    if (isDividedBy() && getSecondOperand() == 0) {
     errors.add(new ConnectorError("secondOperand",
         new IllegalArgumentException("division by 0 are not allowed!")));
    }
    return errors;
  }
}
