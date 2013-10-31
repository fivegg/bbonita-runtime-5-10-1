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

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.ow2.bonita.facade.def.element.BusinessArchive;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.exception.DeploymentException;
import org.ow2.bonita.facade.exception.ProcessNotFoundException;
import org.ow2.bonita.facade.exception.RuleAlreadyExistsException;
import org.ow2.bonita.facade.exception.RuleNotFoundException;
import org.ow2.bonita.facade.exception.UndeletableInstanceException;
import org.ow2.bonita.facade.exception.UndeletableProcessException;
import org.ow2.bonita.facade.exception.UserNotFoundException;
import org.ow2.bonita.facade.privilege.PrivilegePolicy;
import org.ow2.bonita.facade.privilege.Rule;
import org.ow2.bonita.facade.privilege.Rule.RuleType;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.util.BusinessArchiveFactory;

/**
 * 
 * @author Elias Ricken de Medeiros
 *
 */
@Path("/API/managementAPI/")
@Produces({"text/*","application/xml"})
@Consumes({"application/x-www-form-urlencoded","text/*", "application/xml"})
public interface AbstractRemoteManagementAPI extends Remote {

	/**
	 * Deploys the given businessArchive into Bonita server.
   * A businessArchive can be build using {@link BusinessArchiveFactory} class.
   * Current limitation: if you are using REST, the businessArchive parameter can only be serialized with Bonita client.
   * @param businessArchive businessArchive to deploy
	 * @param options the options map (domain, queryList, user)
	 * @return the deployed process
	 * @throws DeploymentException
	 * @throws RemoteException
	 */
  @Consumes({"application/octet-stream","text/*", "application/xml"})
	@POST @Path("deploy")  
  ProcessDefinition deploy(
  		final BusinessArchive businessARchive, 
  		@HeaderParam("options") final Map<String, String> options) 
	throws DeploymentException, RemoteException;

	/**
	 * Deploys a JAR giving its bytes table.
   * @param jarName the jar name
   * @param jar the bytes table of the JAR.
	 * @param options the options map (domain, queryList, user)
	 * @throws DeploymentException
	 * @throws RemoteException
	 */
  @Consumes({"application/octet-stream","text/*", "application/xml"})
	@POST @Path("deployJar/{jarName}")
  void deployJar(
  		@PathParam("jarName")String jarName, 
  		byte[] jar, 
  		@HeaderParam("options") final Map<String, String> options) 
	throws DeploymentException, RemoteException;

	/**
	 * Removes a JAR according to its name.
   * @param jarName the JAR name.
	 * @param options the options map (domain, queryList, user)
	 * @throws DeploymentException
	 * @throws RemoteException
	 */
	@POST @Path("removeJar/{jarName}")
  void removeJar(
  		@PathParam("jarName") String jarName, 
  		@FormParam("options") final Map<String, String> options) 
	throws DeploymentException, RemoteException;

	/**
	 * Deletes from journal and history:<br>
   * <ul>
   *   <li>the process</li>
   *   <li>all instances of this process.</li>
   * <ul>
   *
   * @param processUUID the process UUID to delete
	 * @param options the options map (domain, queryList, user)
	 * @throws ProcessNotFoundException
	 * @throws UndeletableProcessException
	 * @throws UndeletableInstanceException
	 * @throws RemoteException
	 */
	@POST @Path("deleteProcess/{processUUID}")
  void deleteProcess(
  		@PathParam("processUUID") ProcessDefinitionUUID processUUID, 
  		@FormParam("options") final Map<String, String> options) 
	throws ProcessNotFoundException, UndeletableProcessException, UndeletableInstanceException, RemoteException;

	/**
	 * Deletes from journal and history :<br>
   * <ul>
   *   <li>all processes</li>
   *   <li>all instances of these processes.</li>
   * <ul>
	 * @param options the options map (domain, queryList, user)
	 * @throws UndeletableProcessException
	 * @throws UndeletableInstanceException
	 * @throws RemoteException
	 */
	@POST @Path("deleteAllProcesses")
  void deleteAllProcesses(
  		@FormParam("options") final Map<String, String> options) 
	throws UndeletableProcessException, UndeletableInstanceException, RemoteException;

