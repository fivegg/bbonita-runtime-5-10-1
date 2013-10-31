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
package org.ow2.bonita.facade;

import java.net.URL;

import org.hibernate.Query;
import org.hibernate.Session;
import org.ow2.bonita.APITestCase;
import org.ow2.bonita.env.Environment;
import org.ow2.bonita.facade.def.element.HookDefinition.Event;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.def.majorElement.impl.ProcessDefinitionImpl;
import org.ow2.bonita.facade.exception.InstanceNotFoundException;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.InstanceState;
import org.ow2.bonita.facade.runtime.ProcessInstance;
import org.ow2.bonita.facade.runtime.impl.ProcessInstanceImpl;
import org.ow2.bonita.facade.uuid.IdFactory;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.AccessorUtil;
import org.ow2.bonita.util.Command;
import org.ow2.bonita.util.EnvTool;
import org.ow2.bonita.util.Misc;
import org.ow2.bonita.util.ProcessBuilder;

/**
 * @author Guillaume Porcher
 */


public class CommandAPITest extends APITestCase {

	public void testTxNotCommited() throws Exception {
		ProcessDefinition process = ProcessBuilder.createProcess("uuid", "1.0")
		.addSystemTask("start")
		.addSystemTask("t1")
		.addSystemTask("t2")
		  .addConnector(Event.automaticOnExit, FailingConnector.class.getName(), true)
		.addSystemTask("end")
		.addTransition("start", "t1")
		.addTransition("t1", "t2")
		.addTransition("t2", "end")
		.done();

		final ManagementAPI managementAPI = AccessorUtil.getManagementAPI();
		managementAPI.deploy(getBusinessArchive(process, null, FailingConnector.class));
		final ProcessDefinitionUUID processUUID = process.getUUID();
		
		final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
		checkState(instanceUUID, ActivityState.FINISHED, "start");
		checkState(instanceUUID, ActivityState.FINISHED, "t1");
		checkState(instanceUUID, ActivityState.FAILED, "t2");
		checkState(instanceUUID, InstanceState.STARTED);
		
		getManagementAPI().deleteProcess(processUUID);
	}
	
  public void testGetProcessInstance() throws Exception {
    URL xpdlUrl = this.getClass().getResource("usertest1_1.0.xpdl");
    getManagementAPI().deployJar("myJar.jar", Misc.generateJar(GetProcessInstance.class));
    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl));
    
    ProcessDefinitionUUID processUUID = process.getUUID();
    ProcessInstanceUUID instanceUUID = null;
    try {
      instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
      ProcessInstance processInstance = getCommandAPI().execute(new GetProcessInstance(instanceUUID));
      assertNotNull(processInstance);
      assertEquals(InstanceState.STARTED, processInstance.getInstanceState());
    } finally {
      
      getRuntimeAPI().deleteProcessInstance(instanceUUID);
      getManagementAPI().disable(processUUID);
      getManagementAPI().deleteProcess(processUUID);
      getManagementAPI().removeJar("myJar.jar");
    }
  }

  public void testGetProcessInstanceBadArgs() throws Exception {
    final ProcessInstanceUUID instanceUUID = IdFactory.getNewInstanceUUID();

    getManagementAPI().deployJar("myJar.jar", Misc.generateJar(GetProcessInstance.class));
    try {
      getCommandAPI().execute(new GetProcessInstance(instanceUUID));
      fail("Expected exception");
    } catch (InstanceNotFoundException e) {
      assertEquals(instanceUUID, e.getInstanceUUID());
    } 

    try {
      getCommandAPI().execute(new GetProcessInstance(null));
      fail("Expected exception");
    } catch (IllegalArgumentException e) {
      // ok
    } 
    getManagementAPI().removeJar("myJar.jar");
  }
  
  public void testDirectQuery() throws Exception {
    URL xpdlUrl = this.getClass().getResource("usertest1_1.0.xpdl");
    
    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl));
    ProcessDefinitionUUID processUUID = process.getUUID();
    
    getManagementAPI().deployJar("myJar.jar", Misc.generateJar(GetProcessDefinition.class));
    ProcessDefinition pack = getCommandAPI().execute(new GetProcessDefinition(processUUID));
    assertNotNull(pack);
    assertEquals(processUUID, pack.getUUID());
    assertEquals("usertest1", pack.getName());

    getManagementAPI().removeJar("myJar.jar");
    getManagementAPI().deleteProcess(processUUID);

  }

  private static class GetProcessInstance implements Command<ProcessInstance> {

    /**
     * 
     */
    private static final long serialVersionUID = -336648734170008833L;
    private final ProcessInstanceUUID instanceUUID;

    public GetProcessInstance(final ProcessInstanceUUID instanceUUID) {
      Misc.checkArgsNotNull(instanceUUID);
      this.instanceUUID = instanceUUID;
    }

    public ProcessInstance execute(final Environment env) throws Exception {
      final ProcessInstance result = EnvTool.getAllQueriers().getProcessInstance(this.instanceUUID);
      if (result == null) {
        throw new InstanceNotFoundException("Useless ID", this.instanceUUID);
      }
      return new ProcessInstanceImpl(result);
    }

  }
  
  private static class GetProcessDefinition implements Command<ProcessDefinition> {

    /**
     * 
     */
    private static final long serialVersionUID = 7324849491306521922L;
    private final ProcessDefinitionUUID processUUID;

    public GetProcessDefinition(final ProcessDefinitionUUID processUUID) {
      Misc.checkArgsNotNull(processUUID);
      this.processUUID = processUUID;
    }

    public ProcessDefinition execute(final Environment env) throws Exception {
      String queryString = "";
      queryString += "select process ";
      queryString += "from org.ow2.bonita.facade.def.majorElement.impl.ProcessDefinitionImpl as process ";
      queryString += "where process.uuid.value = :processUUID";

      Session session = env.get(Session.class);
      Query query = session.createQuery(queryString);
      query.setCacheable(true);
      query.setParameter("processUUID", processUUID.toString());
      ProcessDefinition process = (ProcessDefinition) query.uniqueResult();
      return new ProcessDefinitionImpl(process);
    }

  }
  
}
