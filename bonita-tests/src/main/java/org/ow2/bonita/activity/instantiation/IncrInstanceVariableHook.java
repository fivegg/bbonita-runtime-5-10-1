package org.ow2.bonita.activity.instantiation;

import org.ow2.bonita.definition.TxHook;
import org.ow2.bonita.facade.APIAccessor;
import org.ow2.bonita.facade.runtime.ActivityInstance;

public class IncrInstanceVariableHook implements TxHook {

  public void execute(APIAccessor accessor,
    ActivityInstance activityInstance) throws Exception {
    Long counter = (Long)accessor.getQueryRuntimeAPI().getActivityInstanceVariable(activityInstance.getUUID(), "counter");
    counter += 3;
    accessor.getRuntimeAPI().setActivityInstanceVariable(activityInstance.getUUID(), "counter", counter);
  }

}
