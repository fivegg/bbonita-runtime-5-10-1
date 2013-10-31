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
 * Modified by Matthieu Chaffotte, Anthony Birembaut, Nicolas Chabanoles, Elias Ricken de Medeiros - BonitaSoft S.A.
 **/
package org.ow2.bonita.facade.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.hibernate.HibernateException;
import org.ow2.bonita.deployment.Deployer;
import org.ow2.bonita.deployment.DeploymentRuntimeException;
import org.ow2.bonita.facade.IdentityAPI;
import org.ow2.bonita.facade.ManagementAPI;
import org.ow2.bonita.facade.def.InternalProcessDefinition;
import org.ow2.bonita.facade.def.element.BusinessArchive;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition.ProcessState;
import org.ow2.bonita.facade.exception.DeploymentException;
import org.ow2.bonita.facade.exception.InstanceNotFoundException;
import org.ow2.bonita.facade.exception.PrivilegeNotFoundException;
import org.ow2.bonita.facade.exception.ProcessNotFoundException;
import org.ow2.bonita.facade.exception.RuleAlreadyExistsException;
import org.ow2.bonita.facade.exception.RuleNotFoundException;
import org.ow2.bonita.facade.exception.UndeletableInstanceException;
import org.ow2.bonita.facade.exception.UndeletableProcessException;
import org.ow2.bonita.facade.exception.UserNotFoundException;
import org.ow2.bonita.facade.identity.Group;
import org.ow2.bonita.facade.identity.impl.GroupImpl;
import org.ow2.bonita.facade.identity.impl.MembershipImpl;
import org.ow2.bonita.facade.identity.impl.RoleImpl;
import org.ow2.bonita.facade.identity.impl.UserImpl;
import org.ow2.bonita.facade.privilege.PrivilegePolicy;
import org.ow2.bonita.facade.privilege.Rule;
import org.ow2.bonita.facade.privilege.Rule.RuleType;
import org.ow2.bonita.facade.privilege.RuleTypePolicy;
import org.ow2.bonita.facade.privilege.impl.ActivityRuleImpl;
import org.ow2.bonita.facade.privilege.impl.CategoryRuleImpl;
import org.ow2.bonita.facade.privilege.impl.CustomRuleImpl;
import org.ow2.bonita.facade.privilege.impl.ProcessRuleImpl;
import org.ow2.bonita.facade.privilege.impl.RuleImpl;
import org.ow2.bonita.facade.privilege.impl.RuleTypePolicyImpl;
import org.ow2.bonita.facade.privilege.impl.SimpleRuleImpl;
import org.ow2.bonita.facade.runtime.InstanceState;
import org.ow2.bonita.facade.runtime.impl.InternalProcessInstance;
import org.ow2.bonita.facade.uuid.AbstractUUID;
import org.ow2.bonita.facade.uuid.ActivityDefinitionUUID;
import org.ow2.bonita.facade.uuid.CategoryUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.facade.uuid.RuleExceptionUUID;
import org.ow2.bonita.runtime.event.Master;
import org.ow2.bonita.services.Archiver;
import org.ow2.bonita.services.AuthenticationService;
import org.ow2.bonita.services.IdentityService;
import org.ow2.bonita.services.LargeDataRepository;
import org.ow2.bonita.services.PrivilegeService;
import org.ow2.bonita.services.Querier;
import org.ow2.bonita.services.Recorder;
import org.ow2.bonita.util.BonitaException;
import org.ow2.bonita.util.BonitaRuntimeException;
import org.ow2.bonita.util.EnvTool;
import org.ow2.bonita.util.ExceptionManager;
import org.ow2.bonita.util.Misc;

/**
 * @author Marc Blachon, Guillaume Porcher, Charles Souillard, Miguel Valdes,
 *         Pierre Vigneras, Rodrigue Le Gall
 */
public class ManagementAPIImpl implements ManagementAPI {

  private static final String DEFAULT_USERS_CREATED = "DEFAULT_USERS_CREATED";
  private static final String MASTER_CREATED = "MASTER_CREATED";

  private final String queryList;

  protected ManagementAPIImpl(final String queryList) {
    this.queryList = queryList;
  }

  private String getQueryList() {
    return queryList;
  }

  @Override
  public ProcessDefinition deploy(final BusinessArchive businessArchive) throws DeploymentException {
    FacadeUtil.checkArgsNotNull(businessArchive);
    try {
      return Deployer.deploy(businessArchive);
    } catch (final Exception e) {
      throw new DeploymentException(e.getMessage(), e);
    }
  }

