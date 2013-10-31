package org.ow2.bonita.facade.privilege;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.exception.RuleNotFoundException;
import org.ow2.bonita.facade.identity.Role;
import org.ow2.bonita.facade.identity.User;
import org.ow2.bonita.facade.privilege.Rule.RuleType;
import org.ow2.bonita.facade.runtime.Category;
import org.ow2.bonita.facade.runtime.command.WebGetVisibleCategoriesCommand;
import org.ow2.bonita.facade.uuid.CategoryUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.light.LightProcessDefinition;
import org.ow2.bonita.util.ProcessBuilder;

public class PrivilegeTest extends APITestCase {

  
  public void testAddPrivilege() throws Exception {
    Set<ProcessDefinitionUUID> processes = new HashSet<ProcessDefinitionUUID>();
    final String name = "r1";
    final String label = "label";
    final String description = "A description";
    final String entityID = "entity";
    final Rule newRule = getManagementAPI().createRule(name, label, description, RuleType.PROCESS_START);
    getManagementAPI().addExceptionsToRuleByUUID(newRule.getUUID(), processes);
    final Set<String> rules = new HashSet<String>();
    rules.add(newRule.getName());
    final Collection<String> userUUIDs = null;
    final Collection<String> roleUUIDs = null;
    final Collection<String> groupUUIDs = null;
    final Collection<String> membershipUUIDs = null;
    getManagementAPI().applyRuleToEntities(newRule.getUUID(),userUUIDs,roleUUIDs,groupUUIDs,membershipUUIDs, Arrays.asList(entityID));

    String userUUID = null;
    List<Rule> applicableRules = getManagementAPI().getApplicableRules(RuleType.PROCESS_START, userUUID, roleUUIDs, groupUUIDs, membershipUUIDs, entityID);
    assertEquals(1, applicableRules.size());
    assertEquals(newRule.getName(), applicableRules.iterator().next().getName());

    getManagementAPI().deleteRuleByUUID(newRule.getUUID());
  }

  public void testAddRule() throws Exception {
    Set<ProcessDefinitionUUID> processes = new HashSet<ProcessDefinitionUUID>();
    final String name = "r1";
    final String label = "label";
    final String description = "A description";
    final Rule newRule = getManagementAPI().createRule(name, label, description, RuleType.PROCESS_START);
    getManagementAPI().addExceptionsToRuleByUUID(newRule.getUUID(), processes);
    assertNotNull(newRule);
    assertEquals(name, newRule.getName());
    assertEquals(label, newRule.getLabel());
    assertEquals(description, newRule.getDescription());

    final Rule storedRule = getManagementAPI().getRuleByUUID(newRule.getUUID());
    assertNotNull(storedRule);
    assertEquals(newRule.getUUID(), storedRule.getUUID());
    assertEquals(name, storedRule.getName());
    assertEquals(label, storedRule.getLabel());
    assertEquals(description, storedRule.getDescription());

    getManagementAPI().deleteRuleByUUID(storedRule.getUUID());
  }

  public void testRemoveItemsFromRule() throws Exception {
    Set<ProcessDefinitionUUID> processes = new HashSet<ProcessDefinitionUUID>();
    processes.add(new ProcessDefinitionUUID("p1"));
    processes.add(new ProcessDefinitionUUID("p2"));
    processes.add(new ProcessDefinitionUUID("p3"));
    final String name = "r1";
    final String label = "label";
    final String description = "A description";
    Rule newRule = getManagementAPI().createRule(name, label, description, RuleType.PROCESS_START);
    getManagementAPI().addExceptionsToRuleByUUID(newRule.getUUID(), processes);

    HashSet<ProcessDefinitionUUID> itemsToRemove = new HashSet<ProcessDefinitionUUID>();
    getManagementAPI().removeExceptionsFromRuleByUUID(newRule.getUUID(), itemsToRemove);
    newRule = getManagementAPI().getRuleByUUID(newRule.getUUID());
    assertEquals(3, newRule.getItems().size());
    assertTrue(newRule.getItems().contains(new ProcessDefinitionUUID("p2").getValue()));

    itemsToRemove.add(new ProcessDefinitionUUID("p2"));
    getManagementAPI().removeExceptionsFromRuleByUUID(newRule.getUUID(), itemsToRemove);
    newRule = getManagementAPI().getRuleByUUID(newRule.getUUID());
    assertEquals(2, newRule.getItems().size());
    assertFalse(newRule.getItems().contains(new ProcessDefinitionUUID("p2").getValue()));

    getManagementAPI().deleteRuleByUUID(newRule.getUUID());
  }

