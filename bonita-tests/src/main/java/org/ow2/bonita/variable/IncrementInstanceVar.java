package org.ow2.bonita.variable;

import org.ow2.bonita.definition.TxHook;
import org.ow2.bonita.facade.APIAccessor;
import org.ow2.bonita.facade.runtime.ActivityInstance;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;

public class IncrementInstanceVar implements TxHook {

  public void execute(APIAccessor accessor, ActivityInstance activityInstance) throws Exception {
    ProcessInstanceUUID instanceUUID = activityInstance.getProcessInstanceUUID();
    Long v = (Long) accessor.getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "v");
    Long newV = Long.valueOf(v + 1);
    Thread.sleep(1010);
    accessor.getRuntimeAPI().setProcessInstanceVariable(instanceUUID, "v", newV);
    Thread.sleep(10);
  }

}