  @Override
  public void deployJar(final String jarName, final byte[] jar) throws DeploymentException {
    FacadeUtil.checkArgsNotNull(jarName, jar);
    if (!jarName.endsWith(".jar")) {
      throw new DeploymentException("Invalid jar name: " + jarName + ". A jar file name must ends with .jar extension.");
    }
    final LargeDataRepository ldr = EnvTool.getLargeDataRepository();
    if (ldr.getData(byte[].class, Misc.getGlobalClassDataCategories(), jarName) != null) {
      throw new DeploymentException("A jar with name: " + jarName + " already exists in repository.");
    }
    ldr.storeData(Misc.getGlobalClassDataCategories(), jarName, jar, true);
    EnvTool.getClassDataLoader().resetCommonClassloader();
  }

  @Override
  public void removeJar(final String jarName) throws DeploymentException {
    FacadeUtil.checkArgsNotNull(jarName);
    if (!jarName.endsWith(".jar")) {
      throw new DeploymentException("Invalid jar name: " + jarName + ". A jar file name must ends with .jar extension.");
    }
    final LargeDataRepository ldr = EnvTool.getLargeDataRepository();
    final boolean found = ldr.deleteData(Misc.getGlobalClassDataCategories(), jarName);
    if (!found) {
      final String message = ExceptionManager.getInstance().getFullMessage("bai_MAPII_6");
      throw new DeploymentException(message, jarName);
    }
    EnvTool.getClassDataLoader().resetCommonClassloader();
  }

  @Override
  public Set<String> getAvailableJars() {
    final LargeDataRepository ldr = EnvTool.getLargeDataRepository();
    return ldr.getKeys(Misc.getGlobalClassDataCategories());
  }

  @Override
  public void delete(final Collection<ProcessDefinitionUUID> processUUIDs) throws ProcessNotFoundException,
      UndeletableProcessException, UndeletableInstanceException {
    FacadeUtil.checkArgsNotNull(processUUIDs);
    for (final ProcessDefinitionUUID processUUID : processUUIDs) {
      deleteProcess(processUUID);
    }
  }

  @Override
  public void deleteProcess(final ProcessDefinitionUUID processUUID) throws ProcessNotFoundException,
      UndeletableProcessException, UndeletableInstanceException {
    FacadeUtil.checkArgsNotNull(processUUID);
    deleteProcess(true, processUUID);
  }

  private void deleteProcess(final boolean deleteAll, final ProcessDefinitionUUID processUUID)
      throws ProcessNotFoundException, UndeletableProcessException, UndeletableInstanceException {
    FacadeUtil.checkArgsNotNull(processUUID);
    final boolean access = EnvTool.isRestrictedApplicationAcces();
    if (access) {
      try {
        final Set<ProcessDefinitionUUID> itemsToRemove = new HashSet<ProcessDefinitionUUID>();
        itemsToRemove.add(processUUID);
        removeProcessDefinitionUUIDFromAllRules(itemsToRemove, RuleType.PROCESS_READ, RuleType.PROCESS_START);
      } catch (final Exception e) {
        throw new ProcessNotFoundException("Unknown process", processUUID);
      }
    }

    final Querier journal = EnvTool.getJournalQueriers();
    final Querier history = EnvTool.getHistoryQueriers();

    InternalProcessDefinition processDef = journal.getProcess(processUUID);
    final boolean inJournal = processDef != null;
    if (processDef == null) {
      processDef = history.getProcess(processUUID);
    } else {
      processDef.setState(ProcessState.DISABLED);
    }

    if (processDef == null) {
      throw new ProcessNotFoundException("bai_MAPII_9", processUUID);
    }

    Deployer.removeStartEvents(processDef);
    if (deleteAll) {
      final RuntimeAPIImpl runtimeAPI = new RuntimeAPIImpl(getQueryList());
      try {
        runtimeAPI.deleteAllProcessInstances(processUUID);
      } catch (final ProcessNotFoundException e) {
        throw new BonitaRuntimeException(e);
      }
    }

    if (inJournal) {
      final Set<InternalProcessInstance> instances = EnvTool.getJournalQueriers().getProcessInstances(processUUID,
          InstanceState.STARTED);
      if (!instances.isEmpty()) {
        final ProcessInstanceUUID instanceUUID = instances.iterator().next().getUUID();
        throw new UndeletableProcessException("bai_MAPII_10", processUUID, instanceUUID);
      }
      removeProcessDependencies(processDef);
      final Recorder recorder = EnvTool.getRecorder();
      recorder.remove(processDef);
    }

    if (!inJournal) {
      final Archiver archiver = EnvTool.getArchiver();
      archiver.remove(processDef);
    }
    EnvTool.getClassDataLoader().removeProcessClassLoader(processDef.getUUID());
    EnvTool.getUUIDService().archiveOrDeleteProcess(processUUID);
    final LargeDataRepository ldr = EnvTool.getLargeDataRepository();
    ldr.deleteData(Misc.getBusinessArchiveCategories(processUUID));
    ldr.deleteData(Misc.getAttachmentCategories(processUUID));
  }

