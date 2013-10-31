package org.ow2.bonita.activity.route;

import java.net.URL;

import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.BonitaException;

public class JoinAndTest extends APITestCase {

  public void testJoinAnd1Tr() throws BonitaException {
    URL xpdlUrl = this.getClass().getResource("joinAnd1Tr.xpdl");
    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl));
    
    ProcessDefinitionUUID processUUID = process.getUUID();
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    checkExecutedOnce(instanceUUID, new String[]{"r1"});
    getManagementAPI().disable(processUUID);
    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testJoinAnd2Tr() throws BonitaException {
    URL xpdlUrl = this.getClass().getResource("joinAnd2Tr.xpdl");
    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl));
    
    ProcessDefinitionUUID processUUID = process.getUUID();
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    checkExecutedOnce(instanceUUID, new String[]{"r1", "r2", "r3"});
    getManagementAPI().disable(processUUID);
    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().deleteProcess(processUUID);
  }
  
}
