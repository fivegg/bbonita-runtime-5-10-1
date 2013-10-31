package org.ow2.bonita.connector.examples.test;

import org.ow2.bonita.connector.core.Connector;
import org.ow2.bonita.connector.core.ConnectorTest;
import org.ow2.bonita.connector.examples.UpperCaseVariableConnector;

public class UpperCaseVariableConnectorTest extends ConnectorTest {

	@Override
	protected Class<? extends Connector> getConnectorClass() {
		return UpperCaseVariableConnector.class;
	}

	public void testUppercaseVariable() throws Exception {
		UpperCaseVariableConnector upper = new UpperCaseVariableConnector();
		upper.setHello("hello");
		upper.setHELLO("HELLO");
		upper.execute();
		assertFalse(upper.getResult());
	}

}
