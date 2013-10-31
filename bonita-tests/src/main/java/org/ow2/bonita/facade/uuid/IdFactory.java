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
package org.ow2.bonita.facade.uuid;

import java.util.Random;

import org.ow2.bonita.util.Misc;

/**
 * @author Pierre Vigneras
 */
public abstract class IdFactory {

  private static Random random = new Random();
  
  private IdFactory() {

  }

  public static ProcessDefinitionUUID getNewProcessUUID() {
    return new ProcessDefinitionUUID(Misc.getUniqueId("testProcess-"));
  }

  public static String getNewProcessId() {
    return Misc.getHumanReadableId("testProcess$");
  }


  public static ProcessInstanceUUID getNewInstanceUUID() {
    return new ProcessInstanceUUID(new ProcessDefinitionUUID("p", "1.0"), random.nextLong());
  }

  public static String getNewActivityId() {
    return Misc.getHumanReadableId("testActivity$");
  }

  public static ActivityInstanceUUID getNewActivityUUID() {
    return new ActivityInstanceUUID(Misc.getUniqueId("testActivity-"));
  }

  public static ActivityDefinitionUUID getNewActivityDefinitionUUID() {
    return new ActivityDefinitionUUID(Misc.getUniqueId("testDefinitionActivity-"));
  }

  public static ActivityInstanceUUID getNewTaskUUID() {
    return new ActivityInstanceUUID(Misc.getUniqueId("testTask-"));
  }

  public static String getNewIterationId() {
    return Misc.getHumanReadableId("testIteration$");
  }
  
  public static String getNewActivityInstanceId() {
    return Misc.getHumanReadableId("testActInstance$");
  }
}