  public void testAddItemsToRule() throws Exception {
    Set<ProcessDefinitionUUID> processes = new HashSet<ProcessDefinitionUUID>();
    processes.add(new ProcessDefinitionUUID("p1"));
    processes.add(new ProcessDefinitionUUID("p2"));
    processes.add(new ProcessDefinitionUUID("p3"));
    final String name = "r1";
    final String label = "label";
    final String description = "A description";
    Rule newRule = getManagementAPI().createRule(name, label, description, RuleType.PROCESS_START);
    getManagementAPI().addExceptionsToRuleByUUID(newRule.getUUID(), processes);

    HashSet<ProcessDefinitionUUID> itemsToAdd = new HashSet<ProcessDefinitionUUID>();
    getManagementAPI().addExceptionsToRuleByUUID(newRule.getUUID(), itemsToAdd);
    newRule = getManagementAPI().getRuleByUUID(newRule.getUUID());
    assertEquals(3, newRule.getItems().size());
    assertFalse(newRule.getItems().contains(new ProcessDefinitionUUID("p4").getValue()));

    itemsToAdd.add(new ProcessDefinitionUUID("p4"));
    getManagementAPI().addExceptionsToRuleByUUID(newRule.getUUID(), itemsToAdd);
    newRule = getManagementAPI().getRuleByUUID(newRule.getUUID());
    assertEquals(4, newRule.getItems().size());
    assertTrue(newRule.getItems().contains(new ProcessDefinitionUUID("p4").getValue()));

    getManagementAPI().deleteRuleByUUID(newRule.getUUID());
  }

  public void testAddRulesToPrivilegeNew() throws Exception {
    Set<ProcessDefinitionUUID> processes = new HashSet<ProcessDefinitionUUID>();
    processes.add(new ProcessDefinitionUUID("p1"));
    processes.add(new ProcessDefinitionUUID("p2"));
    processes.add(new ProcessDefinitionUUID("p3"));
    final String name = "r1";
    final String name2 = "r2";
    final String label = "label";
    final String description = "A description";
    Rule newRule1 = getManagementAPI().createRule(name, label, description, RuleType.PROCESS_START);
    getManagementAPI().addExceptionsToRuleByUUID(newRule1.getUUID(), processes);
    Rule newRule2 = getManagementAPI().createRule(name2, label, description, RuleType.PROCESS_START);
    getManagementAPI().addExceptionsToRuleByUUID(newRule2.getUUID(), processes);
    
    String entityID = "entity";
    final Collection<String> userUUIDs = null;
    final Collection<String> roleUUIDs = null;
    final Collection<String> groupUUIDs = null;
    final Collection<String> membershipUUIDs = null;
    getManagementAPI().applyRuleToEntities(newRule1.getUUID(),userUUIDs,roleUUIDs,groupUUIDs,membershipUUIDs, Arrays.asList(entityID));
    assertEquals(1, getManagementAPI().getRuleByUUID(newRule1.getUUID()).getEntities().size());

    getManagementAPI().applyRuleToEntities(newRule2.getUUID(),userUUIDs,roleUUIDs,groupUUIDs,membershipUUIDs, Arrays.asList(entityID));
    assertEquals(1, getManagementAPI().getRuleByUUID(newRule1.getUUID()).getEntities().size());
    assertEquals(1, getManagementAPI().getRuleByUUID(newRule2.getUUID()).getEntities().size());

    final String userUUID = null;
    List<Rule> userRules = getManagementAPI().getApplicableRules(RuleType.PROCESS_START, userUUID, roleUUIDs, groupUUIDs, membershipUUIDs, entityID);
    assertEquals(2, userRules.size());

    Set<String> ruleNames = new HashSet<String>();
    for (Rule rule : userRules) {
      ruleNames.add(rule.getName());
    }
    assertTrue(ruleNames.contains(newRule2.getName()));

    getManagementAPI().deleteRuleByUUID(newRule1.getUUID());
    getManagementAPI().deleteRuleByUUID(newRule2.getUUID());

  }

