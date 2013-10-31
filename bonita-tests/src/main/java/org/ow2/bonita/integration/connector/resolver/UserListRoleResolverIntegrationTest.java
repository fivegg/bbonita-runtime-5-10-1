package org.ow2.bonita.integration.connector.resolver;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.security.auth.login.LoginException;

import junit.framework.Assert;

import org.bonitasoft.connectors.bonita.resolvers.UserListRoleResolver;
import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.exception.InstanceNotFoundException;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.InstanceState;
import org.ow2.bonita.facade.runtime.TaskInstance;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.ProcessBuilder;

public class UserListRoleResolverIntegrationTest extends APITestCase {

  protected void checkAdminAndCustomerTasks(final ProcessInstanceUUID instanceUUID) throws LoginException,
      InstanceNotFoundException {
    loginAs("admin", "bpm");
    Collection<TaskInstance> taskActivities = getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    Assert.assertEquals(0, taskActivities.size());
    loginAs("john", "bpm");
    taskActivities = getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    Assert.assertEquals(1, taskActivities.size());
  }

  protected void checkAdminAndOtherTasks(final ProcessInstanceUUID instanceUUID) throws LoginException,
      InstanceNotFoundException {
    loginAs("admin", "bpm");
    Collection<TaskInstance> taskActivities = getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    Assert.assertEquals(0, taskActivities.size());
    loginAs("john", "bpm");
    taskActivities = getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    Assert.assertEquals(0, taskActivities.size());
  }

