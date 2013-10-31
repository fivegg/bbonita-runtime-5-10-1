package org.ow2.bonita.async;

import org.ow2.bonita.connector.core.ProcessConnector;

public class WaitConnector extends ProcessConnector{

	private long time;
	
	@Override
	protected void executeConnector() throws Exception {
		Thread.sleep(time);
	}
	
	protected java.util.List<org.ow2.bonita.connector.core.ConnectorError> validateValues() {
		return null;
	};
	
	public void setTime(long time) {
		this.time = time;
	}
}