  private void removeProcessDefinitionUUIDFromAllRules(final Set<ProcessDefinitionUUID> itemsToRemove,
      final RuleType... ruleTypes) throws RuleNotFoundException, PrivilegeNotFoundException {
    FacadeUtil.checkArgsNotNull(itemsToRemove, ruleTypes);
    final PrivilegeService privilegeService = EnvTool.getPrivilegeService();
    final Set<Rule> rules = privilegeService.getRulesByType(ruleTypes);
    for (final Rule rule : rules) {
      removeExceptionsFromRuleByUUID(rule.getUUID(), itemsToRemove);
    }
  }

  private void removeProcessDependencies(final ProcessDefinition processDef) {
    final Set<InternalProcessInstance> instances = EnvTool.getJournalQueriers().getProcessInstances(
        processDef.getUUID(), InstanceState.STARTED);
    // Deployer.removeStartEvents(processDef);
    if (instances != null && !instances.isEmpty()) {
      final String message = ExceptionManager.getInstance().getFullMessage("bd_D_9");
      throw new DeploymentRuntimeException(message);
    }
    EnvTool.getClassDataLoader().removeProcessClassLoader(processDef.getUUID());
  }

  @Override
  public void deleteAllProcesses() throws UndeletableInstanceException, UndeletableProcessException {
    final Querier querier = EnvTool.getAllQueriers();
    Set<InternalProcessDefinition> processes = new HashSet<InternalProcessDefinition>();
    Collection<InternalProcessInstance> parentInstances = new HashSet<InternalProcessInstance>();
    final boolean access = EnvTool.isRestrictedApplicationAcces();
    if (access) {
      final String applicationName = EnvTool.getApplicationAccessName();
      if (applicationName != null) {
        final Set<ProcessDefinitionUUID> readyToRemove = FacadeUtil.getAllowedProcessUUIDsFor(applicationName,
            RuleType.PROCESS_READ);
        if (!readyToRemove.isEmpty()) {
          processes = querier.getProcesses(readyToRemove);
          parentInstances = querier.getProcessInstances(readyToRemove);
        }
      }

    } else {
      parentInstances = querier.getParentInstances();
      processes = querier.getProcesses();
    }
    final RuntimeAPIImpl runtimeAPI = new RuntimeAPIImpl(getQueryList());
    for (final InternalProcessInstance internalProcessInstance : parentInstances) {
      try {
        runtimeAPI.deleteProcessInstance(internalProcessInstance.getUUID());
      } catch (final InstanceNotFoundException e) {
        throw new BonitaRuntimeException("Unable to delete process instance: " + internalProcessInstance.getUUID());
      }
    }

    for (final InternalProcessDefinition process : processes) {
      try {
        deleteProcess(false, process.getUUID());
      } catch (final ProcessNotFoundException e) {
        throw new BonitaRuntimeException("Unable to delete process: " + process.getUUID());
      }
    }
  }

  @Override
  public String getLoggedUser() {
    return EnvTool.getUserId();
  }

  @Override
  public void addMetaData(final String key, final String value) {
    EnvTool.getJournal().storeMetaData(key, value);
  }

  @Override
  public void deleteMetaData(final String key) {
    EnvTool.getJournal().deleteMetaData(key);
  }

  @Override
  public String getMetaData(final String key) {
    return EnvTool.getJournal().getMetaData(key);
  }

  @Override
  public void archive(final ProcessDefinitionUUID processUUID) throws DeploymentException {
    FacadeUtil.checkArgsNotNull(processUUID);
    try {
      Deployer.archiveProcess(processUUID, EnvTool.getUserId());
    } catch (final DeploymentRuntimeException e) {
      throw new DeploymentException(e.getMessage(), e);
    }
  }

  @Override
  public void archive(final Collection<ProcessDefinitionUUID> processUUIDs) throws DeploymentException {
    FacadeUtil.checkArgsNotNull(processUUIDs);
    for (final ProcessDefinitionUUID processUUID : processUUIDs) {
      try {
        Deployer.archiveProcess(processUUID, EnvTool.getUserId());
      } catch (final DeploymentRuntimeException e) {
        throw new DeploymentException(e.getMessage(), e);
      }
    }
  }

