package org.ow2.bonita.activity.multipleinstances.integration;

import java.util.Collection;
import java.util.Iterator;

import org.bonitasoft.connectors.bonita.joincheckers.PercentageJoinChecker;
import org.ow2.bonita.APITestCase;
import org.ow2.bonita.activity.multipleinstances.instantiator.NoContextMulitpleActivitiesInstantiator;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.InstanceState;
import org.ow2.bonita.facade.runtime.TaskInstance;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.ProcessBuilder;

public class PercentageJoinCheckerTest extends APITestCase {

  public void testSubProcessPercentageWithVersion() throws Exception {
    subProcessPercentageWithVersion("1.0");
  }
  
  public void testSubProcessPercentageWithoutVersion() throws Exception {
    subProcessPercentageWithVersion(null);
  }
  
  private void subProcessPercentageWithVersion(final String version) throws Exception {
    ProcessBuilder builder = ProcessBuilder.createProcess("multiple", "1.0")
    .addHuman(getLogin());
    
    if (version == null) {
      builder.addSubProcess("multi", "sub");
    } else {
      builder.addSubProcess("multi", "sub", version);
    }
    
    ProcessDefinition parent = builder
        .addMultipleActivitiesInstantiator(NoContextMulitpleActivitiesInstantiator.class.getName())
          .addInputParameter("number", 4)
        .addMultipleActivitiesJoinChecker(PercentageJoinChecker.class.getName())
          .addInputParameter("percentage", 0.5)
      .done();
    
    ProcessDefinition sub =
      ProcessBuilder.createProcess("sub", "1.0")
      .addSystemTask("t")
      .done();

    sub = getManagementAPI().deploy(getBusinessArchive(sub));
    parent = getManagementAPI().deploy(getBusinessArchive(parent, getResourcesFromConnector(PercentageJoinChecker.class), NoContextMulitpleActivitiesInstantiator.class, PercentageJoinChecker.class));
    
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(parent.getUUID());
    assertEquals(InstanceState.FINISHED, getQueryRuntimeAPI().getLightProcessInstance(instanceUUID).getInstanceState());
    assertEquals(2, getQueryRuntimeAPI().getLightProcessInstances(sub.getUUID()).size());
    
    getManagementAPI().deleteProcess(parent.getUUID());
    getManagementAPI().deleteProcess(sub.getUUID());
  }
  
  public void testSimplePercentage() throws Exception {
    ProcessDefinition definition =
      ProcessBuilder.createProcess("multiple", "1.0")
      .addHuman(getLogin())
      .addHumanTask("multi", getLogin())
        .addMultipleActivitiesInstantiator(NoContextMulitpleActivitiesInstantiator.class.getName())
          .addInputParameter("number", 10)
        .addMultipleActivitiesJoinChecker(PercentageJoinChecker.class.getName())
          .addInputParameter("percentage", 0.3)
      .addHumanTask("next", getLogin())
      .addTransition("multi", "next")
      .done();

    definition = getManagementAPI().deploy(getBusinessArchive(definition,
        getResourcesFromConnector(PercentageJoinChecker.class),
        NoContextMulitpleActivitiesInstantiator.class, PercentageJoinChecker.class));
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());
    Collection<TaskInstance> tasks = getQueryRuntimeAPI().getTaskList(getLogin(), ActivityState.READY);
    assertEquals(10, tasks.size());
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

  public void testGroovyPercentage() throws Exception {
    ProcessDefinition definition =
      ProcessBuilder.createProcess("multiple", "1.0")
      .addHuman(getLogin())
      .addHumanTask("multi", getLogin())
        .addMultipleActivitiesInstantiator(NoContextMulitpleActivitiesInstantiator.class.getName())
          .addInputParameter("number", 10)
        .addMultipleActivitiesJoinChecker(PercentageJoinChecker.class.getName())
          .addInputParameter("percentage", "${30/100}")
      .addHumanTask("next", getLogin())
      .addTransition("multi", "next")
      .done();

    definition = getManagementAPI().deploy(getBusinessArchive(definition,
        getResourcesFromConnector(PercentageJoinChecker.class),
        NoContextMulitpleActivitiesInstantiator.class, PercentageJoinChecker.class));
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());
    Collection<TaskInstance> tasks = getQueryRuntimeAPI().getTaskList(getLogin(), ActivityState.READY);
    assertEquals(10, tasks.size());
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