  public void testAUser() throws Exception {
    final ProcessDefinition definition = ProcessBuilder.createProcess("AUser", "1.0").addGroup("Custom")
        .addGroupResolver(UserListRoleResolver.class.getName()).addInputParameter("users", "john")
        .addHumanTask("Request", "Custom").done();

    final ProcessDefinition process = getManagementAPI().deploy(
        getBusinessArchive(definition, null, UserListRoleResolver.class));

    final ProcessDefinitionUUID processUUID = process.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    checkAdminAndCustomerTasks(instanceUUID);

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testAGroovyUser() throws Exception {
    final ProcessDefinition definition = ProcessBuilder.createProcess("AUser", "1.0").addGroup("Custom")
        .addGroupResolver(UserListRoleResolver.class.getName())
        .addInputParameter("users", "${men = ['john', 'jack', 'joe']; men[0]}").addHumanTask("Request", "Custom")
        .done();

    final ProcessDefinition process = getManagementAPI().deploy(
        getBusinessArchive(definition, null, UserListRoleResolver.class));

    final ProcessDefinitionUUID processUUID = process.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    checkAdminAndCustomerTasks(instanceUUID);

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testAUserList() throws Exception {
    final ProcessDefinition definition = ProcessBuilder.createProcess("AUser", "1.0").addGroup("Custom")
        .addGroupResolver(UserListRoleResolver.class.getName()).addInputParameter("users", "john, jack, joe")
        .addHumanTask("Request", "Custom").done();

    final ProcessDefinition process = getManagementAPI().deploy(
        getBusinessArchive(definition, null, UserListRoleResolver.class));

    final ProcessDefinitionUUID processUUID = process.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    checkAdminAndCustomerTasks(instanceUUID);

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testFailAGroovyUserList() throws Exception {
    final ProcessDefinition definition = ProcessBuilder.createProcess("AUser", "1.0").addGroup("Custom")
        .addGroupResolver(UserListRoleResolver.class.getName())
        .addInputParameter("users", "${men = ['john', 'jack', 'joe']}").addHumanTask("Request", "Custom").done();

    final ProcessDefinition process = getManagementAPI().deploy(
        getBusinessArchive(definition, null, UserListRoleResolver.class));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    checkAdminAndCustomerTasks(instanceUUID);
    checkState(instanceUUID, InstanceState.STARTED);

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testFailABoolean() throws Exception {
    final ProcessDefinition definition = ProcessBuilder.createProcess("AUser", "1.0").addGroup("Custom")
        .addGroupResolver(UserListRoleResolver.class.getName()).addInputParameter("users", true)
        .addHumanTask("Request", "Custom").done();

    final ProcessDefinition process = getManagementAPI().deploy(
        getBusinessArchive(definition, null, UserListRoleResolver.class));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    checkState(instanceUUID, ActivityState.FAILED, "Request");
    checkState(instanceUUID, InstanceState.STARTED);

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testFailAnIntegerGroovy() throws Exception {
    final ProcessDefinition definition = ProcessBuilder.createProcess("AUser", "1.0").addGroup("Custom")
        .addGroupResolver(UserListRoleResolver.class.getName()).addInputParameter("users", "${4 + 5}")
        .addHumanTask("Request", "Custom").done();

    final ProcessDefinition process = getManagementAPI().deploy(
        getBusinessArchive(definition, null, UserListRoleResolver.class));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    checkState(instanceUUID, ActivityState.FAILED, "Request");
    checkState(instanceUUID, InstanceState.STARTED);

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testFailAWrongUser() throws Exception {
    final ProcessDefinition definition = ProcessBuilder.createProcess("AUser", "1.0").addGroup("Custom")
        .addGroupResolver(UserListRoleResolver.class.getName()).addInputParameter("users", "albert")
        .addHumanTask("Request", "Custom").done();

    final ProcessDefinition process = getManagementAPI().deploy(
        getBusinessArchive(definition, null, UserListRoleResolver.class));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    checkAdminAndOtherTasks(instanceUUID);
    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testAVariableGroovyUser() throws Exception {
    final ProcessDefinition definition = ProcessBuilder.createProcess("AUser", "1.0")
        .addStringData("var1", "${men = ['john', 'jack', 'joe']; men[0]}").addGroup("Custom")
        .addGroupResolver(UserListRoleResolver.class.getName()).addInputParameter("users", "${var1}")
        .addHumanTask("Request", "Custom").done();

    final ProcessDefinition process = getManagementAPI().deploy(
        getBusinessArchive(definition, null, UserListRoleResolver.class));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    checkAdminAndCustomerTasks(instanceUUID);

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testAVariableUserList() throws Exception {
    final ProcessDefinition definition = ProcessBuilder.createProcess("AUser", "1.0")
        .addStringData("var1", "john, jack, joe").addGroup("Custom")
        .addGroupResolver(UserListRoleResolver.class.getName()).addInputParameter("users", "${var1}")
        .addHumanTask("Request", "Custom").done();

    final ProcessDefinition process = getManagementAPI().deploy(
        getBusinessArchive(definition, null, UserListRoleResolver.class));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    checkAdminAndCustomerTasks(instanceUUID);

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testAGroovyUserList() throws Exception {
    final ProcessDefinition definition = ProcessBuilder.createProcess("AUser", "1.0")
        .addStringData("var1", "${men = ['john', 'jack', 'joe']}").addGroup("Custom")
        .addGroupResolver(UserListRoleResolver.class.getName()).addInputParameter("users", "${var1}")
        .addHumanTask("Request", "Custom").done();

    final ProcessDefinition process = getManagementAPI().deploy(
        getBusinessArchive(definition, null, UserListRoleResolver.class));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    checkAdminAndCustomerTasks(instanceUUID);

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testAProcessUserList() throws Exception {
    final ProcessDefinition definition = ProcessBuilder.createProcess("AUser", "1.0")
        .addObjectData("users", List.class.getName(), Arrays.asList("john", "jack", "joe")).addGroup("Custom")
        .addGroupResolver(UserListRoleResolver.class.getName()).addInputParameter("users", "${users}")
        .addHumanTask("Request", "Custom").done();

    final ProcessDefinition process = getManagementAPI().deploy(
        getBusinessArchive(definition, null, UserListRoleResolver.class));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    checkAdminAndCustomerTasks(instanceUUID);

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testATaskUserList() throws Exception {
    final ProcessDefinition definition = ProcessBuilder.createProcess("AUser", "1.0").addGroup("Custom")
        .addGroupResolver(UserListRoleResolver.class.getName()).addInputParameter("users", "${users}")
        .addHumanTask("Request", "Custom")
        .addObjectData("users", List.class.getName(), Arrays.asList("john", "jack", "joe")).done();

    final ProcessDefinition process = getManagementAPI().deploy(
        getBusinessArchive(definition, null, UserListRoleResolver.class));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    checkAdminAndCustomerTasks(instanceUUID);

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

}