  public void testApplySamePrivilegeToEntitiesWithExistingEntity() throws Exception {
    Set<ProcessDefinitionUUID> processes = new HashSet<ProcessDefinitionUUID>();
    processes.add(new ProcessDefinitionUUID("p1"));
    processes.add(new ProcessDefinitionUUID("p2"));
    final String name = "r1";
    final String name2 = "r2";
    final String label = "label";
    final String description = "A description";
    Rule newRule1 = getManagementAPI().createRule(name, label, description, RuleType.PROCESS_START);
    getManagementAPI().addExceptionsToRuleByUUID(newRule1.getUUID(), processes);

    // add another process in the second rule.
    processes.add(new ProcessDefinitionUUID("p3"));
    Rule newRule2 = getManagementAPI().createRule(name2, label, description, RuleType.PROCESS_START);
    getManagementAPI().addExceptionsToRuleByUUID(newRule2.getUUID(), processes);
    
    String entityID = "entity";
    String entityID2 = "entity2";
    final Collection<String> userUUIDs = null;
    final Collection<String> roleUUIDs = null;
    final Collection<String> groupUUIDs = null;
    final Collection<String> membershipUUIDs = null;
    getManagementAPI().applyRuleToEntities(newRule1.getUUID(),userUUIDs,roleUUIDs,groupUUIDs,membershipUUIDs, Arrays.asList(entityID));
    assertEquals(1, getManagementAPI().getRuleByUUID(newRule1.getUUID()).getEntities().size());

    getManagementAPI().applyRuleToEntities(newRule1.getUUID(),userUUIDs,roleUUIDs,groupUUIDs,membershipUUIDs, Arrays.asList(entityID2));
    getManagementAPI().applyRuleToEntities(newRule2.getUUID(),userUUIDs,roleUUIDs,groupUUIDs,membershipUUIDs, Arrays.asList(entityID2));
    assertEquals(2, getManagementAPI().getRuleByUUID(newRule1.getUUID()).getEntities().size());
    assertEquals(1, getManagementAPI().getRuleByUUID(newRule2.getUUID()).getEntities().size());

    HashSet<String> newEntities = new HashSet<String>();
    newEntities.add(entityID2);

    // override second privilege.
    getManagementAPI().removeRuleFromEntities(newRule2.getUUID(), userUUIDs, roleUUIDs, groupUUIDs, membershipUUIDs, Arrays.asList(entityID2));
    assertEquals(2, getManagementAPI().getRuleByUUID(newRule1.getUUID()).getEntities().size());
    assertEquals(0, getManagementAPI().getRuleByUUID(newRule2.getUUID()).getEntities().size());

    final String userUUID = null;
    List<Rule> userRules = getManagementAPI().getApplicableRules(RuleType.PROCESS_START,userUUID ,roleUUIDs,groupUUIDs,membershipUUIDs,entityID2);
    assertNotNull(userRules);
    Set<String> ruleNames = new HashSet<String>();
    for (Rule rule : userRules) {
      ruleNames.add(rule.getName());
    }
    assertFalse(ruleNames.contains(name2));

    getManagementAPI().deleteRuleByUUID(newRule1.getUUID());
    getManagementAPI().deleteRuleByUUID(newRule2.getUUID());

  }
  

  public void testListProcessesWithDefaultPolicyAllow() throws Exception {

    // Create processes.
    ProcessDefinition process = ProcessBuilder.createProcess("P1", "11.22").addSystemTask("t1").done();

    ProcessDefinition process2 = ProcessBuilder.createProcess("P2", "AA.bb").addSystemTask("t1").done();

    ProcessDefinition process3 = ProcessBuilder.createProcess("P3", "a.b").addSystemTask("t1").done();

    // Deploy processes.
    process = getManagementAPI().deploy(getBusinessArchive(process));
    process2 = getManagementAPI().deploy(getBusinessArchive(process2));
    process3 = getManagementAPI().deploy(getBusinessArchive(process3));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    final ProcessDefinitionUUID processUUID2 = process2.getUUID();
    final ProcessDefinitionUUID processUUID3 = process3.getUUID();

    // Create rule allowing access to a subset of processes.
    Set<ProcessDefinitionUUID> processes = new HashSet<ProcessDefinitionUUID>();
    processes.add(processUUID);
    processes.add(processUUID2);
    final String ruleName = "r1";
    final String ruleLabel = "2 first processes";
    final String ruleDescription = "Exceptions are P1 and P2.";
    Rule newRule = getManagementAPI().createRule(ruleName, ruleLabel, ruleDescription, RuleType.PROCESS_START);
    getManagementAPI().addExceptionsToRuleByUUID(newRule.getUUID(), processes);

    // Create privilege for user using the previously created rule.
    User user = getIdentityAPI().findUserByUserName(getLogin());
    if (user == null) {
      throw new RuntimeException("No user found!");
    }

    // As the global policy is supposed to be 'Allow_by_default' it means that
    // the user can start all the processes except those listed in the rules.
    getManagementAPI().applyRuleToEntities(newRule.getUUID(), null, null, null, null, Arrays.asList(user.getUsername()));

    // List processes the user is NOT allowed to start.
    List<Rule> userRules = getManagementAPI().getApplicableRules(RuleType.PROCESS_START,null,null,null,null,user.getUsername());
    Set<String> processException;
    Set<ProcessDefinitionUUID> exceptionProcessesUUID = new HashSet<ProcessDefinitionUUID>();
    for (Rule rule : userRules) {
      processException = rule.getItems();
      for (String processID : processException) {
        exceptionProcessesUUID.add(new ProcessDefinitionUUID(processID));
      }
    }

    // List of processes that the user can start.
    List<LightProcessDefinition> allProcesses = getQueryDefinitionAPI().getAllLightProcessesExcept(
        exceptionProcessesUUID, 0, 20);
    Set<ProcessDefinitionUUID> allProcessesUUID = new HashSet<ProcessDefinitionUUID>();
    for (LightProcessDefinition processDef : allProcesses) {
      allProcessesUUID.add(processDef.getUUID());
    }

    // Check the list of processes
    assertEquals(1, allProcessesUUID.size());
    assertFalse(allProcessesUUID.contains(processUUID));
    assertFalse(allProcessesUUID.contains(processUUID2));
    assertTrue(allProcessesUUID.contains(processUUID3));

    getManagementAPI().deleteRuleByUUID(newRule.getUUID());

    getManagementAPI().deleteAllProcesses();

  }


