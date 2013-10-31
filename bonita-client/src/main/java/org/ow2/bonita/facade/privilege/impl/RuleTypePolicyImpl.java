package org.ow2.bonita.facade.privilege.impl;

import java.io.Serializable;

import org.ow2.bonita.facade.privilege.PrivilegePolicy;
import org.ow2.bonita.facade.privilege.RuleTypePolicy;
import org.ow2.bonita.facade.privilege.Rule.RuleType;
import org.ow2.bonita.util.Misc;

public class RuleTypePolicyImpl implements RuleTypePolicy, Serializable {

  /**
   * UID
   */
  private static final long serialVersionUID = -2030654783350156654L;

  protected long dbid;
  
  protected String ruleType;
  
  protected String policy;

  protected RuleTypePolicyImpl() {}
  
  public RuleTypePolicyImpl(RuleType ruleType, PrivilegePolicy policy) {
    Misc.checkArgsNotNull(ruleType, policy);
    this.ruleType = ruleType.name();
    this.policy = policy.name();
  }
  
  public RuleTypePolicyImpl(RuleTypePolicy src) {
    this.ruleType = src.getRuleType().name();
    this.policy = src.getPolicy().name();
  }

  public PrivilegePolicy getPolicy() {
    return PrivilegePolicy.valueOf(policy);
  }

  public RuleType getRuleType() {
    return RuleType.valueOf(this.ruleType);
  }

  public void setPolicy(PrivilegePolicy policy) {
    this.policy = policy.name();
  }

}
