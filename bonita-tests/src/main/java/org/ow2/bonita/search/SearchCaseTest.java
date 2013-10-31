package org.ow2.bonita.search;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.QueryRuntimeAPI;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.exception.DeploymentException;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.InstanceState;
import org.ow2.bonita.facade.runtime.ProcessInstance;
import org.ow2.bonita.facade.runtime.TaskInstance;
import org.ow2.bonita.facade.runtime.impl.CaseImpl;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.light.LightActivityInstance;
import org.ow2.bonita.light.LightProcessInstance;
import org.ow2.bonita.search.index.ActivityInstanceIndex;
import org.ow2.bonita.search.index.CaseIndex;
import org.ow2.bonita.search.index.ProcessInstanceIndex;
import org.ow2.bonita.util.AccessorUtil;
import org.ow2.bonita.util.ProcessBuilder;

public class SearchCaseTest extends APITestCase {

  @Override
  protected void tearDown() throws Exception {
    getWebAPI().deletePhantomCases();
    getWebAPI().deleteAllCases();
    super.tearDown();
  }

  public void testSearchCaseUsingLabel() throws Exception {
    final ProcessDefinition yksi = ProcessBuilder.createProcess("yksi","1.0")
    .addHuman(getLogin())
    .addHumanTask("step1", getLogin())
    .done();

    getManagementAPI().deploy(getBusinessArchive(yksi));
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(yksi.getUUID());

    final String label = "starred";
    final Set<ProcessInstanceUUID> instanceUUIDs = new HashSet<ProcessInstanceUUID>();
    instanceUUIDs.add(instanceUUID);

    getWebAPI().addLabel(label, getLogin(), null, null, null, true, true, null, null, 0, true);
    getWebAPI().addCasesToLabel(getLogin(), label, instanceUUIDs);

    final SearchQueryBuilder query = new SearchQueryBuilder(new CaseIndex());
    query.criterion(CaseIndex.LABEL_NAME).equalsTo(label);
    final List<CaseImpl> cases = getQueryRuntimeAPI().search(query, 0, 10);
    assertEquals(1, cases.size());

    getManagementAPI().deleteProcess(yksi.getUUID());
  }

  public void testSearchCaseUsingOwner() throws Exception {
    final ProcessDefinition yksi = ProcessBuilder.createProcess("yksi","1.0")
    .addHuman(getLogin())
    .addHumanTask("step1", getLogin())
    .done();

    getManagementAPI().deploy(getBusinessArchive(yksi));
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(yksi.getUUID());

    final String label = "starred";
    final Set<ProcessInstanceUUID> instanceUUIDs = new HashSet<ProcessInstanceUUID>();
    instanceUUIDs.add(instanceUUID);

    getWebAPI().addLabel(label, getLogin(), null, null, null, true, true, null, null, 0, true);
    getWebAPI().addCasesToLabel(getLogin(), label, instanceUUIDs);

    final SearchQueryBuilder query = new SearchQueryBuilder(new CaseIndex());
    query.criterion(CaseIndex.OWNER_NAME).equalsTo(getLogin());
    final List<CaseImpl> cases = getQueryRuntimeAPI().search(query, 0, 10);
    assertEquals(1, cases.size());

    getManagementAPI().deleteProcess(yksi.getUUID());
  }

  public void testSearchCaseUsingInstanceUUID() throws Exception {
    final ProcessDefinition yksi = ProcessBuilder.createProcess("yksi","1.0")
    .addHuman(getLogin())
    .addHumanTask("step1", getLogin())
    .done();

    getManagementAPI().deploy(getBusinessArchive(yksi));
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(yksi.getUUID());

    final String label = "starred";
    final Set<ProcessInstanceUUID> instanceUUIDs = new HashSet<ProcessInstanceUUID>();
    instanceUUIDs.add(instanceUUID);

    getWebAPI().addLabel(label, getLogin(), null, null, null, true, true, null, null, 0, true);
    getWebAPI().addCasesToLabel(getLogin(), label, instanceUUIDs);

    final SearchQueryBuilder query = new SearchQueryBuilder(new CaseIndex());
    query.criterion(CaseIndex.PROCESS_INSTANCE_UUID).equalsTo(instanceUUID.getValue());
    final List<CaseImpl> cases = getQueryRuntimeAPI().search(query, 0, 10);
    assertEquals(1, cases.size());

    getWebAPI().deleteAllCases();
    getManagementAPI().deleteProcess(yksi.getUUID());
  }