  public void testListProcessesWithDefaultPolicyDeny() throws Exception {

    // Create processes.
    ProcessDefinition process = ProcessBuilder.createProcess("P1", "11.22").addSystemTask("t1").done();

    ProcessDefinition process2 = ProcessBuilder.createProcess("P2", "AA.bb").addSystemTask("t1").done();

    ProcessDefinition process3 = ProcessBuilder.createProcess("P3", "a.b").addSystemTask("t1").done();

    // Deploy processes.
    process = getManagementAPI().deploy(getBusinessArchive(process));
    process2 = getManagementAPI().deploy(getBusinessArchive(process2));
    process3 = getManagementAPI().deploy(getBusinessArchive(process3));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    final ProcessDefinitionUUID processUUID2 = process2.getUUID();
    final ProcessDefinitionUUID processUUID3 = process3.getUUID();

    // Create rule allowing access to a subset of processes.
    Set<ProcessDefinitionUUID> processes = new HashSet<ProcessDefinitionUUID>();
    processes.add(processUUID);
    processes.add(processUUID2);
    final String ruleName = "r1";
    final String ruleLabel = "2 first processes";
    final String ruleDescription = "Exceptions are P1 and P2.";
    Rule newRule = getManagementAPI().createRule(ruleName, ruleLabel, ruleDescription, RuleType.PROCESS_START);
    getManagementAPI().addExceptionsToRuleByUUID(newRule.getUUID(), processes);

    // Create privilege for user using the previously created rule.
    User user = getIdentityAPI().findUserByUserName(getLogin());
    if (user == null) {
      throw new RuntimeException("No user found!");
    }

    // As the global policy is supposed to be 'Deny_by_default' it means that
    // the user can only start the processes listed in the rules.
    getManagementAPI().applyRuleToEntities(newRule.getUUID(), null,null,null,null,Arrays.asList(user.getUsername()));

    // List processes the user can start.
    List<Rule> userRules = getManagementAPI().getApplicableRules(RuleType.PROCESS_START,null,null,null,null,user.getUsername());

    Set<String> processException;
    Set<ProcessDefinitionUUID> processUUIDException = new HashSet<ProcessDefinitionUUID>();
    for (Rule rule : userRules) {
      processException = rule.getItems();
      for (String processID : processException) {
        processUUIDException.add(new ProcessDefinitionUUID(processID));
      }
    }
    Set<LightProcessDefinition> allProcesses = getQueryDefinitionAPI().getLightProcesses(processUUIDException);
    Set<ProcessDefinitionUUID> allProcessesUUID = new HashSet<ProcessDefinitionUUID>();
    for (LightProcessDefinition lightProcessDefinition : allProcesses) {
      allProcessesUUID.add(lightProcessDefinition.getUUID());
    }

    // Check the list of processes
    assertEquals(2, allProcessesUUID.size());
    assertTrue(allProcessesUUID.contains(processUUID));
    assertTrue(allProcessesUUID.contains(processUUID2));
    assertFalse(allProcessesUUID.contains(processUUID3));

    getManagementAPI().deleteRuleByUUID(newRule.getUUID());

    getManagementAPI().deleteAllProcesses();

  }
  
  
  public void testListCategorieWithDefaultPolicyAllow() throws Exception {
    final String visibleCategoryName = "visible";
    final String restrictedCategoryName = "hidden";

    // Create processes.
    ProcessDefinition process = ProcessBuilder.createProcess("P1", "11.22")
      .addSystemTask("t1")
      .addCategory(visibleCategoryName)
      .done();
    ProcessDefinition process2 = ProcessBuilder.createProcess("P2", "AA.bb")
      .addSystemTask("t1")
      .addCategory(restrictedCategoryName)
      .done();
    ProcessDefinition process3 = ProcessBuilder.createProcess("P3", "a.b")
      .addSystemTask("t1")
      .addCategory(visibleCategoryName)
      .done();

    // Deploy processes.
    getManagementAPI().deploy(getBusinessArchive(process));
    getManagementAPI().deploy(getBusinessArchive(process2));
    getManagementAPI().deploy(getBusinessArchive(process3));

    Set<Category> categories = getWebAPI().getAllCategories();
    final Category visibleCategory = getCategory(categories, visibleCategoryName);
    final Category restrictedCategory = getCategory(categories, restrictedCategoryName);

    // list categories the user can see.
    categories = getCommandAPI().execute(new WebGetVisibleCategoriesCommand(null, null, null, null, getLogin()));
    assertNotNull(categories);
    assertEquals(2, categories.size());

    // Create rule preventing access to a subset of categories.
    Set<CategoryUUID> exceptions = new HashSet<CategoryUUID>();
    exceptions.add(new CategoryUUID(restrictedCategory.getUUID()));
    final String ruleName = "r1";
    final String ruleLabel = "2nd process";
    final String ruleDescription = "Exception is restrictedCategory.";
    Rule newRule = getManagementAPI().createRule(ruleName, ruleLabel, ruleDescription, RuleType.CATEGORY_READ);
    getManagementAPI().addExceptionsToRuleByUUID(newRule.getUUID(), exceptions);

    // Create privilege for user using the previously created rule.
    // As the global policy is supposed to be 'Allow_by_default' it means that
    // the user can see all categories except those listed in the rules.
    getManagementAPI().applyRuleToEntities(newRule.getUUID(), null, null, null, null, Arrays.asList(getLogin()));

    // List categories the user can see.
    categories = getCommandAPI().execute(new WebGetVisibleCategoriesCommand(null, null, null, null, getLogin()));
    assertNotNull(categories);
    assertEquals(1, categories.size());
    assertEquals(visibleCategory, categories.iterator().next());

    getManagementAPI().deleteRuleByUUID(newRule.getUUID());
    getManagementAPI().deleteAllProcesses();
    getWebAPI().deleteCategories(
        new HashSet<String>(Arrays.asList(visibleCategoryName, restrictedCategoryName)));

  }

  
  public void testListCategoriesWithDefaultPolicyDeny() throws Exception {
    // Set the policy.
    getManagementAPI().setRuleTypePolicy(RuleType.CATEGORY_READ, PrivilegePolicy.DENY_BY_DEFAULT);
    final String visibleCategoryName = "visible";
    final String restrictedCategoryName = "hidden";

    // Create processes.
    ProcessDefinition process = ProcessBuilder.createProcess("P1", "11.22")
      .addSystemTask("t1")
      .addCategory(visibleCategoryName)
      .done();

    ProcessDefinition process2 = ProcessBuilder.createProcess("P2", "AA.bb")
      .addSystemTask("t1")
      .addCategory(restrictedCategoryName)
      .done();

    ProcessDefinition process3 = ProcessBuilder.createProcess("P3", "a.b")
      .addSystemTask("t1")
      .addCategory(visibleCategoryName)
      .done();

    // Deploy processes.
    getManagementAPI().deploy(getBusinessArchive(process));
    getManagementAPI().deploy(getBusinessArchive(process2));
    getManagementAPI().deploy(getBusinessArchive(process3));

    Set<Category> categories = getWebAPI().getAllCategories();
    final Category visibleCategory = getCategory(categories, visibleCategoryName);
    // list categories the user can see.
    categories = getCommandAPI().execute(new WebGetVisibleCategoriesCommand(null, null, null, null, getLogin()));
    assertNotNull(categories);
    assertEquals(0, categories.size());

    // Create rule preventing access to a subset of categories.
    Set<CategoryUUID> exceptions = new HashSet<CategoryUUID>();
    exceptions.add(new CategoryUUID(visibleCategory.getUUID()));
    final String ruleName = "r2";
    final String ruleLabel = "1st and 3rd processes";
    final String ruleDescription = "Exception is visibleCategory.";
    Rule newRule = getManagementAPI().createRule(ruleName, ruleLabel, ruleDescription, RuleType.CATEGORY_READ);
    getManagementAPI().addExceptionsToRuleByUUID(newRule.getUUID(), exceptions);

    // Create privilege for user using the previously created rule.
    // As the global policy is supposed to be 'Allow_by_default' it means that
    // the user can see all categories except those listed in the rules.
    getManagementAPI().applyRuleToEntities(newRule.getUUID(), null, null, null, null, Arrays.asList(getLogin()));

    // List categories the user can see.
    categories = getCommandAPI().execute(new WebGetVisibleCategoriesCommand(null, null,null,null,getLogin()));
    assertNotNull(categories);
    assertEquals(1, categories.size());
    assertEquals(visibleCategory, categories.iterator().next());

    // clean
    getManagementAPI().deleteRuleByUUID(newRule.getUUID());
    getManagementAPI().deleteAllProcesses();
    getWebAPI().deleteCategories(
        new HashSet<String>(Arrays.asList(visibleCategoryName, restrictedCategoryName)));
    getManagementAPI().setRuleTypePolicy(RuleType.CATEGORY_READ, PrivilegePolicy.ALLOW_BY_DEFAULT);
  }

