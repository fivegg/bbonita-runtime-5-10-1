package org.ow2.bonita.facade.privilege;

import org.ow2.bonita.facade.privilege.Rule.RuleType;

public interface RuleTypePolicy {
  
  RuleType getRuleType();
  
  PrivilegePolicy getPolicy();
}