  public void testSearchCaseUsingLabels() throws Exception {
    final ProcessDefinition yksi = ProcessBuilder.createProcess("yksi","1.0")
    .addHuman(getLogin())
    .addHumanTask("step1", getLogin())
    .done();

    final ProcessDefinition kaksi = ProcessBuilder.createProcess("kaksi","1.0")
    .addHuman(getLogin())
    .addHumanTask("step1", getLogin())
    .done();

    getManagementAPI().deploy(getBusinessArchive(yksi));
    final ProcessInstanceUUID yksiUUID = getRuntimeAPI().instantiateProcess(yksi.getUUID());
    getManagementAPI().deploy(getBusinessArchive(kaksi));
    final ProcessInstanceUUID kaksiUUID = getRuntimeAPI().instantiateProcess(kaksi.getUUID());

    final String starred = "starred";
    final String inbox = "inbox";
    Set<ProcessInstanceUUID> instanceUUIDs = new HashSet<ProcessInstanceUUID>();
    instanceUUIDs.add(yksiUUID);

    getWebAPI().addLabel(starred, getLogin(), null, null, null, true, true, null, null, 0, true);
    getWebAPI().addCasesToLabel(getLogin(), starred, instanceUUIDs);

    instanceUUIDs = new HashSet<ProcessInstanceUUID>();
    instanceUUIDs.add(yksiUUID);
    instanceUUIDs.add(kaksiUUID);
    getWebAPI().addLabel(inbox, getLogin(), null, null, null, true, true, null, null, 0, true);
    getWebAPI().addCasesToLabel(getLogin(), inbox, instanceUUIDs);

    SearchQueryBuilder query = new SearchQueryBuilder(new CaseIndex());
    query.criterion(CaseIndex.LABEL_NAME).equalsTo(inbox);
    List<CaseImpl> cases = getQueryRuntimeAPI().search(query, 0, 10);
    assertEquals(2, cases.size());

    query = new SearchQueryBuilder(new CaseIndex());
    query.criterion(CaseIndex.LABEL_NAME).equalsTo(starred);
    cases = getQueryRuntimeAPI().search(query, 0, 10);
    assertEquals(1, cases.size());

    getManagementAPI().deleteProcess(yksi.getUUID());
    getManagementAPI().deleteProcess(kaksi.getUUID());
  }

  public void testCase7975() throws Exception {
	    final ProcessDefinition process = ProcessBuilder.createProcess("case7975", "2.0")
	      .addHuman(getLogin())
	      .addSystemTask("Start")
	      .addHumanTask("Step1", getLogin())
	      .addHumanTask("Step2", getLogin())
	      .addHumanTask("DoNotExecute", getLogin())
	      .addHumanTask("WillBeCancelled", getLogin())
	      .addDecisionNode("XorGate")
	      .addTerminateEndEvent("End")

	      .addTransition("Start", "Step1")
	      .addTransition("Start", "DoNotExecute")
	      .addTransition("DoNotExecute", "XorGate")
	      .addTransition("Step2", "XorGate")
	      .addTransition("Step1", "WillBeCancelled")
	      .addTransition("Step1", "Step2")
	      .addTransition("WillBeCancelled", "End")
	    .done();

	    getManagementAPI().deploy(getBusinessArchive(process));

	    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());
	    ProcessInstance instance = getQueryRuntimeAPI().getProcessInstance(instanceUUID);
	    assertEquals(InstanceState.STARTED, instance.getInstanceState());

	    Collection<TaskInstance> tasks = getQueryRuntimeAPI().getTaskList(ActivityState.READY);
	    assertEquals(2, tasks.size());
	    
	    boolean found = false;
	    for (final TaskInstance task : tasks) {
	    	if ("Step1".equals(task.getActivityName())) {
	    		found = true;
	    		getRuntimeAPI().executeTask(task.getUUID(), true);
	    	}
	    }
	    assertTrue(found);
	    
	    tasks = getQueryRuntimeAPI().getTaskList(ActivityState.READY);
	    assertEquals(3, tasks.size());
	    
	    found = false;
	    for (final TaskInstance task : tasks) {
	    	if ("Step2".equals(task.getActivityName())) {
	    		found = true;
	    		getRuntimeAPI().executeTask(task.getUUID(), true);
	    	}
	    }
	    assertTrue(found);
	    
	    tasks = getQueryRuntimeAPI().getTaskList(ActivityState.READY);
	    assertEquals(1, tasks.size());
	    final TaskInstance task = tasks.iterator().next();
	    assertEquals("WillBeCancelled", task.getActivityName());

	    instance = getQueryRuntimeAPI().getProcessInstance(instanceUUID);
	    final QueryRuntimeAPI journalQueryRuntimeAPI = AccessorUtil.getQueryRuntimeAPI(AccessorUtil.QUERYLIST_JOURNAL_KEY);
	    
	    /*
	    final SearchQueryBuilder query1 = new SearchQueryBuilder(new ProcessInstanceIndex())
	    .leftParenthesis()
	    .criterion(ProcessInstanceIndex.STARTED_BY)
	    .startsWith(getLogin())
	    .rightParenthesis();
	    final List<LightProcessInstance> result1 = journalQueryRuntimeAPI.search(query1, 0, 10);
	    assertEquals(1, result1.size());
	    assertEquals(instance.getUUID(), result1.iterator().next().getUUID());
	    */
	    
	    final List<LightProcessInstance> result2 = getQueryRuntimeAPI().getLightParentProcessInstancesWithActiveUser(getLogin(), 0, 10);
	    