  public void testUpdateDefaultPolicyNew() throws Exception {

    assertEquals(PrivilegePolicy.ALLOW_BY_DEFAULT, getManagementAPI().getRuleTypePolicy(RuleType.PROCESS_START));

    getManagementAPI().setRuleTypePolicy(RuleType.PROCESS_START, PrivilegePolicy.DENY_BY_DEFAULT);
    assertEquals(PrivilegePolicy.DENY_BY_DEFAULT, getManagementAPI().getRuleTypePolicy(RuleType.PROCESS_START));

    getManagementAPI().setRuleTypePolicy(RuleType.PROCESS_START, PrivilegePolicy.ALLOW_BY_DEFAULT);
    assertEquals(PrivilegePolicy.ALLOW_BY_DEFAULT, getManagementAPI().getRuleTypePolicy(RuleType.PROCESS_START));

    getManagementAPI().setRuleTypePolicy(RuleType.PROCESS_READ,PrivilegePolicy.DENY_BY_DEFAULT);
    assertEquals(PrivilegePolicy.DENY_BY_DEFAULT, getManagementAPI().getRuleTypePolicy(RuleType.PROCESS_READ));

    getManagementAPI().setRuleTypePolicy(RuleType.PROCESS_READ, PrivilegePolicy.ALLOW_BY_DEFAULT);
    assertEquals(PrivilegePolicy.ALLOW_BY_DEFAULT, getManagementAPI().getRuleTypePolicy(RuleType.PROCESS_READ));
  }

  
  public void testGetNumberOfRules() throws Exception {

    long numberOfRules = getManagementAPI().getNumberOfRules(RuleType.PROCESS_START);
    assertEquals(0, numberOfRules);

    Set<ProcessDefinitionUUID> processes = new HashSet<ProcessDefinitionUUID>();
    final String name = "r1";
    final String label = "label";
    final String description = "A description";
    Rule rule = getManagementAPI().createRule(name, label, description, RuleType.PROCESS_START);
    getManagementAPI().addExceptionsToRuleByUUID(rule.getUUID(), processes);

    numberOfRules = getManagementAPI().getNumberOfRules(RuleType.PROCESS_START);
    assertEquals(1, numberOfRules);

    getManagementAPI().deleteRuleByUUID(rule.getUUID());
  }

