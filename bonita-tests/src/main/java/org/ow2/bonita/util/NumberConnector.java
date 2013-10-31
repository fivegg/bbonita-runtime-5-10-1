package org.ow2.bonita.util;

import java.util.List;

import org.ow2.bonita.connector.core.ConnectorError;
import org.ow2.bonita.connector.core.ProcessConnector;

public class NumberConnector extends ProcessConnector {

  private int number;
  
  public void setNumber(int nb) {
    this.number = nb;
  }
  
  @Override
  protected void executeConnector() throws Exception {
    int total = Integer.parseInt((String) getApiAccessor().getQueryDefinitionAPI().getProcessMetaData(getProcessDefinitionUUID(), "total"));
    total += number;
    getApiAccessor().getRuntimeAPI().addProcessMetaData(getProcessDefinitionUUID(), "total", Integer.toString(total));
  }
  
  @Override
  protected List<ConnectorError> validateValues() {
    return null;
  }
}