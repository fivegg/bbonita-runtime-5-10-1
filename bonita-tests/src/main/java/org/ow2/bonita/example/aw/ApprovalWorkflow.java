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
package org.ow2.bonita.example.aw;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.login.LoginContext;

import org.ow2.bonita.example.aw.hook.Accept;
import org.ow2.bonita.example.aw.hook.Reject;
import org.ow2.bonita.example.aw.mapper.ApprovalMapper;
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
 * @author "Pierre Vigneras", "Marc Blachon", "Charles Souillard"
 */
public final class ApprovalWorkflow {

  private static final Logger LOG = Logger.getLogger(ApprovalWorkflow.class.getName());
  public static final String PROCESS_ID = "ApprovalWorkflow";

  private static final String BAR_PREFIX = "-bar=";
  private static final String DECISION_PREFIX = "-decision=";

  private ApprovalWorkflow() { }

  private static void usage(final String[] args) {
    LOG.severe("Usage: java " + ApprovalWorkflow.class.getName() + " " 
        + BAR_PREFIX + "barFile " + DECISION_PREFIX + "<accepted>");
    LOG.severe("\t<accepted>: whether the request is approved "
        + "('y', 'yes' or 'true') or not (other)");
    LOG.severe("");
    LOG.severe("Actual args:");
    if (args == null) {
      LOG.severe("  args is null");  
    } else {
      for (String arg : args) {
        LOG.severe("  arg:" + arg);
      }
    }
    String message = ExceptionManager.getInstance().getFullMessage("bex_AW_1");
    throw new BonitaRuntimeException(message); 
  }

  public static void main(final String[] args) throws Exception {
    if (args == null || args.length != 2) {
      usage(args);
    }
    ProcessDefinition clientProcess = null;
    String accepted = null;
    for (String arg : args) {
      if (arg.startsWith(BAR_PREFIX)) {
        File xpdlFile = new File(arg.substring(BAR_PREFIX.length()));
        clientProcess = ProcessBuilder.createProcessFromXpdlFile(xpdlFile.toURL());
      } else if (arg.startsWith(DECISION_PREFIX)) {
        accepted = arg.substring(DECISION_PREFIX.length());
      } else {
        usage(args);
      }
    }

    final boolean isGranted = "true".equals(accepted) || "y".equals(accepted) || "yes".equals(accepted);

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

    ProcessInstanceUUID instanceUUID = execute(clientProcess, isGranted);

    cleanProcess(instanceUUID);

    loginContext.logout();

    if (LOG.isLoggable(Level.INFO)) {
      LOG.info("\n***** End of ApprovalWorkflow Main. *****\n");
    }
  }

  public static ProcessInstanceUUID execute(final ProcessDefinition clientProcess, final boolean isGranted) throws BonitaException, IOException, ClassNotFoundException {
    //get APIs
    final ManagementAPI managementAPI = AccessorUtil.getAPIAccessor().getManagementAPI();

    //deployment
    BusinessArchive businessArchive = BusinessArchiveFactory.getBusinessArchive(clientProcess, ApprovalMapper.class, Accept.class, Reject.class);
    final ProcessDefinition process = managementAPI.deploy(businessArchive);
    return instantiate(isGranted, process.getUUID());

  }

  public static ProcessInstanceUUID instantiate(final boolean isGranted, final ProcessDefinitionUUID processUUID) throws BonitaException, IOException, ClassNotFoundException {
    //get APIs
    final RuntimeAPI runtimeAPI = AccessorUtil.getAPIAccessor().getRuntimeAPI();
    final QueryRuntimeAPI queryRuntimeAPI = AccessorUtil.getAPIAccessor().getQueryRuntimeAPI();
    //instantiation
    final ProcessInstanceUUID instanceUUID = runtimeAPI.instantiateProcess(processUUID);

    //tasks execution
    Collection<TaskInstance> activities = queryRuntimeAPI.getTaskList(instanceUUID, ActivityState.READY);
    if (activities.isEmpty()) {
      String msg = ExceptionManager.getInstance().getFullMessage("bex_AW_3");
      throw new BonitaRuntimeException(msg);
    }
    while (!activities.isEmpty()) {     
      for (TaskInstance activity : activities) {
        final ActivityInstanceUUID taskUUID = activity.getUUID();
        final String activityId = activity.getActivityName();
        log("Starting task associated to activity: " + activityId);
        runtimeAPI.startTask(taskUUID, true);
        if (activityId.equals("Approval")) {
          log("Setting isGranted variable on activity Approval with value: " + isGranted);
          runtimeAPI.setActivityInstanceVariable(activity.getUUID(), "isGranted", isGranted);
        }
        log("Finishing task associated to activity: " + activityId);
        runtimeAPI.finishTask(taskUUID, true);
        log("Task associated to activity: " + activityId + " finished.");
      }
      activities = queryRuntimeAPI.getTaskList(instanceUUID, ActivityState.READY);
    }
    log("No more activity to execute.");
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

  private static void log(String msg) {
    if (LOG.isLoggable(Level.INFO)) {
      LOG.info(Misc.LINE_SEPARATOR + "***** " + msg + " *****" + Misc.LINE_SEPARATOR);
    }
  }
}
