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
package org.ow2.bonita.integration.transition;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.hook.DefaultTestHook;
import org.ow2.bonita.util.BonitaException;

/**
 *
 * @author Miguel Valdes Faura
 *
 */
public class TransitionConditionMultiTypesTest extends APITestCase {

  public void testLeftCondition() throws BonitaException {
    URL xpdlUrl = this.getClass().getResource("testTransitionConditionMultiTypes.xpdl");
    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl,
        DefaultTestHook.class));
    
    ProcessDefinitionUUID processUUID = process.getUUID();
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    checkExecutedOnce(instanceUUID, new String[]{"initial", "a", "b", "d"});

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testRightCondition() throws BonitaException {
    URL xpdlUrl = this.getClass().getResource("testTransitionConditionMultiTypes.xpdl");
    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl,
        DefaultTestHook.class));
    
    ProcessDefinitionUUID processUUID = process.getUUID();
        
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("enum1", "no");
   
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID, variables, null);

    checkExecutedOnce(instanceUUID, new String[]{"initial", "a", "c", "d"});

    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }
}
