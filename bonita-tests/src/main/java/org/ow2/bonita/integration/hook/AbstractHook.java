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
package org.ow2.bonita.integration.hook;

import org.ow2.bonita.definition.Hook;
import org.ow2.bonita.facade.QueryAPIAccessor;
import org.ow2.bonita.facade.QueryRuntimeAPI;
import org.ow2.bonita.facade.exception.ActivityNotFoundException;
import org.ow2.bonita.facade.exception.VariableNotFoundException;
import org.ow2.bonita.facade.runtime.ActivityInstance;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.BonitaRuntimeException;


/**
 * @author Pierre Vigneras
 */
public abstract class AbstractHook implements Hook {

  protected abstract void checkState(ActivityInstance activityInstance);

  protected void assertNotNull(String message, Object o) {
    if (o == null) {
      throw new BonitaRuntimeException(message);
    }
  }
  
  protected void assertEquals(String message, Object expected, Object real) {
    if (!expected.equals(real)) {
      throw new BonitaRuntimeException(message);
    }
  }
  
  public void execute(QueryAPIAccessor accessor, ActivityInstance activityInstance) throws Exception {
    assertNotNull("ActivityInstance is null !", activityInstance);

    QueryRuntimeAPI queryRuntimeAPI = accessor.getQueryRuntimeAPI();
    ActivityInstanceUUID activityUUID = activityInstance.getUUID();
    ProcessInstanceUUID instanceUUID = activityInstance.getProcessInstanceUUID();

    checkState(activityInstance);

    // INIT state so there is no record.
    try {
      queryRuntimeAPI.getActivityInstances(instanceUUID, "secondActivity");
      throw new BonitaRuntimeException("this activity does not exist yet in records");
    } catch (ActivityNotFoundException e) {
      // normal execution
    }

    //check if the process variable is accessible and correctly initialized
    String processVarValue = (String) queryRuntimeAPI.getProcessInstanceVariable(instanceUUID, "processVar");
    assertNotNull("processVar is null !", processVarValue);
    assertEquals("processVarValue is not equals to 1: " + processVarValue, "1", processVarValue);

    //check if the firstActivity (current) var is accessible and correctly initialized
    String firstActivityVarValue =
      (String) queryRuntimeAPI.getActivityInstanceVariable(activityUUID, "firstActivityVar");
    assertNotNull("firstActivityVar is null !", firstActivityVarValue);
    assertEquals("firstActivityVarValue is not equals to initial1: " + firstActivityVarValue, "initial1", firstActivityVarValue);

    //check that the secondActivity var is not yet accessible
    try {
      queryRuntimeAPI.getActivityInstanceVariable(activityUUID, "secondActivityVar");
      throw new BonitaRuntimeException("secondActivityVar is not null !");
    } catch (VariableNotFoundException e) {
      // OK
      assertEquals(activityUUID.toString(), activityUUID, e.getActivityUUID());
      assertEquals("secondActivityVar", "secondActivityVar", e.getVariableId());
    }
  }
}
