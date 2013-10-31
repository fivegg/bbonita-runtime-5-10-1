package org.ow2.bonita.runtime;

import java.util.List;
import java.util.logging.Logger;

import org.ow2.bonita.connector.core.ConnectorError;
import org.ow2.bonita.connector.core.ProcessConnector;


public class ReplayErrorConnector extends ProcessConnector {
	
	protected static final Logger LOG = Logger.getLogger(ReplayErrorConnector.class.getName());

	protected Boolean value=false;
	
	@Override
	protected void executeConnector() throws Exception {
		LOG.info("======== ReplayErrorConnector execution ===========");
		if(this.value){
			throw new Exception();
		}
	}

	@Override
	protected List<ConnectorError> validateValues() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setValue(Boolean v){
		this.value=v;
	}	
	
}
