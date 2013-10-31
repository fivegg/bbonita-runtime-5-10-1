/**
 * Copyright (C) 2006  Bull S. A. S.
 * Bull, Rue Jean Jaures, B.P.68, 78340, Les Clayes-sous-Bois
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA  02110-1301, USA.
 * 
 * Modified by Nicolas Chabanoles - BonitaSoft S.A.
 **/
package org.ow2.bonita.facade.impl;

import java.util.HashSet;
import java.util.Set;

import org.ow2.bonita.facade.def.InternalProcessDefinition;
import org.ow2.bonita.facade.exception.BonitaWrapperException;
import org.ow2.bonita.facade.exception.ProcessNotFoundException;
import org.ow2.bonita.facade.privilege.PrivilegePolicy;
import org.ow2.bonita.facade.privilege.Rule.RuleType;
import org.ow2.bonita.facade.runtime.impl.InternalProcessInstance;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.services.Querier;
import org.ow2.bonita.util.EnvTool;
import org.ow2.bonita.util.Misc;

public final class FacadeUtil {

  private FacadeUtil() { }

  public static InternalProcessDefinition getProcessDefinition(final ProcessDefinitionUUID processUUID) throws ProcessNotFoundException {
    Misc.checkArgsNotNull(processUUID);
    final Querier journal = EnvTool.getJournalQueriers();
    final InternalProcessDefinition process = (InternalProcessDefinition) journal.getProcess(processUUID);
    if (process == null) {
      throw new ProcessNotFoundException("bai_FU_1", processUUID);
    }
    return process;
  }

  public static InternalProcessInstance getInstance(final ProcessInstanceUUID instanceUUID, final String queryList) {
    Misc.checkArgsNotNull(instanceUUID);
    // get instance repository
    Querier journal = null;
    if (queryList != null) {
      journal = EnvTool.getJournalQueriers(queryList);
    } else {
      journal = EnvTool.getJournalQueriers();
    }

    return (InternalProcessInstance) journal.getProcessInstance(instanceUUID);
  }

  static void checkArgsNotNull(final Object... args) {
    try {
      Misc.checkArgsNotNull(1, args);
    } catch (final IllegalArgumentException e) {
      throw new BonitaWrapperException(e);
    }
  }
  
  public static Set<ProcessDefinitionUUID> getAllowedProcessUUIDsFor(String entityID, RuleType ruleType) {
    FacadeUtil.checkArgsNotNull(entityID, ruleType);
    PrivilegePolicy policy = EnvTool.getPrivilegeService().getRuleTypePolicy(ruleType).getPolicy();
    Set<String> processUUIDs = EnvTool.getPrivilegeService().getExceptions(entityID, ruleType);
    Set<ProcessDefinitionUUID> result = new HashSet<ProcessDefinitionUUID>();
    for (String processUUID : processUUIDs) {
      result.add(new ProcessDefinitionUUID(processUUID));
    }
    if(policy == PrivilegePolicy.ALLOW_BY_DEFAULT){
      // The process listed in the rules are the one NOT allowed.
      result = EnvTool.getAllQueriers().getAllProcessDefinitionUUIDsExcept(result);
    }
    
    return result;
  }

}
