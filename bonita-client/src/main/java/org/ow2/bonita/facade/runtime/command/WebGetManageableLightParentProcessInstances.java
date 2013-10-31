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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ow2.bonita.env.Environment;
import org.ow2.bonita.facade.APIAccessor;
import org.ow2.bonita.facade.ManagementAPI;
import org.ow2.bonita.facade.QueryRuntimeAPI;
import org.ow2.bonita.facade.impl.StandardAPIAccessorImpl;
import org.ow2.bonita.facade.paging.ProcessInstanceCriterion;
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
public class WebGetManageableLightParentProcessInstances implements Command<List<LightProcessInstance>> {

  private static final long serialVersionUID = 4223637974438770807L;
  private final int fromIndex;
  private final int pageSize;
  private final boolean searchInHistory;
  private final String userID;
  private final Set<String> roleUUIDs;
  private final Set<String> groupUUIDs;
  private final Set<String> membershipUUIDs;
  private final String entityID;

  public WebGetManageableLightParentProcessInstances(final int fromIndex, final int pageSize,
      final boolean searchInHistory, final String userID, final Set<String> userRoles, final Set<String> userGroups,
      final Set<String> userMemberships, final String userName) {
    this.fromIndex = fromIndex;
    this.pageSize = pageSize;
    this.searchInHistory = searchInHistory;
    this.userID = userID;
    roleUUIDs = userRoles;
    groupUUIDs = userGroups;
    membershipUUIDs = userMemberships;
    entityID = userName;
  }

  @Override
  public List<LightProcessInstance> execute(final Environment environment) throws Exception {
    final APIAccessor accessor = new StandardAPIAccessorImpl();
    final ManagementAPI managementAPI = accessor.getManagementAPI();
    final QueryRuntimeAPI queryRuntimeAPI;
    if (searchInHistory) {
      queryRuntimeAPI = accessor.getQueryRuntimeAPI(AccessorUtil.QUERYLIST_HISTORY_KEY);
    } else {
      queryRuntimeAPI = accessor.getQueryRuntimeAPI(AccessorUtil.QUERYLIST_JOURNAL_KEY);
    }

    final Set<String> exceptions = new HashSet<String>();
    final List<Rule> applicableRules = managementAPI.getApplicableRules(RuleType.PROCESS_MANAGE, userID, roleUUIDs,
        groupUUIDs, membershipUUIDs, entityID);
    for (final Rule rule : applicableRules) {
      exceptions.addAll(rule.getItems());
    }

    final Set<ProcessDefinitionUUID> processUUIDs = new HashSet<ProcessDefinitionUUID>();
    for (final String processUUID : exceptions) {
      processUUIDs.add(new ProcessDefinitionUUID(processUUID));
    }
    final PrivilegePolicy processStartPolicy = managementAPI.getRuleTypePolicy(RuleType.PROCESS_MANAGE);

    final List<LightProcessInstance> result;

    switch (processStartPolicy) {
      case ALLOW_BY_DEFAULT:
        // The exceptions are the processes the entity cannot manage.
        if (processUUIDs != null && !processUUIDs.isEmpty()) {
          result = queryRuntimeAPI.getLightParentProcessInstancesExcept(processUUIDs, fromIndex, pageSize,
              ProcessInstanceCriterion.DEFAULT);
          return result;
        } else {
          return queryRuntimeAPI.getLightParentProcessInstances(fromIndex, pageSize);
        }

      case DENY_BY_DEFAULT:
        // The exceptions are the processes the entity can manage.
        if (processUUIDs.size() > 0) {
          return queryRuntimeAPI.getLightParentProcessInstances(processUUIDs, fromIndex, pageSize,
              ProcessInstanceCriterion.DEFAULT);
        } else {
          return Collections.emptyList();
        }
      default:
        throw new IllegalArgumentException();
    }
  }

}
