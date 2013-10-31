package org.ow2.bonita.connector.examples.test;

import java.util.List;

import org.ow2.bonita.connector.core.Connector;
import org.ow2.bonita.connector.core.ConnectorError;
import org.ow2.bonita.connector.core.ConnectorTest;
import org.ow2.bonita.connector.examples.SetterConnector;
import org.ow2.bonita.util.BonitaException;

public class SetterConnectorTest extends ConnectorTest {

  @Override
  protected Class<? extends Connector> getConnectorClass() {
    return SetterConnector.class;
  }
  
  public void testValidateConnector() throws BonitaException {
    List<ConnectorError> errors = Connector.validateConnector(getConnectorClass());
    assertEquals(10, errors.size());
    ConnectorError one = new ConnectorError("null",
        new IllegalArgumentException("A setter name is null"));
    ConnectorError two = new ConnectorError("",
        new IllegalArgumentException("A setter name is empty"));
    ConnectorError three = new ConnectorError("isMale",
        new IllegalArgumentException("A setter method starts with set"));
    ConnectorError four = new ConnectorError("setAddress",
        new IllegalArgumentException("setAddress(Boolean) does not refer to a method of org.ow2.bonita.connector.examples.SetterConnector"));
    ConnectorError five = new ConnectorError("setA",
        new IllegalArgumentException("setA returns a value so it is not a setter method"));
    ConnectorError six = new ConnectorError("setAge",
        new IllegalArgumentException("Impossible to set a field when required and forbidden are equal"));
    ConnectorError seven = new ConnectorError("setAddress",
        new IllegalArgumentException("is already set"));
    ConnectorError eight = new ConnectorError("setAddress",
        new IllegalArgumentException("Operator not valid"));
    ConnectorError nine = new ConnectorError("setAddress",
        new IllegalArgumentException("The left expression is missing"));
    ConnectorError ten = new ConnectorError("setB",
        new IllegalArgumentException("A setter method does not refer to an attribute of org.ow2.bonita.connector.examples.SetterConnector"));
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
  }

}