	/**
	 * Gets the current logged user. 
	 * @param options the options map (domain, queryList, user)
	 * @return the logged user
	 * @throws RemoteException
	 */
	@POST @Path("getLoggedUser")
  String getLoggedUser(
  		@FormParam("options") final Map<String, String> options) 
	throws RemoteException;

	/**
	 * Gets the name of all available JARs.
	 * @param options the options map (domain, queryList, user)
	 * @return a collection of JAR names
	 * @throws RemoteException
	 */
	@POST @Path("getAvailableJars")
  Set<String> getAvailableJars(
  		@FormParam("options") final Map<String, String> options) 
  throws RemoteException;
  
	/**
	 * Adds or updates a meta data.
   * @param key the meta data key
   * @param value the meta data value
	 * @param options the options map (domain, queryList, user)
	 * @throws RemoteException
	 */
	@POST @Path("addMetaData/{key}/{value}")
  void addMetaData(
  		@PathParam("key") final String key, 
  		@PathParam("value") final String value, 
  		@FormParam("options") final Map<String, String> options) 
	throws RemoteException;

	/**
	 * Obtains a meta data.
   * @param key the key of the meta data
	 * @param options the options map (domain, queryList, user)
	 * @return the value of the meta data
	 * @throws RemoteException
	 */
	@POST @Path("getMetaData/{key}")
  String getMetaData(
  		@PathParam("key") final String key, 
  		@FormParam("options") final Map<String, String> options) 
	throws RemoteException;

	/**
	 * Deletes a meta data.
   * @param key the key of the meta data
	 * @param options the options map (domain, queryList, user)
	 * @throws RemoteException
	 */
	@POST @Path("deleteMetaData/{key}")
  void deleteMetaData(
  		@PathParam("key") final String key, 
  		@FormParam("options") final Map<String, String> options) 
	throws RemoteException;

	/**
	 * Disables a process.
   * @param processUUID the process definition UUID
	 * @param options the options map (domain, queryList, user)
	 * @throws DeploymentException
	 * @throws RemoteException
	 */
	@POST @Path("disable/{processUUID}")
  void disable(
  		@PathParam("processUUID") final ProcessDefinitionUUID processUUID, 
  		@FormParam("options") final Map<String, String> options) 
	throws DeploymentException, RemoteException;

	/**
	 * Enable a process.
   * @param processUUID the process definition UUID
	 * @param options the options map (domain, queryList, user)
	 * @throws DeploymentException
	 * @throws RemoteException
	 */
	@POST @Path("enable/{processUUID}")
  void enable(
  		@PathParam("processUUID") final ProcessDefinitionUUID processUUID, 
  		@FormParam("options") final Map<String, String> options) 
	throws DeploymentException, RemoteException;

	/**
	 * Archive a process. An archived process cannot be enable anymore.
   * @param processUUID the process definition UUID
	 * @param options the options map (domain, queryList, user)
	 * @throws DeploymentException
	 * @throws RemoteException
	 */
	@POST @Path("archive/{processUUID}")
  void archive(
  		@PathParam("processUUID") final ProcessDefinitionUUID processUUID, 
  		@FormParam("options") final Map<String, String> options) 
	throws DeploymentException, RemoteException;
	
	/**
	 * Check whether a user has administrator privileges
   * @param username the user's user name
	 * @param options the options map (domain, queryList, user)
	 * @return
	 * @throws UserNotFoundException
	 * @throws RemoteException
	 */
	@POST @Path("isUserAdmin/{username}")
  @Produces("text/*")
  Boolean isUserAdmin(
  		@PathParam("username") final String username, 
  		@FormParam("options") final Map<String, String> options) 
	throws UserNotFoundException, RemoteException;
  
	/**
	 * Check some user's credentials
   * @param username the user's user name
   * @param password the user's password
	 * @param options the options map (domain, queryList, user)
	 * @return
	 * @throws RemoteException
	 */
	@POST @Path("checkUserCredentials/{username}")
  @Produces("text/*")  
  Boolean checkUserCredentials(
  		@PathParam("username") final String username, 
  		@FormParam("password") final String password, 
  		@FormParam("options") final Map<String, String> options) 
	throws RemoteException;

