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

import java.lang.reflect.Field;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.ow2.bonita.connector.examples.InputFieldsConnector;
import org.ow2.bonita.connector.examples.InputFieldsOnCascadeConnector;
import org.ow2.bonita.util.BonitaException;

public abstract class ConnectorTest extends TestCase {

  protected static final Logger LOG = Logger.getLogger(ConnectorTest.class.getName());

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    if (ConnectorTest.LOG.isLoggable(Level.WARNING)) {
      ConnectorTest.LOG.warning("======== Starting test: " + this.getClass().getName() + "." + this.getName() + "() ==========");
    }
  }

  @Override
  protected void tearDown() throws Exception {
    if (ConnectorTest.LOG.isLoggable(Level.WARNING)) {
      ConnectorTest.LOG.warning("======== Ending test: " + this.getName() + " ==========");
    }
    super.tearDown();
  }

  public final void testGetFieldName() {
    String fieldName = Connector.getFieldName("setName");
    Assert.assertEquals("name", fieldName);
    fieldName = Connector.getFieldName("getName");
    Assert.assertEquals("name", fieldName);
    fieldName = Connector.getFieldName("isName");
    Assert.assertEquals("name", fieldName);
    fieldName = Connector.getFieldName("set");
    Assert.assertEquals("", fieldName);
    fieldName = Connector.getFieldName("get");
    Assert.assertEquals("", fieldName);
    fieldName = Connector.getFieldName("is");
    Assert.assertEquals("", fieldName);
  }

  public final void testGetField() {
    Class<InputFieldsConnector> mock = InputFieldsConnector.class;
    Class<InputFieldsOnCascadeConnector> mock2 = InputFieldsOnCascadeConnector.class;
    Field field = Connector.getField(mock, "address");
    Assert.assertNull(field);
    field = Connector.getField(mock, "name");
    Assert.assertNotNull(field);
    field = Connector.getField(mock, "Name");
    Assert.assertNull(field);

    field = Connector.getField(mock2, "Name");
    Assert.assertNull(field);
    field = Connector.getField(mock2, "name");
    Assert.assertNotNull(field);
    field = Connector.getField(mock2, "address");
    Assert.assertNotNull(field);
  }

  public void testIsFieldExist() {
    Class<InputFieldsConnector> mock = InputFieldsConnector.class;
    Class<InputFieldsOnCascadeConnector> mock2 = InputFieldsOnCascadeConnector.class;

    Assert.assertTrue(Connector.isFieldExist(mock, "name"));
    Assert.assertFalse(Connector.isFieldExist(mock, "Name"));
    Assert.assertFalse(Connector.isFieldExist(mock, "address"));
    Assert.assertFalse(Connector.isFieldExist(mock, ""));

    Assert.assertTrue(Connector.isFieldExist(mock2, "name"));
    Assert.assertFalse(Connector.isFieldExist(mock2, "Name"));
    Assert.assertTrue(Connector.isFieldExist(mock2, "address"));
  }

  public void testValidateConnector() throws BonitaException {
    List<ConnectorError> errors = Connector.validateConnector(getConnectorClass());
    for (ConnectorError error : errors) {
      System.out.println(error.getField() + " " + error.getError());
    }
    Assert.assertTrue(
        Connector.validateConnector(getConnectorClass()).isEmpty());
  }

  public void work(Connector connector) throws BonitaException {
    if (connector.containsErrors()) {
      containsErrors(connector, 1);
      fail(connector.getClass().getName() + " contains errors!");
    }
    try {
      connector.execute();
    } catch (Exception e) {
      e.printStackTrace();
      fail("The execution of " + connector.getClass().getName() + " should work.");
    }
  }
  
  public void containsErrors(Connector connector, int errorNumber) throws BonitaException {
    List<ConnectorError> errors= connector.validate();
    for (ConnectorError error : errors) {
      System.out.println(error.getField() + " " + error.getError());
    }
    Assert.assertEquals(errorNumber, errors.size());
  }
  
  public void fail(Connector connector) {
    try {
      connector.execute();
      fail("It should fail!");
    } catch (Exception e) {
    }
  }
  
  protected abstract Class<? extends Connector> getConnectorClass();
}
