package org.ow2.bonita.async;

import org.ow2.bonita.definition.TxHook;
import org.ow2.bonita.facade.APIAccessor;
import org.ow2.bonita.facade.runtime.ActivityInstance;

public class NoOpHook implements TxHook {

  public void execute(APIAccessor accessor,
      ActivityInstance activityInstance) throws Exception {
    // TODO Auto-generated method stub

  }

}
