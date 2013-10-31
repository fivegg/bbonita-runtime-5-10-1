package org.ow2.bonita.integration.connector.test;

import java.util.List;

import org.ow2.bonita.connector.core.Connector;
import org.ow2.bonita.connector.core.ConnectorError;

public class OutputConnector extends Connector {

  private String output;

  public String getOutput() {
    return output;
  }

  @Override
  protected void executeConnector() throws Exception {
    output = "Something";
  }

  @Override
  protected List<ConnectorError> validateValues() {
    return null;
  }
}
