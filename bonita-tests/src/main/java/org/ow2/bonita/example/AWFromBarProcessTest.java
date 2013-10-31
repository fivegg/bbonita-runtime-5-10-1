package org.ow2.bonita.example;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.ow2.bonita.APITestCase;
import org.ow2.bonita.example.aw.ApprovalWorkflow;
import org.ow2.bonita.facade.def.element.BusinessArchive;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.AccessorUtil;
import org.ow2.bonita.util.BusinessArchiveFactory;

public class AWFromBarProcessTest extends APITestCase {

  public void testDeployFromV4Bar() throws Exception {
    loginAs("john", "bpm");
    InputStream is = this.getClass().getResourceAsStream("approvalWorkflow.bar");
    
    File f = File.createTempFile("approvalWorkflow", ".bar");
    OutputStream os = new FileOutputStream(f);  
    byte[] buffer = new byte[4096];  
    int bytesRead;  
    while ((bytesRead = is.read(buffer)) != -1) {  
      os.write(buffer, 0, bytesRead);  
    }  
    is.close();  
    os.close();    
    BusinessArchive businessArchive = BusinessArchiveFactory.getBusinessArchive(f);
    
    ProcessDefinition process = AccessorUtil.getManagementAPI().deploy(businessArchive);
    final ProcessDefinitionUUID processUUID = process.getUUID();
    ProcessInstanceUUID instanceUUID = ApprovalWorkflow.instantiate(true, processUUID);
    ApprovalWorkflow.cleanProcess(instanceUUID);

    assertTrue(f.delete());
  }
  
}
