package org.ow2.bonita.connector.examples.test;

import java.util.List;

import org.ow2.bonita.connector.core.Connector;
import org.ow2.bonita.connector.core.ConnectorError;
import org.ow2.bonita.connector.core.ConnectorTest;
import org.ow2.bonita.connector.examples.GetterConnector;
import org.ow2.bonita.util.BonitaException;

public class GetterConnectorTest extends ConnectorTest {

  @Override
  protected Class<? extends Connector> getConnectorClass() {
    return GetterConnector.class;
  }
  
  public void testValidateConnector() throws BonitaException {
    List<ConnectorError> errors = Connector.validateConnector(getConnectorClass());
    assertEquals(6, errors.size());
    ConnectorError one = new ConnectorError("null",
        new IllegalArgumentException("A getter name is null"));
    ConnectorError two = new ConnectorError("",
        new IllegalArgumentException("A getter name is empty"));
    ConnectorError three = new ConnectorError("address",
        new IllegalArgumentException("address does not refer to a method of org.ow2.bonita.connector.examples.GetterConnector"));
    ConnectorError four = new ConnectorError("name",
        new IllegalArgumentException("is already set"));
    ConnectorError five = new ConnectorError("data",
        new IllegalArgumentException("A getter method do return a value"));
    ConnectorError six = new ConnectorError("nam",
        new IllegalArgumentException("nam does not refer to a method of org.ow2.bonita.connector.examples.GetterConnector"));
    assertTrue(errors.contains(one));
    assertTrue(errors.contains(two));
    assertTrue(errors.contains(three));
    assertTrue(errors.contains(four));
    assertTrue(errors.contains(five));
    assertTrue(errors.contains(six));
  }

}
