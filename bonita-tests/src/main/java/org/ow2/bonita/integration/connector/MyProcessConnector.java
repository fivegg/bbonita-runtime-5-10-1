package org.ow2.bonita.integration.connector;

import java.util.List;

import org.ow2.bonita.connector.core.ConnectorError;
import org.ow2.bonita.connector.core.ProcessConnector;

public class MyProcessConnector extends ProcessConnector {

  private String result;

  public String getResult() {
    return result;
  }

  @Override
  protected void executeConnector() throws Exception {
    StringBuilder builder = new StringBuilder();
    builder.append(getProcessDefinitionUUID());
    builder.append("|");
    builder.append(getProcessInstanceUUID());
    builder.append("|");
    builder.append(getActivityInstanceUUID());
    result = builder.toString();
  }

  @Override
  protected List<ConnectorError> validateValues() {
    return null;
  }

}
