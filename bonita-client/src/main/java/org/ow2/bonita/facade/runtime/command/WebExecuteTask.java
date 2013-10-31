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

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ow2.bonita.env.Environment;
import org.ow2.bonita.facade.runtime.InitialAttachment;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.util.Command;

/***
 * This command starts a task sets the variables and attachments, executes the Groovy scripts and terminate the task
 * 
 * @author Anthony Birembaut, Matthieu Chaffotte, Nicolas Chabanoles, Elias Ricken de Medeiros
 */
public class WebExecuteTask implements Command<Void> {

  private static final long serialVersionUID = -183964674754241232L;
  
  protected ActivityInstanceUUID taskUUID;
  protected Map<String, Object> processVariables;
  protected Map<String, Object> activityVariables;
  protected Map<String, Object> undefinedVariables;
  protected Set<InitialAttachment> attachments;
  protected List<String> scriptsToExecute;
  protected Map<String, Object> scriptContext;
  
  public WebExecuteTask(final ActivityInstanceUUID taskUUID, final Map<String, Object> processVariables,
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
  
 

  @Override
  public Void execute(final Environment environment) throws Exception {
    final Class<?> commandClass = Class.forName("org.ow2.bonita.facade.runtime.command.ServerWebExecuteTask");
    @SuppressWarnings("unchecked")
    final Constructor<Command<Void>> commandConstructor = (Constructor<Command<Void>>) commandClass.getConstructor(ActivityInstanceUUID.class, Map.class, Map.class, Map.class, Set.class, List.class, Map.class);
    final Command<Void> command = commandConstructor.newInstance(taskUUID, processVariables, activityVariables, undefinedVariables, attachments, scriptsToExecute, scriptContext);
    command.execute(environment);
    return null;
  }

}
