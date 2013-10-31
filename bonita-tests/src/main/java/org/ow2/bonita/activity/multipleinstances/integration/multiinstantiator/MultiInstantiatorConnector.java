package org.ow2.bonita.activity.multipleinstances.integration.multiinstantiator;

import java.util.ArrayList;
import java.util.List;

import org.ow2.bonita.connector.core.ConnectorError;
import org.ow2.bonita.connector.core.MultiInstantiator;

public class MultiInstantiatorConnector extends MultiInstantiator {

  private int nbInstances;

  @Override
  public int getJoinNumber() {
    return nbInstances - 2;
  }

  public void setNbInstances(int instances) {
    this.nbInstances = instances;
  }

  public void setNbInstances(Integer instances) {
    this.nbInstances = instances;
  }
  
  @Override
  public List<Object> getVariableValues() {
    List<Object> res = new ArrayList<Object>();
    for (int i = 0; i < nbInstances; i++) {
      res.add(i);
    }
    return res;
  }

  @Override
  protected List<ConnectorError> validateValues() {
    return null;
  }

}
