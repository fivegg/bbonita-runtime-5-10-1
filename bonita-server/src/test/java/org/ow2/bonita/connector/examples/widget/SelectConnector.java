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

public class SelectConnector extends Connector {

  private String a;
  private String b;
  private String c;
  private String d;
  private String e;
  private String f;
  private String g;

  public String getA() {
    return a;
  }

  public String getB() {
    return b;
  }

  public String getC() {
    return c;
  }

  public String getD() {
    return d;
  }

  public String getE() {
    return e;
  }

  public String getF() {
    return f;
  }

  public String getG() {
    return g;
  }

  public void setA(String a) {
    this.a = a;
  }

  public void setB(String b) {
    this.b = b;
  }

  public void setC(String c) {
    this.c = c;
  }

  public void setD(String d) {
    this.d = d;
  }

  public void setE(String e) {
    this.e = e;
  }

  public void setF(String f) {
    this.f = f;
  }

  public void setG(String g) {
    this.g = g;
  }

  @Override
  protected void executeConnector() throws Exception {
  }

  @Override
  protected List<ConnectorError> validateValues() {
    return null;
  }
}
