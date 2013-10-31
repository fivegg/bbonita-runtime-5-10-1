package org.ow2.bonita.services.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.ManagementAPI;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.runtime.ProcessInstance;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.AccessorUtil;
import org.ow2.bonita.util.BonitaConstants;
import org.ow2.bonita.util.ProcessBuilder;

public class RestartTest extends APITestCase {

  public void testUUIDNotResetAfterRestart() throws Exception {
    ProcessDefinition process = ProcessBuilder.createProcess("uuidrestart", "1.0")
    .addSystemTask("start")
    .done();

    final ManagementAPI managementAPI = AccessorUtil.getManagementAPI();
    managementAPI.deploy(getBusinessArchive(process));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final File output = new File("instance-" + System.currentTimeMillis() + ".txt");
    output.createNewFile();
    output.deleteOnExit();

    System.err.println("Storing uuid into file: " + output.getAbsolutePath());

    exec(output, processUUID);
    exec(output, processUUID);

    final BufferedReader reader = new BufferedReader(new FileReader(output.getAbsolutePath()));
    final String valueFromFile = reader.readLine();
    reader.close();

    System.err.println("Value read from file: " + valueFromFile);
    final ProcessInstanceUUID instanceUUID2 = new ProcessInstanceUUID(valueFromFile);
    final ProcessInstance instance2 = getQueryRuntimeAPI().getProcessInstance(instanceUUID2);
    assertEquals(2, instance2.getNb());

    getManagementAPI().deleteProcess(processUUID);
  }

  private void exec(final File output, final ProcessDefinitionUUID processUUID) throws Exception {
    final String cp = System.getProperty("surefire.test.class.path");
    final String args = processUUID + " " + output.getAbsolutePath();

    final String command = "java -cp " + cp + " -D" + BonitaConstants.JAAS_PROPERTY + "=" + System.getProperty(BonitaConstants.JAAS_PROPERTY) + " " + ProcessInstantiator.class.getName() + " " + args;

    System.err.println("***** COMMAND\n");
    System.err.println(command);
    System.err.println("\n*****");

    final Process exec1 = Runtime.getRuntime().exec(command);
    
    final InputStream stderr = exec1.getErrorStream();
    final InputStreamReader isr = new InputStreamReader(stderr, BonitaConstants.FILE_ENCONDING);
    final BufferedReader br = new BufferedReader(isr);
    String line = null;
    System.out.println("<TRACE>");
    while ( (line = br.readLine()) != null) {
      System.out.println(line);
    }
    br.close();
    System.out.println("</TRACE>");
    int exitVal = exec1.waitFor();
    System.out.println("Process exitValue: " + exitVal);
     
  }

}
