package org.ow2.bonita.activity.instantiation;

import org.ow2.bonita.definition.TxHook;
import org.ow2.bonita.facade.APIAccessor;
import org.ow2.bonita.facade.runtime.ActivityInstance;

public class CheckExecutionHook implements TxHook {

  public void execute(APIAccessor accessor,
    ActivityInstance activityInstance) throws Exception {
    
    int nbPass = 0;
    int limitPass = 2;
    
    for (ActivityInstance act : accessor.getQueryRuntimeAPI().getActivityInstances(activityInstance.getProcessInstanceUUID(), "a2")) {
      if (act.getLastKnownVariableValues().get("conditionVar").equals("true")) {
        nbPass++;
      }
    }
    accessor.getRuntimeAPI().setActivityInstanceVariable(activityInstance.getUUID(), "conditionVar2", Boolean.toString(nbPass >= limitPass));
  }

}
