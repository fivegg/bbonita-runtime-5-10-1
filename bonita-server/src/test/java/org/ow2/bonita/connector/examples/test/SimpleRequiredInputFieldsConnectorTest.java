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
package org.ow2.bonita.connector.examples.test;

import java.util.List;

import org.ow2.bonita.connector.core.Connector;
import org.ow2.bonita.connector.core.ConnectorError;
import org.ow2.bonita.connector.core.ConnectorTest;
import org.ow2.bonita.connector.examples.SimpleRequiredInputFieldsConnector;
import org.ow2.bonita.util.BonitaException;

public class SimpleRequiredInputFieldsConnectorTest extends ConnectorTest {

  public void testValidate() throws BonitaException {
    SimpleRequiredInputFieldsConnector mock = new SimpleRequiredInputFieldsConnector();
    mock.setFirstname("");
    mock.setLastname("");
    mock.setEmail(null);
    mock.setSex(null);
    assertFalse(mock.containsErrors());
    mock.setFirstname("");
    mock.setLastname("");
    mock.setEmail("");
    mock.setSex(null);
    assertFalse(mock.containsErrors());
    mock.setFirstname("");
    mock.setLastname("");
    mock.setEmail("");
    mock.setSex("");
    assertFalse(mock.containsErrors());
    mock.setFirstname(" ");
    mock.setLastname("");
    mock.setEmail(null);
    mock.setSex(null);
    assertFalse(mock.containsErrors());
    
    mock.setFirstname(null);
    mock.setLastname(null);
    mock.setEmail(null);
    mock.setSex(null);
    List<ConnectorError> errors = mock.validate();
    assertEquals(2, errors.size());
    ConnectorError error = errors.get(0);
    assertEquals("firstname", error.getField());
    error = errors.get(1);
    assertEquals("lastname", error.getField());
    
    mock.setFirstname("");
    mock.setLastname(null);
    mock.setEmail(null);
    mock.setSex(null);
    errors = mock.validate();
    assertEquals(1, errors.size());
    error = errors.get(0);
    assertEquals("lastname", error.getField());
    
    mock.setFirstname(null);
    mock.setLastname("");
    mock.setEmail(null);
    mock.setSex(null);
    errors = mock.validate();
    assertEquals(1, errors.size());
    error = errors.get(0);
    assertEquals("firstname", error.getField());
  }

  @Override
  protected Class<? extends Connector> getConnectorClass() {
    return SimpleRequiredInputFieldsConnector.class;
  }
}
