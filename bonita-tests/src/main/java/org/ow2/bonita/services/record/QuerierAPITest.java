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
package org.ow2.bonita.services.record;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.QueryDefinitionAPI;
import org.ow2.bonita.facade.QueryRuntimeAPI;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition.ProcessState;
import org.ow2.bonita.facade.runtime.ActivityInstance;
import org.ow2.bonita.facade.runtime.ProcessInstance;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.hook.DefaultTestHook;
import org.ow2.bonita.util.BonitaException;
import org.ow2.bonita.util.Misc;

/**
 * @author Pierre Vigneras
 */
public class QuerierAPITest extends APITestCase {
  // This MAPPING is as follow:
  // key: resource name (XPDL file in the classpath)
  // value: a map that gives for a given process processDefinitionUUID the number of activities.
  private static final Map<String, Map<String, Integer>> RESOURCES =
    new HashMap<String, Map<String, Integer>>();

  static {
    String fileName = "splitAndJoin.xpdl";
    String processId = "Example4";
    int expectedActivityNb = 5;
    RESOURCES.put(fileName, new HashMap<String, Integer>());
    RESOURCES.get(fileName).put(processId, expectedActivityNb);

    fileName = "splitXorJoin.xpdl";
    processId = "Example2";
    expectedActivityNb = 4;
    RESOURCES.put(fileName, new HashMap<String, Integer>());
    RESOURCES.get(fileName).put(processId, expectedActivityNb);

    fileName = "SIMPROCESS/Example1.xpdl";
    processId = "Example1";
    expectedActivityNb = 3;
    RESOURCES.put(fileName, new HashMap<String, Integer>());
    RESOURCES.get(fileName).put(processId, expectedActivityNb);

    fileName = "SIMPROCESS/Example3.xpdl";
    processId = "Example3";
    expectedActivityNb = 6;
    RESOURCES.put(fileName, new HashMap<String, Integer>());
    RESOURCES.get(fileName).put(processId, expectedActivityNb);

  };

  public QuerierAPITest() {
    super();
  }

  public void testQueryRuntimeAPI() throws BonitaException {
    int processNb = 0;
    int instanceNb = 0;
    int activityNb = 0;
    Set<ProcessDefinitionUUID> deployedPackagesUUID = new HashSet<ProcessDefinitionUUID>();
    ;
    for (String resourceName : RESOURCES.keySet()) {
      final URL url = QuerierAPITest.class.getResource(resourceName);
      Misc.badStateIfNull(url, "Can't find URL for resource name: " + resourceName);
      final Map<String, Integer> processes = RESOURCES.get(resourceName);
      processNb += processes.size();
      ProcessDefinition deployedProcess = 
        getManagementAPI().deploy(getBusinessArchiveFromXpdl(url, DefaultTestHook.class));
      ProcessDefinitionUUID processUUID = deployedProcess.getUUID();
      
        int randomInstances = Misc.RANDOM.nextInt(3) + 1;  // 10 instances maximum
        while (randomInstances-- > 0) {
          getRuntimeAPI().instantiateProcess(deployedProcess.getUUID());
          instanceNb++;
          activityNb += processes.get(deployedProcess.getName());
        }
      
      deployedPackagesUUID.add(processUUID);
      getManagementAPI().disable(processUUID);
      getManagementAPI().archive(processUUID);
    }

    final QueryDefinitionAPI queryDefinitionAPI = getQueryDefinitionAPI();
    Misc.badStateIfNull(queryDefinitionAPI, "Can't get a definition API");
    final QueryRuntimeAPI querier = getQueryRuntimeAPI();
    Misc.badStateIfNull(querier, "Can't get a querier");
    final Set<ProcessDefinition> packages = queryDefinitionAPI.getProcesses();
    final Set<ProcessDefinition> deployedProcesses = queryDefinitionAPI.getProcesses(ProcessState.ENABLED);
    assertNotNull(packages);
    final Set<ProcessDefinition> processes = queryDefinitionAPI.getProcesses();
    final List<ProcessInstance> instances = new ArrayList<ProcessInstance>();
    final List<ActivityInstance> activities = new ArrayList<ActivityInstance>();

    for (ProcessDefinition process : processes) {
      instances.addAll(querier.getProcessInstances(process.getUUID()));
    }
    for (ProcessInstance instance : instances) {
      activities.addAll(querier.getActivityInstances(instance.getUUID()));
    }
    assertEquals("package number differs : ", 0, deployedProcesses.size());
    assertEquals("package number differs : ", RESOURCES.size(), packages.size());
    assertEquals("process number differs : ", processNb, processes.size());
    assertEquals("instance number differs : ", instanceNb, instances.size());
    assertEquals("activity number differs : ", activityNb, activities.size());

    for (ProcessDefinition proc : processes) {
      getManagementAPI().deleteProcess(proc.getUUID());
    }
  }
}
