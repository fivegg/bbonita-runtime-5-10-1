package org.ow2.bonita.activity.instantiation.instantiator;

import java.util.ArrayList;
import java.util.List;

import org.ow2.bonita.definition.MultiInstantiator;
import org.ow2.bonita.definition.MultiInstantiatorDescriptor;
import org.ow2.bonita.facade.QueryAPIAccessor;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;

public class HookActivityInstantiator implements MultiInstantiator {

  public MultiInstantiatorDescriptor execute(QueryAPIAccessor accessor,
    ProcessInstanceUUID instanceUUID, String activityId, String iterationId)
    throws Exception {
    List<Object> values = new ArrayList<Object>();
    values.add(1L);
    values.add(2L);
    values.add(3L);
    return new MultiInstantiatorDescriptor(3, values);
  }

}
