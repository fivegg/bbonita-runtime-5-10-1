package org.ow2.bonita.connector.examples.test;

import org.ow2.bonita.connector.core.Connector;
import org.ow2.bonita.connector.core.ConnectorTest;
import org.ow2.bonita.connector.examples.InputsOutputConnector;

public class InputsOutputConnectorTest extends ConnectorTest {

  @Override
  protected Class<? extends Connector> getConnectorClass() {
    return InputsOutputConnector.class;
  }
}
