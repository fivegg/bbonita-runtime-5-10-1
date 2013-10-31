package org.ow2.bonita.deadline;

import org.ow2.bonita.definition.TxHook;
import org.ow2.bonita.facade.APIAccessor;
import org.ow2.bonita.facade.RuntimeAPI;
import org.ow2.bonita.facade.runtime.ActivityInstance;

public class SearchDemandHook implements TxHook {

  private static boolean first = true;

  public void execute(APIAccessor apiAccessor, ActivityInstance act) throws Exception {
    if (first) {
      first = false;
    } else {
      RuntimeAPI runtimeAPI = apiAccessor.getRuntimeAPI();
      runtimeAPI.setProcessInstanceVariable(act.getProcessInstanceUUID(), "demandTrouvee", "yes");
    }
  }

}