  @Override
  public void disable(final ProcessDefinitionUUID processUUID) throws DeploymentException {
    FacadeUtil.checkArgsNotNull(processUUID);
    try {
      Deployer.disableProcess(processUUID);
    } catch (final DeploymentRuntimeException e) {
      throw new DeploymentException(e.getMessage(), e);
    }
  }

  @Override
  public void disable(final Collection<ProcessDefinitionUUID> processUUIDs) throws DeploymentException {
    FacadeUtil.checkArgsNotNull(processUUIDs);
    for (final ProcessDefinitionUUID processUUID : processUUIDs) {
      try {
        Deployer.disableProcess(processUUID);
      } catch (final DeploymentRuntimeException e) {
        throw new DeploymentException(e.getMessage(), e, processUUID);
      }
    }
  }

  @Override
  public void enable(final ProcessDefinitionUUID processUUID) throws DeploymentException {
    FacadeUtil.checkArgsNotNull(processUUID);
    try {
      Deployer.enableProcess(processUUID);
    } catch (final DeploymentRuntimeException e) {
      throw new DeploymentException(e.getMessage(), e);
    }
  }

  @Override
  public void enable(final Collection<ProcessDefinitionUUID> processUUIDs) throws DeploymentException {
    FacadeUtil.checkArgsNotNull(processUUIDs);
    for (final ProcessDefinitionUUID processUUID : processUUIDs) {
      enable(processUUID);
    }
  }

  @Override
  public boolean isUserAdmin(final String username) throws UserNotFoundException {
    final AuthenticationService adminService = EnvTool.getAuthenticationService();
    return adminService.isUserAdmin(username);
  }

  @Override
  public boolean checkUserCredentials(final String username, final String password) {
    initializeDefaultDatabase();
    return checkUserCredentials(username, password, false);
  }

  private boolean checkUserCredentials(final String username, final String password, final boolean isPasswordHash) {
    boolean allowed = false;
    final AuthenticationService authenticationService = EnvTool.getAuthenticationService();
    if (!isPasswordHash) {
      allowed = authenticationService.checkUserCredentials(username, password);
    } else {
      allowed = authenticationService.checkUserCredentialsWithPasswordHash(username, password);
    }
    return allowed;
  }
  
  private void createDefaultUsers() {
    final String defaultUsersCreated = EnvTool.getJournal().getMetaData(DEFAULT_USERS_CREATED);
    if (defaultUsersCreated == null) {
      final IdentityService identityService = EnvTool.getIdentityService();
      final RoleImpl memberRole = createDefaultRole(identityService, IdentityAPI.USER_ROLE_NAME,
          IdentityAPI.USER_ROLE_LABEL, IdentityAPI.USER_ROLE_DESCRIPTION);
      final RoleImpl adminRole = createDefaultRole(identityService, IdentityAPI.ADMIN_ROLE_NAME,
          IdentityAPI.ADMIN_ROLE_LABEL, IdentityAPI.ADMIN_ROLE_DESCRIPTION);

      final GroupImpl defaultGroup = createDefaultGroup(identityService, IdentityAPI.DEFAULT_GROUP_NAME,
          IdentityAPI.DEFAULT_GROUP_LABEL, IdentityAPI.DEFAULT_GROUP_DESCRIPTION, null);

      final MembershipImpl memberMembership = createDefaultMembership(identityService, defaultGroup, memberRole);
      final MembershipImpl adminMembership = createDefaultMembership(identityService, defaultGroup, adminRole);

      final UserImpl adminUser = addDefaultUser(identityService, "admin", null, null, "bpm", null, null);
      identityService.addMembershipToUser(adminUser, adminMembership);
      final UserImpl user1 = addDefaultUser(identityService, "john", "John", "Doe", "bpm", null, null);
      identityService.addMembershipToUser(user1, memberMembership);
      final UserImpl user2 = addDefaultUser(identityService, "jack", "Jack", "Doe", "bpm", user1.getUUID(),
          user1.getUUID());
      identityService.addMembershipToUser(user2, memberMembership);
      final UserImpl user3 = addDefaultUser(identityService, "james", "James", "Doe", "bpm", user1.getUUID(),
          user2.getUUID());
      identityService.addMembershipToUser(user3, memberMembership);

      EnvTool.getJournal().storeMetaData(DEFAULT_USERS_CREATED, "true");
    }
      
    }

  
  private void initializeMaster() {
	  final String masterCreated = EnvTool.getJournal().getMetaData(MASTER_CREATED);
	  if (masterCreated == null) {
		  final Recorder recorder = EnvTool.getJournal();
		  final Master master = new Master(0L, "unknown");
		  recorder.recordNewMaster(master);
		  EnvTool.getJournal().storeMetaData(MASTER_CREATED, "true");
	  }
  }
  
