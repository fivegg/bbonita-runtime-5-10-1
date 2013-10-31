package org.ow2.bonita.activity.implementationNo;

import java.net.URL;

import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.BonitaException;

public class ImplNoManuTest extends APITestCase {
  public void testImplNoManu0JoinSplit() throws BonitaException {
    URL xpdlUrl = this.getClass().getResource("implNoManu0JoinSplit.xpdl");
    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl));
    
    ProcessDefinitionUUID processUUID = process.getUUID();
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    checkStopped(instanceUUID, new String[]{});
    // start & terminate "act1" task
    executeTask(instanceUUID, "act1");
    checkExecutedOnce(instanceUUID, new String[]{"act1"});
    getManagementAPI().disable(processUUID);
    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testImplNoManuJoinSplit() throws BonitaException {
    URL xpdlUrl = this.getClass().getResource("implNoManuJoinSplit.xpdl");
    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl));
    
    ProcessDefinitionUUID processUUID = process.getUUID();
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    checkStopped(instanceUUID, new String[]{});
    // start & terminate "act1" task
    executeTask(instanceUUID, "act1");
    checkExecutedOnce(instanceUUID, new String[]{"act1"});
    getManagementAPI().disable(processUUID);
    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testImplNoManuSplit() throws BonitaException {
    URL xpdlUrl = this.getClass().getResource("implNoManuSplit.xpdl");
    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl));
    
    ProcessDefinitionUUID processUUID = process.getUUID();
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    checkStopped(instanceUUID, new String[]{});
    // start & terminate "act1" task
    executeTask(instanceUUID, "act1");
    checkExecutedOnce(instanceUUID, new String[]{"act1"});
    getManagementAPI().disable(processUUID);
    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testImplNoManuJoin() throws BonitaException {
    URL xpdlUrl = this.getClass().getResource("implNoManuJoin.xpdl");
    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl));
    
    ProcessDefinitionUUID processUUID = process.getUUID();
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    checkStopped(instanceUUID, new String[]{});
    // start & terminate "act1" task
    executeTask(instanceUUID, "act1");
    checkExecutedOnce(instanceUUID, new String[]{"act1"});
    getManagementAPI().disable(processUUID);
    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().deleteProcess(processUUID);
  }
}
