package org.ow2.bonita.activity.instantiation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ow2.bonita.connector.core.MultipleInstancesInstantiator;

public class NullContextInitiator extends MultipleInstancesInstantiator {

  @Override
  protected List<Map<String, Object>> defineActivitiesContext() throws Exception {
    List<Map<String, Object>> contexts = new ArrayList<Map<String,Object>>();
    Map<String, Object> first = new HashMap<String, Object>();
    contexts.add(first);
    contexts.add(null);
    return contexts;
  }

}
