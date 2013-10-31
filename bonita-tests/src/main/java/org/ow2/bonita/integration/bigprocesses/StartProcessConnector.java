package org.ow2.bonita.integration.bigprocesses;

import java.util.List;

import org.ow2.bonita.connector.core.ConnectorError;
import org.ow2.bonita.connector.core.ProcessConnector;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;

public class StartProcessConnector extends ProcessConnector {

  private ProcessDefinitionUUID processUUIDToStart;

  public StartProcessConnector() { }
  
  public void setProcessUUIDToStart(ProcessDefinitionUUID processUUIDToStart) {
    this.processUUIDToStart = processUUIDToStart;
  }
  
  @Override
  protected void executeConnector() throws Exception {
    getApiAccessor().getRuntimeAPI().instantiateProcess(processUUIDToStart);
  }
  
  @Override
  protected List<ConnectorError> validateValues() {
    return null;
  }
  
}
