/**
 * Copyright (C) 2009  BonitaSoft S.A..
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
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
package org.ow2.bonita.connector.examples.internationalization;

import java.util.List;

import org.ow2.bonita.connector.core.Connector;
import org.ow2.bonita.connector.core.ConnectorError;

/**
 * @author chaffotm
 *
 */
//@Language("org.ow2.bonita.connector.examples.Internationalization")
public class EnumerationLabelsConnector extends Connector {

  private String a;
  private String b;

  public String getA() {
    return a;
  }

  public String getB() {
    return b;
  }

  /*@Input
  @Enumeration(lines=2,selection=Selection.SINGLE,
      selectedIndices={1},values={
      @Option(id="A",value="s"),@Option(id="B",value="g"),
      @Option(id="Q",value="z"),@Option(id="P",value="f")})*/
  public void setA(String a) {
    this.a = a;
  }

  //@Input
  public void setB(String b) {
    this.b = b;
  }

  @Override
  protected void executeConnector() throws Exception {
  }

  @Override
  protected List<ConnectorError> validateValues() {
    return null;
  }
}