  public void testAddUpdateRemoveRule() throws Exception {
    Set<ProcessDefinitionUUID> processes = new HashSet<ProcessDefinitionUUID>();
    final String processUUID1Str = "process1";
    processes.add(new ProcessDefinitionUUID(processUUID1Str));
    final String name = "r1";
    final String label = "label";
    final String description = "A description";
    Rule newRule = getManagementAPI().createRule(name, label, description, RuleType.PROCESS_START);
    getManagementAPI().addExceptionsToRuleByUUID(newRule.getUUID(), processes);
    assertNotNull(newRule);
    assertEquals(name, newRule.getName());
    assertEquals(label, newRule.getLabel());
    assertEquals(description, newRule.getDescription());
    assertEquals(RuleType.PROCESS_START, newRule.getType());

    String ruleUUID = newRule.getUUID();
    
    Rule storedRule = getManagementAPI().getRuleByUUID(ruleUUID);
    assertNotNull(storedRule);
    assertEquals(name, storedRule.getName());
    assertEquals(label, storedRule.getLabel());
    assertEquals(description, storedRule.getDescription());
    assertEquals(RuleType.PROCESS_START, storedRule.getType());
    assertEquals(processUUID1Str, storedRule.getItems().iterator().next());
    
    getManagementAPI().removeExceptionsFromRuleByUUID(storedRule.getUUID(), processes);
    processes.clear();
    final String processUUID2Str = "process2";
    processes.add(new ProcessDefinitionUUID(processUUID2Str));
    final String newName = "newname";
    final String newLabel = "new label";
    final String newDescription = "new description";
    Rule udatedRule = getManagementAPI().updateRuleByUUID(ruleUUID, newName, newLabel, newDescription);
    getManagementAPI().addExceptionsToRuleByUUID(udatedRule.getUUID(), processes);
    assertNotNull(udatedRule);
    assertEquals(newName, udatedRule.getName());
    assertEquals(newLabel, udatedRule.getLabel());
    assertEquals(newDescription, udatedRule.getDescription());
    
    storedRule = getManagementAPI().getRuleByUUID(ruleUUID);
    assertNotNull(storedRule);
    assertEquals(newName, storedRule.getName());
    assertEquals(newLabel, storedRule.getLabel());
    assertEquals(newDescription, storedRule.getDescription());
    assertEquals(processUUID2Str, storedRule.getItems().iterator().next());

    getManagementAPI().deleteRuleByUUID(ruleUUID);
    try {
      getManagementAPI().getRuleByUUID(ruleUUID);
      fail("an exception should have been thrown as the rule shouldn't exist anymore");
    } catch (RuleNotFoundException e) {
      //Nothing to do (the exception is expected)
    } 
  }
  
