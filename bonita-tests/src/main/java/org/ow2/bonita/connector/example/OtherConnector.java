package org.ow2.bonita.connector.example;

import java.util.List;

import org.ow2.bonita.connector.core.Connector;
import org.ow2.bonita.connector.core.ConnectorError;

public class OtherConnector extends Connector {

	@Override
	protected void executeConnector() throws Exception {
	}

	@Override
	protected List<ConnectorError> validateValues() {
		return null;
	}
}
