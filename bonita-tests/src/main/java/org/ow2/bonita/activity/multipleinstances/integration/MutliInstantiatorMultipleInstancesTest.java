package org.ow2.bonita.activity.multipleinstances.integration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bonitasoft.connectors.bonita.instantiators.FixedNumberInstantiator;
import org.bonitasoft.connectors.bonita.joincheckers.FixedNumberJoinChecker;
import org.bonitasoft.connectors.legacy.MultiInstantiatorInstantiator;
import org.bonitasoft.connectors.legacy.MultiInstantiatorJoinChecker;
import org.ow2.bonita.APITestCase;
import org.ow2.bonita.activity.multipleinstances.integration.multiinstantiator.MultiInstantiatorConnector;
import org.ow2.bonita.activity.multipleinstances.integration.multiinstantiator.SimpleMultiInstantiator;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.TaskInstance;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.ProcessBuilder;

public class MutliInstantiatorMultipleInstancesTest extends APITestCase {

  public void testWrappedMultiInstantiator() throws Exception {
    ProcessDefinition definition =
      ProcessBuilder.createProcess("legacy", "2.4")
      .addHuman(getLogin())
      .addHumanTask("multi", getLogin())
        .addMultipleActivitiesInstantiator(MultiInstantiatorInstantiator.class.getName())
          .addInputParameter("className", SimpleMultiInstantiator.class.getName())
          .addInputParameter("variableName", "value")
        .addMultipleActivitiesJoinChecker(MultiInstantiatorJoinChecker.class.getName())
          .addInputParameter("className", SimpleMultiInstantiator.class.getName())
      .addHumanTask("next", getLogin())
      .addTransition("multi", "next")
    .done();

    definition = getManagementAPI().deploy(getBusinessArchive(definition, null,
        FixedNumberInstantiator.class, FixedNumberJoinChecker.class));
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());
    Collection<TaskInstance> tasks = getQueryRuntimeAPI().getTaskList(getLogin(), ActivityState.READY);
    assertEquals(5, tasks.size());
    Iterator<TaskInstance> iterator = tasks.iterator();
    for (int i = 0; i < 3; i++) {
      TaskInstance task = iterator.next();
      executeTask(instanceUUID, task.getActivityName());
    }
    tasks = getQueryRuntimeAPI().getTaskList(getLogin(), ActivityState.READY);
    assertEquals(1, tasks.size());
    assertEquals("next", tasks.iterator().next().getActivityName());
    getManagementAPI().deleteProcess(definition.getUUID());
  }

  public void testWrappedMultiInstantiatorConnector() throws Exception {
    List<List<Object>> val = new ArrayList<List<Object>>();
    List<Object> one = new ArrayList<Object>();
    one.add("setNbInstances");
    one.add(5);
    val.add(one);

    Map<String, Object[]> values = new HashMap<String, Object[]>();
    values.put("setNbInstances", new Object[] {5});

    ProcessDefinition definition =
      ProcessBuilder.createProcess("legacy", "2.8")
      .addHuman(getLogin())
      .addHumanTask("multi", getLogin())
        .addMultipleActivitiesInstantiator(org.ow2.bonita.connector.core.MultiInstantiatorInstantiator.class.getName())
          .addInputParameter("className", MultiInstantiatorConnector.class.getName())
          .addInputParameter("variableName", "value")
          .addInputParameter("instantiatorParameters", val)
        .addMultipleActivitiesJoinChecker(org.ow2.bonita.connector.core.MultiInstantiatorJoinChecker.class.getName())
          .addInputParameter("className", MultiInstantiatorConnector.class.getName())
          .addInputParameter("instantiatorParameters", values)
      .addHumanTask("next", getLogin())
      .addTransition("multi", "next")
    .done();

    definition = getManagementAPI().deploy(getBusinessArchive(definition, null,
        MultiInstantiatorConnector.class, org.ow2.bonita.connector.core.MultiInstantiatorInstantiator.class, org.ow2.bonita.connector.core.MultiInstantiatorJoinChecker.class));
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());
    Collection<TaskInstance> tasks = getQueryRuntimeAPI().getTaskList(getLogin(), ActivityState.READY);
    assertEquals(5, tasks.size());
    Iterator<TaskInstance> iterator = tasks.iterator();
    for (int i = 0; i < 3; i++) {
      TaskInstance task = iterator.next();
      executeTask(instanceUUID, task.getActivityName());
    }
    tasks = getQueryRuntimeAPI().getTaskList(getLogin(), ActivityState.READY);
    assertEquals(1, tasks.size());
    assertEquals("next", tasks.iterator().next().getActivityName());
    getManagementAPI().deleteProcess(definition.getUUID());
  }

}
