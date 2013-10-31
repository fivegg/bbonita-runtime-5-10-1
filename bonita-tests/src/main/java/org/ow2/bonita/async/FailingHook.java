package org.ow2.bonita.async;

import org.ow2.bonita.definition.TxHook;
import org.ow2.bonita.facade.APIAccessor;
import org.ow2.bonita.facade.runtime.ActivityInstance;
import org.ow2.bonita.util.BonitaRuntimeException;

public class FailingHook implements TxHook {

  public void execute(APIAccessor accessor,
      ActivityInstance activityInstance) throws Exception {
    throw new BonitaRuntimeException("Expected error in " + this.getClass().getName());
  }

}