	/**
	  Check some user's credentials
   * @param username the user's user name
   * @param passwordHash the user's password hash
	 * @param options the options map (domain, queryList, user)
	 * @return
	 * @throws RemoteException
	 */
	@POST
  @Path("checkUserCredentialsWithPasswordHash/{username}")
  @Produces("text/*")  
  Boolean checkUserCredentialsWithPasswordHash(
  		@PathParam("username") final String username, 
  		@FormParam("passwordHash") final String passwordHash, 
  		@FormParam("options") final Map<String, String> options) 
	throws RemoteException;
  
  /**
   * The global policy is applied to all entities.<br>
   * Rules apply conditions to the policy as they are applied to specific entities.<br>
   * This method creates a rule identified by its name.<br>
   * It means that the entities bound to this rule will NOT follow the global policy anymore.
   * @param name
   * @param label
   * @param decription
   * @param type
   * @param options the options map (domain, queryList, user)
   * @return
   * @throws RuleAlreadyExistsException
   * @throws RemoteException
   */
  @POST @Path("createRule/{name}/{label}/{type}")
  Rule createRule(
  		@PathParam("name") final String name, 
  		@PathParam("label") final String label, 
  		@FormParam("decription") final String decription, 
  		@PathParam("type") final RuleType type, 
  		@FormParam("options") final Map<String, String> options) 
  throws  RuleAlreadyExistsException, RemoteException;
  
  /**
   * Lists the rules according to its type from a specific index.
   * @param ruleType
   * @param fromIndex
   * @param pageSige
   * @param options the options map (domain, queryList, user)
   * @return
   * @throws RemoteException
   */
  @POST @Path("getRules/{ruleType}")
  List<Rule> getRules(
  		@PathParam("ruleType")  final RuleType ruleType, 
  		@QueryParam("fromIndex")final int fromIndex, 
  		@QueryParam("pageSige")final int pageSige, 
  		@FormParam("options") final Map<String, String> options) 
  throws RemoteException;
  
  /**
   * Get the number of rules having the given type.
   * @param ruleType
   * @param options the options map (domain, queryList, user)
   * @return
   * @throws RemoteException
   */
  @POST @Path("getNumberOfRules/{ruleType}")
  long getNumberOfRules(
  		@PathParam("ruleType")  final RuleType ruleType, 
  		@FormParam("options") final Map<String, String> options) 
  throws RemoteException;
  
  /**
   * Get a rule type policy.<br>
   * PrivilegePolicy.ALLOW_BY_DEFAULT: Means that by default all entities are allowed to act on everything. Except the items listed in rules.<br>
   * PrivilegePolicy.DENY_BY_DEFAULT: Means that by default all entities are NOT allowed to act on anything. Except the items explicitly listed in rules.<br>
   * @param ruleType the rule type
   * @param options the options map (domain, queryList, user)
   * @return
   * @throws RemoteException
   */
  @POST @Path("getRuleTypePolicy/{ruleType}")
  PrivilegePolicy getRuleTypePolicy(
  		@PathParam("ruleType") final RuleType ruleType, 
  		@FormParam("options") final Map<String, String> options) 
  throws RemoteException;
  
  /**
   * Update a rule type policy.<br>
   * @param newPolicy the {@link PrivilegePolicy} to apply to the rule type
   * PrivilegePolicy.ALLOW_BY_DEFAULT: Means that by default all entities are allowed to act on everything. Except the items listed in rules.<br>
   * PrivilegePolicy.DENY_BY_DEFAULT: Means that by default all entities are NOT allowed to act on anything. Except the items explicitly listed in rules.<br>
   * @param ruleType the rule type
   * @param options the options map (domain, queryList, user)
   * @throws RemoteException
   */
  @POST @Path("setRuleTypePolicy/{ruleType}/{newPolicy}")
  void setRuleTypePolicy(
  		@PathParam("ruleType")  final RuleType ruleType, 
  		@PathParam("newPolicy")final PrivilegePolicy newPolicy, 
  		@FormParam("options") final Map<String, String> options) 
  throws RemoteException;
  
