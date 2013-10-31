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
import org.ow2.bonita.connector.examples.widget.TextareaConnector;
import org.ow2.bonita.util.BonitaException;

public class TextareaConnectorTest extends ConnectorTest {

  @Override
  protected Class<? extends Connector> getConnectorClass() {
    return TextareaConnector.class;
  }

  @Override
  public void testValidateConnector() throws BonitaException {
    List<ConnectorError> errors = Connector.validateConnector(getConnectorClass());
    assertEquals(11, errors.size());
    ConnectorError one = new ConnectorError("a",
        new IllegalArgumentException("The Textarea widget does not refer to any setter"));
    ConnectorError two = new ConnectorError("c",
        new IllegalArgumentException("The row number of the textarea cannot be less than 1"));
    ConnectorError three = new ConnectorError("e",
        new IllegalArgumentException("The column number of the textarea cannot be less than 1"));
    ConnectorError four = new ConnectorError("d",
        new IllegalArgumentException("The maximum number of characters cannot be less than 1"));
    ConnectorError five = new ConnectorError("f",
        new IllegalArgumentException("The maximum number of characters per row cannot be less than 1"));
    ConnectorError six = new ConnectorError("g",
        new IllegalArgumentException("The row number of the textarea cannot be less than 1"));
    ConnectorError seven = new ConnectorError("g",
        new IllegalArgumentException("The column number of the textarea cannot be less than 1"));
    ConnectorError eight = new ConnectorError("g",
        new IllegalArgumentException("The maximum number of characters cannot be less than 1"));
    ConnectorError nine = new ConnectorError("g",
        new IllegalArgumentException("The maximum number of characters per row cannot be less than 1"));
    ConnectorError ten = new ConnectorError("null",
        new IllegalArgumentException("The label Id is null"));
    ConnectorError eleven = new ConnectorError("",
        new IllegalArgumentException("The label Id is empty"));
    assertTrue(errors.contains(one));
    assertTrue(errors.contains(two));
    assertTrue(errors.contains(three));
    assertTrue(errors.contains(four));
    assertTrue(errors.contains(five));
    assertTrue(errors.contains(six));
    assertTrue(errors.contains(seven));
    assertTrue(errors.contains(eight));
    assertTrue(errors.contains(nine));
    assertTrue(errors.contains(ten));
    assertTrue(errors.contains(eleven));
  }

}
