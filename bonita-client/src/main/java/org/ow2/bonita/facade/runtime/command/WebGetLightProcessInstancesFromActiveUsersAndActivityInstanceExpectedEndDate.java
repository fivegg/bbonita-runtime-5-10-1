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

import java.util.Collection;
import java.util.Collections;
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
import org.ow2.bonita.light.LightProcessInstance;
import org.ow2.bonita.util.AccessorUtil;
import org.ow2.bonita.util.Command;

/**
 * 
 * @author Matthieu Chaffotte
 * 
 */
public class WebGetLightProcessInstancesFromActiveUsersAndActivityInstanceExpectedEndDate implements
    Command<List<LightProcessInstance>> {

  private static final long serialVersionUID = 1073206586260605889L;
  private final String userId;
  private final Collection<String> roleUUIDs;
  private final Collection<String> groupUUIDs;
  private final Collection<String> membershipUUIDs;
  private final String username;
  private final int fromIndex;
  private final int pageSize;
  private final boolean searchInHistory;
  private final int remainingDays;

  public WebGetLightProcessInstancesFromActiveUsersAndActivityInstanceExpectedEndDate(final String userId,
      final Set<String> roleUUIDs, final Set<String> groupUUIDs, final Set<String> membershipUUIDs,
      final String username, final int remainingDays, final int fromIndex, final int pageSize,
      final boolean searchInHistory) {
    this.remainingDays = remainingDays;
    this.userId = userId;
    this.roleUUIDs = roleUUIDs;
    this.groupUUIDs = groupUUIDs;
    this.membershipUUIDs = membershipUUIDs;
    this.username = username;
    this.fromIndex = fromIndex;
    this.pageSize = pageSize;
    this.searchInHistory = searchInHistory;
  }

  @Override
  public List<LightProcessInstance> execute(final Environment environment) throws Exception {
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
          List<LightProcessInstance> result;
          result = queryRuntimeAPI
              .getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDateExcept(username,
                  remainingDays, fromIndex, pageSize, processUUIDs);
          return result;
        } else {
          List<LightProcessInstance> result;
          result = queryRuntimeAPI.getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(
              username, remainingDays, fromIndex, pageSize);
          return result;
        }

      case DENY_BY_DEFAULT:
        // The exceptions are the processes the entity can see.
        if (processUUIDs.size() > 0) {
          List<LightProcessInstance> result;
          result = queryRuntimeAPI.getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate(
              username, remainingDays, fromIndex, pageSize, processUUIDs);
          return result;
        } else {
          return Collections.emptyList();
        }
      default:
        throw new IllegalArgumentException();
    }
  }

}
