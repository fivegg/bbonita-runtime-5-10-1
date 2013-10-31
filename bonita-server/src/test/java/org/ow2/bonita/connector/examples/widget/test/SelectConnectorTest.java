/**
 * Copyright (C) 2009  Bull S. A. S.
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
import org.ow2.bonita.connector.examples.widget.SelectConnector;
import org.ow2.bonita.util.BonitaException;

public class SelectConnectorTest extends ConnectorTest {

  @Override
  protected Class<? extends Connector> getConnectorClass() {
    return SelectConnector.class;
  }
  
  @Override
  public void testValidateConnector() throws BonitaException {
    List<ConnectorError> errors = Connector.validateConnector(getConnectorClass());
    assertEquals(6, errors.size());
    ConnectorError one = new ConnectorError("a",
        new IllegalArgumentException("The Select widget does not refer to any setter"));
    ConnectorError two = new ConnectorError("null",
        new IllegalArgumentException("The label Id is null"));
    ConnectorError three = new ConnectorError("e",
        new IllegalArgumentException("An option label cannot be empty"));
    ConnectorError four = new ConnectorError("f",
        new IllegalArgumentException("An option value cannot be empty"));
    ConnectorError five = new ConnectorError("g",
        new IllegalArgumentException("The select values cannot be empty"));
    ConnectorError six = new ConnectorError("",
        new IllegalArgumentException("The label Id is empty"));
    assertTrue(errors.contains(one));
    assertTrue(errors.contains(two));
    assertTrue(errors.contains(three));
    assertTrue(errors.contains(four));
    assertTrue(errors.contains(five));
    assertTrue(errors.contains(six));
  }

}
