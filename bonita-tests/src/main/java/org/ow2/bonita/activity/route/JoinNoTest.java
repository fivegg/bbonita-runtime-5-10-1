package org.ow2.bonita.activity.route;

import java.net.URL;

import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.BonitaException;

public class JoinNoTest extends APITestCase {
  public void testJoinNo1Tr() throws BonitaException {
    URL xpdlUrl = this.getClass().getResource("joinNo1Tr.xpdl");
    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl));
    
    ProcessDefinitionUUID processUUID = process.getUUID();
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    checkExecutedOnce(instanceUUID, new String[]{"r1"});
    getManagementAPI().disable(processUUID);
    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  /**
   * Check that no join is parsed as a XOR join
   */
  public void testJoinNo2Tr() throws BonitaException {
    URL xpdlUrl = this.getClass().getResource("joinNo2Tr.xpdl");
    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl));
    
    ProcessDefinitionUUID processUUID = process.getUUID();
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    checkExecutedOnce(instanceUUID, new String[]{"r1","r2","r3"});
    //checkOnlyOneExecuted(instanceUUID, new String[]{"r1","r2"});
    getManagementAPI().disable(processUUID);
    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().deleteProcess(processUUID);
  }
}
