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
package org.ow2.bonita.connector.core;

import java.util.List;
import java.util.Locale;

import junit.framework.TestCase;

import org.ow2.bonita.connector.core.desc.Category;
import org.ow2.bonita.connector.examples.InputsOutputConnector;
import org.ow2.bonita.connector.examples.NoFieldsConnector;
import org.ow2.bonita.connector.examples.widget.PagesConnector;

public abstract class ConnectorDescriptionTest extends TestCase {

  protected ConnectorDescription connector;

  public final static void testNullConnector() throws ConnectorException {
    try {
      new ConnectorDescription(null);
      fail("A Connector class cannot be null");
    } catch(IllegalArgumentException iae) {
    }
  }

  public final static void testErrorConnector() {
    try {
      new ConnectorDescription(PagesConnector.class);
      fail("This connector contains errors!");
    } catch(ConnectorException iae) {
    }
  }
  
  public void testEquals() throws ConnectorException {
    ConnectorDescription cd1 = new ConnectorDescription(InputsOutputConnector.class);
    ConnectorDescription cd2 = new ConnectorDescription(InputsOutputConnector.class); 
    assertTrue(cd1.equals(cd2));
  }
  
  public void testNotEquals() throws ConnectorException {
    ConnectorDescription cd1 = new ConnectorDescription(InputsOutputConnector.class);
    ConnectorDescription cd2 = new ConnectorDescription(NoFieldsConnector.class); 
    assertFalse(cd1.equals(cd2));
  }
  
  public void testNullEquals() throws ConnectorException {
    ConnectorDescription cd1 = new ConnectorDescription(InputsOutputConnector.class);
    assertFalse(cd1.equals(null));
  }
  
  public void testObjectEquals() throws ConnectorException {
    ConnectorDescription cd1 = new ConnectorDescription(InputsOutputConnector.class);
    assertFalse(cd1.equals(new Object()));
  }
  
  public void testGetConnectorId() throws ConnectorException {
    connector = new ConnectorDescription(getConnectorClass());
    assertEquals(getExpectedConnectorId(), connector.getId());
    connector = null;
  }

  public void testGetDescription() throws ConnectorException {
    connector = new ConnectorDescription(getConnectorClass(), Locale.ENGLISH);
    String desc = connector.getDescription();
    assertEquals(getExpectedDescription(), desc);
    connector = null;
  }

  public void testGetCategoryId() throws ConnectorException {
    connector = new ConnectorDescription(getConnectorClass(), Locale.ENGLISH);
    List<Category> categories = connector.getCategories();
    assertEquals(getExpectedCategoryId(), categories);
    connector = null;
  }
  
  public void testGetOutputs() throws ConnectorException {
    connector = new ConnectorDescription(getConnectorClass(), Locale.ENGLISH);
    assertNotNull(connector.getOutputs());
    connector = null;
  }

  public abstract void testGetPages() throws ConnectorException;

  public abstract void testGetAllInputs() throws ConnectorException;

  public abstract void testGetAllPageInputs() throws ConnectorException;

  protected abstract Class<? extends Connector> getConnectorClass();

  protected abstract String getExpectedConnectorId();

  protected abstract String getExpectedDescription();

  protected abstract List<Category> getExpectedCategoryId();
}
