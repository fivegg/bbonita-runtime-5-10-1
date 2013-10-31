package org.ow2.bonita.integration.connector.filter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bonitasoft.connectors.bonita.filters.HasPerformedTask;
import org.bonitasoft.connectors.bonita.resolvers.UserListRoleResolver;
import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.runtime.ActivityInstance;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.ProcessBuilder;

public class HasPerformedTaskIntegrationTest extends APITestCase {

  public void testSimplePerformer() throws Exception {
    Set<String> tasks = new HashSet<String>();
    tasks.add("first");

    ProcessDefinition definition = ProcessBuilder.createProcess("performer", "8.2")
      .addGroup("humans")
        .addGroupResolver(UserListRoleResolver.class.getName())
          .addInputParameter("users", "john, james, admin")
      .addHuman("john")
      .addHumanTask("first", "john")
      .addHumanTask("last", "humans")
        .addFilter(HasPerformedTask.class.getName())
          .addInputParameter("taskNames", tasks)
      .addTransition("first", "last")
    .done();

    definition = getManagementAPI().deploy(getBusinessArchive(definition,
        null, UserListRoleResolver.class, HasPerformedTask.class));
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());

    loginAs("john", getPassword());
    executeTask(instanceUUID, "first");

    Set<ActivityInstance> activities = getQueryRuntimeAPI().getActivityInstances(instanceUUID, "last");
    assertEquals(1, activities.size());
    ActivityInstance activity = activities.iterator().next();
    assertEquals("last", activity.getActivityName());
    String user = activity.getTask().getTaskUser();
    assertEquals("john", user);

    getManagementAPI().deleteProcess(definition.getUUID());
  }

  public void testListPerformer() throws Exception {
    List<String> tasks = new ArrayList<String>();
    tasks.add("first");

    ProcessDefinition definition = ProcessBuilder.createProcess("performer", "8.2")
      .addGroup("humans")
        .addGroupResolver(UserListRoleResolver.class.getName())
          .addInputParameter("users", "john, james, admin")
      .addHuman("john")
      .addHumanTask("first", "john")
      .addHumanTask("last", "humans")
        .addFilter(HasPerformedTask.class.getName())
          .addInputParameter("taskNames", tasks)
      .addTransition("first", "last")
    .done();

    definition = getManagementAPI().deploy(getBusinessArchive(definition,
        null, UserListRoleResolver.class, HasPerformedTask.class));
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());

    loginAs("john", getPassword());
    executeTask(instanceUUID, "first");

    Set<ActivityInstance> activities = getQueryRuntimeAPI().getActivityInstances(instanceUUID, "last");
    assertEquals(1, activities.size());
    ActivityInstance activity = activities.iterator().next();
    assertEquals("last", activity.getActivityName());
    String user = activity.getTask().getTaskUser();
    assertEquals("john", user);

    getManagementAPI().deleteProcess(definition.getUUID());
  }
}
