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
package org.ow2.bonita.connector.examples.widget;

import java.util.List;

import org.ow2.bonita.connector.core.Connector;
import org.ow2.bonita.connector.core.ConnectorError;

public class RadioConnector extends Connector {

  private Boolean a;
  private Boolean b;
  private Boolean c;
  private Boolean d;
  private Boolean e;
  private Boolean f;
  private Boolean g;
  private Boolean h;
  private Boolean i;
  
  public Boolean isA() {
    return a;
  }

  public Boolean isB() {
    return b;
  }

  public Boolean isC() {
    return c;
  }

  public Boolean isD() {
    return d;
  }

  public Boolean isE() {
    return e;
  }

  public Boolean isF() {
    return f;
  }

  public Boolean isG() {
    return g;
  }
  
  public Boolean isH() {
    return h;
  }
  
  public Boolean isI() {
    return i;
  }

  public void setA(Boolean a) {
    this.a = a;
  }

  public void setB(Boolean b) {
    this.b = b;
  }

  public void setC(Boolean c) {
    this.c = c;
  }

  public void setD(Boolean d) {
    this.d = d;
  }

  public void setE(Boolean e) {
    this.e = e;
  }

  public void setF(Boolean f) {
    this.f = f;
  }

  public void setG(Boolean g) {
    this.g = g;
  }

  public void setH(Boolean h) {
    this.h = h;
  }

  public void setI(Boolean i) {
    this.i = i;
  }

  @Override
  protected void executeConnector() throws Exception {
  }

  @Override
  protected List<ConnectorError> validateValues() {
    return null;
  }
}
