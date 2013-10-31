package org.ow2.bonita.multitenancy;

import java.util.Collections;
import java.util.List;

import org.ow2.bonita.connector.core.ConnectorError;
import org.ow2.bonita.connector.core.ProcessConnector;

public class CheckClassesConnector extends ProcessConnector {

  private String foundClassName;
  private String notFoundClassName;
  
  @Override
  protected void executeConnector() throws Exception {
    Thread.currentThread().getContextClassLoader().loadClass(foundClassName);
    try {
      Thread.currentThread().getContextClassLoader().loadClass(notFoundClassName);
      throw new RuntimeException("Class: " + notFoundClassName + " must not be found!");
    } catch (ClassNotFoundException e) {
    }
  }

  public void setFoundClassName(String foundClassName) {
    this.foundClassName = foundClassName;
  }
  
  public void setNotFoundClassName(String notFoundClassName) {
    this.notFoundClassName = notFoundClassName;
  }
  
  @Override
  protected List<ConnectorError> validateValues() {
    return Collections.emptyList();
  }

}
