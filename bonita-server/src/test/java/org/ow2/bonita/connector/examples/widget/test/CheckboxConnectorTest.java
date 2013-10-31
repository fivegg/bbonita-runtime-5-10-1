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
package org.ow2.bonita.connector.examples.widget.test;

import java.util.List;

import org.ow2.bonita.connector.core.Connector;
import org.ow2.bonita.connector.core.ConnectorError;
import org.ow2.bonita.connector.core.ConnectorTest;
import org.ow2.bonita.connector.examples.widget.CheckboxConnector;

public class CheckboxConnectorTest extends ConnectorTest {

  @Override
  protected Class<? extends Connector> getConnectorClass() {
    return CheckboxConnector.class;
  }

  @Override
  public void testValidateConnector() {
    List<ConnectorError> errors = Connector.validateConnector(getConnectorClass());

    assertEquals(5, errors.size());
    ConnectorError one = new ConnectorError("a",
        new IllegalArgumentException("The Checkbox widget does not refer to any setter"));
    ConnectorError two = new ConnectorError("",
        new IllegalArgumentException("The label Id is empty"));
    ConnectorError three = new ConnectorError("h",
        new IllegalArgumentException("The checkbox name cannot be emtpy"));
    ConnectorError four = new ConnectorError("i",
        new IllegalArgumentException("The checkbox value cannot be emtpy"));
    ConnectorError five = new ConnectorError("null",
        new IllegalArgumentException("The label Id is null"));
    assertTrue(errors.contains(one));
    assertTrue(errors.contains(two));
    assertTrue(errors.contains(three));
    assertTrue(errors.contains(four));
    assertTrue(errors.contains(five));
  }
}
