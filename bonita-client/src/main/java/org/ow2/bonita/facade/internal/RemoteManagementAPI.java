/**
 * Copyright (C) 2006  Bull S. A. S.
 * Bull, Rue Jean Jaures, B.P.68, 78340, Les Clayes-sous-Bois
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General   License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General   License for more details.
 * You should have received a copy of the GNU Lesser General   License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA  02110-1301, USA.
 * 
 * Modified by Matthieu Chaffotte, Elias Ricken de Medeiros - BonitaSoft S.A.
 **/
package org.ow2.bonita.facade.internal;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ow2.bonita.facade.exception.DeploymentException;
import org.ow2.bonita.facade.exception.ProcessNotFoundException;
import org.ow2.bonita.facade.exception.RuleNotFoundException;
import org.ow2.bonita.facade.exception.UndeletableInstanceException;
import org.ow2.bonita.facade.exception.UndeletableProcessException;
import org.ow2.bonita.facade.privilege.Rule;
import org.ow2.bonita.facade.privilege.Rule.RuleType;
import org.ow2.bonita.facade.uuid.AbstractUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;

/**
 * For internal use only.
 */
public interface RemoteManagementAPI extends AbstractRemoteManagementAPI {

	void disable(
      final Collection<ProcessDefinitionUUID> processUUIDs,
      final Map<String, String> options) 
	throws DeploymentException, RemoteException;

	void enable(
      final Collection<ProcessDefinitionUUID> processUUIDs,
      final Map<String, String> options) 
	throws DeploymentException, RemoteException;

	void archive(
      final Collection<ProcessDefinitionUUID> processUUIDs,
      final Map<String, String> options) 
	throws DeploymentException, RemoteException;

	void delete(
      final Collection<ProcessDefinitionUUID> processUUIDs,
      final Map<String, String> options)	
	throws ProcessNotFoundException, UndeletableProcessException, UndeletableInstanceException, RemoteException;

  <E extends AbstractUUID> void addExceptionsToRuleByUUID(
      final String ruleUUID, 
  		final Set<E> exceptions,
  		final Map<String, String> options) 
  throws RuleNotFoundException, RemoteException;

  void applyRuleToEntities(
      final String ruleUUID,
      final Collection<String> userUUIDs,
      final Collection<String> roleUUIDs,
      final Collection<String> groupUUIDs,
      final Collection<String> membershipUUIDs,
      final Collection<String> entityIDs,
      final Map<String, String> options)
  throws RuleNotFoundException, RemoteException;

  List<Rule> getAllApplicableRules(
      final String userUUID,
      final Collection<String> roleUUIDs, 
  		final Collection<String> groupUUIDs,
  		final Collection<String> membershipUUIDs,
  		final String entityID,
  		final Map<String, String> options)
  throws RemoteException;

  List<Rule> getApplicableRules(
      final RuleType ruleType,
      final String userUUID, 
  		final Collection<String> roleUUIDs,
  		final Collection<String> groupUUIDs, 
  		final Collection<String> membershipUUIDs,
  		final String entityID, 
  		final Map<String, String> options)
  throws RemoteException;

  List<Rule> getRulesByUUIDs(
      final Collection<String> ruleUUIDs,
      final Map<String, String> options) 
  throws RuleNotFoundException, RemoteException;
  
  <E extends AbstractUUID> void removeExceptionsFromRuleByUUID(
      final String ruleUUID,  
  		final Set<E> exceptions, 
  		final Map<String, String> options)
  throws RuleNotFoundException, RemoteException;

  void removeRuleFromEntities(
      final String ruleUUID,
      final Collection<String> userUUIDs, 
  		final Collection<String> roleUUIDs,
  		final Collection<String> groupUUIDs, 
  		final Collection<String> membershipUUIDs,
  		final Collection<String> entityIDs, 
  		final Map<String, String> options)
  throws RuleNotFoundException, RemoteException;
 
}
