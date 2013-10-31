package org.ow2.bonita.services.record;

import org.ow2.bonita.definition.TxHook;
import org.ow2.bonita.facade.APIAccessor;
import org.ow2.bonita.facade.runtime.ActivityInstance;

/**
 * Author Charles Souillard
 *
 */

public class DefHook1 implements TxHook {
  private static final long serialVersionUID = 1L;

  public void execute(APIAccessor accessor, ActivityInstance activityInstance) throws Exception {
    // nothing to do
  }

}
