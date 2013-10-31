/**
 * Copyright (C) 2009-2013 BonitaSoft S.A.
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
package org.ow2.bonita.facade.runtime.command;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ow2.bonita.env.Environment;
import org.ow2.bonita.facade.APIAccessor;
import org.ow2.bonita.facade.ManagementAPI;
import org.ow2.bonita.facade.QueryRuntimeAPI;
import org.ow2.bonita.facade.impl.StandardAPIAccessorImpl;
import org.ow2.bonita.facade.privilege.PrivilegePolicy;
import org.ow2.bonita.facade.privilege.Rule;
import org.ow2.bonita.facade.privilege.Rule.RuleType;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.util.AccessorUtil;
import org.ow2.bonita.util.Command;

/**
 * 
 * @author Matthieu Chaffotte
 * 
 */
public class WebGetProcessInstancesNumberWithInvolvedUserAndCategory implements Command<Integer> {

  private static final long serialVersionUID = 6841540520107266785L;
  private final String userId;
  private final Set<String> roleUUIDs;
  private final Set<String> groupUUIDs;
  private final Set<String> membershipUUIDs;
  private final String username;
  private final boolean searchInHistory;
  private final String category;

  public WebGetProcessInstancesNumberWithInvolvedUserAndCategory(final String userId, final Set<String> roleUUIDs,
      final Set<String> groupUUIDs, final Set<String> membershipUUIDs, final String username, final String category,
      final boolean searchInHistory) {
    this.userId = userId;
    this.roleUUIDs = roleUUIDs;
    this.groupUUIDs = groupUUIDs;
    this.membershipUUIDs = membershipUUIDs;
    this.username = username;
    this.searchInHistory = searchInHistory;
    this.category = category;
  }

  @Override
  public Integer execute(final Environment environment) throws Exception {
    final APIAccessor accessor = new StandardAPIAccessorImpl();
    final QueryRuntimeAPI queryRuntimeAPI;
    if (searchInHistory) {
      queryRuntimeAPI = accessor.getQueryRuntimeAPI(AccessorUtil.QUERYLIST_HISTORY_KEY);
    } else {
      queryRuntimeAPI = accessor.getQueryRuntimeAPI(AccessorUtil.QUERYLIST_JOURNAL_KEY);
    }

    final ManagementAPI managementAPI = accessor.getManagementAPI();
    final Set<String> exceptions = new HashSet<String>();
    final List<Rule> applicableRules = managementAPI.getApplicableRules(RuleType.PROCESS_READ, userId, roleUUIDs,
        groupUUIDs, membershipUUIDs, username);
    for (final Rule rule : applicableRules) {
      exceptions.addAll(rule.getItems());
    }

    final Set<ProcessDefinitionUUID> processUUIDs = new HashSet<ProcessDefinitionUUID>();
    for (final String processUUID : exceptions) {
      processUUIDs.add(new ProcessDefinitionUUID(processUUID));
    }
    final PrivilegePolicy processStartPolicy = managementAPI.getRuleTypePolicy(RuleType.PROCESS_READ);
    switch (processStartPolicy) {
      case ALLOW_BY_DEFAULT:
        // The exceptions are the processes the entity cannot see.
        if (processUUIDs != null && !processUUIDs.isEmpty()) {
          Integer result;
          result = queryRuntimeAPI.getNumberOfParentProcessInstancesWithInvolvedUserAndCategoryExcept(username,
              category, processUUIDs);
          return result;
        } else {
          Integer result;
          result = queryRuntimeAPI.getNumberOfParentProcessInstancesWithInvolvedUserAndCategory(username, category);
          return result;
        }

      case DENY_BY_DEFAULT:
        // The exceptions are the processes the entity can see.
        if (processUUIDs.size() > 0) {
          Integer result;
          result = queryRuntimeAPI.getNumberOfParentProcessInstancesWithInvolvedUserAndCategory(username, category,
              processUUIDs);
          return result;
        } else {
          return 0;
        }
      default:
        throw new IllegalArgumentException();
    }
  }

}
