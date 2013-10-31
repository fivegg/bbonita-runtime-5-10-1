/**
 * Copyright (C) 2006  Bull S. A. S.
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
package org.ow2.bonita.example.carpool;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.login.LoginContext;

import org.ow2.bonita.example.carpool.hook.Association;
import org.ow2.bonita.example.carpool.hook.CancelPlace;
import org.ow2.bonita.example.carpool.hook.CancelRequestDL;
import org.ow2.bonita.example.carpool.hook.Initial;
import org.ow2.bonita.example.carpool.hook.SendMessage;
import org.ow2.bonita.example.carpool.hook.UpdateRequest;
import org.ow2.bonita.example.carpool.hook.WaitAnswer;
import org.ow2.bonita.example.carpool.hook.WaitAnswerDL;
import org.ow2.bonita.example.carpool.hook.WaitRequest;
import org.ow2.bonita.example.carpool.hook.WaitRequestDL;
import org.ow2.bonita.facade.ManagementAPI;
import org.ow2.bonita.facade.QueryRuntimeAPI;
import org.ow2.bonita.facade.RuntimeAPI;
import org.ow2.bonita.facade.def.element.BusinessArchive;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.exception.ActivityNotFoundException;
import org.ow2.bonita.facade.exception.InstanceNotFoundException;
import org.ow2.bonita.facade.runtime.ActivityInstance;
import org.ow2.bonita.facade.runtime.InstanceState;
import org.ow2.bonita.facade.runtime.ProcessInstance;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.AccessorUtil;
import org.ow2.bonita.util.BonitaConstants;
import org.ow2.bonita.util.BonitaException;
import org.ow2.bonita.util.BonitaRuntimeException;
import org.ow2.bonita.util.BusinessArchiveFactory;
import org.ow2.bonita.util.ExceptionManager;
import org.ow2.bonita.util.ProcessBuilder;
import org.ow2.bonita.util.SimpleCallbackHandler;
import org.ow2.bonita.util.StandardCallbackHandler;

public final class Carpool {

  private Carpool() { }

  private static final Logger LOG = Logger.getLogger(Carpool.class.getName());
  public static final String PROCESS_ID = "carpool";
  public static final int CANCEL_PLACE_IMMEDIATELY = 1;
  public static final int CANCEL_PLACE_AFTER_3_WAIT = 2;
  public static final int CANCEL_REQUEST = 3;
  public static final int ASSOCIATION = 4;

  private static final long CANCEL_REQUEST_TIME = 5000;
  private static final long WAIT_REQUEST_TIME = 5000;
  private static final long WAIT_ANSWER_TIME = 5000;

  private static final String BAR_PREFIX = "-bar=";
  private static final String MODE_PREFIX = "-mode=";

  private static Map<String, Object> getVariables(final int mode) {
    if (mode == CANCEL_REQUEST) {
      return null;
    } else if (mode == CANCEL_PLACE_IMMEDIATELY) {
      Map<String, Object> variables = new TreeMap<String, Object>();
      variables.put("offerTimeout", "yes");
      return variables;
    } else if (mode == CANCEL_PLACE_AFTER_3_WAIT) {
      return null;
    } else if (mode == ASSOCIATION) {
      Map<String, Object> variables = new TreeMap<String, Object>();
      variables.put("requestFound", "yes");
      variables.put("answerFound", "yes");
      return variables;
    }
    String message = ExceptionManager.getInstance().getFullMessage(
    		"bex_C_1", mode);
    throw new BonitaRuntimeException(message);
  }

  private static void waitForActivityExec(String activityId, int execNb, long maxWaitingTime,
      QueryRuntimeAPI queryRuntimeAPI, ProcessInstanceUUID instanceUUID) throws BonitaException {
    long start = System.currentTimeMillis();
    do {
      try {
        Set<ActivityInstance> activities = queryRuntimeAPI.getActivityInstances(instanceUUID, activityId);
        if (activities != null && activities.size() == execNb) {
          break;
        }
        sleep(500);
      } catch (ActivityNotFoundException e) {
        //nothing : the activity is not yet executed once
        sleep(500);
      } 
    } while ((start + maxWaitingTime) > System.currentTimeMillis());
  }

  private static void sleep(final long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      throw new BonitaRuntimeException(e);
    }
  }

  private static void waitForInstanceEnd(long processingTime, ProcessInstanceUUID instanceUUID,
      QueryRuntimeAPI queryRuntimeAPI) throws BonitaException {
    long deadline = System.currentTimeMillis() + processingTime;

    ProcessInstance processInstance = null;
    while (System.currentTimeMillis() < deadline) {
      try {
        processInstance = queryRuntimeAPI.getProcessInstance(instanceUUID);
        if (InstanceState.FINISHED.equals(processInstance.getInstanceState())) {
          break;
        }
      } catch (InstanceNotFoundException infe) {
        
      } 
      try {
        Thread.sleep(1000);
      } catch (Exception e) {
        throw new BonitaRuntimeException(e);
      }
      processInstance = null;
    }

    if (processInstance != null && !InstanceState.FINISHED.equals(processInstance.getInstanceState())) {
    	String message = ExceptionManager.getInstance().getFullMessage("bex_C_2");
    	throw new BonitaRuntimeException(message);
    }
  }

  private static void usage(final String[] args) {
    LOG.severe("Usage: " + Carpool.class + " " + BAR_PREFIX + "<bar file> " + MODE_PREFIX + "<mode>");
    LOG.severe("Available modes ares : ");
    LOG.severe(" 1 -> CANCEL_PLACE_IMMEDIATELY");
    LOG.severe(" 2 -> CANCEL_PLACE_AFTER_3_WAIT");
    LOG.severe(" 3 -> CANCEL_REQUEST");
    LOG.severe(" 4 -> ASSOCIATION");
    LOG.severe("");
    LOG.severe("Actual args:");
    if (args == null) {
      LOG.severe("  args is null");  
    } else {
      for (String arg : args) {
        LOG.severe("  arg:" + arg);
      }
    }
    String message = ExceptionManager.getInstance().getFullMessage("bex_C_3");
    throw new BonitaRuntimeException(message);
  }
  
  public static void main(final String[] args) throws Exception {

    if (args == null || args.length != 2) {
      usage(args);
    }
    ProcessDefinition clientProcess = null;
    int mode = 0;
    for (String arg : args) {
      if (arg.startsWith(BAR_PREFIX)) {
        File xpdlFile = new File(arg.substring(BAR_PREFIX.length()));
        clientProcess = ProcessBuilder.createProcessFromXpdlFile(xpdlFile.toURL());
      } else if (arg.startsWith(MODE_PREFIX)) {
        mode = new Integer(arg.substring(MODE_PREFIX.length()));
      } else {
        usage(args);
      }
    }

    final String loginMode = System.getProperty(BonitaConstants.LOGIN_MODE_PROPERTY);
    LoginContext loginContext = null;
    if (loginMode != null && BonitaConstants.LOGIN_MODE_TEST.equals(loginMode)) {
      loginContext = new LoginContext("BonitaAuth", new SimpleCallbackHandler("john", "bpm"));
      loginContext.login();
      loginContext.logout();
      loginContext = new LoginContext("BonitaStore", new SimpleCallbackHandler("john", "bpm"));
    } else {
      loginContext = new LoginContext("BonitaStore", new StandardCallbackHandler());
    }
    loginContext.login();

    ProcessInstanceUUID instanceUUID = execute(clientProcess, mode);
    cleanProcess(instanceUUID);

    loginContext.logout();
    if (LOG.isLoggable(Level.INFO)) {
      LOG.info("\n***** End of Carpool Main. *****\n");
    }
    //terminates JVM as there is still some threads running (EventExecutor, EventDispatcherThread) 
    //that will be properly terminated by a shutdownhook
    System.exit(0);
  }

  public static ProcessInstanceUUID execute(final ProcessDefinition clientProcess, final int mode) throws BonitaException, IOException, ClassNotFoundException {
    //get APIs
    final RuntimeAPI runtimeAPI = AccessorUtil.getRuntimeAPI();
    final QueryRuntimeAPI queryRuntimeAPI = AccessorUtil.getQueryRuntimeAPI();
    final ManagementAPI managementAPI = AccessorUtil.getManagementAPI();

    //deployment
    BusinessArchive businessArchive = BusinessArchiveFactory.getBusinessArchive(clientProcess, 
        UpdateRequest.class, SendMessage.class, CancelRequestDL.class, Association.class, 
        WaitRequestDL.class, WaitAnswerDL.class, CancelPlace.class, Initial.class, 
        WaitRequest.class, WaitAnswer.class);
    final ProcessDefinition process = managementAPI.deploy(businessArchive);

    //instantiation
    final ProcessInstanceUUID instanceUUID = runtimeAPI.instantiateProcess(process.getUUID(), getVariables(mode), null);

    long processingTime = 10000;
    if (mode == CANCEL_REQUEST) {
      waitForActivityExec("WaitRequest", 2, WAIT_REQUEST_TIME * 3, queryRuntimeAPI, instanceUUID);
      runtimeAPI.setProcessInstanceVariable(instanceUUID, "requestFound", "yes");
      waitForActivityExec("WaitAnswer", 1, WAIT_ANSWER_TIME * 2, queryRuntimeAPI, instanceUUID);
      runtimeAPI.setProcessInstanceVariable(instanceUUID, "answerTimeout", "yes");
      processingTime += CANCEL_REQUEST_TIME;
    } else if (mode == CANCEL_PLACE_AFTER_3_WAIT) {
      waitForActivityExec("WaitRequest", 3, WAIT_REQUEST_TIME * 4, queryRuntimeAPI, instanceUUID);
      runtimeAPI.setProcessInstanceVariable(instanceUUID, "offerTimeout", "yes");
    }
    waitForInstanceEnd(processingTime, instanceUUID, queryRuntimeAPI);

    return instanceUUID;
  }

  public static void cleanProcess(ProcessInstanceUUID instanceUUID) throws BonitaException {
    final ManagementAPI managementAPI = AccessorUtil.getManagementAPI();
    final QueryRuntimeAPI queryRuntimeAPI = AccessorUtil.getQueryRuntimeAPI();
    final ProcessInstance instance = queryRuntimeAPI.getProcessInstance(instanceUUID);
    final ProcessDefinitionUUID processUUID = instance.getProcessDefinitionUUID();

    //undeployment
    managementAPI.disable(processUUID);
    //journal + history cleaning
    managementAPI.deleteProcess(processUUID);
  }

}
