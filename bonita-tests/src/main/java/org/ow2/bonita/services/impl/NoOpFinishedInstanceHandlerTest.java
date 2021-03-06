package org.ow2.bonita.services.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.ow2.bonita.APITestCase;
import org.ow2.bonita.env.generator.DbHistoryEnvGenerator;
import org.ow2.bonita.env.generator.EnvGenerator;
import org.ow2.bonita.facade.QueryRuntimeAPI;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.runtime.ProcessInstance;
import org.ow2.bonita.facade.runtime.impl.InternalProcessInstance;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.services.handlers.FinishedInstanceHandler;
import org.ow2.bonita.services.handlers.impl.NoOpFinishedInstanceHandler;
import org.ow2.bonita.util.AccessorUtil;
import org.ow2.bonita.util.BonitaException;
import org.ow2.bonita.util.ProcessBuilder;

public class NoOpFinishedInstanceHandlerTest extends APITestCase {

  public static class TestFinishedInstanceHandler implements FinishedInstanceHandler {
    private static List<ProcessInstanceUUID> finished = new ArrayList<ProcessInstanceUUID>();

    @Override
    public void handleFinishedInstance(final InternalProcessInstance instance) {
      finished.add(instance.getUUID());
    }
  }

  public static EnvGenerator getEnvGenerator() {
    final EnvGenerator envGenerator = new DbHistoryEnvGenerator();
    envGenerator.addApplicationEntry(EnvGenerator.getChainerEnvEntry(FinishedInstanceHandler.DEFAULT_KEY, "", true,
        NoOpFinishedInstanceHandler.class));
    return envGenerator;
  }

  public void testHistory() throws BonitaException {
    final ProcessDefinition definition = ProcessBuilder.createProcess("delete", "1.2").addSystemTask("auto").done();
    final ProcessDefinition processDefinition = getManagementAPI().deploy(getBusinessArchive(definition));
    final QueryRuntimeAPI historyQRAPI = AccessorUtil.getQueryRuntimeAPI(AccessorUtil.QUERYLIST_HISTORY_KEY);

    Set<ProcessInstance> processInstances = historyQRAPI.getProcessInstances();
    assertEquals(0, processInstances.size());

    getRuntimeAPI().instantiateProcess(processDefinition.getUUID());
    processInstances = historyQRAPI.getProcessInstances();
    assertEquals(0, processInstances.size());

    final QueryRuntimeAPI journalQAPI = AccessorUtil.getQueryRuntimeAPI(AccessorUtil.QUERYLIST_JOURNAL_KEY);
    processInstances = journalQAPI.getProcessInstances();
    assertEquals(1, processInstances.size());

    getManagementAPI().deleteAllProcesses();
  }

}
