package org.ow2.bonita.async;

import org.ow2.bonita.definition.TxHook;
import org.ow2.bonita.facade.APIAccessor;
import org.ow2.bonita.facade.QueryRuntimeAPI;
import org.ow2.bonita.facade.RuntimeAPI;
import org.ow2.bonita.facade.runtime.ActivityInstance;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.AccessorUtil;

public class MultiIncrementHook implements TxHook {

  public static Object mutex = new Object();
  
  public void execute(APIAccessor accessor,
      ActivityInstance activityInstance) throws Exception {
    
    RuntimeAPI runtimeAPI = AccessorUtil.getRuntimeAPI();
    QueryRuntimeAPI queryRuntimeAPI = AccessorUtil.getQueryRuntimeAPI();
    ActivityInstanceUUID activityUUID = activityInstance.getUUID();
    
    Long multiInstvarValue = (Long) queryRuntimeAPI.getActivityInstanceVariable(activityUUID, "multiInstVar");
    
    String variableId = "counter";
    ProcessInstanceUUID instanceUUID = activityInstance.getProcessInstanceUUID();
    
    synchronized (mutex) {
      Long counter = (Long) queryRuntimeAPI.getProcessInstanceVariable(instanceUUID, variableId);
      runtimeAPI.setProcessInstanceVariable(instanceUUID, variableId, Long.valueOf(counter + multiInstvarValue));
    }
    

  }

}
