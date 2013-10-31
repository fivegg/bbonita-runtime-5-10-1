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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ow2.bonita.APITestCase;
import org.ow2.bonita.command.AddExpiredWebTemporaryTokenCommand;
import org.ow2.bonita.command.ListExpiredWebTemporaryTokensCommand;
import org.ow2.bonita.facade.def.majorElement.ActivityDefinition.JoinType;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.Category;
import org.ow2.bonita.facade.runtime.Document;
import org.ow2.bonita.facade.runtime.Label;
import org.ow2.bonita.facade.runtime.WebTemporaryToken;
import org.ow2.bonita.facade.runtime.command.GetProcessInstancesActivitiesCommand;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.light.LightActivityInstance;
import org.ow2.bonita.light.LightProcessInstance;
import org.ow2.bonita.light.LightTaskInstance;
import org.ow2.bonita.search.DocumentResult;
import org.ow2.bonita.search.DocumentSearchBuilder;
import org.ow2.bonita.search.index.DocumentIndex;
import org.ow2.bonita.util.AccessorUtil;
import org.ow2.bonita.util.BonitaException;
import org.ow2.bonita.util.BusinessArchiveFactory;
import org.ow2.bonita.util.Misc;
import org.ow2.bonita.util.ProcessBuilder;

/**
 * @author Charles Souillard, Matthieu Chaffotte, Christophe Leroy
 */
public class WebAPITest extends APITestCase {

  private Set<String> getSetFromString(final String... array) {
    final Set<String> set = new HashSet<String>();
    for (final String s : array) {
      set.add(s);
    }
    return set;
  }

  public void testGetProcessInstancesStepsCommand() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("process", "1.0")
    .addHuman(getLogin())
    .addHumanTask("t1", getLogin())
    .addHumanTask("t2", getLogin())
    .addDecisionNode("decision")
    .addJoinType(JoinType.AND)
    .addTransition("t1", "decision")
    .addTransition("t2", "decision")
    .done();

