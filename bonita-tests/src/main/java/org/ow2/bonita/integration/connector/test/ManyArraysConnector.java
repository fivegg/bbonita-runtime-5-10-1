package org.ow2.bonita.integration.connector.test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ow2.bonita.connector.core.Connector;
import org.ow2.bonita.connector.core.ConnectorError;

public class ManyArraysConnector extends Connector {

	private Map<String, String> map = new HashMap<String, String>();
	private String output;
	private Integer[][] integers = new Integer[0][0];
	private String intout;

	public void setIntegers(Integer[][] integers) {
		this.integers = integers;
	}
	
	public void setIntegers(List<List<Object>> map) {
		int rows = map.size();
		int cols = map.get(0).size();
		integers = new Integer[rows][cols];
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				integers[i][j] = (Integer) map.get(i).get(j);
			}
		}
	}

	public void setMap(List<List<Object>> map) {
		this.map = bonitaListToMap(map, String.class, String.class);
	}

	public String getOutput() {
		return output;
	}

	public String getIntout() {
		return intout;
	}

	public void setMap(Map<String, String> map) {
		this.map = map;
	}

	@Override
	protected void executeConnector() throws Exception {
		output = map.toString();
		
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < integers.length; i++) {
			for (int j = 0; j < integers.length; j++) {
				builder.append(integers[i][j]);
				builder.append("|");
			}
		}
		intout = builder.toString();
	}

	@Override
	protected List<ConnectorError> validateValues() {
		return null;
	}
}
