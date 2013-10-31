package org.ow2.bonita.activity.multipleinstances.instantiator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ow2.bonita.connector.core.MultipleInstancesInstantiator;

public class SimpleContextInstantiator extends MultipleInstancesInstantiator {

  @Override
  protected List<Map<String, Object>> defineActivitiesContext() throws Exception {
    List<Map<String, Object>> contexts = new ArrayList<Map<String,Object>>();
    
    Map<String, Object> firstOne = new HashMap<String, Object>();
    firstOne.put("a", 10);

    Map<String, Object> secondOne = new HashMap<String, Object>();
    secondOne.put("a", 50);
    secondOne.put("b", "text");

    Map<String, Object> thirdOne = new HashMap<String, Object>();
    
    contexts.add(firstOne);
    contexts.add(secondOne);
    contexts.add(thirdOne);
    return contexts;
  }

}
