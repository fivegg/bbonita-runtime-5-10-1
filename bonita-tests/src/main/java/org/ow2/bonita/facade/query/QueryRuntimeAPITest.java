/**
 * Copyright (C) 2009  BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA  02110-1301, USA.
 **/
package org.ow2.bonita.facade.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.security.auth.login.LoginException;

import org.bonitasoft.connectors.bonita.resolvers.ProcessInitiatorRoleResolver;
import org.bonitasoft.connectors.bonita.resolvers.UserListRoleResolver;
import org.ow2.bonita.APITestCase;
import org.ow2.bonita.command.ChangeExpectedEndDateActitityInstance;
import org.ow2.bonita.facade.ManagementAPI;
import org.ow2.bonita.facade.QueryRuntimeAPI;
import org.ow2.bonita.facade.RuntimeAPI;
import org.ow2.bonita.facade.def.majorElement.ActivityDefinition.JoinType;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.exception.BonitaInternalException;
import org.ow2.bonita.facade.exception.DeploymentException;
import org.ow2.bonita.facade.exception.InstanceNotFoundException;
import org.ow2.bonita.facade.exception.ProcessNotFoundException;
import org.ow2.bonita.facade.exception.VariableNotFoundException;
import org.ow2.bonita.facade.paging.ActivityInstanceCriterion;
import org.ow2.bonita.facade.paging.ProcessInstanceCriterion;
import org.ow2.bonita.facade.runtime.ActivityInstance;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.InstanceState;
import org.ow2.bonita.facade.runtime.ProcessInstance;
import org.ow2.bonita.facade.runtime.TaskInstance;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.light.LightActivityInstance;
import org.ow2.bonita.light.LightProcessDefinition;
import org.ow2.bonita.light.LightProcessInstance;
import org.ow2.bonita.light.LightTaskInstance;
import org.ow2.bonita.light.impl.LightActivityInstanceImpl;
import org.ow2.bonita.util.BonitaConstants;
import org.ow2.bonita.util.BonitaException;
import org.ow2.bonita.util.BusinessArchiveFactory;
import org.ow2.bonita.util.DateUtil;
import org.ow2.bonita.util.Misc;
import org.ow2.bonita.util.ProcessBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * @author Charles Souillard, Matthieu Chaffotte, Elias Ricken de Medeiros
 */
public class QueryRuntimeAPITest extends APITestCase {

  public void testGetTaskCandidates() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("myProcess", "1.0").addHuman(getLogin()).addHuman("a")
        .addHuman("b").addHuman("c").addHumanTask("t1", getLogin(), "a", "b").addHumanTask("t2", getLogin(), "b", "c")
        .done();

    process = getManagementAPI().deploy(getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    getRuntimeAPI().instantiateProcess(processUUID);

    final Set<String> t1Candidates = new HashSet<String>();
    t1Candidates.add(getLogin());
    t1Candidates.add("a");
    t1Candidates.add("b");

    final Set<String> t2Candidates = new HashSet<String>();
    t2Candidates.add(getLogin());
    t2Candidates.add("c");
    t2Candidates.add("b");

    final Collection<LightTaskInstance> tasks = getQueryRuntimeAPI().getLightTaskList(ActivityState.READY);
    assertNotNull(tasks);
    assertEquals(2, tasks.size());
    final Iterator<LightTaskInstance> it = tasks.iterator();
    final LightTaskInstance task1 = it.next();
    final LightTaskInstance task2 = it.next();
    final ActivityInstanceUUID task1UUID = task1.getUUID();
    final ActivityInstanceUUID task2UUID = task2.getUUID();

    if ("t1".equals(task1.getActivityName())) {
      assertEquals(t1Candidates, getQueryRuntimeAPI().getTaskCandidates(task1UUID));
      assertEquals(t2Candidates, getQueryRuntimeAPI().getTaskCandidates(task2UUID));
    } else {
      assertEquals(t2Candidates, getQueryRuntimeAPI().getTaskCandidates(task1UUID));
      assertEquals(t1Candidates, getQueryRuntimeAPI().getTaskCandidates(task2UUID));
    }

    final Set<ActivityInstanceUUID> uuids = new HashSet<ActivityInstanceUUID>();
    uuids.add(task1UUID);
    uuids.add(task2UUID);
    final Map<ActivityInstanceUUID, Set<String>> map = new HashMap<ActivityInstanceUUID, Set<String>>();

    if ("t1".equals(task1.getActivityName())) {
      map.put(task1UUID, t1Candidates);
      map.put(task2UUID, t2Candidates);
    } else {
      map.put(task1UUID, t2Candidates);
      map.put(task2UUID, t1Candidates);
    }

    assertEquals(map, getQueryRuntimeAPI().getTaskCandidates(uuids));

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetLightTaskList() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("myProcess", "1.0").addHuman(getLogin())
        .addHumanTask("t", getLogin()).done();

    process = getManagementAPI().deploy(getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    final Collection<ActivityState> states = new HashSet<ActivityState>();
    states.add(ActivityState.READY);

    checkIsLightTaskInstance(getQueryRuntimeAPI().getLightTaskList(ActivityState.READY));
    checkIsLightTaskInstance(getQueryRuntimeAPI().getLightTaskList(instanceUUID, ActivityState.READY));
    checkIsLightTaskInstance(getQueryRuntimeAPI().getLightTaskList(instanceUUID, states));
    checkIsLightTaskInstance(getQueryRuntimeAPI().getLightTaskList(getLogin(), ActivityState.READY));
    checkIsLightTaskInstance(getQueryRuntimeAPI().getLightTaskList(instanceUUID, getLogin(), ActivityState.READY));

    getManagementAPI().deleteProcess(processUUID);
  }

  private void checkIsLightTaskInstance(final Collection<LightTaskInstance> tasks) throws Exception {
    assertNotNull(tasks);
    assertEquals(1, tasks.size());
    final LightTaskInstance task = tasks.iterator().next();
    assertEquals(LightActivityInstanceImpl.class, task.getClass());
  }

  public void testDynamicLabelsAndDescriptions() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("myProcess", "1.0").addStringData("processVar", "pValue")
        .addSystemTask("a").addDynamicDescription("${true}").addDynamicLabel("${true}").addSystemTask("b")
        .addDynamicDescription("${\"xx\" + processVar}").addDynamicLabel("${\"xx\" + processVar}").addSystemTask("c")
        .addDynamicDescription("${\"xx\" + localVar}").addDynamicLabel("${\"xx\" + localVar}")
        .addStringData("localVar", "lValue1").addSystemTask("d")
        .addDynamicDescription("${\"xx\" + processVar + localVar}")
        .addDynamicLabel("${\"xx\" + processVar + localVar}").addStringData("localVar", "lValue2")
        .addTransition("a", "b").addTransition("b", "c").addTransition("c", "d").done();

    process = getManagementAPI().deploy(getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    final ProcessInstance instance = getQueryRuntimeAPI().getProcessInstance(instanceUUID);

    assertEquals(InstanceState.FINISHED, instance.getInstanceState());
    assertEquals(4, instance.getActivities().size());

    final ActivityInstance a = instance.getActivities("a").iterator().next();
    final ActivityInstance b = instance.getActivities("b").iterator().next();
    final ActivityInstance c = instance.getActivities("c").iterator().next();
    final ActivityInstance d = instance.getActivities("d").iterator().next();

    assertEquals("true", a.getDynamicDescription());
    assertEquals("true", a.getDynamicLabel());

    assertEquals("xxpValue", b.getDynamicDescription());
    assertEquals("xxpValue", b.getDynamicLabel());

    assertEquals("xxlValue1", c.getDynamicDescription());
    assertEquals("xxlValue1", c.getDynamicLabel());

    assertEquals("xxpValuelValue2", d.getDynamicDescription());
    assertEquals("xxpValuelValue2", d.getDynamicLabel());

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testQuerySplitting() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("my", "22.33").addSystemTask("t1").done();

    process = getManagementAPI().deploy(getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final Collection<ProcessInstanceUUID> instanceUUIDs = new HashSet<ProcessInstanceUUID>();
    final int size = BonitaConstants.MAX_QUERY_SIZE + 2;

    for (int i = 0; i < size; i++) {
      instanceUUIDs.add(getRuntimeAPI().instantiateProcess(processUUID));
    }
    assertEquals(size, getQueryRuntimeAPI().getLightProcessInstances(instanceUUIDs).size());

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetProcessInstancesSubSet() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("my", "22.33").addSystemTask("t1").done();

    process = getManagementAPI().deploy(getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final Collection<ProcessInstanceUUID> instanceUUIDs = new HashSet<ProcessInstanceUUID>();
    instanceUUIDs.add(getRuntimeAPI().instantiateProcess(processUUID));
    instanceUUIDs.add(getRuntimeAPI().instantiateProcess(processUUID));
    instanceUUIDs.add(getRuntimeAPI().instantiateProcess(processUUID));

    getRuntimeAPI().instantiateProcess(processUUID);

    assertEquals(3, getQueryRuntimeAPI().getProcessInstances(instanceUUIDs).size());

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetNumberOf() throws Exception {
    ProcessDefinition process = getProcessWithASubprocess();

    ProcessDefinition subProcess = getSuprocess();

    subProcess = getManagementAPI().deploy(getBusinessArchive(subProcess));
    process = getManagementAPI().deploy(getBusinessArchive(process));

    final ProcessDefinitionUUID parentProcessUUID = process.getUUID();

    assertEquals(2, getQueryDefinitionAPI().getNumberOfProcesses());

    getRuntimeAPI().instantiateProcess(parentProcessUUID);

    assertEquals(2, getQueryRuntimeAPI().getNumberOfProcessInstances());

    getRuntimeAPI().instantiateProcess(parentProcessUUID);
    assertEquals(4, getQueryRuntimeAPI().getNumberOfProcessInstances());

    assertEquals(2, getQueryRuntimeAPI().getNumberOfParentProcessInstances());

    final Set<ProcessDefinitionUUID> processUUIDs = new HashSet<ProcessDefinitionUUID>();
    assertEquals(0, getQueryRuntimeAPI().getNumberOfParentProcessInstances(processUUIDs));
    assertEquals(2, getQueryRuntimeAPI().getNumberOfParentProcessInstancesExcept(processUUIDs));
    processUUIDs.add(process.getUUID());
    assertEquals(2, getQueryRuntimeAPI().getNumberOfParentProcessInstances(processUUIDs));
    assertEquals(0, getQueryRuntimeAPI().getNumberOfParentProcessInstancesExcept(processUUIDs));

    // Archive one instance and checks numbers are the same
    Collection<TaskInstance> tasks = getQueryRuntimeAPI().getTaskList(ActivityState.READY);
    assertEquals(2, tasks.size());
    TaskInstance t = tasks.iterator().next();
    getRuntimeAPI().executeTask(t.getUUID(), true);
    assertEquals(InstanceState.FINISHED, getQueryRuntimeAPI().getProcessInstance(t.getProcessInstanceUUID())
        .getInstanceState());
    assertEquals(InstanceState.FINISHED, getQueryRuntimeAPI().getProcessInstance(t.getRootInstanceUUID())
        .getInstanceState());

    tasks = getQueryRuntimeAPI().getTaskList(ActivityState.READY);
    assertEquals(1, tasks.size());
    t = tasks.iterator().next();

    assertEquals(InstanceState.STARTED, getQueryRuntimeAPI().getProcessInstance(t.getProcessInstanceUUID())
        .getInstanceState());
    assertEquals(InstanceState.STARTED, getQueryRuntimeAPI().getProcessInstance(t.getRootInstanceUUID())
        .getInstanceState());

    assertEquals(4, getQueryRuntimeAPI().getNumberOfProcessInstances());

    assertEquals(2, getQueryRuntimeAPI().getNumberOfParentProcessInstances());

    getRuntimeAPI().executeTask(t.getUUID(), true);
    assertEquals(InstanceState.FINISHED, getQueryRuntimeAPI().getProcessInstance(t.getProcessInstanceUUID())
        .getInstanceState());
    assertEquals(InstanceState.FINISHED, getQueryRuntimeAPI().getProcessInstance(t.getRootInstanceUUID())
        .getInstanceState());

    getManagementAPI().disable(parentProcessUUID);
    getManagementAPI().archive(parentProcessUUID);
    assertEquals(2, getQueryDefinitionAPI().getNumberOfProcesses());

    getManagementAPI().disable(subProcess.getUUID());
    assertEquals(2, getQueryDefinitionAPI().getNumberOfProcesses());

    getManagementAPI().deleteAllProcesses();
  }

  public void testGetActivitiesFromRootInstance() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("parentProcess", "1.0").addHuman(getLogin())
        .addSystemTask("s").addSubProcess("aSubProcess", "aSubProcess").addHumanTask("t1", getLogin())
        .addJoinType(JoinType.AND).addHumanTask("t2", getLogin()).addTransition("s", "t1")
        .addTransition("aSubProcess", "t1").done();

    ProcessDefinition subProcess = ProcessBuilder.createProcess("aSubProcess", "1.0").addHuman(getLogin())
        .addSystemTask("subs").addHumanTask("subt1", getLogin()).addSystemTask("subs2").addTransition("subs", "subt1")
        .addTransition("subt1", "subs2").done();

    subProcess = getManagementAPI().deploy(getBusinessArchive(subProcess));
    process = getManagementAPI().deploy(getBusinessArchive(process));

    final ProcessDefinitionUUID processUUID = process.getUUID();

    final ProcessInstanceUUID parentInstanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    final ProcessInstance parentInstance = getQueryRuntimeAPI().getProcessInstance(parentInstanceUUID);
    assertEquals(3, parentInstance.getActivities().size());
    assertEquals(1, parentInstance.getChildrenInstanceUUID().size());
    final ProcessInstanceUUID childInstanceUUID = parentInstance.getChildrenInstanceUUID().iterator().next();
    final ProcessInstance childInstance = getQueryRuntimeAPI().getProcessInstance(childInstanceUUID);
    assertEquals(2, childInstance.getActivities().size());

    final List<LightActivityInstance> activities = getQueryRuntimeAPI().getLightActivityInstancesFromRoot(
        parentInstanceUUID);
    final List<LightTaskInstance> tasks = getQueryRuntimeAPI().getLightTaskInstancesFromRoot(parentInstanceUUID);

    assertNotNull(activities);
    assertNotNull(tasks);

    assertEquals(5, activities.size());
    assertEquals(2, tasks.size());

    getManagementAPI().deleteProcess(processUUID);
    getManagementAPI().deleteProcess(subProcess.getUUID());
  }

  public void testGetOneTask() throws BonitaException {
    ProcessDefinition process = ProcessBuilder.createProcess("parentProcess", "1.0").addHuman(getLogin())
        .addHumanTask("t", getLogin()).done();

    process = getManagementAPI().deploy(getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    assertNull(getQueryRuntimeAPI().getOneTask(processUUID, ActivityState.READY));

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    assertNotNull(getQueryRuntimeAPI().getOneTask(processUUID, ActivityState.READY));

    // check instance
    assertNotNull(getQueryRuntimeAPI().getOneTask(instanceUUID, ActivityState.READY));

    // check global
    assertNotNull(getQueryRuntimeAPI().getOneTask(ActivityState.READY));

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testRootInstanceUUID() throws BonitaException {
    final ProcessDefinition parentProcess = ProcessBuilder.createProcess("parentProcess", "1.0")
        .addSubProcess("aSubProcess", "aSubProcess").done();

    final ProcessDefinition subProcess = ProcessBuilder.createProcess("aSubProcess", "1.0").addHuman(getLogin())
        .addSubProcess("zSubSubProcess", "zSubSubProcess").done();

    final ProcessDefinition subSubProcess = ProcessBuilder.createProcess("zSubSubProcess", "1.0").addHuman(getLogin())
        .addHumanTask("t", getLogin()).done();

    getManagementAPI().deploy(getBusinessArchive(subSubProcess));
    getManagementAPI().deploy(getBusinessArchive(subProcess));
    getManagementAPI().deploy(getBusinessArchive(parentProcess));

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(parentProcess.getUUID());

    final ProcessInstance parentInstance = getQueryRuntimeAPI().getProcessInstance(instanceUUID);
    assertEquals(1, getQueryRuntimeAPI().getProcessInstances(subProcess.getUUID()).size());
    final ProcessInstance child1ProcessInstance = getQueryRuntimeAPI().getProcessInstances(subProcess.getUUID())
        .iterator().next();
    assertEquals(1, getQueryRuntimeAPI().getProcessInstances(subSubProcess.getUUID()).size());
    final ProcessInstance child2ProcessInstance = getQueryRuntimeAPI().getProcessInstances(subSubProcess.getUUID())
        .iterator().next();

    assertEquals(instanceUUID, parentInstance.getRootInstanceUUID());
    assertEquals(instanceUUID, child1ProcessInstance.getRootInstanceUUID());
    assertEquals(instanceUUID, child2ProcessInstance.getRootInstanceUUID());

    Set<ActivityInstance> activities = null;

    activities = getQueryRuntimeAPI().getActivityInstances(parentInstance.getUUID());
    assertEquals(1, activities.size());
    final ActivityInstance parentActivity = activities.iterator().next();

    activities = getQueryRuntimeAPI().getActivityInstances(child1ProcessInstance.getUUID());
    assertEquals(1, activities.size());
    final ActivityInstance child1Activity = activities.iterator().next();

    activities = getQueryRuntimeAPI().getActivityInstances(child2ProcessInstance.getUUID());
    assertEquals(1, activities.size());
    final ActivityInstance child2Activity = activities.iterator().next();

    assertEquals(instanceUUID, parentActivity.getRootInstanceUUID());
    assertEquals(instanceUUID, child1Activity.getRootInstanceUUID());
    assertEquals(instanceUUID, child2Activity.getRootInstanceUUID());

    getManagementAPI().deleteAllProcesses();
  }

  public void testGetProcessInstancesPaging() throws Exception {
    final ProcessDefinition process = ProcessBuilder.createProcess("p", "5.0").addHuman(getLogin())
        .addHumanTask("task", getLogin()).done();
    final ProcessDefinitionUUID processUUID = process.getUUID();

    getManagementAPI().deploy(getBusinessArchive(process));
    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();

    final int instanceNumber = 10;
    instanciateProcesses(processUUID, instanceNumber, instanceUUIDs);
    Collections.reverse(instanceUUIDs);

    checkInstanceListContent(0, 5, 5, instanceUUIDs, null, ProcessInstanceCriterion.DEFAULT);
    checkInstanceListContent(3, 3, 3, instanceUUIDs, null, ProcessInstanceCriterion.DEFAULT);
    checkInstanceListContent(5, 8, 5, instanceUUIDs, null, ProcessInstanceCriterion.DEFAULT);
    checkInstanceListContent(10, 20, 0, instanceUUIDs, null, ProcessInstanceCriterion.DEFAULT);

    final List<ProcessInstanceUUID> filter = Misc.subList(ProcessInstanceUUID.class, instanceUUIDs, 0, 7);
    assertEquals(7, filter.size());

    checkInstanceListContent(0, 5, 5, instanceUUIDs, filter, ProcessInstanceCriterion.DEFAULT);
    checkInstanceListContent(5, 5, 2, instanceUUIDs, filter, ProcessInstanceCriterion.DEFAULT);
    checkInstanceListContent(7, 20, 0, instanceUUIDs, filter, ProcessInstanceCriterion.DEFAULT);

    getRuntimeAPI().deleteAllProcessInstances(processUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetProcessInstancesPagingJournalVsHistory() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("theProcess", "1.0").addHuman(getLogin())
        .addHumanTask("t", getLogin()).done();
    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final List<ProcessInstanceUUID> filter = new ArrayList<ProcessInstanceUUID>();
    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();

    final int pageSize = 20;

    final int initialInstanceCount = 41;
    for (int i = 0; i < initialInstanceCount; i++) {
      final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
      instanceUUIDs.add(instanceUUID);
      if (i % 2 == 0) {
        filter.add(instanceUUID);
      }
      Thread.sleep(10);
    }
    Collections.reverse(filter);
    Collections.reverse(instanceUUIDs);

    // check all instances are available
    assertEquals(initialInstanceCount, getQueryRuntimeAPI().getProcessInstances().size());

    // check that cases are correctly splitted in pages
    assertEquals(pageSize, getQueryRuntimeAPI().getLightProcessInstances(0, pageSize).size());
    assertEquals(pageSize, getQueryRuntimeAPI().getLightProcessInstances(pageSize, pageSize).size());
    assertEquals(initialInstanceCount - 2 * pageSize,
        getQueryRuntimeAPI().getLightProcessInstances(2 * pageSize, pageSize).size());

    checkInstanceListContent(0, 5, 5, instanceUUIDs, filter, ProcessInstanceCriterion.DEFAULT);
    checkInstanceListContent(5, 5, 5, instanceUUIDs, filter, ProcessInstanceCriterion.DEFAULT);
    checkInstanceListContent(0, 21, 21, instanceUUIDs, filter, ProcessInstanceCriterion.DEFAULT);
    checkInstanceListContent(0, 22, 21, instanceUUIDs, filter, ProcessInstanceCriterion.DEFAULT);
    checkInstanceListContent(0, 50, 21, instanceUUIDs, filter, ProcessInstanceCriterion.DEFAULT);

    // ends an instance so that one is moved to History
    final Collection<TaskInstance> tasks = getQueryRuntimeAPI().getTaskList(filter.get(4), ActivityState.READY);
    assertEquals(1, tasks.size());
    final TaskInstance taskToExecute = tasks.iterator().next();
    getRuntimeAPI().executeTask(taskToExecute.getUUID(), true);
    final ProcessInstanceUUID instanceUUID = taskToExecute.getProcessInstanceUUID();
    assertEquals(InstanceState.FINISHED, getQueryRuntimeAPI().getLightProcessInstance(instanceUUID).getInstanceState());

    // check that cases are correctly splitted in pages
    assertEquals(pageSize, getQueryRuntimeAPI().getLightProcessInstances(0, pageSize).size());
    assertEquals(pageSize, getQueryRuntimeAPI().getLightProcessInstances(pageSize, pageSize).size());
    assertEquals(initialInstanceCount - 2 * pageSize,
        getQueryRuntimeAPI().getLightProcessInstances(2 * pageSize, pageSize).size());

    filter.remove(instanceUUID);
    filter.add(0, instanceUUID);
    instanceUUIDs.remove(instanceUUID);
    instanceUUIDs.add(0, instanceUUID);
    checkInstanceListContent(0, 5, 5, instanceUUIDs, filter, ProcessInstanceCriterion.DEFAULT);
    checkInstanceListContent(5, 5, 5, instanceUUIDs, filter, ProcessInstanceCriterion.DEFAULT);
    checkInstanceListContent(0, 21, 21, instanceUUIDs, filter, ProcessInstanceCriterion.DEFAULT);
    checkInstanceListContent(0, 22, 21, instanceUUIDs, filter, ProcessInstanceCriterion.DEFAULT);
    checkInstanceListContent(0, 50, 21, instanceUUIDs, filter, ProcessInstanceCriterion.DEFAULT);

    getRuntimeAPI().deleteAllProcessInstances(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetProcessInstancesPagingJournalVsHistory2() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("theProcess", "1.0").addHuman(getLogin())
        .addHumanTask("t", getLogin()).done();
    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final int pageSize = 2;
    final ProcessInstanceUUID instance1 = getRuntimeAPI().instantiateProcess(processUUID);
    executeTask(instance1, "t");
    Thread.sleep(10);
    final ProcessInstanceUUID instance2 = getRuntimeAPI().instantiateProcess(processUUID);
    Thread.sleep(10);
    final ProcessInstanceUUID instance3 = getRuntimeAPI().instantiateProcess(processUUID);
    executeTask(instance3, "t");
    Thread.sleep(10);
    final ProcessInstanceUUID instance4 = getRuntimeAPI().instantiateProcess(processUUID);
    Thread.sleep(10);
    final ProcessInstanceUUID instance5 = getRuntimeAPI().instantiateProcess(processUUID);
    executeTask(instance5, "t");
    Thread.sleep(10);
    final ProcessInstanceUUID instance6 = getRuntimeAPI().instantiateProcess(processUUID);
    Thread.sleep(10);
    final ProcessInstanceUUID instance7 = getRuntimeAPI().instantiateProcess(processUUID);
    executeTask(instance7, "t");
    Thread.sleep(10);
    final ProcessInstanceUUID instance8 = getRuntimeAPI().instantiateProcess(processUUID);

    // check all instances are available
    assertEquals(8, getQueryRuntimeAPI().getLightProcessInstances().size());

    // check finished instances
    assertEquals(InstanceState.FINISHED, getQueryRuntimeAPI().getLightProcessInstance(instance1).getInstanceState());
    assertEquals(InstanceState.FINISHED, getQueryRuntimeAPI().getLightProcessInstance(instance3).getInstanceState());
    assertEquals(InstanceState.FINISHED, getQueryRuntimeAPI().getLightProcessInstance(instance5).getInstanceState());
    assertEquals(InstanceState.FINISHED, getQueryRuntimeAPI().getLightProcessInstance(instance7).getInstanceState());

    final List<ProcessInstanceUUID> expectedInstanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    expectedInstanceUUIDs.add(instance8);
    expectedInstanceUUIDs.add(instance7);
    expectedInstanceUUIDs.add(instance6);
    expectedInstanceUUIDs.add(instance5);
    expectedInstanceUUIDs.add(instance4);
    expectedInstanceUUIDs.add(instance3);
    expectedInstanceUUIDs.add(instance2);
    expectedInstanceUUIDs.add(instance1);

    // journal contains instances: 1, 3, 5, 7. history contains 2, 4, 6, 8.
    // instances are ordered
    checkInstanceListContent(0, pageSize, pageSize, expectedInstanceUUIDs, null, ProcessInstanceCriterion.DEFAULT);
    checkInstanceListContent(2, pageSize, pageSize, expectedInstanceUUIDs, null, ProcessInstanceCriterion.DEFAULT);
    checkInstanceListContent(4, pageSize, pageSize, expectedInstanceUUIDs, null, ProcessInstanceCriterion.DEFAULT);
    checkInstanceListContent(6, pageSize, pageSize, expectedInstanceUUIDs, null, ProcessInstanceCriterion.DEFAULT);
    checkInstanceListContent(0, 8, 8, expectedInstanceUUIDs, null, ProcessInstanceCriterion.DEFAULT);

    getRuntimeAPI().deleteAllProcessInstances(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetLightProcessInstancesPagingOrderByLastUpdateAsc() throws Exception {
    final ProcessDefinition process = getProcess();
    final ProcessDefinitionUUID processUUID = process.getUUID();

    getManagementAPI().deploy(getBusinessArchive(process));
    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();

    final int instanceNumber = 10;
    instanciateProcesses(processUUID, instanceNumber, instanceUUIDs);

    updateInstanceVariable(instanceUUIDs, "text", "Hello", true);

    Collections.reverse(instanceUUIDs);
    checkInstanceListContent(0, 5, 5, instanceUUIDs, null, ProcessInstanceCriterion.LAST_UPDATE_ASC);
    checkInstanceListContent(3, 3, 3, instanceUUIDs, null, ProcessInstanceCriterion.LAST_UPDATE_ASC);
    checkInstanceListContent(5, 8, 5, instanceUUIDs, null, ProcessInstanceCriterion.LAST_UPDATE_ASC);
    checkInstanceListContent(10, 20, 0, instanceUUIDs, null, ProcessInstanceCriterion.LAST_UPDATE_ASC);

    final List<ProcessInstanceUUID> filter = Misc.subList(ProcessInstanceUUID.class, instanceUUIDs, 0, 7);
    assertEquals(7, filter.size());

    checkInstanceListContent(0, 5, 5, instanceUUIDs, filter, ProcessInstanceCriterion.LAST_UPDATE_ASC);
    checkInstanceListContent(5, 5, 2, instanceUUIDs, filter, ProcessInstanceCriterion.LAST_UPDATE_ASC);
    checkInstanceListContent(7, 20, 0, instanceUUIDs, filter, ProcessInstanceCriterion.LAST_UPDATE_ASC);

    getRuntimeAPI().deleteAllProcessInstances(processUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetLightProcessInstancesPagingOrderByStartedDateAsc() throws Exception {
    final ProcessDefinition process = getProcess();
    final ProcessDefinitionUUID processUUID = process.getUUID();

    getManagementAPI().deploy(getBusinessArchive(process));
    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();

    final int instanceNumber = 10;
    instanciateProcesses(processUUID, instanceNumber, instanceUUIDs);

    checkInstanceListContent(0, 5, 5, instanceUUIDs, null, ProcessInstanceCriterion.STARTED_DATE_ASC);
    checkInstanceListContent(3, 3, 3, instanceUUIDs, null, ProcessInstanceCriterion.STARTED_DATE_ASC);
    checkInstanceListContent(5, 8, 5, instanceUUIDs, null, ProcessInstanceCriterion.STARTED_DATE_ASC);
    checkInstanceListContent(10, 20, 0, instanceUUIDs, null, ProcessInstanceCriterion.STARTED_DATE_ASC);

    final List<ProcessInstanceUUID> filter = Misc.subList(ProcessInstanceUUID.class, instanceUUIDs, 0, 7);
    assertEquals(7, filter.size());

    checkInstanceListContent(0, 5, 5, instanceUUIDs, filter, ProcessInstanceCriterion.STARTED_DATE_ASC);
    checkInstanceListContent(5, 5, 2, instanceUUIDs, filter, ProcessInstanceCriterion.STARTED_DATE_ASC);
    checkInstanceListContent(7, 20, 0, instanceUUIDs, filter, ProcessInstanceCriterion.STARTED_DATE_ASC);

    getRuntimeAPI().deleteAllProcessInstances(processUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetLightProcessInstancesPagingOrderByEndedDateAsc() throws Exception {
    final ProcessDefinition process = getProcess();
    final ProcessDefinitionUUID processUUID = process.getUUID();

    getManagementAPI().deploy(getBusinessArchive(process));
    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();

    final int instanceNumber = 10;
    instanciateProcesses(processUUID, instanceNumber, instanceUUIDs);

    Collections.rotate(instanceUUIDs, 3);
    for (int i = 0; i < instanceNumber; i++) {
      final ProcessInstanceUUID instanceUUID = instanceUUIDs.get(i);
      executeTask(instanceUUID, "step1");
      Thread.sleep(3);
    }

    checkInstanceListContent(0, 5, 5, instanceUUIDs, null, ProcessInstanceCriterion.ENDED_DATE_ASC);
    checkInstanceListContent(3, 3, 3, instanceUUIDs, null, ProcessInstanceCriterion.ENDED_DATE_ASC);
    checkInstanceListContent(5, 8, 5, instanceUUIDs, null, ProcessInstanceCriterion.ENDED_DATE_ASC);
    checkInstanceListContent(10, 20, 0, instanceUUIDs, null, ProcessInstanceCriterion.ENDED_DATE_ASC);

    final List<ProcessInstanceUUID> filter = Misc.subList(ProcessInstanceUUID.class, instanceUUIDs, 0, 7);
    assertEquals(7, filter.size());

    checkInstanceListContent(0, 5, 5, instanceUUIDs, filter, ProcessInstanceCriterion.ENDED_DATE_ASC);
    checkInstanceListContent(5, 5, 2, instanceUUIDs, filter, ProcessInstanceCriterion.ENDED_DATE_ASC);
    checkInstanceListContent(7, 20, 0, instanceUUIDs, filter, ProcessInstanceCriterion.ENDED_DATE_ASC);

    getRuntimeAPI().deleteAllProcessInstances(processUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetLightProcessInstancesPagingOrderByIntanceNumberAsc() throws Exception {
    final ProcessDefinition process = getProcess();
    final ProcessDefinitionUUID processUUID = process.getUUID();

    getManagementAPI().deploy(getBusinessArchive(process));
    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();

    final int instanceNumber = 10;
    instanciateProcesses(processUUID, instanceNumber, instanceUUIDs);

    checkInstanceListContent(0, 5, 5, instanceUUIDs, null, ProcessInstanceCriterion.INSTANCE_NUMBER_ASC);
    checkInstanceListContent(3, 3, 3, instanceUUIDs, null, ProcessInstanceCriterion.INSTANCE_NUMBER_ASC);
    checkInstanceListContent(5, 8, 5, instanceUUIDs, null, ProcessInstanceCriterion.INSTANCE_NUMBER_ASC);
    checkInstanceListContent(10, 20, 0, instanceUUIDs, null, ProcessInstanceCriterion.INSTANCE_NUMBER_ASC);

    final List<ProcessInstanceUUID> filter = Misc.subList(ProcessInstanceUUID.class, instanceUUIDs, 0, 7);
    assertEquals(7, filter.size());

    checkInstanceListContent(0, 5, 5, instanceUUIDs, filter, ProcessInstanceCriterion.INSTANCE_NUMBER_ASC);
    checkInstanceListContent(5, 5, 2, instanceUUIDs, filter, ProcessInstanceCriterion.INSTANCE_NUMBER_ASC);
    checkInstanceListContent(7, 20, 0, instanceUUIDs, filter, ProcessInstanceCriterion.INSTANCE_NUMBER_ASC);

    getRuntimeAPI().deleteAllProcessInstances(processUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetLightProcessInstancesPagingOrderByUUIDAsc() throws Exception {
    final ProcessDefinition process = getProcess();
    final ProcessDefinitionUUID processUUID = process.getUUID();

    getManagementAPI().deploy(getBusinessArchive(process));
    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();

    final int instanceNumber = 10;
    instanciateProcesses(processUUID, instanceNumber, instanceUUIDs);

    Collections.sort(instanceUUIDs, new ProcessInstanceUUIDComparator());

    checkInstanceListContent(0, 5, 5, instanceUUIDs, null, ProcessInstanceCriterion.INSTANCE_UUID_ASC);
    checkInstanceListContent(3, 3, 3, instanceUUIDs, null, ProcessInstanceCriterion.INSTANCE_UUID_ASC);
    checkInstanceListContent(5, 8, 5, instanceUUIDs, null, ProcessInstanceCriterion.INSTANCE_UUID_ASC);
    checkInstanceListContent(10, 20, 0, instanceUUIDs, null, ProcessInstanceCriterion.INSTANCE_UUID_ASC);

    final List<ProcessInstanceUUID> filter = Misc.subList(ProcessInstanceUUID.class, instanceUUIDs, 0, 7);
    assertEquals(7, filter.size());

    checkInstanceListContent(0, 5, 5, instanceUUIDs, filter, ProcessInstanceCriterion.INSTANCE_UUID_ASC);
    checkInstanceListContent(5, 5, 2, instanceUUIDs, filter, ProcessInstanceCriterion.INSTANCE_UUID_ASC);
    checkInstanceListContent(7, 20, 0, instanceUUIDs, filter, ProcessInstanceCriterion.INSTANCE_UUID_ASC);

    getRuntimeAPI().deleteAllProcessInstances(processUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetLightProcessInstancesPagingOrderByLastUpdateDesc() throws Exception {
    final ProcessDefinition process = getProcess();
    final ProcessDefinitionUUID processUUID = process.getUUID();

    getManagementAPI().deploy(getBusinessArchive(process));
    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();

    final int instanceNumber = 10;
    instanciateProcesses(processUUID, instanceNumber, instanceUUIDs);

    updateInstanceVariable(instanceUUIDs, "text", "Hello", true);

    checkInstanceListContent(0, 5, 5, instanceUUIDs, null, ProcessInstanceCriterion.LAST_UPDATE_DESC);
    checkInstanceListContent(3, 3, 3, instanceUUIDs, null, ProcessInstanceCriterion.LAST_UPDATE_DESC);
    checkInstanceListContent(5, 8, 5, instanceUUIDs, null, ProcessInstanceCriterion.LAST_UPDATE_DESC);
    checkInstanceListContent(10, 20, 0, instanceUUIDs, null, ProcessInstanceCriterion.LAST_UPDATE_DESC);

    final List<ProcessInstanceUUID> filter = Misc.subList(ProcessInstanceUUID.class, instanceUUIDs, 0, 7);
    assertEquals(7, filter.size());

    checkInstanceListContent(0, 5, 5, instanceUUIDs, filter, ProcessInstanceCriterion.LAST_UPDATE_DESC);
    checkInstanceListContent(5, 5, 2, instanceUUIDs, filter, ProcessInstanceCriterion.LAST_UPDATE_DESC);
    checkInstanceListContent(7, 20, 0, instanceUUIDs, filter, ProcessInstanceCriterion.LAST_UPDATE_DESC);

    getRuntimeAPI().deleteAllProcessInstances(processUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetLightProcessInstancesPagingOrderByStartedDateDesc() throws Exception {
    final ProcessDefinition process = getProcess();
    final ProcessDefinitionUUID processUUID = process.getUUID();

    getManagementAPI().deploy(getBusinessArchive(process));
    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();

    final int instanceNumber = 10;
    instanciateProcesses(processUUID, instanceNumber, instanceUUIDs);

    Collections.reverse(instanceUUIDs);
    checkInstanceListContent(0, 5, 5, instanceUUIDs, null, ProcessInstanceCriterion.STARTED_DATE_DESC);
    checkInstanceListContent(3, 3, 3, instanceUUIDs, null, ProcessInstanceCriterion.STARTED_DATE_DESC);
    checkInstanceListContent(5, 8, 5, instanceUUIDs, null, ProcessInstanceCriterion.STARTED_DATE_DESC);
    checkInstanceListContent(10, 20, 0, instanceUUIDs, null, ProcessInstanceCriterion.STARTED_DATE_DESC);

    final List<ProcessInstanceUUID> filter = Misc.subList(ProcessInstanceUUID.class, instanceUUIDs, 0, 7);
    assertEquals(7, filter.size());

    checkInstanceListContent(0, 5, 5, instanceUUIDs, filter, ProcessInstanceCriterion.STARTED_DATE_DESC);
    checkInstanceListContent(5, 5, 2, instanceUUIDs, filter, ProcessInstanceCriterion.STARTED_DATE_DESC);
    checkInstanceListContent(7, 20, 0, instanceUUIDs, filter, ProcessInstanceCriterion.STARTED_DATE_DESC);

    getRuntimeAPI().deleteAllProcessInstances(processUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetLightProcessInstancesPagingOrderByEndedDateDesc() throws Exception {
    final ProcessDefinition process = getProcess();
    final ProcessDefinitionUUID processUUID = process.getUUID();

    getManagementAPI().deploy(getBusinessArchive(process));
    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();

    final int instanceNumber = 10;
    instanciateProcesses(processUUID, instanceNumber, instanceUUIDs);

    Collections.rotate(instanceUUIDs, 3);
    for (int i = 0; i < instanceNumber; i++) {
      final ProcessInstanceUUID instanceUUID = instanceUUIDs.get(i);
      executeTask(instanceUUID, "step1");
      Thread.sleep(3);
    }
    Collections.reverse(instanceUUIDs);

    checkInstanceListContent(0, 5, 5, instanceUUIDs, null, ProcessInstanceCriterion.ENDED_DATE_DESC);
    checkInstanceListContent(3, 3, 3, instanceUUIDs, null, ProcessInstanceCriterion.ENDED_DATE_DESC);
    checkInstanceListContent(5, 8, 5, instanceUUIDs, null, ProcessInstanceCriterion.ENDED_DATE_DESC);
    checkInstanceListContent(10, 20, 0, instanceUUIDs, null, ProcessInstanceCriterion.ENDED_DATE_DESC);

    final List<ProcessInstanceUUID> filter = Misc.subList(ProcessInstanceUUID.class, instanceUUIDs, 0, 7);
    assertEquals(7, filter.size());

    checkInstanceListContent(0, 5, 5, instanceUUIDs, filter, ProcessInstanceCriterion.ENDED_DATE_DESC);
    checkInstanceListContent(5, 5, 2, instanceUUIDs, filter, ProcessInstanceCriterion.ENDED_DATE_DESC);
    checkInstanceListContent(7, 20, 0, instanceUUIDs, filter, ProcessInstanceCriterion.ENDED_DATE_DESC);

    getRuntimeAPI().deleteAllProcessInstances(processUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetLightProcessInstancesPagingOrderByIntanceNumberDesc() throws Exception {
    final ProcessDefinition process = getProcess();
    final ProcessDefinitionUUID processUUID = process.getUUID();

    getManagementAPI().deploy(getBusinessArchive(process));
    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();

    final int instanceNumber = 10;
    instanciateProcesses(processUUID, instanceNumber, instanceUUIDs);

    Collections.reverse(instanceUUIDs);

    checkInstanceListContent(0, 5, 5, instanceUUIDs, null, ProcessInstanceCriterion.INSTANCE_NUMBER_DESC);
    checkInstanceListContent(3, 3, 3, instanceUUIDs, null, ProcessInstanceCriterion.INSTANCE_NUMBER_DESC);
    checkInstanceListContent(5, 8, 5, instanceUUIDs, null, ProcessInstanceCriterion.INSTANCE_NUMBER_DESC);
    checkInstanceListContent(10, 20, 0, instanceUUIDs, null, ProcessInstanceCriterion.INSTANCE_NUMBER_DESC);

    final List<ProcessInstanceUUID> filter = Misc.subList(ProcessInstanceUUID.class, instanceUUIDs, 0, 7);
    assertEquals(7, filter.size());

    checkInstanceListContent(0, 5, 5, instanceUUIDs, filter, ProcessInstanceCriterion.INSTANCE_NUMBER_DESC);
    checkInstanceListContent(5, 5, 2, instanceUUIDs, filter, ProcessInstanceCriterion.INSTANCE_NUMBER_DESC);
    checkInstanceListContent(7, 20, 0, instanceUUIDs, filter, ProcessInstanceCriterion.INSTANCE_NUMBER_DESC);

    getRuntimeAPI().deleteAllProcessInstances(processUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetLightProcessInstancesPagingOrderByUUIDDesc() throws Exception {
    final ProcessDefinition process = getProcess();
    final ProcessDefinitionUUID processUUID = process.getUUID();

    getManagementAPI().deploy(getBusinessArchive(process));
    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();

    final int instanceNumber = 10;
    instanciateProcesses(processUUID, instanceNumber, instanceUUIDs);

    Collections.sort(instanceUUIDs, new ProcessInstanceUUIDComparator());
    Collections.reverse(instanceUUIDs);

    checkInstanceListContent(0, 5, 5, instanceUUIDs, null, ProcessInstanceCriterion.INSTANCE_UUID_DESC);
    checkInstanceListContent(3, 3, 3, instanceUUIDs, null, ProcessInstanceCriterion.INSTANCE_UUID_DESC);
    checkInstanceListContent(5, 8, 5, instanceUUIDs, null, ProcessInstanceCriterion.INSTANCE_UUID_DESC);
    checkInstanceListContent(10, 20, 0, instanceUUIDs, null, ProcessInstanceCriterion.INSTANCE_UUID_DESC);

    final List<ProcessInstanceUUID> filter = Misc.subList(ProcessInstanceUUID.class, instanceUUIDs, 0, 7);
    assertEquals(7, filter.size());

    checkInstanceListContent(0, 5, 5, instanceUUIDs, filter, ProcessInstanceCriterion.INSTANCE_UUID_DESC);
    checkInstanceListContent(5, 5, 2, instanceUUIDs, filter, ProcessInstanceCriterion.INSTANCE_UUID_DESC);
    checkInstanceListContent(7, 20, 0, instanceUUIDs, filter, ProcessInstanceCriterion.INSTANCE_UUID_DESC);

    getRuntimeAPI().deleteAllProcessInstances(processUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  private void checkInstanceListContent(final int fromIndex, final int pageSize, final int expectedResults,
      final List<ProcessInstanceUUID> instanceUUIDs, final List<ProcessInstanceUUID> filter,
      final ProcessInstanceCriterion pagingCriterion) {
    List<LightProcessInstance> instances = null;
    if (filter == null) {
      instances = getQueryRuntimeAPI().getLightProcessInstances(fromIndex, pageSize, pagingCriterion);
    } else {
      final Set<ProcessInstanceUUID> setFilter = new HashSet<ProcessInstanceUUID>(filter);
      instances = getQueryRuntimeAPI().getLightProcessInstances(setFilter, fromIndex, pageSize, pagingCriterion);
    }
    assertEquals(expectedResults, instances.size());
    final List<ProcessInstanceUUID> retrivedInstances = new ArrayList<ProcessInstanceUUID>();
    final Iterator<LightProcessInstance> it = instances.iterator();
    while (it.hasNext()) {
      retrivedInstances.add(it.next().getProcessInstanceUUID());
    }
    List<ProcessInstanceUUID> instancesToUse = null;
    if (filter == null) {
      instancesToUse = instanceUUIDs;
    } else {
      instancesToUse = filter;
    }
    final int toIndex = fromIndex + pageSize > instancesToUse.size() ? instancesToUse.size() : fromIndex + pageSize;

    final List<ProcessInstanceUUID> expectedInstances = Misc.subList(ProcessInstanceUUID.class, instancesToUse,
        fromIndex, toIndex);
    final String errorMsg = "Checking retrieved instances order. Expected: " + expectedInstances + " but was "
        + retrivedInstances;
    assertEquals(errorMsg, expectedInstances, retrivedInstances);
  }

  public void testGetProcessInstances() throws BonitaException {
    final ProcessDefinition processDef = getManagementAPI().deploy(
        getBusinessArchiveFromXpdl(this.getClass().getResource("queryRuntime.xpdl")));
    final ProcessDefinitionUUID processUUID = processDef.getUUID();

    Collection<ProcessInstanceUUID> instanceUUIDs = null;

    Set<ProcessInstance> instances = getQueryRuntimeAPI().getProcessInstances(instanceUUIDs);
    assertTrue("No instance created yet, returned set must be empty", instances.isEmpty());

    instanceUUIDs = new HashSet<ProcessInstanceUUID>();
    instances = getQueryRuntimeAPI().getProcessInstances(instanceUUIDs);
    assertTrue("No instance created yet, returned set must be empty", instances.isEmpty());

    instanceUUIDs = new HashSet<ProcessInstanceUUID>();
    instanceUUIDs.add(getRuntimeAPI().instantiateProcess(processUUID));
    instanceUUIDs.add(getRuntimeAPI().instantiateProcess(processUUID));
    instanceUUIDs.add(getRuntimeAPI().instantiateProcess(processUUID));
    instanceUUIDs.add(getRuntimeAPI().instantiateProcess(processUUID));
    instanceUUIDs.add(getRuntimeAPI().instantiateProcess(processUUID));

    instances = getQueryRuntimeAPI().getProcessInstances(instanceUUIDs);
    assertEquals(instanceUUIDs.size(), instances.size());
    final Iterator<ProcessInstance> it = instances.iterator();
    for (int i = 0; i < instanceUUIDs.size(); i++) {
      assertTrue(instanceUUIDs.contains(it.next().getUUID()));
    }

    getRuntimeAPI().deleteAllProcessInstances(processUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetActivityInstanceState() throws BonitaException {
    ProcessDefinition process = ProcessBuilder.createProcess("p", "1.0").addGroup("initiator")
        .addGroupResolver(ProcessInitiatorRoleResolver.class.getName()).addSystemTask("t1")
        .addHumanTask("t2", "initiator").addTransition("t1_t2", "t1", "t2").done();

    process = getManagementAPI().deploy(getBusinessArchive(process, null, ProcessInitiatorRoleResolver.class));

    final ProcessDefinitionUUID processUUID = process.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    final Set<ActivityInstance> activities = getQueryRuntimeAPI().getActivityInstances(instanceUUID);

    ActivityInstanceUUID t1UUID = null;
    ActivityInstanceUUID t2UUID = null;
    for (final ActivityInstance activity : activities) {
      if (activity.getActivityName().equals("t1")) {
        t1UUID = activity.getUUID();
      } else if (activity.getActivityName().equals("t2")) {
        t2UUID = activity.getUUID();
      }
    }

    assertNotNull(t1UUID);
    assertNotNull(t2UUID);

    assertEquals(ActivityState.FINISHED, getQueryRuntimeAPI().getActivityInstanceState(t1UUID));
    assertEquals(ActivityState.READY, getQueryRuntimeAPI().getActivityInstanceState(t2UUID));
    getRuntimeAPI().startTask(t2UUID, true);
    assertEquals(ActivityState.EXECUTING, getQueryRuntimeAPI().getActivityInstanceState(t2UUID));
    getRuntimeAPI().suspendTask(t2UUID, true);
    assertEquals(ActivityState.SUSPENDED, getQueryRuntimeAPI().getActivityInstanceState(t2UUID));
    getRuntimeAPI().resumeTask(t2UUID, true);
    getRuntimeAPI().finishTask(t2UUID, true);
    assertEquals(ActivityState.FINISHED, getQueryRuntimeAPI().getActivityInstanceState(t2UUID));
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetUserInstances() throws BonitaException, LoginException {
    final String userId = getLogin();
    ProcessDefinition process = ProcessBuilder.createProcess("p", "1.0").addHuman(userId).addHuman("wrongUser")
        .addHuman("james").addGroup("initiator").addGroupResolver(ProcessInitiatorRoleResolver.class.getName())
        .addHumanTask("t1", userId).addHumanTask("t2", "initiator").addHumanTask("t3", "wrongUser")
        .addHumanTask("t4", "james").done();

    process = getManagementAPI().deploy(getBusinessArchive(process, null, ProcessInitiatorRoleResolver.class));

    final ProcessDefinitionUUID processUUID = process.getUUID();
    final ProcessInstanceUUID instance1 = getRuntimeAPI().instantiateProcess(processUUID);
    loginAs("john", "bpm");
    final ProcessInstanceUUID instance2 = getRuntimeAPI().instantiateProcess(processUUID);

    checkInstances(getLogin(), 1, instance1);
    getRuntimeAPI().deleteProcessInstance(instance1);
    checkInstances(getLogin(), 0);
    getRuntimeAPI().deleteProcessInstance(instance2);
    checkInstances(getLogin(), 0);

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetUserInstancesInitiator() throws BonitaException, LoginException {
    ProcessDefinition process = ProcessBuilder.createProcess("p", "1.0").addSystemTask("t").done();
    process = getManagementAPI().deploy(getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final ProcessInstanceUUID instanceUUID1 = getRuntimeAPI().instantiateProcess(processUUID);
    final ProcessInstanceUUID instanceUUID2 = getRuntimeAPI().instantiateProcess(processUUID);

    final ProcessInstance instance1 = getQueryRuntimeAPI().getProcessInstance(instanceUUID1);
    final ProcessInstance instance2 = getQueryRuntimeAPI().getProcessInstance(instanceUUID2);

    assertEquals(getLogin(), instance1.getStartedBy());
    assertEquals(getLogin(), instance2.getStartedBy());

    assertEquals(getLogin(), instance1.getInstanceStateUpdates().iterator().next().getUpdatedBy());
    assertEquals(getLogin(), instance2.getInstanceStateUpdates().iterator().next().getUpdatedBy());

    checkInstances(getLogin(), 2, instanceUUID1, instanceUUID2);

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetUserInstancesAssigned() throws BonitaException, LoginException {
    ProcessDefinition process = ProcessBuilder.createProcess("p", "1.0").addGroup("users")
        .addGroupResolver(UserListRoleResolver.class.getName()).addInputParameter("users", "jack")
        .addHumanTask("t", "users").done();
    process = getManagementAPI().deploy(getBusinessArchive(process, null, UserListRoleResolver.class));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    loginAs("jack", "bpm");
    final ProcessInstanceUUID instance1 = getRuntimeAPI().instantiateProcess(processUUID);
    checkInstances("jack", 1, instance1);

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetUserInstancesCandidates() throws BonitaException, LoginException {
    ProcessDefinition process = ProcessBuilder.createProcess("p", "1.0").addGroup("users")
        .addGroupResolver(UserListRoleResolver.class.getName()).addInputParameter("users", "jack,john,james")
        .addHumanTask("t", "users").done();
    process = getManagementAPI().deploy(getBusinessArchive(process, null, UserListRoleResolver.class));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    loginAs("jack", "bpm");
    final ProcessInstanceUUID instance1 = getRuntimeAPI().instantiateProcess(processUUID);
    checkInstances("jack", 1, instance1);

    getManagementAPI().deleteProcess(processUUID);
  }

  private void checkInstances(final String user, final int number, final ProcessInstanceUUID... uuids)
      throws LoginException {
    loginAs(user, "bpm");
    final Set<ProcessInstance> instances = getQueryRuntimeAPI().getUserInstances();
    assertNotNull(instances);
    assertEquals(number, instances.size());
  }

  public void testTwoHumanTask() throws BonitaException {
    ProcessDefinition process = ProcessBuilder.createProcess("p", "1.0").addHuman(getLogin())
        .addHumanTask("t1", getLogin()).addHumanTask("t2", getLogin()).addTransition("t1_t2", "t1", "t2").done();

    process = getManagementAPI().deploy(getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    Collection<TaskInstance> tasks = getQueryRuntimeAPI().getTasks(instanceUUID);
    assertEquals(1, tasks.size());
    final ActivityInstanceUUID t1UUID = tasks.iterator().next().getUUID();
    getRuntimeAPI().startTask(t1UUID, true);
    getRuntimeAPI().finishTask(t1UUID, true);

    tasks = getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
    assertEquals(1, tasks.size());

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetTaskListWithActivityState() throws BonitaException {
    ProcessDefinition process = ProcessBuilder.createProcess("p", "1.0").addGroup("initiator")
        .addGroupResolver(ProcessInitiatorRoleResolver.class.getName()).addGroup("userList")
        .addGroupResolver(UserListRoleResolver.class.getName())
        .addInputParameter("users", "unknownUser1, unknownUser2, " + getLogin()).addGroup("wrongUserList")
        .addGroupResolver(UserListRoleResolver.class.getName())
        .addInputParameter("users", "unknownUser1, unknownUser2").addHumanTask("t1", "initiator")
        .addHumanTask("t2", "userList").addHumanTask("t3", "wrongUserList").addHumanTask("end", "initiator")
        .addJoinType(JoinType.AND).addTransition("t1_end", "t1", "end").addTransition("t2_end", "t2", "end")
        .addTransition("t3_end", "t3", "end").done();

    process = getManagementAPI().deploy(
        getBusinessArchive(process, null, ProcessInitiatorRoleResolver.class, UserListRoleResolver.class));

    final ProcessDefinitionUUID processUUID = process.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    Collection<TaskInstance> activities = null;

    final Collection<ActivityState> taskStates = new HashSet<ActivityState>();
    taskStates.add(ActivityState.EXECUTING);

    activities = getQueryRuntimeAPI().getTaskList(instanceUUID, taskStates);
    assertEquals(0, activities.size());

    taskStates.add(ActivityState.READY);
    activities = getQueryRuntimeAPI().getTaskList(instanceUUID, taskStates);
    // t1, t2 and t3 are READY but t3 is not in task list
    assertEquals(activities.toString(), 2, activities.size());
    assertTrue(contains(activities, "t1"));
    assertTrue(contains(activities, "t2"));

    // start t1
    getRuntimeAPI().startTask(getTask(activities, "t1").getUUID(), true);
    activities = getQueryRuntimeAPI().getTaskList(instanceUUID, taskStates);
    assertEquals(2, activities.size());
    assertTrue(contains(activities, "t1"));
    assertTrue(contains(activities, "t2"));

    // finish t1
    getRuntimeAPI().finishTask(getTask(activities, "t1").getUUID(), true);
    activities = getQueryRuntimeAPI().getTaskList(instanceUUID, taskStates);
    assertEquals(1, activities.size());
    assertTrue(contains(activities, "t2"));

    taskStates.add(ActivityState.FINISHED);

    activities = getQueryRuntimeAPI().getTaskList(instanceUUID, taskStates);
    assertEquals(2, activities.size());
    assertTrue(contains(activities, "t1"));
    assertTrue(contains(activities, "t2"));

    getManagementAPI().deleteProcess(processUUID);
  }

  private boolean contains(final Collection<TaskInstance> tasks, final String activityName) {
    return getTask(tasks, activityName) != null;
  }

  private TaskInstance getTask(final Collection<TaskInstance> tasks, final String activityName) {
    for (final TaskInstance task : tasks) {
      if (task.getActivityName().equals(activityName)) {
        return task;
      }
    }
    return null;
  }

  public void testGetTaskListWithStateCollection() throws BonitaException, LoginException {
    final Collection<ActivityState> taskStates = new HashSet<ActivityState>();
    taskStates.add(ActivityState.READY);
    taskStates.add(ActivityState.EXECUTING);
    taskStates.add(ActivityState.SUSPENDED);
    taskStates.add(ActivityState.FINISHED);

    ProcessDefinition process = ProcessBuilder.createProcess("p", "1.0").addGroup("initiator")
        .addGroupResolver(ProcessInitiatorRoleResolver.class.getName()).addHumanTask("t", "initiator").done();
    process = getManagementAPI().deploy(getBusinessArchive(process, null, ProcessInitiatorRoleResolver.class));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    loginAs("admin", "bpm");
    final ProcessInstanceUUID instanceUUID1 = getRuntimeAPI().instantiateProcess(processUUID);
    final Collection<TaskInstance> taskList = getQueryRuntimeAPI().getTaskList(instanceUUID1, taskStates);
    assertEquals(1, taskList.size());
    TaskInstance task = taskList.iterator().next();
    assertTrue(task.getTaskCandidates().contains("admin"));

    loginAs("john", "bpm");
    assertEquals(0, getQueryRuntimeAPI().getTaskList(instanceUUID1, ActivityState.READY).size());
    assertEquals(0, getQueryRuntimeAPI().getTaskList(instanceUUID1, taskStates).size());

    task = getQueryRuntimeAPI().getTask(task.getUUID());
    assertTrue(task.getTaskCandidates().contains("admin"));

    final ProcessInstanceUUID instanceUUID2 = getRuntimeAPI().instantiateProcess(processUUID);
    assertEquals(1, getQueryRuntimeAPI().getTaskList(instanceUUID2, taskStates).size());

    task = getQueryRuntimeAPI().getTask(task.getUUID());
    assertTrue(task.getTaskCandidates().contains("admin"));

    assertEquals(0, getQueryRuntimeAPI().getTaskList(instanceUUID1, ActivityState.READY).size());
    assertEquals(0, getQueryRuntimeAPI().getTaskList(instanceUUID1, taskStates).size());

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testCanExecuteActivity() throws BonitaException {
    ProcessDefinition process = ProcessBuilder.createProcess("p", "1.0").addGroup("initiator")
        .addGroupResolver(ProcessInitiatorRoleResolver.class.getName()).addGroup("unknownUser")
        .addGroupResolver(UserListRoleResolver.class.getName()).addInputParameter("users", "unknownUser")
        .addGroup("wrongUserList").addGroupResolver(UserListRoleResolver.class.getName())
        .addInputParameter("users", "unknownUser1, unknownUser2").addGroup("correctUserList")
        .addGroupResolver(UserListRoleResolver.class.getName())
        .addInputParameter("users", "unknownUser1, unknownUser2, " + getLogin()).addSystemTask("t1")
        .addHumanTask("t2", "initiator").addHumanTask("t3", "unknownUser").addHumanTask("t4", "wrongUserList")
        .addHumanTask("t5", "correctUserList").addTransition("t1_t2", "t1", "t2").addTransition("t1_t3", "t1", "t3")
        .addTransition("t1_t4", "t1", "t4").addTransition("t1_t5", "t1", "t5").done();

    process = getManagementAPI().deploy(
        getBusinessArchive(process, null, UserListRoleResolver.class, ProcessInitiatorRoleResolver.class));

    final ProcessDefinitionUUID processUUID = process.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    final Set<ActivityInstance> activities = getQueryRuntimeAPI().getActivityInstances(instanceUUID);

    ActivityInstanceUUID t1UUID = null;
    ActivityInstanceUUID t2UUID = null;
    ActivityInstanceUUID t3UUID = null;
    ActivityInstanceUUID t4UUID = null;
    ActivityInstanceUUID t5UUID = null;
    for (final ActivityInstance activity : activities) {
      if (activity.getActivityName().equals("t1")) {
        t1UUID = activity.getUUID();
      } else if (activity.getActivityName().equals("t2")) {
        t2UUID = activity.getUUID();
      } else if (activity.getActivityName().equals("t3")) {
        t3UUID = activity.getUUID();
      } else if (activity.getActivityName().equals("t4")) {
        t4UUID = activity.getUUID();
      } else if (activity.getActivityName().equals("t5")) {
        t5UUID = activity.getUUID();
      }
    }

    assertNotNull(t1UUID);
    assertNotNull(t2UUID);
    assertNotNull(t3UUID);
    assertNotNull(t4UUID);
    assertNotNull(t5UUID);

    assertFalse(getQueryRuntimeAPI().canExecuteTask(t1UUID));
    assertTrue(getQueryRuntimeAPI().canExecuteTask(t2UUID));
    assertFalse(getQueryRuntimeAPI().canExecuteTask(t3UUID));
    assertFalse(getQueryRuntimeAPI().canExecuteTask(t4UUID));
    assertTrue(getQueryRuntimeAPI().canExecuteTask(t5UUID));

    getRuntimeAPI().startTask(t2UUID, true);
    // t2 is not READY...
    assertFalse(getQueryRuntimeAPI().canExecuteTask(t2UUID));

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetProcessInstancesWithTaskState() throws BonitaException {
    final ProcessDefinition processDef = getManagementAPI().deploy(
        getBusinessArchiveFromXpdl(this.getClass().getResource("queryRuntime-states.xpdl")));
    final ProcessDefinitionUUID processUUID = processDef.getUUID();

    Collection<ActivityState> activityStates = null;
    Collection<TaskInstance> todolist = null;

    Set<ProcessInstance> instances = getQueryRuntimeAPI().getProcessInstancesWithTaskState(activityStates);
    assertTrue("No instance created yet, returned set must be empty", instances.isEmpty());

    activityStates = new HashSet<ActivityState>();
    instances = getQueryRuntimeAPI().getProcessInstancesWithTaskState(activityStates);
    assertTrue("No instance created yet, returned set must be empty", instances.isEmpty());

    activityStates.add(ActivityState.READY);

    final ProcessInstanceUUID instance1UUID = getRuntimeAPI().instantiateProcess(processUUID);

    // instance1.A = READY

    todolist = getQueryRuntimeAPI().getTaskList(ActivityState.READY);
    assertEquals("todolist must contain 1 element", 1, todolist.size());
    TaskInstance instance1A = todolist.iterator().next();

    instances = getQueryRuntimeAPI().getProcessInstancesWithTaskState(activityStates);
    assertEquals("One instance must be found with ActivityState = READY", 1, instances.size());

    todolist = getQueryRuntimeAPI().getTaskList(instances.iterator().next().getProcessInstanceUUID(),
        ActivityState.READY);
    assertEquals("todolist must contain 1 element", 1, todolist.size());
    instance1A = todolist.iterator().next();

    getRuntimeAPI().startTask(instance1A.getUUID(), true);

    // instance1.A=EXECUTING

    instances = getQueryRuntimeAPI().getProcessInstancesWithTaskState(activityStates);
    assertEquals("No instance must be found with ActivityState = READY", 0, instances.size());

    activityStates.add(ActivityState.EXECUTING);

    instances = getQueryRuntimeAPI().getProcessInstancesWithTaskState(activityStates);
    assertEquals("One instance must be found with ActivityState = EXECUTING", 1, instances.size());

    final ProcessInstanceUUID instance2UUID = getRuntimeAPI().instantiateProcess(processUUID);

    // instance2.A=READY && instance1.A=EXECUTING

    instances = getQueryRuntimeAPI().getProcessInstancesWithTaskState(activityStates);
    assertEquals("Two instances must be found with ActivityStates = READY + EXECUTING", 2, instances.size());

    getRuntimeAPI().finishTask(instance1A.getUUID(), true);

    // instance1.A=FINISHED && instance1.B=FINISHED (auto activity) &&
    // instance1.C=READY && instance2.A=READY

    assertEquals("ActiivtyA of instance1 must be FINISHED", ActivityState.FINISHED,
        getQueryRuntimeAPI().getTask(instance1A.getUUID()).getState());

    todolist = getQueryRuntimeAPI().getTaskList(instance1UUID, ActivityState.READY);
    assertEquals("todolist must contain 1 element", 1, todolist.size());

    instances = getQueryRuntimeAPI().getProcessInstancesWithTaskState(activityStates);
    assertEquals("Two instances must be found with ActivityStates = READY + EXECUTING", 2, instances.size());

    activityStates.add(ActivityState.FINISHED);

    instances = getQueryRuntimeAPI().getProcessInstancesWithTaskState(activityStates);
    assertEquals("Two instances must be found with ActivityStates = READY + EXECUTING + FINISHED", 2, instances.size());

    final TaskInstance instance1C = todolist.iterator().next();
    getRuntimeAPI().startTask(instance1C.getUUID(), true);
    getRuntimeAPI().finishTask(instance1C.getUUID(), true);

    assertNotNull("Instance1 must be ended", getQueryRuntimeAPI().getProcessInstance(instance1UUID).getEndedBy());

    // instance1.A=FINISHED && instance1.B=FINISHED && instance1.C=FINISHED &&
    // instance2.A=READY

    instances = getQueryRuntimeAPI().getProcessInstancesWithTaskState(activityStates);
    assertEquals("Two instances must be found with ActivityStates = READY + EXECUTING + FINISHED", 2, instances.size());

    getRuntimeAPI().deleteProcessInstance(instance1UUID);

    instances = getQueryRuntimeAPI().getProcessInstancesWithTaskState(activityStates);
    assertEquals("One instance must be found with ActivityStates = READY + EXECUTING + FINISHED", 1, instances.size());

    getRuntimeAPI().deleteProcessInstance(instance2UUID);

    instances = getQueryRuntimeAPI().getProcessInstancesWithTaskState(activityStates);
    assertEquals("No instance must be found with ActivityStates = READY + EXECUTING + FINISHED", 0, instances.size());

    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetLightTaskInstance() throws Exception {
    ProcessDefinition definition = ProcessBuilder.createProcess("kakykytkaks", null).addHuman(getLogin())
        .addHumanTask("yo", getLogin()).done();

    definition = getManagementAPI().deploy(getBusinessArchive(definition));
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());
    final ActivityInstanceUUID taskUUID = getQueryRuntimeAPI().getOneTask(instanceUUID, ActivityState.READY);

    final LightTaskInstance task = getQueryRuntimeAPI().getLightTaskInstance(taskUUID);
    assertNotNull(task);
    assertEquals("yo", task.getActivityName());

    getManagementAPI().deleteAllProcesses();
  }

  public void testGetProcessInstancesWithInstanceStates() throws Exception {
    ProcessDefinition definition = ProcessBuilder.createProcess("test_getinstances_with_state", null)
        .addHuman(getLogin()).addHumanTask("task1", getLogin()).done();

    definition = getManagementAPI().deploy(getBusinessArchive(definition));
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());
    final ProcessInstanceUUID instanceUUID2 = getRuntimeAPI().instantiateProcess(definition.getUUID());

    final Set<InstanceState> instanceStates = new HashSet<InstanceState>();
    instanceStates.add(InstanceState.STARTED);
    Set<ProcessInstance> instances = getQueryRuntimeAPI().getProcessInstancesWithInstanceStates(instanceStates);
    assertEquals("instances started", 2, instances.size());

    getRuntimeAPI().cancelProcessInstance(instanceUUID2);
    instanceStates.clear();
    instanceStates.add(InstanceState.CANCELLED);
    instances = getQueryRuntimeAPI().getProcessInstancesWithInstanceStates(instanceStates);
    assertEquals("instances cancelled", 1, instances.size());

    final ActivityInstanceUUID taskUUID = getQueryRuntimeAPI().getOneTask(instanceUUID, ActivityState.READY);
    getRuntimeAPI().executeTask(taskUUID, true);
    instanceStates.add(InstanceState.FINISHED);
    instances = getQueryRuntimeAPI().getProcessInstancesWithInstanceStates(instanceStates);
    assertEquals("instances cancelled and finished", 2, instances.size());

    getRuntimeAPI().deleteAllProcessInstances(definition.getUUID());
    instanceStates.add(InstanceState.STARTED);
    instanceStates.add(InstanceState.ABORTED);
    instances = getQueryRuntimeAPI().getProcessInstancesWithInstanceStates(instanceStates);
    assertEquals("instances with all states", 0, instances.size());

    instanceStates.clear();
    try {
      instances = getQueryRuntimeAPI().getProcessInstancesWithInstanceStates(instanceStates);
      fail("An empty collection of instances states shouldn't be allowed");
    } catch (final BonitaInternalException e) {
      // This exception is expected
    }
    getManagementAPI().deleteProcess(definition.getUUID());
  }

  public void testGetTasks() throws Exception {
    ProcessDefinition definition = ProcessBuilder.createProcess("tasks", "1.0").addGroup("humans")
        .addGroupResolver(UserListRoleResolver.class.getName()).addInputParameter("users", "john, admin, james")
        .addSystemTask("start").addHumanTask("first", "humans").addHumanTask("second", "humans")
        .addHumanTask("third", "humans").addHumanTask("fourth", "humans").addTransition("start", "first")
        .addTransition("first", "second").addTransition("second", "third").addTransition("third", "fourth").done();

    definition = getManagementAPI().deploy(getBusinessArchive(definition, null, UserListRoleResolver.class));
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());

    final Set<String> taskNames = new HashSet<String>();

    Set<LightTaskInstance> tasks = getQueryRuntimeAPI().getLightTasks(instanceUUID, taskNames);
    assertEquals(0, tasks.size());
    taskNames.add("first");
    tasks = getQueryRuntimeAPI().getLightTasks(instanceUUID, taskNames);
    assertEquals(1, tasks.size());

    loginAs("john", "bpm");
    executeTask(instanceUUID, "first");

    loginAs("james", "bpm");
    executeTask(instanceUUID, "second");
    taskNames.add("second");
    taskNames.add("fourth");
    tasks = getQueryRuntimeAPI().getLightTasks(instanceUUID, taskNames);
    assertEquals(2, tasks.size());

    taskNames.add("third");
    tasks = getQueryRuntimeAPI().getLightTasks(instanceUUID, taskNames);
    assertEquals(3, tasks.size());

    getManagementAPI().deleteProcess(definition.getUUID());
  }

  public void testGetLightUserInstancesPaging() throws Exception {
    ProcessDefinition process = getProcessWithASubprocess();

    ProcessDefinition subProcess = getSuprocess();

    subProcess = getManagementAPI().deploy(getBusinessArchive(subProcess));
    process = getManagementAPI().deploy(getBusinessArchive(process));

    final ProcessDefinitionUUID parentProcessUUID = process.getUUID();

    final int numberOfInstances = 10;
    for (int i = 0; i < numberOfInstances; i++) {
      getRuntimeAPI().instantiateProcess(parentProcessUUID);
    }

    List<LightProcessInstance> userInstances = getQueryRuntimeAPI().getLightParentUserInstances(0, 8);
    assertEquals(8, userInstances.size());
    userInstances = getQueryRuntimeAPI().getLightParentUserInstances(0, 20);
    assertEquals(numberOfInstances, userInstances.size());

    getManagementAPI().deleteProcess(process.getUUID());
    getManagementAPI().deleteProcess(subProcess.getUUID());
  }

  public void testGetLightParentUserInstancesPagingOrderByLastUpdateAsc() throws Exception {
    ProcessDefinition process = getProcessWithASubprocess();

    ProcessDefinition subProcess = getSuprocess();

    subProcess = getManagementAPI().deploy(getBusinessArchive(subProcess));
    process = getManagementAPI().deploy(getBusinessArchive(process));

    final ProcessDefinitionUUID parentProcessUUID = process.getUUID();

    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    final int numberOfInstances = 10;
    instanciateProcesses(parentProcessUUID, numberOfInstances, instanceUUIDs);

    checkParentUserInstanceListContent(0, 8, 8, instanceUUIDs, ProcessInstanceCriterion.LAST_UPDATE_ASC);
    checkParentUserInstanceListContent(5, 20, 5, instanceUUIDs, ProcessInstanceCriterion.LAST_UPDATE_ASC);

    getManagementAPI().deleteProcess(process.getUUID());
    getManagementAPI().deleteProcess(subProcess.getUUID());
  }

  public void testGetLightParentUserInstancesPagingOrderByStartedDateAsc() throws Exception {
    ProcessDefinition process = getProcessWithASubprocess();

    ProcessDefinition subProcess = getSuprocess();

    subProcess = getManagementAPI().deploy(getBusinessArchive(subProcess));
    process = getManagementAPI().deploy(getBusinessArchive(process));

    final ProcessDefinitionUUID parentProcessUUID = process.getUUID();

    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    final int numberOfInstances = 10;
    instanciateProcesses(parentProcessUUID, numberOfInstances, instanceUUIDs);

    checkParentUserInstanceListContent(0, 8, 8, instanceUUIDs, ProcessInstanceCriterion.STARTED_DATE_ASC);
    checkParentUserInstanceListContent(5, 20, 5, instanceUUIDs, ProcessInstanceCriterion.STARTED_DATE_ASC);

    getManagementAPI().deleteProcess(process.getUUID());
    getManagementAPI().deleteProcess(subProcess.getUUID());
  }

  public void testGetLightParentUserInstancesPagingOrderByEndedDateAsc() throws Exception {
    ProcessDefinition process = getProcessWithASubprocess();

    ProcessDefinition subProcess = getSuprocess();

    subProcess = getManagementAPI().deploy(getBusinessArchive(subProcess));
    process = getManagementAPI().deploy(getBusinessArchive(process));

    final ProcessDefinitionUUID parentProcessUUID = process.getUUID();

    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    final int numberOfInstances = 10;
    instanciateProcesses(parentProcessUUID, numberOfInstances, instanceUUIDs);

    Collections.reverse(instanceUUIDs);
    for (int i = 0; i < numberOfInstances; i++) {
      final ProcessInstance instance = getQueryRuntimeAPI().getProcessInstance(instanceUUIDs.get(i));
      final Set<ProcessInstanceUUID> childrenUUIDs = instance.getChildrenInstanceUUID();
      assertEquals(1, childrenUUIDs.size());
      executeTask(childrenUUIDs.iterator().next(), "h");
      Thread.sleep(3);
    }

    checkParentUserInstanceListContent(0, 8, 8, instanceUUIDs, ProcessInstanceCriterion.ENDED_DATE_ASC);
    checkParentUserInstanceListContent(5, 20, 5, instanceUUIDs, ProcessInstanceCriterion.ENDED_DATE_ASC);

    getManagementAPI().deleteProcess(process.getUUID());
    getManagementAPI().deleteProcess(subProcess.getUUID());
  }

  public void testGetLightParentUserInstancesPagingOrderByInstanceNumberAsc() throws Exception {
    ProcessDefinition process = getProcessWithASubprocess();

    ProcessDefinition subProcess = getSuprocess();

    subProcess = getManagementAPI().deploy(getBusinessArchive(subProcess));
    process = getManagementAPI().deploy(getBusinessArchive(process));

    final ProcessDefinitionUUID parentProcessUUID = process.getUUID();

    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    final int numberOfInstances = 10;
    instanciateProcesses(parentProcessUUID, numberOfInstances, instanceUUIDs);

    checkParentUserInstanceListContent(0, 8, 8, instanceUUIDs, ProcessInstanceCriterion.INSTANCE_NUMBER_ASC);
    checkParentUserInstanceListContent(5, 20, 5, instanceUUIDs, ProcessInstanceCriterion.INSTANCE_NUMBER_ASC);

    getManagementAPI().deleteProcess(process.getUUID());
    getManagementAPI().deleteProcess(subProcess.getUUID());
  }

  public void testGetLightParentUserInstancesPagingOrderByInstanceUUIDAsc() throws Exception {
    ProcessDefinition process = getProcessWithASubprocess();

    ProcessDefinition subProcess = getSuprocess();

    subProcess = getManagementAPI().deploy(getBusinessArchive(subProcess));
    process = getManagementAPI().deploy(getBusinessArchive(process));

    final ProcessDefinitionUUID parentProcessUUID = process.getUUID();

    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    final int numberOfInstances = 10;
    instanciateProcesses(parentProcessUUID, numberOfInstances, instanceUUIDs);

    Collections.sort(instanceUUIDs, new ProcessInstanceUUIDComparator());

    checkParentUserInstanceListContent(0, 8, 8, instanceUUIDs, ProcessInstanceCriterion.INSTANCE_UUID_ASC);
    checkParentUserInstanceListContent(5, 20, 5, instanceUUIDs, ProcessInstanceCriterion.INSTANCE_UUID_ASC);

    getManagementAPI().deleteProcess(process.getUUID());
    getManagementAPI().deleteProcess(subProcess.getUUID());
  }

  public void testGetLightParentUserInstancesPagingOrderByLastUpdateDesc() throws Exception {
    ProcessDefinition process = getProcessWithASubprocess();

    ProcessDefinition subProcess = getSuprocess();

    subProcess = getManagementAPI().deploy(getBusinessArchive(subProcess));
    process = getManagementAPI().deploy(getBusinessArchive(process));

    final ProcessDefinitionUUID parentProcessUUID = process.getUUID();

    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    final int numberOfInstances = 10;
    instanciateProcesses(parentProcessUUID, numberOfInstances, instanceUUIDs);

    Collections.reverse(instanceUUIDs);

    checkParentUserInstanceListContent(0, 8, 8, instanceUUIDs, ProcessInstanceCriterion.LAST_UPDATE_DESC);
    checkParentUserInstanceListContent(5, 20, 5, instanceUUIDs, ProcessInstanceCriterion.LAST_UPDATE_DESC);

    getManagementAPI().deleteProcess(process.getUUID());
    getManagementAPI().deleteProcess(subProcess.getUUID());
  }

  public void testGetLightParentUserInstancesPagingOrderByStartedDateDesc() throws Exception {
    ProcessDefinition process = getProcessWithASubprocess();

    ProcessDefinition subProcess = getSuprocess();

    subProcess = getManagementAPI().deploy(getBusinessArchive(subProcess));
    process = getManagementAPI().deploy(getBusinessArchive(process));

    final ProcessDefinitionUUID parentProcessUUID = process.getUUID();

    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    final int numberOfInstances = 10;
    instanciateProcesses(parentProcessUUID, numberOfInstances, instanceUUIDs);

    Collections.reverse(instanceUUIDs);
    checkParentUserInstanceListContent(0, 8, 8, instanceUUIDs, ProcessInstanceCriterion.STARTED_DATE_DESC);
    checkParentUserInstanceListContent(5, 20, 5, instanceUUIDs, ProcessInstanceCriterion.STARTED_DATE_DESC);

    getManagementAPI().deleteProcess(process.getUUID());
    getManagementAPI().deleteProcess(subProcess.getUUID());
  }

  public void testGetLightParentUserInstancesPagingOrderByEndedDateDesc() throws Exception {
    ProcessDefinition process = getProcessWithASubprocess();

    ProcessDefinition subProcess = getSuprocess();

    subProcess = getManagementAPI().deploy(getBusinessArchive(subProcess));
    process = getManagementAPI().deploy(getBusinessArchive(process));

    final ProcessDefinitionUUID parentProcessUUID = process.getUUID();

    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    final int numberOfInstances = 10;
    instanciateProcesses(parentProcessUUID, numberOfInstances, instanceUUIDs);

    Collections.reverse(instanceUUIDs);
    for (int i = 0; i < numberOfInstances; i++) {
      final ProcessInstance instance = getQueryRuntimeAPI().getProcessInstance(instanceUUIDs.get(i));
      final Set<ProcessInstanceUUID> childrenUUIDs = instance.getChildrenInstanceUUID();
      assertEquals(1, childrenUUIDs.size());
      executeTask(childrenUUIDs.iterator().next(), "h");
      Thread.sleep(3);
    }

    Collections.reverse(instanceUUIDs);
    checkParentUserInstanceListContent(0, 8, 8, instanceUUIDs, ProcessInstanceCriterion.ENDED_DATE_DESC);
    checkParentUserInstanceListContent(5, 20, 5, instanceUUIDs, ProcessInstanceCriterion.ENDED_DATE_DESC);

    getManagementAPI().deleteProcess(process.getUUID());
    getManagementAPI().deleteProcess(subProcess.getUUID());
  }

  public void testGetLightParentUserInstancesPagingOrderByInstanceNumberDesc() throws Exception {
    ProcessDefinition process = getProcessWithASubprocess();

    ProcessDefinition subProcess = getSuprocess();

    subProcess = getManagementAPI().deploy(getBusinessArchive(subProcess));
    process = getManagementAPI().deploy(getBusinessArchive(process));

    final ProcessDefinitionUUID parentProcessUUID = process.getUUID();

    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    final int numberOfInstances = 10;
    instanciateProcesses(parentProcessUUID, numberOfInstances, instanceUUIDs);

    Collections.reverse(instanceUUIDs);

    checkParentUserInstanceListContent(0, 8, 8, instanceUUIDs, ProcessInstanceCriterion.INSTANCE_NUMBER_DESC);
    checkParentUserInstanceListContent(5, 20, 5, instanceUUIDs, ProcessInstanceCriterion.INSTANCE_NUMBER_DESC);

    getManagementAPI().deleteProcess(process.getUUID());
    getManagementAPI().deleteProcess(subProcess.getUUID());
  }

  public void testGetLightParentUserInstancesPagingOrderByInstanceUUIDDesc() throws Exception {
    ProcessDefinition process = getProcessWithASubprocess();

    ProcessDefinition subProcess = getSuprocess();

    subProcess = getManagementAPI().deploy(getBusinessArchive(subProcess));
    process = getManagementAPI().deploy(getBusinessArchive(process));

    final ProcessDefinitionUUID parentProcessUUID = process.getUUID();

    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    final int numberOfInstances = 10;
    instanciateProcesses(parentProcessUUID, numberOfInstances, instanceUUIDs);

    Collections.sort(instanceUUIDs, new ProcessInstanceUUIDComparator());
    Collections.reverse(instanceUUIDs);

    checkParentUserInstanceListContent(0, 8, 8, instanceUUIDs, ProcessInstanceCriterion.INSTANCE_UUID_DESC);
    checkParentUserInstanceListContent(5, 20, 5, instanceUUIDs, ProcessInstanceCriterion.INSTANCE_UUID_DESC);

    getManagementAPI().deleteProcess(process.getUUID());
    getManagementAPI().deleteProcess(subProcess.getUUID());
  }

  private void checkParentUserInstanceListContent(final int fromIndex, final int pageSize, final int expectedResults,
      final List<ProcessInstanceUUID> instanceUUIDs, final ProcessInstanceCriterion pagingCriterion) {
    final List<LightProcessInstance> instances = getQueryRuntimeAPI().getLightParentUserInstances(fromIndex, pageSize,
        pagingCriterion);
    checkListContent(fromIndex, pageSize, expectedResults, instanceUUIDs, instances);
  }

  private void checkParentUserInstanceExceptListContent(final int fromIndex, final int pageSize,
      final int expectedResults, final List<ProcessInstanceUUID> instanceUUIDs,
      final Set<ProcessDefinitionUUID> processUUIDs, final ProcessInstanceCriterion pagingCriterion) {
    final List<LightProcessInstance> instances = getQueryRuntimeAPI().getLightParentUserInstancesExcept(fromIndex,
        pageSize, processUUIDs, pagingCriterion);
    checkListContent(fromIndex, pageSize, expectedResults, instanceUUIDs, instances);
  }

  private void checkParentUserInstanceListContent(final int fromIndex, final int pageSize, final int expectedResults,
      final List<ProcessInstanceUUID> instanceUUIDs, final Set<ProcessDefinitionUUID> processUUIDs,
      final ProcessInstanceCriterion pagingCriterion) {
    final List<LightProcessInstance> instances = getQueryRuntimeAPI().getLightParentUserInstances(fromIndex, pageSize,
        processUUIDs, pagingCriterion);
    checkListContent(fromIndex, pageSize, expectedResults, instanceUUIDs, instances);
  }

  private void checkParentProcessInstancesWithActiveUserListContent(final String userId, final int fromIndex,
      final int pageSize, final int expectedResults, final List<ProcessInstanceUUID> instanceUUIDs,
      final ProcessInstanceCriterion pagingCriterion) {
    final List<LightProcessInstance> instances = getQueryRuntimeAPI().getLightParentProcessInstancesWithActiveUser(
        userId, fromIndex, pageSize, pagingCriterion);
    checkListContent(fromIndex, pageSize, expectedResults, instanceUUIDs, instances);
  }

  private void checkListContent(final int fromIndex, final int pageSize, final int expectedNbofResults,
      final List<ProcessInstanceUUID> instanceUUIDs, final List<LightProcessInstance> instances) {
    assertEquals(expectedNbofResults, instances.size());
    final List<ProcessInstanceUUID> retrivedInstances = new ArrayList<ProcessInstanceUUID>();
    final Iterator<LightProcessInstance> it = instances.iterator();
    while (it.hasNext()) {
      retrivedInstances.add(it.next().getProcessInstanceUUID());
    }

    final int toIndex = fromIndex + pageSize > instanceUUIDs.size() ? instanceUUIDs.size() : fromIndex + pageSize;

    final List<ProcessInstanceUUID> expectedInstances = Misc.subList(ProcessInstanceUUID.class, instanceUUIDs,
        fromIndex, toIndex);
    assertEquals(expectedInstances, retrivedInstances);
  }

  public void testDynamicExecutionSummary() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("myProcess", "1.0").addHuman(getLogin())
        .addStringData("processVar", "pValue").addHumanTask("a", getLogin()).addDynamicExecutionSummary("a${2+2}")
        .addSystemTask("b").addDynamicExecutionSummary("${3+3}").addTransition("a", "b").done();

    process = getManagementAPI().deploy(getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    ProcessInstance instance = getQueryRuntimeAPI().getProcessInstance(instanceUUID);

    Set<ActivityInstance> activities = instance.getActivities();
    assertEquals(1, activities.size());

    ActivityInstance a = instance.getActivities("a").iterator().next();
    assertNull(a.getDynamicExecutionSummary());

    getRuntimeAPI().executeTask(a.getUUID(), true);
    instance = getQueryRuntimeAPI().getProcessInstance(instanceUUID);

    activities = getQueryRuntimeAPI().getActivityInstances(instanceUUID);

    assertEquals(InstanceState.FINISHED, instance.getInstanceState());
    assertEquals(2, instance.getActivities().size());

    a = instance.getActivities("a").iterator().next();
    final ActivityInstance b = instance.getActivities("b").iterator().next();

    assertEquals("a4", a.getDynamicExecutionSummary());
    assertEquals("6", b.getDynamicExecutionSummary());

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetXMLData() throws Exception {
    final String separator = BonitaConstants.XPATH_VAR_SEPARATOR;

    ProcessDefinition process = ProcessBuilder.createProcess("myProcess", "1.0")
        .addXMLData("xmlProcessVar", "<node att=\"attValue\"><child>childValue</child></node>").addHuman(getLogin())
        .addHumanTask("task", getLogin())
        .addXMLData("xmlActivityVar", "<node att=\"attValue\"><child>childValue</child></node>").done();

    process = getManagementAPI().deploy(getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    final ActivityInstanceUUID activityUUID = getQueryRuntimeAPI().getOneTask(instanceUUID, ActivityState.READY);
    // / process
    // test node
    Object value = null;

    value = getQueryRuntimeAPI().getVariable(activityUUID, "xmlProcessVar");
    assertTrue("value should be a String", value instanceof Document);

    value = getQueryRuntimeAPI().getVariable(activityUUID, "xmlProcessVar" + separator + "/node");
    assertTrue("value should be a node", value instanceof Node);
    // test text
    value = getQueryRuntimeAPI().getVariable(activityUUID, "xmlProcessVar" + separator + "/node/child/text()");
    assertEquals("childValue", value);
    // test attribute
    value = getQueryRuntimeAPI().getVariable(activityUUID, "xmlProcessVar" + separator + "/node/@att");
    assertEquals("attValue", value);
    // test child
    value = getQueryRuntimeAPI().getVariable(activityUUID, "xmlProcessVar" + separator + "/node/child");
    assertTrue(value instanceof Node);

    // / activity
    // test node
    value = getQueryRuntimeAPI().getVariable(activityUUID, "xmlActivityVar");
    assertTrue("value should be a String", value instanceof Document);

    value = getQueryRuntimeAPI().getVariable(activityUUID, "xmlActivityVar" + separator + "/node");
    assertTrue("value should be a node", value instanceof Node);
    // test text
    value = getQueryRuntimeAPI().getVariable(activityUUID, "xmlActivityVar" + separator + "/node/child/text()");
    assertEquals("childValue", value);
    // test attribute
    value = getQueryRuntimeAPI().getVariable(activityUUID, "xmlActivityVar" + separator + "/node/@att");
    assertEquals("attValue", value);
    // test child
    value = getQueryRuntimeAPI().getVariable(activityUUID, "xmlActivityVar" + separator + "/node/child");
    assertTrue(value instanceof Node);

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetXMLDataUsingScriptAsInitialValue() throws Exception {
    final String separator = BonitaConstants.XPATH_VAR_SEPARATOR;

    final StringBuilder stb = new StringBuilder();

    stb.append("${\"<node att=\\\"attValue\\\"><child>childValue</child></node>\"}");

    ProcessDefinition process = ProcessBuilder.createProcess("xmlDefaultValueProcess", "1.0").addHuman(getLogin())
        .addXMLDataFromScript("globalXmlData", stb.toString()).addHumanTask("step1", getLogin())
        .addXMLDataFromScript("localXmlData", stb.toString()).done();

    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    final ActivityInstanceUUID activityUUID = getQueryRuntimeAPI().getOneTask(instanceUUID, ActivityState.READY);
    // / process
    // test node
    Object value = null;

    value = getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "globalXmlData");
    assertTrue("value should be a Document", value instanceof Document);

    value = getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "globalXmlData" + separator + "/node");
    assertTrue("value should be a node", value instanceof Node);
    // test text
    value = getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID,
        "globalXmlData" + separator + "/node/child/text()");
    assertEquals("childValue", value);
    // test attribute
    value = getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "globalXmlData" + separator + "/node/@att");
    assertEquals("attValue", value);
    // test child
    value = getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "globalXmlData" + separator + "/node/child");
    assertTrue(value instanceof Node);

    // activity variable
    value = getQueryRuntimeAPI().getVariable(activityUUID, "localXmlData");
    assertTrue("value should be a Document", value instanceof Document);

    value = getQueryRuntimeAPI().getVariable(activityUUID, "localXmlData" + separator + "/node");
    assertTrue("value should be a node", value instanceof Node);
    // test text
    value = getQueryRuntimeAPI().getVariable(activityUUID, "localXmlData" + separator + "/node/child/text()");
    assertEquals("childValue", value);
    // test attribute
    value = getQueryRuntimeAPI().getVariable(activityUUID, "localXmlData" + separator + "/node/@att");
    assertEquals("attValue", value);
    // test child
    value = getQueryRuntimeAPI().getVariable(activityUUID, "localXmlData" + separator + "/node/child");
    assertTrue(value instanceof Node);

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetNumberOfParentProcessInstancesWithOverdueTasks() throws Exception {
    final ManagementAPI managementAPI = getManagementAPI();
    final RuntimeAPI runtimeAPI = getRuntimeAPI();
    final QueryRuntimeAPI queryRuntimeAPI = getQueryRuntimeAPI();

    Integer nbOfProcessInstances = queryRuntimeAPI.getNumberOfParentProcessInstancesWithOverdueTasks(getLogin());
    assertEquals(0, nbOfProcessInstances.intValue());

    final ProcessDefinition withoutTimeEstimationProcess = ProcessBuilder.createProcess("withoutTimeEstimation", "1.0")
        .addHuman(getLogin()).addHumanTask("overdue", getLogin()).addHumanTask("onTrack", getLogin())
        .addHumanTask("atRisk", getLogin()).done();
    managementAPI.deploy(BusinessArchiveFactory.getBusinessArchive(withoutTimeEstimationProcess));

    final ProcessDefinitionUUID withoutTimeEstimationProcessUUID = withoutTimeEstimationProcess.getUUID();

    // Start a case.
    runtimeAPI.instantiateProcess(withoutTimeEstimationProcessUUID);

    nbOfProcessInstances = queryRuntimeAPI.getNumberOfParentProcessInstancesWithOverdueTasks(getLogin());
    assertEquals(0, nbOfProcessInstances.intValue());

    final ProcessDefinition withTimeEstimationProcess = ProcessBuilder.createProcess("withTimeEstimation", "1.0")
        .addHuman(getLogin()).addHumanTask("overdue", getLogin()).addActivityExecutingTime(1000)
        .addHumanTask("onTrack", getLogin()).addActivityExecutingTime(999999999).addHumanTask("atRisk", getLogin())
        .addActivityExecutingTime(10000).done();
    managementAPI.deploy(BusinessArchiveFactory.getBusinessArchive(withTimeEstimationProcess));
    final ProcessDefinitionUUID withTimeEstimationProcessUUID = withTimeEstimationProcess.getUUID();

    // Start a case.
    runtimeAPI.instantiateProcess(withTimeEstimationProcessUUID);
    // Be sure the step is overdue.
    Thread.sleep(1010);

    nbOfProcessInstances = queryRuntimeAPI.getNumberOfParentProcessInstancesWithOverdueTasks(getLogin());
    assertEquals(1, nbOfProcessInstances.intValue());

    getManagementAPI().deleteProcess(withTimeEstimationProcessUUID);
    getManagementAPI().deleteProcess(withoutTimeEstimationProcessUUID);
  }

  public void testGetProcessInstanceVariables() throws Exception {
    ProcessDefinition definition = ProcessBuilder.createProcess("var", "95.2").addStringData("text")
        .addIntegerData("number", 83).addHuman(getLogin()).addHumanTask("step", getLogin()).addStringData("area")
        .done();

    definition = getManagementAPI().deploy(getBusinessArchive(definition));
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());
    final Date firstDate = new Date();
    Map<String, Object> variables = getQueryRuntimeAPI().getProcessInstanceVariables(instanceUUID, firstDate);
    assertEquals(2, variables.size());
    assertEquals(Integer.valueOf(83), variables.get("number"));
    assertNull(variables.get("text"));

    Thread.sleep(2000);
    getRuntimeAPI().setProcessInstanceVariable(instanceUUID, "text", "hello");

    final Date secondDate = new Date();
    assertTrue(firstDate.getTime() < secondDate.getTime());
    variables = getQueryRuntimeAPI().getProcessInstanceVariables(instanceUUID, secondDate);
    assertEquals(2, variables.size());
    assertEquals(Integer.valueOf(83), variables.get("number"));
    assertEquals("hello", variables.get("text"));

    Thread.sleep(2000);
    getRuntimeAPI().setProcessInstanceVariable(instanceUUID, "number", 54);
    final Date thirdDate = new Date();
    assertTrue(secondDate.getTime() < thirdDate.getTime());
    variables = getQueryRuntimeAPI().getProcessInstanceVariables(instanceUUID, thirdDate);
    assertEquals(2, variables.size());
    assertEquals(Integer.valueOf(54), variables.get("number"));
    assertEquals("hello", variables.get("text"));

    variables = getQueryRuntimeAPI().getProcessInstanceVariables(instanceUUID, firstDate);
    assertEquals(2, variables.size());
    assertEquals(Integer.valueOf(83), variables.get("number"));
    assertNull(variables.get("text"));

    variables = getQueryRuntimeAPI().getProcessInstanceVariables(instanceUUID, secondDate);
    assertEquals(2, variables.size());
    assertEquals(Integer.valueOf(83), variables.get("number"));
    assertEquals("hello", variables.get("text"));

    getManagementAPI().deleteProcess(definition.getUUID());
  }

  public void testGetLightActivitiesPaging() throws BonitaException {
    ProcessDefinition definition = getProcess();
    definition = getManagementAPI().deploy(getBusinessArchive(definition));
    final ProcessDefinitionUUID processUUID = definition.getUUID();

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());

    executeTask(instanceUUID, "step1");

    List<LightActivityInstance> activities = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID, 0, 2,
        ActivityInstanceCriterion.DEFAULT);
    assertEquals(2, activities.size());
    assertEquals("step3", activities.get(0).getActivityName());
    assertEquals("step5", activities.get(1).getActivityName());

    activities = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID, 2, 2, ActivityInstanceCriterion.DEFAULT);
    assertEquals(2, activities.size());
    assertEquals("step4", activities.get(0).getActivityName());
    assertEquals("step2", activities.get(1).getActivityName());

    activities = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID, 4, 2, ActivityInstanceCriterion.DEFAULT);
    assertEquals(1, activities.size());
    assertEquals("step1", activities.get(0).getActivityName());

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetLightActivitiesPaginOrderByLastUpdateAsc() throws BonitaException {
    ProcessDefinition definition = getProcess();
    definition = getManagementAPI().deploy(getBusinessArchive(definition));
    final ProcessDefinitionUUID processUUID = definition.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());

    executeTask(instanceUUID, "step1");

    List<LightActivityInstance> activities = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID, 0, 2,
        ActivityInstanceCriterion.LAST_UPDATE_ASC);
    assertEquals(2, activities.size());
    assertEquals("step1", activities.get(0).getActivityName());
    assertEquals("step2", activities.get(1).getActivityName());

    activities = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID, 2, 2,
        ActivityInstanceCriterion.LAST_UPDATE_ASC);
    assertEquals(2, activities.size());
    assertEquals("step4", activities.get(0).getActivityName());
    assertEquals("step5", activities.get(1).getActivityName());

    activities = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID, 4, 2,
        ActivityInstanceCriterion.LAST_UPDATE_ASC);
    assertEquals(1, activities.size());
    assertEquals("step3", activities.get(0).getActivityName());

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetLightActivitiesPagingOrderByStartedDateAsc() throws BonitaException {
    ProcessDefinition definition = getProcess();
    definition = getManagementAPI().deploy(getBusinessArchive(definition));
    final ProcessDefinitionUUID processUUID = definition.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());

    executeTask(instanceUUID, "step1");

    List<LightActivityInstance> activities = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID, 0, 2,
        ActivityInstanceCriterion.STARTED_DATE_ASC);
    assertEquals(2, activities.size());
    assertEquals("step1", activities.get(0).getActivityName());
    assertEquals("step2", activities.get(1).getActivityName());

    activities = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID, 2, 2,
        ActivityInstanceCriterion.STARTED_DATE_ASC);
    assertEquals(2, activities.size());
    assertEquals("step4", activities.get(0).getActivityName());
    assertEquals("step5", activities.get(1).getActivityName());

    activities = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID, 4, 2,
        ActivityInstanceCriterion.STARTED_DATE_ASC);
    assertEquals(1, activities.size());
    assertEquals("step3", activities.get(0).getActivityName());

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetLightActivitiesPagingOrderByEndedDateAsc() throws BonitaException {
    ProcessDefinition definition = getProcess();
    definition = getManagementAPI().deploy(getBusinessArchive(definition));
    final ProcessDefinitionUUID processUUID = definition.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());

    executeTask(instanceUUID, "step1");

    List<LightActivityInstance> activities = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID, 0, 2,
        ActivityInstanceCriterion.ENDED_DATE_ASC);
    assertEquals(2, activities.size());
    assertEquals("step1", activities.get(0).getActivityName());
    assertEquals("step2", activities.get(1).getActivityName());

    activities = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID, 2, 2,
        ActivityInstanceCriterion.ENDED_DATE_ASC);
    assertEquals(2, activities.size());
    assertEquals("step4", activities.get(0).getActivityName());
    assertEquals("step5", activities.get(1).getActivityName());

    activities = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID, 4, 2,
        ActivityInstanceCriterion.ENDED_DATE_ASC);
    assertEquals(1, activities.size());
    assertEquals("step3", activities.get(0).getActivityName());

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetLightActivitiesPagingOrderByNameAsc() throws BonitaException {
    ProcessDefinition definition = getProcess();
    definition = getManagementAPI().deploy(getBusinessArchive(definition));
    final ProcessDefinitionUUID processUUID = definition.getUUID();

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());

    executeTask(instanceUUID, "step1");

    List<LightActivityInstance> activities = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID, 0, 2,
        ActivityInstanceCriterion.NAME_ASC);
    assertEquals(2, activities.size());
    assertEquals("step1", activities.get(0).getActivityName());
    assertEquals("step2", activities.get(1).getActivityName());

    activities = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID, 2, 2, ActivityInstanceCriterion.NAME_ASC);
    assertEquals(2, activities.size());
    assertEquals("step3", activities.get(0).getActivityName());
    assertEquals("step4", activities.get(1).getActivityName());

    activities = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID, 4, 2, ActivityInstanceCriterion.NAME_ASC);
    assertEquals(1, activities.size());
    assertEquals("step5", activities.get(0).getActivityName());

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetLightActivitiesPagingOrderByPriorityAsc() throws BonitaException {
    ProcessDefinition definition = getProcessWithPriorities();
    definition = getManagementAPI().deploy(getBusinessArchive(definition));
    final ProcessDefinitionUUID processUUID = definition.getUUID();

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());

    List<LightActivityInstance> activities = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID, 0, 2,
        ActivityInstanceCriterion.PRIORITY_ASC);
    assertEquals(2, activities.size());
    assertEquals("step2", activities.get(0).getActivityName());
    assertEquals("step1", activities.get(1).getActivityName());

    activities = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID, 2, 4,
        ActivityInstanceCriterion.PRIORITY_ASC);
    assertEquals(1, activities.size());
    assertEquals("step3", activities.get(0).getActivityName());

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetLightActivitiesPaginOrderByLastUpdateDesc() throws BonitaException {
    ProcessDefinition definition = getProcess();
    definition = getManagementAPI().deploy(getBusinessArchive(definition));
    final ProcessDefinitionUUID processUUID = definition.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());

    executeTask(instanceUUID, "step1");

    List<LightActivityInstance> activities = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID, 0, 2,
        ActivityInstanceCriterion.LAST_UPDATE_DESC);
    assertEquals(2, activities.size());
    assertEquals("step3", activities.get(0).getActivityName());
    assertEquals("step5", activities.get(1).getActivityName());

    activities = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID, 2, 2,
        ActivityInstanceCriterion.LAST_UPDATE_DESC);
    assertEquals(2, activities.size());
    assertEquals("step4", activities.get(0).getActivityName());
    assertEquals("step2", activities.get(1).getActivityName());

    activities = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID, 4, 2,
        ActivityInstanceCriterion.LAST_UPDATE_DESC);
    assertEquals(1, activities.size());
    assertEquals("step1", activities.get(0).getActivityName());

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetLightActivitiesPagingOrderByStartedDateDesc() throws BonitaException {
    ProcessDefinition definition = getProcess();
    definition = getManagementAPI().deploy(getBusinessArchive(definition));
    final ProcessDefinitionUUID processUUID = definition.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());

    executeTask(instanceUUID, "step1");

    List<LightActivityInstance> activities = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID, 0, 2,
        ActivityInstanceCriterion.STARTED_DATE_DESC);
    assertEquals(2, activities.size());
    assertEquals("step3", activities.get(0).getActivityName());
    assertEquals("step5", activities.get(1).getActivityName());

    activities = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID, 2, 2,
        ActivityInstanceCriterion.STARTED_DATE_DESC);
    assertEquals(2, activities.size());
    assertEquals("step4", activities.get(0).getActivityName());
    assertEquals("step2", activities.get(1).getActivityName());

    activities = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID, 4, 2,
        ActivityInstanceCriterion.STARTED_DATE_DESC);
    assertEquals(1, activities.size());
    assertEquals("step1", activities.get(0).getActivityName());

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetLightActivitiesPagingOrderByEndedDateDesc() throws BonitaException {
    ProcessDefinition definition = getProcess();
    definition = getManagementAPI().deploy(getBusinessArchive(definition));
    final ProcessDefinitionUUID processUUID = definition.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());

    executeTask(instanceUUID, "step1");

    List<LightActivityInstance> activities = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID, 0, 2,
        ActivityInstanceCriterion.ENDED_DATE_DESC);
    assertEquals(2, activities.size());
    assertEquals("step3", activities.get(0).getActivityName());
    assertEquals("step5", activities.get(1).getActivityName());

    activities = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID, 2, 2,
        ActivityInstanceCriterion.ENDED_DATE_DESC);
    assertEquals(2, activities.size());
    assertEquals("step4", activities.get(0).getActivityName());
    assertEquals("step2", activities.get(1).getActivityName());

    activities = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID, 4, 2,
        ActivityInstanceCriterion.ENDED_DATE_DESC);
    assertEquals(1, activities.size());
    assertEquals("step1", activities.get(0).getActivityName());

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetLightActivitiesPagingOrderByNameDesc() throws BonitaException {
    ProcessDefinition definition = getProcess();
    definition = getManagementAPI().deploy(getBusinessArchive(definition));
    final ProcessDefinitionUUID processUUID = definition.getUUID();

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());

    executeTask(instanceUUID, "step1");

    List<LightActivityInstance> activities = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID, 0, 2,
        ActivityInstanceCriterion.NAME_DESC);
    assertEquals(2, activities.size());
    assertEquals("step5", activities.get(0).getActivityName());
    assertEquals("step4", activities.get(1).getActivityName());

    activities = getQueryRuntimeAPI()
        .getLightActivityInstances(instanceUUID, 2, 2, ActivityInstanceCriterion.NAME_DESC);
    assertEquals(2, activities.size());
    assertEquals("step3", activities.get(0).getActivityName());
    assertEquals("step2", activities.get(1).getActivityName());

    activities = getQueryRuntimeAPI()
        .getLightActivityInstances(instanceUUID, 4, 2, ActivityInstanceCriterion.NAME_DESC);
    assertEquals(1, activities.size());
    assertEquals("step1", activities.get(0).getActivityName());

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetLightActivitiesPagingOrderByPriorityDesc() throws BonitaException {
    ProcessDefinition definition = getProcessWithPriorities();
    definition = getManagementAPI().deploy(getBusinessArchive(definition));
    final ProcessDefinitionUUID processUUID = definition.getUUID();

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(definition.getUUID());

    List<LightActivityInstance> activities = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID, 0, 2,
        ActivityInstanceCriterion.PRIORITY_DESC);
    assertEquals(2, activities.size());
    assertEquals("step3", activities.get(0).getActivityName());
    assertEquals("step1", activities.get(1).getActivityName());

    activities = getQueryRuntimeAPI().getLightActivityInstances(instanceUUID, 2, 4,
        ActivityInstanceCriterion.PRIORITY_DESC);
    assertEquals(1, activities.size());
    assertEquals("step2", activities.get(0).getActivityName());

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  private ProcessDefinition getProcess() {
    final Date now = new Date();
    final Date tomorrow = DateUtil.getNextDay(now);

    final ProcessDefinition definition = ProcessBuilder.createProcess("process", "1.0").addStringData("text")
        .addHuman(getLogin()).addHuman("john").addHumanTask("step1", getLogin(), "john").addStringData("text1")
        .addActivityExecutingTime(tomorrow.getTime() - now.getTime()).addSystemTask("step2").addStringData("text2")
        .addSystemTask("step3").addStringData("text3").addSystemTask("step4").addStringData("text4")
        .addSystemTask("step5").addStringData("text5").addTransition("step1", "step2").addTransition("step2", "step4")
        .addTransition("step4", "step5").addTransition("step5", "step3").done();

    return definition;
  }

  private ProcessDefinition getProcess2() {
    final Date now = new Date();
    final Date afterTomorrow = DateUtil.getNextDay(DateUtil.getNextDay(now));

    final ProcessDefinition definition = ProcessBuilder.createProcess("process2", "1.0").addStringData("text")
        .addHuman(getLogin()).addHuman("john").addHumanTask("step1", getLogin(), "john").addStringData("text1")
        .addActivityExecutingTime(afterTomorrow.getTime() - now.getTime()).done();

    return definition;
  }

  private ProcessDefinition getProcessWithPriorities() {
    final ProcessDefinition definition = ProcessBuilder.createProcess("processWithPriorities", "1.0")
        .addStringData("text").addSystemTask("step1").addActivityPriority(1).addSystemTask("step2")
        .addActivityPriority(2).addSystemTask("step3").addActivityPriority(0).addTransition("step1", "step2")
        .addTransition("step2", "step3").done();

    return definition;
  }

  private void checkParentInstanceListContent(final int fromIndex, final int pageSize, final int expectedResults,
      final List<ProcessInstanceUUID> instanceUUIDs, final ProcessInstanceCriterion pagingCriterion) {
    final List<LightProcessInstance> instances = getQueryRuntimeAPI().getLightParentProcessInstances(fromIndex,
        pageSize, pagingCriterion);
    checkListContent(fromIndex, pageSize, expectedResults, instanceUUIDs, instances);
  }

  public void testGetLightParentProcessInstancesOrderByLastUpdateAsc() throws Exception {
    ProcessDefinition process = getProcessWithASubprocess();
    ProcessDefinition subProcess = getSuprocess();

    subProcess = getManagementAPI().deploy(getBusinessArchive(subProcess));
    process = getManagementAPI().deploy(getBusinessArchive(process));

    final ProcessDefinitionUUID parentProcessUUID = process.getUUID();
    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();

    final int instanceNumber = 10;
    instanciateProcesses(parentProcessUUID, instanceNumber, instanceUUIDs);

    updateInstanceVariable(instanceUUIDs, "text", "Hello", true);

    Collections.reverse(instanceUUIDs);

    checkParentInstanceListContent(0, 8, 8, instanceUUIDs, ProcessInstanceCriterion.LAST_UPDATE_ASC);
    checkParentInstanceListContent(5, 20, 5, instanceUUIDs, ProcessInstanceCriterion.LAST_UPDATE_ASC);

    getRuntimeAPI().deleteAllProcessInstances(parentProcessUUID);
    getManagementAPI().disable(parentProcessUUID);
    getManagementAPI().deleteProcess(parentProcessUUID);

    final ProcessDefinitionUUID subProcessUUID = subProcess.getUUID();
    getManagementAPI().disable(subProcessUUID);
    getManagementAPI().deleteProcess(subProcessUUID);

  }

  public void testGetLightParentProcessInstancesOrderByStartedDateAsc() throws Exception {
    ProcessDefinition process = getProcessWithASubprocess();
    ProcessDefinition subProcess = getSuprocess();

    subProcess = getManagementAPI().deploy(getBusinessArchive(subProcess));
    process = getManagementAPI().deploy(getBusinessArchive(process));

    final ProcessDefinitionUUID parentProcessUUID = process.getUUID();
    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();

    final int instanceNumber = 10;
    instanciateProcesses(parentProcessUUID, instanceNumber, instanceUUIDs);

    checkParentInstanceListContent(0, 5, 5, instanceUUIDs, ProcessInstanceCriterion.STARTED_DATE_ASC);
    checkParentInstanceListContent(5, 20, 5, instanceUUIDs, ProcessInstanceCriterion.STARTED_DATE_ASC);

    getRuntimeAPI().deleteAllProcessInstances(parentProcessUUID);
    getManagementAPI().disable(parentProcessUUID);
    getManagementAPI().deleteProcess(parentProcessUUID);

    final ProcessDefinitionUUID subProcessUUID = subProcess.getUUID();
    getManagementAPI().disable(subProcessUUID);
    getManagementAPI().deleteProcess(subProcessUUID);
  }

  public void testGetLightParentProcessInstancesOrderByEndedDateAsc() throws Exception {
    ProcessDefinition process = getProcessWithASubprocess();
    ProcessDefinition subProcess = getSuprocess();

    subProcess = getManagementAPI().deploy(getBusinessArchive(subProcess));
    process = getManagementAPI().deploy(getBusinessArchive(process));

    final ProcessDefinitionUUID parentProcessUUID = process.getUUID();
    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();

    final int instanceNumber = 10;
    instanciateProcesses(parentProcessUUID, instanceNumber, instanceUUIDs);

    Collections.reverse(instanceUUIDs);

    for (int i = 0; i < instanceNumber; i++) {
      final ProcessInstance instance = getQueryRuntimeAPI().getProcessInstance(instanceUUIDs.get(i));
      final Set<ProcessInstanceUUID> childrenUUIDs = instance.getChildrenInstanceUUID();
      assertEquals(1, childrenUUIDs.size());
      executeTask(childrenUUIDs.iterator().next(), "h");
      Thread.sleep(3);
    }

    checkParentInstanceListContent(0, 5, 5, instanceUUIDs, ProcessInstanceCriterion.ENDED_DATE_ASC);
    checkParentInstanceListContent(5, 20, 5, instanceUUIDs, ProcessInstanceCriterion.ENDED_DATE_ASC);

    getRuntimeAPI().deleteAllProcessInstances(parentProcessUUID);
    getManagementAPI().disable(parentProcessUUID);
    getManagementAPI().deleteProcess(parentProcessUUID);

    final ProcessDefinitionUUID subProcessUUID = subProcess.getUUID();
    getManagementAPI().disable(subProcessUUID);
    getManagementAPI().deleteProcess(subProcessUUID);
  }

  public void testGetLightParentProcessInstancesOrderByInstanceNumberAsc() throws Exception {
    ProcessDefinition process = getProcessWithASubprocess();
    ProcessDefinition subProcess = getSuprocess();

    subProcess = getManagementAPI().deploy(getBusinessArchive(subProcess));
    process = getManagementAPI().deploy(getBusinessArchive(process));

    final ProcessDefinitionUUID parentProcessUUID = process.getUUID();
    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();

    final int instanceNumber = 10;
    instanciateProcesses(parentProcessUUID, instanceNumber, instanceUUIDs);

    checkParentInstanceListContent(0, 5, 5, instanceUUIDs, ProcessInstanceCriterion.INSTANCE_NUMBER_ASC);
    checkParentInstanceListContent(5, 20, 5, instanceUUIDs, ProcessInstanceCriterion.INSTANCE_NUMBER_ASC);

    getRuntimeAPI().deleteAllProcessInstances(parentProcessUUID);
    getManagementAPI().disable(parentProcessUUID);
    getManagementAPI().deleteProcess(parentProcessUUID);

    final ProcessDefinitionUUID subProcessUUID = subProcess.getUUID();
    getManagementAPI().disable(subProcessUUID);
    getManagementAPI().deleteProcess(subProcessUUID);
  }

  public void testGetLightParentProcessInstancesOrderByInstanceUUIDAsc() throws Exception {
    ProcessDefinition process = getProcessWithASubprocess();
    ProcessDefinition subProcess = getSuprocess();

    subProcess = getManagementAPI().deploy(getBusinessArchive(subProcess));
    process = getManagementAPI().deploy(getBusinessArchive(process));

    final ProcessDefinitionUUID parentProcessUUID = process.getUUID();
    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();

    final int instanceNumber = 10;
    instanciateProcesses(parentProcessUUID, instanceNumber, instanceUUIDs);

    Collections.sort(instanceUUIDs, new ProcessInstanceUUIDComparator());

    checkParentInstanceListContent(0, 5, 5, instanceUUIDs, ProcessInstanceCriterion.INSTANCE_UUID_ASC);
    checkParentInstanceListContent(5, 20, 5, instanceUUIDs, ProcessInstanceCriterion.INSTANCE_UUID_ASC);

    getRuntimeAPI().deleteAllProcessInstances(parentProcessUUID);
    getManagementAPI().disable(parentProcessUUID);
    getManagementAPI().deleteProcess(parentProcessUUID);

    final ProcessDefinitionUUID subProcessUUID = subProcess.getUUID();
    getManagementAPI().disable(subProcessUUID);
    getManagementAPI().deleteProcess(subProcessUUID);

  }

  public void testGetLightParentProcessInstancesOrderByLastUpdateDesc() throws Exception {
    ProcessDefinition process = getProcessWithASubprocess();
    ProcessDefinition subProcess = getSuprocess();

    subProcess = getManagementAPI().deploy(getBusinessArchive(subProcess));
    process = getManagementAPI().deploy(getBusinessArchive(process));

    final ProcessDefinitionUUID parentProcessUUID = process.getUUID();
    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();

    final int instanceNumber = 10;
    instanciateProcesses(parentProcessUUID, instanceNumber, instanceUUIDs);

    updateInstanceVariable(instanceUUIDs, "text", "Hello", true);

    checkParentInstanceListContent(0, 8, 8, instanceUUIDs, ProcessInstanceCriterion.LAST_UPDATE_DESC);
    checkParentInstanceListContent(5, 20, 5, instanceUUIDs, ProcessInstanceCriterion.LAST_UPDATE_DESC);

    getRuntimeAPI().deleteAllProcessInstances(parentProcessUUID);
    getManagementAPI().disable(parentProcessUUID);
    getManagementAPI().deleteProcess(parentProcessUUID);

    final ProcessDefinitionUUID subProcessUUID = subProcess.getUUID();
    getManagementAPI().disable(subProcessUUID);
    getManagementAPI().deleteProcess(subProcessUUID);

  }

  public void testGetLightParentProcessInstancesOrderByStartedDateDesc() throws Exception {
    ProcessDefinition process = getProcessWithASubprocess();
    ProcessDefinition subProcess = getSuprocess();

    subProcess = getManagementAPI().deploy(getBusinessArchive(subProcess));
    process = getManagementAPI().deploy(getBusinessArchive(process));

    final ProcessDefinitionUUID parentProcessUUID = process.getUUID();
    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();

    final int instanceNumber = 10;
    instanciateProcesses(parentProcessUUID, instanceNumber, instanceUUIDs);

    Collections.reverse(instanceUUIDs);
    checkParentInstanceListContent(0, 5, 5, instanceUUIDs, ProcessInstanceCriterion.STARTED_DATE_DESC);
    checkParentInstanceListContent(5, 20, 5, instanceUUIDs, ProcessInstanceCriterion.STARTED_DATE_DESC);

    getRuntimeAPI().deleteAllProcessInstances(parentProcessUUID);
    getManagementAPI().disable(parentProcessUUID);
    getManagementAPI().deleteProcess(parentProcessUUID);

    final ProcessDefinitionUUID subProcessUUID = subProcess.getUUID();
    getManagementAPI().disable(subProcessUUID);
    getManagementAPI().deleteProcess(subProcessUUID);
  }

  public void testGetLightParentProcessInstancesOrderByEndedDateDesc() throws Exception {
    ProcessDefinition process = getProcessWithASubprocess();
    ProcessDefinition subProcess = getSuprocess();

    subProcess = getManagementAPI().deploy(getBusinessArchive(subProcess));
    process = getManagementAPI().deploy(getBusinessArchive(process));

    final ProcessDefinitionUUID parentProcessUUID = process.getUUID();
    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();

    final int instanceNumber = 10;
    instanciateProcesses(parentProcessUUID, instanceNumber, instanceUUIDs);

    Collections.reverse(instanceUUIDs);

    for (int i = 0; i < instanceNumber; i++) {
      final ProcessInstance instance = getQueryRuntimeAPI().getProcessInstance(instanceUUIDs.get(i));
      final Set<ProcessInstanceUUID> childrenUUIDs = instance.getChildrenInstanceUUID();
      assertEquals(1, childrenUUIDs.size());
      executeTask(childrenUUIDs.iterator().next(), "h");
      Thread.sleep(3);
    }

    Collections.reverse(instanceUUIDs);
    checkParentInstanceListContent(0, 5, 5, instanceUUIDs, ProcessInstanceCriterion.ENDED_DATE_DESC);
    checkParentInstanceListContent(5, 20, 5, instanceUUIDs, ProcessInstanceCriterion.ENDED_DATE_DESC);

    getRuntimeAPI().deleteAllProcessInstances(parentProcessUUID);
    getManagementAPI().disable(parentProcessUUID);
    getManagementAPI().deleteProcess(parentProcessUUID);

    final ProcessDefinitionUUID subProcessUUID = subProcess.getUUID();
    getManagementAPI().disable(subProcessUUID);
    getManagementAPI().deleteProcess(subProcessUUID);
  }

  public void testGetLightParentProcessInstancesOrderByInstanceNumberDesc() throws Exception {
    ProcessDefinition process = getProcessWithASubprocess();
    ProcessDefinition subProcess = getSuprocess();

    subProcess = getManagementAPI().deploy(getBusinessArchive(subProcess));
    process = getManagementAPI().deploy(getBusinessArchive(process));

    final ProcessDefinitionUUID parentProcessUUID = process.getUUID();
    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();

    final int instanceNumber = 10;
    instanciateProcesses(parentProcessUUID, instanceNumber, instanceUUIDs);

    Collections.reverse(instanceUUIDs);
    checkParentInstanceListContent(0, 5, 5, instanceUUIDs, ProcessInstanceCriterion.INSTANCE_NUMBER_DESC);
    checkParentInstanceListContent(5, 20, 5, instanceUUIDs, ProcessInstanceCriterion.INSTANCE_NUMBER_DESC);

    getRuntimeAPI().deleteAllProcessInstances(parentProcessUUID);
    getManagementAPI().disable(parentProcessUUID);
    getManagementAPI().deleteProcess(parentProcessUUID);

    final ProcessDefinitionUUID subProcessUUID = subProcess.getUUID();
    getManagementAPI().disable(subProcessUUID);
    getManagementAPI().deleteProcess(subProcessUUID);
  }

  public void testGetLightParentProcessInstancesOrderByInstanceUUIDDesc() throws Exception {
    ProcessDefinition process = getProcessWithASubprocess();
    ProcessDefinition subProcess = getSuprocess();

    subProcess = getManagementAPI().deploy(getBusinessArchive(subProcess));
    process = getManagementAPI().deploy(getBusinessArchive(process));

    final ProcessDefinitionUUID parentProcessUUID = process.getUUID();
    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();

    final int instanceNumber = 10;
    instanciateProcesses(parentProcessUUID, instanceNumber, instanceUUIDs);

    Collections.sort(instanceUUIDs, new ProcessInstanceUUIDComparator());
    Collections.reverse(instanceUUIDs);

    checkParentInstanceListContent(0, 5, 5, instanceUUIDs, ProcessInstanceCriterion.INSTANCE_UUID_DESC);
    checkParentInstanceListContent(5, 20, 5, instanceUUIDs, ProcessInstanceCriterion.INSTANCE_UUID_DESC);

    getRuntimeAPI().deleteAllProcessInstances(parentProcessUUID);
    getManagementAPI().disable(parentProcessUUID);
    getManagementAPI().deleteProcess(parentProcessUUID);

    final ProcessDefinitionUUID subProcessUUID = subProcess.getUUID();
    getManagementAPI().disable(subProcessUUID);
    getManagementAPI().deleteProcess(subProcessUUID);

  }

  private ProcessDefinition getSuprocess() {
    final ProcessDefinition subProcess = ProcessBuilder.createProcess("aSubProcess", "1.0").addHuman(getLogin())
        .addHumanTask("h", getLogin()).done();
    return subProcess;
  }

  private ProcessDefinition getProcessWithASubprocess() {
    final ProcessDefinition process = ProcessBuilder.createProcess("pProcess", "1.0").addStringData("text")
        .addSubProcess("aSubProcess", "aSubProcess").done();
    return process;
  }

  public void testGetLightParentUserInstancesExceptOrderByLastUpdateAsc() throws Exception {
    ProcessDefinition process1 = getProcess();
    ProcessDefinition process2 = getProcessWithPriorities();

    process1 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process1));
    process2 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process2));

    final ProcessDefinitionUUID processUUID1 = process1.getUUID();
    final ProcessDefinitionUUID processUUID2 = process2.getUUID();

    final int numberOfProcess1 = 10;
    final List<ProcessInstanceUUID> instanceUUIDsP1 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID1, numberOfProcess1, instanceUUIDsP1);
    updateInstanceVariable(instanceUUIDsP1, "text", "Hello", true);

    final int numberOfProcess2 = 5;
    final List<ProcessInstanceUUID> instanceUUIDsP2 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID2, numberOfProcess2, instanceUUIDsP2);

    final Set<ProcessDefinitionUUID> exceptP1 = new HashSet<ProcessDefinitionUUID>();
    exceptP1.add(processUUID1);

    final Set<ProcessDefinitionUUID> exceptP2 = new HashSet<ProcessDefinitionUUID>();
    exceptP2.add(processUUID2);

    Collections.reverse(instanceUUIDsP1);

    checkParentUserInstanceExceptListContent(0, 5, 5, instanceUUIDsP1, exceptP2,
        ProcessInstanceCriterion.LAST_UPDATE_ASC);
    checkParentUserInstanceExceptListContent(5, 10, 5, instanceUUIDsP1, exceptP2,
        ProcessInstanceCriterion.LAST_UPDATE_ASC);
    checkParentUserInstanceExceptListContent(0, 3, 3, instanceUUIDsP2, exceptP1,
        ProcessInstanceCriterion.LAST_UPDATE_ASC);
    checkParentUserInstanceExceptListContent(3, 5, 2, instanceUUIDsP2, exceptP1,
        ProcessInstanceCriterion.LAST_UPDATE_ASC);

    getRuntimeAPI().deleteAllProcessInstances(processUUID1);
    getRuntimeAPI().deleteAllProcessInstances(processUUID2);
    getManagementAPI().disable(processUUID1);
    getManagementAPI().disable(processUUID2);
    getManagementAPI().deleteProcess(processUUID1);
    getManagementAPI().deleteProcess(processUUID2);
  }

  public void testGetLightParentUserInstancesExceptOrderByStartedDateAsc() throws Exception {
    ProcessDefinition process1 = getProcess();
    ProcessDefinition process2 = getProcessWithPriorities();

    process1 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process1));
    process2 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process2));

    final ProcessDefinitionUUID processUUID1 = process1.getUUID();
    final ProcessDefinitionUUID processUUID2 = process2.getUUID();

    final int numberOfProcess1 = 10;
    final List<ProcessInstanceUUID> instanceUUIDsP1 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID1, numberOfProcess1, instanceUUIDsP1);

    final int numberOfProcess2 = 5;
    final List<ProcessInstanceUUID> instanceUUIDsP2 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID2, numberOfProcess2, instanceUUIDsP2);

    final Set<ProcessDefinitionUUID> exceptP1 = new HashSet<ProcessDefinitionUUID>();
    exceptP1.add(processUUID1);

    final Set<ProcessDefinitionUUID> exceptP2 = new HashSet<ProcessDefinitionUUID>();
    exceptP2.add(processUUID2);

    checkParentUserInstanceExceptListContent(0, 5, 5, instanceUUIDsP1, exceptP2,
        ProcessInstanceCriterion.STARTED_DATE_ASC);
    checkParentUserInstanceExceptListContent(5, 10, 5, instanceUUIDsP1, exceptP2,
        ProcessInstanceCriterion.STARTED_DATE_ASC);
    checkParentUserInstanceExceptListContent(0, 3, 3, instanceUUIDsP2, exceptP1,
        ProcessInstanceCriterion.STARTED_DATE_ASC);
    checkParentUserInstanceExceptListContent(3, 5, 2, instanceUUIDsP2, exceptP1,
        ProcessInstanceCriterion.STARTED_DATE_ASC);

    getRuntimeAPI().deleteAllProcessInstances(processUUID1);
    getRuntimeAPI().deleteAllProcessInstances(processUUID2);
    getManagementAPI().disable(processUUID1);
    getManagementAPI().disable(processUUID2);
    getManagementAPI().deleteProcess(processUUID1);
    getManagementAPI().deleteProcess(processUUID2);
  }

  public void testGetLightParentUserInstancesExceptOrderByEndedDateAsc() throws Exception {
    ProcessDefinition process1 = getProcess();
    ProcessDefinition process2 = getProcessWithPriorities();

    process1 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process1));
    process2 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process2));

    final ProcessDefinitionUUID processUUID1 = process1.getUUID();
    final ProcessDefinitionUUID processUUID2 = process2.getUUID();

    final int numberOfProcess1 = 10;
    final List<ProcessInstanceUUID> instanceUUIDsP1 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID1, numberOfProcess1, instanceUUIDsP1);

    for (final ProcessInstanceUUID processInstanceUUID : instanceUUIDsP1) {
      executeTask(processInstanceUUID, "step1");
      Thread.sleep(3);
    }

    final int numberOfProcess2 = 5;
    final List<ProcessInstanceUUID> instanceUUIDsP2 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID2, numberOfProcess2, instanceUUIDsP2);

    final Set<ProcessDefinitionUUID> exceptP1 = new HashSet<ProcessDefinitionUUID>();
    exceptP1.add(processUUID1);

    final Set<ProcessDefinitionUUID> exceptP2 = new HashSet<ProcessDefinitionUUID>();
    exceptP2.add(processUUID2);

    checkParentUserInstanceExceptListContent(0, 5, 5, instanceUUIDsP1, exceptP2,
        ProcessInstanceCriterion.ENDED_DATE_ASC);
    checkParentUserInstanceExceptListContent(5, 10, 5, instanceUUIDsP1, exceptP2,
        ProcessInstanceCriterion.ENDED_DATE_ASC);
    checkParentUserInstanceExceptListContent(0, 3, 3, instanceUUIDsP2, exceptP1,
        ProcessInstanceCriterion.ENDED_DATE_ASC);
    checkParentUserInstanceExceptListContent(3, 5, 2, instanceUUIDsP2, exceptP1,
        ProcessInstanceCriterion.ENDED_DATE_ASC);

    getRuntimeAPI().deleteAllProcessInstances(processUUID1);
    getRuntimeAPI().deleteAllProcessInstances(processUUID2);
    getManagementAPI().disable(processUUID1);
    getManagementAPI().disable(processUUID2);
    getManagementAPI().deleteProcess(processUUID1);
    getManagementAPI().deleteProcess(processUUID2);
  }

  public void testGetLightParentUserInstancesExceptOrderByInstanceNumberAsc() throws Exception {
    ProcessDefinition process1 = getProcess();
    ProcessDefinition process2 = getProcessWithPriorities();

    process1 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process1));
    process2 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process2));

    final ProcessDefinitionUUID processUUID1 = process1.getUUID();
    final ProcessDefinitionUUID processUUID2 = process2.getUUID();

    final int numberOfProcess1 = 10;
    final List<ProcessInstanceUUID> instanceUUIDsP1 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID1, numberOfProcess1, instanceUUIDsP1);

    final int numberOfProcess2 = 5;
    final List<ProcessInstanceUUID> instanceUUIDsP2 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID2, numberOfProcess2, instanceUUIDsP2);

    final Set<ProcessDefinitionUUID> exceptP1 = new HashSet<ProcessDefinitionUUID>();
    exceptP1.add(processUUID1);

    final Set<ProcessDefinitionUUID> exceptP2 = new HashSet<ProcessDefinitionUUID>();
    exceptP2.add(processUUID2);

    checkParentUserInstanceExceptListContent(0, 5, 5, instanceUUIDsP1, exceptP2,
        ProcessInstanceCriterion.INSTANCE_NUMBER_ASC);
    checkParentUserInstanceExceptListContent(5, 10, 5, instanceUUIDsP1, exceptP2,
        ProcessInstanceCriterion.INSTANCE_NUMBER_ASC);
    checkParentUserInstanceExceptListContent(0, 3, 3, instanceUUIDsP2, exceptP1,
        ProcessInstanceCriterion.INSTANCE_NUMBER_ASC);
    checkParentUserInstanceExceptListContent(3, 5, 2, instanceUUIDsP2, exceptP1,
        ProcessInstanceCriterion.INSTANCE_NUMBER_ASC);

    getRuntimeAPI().deleteAllProcessInstances(processUUID1);
    getRuntimeAPI().deleteAllProcessInstances(processUUID2);
    getManagementAPI().disable(processUUID1);
    getManagementAPI().disable(processUUID2);
    getManagementAPI().deleteProcess(processUUID1);
    getManagementAPI().deleteProcess(processUUID2);
  }

  public void testGetLightParentUserInstancesExceptOrderByInstanceUUIDAsc() throws Exception {
    ProcessDefinition process1 = getProcess();
    ProcessDefinition process2 = getProcessWithPriorities();

    process1 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process1));
    process2 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process2));

    final ProcessDefinitionUUID processUUID1 = process1.getUUID();
    final ProcessDefinitionUUID processUUID2 = process2.getUUID();

    final int numberOfProcess1 = 10;
    final List<ProcessInstanceUUID> instanceUUIDsP1 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID1, numberOfProcess1, instanceUUIDsP1);

    final int numberOfProcess2 = 5;
    final List<ProcessInstanceUUID> instanceUUIDsP2 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID2, numberOfProcess2, instanceUUIDsP2);

    final Set<ProcessDefinitionUUID> exceptP1 = new HashSet<ProcessDefinitionUUID>();
    exceptP1.add(processUUID1);

    final Set<ProcessDefinitionUUID> exceptP2 = new HashSet<ProcessDefinitionUUID>();
    exceptP2.add(processUUID2);

    Collections.sort(instanceUUIDsP1, new ProcessInstanceUUIDComparator());

    checkParentUserInstanceExceptListContent(0, 5, 5, instanceUUIDsP1, exceptP2,
        ProcessInstanceCriterion.INSTANCE_UUID_ASC);
    checkParentUserInstanceExceptListContent(5, 10, 5, instanceUUIDsP1, exceptP2,
        ProcessInstanceCriterion.INSTANCE_UUID_ASC);
    checkParentUserInstanceExceptListContent(0, 3, 3, instanceUUIDsP2, exceptP1,
        ProcessInstanceCriterion.INSTANCE_UUID_ASC);
    checkParentUserInstanceExceptListContent(3, 5, 2, instanceUUIDsP2, exceptP1,
        ProcessInstanceCriterion.INSTANCE_UUID_ASC);

    getRuntimeAPI().deleteAllProcessInstances(processUUID1);
    getRuntimeAPI().deleteAllProcessInstances(processUUID2);
    getManagementAPI().disable(processUUID1);
    getManagementAPI().disable(processUUID2);
    getManagementAPI().deleteProcess(processUUID1);
    getManagementAPI().deleteProcess(processUUID2);
  }

  public void testGetLightParentUserInstancesExceptOrderByLastUpdateDesc() throws Exception {
    ProcessDefinition process1 = getProcess();
    ProcessDefinition process2 = getProcessWithPriorities();

    process1 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process1));
    process2 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process2));

    final ProcessDefinitionUUID processUUID1 = process1.getUUID();
    final ProcessDefinitionUUID processUUID2 = process2.getUUID();

    final int numberOfProcess1 = 10;
    final List<ProcessInstanceUUID> instanceUUIDsP1 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID1, numberOfProcess1, instanceUUIDsP1);

    final int numberOfProcess2 = 5;
    final List<ProcessInstanceUUID> instanceUUIDsP2 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID2, numberOfProcess2, instanceUUIDsP2);

    final Set<ProcessDefinitionUUID> exceptP1 = new HashSet<ProcessDefinitionUUID>();
    exceptP1.add(processUUID1);

    final Set<ProcessDefinitionUUID> exceptP2 = new HashSet<ProcessDefinitionUUID>();
    exceptP2.add(processUUID2);

    Collections.reverse(instanceUUIDsP1);
    Collections.reverse(instanceUUIDsP2);

    checkParentUserInstanceExceptListContent(0, 5, 5, instanceUUIDsP1, exceptP2,
        ProcessInstanceCriterion.LAST_UPDATE_DESC);
    checkParentUserInstanceExceptListContent(5, 10, 5, instanceUUIDsP1, exceptP2,
        ProcessInstanceCriterion.LAST_UPDATE_DESC);
    checkParentUserInstanceExceptListContent(0, 3, 3, instanceUUIDsP2, exceptP1,
        ProcessInstanceCriterion.LAST_UPDATE_DESC);
    checkParentUserInstanceExceptListContent(3, 5, 2, instanceUUIDsP2, exceptP1,
        ProcessInstanceCriterion.LAST_UPDATE_DESC);

    getRuntimeAPI().deleteAllProcessInstances(processUUID1);
    getRuntimeAPI().deleteAllProcessInstances(processUUID2);
    getManagementAPI().disable(processUUID1);
    getManagementAPI().disable(processUUID2);
    getManagementAPI().deleteProcess(processUUID1);
    getManagementAPI().deleteProcess(processUUID2);
  }

  public void testGetLightParentUserInstancesExceptOrderByStartedDateDesc() throws Exception {
    ProcessDefinition process1 = getProcess();
    ProcessDefinition process2 = getProcessWithPriorities();

    process1 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process1));
    process2 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process2));

    final ProcessDefinitionUUID processUUID1 = process1.getUUID();
    final ProcessDefinitionUUID processUUID2 = process2.getUUID();

    final int numberOfProcess1 = 10;
    final List<ProcessInstanceUUID> instanceUUIDsP1 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID1, numberOfProcess1, instanceUUIDsP1);

    final int numberOfProcess2 = 5;
    final List<ProcessInstanceUUID> instanceUUIDsP2 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID2, numberOfProcess2, instanceUUIDsP2);

    final Set<ProcessDefinitionUUID> exceptP1 = new HashSet<ProcessDefinitionUUID>();
    exceptP1.add(processUUID1);

    final Set<ProcessDefinitionUUID> exceptP2 = new HashSet<ProcessDefinitionUUID>();
    exceptP2.add(processUUID2);

    Collections.reverse(instanceUUIDsP1);
    Collections.reverse(instanceUUIDsP2);
    checkParentUserInstanceExceptListContent(0, 5, 5, instanceUUIDsP1, exceptP2,
        ProcessInstanceCriterion.STARTED_DATE_DESC);
    checkParentUserInstanceExceptListContent(5, 10, 5, instanceUUIDsP1, exceptP2,
        ProcessInstanceCriterion.STARTED_DATE_DESC);
    checkParentUserInstanceExceptListContent(0, 3, 3, instanceUUIDsP2, exceptP1,
        ProcessInstanceCriterion.STARTED_DATE_DESC);
    checkParentUserInstanceExceptListContent(3, 5, 2, instanceUUIDsP2, exceptP1,
        ProcessInstanceCriterion.STARTED_DATE_DESC);

    getRuntimeAPI().deleteAllProcessInstances(processUUID1);
    getRuntimeAPI().deleteAllProcessInstances(processUUID2);
    getManagementAPI().disable(processUUID1);
    getManagementAPI().disable(processUUID2);
    getManagementAPI().deleteProcess(processUUID1);
    getManagementAPI().deleteProcess(processUUID2);
  }

  public void testGetLightParentUserInstancesExceptOrderByEndedDateDesc() throws Exception {
    ProcessDefinition process1 = getProcess();
    ProcessDefinition process2 = getProcessWithPriorities();

    process1 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process1));
    process2 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process2));

    final ProcessDefinitionUUID processUUID1 = process1.getUUID();
    final ProcessDefinitionUUID processUUID2 = process2.getUUID();

    final int numberOfProcess1 = 10;
    final List<ProcessInstanceUUID> instanceUUIDsP1 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID1, numberOfProcess1, instanceUUIDsP1);

    for (final ProcessInstanceUUID processInstanceUUID : instanceUUIDsP1) {
      executeTask(processInstanceUUID, "step1");
      Thread.sleep(3);
    }

    final int numberOfProcess2 = 5;
    final List<ProcessInstanceUUID> instanceUUIDsP2 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID2, numberOfProcess2, instanceUUIDsP2);

    final Set<ProcessDefinitionUUID> exceptP1 = new HashSet<ProcessDefinitionUUID>();
    exceptP1.add(processUUID1);

    final Set<ProcessDefinitionUUID> exceptP2 = new HashSet<ProcessDefinitionUUID>();
    exceptP2.add(processUUID2);

    Collections.reverse(instanceUUIDsP1);
    Collections.reverse(instanceUUIDsP2);
    checkParentUserInstanceExceptListContent(0, 5, 5, instanceUUIDsP1, exceptP2,
        ProcessInstanceCriterion.ENDED_DATE_DESC);
    checkParentUserInstanceExceptListContent(5, 10, 5, instanceUUIDsP1, exceptP2,
        ProcessInstanceCriterion.ENDED_DATE_DESC);
    checkParentUserInstanceExceptListContent(0, 3, 3, instanceUUIDsP2, exceptP1,
        ProcessInstanceCriterion.ENDED_DATE_DESC);
    checkParentUserInstanceExceptListContent(3, 5, 2, instanceUUIDsP2, exceptP1,
        ProcessInstanceCriterion.ENDED_DATE_DESC);

    getRuntimeAPI().deleteAllProcessInstances(processUUID1);
    getRuntimeAPI().deleteAllProcessInstances(processUUID2);
    getManagementAPI().disable(processUUID1);
    getManagementAPI().disable(processUUID2);
    getManagementAPI().deleteProcess(processUUID1);
    getManagementAPI().deleteProcess(processUUID2);
  }

  public void testGetLightParentUserInstancesExceptOrderByInstanceNumberDesc() throws Exception {
    ProcessDefinition process1 = getProcess();
    ProcessDefinition process2 = getProcessWithPriorities();

    process1 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process1));
    process2 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process2));

    final ProcessDefinitionUUID processUUID1 = process1.getUUID();
    final ProcessDefinitionUUID processUUID2 = process2.getUUID();

    final int numberOfProcess1 = 10;
    final List<ProcessInstanceUUID> instanceUUIDsP1 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID1, numberOfProcess1, instanceUUIDsP1);

    final int numberOfProcess2 = 5;
    final List<ProcessInstanceUUID> instanceUUIDsP2 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID2, numberOfProcess2, instanceUUIDsP2);

    final Set<ProcessDefinitionUUID> exceptP1 = new HashSet<ProcessDefinitionUUID>();
    exceptP1.add(processUUID1);

    final Set<ProcessDefinitionUUID> exceptP2 = new HashSet<ProcessDefinitionUUID>();
    exceptP2.add(processUUID2);

    Collections.reverse(instanceUUIDsP1);
    Collections.reverse(instanceUUIDsP2);
    checkParentUserInstanceExceptListContent(0, 5, 5, instanceUUIDsP1, exceptP2,
        ProcessInstanceCriterion.INSTANCE_NUMBER_DESC);
    checkParentUserInstanceExceptListContent(5, 10, 5, instanceUUIDsP1, exceptP2,
        ProcessInstanceCriterion.INSTANCE_NUMBER_DESC);
    checkParentUserInstanceExceptListContent(0, 3, 3, instanceUUIDsP2, exceptP1,
        ProcessInstanceCriterion.INSTANCE_NUMBER_DESC);
    checkParentUserInstanceExceptListContent(3, 5, 2, instanceUUIDsP2, exceptP1,
        ProcessInstanceCriterion.INSTANCE_NUMBER_DESC);

    getRuntimeAPI().deleteAllProcessInstances(processUUID1);
    getRuntimeAPI().deleteAllProcessInstances(processUUID2);
    getManagementAPI().disable(processUUID1);
    getManagementAPI().disable(processUUID2);
    getManagementAPI().deleteProcess(processUUID1);
    getManagementAPI().deleteProcess(processUUID2);
  }

  public void testGetLightParentUserInstancesExceptOrderByInstanceUUIDDesc() throws Exception {
    ProcessDefinition process1 = getProcess();
    ProcessDefinition process2 = getProcessWithPriorities();

    process1 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process1));
    process2 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process2));

    final ProcessDefinitionUUID processUUID1 = process1.getUUID();
    final ProcessDefinitionUUID processUUID2 = process2.getUUID();

    final int numberOfProcess1 = 10;
    final List<ProcessInstanceUUID> instanceUUIDsP1 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID1, numberOfProcess1, instanceUUIDsP1);

    final int numberOfProcess2 = 5;
    final List<ProcessInstanceUUID> instanceUUIDsP2 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID2, numberOfProcess2, instanceUUIDsP2);

    final Set<ProcessDefinitionUUID> exceptP1 = new HashSet<ProcessDefinitionUUID>();
    exceptP1.add(processUUID1);

    final Set<ProcessDefinitionUUID> exceptP2 = new HashSet<ProcessDefinitionUUID>();
    exceptP2.add(processUUID2);

    Collections.sort(instanceUUIDsP1, new ProcessInstanceUUIDComparator());

    Collections.reverse(instanceUUIDsP1);
    Collections.reverse(instanceUUIDsP2);
    checkParentUserInstanceExceptListContent(0, 5, 5, instanceUUIDsP1, exceptP2,
        ProcessInstanceCriterion.INSTANCE_UUID_DESC);
    checkParentUserInstanceExceptListContent(5, 10, 5, instanceUUIDsP1, exceptP2,
        ProcessInstanceCriterion.INSTANCE_UUID_DESC);
    checkParentUserInstanceExceptListContent(0, 3, 3, instanceUUIDsP2, exceptP1,
        ProcessInstanceCriterion.INSTANCE_UUID_DESC);
    checkParentUserInstanceExceptListContent(3, 5, 2, instanceUUIDsP2, exceptP1,
        ProcessInstanceCriterion.INSTANCE_UUID_DESC);

    getRuntimeAPI().deleteAllProcessInstances(processUUID1);
    getRuntimeAPI().deleteAllProcessInstances(processUUID2);
    getManagementAPI().disable(processUUID1);
    getManagementAPI().disable(processUUID2);
    getManagementAPI().deleteProcess(processUUID1);
    getManagementAPI().deleteProcess(processUUID2);
  }

  public void testGetLightParentUserInstancesFromProcessUUIDsOrderByLastUpdateAsc() throws Exception {
    ProcessDefinition process1 = getProcess();
    ProcessDefinition process2 = getProcessWithPriorities();

    process1 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process1));
    process2 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process2));

    final ProcessDefinitionUUID processUUID1 = process1.getUUID();
    final ProcessDefinitionUUID processUUID2 = process2.getUUID();

    final int numberOfProcess1 = 10;
    final List<ProcessInstanceUUID> instanceUUIDsP1 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID1, numberOfProcess1, instanceUUIDsP1);

    final int numberOfProcess2 = 5;
    final List<ProcessInstanceUUID> instanceUUIDsP2 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID2, numberOfProcess2, instanceUUIDsP2);

    final Set<ProcessDefinitionUUID> fromP1 = new HashSet<ProcessDefinitionUUID>();
    fromP1.add(processUUID1);

    final Set<ProcessDefinitionUUID> fromP2 = new HashSet<ProcessDefinitionUUID>();
    fromP2.add(processUUID2);

    checkParentUserInstanceListContent(0, 5, 5, instanceUUIDsP1, fromP1, ProcessInstanceCriterion.LAST_UPDATE_ASC);
    checkParentUserInstanceListContent(5, 10, 5, instanceUUIDsP1, fromP1, ProcessInstanceCriterion.LAST_UPDATE_ASC);
    checkParentUserInstanceListContent(0, 3, 3, instanceUUIDsP2, fromP2, ProcessInstanceCriterion.LAST_UPDATE_ASC);
    checkParentUserInstanceListContent(3, 5, 2, instanceUUIDsP2, fromP2, ProcessInstanceCriterion.LAST_UPDATE_ASC);

    getRuntimeAPI().deleteAllProcessInstances(processUUID1);
    getRuntimeAPI().deleteAllProcessInstances(processUUID2);
    getManagementAPI().disable(processUUID1);
    getManagementAPI().disable(processUUID2);
    getManagementAPI().deleteProcess(processUUID1);
    getManagementAPI().deleteProcess(processUUID2);
  }

  public void testGetLightParentUserInstancesFromProcessUUIDsOrderByStartedDateAsc() throws Exception {
    ProcessDefinition process1 = getProcess();
    ProcessDefinition process2 = getProcessWithPriorities();

    process1 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process1));
    process2 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process2));

    final ProcessDefinitionUUID processUUID1 = process1.getUUID();
    final ProcessDefinitionUUID processUUID2 = process2.getUUID();

    final int numberOfProcess1 = 10;
    final List<ProcessInstanceUUID> instanceUUIDsP1 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID1, numberOfProcess1, instanceUUIDsP1);

    final int numberOfProcess2 = 5;
    final List<ProcessInstanceUUID> instanceUUIDsP2 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID2, numberOfProcess2, instanceUUIDsP2);

    final Set<ProcessDefinitionUUID> fromP1 = new HashSet<ProcessDefinitionUUID>();
    fromP1.add(processUUID1);

    final Set<ProcessDefinitionUUID> fromP2 = new HashSet<ProcessDefinitionUUID>();
    fromP2.add(processUUID2);

    checkParentUserInstanceListContent(0, 5, 5, instanceUUIDsP1, fromP1, ProcessInstanceCriterion.STARTED_DATE_ASC);
    checkParentUserInstanceListContent(5, 10, 5, instanceUUIDsP1, fromP1, ProcessInstanceCriterion.STARTED_DATE_ASC);
    checkParentUserInstanceListContent(0, 3, 3, instanceUUIDsP2, fromP2, ProcessInstanceCriterion.STARTED_DATE_ASC);
    checkParentUserInstanceListContent(3, 5, 2, instanceUUIDsP2, fromP2, ProcessInstanceCriterion.STARTED_DATE_ASC);

    getRuntimeAPI().deleteAllProcessInstances(processUUID1);
    getRuntimeAPI().deleteAllProcessInstances(processUUID2);
    getManagementAPI().disable(processUUID1);
    getManagementAPI().disable(processUUID2);
    getManagementAPI().deleteProcess(processUUID1);
    getManagementAPI().deleteProcess(processUUID2);
  }

  public void testGetLightParentUserInstancesFromProcessUUIDsOrderByEndedDateAsc() throws Exception {
    ProcessDefinition process1 = getProcess();
    ProcessDefinition process2 = getProcessWithPriorities();

    process1 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process1));
    process2 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process2));

    final ProcessDefinitionUUID processUUID1 = process1.getUUID();
    final ProcessDefinitionUUID processUUID2 = process2.getUUID();

    final int numberOfProcess1 = 10;
    final List<ProcessInstanceUUID> instanceUUIDsP1 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID1, numberOfProcess1, instanceUUIDsP1);

    for (final ProcessInstanceUUID processInstanceUUID : instanceUUIDsP1) {
      executeTask(processInstanceUUID, "step1");
      Thread.sleep(3);
    }

    final int numberOfProcess2 = 5;
    final List<ProcessInstanceUUID> instanceUUIDsP2 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID2, numberOfProcess2, instanceUUIDsP2);

    final Set<ProcessDefinitionUUID> fromP1 = new HashSet<ProcessDefinitionUUID>();
    fromP1.add(processUUID1);

    final Set<ProcessDefinitionUUID> fromP2 = new HashSet<ProcessDefinitionUUID>();
    fromP2.add(processUUID2);

    checkParentUserInstanceListContent(0, 5, 5, instanceUUIDsP1, fromP1, ProcessInstanceCriterion.ENDED_DATE_ASC);
    checkParentUserInstanceListContent(5, 10, 5, instanceUUIDsP1, fromP1, ProcessInstanceCriterion.ENDED_DATE_ASC);
    checkParentUserInstanceListContent(0, 3, 3, instanceUUIDsP2, fromP2, ProcessInstanceCriterion.ENDED_DATE_ASC);
    checkParentUserInstanceListContent(3, 5, 2, instanceUUIDsP2, fromP2, ProcessInstanceCriterion.ENDED_DATE_ASC);

    getRuntimeAPI().deleteAllProcessInstances(processUUID1);
    getRuntimeAPI().deleteAllProcessInstances(processUUID2);
    getManagementAPI().disable(processUUID1);
    getManagementAPI().disable(processUUID2);
    getManagementAPI().deleteProcess(processUUID1);
    getManagementAPI().deleteProcess(processUUID2);
  }

  public void testGetLightParentUserInstancesFromProcessUUIDsOrderByInstanceNumberAsc() throws Exception {
    ProcessDefinition process1 = getProcess();
    ProcessDefinition process2 = getProcessWithPriorities();

    process1 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process1));
    process2 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process2));

    final ProcessDefinitionUUID processUUID1 = process1.getUUID();
    final ProcessDefinitionUUID processUUID2 = process2.getUUID();

    final int numberOfProcess1 = 10;
    final List<ProcessInstanceUUID> instanceUUIDsP1 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID1, numberOfProcess1, instanceUUIDsP1);

    final int numberOfProcess2 = 5;
    final List<ProcessInstanceUUID> instanceUUIDsP2 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID2, numberOfProcess2, instanceUUIDsP2);

    final Set<ProcessDefinitionUUID> fromP1 = new HashSet<ProcessDefinitionUUID>();
    fromP1.add(processUUID1);

    final Set<ProcessDefinitionUUID> fromP2 = new HashSet<ProcessDefinitionUUID>();
    fromP2.add(processUUID2);

    checkParentUserInstanceListContent(0, 5, 5, instanceUUIDsP1, fromP1, ProcessInstanceCriterion.INSTANCE_NUMBER_ASC);
    checkParentUserInstanceListContent(5, 10, 5, instanceUUIDsP1, fromP1, ProcessInstanceCriterion.INSTANCE_NUMBER_ASC);
    checkParentUserInstanceListContent(0, 3, 3, instanceUUIDsP2, fromP2, ProcessInstanceCriterion.INSTANCE_NUMBER_ASC);
    checkParentUserInstanceListContent(3, 5, 2, instanceUUIDsP2, fromP2, ProcessInstanceCriterion.INSTANCE_NUMBER_ASC);

    getRuntimeAPI().deleteAllProcessInstances(processUUID1);
    getRuntimeAPI().deleteAllProcessInstances(processUUID2);
    getManagementAPI().disable(processUUID1);
    getManagementAPI().disable(processUUID2);
    getManagementAPI().deleteProcess(processUUID1);
    getManagementAPI().deleteProcess(processUUID2);
  }

  public void testGetLightParentUserInstancesFromProcessUUIDsOrderByInstanceUUIDAsc() throws Exception {
    ProcessDefinition process1 = getProcess();
    ProcessDefinition process2 = getProcessWithPriorities();

    process1 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process1));
    process2 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process2));

    final ProcessDefinitionUUID processUUID1 = process1.getUUID();
    final ProcessDefinitionUUID processUUID2 = process2.getUUID();

    final int numberOfProcess1 = 10;
    final List<ProcessInstanceUUID> instanceUUIDsP1 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID1, numberOfProcess1, instanceUUIDsP1);

    final int numberOfProcess2 = 5;
    final List<ProcessInstanceUUID> instanceUUIDsP2 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID2, numberOfProcess2, instanceUUIDsP2);

    final Set<ProcessDefinitionUUID> fromP1 = new HashSet<ProcessDefinitionUUID>();
    fromP1.add(processUUID1);

    final Set<ProcessDefinitionUUID> fromP2 = new HashSet<ProcessDefinitionUUID>();
    fromP2.add(processUUID2);

    Collections.sort(instanceUUIDsP1, new ProcessInstanceUUIDComparator());

    checkParentUserInstanceListContent(0, 5, 5, instanceUUIDsP1, fromP1, ProcessInstanceCriterion.INSTANCE_UUID_ASC);
    checkParentUserInstanceListContent(5, 10, 5, instanceUUIDsP1, fromP1, ProcessInstanceCriterion.INSTANCE_UUID_ASC);
    checkParentUserInstanceListContent(0, 3, 3, instanceUUIDsP2, fromP2, ProcessInstanceCriterion.INSTANCE_UUID_ASC);
    checkParentUserInstanceListContent(3, 5, 2, instanceUUIDsP2, fromP2, ProcessInstanceCriterion.INSTANCE_UUID_ASC);

    getRuntimeAPI().deleteAllProcessInstances(processUUID1);
    getRuntimeAPI().deleteAllProcessInstances(processUUID2);
    getManagementAPI().disable(processUUID1);
    getManagementAPI().disable(processUUID2);
    getManagementAPI().deleteProcess(processUUID1);
    getManagementAPI().deleteProcess(processUUID2);
  }

  public void testGetLightParentUserInstancesFromProcessUUIDsOrderByLastUpdateDesc() throws Exception {
    ProcessDefinition process1 = getProcess();
    ProcessDefinition process2 = getProcessWithPriorities();

    process1 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process1));
    process2 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process2));

    final ProcessDefinitionUUID processUUID1 = process1.getUUID();
    final ProcessDefinitionUUID processUUID2 = process2.getUUID();

    final int numberOfProcess1 = 10;
    final List<ProcessInstanceUUID> instanceUUIDsP1 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID1, numberOfProcess1, instanceUUIDsP1);

    final int numberOfProcess2 = 5;
    final List<ProcessInstanceUUID> instanceUUIDsP2 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID2, numberOfProcess2, instanceUUIDsP2);

    final Set<ProcessDefinitionUUID> fromP1 = new HashSet<ProcessDefinitionUUID>();
    fromP1.add(processUUID1);

    final Set<ProcessDefinitionUUID> fromP2 = new HashSet<ProcessDefinitionUUID>();
    fromP2.add(processUUID2);

    Collections.reverse(instanceUUIDsP1);
    Collections.reverse(instanceUUIDsP2);

    checkParentUserInstanceListContent(0, 5, 5, instanceUUIDsP1, fromP1, ProcessInstanceCriterion.LAST_UPDATE_DESC);
    checkParentUserInstanceListContent(5, 10, 5, instanceUUIDsP1, fromP1, ProcessInstanceCriterion.LAST_UPDATE_DESC);
    checkParentUserInstanceListContent(0, 3, 3, instanceUUIDsP2, fromP2, ProcessInstanceCriterion.LAST_UPDATE_DESC);
    checkParentUserInstanceListContent(3, 5, 2, instanceUUIDsP2, fromP2, ProcessInstanceCriterion.LAST_UPDATE_DESC);

    getRuntimeAPI().deleteAllProcessInstances(processUUID1);
    getRuntimeAPI().deleteAllProcessInstances(processUUID2);
    getManagementAPI().disable(processUUID1);
    getManagementAPI().disable(processUUID2);
    getManagementAPI().deleteProcess(processUUID1);
    getManagementAPI().deleteProcess(processUUID2);
  }

  public void testGetLightParentUserInstancesFromProcessUUIDsOrderByStartedDateDesc() throws Exception {
    ProcessDefinition process1 = getProcess();
    ProcessDefinition process2 = getProcessWithPriorities();

    process1 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process1));
    process2 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process2));

    final ProcessDefinitionUUID processUUID1 = process1.getUUID();
    final ProcessDefinitionUUID processUUID2 = process2.getUUID();

    final int numberOfProcess1 = 10;
    final List<ProcessInstanceUUID> instanceUUIDsP1 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID1, numberOfProcess1, instanceUUIDsP1);

    final int numberOfProcess2 = 5;
    final List<ProcessInstanceUUID> instanceUUIDsP2 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID2, numberOfProcess2, instanceUUIDsP2);

    final Set<ProcessDefinitionUUID> fromP1 = new HashSet<ProcessDefinitionUUID>();
    fromP1.add(processUUID1);

    final Set<ProcessDefinitionUUID> fromP2 = new HashSet<ProcessDefinitionUUID>();
    fromP2.add(processUUID2);

    Collections.reverse(instanceUUIDsP1);
    Collections.reverse(instanceUUIDsP2);

    checkParentUserInstanceListContent(0, 5, 5, instanceUUIDsP1, fromP1, ProcessInstanceCriterion.STARTED_DATE_DESC);
    checkParentUserInstanceListContent(5, 10, 5, instanceUUIDsP1, fromP1, ProcessInstanceCriterion.STARTED_DATE_DESC);
    checkParentUserInstanceListContent(0, 3, 3, instanceUUIDsP2, fromP2, ProcessInstanceCriterion.STARTED_DATE_DESC);
    checkParentUserInstanceListContent(3, 5, 2, instanceUUIDsP2, fromP2, ProcessInstanceCriterion.STARTED_DATE_DESC);

    getRuntimeAPI().deleteAllProcessInstances(processUUID1);
    getRuntimeAPI().deleteAllProcessInstances(processUUID2);
    getManagementAPI().disable(processUUID1);
    getManagementAPI().disable(processUUID2);
    getManagementAPI().deleteProcess(processUUID1);
    getManagementAPI().deleteProcess(processUUID2);
  }

  public void testGetLightParentUserInstancesFromProcessUUIDsOrderByEndedDateDesc() throws Exception {
    ProcessDefinition process1 = getProcess();
    ProcessDefinition process2 = getProcessWithPriorities();

    process1 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process1));
    process2 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process2));

    final ProcessDefinitionUUID processUUID1 = process1.getUUID();
    final ProcessDefinitionUUID processUUID2 = process2.getUUID();

    final int numberOfProcess1 = 10;
    final List<ProcessInstanceUUID> instanceUUIDsP1 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID1, numberOfProcess1, instanceUUIDsP1);

    for (final ProcessInstanceUUID processInstanceUUID : instanceUUIDsP1) {
      executeTask(processInstanceUUID, "step1");
      Thread.sleep(3);
    }

    final int numberOfProcess2 = 5;
    final List<ProcessInstanceUUID> instanceUUIDsP2 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID2, numberOfProcess2, instanceUUIDsP2);

    final Set<ProcessDefinitionUUID> fromP1 = new HashSet<ProcessDefinitionUUID>();
    fromP1.add(processUUID1);

    final Set<ProcessDefinitionUUID> fromP2 = new HashSet<ProcessDefinitionUUID>();
    fromP2.add(processUUID2);

    Collections.reverse(instanceUUIDsP1);
    Collections.reverse(instanceUUIDsP2);

    checkParentUserInstanceListContent(0, 5, 5, instanceUUIDsP1, fromP1, ProcessInstanceCriterion.ENDED_DATE_DESC);
    checkParentUserInstanceListContent(5, 10, 5, instanceUUIDsP1, fromP1, ProcessInstanceCriterion.ENDED_DATE_DESC);
    checkParentUserInstanceListContent(0, 3, 3, instanceUUIDsP2, fromP2, ProcessInstanceCriterion.ENDED_DATE_DESC);
    checkParentUserInstanceListContent(3, 5, 2, instanceUUIDsP2, fromP2, ProcessInstanceCriterion.ENDED_DATE_DESC);

    getRuntimeAPI().deleteAllProcessInstances(processUUID1);
    getRuntimeAPI().deleteAllProcessInstances(processUUID2);
    getManagementAPI().disable(processUUID1);
    getManagementAPI().disable(processUUID2);
    getManagementAPI().deleteProcess(processUUID1);
    getManagementAPI().deleteProcess(processUUID2);
  }

  public void testGetLightParentUserInstancesFromProcessUUIDsOrderByInstanceNumberDesc() throws Exception {
    ProcessDefinition process1 = getProcess();
    ProcessDefinition process2 = getProcessWithPriorities();

    process1 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process1));
    process2 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process2));

    final ProcessDefinitionUUID processUUID1 = process1.getUUID();
    final ProcessDefinitionUUID processUUID2 = process2.getUUID();

    final int numberOfProcess1 = 10;
    final List<ProcessInstanceUUID> instanceUUIDsP1 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID1, numberOfProcess1, instanceUUIDsP1);

    final int numberOfProcess2 = 5;
    final List<ProcessInstanceUUID> instanceUUIDsP2 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID2, numberOfProcess2, instanceUUIDsP2);

    final Set<ProcessDefinitionUUID> fromP1 = new HashSet<ProcessDefinitionUUID>();
    fromP1.add(processUUID1);

    final Set<ProcessDefinitionUUID> fromP2 = new HashSet<ProcessDefinitionUUID>();
    fromP2.add(processUUID2);

    Collections.reverse(instanceUUIDsP1);
    Collections.reverse(instanceUUIDsP2);

    checkParentUserInstanceListContent(0, 5, 5, instanceUUIDsP1, fromP1, ProcessInstanceCriterion.INSTANCE_NUMBER_DESC);
    checkParentUserInstanceListContent(5, 10, 5, instanceUUIDsP1, fromP1, ProcessInstanceCriterion.INSTANCE_NUMBER_DESC);
    checkParentUserInstanceListContent(0, 3, 3, instanceUUIDsP2, fromP2, ProcessInstanceCriterion.INSTANCE_NUMBER_DESC);
    checkParentUserInstanceListContent(3, 5, 2, instanceUUIDsP2, fromP2, ProcessInstanceCriterion.INSTANCE_NUMBER_DESC);

    getRuntimeAPI().deleteAllProcessInstances(processUUID1);
    getRuntimeAPI().deleteAllProcessInstances(processUUID2);
    getManagementAPI().disable(processUUID1);
    getManagementAPI().disable(processUUID2);
    getManagementAPI().deleteProcess(processUUID1);
    getManagementAPI().deleteProcess(processUUID2);
  }

  public void testGetLightParentUserInstancesFromProcessUUIDsOrderByInstanceUUIDDesc() throws Exception {
    ProcessDefinition process1 = getProcess();
    ProcessDefinition process2 = getProcessWithPriorities();

    process1 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process1));
    process2 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process2));

    final ProcessDefinitionUUID processUUID1 = process1.getUUID();
    final ProcessDefinitionUUID processUUID2 = process2.getUUID();

    final int numberOfProcess1 = 10;
    final List<ProcessInstanceUUID> instanceUUIDsP1 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID1, numberOfProcess1, instanceUUIDsP1);

    final int numberOfProcess2 = 5;
    final List<ProcessInstanceUUID> instanceUUIDsP2 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID2, numberOfProcess2, instanceUUIDsP2);

    final Set<ProcessDefinitionUUID> fromP1 = new HashSet<ProcessDefinitionUUID>();
    fromP1.add(processUUID1);

    final Set<ProcessDefinitionUUID> fromP2 = new HashSet<ProcessDefinitionUUID>();
    fromP2.add(processUUID2);

    Collections.sort(instanceUUIDsP1, new ProcessInstanceUUIDComparator());
    Collections.reverse(instanceUUIDsP1);
    Collections.reverse(instanceUUIDsP2);

    checkParentUserInstanceListContent(0, 5, 5, instanceUUIDsP1, fromP1, ProcessInstanceCriterion.INSTANCE_UUID_DESC);
    checkParentUserInstanceListContent(5, 10, 5, instanceUUIDsP1, fromP1, ProcessInstanceCriterion.INSTANCE_UUID_DESC);
    checkParentUserInstanceListContent(0, 3, 3, instanceUUIDsP2, fromP2, ProcessInstanceCriterion.INSTANCE_UUID_DESC);
    checkParentUserInstanceListContent(3, 5, 2, instanceUUIDsP2, fromP2, ProcessInstanceCriterion.INSTANCE_UUID_DESC);

    getRuntimeAPI().deleteAllProcessInstances(processUUID1);
    getRuntimeAPI().deleteAllProcessInstances(processUUID2);
    getManagementAPI().disable(processUUID1);
    getManagementAPI().disable(processUUID2);
    getManagementAPI().deleteProcess(processUUID1);
    getManagementAPI().deleteProcess(processUUID2);
  }

  public void testGetLightParentProcessInstancesWithActiveUserOrderByLastUpdateAsc() throws Exception {
    ProcessDefinition process = getProcess();

    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final int numberOfProcess = 10;
    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID, numberOfProcess, instanceUUIDs);

    updateInstanceVariable(instanceUUIDs, "text", "Hello", true);

    Collections.reverse(instanceUUIDs);
    checkParentProcessInstancesWithActiveUserListContent("john", 0, 5, 5, instanceUUIDs,
        ProcessInstanceCriterion.LAST_UPDATE_ASC);
    checkParentProcessInstancesWithActiveUserListContent("john", 5, 10, 5, instanceUUIDs,
        ProcessInstanceCriterion.LAST_UPDATE_ASC);

    getRuntimeAPI().deleteAllProcessInstances(processUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetLightParentProcessInstancesWithActiveUserOrderByStartedDateAsc() throws Exception {
    ProcessDefinition process = getProcess();

    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final int numberOfProcess = 10;
    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID, numberOfProcess, instanceUUIDs);

    updateInstanceVariable(instanceUUIDs, "text", "Hello", true);

    checkParentProcessInstancesWithActiveUserListContent("john", 0, 5, 5, instanceUUIDs,
        ProcessInstanceCriterion.STARTED_DATE_ASC);
    checkParentProcessInstancesWithActiveUserListContent("john", 5, 10, 5, instanceUUIDs,
        ProcessInstanceCriterion.STARTED_DATE_ASC);

    getRuntimeAPI().deleteAllProcessInstances(processUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetLightParentProcessInstancesWithActiveUserOrderByEndedDateAsc() throws Exception {
    ProcessDefinition process = getProcess();

    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final int numberOfProcess = 10;
    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID, numberOfProcess, instanceUUIDs);

    updateInstanceVariable(instanceUUIDs, "text", "Hello", true);

    // The instance is not finished, so all end dates will be empty
    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithActiveUser(
        "john", 0, 5, ProcessInstanceCriterion.ENDED_DATE_ASC);
    assertEquals(5, retrievedInstances.size());
    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithActiveUser("john", 5, 10,
        ProcessInstanceCriterion.ENDED_DATE_ASC);
    assertEquals(5, retrievedInstances.size());

    for (int i = numberOfProcess - 1; i >= 0; i--) {
      executeTask(instanceUUIDs.get(i), "step1");
      Thread.sleep(3);
    }

    getRuntimeAPI().deleteAllProcessInstances(processUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetLightParentProcessInstancesWithActiveUserOrderByInstanceNumberAsc() throws Exception {
    ProcessDefinition process = getProcess();

    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final int numberOfProcess = 10;
    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID, numberOfProcess, instanceUUIDs);

    updateInstanceVariable(instanceUUIDs, "text", "Hello", true);

    checkParentProcessInstancesWithActiveUserListContent("john", 0, 5, 5, instanceUUIDs,
        ProcessInstanceCriterion.INSTANCE_NUMBER_ASC);
    checkParentProcessInstancesWithActiveUserListContent("john", 5, 10, 5, instanceUUIDs,
        ProcessInstanceCriterion.INSTANCE_NUMBER_ASC);

    getRuntimeAPI().deleteAllProcessInstances(processUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetLightParentProcessInstancesWithActiveUserOrderByInstanceUUIDAsc() throws Exception {
    ProcessDefinition process = getProcess();

    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final int numberOfProcess = 10;
    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID, numberOfProcess, instanceUUIDs);

    updateInstanceVariable(instanceUUIDs, "text", "Hello", true);

    Collections.sort(instanceUUIDs, new ProcessInstanceUUIDComparator());

    checkParentProcessInstancesWithActiveUserListContent("john", 0, 5, 5, instanceUUIDs,
        ProcessInstanceCriterion.INSTANCE_UUID_ASC);
    checkParentProcessInstancesWithActiveUserListContent("john", 5, 10, 5, instanceUUIDs,
        ProcessInstanceCriterion.INSTANCE_UUID_ASC);

    getRuntimeAPI().deleteAllProcessInstances(processUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetLightParentProcessInstancesWithActiveUserOrderByLastUpdateDesc() throws Exception {
    ProcessDefinition process = getProcess();

    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final int numberOfProcess = 10;
    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID, numberOfProcess, instanceUUIDs);

    updateInstanceVariable(instanceUUIDs, "text", "Hello", true);

    checkParentProcessInstancesWithActiveUserListContent("john", 0, 5, 5, instanceUUIDs,
        ProcessInstanceCriterion.LAST_UPDATE_DESC);
    checkParentProcessInstancesWithActiveUserListContent("john", 5, 10, 5, instanceUUIDs,
        ProcessInstanceCriterion.LAST_UPDATE_DESC);

    getRuntimeAPI().deleteAllProcessInstances(processUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetLightParentProcessInstancesWithActiveUserOrderByStartedDateDesc() throws Exception {
    ProcessDefinition process = getProcess();

    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final int numberOfProcess = 10;
    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID, numberOfProcess, instanceUUIDs);

    updateInstanceVariable(instanceUUIDs, "text", "Hello", true);

    Collections.reverse(instanceUUIDs);
    checkParentProcessInstancesWithActiveUserListContent("john", 0, 5, 5, instanceUUIDs,
        ProcessInstanceCriterion.STARTED_DATE_DESC);
    checkParentProcessInstancesWithActiveUserListContent("john", 5, 10, 5, instanceUUIDs,
        ProcessInstanceCriterion.STARTED_DATE_DESC);

    getRuntimeAPI().deleteAllProcessInstances(processUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetLightParentProcessInstancesWithActiveUserOrderByEndedDateDesc() throws Exception {
    ProcessDefinition process = getProcess();

    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final int numberOfProcess = 10;
    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID, numberOfProcess, instanceUUIDs);

    updateInstanceVariable(instanceUUIDs, "text", "Hello", true);

    // The instance is not finished, so all end dates will be empty
    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithActiveUser(
        "john", 0, 5, ProcessInstanceCriterion.ENDED_DATE_DESC);
    assertEquals(5, retrievedInstances.size());
    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithActiveUser("john", 5, 10,
        ProcessInstanceCriterion.ENDED_DATE_DESC);
    assertEquals(5, retrievedInstances.size());

    for (int i = numberOfProcess - 1; i >= 0; i--) {
      executeTask(instanceUUIDs.get(i), "step1");
      Thread.sleep(3);
    }

    getRuntimeAPI().deleteAllProcessInstances(processUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetLightParentProcessInstancesWithActiveUserOrderByInstanceNumberDesc() throws Exception {
    ProcessDefinition process = getProcess();

    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final int numberOfProcess = 10;
    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID, numberOfProcess, instanceUUIDs);

    updateInstanceVariable(instanceUUIDs, "text", "Hello", true);

    Collections.reverse(instanceUUIDs);
    checkParentProcessInstancesWithActiveUserListContent("john", 0, 5, 5, instanceUUIDs,
        ProcessInstanceCriterion.INSTANCE_NUMBER_DESC);
    checkParentProcessInstancesWithActiveUserListContent("john", 5, 10, 5, instanceUUIDs,
        ProcessInstanceCriterion.INSTANCE_NUMBER_DESC);

    getRuntimeAPI().deleteAllProcessInstances(processUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetLightParentProcessInstancesWithActiveUserOrderByInstanceUUIDDesc() throws Exception {
    ProcessDefinition process = getProcess();

    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final int numberOfProcess = 10;
    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID, numberOfProcess, instanceUUIDs);

    updateInstanceVariable(instanceUUIDs, "text", "Hello", true);

    Collections.sort(instanceUUIDs, new ProcessInstanceUUIDComparator());
    Collections.reverse(instanceUUIDs);

    checkParentProcessInstancesWithActiveUserListContent("john", 0, 5, 5, instanceUUIDs,
        ProcessInstanceCriterion.INSTANCE_UUID_DESC);
    checkParentProcessInstancesWithActiveUserListContent("john", 5, 10, 5, instanceUUIDs,
        ProcessInstanceCriterion.INSTANCE_UUID_DESC);

    getRuntimeAPI().deleteAllProcessInstances(processUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetLightParentProcessInstancesWithActiveUserFromProcessUUIDsOrderByLastUpdateAsc() throws Exception {
    ProcessDefinition process1 = getProcess();
    ProcessDefinition process2 = getProcess2();

    process1 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process1));
    process2 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process2));

    final ProcessDefinitionUUID processUUID1 = process1.getUUID();
    final ProcessDefinitionUUID processUUID2 = process2.getUUID();

    final int numberOfProcess1 = 10;
    final List<ProcessInstanceUUID> instanceUUIDsP1 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID1, numberOfProcess1, instanceUUIDsP1);

    updateInstanceVariable(instanceUUIDsP1, "text", "Hello", true);

    final int numberOfProcess2 = 5;
    final List<ProcessInstanceUUID> instanceUUIDsP2 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID2, numberOfProcess2, instanceUUIDsP2);

    updateInstanceVariable(instanceUUIDsP2, "text", "Hello", false);

    final Set<ProcessDefinitionUUID> fromP1 = new HashSet<ProcessDefinitionUUID>();
    fromP1.add(processUUID1);

    final Set<ProcessDefinitionUUID> fromP2 = new HashSet<ProcessDefinitionUUID>();
    fromP2.add(processUUID2);

    Collections.reverse(instanceUUIDsP1);

    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithActiveUser(
        "john", 0, 5, fromP1, ProcessInstanceCriterion.LAST_UPDATE_ASC);
    checkListContent(0, 5, 5, instanceUUIDsP1, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithActiveUser("john", 5, 10, fromP1,
        ProcessInstanceCriterion.LAST_UPDATE_ASC);
    checkListContent(5, 10, 5, instanceUUIDsP1, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithActiveUser("john", 0, 3, fromP2,
        ProcessInstanceCriterion.LAST_UPDATE_ASC);
    checkListContent(0, 3, 3, instanceUUIDsP2, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithActiveUser("john", 3, 5, fromP2,
        ProcessInstanceCriterion.LAST_UPDATE_ASC);
    checkListContent(3, 5, 2, instanceUUIDsP2, retrievedInstances);

    getRuntimeAPI().deleteAllProcessInstances(processUUID1);
    getRuntimeAPI().deleteAllProcessInstances(processUUID2);
    getManagementAPI().disable(processUUID1);
    getManagementAPI().disable(processUUID2);
    getManagementAPI().deleteProcess(processUUID1);
    getManagementAPI().deleteProcess(processUUID2);
  }

  public void testGetLightParentProcessInstancesWithActiveUserFromProcessUUIDsOrderByStartedDateAsc() throws Exception {
    ProcessDefinition process1 = getProcess();
    ProcessDefinition process2 = getProcess2();

    process1 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process1));
    process2 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process2));

    final ProcessDefinitionUUID processUUID1 = process1.getUUID();
    final ProcessDefinitionUUID processUUID2 = process2.getUUID();

    final int numberOfProcess1 = 10;
    final List<ProcessInstanceUUID> instanceUUIDsP1 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID1, numberOfProcess1, instanceUUIDsP1);

    updateInstanceVariable(instanceUUIDsP1, "text", "Hello", true);

    final int numberOfProcess2 = 5;
    final List<ProcessInstanceUUID> instanceUUIDsP2 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID2, numberOfProcess2, instanceUUIDsP2);

    for (int i = 0; i < numberOfProcess2; i++) {
      getRuntimeAPI().setProcessInstanceVariable(instanceUUIDsP2.get(i), "text", "hello");
      Thread.sleep(3);
    }

    final Set<ProcessDefinitionUUID> fromP1 = new HashSet<ProcessDefinitionUUID>();
    fromP1.add(processUUID1);

    final Set<ProcessDefinitionUUID> fromP2 = new HashSet<ProcessDefinitionUUID>();
    fromP2.add(processUUID2);

    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithActiveUser(
        "john", 0, 5, fromP1, ProcessInstanceCriterion.STARTED_DATE_ASC);
    checkListContent(0, 5, 5, instanceUUIDsP1, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithActiveUser("john", 5, 10, fromP1,
        ProcessInstanceCriterion.STARTED_DATE_ASC);
    checkListContent(5, 10, 5, instanceUUIDsP1, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithActiveUser("john", 0, 3, fromP2,
        ProcessInstanceCriterion.STARTED_DATE_ASC);
    checkListContent(0, 3, 3, instanceUUIDsP2, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithActiveUser("john", 3, 5, fromP2,
        ProcessInstanceCriterion.STARTED_DATE_ASC);
    checkListContent(3, 5, 2, instanceUUIDsP2, retrievedInstances);

    getRuntimeAPI().deleteAllProcessInstances(processUUID1);
    getRuntimeAPI().deleteAllProcessInstances(processUUID2);
    getManagementAPI().disable(processUUID1);
    getManagementAPI().disable(processUUID2);
    getManagementAPI().deleteProcess(processUUID1);
    getManagementAPI().deleteProcess(processUUID2);
  }

  public void testGetLightParentProcessInstancesWithActiveUserFromProcessUUIDsOrderByEndedDateAsc() throws Exception {
    ProcessDefinition process1 = getProcess();
    ProcessDefinition process2 = getProcess2();

    process1 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process1));
    process2 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process2));

    final ProcessDefinitionUUID processUUID1 = process1.getUUID();
    final ProcessDefinitionUUID processUUID2 = process2.getUUID();

    final int numberOfProcess1 = 10;
    final List<ProcessInstanceUUID> instanceUUIDsP1 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID1, numberOfProcess1, instanceUUIDsP1);

    final int numberOfProcess2 = 5;
    final List<ProcessInstanceUUID> instanceUUIDsP2 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID2, numberOfProcess2, instanceUUIDsP2);

    final Set<ProcessDefinitionUUID> fromP1 = new HashSet<ProcessDefinitionUUID>();
    fromP1.add(processUUID1);

    final Set<ProcessDefinitionUUID> fromP2 = new HashSet<ProcessDefinitionUUID>();
    fromP2.add(processUUID2);

    // there is no ended date, the instance is not finished
    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithActiveUser(
        "john", 0, 5, fromP1, ProcessInstanceCriterion.ENDED_DATE_ASC);
    assertEquals(5, retrievedInstances.size());

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithActiveUser("john", 5, 10, fromP1,
        ProcessInstanceCriterion.ENDED_DATE_ASC);
    assertEquals(5, retrievedInstances.size());

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithActiveUser("john", 0, 3, fromP2,
        ProcessInstanceCriterion.ENDED_DATE_ASC);
    assertEquals(3, retrievedInstances.size());

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithActiveUser("john", 3, 5, fromP2,
        ProcessInstanceCriterion.ENDED_DATE_ASC);
    assertEquals(2, retrievedInstances.size());

    getManagementAPI().deleteProcess(processUUID1);
    getManagementAPI().deleteProcess(processUUID2);
  }

  public void testGetLightParentProcessInstancesWithActiveUserFromProcessUUIDsOrderByInstanceNumberAsc()
      throws Exception {
    ProcessDefinition process1 = getProcess();
    ProcessDefinition process2 = getProcess2();

    process1 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process1));
    process2 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process2));

    final ProcessDefinitionUUID processUUID1 = process1.getUUID();
    final ProcessDefinitionUUID processUUID2 = process2.getUUID();

    final int numberOfProcess1 = 10;
    final List<ProcessInstanceUUID> instanceUUIDsP1 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID1, numberOfProcess1, instanceUUIDsP1);

    updateInstanceVariable(instanceUUIDsP1, "text", "Hello", true);

    final int numberOfProcess2 = 5;
    final List<ProcessInstanceUUID> instanceUUIDsP2 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID2, numberOfProcess2, instanceUUIDsP2);

    for (int i = 0; i < numberOfProcess2; i++) {
      getRuntimeAPI().setProcessInstanceVariable(instanceUUIDsP2.get(i), "text", "hello");
      Thread.sleep(3);
    }

    final Set<ProcessDefinitionUUID> fromP1 = new HashSet<ProcessDefinitionUUID>();
    fromP1.add(processUUID1);

    final Set<ProcessDefinitionUUID> fromP2 = new HashSet<ProcessDefinitionUUID>();
    fromP2.add(processUUID2);

    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithActiveUser(
        "john", 0, 5, fromP1, ProcessInstanceCriterion.INSTANCE_NUMBER_ASC);
    checkListContent(0, 5, 5, instanceUUIDsP1, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithActiveUser("john", 5, 10, fromP1,
        ProcessInstanceCriterion.INSTANCE_NUMBER_ASC);
    checkListContent(5, 10, 5, instanceUUIDsP1, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithActiveUser("john", 0, 3, fromP2,
        ProcessInstanceCriterion.INSTANCE_NUMBER_ASC);
    checkListContent(0, 3, 3, instanceUUIDsP2, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithActiveUser("john", 3, 5, fromP2,
        ProcessInstanceCriterion.INSTANCE_NUMBER_ASC);
    checkListContent(3, 5, 2, instanceUUIDsP2, retrievedInstances);

    getRuntimeAPI().deleteAllProcessInstances(processUUID1);
    getRuntimeAPI().deleteAllProcessInstances(processUUID2);
    getManagementAPI().disable(processUUID1);
    getManagementAPI().disable(processUUID2);
    getManagementAPI().deleteProcess(processUUID1);
    getManagementAPI().deleteProcess(processUUID2);
  }

  public void testGetLightParentProcessInstancesWithActiveUserFromProcessUUIDsOrderByInstanceUUIDAsc() throws Exception {
    ProcessDefinition process1 = getProcess();
    ProcessDefinition process2 = getProcess2();

    process1 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process1));
    process2 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process2));

    final ProcessDefinitionUUID processUUID1 = process1.getUUID();
    final ProcessDefinitionUUID processUUID2 = process2.getUUID();

    final int numberOfProcess1 = 10;
    final List<ProcessInstanceUUID> instanceUUIDsP1 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID1, numberOfProcess1, instanceUUIDsP1);

    updateInstanceVariable(instanceUUIDsP1, "text", "Hello", true);

    final int numberOfProcess2 = 5;
    final List<ProcessInstanceUUID> instanceUUIDsP2 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID2, numberOfProcess2, instanceUUIDsP2);

    for (int i = 0; i < numberOfProcess2; i++) {
      getRuntimeAPI().setProcessInstanceVariable(instanceUUIDsP2.get(i), "text", "hello");
      Thread.sleep(3);
    }

    final Set<ProcessDefinitionUUID> fromP1 = new HashSet<ProcessDefinitionUUID>();
    fromP1.add(processUUID1);

    final Set<ProcessDefinitionUUID> fromP2 = new HashSet<ProcessDefinitionUUID>();
    fromP2.add(processUUID2);

    Collections.sort(instanceUUIDsP1, new ProcessInstanceUUIDComparator());
    Collections.sort(instanceUUIDsP2, new ProcessInstanceUUIDComparator());

    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithActiveUser(
        "john", 0, 5, fromP1, ProcessInstanceCriterion.INSTANCE_UUID_ASC);
    checkListContent(0, 5, 5, instanceUUIDsP1, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithActiveUser("john", 5, 10, fromP1,
        ProcessInstanceCriterion.INSTANCE_UUID_ASC);
    checkListContent(5, 10, 5, instanceUUIDsP1, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithActiveUser("john", 0, 3, fromP2,
        ProcessInstanceCriterion.INSTANCE_UUID_ASC);
    checkListContent(0, 3, 3, instanceUUIDsP2, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithActiveUser("john", 3, 5, fromP2,
        ProcessInstanceCriterion.INSTANCE_UUID_ASC);
    checkListContent(3, 5, 2, instanceUUIDsP2, retrievedInstances);

    getRuntimeAPI().deleteAllProcessInstances(processUUID1);
    getRuntimeAPI().deleteAllProcessInstances(processUUID2);
    getManagementAPI().disable(processUUID1);
    getManagementAPI().disable(processUUID2);
    getManagementAPI().deleteProcess(processUUID1);
    getManagementAPI().deleteProcess(processUUID2);
  }

  public void testGetLightParentProcessInstancesWithActiveUserFromProcessUUIDsOrderByLastUpdateDesc() throws Exception {
    ProcessDefinition process1 = getProcess();
    ProcessDefinition process2 = getProcess2();

    process1 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process1));
    process2 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process2));

    final ProcessDefinitionUUID processUUID1 = process1.getUUID();
    final ProcessDefinitionUUID processUUID2 = process2.getUUID();

    final int numberOfProcess1 = 10;
    final List<ProcessInstanceUUID> instanceUUIDsP1 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID1, numberOfProcess1, instanceUUIDsP1);

    updateInstanceVariable(instanceUUIDsP1, "text", "Hello", true);

    final int numberOfProcess2 = 5;
    final List<ProcessInstanceUUID> instanceUUIDsP2 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID2, numberOfProcess2, instanceUUIDsP2);

    updateInstanceVariable(instanceUUIDsP2, "text", "Hello", false);

    final Set<ProcessDefinitionUUID> fromP1 = new HashSet<ProcessDefinitionUUID>();
    fromP1.add(processUUID1);

    final Set<ProcessDefinitionUUID> fromP2 = new HashSet<ProcessDefinitionUUID>();
    fromP2.add(processUUID2);

    Collections.reverse(instanceUUIDsP2);

    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithActiveUser(
        "john", 0, 5, fromP1, ProcessInstanceCriterion.LAST_UPDATE_DESC);
    checkListContent(0, 5, 5, instanceUUIDsP1, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithActiveUser("john", 5, 10, fromP1,
        ProcessInstanceCriterion.LAST_UPDATE_DESC);
    checkListContent(5, 10, 5, instanceUUIDsP1, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithActiveUser("john", 0, 3, fromP2,
        ProcessInstanceCriterion.LAST_UPDATE_DESC);
    checkListContent(0, 3, 3, instanceUUIDsP2, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithActiveUser("john", 3, 5, fromP2,
        ProcessInstanceCriterion.LAST_UPDATE_DESC);
    checkListContent(3, 5, 2, instanceUUIDsP2, retrievedInstances);

    getRuntimeAPI().deleteAllProcessInstances(processUUID1);
    getRuntimeAPI().deleteAllProcessInstances(processUUID2);
    getManagementAPI().disable(processUUID1);
    getManagementAPI().disable(processUUID2);
    getManagementAPI().deleteProcess(processUUID1);
    getManagementAPI().deleteProcess(processUUID2);
  }

  public void testGetLightParentProcessInstancesWithActiveUserFromProcessUUIDsOrderByStartedDateDesc() throws Exception {
    ProcessDefinition process1 = getProcess();
    ProcessDefinition process2 = getProcess2();

    process1 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process1));
    process2 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process2));

    final ProcessDefinitionUUID processUUID1 = process1.getUUID();
    final ProcessDefinitionUUID processUUID2 = process2.getUUID();

    final int numberOfProcess1 = 10;
    final List<ProcessInstanceUUID> instanceUUIDsP1 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID1, numberOfProcess1, instanceUUIDsP1);

    updateInstanceVariable(instanceUUIDsP1, "text", "Hello", true);

    final int numberOfProcess2 = 5;
    final List<ProcessInstanceUUID> instanceUUIDsP2 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID2, numberOfProcess2, instanceUUIDsP2);

    updateInstanceVariable(instanceUUIDsP2, "text", "Hello", false);

    final Set<ProcessDefinitionUUID> fromP1 = new HashSet<ProcessDefinitionUUID>();
    fromP1.add(processUUID1);

    final Set<ProcessDefinitionUUID> fromP2 = new HashSet<ProcessDefinitionUUID>();
    fromP2.add(processUUID2);

    Collections.reverse(instanceUUIDsP1);
    Collections.reverse(instanceUUIDsP2);

    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithActiveUser(
        "john", 0, 5, fromP1, ProcessInstanceCriterion.STARTED_DATE_DESC);
    checkListContent(0, 5, 5, instanceUUIDsP1, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithActiveUser("john", 5, 10, fromP1,
        ProcessInstanceCriterion.STARTED_DATE_DESC);
    checkListContent(5, 10, 5, instanceUUIDsP1, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithActiveUser("john", 0, 3, fromP2,
        ProcessInstanceCriterion.STARTED_DATE_DESC);
    checkListContent(0, 3, 3, instanceUUIDsP2, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithActiveUser("john", 3, 5, fromP2,
        ProcessInstanceCriterion.STARTED_DATE_DESC);
    checkListContent(3, 5, 2, instanceUUIDsP2, retrievedInstances);

    getRuntimeAPI().deleteAllProcessInstances(processUUID1);
    getRuntimeAPI().deleteAllProcessInstances(processUUID2);
    getManagementAPI().disable(processUUID1);
    getManagementAPI().disable(processUUID2);
    getManagementAPI().deleteProcess(processUUID1);
    getManagementAPI().deleteProcess(processUUID2);
  }

  public void testGetLightParentProcessInstancesWithActiveUserFromProcessUUIDsOrderByEndedDateDesc() throws Exception {
    ProcessDefinition process1 = getProcess();
    ProcessDefinition process2 = getProcess2();

    process1 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process1));
    process2 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process2));

    final ProcessDefinitionUUID processUUID1 = process1.getUUID();
    final ProcessDefinitionUUID processUUID2 = process2.getUUID();

    final int numberOfProcess1 = 10;
    final List<ProcessInstanceUUID> instanceUUIDsP1 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID1, numberOfProcess1, instanceUUIDsP1);

    updateInstanceVariable(instanceUUIDsP1, "text", "Hello", true);

    final int numberOfProcess2 = 5;
    final List<ProcessInstanceUUID> instanceUUIDsP2 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID2, numberOfProcess2, instanceUUIDsP2);

    updateInstanceVariable(instanceUUIDsP2, "text", "Hello", false);

    final Set<ProcessDefinitionUUID> fromP1 = new HashSet<ProcessDefinitionUUID>();
    fromP1.add(processUUID1);

    final Set<ProcessDefinitionUUID> fromP2 = new HashSet<ProcessDefinitionUUID>();
    fromP2.add(processUUID2);

    // there is no ended date, the instance is not finished
    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithActiveUser(
        "john", 0, 5, fromP1, ProcessInstanceCriterion.ENDED_DATE_DESC);
    assertEquals(5, retrievedInstances.size());

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithActiveUser("john", 5, 10, fromP1,
        ProcessInstanceCriterion.ENDED_DATE_DESC);
    assertEquals(5, retrievedInstances.size());

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithActiveUser("john", 0, 3, fromP2,
        ProcessInstanceCriterion.ENDED_DATE_DESC);
    assertEquals(3, retrievedInstances.size());

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithActiveUser("john", 3, 5, fromP2,
        ProcessInstanceCriterion.ENDED_DATE_DESC);
    assertEquals(2, retrievedInstances.size());

    getRuntimeAPI().deleteAllProcessInstances(processUUID1);
    getRuntimeAPI().deleteAllProcessInstances(processUUID2);
    getManagementAPI().disable(processUUID1);
    getManagementAPI().disable(processUUID2);
    getManagementAPI().deleteProcess(processUUID1);
    getManagementAPI().deleteProcess(processUUID2);
  }

  public void testGetLightParentProcessInstancesWithActiveUserFromProcessUUIDsOrderByInstanceNumberDesc()
      throws Exception {
    ProcessDefinition process1 = getProcess();
    ProcessDefinition process2 = getProcess2();

    process1 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process1));
    process2 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process2));

    final ProcessDefinitionUUID processUUID1 = process1.getUUID();
    final ProcessDefinitionUUID processUUID2 = process2.getUUID();

    final int numberOfProcess1 = 10;
    final List<ProcessInstanceUUID> instanceUUIDsP1 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID1, numberOfProcess1, instanceUUIDsP1);

    updateInstanceVariable(instanceUUIDsP1, "text", "Hello", true);

    final int numberOfProcess2 = 5;
    final List<ProcessInstanceUUID> instanceUUIDsP2 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID2, numberOfProcess2, instanceUUIDsP2);

    updateInstanceVariable(instanceUUIDsP2, "text", "Hello", false);

    final Set<ProcessDefinitionUUID> fromP1 = new HashSet<ProcessDefinitionUUID>();
    fromP1.add(processUUID1);

    final Set<ProcessDefinitionUUID> fromP2 = new HashSet<ProcessDefinitionUUID>();
    fromP2.add(processUUID2);

    Collections.reverse(instanceUUIDsP1);
    Collections.reverse(instanceUUIDsP2);

    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithActiveUser(
        "john", 0, 5, fromP1, ProcessInstanceCriterion.INSTANCE_NUMBER_DESC);
    checkListContent(0, 5, 5, instanceUUIDsP1, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithActiveUser("john", 5, 10, fromP1,
        ProcessInstanceCriterion.INSTANCE_NUMBER_DESC);
    checkListContent(5, 10, 5, instanceUUIDsP1, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithActiveUser("john", 0, 3, fromP2,
        ProcessInstanceCriterion.INSTANCE_NUMBER_DESC);
    checkListContent(0, 3, 3, instanceUUIDsP2, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithActiveUser("john", 3, 5, fromP2,
        ProcessInstanceCriterion.INSTANCE_NUMBER_DESC);
    checkListContent(3, 5, 2, instanceUUIDsP2, retrievedInstances);

    getRuntimeAPI().deleteAllProcessInstances(processUUID1);
    getRuntimeAPI().deleteAllProcessInstances(processUUID2);
    getManagementAPI().disable(processUUID1);
    getManagementAPI().disable(processUUID2);
    getManagementAPI().deleteProcess(processUUID1);
    getManagementAPI().deleteProcess(processUUID2);
  }

  public void testGetLightParentProcessInstancesWithActiveUserFromProcessUUIDsOrderByInstanceUUIDDesc()
      throws Exception {
    ProcessDefinition process1 = getProcess();
    ProcessDefinition process2 = getProcess2();

    process1 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process1));
    process2 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process2));

    final ProcessDefinitionUUID processUUID1 = process1.getUUID();
    final ProcessDefinitionUUID processUUID2 = process2.getUUID();

    final int numberOfProcess1 = 10;
    final List<ProcessInstanceUUID> instanceUUIDsP1 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID1, numberOfProcess1, instanceUUIDsP1);

    updateInstanceVariable(instanceUUIDsP1, "text", "Hello", true);

    final int numberOfProcess2 = 5;
    final List<ProcessInstanceUUID> instanceUUIDsP2 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID2, numberOfProcess2, instanceUUIDsP2);

    updateInstanceVariable(instanceUUIDsP2, "text", "Hello", false);

    final Set<ProcessDefinitionUUID> fromP1 = new HashSet<ProcessDefinitionUUID>();
    fromP1.add(processUUID1);

    final Set<ProcessDefinitionUUID> fromP2 = new HashSet<ProcessDefinitionUUID>();
    fromP2.add(processUUID2);

    Collections.sort(instanceUUIDsP1, new ProcessInstanceUUIDComparator());
    Collections.sort(instanceUUIDsP2, new ProcessInstanceUUIDComparator());
    Collections.reverse(instanceUUIDsP1);
    Collections.reverse(instanceUUIDsP2);

    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithActiveUser(
        "john", 0, 5, fromP1, ProcessInstanceCriterion.INSTANCE_UUID_DESC);
    checkListContent(0, 5, 5, instanceUUIDsP1, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithActiveUser("john", 5, 10, fromP1,
        ProcessInstanceCriterion.INSTANCE_UUID_DESC);
    checkListContent(5, 10, 5, instanceUUIDsP1, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithActiveUser("john", 0, 3, fromP2,
        ProcessInstanceCriterion.INSTANCE_UUID_DESC);
    checkListContent(0, 3, 3, instanceUUIDsP2, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithActiveUser("john", 3, 5, fromP2,
        ProcessInstanceCriterion.INSTANCE_UUID_DESC);
    checkListContent(3, 5, 2, instanceUUIDsP2, retrievedInstances);

    getRuntimeAPI().deleteAllProcessInstances(processUUID1);
    getRuntimeAPI().deleteAllProcessInstances(processUUID2);
    getManagementAPI().disable(processUUID1);
    getManagementAPI().disable(processUUID2);
    getManagementAPI().deleteProcess(processUUID1);
    getManagementAPI().deleteProcess(processUUID2);
  }

  public void testGetLightParentProcessInstancesWithActiveUserExceptOrderByLastUpdateAsc() throws Exception {
    ProcessDefinition process1 = getProcess();
    ProcessDefinition process2 = getProcess2();

    process1 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process1));
    process2 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process2));

    final ProcessDefinitionUUID processUUID1 = process1.getUUID();
    final ProcessDefinitionUUID processUUID2 = process2.getUUID();

    final int numberOfProcess1 = 10;
    final List<ProcessInstanceUUID> instanceUUIDsP1 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID1, numberOfProcess1, instanceUUIDsP1);

    updateInstanceVariable(instanceUUIDsP1, "text", "Hello", true);

    final int numberOfProcess2 = 5;
    final List<ProcessInstanceUUID> instanceUUIDsP2 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID2, numberOfProcess2, instanceUUIDsP2);

    updateInstanceVariable(instanceUUIDsP2, "text", "Hello", false);

    final Set<ProcessDefinitionUUID> exceptP1 = new HashSet<ProcessDefinitionUUID>();
    exceptP1.add(processUUID1);

    final Set<ProcessDefinitionUUID> exceptP2 = new HashSet<ProcessDefinitionUUID>();
    exceptP2.add(processUUID2);

    Collections.reverse(instanceUUIDsP1);

    // except process2
    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserExcept("john", 0, 5, exceptP2,
            ProcessInstanceCriterion.LAST_UPDATE_ASC);
    checkListContent(0, 5, 5, instanceUUIDsP1, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithActiveUserExcept("john", 5, 10,
        exceptP2, ProcessInstanceCriterion.LAST_UPDATE_ASC);
    checkListContent(5, 10, 5, instanceUUIDsP1, retrievedInstances);

    // except process1
    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithActiveUserExcept("john", 0, 3,
        exceptP1, ProcessInstanceCriterion.LAST_UPDATE_ASC);
    checkListContent(0, 3, 3, instanceUUIDsP2, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithActiveUserExcept("john", 3, 5,
        exceptP1, ProcessInstanceCriterion.LAST_UPDATE_ASC);
    checkListContent(3, 5, 2, instanceUUIDsP2, retrievedInstances);

    // except process1 and process2
    final Set<ProcessDefinitionUUID> exceptP1AndP2 = new HashSet<ProcessDefinitionUUID>();
    exceptP1AndP2.add(processUUID1);
    exceptP1AndP2.add(processUUID2);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithActiveUserExcept("john", 0, 5,
        exceptP1AndP2, ProcessInstanceCriterion.LAST_UPDATE_ASC);
    assertEquals(0, retrievedInstances.size());

    getRuntimeAPI().deleteAllProcessInstances(processUUID1);
    getRuntimeAPI().deleteAllProcessInstances(processUUID2);
    getManagementAPI().disable(processUUID1);
    getManagementAPI().disable(processUUID2);
    getManagementAPI().deleteProcess(processUUID1);
    getManagementAPI().deleteProcess(processUUID2);
  }

  public void testGetLightParentProcessInstancesWithActiveUserExceptOrderByStartedDateAsc() throws Exception {
    ProcessDefinition process1 = getProcess();
    ProcessDefinition process2 = getProcess2();

    process1 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process1));
    process2 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process2));

    final ProcessDefinitionUUID processUUID1 = process1.getUUID();
    final ProcessDefinitionUUID processUUID2 = process2.getUUID();

    final int numberOfProcess1 = 10;
    final List<ProcessInstanceUUID> instanceUUIDsP1 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID1, numberOfProcess1, instanceUUIDsP1);

    updateInstanceVariable(instanceUUIDsP1, "text", "Hello", true);

    final int numberOfProcess2 = 5;
    final List<ProcessInstanceUUID> instanceUUIDsP2 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID2, numberOfProcess2, instanceUUIDsP2);

    for (int i = 0; i < numberOfProcess2; i++) {
      getRuntimeAPI().setProcessInstanceVariable(instanceUUIDsP2.get(i), "text", "hello");
      Thread.sleep(3);
    }

    final Set<ProcessDefinitionUUID> exceptP1 = new HashSet<ProcessDefinitionUUID>();
    exceptP1.add(processUUID1);

    final Set<ProcessDefinitionUUID> exceptP2 = new HashSet<ProcessDefinitionUUID>();
    exceptP2.add(processUUID2);

    // except process2
    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserExcept("john", 0, 5, exceptP2,
            ProcessInstanceCriterion.STARTED_DATE_ASC);
    checkListContent(0, 5, 5, instanceUUIDsP1, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithActiveUserExcept("john", 5, 10,
        exceptP2, ProcessInstanceCriterion.STARTED_DATE_ASC);
    checkListContent(5, 10, 5, instanceUUIDsP1, retrievedInstances);

    // except process1
    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithActiveUserExcept("john", 0, 3,
        exceptP1, ProcessInstanceCriterion.STARTED_DATE_ASC);
    checkListContent(0, 3, 3, instanceUUIDsP2, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithActiveUserExcept("john", 3, 5,
        exceptP1, ProcessInstanceCriterion.STARTED_DATE_ASC);
    checkListContent(3, 5, 2, instanceUUIDsP2, retrievedInstances);

    // except process1 and process2
    final Set<ProcessDefinitionUUID> exceptP1AndP2 = new HashSet<ProcessDefinitionUUID>();
    exceptP1AndP2.add(processUUID1);
    exceptP1AndP2.add(processUUID2);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithActiveUserExcept("john", 0, 5,
        exceptP1AndP2, ProcessInstanceCriterion.STARTED_DATE_ASC);
    assertEquals(0, retrievedInstances.size());

    getRuntimeAPI().deleteAllProcessInstances(processUUID1);
    getRuntimeAPI().deleteAllProcessInstances(processUUID2);
    getManagementAPI().disable(processUUID1);
    getManagementAPI().disable(processUUID2);
    getManagementAPI().deleteProcess(processUUID1);
    getManagementAPI().deleteProcess(processUUID2);
  }

  public void testGetLightParentProcessInstancesWithActiveUserExceptOrderByEndedDateAsc() throws Exception {
    ProcessDefinition process1 = getProcess();
    ProcessDefinition process2 = getProcess2();

    process1 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process1));
    process2 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process2));

    final ProcessDefinitionUUID processUUID1 = process1.getUUID();
    final ProcessDefinitionUUID processUUID2 = process2.getUUID();

    final int numberOfProcess1 = 10;
    final List<ProcessInstanceUUID> instanceUUIDsP1 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID1, numberOfProcess1, instanceUUIDsP1);

    updateInstanceVariable(instanceUUIDsP1, "text", "Hello", true);

    final int numberOfProcess2 = 5;
    final List<ProcessInstanceUUID> instanceUUIDsP2 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID2, numberOfProcess2, instanceUUIDsP2);

    for (int i = 0; i < numberOfProcess2; i++) {
      getRuntimeAPI().setProcessInstanceVariable(instanceUUIDsP2.get(i), "text", "hello");
      Thread.sleep(3);
    }

    final Set<ProcessDefinitionUUID> exceptP1 = new HashSet<ProcessDefinitionUUID>();
    exceptP1.add(processUUID1);

    final Set<ProcessDefinitionUUID> exceptP2 = new HashSet<ProcessDefinitionUUID>();
    exceptP2.add(processUUID2);

    // there is no ended date, the instance is not finished
    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserExcept("john", 0, 5, exceptP2,
            ProcessInstanceCriterion.ENDED_DATE_ASC);
    assertEquals(5, retrievedInstances.size());

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithActiveUserExcept("john", 5, 10,
        exceptP2, ProcessInstanceCriterion.ENDED_DATE_ASC);
    assertEquals(5, retrievedInstances.size());

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithActiveUserExcept("john", 0, 3,
        exceptP1, ProcessInstanceCriterion.ENDED_DATE_ASC);
    assertEquals(3, retrievedInstances.size());

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithActiveUserExcept("john", 3, 5,
        exceptP1, ProcessInstanceCriterion.ENDED_DATE_ASC);
    assertEquals(2, retrievedInstances.size());

    final Set<ProcessDefinitionUUID> exceptP1AndP2 = new HashSet<ProcessDefinitionUUID>();
    exceptP1AndP2.add(processUUID1);
    exceptP1AndP2.add(processUUID2);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithActiveUserExcept("john", 0, 5,
        exceptP1AndP2, ProcessInstanceCriterion.ENDED_DATE_ASC);
    assertEquals(0, retrievedInstances.size());

    getManagementAPI().deleteProcess(processUUID1);
    getManagementAPI().deleteProcess(processUUID2);
  }

  public void testGetLightParentProcessInstancesWithActiveUserExceptOrderByInstanceNumberAsc() throws Exception {
    ProcessDefinition process1 = getProcess();
    ProcessDefinition process2 = getProcess2();

    process1 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process1));
    process2 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process2));

    final ProcessDefinitionUUID processUUID1 = process1.getUUID();
    final ProcessDefinitionUUID processUUID2 = process2.getUUID();

    final int numberOfProcess1 = 10;
    final List<ProcessInstanceUUID> instanceUUIDsP1 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID1, numberOfProcess1, instanceUUIDsP1);

    updateInstanceVariable(instanceUUIDsP1, "text", "Hello", true);

    final int numberOfProcess2 = 5;
    final List<ProcessInstanceUUID> instanceUUIDsP2 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID2, numberOfProcess2, instanceUUIDsP2);

    for (int i = 0; i < numberOfProcess2; i++) {
      getRuntimeAPI().setProcessInstanceVariable(instanceUUIDsP2.get(i), "text", "hello");
      Thread.sleep(3);
    }

    final Set<ProcessDefinitionUUID> exceptP1 = new HashSet<ProcessDefinitionUUID>();
    exceptP1.add(processUUID1);

    final Set<ProcessDefinitionUUID> exceptP2 = new HashSet<ProcessDefinitionUUID>();
    exceptP2.add(processUUID2);

    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserExcept("john", 0, 5, exceptP2,
            ProcessInstanceCriterion.INSTANCE_NUMBER_ASC);
    checkListContent(0, 5, 5, instanceUUIDsP1, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithActiveUserExcept("john", 5, 10,
        exceptP2, ProcessInstanceCriterion.INSTANCE_NUMBER_ASC);
    checkListContent(5, 10, 5, instanceUUIDsP1, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithActiveUserExcept("john", 0, 3,
        exceptP1, ProcessInstanceCriterion.INSTANCE_NUMBER_ASC);
    checkListContent(0, 3, 3, instanceUUIDsP2, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithActiveUserExcept("john", 3, 5,
        exceptP1, ProcessInstanceCriterion.INSTANCE_NUMBER_ASC);
    checkListContent(3, 5, 2, instanceUUIDsP2, retrievedInstances);

    final Set<ProcessDefinitionUUID> exceptP1AndP2 = new HashSet<ProcessDefinitionUUID>();
    exceptP1AndP2.add(processUUID1);
    exceptP1AndP2.add(processUUID2);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithActiveUserExcept("john", 0, 5,
        exceptP1AndP2, ProcessInstanceCriterion.INSTANCE_NUMBER_ASC);
    assertEquals(0, retrievedInstances.size());

    getRuntimeAPI().deleteAllProcessInstances(processUUID1);
    getRuntimeAPI().deleteAllProcessInstances(processUUID2);
    getManagementAPI().disable(processUUID1);
    getManagementAPI().disable(processUUID2);
    getManagementAPI().deleteProcess(processUUID1);
    getManagementAPI().deleteProcess(processUUID2);
  }

  public void testGetLightParentProcessInstancesWithActiveUserExceptOrderByInstanceUUIDAsc() throws Exception {
    ProcessDefinition process1 = getProcess();
    ProcessDefinition process2 = getProcess2();

    process1 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process1));
    process2 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process2));

    final ProcessDefinitionUUID processUUID1 = process1.getUUID();
    final ProcessDefinitionUUID processUUID2 = process2.getUUID();

    final int numberOfProcess1 = 10;
    final List<ProcessInstanceUUID> instanceUUIDsP1 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID1, numberOfProcess1, instanceUUIDsP1);

    updateInstanceVariable(instanceUUIDsP1, "text", "Hello", true);

    final int numberOfProcess2 = 5;
    final List<ProcessInstanceUUID> instanceUUIDsP2 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID2, numberOfProcess2, instanceUUIDsP2);

    for (int i = 0; i < numberOfProcess2; i++) {
      getRuntimeAPI().setProcessInstanceVariable(instanceUUIDsP2.get(i), "text", "hello");
      Thread.sleep(3);
    }

    final Set<ProcessDefinitionUUID> exceptP1 = new HashSet<ProcessDefinitionUUID>();
    exceptP1.add(processUUID1);

    final Set<ProcessDefinitionUUID> exceptP2 = new HashSet<ProcessDefinitionUUID>();
    exceptP2.add(processUUID2);

    Collections.sort(instanceUUIDsP1, new ProcessInstanceUUIDComparator());
    Collections.sort(instanceUUIDsP2, new ProcessInstanceUUIDComparator());

    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserExcept("john", 0, 5, exceptP2,
            ProcessInstanceCriterion.INSTANCE_UUID_ASC);
    checkListContent(0, 5, 5, instanceUUIDsP1, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithActiveUserExcept("john", 5, 10,
        exceptP2, ProcessInstanceCriterion.INSTANCE_UUID_ASC);
    checkListContent(5, 10, 5, instanceUUIDsP1, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithActiveUserExcept("john", 0, 3,
        exceptP1, ProcessInstanceCriterion.INSTANCE_UUID_ASC);
    checkListContent(0, 3, 3, instanceUUIDsP2, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithActiveUserExcept("john", 3, 5,
        exceptP1, ProcessInstanceCriterion.INSTANCE_UUID_ASC);
    checkListContent(3, 5, 2, instanceUUIDsP2, retrievedInstances);

    final Set<ProcessDefinitionUUID> exceptP1AndP2 = new HashSet<ProcessDefinitionUUID>();
    exceptP1AndP2.add(processUUID1);
    exceptP1AndP2.add(processUUID2);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithActiveUserExcept("john", 0, 5,
        exceptP1AndP2, ProcessInstanceCriterion.INSTANCE_UUID_ASC);
    assertEquals(0, retrievedInstances.size());

    getRuntimeAPI().deleteAllProcessInstances(processUUID1);
    getRuntimeAPI().deleteAllProcessInstances(processUUID2);
    getManagementAPI().disable(processUUID1);
    getManagementAPI().disable(processUUID2);
    getManagementAPI().deleteProcess(processUUID1);
    getManagementAPI().deleteProcess(processUUID2);
  }

  public void testGetLightParentProcessInstancesWithActiveUserExceptOrderByLastUpdateDesc() throws Exception {
    ProcessDefinition process1 = getProcess();
    ProcessDefinition process2 = getProcess2();

    process1 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process1));
    process2 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process2));

    final ProcessDefinitionUUID processUUID1 = process1.getUUID();
    final ProcessDefinitionUUID processUUID2 = process2.getUUID();

    final int numberOfProcess1 = 10;
    final List<ProcessInstanceUUID> instanceUUIDsP1 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID1, numberOfProcess1, instanceUUIDsP1);

    updateInstanceVariable(instanceUUIDsP1, "text", "Hello", true);

    final int numberOfProcess2 = 5;
    final List<ProcessInstanceUUID> instanceUUIDsP2 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID2, numberOfProcess2, instanceUUIDsP2);

    for (int i = 0; i < numberOfProcess2; i++) {
      getRuntimeAPI().setProcessInstanceVariable(instanceUUIDsP2.get(i), "text", "hello");
      Thread.sleep(3);
    }

    final Set<ProcessDefinitionUUID> exceptP1 = new HashSet<ProcessDefinitionUUID>();
    exceptP1.add(processUUID1);

    final Set<ProcessDefinitionUUID> exceptP2 = new HashSet<ProcessDefinitionUUID>();
    exceptP2.add(processUUID2);

    Collections.reverse(instanceUUIDsP2);

    // except process2
    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserExcept("john", 0, 5, exceptP2,
            ProcessInstanceCriterion.LAST_UPDATE_DESC);
    checkListContent(0, 5, 5, instanceUUIDsP1, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithActiveUserExcept("john", 5, 10,
        exceptP2, ProcessInstanceCriterion.LAST_UPDATE_DESC);
    checkListContent(5, 10, 5, instanceUUIDsP1, retrievedInstances);

    getRuntimeAPI().deleteAllProcessInstances(processUUID1);
    getRuntimeAPI().deleteAllProcessInstances(processUUID2);
    getManagementAPI().disable(processUUID1);
    getManagementAPI().disable(processUUID2);
    getManagementAPI().deleteProcess(processUUID1);
    getManagementAPI().deleteProcess(processUUID2);
  }

  public void testGetLightParentProcessInstancesWithActiveUserExceptOrderByStartedDateDesc() throws Exception {
    ProcessDefinition process1 = getProcess();
    ProcessDefinition process2 = getProcess2();

    process1 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process1));
    process2 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process2));

    final ProcessDefinitionUUID processUUID1 = process1.getUUID();
    final ProcessDefinitionUUID processUUID2 = process2.getUUID();

    final int numberOfProcess1 = 10;
    final List<ProcessInstanceUUID> instanceUUIDsP1 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID1, numberOfProcess1, instanceUUIDsP1);

    updateInstanceVariable(instanceUUIDsP1, "text", "Hello", true);

    final int numberOfProcess2 = 5;
    final List<ProcessInstanceUUID> instanceUUIDsP2 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID2, numberOfProcess2, instanceUUIDsP2);

    for (int i = 0; i < numberOfProcess2; i++) {
      getRuntimeAPI().setProcessInstanceVariable(instanceUUIDsP2.get(i), "text", "hello");
      Thread.sleep(3);
    }

    final Set<ProcessDefinitionUUID> exceptP1 = new HashSet<ProcessDefinitionUUID>();
    exceptP1.add(processUUID1);

    final Set<ProcessDefinitionUUID> exceptP2 = new HashSet<ProcessDefinitionUUID>();
    exceptP2.add(processUUID2);

    Collections.reverse(instanceUUIDsP1);

    // except process2
    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserExcept("john", 0, 5, exceptP2,
            ProcessInstanceCriterion.STARTED_DATE_DESC);
    checkListContent(0, 5, 5, instanceUUIDsP1, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithActiveUserExcept("john", 5, 10,
        exceptP2, ProcessInstanceCriterion.STARTED_DATE_DESC);
    checkListContent(5, 10, 5, instanceUUIDsP1, retrievedInstances);

    getRuntimeAPI().deleteAllProcessInstances(processUUID1);
    getRuntimeAPI().deleteAllProcessInstances(processUUID2);
    getManagementAPI().disable(processUUID1);
    getManagementAPI().disable(processUUID2);
    getManagementAPI().deleteProcess(processUUID1);
    getManagementAPI().deleteProcess(processUUID2);
  }

  public void testGetLightParentProcessInstancesWithActiveUserExceptOrderByEndedDateDesc() throws Exception {
    ProcessDefinition process1 = getProcess();
    ProcessDefinition process2 = getProcess2();

    process1 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process1));
    process2 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process2));

    final ProcessDefinitionUUID processUUID1 = process1.getUUID();
    final ProcessDefinitionUUID processUUID2 = process2.getUUID();

    final int numberOfProcess1 = 10;
    final List<ProcessInstanceUUID> instanceUUIDsP1 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID1, numberOfProcess1, instanceUUIDsP1);

    updateInstanceVariable(instanceUUIDsP1, "text", "Hello", true);

    final int numberOfProcess2 = 5;
    final List<ProcessInstanceUUID> instanceUUIDsP2 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID2, numberOfProcess2, instanceUUIDsP2);

    for (int i = 0; i < numberOfProcess2; i++) {
      getRuntimeAPI().setProcessInstanceVariable(instanceUUIDsP2.get(i), "text", "hello");
      Thread.sleep(3);
    }

    final Set<ProcessDefinitionUUID> exceptP1 = new HashSet<ProcessDefinitionUUID>();
    exceptP1.add(processUUID1);

    final Set<ProcessDefinitionUUID> exceptP2 = new HashSet<ProcessDefinitionUUID>();
    exceptP2.add(processUUID2);

    // there is no ended date, the instance is not finished
    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserExcept("john", 0, 5, exceptP2,
            ProcessInstanceCriterion.ENDED_DATE_DESC);
    assertEquals(5, retrievedInstances.size());

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithActiveUserExcept("john", 5, 10,
        exceptP2, ProcessInstanceCriterion.ENDED_DATE_DESC);
    assertEquals(5, retrievedInstances.size());

    getRuntimeAPI().deleteAllProcessInstances(processUUID1);
    getRuntimeAPI().deleteAllProcessInstances(processUUID2);
    getManagementAPI().disable(processUUID1);
    getManagementAPI().disable(processUUID2);
    getManagementAPI().deleteProcess(processUUID1);
    getManagementAPI().deleteProcess(processUUID2);
  }

  public void testGetLightParentProcessInstancesWithActiveUserExceptOrderByInstanceNumberDesc() throws Exception {
    ProcessDefinition process1 = getProcess();
    ProcessDefinition process2 = getProcess2();

    process1 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process1));
    process2 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process2));

    final ProcessDefinitionUUID processUUID1 = process1.getUUID();
    final ProcessDefinitionUUID processUUID2 = process2.getUUID();

    final int numberOfProcess1 = 10;
    final List<ProcessInstanceUUID> instanceUUIDsP1 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID1, numberOfProcess1, instanceUUIDsP1);

    updateInstanceVariable(instanceUUIDsP1, "text", "Hello", true);

    final int numberOfProcess2 = 5;
    final List<ProcessInstanceUUID> instanceUUIDsP2 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID2, numberOfProcess2, instanceUUIDsP2);

    for (int i = 0; i < numberOfProcess2; i++) {
      getRuntimeAPI().setProcessInstanceVariable(instanceUUIDsP2.get(i), "text", "hello");
      Thread.sleep(3);
    }

    final Set<ProcessDefinitionUUID> exceptP2 = new HashSet<ProcessDefinitionUUID>();
    exceptP2.add(processUUID2);

    Collections.reverse(instanceUUIDsP1);

    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserExcept("john", 0, 5, exceptP2,
            ProcessInstanceCriterion.INSTANCE_NUMBER_DESC);
    checkListContent(0, 5, 5, instanceUUIDsP1, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithActiveUserExcept("john", 5, 10,
        exceptP2, ProcessInstanceCriterion.INSTANCE_NUMBER_DESC);
    checkListContent(5, 10, 5, instanceUUIDsP1, retrievedInstances);

    getRuntimeAPI().deleteAllProcessInstances(processUUID1);
    getRuntimeAPI().deleteAllProcessInstances(processUUID2);
    getManagementAPI().disable(processUUID1);
    getManagementAPI().disable(processUUID2);
    getManagementAPI().deleteProcess(processUUID1);
    getManagementAPI().deleteProcess(processUUID2);
  }

  public void testGetLightParentProcessInstancesWithActiveUserExceptOrderByInstanceUUIDDesc() throws Exception {
    ProcessDefinition process1 = getProcess();
    ProcessDefinition process2 = getProcess2();

    process1 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process1));
    process2 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process2));

    final ProcessDefinitionUUID processUUID1 = process1.getUUID();
    final ProcessDefinitionUUID processUUID2 = process2.getUUID();

    final int numberOfProcess1 = 10;
    final List<ProcessInstanceUUID> instanceUUIDsP1 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID1, numberOfProcess1, instanceUUIDsP1);

    updateInstanceVariable(instanceUUIDsP1, "text", "Hello", true);

    final int numberOfProcess2 = 5;
    final List<ProcessInstanceUUID> instanceUUIDsP2 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID2, numberOfProcess2, instanceUUIDsP2);

    for (int i = 0; i < numberOfProcess2; i++) {
      getRuntimeAPI().setProcessInstanceVariable(instanceUUIDsP2.get(i), "text", "hello");
      Thread.sleep(3);
    }

    final Set<ProcessDefinitionUUID> exceptP2 = new HashSet<ProcessDefinitionUUID>();
    exceptP2.add(processUUID2);

    Collections.sort(instanceUUIDsP1, new ProcessInstanceUUIDComparator());
    Collections.reverse(instanceUUIDsP1);

    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserExcept("john", 0, 5, exceptP2,
            ProcessInstanceCriterion.INSTANCE_UUID_DESC);
    checkListContent(0, 5, 5, instanceUUIDsP1, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithActiveUserExcept("john", 5, 10,
        exceptP2, ProcessInstanceCriterion.INSTANCE_UUID_DESC);
    checkListContent(5, 10, 5, instanceUUIDsP1, retrievedInstances);

    getRuntimeAPI().deleteAllProcessInstances(processUUID1);
    getRuntimeAPI().deleteAllProcessInstances(processUUID2);
    getManagementAPI().disable(processUUID1);
    getManagementAPI().disable(processUUID2);
    getManagementAPI().deleteProcess(processUUID1);
    getManagementAPI().deleteProcess(processUUID2);
  }

  public void testGetLightParentProcessInstancesWithInvolvedUserPagingOrderByLastUpdateAsc() throws Exception {
    ProcessDefinition process = getProcess();

    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final int numberOfProcess = 10;
    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID, numberOfProcess, instanceUUIDs);

    for (int i = 0; i < numberOfProcess; i++) {
      getRuntimeAPI().setProcessInstanceVariable(instanceUUIDs.get(i), "text", "hello");
      Thread.sleep(3);
    }

    final List<ProcessInstanceUUID> expectedUUIDsOrder = getListCopy(instanceUUIDs);
    // last update asc
    List<LightProcessInstance> instances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUser("john",
        0, 5, ProcessInstanceCriterion.LAST_UPDATE_ASC);
    checkListContent(0, 5, 5, expectedUUIDsOrder, instances);

    instances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUser("john", 5, 10,
        ProcessInstanceCriterion.LAST_UPDATE_ASC);
    checkListContent(5, 10, 5, expectedUUIDsOrder, instances);

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetLightParentProcessInstancesWithInvolvedUserPagingOrderByStartedDateAsc() throws Exception {
    ProcessDefinition process = getProcess();

    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final int numberOfProcess = 10;
    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID, numberOfProcess, instanceUUIDs);

    // Started date asc
    List<LightProcessInstance> instances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUser("john",
        0, 5, ProcessInstanceCriterion.STARTED_DATE_ASC);
    checkListContent(0, 5, 5, instanceUUIDs, instances);

    instances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUser("john", 5, 10,
        ProcessInstanceCriterion.STARTED_DATE_ASC);
    checkListContent(5, 10, 5, instanceUUIDs, instances);

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetLightParentProcessInstancesWithInvolvedUserPagingOrderByEndedDateAsc() throws Exception {
    ProcessDefinition process = getProcess();

    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final int numberOfProcess = 10;
    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID, numberOfProcess, instanceUUIDs);

    // execute human tasks
    for (int i = numberOfProcess - 1; i >= 0; i--) {
      executeTask(instanceUUIDs.get(i), "step1");
      Thread.sleep(3);
    }

    // ended date asc
    Collections.reverse(instanceUUIDs);
    List<LightProcessInstance> instances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUser("john",
        0, 5, ProcessInstanceCriterion.ENDED_DATE_ASC);
    checkListContent(0, 5, 5, instanceUUIDs, instances);

    instances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUser("john", 5, 10,
        ProcessInstanceCriterion.ENDED_DATE_ASC);
    checkListContent(5, 10, 5, instanceUUIDs, instances);

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetLightParentProcessInstancesWithInvolvedUserPagingOrderByInstanceNumberAsc() throws Exception {
    ProcessDefinition process = getProcess();

    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final int numberOfProcess = 10;
    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID, numberOfProcess, instanceUUIDs);

    // instance number asc
    List<LightProcessInstance> instances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUser("john",
        0, 5, ProcessInstanceCriterion.INSTANCE_NUMBER_ASC);
    checkListContent(0, 5, 5, instanceUUIDs, instances);

    instances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUser("john", 5, 10,
        ProcessInstanceCriterion.INSTANCE_NUMBER_ASC);
    checkListContent(5, 10, 5, instanceUUIDs, instances);

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetLightParentProcessInstancesWithInvolvedUserPagingOrderByInstanceUUIDAsc() throws Exception {
    ProcessDefinition process = getProcess();

    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final int numberOfProcess = 10;
    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID, numberOfProcess, instanceUUIDs);

    // instance UUID asc
    Collections.sort(instanceUUIDs, new ProcessInstanceUUIDComparator());
    List<LightProcessInstance> instances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUser("john",
        0, 5, ProcessInstanceCriterion.INSTANCE_UUID_ASC);
    checkListContent(0, 5, 5, instanceUUIDs, instances);

    instances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUser("john", 5, 10,
        ProcessInstanceCriterion.INSTANCE_UUID_ASC);
    checkListContent(5, 10, 5, instanceUUIDs, instances);

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetLightParentProcessInstancesWithInvolvedUserPagingOrderByLastUpdateDesc() throws Exception {
    ProcessDefinition process = getProcess();

    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final int numberOfProcess = 10;
    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID, numberOfProcess, instanceUUIDs);

    for (int i = 0; i < numberOfProcess; i++) {
      getRuntimeAPI().setProcessInstanceVariable(instanceUUIDs.get(i), "text", "hello");
      Thread.sleep(3);
    }

    // last update desc
    Collections.reverse(instanceUUIDs);
    List<LightProcessInstance> instances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUser("john",
        0, 5, ProcessInstanceCriterion.LAST_UPDATE_DESC);
    checkListContent(0, 5, 5, instanceUUIDs, instances);

    instances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUser("john", 5, 10,
        ProcessInstanceCriterion.LAST_UPDATE_DESC);
    checkListContent(5, 10, 5, instanceUUIDs, instances);

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetLightParentProcessInstancesWithInvolvedUserPagingOrderByStartedDateDesc() throws Exception {
    ProcessDefinition process = getProcess();

    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final int numberOfProcess = 10;
    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID, numberOfProcess, instanceUUIDs);

    // started date desc
    Collections.reverse(instanceUUIDs);
    List<LightProcessInstance> instances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUser("john",
        0, 5, ProcessInstanceCriterion.STARTED_DATE_DESC);
    checkListContent(0, 5, 5, instanceUUIDs, instances);

    instances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUser("john", 5, 10,
        ProcessInstanceCriterion.STARTED_DATE_DESC);
    checkListContent(5, 10, 5, instanceUUIDs, instances);

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetLightParentProcessInstancesWithInvolvedUserPagingOrderByEndedDateDesc() throws Exception {
    ProcessDefinition process = getProcess();

    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final int numberOfProcess = 10;
    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID, numberOfProcess, instanceUUIDs);

    // execute human tasks
    for (int i = numberOfProcess - 1; i >= 0; i--) {
      executeTask(instanceUUIDs.get(i), "step1");
      Thread.sleep(3);
    }

    // ended date desc
    List<LightProcessInstance> instances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUser("john",
        0, 5, ProcessInstanceCriterion.ENDED_DATE_DESC);
    checkListContent(0, 5, 5, instanceUUIDs, instances);

    instances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUser("john", 5, 10,
        ProcessInstanceCriterion.ENDED_DATE_DESC);
    checkListContent(5, 10, 5, instanceUUIDs, instances);

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetLightParentProcessInstancesWithInvolvedUserPagingOrderByInstanceNumberDesc() throws Exception {
    ProcessDefinition process = getProcess();

    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final int numberOfProcess = 10;
    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID, numberOfProcess, instanceUUIDs);

    // instance number desc
    Collections.reverse(instanceUUIDs);
    List<LightProcessInstance> instances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUser("john",
        0, 5, ProcessInstanceCriterion.INSTANCE_NUMBER_DESC);
    checkListContent(0, 5, 5, instanceUUIDs, instances);

    instances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUser("john", 5, 10,
        ProcessInstanceCriterion.INSTANCE_NUMBER_DESC);
    checkListContent(5, 10, 5, instanceUUIDs, instances);

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetLightParentProcessInstancesWithInvolvedUserPagingOrderByInstanceUUIDDesc() throws Exception {
    ProcessDefinition process = getProcess();

    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final int numberOfProcess = 10;
    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID, numberOfProcess, instanceUUIDs);

    // instance UUID desc
    Collections.sort(instanceUUIDs, new ProcessInstanceUUIDComparator());
    Collections.reverse(instanceUUIDs);
    List<LightProcessInstance> instances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUser("john",
        0, 5, ProcessInstanceCriterion.INSTANCE_UUID_DESC);
    checkListContent(0, 5, 5, instanceUUIDs, instances);

    instances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUser("john", 5, 10,
        ProcessInstanceCriterion.INSTANCE_UUID_DESC);
    checkListContent(5, 10, 5, instanceUUIDs, instances);

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetLightParentProcessInstancesWithInvolvedUserPagingOrderByDefault() throws Exception {
    ProcessDefinition process = getProcess();

    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final int numberOfProcess = 10;
    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID, numberOfProcess, instanceUUIDs);

    for (int i = 0; i < numberOfProcess; i++) {
      getRuntimeAPI().setProcessInstanceVariable(instanceUUIDs.get(i), "text", "hello");
      Thread.sleep(3);
    }

    // default (last update desc)
    Collections.reverse(instanceUUIDs);
    List<LightProcessInstance> instances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUser("john",
        0, 5, ProcessInstanceCriterion.DEFAULT);
    checkListContent(0, 5, 5, instanceUUIDs, instances);

    instances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUser("john", 5, 10,
        ProcessInstanceCriterion.DEFAULT);
    checkListContent(5, 10, 5, instanceUUIDs, instances);

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetLightParentProcessInstancesWithInvolvedUserFromProcessUUIDsPagingOderByLastUpdateAsc()
      throws Exception {
    ProcessDefinition process1 = getProcess();
    ProcessDefinition process2 = getProcess2();

    process1 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process1));
    process2 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process2));

    final ProcessDefinitionUUID processUUID1 = process1.getUUID();
    final ProcessDefinitionUUID processUUID2 = process2.getUUID();

    final int numberOfProcess1 = 10;
    final List<ProcessInstanceUUID> instanceUUIDsP1 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID1, numberOfProcess1, instanceUUIDsP1);

    updateInstanceVariable(instanceUUIDsP1, "text", "Hello", true);

    final int numberOfProcess2 = 5;
    final List<ProcessInstanceUUID> instanceUUIDsP2 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID2, numberOfProcess2, instanceUUIDsP2);

    updateInstanceVariable(instanceUUIDsP2, "text", "Hello", true);

    final Set<ProcessDefinitionUUID> fromP1 = new HashSet<ProcessDefinitionUUID>();
    fromP1.add(processUUID1);

    final Set<ProcessDefinitionUUID> fromP2 = new HashSet<ProcessDefinitionUUID>();
    fromP2.add(processUUID2);

    final Set<ProcessDefinitionUUID> fromP1AndP2 = new HashSet<ProcessDefinitionUUID>();
    fromP1AndP2.add(processUUID1);
    fromP1AndP2.add(processUUID2);

    // **** last update
    // last update asc from p1
    Collections.reverse(instanceUUIDsP1);
    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithInvolvedUser("john", 0, 5, fromP1, ProcessInstanceCriterion.LAST_UPDATE_ASC);
    checkListContent(0, 5, 5, instanceUUIDsP1, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUser("john", 5, 10, fromP1,
        ProcessInstanceCriterion.LAST_UPDATE_ASC);
    checkListContent(5, 10, 5, instanceUUIDsP1, retrievedInstances);

    // last update asc from p2
    Collections.reverse(instanceUUIDsP2);
    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUser("john", 0, 3, fromP2,
        ProcessInstanceCriterion.LAST_UPDATE_ASC);
    checkListContent(0, 3, 3, instanceUUIDsP2, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUser("john", 3, 5, fromP2,
        ProcessInstanceCriterion.LAST_UPDATE_ASC);
    checkListContent(3, 5, 2, instanceUUIDsP2, retrievedInstances);

    // last update asc from p1 and p2
    final List<ProcessInstanceUUID> expectedOrder = new ArrayList<ProcessInstanceUUID>();
    expectedOrder.addAll(instanceUUIDsP1);
    expectedOrder.addAll(instanceUUIDsP2);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUser("john", 0, 8, fromP1AndP2,
        ProcessInstanceCriterion.LAST_UPDATE_ASC);
    checkListContent(0, 8, 8, expectedOrder, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUser("john", 8, 20,
        fromP1AndP2, ProcessInstanceCriterion.LAST_UPDATE_ASC);
    checkListContent(8, 20, 7, expectedOrder, retrievedInstances);

    getManagementAPI().deleteProcess(processUUID1);
    getManagementAPI().deleteProcess(processUUID2);
  }

  public void testGetLightParentProcessInstancesWithInvolvedUserFromProcessUUIDsPagingOderByStartedDateAsc()
      throws Exception {
    ProcessDefinition process1 = getProcess();
    ProcessDefinition process2 = getProcess2();

    process1 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process1));
    process2 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process2));

    final ProcessDefinitionUUID processUUID1 = process1.getUUID();
    final ProcessDefinitionUUID processUUID2 = process2.getUUID();

    final int numberOfProcess1 = 10;
    final List<ProcessInstanceUUID> instanceUUIDsP1 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID1, numberOfProcess1, instanceUUIDsP1);

    final int numberOfProcess2 = 5;
    final List<ProcessInstanceUUID> instanceUUIDsP2 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID2, numberOfProcess2, instanceUUIDsP2);

    final List<ProcessInstanceUUID> instanceUUIDsP1AndP2 = new ArrayList<ProcessInstanceUUID>();
    instanceUUIDsP1AndP2.addAll(instanceUUIDsP1);
    instanceUUIDsP1AndP2.addAll(instanceUUIDsP2);

    final Set<ProcessDefinitionUUID> fromP1 = new HashSet<ProcessDefinitionUUID>();
    fromP1.add(processUUID1);

    final Set<ProcessDefinitionUUID> fromP2 = new HashSet<ProcessDefinitionUUID>();
    fromP2.add(processUUID2);

    final Set<ProcessDefinitionUUID> fromP1AndP2 = new HashSet<ProcessDefinitionUUID>();
    fromP1AndP2.add(processUUID1);
    fromP1AndP2.add(processUUID2);

    // started date
    // started date asc from p1
    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithInvolvedUser("john", 0, 5, fromP1, ProcessInstanceCriterion.STARTED_DATE_ASC);
    checkListContent(0, 5, 5, instanceUUIDsP1, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUser("john", 5, 10, fromP1,
        ProcessInstanceCriterion.STARTED_DATE_ASC);
    checkListContent(5, 10, 5, instanceUUIDsP1, retrievedInstances);

    // started date asc from p2
    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUser("john", 0, 3, fromP2,
        ProcessInstanceCriterion.STARTED_DATE_ASC);
    checkListContent(0, 3, 3, instanceUUIDsP2, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUser("john", 3, 5, fromP2,
        ProcessInstanceCriterion.STARTED_DATE_ASC);
    checkListContent(3, 5, 2, instanceUUIDsP2, retrievedInstances);

    // started date asc from p1 and p2
    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUser("john", 0, 8, fromP1AndP2,
        ProcessInstanceCriterion.STARTED_DATE_ASC);
    checkListContent(0, 8, 8, instanceUUIDsP1AndP2, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUser("john", 8, 20,
        fromP1AndP2, ProcessInstanceCriterion.STARTED_DATE_ASC);
    checkListContent(8, 20, 7, instanceUUIDsP1AndP2, retrievedInstances);

    getManagementAPI().deleteProcess(processUUID1);
    getManagementAPI().deleteProcess(processUUID2);
  }

  public void testGetLightParentProcessInstancesWithInvolvedUserFromProcessUUIDsPagingOderByEndedDateAsc()
      throws Exception {
    ProcessDefinition process1 = getProcess();
    ProcessDefinition process2 = getProcess2();

    process1 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process1));
    process2 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process2));

    final ProcessDefinitionUUID processUUID1 = process1.getUUID();
    final ProcessDefinitionUUID processUUID2 = process2.getUUID();

    final int numberOfProcess1 = 10;
    final List<ProcessInstanceUUID> instanceUUIDsP1 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID1, numberOfProcess1, instanceUUIDsP1);

    final int numberOfProcess2 = 5;
    final List<ProcessInstanceUUID> instanceUUIDsP2 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID2, numberOfProcess2, instanceUUIDsP2);

    final List<ProcessInstanceUUID> instanceUUIDsP2AndP1 = new ArrayList<ProcessInstanceUUID>();
    instanceUUIDsP2AndP1.addAll(instanceUUIDsP2);
    instanceUUIDsP2AndP1.addAll(instanceUUIDsP1);

    final Set<ProcessDefinitionUUID> fromP1 = new HashSet<ProcessDefinitionUUID>();
    fromP1.add(processUUID1);

    final Set<ProcessDefinitionUUID> fromP2 = new HashSet<ProcessDefinitionUUID>();
    fromP2.add(processUUID2);

    final Set<ProcessDefinitionUUID> fromP1AndP2 = new HashSet<ProcessDefinitionUUID>();
    fromP1AndP2.add(processUUID1);
    fromP1AndP2.add(processUUID2);

    // execute human tasks
    for (int i = 0; i < numberOfProcess2 + numberOfProcess1; i++) {
      executeTask(instanceUUIDsP2AndP1.get(i), "step1");
      Thread.sleep(3);
    }

    // ended date
    // ended date asc from p1
    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithInvolvedUser("john", 0, 5, fromP1, ProcessInstanceCriterion.ENDED_DATE_ASC);
    checkListContent(0, 5, 5, instanceUUIDsP1, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUser("john", 5, 10, fromP1,
        ProcessInstanceCriterion.ENDED_DATE_ASC);
    checkListContent(5, 10, 5, instanceUUIDsP1, retrievedInstances);

    // ended date asc from p2
    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUser("john", 0, 3, fromP2,
        ProcessInstanceCriterion.ENDED_DATE_ASC);
    checkListContent(0, 3, 3, instanceUUIDsP2, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUser("john", 3, 5, fromP2,
        ProcessInstanceCriterion.ENDED_DATE_ASC);
    checkListContent(3, 5, 2, instanceUUIDsP2, retrievedInstances);

    // ended date asc from p1 and p2
    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUser("john", 0, 8, fromP1AndP2,
        ProcessInstanceCriterion.ENDED_DATE_ASC);
    checkListContent(0, 8, 8, instanceUUIDsP2AndP1, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUser("john", 8, 20,
        fromP1AndP2, ProcessInstanceCriterion.ENDED_DATE_ASC);
    checkListContent(8, 20, 7, instanceUUIDsP2AndP1, retrievedInstances);

    getManagementAPI().deleteProcess(processUUID1);
    getManagementAPI().deleteProcess(processUUID2);
  }

  public void testGetLightParentProcessInstancesWithInvolvedUserFromProcessUUIDsPagingOderByInstanceNumberAsc()
      throws Exception {
    ProcessDefinition process1 = getProcess();
    ProcessDefinition process2 = getProcess2();

    process1 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process1));
    process2 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process2));

    final ProcessDefinitionUUID processUUID1 = process1.getUUID();
    final ProcessDefinitionUUID processUUID2 = process2.getUUID();

    final int numberOfProcess1 = 10;
    final List<ProcessInstanceUUID> instanceUUIDsP1 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID1, numberOfProcess1, instanceUUIDsP1);

    final int numberOfProcess2 = 5;
    final List<ProcessInstanceUUID> instanceUUIDsP2 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID2, numberOfProcess2, instanceUUIDsP2);

    final Set<ProcessDefinitionUUID> fromP1 = new HashSet<ProcessDefinitionUUID>();
    fromP1.add(processUUID1);

    final Set<ProcessDefinitionUUID> fromP2 = new HashSet<ProcessDefinitionUUID>();
    fromP2.add(processUUID2);

    // instance number
    // instance number asc from p1
    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithInvolvedUser("john", 0, 5, fromP1,
            ProcessInstanceCriterion.INSTANCE_NUMBER_ASC);
    checkListContent(0, 5, 5, instanceUUIDsP1, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUser("john", 5, 10, fromP1,
        ProcessInstanceCriterion.INSTANCE_NUMBER_ASC);
    checkListContent(5, 10, 5, instanceUUIDsP1, retrievedInstances);

    // instance number asc from p2
    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUser("john", 0, 3, fromP2,
        ProcessInstanceCriterion.INSTANCE_NUMBER_ASC);
    checkListContent(0, 3, 3, instanceUUIDsP2, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUser("john", 3, 5, fromP2,
        ProcessInstanceCriterion.INSTANCE_NUMBER_ASC);
    checkListContent(3, 5, 2, instanceUUIDsP2, retrievedInstances);

    getManagementAPI().deleteProcess(processUUID1);
    getManagementAPI().deleteProcess(processUUID2);
  }

  public void testGetLightParentProcessInstancesWithInvolvedUserFromProcessUUIDsPagingOderByInstanceUUIDAsc()
      throws Exception {
    ProcessDefinition process1 = getProcess();
    ProcessDefinition process2 = getProcess2();

    process1 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process1));
    process2 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process2));

    final ProcessDefinitionUUID processUUID1 = process1.getUUID();
    final ProcessDefinitionUUID processUUID2 = process2.getUUID();

    final int numberOfProcess1 = 10;
    final List<ProcessInstanceUUID> instanceUUIDsP1 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID1, numberOfProcess1, instanceUUIDsP1);

    final int numberOfProcess2 = 5;
    final List<ProcessInstanceUUID> instanceUUIDsP2 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID2, numberOfProcess2, instanceUUIDsP2);

    final List<ProcessInstanceUUID> instanceUUIDsP1AndP2 = new ArrayList<ProcessInstanceUUID>();
    instanceUUIDsP1AndP2.addAll(instanceUUIDsP1);
    instanceUUIDsP1AndP2.addAll(instanceUUIDsP2);

    final Set<ProcessDefinitionUUID> fromP1 = new HashSet<ProcessDefinitionUUID>();
    fromP1.add(processUUID1);

    final Set<ProcessDefinitionUUID> fromP2 = new HashSet<ProcessDefinitionUUID>();
    fromP2.add(processUUID2);

    final Set<ProcessDefinitionUUID> fromP1AndP2 = new HashSet<ProcessDefinitionUUID>();
    fromP1AndP2.add(processUUID1);
    fromP1AndP2.add(processUUID2);

    // instance UUID
    // instance UUID asc from p1
    Collections.sort(instanceUUIDsP1, new ProcessInstanceUUIDComparator());
    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithInvolvedUser("john", 0, 5, fromP1,
            ProcessInstanceCriterion.INSTANCE_UUID_ASC);
    checkListContent(0, 5, 5, instanceUUIDsP1, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUser("john", 5, 10, fromP1,
        ProcessInstanceCriterion.INSTANCE_UUID_ASC);
    checkListContent(5, 10, 5, instanceUUIDsP1, retrievedInstances);

    // instance UUID asc from p2
    Collections.sort(instanceUUIDsP2, new ProcessInstanceUUIDComparator());
    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUser("john", 0, 3, fromP2,
        ProcessInstanceCriterion.INSTANCE_UUID_ASC);
    checkListContent(0, 3, 3, instanceUUIDsP2, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUser("john", 3, 5, fromP2,
        ProcessInstanceCriterion.INSTANCE_UUID_ASC);
    checkListContent(3, 5, 2, instanceUUIDsP2, retrievedInstances);

    // instance UUID asc from p1 and p2
    Collections.sort(instanceUUIDsP1AndP2, new ProcessInstanceUUIDComparator());
    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUser("john", 0, 8, fromP1AndP2,
        ProcessInstanceCriterion.INSTANCE_UUID_ASC);
    checkListContent(0, 8, 8, instanceUUIDsP1AndP2, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUser("john", 8, 20,
        fromP1AndP2, ProcessInstanceCriterion.INSTANCE_UUID_ASC);
    checkListContent(8, 20, 7, instanceUUIDsP1AndP2, retrievedInstances);

    getManagementAPI().deleteProcess(processUUID1);
    getManagementAPI().deleteProcess(processUUID2);
  }

  public void testGetLightParentProcessInstancesWithInvolvedUserFromProcessUUIDsPagingOderByLastUpdateDesc()
      throws Exception {
    ProcessDefinition process1 = getProcess();
    ProcessDefinition process2 = getProcess2();

    process1 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process1));
    process2 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process2));

    final ProcessDefinitionUUID processUUID1 = process1.getUUID();
    final ProcessDefinitionUUID processUUID2 = process2.getUUID();

    final int numberOfProcess1 = 10;
    final List<ProcessInstanceUUID> instanceUUIDsP1 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID1, numberOfProcess1, instanceUUIDsP1);
    updateInstanceVariable(instanceUUIDsP1, "text", "Hello", true);

    final int numberOfProcess2 = 5;
    final List<ProcessInstanceUUID> instanceUUIDsP2 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID2, numberOfProcess2, instanceUUIDsP2);
    updateInstanceVariable(instanceUUIDsP2, "text", "Hello", true);

    final Set<ProcessDefinitionUUID> fromP1 = new HashSet<ProcessDefinitionUUID>();
    fromP1.add(processUUID1);

    final Set<ProcessDefinitionUUID> fromP2 = new HashSet<ProcessDefinitionUUID>();
    fromP2.add(processUUID2);

    final Set<ProcessDefinitionUUID> fromP1AndP2 = new HashSet<ProcessDefinitionUUID>();
    fromP1AndP2.add(processUUID1);
    fromP1AndP2.add(processUUID2);

    // last update desc from p1
    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithInvolvedUser("john", 0, 5, fromP1, ProcessInstanceCriterion.LAST_UPDATE_DESC);
    checkListContent(0, 5, 5, instanceUUIDsP1, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUser("john", 5, 10, fromP1,
        ProcessInstanceCriterion.LAST_UPDATE_DESC);
    checkListContent(5, 10, 5, instanceUUIDsP1, retrievedInstances);

    // last update desc from p2
    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUser("john", 0, 3, fromP2,
        ProcessInstanceCriterion.LAST_UPDATE_DESC);
    checkListContent(0, 3, 3, instanceUUIDsP2, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUser("john", 3, 5, fromP2,
        ProcessInstanceCriterion.LAST_UPDATE_DESC);
    checkListContent(3, 5, 2, instanceUUIDsP2, retrievedInstances);

    // last update desc from p1 and p2
    final List<ProcessInstanceUUID> expectP1 = getListCopy(instanceUUIDsP1);
    Collections.reverse(expectP1);
    final List<ProcessInstanceUUID> expectP2 = getListCopy(instanceUUIDsP2);
    Collections.reverse(expectP2);

    final List<ProcessInstanceUUID> expectedOrder = new ArrayList<ProcessInstanceUUID>();
    expectedOrder.addAll(expectP1);
    expectedOrder.addAll(expectP2);
    Collections.reverse(expectedOrder);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUser("john", 0, 8, fromP1AndP2,
        ProcessInstanceCriterion.LAST_UPDATE_DESC);
    checkListContent(0, 8, 8, expectedOrder, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUser("john", 8, 20,
        fromP1AndP2, ProcessInstanceCriterion.LAST_UPDATE_DESC);
    checkListContent(8, 20, 7, expectedOrder, retrievedInstances);

    getManagementAPI().deleteProcess(processUUID1);
    getManagementAPI().deleteProcess(processUUID2);
  }

  public void testGetLightParentProcessInstancesWithInvolvedUserFromProcessUUIDsPagingOderByStartedDateDesc()
      throws Exception {
    ProcessDefinition process1 = getProcess();
    ProcessDefinition process2 = getProcess2();

    process1 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process1));
    process2 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process2));

    final ProcessDefinitionUUID processUUID1 = process1.getUUID();
    final ProcessDefinitionUUID processUUID2 = process2.getUUID();

    final int numberOfProcess1 = 10;
    final List<ProcessInstanceUUID> instanceUUIDsP1 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID1, numberOfProcess1, instanceUUIDsP1);

    updateInstanceVariable(instanceUUIDsP1, "text", "Hello", true);

    final int numberOfProcess2 = 5;
    final List<ProcessInstanceUUID> instanceUUIDsP2 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID2, numberOfProcess2, instanceUUIDsP2);

    updateInstanceVariable(instanceUUIDsP2, "text", "Hello", true);

    final List<ProcessInstanceUUID> instanceUUIDsP1AndP2 = new ArrayList<ProcessInstanceUUID>();
    instanceUUIDsP1AndP2.addAll(instanceUUIDsP1);
    instanceUUIDsP1AndP2.addAll(instanceUUIDsP2);

    final Set<ProcessDefinitionUUID> fromP1 = new HashSet<ProcessDefinitionUUID>();
    fromP1.add(processUUID1);

    final Set<ProcessDefinitionUUID> fromP2 = new HashSet<ProcessDefinitionUUID>();
    fromP2.add(processUUID2);

    final Set<ProcessDefinitionUUID> fromP1AndP2 = new HashSet<ProcessDefinitionUUID>();
    fromP1AndP2.add(processUUID1);
    fromP1AndP2.add(processUUID2);

    // started date desc from p1
    Collections.reverse(instanceUUIDsP1);
    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithInvolvedUser("john", 0, 5, fromP1,
            ProcessInstanceCriterion.STARTED_DATE_DESC);
    checkListContent(0, 5, 5, instanceUUIDsP1, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUser("john", 5, 10, fromP1,
        ProcessInstanceCriterion.STARTED_DATE_DESC);
    checkListContent(5, 10, 5, instanceUUIDsP1, retrievedInstances);

    // started date desc from p2
    Collections.reverse(instanceUUIDsP2);
    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUser("john", 0, 3, fromP2,
        ProcessInstanceCriterion.STARTED_DATE_DESC);
    checkListContent(0, 3, 3, instanceUUIDsP2, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUser("john", 3, 5, fromP2,
        ProcessInstanceCriterion.STARTED_DATE_DESC);
    checkListContent(3, 5, 2, instanceUUIDsP2, retrievedInstances);

    // started date desc from p1 and p2
    Collections.reverse(instanceUUIDsP1AndP2);
    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUser("john", 0, 8, fromP1AndP2,
        ProcessInstanceCriterion.STARTED_DATE_DESC);
    checkListContent(0, 8, 8, instanceUUIDsP1AndP2, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUser("john", 8, 20,
        fromP1AndP2, ProcessInstanceCriterion.STARTED_DATE_DESC);
    checkListContent(8, 20, 7, instanceUUIDsP1AndP2, retrievedInstances);

    getManagementAPI().deleteProcess(processUUID1);
    getManagementAPI().deleteProcess(processUUID2);
  }

  public void testGetLightParentProcessInstancesWithInvolvedUserFromProcessUUIDsPagingOderByEndedDateDesc()
      throws Exception {
    ProcessDefinition process1 = getProcess();
    ProcessDefinition process2 = getProcess2();

    process1 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process1));
    process2 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process2));

    final ProcessDefinitionUUID processUUID1 = process1.getUUID();
    final ProcessDefinitionUUID processUUID2 = process2.getUUID();

    final int numberOfProcess1 = 10;
    final List<ProcessInstanceUUID> instanceUUIDsP1 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID1, numberOfProcess1, instanceUUIDsP1);

    final int numberOfProcess2 = 5;
    final List<ProcessInstanceUUID> instanceUUIDsP2 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID2, numberOfProcess2, instanceUUIDsP2);

    final List<ProcessInstanceUUID> instanceUUIDsP2AndP1 = new ArrayList<ProcessInstanceUUID>();
    instanceUUIDsP2AndP1.addAll(instanceUUIDsP2);
    instanceUUIDsP2AndP1.addAll(instanceUUIDsP1);

    final Set<ProcessDefinitionUUID> fromP1 = new HashSet<ProcessDefinitionUUID>();
    fromP1.add(processUUID1);

    final Set<ProcessDefinitionUUID> fromP2 = new HashSet<ProcessDefinitionUUID>();
    fromP2.add(processUUID2);

    final Set<ProcessDefinitionUUID> fromP1AndP2 = new HashSet<ProcessDefinitionUUID>();
    fromP1AndP2.add(processUUID1);
    fromP1AndP2.add(processUUID2);

    // execute human tasks
    for (int i = 0; i < numberOfProcess2 + numberOfProcess1; i++) {
      executeTask(instanceUUIDsP2AndP1.get(i), "step1");
      Thread.sleep(3);
    }

    // ended date desc from p1
    Collections.reverse(instanceUUIDsP1);
    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithInvolvedUser("john", 0, 5, fromP1, ProcessInstanceCriterion.ENDED_DATE_DESC);
    checkListContent(0, 5, 5, instanceUUIDsP1, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUser("john", 5, 10, fromP1,
        ProcessInstanceCriterion.ENDED_DATE_DESC);
    checkListContent(5, 10, 5, instanceUUIDsP1, retrievedInstances);

    // ended date desc from p2
    Collections.reverse(instanceUUIDsP2);
    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUser("john", 0, 3, fromP2,
        ProcessInstanceCriterion.ENDED_DATE_DESC);
    checkListContent(0, 3, 3, instanceUUIDsP2, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUser("john", 3, 5, fromP2,
        ProcessInstanceCriterion.ENDED_DATE_DESC);
    checkListContent(3, 5, 2, instanceUUIDsP2, retrievedInstances);

    // ended date desc from p1 and p2
    Collections.reverse(instanceUUIDsP2AndP1);
    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUser("john", 0, 8, fromP1AndP2,
        ProcessInstanceCriterion.ENDED_DATE_DESC);
    checkListContent(0, 8, 8, instanceUUIDsP2AndP1, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUser("john", 8, 20,
        fromP1AndP2, ProcessInstanceCriterion.ENDED_DATE_DESC);
    checkListContent(8, 20, 7, instanceUUIDsP2AndP1, retrievedInstances);

    getManagementAPI().deleteProcess(processUUID1);
    getManagementAPI().deleteProcess(processUUID2);
  }

  public void testGetLightParentProcessInstancesWithInvolvedUserFromProcessUUIDsPagingOderByInstanceNumberDesc()
      throws Exception {
    ProcessDefinition process1 = getProcess();
    ProcessDefinition process2 = getProcess2();

    process1 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process1));
    process2 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process2));

    final ProcessDefinitionUUID processUUID1 = process1.getUUID();
    final ProcessDefinitionUUID processUUID2 = process2.getUUID();

    final int numberOfProcess1 = 10;
    final List<ProcessInstanceUUID> instanceUUIDsP1 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID1, numberOfProcess1, instanceUUIDsP1);

    final int numberOfProcess2 = 5;
    final List<ProcessInstanceUUID> instanceUUIDsP2 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID2, numberOfProcess2, instanceUUIDsP2);

    final Set<ProcessDefinitionUUID> fromP1 = new HashSet<ProcessDefinitionUUID>();
    fromP1.add(processUUID1);

    final Set<ProcessDefinitionUUID> fromP2 = new HashSet<ProcessDefinitionUUID>();
    fromP2.add(processUUID2);

    // instance number desc from p1
    Collections.reverse(instanceUUIDsP1);
    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithInvolvedUser("john", 0, 5, fromP1,
            ProcessInstanceCriterion.INSTANCE_NUMBER_DESC);
    checkListContent(0, 5, 5, instanceUUIDsP1, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUser("john", 5, 10, fromP1,
        ProcessInstanceCriterion.INSTANCE_NUMBER_DESC);
    checkListContent(5, 10, 5, instanceUUIDsP1, retrievedInstances);

    // instance number desc from p2
    Collections.reverse(instanceUUIDsP2);
    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUser("john", 0, 3, fromP2,
        ProcessInstanceCriterion.INSTANCE_NUMBER_DESC);
    checkListContent(0, 3, 3, instanceUUIDsP2, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUser("john", 3, 5, fromP2,
        ProcessInstanceCriterion.INSTANCE_NUMBER_DESC);
    checkListContent(3, 5, 2, instanceUUIDsP2, retrievedInstances);

    getManagementAPI().deleteProcess(processUUID1);
    getManagementAPI().deleteProcess(processUUID2);
  }

  public void testGetLightParentProcessInstancesWithInvolvedUserFromProcessUUIDsPagingOderByInstanceUUIDDesc()
      throws Exception {
    ProcessDefinition process1 = getProcess();
    ProcessDefinition process2 = getProcess2();

    process1 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process1));
    process2 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process2));

    final ProcessDefinitionUUID processUUID1 = process1.getUUID();
    final ProcessDefinitionUUID processUUID2 = process2.getUUID();

    final int numberOfProcess1 = 10;
    final List<ProcessInstanceUUID> instanceUUIDsP1 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID1, numberOfProcess1, instanceUUIDsP1);

    final int numberOfProcess2 = 5;
    final List<ProcessInstanceUUID> instanceUUIDsP2 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID2, numberOfProcess2, instanceUUIDsP2);

    final List<ProcessInstanceUUID> instanceUUIDsP1AndP2 = new ArrayList<ProcessInstanceUUID>();
    instanceUUIDsP1AndP2.addAll(instanceUUIDsP1);
    instanceUUIDsP1AndP2.addAll(instanceUUIDsP2);

    final Set<ProcessDefinitionUUID> fromP1 = new HashSet<ProcessDefinitionUUID>();
    fromP1.add(processUUID1);

    final Set<ProcessDefinitionUUID> fromP2 = new HashSet<ProcessDefinitionUUID>();
    fromP2.add(processUUID2);

    final Set<ProcessDefinitionUUID> fromP1AndP2 = new HashSet<ProcessDefinitionUUID>();
    fromP1AndP2.add(processUUID1);
    fromP1AndP2.add(processUUID2);

    // instance UUID desc from p1
    Collections.sort(instanceUUIDsP1, new ProcessInstanceUUIDComparator());
    Collections.reverse(instanceUUIDsP1);
    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithInvolvedUser("john", 0, 5, fromP1,
            ProcessInstanceCriterion.INSTANCE_UUID_DESC);
    checkListContent(0, 5, 5, instanceUUIDsP1, retrievedInstances);

    // instance UUID desc from p2
    Collections.sort(instanceUUIDsP2, new ProcessInstanceUUIDComparator());
    Collections.reverse(instanceUUIDsP2);
    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUser("john", 0, 3, fromP2,
        ProcessInstanceCriterion.INSTANCE_UUID_DESC);
    checkListContent(0, 3, 3, instanceUUIDsP2, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUser("john", 3, 5, fromP2,
        ProcessInstanceCriterion.INSTANCE_UUID_DESC);
    checkListContent(3, 5, 2, instanceUUIDsP2, retrievedInstances);

    // instance UUID desc from p1 and p2
    Collections.sort(instanceUUIDsP1AndP2, new ProcessInstanceUUIDComparator());
    Collections.reverse(instanceUUIDsP1AndP2);
    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUser("john", 0, 8, fromP1AndP2,
        ProcessInstanceCriterion.INSTANCE_UUID_DESC);
    checkListContent(0, 8, 8, instanceUUIDsP1AndP2, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUser("john", 8, 20,
        fromP1AndP2, ProcessInstanceCriterion.INSTANCE_UUID_DESC);
    checkListContent(8, 20, 7, instanceUUIDsP1AndP2, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUser("john", 5, 10, fromP1,
        ProcessInstanceCriterion.INSTANCE_UUID_DESC);
    checkListContent(5, 10, 5, instanceUUIDsP1, retrievedInstances);

    getManagementAPI().deleteProcess(processUUID1);
    getManagementAPI().deleteProcess(processUUID2);
  }

  public void testGetLightParentProcessInstancesWithInvolvedUserFromProcessUUIDsPagingOderByDefault() throws Exception {
    ProcessDefinition process1 = getProcess();
    ProcessDefinition process2 = getProcess2();

    process1 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process1));
    process2 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process2));

    final ProcessDefinitionUUID processUUID1 = process1.getUUID();
    final ProcessDefinitionUUID processUUID2 = process2.getUUID();

    final int numberOfProcess1 = 10;
    final List<ProcessInstanceUUID> instanceUUIDsP1 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID1, numberOfProcess1, instanceUUIDsP1);

    updateInstanceVariable(instanceUUIDsP1, "text", "Hello", true);

    final int numberOfProcess2 = 5;
    final List<ProcessInstanceUUID> instanceUUIDsP2 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID2, numberOfProcess2, instanceUUIDsP2);

    updateInstanceVariable(instanceUUIDsP2, "text", "Hello", true);

    final Set<ProcessDefinitionUUID> fromP1 = new HashSet<ProcessDefinitionUUID>();
    fromP1.add(processUUID1);

    final Set<ProcessDefinitionUUID> fromP2 = new HashSet<ProcessDefinitionUUID>();
    fromP2.add(processUUID2);

    final Set<ProcessDefinitionUUID> fromP1AndP2 = new HashSet<ProcessDefinitionUUID>();
    fromP1AndP2.add(processUUID1);
    fromP1AndP2.add(processUUID2);

    // default (last update desc)
    // default from p1
    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithInvolvedUser("john", 0, 5, fromP1, ProcessInstanceCriterion.DEFAULT);
    checkListContent(0, 5, 5, instanceUUIDsP1, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUser("john", 5, 10, fromP1,
        ProcessInstanceCriterion.DEFAULT);
    checkListContent(5, 10, 5, instanceUUIDsP1, retrievedInstances);

    // default from p2
    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUser("john", 0, 3, fromP2,
        ProcessInstanceCriterion.DEFAULT);
    checkListContent(0, 3, 3, instanceUUIDsP2, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUser("john", 3, 5, fromP2,
        ProcessInstanceCriterion.DEFAULT);
    checkListContent(3, 5, 2, instanceUUIDsP2, retrievedInstances);

    // default from p1 and p2
    final List<ProcessInstanceUUID> expectP1 = getListCopy(instanceUUIDsP1);
    Collections.reverse(expectP1);
    final List<ProcessInstanceUUID> expectP2 = getListCopy(instanceUUIDsP2);
    Collections.reverse(expectP2);

    final List<ProcessInstanceUUID> expectedOrder = new ArrayList<ProcessInstanceUUID>();
    expectedOrder.addAll(expectP1);
    expectedOrder.addAll(expectP2);
    Collections.reverse(expectedOrder);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUser("john", 0, 8, fromP1AndP2,
        ProcessInstanceCriterion.DEFAULT);
    checkListContent(0, 8, 8, expectedOrder, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUser("john", 8, 20,
        fromP1AndP2, ProcessInstanceCriterion.DEFAULT);
    checkListContent(8, 20, 7, expectedOrder, retrievedInstances);

    // empty list
    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUser("john", 0, 8,
        new HashSet<ProcessDefinitionUUID>(), ProcessInstanceCriterion.DEFAULT);
    assertEquals(0, retrievedInstances.size());

    getManagementAPI().deleteProcess(processUUID1);
    getManagementAPI().deleteProcess(processUUID2);
  }

  public void testGetLightParentProcessInstancesWithInvolvedUserExceptPagingOrderByLastUpdateAsc() throws Exception {
    ProcessDefinition process1 = getProcess();
    ProcessDefinition process2 = getProcess2();

    process1 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process1));
    process2 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process2));

    final ProcessDefinitionUUID processUUID1 = process1.getUUID();
    final ProcessDefinitionUUID processUUID2 = process2.getUUID();

    final int numberOfProcess = 10;
    final List<ProcessInstanceUUID> instanceUUIDsP1 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID1, numberOfProcess, instanceUUIDsP1);
    updateInstanceVariable(instanceUUIDsP1, "text", "Hello", true);

    final List<ProcessInstanceUUID> instanceUUIDsP2 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID2, numberOfProcess, instanceUUIDsP2);
    updateInstanceVariable(instanceUUIDsP2, "text", "Hello", true);

    final Set<ProcessDefinitionUUID> exceptP1 = Collections.singleton(processUUID1);
    final Set<ProcessDefinitionUUID> exceptP2 = Collections.singleton(processUUID2);

    // **** last update
    // last update asc except p1
    List<ProcessInstanceUUID> expectedOrder = getListCopy(instanceUUIDsP2);
    Collections.reverse(expectedOrder);
    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithInvolvedUserExcept("john", 0, 5, exceptP1,
            ProcessInstanceCriterion.LAST_UPDATE_ASC);
    checkListContent(0, 5, 5, expectedOrder, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUserExcept("john", 5, 10,
        exceptP1, ProcessInstanceCriterion.LAST_UPDATE_ASC);
    checkListContent(5, 10, 5, expectedOrder, retrievedInstances);

    // last update asc except p2
    expectedOrder = getListCopy(instanceUUIDsP1);
    Collections.reverse(expectedOrder);
    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUserExcept("john", 0, 5,
        exceptP2, ProcessInstanceCriterion.LAST_UPDATE_ASC);
    checkListContent(0, 5, 5, expectedOrder, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUserExcept("john", 5, 5,
        exceptP2, ProcessInstanceCriterion.LAST_UPDATE_ASC);
    checkListContent(5, 5, 5, expectedOrder, retrievedInstances);

    // last update asc except null
    final List<ProcessInstanceUUID> expectP1 = getListCopy(instanceUUIDsP1);
    Collections.reverse(expectP1);
    final List<ProcessInstanceUUID> expectP2 = getListCopy(instanceUUIDsP2);
    Collections.reverse(expectP2);

    expectedOrder = new ArrayList<ProcessInstanceUUID>();
    expectedOrder.addAll(expectP1);
    expectedOrder.addAll(expectP2);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUserExcept("john", 0, 8, null,
        ProcessInstanceCriterion.LAST_UPDATE_ASC);
    checkListContent(0, 8, 8, expectedOrder, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUserExcept("john", 8, 20, null,
        ProcessInstanceCriterion.LAST_UPDATE_ASC);
    checkListContent(8, 20, 12, expectedOrder, retrievedInstances);

    getManagementAPI().deleteProcess(processUUID1);
    getManagementAPI().deleteProcess(processUUID2);
  }

  public void testGetLightParentProcessInstancesWithInvolvedUserExceptPagingOrderByStartedDateAsc() throws Exception {
    ProcessDefinition process1 = getProcess();
    ProcessDefinition process2 = getProcess2();

    process1 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process1));
    process2 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process2));

    final ProcessDefinitionUUID processUUID1 = process1.getUUID();
    final ProcessDefinitionUUID processUUID2 = process2.getUUID();

    final int numberOfProcess = 10;
    final List<ProcessInstanceUUID> instanceUUIDsP1 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID1, numberOfProcess, instanceUUIDsP1);

    final List<ProcessInstanceUUID> instanceUUIDsP2 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID2, numberOfProcess, instanceUUIDsP2);

    final List<ProcessInstanceUUID> instanceUUIDsP1AndP2 = new ArrayList<ProcessInstanceUUID>();
    instanceUUIDsP1AndP2.addAll(instanceUUIDsP1);
    instanceUUIDsP1AndP2.addAll(instanceUUIDsP2);

    final Set<ProcessDefinitionUUID> exceptP1 = new HashSet<ProcessDefinitionUUID>();
    exceptP1.add(processUUID1);

    final Set<ProcessDefinitionUUID> exceptP2 = new HashSet<ProcessDefinitionUUID>();
    exceptP2.add(processUUID2);

    // started date
    // started date asc except p1
    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithInvolvedUserExcept("john", 0, 5, exceptP1,
            ProcessInstanceCriterion.STARTED_DATE_ASC);
    checkListContent(0, 5, 5, instanceUUIDsP2, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUserExcept("john", 5, 10,
        exceptP1, ProcessInstanceCriterion.STARTED_DATE_ASC);
    checkListContent(5, 10, 5, instanceUUIDsP2, retrievedInstances);

    // started date asc except p2
    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUserExcept("john", 0, 5,
        exceptP2, ProcessInstanceCriterion.STARTED_DATE_ASC);
    checkListContent(0, 5, 5, instanceUUIDsP1, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUserExcept("john", 5, 5,
        exceptP2, ProcessInstanceCriterion.STARTED_DATE_ASC);
    checkListContent(5, 5, 5, instanceUUIDsP1, retrievedInstances);

    // started date asc except null
    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUserExcept("john", 0, 8, null,
        ProcessInstanceCriterion.STARTED_DATE_ASC);
    checkListContent(0, 8, 8, instanceUUIDsP1AndP2, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUserExcept("john", 8, 20, null,
        ProcessInstanceCriterion.STARTED_DATE_ASC);
    checkListContent(8, 20, 12, instanceUUIDsP1AndP2, retrievedInstances);

    getManagementAPI().deleteProcess(processUUID1);
    getManagementAPI().deleteProcess(processUUID2);
  }

  public void testGetLightParentProcessInstancesWithInvolvedUserExceptPagingOrderByEndedDateAsc() throws Exception {
    ProcessDefinition process1 = getProcess();
    ProcessDefinition process2 = getProcess2();

    process1 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process1));
    process2 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process2));

    final ProcessDefinitionUUID processUUID1 = process1.getUUID();
    final ProcessDefinitionUUID processUUID2 = process2.getUUID();

    final int numberOfProcess = 10;
    final List<ProcessInstanceUUID> instanceUUIDsP1 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID1, numberOfProcess, instanceUUIDsP1);

    final List<ProcessInstanceUUID> instanceUUIDsP2 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID2, numberOfProcess, instanceUUIDsP2);

    final List<ProcessInstanceUUID> instanceUUIDsP2AndP1 = new ArrayList<ProcessInstanceUUID>();
    instanceUUIDsP2AndP1.addAll(instanceUUIDsP2);
    instanceUUIDsP2AndP1.addAll(instanceUUIDsP1);

    final Set<ProcessDefinitionUUID> exceptP1 = new HashSet<ProcessDefinitionUUID>();
    exceptP1.add(processUUID1);

    final Set<ProcessDefinitionUUID> exceptP2 = new HashSet<ProcessDefinitionUUID>();
    exceptP2.add(processUUID2);

    // execute human tasks
    for (int i = 0; i < numberOfProcess + numberOfProcess; i++) {
      executeTask(instanceUUIDsP2AndP1.get(i), "step1");
      Thread.sleep(3);
    }

    // ended date
    // ended date asc except p1
    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithInvolvedUserExcept("john", 0, 5, exceptP1,
            ProcessInstanceCriterion.ENDED_DATE_ASC);
    checkListContent(0, 5, 5, instanceUUIDsP2, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUserExcept("john", 5, 10,
        exceptP1, ProcessInstanceCriterion.ENDED_DATE_ASC);
    checkListContent(5, 10, 5, instanceUUIDsP2, retrievedInstances);

    // ended date asc except p2
    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUserExcept("john", 0, 5,
        exceptP2, ProcessInstanceCriterion.ENDED_DATE_ASC);
    checkListContent(0, 5, 5, instanceUUIDsP1, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUserExcept("john", 5, 5,
        exceptP2, ProcessInstanceCriterion.ENDED_DATE_ASC);
    checkListContent(5, 5, 5, instanceUUIDsP1, retrievedInstances);

    // ended date asc except null
    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUserExcept("john", 0, 8, null,
        ProcessInstanceCriterion.ENDED_DATE_ASC);
    checkListContent(0, 8, 8, instanceUUIDsP2AndP1, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUserExcept("john", 8, 20, null,
        ProcessInstanceCriterion.ENDED_DATE_ASC);
    checkListContent(8, 20, 12, instanceUUIDsP2AndP1, retrievedInstances);

    getManagementAPI().deleteProcess(processUUID1);
    getManagementAPI().deleteProcess(processUUID2);
  }

  public void testGetLightParentProcessInstancesWithInvolvedUserExceptPagingOrderByInstanceNumberAsc() throws Exception {
    ProcessDefinition process1 = getProcess();
    ProcessDefinition process2 = getProcess2();

    process1 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process1));
    process2 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process2));

    final ProcessDefinitionUUID processUUID1 = process1.getUUID();
    final ProcessDefinitionUUID processUUID2 = process2.getUUID();

    final int numberOfProcess = 10;
    final List<ProcessInstanceUUID> instanceUUIDsP1 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID1, numberOfProcess, instanceUUIDsP1);

    final List<ProcessInstanceUUID> instanceUUIDsP2 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID2, numberOfProcess, instanceUUIDsP2);

    final Set<ProcessDefinitionUUID> exceptP1 = new HashSet<ProcessDefinitionUUID>();
    exceptP1.add(processUUID1);

    final Set<ProcessDefinitionUUID> exceptP2 = new HashSet<ProcessDefinitionUUID>();
    exceptP2.add(processUUID2);

    // instance number asc except p1
    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithInvolvedUserExcept("john", 0, 5, exceptP1,
            ProcessInstanceCriterion.INSTANCE_NUMBER_ASC);
    checkListContent(0, 5, 5, instanceUUIDsP2, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUserExcept("john", 5, 10,
        exceptP1, ProcessInstanceCriterion.INSTANCE_NUMBER_ASC);
    checkListContent(5, 10, 5, instanceUUIDsP2, retrievedInstances);

    // instance number asc except p2
    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUserExcept("john", 0, 5,
        exceptP2, ProcessInstanceCriterion.INSTANCE_NUMBER_ASC);
    checkListContent(0, 5, 5, instanceUUIDsP1, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUserExcept("john", 5, 5,
        exceptP2, ProcessInstanceCriterion.INSTANCE_NUMBER_ASC);
    checkListContent(5, 5, 5, instanceUUIDsP1, retrievedInstances);

    getManagementAPI().deleteProcess(processUUID1);
    getManagementAPI().deleteProcess(processUUID2);
  }

  public void testGetLightParentProcessInstancesWithInvolvedUserExceptPagingOrderByProcessUUIDAsc() throws Exception {
    ProcessDefinition process1 = getProcess();
    ProcessDefinition process2 = getProcess2();

    process1 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process1));
    process2 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process2));

    final ProcessDefinitionUUID processUUID1 = process1.getUUID();
    final ProcessDefinitionUUID processUUID2 = process2.getUUID();

    final int numberOfProcess = 10;
    final List<ProcessInstanceUUID> instanceUUIDsP1 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID1, numberOfProcess, instanceUUIDsP1);

    final List<ProcessInstanceUUID> instanceUUIDsP2 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID2, numberOfProcess, instanceUUIDsP2);

    final List<ProcessInstanceUUID> instanceUUIDsP1AndP2 = new ArrayList<ProcessInstanceUUID>();
    instanceUUIDsP1AndP2.addAll(instanceUUIDsP1);
    instanceUUIDsP1AndP2.addAll(instanceUUIDsP2);

    final Set<ProcessDefinitionUUID> exceptP1 = new HashSet<ProcessDefinitionUUID>();
    exceptP1.add(processUUID1);

    final Set<ProcessDefinitionUUID> exceptP2 = new HashSet<ProcessDefinitionUUID>();
    exceptP2.add(processUUID2);

    // instance UUID asc except p1
    Collections.sort(instanceUUIDsP2, new ProcessInstanceUUIDComparator());
    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithInvolvedUserExcept("john", 0, 5, exceptP1,
            ProcessInstanceCriterion.INSTANCE_UUID_ASC);
    checkListContent(0, 5, 5, instanceUUIDsP2, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUserExcept("john", 5, 10,
        exceptP1, ProcessInstanceCriterion.INSTANCE_UUID_ASC);
    checkListContent(5, 10, 5, instanceUUIDsP2, retrievedInstances);

    // instance UUID asc except p2
    Collections.sort(instanceUUIDsP1, new ProcessInstanceUUIDComparator());
    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUserExcept("john", 0, 5,
        exceptP2, ProcessInstanceCriterion.INSTANCE_UUID_ASC);
    checkListContent(0, 5, 5, instanceUUIDsP1, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUserExcept("john", 5, 5,
        exceptP2, ProcessInstanceCriterion.INSTANCE_UUID_ASC);
    checkListContent(5, 5, 5, instanceUUIDsP1, retrievedInstances);

    // instance UUID asc exceptnull
    Collections.sort(instanceUUIDsP1AndP2, new ProcessInstanceUUIDComparator());
    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUserExcept("john", 0, 8, null,
        ProcessInstanceCriterion.INSTANCE_UUID_ASC);
    checkListContent(0, 8, 8, instanceUUIDsP1AndP2, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUserExcept("john", 8, 20, null,
        ProcessInstanceCriterion.INSTANCE_UUID_ASC);
    checkListContent(8, 20, 12, instanceUUIDsP1AndP2, retrievedInstances);

    getManagementAPI().deleteProcess(processUUID1);
    getManagementAPI().deleteProcess(processUUID2);
  }

  public void testGetLightParentProcessInstancesWithInvolvedUserExceptPagingOrderByLastUpdateDesc() throws Exception {
    ProcessDefinition process1 = getProcess();
    ProcessDefinition process2 = getProcess2();

    process1 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process1));
    process2 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process2));

    final ProcessDefinitionUUID processUUID1 = process1.getUUID();
    final ProcessDefinitionUUID processUUID2 = process2.getUUID();

    final int numberOfProcess = 10;
    final List<ProcessInstanceUUID> instanceUUIDsP1 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID1, numberOfProcess, instanceUUIDsP1);

    updateInstanceVariable(instanceUUIDsP1, "text", "Hello", true);

    final List<ProcessInstanceUUID> instanceUUIDsP2 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID2, numberOfProcess, instanceUUIDsP2);

    updateInstanceVariable(instanceUUIDsP2, "text", "Hello", true);

    final Set<ProcessDefinitionUUID> exceptP1 = new HashSet<ProcessDefinitionUUID>();
    exceptP1.add(processUUID1);

    final Set<ProcessDefinitionUUID> exceptP2 = new HashSet<ProcessDefinitionUUID>();
    exceptP2.add(processUUID2);

    // last update desc except p1
    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithInvolvedUserExcept("john", 0, 5, exceptP1,
            ProcessInstanceCriterion.LAST_UPDATE_DESC);
    checkListContent(0, 5, 5, instanceUUIDsP2, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUserExcept("john", 5, 10,
        exceptP1, ProcessInstanceCriterion.LAST_UPDATE_DESC);
    checkListContent(5, 10, 5, instanceUUIDsP2, retrievedInstances);

    // last update desc except p2
    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUserExcept("john", 0, 5,
        exceptP2, ProcessInstanceCriterion.LAST_UPDATE_DESC);
    checkListContent(0, 5, 5, instanceUUIDsP1, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUserExcept("john", 5, 5,
        exceptP2, ProcessInstanceCriterion.LAST_UPDATE_DESC);
    checkListContent(5, 5, 5, instanceUUIDsP1, retrievedInstances);

    // last update asc except null
    final List<ProcessInstanceUUID> expectP1 = getListCopy(instanceUUIDsP1);
    Collections.reverse(expectP1);
    final List<ProcessInstanceUUID> expectP2 = getListCopy(instanceUUIDsP2);
    Collections.reverse(expectP2);

    final List<ProcessInstanceUUID> expectedOrder = new ArrayList<ProcessInstanceUUID>();
    expectedOrder.addAll(expectP1);
    expectedOrder.addAll(expectP2);
    Collections.reverse(expectedOrder);

    // last update desc except null
    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUserExcept("john", 0, 8, null,
        ProcessInstanceCriterion.LAST_UPDATE_DESC);
    checkListContent(0, 8, 8, expectedOrder, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUserExcept("john", 8, 20, null,
        ProcessInstanceCriterion.LAST_UPDATE_DESC);
    checkListContent(8, 20, 12, expectedOrder, retrievedInstances);

    getManagementAPI().deleteProcess(processUUID1);
    getManagementAPI().deleteProcess(processUUID2);
  }

  public void testGetLightParentProcessInstancesWithInvolvedUserExceptPagingOrderByStartedDateDesc() throws Exception {
    ProcessDefinition process1 = getProcess();
    ProcessDefinition process2 = getProcess2();

    process1 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process1));
    process2 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process2));

    final ProcessDefinitionUUID processUUID1 = process1.getUUID();
    final ProcessDefinitionUUID processUUID2 = process2.getUUID();

    final int numberOfProcess = 10;
    final List<ProcessInstanceUUID> instanceUUIDsP1 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID1, numberOfProcess, instanceUUIDsP1);

    final List<ProcessInstanceUUID> instanceUUIDsP2 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID2, numberOfProcess, instanceUUIDsP2);

    final List<ProcessInstanceUUID> instanceUUIDsP1AndP2 = new ArrayList<ProcessInstanceUUID>();
    instanceUUIDsP1AndP2.addAll(instanceUUIDsP1);
    instanceUUIDsP1AndP2.addAll(instanceUUIDsP2);

    final Set<ProcessDefinitionUUID> exceptP1 = new HashSet<ProcessDefinitionUUID>();
    exceptP1.add(processUUID1);

    final Set<ProcessDefinitionUUID> exceptP2 = new HashSet<ProcessDefinitionUUID>();
    exceptP2.add(processUUID2);

    // started date desc except p1
    Collections.reverse(instanceUUIDsP2);
    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithInvolvedUserExcept("john", 0, 5, exceptP1,
            ProcessInstanceCriterion.STARTED_DATE_DESC);
    checkListContent(0, 5, 5, instanceUUIDsP2, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUserExcept("john", 5, 10,
        exceptP1, ProcessInstanceCriterion.STARTED_DATE_DESC);
    checkListContent(5, 10, 5, instanceUUIDsP2, retrievedInstances);

    // started date desc except p2
    Collections.reverse(instanceUUIDsP1);
    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUserExcept("john", 0, 5,
        exceptP2, ProcessInstanceCriterion.STARTED_DATE_DESC);
    checkListContent(0, 5, 5, instanceUUIDsP1, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUserExcept("john", 5, 5,
        exceptP2, ProcessInstanceCriterion.STARTED_DATE_DESC);
    checkListContent(5, 5, 5, instanceUUIDsP1, retrievedInstances);

    // started date desc except p1 and p2
    Collections.reverse(instanceUUIDsP1AndP2);
    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUserExcept("john", 0, 8, null,
        ProcessInstanceCriterion.STARTED_DATE_DESC);
    checkListContent(0, 8, 8, instanceUUIDsP1AndP2, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUserExcept("john", 8, 20, null,
        ProcessInstanceCriterion.STARTED_DATE_DESC);
    checkListContent(8, 20, 12, instanceUUIDsP1AndP2, retrievedInstances);

    getManagementAPI().deleteProcess(processUUID1);
    getManagementAPI().deleteProcess(processUUID2);
  }

  public void testGetLightParentProcessInstancesWithInvolvedUserExceptPagingOrderByEndedDateDesc() throws Exception {
    ProcessDefinition process1 = getProcess();
    ProcessDefinition process2 = getProcess2();

    process1 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process1));
    process2 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process2));

    final ProcessDefinitionUUID processUUID1 = process1.getUUID();
    final ProcessDefinitionUUID processUUID2 = process2.getUUID();

    final int numberOfProcess = 10;
    final List<ProcessInstanceUUID> instanceUUIDsP1 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID1, numberOfProcess, instanceUUIDsP1);

    final List<ProcessInstanceUUID> instanceUUIDsP2 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID2, numberOfProcess, instanceUUIDsP2);

    final List<ProcessInstanceUUID> instanceUUIDsP2AndP1 = new ArrayList<ProcessInstanceUUID>();
    instanceUUIDsP2AndP1.addAll(instanceUUIDsP2);
    instanceUUIDsP2AndP1.addAll(instanceUUIDsP1);

    final Set<ProcessDefinitionUUID> exceptP1 = Collections.singleton(processUUID1);
    final Set<ProcessDefinitionUUID> exceptP2 = Collections.singleton(processUUID2);

    // execute human tasks
    for (int i = 0; i < numberOfProcess + numberOfProcess; i++) {
      executeTask(instanceUUIDsP2AndP1.get(i), "step1");
      Thread.sleep(3);
    }

    // ended date desc except p1
    Collections.reverse(instanceUUIDsP2);
    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithInvolvedUserExcept("john", 0, 5, exceptP1,
            ProcessInstanceCriterion.ENDED_DATE_DESC);
    checkListContent(0, 5, 5, instanceUUIDsP2, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUserExcept("john", 5, 10,
        exceptP1, ProcessInstanceCriterion.ENDED_DATE_DESC);
    checkListContent(5, 10, 5, instanceUUIDsP2, retrievedInstances);

    // ended date desc except p2
    Collections.reverse(instanceUUIDsP1);
    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUserExcept("john", 0, 5,
        exceptP2, ProcessInstanceCriterion.ENDED_DATE_DESC);
    checkListContent(0, 5, 5, instanceUUIDsP1, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUserExcept("john", 5, 5,
        exceptP2, ProcessInstanceCriterion.ENDED_DATE_DESC);
    checkListContent(5, 5, 5, instanceUUIDsP1, retrievedInstances);

    // ended date desc except p1 and p2
    Collections.reverse(instanceUUIDsP2AndP1);
    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUserExcept("john", 0, 8, null,
        ProcessInstanceCriterion.ENDED_DATE_DESC);
    checkListContent(0, 8, 8, instanceUUIDsP2AndP1, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUserExcept("john", 8, 20, null,
        ProcessInstanceCriterion.ENDED_DATE_DESC);
    checkListContent(8, 20, 12, instanceUUIDsP2AndP1, retrievedInstances);

    getManagementAPI().deleteProcess(processUUID1);
    getManagementAPI().deleteProcess(processUUID2);
  }

  public void testGetLightParentProcessInstancesWithInvolvedUserExceptPagingOrderByInstanceNumberDesc()
      throws Exception {
    ProcessDefinition process1 = getProcess();
    ProcessDefinition process2 = getProcess2();

    process1 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process1));
    process2 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process2));

    final ProcessDefinitionUUID processUUID1 = process1.getUUID();
    final ProcessDefinitionUUID processUUID2 = process2.getUUID();

    final int numberOfProcess = 10;
    final List<ProcessInstanceUUID> instanceUUIDsP1 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID1, numberOfProcess, instanceUUIDsP1);

    final List<ProcessInstanceUUID> instanceUUIDsP2 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID2, numberOfProcess, instanceUUIDsP2);

    final Set<ProcessDefinitionUUID> exceptP1 = new HashSet<ProcessDefinitionUUID>();
    exceptP1.add(processUUID1);

    final Set<ProcessDefinitionUUID> exceptP2 = new HashSet<ProcessDefinitionUUID>();
    exceptP2.add(processUUID2);

    // instance number desc except p1
    Collections.reverse(instanceUUIDsP2);
    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithInvolvedUserExcept("john", 0, 5, exceptP1,
            ProcessInstanceCriterion.INSTANCE_NUMBER_DESC);
    checkListContent(0, 5, 5, instanceUUIDsP2, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUserExcept("john", 5, 10,
        exceptP1, ProcessInstanceCriterion.INSTANCE_NUMBER_DESC);
    checkListContent(5, 10, 5, instanceUUIDsP2, retrievedInstances);

    // instance number desc except p2
    Collections.reverse(instanceUUIDsP1);
    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUserExcept("john", 0, 5,
        exceptP2, ProcessInstanceCriterion.INSTANCE_NUMBER_DESC);
    checkListContent(0, 5, 5, instanceUUIDsP1, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUserExcept("john", 5, 5,
        exceptP2, ProcessInstanceCriterion.INSTANCE_NUMBER_DESC);
    checkListContent(5, 5, 5, instanceUUIDsP1, retrievedInstances);

    getManagementAPI().deleteProcess(processUUID1);
    getManagementAPI().deleteProcess(processUUID2);
  }

  public void testGetLightParentProcessInstancesWithInvolvedUserExceptPagingOrderByProcessUUIDDesc() throws Exception {
    ProcessDefinition process1 = getProcess();
    ProcessDefinition process2 = getProcess2();

    process1 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process1));
    process2 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process2));

    final ProcessDefinitionUUID processUUID1 = process1.getUUID();
    final ProcessDefinitionUUID processUUID2 = process2.getUUID();

    final int numberOfProcess = 10;
    final List<ProcessInstanceUUID> instanceUUIDsP1 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID1, numberOfProcess, instanceUUIDsP1);

    final List<ProcessInstanceUUID> instanceUUIDsP2 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID2, numberOfProcess, instanceUUIDsP2);

    final Set<ProcessDefinitionUUID> exceptP1 = new HashSet<ProcessDefinitionUUID>();
    exceptP1.add(processUUID1);

    final Set<ProcessDefinitionUUID> exceptP2 = new HashSet<ProcessDefinitionUUID>();
    exceptP2.add(processUUID2);

    // instance UUID desc except p1
    Collections.sort(instanceUUIDsP2, new ProcessInstanceUUIDComparator());
    Collections.reverse(instanceUUIDsP2);
    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithInvolvedUserExcept("john", 0, 5, exceptP1,
            ProcessInstanceCriterion.INSTANCE_UUID_DESC);
    checkListContent(0, 5, 5, instanceUUIDsP2, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUserExcept("john", 5, 10,
        exceptP1, ProcessInstanceCriterion.INSTANCE_UUID_DESC);
    checkListContent(5, 10, 5, instanceUUIDsP2, retrievedInstances);

    // instance UUID desc except p2
    Collections.sort(instanceUUIDsP1, new ProcessInstanceUUIDComparator());
    Collections.reverse(instanceUUIDsP1);
    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUserExcept("john", 0, 5,
        exceptP2, ProcessInstanceCriterion.INSTANCE_UUID_DESC);
    checkListContent(0, 5, 5, instanceUUIDsP1, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUserExcept("john", 5, 5,
        exceptP2, ProcessInstanceCriterion.INSTANCE_UUID_DESC);
    checkListContent(5, 5, 5, instanceUUIDsP1, retrievedInstances);

    getManagementAPI().deleteProcess(processUUID1);
    getManagementAPI().deleteProcess(processUUID2);
  }

  public void testGetLightParentProcessInstancesWithInvolvedUserExceptPagingOrderByProcessDefault() throws Exception {
    ProcessDefinition process1 = getProcess();
    ProcessDefinition process2 = getProcess2();

    process1 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process1));
    process2 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process2));

    final ProcessDefinitionUUID processUUID1 = process1.getUUID();
    final ProcessDefinitionUUID processUUID2 = process2.getUUID();

    final int numberOfProcess = 10;
    final List<ProcessInstanceUUID> instanceUUIDsP1 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID1, numberOfProcess, instanceUUIDsP1);

    updateInstanceVariable(instanceUUIDsP1, "text", "Hello", false);

    final List<ProcessInstanceUUID> instanceUUIDsP2 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID2, numberOfProcess, instanceUUIDsP2);

    updateInstanceVariable(instanceUUIDsP2, "text", "Hello", false);

    final List<ProcessInstanceUUID> instanceUUIDsP1AndP2 = new ArrayList<ProcessInstanceUUID>();
    instanceUUIDsP1AndP2.addAll(instanceUUIDsP1);
    instanceUUIDsP1AndP2.addAll(instanceUUIDsP2);

    final List<ProcessInstanceUUID> instanceUUIDsP2AndP1 = new ArrayList<ProcessInstanceUUID>();
    instanceUUIDsP2AndP1.addAll(instanceUUIDsP2);
    instanceUUIDsP2AndP1.addAll(instanceUUIDsP1);

    final Set<ProcessDefinitionUUID> exceptP1 = new HashSet<ProcessDefinitionUUID>();
    exceptP1.add(processUUID1);

    final Set<ProcessDefinitionUUID> exceptP2 = new HashSet<ProcessDefinitionUUID>();
    exceptP2.add(processUUID2);

    final Set<ProcessDefinitionUUID> excpetP1AndP2 = new HashSet<ProcessDefinitionUUID>();
    excpetP1AndP2.add(processUUID1);
    excpetP1AndP2.add(processUUID2);

    // default (last update desc)
    // default except p1
    Collections.reverse(instanceUUIDsP2);
    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithInvolvedUserExcept("john", 0, 5, exceptP1, ProcessInstanceCriterion.DEFAULT);
    checkListContent(0, 5, 5, instanceUUIDsP2, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUserExcept("john", 5, 10,
        exceptP1, ProcessInstanceCriterion.DEFAULT);
    checkListContent(5, 10, 5, instanceUUIDsP2, retrievedInstances);

    // default except p2
    Collections.reverse(instanceUUIDsP1);
    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUserExcept("john", 0, 5,
        exceptP2, ProcessInstanceCriterion.DEFAULT);
    checkListContent(0, 5, 5, instanceUUIDsP1, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUserExcept("john", 5, 5,
        exceptP2, ProcessInstanceCriterion.DEFAULT);
    checkListContent(5, 5, 5, instanceUUIDsP1, retrievedInstances);

    // default except null
    // last update asc except null
    final List<ProcessInstanceUUID> expectP1 = getListCopy(instanceUUIDsP1);
    Collections.reverse(expectP1);
    final List<ProcessInstanceUUID> expectP2 = getListCopy(instanceUUIDsP2);
    Collections.reverse(expectP2);

    final List<ProcessInstanceUUID> expectedOrder = new ArrayList<ProcessInstanceUUID>();
    expectedOrder.addAll(expectP1);
    expectedOrder.addAll(expectP2);

    Collections.reverse(expectedOrder);
    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUserExcept("john", 0, 8, null,
        ProcessInstanceCriterion.DEFAULT);
    checkListContent(0, 8, 8, expectedOrder, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUserExcept("john", 8, 20, null,
        ProcessInstanceCriterion.DEFAULT);
    checkListContent(8, 20, 12, expectedOrder, retrievedInstances);

    // expect p1 and p2
    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithInvolvedUserExcept("john", 0, 8,
        excpetP1AndP2, ProcessInstanceCriterion.DEFAULT);
    assertEquals(0, retrievedInstances.size());

    getManagementAPI().deleteProcess(processUUID1);
    getManagementAPI().deleteProcess(processUUID2);
  }

  public void testGetLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDateFromProcessUUIDsPaging()
      throws Exception {
    ProcessDefinition process1 = getProcess();
    ProcessDefinition process2 = getProcess2();

    process1 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process1));
    process2 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process2));

    final ProcessDefinitionUUID processUUID1 = process1.getUUID();
    final ProcessDefinitionUUID processUUID2 = process2.getUUID();

    final int numberOfProcess1 = 10;
    final List<ProcessInstanceUUID> instanceUUIDsP1 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID1, numberOfProcess1, instanceUUIDsP1);
    updateInstanceVariable(instanceUUIDsP1, "text", "Hello", true);

    final int numberOfProcess2 = 5;
    final List<ProcessInstanceUUID> instanceUUIDsP2 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID2, numberOfProcess2, instanceUUIDsP2);
    updateInstanceVariable(instanceUUIDsP2, "text", "Hello", true);

    final List<ProcessInstanceUUID> instanceUUIDsP2AndP1 = new ArrayList<ProcessInstanceUUID>();
    instanceUUIDsP2AndP1.addAll(instanceUUIDsP2);
    instanceUUIDsP2AndP1.addAll(instanceUUIDsP1);

    final Set<ProcessDefinitionUUID> fromP1 = new HashSet<ProcessDefinitionUUID>();
    fromP1.add(processUUID1);

    final Set<ProcessDefinitionUUID> fromP2 = new HashSet<ProcessDefinitionUUID>();
    fromP2.add(processUUID2);

    final Set<ProcessDefinitionUUID> fromP1AndP2 = new HashSet<ProcessDefinitionUUID>();
    fromP1AndP2.add(processUUID1);
    fromP1AndP2.add(processUUID2);

    // **** last update
    // last update asc from p1
    List<ProcessInstanceUUID> expectedOrder = getListCopy(instanceUUIDsP1);
    Collections.reverse(expectedOrder);
    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate("john", 1, 0, 5, fromP1,
            ProcessInstanceCriterion.LAST_UPDATE_ASC);
    checkListContent(0, 5, 5, expectedOrder, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate("john", 1, 5, 10, fromP1,
            ProcessInstanceCriterion.LAST_UPDATE_ASC);
    checkListContent(5, 10, 5, expectedOrder, retrievedInstances);

    // last update desc from p1
    Collections.reverse(expectedOrder);
    retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate("john", 1, 0, 5, fromP1,
            ProcessInstanceCriterion.LAST_UPDATE_DESC);
    checkListContent(0, 5, 5, expectedOrder, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate("john", 1, 5, 10, fromP1,
            ProcessInstanceCriterion.LAST_UPDATE_DESC);
    checkListContent(5, 10, 5, expectedOrder, retrievedInstances);

    // last update asc from p2
    expectedOrder = getListCopy(instanceUUIDsP2);
    Collections.reverse(expectedOrder);
    retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate("john", 2, 0, 3, fromP2,
            ProcessInstanceCriterion.LAST_UPDATE_ASC);
    checkListContent(0, 3, 3, expectedOrder, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate("john", 2, 3, 5, fromP2,
            ProcessInstanceCriterion.LAST_UPDATE_ASC);
    checkListContent(3, 5, 2, expectedOrder, retrievedInstances);

    // last update desc from p2
    Collections.reverse(expectedOrder);
    retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate("john", 2, 0, 3, fromP2,
            ProcessInstanceCriterion.LAST_UPDATE_DESC);
    checkListContent(0, 3, 3, expectedOrder, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate("john", 2, 3, 5, fromP2,
            ProcessInstanceCriterion.LAST_UPDATE_DESC);
    checkListContent(3, 5, 2, expectedOrder, retrievedInstances);

    // started date
    // started date asc from p1
    expectedOrder = getListCopy(instanceUUIDsP1);
    retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate("john", 1, 0, 5, fromP1,
            ProcessInstanceCriterion.STARTED_DATE_ASC);
    checkListContent(0, 5, 5, expectedOrder, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate("john", 1, 5, 10, fromP1,
            ProcessInstanceCriterion.STARTED_DATE_ASC);
    checkListContent(5, 10, 5, expectedOrder, retrievedInstances);

    // started date desc from p1
    Collections.reverse(expectedOrder);
    retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate("john", 1, 0, 5, fromP1,
            ProcessInstanceCriterion.STARTED_DATE_DESC);
    checkListContent(0, 5, 5, expectedOrder, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate("john", 1, 5, 10, fromP1,
            ProcessInstanceCriterion.STARTED_DATE_DESC);
    checkListContent(5, 10, 5, expectedOrder, retrievedInstances);

    // started date asc from p2
    expectedOrder = getListCopy(instanceUUIDsP2);
    retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate("john", 2, 0, 3, fromP2,
            ProcessInstanceCriterion.STARTED_DATE_ASC);
    checkListContent(0, 3, 3, expectedOrder, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate("john", 2, 3, 5, fromP2,
            ProcessInstanceCriterion.STARTED_DATE_ASC);
    checkListContent(3, 5, 2, expectedOrder, retrievedInstances);

    // started date desc from p2
    Collections.reverse(expectedOrder);
    retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate("john", 2, 0, 3, fromP2,
            ProcessInstanceCriterion.STARTED_DATE_DESC);
    checkListContent(0, 3, 3, expectedOrder, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate("john", 2, 3, 5, fromP2,
            ProcessInstanceCriterion.STARTED_DATE_DESC);
    checkListContent(3, 5, 2, expectedOrder, retrievedInstances);

    // ended date: the ended date is empty
    // ended date asc from p1
    expectedOrder = getListCopy(instanceUUIDsP1);
    retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate("john", 1, 0, 5, fromP1,
            ProcessInstanceCriterion.ENDED_DATE_ASC);
    assertEquals(5, retrievedInstances.size());

    retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate("john", 1, 5, 10, fromP1,
            ProcessInstanceCriterion.ENDED_DATE_ASC);
    assertEquals(5, retrievedInstances.size());

    // ended date desc from p1
    Collections.reverse(expectedOrder);
    retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate("john", 1, 0, 5, fromP1,
            ProcessInstanceCriterion.ENDED_DATE_DESC);
    assertEquals(5, retrievedInstances.size());

    retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate("john", 1, 5, 10, fromP1,
            ProcessInstanceCriterion.ENDED_DATE_DESC);
    assertEquals(5, retrievedInstances.size());

    // ended date asc from p2
    expectedOrder = getListCopy(instanceUUIDsP2);
    retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate("john", 2, 0, 3, fromP2,
            ProcessInstanceCriterion.ENDED_DATE_ASC);
    assertEquals(3, retrievedInstances.size());

    retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate("john", 2, 3, 5, fromP2,
            ProcessInstanceCriterion.ENDED_DATE_ASC);
    assertEquals(2, retrievedInstances.size());

    // ended date desc from p2
    Collections.reverse(expectedOrder);
    retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate("john", 2, 0, 3, fromP2,
            ProcessInstanceCriterion.ENDED_DATE_DESC);
    assertEquals(3, retrievedInstances.size());

    retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate("john", 2, 3, 5, fromP2,
            ProcessInstanceCriterion.ENDED_DATE_DESC);
    assertEquals(2, retrievedInstances.size());

    // instance number
    // instance number asc from p1
    expectedOrder = getListCopy(instanceUUIDsP1);
    retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate("john", 1, 0, 5, fromP1,
            ProcessInstanceCriterion.INSTANCE_NUMBER_ASC);
    checkListContent(0, 5, 5, expectedOrder, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate("john", 1, 5, 10, fromP1,
            ProcessInstanceCriterion.INSTANCE_NUMBER_ASC);
    checkListContent(5, 10, 5, expectedOrder, retrievedInstances);

    // instance number desc from p1
    Collections.reverse(expectedOrder);
    retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate("john", 1, 0, 5, fromP1,
            ProcessInstanceCriterion.INSTANCE_NUMBER_DESC);
    checkListContent(0, 5, 5, expectedOrder, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate("john", 1, 5, 10, fromP1,
            ProcessInstanceCriterion.INSTANCE_NUMBER_DESC);
    checkListContent(5, 10, 5, expectedOrder, retrievedInstances);

    // instance number asc from p2
    expectedOrder = getListCopy(instanceUUIDsP2);
    retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate("john", 2, 0, 3, fromP2,
            ProcessInstanceCriterion.INSTANCE_NUMBER_ASC);
    checkListContent(0, 3, 3, expectedOrder, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate("john", 2, 3, 5, fromP2,
            ProcessInstanceCriterion.INSTANCE_NUMBER_ASC);
    checkListContent(3, 5, 2, expectedOrder, retrievedInstances);

    // instance number desc from p2
    Collections.reverse(expectedOrder);
    retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate("john", 2, 0, 3, fromP2,
            ProcessInstanceCriterion.INSTANCE_NUMBER_DESC);
    checkListContent(0, 3, 3, expectedOrder, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate("john", 2, 3, 5, fromP2,
            ProcessInstanceCriterion.INSTANCE_NUMBER_DESC);
    checkListContent(3, 5, 2, expectedOrder, retrievedInstances);

    // instance UUID
    // instance UUID asc from p1
    expectedOrder = getListCopy(instanceUUIDsP1);
    Collections.sort(expectedOrder, new ProcessInstanceUUIDComparator());
    retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate("john", 1, 0, 5, fromP1,
            ProcessInstanceCriterion.INSTANCE_UUID_ASC);
    checkListContent(0, 5, 5, expectedOrder, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate("john", 1, 5, 10, fromP1,
            ProcessInstanceCriterion.INSTANCE_UUID_ASC);
    checkListContent(5, 10, 5, expectedOrder, retrievedInstances);

    // instance UUID desc from p1
    Collections.reverse(expectedOrder);
    retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate("john", 1, 0, 5, fromP1,
            ProcessInstanceCriterion.INSTANCE_UUID_DESC);
    checkListContent(0, 5, 5, expectedOrder, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate("john", 1, 5, 10, fromP1,
            ProcessInstanceCriterion.INSTANCE_UUID_DESC);
    checkListContent(5, 10, 5, expectedOrder, retrievedInstances);

    // instance UUID asc from p2
    expectedOrder = getListCopy(instanceUUIDsP2);
    Collections.sort(expectedOrder, new ProcessInstanceUUIDComparator());
    retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate("john", 2, 0, 3, fromP2,
            ProcessInstanceCriterion.INSTANCE_UUID_ASC);
    checkListContent(0, 3, 3, expectedOrder, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate("john", 2, 3, 5, fromP2,
            ProcessInstanceCriterion.INSTANCE_UUID_ASC);
    checkListContent(3, 5, 2, expectedOrder, retrievedInstances);

    // instance UUID desc from p2
    Collections.reverse(expectedOrder);
    retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate("john", 2, 0, 3, fromP2,
            ProcessInstanceCriterion.INSTANCE_UUID_DESC);
    checkListContent(0, 3, 3, expectedOrder, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate("john", 2, 3, 5, fromP2,
            ProcessInstanceCriterion.INSTANCE_UUID_DESC);
    checkListContent(3, 5, 2, expectedOrder, retrievedInstances);

    // default (last update desc)
    // default from p1
    expectedOrder = getListCopy(instanceUUIDsP1);
    retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate("john", 3, 0, 5, fromP1,
            ProcessInstanceCriterion.DEFAULT);
    checkListContent(0, 5, 5, expectedOrder, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate("john", 3, 5, 10, fromP1,
            ProcessInstanceCriterion.DEFAULT);
    checkListContent(5, 10, 5, expectedOrder, retrievedInstances);

    // default from p2
    expectedOrder = getListCopy(instanceUUIDsP2);
    retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate("john", 3, 0, 3, fromP2,
            ProcessInstanceCriterion.DEFAULT);
    checkListContent(0, 3, 3, expectedOrder, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate("john", 3, 3, 5, fromP2,
            ProcessInstanceCriterion.DEFAULT);
    checkListContent(3, 5, 2, expectedOrder, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate("john", 1, 3, 5, fromP2,
            ProcessInstanceCriterion.DEFAULT);
    assertEquals(0, retrievedInstances.size());

    // empty list
    retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate("john", 3, 0, 8,
            new HashSet<ProcessDefinitionUUID>(), ProcessInstanceCriterion.DEFAULT);
    assertEquals(0, retrievedInstances.size());

    // execute human tasks
    for (int i = 0; i < numberOfProcess2 + numberOfProcess1; i++) {
      executeTask(instanceUUIDsP2AndP1.get(i), "step1");
      Thread.sleep(3);
    }

    retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate("john", 3, 0, 3, fromP2,
            ProcessInstanceCriterion.DEFAULT);
    assertEquals(0, retrievedInstances.size());

    getRuntimeAPI().deleteAllProcessInstances(processUUID1);
    getRuntimeAPI().deleteAllProcessInstances(processUUID2);
    getManagementAPI().disable(processUUID1);
    getManagementAPI().disable(processUUID2);
    getManagementAPI().deleteProcess(processUUID1);
    getManagementAPI().deleteProcess(processUUID2);
  }

  private void updateInstanceVariable(final List<ProcessInstanceUUID> instanceUUIDs, final String variableId,
      final Object variableValue, final boolean reverseUpdateOrder) throws InstanceNotFoundException,
      VariableNotFoundException, InterruptedException {
    if (reverseUpdateOrder) {
      for (int i = instanceUUIDs.size() - 1; i >= 0; i--) {
        getRuntimeAPI().setProcessInstanceVariable(instanceUUIDs.get(i), variableId, variableValue);
        Thread.sleep(3);
      }
    } else {
      for (int i = 0; i < instanceUUIDs.size(); i++) {
        getRuntimeAPI().setProcessInstanceVariable(instanceUUIDs.get(i), variableId, variableValue);
        Thread.sleep(3);
      }
    }
  }

  private void instanciateProcesses(final ProcessDefinitionUUID processUUID, final int numberOfInstances,
      final List<ProcessInstanceUUID> instanceUUIDs) throws ProcessNotFoundException, InterruptedException {
    for (int i = 0; i < numberOfInstances; i++) {
      instanceUUIDs.add(getRuntimeAPI().instantiateProcess(processUUID));
      Thread.sleep(3);
    }
  }

  public void testGetLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDateOrderByLastUpdateAsc()
      throws Exception {
    ProcessDefinition process1 = getProcess();
    process1 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process1));
    final ProcessDefinitionUUID processUUID1 = process1.getUUID();

    final int numberOfProcess1 = 10;
    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID1, numberOfProcess1, instanceUUIDs);
    updateInstanceVariable(instanceUUIDs, "text", "Hello", true);

    Collections.reverse(instanceUUIDs);
    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate("john", 1, 0, 5,
            ProcessInstanceCriterion.LAST_UPDATE_ASC);
    checkListContent(0, 5, 5, instanceUUIDs, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate("john", 1, 5, 10,
            ProcessInstanceCriterion.LAST_UPDATE_ASC);
    checkListContent(5, 10, 5, instanceUUIDs, retrievedInstances);

    getRuntimeAPI().deleteAllProcessInstances(processUUID1);
    getManagementAPI().disable(processUUID1);
    getManagementAPI().deleteProcess(processUUID1);
  }

  public void testGetLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDateOrderByLastUpdateDesc()
      throws Exception {
    ProcessDefinition process = getProcess();
    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final int numberOfProcess1 = 10;
    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID, numberOfProcess1, instanceUUIDs);
    updateInstanceVariable(instanceUUIDs, "text", "Hello", true);

    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate("john", 1, 0, 5,
            ProcessInstanceCriterion.LAST_UPDATE_DESC);
    checkListContent(0, 5, 5, instanceUUIDs, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate("john", 1, 5, 10,
            ProcessInstanceCriterion.LAST_UPDATE_DESC);
    checkListContent(5, 10, 5, instanceUUIDs, retrievedInstances);

    getRuntimeAPI().deleteAllProcessInstances(processUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDateOrderByStartedDateAsc()
      throws Exception {
    ProcessDefinition process = getProcess();
    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final int numberOfProcess1 = 10;
    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID, numberOfProcess1, instanceUUIDs);
    updateInstanceVariable(instanceUUIDs, "text", "Hello", true);

    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate("john", 1, 0, 5,
            ProcessInstanceCriterion.STARTED_DATE_ASC);
    checkListContent(0, 5, 5, instanceUUIDs, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate("john", 1, 5, 10,
            ProcessInstanceCriterion.STARTED_DATE_ASC);
    checkListContent(5, 10, 5, instanceUUIDs, retrievedInstances);

    getRuntimeAPI().deleteAllProcessInstances(processUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDateOrderByStartedDateDesc()
      throws Exception {
    ProcessDefinition process = getProcess();
    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final int numberOfProcess1 = 10;
    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID, numberOfProcess1, instanceUUIDs);
    updateInstanceVariable(instanceUUIDs, "text", "Hello", true);

    Collections.reverse(instanceUUIDs);

    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate("john", 1, 0, 5,
            ProcessInstanceCriterion.STARTED_DATE_DESC);
    checkListContent(0, 5, 5, instanceUUIDs, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate("john", 1, 5, 10,
            ProcessInstanceCriterion.STARTED_DATE_DESC);
    checkListContent(5, 10, 5, instanceUUIDs, retrievedInstances);

    getRuntimeAPI().deleteAllProcessInstances(processUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDateOrderByEndedDateAsc()
      throws Exception {
    ProcessDefinition process = getProcess();
    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final int numberOfProcess1 = 10;
    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID, numberOfProcess1, instanceUUIDs);
    updateInstanceVariable(instanceUUIDs, "text", "Hello", true);

    // the end date is empty
    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate("john", 1, 0, 5,
            ProcessInstanceCriterion.ENDED_DATE_ASC);
    assertEquals(5, retrievedInstances.size());

    retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate("john", 1, 5, 10,
            ProcessInstanceCriterion.ENDED_DATE_ASC);
    assertEquals(5, retrievedInstances.size());

    getRuntimeAPI().deleteAllProcessInstances(processUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDateOrderByEndedDateDesc()
      throws Exception {
    ProcessDefinition process = getProcess();
    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final int numberOfProcess1 = 10;
    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID, numberOfProcess1, instanceUUIDs);
    updateInstanceVariable(instanceUUIDs, "text", "Hello", true);

    // the end date is empty
    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate("john", 1, 0, 5,
            ProcessInstanceCriterion.ENDED_DATE_DESC);
    assertEquals(5, retrievedInstances.size());

    retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate("john", 1, 5, 10,
            ProcessInstanceCriterion.ENDED_DATE_DESC);
    assertEquals(5, retrievedInstances.size());

    getRuntimeAPI().deleteAllProcessInstances(processUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDateOrderByInstanceNumberAsc()
      throws Exception {
    ProcessDefinition process = getProcess();
    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final int numberOfProcess1 = 10;
    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID, numberOfProcess1, instanceUUIDs);
    updateInstanceVariable(instanceUUIDs, "text", "Hello", true);

    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate("john", 1, 0, 5,
            ProcessInstanceCriterion.INSTANCE_NUMBER_ASC);
    checkListContent(0, 5, 5, instanceUUIDs, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate("john", 1, 5, 10,
            ProcessInstanceCriterion.INSTANCE_NUMBER_ASC);
    checkListContent(5, 10, 5, instanceUUIDs, retrievedInstances);

    getRuntimeAPI().deleteAllProcessInstances(processUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDateOrderByInstanceNumberDesc()
      throws Exception {
    ProcessDefinition process = getProcess();
    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final int numberOfProcess1 = 10;
    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID, numberOfProcess1, instanceUUIDs);
    updateInstanceVariable(instanceUUIDs, "text", "Hello", true);

    Collections.reverse(instanceUUIDs);
    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate("john", 1, 0, 5,
            ProcessInstanceCriterion.INSTANCE_NUMBER_DESC);
    checkListContent(0, 5, 5, instanceUUIDs, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate("john", 1, 5, 10,
            ProcessInstanceCriterion.INSTANCE_NUMBER_DESC);
    checkListContent(5, 10, 5, instanceUUIDs, retrievedInstances);

    getRuntimeAPI().deleteAllProcessInstances(processUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDateOrderByInstanceUUIDAsc()
      throws Exception {
    ProcessDefinition process = getProcess();
    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final int numberOfProcess1 = 10;
    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID, numberOfProcess1, instanceUUIDs);
    updateInstanceVariable(instanceUUIDs, "text", "Hello", true);

    Collections.sort(instanceUUIDs, new ProcessInstanceUUIDComparator());
    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate("john", 1, 0, 5,
            ProcessInstanceCriterion.INSTANCE_UUID_ASC);
    checkListContent(0, 5, 5, instanceUUIDs, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate("john", 1, 5, 10,
            ProcessInstanceCriterion.INSTANCE_UUID_ASC);
    checkListContent(5, 10, 5, instanceUUIDs, retrievedInstances);

    getRuntimeAPI().deleteAllProcessInstances(processUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDateOrderByInstanceUUIDDesc()
      throws Exception {
    ProcessDefinition process = getProcess();
    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final int numberOfProcess1 = 10;
    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID, numberOfProcess1, instanceUUIDs);
    updateInstanceVariable(instanceUUIDs, "text", "Hello", true);

    Collections.sort(instanceUUIDs, new ProcessInstanceUUIDComparator());
    Collections.reverse(instanceUUIDs);
    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate("john", 1, 0, 5,
            ProcessInstanceCriterion.INSTANCE_UUID_DESC);
    checkListContent(0, 5, 5, instanceUUIDs, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDate("john", 1, 5, 10,
            ProcessInstanceCriterion.INSTANCE_UUID_DESC);
    checkListContent(5, 10, 5, instanceUUIDs, retrievedInstances);

    getRuntimeAPI().deleteAllProcessInstances(processUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDateExceptOrderByLastUpdateAsc()
      throws Exception {
    ProcessDefinition process1 = getProcess();
    ProcessDefinition process2 = getProcess2();

    process1 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process1));
    process2 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process2));

    final ProcessDefinitionUUID processUUID1 = process1.getUUID();
    final ProcessDefinitionUUID processUUID2 = process2.getUUID();

    final int numberOfProcess1 = 10;
    final List<ProcessInstanceUUID> instanceUUIDsP1 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID1, numberOfProcess1, instanceUUIDsP1);
    updateInstanceVariable(instanceUUIDsP1, "text", "Hello", true);

    final int numberOfProcess2 = 5;
    final List<ProcessInstanceUUID> instanceUUIDsP2 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID2, numberOfProcess2, instanceUUIDsP2);
    updateInstanceVariable(instanceUUIDsP2, "text", "Hello", true);

    final Set<ProcessDefinitionUUID> exceptP1 = new HashSet<ProcessDefinitionUUID>();
    exceptP1.add(processUUID1);

    Collections.reverse(instanceUUIDsP2);
    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDateExcept("john", 2, 0, 3,
            exceptP1, ProcessInstanceCriterion.LAST_UPDATE_ASC);
    checkListContent(0, 3, 3, instanceUUIDsP2, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDateExcept("john", 2, 3, 5,
            exceptP1, ProcessInstanceCriterion.LAST_UPDATE_ASC);
    checkListContent(3, 5, 2, instanceUUIDsP2, retrievedInstances);

    getRuntimeAPI().deleteAllProcessInstances(processUUID1);
    getRuntimeAPI().deleteAllProcessInstances(processUUID2);
    getManagementAPI().disable(processUUID1);
    getManagementAPI().disable(processUUID2);
    getManagementAPI().deleteProcess(processUUID1);
    getManagementAPI().deleteProcess(processUUID2);
  }

  public void testGetLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDateExceptOrderByLastUpdateDesc()
      throws Exception {
    ProcessDefinition process1 = getProcess();
    ProcessDefinition process2 = getProcess2();

    process1 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process1));
    process2 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process2));

    final ProcessDefinitionUUID processUUID1 = process1.getUUID();
    final ProcessDefinitionUUID processUUID2 = process2.getUUID();

    final int numberOfProcess1 = 10;
    final List<ProcessInstanceUUID> instanceUUIDsP1 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID1, numberOfProcess1, instanceUUIDsP1);
    updateInstanceVariable(instanceUUIDsP1, "text", "Hello", true);

    final int numberOfProcess2 = 5;
    final List<ProcessInstanceUUID> instanceUUIDsP2 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID2, numberOfProcess2, instanceUUIDsP2);
    updateInstanceVariable(instanceUUIDsP2, "text", "Hello", true);

    final Set<ProcessDefinitionUUID> exceptP1 = new HashSet<ProcessDefinitionUUID>();
    exceptP1.add(processUUID1);

    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDateExcept("john", 2, 0, 3,
            exceptP1, ProcessInstanceCriterion.LAST_UPDATE_DESC);
    checkListContent(0, 3, 3, instanceUUIDsP2, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDateExcept("john", 2, 3, 5,
            exceptP1, ProcessInstanceCriterion.LAST_UPDATE_DESC);
    checkListContent(3, 5, 2, instanceUUIDsP2, retrievedInstances);

    getRuntimeAPI().deleteAllProcessInstances(processUUID1);
    getRuntimeAPI().deleteAllProcessInstances(processUUID2);
    getManagementAPI().disable(processUUID1);
    getManagementAPI().disable(processUUID2);
    getManagementAPI().deleteProcess(processUUID1);
    getManagementAPI().deleteProcess(processUUID2);
  }

  public void testGetLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDateExceptOrderByStartedDateAsc()
      throws Exception {
    ProcessDefinition process1 = getProcess();
    ProcessDefinition process2 = getProcess2();

    process1 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process1));
    process2 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process2));

    final ProcessDefinitionUUID processUUID1 = process1.getUUID();
    final ProcessDefinitionUUID processUUID2 = process2.getUUID();

    final int numberOfProcess1 = 10;
    final List<ProcessInstanceUUID> instanceUUIDsP1 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID1, numberOfProcess1, instanceUUIDsP1);
    updateInstanceVariable(instanceUUIDsP1, "text", "Hello", true);

    final int numberOfProcess2 = 5;
    final List<ProcessInstanceUUID> instanceUUIDsP2 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID2, numberOfProcess2, instanceUUIDsP2);
    updateInstanceVariable(instanceUUIDsP2, "text", "Hello", true);

    final Set<ProcessDefinitionUUID> exceptP1 = new HashSet<ProcessDefinitionUUID>();
    exceptP1.add(processUUID1);

    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDateExcept("john", 2, 0, 3,
            exceptP1, ProcessInstanceCriterion.STARTED_DATE_ASC);
    checkListContent(0, 3, 3, instanceUUIDsP2, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDateExcept("john", 2, 3, 5,
            exceptP1, ProcessInstanceCriterion.STARTED_DATE_ASC);
    checkListContent(3, 5, 2, instanceUUIDsP2, retrievedInstances);

    getRuntimeAPI().deleteAllProcessInstances(processUUID1);
    getRuntimeAPI().deleteAllProcessInstances(processUUID2);
    getManagementAPI().disable(processUUID1);
    getManagementAPI().disable(processUUID2);
    getManagementAPI().deleteProcess(processUUID1);
    getManagementAPI().deleteProcess(processUUID2);
  }

  public void testGetLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDateExceptOrderByStartedDateDesc()
      throws Exception {
    ProcessDefinition process1 = getProcess();
    ProcessDefinition process2 = getProcess2();

    process1 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process1));
    process2 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process2));

    final ProcessDefinitionUUID processUUID1 = process1.getUUID();
    final ProcessDefinitionUUID processUUID2 = process2.getUUID();

    final int numberOfProcess1 = 10;
    final List<ProcessInstanceUUID> instanceUUIDsP1 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID1, numberOfProcess1, instanceUUIDsP1);
    updateInstanceVariable(instanceUUIDsP1, "text", "Hello", true);

    final int numberOfProcess2 = 5;
    final List<ProcessInstanceUUID> instanceUUIDsP2 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID2, numberOfProcess2, instanceUUIDsP2);
    updateInstanceVariable(instanceUUIDsP2, "text", "Hello", true);

    final Set<ProcessDefinitionUUID> exceptP1 = new HashSet<ProcessDefinitionUUID>();
    exceptP1.add(processUUID1);

    Collections.reverse(instanceUUIDsP2);
    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDateExcept("john", 2, 0, 3,
            exceptP1, ProcessInstanceCriterion.STARTED_DATE_DESC);
    checkListContent(0, 3, 3, instanceUUIDsP2, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDateExcept("john", 2, 3, 5,
            exceptP1, ProcessInstanceCriterion.STARTED_DATE_DESC);
    checkListContent(3, 5, 2, instanceUUIDsP2, retrievedInstances);

    getRuntimeAPI().deleteAllProcessInstances(processUUID1);
    getRuntimeAPI().deleteAllProcessInstances(processUUID2);
    getManagementAPI().disable(processUUID1);
    getManagementAPI().disable(processUUID2);
    getManagementAPI().deleteProcess(processUUID1);
    getManagementAPI().deleteProcess(processUUID2);
  }

  public void testGetLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDateExceptOrderByEndedDateAsc()
      throws Exception {
    ProcessDefinition process1 = getProcess();
    ProcessDefinition process2 = getProcess2();

    process1 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process1));
    process2 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process2));

    final ProcessDefinitionUUID processUUID1 = process1.getUUID();
    final ProcessDefinitionUUID processUUID2 = process2.getUUID();

    final int numberOfProcess1 = 10;
    final List<ProcessInstanceUUID> instanceUUIDsP1 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID1, numberOfProcess1, instanceUUIDsP1);
    updateInstanceVariable(instanceUUIDsP1, "text", "Hello", true);

    final int numberOfProcess2 = 5;
    final List<ProcessInstanceUUID> instanceUUIDsP2 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID2, numberOfProcess2, instanceUUIDsP2);
    updateInstanceVariable(instanceUUIDsP2, "text", "Hello", true);

    final Set<ProcessDefinitionUUID> exceptP1 = new HashSet<ProcessDefinitionUUID>();
    exceptP1.add(processUUID1);

    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDateExcept("john", 2, 0, 3,
            exceptP1, ProcessInstanceCriterion.ENDED_DATE_ASC);
    assertEquals(3, retrievedInstances.size());

    retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDateExcept("john", 2, 3, 5,
            exceptP1, ProcessInstanceCriterion.ENDED_DATE_ASC);
    assertEquals(2, retrievedInstances.size());

    getRuntimeAPI().deleteAllProcessInstances(processUUID1);
    getRuntimeAPI().deleteAllProcessInstances(processUUID2);
    getManagementAPI().disable(processUUID1);
    getManagementAPI().disable(processUUID2);
    getManagementAPI().deleteProcess(processUUID1);
    getManagementAPI().deleteProcess(processUUID2);
  }

  public void testGetLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDateExceptOrderByEndedDateDesc()
      throws Exception {
    ProcessDefinition process1 = getProcess();
    ProcessDefinition process2 = getProcess2();

    process1 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process1));
    process2 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process2));

    final ProcessDefinitionUUID processUUID1 = process1.getUUID();
    final ProcessDefinitionUUID processUUID2 = process2.getUUID();

    final int numberOfProcess1 = 10;
    final List<ProcessInstanceUUID> instanceUUIDsP1 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID1, numberOfProcess1, instanceUUIDsP1);
    updateInstanceVariable(instanceUUIDsP1, "text", "Hello", true);

    final int numberOfProcess2 = 5;
    final List<ProcessInstanceUUID> instanceUUIDsP2 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID2, numberOfProcess2, instanceUUIDsP2);
    updateInstanceVariable(instanceUUIDsP2, "text", "Hello", true);

    final Set<ProcessDefinitionUUID> exceptP1 = new HashSet<ProcessDefinitionUUID>();
    exceptP1.add(processUUID1);

    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDateExcept("john", 2, 0, 3,
            exceptP1, ProcessInstanceCriterion.ENDED_DATE_DESC);
    assertEquals(3, retrievedInstances.size());

    retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDateExcept("john", 2, 3, 5,
            exceptP1, ProcessInstanceCriterion.ENDED_DATE_DESC);
    assertEquals(2, retrievedInstances.size());

    getRuntimeAPI().deleteAllProcessInstances(processUUID1);
    getRuntimeAPI().deleteAllProcessInstances(processUUID2);
    getManagementAPI().disable(processUUID1);
    getManagementAPI().disable(processUUID2);
    getManagementAPI().deleteProcess(processUUID1);
    getManagementAPI().deleteProcess(processUUID2);
  }

  public void testGetLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDateExceptOrderByNbAsc()
      throws Exception {
    ProcessDefinition process1 = getProcess();
    ProcessDefinition process2 = getProcess2();

    process1 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process1));
    process2 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process2));

    final ProcessDefinitionUUID processUUID1 = process1.getUUID();
    final ProcessDefinitionUUID processUUID2 = process2.getUUID();

    final int numberOfProcess1 = 10;
    final List<ProcessInstanceUUID> instanceUUIDsP1 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID1, numberOfProcess1, instanceUUIDsP1);
    updateInstanceVariable(instanceUUIDsP1, "text", "Hello", true);

    final int numberOfProcess2 = 5;
    final List<ProcessInstanceUUID> instanceUUIDsP2 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID2, numberOfProcess2, instanceUUIDsP2);
    updateInstanceVariable(instanceUUIDsP2, "text", "Hello", true);

    final Set<ProcessDefinitionUUID> exceptP1 = new HashSet<ProcessDefinitionUUID>();
    exceptP1.add(processUUID1);

    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDateExcept("john", 2, 0, 3,
            exceptP1, ProcessInstanceCriterion.INSTANCE_NUMBER_ASC);
    checkListContent(0, 3, 3, instanceUUIDsP2, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDateExcept("john", 2, 3, 5,
            exceptP1, ProcessInstanceCriterion.INSTANCE_NUMBER_ASC);
    checkListContent(3, 5, 2, instanceUUIDsP2, retrievedInstances);

    getRuntimeAPI().deleteAllProcessInstances(processUUID1);
    getRuntimeAPI().deleteAllProcessInstances(processUUID2);
    getManagementAPI().disable(processUUID1);
    getManagementAPI().disable(processUUID2);
    getManagementAPI().deleteProcess(processUUID1);
    getManagementAPI().deleteProcess(processUUID2);
  }

  public void testGetLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDateExceptOrderByNbDesc()
      throws Exception {
    ProcessDefinition process1 = getProcess();
    ProcessDefinition process2 = getProcess2();

    process1 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process1));
    process2 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process2));

    final ProcessDefinitionUUID processUUID1 = process1.getUUID();
    final ProcessDefinitionUUID processUUID2 = process2.getUUID();

    final int numberOfProcess1 = 10;
    final List<ProcessInstanceUUID> instanceUUIDsP1 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID1, numberOfProcess1, instanceUUIDsP1);
    updateInstanceVariable(instanceUUIDsP1, "text", "Hello", true);

    final int numberOfProcess2 = 5;
    final List<ProcessInstanceUUID> instanceUUIDsP2 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID2, numberOfProcess2, instanceUUIDsP2);
    updateInstanceVariable(instanceUUIDsP2, "text", "Hello", true);

    final Set<ProcessDefinitionUUID> exceptP1 = new HashSet<ProcessDefinitionUUID>();
    exceptP1.add(processUUID1);

    Collections.reverse(instanceUUIDsP2);
    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDateExcept("john", 2, 0, 3,
            exceptP1, ProcessInstanceCriterion.INSTANCE_NUMBER_DESC);
    checkListContent(0, 3, 3, instanceUUIDsP2, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDateExcept("john", 2, 3, 5,
            exceptP1, ProcessInstanceCriterion.INSTANCE_NUMBER_DESC);
    checkListContent(3, 5, 2, instanceUUIDsP2, retrievedInstances);

    getRuntimeAPI().deleteAllProcessInstances(processUUID1);
    getRuntimeAPI().deleteAllProcessInstances(processUUID2);
    getManagementAPI().disable(processUUID1);
    getManagementAPI().disable(processUUID2);
    getManagementAPI().deleteProcess(processUUID1);
    getManagementAPI().deleteProcess(processUUID2);
  }

  public void testGetLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDateExceptOrderByUUIDAsc()
      throws Exception {
    ProcessDefinition process1 = getProcess();
    ProcessDefinition process2 = getProcess2();

    process1 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process1));
    process2 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process2));

    final ProcessDefinitionUUID processUUID1 = process1.getUUID();
    final ProcessDefinitionUUID processUUID2 = process2.getUUID();

    final int numberOfProcess1 = 10;
    final List<ProcessInstanceUUID> instanceUUIDsP1 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID1, numberOfProcess1, instanceUUIDsP1);
    updateInstanceVariable(instanceUUIDsP1, "text", "Hello", true);

    final int numberOfProcess2 = 5;
    final List<ProcessInstanceUUID> instanceUUIDsP2 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID2, numberOfProcess2, instanceUUIDsP2);
    updateInstanceVariable(instanceUUIDsP2, "text", "Hello", true);

    final Set<ProcessDefinitionUUID> exceptP1 = new HashSet<ProcessDefinitionUUID>();
    exceptP1.add(processUUID1);

    Collections.sort(instanceUUIDsP2, new ProcessInstanceUUIDComparator());
    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDateExcept("john", 2, 0, 3,
            exceptP1, ProcessInstanceCriterion.INSTANCE_UUID_ASC);
    checkListContent(0, 3, 3, instanceUUIDsP2, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDateExcept("john", 2, 3, 5,
            exceptP1, ProcessInstanceCriterion.INSTANCE_UUID_ASC);
    checkListContent(3, 5, 2, instanceUUIDsP2, retrievedInstances);

    getRuntimeAPI().deleteAllProcessInstances(processUUID1);
    getRuntimeAPI().deleteAllProcessInstances(processUUID2);
    getManagementAPI().disable(processUUID1);
    getManagementAPI().disable(processUUID2);
    getManagementAPI().deleteProcess(processUUID1);
    getManagementAPI().deleteProcess(processUUID2);
  }

  public void testGetLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDateExceptOrderByUUIDDesc()
      throws Exception {
    ProcessDefinition process1 = getProcess();
    ProcessDefinition process2 = getProcess2();

    process1 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process1));
    process2 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process2));

    final ProcessDefinitionUUID processUUID1 = process1.getUUID();
    final ProcessDefinitionUUID processUUID2 = process2.getUUID();

    final int numberOfProcess1 = 10;
    final List<ProcessInstanceUUID> instanceUUIDsP1 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID1, numberOfProcess1, instanceUUIDsP1);
    updateInstanceVariable(instanceUUIDsP1, "text", "Hello", true);

    final int numberOfProcess2 = 5;
    final List<ProcessInstanceUUID> instanceUUIDsP2 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID2, numberOfProcess2, instanceUUIDsP2);
    updateInstanceVariable(instanceUUIDsP2, "text", "Hello", true);

    final Set<ProcessDefinitionUUID> exceptP1 = new HashSet<ProcessDefinitionUUID>();
    exceptP1.add(processUUID1);

    Collections.sort(instanceUUIDsP2, new ProcessInstanceUUIDComparator());
    Collections.reverse(instanceUUIDsP2);
    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDateExcept("john", 2, 0, 3,
            exceptP1, ProcessInstanceCriterion.INSTANCE_UUID_DESC);
    checkListContent(0, 3, 3, instanceUUIDsP2, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithActiveUserAndActivityInstanceExpectedEndDateExcept("john", 2, 3, 5,
            exceptP1, ProcessInstanceCriterion.INSTANCE_UUID_DESC);
    checkListContent(3, 5, 2, instanceUUIDsP2, retrievedInstances);

    getRuntimeAPI().deleteAllProcessInstances(processUUID1);
    getRuntimeAPI().deleteAllProcessInstances(processUUID2);
    getManagementAPI().disable(processUUID1);
    getManagementAPI().disable(processUUID2);
    getManagementAPI().deleteProcess(processUUID1);
    getManagementAPI().deleteProcess(processUUID2);
  }

  public void testGetLightParentProcessInstancesWithOverdueTasksOrderByLastUpdateAsc() throws Exception {
    ProcessDefinition process = getProcess();
    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final int numberOfProcess = 10;
    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID, numberOfProcess, instanceUUIDs);

    getManagementAPI().deployJar("getExecJar.jar", Misc.generateJar(ChangeExpectedEndDateActitityInstance.class));

    final Date today = new Date();
    for (int i = 0; i < numberOfProcess; i++) {
      final TaskInstance task = getQueryRuntimeAPI().getTasks(instanceUUIDs.get(i)).iterator().next();
      assertEquals("step1", task.getActivityName());
      final ChangeExpectedEndDateActitityInstance dayBeforeYesterday = new ChangeExpectedEndDateActitityInstance(
          task.getUUID(), DateUtil.backTo(today, 2));
      getCommandAPI().execute(dayBeforeYesterday);
    }

    updateInstanceVariable(instanceUUIDs, "text", "Hello", true);

    Collections.reverse(instanceUUIDs);
    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithOverdueTasks("john", 0, 5, ProcessInstanceCriterion.LAST_UPDATE_ASC);
    checkListContent(0, 5, 5, instanceUUIDs, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithOverdueTasks("john", 5, 11,
        ProcessInstanceCriterion.LAST_UPDATE_ASC);
    checkListContent(5, 11, 5, instanceUUIDs, retrievedInstances);

    getManagementAPI().removeJar("getExecJar.jar");
    getRuntimeAPI().deleteAllProcessInstances(processUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetLightParentProcessInstancesWithOverdueTasksOrderByLastUpdateDesc() throws Exception {
    ProcessDefinition process = getProcess();
    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final int numberOfProcess = 10;
    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID, numberOfProcess, instanceUUIDs);

    getManagementAPI().deployJar("getExecJar.jar", Misc.generateJar(ChangeExpectedEndDateActitityInstance.class));

    final Date today = new Date();
    for (int i = 0; i < numberOfProcess; i++) {
      final TaskInstance task = getQueryRuntimeAPI().getTasks(instanceUUIDs.get(i)).iterator().next();
      final ChangeExpectedEndDateActitityInstance dayBeforeYesterday = new ChangeExpectedEndDateActitityInstance(
          task.getUUID(), DateUtil.backTo(today, 2));
      getCommandAPI().execute(dayBeforeYesterday);
    }

    updateInstanceVariable(instanceUUIDs, "text", "Hello", true);

    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithOverdueTasks("john", 0, 5, ProcessInstanceCriterion.LAST_UPDATE_DESC);
    checkListContent(0, 5, 5, instanceUUIDs, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithOverdueTasks("john", 5, 11,
        ProcessInstanceCriterion.LAST_UPDATE_DESC);
    checkListContent(5, 11, 5, instanceUUIDs, retrievedInstances);

    getManagementAPI().removeJar("getExecJar.jar");
    getRuntimeAPI().deleteAllProcessInstances(processUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetLightParentProcessInstancesWithOverdueTasksOrderByStartedDateAsc() throws Exception {
    ProcessDefinition process = getProcess();
    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final int numberOfProcess = 10;
    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID, numberOfProcess, instanceUUIDs);

    getManagementAPI().deployJar("getExecJar.jar", Misc.generateJar(ChangeExpectedEndDateActitityInstance.class));

    final Date today = new Date();
    for (int i = 0; i < numberOfProcess; i++) {
      final TaskInstance task = getQueryRuntimeAPI().getTasks(instanceUUIDs.get(i)).iterator().next();
      final ChangeExpectedEndDateActitityInstance dayBeforeYesterday = new ChangeExpectedEndDateActitityInstance(
          task.getUUID(), DateUtil.backTo(today, 2));
      getCommandAPI().execute(dayBeforeYesterday);
    }

    updateInstanceVariable(instanceUUIDs, "text", "Hello", true);

    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithOverdueTasks("john", 0, 5, ProcessInstanceCriterion.STARTED_DATE_ASC);
    checkListContent(0, 5, 5, instanceUUIDs, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithOverdueTasks("john", 5, 11,
        ProcessInstanceCriterion.STARTED_DATE_ASC);
    checkListContent(5, 11, 5, instanceUUIDs, retrievedInstances);

    getManagementAPI().removeJar("getExecJar.jar");
    getRuntimeAPI().deleteAllProcessInstances(processUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetLightParentProcessInstancesWithOverdueTasksOrderByStartedDateDesc() throws Exception {
    ProcessDefinition process = getProcess();
    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final int numberOfProcess = 10;
    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID, numberOfProcess, instanceUUIDs);

    getManagementAPI().deployJar("getExecJar.jar", Misc.generateJar(ChangeExpectedEndDateActitityInstance.class));

    final Date today = new Date();
    for (int i = 0; i < numberOfProcess; i++) {
      final TaskInstance task = getQueryRuntimeAPI().getTasks(instanceUUIDs.get(i)).iterator().next();
      final ChangeExpectedEndDateActitityInstance dayBeforeYesterday = new ChangeExpectedEndDateActitityInstance(
          task.getUUID(), DateUtil.backTo(today, 2));
      getCommandAPI().execute(dayBeforeYesterday);
    }

    updateInstanceVariable(instanceUUIDs, "text", "Hello", true);

    Collections.reverse(instanceUUIDs);
    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithOverdueTasks("john", 0, 5, ProcessInstanceCriterion.STARTED_DATE_DESC);
    checkListContent(0, 5, 5, instanceUUIDs, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithOverdueTasks("john", 5, 11,
        ProcessInstanceCriterion.STARTED_DATE_DESC);
    checkListContent(5, 11, 5, instanceUUIDs, retrievedInstances);

    getManagementAPI().removeJar("getExecJar.jar");
    getRuntimeAPI().deleteAllProcessInstances(processUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetLightParentProcessInstancesWithOverdueTasksOrderByEndedDateAsc() throws Exception {
    ProcessDefinition process = getProcess();
    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final int numberOfProcess = 10;
    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID, numberOfProcess, instanceUUIDs);

    getManagementAPI().deployJar("getExecJar.jar", Misc.generateJar(ChangeExpectedEndDateActitityInstance.class));

    final Date today = new Date();
    for (int i = 0; i < numberOfProcess; i++) {
      final TaskInstance task = getQueryRuntimeAPI().getTasks(instanceUUIDs.get(i)).iterator().next();
      final ChangeExpectedEndDateActitityInstance dayBeforeYesterday = new ChangeExpectedEndDateActitityInstance(
          task.getUUID(), DateUtil.backTo(today, 2));
      getCommandAPI().execute(dayBeforeYesterday);
    }

    updateInstanceVariable(instanceUUIDs, "text", "Hello", true);

    // The end date is empty
    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithOverdueTasks("john", 0, 5, ProcessInstanceCriterion.ENDED_DATE_ASC);
    assertEquals(5, retrievedInstances.size());

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithOverdueTasks("john", 5, 11,
        ProcessInstanceCriterion.ENDED_DATE_ASC);
    assertEquals(5, retrievedInstances.size());

    getManagementAPI().removeJar("getExecJar.jar");
    getRuntimeAPI().deleteAllProcessInstances(processUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetLightParentProcessInstancesWithOverdueTasksOrderByEndedDateDesc() throws Exception {
    ProcessDefinition process = getProcess();
    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final int numberOfProcess = 10;
    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID, numberOfProcess, instanceUUIDs);

    getManagementAPI().deployJar("getExecJar.jar", Misc.generateJar(ChangeExpectedEndDateActitityInstance.class));

    final Date today = new Date();
    for (int i = 0; i < numberOfProcess; i++) {
      final TaskInstance task = getQueryRuntimeAPI().getTasks(instanceUUIDs.get(i)).iterator().next();
      final ChangeExpectedEndDateActitityInstance dayBeforeYesterday = new ChangeExpectedEndDateActitityInstance(
          task.getUUID(), DateUtil.backTo(today, 2));
      getCommandAPI().execute(dayBeforeYesterday);
    }

    updateInstanceVariable(instanceUUIDs, "text", "Hello", true);

    // The end date is empty
    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithOverdueTasks("john", 0, 5, ProcessInstanceCriterion.ENDED_DATE_DESC);
    assertEquals(5, retrievedInstances.size());

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithOverdueTasks("john", 5, 11,
        ProcessInstanceCriterion.ENDED_DATE_DESC);
    assertEquals(5, retrievedInstances.size());

    getManagementAPI().removeJar("getExecJar.jar");
    getRuntimeAPI().deleteAllProcessInstances(processUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetLightParentProcessInstancesWithOverdueTasksOrderByNbAsc() throws Exception {
    ProcessDefinition process = getProcess();
    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final int numberOfProcess = 10;
    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID, numberOfProcess, instanceUUIDs);

    getManagementAPI().deployJar("getExecJar.jar", Misc.generateJar(ChangeExpectedEndDateActitityInstance.class));

    final Date today = new Date();
    for (int i = 0; i < numberOfProcess; i++) {
      final TaskInstance task = getQueryRuntimeAPI().getTasks(instanceUUIDs.get(i)).iterator().next();
      final ChangeExpectedEndDateActitityInstance dayBeforeYesterday = new ChangeExpectedEndDateActitityInstance(
          task.getUUID(), DateUtil.backTo(today, 2));
      getCommandAPI().execute(dayBeforeYesterday);
    }

    updateInstanceVariable(instanceUUIDs, "text", "Hello", true);

    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithOverdueTasks("john", 0, 5, ProcessInstanceCriterion.INSTANCE_NUMBER_ASC);
    checkListContent(0, 5, 5, instanceUUIDs, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithOverdueTasks("john", 5, 11,
        ProcessInstanceCriterion.INSTANCE_NUMBER_ASC);
    checkListContent(5, 11, 5, instanceUUIDs, retrievedInstances);

    getManagementAPI().removeJar("getExecJar.jar");
    getRuntimeAPI().deleteAllProcessInstances(processUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetLightParentProcessInstancesWithOverdueTasksOrderByNbDesc() throws Exception {
    ProcessDefinition process = getProcess();
    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final int numberOfProcess = 10;
    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID, numberOfProcess, instanceUUIDs);

    getManagementAPI().deployJar("getExecJar.jar", Misc.generateJar(ChangeExpectedEndDateActitityInstance.class));

    final Date today = new Date();
    for (int i = 0; i < numberOfProcess; i++) {
      final TaskInstance task = getQueryRuntimeAPI().getTasks(instanceUUIDs.get(i)).iterator().next();
      final ChangeExpectedEndDateActitityInstance dayBeforeYesterday = new ChangeExpectedEndDateActitityInstance(
          task.getUUID(), DateUtil.backTo(today, 2));
      getCommandAPI().execute(dayBeforeYesterday);
    }

    updateInstanceVariable(instanceUUIDs, "text", "Hello", true);

    Collections.reverse(instanceUUIDs);
    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithOverdueTasks("john", 0, 5, ProcessInstanceCriterion.INSTANCE_NUMBER_DESC);
    checkListContent(0, 5, 5, instanceUUIDs, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithOverdueTasks("john", 5, 11,
        ProcessInstanceCriterion.INSTANCE_NUMBER_DESC);
    checkListContent(5, 11, 5, instanceUUIDs, retrievedInstances);

    getManagementAPI().removeJar("getExecJar.jar");
    getRuntimeAPI().deleteAllProcessInstances(processUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetLightParentProcessInstancesWithOverdueTasksOrderByUUIDAsc() throws Exception {
    ProcessDefinition process = getProcess();
    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final int numberOfProcess = 10;
    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID, numberOfProcess, instanceUUIDs);

    getManagementAPI().deployJar("getExecJar.jar", Misc.generateJar(ChangeExpectedEndDateActitityInstance.class));

    final Date today = new Date();
    for (int i = 0; i < numberOfProcess; i++) {
      final TaskInstance task = getQueryRuntimeAPI().getTasks(instanceUUIDs.get(i)).iterator().next();
      final ChangeExpectedEndDateActitityInstance dayBeforeYesterday = new ChangeExpectedEndDateActitityInstance(
          task.getUUID(), DateUtil.backTo(today, 2));
      getCommandAPI().execute(dayBeforeYesterday);
    }

    updateInstanceVariable(instanceUUIDs, "text", "Hello", true);

    Collections.sort(instanceUUIDs, new ProcessInstanceUUIDComparator());
    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithOverdueTasks("john", 0, 5, ProcessInstanceCriterion.INSTANCE_UUID_ASC);
    checkListContent(0, 5, 5, instanceUUIDs, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithOverdueTasks("john", 5, 11,
        ProcessInstanceCriterion.INSTANCE_UUID_ASC);
    checkListContent(5, 11, 5, instanceUUIDs, retrievedInstances);

    getManagementAPI().removeJar("getExecJar.jar");
    getRuntimeAPI().deleteAllProcessInstances(processUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetLightParentProcessInstancesWithOverdueTasksOrderByUUIDDesc() throws Exception {
    ProcessDefinition process = getProcess();
    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final int numberOfProcess = 10;
    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID, numberOfProcess, instanceUUIDs);

    getManagementAPI().deployJar("getExecJar.jar", Misc.generateJar(ChangeExpectedEndDateActitityInstance.class));

    final Date today = new Date();
    for (int i = 0; i < numberOfProcess; i++) {
      final TaskInstance task = getQueryRuntimeAPI().getTasks(instanceUUIDs.get(i)).iterator().next();
      final ChangeExpectedEndDateActitityInstance dayBeforeYesterday = new ChangeExpectedEndDateActitityInstance(
          task.getUUID(), DateUtil.backTo(today, 2));
      getCommandAPI().execute(dayBeforeYesterday);
    }

    updateInstanceVariable(instanceUUIDs, "text", "Hello", true);

    Collections.sort(instanceUUIDs, new ProcessInstanceUUIDComparator());
    Collections.reverse(instanceUUIDs);
    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithOverdueTasks("john", 0, 5, ProcessInstanceCriterion.INSTANCE_UUID_DESC);
    checkListContent(0, 5, 5, instanceUUIDs, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithOverdueTasks("john", 5, 11,
        ProcessInstanceCriterion.INSTANCE_UUID_DESC);
    checkListContent(5, 11, 5, instanceUUIDs, retrievedInstances);

    getManagementAPI().removeJar("getExecJar.jar");
    getRuntimeAPI().deleteAllProcessInstances(processUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetLightParentProcessInstancesWithOverdueTasksFromUUIDsOrderByLastUpdateAsc() throws Exception {
    ProcessDefinition process = getProcess();
    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final int numberOfProcess = 10;
    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID, numberOfProcess, instanceUUIDs);

    getManagementAPI().deployJar("getExecJar.jar", Misc.generateJar(ChangeExpectedEndDateActitityInstance.class));

    final Date today = new Date();
    for (int i = 0; i < numberOfProcess; i++) {
      final TaskInstance task = getQueryRuntimeAPI().getTasks(instanceUUIDs.get(i)).iterator().next();
      final ChangeExpectedEndDateActitityInstance dayBeforeYesterday = new ChangeExpectedEndDateActitityInstance(
          task.getUUID(), DateUtil.backTo(today, 2));
      getCommandAPI().execute(dayBeforeYesterday);
    }

    updateInstanceVariable(instanceUUIDs, "text", "Hello", true);

    final Set<ProcessDefinitionUUID> fromP = Collections.singleton(processUUID);

    Collections.reverse(instanceUUIDs);
    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithOverdueTasks("john", 0, 5, fromP, ProcessInstanceCriterion.LAST_UPDATE_ASC);
    checkListContent(0, 5, 5, instanceUUIDs, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithOverdueTasks("john", 5, 11, fromP,
        ProcessInstanceCriterion.LAST_UPDATE_ASC);
    checkListContent(5, 11, 5, instanceUUIDs, retrievedInstances);

    getManagementAPI().removeJar("getExecJar.jar");
    getRuntimeAPI().deleteAllProcessInstances(processUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetLightParentProcessInstancesWithOverdueTasksFromUUIDsOrderByLastUpdateDesc() throws Exception {
    ProcessDefinition process = getProcess();
    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final int numberOfProcess = 10;
    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID, numberOfProcess, instanceUUIDs);

    getManagementAPI().deployJar("getExecJar.jar", Misc.generateJar(ChangeExpectedEndDateActitityInstance.class));

    final Date today = new Date();
    for (int i = 0; i < numberOfProcess; i++) {
      final TaskInstance task = getQueryRuntimeAPI().getTasks(instanceUUIDs.get(i)).iterator().next();
      final ChangeExpectedEndDateActitityInstance dayBeforeYesterday = new ChangeExpectedEndDateActitityInstance(
          task.getUUID(), DateUtil.backTo(today, 2));
      getCommandAPI().execute(dayBeforeYesterday);
    }

    updateInstanceVariable(instanceUUIDs, "text", "Hello", true);

    final Set<ProcessDefinitionUUID> fromP = Collections.singleton(processUUID);

    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithOverdueTasks("john", 0, 5, fromP, ProcessInstanceCriterion.LAST_UPDATE_DESC);
    checkListContent(0, 5, 5, instanceUUIDs, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithOverdueTasks("john", 5, 11, fromP,
        ProcessInstanceCriterion.LAST_UPDATE_DESC);
    checkListContent(5, 11, 5, instanceUUIDs, retrievedInstances);

    getManagementAPI().removeJar("getExecJar.jar");
    getRuntimeAPI().deleteAllProcessInstances(processUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetLightParentProcessInstancesWithOverdueTasksFromUUIDsOrderByStartedDateAsc() throws Exception {
    ProcessDefinition process = getProcess();
    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final int numberOfProcess = 10;
    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID, numberOfProcess, instanceUUIDs);

    getManagementAPI().deployJar("getExecJar.jar", Misc.generateJar(ChangeExpectedEndDateActitityInstance.class));

    final Date today = new Date();
    for (int i = 0; i < numberOfProcess; i++) {
      final TaskInstance task = getQueryRuntimeAPI().getTasks(instanceUUIDs.get(i)).iterator().next();
      final ChangeExpectedEndDateActitityInstance dayBeforeYesterday = new ChangeExpectedEndDateActitityInstance(
          task.getUUID(), DateUtil.backTo(today, 2));
      getCommandAPI().execute(dayBeforeYesterday);
    }

    updateInstanceVariable(instanceUUIDs, "text", "Hello", true);

    final Set<ProcessDefinitionUUID> fromP = Collections.singleton(processUUID);

    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithOverdueTasks("john", 0, 5, fromP, ProcessInstanceCriterion.STARTED_DATE_ASC);
    checkListContent(0, 5, 5, instanceUUIDs, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithOverdueTasks("john", 5, 11, fromP,
        ProcessInstanceCriterion.STARTED_DATE_ASC);
    checkListContent(5, 11, 5, instanceUUIDs, retrievedInstances);

    getManagementAPI().removeJar("getExecJar.jar");
    getRuntimeAPI().deleteAllProcessInstances(processUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetLightParentProcessInstancesWithOverdueTasksFromUUIDsOrderByStartedDateDesc() throws Exception {
    ProcessDefinition process = getProcess();
    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final int numberOfProcess = 10;
    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID, numberOfProcess, instanceUUIDs);

    getManagementAPI().deployJar("getExecJar.jar", Misc.generateJar(ChangeExpectedEndDateActitityInstance.class));

    final Date today = new Date();
    for (int i = 0; i < numberOfProcess; i++) {
      final TaskInstance task = getQueryRuntimeAPI().getTasks(instanceUUIDs.get(i)).iterator().next();
      final ChangeExpectedEndDateActitityInstance dayBeforeYesterday = new ChangeExpectedEndDateActitityInstance(
          task.getUUID(), DateUtil.backTo(today, 2));
      getCommandAPI().execute(dayBeforeYesterday);
    }

    updateInstanceVariable(instanceUUIDs, "text", "Hello", true);

    final Set<ProcessDefinitionUUID> fromP = Collections.singleton(processUUID);

    Collections.reverse(instanceUUIDs);
    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithOverdueTasks("john", 0, 5, fromP, ProcessInstanceCriterion.STARTED_DATE_DESC);
    checkListContent(0, 5, 5, instanceUUIDs, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithOverdueTasks("john", 5, 11, fromP,
        ProcessInstanceCriterion.STARTED_DATE_DESC);
    checkListContent(5, 11, 5, instanceUUIDs, retrievedInstances);

    getManagementAPI().removeJar("getExecJar.jar");
    getRuntimeAPI().deleteAllProcessInstances(processUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetLightParentProcessInstancesWithOverdueTasksFromUUIDsOrderByEndedDateAsc() throws Exception {
    ProcessDefinition process = getProcess();
    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final int numberOfProcess = 10;
    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID, numberOfProcess, instanceUUIDs);

    getManagementAPI().deployJar("getExecJar.jar", Misc.generateJar(ChangeExpectedEndDateActitityInstance.class));

    final Date today = new Date();
    for (int i = 0; i < numberOfProcess; i++) {
      final TaskInstance task = getQueryRuntimeAPI().getTasks(instanceUUIDs.get(i)).iterator().next();
      final ChangeExpectedEndDateActitityInstance dayBeforeYesterday = new ChangeExpectedEndDateActitityInstance(
          task.getUUID(), DateUtil.backTo(today, 2));
      getCommandAPI().execute(dayBeforeYesterday);
    }

    updateInstanceVariable(instanceUUIDs, "text", "Hello", true);

    final Set<ProcessDefinitionUUID> fromP = Collections.singleton(processUUID);

    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithOverdueTasks("john", 0, 5, fromP, ProcessInstanceCriterion.ENDED_DATE_ASC);
    assertEquals(5, retrievedInstances.size());

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithOverdueTasks("john", 5, 11, fromP,
        ProcessInstanceCriterion.ENDED_DATE_ASC);
    assertEquals(5, retrievedInstances.size());

    getManagementAPI().removeJar("getExecJar.jar");
    getRuntimeAPI().deleteAllProcessInstances(processUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetLightParentProcessInstancesWithOverdueTasksFromUUIDsOrderByEndedDateDesc() throws Exception {
    ProcessDefinition process = getProcess();
    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final int numberOfProcess = 10;
    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID, numberOfProcess, instanceUUIDs);

    getManagementAPI().deployJar("getExecJar.jar", Misc.generateJar(ChangeExpectedEndDateActitityInstance.class));

    final Date today = new Date();
    for (int i = 0; i < numberOfProcess; i++) {
      final TaskInstance task = getQueryRuntimeAPI().getTasks(instanceUUIDs.get(i)).iterator().next();
      final ChangeExpectedEndDateActitityInstance dayBeforeYesterday = new ChangeExpectedEndDateActitityInstance(
          task.getUUID(), DateUtil.backTo(today, 2));
      getCommandAPI().execute(dayBeforeYesterday);
    }

    updateInstanceVariable(instanceUUIDs, "text", "Hello", true);

    final Set<ProcessDefinitionUUID> fromP = Collections.singleton(processUUID);

    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithOverdueTasks("john", 0, 5, fromP, ProcessInstanceCriterion.ENDED_DATE_DESC);
    assertEquals(5, retrievedInstances.size());

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithOverdueTasks("john", 5, 11, fromP,
        ProcessInstanceCriterion.ENDED_DATE_DESC);
    assertEquals(5, retrievedInstances.size());

    getManagementAPI().removeJar("getExecJar.jar");
    getRuntimeAPI().deleteAllProcessInstances(processUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetLightParentProcessInstancesWithOverdueTasksFromUUIDsOrderByNbAsc() throws Exception {
    ProcessDefinition process = getProcess();
    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final int numberOfProcess = 10;
    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID, numberOfProcess, instanceUUIDs);

    getManagementAPI().deployJar("getExecJar.jar", Misc.generateJar(ChangeExpectedEndDateActitityInstance.class));

    final Date today = new Date();
    for (int i = 0; i < numberOfProcess; i++) {
      final TaskInstance task = getQueryRuntimeAPI().getTasks(instanceUUIDs.get(i)).iterator().next();
      final ChangeExpectedEndDateActitityInstance dayBeforeYesterday = new ChangeExpectedEndDateActitityInstance(
          task.getUUID(), DateUtil.backTo(today, 2));
      getCommandAPI().execute(dayBeforeYesterday);
    }

    updateInstanceVariable(instanceUUIDs, "text", "Hello", true);

    final Set<ProcessDefinitionUUID> fromP = Collections.singleton(processUUID);

    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithOverdueTasks("john", 0, 5, fromP,
            ProcessInstanceCriterion.INSTANCE_NUMBER_ASC);
    checkListContent(0, 5, 5, instanceUUIDs, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithOverdueTasks("john", 5, 11, fromP,
        ProcessInstanceCriterion.INSTANCE_NUMBER_ASC);
    checkListContent(5, 11, 5, instanceUUIDs, retrievedInstances);

    getManagementAPI().removeJar("getExecJar.jar");
    getRuntimeAPI().deleteAllProcessInstances(processUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetLightParentProcessInstancesWithOverdueTasksFromUUIDsOrderByNbDesc() throws Exception {
    ProcessDefinition process = getProcess();
    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final int numberOfProcess = 10;
    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID, numberOfProcess, instanceUUIDs);

    getManagementAPI().deployJar("getExecJar.jar", Misc.generateJar(ChangeExpectedEndDateActitityInstance.class));

    final Date today = new Date();
    for (int i = 0; i < numberOfProcess; i++) {
      final TaskInstance task = getQueryRuntimeAPI().getTasks(instanceUUIDs.get(i)).iterator().next();
      final ChangeExpectedEndDateActitityInstance dayBeforeYesterday = new ChangeExpectedEndDateActitityInstance(
          task.getUUID(), DateUtil.backTo(today, 2));
      getCommandAPI().execute(dayBeforeYesterday);
    }

    updateInstanceVariable(instanceUUIDs, "text", "Hello", true);

    final Set<ProcessDefinitionUUID> fromP = Collections.singleton(processUUID);

    Collections.reverse(instanceUUIDs);
    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithOverdueTasks("john", 0, 5, fromP,
            ProcessInstanceCriterion.INSTANCE_NUMBER_DESC);
    checkListContent(0, 5, 5, instanceUUIDs, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithOverdueTasks("john", 5, 11, fromP,
        ProcessInstanceCriterion.INSTANCE_NUMBER_DESC);
    checkListContent(5, 11, 5, instanceUUIDs, retrievedInstances);

    getManagementAPI().removeJar("getExecJar.jar");
    getRuntimeAPI().deleteAllProcessInstances(processUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetLightParentProcessInstancesWithOverdueTasksFromUUIDsOrderByUUIDAsc() throws Exception {
    ProcessDefinition process = getProcess();
    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final int numberOfProcess = 10;
    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID, numberOfProcess, instanceUUIDs);

    getManagementAPI().deployJar("getExecJar.jar", Misc.generateJar(ChangeExpectedEndDateActitityInstance.class));

    final Date today = new Date();
    for (int i = 0; i < numberOfProcess; i++) {
      final TaskInstance task = getQueryRuntimeAPI().getTasks(instanceUUIDs.get(i)).iterator().next();
      final ChangeExpectedEndDateActitityInstance dayBeforeYesterday = new ChangeExpectedEndDateActitityInstance(
          task.getUUID(), DateUtil.backTo(today, 2));
      getCommandAPI().execute(dayBeforeYesterday);
    }

    updateInstanceVariable(instanceUUIDs, "text", "Hello", true);

    final Set<ProcessDefinitionUUID> fromP = Collections.singleton(processUUID);

    Collections.sort(instanceUUIDs, new ProcessInstanceUUIDComparator());
    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithOverdueTasks("john", 0, 5, fromP, ProcessInstanceCriterion.INSTANCE_UUID_ASC);
    checkListContent(0, 5, 5, instanceUUIDs, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithOverdueTasks("john", 5, 11, fromP,
        ProcessInstanceCriterion.INSTANCE_UUID_ASC);
    checkListContent(5, 11, 5, instanceUUIDs, retrievedInstances);

    getManagementAPI().removeJar("getExecJar.jar");
    getRuntimeAPI().deleteAllProcessInstances(processUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetLightParentProcessInstancesWithOverdueTasksFromUUIDsOrderByUUIDDesc() throws Exception {
    ProcessDefinition process = getProcess();
    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final int numberOfProcess = 10;
    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID, numberOfProcess, instanceUUIDs);

    getManagementAPI().deployJar("getExecJar.jar", Misc.generateJar(ChangeExpectedEndDateActitityInstance.class));

    final Date today = new Date();
    for (int i = 0; i < numberOfProcess; i++) {
      final TaskInstance task = getQueryRuntimeAPI().getTasks(instanceUUIDs.get(i)).iterator().next();
      final ChangeExpectedEndDateActitityInstance dayBeforeYesterday = new ChangeExpectedEndDateActitityInstance(
          task.getUUID(), DateUtil.backTo(today, 2));
      getCommandAPI().execute(dayBeforeYesterday);
    }

    updateInstanceVariable(instanceUUIDs, "text", "Hello", true);

    final Set<ProcessDefinitionUUID> fromP = Collections.singleton(processUUID);

    Collections.sort(instanceUUIDs, new ProcessInstanceUUIDComparator());
    Collections.reverse(instanceUUIDs);
    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithOverdueTasks("john", 0, 5, fromP,
            ProcessInstanceCriterion.INSTANCE_UUID_DESC);
    checkListContent(0, 5, 5, instanceUUIDs, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithOverdueTasks("john", 5, 11, fromP,
        ProcessInstanceCriterion.INSTANCE_UUID_DESC);
    checkListContent(5, 11, 5, instanceUUIDs, retrievedInstances);

    getManagementAPI().removeJar("getExecJar.jar");
    getRuntimeAPI().deleteAllProcessInstances(processUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetLightParentProcessInstancesWithOverdueTasksExceptOrderByLastUpdateAsc() throws Exception {
    ProcessDefinition process1 = getProcess();
    ProcessDefinition process2 = getProcess2();

    process1 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process1));
    process2 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process2));

    final ProcessDefinitionUUID processUUID1 = process1.getUUID();
    final ProcessDefinitionUUID processUUID2 = process2.getUUID();

    final int numberOfProcess1 = 10;
    final List<ProcessInstanceUUID> instanceUUIDsP1 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID1, numberOfProcess1, instanceUUIDsP1);

    getManagementAPI().deployJar("getExecJar.jar", Misc.generateJar(ChangeExpectedEndDateActitityInstance.class));

    final Date today = new Date();
    for (int i = 0; i < numberOfProcess1; i++) {
      final TaskInstance task = getQueryRuntimeAPI().getTasks(instanceUUIDsP1.get(i)).iterator().next();
      final ChangeExpectedEndDateActitityInstance dayBeforeYesterday = new ChangeExpectedEndDateActitityInstance(
          task.getUUID(), DateUtil.backTo(today, 2));
      getCommandAPI().execute(dayBeforeYesterday);
    }

    updateInstanceVariable(instanceUUIDsP1, "text", "Hello", true);

    final int numberOfProcess2 = 5;
    final List<ProcessInstanceUUID> instanceUUIDsP2 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID2, numberOfProcess2, instanceUUIDsP2);

    for (int i = 0; i < numberOfProcess2; i++) {
      final TaskInstance task = getQueryRuntimeAPI().getTasks(instanceUUIDsP2.get(i)).iterator().next();
      final ChangeExpectedEndDateActitityInstance dayBeforeYesterday = new ChangeExpectedEndDateActitityInstance(
          task.getUUID(), DateUtil.backTo(today, 2));
      getCommandAPI().execute(dayBeforeYesterday);
    }

    updateInstanceVariable(instanceUUIDsP2, "text", "Hello", true);

    final Set<ProcessDefinitionUUID> exceptP1 = Collections.singleton(processUUID1);

    Collections.reverse(instanceUUIDsP2);
    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithOverdueTasksExcept("john", 0, 3, exceptP1,
            ProcessInstanceCriterion.LAST_UPDATE_ASC);
    checkListContent(0, 3, 3, instanceUUIDsP2, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithOverdueTasksExcept("john", 3, 3,
        exceptP1, ProcessInstanceCriterion.LAST_UPDATE_ASC);
    checkListContent(3, 3, 2, instanceUUIDsP2, retrievedInstances);

    getManagementAPI().removeJar("getExecJar.jar");
    getRuntimeAPI().deleteAllProcessInstances(processUUID1);
    getRuntimeAPI().deleteAllProcessInstances(processUUID2);
    getManagementAPI().disable(processUUID1);
    getManagementAPI().disable(processUUID2);
    getManagementAPI().deleteProcess(processUUID1);
    getManagementAPI().deleteProcess(processUUID2);
  }

  public void testGetLightParentProcessInstancesWithOverdueTasksExceptOrderByLastUpdateDesc() throws Exception {
    ProcessDefinition process1 = getProcess();
    ProcessDefinition process2 = getProcess2();

    process1 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process1));
    process2 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process2));

    final ProcessDefinitionUUID processUUID1 = process1.getUUID();
    final ProcessDefinitionUUID processUUID2 = process2.getUUID();

    final int numberOfProcess1 = 10;
    final List<ProcessInstanceUUID> instanceUUIDsP1 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID1, numberOfProcess1, instanceUUIDsP1);

    getManagementAPI().deployJar("getExecJar.jar", Misc.generateJar(ChangeExpectedEndDateActitityInstance.class));

    final Date today = new Date();
    for (int i = 0; i < numberOfProcess1; i++) {
      final TaskInstance task = getQueryRuntimeAPI().getTasks(instanceUUIDsP1.get(i)).iterator().next();
      final ChangeExpectedEndDateActitityInstance dayBeforeYesterday = new ChangeExpectedEndDateActitityInstance(
          task.getUUID(), DateUtil.backTo(today, 2));
      getCommandAPI().execute(dayBeforeYesterday);
    }

    updateInstanceVariable(instanceUUIDsP1, "text", "Hello", true);

    final int numberOfProcess2 = 5;
    final List<ProcessInstanceUUID> instanceUUIDsP2 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID2, numberOfProcess2, instanceUUIDsP2);

    for (int i = 0; i < numberOfProcess2; i++) {
      final TaskInstance task = getQueryRuntimeAPI().getTasks(instanceUUIDsP2.get(i)).iterator().next();
      final ChangeExpectedEndDateActitityInstance dayBeforeYesterday = new ChangeExpectedEndDateActitityInstance(
          task.getUUID(), DateUtil.backTo(today, 2));
      getCommandAPI().execute(dayBeforeYesterday);
    }

    updateInstanceVariable(instanceUUIDsP2, "text", "Hello", true);

    final Set<ProcessDefinitionUUID> exceptP1 = Collections.singleton(processUUID1);

    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithOverdueTasksExcept("john", 0, 3, exceptP1,
            ProcessInstanceCriterion.LAST_UPDATE_DESC);
    checkListContent(0, 3, 3, instanceUUIDsP2, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithOverdueTasksExcept("john", 3, 3,
        exceptP1, ProcessInstanceCriterion.LAST_UPDATE_DESC);
    checkListContent(3, 3, 2, instanceUUIDsP2, retrievedInstances);

    getManagementAPI().removeJar("getExecJar.jar");
    getRuntimeAPI().deleteAllProcessInstances(processUUID1);
    getRuntimeAPI().deleteAllProcessInstances(processUUID2);
    getManagementAPI().disable(processUUID1);
    getManagementAPI().disable(processUUID2);
    getManagementAPI().deleteProcess(processUUID1);
    getManagementAPI().deleteProcess(processUUID2);
  }

  public void testGetLightParentProcessInstancesWithOverdueTasksExceptOrderByStartedDateAsc() throws Exception {
    ProcessDefinition process1 = getProcess();
    ProcessDefinition process2 = getProcess2();

    process1 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process1));
    process2 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process2));

    final ProcessDefinitionUUID processUUID1 = process1.getUUID();
    final ProcessDefinitionUUID processUUID2 = process2.getUUID();

    final int numberOfProcess1 = 10;
    final List<ProcessInstanceUUID> instanceUUIDsP1 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID1, numberOfProcess1, instanceUUIDsP1);

    getManagementAPI().deployJar("getExecJar.jar", Misc.generateJar(ChangeExpectedEndDateActitityInstance.class));

    final Date today = new Date();
    for (int i = 0; i < numberOfProcess1; i++) {
      final TaskInstance task = getQueryRuntimeAPI().getTasks(instanceUUIDsP1.get(i)).iterator().next();
      final ChangeExpectedEndDateActitityInstance dayBeforeYesterday = new ChangeExpectedEndDateActitityInstance(
          task.getUUID(), DateUtil.backTo(today, 2));
      getCommandAPI().execute(dayBeforeYesterday);
    }

    updateInstanceVariable(instanceUUIDsP1, "text", "Hello", true);

    final int numberOfProcess2 = 5;
    final List<ProcessInstanceUUID> instanceUUIDsP2 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID2, numberOfProcess2, instanceUUIDsP2);

    for (int i = 0; i < numberOfProcess2; i++) {
      final TaskInstance task = getQueryRuntimeAPI().getTasks(instanceUUIDsP2.get(i)).iterator().next();
      final ChangeExpectedEndDateActitityInstance dayBeforeYesterday = new ChangeExpectedEndDateActitityInstance(
          task.getUUID(), DateUtil.backTo(today, 2));
      getCommandAPI().execute(dayBeforeYesterday);
    }

    updateInstanceVariable(instanceUUIDsP2, "text", "Hello", true);

    final Set<ProcessDefinitionUUID> exceptP1 = Collections.singleton(processUUID1);

    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithOverdueTasksExcept("john", 0, 3, exceptP1,
            ProcessInstanceCriterion.STARTED_DATE_ASC);
    checkListContent(0, 3, 3, instanceUUIDsP2, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithOverdueTasksExcept("john", 3, 3,
        exceptP1, ProcessInstanceCriterion.STARTED_DATE_ASC);
    checkListContent(3, 3, 2, instanceUUIDsP2, retrievedInstances);

    getManagementAPI().removeJar("getExecJar.jar");
    getRuntimeAPI().deleteAllProcessInstances(processUUID1);
    getRuntimeAPI().deleteAllProcessInstances(processUUID2);
    getManagementAPI().disable(processUUID1);
    getManagementAPI().disable(processUUID2);
    getManagementAPI().deleteProcess(processUUID1);
    getManagementAPI().deleteProcess(processUUID2);
  }

  public void testGetLightParentProcessInstancesWithOverdueTasksExceptOrderByStartedDateDesc() throws Exception {
    ProcessDefinition process1 = getProcess();
    ProcessDefinition process2 = getProcess2();

    process1 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process1));
    process2 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process2));

    final ProcessDefinitionUUID processUUID1 = process1.getUUID();
    final ProcessDefinitionUUID processUUID2 = process2.getUUID();

    final int numberOfProcess1 = 10;
    final List<ProcessInstanceUUID> instanceUUIDsP1 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID1, numberOfProcess1, instanceUUIDsP1);

    getManagementAPI().deployJar("getExecJar.jar", Misc.generateJar(ChangeExpectedEndDateActitityInstance.class));

    final Date today = new Date();
    for (int i = 0; i < numberOfProcess1; i++) {
      final TaskInstance task = getQueryRuntimeAPI().getTasks(instanceUUIDsP1.get(i)).iterator().next();
      final ChangeExpectedEndDateActitityInstance dayBeforeYesterday = new ChangeExpectedEndDateActitityInstance(
          task.getUUID(), DateUtil.backTo(today, 2));
      getCommandAPI().execute(dayBeforeYesterday);
    }

    updateInstanceVariable(instanceUUIDsP1, "text", "Hello", true);

    final int numberOfProcess2 = 5;
    final List<ProcessInstanceUUID> instanceUUIDsP2 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID2, numberOfProcess2, instanceUUIDsP2);

    for (int i = 0; i < numberOfProcess2; i++) {
      final TaskInstance task = getQueryRuntimeAPI().getTasks(instanceUUIDsP2.get(i)).iterator().next();
      final ChangeExpectedEndDateActitityInstance dayBeforeYesterday = new ChangeExpectedEndDateActitityInstance(
          task.getUUID(), DateUtil.backTo(today, 2));
      getCommandAPI().execute(dayBeforeYesterday);
    }

    updateInstanceVariable(instanceUUIDsP2, "text", "Hello", true);

    final Set<ProcessDefinitionUUID> exceptP1 = Collections.singleton(processUUID1);

    Collections.reverse(instanceUUIDsP2);
    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithOverdueTasksExcept("john", 0, 3, exceptP1,
            ProcessInstanceCriterion.STARTED_DATE_DESC);
    checkListContent(0, 3, 3, instanceUUIDsP2, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithOverdueTasksExcept("john", 3, 3,
        exceptP1, ProcessInstanceCriterion.STARTED_DATE_DESC);
    checkListContent(3, 3, 2, instanceUUIDsP2, retrievedInstances);

    getManagementAPI().removeJar("getExecJar.jar");
    getRuntimeAPI().deleteAllProcessInstances(processUUID1);
    getRuntimeAPI().deleteAllProcessInstances(processUUID2);
    getManagementAPI().disable(processUUID1);
    getManagementAPI().disable(processUUID2);
    getManagementAPI().deleteProcess(processUUID1);
    getManagementAPI().deleteProcess(processUUID2);
  }

  public void testGetLightParentProcessInstancesWithOverdueTasksExceptOrderByEndedDateAsc() throws Exception {
    ProcessDefinition process1 = getProcess();
    ProcessDefinition process2 = getProcess2();

    process1 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process1));
    process2 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process2));

    final ProcessDefinitionUUID processUUID1 = process1.getUUID();
    final ProcessDefinitionUUID processUUID2 = process2.getUUID();

    final int numberOfProcess1 = 10;
    final List<ProcessInstanceUUID> instanceUUIDsP1 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID1, numberOfProcess1, instanceUUIDsP1);

    getManagementAPI().deployJar("getExecJar.jar", Misc.generateJar(ChangeExpectedEndDateActitityInstance.class));

    final Date today = new Date();
    for (int i = 0; i < numberOfProcess1; i++) {
      final TaskInstance task = getQueryRuntimeAPI().getTasks(instanceUUIDsP1.get(i)).iterator().next();
      final ChangeExpectedEndDateActitityInstance dayBeforeYesterday = new ChangeExpectedEndDateActitityInstance(
          task.getUUID(), DateUtil.backTo(today, 2));
      getCommandAPI().execute(dayBeforeYesterday);
    }

    updateInstanceVariable(instanceUUIDsP1, "text", "Hello", true);

    final int numberOfProcess2 = 5;
    final List<ProcessInstanceUUID> instanceUUIDsP2 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID2, numberOfProcess2, instanceUUIDsP2);

    for (int i = 0; i < numberOfProcess2; i++) {
      final TaskInstance task = getQueryRuntimeAPI().getTasks(instanceUUIDsP2.get(i)).iterator().next();
      final ChangeExpectedEndDateActitityInstance dayBeforeYesterday = new ChangeExpectedEndDateActitityInstance(
          task.getUUID(), DateUtil.backTo(today, 2));
      getCommandAPI().execute(dayBeforeYesterday);
    }

    updateInstanceVariable(instanceUUIDsP2, "text", "Hello", true);

    final Set<ProcessDefinitionUUID> exceptP1 = Collections.singleton(processUUID1);

    // the end date is empty
    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithOverdueTasksExcept("john", 0, 3, exceptP1,
            ProcessInstanceCriterion.ENDED_DATE_ASC);
    assertEquals(3, retrievedInstances.size());

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithOverdueTasksExcept("john", 3, 3,
        exceptP1, ProcessInstanceCriterion.ENDED_DATE_ASC);
    assertEquals(2, retrievedInstances.size());

    getManagementAPI().removeJar("getExecJar.jar");
    getRuntimeAPI().deleteAllProcessInstances(processUUID1);
    getRuntimeAPI().deleteAllProcessInstances(processUUID2);
    getManagementAPI().disable(processUUID1);
    getManagementAPI().disable(processUUID2);
    getManagementAPI().deleteProcess(processUUID1);
    getManagementAPI().deleteProcess(processUUID2);
  }

  public void testGetLightParentProcessInstancesWithOverdueTasksExceptOrderByEndedDateDesc() throws Exception {
    ProcessDefinition process1 = getProcess();
    ProcessDefinition process2 = getProcess2();

    process1 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process1));
    process2 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process2));

    final ProcessDefinitionUUID processUUID1 = process1.getUUID();
    final ProcessDefinitionUUID processUUID2 = process2.getUUID();

    final int numberOfProcess1 = 10;
    final List<ProcessInstanceUUID> instanceUUIDsP1 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID1, numberOfProcess1, instanceUUIDsP1);

    getManagementAPI().deployJar("getExecJar.jar", Misc.generateJar(ChangeExpectedEndDateActitityInstance.class));

    final Date today = new Date();
    for (int i = 0; i < numberOfProcess1; i++) {
      final TaskInstance task = getQueryRuntimeAPI().getTasks(instanceUUIDsP1.get(i)).iterator().next();
      final ChangeExpectedEndDateActitityInstance dayBeforeYesterday = new ChangeExpectedEndDateActitityInstance(
          task.getUUID(), DateUtil.backTo(today, 2));
      getCommandAPI().execute(dayBeforeYesterday);
    }

    updateInstanceVariable(instanceUUIDsP1, "text", "Hello", true);

    final int numberOfProcess2 = 5;
    final List<ProcessInstanceUUID> instanceUUIDsP2 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID2, numberOfProcess2, instanceUUIDsP2);

    for (int i = 0; i < numberOfProcess2; i++) {
      final TaskInstance task = getQueryRuntimeAPI().getTasks(instanceUUIDsP2.get(i)).iterator().next();
      final ChangeExpectedEndDateActitityInstance dayBeforeYesterday = new ChangeExpectedEndDateActitityInstance(
          task.getUUID(), DateUtil.backTo(today, 2));
      getCommandAPI().execute(dayBeforeYesterday);
    }

    updateInstanceVariable(instanceUUIDsP2, "text", "Hello", true);

    final Set<ProcessDefinitionUUID> exceptP1 = Collections.singleton(processUUID1);

    // the end date is empty
    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithOverdueTasksExcept("john", 0, 3, exceptP1,
            ProcessInstanceCriterion.ENDED_DATE_ASC);
    assertEquals(3, retrievedInstances.size());

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithOverdueTasksExcept("john", 3, 3,
        exceptP1, ProcessInstanceCriterion.ENDED_DATE_ASC);
    assertEquals(2, retrievedInstances.size());

    getManagementAPI().removeJar("getExecJar.jar");
    getRuntimeAPI().deleteAllProcessInstances(processUUID1);
    getRuntimeAPI().deleteAllProcessInstances(processUUID2);
    getManagementAPI().disable(processUUID1);
    getManagementAPI().disable(processUUID2);
    getManagementAPI().deleteProcess(processUUID1);
    getManagementAPI().deleteProcess(processUUID2);
  }

  public void testGetLightParentProcessInstancesWithOverdueTasksExceptOrderByNbAsc() throws Exception {
    ProcessDefinition process1 = getProcess();
    ProcessDefinition process2 = getProcess2();

    process1 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process1));
    process2 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process2));

    final ProcessDefinitionUUID processUUID1 = process1.getUUID();
    final ProcessDefinitionUUID processUUID2 = process2.getUUID();

    final int numberOfProcess1 = 10;
    final List<ProcessInstanceUUID> instanceUUIDsP1 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID1, numberOfProcess1, instanceUUIDsP1);

    getManagementAPI().deployJar("getExecJar.jar", Misc.generateJar(ChangeExpectedEndDateActitityInstance.class));

    final Date today = new Date();
    for (int i = 0; i < numberOfProcess1; i++) {
      final TaskInstance task = getQueryRuntimeAPI().getTasks(instanceUUIDsP1.get(i)).iterator().next();
      final ChangeExpectedEndDateActitityInstance dayBeforeYesterday = new ChangeExpectedEndDateActitityInstance(
          task.getUUID(), DateUtil.backTo(today, 2));
      getCommandAPI().execute(dayBeforeYesterday);
    }

    updateInstanceVariable(instanceUUIDsP1, "text", "Hello", true);

    final int numberOfProcess2 = 5;
    final List<ProcessInstanceUUID> instanceUUIDsP2 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID2, numberOfProcess2, instanceUUIDsP2);

    for (int i = 0; i < numberOfProcess2; i++) {
      final TaskInstance task = getQueryRuntimeAPI().getTasks(instanceUUIDsP2.get(i)).iterator().next();
      final ChangeExpectedEndDateActitityInstance dayBeforeYesterday = new ChangeExpectedEndDateActitityInstance(
          task.getUUID(), DateUtil.backTo(today, 2));
      getCommandAPI().execute(dayBeforeYesterday);
    }

    updateInstanceVariable(instanceUUIDsP2, "text", "Hello", true);

    final Set<ProcessDefinitionUUID> exceptP1 = Collections.singleton(processUUID1);

    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithOverdueTasksExcept("john", 0, 3, exceptP1,
            ProcessInstanceCriterion.INSTANCE_NUMBER_ASC);
    checkListContent(0, 3, 3, instanceUUIDsP2, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithOverdueTasksExcept("john", 3, 3,
        exceptP1, ProcessInstanceCriterion.INSTANCE_NUMBER_ASC);
    checkListContent(3, 3, 2, instanceUUIDsP2, retrievedInstances);

    getManagementAPI().removeJar("getExecJar.jar");
    getRuntimeAPI().deleteAllProcessInstances(processUUID1);
    getRuntimeAPI().deleteAllProcessInstances(processUUID2);
    getManagementAPI().disable(processUUID1);
    getManagementAPI().disable(processUUID2);
    getManagementAPI().deleteProcess(processUUID1);
    getManagementAPI().deleteProcess(processUUID2);
  }

  public void testGetLightParentProcessInstancesWithOverdueTasksExceptOrderByNbDesc() throws Exception {
    ProcessDefinition process1 = getProcess();
    ProcessDefinition process2 = getProcess2();

    process1 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process1));
    process2 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process2));

    final ProcessDefinitionUUID processUUID1 = process1.getUUID();
    final ProcessDefinitionUUID processUUID2 = process2.getUUID();

    final int numberOfProcess1 = 10;
    final List<ProcessInstanceUUID> instanceUUIDsP1 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID1, numberOfProcess1, instanceUUIDsP1);

    getManagementAPI().deployJar("getExecJar.jar", Misc.generateJar(ChangeExpectedEndDateActitityInstance.class));

    final Date today = new Date();
    for (int i = 0; i < numberOfProcess1; i++) {
      final TaskInstance task = getQueryRuntimeAPI().getTasks(instanceUUIDsP1.get(i)).iterator().next();
      final ChangeExpectedEndDateActitityInstance dayBeforeYesterday = new ChangeExpectedEndDateActitityInstance(
          task.getUUID(), DateUtil.backTo(today, 2));
      getCommandAPI().execute(dayBeforeYesterday);
    }

    updateInstanceVariable(instanceUUIDsP1, "text", "Hello", true);

    final int numberOfProcess2 = 5;
    final List<ProcessInstanceUUID> instanceUUIDsP2 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID2, numberOfProcess2, instanceUUIDsP2);

    for (int i = 0; i < numberOfProcess2; i++) {
      final TaskInstance task = getQueryRuntimeAPI().getTasks(instanceUUIDsP2.get(i)).iterator().next();
      final ChangeExpectedEndDateActitityInstance dayBeforeYesterday = new ChangeExpectedEndDateActitityInstance(
          task.getUUID(), DateUtil.backTo(today, 2));
      getCommandAPI().execute(dayBeforeYesterday);
    }

    updateInstanceVariable(instanceUUIDsP2, "text", "Hello", true);

    final Set<ProcessDefinitionUUID> exceptP1 = Collections.singleton(processUUID1);

    Collections.reverse(instanceUUIDsP2);
    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithOverdueTasksExcept("john", 0, 3, exceptP1,
            ProcessInstanceCriterion.INSTANCE_NUMBER_DESC);
    checkListContent(0, 3, 3, instanceUUIDsP2, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithOverdueTasksExcept("john", 3, 3,
        exceptP1, ProcessInstanceCriterion.INSTANCE_NUMBER_DESC);
    checkListContent(3, 3, 2, instanceUUIDsP2, retrievedInstances);

    getManagementAPI().removeJar("getExecJar.jar");
    getRuntimeAPI().deleteAllProcessInstances(processUUID1);
    getRuntimeAPI().deleteAllProcessInstances(processUUID2);
    getManagementAPI().disable(processUUID1);
    getManagementAPI().disable(processUUID2);
    getManagementAPI().deleteProcess(processUUID1);
    getManagementAPI().deleteProcess(processUUID2);
  }

  public void testGetLightParentProcessInstancesWithOverdueTasksExceptOrderByUUIDAsc() throws Exception {
    ProcessDefinition process1 = getProcess();
    ProcessDefinition process2 = getProcess2();

    process1 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process1));
    process2 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process2));

    final ProcessDefinitionUUID processUUID1 = process1.getUUID();
    final ProcessDefinitionUUID processUUID2 = process2.getUUID();

    final int numberOfProcess1 = 10;
    final List<ProcessInstanceUUID> instanceUUIDsP1 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID1, numberOfProcess1, instanceUUIDsP1);

    getManagementAPI().deployJar("getExecJar.jar", Misc.generateJar(ChangeExpectedEndDateActitityInstance.class));

    final Date today = new Date();
    for (int i = 0; i < numberOfProcess1; i++) {
      final TaskInstance task = getQueryRuntimeAPI().getTasks(instanceUUIDsP1.get(i)).iterator().next();
      final ChangeExpectedEndDateActitityInstance dayBeforeYesterday = new ChangeExpectedEndDateActitityInstance(
          task.getUUID(), DateUtil.backTo(today, 2));
      getCommandAPI().execute(dayBeforeYesterday);
    }

    updateInstanceVariable(instanceUUIDsP1, "text", "Hello", true);

    final int numberOfProcess2 = 5;
    final List<ProcessInstanceUUID> instanceUUIDsP2 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID2, numberOfProcess2, instanceUUIDsP2);

    for (int i = 0; i < numberOfProcess2; i++) {
      final TaskInstance task = getQueryRuntimeAPI().getTasks(instanceUUIDsP2.get(i)).iterator().next();
      final ChangeExpectedEndDateActitityInstance dayBeforeYesterday = new ChangeExpectedEndDateActitityInstance(
          task.getUUID(), DateUtil.backTo(today, 2));
      getCommandAPI().execute(dayBeforeYesterday);
    }

    updateInstanceVariable(instanceUUIDsP2, "text", "Hello", true);

    final Set<ProcessDefinitionUUID> exceptP1 = Collections.singleton(processUUID1);

    Collections.sort(instanceUUIDsP2, new ProcessInstanceUUIDComparator());
    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithOverdueTasksExcept("john", 0, 3, exceptP1,
            ProcessInstanceCriterion.INSTANCE_UUID_ASC);
    checkListContent(0, 3, 3, instanceUUIDsP2, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithOverdueTasksExcept("john", 3, 3,
        exceptP1, ProcessInstanceCriterion.INSTANCE_UUID_ASC);
    checkListContent(3, 3, 2, instanceUUIDsP2, retrievedInstances);

    getManagementAPI().removeJar("getExecJar.jar");
    getRuntimeAPI().deleteAllProcessInstances(processUUID1);
    getRuntimeAPI().deleteAllProcessInstances(processUUID2);
    getManagementAPI().disable(processUUID1);
    getManagementAPI().disable(processUUID2);
    getManagementAPI().deleteProcess(processUUID1);
    getManagementAPI().deleteProcess(processUUID2);
  }

  public void testGetLightParentProcessInstancesWithOverdueTasksExceptOrderByUUIDDesc() throws Exception {
    ProcessDefinition process1 = getProcess();
    ProcessDefinition process2 = getProcess2();

    process1 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process1));
    process2 = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process2));

    final ProcessDefinitionUUID processUUID1 = process1.getUUID();
    final ProcessDefinitionUUID processUUID2 = process2.getUUID();

    final int numberOfProcess1 = 10;
    final List<ProcessInstanceUUID> instanceUUIDsP1 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID1, numberOfProcess1, instanceUUIDsP1);

    getManagementAPI().deployJar("getExecJar.jar", Misc.generateJar(ChangeExpectedEndDateActitityInstance.class));

    final Date today = new Date();
    for (int i = 0; i < numberOfProcess1; i++) {
      final TaskInstance task = getQueryRuntimeAPI().getTasks(instanceUUIDsP1.get(i)).iterator().next();
      final ChangeExpectedEndDateActitityInstance dayBeforeYesterday = new ChangeExpectedEndDateActitityInstance(
          task.getUUID(), DateUtil.backTo(today, 2));
      getCommandAPI().execute(dayBeforeYesterday);
    }

    updateInstanceVariable(instanceUUIDsP1, "text", "Hello", true);

    final int numberOfProcess2 = 5;
    final List<ProcessInstanceUUID> instanceUUIDsP2 = new ArrayList<ProcessInstanceUUID>();
    instanciateProcesses(processUUID2, numberOfProcess2, instanceUUIDsP2);

    for (int i = 0; i < numberOfProcess2; i++) {
      final TaskInstance task = getQueryRuntimeAPI().getTasks(instanceUUIDsP2.get(i)).iterator().next();
      final ChangeExpectedEndDateActitityInstance dayBeforeYesterday = new ChangeExpectedEndDateActitityInstance(
          task.getUUID(), DateUtil.backTo(today, 2));
      getCommandAPI().execute(dayBeforeYesterday);
    }

    updateInstanceVariable(instanceUUIDsP2, "text", "Hello", true);

    final Set<ProcessDefinitionUUID> exceptP1 = Collections.singleton(processUUID1);

    Collections.sort(instanceUUIDsP2, new ProcessInstanceUUIDComparator());
    Collections.reverse(instanceUUIDsP2);
    List<LightProcessInstance> retrievedInstances = getQueryRuntimeAPI()
        .getLightParentProcessInstancesWithOverdueTasksExcept("john", 0, 3, exceptP1,
            ProcessInstanceCriterion.INSTANCE_UUID_DESC);
    checkListContent(0, 3, 3, instanceUUIDsP2, retrievedInstances);

    retrievedInstances = getQueryRuntimeAPI().getLightParentProcessInstancesWithOverdueTasksExcept("john", 3, 3,
        exceptP1, ProcessInstanceCriterion.INSTANCE_UUID_DESC);
    checkListContent(3, 3, 2, instanceUUIDsP2, retrievedInstances);

    getManagementAPI().removeJar("getExecJar.jar");
    getRuntimeAPI().deleteAllProcessInstances(processUUID1);
    getRuntimeAPI().deleteAllProcessInstances(processUUID2);
    getManagementAPI().disable(processUUID1);
    getManagementAPI().disable(processUUID2);
    getManagementAPI().deleteProcess(processUUID1);
    getManagementAPI().deleteProcess(processUUID2);
  }

  private <T> List<T> getListCopy(final List<T> instanceUUIDs) {
    final List<T> listCopy = new ArrayList<T>();
    listCopy.addAll(instanceUUIDs);
    return listCopy;
  }

  private void createProcessesWithaSubProcess(final int numberOfProcess,
      final List<ProcessDefinitionUUID> parentProcessUUIDs, final List<ProcessDefinitionUUID> subProcessUUIDs)
      throws DeploymentException {
    for (int i = 0; i < numberOfProcess; i++) {
      ProcessDefinition process = ProcessBuilder.createProcess("pProcess" + i, "1.0").addStringData("text")
          .addSubProcess("aSubProcess", "aSubProcess" + i).done();

      ProcessDefinition subProcess = ProcessBuilder.createProcess("aSubProcess" + i, "1.0").addHuman(getLogin())
          .addHumanTask("h", getLogin()).done();

      subProcess = getManagementAPI().deploy(getBusinessArchive(subProcess));
      process = getManagementAPI().deploy(getBusinessArchive(process));

      subProcessUUIDs.add(subProcess.getUUID());
      parentProcessUUIDs.add(process.getUUID());
    }
  }

  public void testGetLightParentProcessInstancesFromProcessUUIDsOrderByDefault() throws Exception {
    final int numberOfProcess = 3;
    final List<ProcessDefinitionUUID> parentProcessUUIDs = new ArrayList<ProcessDefinitionUUID>();
    final List<ProcessDefinitionUUID> subProcessUUIDs = new ArrayList<ProcessDefinitionUUID>();
    createProcessesWithaSubProcess(numberOfProcess, parentProcessUUIDs, subProcessUUIDs);

    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    final int numberOfInstances = 2;
    for (int i = 0; i < numberOfProcess; i++) {
      instanciateProcesses(parentProcessUUIDs.get(i), numberOfInstances, instanceUUIDs);
    }
    updateInstanceVariable(instanceUUIDs, "text", "Hello", true);

    final Set<ProcessDefinitionUUID> fromProcess = new HashSet<ProcessDefinitionUUID>();
    fromProcess.addAll(parentProcessUUIDs.subList(0, 2));
    fromProcess.addAll(subProcessUUIDs.subList(0, 2));

    final List<ProcessInstanceUUID> usedInstancesUUIDs = instanceUUIDs.subList(0, 4);

    List<LightProcessInstance> instances = getQueryRuntimeAPI().getLightParentProcessInstances(fromProcess, 0, 2,
        ProcessInstanceCriterion.DEFAULT);
    checkListContent(0, 2, 2, usedInstancesUUIDs, instances);

    instances = getQueryRuntimeAPI()
        .getLightParentProcessInstances(fromProcess, 2, 3, ProcessInstanceCriterion.DEFAULT);
    checkListContent(2, 3, 2, usedInstancesUUIDs, instances);

    getManagementAPI().delete(parentProcessUUIDs);
    getManagementAPI().delete(subProcessUUIDs);
  }

  public void testGetLightParentProcessInstancesFromProcessUUIDsOrderByLastUpdateAsc() throws Exception {
    final int numberOfProcess = 3;
    final List<ProcessDefinitionUUID> parentProcessUUIDs = new ArrayList<ProcessDefinitionUUID>();
    final List<ProcessDefinitionUUID> subProcessUUIDs = new ArrayList<ProcessDefinitionUUID>();
    createProcessesWithaSubProcess(numberOfProcess, parentProcessUUIDs, subProcessUUIDs);

    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    final int numberOfInstances = 2;
    for (int i = 0; i < numberOfProcess; i++) {
      instanciateProcesses(parentProcessUUIDs.get(i), numberOfInstances, instanceUUIDs);
    }
    updateInstanceVariable(instanceUUIDs, "text", "Hello", true);
    final List<ProcessInstanceUUID> usedInstancesUUIDs = instanceUUIDs.subList(0, 4);
    Collections.reverse(usedInstancesUUIDs);

    final Set<ProcessDefinitionUUID> fromProcess = new HashSet<ProcessDefinitionUUID>();
    fromProcess.addAll(parentProcessUUIDs.subList(0, 2));
    fromProcess.addAll(subProcessUUIDs.subList(0, 2));

    List<LightProcessInstance> instances = getQueryRuntimeAPI().getLightParentProcessInstances(fromProcess, 0, 2,
        ProcessInstanceCriterion.LAST_UPDATE_ASC);
    checkListContent(0, 2, 2, usedInstancesUUIDs, instances);

    instances = getQueryRuntimeAPI().getLightParentProcessInstances(fromProcess, 2, 3,
        ProcessInstanceCriterion.LAST_UPDATE_ASC);
    checkListContent(2, 3, 2, usedInstancesUUIDs, instances);

    getManagementAPI().delete(parentProcessUUIDs);
    getManagementAPI().delete(subProcessUUIDs);
  }

  public void testGetLightParentProcessInstancesFromProcessUUIDsOrderByLastUpdateDesc() throws Exception {
    final int numberOfProcess = 3;
    final List<ProcessDefinitionUUID> parentProcessUUIDs = new ArrayList<ProcessDefinitionUUID>();
    final List<ProcessDefinitionUUID> subProcessUUIDs = new ArrayList<ProcessDefinitionUUID>();
    createProcessesWithaSubProcess(numberOfProcess, parentProcessUUIDs, subProcessUUIDs);

    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    final int numberOfInstances = 2;
    for (int i = 0; i < numberOfProcess; i++) {
      instanciateProcesses(parentProcessUUIDs.get(i), numberOfInstances, instanceUUIDs);
    }
    updateInstanceVariable(instanceUUIDs, "text", "Hello", true);
    final List<ProcessInstanceUUID> usedInstancesUUIDs = instanceUUIDs.subList(0, 4);

    final Set<ProcessDefinitionUUID> fromProcess = new HashSet<ProcessDefinitionUUID>();
    fromProcess.addAll(parentProcessUUIDs.subList(0, 2));
    fromProcess.addAll(subProcessUUIDs.subList(0, 2));

    List<LightProcessInstance> instances = getQueryRuntimeAPI().getLightParentProcessInstances(fromProcess, 0, 2,
        ProcessInstanceCriterion.LAST_UPDATE_DESC);
    checkListContent(0, 2, 2, usedInstancesUUIDs, instances);

    instances = getQueryRuntimeAPI().getLightParentProcessInstances(fromProcess, 2, 3,
        ProcessInstanceCriterion.LAST_UPDATE_DESC);
    checkListContent(2, 3, 2, usedInstancesUUIDs, instances);

    getManagementAPI().delete(parentProcessUUIDs);
    getManagementAPI().delete(subProcessUUIDs);
  }

  public void testGetLightParentProcessInstancesFromProcessUUIDsOrderByStartedDateAsc() throws Exception {
    final int numberOfProcess = 3;
    final List<ProcessDefinitionUUID> parentProcessUUIDs = new ArrayList<ProcessDefinitionUUID>();
    final List<ProcessDefinitionUUID> subProcessUUIDs = new ArrayList<ProcessDefinitionUUID>();
    createProcessesWithaSubProcess(numberOfProcess, parentProcessUUIDs, subProcessUUIDs);

    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    final int numberOfInstances = 2;
    for (int i = 0; i < numberOfProcess; i++) {
      instanciateProcesses(parentProcessUUIDs.get(i), numberOfInstances, instanceUUIDs);
    }

    final List<ProcessInstanceUUID> usedInstancesUUIDs = instanceUUIDs.subList(0, 4);

    final Set<ProcessDefinitionUUID> fromProcess = new HashSet<ProcessDefinitionUUID>();
    fromProcess.addAll(parentProcessUUIDs.subList(0, 2));
    fromProcess.addAll(subProcessUUIDs.subList(0, 2));

    List<LightProcessInstance> instances = getQueryRuntimeAPI().getLightParentProcessInstances(fromProcess, 0, 2,
        ProcessInstanceCriterion.STARTED_DATE_ASC);
    checkListContent(0, 2, 2, usedInstancesUUIDs, instances);

    instances = getQueryRuntimeAPI().getLightParentProcessInstances(fromProcess, 2, 3,
        ProcessInstanceCriterion.STARTED_DATE_ASC);
    checkListContent(2, 3, 2, usedInstancesUUIDs, instances);

    getManagementAPI().delete(parentProcessUUIDs);
    getManagementAPI().delete(subProcessUUIDs);
  }

  public void testGetLightParentProcessInstancesFromProcessUUIDsOrderByStartedDateDesc() throws Exception {
    final int numberOfProcess = 3;
    final List<ProcessDefinitionUUID> parentProcessUUIDs = new ArrayList<ProcessDefinitionUUID>();
    final List<ProcessDefinitionUUID> subProcessUUIDs = new ArrayList<ProcessDefinitionUUID>();
    createProcessesWithaSubProcess(numberOfProcess, parentProcessUUIDs, subProcessUUIDs);

    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    final int numberOfInstances = 2;
    for (int i = 0; i < numberOfProcess; i++) {
      instanciateProcesses(parentProcessUUIDs.get(i), numberOfInstances, instanceUUIDs);
    }

    final List<ProcessInstanceUUID> usedInstancesUUIDs = instanceUUIDs.subList(0, 4);
    Collections.reverse(usedInstancesUUIDs);

    final Set<ProcessDefinitionUUID> fromProcess = new HashSet<ProcessDefinitionUUID>();
    fromProcess.addAll(parentProcessUUIDs.subList(0, 2));
    fromProcess.addAll(subProcessUUIDs.subList(0, 2));

    List<LightProcessInstance> instances = getQueryRuntimeAPI().getLightParentProcessInstances(fromProcess, 0, 2,
        ProcessInstanceCriterion.STARTED_DATE_DESC);
    checkListContent(0, 2, 2, usedInstancesUUIDs, instances);

    instances = getQueryRuntimeAPI().getLightParentProcessInstances(fromProcess, 2, 3,
        ProcessInstanceCriterion.STARTED_DATE_DESC);
    checkListContent(2, 3, 2, usedInstancesUUIDs, instances);

    getManagementAPI().delete(parentProcessUUIDs);
    getManagementAPI().delete(subProcessUUIDs);
  }

  public void testGetLightParentProcessInstancesFromProcessUUIDsOrderByEndedDateAsc() throws Exception {
    final int numberOfProcess = 3;
    final List<ProcessDefinitionUUID> parentProcessUUIDs = new ArrayList<ProcessDefinitionUUID>();
    final List<ProcessDefinitionUUID> subProcessUUIDs = new ArrayList<ProcessDefinitionUUID>();
    createProcessesWithaSubProcess(numberOfProcess, parentProcessUUIDs, subProcessUUIDs);

    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    final int numberOfInstances = 2;
    for (int i = 0; i < numberOfProcess; i++) {
      instanciateProcesses(parentProcessUUIDs.get(i), numberOfInstances, instanceUUIDs);
    }

    for (final ProcessInstanceUUID processInstanceUUID : instanceUUIDs) {
      final ProcessInstance instance = getQueryRuntimeAPI().getProcessInstance(processInstanceUUID);
      final Set<ProcessInstanceUUID> childrenUUIDs = instance.getChildrenInstanceUUID();
      assertEquals(1, childrenUUIDs.size());
      executeTask(childrenUUIDs.iterator().next(), "h");
      Thread.sleep(3);
    }

    final List<ProcessInstanceUUID> usedInstancesUUIDs = instanceUUIDs.subList(0, 4);

    final Set<ProcessDefinitionUUID> fromProcess = new HashSet<ProcessDefinitionUUID>();
    fromProcess.addAll(parentProcessUUIDs.subList(0, 2));
    fromProcess.addAll(subProcessUUIDs.subList(0, 2));

    List<LightProcessInstance> instances = getQueryRuntimeAPI().getLightParentProcessInstances(fromProcess, 0, 2,
        ProcessInstanceCriterion.ENDED_DATE_ASC);
    checkListContent(0, 2, 2, usedInstancesUUIDs, instances);

    instances = getQueryRuntimeAPI().getLightParentProcessInstances(fromProcess, 2, 3,
        ProcessInstanceCriterion.ENDED_DATE_ASC);
    checkListContent(2, 3, 2, usedInstancesUUIDs, instances);

    getManagementAPI().delete(parentProcessUUIDs);
    getManagementAPI().delete(subProcessUUIDs);
  }

  public void testGetLightParentProcessInstancesFromProcessUUIDsOrderByEndedDateDesc() throws Exception {
    final int numberOfProcess = 3;
    final List<ProcessDefinitionUUID> parentProcessUUIDs = new ArrayList<ProcessDefinitionUUID>();
    final List<ProcessDefinitionUUID> subProcessUUIDs = new ArrayList<ProcessDefinitionUUID>();
    createProcessesWithaSubProcess(numberOfProcess, parentProcessUUIDs, subProcessUUIDs);

    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    final int numberOfInstances = 2;
    for (int i = 0; i < numberOfProcess; i++) {
      instanciateProcesses(parentProcessUUIDs.get(i), numberOfInstances, instanceUUIDs);
    }

    for (final ProcessInstanceUUID processInstanceUUID : instanceUUIDs) {
      final ProcessInstance instance = getQueryRuntimeAPI().getProcessInstance(processInstanceUUID);
      final Set<ProcessInstanceUUID> childrenUUIDs = instance.getChildrenInstanceUUID();
      assertEquals(1, childrenUUIDs.size());
      executeTask(childrenUUIDs.iterator().next(), "h");
      Thread.sleep(3);
    }

    final List<ProcessInstanceUUID> usedInstancesUUIDs = instanceUUIDs.subList(0, 4);
    Collections.reverse(usedInstancesUUIDs);

    final Set<ProcessDefinitionUUID> fromProcess = new HashSet<ProcessDefinitionUUID>();
    fromProcess.addAll(parentProcessUUIDs.subList(0, 2));
    fromProcess.addAll(subProcessUUIDs.subList(0, 2));

    List<LightProcessInstance> instances = getQueryRuntimeAPI().getLightParentProcessInstances(fromProcess, 0, 2,
        ProcessInstanceCriterion.ENDED_DATE_DESC);
    checkListContent(0, 2, 2, usedInstancesUUIDs, instances);

    instances = getQueryRuntimeAPI().getLightParentProcessInstances(fromProcess, 2, 3,
        ProcessInstanceCriterion.ENDED_DATE_DESC);
    checkListContent(2, 3, 2, usedInstancesUUIDs, instances);

    getManagementAPI().delete(parentProcessUUIDs);
    getManagementAPI().delete(subProcessUUIDs);
  }

  public void testGetLightParentProcessInstancesFromProcessUUIDsOrderByInstanceNumberAsc() throws Exception {
    final int numberOfProcess = 3;
    final List<ProcessDefinitionUUID> parentProcessUUIDs = new ArrayList<ProcessDefinitionUUID>();
    final List<ProcessDefinitionUUID> subProcessUUIDs = new ArrayList<ProcessDefinitionUUID>();
    createProcessesWithaSubProcess(numberOfProcess, parentProcessUUIDs, subProcessUUIDs);

    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    final int numberOfInstances = 2;
    for (int i = 0; i < numberOfProcess; i++) {
      instanciateProcesses(parentProcessUUIDs.get(i), numberOfInstances, instanceUUIDs);
    }

    final Set<ProcessDefinitionUUID> fromProcess = new HashSet<ProcessDefinitionUUID>();
    fromProcess.addAll(parentProcessUUIDs.subList(0, 2));
    fromProcess.addAll(subProcessUUIDs.subList(0, 2));

    List<LightProcessInstance> instances = getQueryRuntimeAPI().getLightParentProcessInstances(fromProcess, 0, 2,
        ProcessInstanceCriterion.INSTANCE_NUMBER_ASC);
    assertEquals(2, instances.size());
    assertEquals(1, instances.get(0).getNb());
    assertTrue(parentProcessUUIDs.subList(0, 2).contains(instances.get(0).getProcessDefinitionUUID()));
    assertEquals(1, instances.get(1).getNb());
    assertTrue(parentProcessUUIDs.subList(0, 2).contains(instances.get(1).getProcessDefinitionUUID()));

    instances = getQueryRuntimeAPI().getLightParentProcessInstances(fromProcess, 2, 3,
        ProcessInstanceCriterion.INSTANCE_NUMBER_ASC);
    assertEquals(2, instances.size());
    assertEquals(2, instances.get(0).getNb());
    assertTrue(parentProcessUUIDs.subList(0, 2).contains(instances.get(0).getProcessDefinitionUUID()));
    assertEquals(2, instances.get(1).getNb());
    assertTrue(parentProcessUUIDs.subList(0, 2).contains(instances.get(1).getProcessDefinitionUUID()));

    getManagementAPI().delete(parentProcessUUIDs);
    getManagementAPI().delete(subProcessUUIDs);
  }

  public void testGetLightParentProcessInstancesFromProcessUUIDsOrderByInstanceNumberDesc() throws Exception {
    final int numberOfProcess = 3;
    final List<ProcessDefinitionUUID> parentProcessUUIDs = new ArrayList<ProcessDefinitionUUID>();
    final List<ProcessDefinitionUUID> subProcessUUIDs = new ArrayList<ProcessDefinitionUUID>();
    createProcessesWithaSubProcess(numberOfProcess, parentProcessUUIDs, subProcessUUIDs);

    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    final int numberOfInstances = 2;
    for (int i = 0; i < numberOfProcess; i++) {
      instanciateProcesses(parentProcessUUIDs.get(i), numberOfInstances, instanceUUIDs);
    }

    final Set<ProcessDefinitionUUID> fromProcess = new HashSet<ProcessDefinitionUUID>();
    fromProcess.addAll(parentProcessUUIDs.subList(0, 2));
    fromProcess.addAll(subProcessUUIDs.subList(0, 2));

    List<LightProcessInstance> instances = getQueryRuntimeAPI().getLightParentProcessInstances(fromProcess, 0, 2,
        ProcessInstanceCriterion.INSTANCE_NUMBER_DESC);
    assertEquals(2, instances.size());
    assertEquals(2, instances.get(0).getNb());
    assertEquals(2, instances.get(1).getNb());

    instances = getQueryRuntimeAPI().getLightParentProcessInstances(fromProcess, 2, 3,
        ProcessInstanceCriterion.INSTANCE_NUMBER_DESC);
    assertEquals(2, instances.size());
    assertEquals(1, instances.get(0).getNb());
    assertEquals(1, instances.get(1).getNb());

    getManagementAPI().delete(parentProcessUUIDs);
    getManagementAPI().delete(subProcessUUIDs);
  }

  public void testGetLightParentProcessInstancesFromProcessUUIDsOrderByInstanceUUIDAsc() throws Exception {
    final int numberOfProcess = 3;
    final List<ProcessDefinitionUUID> parentProcessUUIDs = new ArrayList<ProcessDefinitionUUID>();
    final List<ProcessDefinitionUUID> subProcessUUIDs = new ArrayList<ProcessDefinitionUUID>();
    createProcessesWithaSubProcess(numberOfProcess, parentProcessUUIDs, subProcessUUIDs);

    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    final int numberOfInstances = 2;
    for (int i = 0; i < numberOfProcess; i++) {
      instanciateProcesses(parentProcessUUIDs.get(i), numberOfInstances, instanceUUIDs);
    }

    final List<ProcessInstanceUUID> usedInstancesUUIDs = instanceUUIDs.subList(0, 4);
    Collections.sort(usedInstancesUUIDs, new ProcessInstanceUUIDComparator());

    final Set<ProcessDefinitionUUID> fromProcess = new HashSet<ProcessDefinitionUUID>();
    fromProcess.addAll(parentProcessUUIDs.subList(0, 2));
    fromProcess.addAll(subProcessUUIDs.subList(0, 2));

    List<LightProcessInstance> instances = getQueryRuntimeAPI().getLightParentProcessInstances(fromProcess, 0, 2,
        ProcessInstanceCriterion.INSTANCE_UUID_ASC);
    checkListContent(0, 2, 2, usedInstancesUUIDs, instances);

    instances = getQueryRuntimeAPI().getLightParentProcessInstances(fromProcess, 2, 3,
        ProcessInstanceCriterion.INSTANCE_UUID_ASC);
    checkListContent(2, 3, 2, usedInstancesUUIDs, instances);

    getManagementAPI().delete(parentProcessUUIDs);
    getManagementAPI().delete(subProcessUUIDs);
  }

  public void testGetLightParentProcessInstancesFromProcessUUIDsOrderByInstanceUUIDDesc() throws Exception {
    final int numberOfProcess = 3;
    final List<ProcessDefinitionUUID> parentProcessUUIDs = new ArrayList<ProcessDefinitionUUID>();
    final List<ProcessDefinitionUUID> subProcessUUIDs = new ArrayList<ProcessDefinitionUUID>();
    createProcessesWithaSubProcess(numberOfProcess, parentProcessUUIDs, subProcessUUIDs);

    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    final int numberOfInstances = 2;
    for (int i = 0; i < numberOfProcess; i++) {
      instanciateProcesses(parentProcessUUIDs.get(i), numberOfInstances, instanceUUIDs);
    }

    final List<ProcessInstanceUUID> usedInstancesUUIDs = instanceUUIDs.subList(0, 4);
    Collections.sort(usedInstancesUUIDs, new ProcessInstanceUUIDComparator());
    Collections.reverse(usedInstancesUUIDs);

    final Set<ProcessDefinitionUUID> fromProcess = new HashSet<ProcessDefinitionUUID>();
    fromProcess.addAll(parentProcessUUIDs.subList(0, 2));
    fromProcess.addAll(subProcessUUIDs.subList(0, 2));

    List<LightProcessInstance> instances = getQueryRuntimeAPI().getLightParentProcessInstances(fromProcess, 0, 2,
        ProcessInstanceCriterion.INSTANCE_UUID_DESC);
    checkListContent(0, 2, 2, usedInstancesUUIDs, instances);

    instances = getQueryRuntimeAPI().getLightParentProcessInstances(fromProcess, 2, 3,
        ProcessInstanceCriterion.INSTANCE_UUID_DESC);
    checkListContent(2, 3, 2, usedInstancesUUIDs, instances);

    getManagementAPI().delete(parentProcessUUIDs);
    getManagementAPI().delete(subProcessUUIDs);
  }

  public void testGetLightParentProcessInstancesExceptOrderByDefault() throws Exception {
    final int numberOfProcess = 4;
    final List<ProcessDefinitionUUID> parentProcessUUIDs = new ArrayList<ProcessDefinitionUUID>();
    final List<ProcessDefinitionUUID> subProcessUUIDs = new ArrayList<ProcessDefinitionUUID>();
    createProcessesWithaSubProcess(numberOfProcess, parentProcessUUIDs, subProcessUUIDs);

    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    final int numberOfInstances = 2;
    for (int i = 0; i < numberOfProcess; i++) {
      instanciateProcesses(parentProcessUUIDs.get(i), numberOfInstances, instanceUUIDs);
    }
    updateInstanceVariable(instanceUUIDs, "text", "Hello", true);

    final Set<ProcessDefinitionUUID> exceptProcesses = new HashSet<ProcessDefinitionUUID>();
    exceptProcesses.addAll(parentProcessUUIDs.subList(2, parentProcessUUIDs.size()));
    exceptProcesses.addAll(subProcessUUIDs.subList(2, subProcessUUIDs.size()));

    final List<ProcessInstanceUUID> usedInstancesUUIDs = instanceUUIDs.subList(0, 4);

    List<LightProcessInstance> instances = getQueryRuntimeAPI().getLightParentProcessInstancesExcept(exceptProcesses,
        0, 2, ProcessInstanceCriterion.DEFAULT);
    checkListContent(0, 2, 2, usedInstancesUUIDs, instances);

    instances = getQueryRuntimeAPI().getLightParentProcessInstancesExcept(exceptProcesses, 2, 3,
        ProcessInstanceCriterion.DEFAULT);
    checkListContent(2, 3, 2, usedInstancesUUIDs, instances);

    getManagementAPI().delete(parentProcessUUIDs);
    getManagementAPI().delete(subProcessUUIDs);
  }

  public void testGetLightParentProcessInstancesExceptOrderByLastUpdateAsc() throws Exception {
    final int numberOfProcess = 3;
    final List<ProcessDefinitionUUID> parentProcessUUIDs = new ArrayList<ProcessDefinitionUUID>();
    final List<ProcessDefinitionUUID> subProcessUUIDs = new ArrayList<ProcessDefinitionUUID>();
    createProcessesWithaSubProcess(numberOfProcess, parentProcessUUIDs, subProcessUUIDs);

    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    final int numberOfInstances = 2;
    for (int i = 0; i < numberOfProcess; i++) {
      instanciateProcesses(parentProcessUUIDs.get(i), numberOfInstances, instanceUUIDs);
    }
    updateInstanceVariable(instanceUUIDs, "text", "Hello", true);
    final List<ProcessInstanceUUID> usedInstancesUUIDs = instanceUUIDs.subList(0, 4);
    Collections.reverse(usedInstancesUUIDs);

    final Set<ProcessDefinitionUUID> exceptProcesses = new HashSet<ProcessDefinitionUUID>();
    exceptProcesses.addAll(parentProcessUUIDs.subList(2, parentProcessUUIDs.size()));
    exceptProcesses.addAll(subProcessUUIDs.subList(2, subProcessUUIDs.size()));

    List<LightProcessInstance> instances = getQueryRuntimeAPI().getLightParentProcessInstancesExcept(exceptProcesses,
        0, 2, ProcessInstanceCriterion.LAST_UPDATE_ASC);
    checkListContent(0, 2, 2, usedInstancesUUIDs, instances);

    instances = getQueryRuntimeAPI().getLightParentProcessInstancesExcept(exceptProcesses, 2, 3,
        ProcessInstanceCriterion.LAST_UPDATE_ASC);
    checkListContent(2, 3, 2, usedInstancesUUIDs, instances);

    getManagementAPI().delete(parentProcessUUIDs);
    getManagementAPI().delete(subProcessUUIDs);
  }

  public void testGetLightParentProcessInstancesExceptOrderByLastUpdateDesc() throws Exception {
    final int numberOfProcess = 3;
    final List<ProcessDefinitionUUID> parentProcessUUIDs = new ArrayList<ProcessDefinitionUUID>();
    final List<ProcessDefinitionUUID> subProcessUUIDs = new ArrayList<ProcessDefinitionUUID>();
    createProcessesWithaSubProcess(numberOfProcess, parentProcessUUIDs, subProcessUUIDs);

    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    final int numberOfInstances = 2;
    for (int i = 0; i < numberOfProcess; i++) {
      instanciateProcesses(parentProcessUUIDs.get(i), numberOfInstances, instanceUUIDs);
    }
    updateInstanceVariable(instanceUUIDs, "text", "Hello", true);
    final List<ProcessInstanceUUID> usedInstancesUUIDs = instanceUUIDs.subList(0, 4);

    final Set<ProcessDefinitionUUID> exceptProcesses = new HashSet<ProcessDefinitionUUID>();
    exceptProcesses.addAll(parentProcessUUIDs.subList(2, parentProcessUUIDs.size()));
    exceptProcesses.addAll(subProcessUUIDs.subList(2, subProcessUUIDs.size()));

    List<LightProcessInstance> instances = getQueryRuntimeAPI().getLightParentProcessInstancesExcept(exceptProcesses,
        0, 2, ProcessInstanceCriterion.LAST_UPDATE_DESC);
    checkListContent(0, 2, 2, usedInstancesUUIDs, instances);

    instances = getQueryRuntimeAPI().getLightParentProcessInstancesExcept(exceptProcesses, 2, 3,
        ProcessInstanceCriterion.LAST_UPDATE_DESC);
    checkListContent(2, 3, 2, usedInstancesUUIDs, instances);

    getManagementAPI().delete(parentProcessUUIDs);
    getManagementAPI().delete(subProcessUUIDs);
  }

  public void testGetLightParentProcessInstancesExceptOrderByStartedDateAsc() throws Exception {
    final int numberOfProcess = 3;
    final List<ProcessDefinitionUUID> parentProcessUUIDs = new ArrayList<ProcessDefinitionUUID>();
    final List<ProcessDefinitionUUID> subProcessUUIDs = new ArrayList<ProcessDefinitionUUID>();
    createProcessesWithaSubProcess(numberOfProcess, parentProcessUUIDs, subProcessUUIDs);

    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    final int numberOfInstances = 2;
    for (int i = 0; i < numberOfProcess; i++) {
      instanciateProcesses(parentProcessUUIDs.get(i), numberOfInstances, instanceUUIDs);
    }

    final List<ProcessInstanceUUID> usedInstancesUUIDs = instanceUUIDs.subList(0, 4);

    final Set<ProcessDefinitionUUID> exceptProcesses = new HashSet<ProcessDefinitionUUID>();
    exceptProcesses.addAll(parentProcessUUIDs.subList(2, parentProcessUUIDs.size()));
    exceptProcesses.addAll(subProcessUUIDs.subList(2, subProcessUUIDs.size()));

    List<LightProcessInstance> instances = getQueryRuntimeAPI().getLightParentProcessInstancesExcept(exceptProcesses,
        0, 2, ProcessInstanceCriterion.STARTED_DATE_ASC);
    checkListContent(0, 2, 2, usedInstancesUUIDs, instances);

    instances = getQueryRuntimeAPI().getLightParentProcessInstancesExcept(exceptProcesses, 2, 3,
        ProcessInstanceCriterion.STARTED_DATE_ASC);
    checkListContent(2, 3, 2, usedInstancesUUIDs, instances);

    getManagementAPI().delete(parentProcessUUIDs);
    getManagementAPI().delete(subProcessUUIDs);
  }

  public void testGetLightParentProcessInstancesExceptOrderByStartedDateDesc() throws Exception {
    final int numberOfProcess = 3;
    final List<ProcessDefinitionUUID> parentProcessUUIDs = new ArrayList<ProcessDefinitionUUID>();
    final List<ProcessDefinitionUUID> subProcessUUIDs = new ArrayList<ProcessDefinitionUUID>();
    createProcessesWithaSubProcess(numberOfProcess, parentProcessUUIDs, subProcessUUIDs);

    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    final int numberOfInstances = 2;
    for (int i = 0; i < numberOfProcess; i++) {
      instanciateProcesses(parentProcessUUIDs.get(i), numberOfInstances, instanceUUIDs);
    }

    final List<ProcessInstanceUUID> usedInstancesUUIDs = instanceUUIDs.subList(0, 4);
    Collections.reverse(usedInstancesUUIDs);

    final Set<ProcessDefinitionUUID> exceptProcesses = new HashSet<ProcessDefinitionUUID>();
    exceptProcesses.addAll(parentProcessUUIDs.subList(2, parentProcessUUIDs.size()));
    exceptProcesses.addAll(subProcessUUIDs.subList(2, subProcessUUIDs.size()));

    List<LightProcessInstance> instances = getQueryRuntimeAPI().getLightParentProcessInstancesExcept(exceptProcesses,
        0, 2, ProcessInstanceCriterion.STARTED_DATE_DESC);
    checkListContent(0, 2, 2, usedInstancesUUIDs, instances);

    instances = getQueryRuntimeAPI().getLightParentProcessInstancesExcept(exceptProcesses, 2, 3,
        ProcessInstanceCriterion.STARTED_DATE_DESC);
    checkListContent(2, 3, 2, usedInstancesUUIDs, instances);

    getManagementAPI().delete(parentProcessUUIDs);
    getManagementAPI().delete(subProcessUUIDs);
  }

  public void testGetLightParentProcessInstancesExceptOrderByEndedDateAsc() throws Exception {
    final int numberOfProcess = 3;
    final List<ProcessDefinitionUUID> parentProcessUUIDs = new ArrayList<ProcessDefinitionUUID>();
    final List<ProcessDefinitionUUID> subProcessUUIDs = new ArrayList<ProcessDefinitionUUID>();
    createProcessesWithaSubProcess(numberOfProcess, parentProcessUUIDs, subProcessUUIDs);

    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    final int numberOfInstances = 2;
    for (int i = 0; i < numberOfProcess; i++) {
      instanciateProcesses(parentProcessUUIDs.get(i), numberOfInstances, instanceUUIDs);
    }

    for (final ProcessInstanceUUID processInstanceUUID : instanceUUIDs) {
      final ProcessInstance instance = getQueryRuntimeAPI().getProcessInstance(processInstanceUUID);
      final Set<ProcessInstanceUUID> childrenUUIDs = instance.getChildrenInstanceUUID();
      assertEquals(1, childrenUUIDs.size());
      executeTask(childrenUUIDs.iterator().next(), "h");
      Thread.sleep(3);
    }

    final List<ProcessInstanceUUID> usedInstancesUUIDs = instanceUUIDs.subList(0, 4);

    final Set<ProcessDefinitionUUID> exceptProcesses = new HashSet<ProcessDefinitionUUID>();
    exceptProcesses.addAll(parentProcessUUIDs.subList(2, parentProcessUUIDs.size()));
    exceptProcesses.addAll(subProcessUUIDs.subList(2, subProcessUUIDs.size()));

    List<LightProcessInstance> instances = getQueryRuntimeAPI().getLightParentProcessInstancesExcept(exceptProcesses,
        0, 2, ProcessInstanceCriterion.ENDED_DATE_ASC);
    checkListContent(0, 2, 2, usedInstancesUUIDs, instances);

    instances = getQueryRuntimeAPI().getLightParentProcessInstancesExcept(exceptProcesses, 2, 3,
        ProcessInstanceCriterion.ENDED_DATE_ASC);
    checkListContent(2, 3, 2, usedInstancesUUIDs, instances);

    getManagementAPI().delete(parentProcessUUIDs);
    getManagementAPI().delete(subProcessUUIDs);
  }

  public void testGetLightParentProcessInstancesExceptOrderByEndedDateDesc() throws Exception {
    final int numberOfProcess = 3;
    final List<ProcessDefinitionUUID> parentProcessUUIDs = new ArrayList<ProcessDefinitionUUID>();
    final List<ProcessDefinitionUUID> subProcessUUIDs = new ArrayList<ProcessDefinitionUUID>();
    createProcessesWithaSubProcess(numberOfProcess, parentProcessUUIDs, subProcessUUIDs);

    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    final int numberOfInstances = 2;
    for (int i = 0; i < numberOfProcess; i++) {
      instanciateProcesses(parentProcessUUIDs.get(i), numberOfInstances, instanceUUIDs);
    }

    for (final ProcessInstanceUUID processInstanceUUID : instanceUUIDs) {
      final ProcessInstance instance = getQueryRuntimeAPI().getProcessInstance(processInstanceUUID);
      final Set<ProcessInstanceUUID> childrenUUIDs = instance.getChildrenInstanceUUID();
      assertEquals(1, childrenUUIDs.size());
      executeTask(childrenUUIDs.iterator().next(), "h");
      Thread.sleep(3);
    }

    final List<ProcessInstanceUUID> usedInstancesUUIDs = instanceUUIDs.subList(0, 4);
    Collections.reverse(usedInstancesUUIDs);

    final Set<ProcessDefinitionUUID> exceptProcesses = new HashSet<ProcessDefinitionUUID>();
    exceptProcesses.addAll(parentProcessUUIDs.subList(2, parentProcessUUIDs.size()));
    exceptProcesses.addAll(subProcessUUIDs.subList(2, subProcessUUIDs.size()));

    List<LightProcessInstance> instances = getQueryRuntimeAPI().getLightParentProcessInstancesExcept(exceptProcesses,
        0, 2, ProcessInstanceCriterion.ENDED_DATE_DESC);
    checkListContent(0, 2, 2, usedInstancesUUIDs, instances);

    instances = getQueryRuntimeAPI().getLightParentProcessInstancesExcept(exceptProcesses, 2, 3,
        ProcessInstanceCriterion.ENDED_DATE_DESC);
    checkListContent(2, 3, 2, usedInstancesUUIDs, instances);

    getManagementAPI().delete(parentProcessUUIDs);
    getManagementAPI().delete(subProcessUUIDs);
  }

  public void testGetLightParentProcessInstancesExceptOrderByInstanceNumberAsc() throws Exception {
    final int numberOfProcess = 3;
    final List<ProcessDefinitionUUID> parentProcessUUIDs = new ArrayList<ProcessDefinitionUUID>();
    final List<ProcessDefinitionUUID> subProcessUUIDs = new ArrayList<ProcessDefinitionUUID>();
    createProcessesWithaSubProcess(numberOfProcess, parentProcessUUIDs, subProcessUUIDs);

    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    final int numberOfInstances = 2;
    for (int i = 0; i < numberOfProcess; i++) {
      instanciateProcesses(parentProcessUUIDs.get(i), numberOfInstances, instanceUUIDs);
    }

    final Set<ProcessDefinitionUUID> exceptProcesses = new HashSet<ProcessDefinitionUUID>();
    exceptProcesses.addAll(parentProcessUUIDs.subList(2, parentProcessUUIDs.size()));
    exceptProcesses.addAll(subProcessUUIDs.subList(2, subProcessUUIDs.size()));

    List<LightProcessInstance> instances = getQueryRuntimeAPI().getLightParentProcessInstancesExcept(exceptProcesses,
        0, 2, ProcessInstanceCriterion.INSTANCE_NUMBER_ASC);
    assertEquals(2, instances.size());
    assertEquals(1, instances.get(0).getNb());
    assertTrue(parentProcessUUIDs.subList(0, 2).contains(instances.get(0).getProcessDefinitionUUID()));
    assertEquals(1, instances.get(1).getNb());
    assertTrue(parentProcessUUIDs.subList(0, 2).contains(instances.get(1).getProcessDefinitionUUID()));

    instances = getQueryRuntimeAPI().getLightParentProcessInstancesExcept(exceptProcesses, 2, 3,
        ProcessInstanceCriterion.INSTANCE_NUMBER_ASC);
    assertEquals(2, instances.size());
    assertEquals(2, instances.get(0).getNb());
    assertEquals(2, instances.get(1).getNb());

    getManagementAPI().delete(parentProcessUUIDs);
    getManagementAPI().delete(subProcessUUIDs);
  }

  public void testGetLightParentProcessInstancesExceptOrderByInstanceNumberDesc() throws Exception {
    final int numberOfProcess = 3;
    final List<ProcessDefinitionUUID> parentProcessUUIDs = new ArrayList<ProcessDefinitionUUID>();
    final List<ProcessDefinitionUUID> subProcessUUIDs = new ArrayList<ProcessDefinitionUUID>();
    createProcessesWithaSubProcess(numberOfProcess, parentProcessUUIDs, subProcessUUIDs);

    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    final int numberOfInstances = 2;
    for (int i = 0; i < numberOfProcess; i++) {
      instanciateProcesses(parentProcessUUIDs.get(i), numberOfInstances, instanceUUIDs);
    }

    final Set<ProcessDefinitionUUID> exceptProcesses = new HashSet<ProcessDefinitionUUID>();
    exceptProcesses.addAll(parentProcessUUIDs.subList(2, parentProcessUUIDs.size()));
    exceptProcesses.addAll(subProcessUUIDs.subList(2, subProcessUUIDs.size()));

    List<LightProcessInstance> instances = getQueryRuntimeAPI().getLightParentProcessInstancesExcept(exceptProcesses,
        0, 2, ProcessInstanceCriterion.INSTANCE_NUMBER_DESC);
    assertEquals(2, instances.size());
    assertEquals(2, instances.get(0).getNb());
    assertTrue(parentProcessUUIDs.subList(0, 2).contains(instances.get(0).getProcessDefinitionUUID()));
    assertEquals(2, instances.get(1).getNb());
    assertTrue(parentProcessUUIDs.subList(0, 2).contains(instances.get(1).getProcessDefinitionUUID()));

    instances = getQueryRuntimeAPI().getLightParentProcessInstancesExcept(exceptProcesses, 2, 3,
        ProcessInstanceCriterion.INSTANCE_NUMBER_DESC);
    assertEquals(2, instances.size());
    assertEquals(1, instances.get(0).getNb());
    assertEquals(1, instances.get(1).getNb());

    getManagementAPI().delete(parentProcessUUIDs);
    getManagementAPI().delete(subProcessUUIDs);
  }

  public void testGetLightParentProcessInstancesExceptOrderByInstanceUUIDAsc() throws Exception {
    final int numberOfProcess = 3;
    final List<ProcessDefinitionUUID> parentProcessUUIDs = new ArrayList<ProcessDefinitionUUID>();
    final List<ProcessDefinitionUUID> subProcessUUIDs = new ArrayList<ProcessDefinitionUUID>();
    createProcessesWithaSubProcess(numberOfProcess, parentProcessUUIDs, subProcessUUIDs);

    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    final int numberOfInstances = 2;
    for (int i = 0; i < numberOfProcess; i++) {
      instanciateProcesses(parentProcessUUIDs.get(i), numberOfInstances, instanceUUIDs);
    }

    final List<ProcessInstanceUUID> usedInstancesUUIDs = instanceUUIDs.subList(0, 4);
    Collections.sort(usedInstancesUUIDs, new ProcessInstanceUUIDComparator());

    final Set<ProcessDefinitionUUID> exceptProcesses = new HashSet<ProcessDefinitionUUID>();
    exceptProcesses.addAll(parentProcessUUIDs.subList(2, parentProcessUUIDs.size()));
    exceptProcesses.addAll(subProcessUUIDs.subList(2, subProcessUUIDs.size()));

    List<LightProcessInstance> instances = getQueryRuntimeAPI().getLightParentProcessInstancesExcept(exceptProcesses,
        0, 2, ProcessInstanceCriterion.INSTANCE_UUID_ASC);
    checkListContent(0, 2, 2, usedInstancesUUIDs, instances);

    instances = getQueryRuntimeAPI().getLightParentProcessInstancesExcept(exceptProcesses, 2, 3,
        ProcessInstanceCriterion.INSTANCE_UUID_ASC);
    checkListContent(2, 3, 2, usedInstancesUUIDs, instances);

    getManagementAPI().delete(parentProcessUUIDs);
    getManagementAPI().delete(subProcessUUIDs);
  }

  public void testGetLightParentProcessInstancesExceptOrderByInstanceUUIDDesc() throws Exception {
    final int numberOfProcess = 3;
    final List<ProcessDefinitionUUID> parentProcessUUIDs = new ArrayList<ProcessDefinitionUUID>();
    final List<ProcessDefinitionUUID> subProcessUUIDs = new ArrayList<ProcessDefinitionUUID>();
    createProcessesWithaSubProcess(numberOfProcess, parentProcessUUIDs, subProcessUUIDs);

    final List<ProcessInstanceUUID> instanceUUIDs = new ArrayList<ProcessInstanceUUID>();
    final int numberOfInstances = 2;
    for (int i = 0; i < numberOfProcess; i++) {
      instanciateProcesses(parentProcessUUIDs.get(i), numberOfInstances, instanceUUIDs);
    }

    final List<ProcessInstanceUUID> usedInstancesUUIDs = instanceUUIDs.subList(0, 4);
    Collections.sort(usedInstancesUUIDs, new ProcessInstanceUUIDComparator());
    Collections.reverse(usedInstancesUUIDs);

    final Set<ProcessDefinitionUUID> exceptProcesses = new HashSet<ProcessDefinitionUUID>();
    exceptProcesses.addAll(parentProcessUUIDs.subList(2, parentProcessUUIDs.size()));
    exceptProcesses.addAll(subProcessUUIDs.subList(2, subProcessUUIDs.size()));

    List<LightProcessInstance> instances = getQueryRuntimeAPI().getLightParentProcessInstancesExcept(exceptProcesses,
        0, 2, ProcessInstanceCriterion.INSTANCE_UUID_DESC);
    checkListContent(0, 2, 2, usedInstancesUUIDs, instances);

    instances = getQueryRuntimeAPI().getLightParentProcessInstancesExcept(exceptProcesses, 2, 3,
        ProcessInstanceCriterion.INSTANCE_UUID_DESC);
    checkListContent(2, 3, 2, usedInstancesUUIDs, instances);

    getManagementAPI().delete(parentProcessUUIDs);
    getManagementAPI().delete(subProcessUUIDs);
  }

  public void testCallingGetProcessIsntanceDontChangeLastUpdate() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("tcgpidclu", "1.0").addAttachment("att2").done();
    process = getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(process));

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(process.getUUID());
    final ProcessInstance firstCallInstance = getQueryRuntimeAPI().getProcessInstance(instanceUUID);
    Thread.sleep(100);
    final ProcessInstance secondCallInstance = getQueryRuntimeAPI().getProcessInstance(instanceUUID);

    assertEquals(firstCallInstance.getLastUpdate(), secondCallInstance.getLastUpdate());

    getManagementAPI().deleteProcess(process.getUUID());
  }

  public void testGetLightParentProcessInstancesWithActiveUserExcept() throws BonitaException {
    final Set<ProcessDefinitionUUID> processes = Collections.emptySet();
    final List<LightProcessInstance> list = getQueryRuntimeAPI().getLightParentProcessInstancesWithActiveUserExcept(
        getLogin(), 0, 10, processes);
    assertTrue(list.isEmpty());
  }

  public void testNumberOfParentProcessWhenNoCategoryMatchesWithRunningProcesses() throws BonitaException {
    final ProcessDefinition procWithoutCatDef = ProcessBuilder.createProcess("ProcWithoutCat", "1.0")
        .addSystemTask("auto1").done();

    final ProcessDefinition procWithCatDef = ProcessBuilder.createProcess("ProcWithCat", "1.0").addSystemTask("auto1")
        .done();

    final ProcessDefinition definition1 = getManagementAPI().deploy(getBusinessArchive(procWithCatDef));
    final ProcessDefinition definition2 = getManagementAPI().deploy(getBusinessArchive(procWithoutCatDef));

    getWebAPI().addCategory("cat", null, null, null);
    final LightProcessDefinition process = getWebAPI().setProcessCategories(definition1.getUUID(),
        Collections.singleton("cat"));
    assertEquals("cat", process.getCategoryNames().iterator().next());

    final Integer number = getQueryRuntimeAPI().getNumberOfParentProcessInstancesWithInvolvedUserAndCategory(
        getLogin(), "cat", Collections.singleton(definition2.getUUID()));
    assertEquals(Integer.valueOf(0), number);

    getWebAPI().deleteCategories(Collections.singleton("cat"));
    getManagementAPI().deleteAllProcesses();
  }

  public void testNumberOfParentProcessWhenNoCategoryMatchesWithRunningProcesses1() throws BonitaException {
    final ProcessDefinition procWithoutCatDef = ProcessBuilder.createProcess("ProcWithoutCat", "1.0")
        .addSystemTask("auto1").done();

    final ProcessDefinition procWithCatDef = ProcessBuilder.createProcess("ProcWithCat", "1.0").addSystemTask("auto1")
        .done();

    final ProcessDefinition definition1 = getManagementAPI().deploy(getBusinessArchive(procWithCatDef));
    getManagementAPI().deploy(getBusinessArchive(procWithoutCatDef));

    getWebAPI().addCategory("cat", null, null, null);
    final LightProcessDefinition process = getWebAPI().setProcessCategories(definition1.getUUID(),
        Collections.singleton("cat"));
    assertEquals("cat", process.getCategoryNames().iterator().next());

    final Integer number = getQueryRuntimeAPI().getNumberOfParentProcessInstancesWithInvolvedUserAndCategory(
        getLogin(), "toto");
    assertEquals(Integer.valueOf(0), number);

    getWebAPI().deleteCategories(Collections.singleton("cat"));
    getManagementAPI().deleteAllProcesses();
  }

}
