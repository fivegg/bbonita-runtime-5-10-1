package org.ow2.bonita.facade;

import java.util.List;

import org.ow2.bonita.connector.core.ConnectorError;
import org.ow2.bonita.connector.core.ProcessConnector;

public class ErrorConnector extends ProcessConnector {
  @Override
  protected void executeConnector() throws Exception {
    throw new Exception("ErrorConnector performed its role");
  }

  @Override
  protected List<ConnectorError> validateValues() {
    return null;
  }
}