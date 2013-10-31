package org.ow2.bonita.connector.examples.widget.test;

import java.util.List;

import org.ow2.bonita.connector.core.Connector;
import org.ow2.bonita.connector.core.ConnectorError;
import org.ow2.bonita.connector.core.ConnectorTest;
import org.ow2.bonita.connector.examples.widget.NullCategoryNameConnector;
import org.ow2.bonita.util.BonitaException;

public class NullCategoryNameConnectorTest extends ConnectorTest{

  @Override
  protected Class<? extends Connector> getConnectorClass() {
    return NullCategoryNameConnector.class;
  }

  @Override
  public void testValidateConnector() throws BonitaException {
    List<ConnectorError> errors = Connector.validateConnector(getConnectorClass());
    assertEquals(1, errors.size());
    ConnectorError error = new ConnectorError("NullCategoryNameConnector",
        new IllegalArgumentException("The category name is missing"));
    assertTrue(errors.contains(error));
  }

}
