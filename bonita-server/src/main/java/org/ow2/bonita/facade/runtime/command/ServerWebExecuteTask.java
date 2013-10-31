/**
 * Copyright (C) 2010  BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
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
package org.ow2.bonita.facade.runtime.command;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.transaction.Status;

import org.ow2.bonita.env.Environment;
import org.ow2.bonita.facade.APIAccessor;
import org.ow2.bonita.facade.QueryRuntimeAPI;
import org.ow2.bonita.facade.RuntimeAPI;
import org.ow2.bonita.facade.exception.UnRollbackableException;
import org.ow2.bonita.facade.impl.StandardAPIAccessorImpl;
import org.ow2.bonita.facade.runtime.InitialAttachment;
import org.ow2.bonita.facade.runtime.TaskInstance;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.Command;
import org.ow2.bonita.util.EnvTool;
import org.ow2.bonita.util.GroovyException;

/***
 * This command starts a task sets the variables and attachments, executes the Groovy scripts and terminate the task
 * 
 * @author Anthony Birembaut, Matthieu Chaffotte, Nicolas Chabanoles, Elias Ricken de Medeiros
 */
public class ServerWebExecuteTask implements Command<Void> {

  private static final long serialVersionUID = -183964674754241232L;
  
  private static final Logger LOG = Logger.getLogger(ServerWebExecuteTask.class.getName());
  
  protected ActivityInstanceUUID taskUUID;
  protected Map<String, Object> processVariables;
  protected Map<String, Object> activityVariables;
  protected Map<String, Object> undefinedVariables;
  protected Set<InitialAttachment> attachments;
  protected List<String> scriptsToExecute;
  protected Map<String, Object> scriptContext;
  
  public ServerWebExecuteTask(final ActivityInstanceUUID taskUUID, final Map<String, Object> processVariables,
      final Map<String, Object> activityVariables, final Map<String, Object> undefinedVariables, final Set<InitialAttachment> attachments, 
      final List<String> scriptsToExecute, final Map<String, Object> scriptContext) {
    super();
    this.taskUUID = taskUUID;
    this.processVariables = processVariables;
    this.activityVariables = activityVariables;
    this.undefinedVariables = undefinedVariables;
    this.attachments = attachments;
    this.scriptsToExecute = scriptsToExecute;
    this.scriptContext = scriptContext;
  }
  
  protected void executeActions(final RuntimeAPI runtimeAPI, final QueryRuntimeAPI queryRuntimeAPI) throws Exception {
    final TaskInstance task = queryRuntimeAPI.getTask(taskUUID);
    final ProcessInstanceUUID instanceUUID = task.getProcessInstanceUUID();
    if (processVariables != null && !processVariables.isEmpty()) {
      runtimeAPI.setProcessInstanceVariables(instanceUUID, processVariables);
    }
    if (activityVariables != null && !activityVariables.isEmpty()) {
      runtimeAPI.setActivityInstanceVariables(taskUUID, activityVariables);
    }
    if(undefinedVariables != null) {
      for (final Entry<String, Object> variableEntry : undefinedVariables.entrySet()) {
        runtimeAPI.setVariable(taskUUID, variableEntry.getKey(), variableEntry.getValue());
      }
    }
    if (attachments != null) {
      for (final InitialAttachment attachment : attachments) {
        runtimeAPI.addAttachment(instanceUUID, attachment.getName(), attachment.getLabel(), attachment.getDescription(), attachment.getFileName(), attachment.getMetaData(), attachment.getContent());
      }
    }
    if (scriptsToExecute != null && !scriptsToExecute.isEmpty()) {
      final Map<String,String> scriptsToExecuteInParallel = new HashMap<String, String>(scriptsToExecute.size());
      for (final String scriptToExecute : scriptsToExecute) {
        scriptsToExecuteInParallel.put(String.valueOf(scriptsToExecuteInParallel.size()), scriptToExecute);
      }
      try {
        runtimeAPI.evaluateGroovyExpressions(scriptsToExecuteInParallel, taskUUID, scriptContext, false, true);
      } catch (final GroovyException e) {
        EnvTool.getTransaction().registerSynchronization(new MarkActivtyAsFailedSynchronization(taskUUID, Status.STATUS_COMMITTED));
        if (LOG.isLoggable(Level.SEVERE)) {
          LOG.log(Level.SEVERE, "Error while executing action. unable to evaluate the groovy expression", e);
        }
        throw new UnRollbackableException("Exception while executiong groovy scripts", e);
      }
    }
  }

  @Override
  public Void execute(final Environment environment) throws Exception {
    final APIAccessor accessor = new StandardAPIAccessorImpl();
    final RuntimeAPI runtimeAPI = accessor.getRuntimeAPI();
    runtimeAPI.startTask(taskUUID, true);
    executeActions(runtimeAPI, accessor.getQueryRuntimeAPI());
    runtimeAPI.finishTask(taskUUID, true);
    return null;
  }

}
