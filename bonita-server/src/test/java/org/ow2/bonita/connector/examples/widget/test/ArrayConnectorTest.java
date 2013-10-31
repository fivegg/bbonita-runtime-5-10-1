package org.ow2.bonita.connector.examples.widget.test;

import java.util.List;

import org.ow2.bonita.connector.core.Connector;
import org.ow2.bonita.connector.core.ConnectorError;
import org.ow2.bonita.connector.core.ConnectorTest;
import org.ow2.bonita.connector.examples.widget.ArrayConnector;

public class ArrayConnectorTest extends ConnectorTest {

  @Override
  protected Class<? extends Connector> getConnectorClass() {
    return ArrayConnector.class;
  }

  @Override
  public void testValidateConnector() {
    List<ConnectorError> errors = Connector.validateConnector(getConnectorClass());
    assertEquals(5, errors.size());

    ConnectorError one = new ConnectorError("null",
        new IllegalArgumentException("The label Id is null"));
    ConnectorError two = new ConnectorError("",
        new IllegalArgumentException("The label Id is empty"));
    ConnectorError three = new ConnectorError("a",
        new IllegalArgumentException("The column number cannot be less than 1"));
    ConnectorError four = new ConnectorError("b",
        new IllegalArgumentException("The row number cannot be less than 1"));
    ConnectorError five = new ConnectorError("c",
        new IllegalArgumentException("The size of the caption array is different from the columns number"));
    assertTrue(errors.contains(one));
    assertTrue(errors.contains(two));
    assertTrue(errors.contains(three));
    assertTrue(errors.contains(four));
    assertTrue(errors.contains(five));
  }
}
