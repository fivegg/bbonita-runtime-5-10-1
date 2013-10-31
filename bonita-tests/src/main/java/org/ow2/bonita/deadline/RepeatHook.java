package org.ow2.bonita.deadline;

import org.ow2.bonita.definition.TxHook;
import org.ow2.bonita.facade.APIAccessor;
import org.ow2.bonita.facade.runtime.ActivityInstance;
import org.ow2.bonita.facade.runtime.TaskInstance;

public class RepeatHook implements TxHook {



  public void execute(APIAccessor apiAccessor, ActivityInstance activity) throws Exception {
    TaskInstance t = (TaskInstance) activity;
    apiAccessor.getRuntimeAPI().startTask(t.getUUID(), true);
    apiAccessor.getRuntimeAPI().finishTask(t.getUUID(), true);
  }

}
