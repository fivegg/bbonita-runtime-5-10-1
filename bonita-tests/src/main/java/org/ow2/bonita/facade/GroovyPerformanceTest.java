/**
 * Copyright (C) 2012  BonitaSoft S.A..
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.ow2.bonita.facade;

import java.util.HashMap;
import java.util.Set;

import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.runtime.ActivityInstance;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.ProcessBuilder;

/**
 * @author Elias Ricken de Medeiros
 *
 */
public class GroovyPerformanceTest  extends APITestCase {
  
  public void testInitializeBinding () throws Exception {
    evaluateGroovyExpression("${d1}", false, false, 600, false);
  }

  public void testEvaluateGroovyExpressionProcessWithoutAtt () throws Exception {
    evaluateGroovyExpression("${d1}", false, false, 40, false);
  }

  public void testEvaluateGroovyExpressionProcessWithoutAttPropagate () throws Exception {
    evaluateGroovyExpression("${d1}", false, false, 40, true);
  }
  
  public void testEvaluateGroovyExpressionProcessWithAtt () throws Exception {
    evaluateGroovyExpression("${d1}", true, false, 40, false);
  }


  public void testEvaluateComplexGroovyExpressionProcessWithAtt () throws Exception {
    evaluateGroovyExpression("${int i = 0; int j = i + 5;}", true, false, 2000, false);
  }

  public void testEvaluateGroovyExpressionRefAtt () throws Exception {
    evaluateGroovyExpression("${att.getName();}", true, false, 2000, false);
  }

  public void testEvaluateGroovyExpressionRefAttPropagate () throws Exception {
    evaluateGroovyExpression("${d2 = att.getName();}", true, false, 2000, true);
  }

  public void testEvaluateGroovyExpressionProcessWithoutAttActivityScope () throws Exception {
    evaluateGroovyExpression("${d1}", false, true, 60, false);
  }
  
  public void testEvaluateGroovyExpressionProcessWithAttActivityScope () throws Exception {
    evaluateGroovyExpression("${d1}", true, true, 60, false);
  }

  private Object evaluateGroovyExpression(String expression, final boolean addAttachment, boolean useActivity, long maxTime, boolean propagate) throws Exception {
    ProcessDefinition processDefinition = getProcessDefinition(addAttachment);
    processDefinition = getManagementAPI().deploy(getBusinessArchive(processDefinition));
    final ProcessInstanceUUID processInstanceUUID = getRuntimeAPI().instantiateProcess(processDefinition.getUUID());
    if (addAttachment) {
      getRuntimeAPI().setProcessInstanceVariable(processInstanceUUID, "att", "att content".getBytes());
    }
    Set<ActivityInstance> activityInstances = getQueryRuntimeAPI().getActivityInstances(processInstanceUUID, "step1");
    assertEquals(1, activityInstances.size());
    ActivityInstance activityInstance = activityInstances.iterator().next();
    
    final long before = System.currentTimeMillis();
    Object result = null;
    if (useActivity) {
      result = getRuntimeAPI().evaluateGroovyExpression(expression, activityInstance.getUUID(), new HashMap<String, Object>() , false, propagate);
    } else { 
      result = getRuntimeAPI().evaluateGroovyExpression(expression,  processInstanceUUID, new HashMap<String, Object>(), false, propagate);
    }
    final long after = System.currentTimeMillis();
    long totalTime = after - before;
    System.err.println("############## Took: " + totalTime);
    assertTrue("expected less then " + maxTime + " ms, but was " + totalTime,  totalTime < maxTime);
    
    getManagementAPI().deleteProcess(processDefinition.getUUID());
    
    return result;
  }

  private ProcessDefinition getProcessDefinition(final boolean addAttachment) {
    final ProcessBuilder processBuilder = ProcessBuilder.createProcess("patt", "1.0");
    if (addAttachment) {
      processBuilder.addAttachment("att");
    }
    final ProcessDefinition processDefinition = processBuilder
      .addIntegerData("d1", 10)
      .addStringData("d2", "text variable to test performance")
      .addStringData("d3", "text variable to test performance")
      .addStringData("d4", "text variable to test performance")
      .addStringData("d5", "text variable to test performance")
      .addHuman(getLogin())
      .addHumanTask("step1", getLogin())
      .done();
    return processDefinition;
  }

}
