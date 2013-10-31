package org.ow2.bonita.multitenancy;

import java.util.Collections;
import java.util.List;

import org.ow2.bonita.connector.core.ConnectorError;
import org.ow2.bonita.connector.core.ProcessConnector;

public class CheckClassConnector extends ProcessConnector {

  private String className;
  
  @Override
  protected void executeConnector() throws Exception {
    Thread.currentThread().getContextClassLoader().loadClass(className);
  }

  public void setClassName(String className) {
    this.className = className;
  }
  
  @Override
  protected List<ConnectorError> validateValues() {
    return Collections.emptyList();
  }

}
