package org.ow2.bonita.connector.examples;

import java.util.List;

import org.ow2.bonita.connector.core.Connector;
import org.ow2.bonita.connector.core.ConnectorError;

public class UpperCaseVariableConnector extends Connector {
	
	private String Hello;
	private String HELLO;
	private boolean Result;

	public boolean getResult() {
		return Result;
	}

	public void setHello(String hello) {
		Hello = hello;
	}

	public void setHELLO(String hELLO) {
		HELLO = hELLO;
	}

	@Override
	protected void executeConnector() throws Exception {
		Result = Hello.equals(HELLO);
	}

	@Override
	protected List<ConnectorError> validateValues() {
		return null;
	}

}
