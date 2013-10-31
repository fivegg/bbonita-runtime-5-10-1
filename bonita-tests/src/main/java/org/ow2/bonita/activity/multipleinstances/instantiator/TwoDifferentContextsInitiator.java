package org.ow2.bonita.activity.multipleinstances.instantiator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ow2.bonita.connector.core.MultipleInstancesInstantiator;

public class TwoDifferentContextsInitiator extends MultipleInstancesInstantiator {

  @Override
  protected List<Map<String, Object>> defineActivitiesContext() throws Exception {
    List<Map<String, Object>> contexts = new ArrayList<Map<String,Object>>();
    Map<String, Object> firstContext = new HashMap<String, Object>();
    firstContext.put("a", 1);
    firstContext.put("b", 1);

    Map<String, Object> secondContext = new HashMap<String, Object>();
    secondContext.put("a", 2);
    secondContext.put("b", 2);

    contexts.add(firstContext);
    contexts.add(secondContext);
    return contexts;
  }

}
