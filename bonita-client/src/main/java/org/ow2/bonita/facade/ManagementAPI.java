/**
 * Copyright (C) 2007  Bull S. A. S.
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
 * Modified by Matthieu Chaffotte, Elias Ricken de Medeiros - BonitaSoft S.A.
 **/
package org.ow2.bonita.facade;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.ow2.bonita.facade.def.element.BusinessArchive;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.exception.BonitaInternalException;
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
import org.ow2.bonita.facade.uuid.AbstractUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.util.BusinessArchiveFactory;

/**
 * Workflow process deployment operations. Individual or grouped deployment of objects relating to the process definition:
 * businessArchive, JAR file, java class data for connectors, ....
 * @see org.ow2.bonita.connector.core.Connector
 * @author Marc Blachon, Guillaume Porcher, Charles Souillard, Miguel Valdes, Pierre Vigneras
 */
public interface ManagementAPI {

  /**
   * Deploys the given businessArchive into Bonita server.
   * A businessArchive can be build using {@link BusinessArchiveFactory} class.
   * @param businessArchive businessArchive to deploy
   * @throws DeploymentException if an error occurs while deploying the given Bar
   * @throws BonitaInternalException if an exception occurs.
   * @return the deployed process
   */
  ProcessDefinition deploy(final BusinessArchive businessArchive) throws DeploymentException;

  /**
   * Deploys a JAR giving its bytes table.
   * @param jarName the jar name
   * @param jar the bytes table of the JAR.
   * @throws DeploymentException if a class has already been deployed with its name.
   * @throws BonitaInternalException if an other exception occurs.
   */
  void deployJar(String jarName, byte[] jar) throws DeploymentException;

  /**
   * Removes a JAR according to its name.
   * @param jarName the JAR name.
   * @throws DeploymentException if the JAR name is not a JAR file or the JAR cannot be found in the repository
   * or a deployed process is still using a class of the JAR file.
   * @throws BonitaInternalException if an other exception occurs.
   */
  void removeJar(String jarName) throws DeploymentException;

  /**
   * Deletes from journal and history:<br>
   * <ul>
   *   <li>the process</li>
   *   <li>all instances of this process.</li>
   * <ul>
   *
   * @param processUUID the process UUID to delete
   * @throws ProcessNotFoundException if the process cannot be found
   * @throws UndeletableProcessException if the process cannot be deleted
   * @throws UndeletableInstanceException if at least a process instance cannot be deleted
   * @throws BonitaInternalException if an other exception occurs.
   */
  void deleteProcess(ProcessDefinitionUUID processUUID)
  throws ProcessNotFoundException, UndeletableProcessException, UndeletableInstanceException;

  /**
   * Deletes from journal and history :<br>
   * <ul>
   *   <li>all processes</li>
   *   <li>all instances of these processes.</li>
   * <ul>
   *
   * @throws UndeletableInstanceException if at least a process instance cannot be deleted
   * @throws UndeletableProcessException if at least a process cannot be deleted
   * @throws BonitaInternalException if an other exception occurs.
   */
  void deleteAllProcesses() throws UndeletableInstanceException, UndeletableProcessException;

  /**
   * Gets the current logged user.
   * @return the logged user
   */
  String getLoggedUser();

  /**
   * Adds or updates a meta data.
   * @param key the meta data key
   * @param value the meta data value
   */
  void addMetaData(String key, String value);

  /**
   * Obtains a meta data.
   * @param key the key of the meta data
   * @return the value of the meta data
   */
  String getMetaData(String key);

  /**
   * Deletes a meta data.
   * @param key the key of the meta data
   */
  void deleteMetaData(String key);

  /**
   * Disables a process.
   * @param processUUID the process definition UUID
   * @throws DeploymentException if a process is not in enable state
   */
  void disable(final ProcessDefinitionUUID processUUID) throws DeploymentException;

  /**
   * Enable a process.
   * @param processUUID the process definition UUID
   * @throws DeploymentException if a process is not in disable state
   */
  void enable(final ProcessDefinitionUUID processUUID) throws DeploymentException;

  /**
   * Archive a process. An archived process cannot be enable anymore.
   * @param processUUID the process definition UUID
   * @throws DeploymentException if a process is not in disable state
   */
  void archive(final ProcessDefinitionUUID processUUID) throws DeploymentException;

