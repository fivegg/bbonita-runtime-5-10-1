package org.ow2.bonita.facade.rest.javaclient;

import java.util.HashMap;
import java.util.Map;

import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.def.element.BusinessArchive;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.services.record.QuerierAPITest;
import org.ow2.bonita.util.BusinessArchiveFactory;
import org.ow2.bonita.util.Misc;
import org.ow2.bonita.util.ProcessBuilder;

public class DeployBigArchiveTest extends APITestCase{
  public void testDeployBigBusinessArchive() throws Exception{
    ProcessDefinition process = ProcessBuilder.createProcess("bigProcess", "1.0")
    .addHuman(getLogin())
    .addHumanTask("step1", getLogin())
    .done();
    
    final int count = 1000;
    byte[] jar = Misc.generateJar(QuerierAPITest.class);
    Map<String, byte[]> resources = new HashMap<String, byte[]>();
    byte[] tmp;
    for (int i = 0; i < count; i++) {
      tmp = (byte[])jar.clone();
      resources.put("resource" + i, tmp);
    }
    
    System.out.println(jar.length);
    BusinessArchive businessArchive = BusinessArchiveFactory.getBusinessArchive(process, resources);
    
    process = getManagementAPI().deploy(businessArchive);
    
    ProcessDefinition dbProcess = getQueryDefinitionAPI().getProcess(process.getUUID());
    assertNotNull(dbProcess);
    
    getManagementAPI().deleteProcess(process.getUUID());
  }
}
