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
package org.ow2.bonita.persistence;

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
public interface PrivilegeDbSession extends DbSession {

  Rule getRule(String ruleUUID);

  List<Rule> getRules();

  List<Rule> getRules(Collection<String> ruleUUIDs);

  Set<Rule> getRulesByType(Set<String> ruleTypes);

  @Deprecated
  Set<Rule> getAllApplicableRules(String entityID);
  @Deprecated
  Set<Rule> getAllApplicableRules(String entityID, RuleType ruleType);

  List<Rule> getRules(RuleType ruleType, int fromIndex, int pageSize);

  Set<String> getAllExceptions(String entityID, RuleType ruleType);

  long getNumberOfRules(RuleType ruleType);

  RuleTypePolicy getRuleTypePolicy(RuleType ruleType);

  Rule findRuleByName(String name);

  @Deprecated
  Set<Rule> findRulesByNames(Set<String> rulesNames);

  List<Rule> getAllApplicableRules(String userUUID, Collection<String> roleUUIDs, Collection<String> groupUUIDs,
      Collection<String> membershipUUIDs, String entityID);

  List<Rule> getApplicableRules(RuleType ruleType, String userUUID, Collection<String> roleUUIDs,
      Collection<String> groupUUIDs, Collection<String> membershipUUIDs, String entityID);
}
