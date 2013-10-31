/**
 * Copyright (C) 2009  BonitaSoft S.A..
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

package org.ow2.bonita.perf;

import java.util.List;

import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.light.LightProcessInstance;
import org.ow2.bonita.util.AccessorUtil;
import org.ow2.bonita.util.BusinessArchiveFactory;
import org.ow2.bonita.util.ProcessBuilder;

/**
 * @author Charles Souillard, Matthieu Chaffotte
 */
public class PerfTest extends APITestCase {

  private static final long InstanceCreationExpectedTime = 200;
  
  private boolean isLinux() {
    String os = System.getProperty("os.name").toLowerCase();
    return os.indexOf("nux") >= 0 || os.indexOf("nix") >= 0;
  }

  public void testFillInDBWith20() throws Exception {
    fillInDB(20, 108);
  }

  public void testFillInDBWith100() throws Exception {
    fillInDB(100, 115);
  }

  public void testFillInDBWith200() throws Exception {
    fillInDB(200, 115);
  }

  /*
  public void testFillInDBWith1000() throws Exception {
    testFillInDB(1000, 2000);
  }
  */
  private void fillInDB(final int caseNumber, final long maxQueryTime) throws Exception {
    fillInDB(caseNumber);
    getInstancesFromRuntimeAndcheckExecTime(maxQueryTime, caseNumber);
  }

  private void getInstancesFromRuntimeAndcheckExecTime(long maxQueryTime, int caseNumber) throws Exception {
    final long before = System.currentTimeMillis();
    final List<LightProcessInstance> instances = AccessorUtil.getQueryRuntimeAPI().getLightProcessInstances(0, 20);
    final long execTime = System.currentTimeMillis() - before;
    assertEquals(20, instances.size());
    System.err.println("Test with " + caseNumber + " took " + execTime + "ms");
    assertTrue("Test took much than " + maxQueryTime + "(" + execTime + ")", (execTime <= maxQueryTime));
    getManagementAPI().deleteAllProcesses();
  }

  private void fillInDB(int caseNumber) throws Exception {
    assertEquals(0, AccessorUtil.getQueryRuntimeAPI().getLightProcessInstances().size());

    ProcessDefinition parentProcess = ProcessBuilder.createProcess("parentProcess", "1.0")
    .addSubProcess("aSubProcess", "aSubProcess").done();

    ProcessDefinition subProcess = ProcessBuilder.createProcess("aSubProcess", "1.0")
    .addHuman(getLogin())
    .addSubProcess("zSubSubProcess", "zSubSubProcess")
    .done();

    ProcessDefinition subSubProcess = ProcessBuilder.createProcess("zSubSubProcess", "1.0")
    .addHuman(getLogin())
    .addHumanTask("t", getLogin())
    .done();

    getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(subSubProcess));
    getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(subProcess));
    getManagementAPI().deploy(BusinessArchiveFactory.getBusinessArchive(parentProcess));

    final ProcessDefinitionUUID processUUID = parentProcess.getUUID();

    final long before = System.currentTimeMillis();
    // Start a case.
    for (int i = 0; i < caseNumber; i++) {
      getRuntimeAPI().instantiateProcess(processUUID);
    }
    final long execTime = System.currentTimeMillis() - before;
    System.err.println("Insert of " + caseNumber + " instances took " + execTime + "ms");
    long maxExpectedTime = caseNumber * InstanceCreationExpectedTime;
    if (!isLinux() || isREST()) {
      maxExpectedTime = (long) (maxExpectedTime * 2);
    }
    assertTrue("Instances insert took much than " + maxExpectedTime + "(" + execTime + ")", (execTime <= maxExpectedTime));
  }

}
