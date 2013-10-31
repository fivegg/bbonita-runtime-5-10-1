package org.ow2.bonita.activity.multipleinstances.integration;

import java.util.Collection;
import java.util.Iterator;

import org.bonitasoft.connectors.bonita.joincheckers.GroovyJoinChecker;
import org.ow2.bonita.APITestCase;
import org.ow2.bonita.activity.multipleinstances.instantiator.NoContextMulitpleActivitiesInstantiator;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.TaskInstance;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.ProcessBuilder;

public class GroovyJoinCheckerTest extends APITestCase {

  public void testSimpleMulitpleActivities() throws Exception {
    ProcessDefinition definition =
      ProcessBuilder.createProcess("multiple", "1.0")
      .addHuman(getLogin())
      .addHumanTask("multi", getLogin())
        .addMultipleActivitiesInstantiator(NoContextMulitpleActivitiesInstantiator.class.getName())
          .addInputParameter("number", 10)
        .addMultipleActivitiesJoinChecker(GroovyJoinChecker.class.getName())
          .addInputParameter("script", "true")
          .addHumanTask("next", getLogin())
      .addTransition("multi", "next")
      .done();

    definition = getManagementAPI().deploy(getBusinessArchive(definition, null,
        NoContextMulitpleActivitiesInstantiator.class, GroovyJoinChecker.class));
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());
    Collection<TaskInstance> tasks = getQueryRuntimeAPI().getTaskList(getLogin(), ActivityState.READY);
    assertEquals(10, tasks.size());
    Iterator<TaskInstance> iterator = tasks.iterator();
    for (int i = 0; i < 1; i++) {
      TaskInstance task = iterator.next();
      executeTask(instanceUUID, task.getActivityName());
    }
    tasks = getQueryRuntimeAPI().getTaskList(getLogin(), ActivityState.READY);
    assertEquals(1, tasks.size());
    assertEquals("next", tasks.iterator().next().getActivityName());
    getManagementAPI().deleteProcess(definition.getUUID());
  }

}
