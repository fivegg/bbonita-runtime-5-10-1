package org.ow2.bonita.activity.multipleinstances.integration.multiinstantiator;

import java.util.ArrayList;
import java.util.List;

import org.ow2.bonita.definition.MultiInstantiator;
import org.ow2.bonita.definition.MultiInstantiatorDescriptor;
import org.ow2.bonita.facade.QueryAPIAccessor;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;

public class SimpleMultiInstantiator implements MultiInstantiator {

  public MultiInstantiatorDescriptor execute(QueryAPIAccessor accessor,
      ProcessInstanceUUID instanceUUID, String activityId, String iterationId)
      throws Exception {    
    List<Object> values = new ArrayList<Object>();
    values.add(true);
    values.add(false);
    values.add(true);
    values.add(true);
    values.add(false);
    return new MultiInstantiatorDescriptor(3, values);
  }

}