  /**
   * Disable a collection of processUUIDs
   * @param processUUIDs the collection of process definition UUIDs
   * @throws DeploymentException if a process is not in enable state
   */
  void disable(final Collection<ProcessDefinitionUUID> processUUIDs) throws DeploymentException;
  
  /**
   * Enable a collection of processUUIDs
   * @param processUUIDs the collection of process definition UUIDs
   * @throws DeploymentException if a process is not in disable state
   */
  void enable(final Collection<ProcessDefinitionUUID> processUUIDs) throws DeploymentException;
  
  /**
   * Archive a collection of processUUIDs
   * @param processUUIDs the collection of process definition UUIDs
   * @throws DeploymentException if a process is not in disable state
   */
  void archive(final Collection<ProcessDefinitionUUID> processUUIDs) throws DeploymentException;
  
  /**
   * Delete a collection of processUUIDs
   * @param processUUIDs the collection of process definition UUIDs
   * @throws DeploymentException if a process is not in enable state
   */
  void delete(final Collection<ProcessDefinitionUUID> processUUIDs) throws ProcessNotFoundException,
  UndeletableProcessException, UndeletableInstanceException;
  
  /**
   * Check whether a user has administrator privileges
   * @param username the user's user name
   * @return true if the user has administrator privileges, false otherwise
   * @throws UserNotFoundException
   */
  boolean isUserAdmin(final String username) throws UserNotFoundException;

  /**
   * Check some user's credentials
   * @param username the user's user name
   * @param password the user's password
   * @return true if the credentials are valid, false otherwise
   */
  boolean checkUserCredentials(final String username, final String password);
  
  /**
   * Check some user's credentials
   * @param username the user's user name
   * @param passwordHash the user's password hash
   * @return true if the credentials are valid, false otherwise
   */
  boolean checkUserCredentialsWithPasswordHash(final String username, final String passwordHash);

  /**
   * Gets the name of all available JARs.
   * @return a collection of JAR names
   */
	Set<String> getAvailableJars();


  /**
   * The global policy is applied to all entities.<br>
   * Rules apply conditions to the policy as they are applied to specific entities.<br>
   * This method creates a rule identified by its name.<br>
   * It means that the entities bound to this rule will NOT follow the global policy anymore.
   * @param name
   * @param label
   * @param description
   * @param type
   * @return the newly created rule
   * @throws RuleAlreadyExistsException
   */
  Rule createRule(final String name, final String label, final String description, final RuleType type) throws RuleAlreadyExistsException;
  
  
  /**
   * The global policy is applied to all entities.<br>
   * Rules apply conditions to the policy as they are applied to specific entities.<br>
   * This method deletes a rule identified by its name.<br>
   * It means that the entities bound to this rule will then follow the global policy (unless they are bound to another rule).
   * @param ruleUUID
   * @throws RuleNotFoundException
   */
  void deleteRuleByUUID(final String ruleUUID) throws RuleNotFoundException;
  
  /**
   * The global policy is applied to all entities.<br>
   * Rules apply conditions to the policy as they are applied to specific entities.<br>
   * This method add exceptions to a rule identified by its name and so increase the scope of the rule.
   * @param <E>
   * @param ruleUUID
   * @param exceptions
   * @throws RuleNotFoundException
   */
  <E extends AbstractUUID> void addExceptionsToRuleByUUID(final String ruleUUID, final Set<E> exceptions) throws RuleNotFoundException;
  
  /**
   * The global policy is applied to all entities.<br>
   * Rules apply conditions to the policy as they are applied to specific entities.<br>
   * This method removes exceptions from rule and so reduces the scope of the rule.
   * @param <E>
   * @param ruleUUID
   * @param exceptions
   * @throws RuleNotFoundException
   */
  <E extends AbstractUUID> void removeExceptionsFromRuleByUUID(final String ruleUUID, final Set<E> exceptions) throws RuleNotFoundException;
  
  /**
   * The global policy is applied to all entities.<br>
   * Rules apply conditions to the policy as they are applied to specific entities.<br>
   * Update a rule identified by its name with the values stored in the rule object.
   * @param ruleUUID
   * @param name
   * @param label
   * @param description
   * @return a {@link Rule}
   * @throws RuleNotFoundException
   * @throws RuleAlreadyExistsException
   */
  Rule updateRuleByUUID(final String ruleUUID, final String name, final String label, final String description) throws RuleNotFoundException, RuleAlreadyExistsException;
  
