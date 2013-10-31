package org.ow2.bonita.activity.multipleinstances.integration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.bonitasoft.connectors.bonita.instantiators.VariableInstantiator;
import org.bonitasoft.connectors.bonita.joincheckers.FixedNumberJoinChecker;
import org.bonitasoft.connectors.bonita.resolvers.UserListRoleResolver;
import org.bonitasoft.connectors.legacy.VariablePerformerAssignFilter;
import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.TaskInstance;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.ProcessBuilder;

public class VariableInstantiatorTest extends APITestCase {

  public void testVariable() throws Exception {
    List<Object> values = new ArrayList<Object>();
    values.add(1);
    values.add(15);
    values.add(24);
    values.add(9);
    values.add(5);
    
    ProcessDefinition definition =
      ProcessBuilder.createProcess("mulitple", "1.0")
      .addHuman(getLogin())
      .addHumanTask("multi", getLogin())
        .addMultipleActivitiesInstantiator(VariableInstantiator.class.getName())
          .addInputParameter("name", "val")
          .addInputParameter("values", values)
        .addMultipleActivitiesJoinChecker(FixedNumberJoinChecker.class.getName())
          .addInputParameter("activityNumber", 3)
          .addHumanTask("next", getLogin())
      .addTransition("multi", "next")
      .done();

    definition = getManagementAPI().deploy(getBusinessArchive(definition, null,
        VariableInstantiator.class, FixedNumberJoinChecker.class));
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());
    Collection<TaskInstance> tasks = getQueryRuntimeAPI().getTaskList(getLogin(), ActivityState.READY);
    assertEquals(values.size(), tasks.size());
    Iterator<TaskInstance> iterator = tasks.iterator();
    for (int i = 0; i < 3; i++) {
      TaskInstance task = iterator.next();
      Integer value = (Integer) getQueryRuntimeAPI().getVariable(task.getUUID(), "val");
      assertTrue(value == 1 || value == 15 || value == 24 || value == 9 || value == 5);
      executeTask(instanceUUID, task.getActivityName());
    }
    tasks = getQueryRuntimeAPI().getTaskList(getLogin(), ActivityState.READY);
    assertEquals(1, tasks.size());
    assertEquals("next", tasks.iterator().next().getActivityName());
    getManagementAPI().deleteProcess(definition.getUUID());
  }

  public void testAssignAUSerForeachMultiInstantiateActivity() throws Exception {
    List<Object> values = new ArrayList<Object>();
    values.add("john");
    values.add("jack");
    values.add("james");
    
    ProcessDefinition definition = ProcessBuilder.createProcess("mulitple", "1.0")
      .addGroup("group")
        .addGroupResolver(UserListRoleResolver.class.getName())
          .addInputParameter("users", "john, jack, james")
      .addHumanTask("multi", "group")
        .addMultipleActivitiesInstantiator(VariableInstantiator.class.getName())
          .addInputParameter("name", "users")
          .addInputParameter("values", values)
        .addMultipleActivitiesJoinChecker(FixedNumberJoinChecker.class.getName())
          .addInputParameter("activityNumber", 3)
        .addFilter(VariablePerformerAssignFilter.class.getName())
          .addInputParameter("variableName", "${users}")
      .addHumanTask("next", getLogin())
      .addTransition("multi", "next")
      .done();

    definition = getManagementAPI().deploy(getBusinessArchive(definition, null,
        VariableInstantiator.class, FixedNumberJoinChecker.class, VariablePerformerAssignFilter.class, UserListRoleResolver.class));
    getRuntimeAPI().instantiateProcess(definition.getUUID());
    Collection<TaskInstance> tasks = getQueryRuntimeAPI().getTaskList(getLogin(), ActivityState.READY);
    assertEquals(0, tasks.size());
    tasks = getQueryRuntimeAPI().getTaskList("john", ActivityState.READY);
    assertEquals(1, tasks.size());
    tasks = getQueryRuntimeAPI().getTaskList("james", ActivityState.READY);
    assertEquals(1, tasks.size());
    tasks = getQueryRuntimeAPI().getTaskList("jack", ActivityState.READY);
    assertEquals(1, tasks.size());

    getManagementAPI().deleteProcess(definition.getUUID());
  }

}
