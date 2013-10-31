package org.ow2.bonita.integration.task;

import java.util.Set;

import org.ow2.bonita.definition.PerformerAssign;
import org.ow2.bonita.facade.QueryAPIAccessor;
import org.ow2.bonita.facade.runtime.ActivityInstance;

public class MyPerformerAssign implements PerformerAssign {

  public String selectUser(QueryAPIAccessor accessor, ActivityInstance activityInstance, Set<String> candidates) 
    throws Exception {
    return "miguel";
  }
}
