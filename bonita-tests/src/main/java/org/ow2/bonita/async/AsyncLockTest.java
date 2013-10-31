package org.ow2.bonita.async;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.QueryRuntimeAPI;
import org.ow2.bonita.facade.def.majorElement.ActivityDefinition.JoinType;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.exception.InstanceNotFoundException;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.light.LightActivityInstance;
import org.ow2.bonita.light.LightProcessInstance;
import org.ow2.bonita.util.AccessorUtil;
import org.ow2.bonita.util.Command;
import org.ow2.bonita.util.EnvTool;
import org.ow2.bonita.util.ProcessBuilder;

public class AsyncLockTest extends APITestCase {

  private void buildBranch(final ProcessBuilder processBuilder, final String prefix, final int nbOfSteps,
      final String incomingGate, final String outgoingGate) {
    for (int i = 1; i <= nbOfSteps; i++) {
      processBuilder.addSystemTask(prefix + i);
      processBuilder.asynchronous();
    }

    for (int i = 1; i < nbOfSteps; i++) {
      processBuilder.addTransition(prefix + i, prefix + (i + 1));
    }
    processBuilder.addTransition(incomingGate, prefix + "1");
    processBuilder.addTransition(prefix + nbOfSteps, outgoingGate);
  }

  public void testLockWorksWithSubProcesses() throws Exception {
    getCommandAPI().execute(new Command<Void>() {
      @Override
      public Void execute(final org.ow2.bonita.env.Environment environment) throws Exception {
        System.err.println("RETRIES: " + EnvTool.getEventExecutor().getRetries());
        return null;
      };

    });

    final ProcessBuilder processBuilder = ProcessBuilder.createProcess("asyncProcessWithAndGates", "1.0");
    processBuilder.addSystemTask("start");

    processBuilder.addDecisionNode("endAndGate").addJoinType(JoinType.AND);
    processBuilder.addTerminateEndEvent("end");
    processBuilder.addTransition("endAndGate", "end");

    final int nbOfBranches = 3;
    final int nbOfStepsPerBranch = 1;
    final int totalNbOfSteps = nbOfBranches * nbOfStepsPerBranch + 3;

    for (int i = 1; i <= nbOfBranches; i++) {
      buildBranch(processBuilder, "t" + i + "_", nbOfStepsPerBranch, "start", "endAndGate");
    }

    final ProcessDefinition process = getManagementAPI().deploy(
        getBusinessArchive(processBuilder.done(), null, WaitConnector.class));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    final long timeBeforeCheck = System.currentTimeMillis();
    final long endWait = timeBeforeCheck + 10000L;

    final QueryRuntimeAPI historyQueryRuntimeAPI = AccessorUtil.getQueryRuntimeAPI(AccessorUtil.QUERYLIST_HISTORY_KEY);
    final QueryRuntimeAPI journalQueryRuntimeAPI = AccessorUtil.getQueryRuntimeAPI(AccessorUtil.QUERYLIST_JOURNAL_KEY);

    final Set<ProcessInstanceUUID> rootInstanceUUIDs = new HashSet<ProcessInstanceUUID>();
    rootInstanceUUIDs.add(instanceUUID);

    LightProcessInstance instance = null;
    do {
      Thread.sleep(1000);
      try {
        instance = historyQueryRuntimeAPI.getLightProcessInstance(instanceUUID);
      } catch (final InstanceNotFoundException e) {
        // ignore
      }
      if (instance == null) {
        System.err.println("*****Instance with UUID " + instanceUUID + " not found after "
            + (System.currentTimeMillis() - timeBeforeCheck));
        final Set<LightActivityInstance> activities = journalQueryRuntimeAPI.getLightActivityInstances(instanceUUID);
        for (final LightActivityInstance activity : activities) {
          System.err.println("*****Activity " + activity.getActivityName() + " is in state " + activity.getState());
        }
      }
    } while (System.currentTimeMillis() < endWait && instance == null);

    assertNotNull(instance);
    final Collection<LightActivityInstance> activities = historyQueryRuntimeAPI.getLightActivityInstances(instanceUUID);
    final List<LightActivityInstance> sortedActivities = new ArrayList<LightActivityInstance>(activities);
    Collections.sort(sortedActivities, new Comparator<LightActivityInstance>() {
      @Override
      public int compare(final LightActivityInstance o1, final LightActivityInstance o2) {
        return o1.getActivityName().compareTo(o2.getActivityName());
      }
    });
    for (final LightActivityInstance activity : sortedActivities) {
      System.err.println("Activity " + activity.getActivityName() + " is in state " + activity.getState());
      assertEquals(ActivityState.FINISHED, activity.getState());
    }
    assertEquals(totalNbOfSteps, activities.size());

    getManagementAPI().deleteProcess(processUUID);
  }

}
