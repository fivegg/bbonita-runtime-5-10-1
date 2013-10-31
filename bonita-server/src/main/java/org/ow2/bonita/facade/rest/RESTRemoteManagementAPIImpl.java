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
package org.ow2.bonita.facade.rest;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ow2.bonita.facade.exception.DeploymentException;
import org.ow2.bonita.facade.exception.ProcessNotFoundException;
import org.ow2.bonita.facade.exception.RuleNotFoundException;
import org.ow2.bonita.facade.exception.UndeletableInstanceException;
import org.ow2.bonita.facade.exception.UndeletableProcessException;
import org.ow2.bonita.facade.impl.AbstractRemoteManagementAPIImpl;
import org.ow2.bonita.facade.internal.RESTRemoteManagementAPI;
import org.ow2.bonita.facade.privilege.Rule;
import org.ow2.bonita.facade.privilege.Rule.RuleType;
import org.ow2.bonita.facade.rest.wrapper.RESTSet;
import org.ow2.bonita.facade.uuid.AbstractUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;

/**
 * 
 * @author Elias Ricken de Medeiros
 *
 */
public class RESTRemoteManagementAPIImpl extends
		AbstractRemoteManagementAPIImpl implements RESTRemoteManagementAPI {

	public void applyRuleToEntities(String ruleUUID, List<String> userUUIDs,
			List<String> roleUUIDs, List<String> groupUUIDs,
			List<String> membershipUUIDs, List<String> entityIDs,
			Map<String, String> options) throws RuleNotFoundException,
			RemoteException {
		getAPI(options).applyRuleToEntities(ruleUUID, userUUIDs, roleUUIDs, groupUUIDs, membershipUUIDs, entityIDs);
	}

	public void archive(List<ProcessDefinitionUUID> processUUIDs,
			Map<String, String> options) throws DeploymentException, RemoteException {
		getAPI(options).archive(processUUIDs);
	}

	public void delete(List<ProcessDefinitionUUID> processUUIDs,
			Map<String, String> options) throws ProcessNotFoundException,
			UndeletableProcessException, UndeletableInstanceException,
			RemoteException {
		getAPI(options).delete(processUUIDs);
	}

	public void disable(List<ProcessDefinitionUUID> processUUIDs,
			Map<String, String> options) throws DeploymentException, RemoteException {
		getAPI(options).disable(processUUIDs);
	}

	public void enable(List<ProcessDefinitionUUID> processUUIDs,
			Map<String, String> options) throws DeploymentException, RemoteException {
		getAPI(options).enable(processUUIDs);
	}

	public List<Rule> getAllApplicableRules(String userUUID,
			List<String> roleUUIDs, List<String> groupUUIDs,
			List<String> membershipUUIDs, String entityID, Map<String, String> options)
			throws RemoteException {
		return getAPI(options).getAllApplicableRules(userUUID, roleUUIDs, groupUUIDs, membershipUUIDs, entityID);
	}

	public List<Rule> getApplicableRules(RuleType ruleType, String userUUID,
			List<String> roleUUIDs, List<String> groupUUIDs,
			List<String> membershipUUIDs, String entityID, Map<String, String> options)
			throws RemoteException {
		return getAPI(options).getApplicableRules(ruleType, userUUID, roleUUIDs, groupUUIDs, membershipUUIDs, entityID);
	}

	public List<Rule> getRulesByUUIDs(List<String> ruleUUIDs,
			Map<String, String> options) throws RuleNotFoundException,
			RemoteException {
		return getAPI(options).getRulesByUUIDs(ruleUUIDs);
	}

	public void removeRuleFromEntities(String ruleUUID, List<String> userUUIDs,
			List<String> roleUUIDs, List<String> groupUUIDs,
			List<String> membershipUUIDs, List<String> entityIDs,
			Map<String, String> options) throws RuleNotFoundException,
			RemoteException {
		getAPI(options).removeRuleFromEntities(ruleUUID, userUUIDs, roleUUIDs, groupUUIDs, membershipUUIDs, entityIDs);
	}

	@SuppressWarnings("unchecked")
	public <E extends AbstractUUID> void addExceptionsToRuleByUUID(
			String ruleUUID, RESTSet exceptions, Map<String, String> options)
			throws RuleNotFoundException, RemoteException {
		getAPI(options).addExceptionsToRuleByUUID(ruleUUID, (Set<E>)exceptions.getSet());
	}

	@SuppressWarnings("unchecked")
	public <E extends AbstractUUID> void removeExceptionsFromRuleByUUID(
			String ruleUUID, RESTSet exceptions, Map<String, String> options)
			throws RuleNotFoundException, RemoteException {
		getAPI(options).removeExceptionsFromRuleByUUID(ruleUUID, (Set<E>)exceptions.getSet());		
	}

}
