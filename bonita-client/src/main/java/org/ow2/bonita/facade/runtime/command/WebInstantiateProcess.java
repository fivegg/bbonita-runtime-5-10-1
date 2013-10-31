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
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ow2.bonita.env.Environment;
import org.ow2.bonita.facade.APIAccessor;
import org.ow2.bonita.facade.RuntimeAPI;
import org.ow2.bonita.facade.impl.StandardAPIAccessorImpl;
import org.ow2.bonita.facade.runtime.InitialAttachment;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.Command;
import org.ow2.bonita.util.GroovyException;

/***
 * This command executes the groovy scripts and instantiate a process with initial variable values and attachments
 * 
 * @author Anthony Birembaut, Nicolas Chabanoles
 */
public class WebInstantiateProcess implements Command<ProcessInstanceUUID> {

  private static final long serialVersionUID = -6141460318114068466L;
  
  private static final Logger LOG = Logger.getLogger(WebInstantiateProcess.class.getName());
  
  protected ProcessDefinitionUUID processDefinitionUUID;
  protected Map<String, Object> processVariables;
  protected Set<InitialAttachment> attachments;
  protected List<String> scriptsToExecute;
  protected Map<String, Object> scriptContext;
  
  public WebInstantiateProcess(ProcessDefinitionUUID processDefinitionUUID, Map<String, Object> processVariables,
      Set<InitialAttachment> attachments, List<String> scriptsToExecute,
      Map<String, Object> scriptContext) {
    super();
    this.processDefinitionUUID = processDefinitionUUID;
    this.processVariables = processVariables;
    this.attachments = attachments;
    this.scriptsToExecute = scriptsToExecute;
    this.scriptContext = scriptContext;
  }

  protected void executeActions(final RuntimeAPI runtimeAPI) throws Exception {
    if (scriptsToExecute != null && !scriptsToExecute.isEmpty() ) {
      Map<String,String> scriptsToExecuteInASingleCall = new HashMap<String, String>(scriptsToExecute.size());
      for (String scriptToExecute : scriptsToExecute) {
        scriptsToExecuteInASingleCall.put(String.valueOf(scriptsToExecuteInASingleCall.size()), scriptToExecute);
      }
      try {
        runtimeAPI.evaluateGroovyExpressions(scriptsToExecuteInASingleCall, processDefinitionUUID, scriptContext);
      } catch (GroovyException e) {
        if (LOG.isLoggable(Level.SEVERE)) {
          LOG.log(Level.SEVERE, "Error while executing action. unable to evaluate the groovy expression", e);
        }
        throw new GroovyException(e.getMessage() ,e);
      }
    }
  }
  
  public ProcessInstanceUUID execute(Environment environment) throws Exception {
    
    final APIAccessor accessor = new StandardAPIAccessorImpl();
    final RuntimeAPI runtimeAPI = accessor.getRuntimeAPI();
    executeActions(runtimeAPI);
    return runtimeAPI.instantiateProcess(processDefinitionUUID, processVariables, attachments);
  }

}
