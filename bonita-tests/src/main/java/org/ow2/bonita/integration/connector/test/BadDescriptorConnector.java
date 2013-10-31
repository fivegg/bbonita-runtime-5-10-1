package org.ow2.bonita.integration.connector.test;

import java.util.List;

import org.ow2.bonita.connector.core.Connector;
import org.ow2.bonita.connector.core.ConnectorError;

public class BadDescriptorConnector extends Connector {

  private String number;
  private int result;
  
  public int getResult() {
    return result;
  }

  public void setNumber(String number) {
    this.number = number;
  }

  @Override
  protected void executeConnector() throws Exception {
    result = Integer.parseInt(number);
  }

  @Override
  protected List<ConnectorError> validateValues() {
    return null;
  }
}
