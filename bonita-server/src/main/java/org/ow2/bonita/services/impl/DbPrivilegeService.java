/**
 * Copyright (C) 2010  BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA  02110-1301, USA.
 **/
package org.ow2.bonita.services.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ow2.bonita.facade.privilege.PrivilegePolicy;
import org.ow2.bonita.facade.privilege.Rule;
import org.ow2.bonita.facade.privilege.RuleTypePolicy;
import org.ow2.bonita.facade.privilege.Rule.RuleType;
import org.ow2.bonita.facade.privilege.impl.RuleImpl;
import org.ow2.bonita.facade.privilege.impl.RuleTypePolicyImpl;
import org.ow2.bonita.persistence.PrivilegeDbSession;
import org.ow2.bonita.persistence.db.HibernateDbSession;
import org.ow2.bonita.services.PrivilegeService;
import org.ow2.bonita.util.EnvTool;

/**
 * @author Nicolas Chabanoles, Rodrigue Le Gall
 * 
 */
public class DbPrivilegeService extends HibernateDbSession implements
    PrivilegeService {

  private String persistenceServiceName;

  public DbPrivilegeService(String persistenceServiceName) {
    super();
    this.persistenceServiceName = persistenceServiceName;
  }

  protected PrivilegeDbSession getDbSession() {
    return EnvTool.getPrivilegeDbSession(persistenceServiceName);
  }

  public Rule getRule(String ruleUUID) {
    return getDbSession().getRule(ruleUUID);
  }
  
  public Rule findRuleByName(String name) {
    return getDbSession().findRuleByName(name);
  }

  @Deprecated
  public Set<Rule> getRules() {
    List<Rule> results = getDbSession().getRules();
    if (results != null) {
      return new HashSet<Rule>(results);
    } else {
      return Collections.emptySet();
    }
  }

  public List<Rule> getAllRules() {
    return getDbSession().getRules();
  }

  public void addRule(Rule rule) {
    getDbSession().save((RuleImpl) rule);
  }
  
  public void deleteRule(Rule rule) {
    getDbSession().delete(rule);
  }

  public void updateRule(Rule rule) {
    //Nothing to do. Hibernate flush deals with the update
  }

  @Deprecated
  public Set<Rule> getRulesByNames(Set<String> rulesName) {
    return getDbSession().findRulesByNames(rulesName);
  }

  public List<Rule> getRules(Collection<String> ruleUUIDs) {
    return getDbSession().getRules(ruleUUIDs);
  }

  public Set<Rule> getRulesByType(RuleType... ruleTypes) {
    Set<String> ruleTypesStr = new HashSet<String>();
    for (RuleType ruleType : ruleTypes) {
      ruleTypesStr.add(ruleType.name());
    }
    return getDbSession().getRulesByType(ruleTypesStr);
  }

  @Deprecated
  public Set<Rule> getAllApplicableRules(String entityID) {
    return getDbSession().getAllApplicableRules(entityID);
  }

  @Deprecated
  public Set<Rule> getApplicableRules(String entityID, RuleType ruleType) {
    return getDbSession().getAllApplicableRules(entityID, ruleType);
  }

  public RuleTypePolicy getRuleTypePolicy(RuleType ruleType) {
    RuleTypePolicy ruleTypePolicy = getDbSession().getRuleTypePolicy(ruleType);
    if (ruleTypePolicy == null) {
      PrivilegePolicy policy = null;
      switch (ruleType) {
      case CATEGORY_READ:
      case PROCESS_START:
      case PROCESS_READ:
      case ACTIVITY_READ:
      case ACTIVITY_DETAILS_READ:
      case CUSTOM:
      case ASSIGN_TO_ME_STEP:
      case ASSIGN_TO_STEP:
      case UNASSIGN_STEP:
      case CHANGE_PRIORITY_STEP:
      case PROCESS_INSTANTIATION_DETAILS_VIEW:
      case PROCESS_ADD_COMMENT:
      case PROCESS_PDF_EXPORT:
      case RESUME_STEP:
      case SUSPEND_STEP:
      case SKIP_STEP:
      case REPORT_VIEW:
      case LOGOUT:
      case DELEGEE_UPDATE:
      case PASSWORD_UPDATE:
        policy = PrivilegePolicy.ALLOW_BY_DEFAULT;
        break;
      case PROCESS_MANAGE:
      case PROCESS_INSTALL:
      case REPORT_INSTALL:
      case REPORT_MANAGE:
        policy = PrivilegePolicy.DENY_BY_DEFAULT;
        break;
      default:
        throw new IllegalArgumentException("Unknown default policy for rule type " + ruleType.name());
      }
      ruleTypePolicy = new RuleTypePolicyImpl(ruleType, policy);
      getDbSession().save(ruleTypePolicy);
    }
    return ruleTypePolicy;
  }

  public void addRuleTypePolicy(RuleTypePolicy ruleTypePolicy) {
    getDbSession().save((RuleTypePolicyImpl) ruleTypePolicy);
  }
  
  public void updateRuleTypePolicy(RuleTypePolicy ruleTypePolicy) {
    //Nothing to do. Hibernate flush deals with the update
  }

  public List<Rule> getRules(RuleType ruleType, int fromIndex, int pageSige) {
    return getDbSession().getRules(ruleType, fromIndex, pageSige);
  }

  public long getNumberOfRules(RuleType ruleType) {
    return getDbSession().getNumberOfRules(ruleType);
  }

  public Set<String> getExceptions(String entityID, RuleType ruleType) {
    return getDbSession().getAllExceptions(entityID, ruleType);
  }

  public List<Rule> getAllApplicableRules(String userUUID, Collection<String> roleUUIDs, Collection<String> groupUUIDs,
      Collection<String> membershipUUIDs, String entityID) {
    return getDbSession().getAllApplicableRules(userUUID, roleUUIDs, groupUUIDs, membershipUUIDs, entityID);
  }

  public List<Rule> getApplicableRules(RuleType ruleType, String userUUID, Collection<String> roleUUIDs,
      Collection<String> groupUUIDs, Collection<String> membershipUUIDs, String entityID) {
    return getDbSession().getApplicableRules(ruleType, userUUID, roleUUIDs, groupUUIDs, membershipUUIDs, entityID);
  }

}
