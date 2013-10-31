package org.ow2.bonita.services.impl;

import java.util.ArrayList;
import java.util.List;

import org.ow2.bonita.connector.core.ConnectorError;
import org.ow2.bonita.connector.core.ProcessConnector;
import org.ow2.bonita.util.AccessorUtil;

public class IncrementIntegerVariableConnector extends ProcessConnector {
	private String variableName;

	@Override
	protected void executeConnector() throws Exception {
		final Integer currentValue = (Integer) getApiAccessor().getQueryRuntimeAPI(AccessorUtil.QUERYLIST_JOURNAL_KEY).getVariable(getActivityInstanceUUID(), variableName);
		final Integer newValue = currentValue + 1;
		getApiAccessor().getRuntimeAPI().setProcessInstanceVariable(getProcessInstanceUUID(), variableName, newValue);
	}
	@Override
	protected List<ConnectorError> validateValues() {
		return new ArrayList<ConnectorError>();
	}

  public void setVariableName(String variableName) {
    this.variableName = variableName;
  }

}