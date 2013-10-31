package org.ow2.bonita.connector.examples.widget;

import java.util.List;

import org.ow2.bonita.connector.core.ConnectorError;
import org.ow2.bonita.connector.core.ProcessConnector;

public class NullCategoryNameConnector extends ProcessConnector {

  private Long integer;

  public Long getInteger() {
    return integer;
  }

  public void setInteger(Long integer) {
    this.integer = integer;
  }

  @Override
  protected void executeConnector() throws Exception {
  }
  
  @Override
  protected List<ConnectorError> validateValues() {
    return null;
  }
}
