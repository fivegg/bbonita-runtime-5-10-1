package org.ow2.bonita.integration.connector;

import java.util.Collection;

import javax.security.auth.login.LoginException;

import junit.framework.Assert;

import org.bonitasoft.connectors.bonita.resolvers.UserListRoleResolver;
import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.TaskInstance;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.BonitaException;
import org.ow2.bonita.util.ProcessBuilder;

public class MultipleRoleMappersTest extends APITestCase {

  private void johnJackJamesCanAdminCannot(ProcessInstanceUUID instanceUUID) throws BonitaException, LoginException {
    johnCanAdminCannot(instanceUUID);
    loginAs("jack", "bpm");
    Collection<TaskInstance> taskActivities = 
      getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    Assert.assertEquals(1, taskActivities.size());
    loginAs("james", "bpm");
    taskActivities = 
      getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    Assert.assertEquals(1, taskActivities.size());
  }
  
  private void adminCanJohnJackJamesCannot(ProcessInstanceUUID instanceUUID) throws BonitaException, LoginException {
    adminCanJohnCannot(instanceUUID);
    loginAs("jack", "bpm");
    Collection<TaskInstance> taskActivities = 
      getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    Assert.assertEquals(0, taskActivities.size());
    loginAs("james", "bpm");
    taskActivities = 
      getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    Assert.assertEquals(0, taskActivities.size());
  }
  
  private void johnCanAdminCannot(ProcessInstanceUUID instanceUUID) throws BonitaException, LoginException {
    loginAs("admin", "bpm");
    Collection<TaskInstance> taskActivities = 
      getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    Assert.assertEquals(0, taskActivities.size());
    loginAs("john", "bpm");
    taskActivities = 
      getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    Assert.assertEquals(1, taskActivities.size());
  }
  
  private void adminCanJohnCannot(ProcessInstanceUUID instanceUUID) throws BonitaException, LoginException {
    loginAs("john", "bpm");
    Collection<TaskInstance> taskActivities = 
      getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    Assert.assertEquals(0, taskActivities.size());
    loginAs("admin", "bpm");
    taskActivities = 
      getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    Assert.assertEquals(1, taskActivities.size());
  }
  
  private void JohnAndAdmin(ProcessDefinition definition) throws BonitaException, LoginException {
    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(definition));
    ProcessDefinitionUUID processUUID = process.getUUID();    
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    johnCanAdminCannot(instanceUUID);
    executeTask(instanceUUID, "first");
    adminCanJohnCannot(instanceUUID);
    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }
  
  private void JohnJackJamesAndAdmin(ProcessDefinition definition) throws BonitaException, LoginException {
    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(definition, null, UserListRoleResolver.class));
    ProcessDefinitionUUID processUUID = process.getUUID();    
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    johnJackJamesCanAdminCannot(instanceUUID);
    executeTask(instanceUUID, "first");
    adminCanJohnJackJamesCannot(instanceUUID);
    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testTwoTasksTwoHumans() throws BonitaException, LoginException {
    ProcessDefinition definition = 
      ProcessBuilder.createProcess("tasks", "1.0")
      .addHuman("john")
      .addHuman("admin")
      .addHumanTask("first", "john")
      .addHumanTask("second", "admin")
      .addTransition("f_s", "first", "second")
    .done();
    
    JohnAndAdmin(definition);
  }
  
  public void testTwoTasksManyHumans() throws BonitaException, LoginException {
    ProcessDefinition definition = 
      ProcessBuilder.createProcess("tasks", "1.0")
      .addHuman("john")
      .addHuman("jack")
      .addHuman("james")
      .addHuman("admin")
      .addHumanTask("first", "john", "james", "jack")
      .addHumanTask("second", "admin")
      .addTransition("f_s", "first", "second")
    .done();
   
    JohnJackJamesAndAdmin(definition);
  }

  public void testTwoTasksARoleMapperAndAHuman() throws BonitaException, LoginException {
    ProcessDefinition definition = 
      ProcessBuilder.createProcess("tasks", "1.0")
      .addHuman("john")
      .addGroup("Users")
        .addGroupResolver(UserListRoleResolver.class.getName())
        .addInputParameter("users", "john, james, jack")
      .addHuman("admin")
      .addHumanTask("first", "Users")
      .addHumanTask("second", "admin")
      .addTransition("f_s", "first", "second")
    .done();
    
    JohnJackJamesAndAdmin(definition);
  }

  public void testTwoTasksTwoRoleMappersAHuman() throws BonitaException, LoginException {
    ProcessDefinition definition = 
      ProcessBuilder.createProcess("tasks", "1.0")
      .addHuman("john")
      .addGroup("Users")
        .addGroupResolver(UserListRoleResolver.class.getName())
          .addInputParameter("users", "john, joe, james") 
      .addGroup("Customer")
        .addGroupResolver(UserListRoleResolver.class.getName())
          .addInputParameter("users", "jack, john, jane")
      .addHuman("admin")
      .addHumanTask("first", "Users", "Customer")
      .addHumanTask("second", "admin")
      .addTransition("f_s", "first", "second")
    .done();

    JohnJackJamesAndAdmin(definition);
  }

  public void testTwoTasksManyRoleMappers() throws BonitaException, LoginException {
    ProcessDefinition definition = 
      ProcessBuilder.createProcess("tasks", "1.0")
      .addHuman("john")
      .addHuman("administrator")
      .addGroup("Users")
        .addGroupResolver(UserListRoleResolver.class.getName())
          .addInputParameter("users", "joe, james") 
      .addGroup("Customer")
        .addGroupResolver(UserListRoleResolver.class.getName())
          .addInputParameter("users", "jack, jane")
      .addHuman("admin")
      .addHumanTask("first", "Users", "Customer", "john")
      .addHumanTask("second", "admin", "administrator")
      .addTransition("f_s", "first", "second")
    .done();

    JohnJackJamesAndAdmin(definition);
  }
}
