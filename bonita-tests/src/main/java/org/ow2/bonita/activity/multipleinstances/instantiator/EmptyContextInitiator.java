package org.ow2.bonita.activity.multipleinstances.instantiator;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.ow2.bonita.connector.core.MultipleInstancesInstantiator;

public class EmptyContextInitiator extends MultipleInstancesInstantiator {

  @Override
  protected List<Map<String, Object>> defineActivitiesContext()
      throws Exception {
    return Collections.emptyList();
  }

}
