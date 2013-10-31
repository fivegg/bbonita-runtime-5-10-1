package org.ow2.bonita.connector;


import java.util.List;

import org.ow2.bonita.connector.core.ConnectorError;
import org.ow2.bonita.connector.core.ProcessConnector;


public class ErrorConnectorWithStaticVar extends ProcessConnector {

private static boolean executed = false;

  @Override

  protected void executeConnector() throws Exception {

  executed = true;

    throw new Exception();

  }


  @Override

  protected List<ConnectorError> validateValues() {

    return null;

  }

  

  public static boolean isExecuted() {

return executed;

}

}