  private void initializeDefaultDatabase() {
	createDefaultUsers();
	initializeMaster();
  }

  @Override
  public boolean checkUserCredentialsWithPasswordHash(final String username, final String passwordHash) {
    initializeDefaultDatabase();
    return checkUserCredentials(username, passwordHash, true);
  }

  private MembershipImpl createDefaultMembership(final IdentityService identityService, final GroupImpl group,
      final RoleImpl role) {
    MembershipImpl membership = identityService.findMembershipByRoleAndGroup(role.getUUID(), group.getUUID());
    if (membership == null) {
      membership = new MembershipImpl();
      membership.setGroup(group);
      membership.setRole(role);
      identityService.addMembership(membership);
    }
    return membership;
  }

  private RoleImpl createDefaultRole(final IdentityService identityService, final String name, final String label,
      final String description) {
    RoleImpl role = identityService.findRoleByName(name);
    if (role == null) {
      role = new RoleImpl(name);
      role.setLabel(label);
      role.setDescription(description);
      identityService.addRole(role);
    }
    return role;
  }

  private GroupImpl createDefaultGroup(final IdentityService identityService, final String name, final String label,
      final String description, final Group parentGroup) {
    final Set<GroupImpl> groups = identityService.findGroupsByName(name);
    GroupImpl group = null;
    if (groups == null || groups.isEmpty()) {
      group = new GroupImpl(name);
      group.setLabel(label);
      group.setDescription(description);
      group.setParentGroup(parentGroup);
      identityService.addGroup(group);
    } else {
      group = groups.iterator().next();
    }
    return group;
  }

  private UserImpl addDefaultUser(final IdentityService identityService, final String username, final String firstName,
      final String lastName, final String password, final String manager, final String delegee) {
    UserImpl user = identityService.findUserByUsername(username);
    if (user == null) {
      user = new UserImpl(username, password);
      user.setFirstName(firstName);
      user.setLastName(lastName);
      user.setManagerUUID(manager);
      user.setDelegeeUUID(delegee);
      identityService.addUser(user);
    }
    return user;
  }

