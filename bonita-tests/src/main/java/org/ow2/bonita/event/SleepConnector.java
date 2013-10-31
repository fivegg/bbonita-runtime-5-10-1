package org.ow2.bonita.event;

import java.util.Collections;
import java.util.List;

import org.ow2.bonita.connector.core.ConnectorError;
import org.ow2.bonita.connector.core.ProcessConnector;

public class SleepConnector extends ProcessConnector {

  private long millis;
  
  @Override
  protected void executeConnector() throws Exception {
    Thread.sleep(millis); 
  }
  @Override
  protected List<ConnectorError> validateValues() {
    return Collections.emptyList();
  }
  
  public void setMillis(long millis) {
    this.millis = millis;
  }
}
