package org.ow2.bonita.activity.multipleinstances.instantiator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ow2.bonita.connector.core.MultipleInstancesInstantiator;

public class NoContextMulitpleActivitiesInstantiator extends MultipleInstancesInstantiator {

  private int number;

  public void setNumber(int number) {
    this.number = number;
  }

  @Override
  protected List<Map<String, Object>> defineActivitiesContext() throws Exception {
    List<Map<String, Object>> context = new ArrayList<Map<String,Object>>();
    for (int i = 0; i < number; i++) {
      Map<String, Object> activityContext = new HashMap<String, Object>();
      context.add(activityContext);
    }
    return context;
  }

}