  public void testSetRuleTypePolicy() throws Exception {
    PrivilegePolicy defaultCategoryReadPolicy = getManagementAPI().getRuleTypePolicy(RuleType.CATEGORY_READ);
    assertNotNull(defaultCategoryReadPolicy);
    assertEquals(PrivilegePolicy.ALLOW_BY_DEFAULT, defaultCategoryReadPolicy);
    
    getManagementAPI().setRuleTypePolicy(RuleType.PROCESS_START, PrivilegePolicy.DENY_BY_DEFAULT);
    PrivilegePolicy processStartPolicy = getManagementAPI().getRuleTypePolicy(RuleType.PROCESS_START);
    assertNotNull(processStartPolicy);
    assertEquals(PrivilegePolicy.DENY_BY_DEFAULT, processStartPolicy);
    
    getManagementAPI().setRuleTypePolicy(RuleType.PROCESS_START, PrivilegePolicy.ALLOW_BY_DEFAULT);
  }
  
  public void testAddRemovePrivileges() throws Exception {
    Set<ProcessDefinitionUUID> processes = new HashSet<ProcessDefinitionUUID>();
    processes.add(new ProcessDefinitionUUID("process1"));
    Rule processRule = getManagementAPI().createRule("r1", "label1", "description1", RuleType.PROCESS_START);
    getManagementAPI().addExceptionsToRuleByUUID(processRule.getUUID(), processes);
    Set<CategoryUUID> categories = new HashSet<CategoryUUID>();
    categories.add(new CategoryUUID("categorie1"));
    Rule categoryRule = getManagementAPI().createRule("r2", "label2", "description2", RuleType.CATEGORY_READ);
    getManagementAPI().addExceptionsToRuleByUUID(categoryRule.getUUID(), categories);
    
    Set<String> userUUIDs = new HashSet<String>();
    User dwight = getIdentityAPI().addUser("dwight.schrute", "battlestargalactica", "dwight", "schrute", "Mr", "Salesman - Assistant Regional Manager", null, null);
    userUUIDs.add(dwight.getUUID());
    Set<String> roleUUIDs = new HashSet<String>();
    Role managerRole = getIdentityAPI().addRole("regionalmanager", "regional manager", "regional branch manager");
    roleUUIDs.add(managerRole.getUUID());
    Role salesmanRole = getIdentityAPI().addRole("salesmanRole", "salesman role", "Dunder Mifflin salesman");
    roleUUIDs.add(salesmanRole.getUUID());
    Set<String> entityIDs = new HashSet<String>();
    entityIDs.add("application");
    
    getManagementAPI().applyRuleToEntities(processRule.getUUID(), userUUIDs, null, null, null, null);
    getManagementAPI().applyRuleToEntities(categoryRule.getUUID(), null, roleUUIDs, null, null, null);
    
    List<Rule> allApplicableRules = getManagementAPI().getAllApplicableRules(dwight.getUUID(), roleUUIDs, null, null, null);
    assertEquals(2, allApplicableRules.size());
    for (Rule rule : allApplicableRules) {
      if (RuleType.PROCESS_START.equals(rule.getType())) {
        assertEquals(processRule.getName(), rule.getName());
      } else {
        assertEquals(categoryRule.getName(), rule.getName());
      }
    }
    List<Rule> applicableCategoryRules = getManagementAPI().getApplicableRules(RuleType.CATEGORY_READ, null, roleUUIDs, null, null, null);
    assertEquals(1, applicableCategoryRules.size());
    assertEquals(categoryRule.getName(), applicableCategoryRules.iterator().next().getName());
    getManagementAPI().removeRuleFromEntities(processRule.getUUID(), userUUIDs, null, null, null, null);
    getManagementAPI().removeRuleFromEntities(categoryRule.getUUID(), null, roleUUIDs, null, null, null);
    allApplicableRules = getManagementAPI().getAllApplicableRules(dwight.getUUID(), roleUUIDs, null, null, null);
    assertEquals(0, allApplicableRules.size());

    getManagementAPI().deleteRuleByUUID(processRule.getUUID());
    getManagementAPI().deleteRuleByUUID(categoryRule.getUUID());
    
    getIdentityAPI().removeUserByUUID(dwight.getUUID());
    getIdentityAPI().removeRoles(roleUUIDs);
  }
  
