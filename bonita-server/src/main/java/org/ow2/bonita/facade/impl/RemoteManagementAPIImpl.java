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
package org.ow2.bonita.facade.impl;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.ow2.bonita.facade.exception.DeploymentException;
import org.ow2.bonita.facade.exception.ProcessNotFoundException;
import org.ow2.bonita.facade.exception.RuleNotFoundException;
import org.ow2.bonita.facade.exception.UndeletableInstanceException;
import org.ow2.bonita.facade.exception.UndeletableProcessException;
import org.ow2.bonita.facade.internal.RemoteManagementAPI;
import org.ow2.bonita.facade.privilege.Rule;
import org.ow2.bonita.facade.privilege.Rule.RuleType;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;

/**
 * 
 * @author Elias Ricken de Medeiros
 * 
 */
public class RemoteManagementAPIImpl extends AbstractRemoteManagementAPIImpl implements RemoteManagementAPI {

  @Override
  public void archive(final Collection<ProcessDefinitionUUID> processUUIDs, final Map<String, String> options)
      throws DeploymentException, RemoteException {
    getAPI(options).archive(processUUIDs);
  }

  @Override
  public void delete(final Collection<ProcessDefinitionUUID> processUUIDs, final Map<String, String> options)
      throws ProcessNotFoundException, UndeletableProcessException, UndeletableInstanceException, RemoteException {
    getAPI(options).delete(processUUIDs);
  }

  @Override
  public void disable(final Collection<ProcessDefinitionUUID> processUUIDs, final Map<String, String> options)
      throws DeploymentException, RemoteException {
    getAPI(options).disable(processUUIDs);
  }

  @Override
  public void enable(final Collection<ProcessDefinitionUUID> processUUIDs, final Map<String, String> options)
      throws DeploymentException, RemoteException {
    getAPI(options).enable(processUUIDs);
  }

  @Override
  public void applyRuleToEntities(final String ruleUUID, final Collection<String> userUUIDs,
      final Collection<String> roleUUIDs, final Collection<String> groupUUIDs,
      final Collection<String> membershipUUIDs, final Collection<String> entityIDs, final Map<String, String> options)
      throws RuleNotFoundException, RemoteException {
    getAPI(options).applyRuleToEntities(ruleUUID, userUUIDs, roleUUIDs, groupUUIDs, membershipUUIDs, entityIDs);
  }

  @Override
  public List<Rule> getAllApplicableRules(final String userUUID, final Collection<String> roleUUIDs,
      final Collection<String> groupUUIDs, final Collection<String> membershipUUIDs, final String entityID,
      final Map<String, String> options) throws RemoteException {
    return getAPI(options).getAllApplicableRules(userUUID, roleUUIDs, groupUUIDs, membershipUUIDs, entityID);
  }

  @Override
  public List<Rule> getApplicableRules(final RuleType ruleType, final String userUUID,
      final Collection<String> roleUUIDs, final Collection<String> groupUUIDs,
      final Collection<String> membershipUUIDs, final String entityID, final Map<String, String> options)
      throws RemoteException {
    return getAPI(options).getApplicableRules(ruleType, userUUID, roleUUIDs, groupUUIDs, membershipUUIDs, entityID);
  }

  @Override
  public List<Rule> getRulesByUUIDs(final Collection<String> ruleUUIDs, final Map<String, String> options)
      throws RuleNotFoundException, RemoteException {
    return getAPI(options).getRulesByUUIDs(ruleUUIDs);
  }

  @Override
  public void removeRuleFromEntities(final String ruleUUID, final Collection<String> userUUIDs,
      final Collection<String> roleUUIDs, final Collection<String> groupUUIDs,
      final Collection<String> membershipUUIDs, final Collection<String> entityIDs, final Map<String, String> options)
      throws RuleNotFoundException, RemoteException {
    getAPI(options).removeRuleFromEntities(ruleUUID, userUUIDs, roleUUIDs, groupUUIDs, membershipUUIDs, entityIDs);
  }
}
