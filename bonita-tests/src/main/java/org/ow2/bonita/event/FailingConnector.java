package org.ow2.bonita.event;

import java.util.Collections;
import java.util.List;

import org.ow2.bonita.connector.core.ConnectorError;
import org.ow2.bonita.connector.core.ProcessConnector;
import org.ow2.bonita.util.BonitaRuntimeException;

public class FailingConnector extends ProcessConnector {

  public static int numberOfExecutions = 0;
  public static long lastTime = 0;
  public static boolean fail = true;
  
  @Override
  protected void executeConnector() throws Exception {
    if (lastTime == 0) {
      lastTime = System.currentTimeMillis();
    }
    FailingConnector.numberOfExecutions++;
    final long current = System.currentTimeMillis();
    System.err.println("\n\n*****\nExecuting FailingConnector. Last execution: " + (current - lastTime) + " ms.\n*****");
    lastTime = current;
    if (fail) {
      throw new BonitaRuntimeException("Expected exception");
    }
  }

  @Override
  protected List<ConnectorError> validateValues() {
    return Collections.emptyList();
  }

}
