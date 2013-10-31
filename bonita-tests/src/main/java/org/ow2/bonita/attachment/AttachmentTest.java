/**
 * Copyright (C) 2009-2010  BonitaSoft S.A.
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
package org.ow2.bonita.attachment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.bonitasoft.connectors.bonita.SetVarConnector;
import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.def.element.AttachmentDefinition;
import org.ow2.bonita.facade.def.element.BusinessArchive;
import org.ow2.bonita.facade.def.element.HookDefinition.Event;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.exception.BonitaInternalException;
import org.ow2.bonita.facade.exception.InstanceNotFoundException;
import org.ow2.bonita.facade.runtime.ActivityInstance;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.AttachmentInstance;
import org.ow2.bonita.facade.runtime.Document;
import org.ow2.bonita.facade.runtime.InitialAttachment;
import org.ow2.bonita.facade.runtime.InstanceState;
import org.ow2.bonita.facade.runtime.ProcessInstance;
import org.ow2.bonita.facade.runtime.TaskInstance;
import org.ow2.bonita.facade.runtime.command.WebDeleteDocumentsOfProcessCommand;
import org.ow2.bonita.facade.runtime.command.WebDeleteProcessCommand;
import org.ow2.bonita.facade.runtime.impl.InitialAttachmentImpl;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.search.DocumentResult;
import org.ow2.bonita.search.DocumentSearchBuilder;
import org.ow2.bonita.search.index.DocumentIndex;
import org.ow2.bonita.util.BusinessArchiveFactory;
import org.ow2.bonita.util.Misc;
import org.ow2.bonita.util.ProcessBuilder;

/**
 * @author Charles Souillard
 */
public class AttachmentTest extends APITestCase {

  public void testBigAttachmentDefinition() throws Exception {
      final long l = 40000;
      final long expectedMinSize = 1000 * 1000;
      final File f = generatePropertiesFile(l);

      final long fileSize = f.length();
      assertTrue(fileSize > expectedMinSize);
      ProcessDefinition process = ProcessBuilder.createProcess("p", "1.0")
      .addAttachment("big", f.getName())
      .addSystemTask("t")
      .done();

      final Map<String, byte[]> resources = new HashMap<String, byte[]>();
      resources.put(f.getName(), Misc.getAllContentFrom(f));

      process = getManagementAPI().deploy(getBusinessArchive(process, resources));
      final ProcessDefinitionUUID processUUID = process.getUUID();

      final DocumentSearchBuilder builder = new DocumentSearchBuilder();
      builder.criterion(DocumentIndex.PROCESS_DEFINITION_UUID).equalsTo(processUUID.getValue())
      .and().criterion(DocumentIndex.NAME).equalsTo("big");
      final DocumentResult searchDocuments = getQueryRuntimeAPI().searchDocuments(builder, 0, 10);
      final List<Document> documents = searchDocuments.getDocuments();
      assertEquals(1, documents.size());
      final Document document = documents.get(0);
      final byte[] documentContent = getQueryRuntimeAPI().getDocumentContent(document.getUUID());

      assertTrue(Arrays.equals(Misc.getAllContentFrom(f), documentContent));
      getRuntimeAPI().instantiateProcess(processUUID);

      f.deleteOnExit();
      getManagementAPI().deleteProcess(processUUID);
  }

