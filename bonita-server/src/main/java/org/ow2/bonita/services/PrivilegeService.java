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
package org.ow2.bonita.services;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.ow2.bonita.facade.privilege.Rule;
import org.ow2.bonita.facade.privilege.RuleTypePolicy;
import org.ow2.bonita.facade.privilege.Rule.RuleType;

/**
 * @author Nicolas Chabanoles
 * 
 */
public interface PrivilegeService {
  
  @Deprecated
  Set<Rule> getRules();
  @Deprecated
  Set<Rule> getRulesByNames(Set<String> rulesNames);
  
  List<Rule> getAllRules();
  
  List<Rule> getRules(Collection<String> ruleUUIDs);
  
  Rule findRuleByName(String ruleName);
  
  Rule getRule(String ruleUUID);

  void addRule(Rule rule);
  
  void updateRule(Rule rule);

  void deleteRule(Rule rule);

  RuleTypePolicy getRuleTypePolicy(RuleType ruleType);

  void addRuleTypePolicy(RuleTypePolicy ruleTypePolicy);
  
  void updateRuleTypePolicy(RuleTypePolicy ruleTypePolicy);

  List<Rule> getAllApplicableRules(String userUUID, Collection<String> roleUUIDs, Collection<String> groupUUIDs, Collection<String> membershipUUIDs, String entityID);

  List<Rule> getApplicableRules(RuleType ruleType, String userUUID, Collection<String> roleUUIDs, Collection<String> groupUUIDs, Collection<String> membershipUUIDs, String entityID);
  
  Set<Rule> getRulesByType(RuleType... ruleTypes);
  @Deprecated
  Set<Rule> getApplicableRules(String entityID, RuleType ruleType);
  @Deprecated
  Set<Rule> getAllApplicableRules(String entityID);

  Set<String> getExceptions(String entityID, RuleType ruleType);

  List<Rule> getRules(RuleType ruleType, int fromIndex, int pageSige);

  long getNumberOfRules(RuleType ruleType);

}