  public void testGetAllApplicableRulesWithNullUserId() throws Exception {
    Set<ProcessDefinitionUUID> processes = new HashSet<ProcessDefinitionUUID>();
    processes.add(new ProcessDefinitionUUID("process1"));
    Rule processRule = getManagementAPI().createRule("r1", "label1", "description1", RuleType.PROCESS_START);
    getManagementAPI().addExceptionsToRuleByUUID(processRule.getUUID(), processes);
    Set<CategoryUUID> categories = new HashSet<CategoryUUID>();
    categories.add(new CategoryUUID("categorie1"));
    Rule categoryRule = getManagementAPI().createRule("r2", "label2", "description2", RuleType.CATEGORY_READ);
    getManagementAPI().addExceptionsToRuleByUUID(categoryRule.getUUID(), categories);
    
    Set<String> userUUIDs = new HashSet<String>();
    User dwight = getIdentityAPI().addUser("dwight.schrute", "battlestargalactica", "dwight", "schrute", "Mr", "Salesman - Assistant Regional Manager", null, null);
    userUUIDs.add(dwight.getUUID());
    Set<String> roleUUIDs = new HashSet<String>();
    Role managerRole = getIdentityAPI().addRole("regionalmanager", "regional manager", "regional branch manager");
    roleUUIDs.add(managerRole.getUUID());
    Role salesmanRole = getIdentityAPI().addRole("salesmanRole", "salesman role", "Dunder Mifflin salesman");
    roleUUIDs.add(salesmanRole.getUUID());
    Set<String> entityIDs = new HashSet<String>();
    entityIDs.add("application");
    
    getManagementAPI().applyRuleToEntities(processRule.getUUID(), userUUIDs, null, null, null, null);
    getManagementAPI().applyRuleToEntities(categoryRule.getUUID(), null, roleUUIDs, null, null, null);
    
    List<Rule> allApplicableRules = getManagementAPI().getAllApplicableRules(null, roleUUIDs, null, null, null);
    assertEquals(1, allApplicableRules.size());
    assertEquals(categoryRule.getName(), allApplicableRules.get(0).getName());
    List<Rule> applicableCategoryRules = getManagementAPI().getApplicableRules(RuleType.CATEGORY_READ, null, roleUUIDs, null, null, null);
    assertEquals(1, applicableCategoryRules.size());
    assertEquals(categoryRule.getName(), applicableCategoryRules.iterator().next().getName());
    getManagementAPI().removeRuleFromEntities(processRule.getUUID(), userUUIDs, null, null, null, null);
    getManagementAPI().removeRuleFromEntities(categoryRule.getUUID(), null, roleUUIDs, null, null, null);
    allApplicableRules = getManagementAPI().getAllApplicableRules(dwight.getUUID(), roleUUIDs, null, null, null);
    assertEquals(0, allApplicableRules.size());

    getManagementAPI().deleteRuleByUUID(processRule.getUUID());
    getManagementAPI().deleteRuleByUUID(categoryRule.getUUID());
    
    getIdentityAPI().removeUserByUUID(dwight.getUUID());
    getIdentityAPI().removeRoles(roleUUIDs);
  }
}
