package org.ow2.bonita.integration.hook;

import org.ow2.bonita.definition.Hook;
import org.ow2.bonita.facade.QueryAPIAccessor;
import org.ow2.bonita.facade.QueryRuntimeAPI;
import org.ow2.bonita.facade.runtime.ActivityInstance;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.util.BonitaRuntimeException;

/**
 * author Marc Blachon, Charles Souillard
 * Hook that check activity variables
 *
 */

public class HookBeforeTerminateCheckActVariable implements Hook {
  private static final long serialVersionUID = 1L;

  public void execute(QueryAPIAccessor accessor, ActivityInstance activityInstance) throws Exception {
    ActivityInstanceUUID activityUUID = activityInstance.getUUID();
    QueryRuntimeAPI queryRuntimeAPI = accessor.getQueryRuntimeAPI();
    String actStr1 = (String) queryRuntimeAPI.getActivityInstanceVariable(activityUUID, "act_str1");
    String actEnumStat = (String) queryRuntimeAPI.getActivityInstanceVariable(activityUUID, "act_enum_stat");

    if (actStr1 == null) {
      throw new BonitaRuntimeException("actStr1 is null");
    }
    if (actEnumStat == null) {
      throw new BonitaRuntimeException("actEnumStat is null");
    }
    if (!actEnumStat.equals("iiii")) {
      throw new BonitaRuntimeException("actEnumStat is not equals to iiii");
    }
  }

}
