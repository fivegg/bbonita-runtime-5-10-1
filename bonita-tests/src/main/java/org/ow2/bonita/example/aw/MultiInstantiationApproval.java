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
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.ow2.bonita.connector.core.PerformerAssignFilter;
import org.ow2.bonita.example.aw.hook.Accept;
import org.ow2.bonita.example.aw.hook.CheckDecision;
import org.ow2.bonita.example.aw.hook.Reject;
import org.ow2.bonita.example.aw.instantiator.ApprovalInstantiator;
import org.ow2.bonita.example.aw.performer.UserPerformerAssign;
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
import org.ow2.bonita.util.BonitaException;
import org.ow2.bonita.util.BonitaRuntimeException;
import org.ow2.bonita.util.BusinessArchiveFactory;
import org.ow2.bonita.util.ExceptionManager;
import org.ow2.bonita.util.Misc;
import org.ow2.bonita.util.ProcessBuilder;
import org.ow2.bonita.util.SimpleCallbackHandler;

/**
 * 
 * @author Charles Souillard
 *
 */
public final class MultiInstantiationApproval {

  private MultiInstantiationApproval() { }

  private static final String BAR_PREFIX = "-bar=";
  private static final String JOHN_DECISION = "-john.decision=";
  private static final String JACK_DECISION = "-jack.decision=";
  private static final String JAMES_DECISION = "-james.decision=";

  private static final Logger LOG = Logger.getLogger(MultiInstantiationApproval.class.getName());
  public static final String PROCESS_ID = "multiInstantiation";

  private static void performTask(final ProcessInstanceUUID instanceUUID, final String user, final Map<String, String> decisions) throws BonitaException, LoginException {
    LoginContext loginContext = new LoginContext("BonitaAuth", new SimpleCallbackHandler(user, "bpm"));
    loginContext.login();
    loginContext.logout();
    loginContext = new LoginContext("BonitaStore", new SimpleCallbackHandler(user, "bpm"));
    loginContext.login();
    String decision = decisions.get(user).toString();
    System.out.println("Logged in as " + user + ". Decision=" + decision);

    if (decision == null || !(decision.equals("yes") || decision.equals("no"))) {
      String message = ExceptionManager.getInstance().getFullMessage(
          "bex_MIA_1", decision);
      throw new BonitaRuntimeException(message);
    }
    final QueryRuntimeAPI queryRuntimeAPI = AccessorUtil.getQueryRuntimeAPI();
    final RuntimeAPI runtimeAPI = AccessorUtil.getRuntimeAPI();

    final Collection<TaskInstance> activities = 
      queryRuntimeAPI.getTaskList(instanceUUID, ActivityState.READY);

    if (activities.isEmpty()) {
      String message = ExceptionManager.getInstance().getFullMessage("bex_MIA_2");
      throw new BonitaRuntimeException(message);
    }
    for (TaskInstance activity : activities) {
      final ActivityInstanceUUID taskUUID = activity.getUUID();
      final String activityId = activity.getActivityName();
      log("Starting task associated to activity: " + activityId);
      runtimeAPI.startTask(taskUUID, true);
      runtimeAPI.setActivityInstanceVariable(activity.getUUID(), "decisionAccepted", decision);
      log("Finishing task associated to activity: " + activityId + ": vote = " + decision);
      runtimeAPI.finishTask(taskUUID, true);
      log("Task associated to activity: " + activityId + " finished.");
    }
    loginContext.logout();
  }

  public static ProcessInstanceUUID execute(final ProcessDefinition clientProcess, final Map<String, String> decisions) throws BonitaException, LoginException, IOException, ClassNotFoundException {
    //get APIs
    final RuntimeAPI runtimeAPI = AccessorUtil.getAPIAccessor().getRuntimeAPI();
    final ManagementAPI managementAPI = AccessorUtil.getAPIAccessor().getManagementAPI();

    LoginContext loginContext = new LoginContext("BonitaAuth", new SimpleCallbackHandler("john", "bpm"));
    loginContext.login();
    loginContext.logout();
    loginContext = new LoginContext("BonitaStore", new SimpleCallbackHandler("john", "bpm"));
    loginContext.login();

    //deployment
    Map<String, byte[]> resources = new HashMap<String, byte[]>();
    String resourceName = PerformerAssignFilter.class.getName() + ".xml";
    URL resourcePath = PerformerAssignFilter.class.getResource(PerformerAssignFilter.class.getSimpleName() + ".xml");
    resources.put(resourceName, Misc.getAllContentFrom(resourcePath));

    BusinessArchive businessArchive = BusinessArchiveFactory.getBusinessArchive(clientProcess, resources, UserPerformerAssign.class, ApprovalInstantiator.class, CheckDecision.class, Accept.class, Reject.class, PerformerAssignFilter.class);
    final ProcessDefinition process = managementAPI.deploy(businessArchive);

    //instantiation
    final ProcessInstanceUUID instanceUUID = runtimeAPI.instantiateProcess(process.getUUID());
    loginContext.logout();

    //john decision
    performTask(instanceUUID, "john", decisions);

    //jack decision
    performTask(instanceUUID, "jack", decisions);

    //james
    performTask(instanceUUID, "james", decisions);

    return instanceUUID;
  }

  private static void usage(final String[] args) {
    LOG.severe("Usage: java " + MultiInstantiationApproval.class.getName() 
        + " " + BAR_PREFIX + "barFile " + JOHN_DECISION + "true/false" + JACK_DECISION + "true/false" + JAMES_DECISION + "true/false");
    LOG.severe("");
    LOG.severe("Actual args:");
    if (args == null) {
      LOG.severe("  args is null");  
    } else {
      for (String arg : args) {
        LOG.severe("  arg:" + arg);
      }
    }
    String message = ExceptionManager.getInstance().getFullMessage("bex_MIA_4");
    throw new BonitaRuntimeException(message);
  }

  public static void main(final String[] args) throws Exception {
    if (args == null || args.length != 4) {
      usage(args);
    }
    Map<String, String> decisions = new HashMap<String, String>();
    ProcessDefinition clientProcess = null;
    for (String arg : args) {
      if (arg.startsWith(BAR_PREFIX)) {
        File xpdlFile = new File(arg.substring(BAR_PREFIX.length()));
        clientProcess = ProcessBuilder.createProcessFromXpdlFile(xpdlFile.toURL());
      } else if (arg.startsWith(JOHN_DECISION)) {
        decisions.put("john", arg.substring(JOHN_DECISION.length()));
      } else if (arg.startsWith(JACK_DECISION)) {
        decisions.put("jack", arg.substring(JACK_DECISION.length()));
      } else if (arg.startsWith(JAMES_DECISION)) {
        decisions.put("james", arg.substring(JAMES_DECISION.length()));
      } else {
        usage(args);
      }
    }

    ProcessInstanceUUID instanceUUID = execute(clientProcess, decisions);

    LoginContext loginContext = new LoginContext("BonitaAuth", new SimpleCallbackHandler("john", "bpm"));
    loginContext.login();
    loginContext.logout();
    loginContext = new LoginContext("BonitaStore", new SimpleCallbackHandler("john", "bpm"));
    loginContext.login();

    cleanProcess(instanceUUID);

    loginContext.logout();

    if (LOG.isLoggable(Level.INFO)) {
      LOG.info("\n***** End of ApprovalWorkflow Main. *****\n");
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