    process = getManagementAPI().deploy(getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final ProcessInstanceUUID instance1 = getRuntimeAPI().instantiateProcess(processUUID);
    final ProcessInstanceUUID instance2 = getRuntimeAPI().instantiateProcess(processUUID);

    final Set<ProcessInstanceUUID> instanceUUIDs = new HashSet<ProcessInstanceUUID>();
    instanceUUIDs.add(instance1);
    instanceUUIDs.add(instance2);

    Map<ProcessInstanceUUID, List<LightActivityInstance>> activities = getCommandAPI().execute(new GetProcessInstancesActivitiesCommand(instanceUUIDs,false));

    assertEquals(2, activities.size());
    assertEquals(2, activities.get(instance1).size());
    assertEquals(2, activities.get(instance2).size());

    Collection<LightTaskInstance> tasks = getQueryRuntimeAPI().getLightTaskList(instance1, ActivityState.READY);
    assertEquals(2, tasks.size());
    getRuntimeAPI().executeTask(tasks.iterator().next().getUUID(), true);

    activities = getCommandAPI().execute(new GetProcessInstancesActivitiesCommand(instanceUUIDs,false));

    assertEquals(1, activities.get(instance1).size());
    assertEquals(2, activities.get(instance2).size());

    Thread.sleep(2000);

    tasks = getQueryRuntimeAPI().getLightTaskList(instance1, ActivityState.READY);
    final LightTaskInstance lastTaskOfInstance1 = tasks.iterator().next();

    assertEquals(1, tasks.size());
    getRuntimeAPI().executeTask(lastTaskOfInstance1.getUUID(), true);

    activities = getCommandAPI().execute(new GetProcessInstancesActivitiesCommand(instanceUUIDs,false));

    assertEquals(1, activities.get(instance1).size());
    assertEquals(lastTaskOfInstance1.getUUID(), activities.get(instance1).get(0).getUUID());
    assertEquals(2, activities.get(instance2).size());

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testListActivitiesWithProcessFullAuto() throws Exception {


    final ProcessDefinition theFullAutoProcess = ProcessBuilder.createProcess("FullyAutomatic", "1.0").addSystemTask("auto1").done();

    AccessorUtil.getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(theFullAutoProcess));

    final ProcessDefinitionUUID processUUID = theFullAutoProcess.getUUID();

    // Start a case.
    final ProcessInstanceUUID instanceUUID = AccessorUtil.getRuntimeAPI().instantiateProcess(processUUID);

    final Set<ProcessInstanceUUID> instances = new HashSet<ProcessInstanceUUID>();
    instances.add(instanceUUID);
    final Map<ProcessInstanceUUID, List<LightActivityInstance>> lightActivityInstances = AccessorUtil.getCommandAPI().execute(new GetProcessInstancesActivitiesCommand(instances,true));
    assertEquals(1, lightActivityInstances.size());
    assertNotNull(lightActivityInstances.get(instanceUUID));
    assertEquals(1,lightActivityInstances.get(instanceUUID).size());

    //Clean.
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testSyncUUIDs() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("process", "1.0")
    .addHuman(getLogin())
    .addHumanTask("t1", getLogin())
    .done();
    process = getManagementAPI().deploy(getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final Set<ProcessInstanceUUID> instanceUUIDs = new HashSet<ProcessInstanceUUID>();
    instanceUUIDs.add(getRuntimeAPI().instantiateProcess(processUUID));
    instanceUUIDs.add(getRuntimeAPI().instantiateProcess(processUUID));
    instanceUUIDs.add(getRuntimeAPI().instantiateProcess(processUUID));
    instanceUUIDs.add(getRuntimeAPI().instantiateProcess(processUUID));
    instanceUUIDs.add(getRuntimeAPI().instantiateProcess(processUUID));

    getWebAPI().addLabel("inbox", getLogin(), null, null, null, true, true, null, null, 0, true);
    getWebAPI().addCasesToLabel(getLogin(), "inbox", instanceUUIDs);

    getWebAPI().deletePhantomCases();

    final Set<String> labels = new HashSet<String>();
    labels.add("inbox");

    assertEquals(instanceUUIDs.size(), getWebAPI().getCases(getLogin(), labels).size());

    final Iterator<ProcessInstanceUUID> it = instanceUUIDs.iterator();
    getRuntimeAPI().deleteProcessInstance(it.next());
    getRuntimeAPI().deleteProcessInstance(it.next());

    assertEquals(instanceUUIDs.size() - 2, getQueryRuntimeAPI().getLightProcessInstances().size());
    assertEquals(instanceUUIDs.size() - 2, getQueryRuntimeAPI().getProcessInstances().size());

    assertEquals(instanceUUIDs.size(), getWebAPI().getCases(getLogin(), labels).size());
    getWebAPI().deletePhantomCases();

    assertEquals(instanceUUIDs.size() - 2, getWebAPI().getCases(getLogin(), labels).size());

    getRuntimeAPI().deleteAllProcessInstances(processUUID);

    assertEquals(instanceUUIDs.size() - 2, getWebAPI().getCases(getLogin(), labels).size());
    getWebAPI().deletePhantomCases();

    assertEquals(0, getWebAPI().getCases(getLogin(), labels).size());

    getWebAPI().removeLabel(getLogin(), "inbox");

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testLabels() throws Exception {
    getWebAPI().addLabel("sysLabel1", "user1", "editableCss", "readOnlyCss", "previewCss", true, true, null, null, 1, true);
    getWebAPI().addLabel("sysLabel2", "user1", "editableCss", "readOnlyCss", "previewCss", true, true, null, null, 2, true);

    getWebAPI().addLabel("userLabel1", "user1", "editableCss", "readOnlyCss", "previewCss", true, true, null, null, 2, false);
    getWebAPI().addLabel("userLabel2", "user1", "editableCss", "readOnlyCss", "previewCss", true, true, null, null, 1, false);

    getWebAPI().addLabel("userLabel1", "user2", "editableCss", "readOnlyCss", "previewCss", true, true, null, null, 1, false);

    final Label sysLabel1 = getWebAPI().getLabel("user1", "sysLabel1");
    assertEquals("user1", sysLabel1.getOwnerName());
    assertEquals("sysLabel1", sysLabel1.getName());
    assertEquals("editableCss", sysLabel1.getEditableCSSStyleName());
    assertEquals("readOnlyCss", sysLabel1.getReadonlyCSSStyleName());
    assertEquals("previewCss", sysLabel1.getPreviewCSSStyleName());
    assertEquals(true, sysLabel1.isVisible());
    assertEquals(true, sysLabel1.isHasToBeDisplayed());
    assertNull(sysLabel1.getIconCSSStyle());
    assertEquals(0, getWebAPI().getCases("user1", getSetFromString("sysLabel1")).size());
    assertEquals(1, sysLabel1.getDisplayOrder());
    assertEquals(true, sysLabel1.isSystemLabel());

    assertEquals("sysLabel1", getWebAPI().getLabel("user1", "sysLabel1").getName());
    assertEquals("userLabel2", getWebAPI().getLabel("user1", "userLabel2").getName());
    assertEquals("user1", getWebAPI().getLabel("user1", "userLabel1").getOwnerName());

    assertEquals(4, getWebAPI().getLabels("user1").size());
    assertEquals(1, getWebAPI().getLabels("user2").size());
    assertEquals(0, getWebAPI().getLabels("wrong").size());

    assertEquals(2, getWebAPI().getSystemLabels("user1").size());
    assertEquals("sysLabel1", getWebAPI().getSystemLabels("user1").get(0).getName());
    assertEquals("sysLabel2", getWebAPI().getSystemLabels("user1").get(1).getName());
    assertEquals(0, getWebAPI().getSystemLabels("user2").size());

    assertEquals(2, getWebAPI().getUserCustomLabels("user1").size());
    assertEquals("userLabel2", getWebAPI().getUserCustomLabels("user1").get(0).getName());
    assertEquals("userLabel1", getWebAPI().getUserCustomLabels("user1").get(1).getName());
    assertEquals(1, getWebAPI().getUserCustomLabels("user2").size());

    getWebAPI().updateLabelCSS("user1", "sysLabel1", "new1", "new2", "new3");
    assertEquals("new1", getWebAPI().getLabel("user1", "sysLabel1").getEditableCSSStyleName());
    assertEquals("new2", getWebAPI().getLabel("user1", "sysLabel1").getPreviewCSSStyleName());
    assertEquals("new3", getWebAPI().getLabel("user1", "sysLabel1").getReadonlyCSSStyleName());

    getWebAPI().updateLabelName("user1", "sysLabel1", "newLabelName");
    assertNull(getWebAPI().getLabel("user1", "sysLabel1"));
    assertNotNull(getWebAPI().getLabel("user1", "newLabelName"));
    assertEquals("newLabelName", getWebAPI().getLabel("user1", "newLabelName").getName());
    assertEquals("new1", getWebAPI().getLabel("user1", "newLabelName").getEditableCSSStyleName());

    getWebAPI().updateLabelVisibility("user1", "newLabelName", false);
    assertFalse(getWebAPI().getLabel("user1", "newLabelName").isVisible());


    ProcessDefinition process = ProcessBuilder.createProcess("p", "1.0")
    .addHuman(getLogin())
    .addHumanTask("t", getLogin())
    .done();

    process = getManagementAPI().deploy(getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final ProcessInstanceUUID case1 = getRuntimeAPI().instantiateProcess(processUUID);
    final ProcessInstanceUUID case2 = getRuntimeAPI().instantiateProcess(processUUID);
    final ProcessInstanceUUID case3 = getRuntimeAPI().instantiateProcess(processUUID);
    final Set<ProcessInstanceUUID> caseList = new HashSet<ProcessInstanceUUID>();
    caseList.add(case1);
    caseList.add(case2);
    caseList.add(case3);

    getWebAPI().addCasesToLabel("user1", "newLabelName", caseList);
    assertEquals(3, getWebAPI().getCases("user1", getSetFromString("newLabelName")).size());
    assertEquals(caseList, getWebAPI().getCases("user1", getSetFromString("newLabelName")));

    assertEquals(1, getWebAPI().getCaseLabels("user1", case1).size());
    assertEquals(1, getWebAPI().getCaseLabels("user1", case2).size());
    assertEquals(1, getWebAPI().getCaseLabels("user1", case3).size());
    assertEquals("newLabelName", getWebAPI().getCaseLabels("user1", case1).iterator().next().getName());

    Map<ProcessInstanceUUID, Set<Label>> casesLabels = getWebAPI().getCasesLabels("user1", caseList);
    assertEquals(3, casesLabels.size());
    assertEquals(1, casesLabels.get(case1).size());

    getWebAPI().addCasesToLabel("user1", "userLabel2", caseList);
    assertEquals(2, getWebAPI().getCaseLabels("user1", case1).size());
    assertEquals(2, getWebAPI().getCaseLabels("user1", case2).size());
    assertEquals(2, getWebAPI().getCaseLabels("user1", case3).size());

    casesLabels = getWebAPI().getCasesLabels("user1", caseList);
    assertEquals(3, casesLabels.size());
    assertEquals(2, casesLabels.get(case1).size());

    getWebAPI().removeCasesFromLabel("user1", "newLabelName", null);
    assertEquals(3, getWebAPI().getCases("user1", getSetFromString("newLabelName")).size());
    assertEquals(caseList, getWebAPI().getCases("user1", getSetFromString("newLabelName")));

    final Set<ProcessInstanceUUID> casesToRemove = new HashSet<ProcessInstanceUUID>();

    getWebAPI().removeCasesFromLabel("user1", "newLabelName", casesToRemove);
    assertEquals(3, getWebAPI().getCases("user1", getSetFromString("newLabelName")).size());
    assertEquals(caseList, getWebAPI().getCases("user1", getSetFromString("newLabelName")));

    casesToRemove.add(case2);
    casesToRemove.add(case3);

    getWebAPI().removeCasesFromLabel("user1", "newLabelName", casesToRemove);

    assertEquals(1, getWebAPI().getCases("user1", getSetFromString("newLabelName")).size());
    assertEquals(case1, getWebAPI().getCases("user1", getSetFromString("newLabelName")).iterator().next());

    //check again
    getWebAPI().removeCasesFromLabel("user1", "newLabelName", casesToRemove);
    assertEquals(1, getWebAPI().getCases("user1", getSetFromString("newLabelName")).size());
    assertEquals(case1, getWebAPI().getCases("user1", getSetFromString("newLabelName")).iterator().next());


    getWebAPI().removeLabel("user1", "newLabelName");
    assertNull(getWebAPI().getLabel("user1", "newLabelName"));
    getWebAPI().removeLabel("user1", "sysLabel2");
    getWebAPI().removeLabel("user1", "userLabel1");
    getWebAPI().removeLabel("user1", "userLabel2");
    getWebAPI().removeLabel("user2", "userLabel1");
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testLabels2() throws BonitaException {
    final String ownerName = "john";
    final String inbox = "inbox";
    final String dummy = "dummy";

    getWebAPI().addLabel(inbox, ownerName, "editableCss", "readOnlyCss", "previewCss", true, true, null, null, 1, true);
    getWebAPI().addLabel(dummy, ownerName, "editableCss", "readOnlyCss", "previewCss", true, true, null, null, 2, true);

    ProcessDefinition process = ProcessBuilder.createProcess("p", "1.0")
    .addHuman(getLogin())
    .addHumanTask("t", getLogin())
    .done();

    process = getManagementAPI().deploy(getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final ProcessInstanceUUID case1 = getRuntimeAPI().instantiateProcess(processUUID);

    final Set<ProcessInstanceUUID> cases = new HashSet<ProcessInstanceUUID>();
    cases.add(case1);

    getWebAPI().addCasesToLabel(ownerName, inbox, cases);

    final Set<String> labels = new HashSet<String>();
    labels.add(inbox);

    final Set<ProcessInstanceUUID> caseUUIDs = getWebAPI().getCases(ownerName, labels);
    assertEquals(1, caseUUIDs.size());

    final List<LightProcessInstance> instances = getWebAPI().getLightProcessInstances(ownerName, labels, 0, 20);
    assertEquals(1, instances.size());

    assertEquals(1, getWebAPI().getCaseLabels(ownerName, case1).size());

    labels.clear();
    labels.add(dummy);
    assertEquals(0, getWebAPI().getCases(ownerName, labels).size());

    getWebAPI().removeLabel(ownerName, inbox);
    getWebAPI().removeLabel(ownerName, dummy);

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testDeleteAllCases() throws Exception {
    final String ownerName = "john";
    final String inbox = "inbox";
    final String unread = "unread";

    getWebAPI().addLabel(inbox, ownerName, "editableCss", "readOnlyCss", "previewCss", true, true, null, null, 1, true);
    getWebAPI().addLabel(unread, ownerName, "editableCss", "readOnlyCss", "previewCss", true, true, null, null, 2, true);

    final Set<ProcessInstanceUUID> cases = new HashSet<ProcessInstanceUUID>();

    ProcessDefinition process = ProcessBuilder.createProcess("p", "1.0")
    .addHuman(getLogin())
    .addHumanTask("t", getLogin())
    .done();

    process = getManagementAPI().deploy(getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final ProcessInstanceUUID case1 = getRuntimeAPI().instantiateProcess(processUUID);
    final ProcessInstanceUUID case2 = getRuntimeAPI().instantiateProcess(processUUID);
    final ProcessInstanceUUID case3 = getRuntimeAPI().instantiateProcess(processUUID);
    final ProcessInstanceUUID case4 = getRuntimeAPI().instantiateProcess(processUUID);
    final ProcessInstanceUUID case5 = getRuntimeAPI().instantiateProcess(processUUID);
    final ProcessInstanceUUID case6 = getRuntimeAPI().instantiateProcess(processUUID);

    cases.add(case1);
    cases.add(case2);
    cases.add(case3);
    cases.add(case4);
    cases.add(case5);
    cases.add(case6);

    final Collection<String> labelNames = new ArrayList<String>();
    labelNames.add(inbox);
    labelNames.add(unread);

    getWebAPI().addCasesToLabel(ownerName, inbox, cases);
    getWebAPI().addCasesToLabel(ownerName, unread, cases);

    assertEquals(cases.size(), getWebAPI().getCasesNumber(ownerName, Arrays.asList(inbox), 100).get(inbox).intValue());
    assertEquals(cases.size(), getWebAPI().getCasesNumber(ownerName, Arrays.asList(unread), 100).get(unread).intValue());

    Map<String, Integer> result = getWebAPI().getCasesNumber(ownerName, labelNames, 100);
    assertEquals(Integer.valueOf(cases.size()), result.get(inbox));
    assertEquals(Integer.valueOf(cases.size()), result.get(unread));

    getWebAPI().deleteAllCases();

    assertEquals(0, getWebAPI().getCasesNumber(ownerName, Arrays.asList(inbox), 100).get(inbox).intValue());
    assertEquals(0, getWebAPI().getCasesNumber(ownerName, Arrays.asList(unread), 100).get(unread).intValue());

    result = getWebAPI().getCasesNumber(ownerName, labelNames, 100);
    assertEquals(Integer.valueOf(0), result.get(inbox));
    assertEquals(Integer.valueOf(0), result.get(unread));

    getWebAPI().removeLabel(ownerName, inbox);
    getWebAPI().removeLabel(ownerName, unread);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testRemove() throws Exception {
    final String ownerName = "john";
    final String inbox = "inbox";
    final String unread = "unread";
    final String mycases = "mycases";

    getWebAPI().addLabel(inbox, ownerName, "editableCss", "readOnlyCss", "previewCss", true, true, null, null, 1, true);
    getWebAPI().addLabel(unread, ownerName, "editableCss", "readOnlyCss", "previewCss", true, true, null, null, 2, true);
    getWebAPI().addLabel(mycases, ownerName, "editableCss", "readOnlyCss", "previewCss", true, true, null, null, 3, true);


    ProcessDefinition process = ProcessBuilder.createProcess("p", "1.0")
    .addHuman(getLogin())
    .addHumanTask("t", getLogin())
    .done();

    process = getManagementAPI().deploy(getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final ProcessInstanceUUID case1 = getRuntimeAPI().instantiateProcess(processUUID);

    final Set<ProcessInstanceUUID> cases = new HashSet<ProcessInstanceUUID>();
    cases.add(case1);

    getWebAPI().addCasesToLabel(ownerName, inbox, cases);
    getWebAPI().addCasesToLabel(ownerName, unread, cases);
    getWebAPI().addCasesToLabel(ownerName, mycases, cases);

    assertEquals(1, getWebAPI().getCasesNumber(ownerName, Arrays.asList(inbox), 100).get(inbox).intValue());
    assertEquals(1, getWebAPI().getCasesNumber(ownerName, Arrays.asList(unread), 100).get(unread).intValue());
    assertEquals(1, getWebAPI().getCasesNumber(ownerName, Arrays.asList(mycases), 100).get(mycases).intValue());

    final Collection<String> labelNames = new ArrayList<String>();
    labelNames.add(inbox);
    labelNames.add(mycases);

    final Map<String, Integer> result = getWebAPI().getCasesNumber(ownerName, unread, labelNames, 100);
    assertEquals(Integer.valueOf(1), result.get(inbox));
    assertEquals(Integer.valueOf(1), result.get(mycases));

    assertEquals(1, getWebAPI().getCasesNumber(ownerName, inbox, Arrays.asList(unread), 100).get(unread).intValue());
    assertEquals(1, getWebAPI().getCasesNumber(ownerName, inbox, Arrays.asList(mycases), 100).get(mycases).intValue());
    assertEquals(1, getWebAPI().getCasesNumber(ownerName, mycases, Arrays.asList(unread), 100).get(unread).intValue());

    getWebAPI().removeCasesFromLabel(ownerName, inbox, cases);
    assertEquals(0, getWebAPI().getCasesNumber(ownerName, Arrays.asList(inbox), 100).get(inbox).intValue());
    assertEquals(1, getWebAPI().getCasesNumber(ownerName, Arrays.asList(unread), 100).get(unread).intValue());
    assertEquals(1, getWebAPI().getCasesNumber(ownerName, Arrays.asList(mycases), 100).get(mycases).intValue());

    final Set<String> labels = new HashSet<String>();
    labels.add(inbox);
    assertEquals(0, getWebAPI().getCases(ownerName, labels).size());

    getWebAPI().removeLabel(ownerName, unread);
    assertEquals(0, getWebAPI().getCasesNumber(ownerName, Arrays.asList(inbox), 100).get(inbox).intValue());
    assertEquals(0, getWebAPI().getCasesNumber(ownerName, Arrays.asList(unread), 100).get(unread).intValue());
    assertEquals(1, getWebAPI().getCasesNumber(ownerName, Arrays.asList(mycases), 100).get(mycases).intValue());

    final Collection<String> removeLabels = new ArrayList<String>();
    removeLabels.add(inbox);
    removeLabels.add(mycases);
    removeLabels.add(unread);
    getWebAPI().removeLabels(ownerName, removeLabels);
    assertEquals(0, getWebAPI().getCasesNumber(ownerName, Arrays.asList(inbox), 100).get(inbox).intValue());
    assertEquals(0, getWebAPI().getCasesNumber(ownerName, Arrays.asList(unread), 100).get(unread).intValue());
    assertEquals(0, getWebAPI().getCasesNumber(ownerName, Arrays.asList(mycases), 100).get(mycases).intValue());

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetCasesNumber() throws Exception {
    final String ownerName = "john";
    final String inbox = "inbox";
    final String unread = "unread";

    getWebAPI().addLabel(inbox, ownerName, "editableCss", "readOnlyCss", "previewCss", true, true, null, null, 1, true);
    getWebAPI().addLabel(unread, ownerName, "editableCss", "readOnlyCss", "previewCss", true, true, null, null, 2, true);

    ProcessDefinition process = ProcessBuilder.createProcess("p", "1.0")
    .addHuman(getLogin())
    .addHumanTask("t", getLogin())
    .done();

    process = getManagementAPI().deploy(getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final ProcessInstanceUUID case1 = getRuntimeAPI().instantiateProcess(processUUID);
    final ProcessInstanceUUID case2 = getRuntimeAPI().instantiateProcess(processUUID);
    final ProcessInstanceUUID case3 = getRuntimeAPI().instantiateProcess(processUUID);
    final ProcessInstanceUUID case4 = getRuntimeAPI().instantiateProcess(processUUID);
    final ProcessInstanceUUID case5 = getRuntimeAPI().instantiateProcess(processUUID);
    final ProcessInstanceUUID case6 = getRuntimeAPI().instantiateProcess(processUUID);

    final Set<ProcessInstanceUUID> label1Cases = new HashSet<ProcessInstanceUUID>();
    label1Cases.add(case1);
    label1Cases.add(case2);
    label1Cases.add(case3);
    label1Cases.add(case4);
    label1Cases.add(case5);

    final Set<ProcessInstanceUUID> label2Cases = new HashSet<ProcessInstanceUUID>();
    label2Cases.add(new ProcessInstanceUUID(case1.getValue()));
    label2Cases.add(case3);
    label2Cases.add(case4);
    label2Cases.add(case6);

    getWebAPI().addCasesToLabel(ownerName, inbox, label1Cases);
    getWebAPI().addCasesToLabel(ownerName, unread, label2Cases);

    assertEquals(5, getWebAPI().getCasesNumber(ownerName, Arrays.asList(inbox), 100).get(inbox).intValue());
    assertEquals(-3, getWebAPI().getCasesNumber(ownerName, Arrays.asList(inbox), 3).get(inbox).intValue());
    assertEquals(5, getWebAPI().getCasesNumber(ownerName, Arrays.asList(inbox)).get(inbox).intValue());
    assertEquals(4, getWebAPI().getCasesNumber(ownerName, Arrays.asList(unread), 100).get(unread).intValue());

    assertEquals(3, getWebAPI().getCasesNumber(ownerName, inbox, Arrays.asList(unread), 100).get(unread).intValue());

    getWebAPI().removeCasesFromLabel(ownerName, inbox, label1Cases);
    assertEquals(0, getWebAPI().getCasesNumber(ownerName, Arrays.asList(inbox), 100).get(inbox).intValue());

    getWebAPI().removeLabel(ownerName, unread);
    assertEquals(0, getWebAPI().getCasesNumber(ownerName, Arrays.asList(unread), 100).get(unread).intValue());

    getWebAPI().deleteAllCases();
    getWebAPI().removeLabel(ownerName, inbox);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testDeleteEntities() throws Exception {
    getWebAPI().addLabel("label1", "user1", "editableCss", "readOnlyCss", "previewCss", true, true, null, null, 1, true);
    getWebAPI().addLabel("label2", "user1", "editableCss", "readOnlyCss", "previewCss", true, true, null, null, 2, true);

    ProcessDefinition process = ProcessBuilder.createProcess("p", "1.0")
    .addHuman(getLogin())
    .addHumanTask("t", getLogin())
    .done();

    process = getManagementAPI().deploy(getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final ProcessInstanceUUID case1 = getRuntimeAPI().instantiateProcess(processUUID);
    final ProcessInstanceUUID case2 = getRuntimeAPI().instantiateProcess(processUUID);
    final ProcessInstanceUUID case3 = getRuntimeAPI().instantiateProcess(processUUID);
    final ProcessInstanceUUID case4 = getRuntimeAPI().instantiateProcess(processUUID);

    final Set<ProcessInstanceUUID> label1Cases = new HashSet<ProcessInstanceUUID>();
    label1Cases.add(case1);
    label1Cases.add(case2);
    label1Cases.add(case3);

    final Set<ProcessInstanceUUID> label2Cases = new HashSet<ProcessInstanceUUID>();
    label2Cases.add(case2);
    label2Cases.add(case3);
    label2Cases.add(case4);

    getWebAPI().addCasesToLabel("user1", "label1", label1Cases);
    getWebAPI().addCasesToLabel("user1", "label2", label2Cases);

    assertEquals(label1Cases, getWebAPI().getCases("user1", getSetFromString("label1")));
    assertEquals(label2Cases, getWebAPI().getCases("user1", getSetFromString("label2")));

    final Set<ProcessInstanceUUID> removedCases = new HashSet<ProcessInstanceUUID>();

    removedCases.add(case2);
    getWebAPI().removeAllCasesFromLabels(removedCases);
    label1Cases.remove(case2);
    label2Cases.remove(case2);
    assertEquals(label1Cases, getWebAPI().getCases("user1", getSetFromString("label1")));
    assertEquals(label2Cases, getWebAPI().getCases("user1", getSetFromString("label2")));

    removedCases.clear();
    removedCases.add(case1);
    removedCases.add(case4);
    getWebAPI().removeAllCasesFromLabels(removedCases);
    label1Cases.remove(case1);
    label2Cases.remove(case4);
    assertEquals(label1Cases, getWebAPI().getCases("user1", getSetFromString("label1")));
    assertEquals(label2Cases, getWebAPI().getCases("user1", getSetFromString("label2")));

    removedCases.clear();
    removedCases.add(case3);
    getWebAPI().removeAllCasesFromLabels(removedCases);
    label1Cases.remove(case3);
    label2Cases.remove(case3);
    assertEquals(label1Cases, getWebAPI().getCases("user1", getSetFromString("label1")));
    assertEquals(label2Cases, getWebAPI().getCases("user1", getSetFromString("label2")));

    getWebAPI().removeLabel("user1", "label1");
    getWebAPI().removeLabel("user1", "label2");
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGenerateTemporaryToken() throws Exception {
    final String token = getWebAPI().generateTemporaryToken("an encrypted value");
    assertNotNull(token);
    assertFalse(token.equals(""));
    try{
      getWebAPI().generateTemporaryToken(null);
      fail("Null is not allowed as input argument");
    } catch (final Exception e) {
      // OK
    }

    try{
      getWebAPI().generateTemporaryToken("");
      fail("Empty string is not allowed as input argument");
    } catch (final Exception e) {
      // OK
    }
  }
  
  public void testGenerateTemporaryTokenWithKeyContainingSeparatorCaracter() throws Exception {
    final String token = getWebAPI().generateTemporaryToken("S1vLdVPgXc9/yZSehaOp8A==");
    assertNotNull(token);
    assertFalse(token.equals(""));
  }

  public void testGetIdentityKeyFromToken() throws Exception {
    final String validIdentityKey = "an encrypted value";
    final String token = getWebAPI().generateTemporaryToken(validIdentityKey);
    String storedIdentityKey = getWebAPI().getIdentityKeyFromTemporaryToken(token);
    assertEquals(validIdentityKey, storedIdentityKey);

    // try to reuse same token
    storedIdentityKey = getWebAPI().getIdentityKeyFromTemporaryToken(token);
    assertNull(storedIdentityKey);
  }
  
  public void testGetIdentityKeyContainingEncodedCaracterFromToken() throws Exception {
	    final String validIdentityKey = "vn%2BN3qPsdyrZ7B14";
	    final String token = getWebAPI().generateTemporaryToken(validIdentityKey);
	    final String storedIdentityKey = getWebAPI().getIdentityKeyFromTemporaryToken(token);
	    assertEquals(validIdentityKey, storedIdentityKey);
  }

  public void testExpiredTokensAreDeleted() throws Exception {
    final int nbOfExpiredTokens = 11;
    getManagementAPI().deployJar("tokenCommand.jar", Misc.generateJar(AddExpiredWebTemporaryTokenCommand.class, ListExpiredWebTemporaryTokensCommand.class));
    getCommandAPI().execute(new AddExpiredWebTemporaryTokenCommand(nbOfExpiredTokens));
    Set<WebTemporaryToken> expiredTokens = getCommandAPI().execute(new ListExpiredWebTemporaryTokensCommand());
    assertEquals(nbOfExpiredTokens, expiredTokens.size());

    final String validIdentityKey = "an encrypted value";
    final String token1 = getWebAPI().generateTemporaryToken(validIdentityKey);
    final String token2 = getWebAPI().generateTemporaryToken(validIdentityKey);

    String storedIdentityKey = getWebAPI().getIdentityKeyFromTemporaryToken(token1);
    assertEquals(validIdentityKey, storedIdentityKey);

    // expired tokens must have been removed
    expiredTokens = getCommandAPI().execute(new ListExpiredWebTemporaryTokensCommand());
    assertEquals(0, expiredTokens.size());

    storedIdentityKey = getWebAPI().getIdentityKeyFromTemporaryToken(token2);
    assertEquals(validIdentityKey, storedIdentityKey);

    // try to reuse same token
    storedIdentityKey = getWebAPI().getIdentityKeyFromTemporaryToken(token1);
    assertNull(storedIdentityKey);
    storedIdentityKey = getWebAPI().getIdentityKeyFromTemporaryToken(token2);
    assertNull(storedIdentityKey);
    getManagementAPI().removeJar("tokenCommand.jar");
  }
  
  public void testDeleteCategories() throws Exception {
    final ProcessDefinition processWithCategory = ProcessBuilder.createProcess("pWithCategory", "1.0").addCategory("bonita").addSystemTask("start").addSystemTask("end").addTransition("start", "end").done();
    
    getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(processWithCategory));
    Set<Category> categories = getWebAPI().getAllCategories();
    assertNotNull(categories);
    assertEquals(1, categories.size());
    
    final Set<String> categoriesName = new HashSet<String>();
    for (final Category category : categories) {
      categoriesName.add(category.getName());
    }
    
    getWebAPI().deleteCategories(categoriesName);
    categories = getWebAPI().getAllCategories();
    assertNotNull(categories);
    assertEquals(0, categories.size());
    
    getManagementAPI().deleteProcess(processWithCategory.getUUID());
  }
  
  public void testAddProcessDocumentTemplate() throws Exception {
      //create a process
      final ProcessDefinition definition = ProcessBuilder.createProcess("process", "1.0")
      .addHuman(getLogin())
      .addHumanTask("step1", getLogin())
      .done();
      final ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(definition));
      //test
      final String content = "<html><body>minitemplate</body></html>";
      final Document document = getWebAPI().addProcessDocumentTemplate("myTemplate", process.getUUID(), "template.html", "plain/html", content.getBytes());             
      final byte[] actualContent = getQueryRuntimeAPI().getDocumentContent(document.getUUID());
      assertEquals(content, new String(actualContent));      
      getManagementAPI().deleteProcess(process.getUUID());
  } 
  
  public void testGetProcessDocumentTemplates() throws Exception {
      //create a process
      final ProcessDefinition definition = ProcessBuilder.createProcess("process", "1.0")
      .addHuman(getLogin())
      .addHumanTask("step1", getLogin())
      .done();
      final ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(definition)); 
      //create a second process
      final ProcessDefinition definition2 = ProcessBuilder.createProcess("process2", "1.0")
      .addHuman(getLogin())
      .addHumanTask("step1", getLogin())
      .done();
      final ProcessDefinition process2 = getManagementAPI().deploy(getBusinessArchive(definition2));       
      //create a template for each process
      final String content = "<html><body>minitemplate</body></html>";
      getWebAPI().addProcessDocumentTemplate("myTemplate", process.getUUID(), "template.html", "plain/html", content.getBytes());     
      final String content2 = "<html><body>minitemplate2</body></html>";
      getWebAPI().addProcessDocumentTemplate("myTemplate2", process2.getUUID(), "template2.html", "plain/html", content2.getBytes());           
      //test
      final List<Document> documents =  getWebAPI().getProcessDocumentTemplates(process.getUUID());      
      final List<Document> documents2 =  getWebAPI().getProcessDocumentTemplates(process2.getUUID());
      assertEquals(1, documents.size());      
      assertEquals(1, documents2.size());
      
      //ensure that meta documents are not retrieved while getting process documents
      final DocumentSearchBuilder builder = new DocumentSearchBuilder();
      builder.criterion(DocumentIndex.PROCESS_DEFINITION_UUID).equalsTo(process.getUUID().getValue());
      final DocumentResult documentResult = getQueryRuntimeAPI().searchDocuments(builder, 0, 10);
      assertEquals(0, documentResult.getCount());
      assertEquals(0, documentResult.getDocuments().size());
      
      getManagementAPI().deleteProcess(process.getUUID());
      getManagementAPI().deleteProcess(process2.getUUID());
  }  
}
