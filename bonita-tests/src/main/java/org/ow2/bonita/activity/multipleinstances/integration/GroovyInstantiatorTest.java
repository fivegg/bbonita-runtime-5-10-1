package org.ow2.bonita.activity.multipleinstances.integration;

import java.util.Collection;
import java.util.Iterator;

import org.bonitasoft.connectors.bonita.instantiators.GroovyInstantiator;
import org.bonitasoft.connectors.bonita.joincheckers.FixedNumberJoinChecker;
import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.TaskInstance;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.ProcessBuilder;

public class GroovyInstantiatorTest extends APITestCase {

  public void testSimpleMultipleActivities() throws Exception {
    ProcessDefinition definition =
      ProcessBuilder.createProcess("mulitple", "1.0")
      .addHuman(getLogin())
      .addHumanTask("multi", getLogin())
        .addMultipleActivitiesInstantiator(GroovyInstantiator.class.getName())
          .addInputParameter("script", "def list = []\n(1..10).each {\n  list.add [:]\n}\nreturn list")
        .addMultipleActivitiesJoinChecker(FixedNumberJoinChecker.class.getName())
          .addInputParameter("activityNumber", 3)
        .addHumanTask("next", getLogin())
      .addTransition("multi", "next")
      .done();

    definition = getManagementAPI().deploy(getBusinessArchive(definition, null,
        GroovyInstantiator.class, FixedNumberJoinChecker.class));
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