  public void testAttachmentDefinition() throws Exception {
    final byte[] initialValue = new byte[] {3, 2, 4, 0, 9, 0};
    final File file = generatePropertiesFile();
    ProcessDefinition process = ProcessBuilder.createProcess("p", "1.0")
    .addAttachment("a")
    .addDescription("descr")
    .addLabel("l")
    .addAttachment("b", "attach/b")
    .addDescription("descr")
    .addLabel("lab")
    .addAttachment("c", "attach/c", file.getName())
    .addAttachment("d", "attach/d", file.getName())
    .addHuman(getLogin())
    .addHumanTask("t", getLogin())
    .done();

    final Map<String, byte[]> resources = new HashMap<String, byte[]>();
    resources.put("attach/b", initialValue);
    resources.put("attach/c", Misc.getAllContentFrom(file));
    resources.put("attach/d", Misc.getAllContentFrom(file));

    process = getManagementAPI().deploy(getBusinessArchive(process, resources));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    assertEquals("descr", process.getAttachment("a").getDescription());
    assertEquals("l", process.getAttachment("a").getLabel());
    assertEquals(file.getName(), process.getAttachment("c").getFileName());
    assertEquals(file.getName(), process.getAttachment("d").getFileName());

    final AttachmentDefinition attachmentB = process.getAttachment("b");
    //assertEquals("b", attachmentB.getFileName());
    final InitialAttachment processAttachment = getQueryDefinitionAPI().getProcessAttachment(processUUID, "b");
    assertTrue(Arrays.equals(initialValue, processAttachment.getContent()));

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    final AttachmentInstance attachment = getQueryRuntimeAPI().getLastAttachment(instanceUUID, "b");
    assertEquals(instanceUUID, attachment.getProcessInstanceUUID());
    assertEquals(getLogin(), attachment.getAuthor());

    assertEquals("b", attachment.getName());
    assertTrue(System.currentTimeMillis() >= attachment.getVersionDate().getTime());
    final byte[] content = getQueryRuntimeAPI().getAttachmentValue(attachment);
    assertTrue(Arrays.equals(initialValue, content));

    assertNull(getQueryRuntimeAPI().getLastAttachment(instanceUUID, "a").getFileName());
    assertEquals(file.getName(), getQueryRuntimeAPI().getLastAttachment(instanceUUID, "c").getFileName());
    assertEquals(file.getName(), getQueryRuntimeAPI().getLastAttachment(instanceUUID, "d").getFileName());

    file.deleteOnExit();
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetAttachemnts() throws Exception {
    final byte[] initialValue = new byte[] {3, 2, 4, 0, 9, 0};
    final File file = generatePropertiesFile();

    final Map<String, byte[]> resources = new HashMap<String, byte[]>();
    resources.put("attachB", initialValue);
    resources.put("attachC", Misc.getAllContentFrom(file));
    resources.put("attachD", Misc.getAllContentFrom(file));

    ProcessDefinition process = ProcessBuilder.createProcess("pe", "1.0")
    .addAttachment("a")
    .addDescription("descr")
    .addLabel("l")
    .addAttachment("b", "attachB")
    .addDescription("descr")
    .addLabel("lab")
    .addAttachment("c", "attachC")
    .addAttachment("d", "attachD")
    .addHuman(getLogin())
    .addHumanTask("t", getLogin())
    .done();

    process = getManagementAPI().deploy(getBusinessArchive(process, resources));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final Set<InitialAttachment> attachements = getQueryDefinitionAPI().getProcessAttachments(processUUID);
    assertEquals(4, attachements.size());

    final InitialAttachment attachement = getQueryDefinitionAPI().getProcessAttachment(processUUID, "b");
    assertNotNull(attachement);

    file.deleteOnExit();
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGetActivityAttachmentFileName() throws Exception {
    final File initialAttachmentFile = File.createTempFile("attachment-test",".txt");

    ProcessDefinition attachmentProcess = ProcessBuilder.createProcess("attachment_process", "1.0")
    .addAttachment("attachment", initialAttachmentFile.getPath(), initialAttachmentFile.getName())
    .addHuman(getLogin())
    .addHumanTask("task1", getLogin())
    .addHumanTask("task2", getLogin())
    .addTransition("transition", "task1", "task2")
    .done();

    final BusinessArchive businessArchive = BusinessArchiveFactory.getBusinessArchive(attachmentProcess);
    attachmentProcess = getManagementAPI().deploy(businessArchive);

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(attachmentProcess.getUUID());

    try {
      Collection<TaskInstance> tasks = getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
      assertEquals(1, tasks.size());
      ActivityInstanceUUID activityInstanceUUID = tasks.iterator().next().getUUID();

      AttachmentInstance currentAttachment = getQueryRuntimeAPI().getLastAttachment(instanceUUID, "attachment");
      assertEquals(initialAttachmentFile.getName(), currentAttachment.getFileName());

      getRuntimeAPI().executeTask(activityInstanceUUID, true);
      currentAttachment = getQueryRuntimeAPI().getLastAttachment(instanceUUID, "attachment", activityInstanceUUID);
      assertEquals(initialAttachmentFile.getName(), currentAttachment.getFileName());

      final File attachmentFile2 = File.createTempFile("new-attachment-test",".txt");
      tasks = getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
      assertEquals(1, tasks.size());
      activityInstanceUUID = tasks.iterator().next().getUUID();

      final Map<String, String> metadata = new HashMap<String, String>();
      metadata.put("content-type", "application/octet-stream");
      getRuntimeAPI().addAttachment(instanceUUID, "attachment", null, null, attachmentFile2.getName(), metadata, new byte[1]);
      currentAttachment = getQueryRuntimeAPI().getLastAttachment(instanceUUID, "attachment", activityInstanceUUID);
      assertNotNull(currentAttachment);
      assertEquals(attachmentFile2.getName(), currentAttachment.getFileName());
      assertEquals("application/octet-stream", currentAttachment.getMetaData().get("content-type"));

      tasks = getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
      assertEquals(1, tasks.size());
      activityInstanceUUID = tasks.iterator().next().getUUID();
      getRuntimeAPI().executeTask(activityInstanceUUID, true);

      currentAttachment = getQueryRuntimeAPI().getLastAttachment(instanceUUID, "attachment", activityInstanceUUID);
      assertNotNull(currentAttachment);
      assertEquals(attachmentFile2.getName(), currentAttachment.getFileName());
      assertTrue("Delete generated file", attachmentFile2.delete());
    } finally {
      assertTrue("Delete generated file", initialAttachmentFile.delete());
      getRuntimeAPI().deleteProcessInstance(instanceUUID);
      getManagementAPI().deleteProcess(attachmentProcess.getUUID());
    }
  }

  public void testTwoDifferentInstancesCannotSeeEachOtherDocuments() throws Exception {
    ProcessDefinition attachmentProcess = ProcessBuilder.createProcess("Plop", "1.0")
    .addAttachment("attachment232")
    .addHuman(getLogin())
    .addHumanTask("task1", getLogin())
    .done();

    final BusinessArchive businessArchive = BusinessArchiveFactory.getBusinessArchive(attachmentProcess);
    attachmentProcess = getManagementAPI().deploy(businessArchive);

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(attachmentProcess.getUUID());

    getRuntimeAPI().addAttachment(instanceUUID, "attachment232", "test", new byte [1]);
    AttachmentInstance lastAttachment = getQueryRuntimeAPI().getLastAttachment(instanceUUID, "attachment232");
    assertNotNull(lastAttachment);
    assertEquals("test", lastAttachment.getFileName());

    executeTask(instanceUUID, "task1");

    waitForInstanceEnd(5000, 100, instanceUUID);

    instanceUUID = getRuntimeAPI().instantiateProcess(attachmentProcess.getUUID());
    lastAttachment = getQueryRuntimeAPI().getLastAttachment(instanceUUID, "attachment232");
    assertNotNull(lastAttachment);
    assertNull(lastAttachment.getFileName());

    getManagementAPI().deleteProcess(attachmentProcess.getUUID());
  }

  public void testGetInstanceAttachmentFileName() throws Exception {
    final File attachmentFile = File.createTempFile("attachment-test",".txt");

    ProcessDefinition attachmentProcess = ProcessBuilder.createProcess("attachment_process", "1.0")
    .addAttachment("attachment", attachmentFile.getPath(), attachmentFile.getName())
    .addHuman(getLogin())
    .addHumanTask("task1", getLogin())
    .addHumanTask("task2", getLogin())
    .addTransition("transition", "task1", "task2")
    .done();

    final BusinessArchive businessArchive = BusinessArchiveFactory.getBusinessArchive(attachmentProcess);
    attachmentProcess = getManagementAPI().deploy(businessArchive);

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(attachmentProcess.getUUID());

    try {
      Collection<TaskInstance> tasks = getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
      assertEquals(1, tasks.size());
      ActivityInstanceUUID activityInstanceUUID = tasks.iterator().next().getUUID();

      Thread.sleep(3000);
      final File attachmentFile2 = File.createTempFile("new-attachment-test",".txt");
      getRuntimeAPI().addAttachment(instanceUUID, "attachment", attachmentFile2.getName(), new byte[1]);
      getRuntimeAPI().executeTask(activityInstanceUUID, true);
      Thread.sleep(3000);
      ProcessInstance processInstance = getQueryRuntimeAPI().getProcessInstance(instanceUUID);
      Date startDate = processInstance.getStartedDate();
      AttachmentInstance attachment = getQueryRuntimeAPI().getLastAttachment(instanceUUID, "attachment", startDate);

      assertNotNull(attachment);
      assertEquals(attachmentFile.getName(), attachment.getFileName());

      tasks = getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
      assertEquals(1, tasks.size());
      activityInstanceUUID = tasks.iterator().next().getUUID();
      getRuntimeAPI().executeTask(activityInstanceUUID, true);
      processInstance = getQueryRuntimeAPI().getProcessInstance(instanceUUID);
      startDate = processInstance.getStartedDate();
      attachment = getQueryRuntimeAPI().getLastAttachment(instanceUUID, "attachment", startDate);

      assertNotNull(attachment);
      assertEquals(attachmentFile.getName(), attachment.getFileName());
      assertTrue(attachmentFile2.delete());
    } finally {
      assertTrue("Delete generated file", attachmentFile.delete());
      getRuntimeAPI().deleteProcessInstance(instanceUUID);
      getManagementAPI().deleteProcess(attachmentProcess.getUUID());
    }
  }

  public void testGetInstanceAttachmentInGroovy() throws Exception {
    final File attachmentFile = File.createTempFile("attachment-test",".txt");

    ProcessDefinition attachmentProcess = ProcessBuilder.createProcess("attachment_process", "1.0")
    .addAttachment("attachment", attachmentFile.getPath(), attachmentFile.getName())
    .addStringData("nothing")
    .addHuman(getLogin())
    .addHumanTask("task1", getLogin())
    .addConnector(Event.taskOnReady, SetVarConnector.class.getName(), true)
    .addInputParameter("variableName", "nothing")
    .addInputParameter("value", "${attachment.getFileName()}")
    .addHumanTask("task2", getLogin())
    .addTransition("transition", "task1", "task2")
    .done();

    final BusinessArchive businessArchive = BusinessArchiveFactory.getBusinessArchive(
        attachmentProcess, getResourcesFromConnector(SetVarConnector.class), SetVarConnector.class);
    attachmentProcess = getManagementAPI().deploy(businessArchive);

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(attachmentProcess.getUUID());

    try {
      final Collection<TaskInstance> tasks = getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
      assertEquals(1, tasks.size());
      final ActivityInstanceUUID activityInstanceUUID = tasks.iterator().next().getUUID();

      getRuntimeAPI().executeTask(activityInstanceUUID, true);
      final String actual = (String) getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "nothing");
      assertEquals(attachmentFile.getName(), actual);
    } finally {
      assertTrue(attachmentFile.delete());
      getRuntimeAPI().deleteProcessInstance(instanceUUID);
      getManagementAPI().deleteProcess(attachmentProcess.getUUID());
    }
  }

  public void testAttachmentPathTooLong() throws Exception{
    final StringBuilder pathBuilder = new StringBuilder();
    for (int i = 0; i < 300; i++) {
      pathBuilder.append("a");
    }
    ProcessDefinition definition =
      ProcessBuilder.createProcess("tooLong", "1.4")
      .addHuman(getLogin())
      .addHumanTask("attch", getLogin())
      .addAttachment("file1", pathBuilder.toString())
      .done();

    definition = getManagementAPI().deploy(getBusinessArchive(definition));
    getManagementAPI().deleteProcess(definition.getUUID());
  }

  /*
  public void testAttachments() throws BonitaException, IOException {
    byte[] initialValue = new byte[]{3, 2, 4, 0, 9, 0};
    byte[] newValue = new byte[]{5, 2, 5, 0, 7, 0};

    File file = generatePropertiesFile();
    String filePath = file.getAbsolutePath();

    byte[] fileAsBytes = Misc.getAllContentFrom(file);

    ProcessDefinition process = ProcessBuilder.createProcess("p", "1.0")
    .addHuman("h")
    .addAttachment("a")
    .addDescription("descr")
    .addLabel("l")
    .addAttachment("b", initialValue)
    .addAttachment("c", filePath)
    .addAttachment("d", file)
    .addHuman(getLogin())
    .addHumanTask("t", getLogin())
    .done();

    process = getManagementAPI().deploy(getBusinessArchive(process));
    ProcessDefinitionUUID processUUID = process.getUUID();

    assertEquals("descr", process.getAttachment("a").getDescription());
    assertEquals("l", process.getAttachment("a").getLabel());
    ProcessInstanceUUID instance1 = getRuntimeAPI().instantiateProcess(processUUID);

    try {
      getQueryRuntimeAPI().getAttachment(null, "a");
    } catch (IllegalArgumentException e) {
      // OK
    }

    checkAttachment(instance1, null, "a");
    checkAttachment(instance1, initialValue, "b");
    checkAttachment(instance1, fileAsBytes, "c");
    checkAttachment(instance1, fileAsBytes, "d");
    checkFile(getQueryRuntimeAPI().getAttachment(instance1, "c"));
    checkFile(getQueryRuntimeAPI().getAttachment(instance1, "d"));

    getRuntimeAPI().setAttachment(instance1, "b", newValue);

    checkAttachment(instance1, newValue, "b");

    byte[] aValue = new byte[]{3, 2, 6, 0, 7, 0, 5, 8, 0, 8};
    Map<String, byte[]> initialAttachments = new HashMap<String, byte[]>();
    initialAttachments.put("a", aValue);
    initialAttachments.put("b", newValue);

    ProcessInstanceUUID instance2 = getRuntimeAPI().instantiateProcess(processUUID, null, initialAttachments);

    checkAttachment(instance2, aValue, "a");
    checkAttachment(instance2, newValue, "b");
    checkAttachment(instance2, fileAsBytes, "c");
    checkAttachment(instance2, fileAsBytes, "d");
    checkFile(getQueryRuntimeAPI().getAttachment(instance2, "c"));
    checkFile(getQueryRuntimeAPI().getAttachment(instance2, "d"));

    byte[] cValue = new byte[]{66};
    byte[] dValue = new byte[]{8, 2, 6, 6, 6};

    Map<String, byte[]> newAttachments = new HashMap<String, byte[]>();
    newAttachments.put("c", cValue);
    newAttachments.put("d", dValue);

    getRuntimeAPI().setAttachments(instance2, newAttachments);

    checkAttachment(instance2, aValue, "a");
    checkAttachment(instance2, newValue, "b");
    checkAttachment(instance2, cValue, "c");
    checkAttachment(instance2, dValue, "d");


    Set<String> attachmentNames = new HashSet<String>();
    attachmentNames.add("a");
    attachmentNames.add("c");
    attachmentNames.add("d");

    Map<String, byte[]> filteredAtachments = getQueryRuntimeAPI().getAttachments(instance2, attachmentNames);
    assertNotNull(filteredAtachments);
    assertEquals(3, filteredAtachments.size());
    assertTrue(Arrays.equals(aValue, filteredAtachments.get("a")));
    assertTrue(Arrays.equals(cValue, filteredAtachments.get("c")));
    assertTrue(Arrays.equals(dValue, filteredAtachments.get("d")));

    getRuntimeAPI().deleteAllProcessInstances(processUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }
   */
  /*
  private void checkAttachment(ProcessInstanceUUID instanceUUID, byte[] expectedValue, String name) {
    assertTrue(Arrays.equals(expectedValue, getQueryRuntimeAPI().getAttachments(instanceUUID).get(name)));
    assertTrue(Arrays.equals(expectedValue, getQueryRuntimeAPI().getAttachment(instanceUUID, name)));
  }

  private void checkFile(byte[] result) throws IOException {
    File file = File.createTempFile("attachment", ".properties");
    Misc.write(file, result);
    Properties properties = new Properties();
    properties.load(new FileInputStream(file.getAbsolutePath()));
    assertEquals(4, properties.size());
    assertEquals("1", properties.getProperty("a"));
    assertEquals("2", properties.getProperty("b"));
    assertEquals("3", properties.getProperty("c"));
    assertEquals("4", properties.getProperty("d"));
  }
   */

  private File generatePropertiesFile() throws IOException {
    return generatePropertiesFile(0);
  }

  private File generatePropertiesFile(final long additionalProperties) throws IOException {
    final Properties properties = new Properties();
    properties.put("a", "1");
    properties.put("b", "2");
    properties.put("c", "3");
    properties.put("d", "4");
    for (long i = 0 ; i < additionalProperties ; i++) {
      properties.put("addedPropertyKey" + Long.toString(i), "AddedpropertyValue" + Long.toString(i));  
    }
    final File file = File.createTempFile("attachment", ".properties");
    properties.store(new FileOutputStream(file.getAbsolutePath()), null);
    file.deleteOnExit();
    return file;
  }

  public void testRemoveAnAttachment() throws Exception {
    final String attachmentToRemove = "myAttachment";
    final String attachment = "aName";

    final byte[] initialValue = new byte[] {1, 2, 3, 4, 5, 6};
    final Map<String, byte[]> resources = new HashMap<String, byte[]>();
    resources.put(attachmentToRemove, initialValue);
    resources.put(attachment, initialValue);

    ProcessDefinition process = ProcessBuilder.createProcess("rmAttchmnt", "1.0")
    .addAttachment(attachmentToRemove)
    .addAttachment(attachment)
    .addHuman(getLogin())
    .addHumanTask("step", getLogin())
    .done();

    process = getManagementAPI().deploy(getBusinessArchive(process, resources));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    Set<String> attachmentNames = getQueryRuntimeAPI().getAttachmentNames(instanceUUID);
    assertEquals(2, attachmentNames.size());
    assertTrue(attachmentNames.contains(attachmentToRemove));
    assertTrue(attachmentNames.contains(attachment));

    getRuntimeAPI().removeAttachment(instanceUUID, attachmentToRemove);
    attachmentNames = getQueryRuntimeAPI().getAttachmentNames(instanceUUID);
    assertEquals(1, attachmentNames.size());
    assertTrue(attachmentNames.contains(attachment));

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testRemoveAnAttachmentWithSeveralVersions() throws Exception {
    final String attachmentToRemove = "myAttachment";
    final String attachment = "aName";

    final byte[] initialValue = new byte[] {1, 2, 3, 4, 5, 6};
    final Map<String, byte[]> resources = new HashMap<String, byte[]>();
    resources.put(attachmentToRemove, initialValue);
    resources.put(attachment, initialValue);

    ProcessDefinition process = ProcessBuilder.createProcess("rmAttchmnt", "1.0")
    .addAttachment(attachmentToRemove)
    .addAttachment(attachment)
    .addHuman(getLogin())
    .addHumanTask("step", getLogin())
    .done();

    process = getManagementAPI().deploy(getBusinessArchive(process, resources));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    Set<String> attachmentNames = getQueryRuntimeAPI().getAttachmentNames(instanceUUID);
    assertEquals(2, attachmentNames.size());
    assertTrue(attachmentNames.contains(attachmentToRemove));
    assertTrue(attachmentNames.contains(attachment));

    getRuntimeAPI().addAttachment(instanceUUID, attachmentToRemove, attachmentToRemove, new byte[] {1, 0, 0, 1, 1, 0});

    List<AttachmentInstance> attachments = getQueryRuntimeAPI().getAttachments(instanceUUID, attachmentToRemove);
    assertEquals(2, attachments.size());

    getRuntimeAPI().removeAttachment(instanceUUID, attachmentToRemove);
    attachmentNames = getQueryRuntimeAPI().getAttachmentNames(instanceUUID);
    assertEquals(1, attachmentNames.size());
    assertTrue(attachmentNames.contains(attachment));

    attachments = getQueryRuntimeAPI().getAttachments(instanceUUID, attachmentToRemove);
    assertEquals(0, attachments.size());

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testRemoveAnUnknownAttachment() throws Exception {
    final String attachmentToRemove = "myAttachment";
    final String attachment = "aName";

    final byte[] initialValue = new byte[] {1, 2, 3, 4, 5, 6};
    final Map<String, byte[]> resources = new HashMap<String, byte[]>();
    resources.put(attachmentToRemove, initialValue);
    resources.put(attachment, initialValue);

    ProcessDefinition process = ProcessBuilder.createProcess("rmAttchmnt", "1.3")
    .addAttachment(attachmentToRemove)
    .addAttachment(attachment)
    .addHuman(getLogin())
    .addHumanTask("step", getLogin())
    .done();

    process = getManagementAPI().deploy(getBusinessArchive(process, resources));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    Set<String> attachmentNames = getQueryRuntimeAPI().getAttachmentNames(instanceUUID);
    assertEquals(2, attachmentNames.size());
    assertTrue(attachmentNames.contains(attachmentToRemove));
    assertTrue(attachmentNames.contains(attachment));

    getRuntimeAPI().removeAttachment(instanceUUID, "test");
    attachmentNames = getQueryRuntimeAPI().getAttachmentNames(instanceUUID);
    assertEquals(2, attachmentNames.size());
    assertTrue(attachmentNames.contains(attachment));
    assertTrue(attachmentNames.contains(attachmentToRemove));

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testRemoveAnAttachmentOnAnUnknownProcess() throws Exception {
    final String attachmentToRemove = "myAttachment";
    final String attachment = "aName";

    final byte[] initialValue = new byte[] {1, 2, 3, 4, 5, 6};
    final Map<String, byte[]> resources = new HashMap<String, byte[]>();
    resources.put(attachmentToRemove, initialValue);
    resources.put(attachment, initialValue);

    ProcessDefinition process = ProcessBuilder.createProcess("rmAttchmnt", "1.3")
    .addAttachment(attachmentToRemove)
    .addAttachment(attachment)
    .addHuman(getLogin())
    .addHumanTask("step", getLogin())
    .done();

    process = getManagementAPI().deploy(getBusinessArchive(process, resources));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    getRuntimeAPI().instantiateProcess(processUUID);
    try {
      getRuntimeAPI().removeAttachment(new ProcessInstanceUUID("unknwon"), "test");
      fail("unknwon is not a valid process instance UUID");
    } catch (final InstanceNotFoundException infe) {
      assertTrue(true);
    }  finally {
      getManagementAPI().deleteProcess(processUUID);
    }
  }

  public void testByteArrayAttachmentWithSetVariable() throws Exception {
    final byte[] initialValue = new byte[]{3, 2, 4, 0, 9, 0};
    final Map<String, byte[]> resources = new HashMap<String, byte[]>();
    resources.put("attachB", initialValue);

    ProcessDefinition process = ProcessBuilder.createProcess("pe", "2.0")
    .addAttachment("b", "attachB")
    .addDescription("descr")
    .addLabel("lab")
    .addHuman(getLogin())
    .addHumanTask("t", getLogin())
    .done();

    process = getManagementAPI().deploy(getBusinessArchive(process, resources));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    final Set<ActivityInstance> activities = getQueryRuntimeAPI().getActivityInstances(instanceUUID, "t");
    assertEquals(1, activities.size());

    final ActivityInstanceUUID activityUUID= activities.iterator().next().getUUID();

    List<AttachmentInstance> attachments = getQueryRuntimeAPI().getAttachments(instanceUUID, "b");
    assertEquals(1, attachments.size());
    final byte[] newValue = new byte[]{6, 5};

    Thread.sleep(5000);
    getRuntimeAPI().setVariable(activityUUID, "b", newValue);
    attachments = getQueryRuntimeAPI().getAttachments(instanceUUID, "b");
    assertEquals(2, attachments.size());

    final byte[] attachmentValue1 = getQueryRuntimeAPI().getAttachmentValue(attachments.get(0));
    final byte[] attachmentValue2 = getQueryRuntimeAPI().getAttachmentValue(attachments.get(1));

    if (attachments.get(1).getVersionDate().getTime() > attachments.get(0).getVersionDate().getTime()) {
      assertEquals(6, attachmentValue1.length);

      assertEquals(2, attachmentValue2.length);
      assertEquals(6, attachmentValue2[0]);
      assertEquals(5, attachmentValue2[1]);
    } else {
      assertEquals(6, attachmentValue2.length);

      assertEquals(2, attachmentValue1.length);
      assertEquals(6, attachmentValue1[0]);
      assertEquals(5, attachmentValue1[1]);
    }

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testAttachementInstanceWithSetVariable() throws Exception {
    final byte[] initialValueC = new byte[] {3, 2};
    final byte[] initialValueD = new byte[] {5, 4};
    final Map<String, byte[]> resources = new HashMap<String, byte[]>();
    resources.put("attachC", initialValueC);
    resources.put("attachD", initialValueD);

    ProcessDefinition process = ProcessBuilder.createProcess("pe", "1.0")
    .addAttachment("c", "attachC")
    .addAttachment("d", "attachD")
    .addHuman(getLogin())
    .addHumanTask("t", getLogin())
    .done();

    process = getManagementAPI().deploy(getBusinessArchive(process, resources));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    final Set<ActivityInstance> activities = getQueryRuntimeAPI().getActivityInstances(instanceUUID, "t");
    assertEquals(1, activities.size());

    final ActivityInstanceUUID activityUUID= activities.iterator().next().getUUID();

    List<AttachmentInstance> attachments = getQueryRuntimeAPI().getAttachments(instanceUUID, "d");
    assertEquals(1, attachments.size());

    attachments = getQueryRuntimeAPI().getAttachments(instanceUUID, "c");
    assertEquals(1, attachments.size());

    getRuntimeAPI().setVariable(activityUUID, "d", attachments.get(0));
    attachments = getQueryRuntimeAPI().getAttachments(instanceUUID, "d");
    assertEquals(2, attachments.size());

    final byte[] attachmentValue1 = getQueryRuntimeAPI().getAttachmentValue(attachments.get(0));
    final byte[] attachmentValue2 = getQueryRuntimeAPI().getAttachmentValue(attachments.get(1));

    if (attachments.get(0).getVersionDate().getTime() > attachments.get(0).getVersionDate().getTime()) {
      assertEquals(2, attachmentValue1.length);
      assertEquals(5, attachmentValue1[0]);
      assertEquals(4, attachmentValue1[1]);

      assertEquals(2, attachmentValue2.length);
      assertEquals(3, attachmentValue2[0]);
      assertEquals(2, attachmentValue2[1]);
    } else {
      assertEquals(2, attachmentValue1.length);
      assertEquals(3, attachmentValue1[0]);
      assertEquals(2, attachmentValue1[1]);

      assertEquals(2, attachmentValue2.length);
      assertEquals(5, attachmentValue2[0]);
      assertEquals(4, attachmentValue2[1]);
    }
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testAttachemntsWithSetVariableActivityVariableAndAttachmentWithTheSameName() throws Exception {
    final byte[] initialValue = new byte[] {3, 2};
    final Map<String, byte[]> resources = new HashMap<String, byte[]>();
    resources.put("attachB", initialValue);

    ProcessDefinition process = ProcessBuilder.createProcess("pe", "2.2")
    .addAttachment("b", "attachB")
    .addDescription("descr")
    .addLabel("lab")
    .addHuman(getLogin())
    .addHumanTask("t", getLogin())
    .addStringData("b")
    .done();

    process = getManagementAPI().deploy(getBusinessArchive(process, resources));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    List<AttachmentInstance> attachments = getQueryRuntimeAPI().getAttachments(instanceUUID, "b");
    assertEquals(1, attachments.size());

    final Set<ActivityInstance> activities = getQueryRuntimeAPI().getActivityInstances(instanceUUID, "t");
    assertEquals(1, activities.size());

    final ActivityInstanceUUID activityUUID = activities.iterator().next().getUUID();

    getRuntimeAPI().setVariable(activityUUID, "b", "hello");
    attachments = getQueryRuntimeAPI().getAttachments(instanceUUID, "b");
    assertEquals(1, attachments.size());
    final byte[] attachmentValue = getQueryRuntimeAPI().getAttachmentValue(attachments.get(0));
    assertEquals(2, attachmentValue.length);
    assertEquals(3, attachmentValue[0]);
    assertEquals(2, attachmentValue[1]);

    final String variableValue = (String)getQueryRuntimeAPI().getActivityInstanceVariable(activityUUID, "b");
    assertEquals("hello", variableValue);

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testWrongAttachmentTypeWithSetVariable() throws Exception {
    final byte[] initialValue = new byte[]{3, 2};
    final Map<String, byte[]> resources = new HashMap<String, byte[]>();
    resources.put("attachB", initialValue);

    ProcessDefinition process = ProcessBuilder.createProcess("pe", "2.2")
    .addAttachment("b", "attachB")
    .addDescription("descr")
    .addLabel("lab")
    .addHuman(getLogin())
    .addHumanTask("t", getLogin())
    .done();

    process = getManagementAPI().deploy(getBusinessArchive(process, resources));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    List<AttachmentInstance> attachments = getQueryRuntimeAPI().getAttachments(instanceUUID, "b");
    assertEquals(1, attachments.size());

    final Set<ActivityInstance> activities = getQueryRuntimeAPI().getActivityInstances(instanceUUID, "t");
    assertEquals(1, activities.size());

    final ActivityInstanceUUID activityUUID = activities.iterator().next().getUUID();

    try {
      getRuntimeAPI().setVariable(activityUUID, "b", "hello");
      fail("Only byte array and AttachmentInstance must be accepted in the entry");
    } catch (final BonitaInternalException e) {
    } 

    attachments = getQueryRuntimeAPI().getAttachments(instanceUUID, "b");
    assertEquals(1, attachments.size());
    final byte[] attachmentValue = getQueryRuntimeAPI().getAttachmentValue(attachments.get(0));
    assertEquals(2, attachmentValue.length);
    assertEquals(3, attachmentValue[0]);
    assertEquals(2, attachmentValue[1]);

    getManagementAPI().deleteProcess(processUUID);
  }

  public void writeText(final File file, final String message) throws IOException {
    FileWriter writer = null;
    try {
      writer = new FileWriter(file);
      final BufferedWriter out = new BufferedWriter(writer);
      out.write(message);
      out.close();
    }
    finally {
      if (writer != null) {
        writer.close();
      }
    }
  }

  public void testSubflowWithAttachments() throws Exception {
    final File parentAttachFile = File.createTempFile("attachment-parent",".txt");
    writeText(parentAttachFile, "parent");
    final File childAttachFile = File.createTempFile("attachment-child",".txt");
    writeText(childAttachFile, "child");
    try {
      ProcessDefinition process = ProcessBuilder.createProcess("parent", "1.0")
      .addAttachment("parentAttach1", parentAttachFile.getPath(), parentAttachFile.getName())
      .addLabel("parentAttach1Label")
      .addDescription("parentAttach1Description")
      .addSubProcess("parentActivity", "sub")
      .addSubProcessInParameter("parentAttach1", "childAttach1")
      .addSubProcessOutParameter("childAttach2", "parentAttach2")
      .done();

      final Map<String, byte[]> parentResources = new HashMap<String, byte[]>();
      parentResources.put(parentAttachFile.getPath(), Misc.getAllContentFrom(parentAttachFile));

      ProcessDefinition subProcess = ProcessBuilder.createProcess("sub", "1.0")
      .addAttachment("childAttach1")
      .addAttachment("childAttach2", childAttachFile.getPath(), childAttachFile.getName())
      .addLabel("childAttach2Label")
      .addDescription("childAttach2Description")
      .addSystemTask("subActivity")
      .done();

      final Map<String, byte[]> childResources = new HashMap<String, byte[]>();
      childResources.put(childAttachFile.getPath(), Misc.getAllContentFrom(childAttachFile));

      subProcess = getManagementAPI().deploy(getBusinessArchive(subProcess, childResources));
      process = getManagementAPI().deploy(getBusinessArchive(process, parentResources));

      final ProcessDefinitionUUID processUUID = process.getUUID();

      final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

      final ProcessInstance instance = getQueryRuntimeAPI().getProcessInstance(instanceUUID);
      assertEquals(InstanceState.FINISHED, instance.getInstanceState());

      final Set<ProcessInstanceUUID> childInstances = instance.getChildrenInstanceUUID();
      assertEquals(1, childInstances.size());
      final ProcessInstanceUUID childInstanceUUID = childInstances.iterator().next();
      assertEquals(InstanceState.FINISHED, getQueryRuntimeAPI().getLightProcessInstance(childInstanceUUID).getInstanceState());

      //check definition attachments
      final AttachmentInstance parentAttach1 = getQueryRuntimeAPI().getLastAttachment(instanceUUID, "parentAttach1");
      assertNotNull(parentAttach1);
      final byte[] parentAttach1Value = getQueryRuntimeAPI().getAttachmentValue(parentAttach1);
      assertNotNull(parentAttach1Value);

      final AttachmentInstance childAttach2 = getQueryRuntimeAPI().getLastAttachment(childInstanceUUID, "childAttach2");
      assertNotNull(childAttach2);
      final byte[] childAttach2Value = getQueryRuntimeAPI().getAttachmentValue(childAttach2);
      assertNotNull(childAttach2Value);

      //check attachment propagation: parent to sub
      final AttachmentInstance childAttach1 = getQueryRuntimeAPI().getLastAttachment(childInstanceUUID, "childAttach1");
      assertNotNull(childAttach1);
      final byte[] childAttach1Value = getQueryRuntimeAPI().getAttachmentValue(childAttach1);
      assertNotNull(childAttach1Value);
      assertTrue(Arrays.equals(parentAttach1Value, childAttach1Value));
      assertEquals(parentAttach1.getAuthor(), childAttach1.getAuthor());
      assertEquals(parentAttach1.getDescription(), childAttach1.getDescription());
      assertEquals(parentAttach1.getFileName(), childAttach1.getFileName());
      assertEquals(parentAttach1.getLabel(), childAttach1.getLabel());
      assertEquals(parentAttach1.getMetaData(), childAttach1.getMetaData());
      assertEquals("childAttach1", childAttach1.getName());
      assertEquals(childInstanceUUID, childAttach1.getProcessInstanceUUID());

      //check attachment propagation: sub to parent
      final AttachmentInstance parentAttach2 = getQueryRuntimeAPI().getLastAttachment(instanceUUID, "parentAttach2");
      assertNotNull(parentAttach2);
      final byte[] parentAttach2Value = getQueryRuntimeAPI().getAttachmentValue(parentAttach2);
      assertNotNull(parentAttach2Value);
      assertTrue(Arrays.equals(childAttach2Value, parentAttach2Value));
      assertEquals(childAttach2.getAuthor(), parentAttach2.getAuthor());
      assertEquals(childAttach2.getDescription(), parentAttach2.getDescription());
      assertEquals(childAttach2.getFileName(), parentAttach2.getFileName());
      assertEquals(childAttach2.getLabel(), parentAttach2.getLabel());
      assertEquals(childAttach2.getMetaData(), parentAttach2.getMetaData());
      assertEquals("parentAttach2", parentAttach2.getName());
      assertEquals(instanceUUID, parentAttach2.getProcessInstanceUUID());

      //check that we can find the attachment in the root instance too
      final AttachmentInstance childInParent = getQueryRuntimeAPI().getLastAttachment(instanceUUID, "childAttach1");
      assertNotNull(childInParent);
      final byte[] childInParentValue = getQueryRuntimeAPI().getAttachmentValue(childInParent);
      assertNotNull(childInParentValue);
      assertTrue(Arrays.equals(parentAttach1Value, childInParentValue));
      assertEquals(parentAttach1.getAuthor(), childInParent.getAuthor());
      assertEquals(parentAttach1.getDescription(), childInParent.getDescription());
      assertEquals(parentAttach1.getFileName(), childInParent.getFileName());
      assertEquals(parentAttach1.getLabel(), childInParent.getLabel());
      assertEquals(parentAttach1.getMetaData(), childInParent.getMetaData());
      assertEquals("childAttach1", childInParent.getName());
      assertEquals(childInstanceUUID, childInParent.getProcessInstanceUUID());


      getManagementAPI().deleteProcess(processUUID);
      getManagementAPI().deleteProcess(subProcess.getUUID());
    } finally {
      parentAttachFile.delete();
      childAttachFile.delete();
    }
  }

  public void testSubflowWithAttachmentThatHaveTheSameNameThatTheParent() throws Exception {
    final File parentAttachFile = File.createTempFile("attachment-parent",".txt");
    writeText(parentAttachFile, "parent");
    try {
      ProcessDefinition process = ProcessBuilder.createProcess("parent", "1.0")
      .addAttachment("theAttachment", parentAttachFile.getPath(), parentAttachFile.getName())
      .addLabel("parentAttach1Label")
      .addDescription("parentAttach1Description")
      .addSubProcess("parentActivity", "sub")
      .addSubProcessInParameter("theAttachment", "theAttachment")
      .done();

      final Map<String, byte[]> parentResources = new HashMap<String, byte[]>();
      parentResources.put(parentAttachFile.getPath(), Misc.getAllContentFrom(parentAttachFile));

      ProcessDefinition subProcess = ProcessBuilder.createProcess("sub", "1.0")
      .addAttachment("theAttachment")
      .addSystemTask("subActivity")
      .done();

      final Map<String, byte[]> childResources = new HashMap<String, byte[]>();

      subProcess = getManagementAPI().deploy(getBusinessArchive(subProcess, childResources));
      process = getManagementAPI().deploy(getBusinessArchive(process, parentResources));

      final ProcessDefinitionUUID processUUID = process.getUUID();

      final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
      //should not fail here because of the attachment in child have same name in parent

      final ProcessInstance instance = getQueryRuntimeAPI().getProcessInstance(instanceUUID);
      assertEquals(InstanceState.FINISHED, instance.getInstanceState());

      final Set<ProcessInstanceUUID> childInstances = instance.getChildrenInstanceUUID();
      assertEquals(1, childInstances.size());
      final ProcessInstanceUUID childInstanceUUID = childInstances.iterator().next();
      assertEquals(InstanceState.FINISHED, getQueryRuntimeAPI().getLightProcessInstance(childInstanceUUID).getInstanceState());

      //check definition attachments
      final AttachmentInstance parentAttach1 = getQueryRuntimeAPI().getLastAttachment(instanceUUID, "theAttachment");
      assertNotNull(parentAttach1);
      final byte[] parentAttach1Value = getQueryRuntimeAPI().getAttachmentValue(parentAttach1);
      assertNotNull(parentAttach1Value);

      //check attachment propagation: parent to sub
      final AttachmentInstance childAttach1 = getQueryRuntimeAPI().getLastAttachment(childInstanceUUID, "theAttachment");
      assertNotNull(childAttach1);
      final byte[] childAttach1Value = getQueryRuntimeAPI().getAttachmentValue(childAttach1);
      assertNotNull(childAttach1Value);
      assertTrue(Arrays.equals(parentAttach1Value, childAttach1Value));
      assertEquals(parentAttach1.getAuthor(), childAttach1.getAuthor());
      assertEquals(parentAttach1.getDescription(), childAttach1.getDescription());
      assertEquals(parentAttach1.getFileName(), childAttach1.getFileName());
      assertEquals(parentAttach1.getLabel(), childAttach1.getLabel());
      assertEquals(parentAttach1.getMetaData(), childAttach1.getMetaData());
      assertEquals("theAttachment", childAttach1.getName());
      assertEquals(childInstanceUUID, childAttach1.getProcessInstanceUUID());


      getManagementAPI().deleteProcess(processUUID);
      getManagementAPI().deleteProcess(subProcess.getUUID());
    } finally {
      parentAttachFile.delete();
    }
  }

  public void testAddNullProcessAttachment() throws Exception {
    ProcessDefinition attachmentProcess = ProcessBuilder.createProcess("attachment_process", "1.0")
    .addHuman(getLogin())
    .addHumanTask("task1", getLogin())
    .done();

    final BusinessArchive businessArchive = BusinessArchiveFactory.getBusinessArchive(attachmentProcess);
    attachmentProcess = getManagementAPI().deploy(businessArchive);

    AttachmentInstance attachmentValue = null;
    ProcessInstanceUUID processInstanceUUID = null;
    try {
      final Map<String, Object> variableValues = new HashMap<String, Object>();
      final Set<InitialAttachment> attachments = new HashSet<InitialAttachment>();
      attachments.add(new InitialAttachmentImpl("attachmentName", null));
      processInstanceUUID = getRuntimeAPI().instantiateProcess(attachmentProcess.getUUID(), variableValues, attachments);
      attachmentValue = getQueryRuntimeAPI().getLastAttachment(processInstanceUUID, "attachmentName");
      final byte[] attachmentContent = getQueryRuntimeAPI().getAttachmentValue(attachmentValue);
      assertNull("the attachment content should be null", attachmentContent);
      assertNull("the attachment file name should be null", attachmentValue.getFileName());
    } finally {
      getRuntimeAPI().deleteDocuments(true, attachmentValue.getUUID());
      getRuntimeAPI().deleteProcessInstance(processInstanceUUID);
      getManagementAPI().deleteProcess(attachmentProcess.getUUID());
    }
  }

  public void testAddNullProcessInstanceAttachment() throws Exception {
    final File attachmentFile = File.createTempFile("attachment-test",".txt");
    final FileOutputStream fileOutputStream = new FileOutputStream(attachmentFile);
    fileOutputStream.write("test".getBytes("UTF-8"));
    fileOutputStream.close();

    ProcessDefinition attachmentProcess = ProcessBuilder.createProcess("attachment_process", "1.0")
    .addAttachment("attachment", attachmentFile.getPath(), attachmentFile.getName())
    .addHuman(getLogin())
    .addHumanTask("task1", getLogin())
    .done();

    final BusinessArchive businessArchive = BusinessArchiveFactory.getBusinessArchive(attachmentProcess);
    attachmentProcess = getManagementAPI().deploy(businessArchive);

    final ProcessInstanceUUID processInstanceUUID = getRuntimeAPI().instantiateProcess(attachmentProcess.getUUID());
    AttachmentInstance attachmentValue = null;
    try {
      getRuntimeAPI().addAttachment(processInstanceUUID, "attachment", null, null);
      attachmentValue = getQueryRuntimeAPI().getLastAttachment(processInstanceUUID, "attachment");
      final byte[] attachmentContent = getQueryRuntimeAPI().getAttachmentValue(attachmentValue);
      assertNull("the attachment content should be null", attachmentContent);
      assertNull("the attachment file name should be null", attachmentValue.getFileName());
    } 
    finally {
      if (attachmentValue != null) {
        getRuntimeAPI().deleteDocuments(true, attachmentValue.getUUID());
      }
      getRuntimeAPI().deleteProcessInstance(processInstanceUUID);
      getManagementAPI().deleteProcess(attachmentProcess.getUUID());
    } 
  }

  public void testGetInstanceAttachmentInitialFileName() throws Exception {

    final File attachmentFile = File.createTempFile("attachment-test",".txt");
    final FileOutputStream fileOutputStream = new FileOutputStream(attachmentFile);
    fileOutputStream.write("test".getBytes("UTF-8"));
    fileOutputStream.close();

    ProcessDefinition attachmentProcess = ProcessBuilder.createProcess("attachment_process", "1.0")
    .addAttachment("attachment", attachmentFile.getPath(), attachmentFile.getName())
    .addHuman("john")
    .addHuman("admin")
    .addHumanTask("task1", "admin")
    .addHumanTask("task2", "john")
    .addTransition("transition", "task1", "task2")
    .done();

    final Map<String, byte[]> parentResources = new HashMap<String, byte[]>();
    parentResources.put(attachmentFile.getPath(), Misc.getAllContentFrom(attachmentFile));

    final BusinessArchive businessArchive = BusinessArchiveFactory.getBusinessArchive(attachmentProcess,parentResources);
    attachmentProcess = getManagementAPI().deploy(businessArchive);

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(attachmentProcess.getUUID());

    try {
      ActivityInstanceUUID activityInstanceUUID = null;
      final Collection<TaskInstance> tasks = getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
      for (final TaskInstance activityInstance : tasks) {
        activityInstanceUUID = activityInstance.getUUID();
      }
      assertNotNull(activityInstanceUUID);
      //In case xcmis is deployed on another server and they don't have exactly the  same time
      Thread.sleep(3000);
      final File attachmentFile2 = File.createTempFile("new-attachment-test",".txt");
      getRuntimeAPI().addAttachment(instanceUUID, "attachment", attachmentFile2.getName(), "test".getBytes("UTF-8"));
      getRuntimeAPI().executeTask(activityInstanceUUID, true);
      Thread.sleep(3000);

      final ProcessInstance processInstance = getQueryRuntimeAPI().getProcessInstance(instanceUUID);
      final Date startDate = processInstance.getStartedDate();
      final AttachmentInstance attachment = getQueryRuntimeAPI().getLastAttachment(instanceUUID, "attachment", startDate);
      assertNotNull(attachment);
      final String fileName = attachment.getFileName();
      assertEquals(attachmentFile.getName(), fileName);
    } finally {
      final AttachmentInstance attachment = getQueryRuntimeAPI().getLastAttachment(instanceUUID, "attachment");
      getRuntimeAPI().deleteDocuments(true, attachment.getUUID());
      getRuntimeAPI().deleteProcessInstance(instanceUUID);
      getManagementAPI().deleteProcess(attachmentProcess.getUUID());
    }
  }

  public void testDeployUndeployRedeploy() throws Exception {
    final byte[] initialValue = new byte[]{3, 2, 4, 0, 9, 0};
    final Map<String, byte[]> resources = new HashMap<String, byte[]>();
    resources.put("attachB", initialValue);

    ProcessDefinition process = ProcessBuilder.createProcess("pEE", "2.0")
    .addAttachment("c", "attachB")
    .addDescription("descr")
    .addLabel("lab")
    .addHuman(getLogin())
    .addHumanTask("t", getLogin())
    .done();

    final BusinessArchive businessArchive = getBusinessArchive(process, resources);
    process = getManagementAPI().deploy(businessArchive);
    final ProcessDefinitionUUID processUUID = process.getUUID();

    getRuntimeAPI().instantiateProcess(processUUID);

    WebDeleteDocumentsOfProcessCommand webDeleteDocsProcessCommand = new WebDeleteDocumentsOfProcessCommand(processUUID);
    getCommandAPI().execute(webDeleteDocsProcessCommand);
    final WebDeleteProcessCommand webDeleteProcessCommand = new WebDeleteProcessCommand(processUUID);
    getCommandAPI().execute(webDeleteProcessCommand);

    process = getManagementAPI().deploy(businessArchive);
    webDeleteDocsProcessCommand = new WebDeleteDocumentsOfProcessCommand(processUUID);
    getCommandAPI().execute(webDeleteDocsProcessCommand);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testGroovyAttachment() throws Exception {
    final String firstContent = "firstContent";
    final byte[] initialValue = firstContent.getBytes();
    final String secondContent = "secondContent";
    final byte[] newValue = secondContent.getBytes();

    ProcessDefinition process = ProcessBuilder.createProcess("pEE", "2.0")
    .addAttachment("attach")
    .addHuman(getLogin())
    .addHumanTask("first", getLogin())
    .done();

    final BusinessArchive businessArchive = getBusinessArchive(process);
    process = getManagementAPI().deploy(businessArchive);
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    getRuntimeAPI().setProcessInstanceVariable(instanceUUID, "attach", initialValue);
    getRuntimeAPI().setProcessInstanceVariable(instanceUUID, "attach", newValue);

    final String groovyScript =
      "${import org.ow2.bonita.facade.APIAccessor;\n" +
      "import org.ow2.bonita.facade.impl.StandardAPIAccessorImpl;\n" +
      "final APIAccessor accessor = new StandardAPIAccessorImpl();\n" +
      "return accessor.getQueryRuntimeAPI().getAttachmentValue(attach);\n}";

    final byte[] actual = (byte[]) getRuntimeAPI().evaluateGroovyExpression(groovyScript, instanceUUID, false);
    assertEquals(secondContent, new String(actual));

    getManagementAPI().deleteProcess(processUUID);
  }

  public void testCallingAddAttachmentOnCreateChangesLastUpdate() throws Exception {
    ProcessDefinition attachmentProcess = ProcessBuilder.createProcess("p", "1.0")
    .addHuman(getLogin())
    .addHumanTask("task1", getLogin())
    .done();

    final BusinessArchive businessArchive = BusinessArchiveFactory.getBusinessArchive(attachmentProcess);
    attachmentProcess = getManagementAPI().deploy(businessArchive);

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(attachmentProcess.getUUID());

    final ProcessInstance initialInstance = getQueryRuntimeAPI().getProcessInstance(instanceUUID);
    Thread.sleep(100);
    getRuntimeAPI().addAttachment(instanceUUID, "attachment232", "test", new byte [1]);
    final ProcessInstance updatedInstance = getQueryRuntimeAPI().getProcessInstance(instanceUUID);
    assertFalse(initialInstance.getLastUpdate().equals(updatedInstance.getLastUpdate()));

    getManagementAPI().deleteProcess(attachmentProcess.getUUID());
  }

  public void testCallingAddAttachmentOnUpdateVersionChangesLastUpdate() throws Exception {
    ProcessDefinition attachmentProcess = ProcessBuilder.createProcess("p", "1.0")
    .addAttachment("attachment232")
    .addHuman(getLogin())
    .addHumanTask("task1", getLogin())
    .done();

    final BusinessArchive businessArchive = BusinessArchiveFactory.getBusinessArchive(attachmentProcess);
    attachmentProcess = getManagementAPI().deploy(businessArchive);

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(attachmentProcess.getUUID());
    final ProcessInstance initialInstance = getQueryRuntimeAPI().getProcessInstance(instanceUUID);
    Thread.sleep(100);
    getRuntimeAPI().addAttachment(instanceUUID, "attachment232", "test", new byte [1]);
    final ProcessInstance updatedInstance = getQueryRuntimeAPI().getProcessInstance(instanceUUID);
    assertFalse(initialInstance.getLastUpdate().equals(updatedInstance.getLastUpdate()));

    getManagementAPI().deleteProcess(attachmentProcess.getUUID());
  }

  public void testNullFileNamettachment() throws Exception {
    final String attachmentName = "myAttachment";

    ProcessDefinition attachmentProcess = ProcessBuilder.createProcess("poupi", "1.0")
    .addAttachment(attachmentName)
      .addLabel(attachmentName)
    .addHuman(getLogin())
    .addHumanTask("task1", getLogin())
    .done();

    final BusinessArchive businessArchive = BusinessArchiveFactory.getBusinessArchive(attachmentProcess);
    attachmentProcess = getManagementAPI().deploy(businessArchive);

    final AttachmentDefinition attachmentDefinition = getQueryDefinitionAPI().getAttachmentDefinition(attachmentProcess.getUUID(), attachmentName);
    assertNull(attachmentDefinition.getFileName());

    getManagementAPI().deleteProcess(attachmentProcess.getUUID());
  }

}
