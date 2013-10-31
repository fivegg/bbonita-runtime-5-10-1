package org.ow2.bonita.activity.instantiation.instantiator;

import org.ow2.bonita.definition.MultiInstantiator;
import org.ow2.bonita.definition.MultiInstantiatorDescriptor;
import org.ow2.bonita.facade.QueryAPIAccessor;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;

public class BadVarActivityInstantiator implements MultiInstantiator {

  public MultiInstantiatorDescriptor execute(QueryAPIAccessor accessor,
    ProcessInstanceUUID instanceUUID, String activityId, String iterationId)
    throws Exception {
    return new MultiInstantiatorDescriptor(1, null);
  }

}
