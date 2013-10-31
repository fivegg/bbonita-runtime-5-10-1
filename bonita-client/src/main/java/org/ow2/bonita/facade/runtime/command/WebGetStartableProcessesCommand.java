/**
 * Copyright (C) 2010-2013 BonitaSoft S.A.
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
import org.ow2.bonita.facade.QueryDefinitionAPI;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition.ProcessState;
import org.ow2.bonita.facade.impl.StandardAPIAccessorImpl;
import org.ow2.bonita.facade.privilege.PrivilegePolicy;
import org.ow2.bonita.facade.privilege.Rule;
import org.ow2.bonita.facade.privilege.Rule.RuleType;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.light.LightProcessDefinition;
import org.ow2.bonita.util.AccessorUtil;
import org.ow2.bonita.util.Command;

/**
 * 
 * @author Matthieu Chaffotte
 * 
 */
public class WebGetStartableProcessesCommand implements Command<Set<LightProcessDefinition>> {

  private static final long serialVersionUID = -822497593009305244L;
  private final String userUUID;
  private final Collection<String> roleUUIDs;
  private final Collection<String> groupUUIDs;
  private final Collection<String> membershipUUIDs;
  private final String entityID;

  public WebGetStartableProcessesCommand(final String userUUID, final Collection<String> roleUUIDs,
      final Collection<String> groupUUIDs, final Collection<String> membershipUUIDs, final String entityID) {
    super();
    this.userUUID = userUUID;
    this.roleUUIDs = roleUUIDs;
    this.groupUUIDs = groupUUIDs;
    this.membershipUUIDs = membershipUUIDs;
    this.entityID = entityID;
  }

  @Override
  public Set<LightProcessDefinition> execute(final Environment environment) throws Exception {
    final APIAccessor accessor = new StandardAPIAccessorImpl();
    final ManagementAPI managementAPI = accessor.getManagementAPI();
    final QueryDefinitionAPI queryDefinitionAPI = accessor.getQueryDefinitionAPI(AccessorUtil.QUERYLIST_JOURNAL_KEY);

    final Set<String> exceptions = new HashSet<String>();
    final List<Rule> applicableRules = managementAPI.getApplicableRules(RuleType.PROCESS_START, userUUID, roleUUIDs,
        groupUUIDs, membershipUUIDs, entityID);
    for (final Rule rule : applicableRules) {
      exceptions.addAll(rule.getItems());
    }

    final Set<ProcessDefinitionUUID> processUUIDs = new HashSet<ProcessDefinitionUUID>();
    for (final String processUUID : exceptions) {
      processUUIDs.add(new ProcessDefinitionUUID(processUUID));
    }
    final PrivilegePolicy processStartPolicy = managementAPI.getRuleTypePolicy(RuleType.PROCESS_START);
    switch (processStartPolicy) {
      case ALLOW_BY_DEFAULT:
        // The exceptions are the processes the entity cannot start.
        if (processUUIDs != null && !processUUIDs.isEmpty()) {
          final List<LightProcessDefinition> tempResult = queryDefinitionAPI.getAllLightProcessesExcept(processUUIDs,
              0, queryDefinitionAPI.getNumberOfProcesses());
          final Set<LightProcessDefinition> result = new HashSet<LightProcessDefinition>();
          for (final LightProcessDefinition lightProcessDefinition : tempResult) {
            if (lightProcessDefinition.getState() == ProcessState.ENABLED) {
              result.add(lightProcessDefinition);
            }
          }
          return result;
        } else {
          return queryDefinitionAPI.getLightProcesses(ProcessState.ENABLED);
        }

      case DENY_BY_DEFAULT:
        // The exceptions are the processes the entity can start.
        if (processUUIDs.size() > 0) {
          final Set<LightProcessDefinition> tempResult2 = queryDefinitionAPI.getLightProcesses(processUUIDs);
          if (tempResult2 == null || tempResult2.isEmpty()) {
            return Collections.emptySet();
          } else {
            final Set<LightProcessDefinition> result2 = new HashSet<LightProcessDefinition>();
            for (final LightProcessDefinition lightProcessDefinition : tempResult2) {
              if (lightProcessDefinition.getState() == ProcessState.ENABLED) {
                result2.add(lightProcessDefinition);
              }
            }
            return result2;
          }
        } else {
          return Collections.emptySet();
        }
      default:
        throw new IllegalArgumentException();
    }
  }

}
