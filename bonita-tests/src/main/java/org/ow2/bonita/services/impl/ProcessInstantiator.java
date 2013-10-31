package org.ow2.bonita.services.impl;

import java.io.File;

import javax.security.auth.login.LoginContext;

import org.ow2.bonita.facade.ManagementAPI;
import org.ow2.bonita.facade.RuntimeAPI;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.AccessorUtil;
import org.ow2.bonita.util.Misc;
import org.ow2.bonita.util.SimpleCallbackHandler;

public class ProcessInstantiator {

  /**
   * @param args
   */
  public static void main(String[] args) {
    final String processUUID = args[0];
    final String filePath = args[1];

    System.err.println("ProcessInstantiator, processUUID= " + processUUID + ", filePath= " + filePath);

    final File output = new File(filePath);
    try {
      LoginContext loginContext = new LoginContext("BonitaAuth", new SimpleCallbackHandler("john", "bpm"));
      loginContext.login();
      loginContext.logout();
      loginContext = new LoginContext("BonitaStore", new SimpleCallbackHandler("john", "bpm"));
      loginContext.login();

      
      final RuntimeAPI runtimeAPI = AccessorUtil.getRuntimeAPI();
      final ProcessInstanceUUID instanceUUID = runtimeAPI.instantiateProcess(new ProcessDefinitionUUID(processUUID));

      System.err.println("\n\n\nWriting uuid: " + instanceUUID + " to file: " + output + "\n\n\n");
      Misc.write(instanceUUID.toString(), output);
      
      final ManagementAPI managementAPI = AccessorUtil.getManagementAPI();
      managementAPI.deployJar("stopEventExecutor.jar", Misc.generateJar(StopEventExecutorCommand.class));
      AccessorUtil.getCommandAPI().execute(new StopEventExecutorCommand());
      managementAPI.removeJar("stopEventExecutor.jar");

      
      loginContext.logout();
    } catch (Exception e) {
      System.err.println("Got exception: " + e.getMessage() + "\n" + Misc.getStackTraceFrom(e));
      e.printStackTrace();
    }
    System.err.println("END OF THREAD");
    System.exit(0);
  }

}
