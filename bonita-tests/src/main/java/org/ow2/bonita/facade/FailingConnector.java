package org.ow2.bonita.facade;

import java.util.ArrayList;
import java.util.List;

import org.ow2.bonita.connector.core.ConnectorError;
import org.ow2.bonita.connector.core.ProcessConnector;

public class FailingConnector extends ProcessConnector {

	@Override
	protected void executeConnector() throws Exception {
		throw new RuntimeException("exception");
	}
	@Override
	protected List<ConnectorError> validateValues() {
		return new ArrayList<ConnectorError>();
	}

}