  public void grantAccessAuthorisation(final String applicationName, final ProcessDefinitionUUID definitionUUID) {
    FacadeUtil.checkArgsNotNull(applicationName, definitionUUID);

    final PrivilegeService privilegeService = EnvTool.getPrivilegeService();
    RuleImpl rule = (RuleImpl) privilegeService.findRuleByName(applicationName);

    try {
      final HashSet<ProcessDefinitionUUID> processes = new HashSet<ProcessDefinitionUUID>();
      processes.add(definitionUUID);
      if (rule != null) {
        addExceptionsToRuleByUUID(rule.getUUID(), processes);
      } else {
        rule = (RuleImpl) createRule(applicationName, applicationName, applicationName, RuleType.PROCESS_READ);
        addExceptionsToRuleByUUID(rule.getUUID(), processes);
      }
    } catch (final BonitaException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  public void grantAccessAuthorisation(final String applicationName, final Set<ProcessDefinitionUUID> definitionUUIDs) {
    FacadeUtil.checkArgsNotNull(applicationName, definitionUUIDs);
    for (final ProcessDefinitionUUID definitionUUID : definitionUUIDs) {
      grantAccessAuthorisation(applicationName, definitionUUID);
    }
  }

  @Override
  public Rule createRule(final String name, final String label, final String description, final RuleType type)
      throws RuleAlreadyExistsException {
    FacadeUtil.checkArgsNotNull(name, type);

    final PrivilegeService privilegeService = EnvTool.getPrivilegeService();
    Rule rule = privilegeService.findRuleByName(name);
    if (rule != null) {
      throw new RuleAlreadyExistsException("bai_MAPII_13", name);
    }
    switch (type) {
      case PROCESS_START:
      case PROCESS_READ:
      case PROCESS_ADD_COMMENT:
      case PROCESS_INSTANTIATION_DETAILS_VIEW:
      case PROCESS_MANAGE:
      case PROCESS_PDF_EXPORT:
        rule = new ProcessRuleImpl(name, label, description, type, null);
        privilegeService.addRule(rule);
        break;
      case ACTIVITY_READ:
      case ACTIVITY_DETAILS_READ:
      case ASSIGN_TO_ME_STEP:
      case ASSIGN_TO_STEP:
      case UNASSIGN_STEP:
      case CHANGE_PRIORITY_STEP:
      case RESUME_STEP:
      case SUSPEND_STEP:
      case SKIP_STEP:
        rule = new ActivityRuleImpl(name, label, description, type, null);
        privilegeService.addRule(rule);
        break;
      case CATEGORY_READ:
        rule = new CategoryRuleImpl(name, label, description, type, null);
        privilegeService.addRule(rule);
        break;
      case REPORT_MANAGE:
      case REPORT_VIEW:
        rule = new CustomRuleImpl(name, label, description, type, null);
        privilegeService.addRule(rule);
        break;
      case LOGOUT:
      case PASSWORD_UPDATE:
      case DELEGEE_UPDATE:
      case PROCESS_INSTALL:
      case REPORT_INSTALL:
        rule = new SimpleRuleImpl(name, label, description, type);
        privilegeService.addRule(rule);
        break;
      default:
        throw new IllegalArgumentException("ManagementAPI.createRule(): RuleType not yet supported: " + type.name());
    }
    return rule;
  }

  private List<Rule> buildRulesResultList(final List<Rule> rules) {
    final List<Rule> rulesResult = new ArrayList<Rule>();
    for (final Rule rule : rules) {
      rulesResult.add(RuleImpl.createRule(rule));
    }
    return rulesResult;
  }

  @Override
  public List<Rule> getRules(final RuleType ruleType, final int fromIndex, final int pageSige) {
    final PrivilegeService privilegeService = EnvTool.getPrivilegeService();
    final List<Rule> rules = privilegeService.getRules(ruleType, fromIndex, pageSige);
    if (rules == null || rules.isEmpty()) {
      return Collections.emptyList();
    }
    return buildRulesResultList(rules);
  }

  @Override
  public long getNumberOfRules(final RuleType ruleType) {
    final PrivilegeService privilegeService = EnvTool.getPrivilegeService();
    return privilegeService.getNumberOfRules(ruleType);
  }

  @Override
  public PrivilegePolicy getRuleTypePolicy(final RuleType ruleType) {
    final PrivilegeService privilegeService = EnvTool.getPrivilegeService();
    final RuleTypePolicy ruleTypePolicy = privilegeService.getRuleTypePolicy(ruleType);
    return ruleTypePolicy.getPolicy();
  }

  @Override
  public void setRuleTypePolicy(final RuleType ruleType, final PrivilegePolicy newPolicy) {
    final PrivilegeService privilegeService = EnvTool.getPrivilegeService();
    final RuleTypePolicyImpl ruleTypePolicy = (RuleTypePolicyImpl) privilegeService.getRuleTypePolicy(ruleType);
    if (newPolicy != ruleTypePolicy.getPolicy()) {
      ruleTypePolicy.setPolicy(newPolicy);
      privilegeService.updateRuleTypePolicy(ruleTypePolicy);
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public <E extends AbstractUUID> void addExceptionsToRuleByUUID(final String ruleUUID, final Set<E> exceptions)
      throws RuleNotFoundException {
    FacadeUtil.checkArgsNotNull(ruleUUID, exceptions);
    if (exceptions.size() > 0) {
      final PrivilegeService privilegeService = EnvTool.getPrivilegeService();
      final RuleImpl rule = (RuleImpl) privilegeService.getRule(ruleUUID);
      if (rule == null) {
        throw new RuleNotFoundException("bai_MAPII_12", ruleUUID);
      }
      if (rule instanceof ProcessRuleImpl) {
        ((ProcessRuleImpl) rule).addProcesses((Set<ProcessDefinitionUUID>) exceptions);
      } else if (rule instanceof CategoryRuleImpl) {
        ((CategoryRuleImpl) rule).addCategories((Set<CategoryUUID>) exceptions);
      } else if (rule instanceof CustomRuleImpl) {
        ((CustomRuleImpl) rule).addCustomExceptions((Set<RuleExceptionUUID>) exceptions);
      } else if (rule instanceof ActivityRuleImpl) {
        ((ActivityRuleImpl) rule).addActivities((Set<ActivityDefinitionUUID>) exceptions);
      } else {
        throw new IllegalArgumentException("Un-managed rule type: " + rule.getType());
      }
      privilegeService.updateRule(rule);
    }
  }

  @Override
  public void applyRuleToEntities(final String ruleUUID, final Collection<String> userUUIDs,
      final Collection<String> roleUUIDs, final Collection<String> groupUUIDs,
      final Collection<String> membershipUUIDs, final Collection<String> entityIDs) throws RuleNotFoundException {
    FacadeUtil.checkArgsNotNull(ruleUUID);
    final PrivilegeService privilegeService = EnvTool.getPrivilegeService();
    final Rule rule = privilegeService.getRule(ruleUUID);
    if (rule == null) {
      throw new RuleNotFoundException("bai_MAPII_12", ruleUUID);
    }
    if (userUUIDs != null) {
      ((RuleImpl) rule).addUsers(userUUIDs);
    }
    if (groupUUIDs != null) {
      ((RuleImpl) rule).addGroups(groupUUIDs);
    }
    if (roleUUIDs != null) {
      ((RuleImpl) rule).addRoles(roleUUIDs);
    }
    if (membershipUUIDs != null) {
      ((RuleImpl) rule).addMemberships(membershipUUIDs);
    }
    if (entityIDs != null) {
      ((RuleImpl) rule).addEntities(entityIDs);
    }
    privilegeService.updateRule(rule);
  }

  @Override
  public void deleteRuleByUUID(final String ruleUUID) throws RuleNotFoundException {
    FacadeUtil.checkArgsNotNull(ruleUUID);
    final PrivilegeService privilegeService = EnvTool.getPrivilegeService();
    final Rule rule = privilegeService.getRule(ruleUUID);
    if (rule == null) {
      throw new RuleNotFoundException("bai_MAPII_12", ruleUUID);
    }
    privilegeService.deleteRule(rule);
  }

  @Override
  public List<Rule> getAllApplicableRules(final String userUUID, final Collection<String> roleUUIDs,
      final Collection<String> groupUUIDs, final Collection<String> membershipUUIDs, final String entityID) {
    final PrivilegeService privilegeService = EnvTool.getPrivilegeService();
    final List<Rule> rules = privilegeService.getAllApplicableRules(userUUID, roleUUIDs, groupUUIDs, membershipUUIDs,
        entityID);

    return buildRulesResultList(rules);
  }

  @Override
  public List<Rule> getApplicableRules(final RuleType ruleType, final String userUUID,
      final Collection<String> roleUUIDs, final Collection<String> groupUUIDs,
      final Collection<String> membershipUUIDs, final String entityID) {
    FacadeUtil.checkArgsNotNull(ruleType);
    final PrivilegeService privilegeService = EnvTool.getPrivilegeService();
    final List<Rule> rules = privilegeService.getApplicableRules(ruleType, userUUID, roleUUIDs, groupUUIDs,
        membershipUUIDs, entityID);

    return buildRulesResultList(rules);
  }

  @Override
  public List<Rule> getAllRules() {
    final PrivilegeService privilegeService = EnvTool.getPrivilegeService();
    final List<Rule> rules = privilegeService.getAllRules();
    if (rules == null || rules.isEmpty()) {
      return Collections.emptyList();
    }
    return buildRulesResultList(rules);
  }

  @Override
  public Rule getRuleByUUID(final String ruleUUID) throws RuleNotFoundException {
    FacadeUtil.checkArgsNotNull(ruleUUID);
    final PrivilegeService privilegeService = EnvTool.getPrivilegeService();
    final Rule rule = privilegeService.getRule(ruleUUID);
    if (rule == null) {
      throw new RuleNotFoundException("bai_MAPII_12", ruleUUID);
    }
    return RuleImpl.createRule(rule);
  }

  @Override
  public List<Rule> getRulesByUUIDs(final Collection<String> ruleUUIDs) throws RuleNotFoundException {
    final PrivilegeService privilegeService = EnvTool.getPrivilegeService();
    if (ruleUUIDs.size() > 0) {
      final List<Rule> rules = privilegeService.getRules(ruleUUIDs);
      if (ruleUUIDs.size() != rules.size()) {
        // The request tries to get an unknown rule.
        final Set<String> storedRuleUUID = new HashSet<String>();
        for (final Rule rule : rules) {
          storedRuleUUID.add(rule.getUUID());
        }
        for (final String ruleUUID : ruleUUIDs) {
          if (!storedRuleUUID.contains(ruleUUID)) {
            throw new RuleNotFoundException("bai_MAPII_12", ruleUUID);
          }
        }
      }
      return buildRulesResultList(rules);
    } else {
      return Collections.emptyList();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public <E extends AbstractUUID> void removeExceptionsFromRuleByUUID(final String ruleUUID, final Set<E> exceptions)
      throws RuleNotFoundException {
    FacadeUtil.checkArgsNotNull(ruleUUID, exceptions);
    if (exceptions.size() > 0) {
      final PrivilegeService privilegeService = EnvTool.getPrivilegeService();
      final RuleImpl rule = (RuleImpl) privilegeService.getRule(ruleUUID);
      if (rule == null) {
        throw new RuleNotFoundException("bai_MAPII_12", ruleUUID);
      }
      if (rule instanceof ProcessRuleImpl) {
        ((ProcessRuleImpl) rule).removeProcesses((Set<ProcessDefinitionUUID>) exceptions);
      } else if (rule instanceof CategoryRuleImpl) {
        ((CategoryRuleImpl) rule).removeCategories((Set<CategoryUUID>) exceptions);
      } else if (rule instanceof CustomRuleImpl) {
        ((CustomRuleImpl) rule).removeCustomExceptions((Set<RuleExceptionUUID>) exceptions);
      } else if (rule instanceof ActivityRuleImpl) {
        ((ActivityRuleImpl) rule).removeActivities((Set<ActivityDefinitionUUID>) exceptions);
      } else {
        throw new IllegalArgumentException("Un-managed rule type: " + rule.getType());
      }
      privilegeService.updateRule(rule);
    }
  }

  @Override
  public void removeRuleFromEntities(final String ruleUUID, final Collection<String> userUUIDs,
      final Collection<String> roleUUIDs, final Collection<String> groupUUIDs,
      final Collection<String> membershipUUIDs, final Collection<String> entityIDs) throws RuleNotFoundException {
    FacadeUtil.checkArgsNotNull(ruleUUID);
    final PrivilegeService privilegeService = EnvTool.getPrivilegeService();
    final Rule rule = privilegeService.getRule(ruleUUID);
    if (rule == null) {
      throw new RuleNotFoundException("bai_MAPII_12", ruleUUID);
    }
    if (userUUIDs != null) {
      ((RuleImpl) rule).removeUsers(userUUIDs);
    }
    if (groupUUIDs != null) {
      ((RuleImpl) rule).removeGroups(groupUUIDs);
    }
    if (roleUUIDs != null) {
      ((RuleImpl) rule).removeRoles(roleUUIDs);
    }
    if (membershipUUIDs != null) {
      ((RuleImpl) rule).removeMemberships(membershipUUIDs);
    }
    if (entityIDs != null) {
      ((RuleImpl) rule).removeEntities(entityIDs);
    }
    privilegeService.updateRule(rule);
  }

  @Override
  public Rule updateRuleByUUID(final String ruleUUID, final String name, final String label, final String description)
      throws RuleNotFoundException, RuleAlreadyExistsException {
    FacadeUtil.checkArgsNotNull(ruleUUID);
    final PrivilegeService privilegeService = EnvTool.getPrivilegeService();
    final RuleImpl rule = (RuleImpl) privilegeService.getRule(ruleUUID);
    if (rule == null) {
      throw new RuleNotFoundException("bai_MAPII_12", ruleUUID);
    }
    final Rule ruleWithSameName = privilegeService.findRuleByName(name);
    if (ruleWithSameName != null && !ruleWithSameName.getUUID().equals(ruleUUID)) {
      throw new RuleAlreadyExistsException("bai_MAPII_13", name);
    }
    rule.setName(name);
    rule.setLabel(label);
    rule.setDescription(description);
    privilegeService.updateRule(rule);
    return RuleImpl.createRule(rule);
  }

  @Override
  public void updateMigrationDate(final ProcessDefinitionUUID processUUID, final Date migrationDate)
      throws ProcessNotFoundException {
    Misc.checkArgsNotNull(processUUID);
    final InternalProcessDefinition process = EnvTool.getAllQueriers().getProcess(processUUID);
    if (process == null) {
      throw new ProcessNotFoundException("bai_QDAPII_5", processUUID);
    }
    process.setMigrationDate(migrationDate);
  }

  @Override
  public void setResource(final ProcessDefinitionUUID processUUID, final String resourcePath, final byte[] content)
      throws ProcessNotFoundException {
    Misc.checkArgsNotNull(processUUID, resourcePath, content);
    final InternalProcessDefinition process = EnvTool.getAllQueriers(getQueryList()).getProcess(processUUID);
    if (process == null) {
      throw new ProcessNotFoundException("bai_QDAPII_5", processUUID);
    }
    final LargeDataRepository ldr = EnvTool.getLargeDataRepository();
    ldr.storeData(Misc.getBusinessArchiveCategories(processUUID), resourcePath, content, true);
    // otherwise the Classloader will take the old content of the resource
    EnvTool.getClassDataLoader().removeProcessClassLoader(processUUID);
  }
}
