package org.ow2.bonita.connector.examples.widget.test;

import java.util.List;

import org.ow2.bonita.connector.core.Connector;
import org.ow2.bonita.connector.core.ConnectorError;
import org.ow2.bonita.connector.core.ConnectorTest;
import org.ow2.bonita.connector.examples.widget.SimpleListConnector;

public class SimpleListConnectorTest extends ConnectorTest {

  @Override
  protected Class<? extends Connector> getConnectorClass() {
    return SimpleListConnector.class;
  }

  @Override
  public void testValidateConnector() {
    List<ConnectorError> errors = Connector.validateConnector(getConnectorClass());
    assertEquals(2, errors.size());
    ConnectorError one = new ConnectorError("null",
        new IllegalArgumentException("The label Id is null"));
    ConnectorError two = new ConnectorError("",
        new IllegalArgumentException("The label Id is empty"));
    assertTrue(errors.contains(one));
    assertTrue(errors.contains(two));
  }

}
