package org.ow2.bonita.util;

import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.identity.Group;
import org.ow2.bonita.facade.identity.User;
import org.ow2.bonita.facade.runtime.InstanceState;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;

public class EncodingTest extends APITestCase{

  public void testCyrillicProcess() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("\u0401", "40.0")
    .addSystemTask("\u0402")
    .done();
    process = getManagementAPI().deploy(getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();
    
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    assertEquals(InstanceState.FINISHED, getQueryRuntimeAPI().getProcessInstance(instanceUUID).getInstanceState());
    getManagementAPI().deleteProcess(processUUID);
  }
  
  public void testAddUserCyrillicCaracter() throws Exception {
    User user = getIdentityAPI().addUser("вый","securedbpm");
    
    User retriedUser = getIdentityAPI().getUserByUUID(user.getUUID());
    assertEquals("вый", retriedUser.getUsername());
    
    getIdentityAPI().removeUserByUUID(user.getUUID());
    
  }
  
  public void testAddGroupCyrillicCaracter() throws Exception {
    Group group = getIdentityAPI().addGroup("вый", "вый", "вый", null);
    
    Group retrievedGroup = getIdentityAPI().getGroupByUUID(group.getUUID());
    assertEquals("вый", retrievedGroup.getName());
    assertEquals("вый", retrievedGroup.getLabel());
    assertEquals("вый", retrievedGroup.getDescription());
    
    getIdentityAPI().removeGroupByUUID(group.getUUID());
  }
  
}