  /**
   * The global policy is applied to all entities.<br>
   * Rules apply conditions to the policy as they are applied to specific entities.<br>
   * This method deletes a rule identified by its name.<br>
   * It means that the entities bound to this rule will then follow the global policy (unless they are bound to another rule).
   * @param ruleUUID
   * @param options the options map (domain, queryList, user)
   * @throws RuleNotFoundException
   * @throws RemoteException
   */
  @POST @Path("deleteRuleByUUID/{ruleUUID}")
  void deleteRuleByUUID(
  		@PathParam("ruleUUID")final String ruleUUID, 
  		@FormParam("options") final Map<String, String> options) 
  throws RuleNotFoundException, RemoteException;
  
  /**
   * Lists all available rules
   * @param options the options map (domain, queryList, user)
   * @return
   * @throws RemoteException
   */
  @POST @Path("getAllRules")
  List<Rule> getAllRules(
  		@FormParam("options") final Map<String, String> options) 
  throws RemoteException;
  
  /**
   * The global policy is applied to all entities.<br>
   * Rules apply conditions to the policy as they are applied to specific entities.<br>
   * Get a rule identified by its name.
   * @param ruleUUID
   * @param options the options map (domain, queryList, user)
   * @return
   * @throws RuleNotFoundException
   * @throws RemoteException
   */
  @POST @Path("getRuleByUUID/{ruleUUID}")
  Rule getRuleByUUID(
  		@PathParam("ruleUUID") final String ruleUUID, 
  		@FormParam("options") final Map<String, String> options) 
  throws RuleNotFoundException, RemoteException;
  
  /**
   * The global policy is applied to all entities.<br>
   * Rules apply conditions to the policy as they are applied to specific entities.<br>
   * Update a rule identified by its name with the values stored in the rule object.
   * @param ruleUUID
   * @param name
   * @param label
   * @param description
   * @param options the options map (domain, queryList, user)
   * @return
   * @throws RuleNotFoundException
   * @throws RuleAlreadyExistsException
   * @throws RemoteException
   */
  @POST @Path("updateRuleByUUID/{ruleUUID}/{name}/{label}")
  Rule updateRuleByUUID(
  		@PathParam("ruleUUID") final String ruleUUID, 
  		@PathParam("name") final String name, 
  		@PathParam("label") final String label, 
  		@FormParam("description")final String description, 
  		@FormParam("options") final Map<String, String> options) 
  throws RuleNotFoundException, RuleAlreadyExistsException, RemoteException;
  
  
  /**
   * Update the migration date for the given process uuid 
   * @param processUUID the ProcessDefinitionUUID
   * @param migrationDate the new migration date
   * @throws ProcessNotFoundException
   * @throws RemoteException
   */
  @POST @Path("updateMigrationDate/{processUUID}")
  void updateMigrationDate(
      @PathParam("processUUID") final ProcessDefinitionUUID processUUID,
      @FormParam("migrationDate") final Date migrationDate,
      @FormParam("options") final Map<String, String> options) 
  throws ProcessNotFoundException, RemoteException;
  
  /**
   * Set the content of a resource in a deployed process. If the resource already exists
   * the new content will overwrite the previous one, if the resource does not exists it
   * will be created
   * @param processUUID the ProcessDefinitionUUID
   * @param resourcePath the path to the resource
   * @param content the resource's content
   * @throws ProcessNotFoundException
   */
  @Consumes("application/octet-stream")
  @POST @Path("setResource/{processUUID}")
  void setResource(
      @PathParam("processUUID") final ProcessDefinitionUUID processUUID, 
      @QueryParam("resourcePath") final String resourcePath, 
      final byte[] content, /*the is no annotation because it has more performance with byte arrays*/
      @HeaderParam("options") final Map<String, String> options) /*with there are parameters 
      with no annotations that's not possible to use form parameters*/
  throws ProcessNotFoundException, RemoteException;
}
