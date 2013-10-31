package org.ow2.bonita.async;

import org.ow2.bonita.definition.TxHook;
import org.ow2.bonita.facade.APIAccessor;
import org.ow2.bonita.facade.QueryRuntimeAPI;
import org.ow2.bonita.facade.RuntimeAPI;
import org.ow2.bonita.facade.runtime.ActivityInstance;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.AccessorUtil;

public class IncrementHook implements TxHook {

  public void execute(APIAccessor accessor,
      ActivityInstance activityInstance) throws Exception {
    RuntimeAPI runtimeAPI = AccessorUtil.getRuntimeAPI();
    QueryRuntimeAPI queryRuntimeAPI = AccessorUtil.getQueryRuntimeAPI();
    String variableId = "counter";
    ProcessInstanceUUID instanceUUID = activityInstance.getProcessInstanceUUID();
    Long counter = (Long) queryRuntimeAPI.getProcessInstanceVariable(instanceUUID, variableId);
    runtimeAPI.setProcessInstanceVariable(instanceUUID, variableId, Long.valueOf(counter + 1));
  }

}
