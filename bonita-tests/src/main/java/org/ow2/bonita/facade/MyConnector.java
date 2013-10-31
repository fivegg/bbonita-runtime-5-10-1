package org.ow2.bonita.facade;

import java.util.List;

import org.ow2.bonita.connector.core.ConnectorError;
import org.ow2.bonita.connector.core.ProcessConnector;
import org.ow2.bonita.util.AccessorUtil;

public class MyConnector extends ProcessConnector {

  String variableId;
  String newValue;
  
  public void setVariableId(String variableId) {
    this.variableId = variableId;
  }
  
  public void setNewValue(String newValue) {
    this.newValue = newValue;
  }
  
  @Override
  protected void executeConnector() throws Exception {
    AccessorUtil.getRuntimeAPI().setVariable(getActivityInstanceUUID(), variableId, newValue);
  }
  
  @Override
  protected List<ConnectorError> validateValues() {
    return null;
  }
}
