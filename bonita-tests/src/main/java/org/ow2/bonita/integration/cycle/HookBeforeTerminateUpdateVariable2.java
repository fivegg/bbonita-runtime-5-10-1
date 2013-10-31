package org.ow2.bonita.integration.cycle;

import org.ow2.bonita.definition.TxHook;
import org.ow2.bonita.facade.APIAccessor;
import org.ow2.bonita.facade.RuntimeAPI;
import org.ow2.bonita.facade.runtime.ActivityInstance;

/**
 * @author Guillaume Porcher
 */
public class HookBeforeTerminateUpdateVariable2 implements TxHook {
  private static final long serialVersionUID = 1L;

  public void execute(APIAccessor accessor, ActivityInstance activityInstance) throws Exception {
    RuntimeAPI runtimeAPI = accessor.getRuntimeAPI();
    runtimeAPI.setProcessInstanceVariable(activityInstance.getProcessInstanceUUID(), "cont2", "0");
  }

}
