/**
 * Copyright (C) 2007 Bull S. A. S. Bull, Rue Jean Jaures, B.P.68, 78340, Les
 * Clayes-sous-Bois This library is free software; you can redistribute it
 * and/or modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation version 2.1 of the License. This
 * library is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details. You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301,
 * USA.
 */
package org.ow2.bonita.variable;

import java.net.URL;
import java.util.Collection;

import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.def.element.BusinessArchive;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.InstanceState;
import org.ow2.bonita.facade.runtime.TaskInstance;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.BonitaException;
import org.ow2.bonita.util.GroovyException;
import org.ow2.bonita.util.ProcessBuilder;

/**
 * Testing process variables (string and enumeration types)
 *
 */
public class ProcessVariableTest extends APITestCase {

  public void testNoInitialValue() throws BonitaException {
    URL xpdlUrl = ProcessVariableTest.class.getResource("noInitialValue.xpdl");
    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl));

    ProcessDefinitionUUID processUUID = process.getUUID();

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    Collection<TaskInstance> todoList = getQueryRuntimeAPI().getTaskList(ActivityState.READY);
    assertNotNull(todoList);
    assertFalse(todoList.isEmpty());
    assertEquals(1 ,todoList.size());
    ActivityInstanceUUID taskUUID = todoList.iterator().next().getUUID();
    getRuntimeAPI().startTask(taskUUID, true);
    getRuntimeAPI().finishTask(taskUUID, true);
    assertEquals(InstanceState.STARTED, getQueryRuntimeAPI().getProcessInstance(instanceUUID).getInstanceState());
    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testInitialHugeGroovyScriptVariable() throws Exception {
    ProcessDefinition definition =
      ProcessBuilder.createProcess("huge", "1.2")
      .addStringDataFromScript("script", "${return \"000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"
          + "0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"
          + "0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"
          + "0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"
          + "0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000\"}")
      .addSystemTask("go")
      .done();
    
    definition = getManagementAPI().deploy(getBusinessArchive(definition));
    getRuntimeAPI().instantiateProcess(definition.getUUID());

    getManagementAPI().deleteProcess(definition.getUUID());
  }

  public void testEvaluateGroovyExpressionWithGroovyInitializedVariable() throws Exception {
    ProcessDefinition process =
      ProcessBuilder.createProcess("process", "1.0")
      .addStringDataFromScript("data1", "${'test' + '1'}")
      .addHuman(getLogin())
      .addHumanTask("task1", getLogin())
      .done();

    BusinessArchive businessArchive = getBusinessArchive(process);
    process = getManagementAPI().deploy(businessArchive);

    String result = (String) getRuntimeAPI().evaluateGroovyExpression("${data1}", process.getUUID());
    assertNotNull(result);
    assertEquals("test1", result);

    getManagementAPI().deleteProcess(process.getUUID());
  }

  public void testEvaluateGroovyExpressionWithNullGroovyInitializedVariable() throws Exception {
    ProcessDefinition process =
      ProcessBuilder.createProcess("process", "1.0")
      .addStringData("string", null)
      .addStringDataFromScript("data1", "${string}")
      .addHuman(getLogin())
      .addHumanTask("task1", getLogin())
      .done();

    BusinessArchive businessArchive = getBusinessArchive(process);
    process = getManagementAPI().deploy(businessArchive);

    String result = (String) getRuntimeAPI().evaluateGroovyExpression("${data1}", process.getUUID());
    assertNull(result);

    getManagementAPI().deleteProcess(process.getUUID());
  }

  public void testEvaluateGroovyExpressionWithMultiGroovyInitializedVariable() throws Exception {
    ProcessDefinition process =
      ProcessBuilder.createProcess("process", "1.0")
      .addStringData("string", null)
      .addStringData("str", "hello")
      .addStringDataFromScript("data1", "${string}")
      .addStringDataFromScript("data2", "${data1}")
      .addStringDataFromScript("data3", "${str}")
      .addStringDataFromScript("data4", "${data3}")
      .addStringDataFromScript("data5", "${data2}")
      .addStringDataFromScript("data6", "${data5}")
      .addHuman(getLogin())
      .addHumanTask("task1", getLogin())
      .done();

    BusinessArchive businessArchive = getBusinessArchive(process);
    process = getManagementAPI().deploy(businessArchive);

    String result = (String) getRuntimeAPI().evaluateGroovyExpression("${data1}", process.getUUID());
    assertNull(result);
    result = (String) getRuntimeAPI().evaluateGroovyExpression("${data2}", process.getUUID());
    assertNull(result);
    result = (String) getRuntimeAPI().evaluateGroovyExpression("${data3}", process.getUUID());
    assertNotNull(result);
    assertEquals("hello", result);
    result = (String) getRuntimeAPI().evaluateGroovyExpression("${data4}", process.getUUID());
    assertNotNull(result);
    assertEquals("hello", result);
    result = (String) getRuntimeAPI().evaluateGroovyExpression("${data6}", process.getUUID());
    assertNull(result);

    getManagementAPI().deleteProcess(process.getUUID());
  }

  public void testEvaluateGroovyExpressionWithFailingGroovyInitializedVariable() throws Exception {
    ProcessDefinition process =
      ProcessBuilder.createProcess("process", "1.0")
      .addStringDataFromScript("data1", "${string}")
      .addStringDataFromScript("data2", "${data1}")
      .addStringDataFromScript("data3", "${data2}")
      .addStringDataFromScript("data4", "${data3}")
      .addHuman(getLogin())
      .addHumanTask("task1", getLogin())
      .done();

    BusinessArchive businessArchive = getBusinessArchive(process);
    process = getManagementAPI().deploy(businessArchive);

    try {
      getRuntimeAPI().evaluateGroovyExpression("${data4}", process.getUUID());
      fail("Unable to evaluate data4");
    } catch (GroovyException e) {
      assertTrue(e.getMessage().contains("Unable to evaluate: '${"));
    } 

    getManagementAPI().deleteProcess(process.getUUID());
  }
}
