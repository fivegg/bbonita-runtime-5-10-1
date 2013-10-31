package org.ow2.bonita.activity.instantiation.instantiator;

import java.util.ArrayList;
import java.util.List;

import org.ow2.bonita.definition.MultiInstantiator;
import org.ow2.bonita.definition.MultiInstantiatorDescriptor;
import org.ow2.bonita.facade.QueryAPIAccessor;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;

public class JoinNumberConditionFail implements MultiInstantiator {

  public MultiInstantiatorDescriptor execute(QueryAPIAccessor accessor,
    ProcessInstanceUUID instanceUUID, String activityId, String iterationId)
    throws Exception {
    List<Object> var = new ArrayList<Object>();
    var.add("false");
    var.add("true");
    var.add("false");
    return new MultiInstantiatorDescriptor(2, var);
  }

}
