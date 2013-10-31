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
package org.ow2.bonita.facade.internal;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.ow2.bonita.facade.exception.DeploymentException;
import org.ow2.bonita.facade.exception.ProcessNotFoundException;
import org.ow2.bonita.facade.exception.RuleNotFoundException;
import org.ow2.bonita.facade.exception.UndeletableInstanceException;
import org.ow2.bonita.facade.exception.UndeletableProcessException;
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
@Path("/API/managementAPI/")
@Produces({"text/*","application/xml"})
@Consumes({"application/x-www-form-urlencoded","text/*", "application/xml"})
public interface RESTRemoteManagementAPI extends AbstractRemoteManagementAPI {
  
	/**
	 * Disable a collection of processUUIDs
   * @param processUUIDs the collection of process definition UUIDs
	 * @param options the options map (domain, queryList, user)
	 * @throws DeploymentException
	 * @throws RemoteException
	 */
	@POST @Path("disable")
  void disable(
      @FormParam("processUUIDs") final List<ProcessDefinitionUUID> processUUIDs,
      @FormParam("options") final Map<String, String> options)
	throws DeploymentException, RemoteException;

	/**
	 * Enable a collection of processUUIDs
   * @param processUUIDs the collection of process definition UUIDs
	 * @param options the options map (domain, queryList, user)
	 * @throws DeploymentException
	 * @throws RemoteException
	 */
	@POST @Path("enable")
  void enable(
      @FormParam("processUUIDs") final List<ProcessDefinitionUUID> processUUIDs,
      @FormParam("options") final Map<String, String> options)
	throws DeploymentException, RemoteException;

	/**
	 * Archive a collection of processUUIDs
   * @param processUUIDs the collection of process definition UUIDs
	 * @param options the options map (domain, queryList, user)
	 * @throws DeploymentException
	 * @throws RemoteException
	 */
	@POST @Path("archive")
  void archive(
      @FormParam("processUUIDs") final List<ProcessDefinitionUUID> processUUIDs,
      @FormParam("options") final Map<String, String> options)
	throws DeploymentException, RemoteException;

	/**
	 * Delete a collection of processUUIDs
   * @param processUUIDs the collection of process definition UUIDs
	 * @param options the options map (domain, queryList, user)
	 * @throws ProcessNotFoundException
	 * @throws UndeletableProcessException
	 * @throws UndeletableInstanceException
	 * @throws RemoteException
	 */
	@POST @Path("delete")
  void delete(
      @FormParam("processUUIDs") final List<ProcessDefinitionUUID> processUUIDs,
      @FormParam("options") final Map<String, String> options)
	throws ProcessNotFoundException, UndeletableProcessException, UndeletableInstanceException, RemoteException;

	/**
	 * The global policy is applied to all entities.<br>
   * Rules apply conditions to the policy as they are applied to specific entities.<br>
   * This method is the way to impose conditions to the general policy for the given entity.<br>
   * For instance applying rules to a collection of user defines what access the users have or do not have.<br>
   * @param ruleUUID
   * @param userUUIDs
   * @param roleUUIDs
   * @param groupUUIDs
   * @param membershipUUIDs
   * @param entityIDs
	 * @param options the options map (domain, queryList, user)
	 * @throws RuleNotFoundException
	 * @throws RemoteException
	 */
	@POST @Path("applyRuleToEntities/{ruleUUID}")
  void applyRuleToEntities(
      @PathParam("ruleUUID") final String ruleUUID,
      @FormParam("userUUIDs") final List<String> userUUIDs,
  		@FormParam("roleUUIDs") final List<String> roleUUIDs,
  		@FormParam("groupUUIDs") final List<String> groupUUIDs,
  		@FormParam("membershipUUIDs") final List<String> membershipUUIDs,
  		@FormParam("entityIDs") final List<String> entityIDs,
  		@FormParam("options") final Map<String, String> options)
	throws RuleNotFoundException, RemoteException;

  /**
   * @param userUUID
   * @param roleUUIDs
   * @param groupUUIDs
   * @param membershipUUIDs
   * @param entityID
   * @param options the options map (domain, queryList, user)
   * @return
   * @throws RemoteException
   */
  @POST @Path("getAllApplicableRules")
  List<Rule> getAllApplicableRules(
      @QueryParam("userUUID") final String userUUID,
      @FormParam("roleUUIDs") final List<String> roleUUIDs,
  		@FormParam("groupUUIDs") final List<String> groupUUIDs,
  		@FormParam("membershipUUIDs") final List<String> membershipUUIDs,
  		@QueryParam("entityID") final String entityID,
  		@FormParam("options") final Map<String, String> options)
  throws RemoteException;

