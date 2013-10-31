/**
 * Copyright (C) 2007  Bull S. A. S.
 * Bull, Rue Jean Jaures, B.P.68, 78340, Les Clayes-sous-Bois
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
package org.ow2.bonita.deadline;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.def.element.DeadlineDefinition;
import org.ow2.bonita.facade.def.element.impl.ConnectorDefinitionImpl;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.exception.ActivityNotFoundException;
import org.ow2.bonita.facade.exception.DeploymentException;
import org.ow2.bonita.facade.exception.ProcessNotFoundException;
import org.ow2.bonita.facade.exception.UndeletableInstanceException;
import org.ow2.bonita.facade.exception.UndeletableProcessException;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.util.Misc;
import org.ow2.bonita.util.ProcessBuilder;

/**
 * @author Pascal Verdage
 */
public class DeadlineParsingTest extends APITestCase {

  private DeadlineDefinition getDeadline(String condition, String className) {
    ConnectorDefinitionImpl deadline = new ConnectorDefinitionImpl(className);
    deadline.setCondition(condition);
    return deadline;
  }
  
  public void testParseNoExecution() throws DeploymentException, ProcessNotFoundException, 
  ActivityNotFoundException, UndeletableProcessException, UndeletableInstanceException {
    Class< ? > hook = NullHook.class;

    ProcessDefinition clientProcess = ProcessBuilder.createProcess("main", "1.0")
      .addHuman("admin")
      .addHumanTask("a", "admin")
      .addDeadline("1000", hook.getName())
      .done();
    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(clientProcess, null, hook));
    
    ProcessDefinitionUUID processUUID = process.getUUID();

    Set<DeadlineDefinition> deadlines = null;
    try {
      deadlines = getQueryDefinitionAPI().getProcessActivity(processUUID, "a").getDeadlines();
    } finally {
      getManagementAPI().disable(processUUID);
      getManagementAPI().deleteProcess(processUUID);
    }
    Set<DeadlineDefinition> expected = new HashSet<DeadlineDefinition>();
    expected.add(getDeadline("1000", hook.getName()));