	    assertEquals(1, result2.size());
	    assertEquals(instance.getUUID(), result2.iterator().next().getUUID());
	    
	    getRuntimeAPI().assignTask(task.getUUID(), getLogin());	    
	    
	    final SearchQueryBuilder query4a = new SearchQueryBuilder(new ActivityInstanceIndex())
	    .leftParenthesis()
	    .criterion(ActivityInstanceIndex.USERID)
	    .startsWith(getLogin())
	    .rightParenthesis();
	    final List<LightActivityInstance> result4a = journalQueryRuntimeAPI.search(query4a, 0, 10);
	    assertEquals(3, result4a.size());
	    
	    final SearchQueryBuilder query4b = new SearchQueryBuilder(new ActivityInstanceIndex())
	    .leftParenthesis()
	    .criterion(ActivityInstanceIndex.CANDIDATE)
	    .startsWith(getLogin())
	    .rightParenthesis();
	    final List<LightActivityInstance> result4b = journalQueryRuntimeAPI.search(query4b, 0, 10);
	    assertEquals(4, result4b.size());
	    
	    final SearchQueryBuilder query = new SearchQueryBuilder(new ProcessInstanceIndex());
	    query.criterion(ProcessInstanceIndex.ACTIVITY + ActivityInstanceIndex.NAME).equalsTo("step1");
	    final List<LightProcessInstance> instances = journalQueryRuntimeAPI.search(query, 0, 10);
	    assertEquals(1, instances.size());
	    /*
	    final SearchQueryBuilder query3 = new SearchQueryBuilder(new ProcessInstanceIndex());
	    query3.criterion(ProcessInstanceIndex.ACTIVITY + ActivityInstanceIndex.CANDIDATE).startsWith(getLogin());
	    final List<LightProcessInstance> result3 = journalQueryRuntimeAPI.search(query3, 0, 10);
	    assertEquals(1, result3.size());
	    assertEquals(instance.getUUID(), result3.iterator().next().getUUID());
	    
	    final SearchQueryBuilder query4 = new SearchQueryBuilder(new ProcessInstanceIndex());
	    query4.criterion(ProcessInstanceIndex.ACTIVITY + ActivityInstanceIndex.USERID).startsWith(getLogin());
	    final List<LightProcessInstance> result4 = journalQueryRuntimeAPI.search(query4, 0, 10);
	    assertEquals(1, result4.size());
	    assertEquals(instance.getUUID(), result4.iterator().next().getUUID());
	    
	    final SearchQueryBuilder query5 = new SearchQueryBuilder(new ProcessInstanceIndex())
	    .leftParenthesis()
	    .criterion(ProcessInstanceIndex.ACTIVITY + ActivityInstanceIndex.USERID)
	    .startsWith(getLogin())
	    .rightParenthesis()
	    .or()
	    .leftParenthesis()
	    .criterion(ProcessInstanceIndex.ACTIVITY + ActivityInstanceIndex.CANDIDATE)
	    .startsWith(getLogin())
	    .rightParenthesis();
	    final List<LightProcessInstance> result5 = journalQueryRuntimeAPI.search(query5, 0, 10);
	    assertEquals(1, result5.size());
	    assertEquals(instance.getUUID(), result5.iterator().next().getUUID());
	    */
	    
	    getManagementAPI().deleteProcess(process.getUUID());
	  }
  
  public void testGetLightParentProcessInstancesWithActiveUserWithParallelTasks() throws Exception {
    final ProcessDefinition processDefinition = deployProcessWithTwoParallelTasksAssignedToUser(getLogin());

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processDefinition.getUUID());
    
    final List<LightProcessInstance> result = getQueryRuntimeAPI().getLightParentProcessInstancesWithActiveUser(getLogin(), 0, 10);
    
    assertEquals(1, result.size());
    assertEquals(instanceUUID, result.iterator().next().getUUID());
    getManagementAPI().deleteProcess(processDefinition.getUUID());
    
  }

  public void testGetNumberOfParentProcessInstancesWithActiveUserWithParallelTasks() throws Exception {
    final ProcessDefinition processDefinition = deployProcessWithTwoParallelTasksAssignedToUser(getLogin());
    
    getRuntimeAPI().instantiateProcess(processDefinition.getUUID());
    
    final Integer result = getQueryRuntimeAPI().getNumberOfParentProcessInstancesWithActiveUser(getLogin());
    
    assertEquals(new Integer(1), result);
    getManagementAPI().deleteProcess(processDefinition.getUUID());
    
  }

  private ProcessDefinition deployProcessWithTwoParallelTasksAssignedToUser(final String username) throws DeploymentException {
    final ProcessDefinition definition = ProcessBuilder.createProcess("proc", "1.0")
      .addHuman(username)
      .addHumanTask("step1", username)
      .addHumanTask("step2", username)
      .done();
    
    return getManagementAPI().deploy(getBusinessArchive(definition));
  }
  
  
}
