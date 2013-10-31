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
 * 
 * Modified by Matthieu Chaffotte - BonitaSoft S.A.
 **/
package org.ow2.bonita.facade.uuid;

import junit.framework.TestCase;

/**
 * @author Pierre Vigneras
 */
public class UUIDTest extends TestCase {

  public void testProcessInstanceUUID() {
    ProcessDefinitionUUID processUUID = new ProcessDefinitionUUID("processName", "processVersion");
    long instanceNb = 1;
    
    final ProcessInstanceUUID instanceUUID = new ProcessInstanceUUID(processUUID, instanceNb);
    
//    assertEquals(processUUID, instanceUUID.getProcessDefinitionUUID());
//    assertEquals(instanceNb, instanceUUID.getInstanceNb());
  }

  public void testActivityInstanceUUIDFromProcessDefinitionUUID() {
    final ProcessDefinitionUUID definitionUUID = new ProcessDefinitionUUID("test", "test");
    final ProcessInstanceUUID instanceUUID = new ProcessInstanceUUID(definitionUUID, 4);
    String activityId = "1";
    String iterationId = "2";
    String activityInstanceId = "3";
    String loopId = "4";
    
    final ActivityInstanceUUID activityUUID = new ActivityInstanceUUID(instanceUUID, activityId, iterationId, activityInstanceId, loopId);
//    assertEquals(instanceUUID, activityUUID.getProcessInstanceUUID());
//    assertEquals(activityId, activityUUID.getActivityName());
//    assertEquals(iterationId, activityUUID.getIterationId());
//    assertEquals(activityInstanceId, activityUUID.getActivityInstanceId());
//    assertEquals(loopId, activityUUID.getLoopId());
  }

  public void testActivityDefinitionUUIDFromActivityInstanceUUID() {
    final ProcessDefinitionUUID processUUID = new ProcessDefinitionUUID("test", "test");
    final ActivityDefinitionUUID activityDefinitionUUID = new ActivityDefinitionUUID(processUUID, "activity");
    final ProcessInstanceUUID instanceUUID = new ProcessInstanceUUID(processUUID, 4);
    final ActivityInstanceUUID activityInstanceUUID = new ActivityInstanceUUID(instanceUUID, "activity", "1", "2", "3");

//    assertEquals(activityDefinitionUUID, activityInstanceUUID.getActivityDefinitionUUID());
  }

}
