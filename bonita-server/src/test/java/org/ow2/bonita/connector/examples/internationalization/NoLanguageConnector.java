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
package org.ow2.bonita.connector.examples.internationalization;

import java.util.List;

import org.ow2.bonita.connector.core.Connector;
import org.ow2.bonita.connector.core.ConnectorError;

public class NoLanguageConnector extends Connector {
	
	private String a;
	private String b;
	private String c;
	private String d;
	
  public String getA() {
  	return a;
  }

  //@Input
  public void setA(String a) {
  	this.a = a;
  }

  public String getB() {
  	return b;
  }

  //@Input
  public void setB(String b) {
  	this.b = b;
  }

  public String getC() {
  	return c;
  }

  //@Input
  public void setC(String c) {
  	this.c = c;
  }

  public String getD() {
  	return d;
  }

  //@Input
  public void setD(String d) {
  	this.d = d;
  }

	@Override
  protected void executeConnector() throws Exception {
  }

  @Override
  protected List<ConnectorError> validateValues() {
    return null;
  }
}
