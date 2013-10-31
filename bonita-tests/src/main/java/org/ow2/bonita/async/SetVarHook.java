package org.ow2.bonita.async;

import org.ow2.bonita.definition.TxHook;
import org.ow2.bonita.facade.APIAccessor;
import org.ow2.bonita.facade.RuntimeAPI;
import org.ow2.bonita.facade.runtime.ActivityInstance;
import org.ow2.bonita.util.AccessorUtil;

public class SetVarHook implements TxHook {

  public void execute(APIAccessor accessor,
      ActivityInstance activityInstance) throws Exception {
    RuntimeAPI runtimeAPI = AccessorUtil.getRuntimeAPI();
    runtimeAPI.setProcessInstanceVariable(activityInstance.getProcessInstanceUUID(), "var", "setFromHook");
  }

}