    assertSameContent(expected, deadlines);
  }

  public void testParseValidExecutions() throws DeploymentException, ProcessNotFoundException, 
  ActivityNotFoundException, UndeletableProcessException, UndeletableInstanceException {
    Class< ? > hook = NullHook.class;

    ProcessDefinition clientProcess = ProcessBuilder.createProcess("main", "1.0")
    .addHuman("admin")
    .addHumanTask("a", "admin")
    .addDeadline("1000", hook.getName())
    .addDeadline("1000", hook.getName())
    .done();
    
    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(clientProcess, null, hook));
    ProcessDefinitionUUID processUUID = process.getUUID();
    Set<DeadlineDefinition> deadlines = null;
    try {
      deadlines = getQueryDefinitionAPI().getProcessActivity(processUUID, "a").getDeadlines();
    } finally {
      getManagementAPI().disable(processUUID);
      getManagementAPI().deleteProcess(processUUID);
    }

    Set<DeadlineDefinition> expected = new HashSet<DeadlineDefinition>();
    expected.add(getDeadline("1000", hook.getName()));
    expected.add(getDeadline("1000", hook.getName()));
    assertSameContent(expected, deadlines);
  }

  public void testParseInvalidCondition() {
    Class< ? > hook = NullHook.class;

    try {
      ProcessBuilder.createProcess("main", "1.0")
      .addHuman("admin")
      .addHumanTask("a", "admin")
      .addDeadline("2008/05/31/23/59/", hook.getName())
      .done();
      fail();
    } catch (Exception e) {
      assertTrue(e.getMessage().contains("deadline condition"));
    }
  }

  public void testParseNoCondition() {
    Class< ? > hook = NullHook.class;

    try {
      ProcessBuilder.createProcess("main", "1.0")
      .addHuman("admin")
      .addHumanTask("a", "admin")
      .addDeadline(null, hook.getName())
      .done();
    
      fail();
    } catch (IllegalArgumentException e) {
      //assertTrue(e.getMessage(), e.getMessage().contains("DeadlineCondition element is not specified"));
      //expected
    }
  }

  public void testParseValidConditions() throws DeploymentException, 
  ProcessNotFoundException, ActivityNotFoundException, 
  UndeletableProcessException, UndeletableInstanceException {
    Class< ? > hook = NullHook.class;

    ProcessDefinition clientProcess = ProcessBuilder.createProcess("main", "1.0")
    .addHuman("admin")
    .addHumanTask("a", "admin")
    .addDeadline("10000", hook.getName())
    .addDeadline("2008/05/31/23/59/59", hook.getName())
    .addDeadline("2008/05/31/23/59/59/999", hook.getName())
    // the parsing stops as soon as a valid format is detected
    .addDeadline("2008/05/31/23/59/59/999aaa", hook.getName())
    .done();
    
    Set<DeadlineDefinition> deadlines = null;
    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(clientProcess, null, hook));
    
    ProcessDefinitionUUID processUUID = process.getUUID();

    try {
      deadlines = getQueryDefinitionAPI().getProcessActivity(processUUID, "a").getDeadlines();
    } finally {
      getManagementAPI().disable(processUUID);
      getManagementAPI().deleteProcess(processUUID);
    }

    Set<DeadlineDefinition> expected = new HashSet<DeadlineDefinition>();
    expected.add(getDeadline("10000", hook.getName()));
    expected.add(getDeadline("2008/05/31/23/59/59", hook.getName()));
    expected.add(getDeadline("2008/05/31/23/59/59/999", hook.getName()));
    expected.add(getDeadline("2008/05/31/23/59/59/999aaa", hook.getName()));

    assertSameContent(expected, deadlines);
  }

  public void testParseInvalidException() {
    ProcessDefinition clientProcess = ProcessBuilder.createProcess("main", "1.0")
    .addHuman("admin")
    .addHumanTask("a", "admin")
    .addDeadline("2008/05/31/23/59/59", "UnexistingHook")
    .done();
    
    try {
      getManagementAPI().deploy(getBusinessArchive(clientProcess));
      fail();
    } catch (DeploymentException e) {
      assertTrue(Misc.getStackTraceFrom(e), e.getMessage().contains("No Class available"));
    } 
  }

  public void testParseNoException() {
    try {
      ProcessBuilder.createProcess("main", "1.0")
      .addHuman("admin")
      .addHumanTask("a", "admin")
      .addDeadline("2008/05/31/23/59/", null)
      .done();

      fail();
    } catch (IllegalArgumentException e) {
      //assertTrue(Misc.getStackTraceFrom(e), e.getMessage().contains("ExceptionName element is not specified"));
      //expected
    }
  }

  public void testParseValidExceptions() throws DeploymentException, 
  ProcessNotFoundException, ActivityNotFoundException, 
  UndeletableProcessException, UndeletableInstanceException {
    ProcessDefinition clientProcess = ProcessBuilder.createProcess("main", "1.0")
    .addHuman("admin")
    .addHumanTask("a", "admin")
    .addDeadline("10000", NotAHook.class.getName())
    .addDeadline("10000", SetTimeHook.class.getName())
    .addDeadline("10000", SetTimeTxHook.class.getName())
    .done();
    
    Set<DeadlineDefinition> deadlines = null;
    ProcessDefinition process = getManagementAPI().deploy(
        getBusinessArchive(clientProcess, null, NotAHook.class, SetTimeHook.class, SetTimeTxHook.class));
    
    ProcessDefinitionUUID processUUID = process.getUUID();

    try {
      deadlines = getQueryDefinitionAPI().getProcessActivity(processUUID, "a").getDeadlines();
    } finally {
      getManagementAPI().disable(processUUID);
      getManagementAPI().deleteProcess(processUUID);
    }

    Set<DeadlineDefinition> expected = new HashSet<DeadlineDefinition>();
    expected.add(getDeadline("10000", NotAHook.class.getName()));
    expected.add(getDeadline("10000", SetTimeHook.class.getName()));
    expected.add(getDeadline("10000", SetTimeTxHook.class.getName()));
    assertSameContent(expected, deadlines);
  }

  public void testSameDeadline() throws DeploymentException, 
  ProcessNotFoundException, ActivityNotFoundException, 
  UndeletableProcessException, UndeletableInstanceException {
    Class< ? > hook = NullHook.class;
    ProcessDefinition clientProcess = ProcessBuilder.createProcess("main", "1.0")
    .addHuman("admin")
    .addHumanTask("a", "admin")
    .addDeadline("1000", hook.getName())
    .addDeadline("1000", hook.getName())
    .addDeadline("1000", hook.getName())
    .done();
    
      
    Set<DeadlineDefinition> deadlines = null;

    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(clientProcess, null, hook));
    
    ProcessDefinitionUUID processUUID = process.getUUID();

    try {
      deadlines = getQueryDefinitionAPI().getProcessActivity(processUUID, "a").getDeadlines();
    } finally {
      getManagementAPI().disable(processUUID);
      getManagementAPI().deleteProcess(processUUID);
    }

    assertEquals(3, deadlines.size());
    Set<DeadlineDefinition> expected = new HashSet<DeadlineDefinition>();
    expected.add(getDeadline("1000", hook.getName()));
    expected.add(getDeadline("1000", hook.getName()));
    expected.add(getDeadline("1000", hook.getName()));
    assertSameContent(expected, deadlines);
  }


  public static void assertSameContent(Set<DeadlineDefinition> expected, Set<DeadlineDefinition> actual) {
    if (actual == null) {
      assertNull("actual is null, but not expected", expected);
    } else {
      assertNotNull("expected is null, but not actual", expected);
      assertEquals(expected.size(), actual.size());
      List<DeadlineDefinition> availableValues = new ArrayList<DeadlineDefinition>(expected);
      for (DeadlineDefinition actualDef : actual) {
        for (DeadlineDefinition availableDef : availableValues) {
          if (actualDef.getClassName().equals(availableDef.getClassName())
              && actualDef.getCondition().equals(availableDef.getCondition())) {
            availableValues.remove(availableDef);
            break;
          }
        }
      }
      assertEquals(availableValues.size() + " expected elements were not found", 0, availableValues.size());
    }
  }
  
}
