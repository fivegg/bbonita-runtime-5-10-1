package org.ow2.bonita.async;

import java.util.ArrayList;
import java.util.List;

import org.ow2.bonita.definition.MultiInstantiatorDescriptor;
import org.ow2.bonita.facade.QueryAPIAccessor;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;

public class MultiInstantiator implements
    org.ow2.bonita.definition.MultiInstantiator {

  public MultiInstantiatorDescriptor execute(QueryAPIAccessor accessor,
      ProcessInstanceUUID instanceUUID, String activityId, String iterationId)
      throws Exception {
    List<Object> varValues = new ArrayList<Object>();
    varValues.add(Long.valueOf(11));
    varValues.add(Long.valueOf(17));
    varValues.add(Long.valueOf(29));
    MultiInstantiatorDescriptor desc = new MultiInstantiatorDescriptor(3, varValues);
    
    return desc;
  }

}
