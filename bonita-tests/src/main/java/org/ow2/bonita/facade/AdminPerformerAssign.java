package org.ow2.bonita.facade;

import java.util.Set;

import org.ow2.bonita.definition.PerformerAssign;
import org.ow2.bonita.facade.runtime.ActivityInstance;

public class AdminPerformerAssign implements PerformerAssign {

  public String selectUser(QueryAPIAccessor accessor, ActivityInstance activityInstance, Set<String> candidates) {
    return "admin";
  }
}
