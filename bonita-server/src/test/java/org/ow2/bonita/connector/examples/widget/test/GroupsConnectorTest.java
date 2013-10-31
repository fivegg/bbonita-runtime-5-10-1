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
package org.ow2.bonita.connector.examples.widget.test;

import java.util.List;

import org.ow2.bonita.connector.core.Connector;
import org.ow2.bonita.connector.core.ConnectorError;
import org.ow2.bonita.connector.core.ConnectorTest;
import org.ow2.bonita.connector.examples.widget.GroupsConnector;
import org.ow2.bonita.util.BonitaException;

public class GroupsConnectorTest extends ConnectorTest {

  @Override
  public void testValidateConnector() throws BonitaException {
    List<ConnectorError> errors = Connector.validateConnector(getConnectorClass());
    assertEquals(4, errors.size());
    ConnectorError one = new ConnectorError("test",
        new IllegalArgumentException("The group has no widgets"));
    ConnectorError two = new ConnectorError("null",
        new IllegalArgumentException("The label Id is null"));
    ConnectorError three = new ConnectorError("",
        new IllegalArgumentException("The label Id is empty"));
    ConnectorError four = new ConnectorError("labelB",
        new IllegalArgumentException("The label Id refers to another group Id, composite widget Id or widget Id"));
    assertTrue(errors.contains(one));
    assertTrue(errors.contains(two));
    assertTrue(errors.contains(three));
    assertTrue(errors.contains(four));
  }

  @Override
  protected Class<? extends Connector> getConnectorClass() {
    return GroupsConnector.class;
  }

}