  /**
   * @param ruleType
   * @param userUUID
   * @param roleUUIDs
   * @param groupUUIDs
   * @param membershipUUIDs
   * @param entityID
   * @param options the options map (domain, queryList, user)
   * @return
   * @throws RemoteException
   */
  @POST @Path("getApplicableRules")
  List<Rule> getApplicableRules(
      @QueryParam("ruleType") final RuleType ruleType,
      @QueryParam("userUUID") final String userUUID,
  		@FormParam("roleUUIDs") final List<String> roleUUIDs,
  		@FormParam("groupUUIDs") final List<String> groupUUIDs,
  		@FormParam("membershipUUIDs") final List<String> membershipUUIDs,
  		@QueryParam("entityID") final String entityID,
  		@FormParam("options") final Map<String, String> options)
  throws RemoteException;

  /**
   * Gets rules from their names.
   * @param ruleNames a set of rule names
   * @return a List of rules
   * @param options the options map (domain, queryList, user)
   * @return
   * @throws RuleNotFoundException
   * @throws RemoteException
   */
  @POST @Path("getRulesByUUIDs")
  List<Rule> getRulesByUUIDs(
      @FormParam("ruleUUIDs") final List<String> ruleUUIDs,
      @FormParam("options") final Map<String, String> options) 
  throws RuleNotFoundException, RemoteException;

  /**
   * The global policy is applied to all entities.<br>
   * Rules apply conditions to the policy as they are applied to specific entities.<br>
   * This method is the way to remove conditions to the general policy for the given entity.<br>
   * For instance removing rules previously applied to a user (entity) defines what access the user has or does not have.<br>
   * @param ruleUUID
   * @param userUUIDs
   * @param roleUUIDs
   * @param groupUUIDs
   * @param membershipUUIDs
   * @param entityIDs
   * @param options the options map (domain, queryList, user)
   * @throws RuleNotFoundException
   * @throws RemoteException
   */
  @POST @Path("removeRuleFromEntities/{ruleUUID}")
  void removeRuleFromEntities(
      @PathParam("ruleUUID") final String ruleUUID,
      @FormParam("userUUIDs") final List<String> userUUIDs,
  		@FormParam("roleUUIDs") final List<String> roleUUIDs,
  		@FormParam("groupUUIDs") final List<String> groupUUIDs,
  		@FormParam("membershipUUIDs") final List<String> membershipUUIDs,
  		@FormParam("entityIDs") final List<String> entityIDs,
  		@FormParam("options") final Map<String, String> options)
  throws RuleNotFoundException, RemoteException;

  /**
   * The global policy is applied to all entities.<br>
   * Rules apply conditions to the policy as they are applied to specific entities.<br>
   * This method add exceptions to a rule identified by its name and so increase the scope of the rule.
   * @param <E>
   * @param ruleUUID
   * @param exceptions
   * @param options the options map (domain, queryList, user)
   * @throws RuleNotFoundException
   * @throws RemoteException
   */
  @POST @Path("addExceptionsToRuleByUUID/{ruleUUID}")
  <E extends AbstractUUID> void addExceptionsToRuleByUUID(
      @PathParam("ruleUUID")final String ruleUUID,
  		@FormParam("exceptions") final RESTSet exceptions,
  		@FormParam("options") final Map<String, String> options)
  throws RuleNotFoundException, RemoteException;

  /**
   * The global policy is applied to all entities.<br>
   * Rules apply conditions to the policy as they are applied to specific entities.<br>
   * This method removes exceptions from rule and so reduces the scope of the rule.
   * @param <E>
   * @param ruleUUID
   * @param exceptions
   * @param options the options map (domain, queryList, user)
   * @throws RuleNotFoundException
   * @throws RemoteException
   */
  @POST @Path("removeExceptionsFromRuleByUUID/{ruleUUID}")
  <E extends AbstractUUID> void removeExceptionsFromRuleByUUID(
      @PathParam("ruleUUID") final String ruleUUID,
      @FormParam("exceptions") final RESTSet exceptions,
  		@FormParam("options") final Map<String, String> options)
  throws RuleNotFoundException, RemoteException;  
  
}
