/**
 * 
 */
package org.ow2.bonita.connector.examples;

import java.util.List;

import org.ow2.bonita.connector.core.Connector;
import org.ow2.bonita.connector.core.ConnectorError;

public final class ObjectConnector extends Connector {
  private Object o;
  
  public Object getO() {
    return o;
  }
  
  public void setO(Object o) {
    this.o = o;
  }
  
  @Override
  protected void executeConnector() throws Exception {
    
  }
  @Override
  protected List<ConnectorError> validateValues() {
    return null;
  }
  
}