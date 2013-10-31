package org.ow2.bonita.connector.example;

import java.util.List;

import org.ow2.bonita.connector.core.Connector;
import org.ow2.bonita.connector.core.ConnectorError;

public class NoDescriptorConnector extends Connector {

	private String name;
	private int age;
	private String result;

	public String getResult() {
		return result;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setAge(int age) {
		this.age = age;
	}

	@Override
	protected void executeConnector() throws Exception {
		StringBuilder builder = new StringBuilder(name);
		builder.append(": ");
		builder.append(age);
		result = builder.toString();
	}

	@Override
	protected List<ConnectorError> validateValues() {
		return null;
	}
}
