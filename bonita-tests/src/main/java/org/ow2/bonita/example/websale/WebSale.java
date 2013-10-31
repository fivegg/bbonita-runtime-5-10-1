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
package org.ow2.bonita.example.websale;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.login.LoginContext;

import org.ow2.bonita.example.websale.hook.Archive;
import org.ow2.bonita.example.websale.hook.Express;
import org.ow2.bonita.example.websale.hook.Reject;
import org.ow2.bonita.facade.ManagementAPI;
import org.ow2.bonita.facade.QueryRuntimeAPI;
import org.ow2.bonita.facade.RuntimeAPI;
import org.ow2.bonita.facade.def.element.BusinessArchive;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.ProcessInstance;
import org.ow2.bonita.facade.runtime.TaskInstance;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.AccessorUtil;
import org.ow2.bonita.util.BonitaConstants;
import org.ow2.bonita.util.BonitaException;
import org.ow2.bonita.util.BonitaRuntimeException;
import org.ow2.bonita.util.BusinessArchiveFactory;
import org.ow2.bonita.util.ExceptionManager;
import org.ow2.bonita.util.Misc;
import org.ow2.bonita.util.ProcessBuilder;
import org.ow2.bonita.util.SimpleCallbackHandler;
import org.ow2.bonita.util.StandardCallbackHandler;

/**
 * @author "Pierre Vigneras", "Marc Blachon", "Charles Souillard, "Miguel Valdes"
 */
public final class WebSale {

  private WebSale() { }
  
  private static final Logger LOG = Logger.getLogger(WebSale.class.getName());
  public static final String PROCESS_ID = "WebSale"; 
  private static final String BAR_PREFIX = "-bar=";
  private static final String DECISION_PREFIX = "-decision=";
  
  public static ProcessInstanceUUID execute(final ProcessDefinition clientProcess, final String userDecision) throws BonitaException, IOException, ClassNotFoundException {
    //get APIs
    final RuntimeAPI runtimeAPI = AccessorUtil.getRuntimeAPI();
    final QueryRuntimeAPI queryRuntimeAPI = AccessorUtil.getQueryRuntimeAPI();
    final ManagementAPI managementAPI = AccessorUtil.getManagementAPI();

    //deployment
    BusinessArchive businessArchive = BusinessArchiveFactory.getBusinessArchive(clientProcess, Reject.class, Archive.class, Express.class);
    final ProcessDefinition process = managementAPI.deploy(businessArchive);

    //instantiation
    final ProcessInstanceUUID instanceUUID = runtimeAPI.instantiateProcess(process.getUUID());

    Collection<TaskInstance> activities = queryRuntimeAPI.getTaskList(instanceUUID, ActivityState.READY);

    String currentUserDecision = userDecision;
    while (!activities.isEmpty()) {
      if (activities.isEmpty()) {
      	String msg = ExceptionManager.getInstance().getFullMessage("bex_WS_2");
        throw new BonitaRuntimeException(msg);
      }
      for (TaskInstance activity : activities) {
        final ActivityInstanceUUID taskUUID = activity.getUUID();
        final String activityId = activity.getActivityName();
        log("Starting task associated to activity: " + activityId);
        runtimeAPI.startTask(taskUUID, true);
        if (activity.getActivityName().equals("SalesReview")) {
          runtimeAPI.setActivityInstanceVariable(activity.getUUID(), "decision", currentUserDecision);
        }
        log("Finishing task associated to activity: " + activityId);

        runtimeAPI.finishTask(taskUUID, true);
        if (currentUserDecision.equals("moreinfo")) {
          // if we moved to moreinfo we set userDecision to reject to avoid infinite loop
          currentUserDecision = "reject";
        }
        log("Task associated to activity: " + activityId);
      }
      activities = queryRuntimeAPI.getTaskList(instanceUUID, ActivityState.READY);      
    }
    return instanceUUID;
  }

  private static void usage(final String[] args) {
    LOG.severe("Usage: java " + WebSale.class.getName() + " barFile <accepted>");
    LOG.severe("\t<accepted>: whether the request is approved "
        + "('grant'), rejected ('reject' )or more info is required ('moreinfo')");
    LOG.severe("");
    LOG.severe("Actual args:");
    if (args == null) {
      LOG.severe("  args is null");  
    } else {
      for (String arg : args) {
        LOG.severe("  arg:" + arg);
      }
    }
    String message = ExceptionManager.getInstance().getFullMessage("bex_WS_3");
    throw new BonitaRuntimeException(message); 
  }
  
  public static void main(final String[] args) throws Exception {
    if (args == null || args.length != 2) {
      usage(args);
    }
    
    ProcessDefinition clientProcess = null;
    String decision = null;
    for (String arg : args) {
      if (arg.startsWith(BAR_PREFIX)) {
        File xpdlFile = new File(arg.substring(BAR_PREFIX.length()));
        clientProcess = ProcessBuilder.createProcessFromXpdlFile(xpdlFile.toURL());
      } else if (arg.startsWith(DECISION_PREFIX)) {
        decision = arg.substring(DECISION_PREFIX.length());
      } else {
        usage(args);
      }
    }
    
    if (!decision.equals("grant") && !decision.equals("reject") && !decision.equals("moreinfo")) {
      decision = "reject";
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
    
    ProcessInstanceUUID instanceUUID = execute(clientProcess, decision);
    cleanProcess(instanceUUID);

    loginContext.logout();
    if (LOG.isLoggable(Level.INFO)) {
      LOG.info("\n***** End of WebSale Main. *****\n");
    }
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
  
  private static void log(String msg) {
    if (LOG.isLoggable(Level.INFO)) {
      LOG.info(Misc.LINE_SEPARATOR + "***** " + msg + " *****" + Misc.LINE_SEPARATOR);
    }
  }
}