  /**
   * The global policy is applied to all entities.<br>
   * Rules apply conditions to the policy as they are applied to specific entities.<br>
   * Get a rule identified by its name.
   * @param ruleUUID
   * @return a {@link Rule}
   * @throws RuleNotFoundException
   */
  Rule getRuleByUUID(final String ruleUUID) throws RuleNotFoundException;
    
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
   * @throws RuleNotFoundException
   */
  void applyRuleToEntities(final String ruleUUID, final Collection<String> userUUIDs, final Collection<String> roleUUIDs, final Collection<String> groupUUIDs, final Collection<String> membershipUUIDs, final Collection<String> entityIDs) throws RuleNotFoundException;
  
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
   * @throws RuleNotFoundException
   */
  void removeRuleFromEntities(final String ruleUUID, final Collection<String> userUUIDs, final Collection<String> roleUUIDs, final Collection<String> groupUUIDs, final Collection<String> membershipUUIDs, final Collection<String> entityIDs) throws RuleNotFoundException;
  
  /**
   * Lists all available rules
   * @return a List of rules
   */
  List<Rule> getAllRules();
  
  /**
   * Lists the rules according to its type from a specific index.
   * @param ruleType
   * @param fromIndex
   * @param pageSige
   * @return an ordered list of rules.
   */
  List<Rule> getRules(final RuleType ruleType, final int fromIndex, final int pageSige);

  /**
   * Get the number of rules having the given type.
   * @param ruleType
   * @return the number of rules.
   */
  long getNumberOfRules(final RuleType ruleType);
  
  /**
   * Gets rules from their names.
   * @param ruleNames a set of rule names
   * @return a List of rules
   * @throws RuleNotFoundException if a rule does not exist.
   */
  List<Rule> getRulesByUUIDs(final Collection<String> ruleUUIDs) throws RuleNotFoundException;

  /**
   * @param ruleType
   * @param userUUID
   * @param roleUUIDs
   * @param groupUUIDs
   * @param membershipUUIDs
   * @param entityID
   * @return
   */
  List<Rule> getApplicableRules(final RuleType ruleType, final String userUUID, final Collection<String> roleUUIDs, final Collection<String> groupUUIDs, final Collection<String> membershipUUIDs, final String entityID);
  
  /**
   * @param userUUID
   * @param roleUUIDs
   * @param groupUUIDs
   * @param membershipUUIDs
   * @param entityID
   * @return
   */
  List<Rule> getAllApplicableRules(final String userUUID, final Collection<String> roleUUIDs, final Collection<String> groupUUIDs, final Collection<String> membershipUUIDs, final String entityID);
  
  /**
   * Get a rule type policy.<br>
   * PrivilegePolicy.ALLOW_BY_DEFAULT: Means that by default all entities are allowed to act on everything. Except the items listed in rules.<br>
   * PrivilegePolicy.DENY_BY_DEFAULT: Means that by default all entities are NOT allowed to act on anything. Except the items explicitly listed in rules.<br>
   * @param ruleType the rule type
   * @return the default policy
   */
  PrivilegePolicy getRuleTypePolicy(final RuleType ruleType);
  
  /**
   * Update a rule type policy.<br>
   * @param newPolicy the {@link PrivilegePolicy} to apply to the rule type
   * PrivilegePolicy.ALLOW_BY_DEFAULT: Means that by default all entities are allowed to act on everything. Except the items listed in rules.<br>
   * PrivilegePolicy.DENY_BY_DEFAULT: Means that by default all entities are NOT allowed to act on anything. Except the items explicitly listed in rules.<br>
   * @param ruleType the rule type
   */  
  void setRuleTypePolicy(final RuleType ruleType, final PrivilegePolicy newPolicy);
  
  
  /**
   * Update the migration date for the given process uuid 
   * @param processUUID the ProcessDefinitionUUID
   * @param migrationDate the new migration date
   * @throws ProcessNotFoundException
   */
  void updateMigrationDate(final ProcessDefinitionUUID processUUID, final Date migrationDate) throws ProcessNotFoundException;
  
  /**
   * Set the content of a resource in a deployed process. If the resource already exists
   * the new content will overwrite the previous one, if the resource does not exists it
   * will be created
   * @param processUUID the ProcessDefinitionUUID
   * @param resourcePath the path to the resource
   * @param content the resource's content
   * @throws ProcessNotFoundException
   */
  void setResource(final ProcessDefinitionUUID processUUID, final String resourcePath, final byte[] content) throws ProcessNotFoundException;

}
