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
 * 
 * Modified by Matthieu Chaffotte - BonitaSoft S.A.
 **/
package org.ow2.bonita.facade;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ow2.bonita.facade.exception.ActivityNotFoundException;
import org.ow2.bonita.facade.exception.BonitaInternalException;
import org.ow2.bonita.facade.exception.DocumentNotFoundException;
import org.ow2.bonita.facade.exception.DocumentationCreationException;
import org.ow2.bonita.facade.exception.EventNotFoundException;
import org.ow2.bonita.facade.exception.IllegalTaskStateException;
import org.ow2.bonita.facade.exception.InstanceNotFoundException;
import org.ow2.bonita.facade.exception.ProcessNotFoundException;
import org.ow2.bonita.facade.exception.TaskNotFoundException;
import org.ow2.bonita.facade.exception.UncancellableInstanceException;
import org.ow2.bonita.facade.exception.UndeletableInstanceException;
import org.ow2.bonita.facade.exception.VariableNotFoundException;
import org.ow2.bonita.facade.runtime.AttachmentInstance;
import org.ow2.bonita.facade.runtime.ConnectorExecutionDescriptor;
import org.ow2.bonita.facade.runtime.Document;
import org.ow2.bonita.facade.runtime.InitialAttachment;
import org.ow2.bonita.facade.uuid.ActivityDefinitionUUID;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.CatchingEventUUID;
import org.ow2.bonita.facade.uuid.DocumentUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.BonitaConstants;
import org.ow2.bonita.util.GroovyException;

/**
 * To manage process definition, process instance and task life cycle operations as well as to set/add/update variables
 * within activity or instance.
 * 
 * Default states for process, processes instances, tasks (aka manual activities) are:
 * <ul>
 * <li>{@link org.ow2.bonita.facade.def.majorElement.ProcessDefinition.ProcessState States for process}: UNDEPLOYED,
 * DEPLOYED</li>
 * <li>{@link org.ow2.bonita.facade.runtime.InstanceState States for process instance}: INITIAL, STARTED, FINISHED</li>
 * <li>{@link org.ow2.bonita.facade.runtime.ActivityState States for task}: INITIAL, READY, EXECUTING, SUSPENDED,
 * FINISHED</li>
 * </ul>
 * 
 * @author Marc Blachon, Guillaume Porcher, Charles Souillard, Miguel Valdes, Pierre Vigneras
 */
public interface RuntimeAPI {

  /**
   * Creates an instance of the specified process and start the execution. returned instance has STARTED state. If the
   * first activity has StartMode=manual then a task has been created. If the first activity has StartMode=automatic
   * then the automatic behavior of the activity has been started.
   * 
   * @param processUUID
   *          the process UUID.
   * @return the UUID of the created instance.
   * @throws ProcessNotFoundException
   *           if the process has not been found.
   * @throws BonitaInternalException
   *           if an exception occurs.
   */
  ProcessInstanceUUID instantiateProcess(ProcessDefinitionUUID processUUID) throws ProcessNotFoundException;

  /**
   * Creates an instance of the specified process and start the execution at the specified activity. Specified activity
   * must be a start activity (no incoming transitions). returned instance has STARTED state. If the first activity has
   * StartMode=manual then a task has been created. If the first activity has StartMode=automatic then the automatic
   * behavior of the activity has been started.
   * 
   * @param processUUID
   *          the process UUID.
   * @param activityUUI
   *          the start activity UUID.
   * @return the UUID of the created instance.
   * @throws ProcessNotFoundException
   *           if the process has not been found.
   */
  ProcessInstanceUUID instantiateProcess(ProcessDefinitionUUID processUUID, ActivityDefinitionUUID activityUUID)
      throws ProcessNotFoundException;

  /**
   * Creates an instance of the specified process with the added variable map and start the execution. returned instance
   * has STARTED state. If the first activity has StartMode=manual then a task has been created. If the first activity
   * has StartMode=automatic then the automatic behavior of the activity has been started.
   * 
   * @param processUUID
   *          the process UUID.
   * @param variables
   *          variables added to the variables already set within the process definition the variable object can be: a
   *          plain {@link String}, a {@link Boolean}, a {@link Date}, a {@link Long} or a {@link Double}.
   * @return the UUID of the created instance.
   * @throws ProcessNotFoundException
   *           if the process has not been found.
   * @throws BonitaInternalException
   *           if an exception occurs.
   */
  ProcessInstanceUUID instantiateProcess(ProcessDefinitionUUID processUUID, Map<String, Object> variables)
      throws ProcessNotFoundException, VariableNotFoundException;

  /**
   * Creates an instance of the specified process with the added variable map, the default attachments and start the
   * execution. returned instance has STARTED state. If the first activity has StartMode=manual then a task has been
   * created. If the first activity has StartMode=automatic then the automatic behavior of the activity has been
   * started.
   * 
   * @param processUUID
   *          the process UUID.
   * @param variables
   *          variables added to the variables already set within the process definition the variable object can be: a
   *          plain {@link String}, a {@link Boolean}, a {@link Date}, a {@link Long} or a {@link Double}.
   * @param attachments
   *          the attachments
   * @return the UUID of the created instance.
   * @throws ProcessNotFoundException
   *           if the process has not been found.
   * @throws BonitaInternalException
   *           if an exception occurs.
   */
  ProcessInstanceUUID instantiateProcess(ProcessDefinitionUUID processUUID, Map<String, Object> variables,
      Collection<InitialAttachment> attachments) throws ProcessNotFoundException, VariableNotFoundException;

  /**
   * Executes the given task. It is equivalent to call startTask and then finishTask. Only one thing differs: start and
   * finish are executed in the same transaction. If a connector or a Groovy script fails, the activity will be put in
   * the state failed. In addition, if the property throw-exception-on-failure is enabled in the file bonita-server.xml
   * the exception will be thrown. This property is disabled by default.
   * 
   * @param taskUUID
   *          the activity instance UUID
   * @param assignTask
   *          true to assign the task to the logged user; false to don't assign the task.
   * @throws TaskNotFoundException
   *           if the task has not been found.
   * @throws IllegalTaskStateException
   *           if the state of the task has not EXECUTING state.
   */
  void executeTask(ActivityInstanceUUID taskUUID, boolean assignTask) throws TaskNotFoundException,
      IllegalTaskStateException;

  /**
   * Starts the task. If successful, this operation changes task state from READY to EXECUTING.<br>
   * If the boolean assignTask is true the task is also assigned to the logged user otherwise the assignment of the task
   * is not affected by this operation. If a connector or a Groovy script fails, the activity will be put in the state
   * failed. In addition, if the property throw-exception-on-failure is enabled in the file bonita-server.xml the
   * exception will be thrown. This property is disabled by default.
   * 
   * @param taskUUID
   *          the task UUID.
   * @param assignTask
   *          true to assign the task to the logged user; false to don't assign the task.
   * @throws TaskNotFoundException
   *           if the task has not been found.
   * @throws IllegalTaskStateException
   *           if the state of the task has not READY state.
   * @throws BonitaInternalException
   *           if an exception occurs.
   */
  void startTask(ActivityInstanceUUID taskUUID, boolean assignTask) throws TaskNotFoundException,
      IllegalTaskStateException;

  /**
   * Finishes the task. If successful, this operation changes task state from EXECUTING to FINISHED.<br>
   * If the boolean assignTask is true the task is also assigned to the logged user otherwise the assignment of the task
   * is not affected by this operation. If a connector or a Groovy script fails, the activity will be put in the state
   * failed. In addition, if the property throw-exception-on-failure is enabled in the file bonita-server.xml the
   * exception will be thrown. This property is disabled by default.
   * 
   * @param taskUUID
   *          the task UUID.
   * @param assignTask
   *          true to assign the task to the logged user; false to don't assign the task.
   * @throws TaskNotFoundException
   *           if the task has not been found.
   * @throws IllegalTaskStateException
   *           if the state of the task has not EXECUTING state.
   * @throws BonitaInternalException
   *           if an exception occurs.
   */
  void finishTask(ActivityInstanceUUID taskUUID, boolean assignTask) throws TaskNotFoundException,
      IllegalTaskStateException;

  /**
   * Suspends the task if the task has EXECUTING state.<br>
   * If successful, this operation changes task state from EXECUTING to SUSPENDED.<br>
   * If the boolean assignTask is true the task is also assigned to the logged user otherwise the assignment of the task
   * is not affected by this operation.
   * 
   * @param taskUUID
   *          the task UUID.
   * @param assignTask
   *          true to assign the task to the logged user; false to don't assign the task.
   * @throws TaskNotFoundException
   *           if the task has not been found.
   * @throws IllegalTaskStateException
   *           if the state of the task has not either READY or EXECUTING state.
   * @throws BonitaInternalException
   *           if an exception occurs.
   */
  void suspendTask(ActivityInstanceUUID taskUUID, boolean assignTask) throws TaskNotFoundException,
      IllegalTaskStateException;

  /**
   * Resumes the task if the task has SUSPENDED state. If successful, this operation changes task state from SUSPENDED
   * to EXECUTING.<br>
   * If the boolean assignTask is true the task is also assigned to the logged user otherwise the assignment of the task
   * is not affected by this operation.
   * 
   * @param taskUUID
   *          the task UUID.
   * @param assignTask
   *          true to assign the task to the logged user; false to don't assign the task.
   * @throws TaskNotFoundException
   *           if the task has not been found.
   * @throws IllegalTaskStateException
   *           if the state of the task has not SUSPENDED state.
   * @throws BonitaInternalException
   *           if an other exception occurs.
   */
  void resumeTask(ActivityInstanceUUID taskUUID, boolean assignTask) throws TaskNotFoundException,
      IllegalTaskStateException;

  /**
   * Launches the execution of both RoleResolver and Filters for the given task.<br>
   * If a RoleResolver has been defined within the participant referenced by the performer of the task, it is executed.<br>
   * If Filters have been defined within the activity of the task they are also executed.
   * 
   * @param taskUUID
   *          the task UUID.
   * @throws TaskNotFoundException
   *           if the task has not been found.
   * @throws BonitaInternalException
   *           if an other exception occurs.
   */
  void assignTask(ActivityInstanceUUID taskUUID) throws TaskNotFoundException;

  /**
   * Forces to assign the given task to the given actor id. If a set of candidates was already set, this method doesn't
   * update it.
   * 
   * @param taskUUID
   *          the task UUID.
   * @param actorId
   *          the actor id.
   * @throws TaskNotFoundException
   *           if the task has not been found.
   */
  void assignTask(ActivityInstanceUUID taskUUID, String actorId) throws TaskNotFoundException;

  /**
   * Forces to replace the candidates set of the given task by the given candidates set. If a userId was already set,
   * this method doesn't update it.
   * 
   * @param taskUUID
   *          the task UUID.
   * @param candidates
   *          the set of candidate actors.
   * @throws TaskNotFoundException
   *           if the task has not been found.
   */
  void assignTask(ActivityInstanceUUID taskUUID, java.util.Set<String> candidates) throws TaskNotFoundException;

  /**
   * If this task had a userId set, set it to null. If a set of candidates was already set, this method doesn't update
   * it.
   * 
   * @param taskUUID
   *          the task UUID.
   * @throws TaskNotFoundException
   *           if the task has not been found.
   */
  void unassignTask(ActivityInstanceUUID taskUUID) throws TaskNotFoundException;

  /**
   * Searches for variable with id variableId within the given process instance with ProcessInstanceUUID instanceUUID.
   * For XML types, see {@link #setVariable(ActivityInstanceUUID, String, Object)};
   * 
   * @param instanceUUID
   *          the instance UUID.
   * @param variableId
   *          the variable id.
   * @param variableValue
   *          the variable value (can be: a plain {@link String}, a {@link Boolean}, a {@link Date}, a {@link Long} or a
   *          {@link Double}).
   * @throws InstanceNotFoundException
   *           if the instance has not been found.
   * @throws VariableNotFoundException
   *           if the variable has not been found.
   * @throws BonitaInternalException
   *           if an exception occurs.
   */
  void setProcessInstanceVariable(ProcessInstanceUUID instanceUUID, String variableId, Object variableValue)
      throws InstanceNotFoundException, VariableNotFoundException;

  /**
   * Searches for variable with id variableId within the given process instance with ProcessInstanceUUID instanceUUID.
   * For XML types, see {@link #setVariable(ActivityInstanceUUID, String, Object)};
   * 
   * @param instanceUUID
   *          the instance UUID.
   * @param variables
   *          Map<String, Object> a {@link Long} or a {@link Double}).
   * @throws InstanceNotFoundException
   *           if the instance has not been found.
   * @throws VariableNotFoundException
   *           if the variable has not been found.
   * @throws BonitaInternalException
   *           if an exception occurs.
   */
  void setProcessInstanceVariables(ProcessInstanceUUID instanceUUID, Map<String, Object> variables)
      throws InstanceNotFoundException, VariableNotFoundException;

  /**
   * Searches for variables with the given activity UUID <br>
   * If the activity variable is found, the given value is set.<br>
   * For XML types, see {@link #setVariable(ActivityInstanceUUID, String, Object)}.
   * 
   * @param activityUUID
   *          the activity UUID.
   * @param Map
   *          <String, Object> variables .
   * @throws ActivityNotFoundException
   *           if the activity has not been found.
   * @throws VariableNotFoundException
   *           if the variable has not been found.
   * @throws BonitaInternalException
   *           if an exception occurs.
   */
  void setActivityInstanceVariables(ActivityInstanceUUID activityUUID, Map<String, Object> variables)
      throws ActivityNotFoundException, VariableNotFoundException;

  /**
   * Evaluates an number of expressions using Groovy. It returns an Map<String, Object>
   * 
   * @param expressions
   *          number of expressions
   * @param activityUUID
   *          the activity UUID
   * @param propagate
   *          if true, the values modified by Groovy update Bonita variables
   * @return either an Object if the expression is a Groovy one or a String
   * @throws InstanceNotFoundException
   *           if the instance has not been found.
   * @throws ActivityNotFoundException
   *           if the activity has not been found.
   * @throws GroovyException
   *           if the expression is not a Groovy one.
   */
  Map<String, Object> evaluateGroovyExpressions(final Map<String, String> expressions,
      final ActivityInstanceUUID activityUUID, final Map<String, Object> context, final boolean useActivityScope,
      final boolean propagate) throws InstanceNotFoundException, ActivityNotFoundException, GroovyException;

  /**
   * Evaluates an number of expressions using Groovy. It returns an Map<String, Object>
   * 
   * @param expressions
   *          number of expressions
   * @param processDefinitionUUID
   *          the process definition UUID
   * @param context
   *          the extra variables added in the Groovy context
   * @return either an Object if the expression is a Groovy one or a String
   * @throws ProcessNotFoundException
   *           if the process with the given UUID does not exists.
   * @throws GroovyException
   *           if the expression is not a Groovy one.
   */
  Map<String, Object> evaluateGroovyExpressions(final Map<String, String> expressions,
      final ProcessDefinitionUUID processDefinitionUUID, final Map<String, Object> context)
      throws InstanceNotFoundException, ProcessNotFoundException, GroovyException;

  Map<String, Object> evaluateGroovyExpressions(final Map<String, String> expression,
      final ProcessInstanceUUID processInstanceUUID, final Map<String, Object> context,
      final boolean useInitialVariableValues, final boolean propagate) throws InstanceNotFoundException,
      GroovyException;

  /**
   * Cancels the process instance with the given instance UUID. If the instance represented by the given instanceUUID
   * has a parentInstance, then UncancellableInstanceException is thrown.
   * 
   * @param instanceUUID
   *          the instance UUID.
   * @throws InstanceNotFoundException
   *           if if the instance has not been found.
   * @throws BonitaInternalException
   *           if an exception occurs.
   */
  void cancelProcessInstance(ProcessInstanceUUID instanceUUID) throws InstanceNotFoundException,
      UncancellableInstanceException;

  /**
   * Cancels for each given instance UUID, the process instance. If the instance represented by the given instanceUUID
   * has a parentInstance, then UncancellableInstanceException is thrown.
   * 
   * @param instanceUUIDs
   *          the instance UUIDs.
   * @throws InstanceNotFoundException
   *           if if the instance has not been found.
   * @throws BonitaInternalException
   *           if an exception occurs.
   */
  void cancelProcessInstances(Collection<ProcessInstanceUUID> instanceUUIDs) throws InstanceNotFoundException,
      UncancellableInstanceException;

  /**
   * <p>
   * Searches for variable with id variableId within the given activity instance with the given UUID.<br>
   * If the variable is found within the activity, the given value is set.<br>
   * If the variable is not found within the activity the search is performed within the process instance.<br>
   * If the variable is found within the process instance, the given value is set.
   * </p>
   * <p>
   * <i>For XML data</i><br/>
   * Here is the effect of set variable for XML data:
   * <table border="1">
   * <tr>
   * <td></td>
   * <td>String</td>
   * <td>Document</td>
   * <td>Element</td>
   * <td>Attribute</td>
   * </tr>
   * <tr>
   * <td>"myXmlData"</td>
   * <td>Stores content of XML String into myXmlData</td>
   * <td>Stores a copy of Document in myXMLData</td>
   * <td>Not supported</td>
   * <td>Not supported</td>
   * </tr>
   * <tr>
   * <td>"myXmlData" + {@link BonitaConstants#XPATH_VAR_SEPARATOR} + "/root/node" (or any XPath expression resolving to
   * an element</td>
   * <td>Creates Node from string, and replace node by new node</td>
   * <td>Not supported</td>
   * <td>Replace node by new element</td>
   * <td>Adds attribute to node</td>
   * </tr>
   * <td>"myXmlData" + {@link BonitaConstants#XPATH_VAR_SEPARATOR} + "/root/node/text()"</td>
   * <td>Sets text content of node</td>
   * <td>Not supported</td>
   * <td>Not supported</td>
   * <td>Not supported</td>
   * </tr>
   * <tr>
   * <td>"myXmlData" + {@link BonitaConstants#XPATH_VAR_SEPARATOR} + "/root/@attribute"</td>
   * <td>Sets the value of attribute. Create it if it does not exist</td>
   * <td>Not supported</td>
   * <td>Not supported</td>
   * <td>Sets the value of attribute to the value of passed attribute</td>
   * </tr>
   * <tr>
   * <td>"myXmlData" + {@link BonitaConstants#XPATH_VAR_SEPARATOR} + "/root/node" +
   * {@link BonitaConstants#XPATH_VAR_SEPARATOR} + {@link BonitaConstants#XPATH_APPEND_FLAG}</td>
   * <td>Not supported</td>
   * <td>Not supported</td>
   * <td>Append a copy of element to node</td>
   * <td>Not supported</td>
   * </tr>
   * </table>
   * 
   * @param activityUUID
   *          the activity UUID.
   * @param variableId
   *          the variable id.
   * @param variableValue
   *          the variable value (can be: a plain {@link String}, a {@link Boolean}, a {@link Date}, a {@link Long}, a
   *          {@link Double}, any Java {@link Object}, or any {@link org.w3c.dom.Document} element according to the type
   *          of the variable).
   * @throws VariableNotFoundException
   *           if the variable has not been found.
   * @throws BonitaInternalException
   *           if an exception occurs.
   */
  void setVariable(ActivityInstanceUUID activityUUID, String variableId, Object variableValue)
      throws ActivityNotFoundException, VariableNotFoundException;

  /**
   * Searches for variable with the given activity UUID and variable Id.<br>
   * If the activity variable is found, the given value is set.<br>
   * For XML types, see {@link #setVariable(ActivityInstanceUUID, String, Object)}.
   * 
   * @param activityUUID
   *          the activity UUID.
   * @param variableId
   *          the variable id.
   * @param variableValue
   *          the variable value(can be: a plain {@link String}, a {@link Boolean}, a {@link Date}, a {@link Long} or a
   *          {@link Double}).
   * @throws ActivityNotFoundException
   *           if the activity has not been found.
   * @throws VariableNotFoundException
   *           if the variable has not been found.
   * @throws BonitaInternalException
   *           if an exception occurs.
   */
  void setActivityInstanceVariable(ActivityInstanceUUID activityUUID, String variableId, Object variableValue)
      throws ActivityNotFoundException, VariableNotFoundException;

  /**
   * Starts the activity. If successful, this operation changes activity state from READY to EXECUTING.<br>
   * 
   * @param activityUUID
   *          the activity UUID.
   * @throws ActivityNotFoundException
   *           if the activity has not been found.
   */
  void startActivity(ActivityInstanceUUID activityUUID) throws ActivityNotFoundException;

  /**
   * Deletes all runtime objects for the process instance with the given instance UUID and delete also recorded data
   * from the journal. If this instance was not found in the journal, then the archived instance is deleted from
   * history. If the instance represented by the given instanceUUID has a parentInstance, then
   * UndeletableInstanceException is thrown.
   * 
   * @param instanceUUID
   *          the instance UUID.
   * @throws InstanceNotFoundException
   *           if if the instance has not been found.
   * @throws BonitaInternalException
   *           if an exception occurs.
   */
  void deleteProcessInstance(ProcessInstanceUUID instanceUUID) throws InstanceNotFoundException,
      UndeletableInstanceException;

  /**
   * Deletes for each given instance UUID, all runtime objects
   * 
   * @param instanceUUIDs
   *          the instance UUIDs.
   * @throws InstanceNotFoundException
   *           if if the instance has not been found.
   */
  void deleteProcessInstances(Collection<ProcessInstanceUUID> instanceUUIDs) throws InstanceNotFoundException,
      UndeletableInstanceException;

  /**
   * Deletes all runtime objects for all instances created with the given process UUIDs collection and delete also all
   * there recorded data from the journal. If instances some instances of this process were not found in the journal,
   * then the archived instances are deleted from history.
   * 
   * @param processUUIDs
   *          the collection of process UUIDs.
   * @throws ProcessNotFoundException
   *           if the process with the given UUID does not exists.
   * @throws BonitaInternalException
   *           if an exception occurs.
   */
  void deleteAllProcessInstances(Collection<ProcessDefinitionUUID> processUUIDs) throws ProcessNotFoundException,
      UndeletableInstanceException;

  /**
   * 
   * @param instanceUUID
   * @param activityName
   */
  void enableEventsInFailure(ProcessInstanceUUID instanceUUID, String activityName);

  /**
   * 
   * @param activityUUID
   */
  void enableEventsInFailure(ActivityInstanceUUID activityUUID);

  void enablePermanentEventInFailure(ActivityDefinitionUUID activityUUID);

  /**
   * Deletes all runtime objects for all instances created with the given process UUID and delete also all there
   * recorded data from the journal. If instances some instances of this process were not found in the journal, then the
   * archived instances are deleted from history.
   * 
   * @param processUUID
   *          the process UUID.
   * @throws ProcessNotFoundException
   *           if the process with the given UUID does not exists.
   * @throws BonitaInternalException
   *           if an exception occurs.
   */
  void deleteAllProcessInstances(ProcessDefinitionUUID processUUID) throws ProcessNotFoundException,
      UndeletableInstanceException;

  /**
   * Adds a comment to the ProcessInstance feed. If the activtyInstance is null, it means that this comment is a process
   * comment.
   * 
   * @deprecated replaced by {@link #addComment(ActivityInstanceUUID, String, String)} or
   *             {@link #addComment(ProcessInstanceUUID, String, String)}
   * 
   * @param instanceUUID
   *          the process instance UUID
   * @param activityUUID
   *          the activity UUID, can be null
   * @param message
   *          the comment
   * @param userId
   *          the userId
   * @throws InstanceNotFoundException
   *           if the instance has not been found.
   * @throws ActivityNotFoundException
   *           if the activity has not been found.
   */
  @Deprecated
  void addComment(final ProcessInstanceUUID instanceUUID, ActivityInstanceUUID activityUUID, String message,
      String userId) throws InstanceNotFoundException, ActivityNotFoundException;

  /**
   * Adds a comment to the ProcessInstance feed.
   * 
   * @param instanceUUID
   *          the process instance UUID
   * @param message
   *          the comment
   * @param userId
   *          the userId
   * @throws InstanceNotFoundException
   *           if the instance has not been found.
   */
  void addComment(final ProcessInstanceUUID instanceUUID, final String message, final String userId)
      throws InstanceNotFoundException;

  /**
   * Adds a comment to an ActivityInstance feed.
   * 
   * @param activityUUID
   *          the activity UUID
   * @param message
   *          the comment
   * @param userId
   *          the userId
   * @throws InstanceNotFoundException
   *           if the instance has not been found.
   * @throws ActivityNotFoundException
   *           if the activity has not been found.
   */
  void addComment(final ActivityInstanceUUID activityUUID, final String message, final String userId)
      throws ActivityNotFoundException, InstanceNotFoundException;

  /**
   * Adds a process meta data.
   * 
   * @param uuid
   *          the process UUID.
   * @param key
   *          the key of the meta data
   * @param value
   *          the value of the meta data
   * @throws ProcessNotFound
   *           if the process with the given UUID does not exists.
   */
  void addProcessMetaData(ProcessDefinitionUUID uuid, String key, String value) throws ProcessNotFoundException;

  /**
   * Deletes a process meta data.
   * 
   * @param uuid
   *          the process UUID
   * @param key
   *          the key of the meta data
   * @throws ProcessNotFoundException
   *           if the process with the given UUID does not exists.
   */
  void deleteProcessMetaData(ProcessDefinitionUUID uuid, String key) throws ProcessNotFoundException;

  /**
   * Evaluates an expression using Groovy. If more than one Groovy expressions are in the expression, they must start
   * with ${ and finish with }. It returns an Object if the expression is an only Groovy one or a String if the
   * expression contains String and or more than one Groovy expression.
   * 
   * @param expression
   *          the expression
   * @param instanceUUID
   *          the process instance UUID
   * @param propagate
   *          true if true, the values modified by Groovy update Bonita variables
   * @return either an Object if the expression is a Groovy one or a String
   * @throws InstanceNotFoundException
   *           if the instance has not been found.
   * @throws GroovyException
   *           if the expression is not a Groovy one.
   */
  Object evaluateGroovyExpression(String expression, ProcessInstanceUUID instanceUUID, boolean propagate)
      throws InstanceNotFoundException, GroovyException;

  /**
   * Evaluates an expression using Groovy. If more than one Groovy expressions are in the expression, they must start
   * with ${ and finish with }. It returns an Object if the expression is an only Groovy one or a String if the
   * expression contains String and or more than one Groovy expression.
   * 
   * @param expression
   *          the expression
   * @param processInstanceUUID
   *          the process instance UUID
   * @param context
   *          the extra variables added in the Groovy context
   * @param propagate
   *          if true, the values modified by Groovy update Bonita variables
   * @return either an Object if the expression is a Groovy one or a String
   * @throws InstanceNotFoundException
   *           if the instance has not been found.
   * @throws GroovyException
   *           if the expression is not a Groovy one.
   */
  Object evaluateGroovyExpression(String expression, ProcessInstanceUUID processInstanceUUID,
      Map<String, Object> context, boolean propagate) throws InstanceNotFoundException, GroovyException;

  /**
   * Evaluates an expression using Groovy. If more than one Groovy expressions are in the expression, they must start
   * with ${ and finish with }. It returns an Object if the expression is an only Groovy one or a String if the
   * expression contains String and or more than one Groovy expression.
   * 
   * @param expression
   *          the expression
   * @param processInstanceUUID
   *          the process instance UUID
   * @param context
   *          the extra variables added in the Groovy context
   * @param useInitialVariableValues
   *          if true, use the process variable values at instantiation as context
   * @param propagate
   *          if true, the values values modified by Groovy update Bonita variables
   * @return either an Object if the expression is a Groovy one or a String
   * @throws InstanceNotFoundException
   *           if the instance has not been found.
   * @throws GroovyException
   *           if the expression is not a Groovy one.
   */
  Object evaluateGroovyExpression(String expression, ProcessInstanceUUID processInstanceUUID,
      Map<String, Object> context, boolean useInitialVariableValues, boolean propagate)
      throws InstanceNotFoundException, GroovyException;

  /**
   * Evaluates an expression using Groovy. If more than one Groovy expressions are in the expression, they must start
   * with ${ and finish with }. It returns an Object if the expression is an only Groovy one or a String if the
   * expression contains String and or more than one Groovy expression.
   * 
   * @param expression
   *          the expression
   * @param processDefinitionUUID
   *          the process definition UUID
   * @return either an Object if the expression is a Groovy one or a String
   * @throws ProcessNotFoundException
   *           if the process with the given UUID does not exists.
   * @throws GroovyException
   *           if the expression is not a Groovy one.
   */
  Object evaluateGroovyExpression(String expression, ProcessDefinitionUUID processDefinitionUUID)
      throws ProcessNotFoundException, GroovyException;

  /**
   * Evaluates an expression using Groovy. If more than one Groovy expressions are in the expression, they must start
   * with ${ and finish with }. It returns an Object if the expression is an only Groovy one or a String if the
   * expression contains String and or more than one Groovy expression.
   * 
   * @param expression
   *          the expression
   * @param processDefinitionUUID
   *          the process definition UUID
   * @param context
   *          the extra variables added in the Groovy context
   * @return either an Object if the expression is a Groovy one or a String
   * @throws ProcessNotFoundException
   *           if the process with the given UUID does not exists.
   * @throws GroovyException
   *           if the expression is not a Groovy one.
   */
  Object evaluateGroovyExpression(String expression, ProcessDefinitionUUID processDefinitionUUID,
      Map<String, Object> context) throws ProcessNotFoundException, GroovyException;

  /**
   * Evaluates an expression using Groovy. If more than one Groovy expressions are in the expression, they must start
   * with ${ and finish with }. It returns an Object if the expression is an only Groovy one or a String if the
   * expression contains String and or more than one Groovy expression.
   * 
   * @param expression
   *          the expression
   * @param activityUUID
   *          the activity UUID
   * @param propagate
   *          if true, the values modified by Groovy update Bonita variables
   * @return either an Object if the expression is a Groovy one or a String
   * @throws InstanceNotFoundException
   *           if the instance has not been found.
   * @throws ActivityNotFoundException
   *           if the activity has not been found.
   * @throws GroovyException
   *           if the expression is not a Groovy one.
   */
  Object evaluateGroovyExpression(String expression, ActivityInstanceUUID activityUUID, boolean useActivityScope,
      boolean propagate) throws InstanceNotFoundException, ActivityNotFoundException, GroovyException;

  /**
   * Evaluates an expression using Groovy. If more than one Groovy expressions are in the expression, they must start
   * with ${ and finish with }. It returns an Object if the expression is an only Groovy one or a String if the
   * expression contains String and or more than one Groovy expression.
   * 
   * @param expression
   *          the expression
   * @param activityInstanceUUID
   *          the activity UUID
   * @param context
   *          the extra variables added in the Groovy context
   * @param propagate
   *          if true, the values modified by Groovy update Bonita variables
   * @return either an Object if the expression is a Groovy one or a String
   * @throws InstanceNotFoundException
   *           if the instance has not been found.
   * @throws ActivityNotFoundException
   *           if the activity has not been found.
   * @throws GroovyException
   *           if the expression is not a Groovy one.
   */
  Object evaluateGroovyExpression(String expression, ActivityInstanceUUID activityInstanceUUID,
      Map<String, Object> context, boolean useActivityScope, boolean propagate) throws InstanceNotFoundException,
      ActivityNotFoundException, GroovyException;

  /**
   * Add an attachment to a process instance.
   * 
   * @deprecated replaced by {@link #createDocument(String, ProcessInstanceUUID, String, String, byte[])} and
   *             {@link #addDocumentVersion(DocumentUUID, boolean, String, String, byte[])}
   * @param instanceUUID
   *          the process instance UUID
   * @param name
   *          the attachment name
   * @param label
   *          the attachment label
   * @param description
   *          the attachment description
   * @param fileName
   *          the attachment file name
   * @param metadata
   *          the attachment meta data
   * @param value
   *          the attachment value (cannot be null)
   */
  @Deprecated
  void addAttachment(ProcessInstanceUUID instanceUUID, String name, String label, String description, String fileName,
      Map<String, String> metadata, byte[] value);

  /**
   * Add an attachment to a process instance.
   * 
   * @deprecated replaced by
   *             {@link #createDocumentOrAddDocumentVersion(ProcessInstanceUUID, String, String, String, byte[])} or
   *             {@link #createDocument(String, ProcessInstanceUUID, String, String, byte[])} and
   *             {@link #addDocumentVersion(DocumentUUID, boolean, String, String, byte[])}
   * @param instanceUUID
   *          the process instance UUID
   * @param name
   *          the attachment name
   * @param fileName
   *          the file name
   * @param value
   *          the content of the attachment
   */
  @Deprecated
  void addAttachment(ProcessInstanceUUID instanceUUID, String name, String fileName, byte[] value);

  /**
   * If there are no documents with the given name for this process instance, create a document a new document (same
   * behavior as {@link #createDocument(String, ProcessInstanceUUID, String, String, byte[])}), otherwise add a new
   * document version to the existent one (same behavior as
   * {@link #addDocumentVersion(DocumentUUID, boolean, String, String, byte[])}).
   * 
   * @param name
   *          the name of the document
   * @param instanceUUID
   *          the {@link ProcessInstanceUUID}
   * @param fileName
   *          the filename
   * @param mimeType
   *          the mime type of the file
   * @param content
   *          the content of the file
   * @return a {@link Document}
   * @throws DocumentationCreationException
   * @throws InstanceNotFoundException
   */
  void createDocumentOrAddDocumentVersion(ProcessInstanceUUID instanceUUID, String name, String fileName,
      String mimeType, byte[] value);

  /**
   * Add all attachments to the process instance defined in the attachment instance.
   * 
   * @deprecated replaced by {@link #createDocument(String, ProcessInstanceUUID, String, String, byte[])} and
   *             {@link #addDocumentVersion(DocumentUUID, boolean, String, String, byte[])}
   * @param attachments
   *          the attachment instances
   */
  @Deprecated
  void addAttachments(Map<AttachmentInstance, byte[]> attachments);

  /**
   * Removes all versions of an attachment according to its name.
   * 
   * @deprecated replaced by {@link #deleteDocuments(boolean, DocumentUUID...)}
   * 
   * @param instanceUUID
   *          the process instance UUID
   * @param name
   *          the attachment name
   * @throws InstanceNotFoundException
   *           if the instance has not been found.
   */
  @Deprecated
  void removeAttachment(ProcessInstanceUUID instanceUUID, String name) throws InstanceNotFoundException;

  /**
   * set an activity instance priority
   * 
   * @param activityInstanceUUID
   *          the activity instance UUID
   * @param priority
   *          the priority
   * @throws ActivityNotFoundException
   *           if the activity has not been found.
   */
  void setActivityInstancePriority(ActivityInstanceUUID activityInstanceUUID, int priority)
      throws ActivityNotFoundException;

  /**
   * 
   * @param eventName
   * @param toProcessName
   * @param toActivityName
   * @param activityUUID
   */
  void deleteEvents(final String eventName, final String toProcessName, final String toActivityName,
      final ActivityInstanceUUID activityUUID);

  /**
   * Executes a Connector.
   * 
   * @param connectorClassName
   *          the Connector class name
   * @param parameters
   *          the input parameters to set the connector.
   * @return the result of the connector execution
   * @throws Exception
   *           if any exception occurs
   */
  Map<String, Object> executeConnector(String connectorClassName, Map<String, Object[]> parameters) throws Exception;

  /**
   * Executes a Connector in the context of a process. The ClassLoader uses the process UUID to load the connectors'
   * class and the process data's initial values are added to the groovy evaluation context.
   * 
   * @param connectorClassName
   *          the Connector class name
   * @param parameters
   *          the input parameters to set the connector.
   * @param definitionUUID
   *          the process definition UUID
   * @return the result of the connector execution
   * @throws Exception
   *           if any exception occurs
   */
  Map<String, Object> executeConnector(String connectorClassName, Map<String, Object[]> parameters,
      ProcessDefinitionUUID definitionUUID) throws Exception;

  /**
   * Executes a Connector in the context of a process. The ClassLoader uses the process UUID to load the connectors'
   * class and the process data's initial values are added to the groovy evaluation context.
   * 
   * @param connectorClassName
   *          the Connector class name
   * @param parameters
   *          the input parameters to set the connector.
   * @param definitionUUID
   *          the process definition UUID
   * @param context
   *          additional context for input parameters expressions evaluation
   * @return the result of the connector execution
   * @throws Exception
   *           if any exception occurs
   */
  Map<String, Object> executeConnector(String connectorClassName, Map<String, Object[]> parameters,
      ProcessDefinitionUUID definitionUUID, Map<String, Object> context) throws Exception;

  /**
   * Executes a Connector in the context of a process instance. The ClassLoader uses the process UUID to load the
   * connectors' class and the process data's values are added to the groovy evaluation context.
   * 
   * @param connectorClassName
   *          the Connector class name
   * @param parameters
   *          the input parameters to set the connector
   * @param processInstanceUUID
   *          the process instance UUID
   * @param context
   *          additional context for input parameters expressions evaluation
   * @param useCurrentVariableValues
   *          if true the current variable values should be used for expression evaluation, otherwise the values at
   *          process instantiation are used
   * @return the result of the connector execution
   * @throws Exception
   *           if any exception occurs
   */
  Map<String, Object> executeConnector(String connectorClassName, Map<String, Object[]> parameters,
      ProcessInstanceUUID processInstanceUUID, Map<String, Object> context, boolean useCurrentVariableValues)
      throws Exception;

  /**
   * Executes a Connector in the context of an activity . The ClassLoader uses the process UUID to load the connectors'
   * class and the process and activity data's values are added to the groovy evaluation context.
   * 
   * @param connectorClassName
   *          the Connector class name
   * @param parameters
   *          the input parameters to set the connector.
   * @param activityInstanceUUID
   *          the activity instance UUID
   * @param context
   *          additional context for input parameters expressions evaluation
   * @param useCurrentVariableValues
   *          if true the current variable values should be used for expression evaluation, otherwise the values at step
   *          end are used
   * @return the result of the connector execution
   * @throws Exception
   *           if any exception occurs
   */
  Map<String, Object> executeConnector(String connectorClassName, Map<String, Object[]> parameters,
      ActivityInstanceUUID activityInstanceUUID, Map<String, Object> context, boolean useCurrentVariableValues)
      throws Exception;

  /**
   * Executes a Connector.
   * 
   * @param connectorClassName
   *          the Connector class name
   * @param parameters
   *          the parameters to set the connector.
   * @param classLoader
   *          the classLoader
   * @return the result of the connector execution
   * @throws Exception
   *           if any exception occurs
   */
  Map<String, Object> executeConnector(String connectorClassName, Map<String, Object[]> parameters,
      ClassLoader classLoader) throws Exception;

  /**
   * Execute a list of Connector in the context of a process. The ClassLoader uses the process UUID to load the
   * connectors' class and the process data's initial values are added to the groovy evaluation context.
   * 
   * @param processDefinitionUUID
   *          the process definition UUID
   * @param connectorExecutionDescriptors
   *          the descriptor used for connector execution (class name, input and output parameters)
   * @param context
   *          additional context for input parameters expressions evaluation
   * @return the context updated based on output parameters of connectorExecutionDescriptors
   * @throws Exception
   *           if any exception occurs
   */
  Map<String, Object> executeConnectors(final ProcessDefinitionUUID processDefinitionUUID,
      final List<ConnectorExecutionDescriptor> connectorExecutionDescriptors, final Map<String, Object> context)
      throws Exception;

  /**
   * Execute a list of Connector in the context of a process instance.
   * 
   * @param processInstanceUUID
   *          the process instance UUID
   * @param connectorExecutionDescriptors
   *          the descriptor used for connector execution (class name, input and output parameters)
   * @param context
   *          additional context for input parameters expressions evaluation
   * @param useCurrentVariableValues
   *          if true the current variable values should be used for expression evaluation, otherwise the values at
   *          process instantiation are used
   * @return the context updated based on output parameters of connectorExecutionDescriptors
   * @throws Exception
   *           if any exception occurs
   */
  Map<String, Object> executeConnectors(final ProcessInstanceUUID processInstanceUUID,
      final List<ConnectorExecutionDescriptor> connectorExecutionDescriptors, final Map<String, Object> context,
      boolean useCurrentVariableValues) throws Exception;

  /**
   * Execute a list of Connector in the context of an activity instance.
   * 
   * @param activityInstanceUUID
   *          the activity instance UUID
   * @param connectorExecutionDescriptors
   *          the descriptor used for connector execution (class name, input and output parameters)
   * @param context
   *          additional context for input parameters expressions evaluation
   * @param useCurrentVariableValues
   *          if true the current variable values should be used for expression evaluation, otherwise the values at step
   *          end are used
   * @return the context updated based on output parameters of connectorExecutionDescriptors
   * @throws Exception
   *           if any exception occurs
   */
  Map<String, Object> executeConnectors(final ActivityInstanceUUID activityInstanceUUID,
      final List<ConnectorExecutionDescriptor> connectorExecutionDescriptors, final Map<String, Object> context,
      boolean useCurrentVariableValues) throws Exception;

  /**
   * Executes a Filter.
   * 
   * @param connectorClassName
   *          the Filter class name
   * @param parameters
   *          the parameters to set the connector.
   * @param members
   *          the member set to filter
   * @return the candidates filtered by the connector
   * @throws Exception
   *           if any exception occurs
   */
  Set<String> executeFilter(String connectorClassName, Map<String, Object[]> parameters, Set<String> members)
      throws Exception;

  /**
   * Executes a Filter.
   * 
   * @param connectorClassName
   *          the Filter class name
   * @param parameters
   *          the parameters to set the connector.
   * @param members
   *          the member set to filter
   * @param definitionUUID
   *          the process definition UUID
   * @return the candidates filtered by the connector
   * @throws Exception
   *           if any exception occurs
   */
  Set<String> executeFilter(String connectorClassName, Map<String, Object[]> parameters, Set<String> members,
      ProcessDefinitionUUID definitionUUID) throws Exception;

  /**
   * Executes a Filter.
   * 
   * @param connectorClassName
   *          the Filter class name
   * @param parameters
   *          the parameters to set the connector.
   * @param members
   *          the member set to filter
   * @param classLoader
   *          the classLoader
   * @return the candidates filtered by the connector
   * @throws Exception
   *           if any exception occurs
   */
  Set<String> executeFilter(String connectorClassName, Map<String, Object[]> parameters, Set<String> members,
      ClassLoader classLoader) throws Exception;

  /**
   * Executes a RoleResolver. Warning: Some Role Resovlers cannot run using this method due to the need of parameters
   * not set by the engine like the processInstanceUUID
   * 
   * @param connectorClassName
   *          the RoleResolver class name
   * @param parameters
   *          the parameters to set the connector.
   * @return the members found by the RoleResolver execution
   * @throws Exception
   *           if any exception occurs
   * @deprecated removed in 6.0
   */
  @Deprecated
  Set<String> executeRoleResolver(String connectorClassName, Map<String, Object[]> parameters) throws Exception;

  /**
   * Executes a RoleResolver. Warning: Some Role Resovlers cannot run using this method due to the need of parameters
   * not set by the engine like the processInstanceUUID
   * 
   * @param connectorClassName
   *          the RoleResolver class name
   * @param parameters
   *          the parameters to set the connector.
   * @param definitionUUID
   *          the process definition UUID
   * @return the members found by the RoleResolver execution
   * @throws Exception
   *           if any exception occurs
   * @deprecated removed in 6.0
   */
  @Deprecated
  Set<String> executeRoleResolver(String connectorClassName, Map<String, Object[]> parameters,
      ProcessDefinitionUUID definitionUUID) throws Exception;

  /**
   * Executes a RoleResolver. Warning: Some Role Resovlers cannot run using this method due to the need of parameters
   * not set by the engine like the processInstanceUUID
   * 
   * @param connectorClassName
   *          the RoleResolver class name
   * @param parameters
   *          the parameters to set the connector.
   * @param classLoader
   *          the classLoader
   * @return the members found by the RoleResolver execution
   * @throws Exception
   *           if any exception occurs
   * @deprecated removed in 6.0
   */
  @Deprecated
  Set<String> executeRoleResolver(String connectorClassName, Map<String, Object[]> parameters, ClassLoader classLoader)
      throws Exception;

  /**
   * Skips the task if the task has READY or FAILED state. The execution jumps to the next activity<br>
   * If successful, this operation changes task state from READY to SKIPPED.<br>
   * 
   * @param taskUUID
   *          the task UUID.
   * @param variablesToUpdate
   *          the variables to be updated while skipping task
   * @throws TaskNotFoundException
   *           if the task has not been found.
   * @throws IllegalTaskStateException
   *           if the state of the task has not READY state.
   * @throws BonitaInternalException
   *           if an exception occurs.
   */
  void skipTask(ActivityInstanceUUID taskUUID, Map<String, Object> variablesToUpdate) throws TaskNotFoundException,
      IllegalTaskStateException;

  /**
   * Skips the activity if the activity has READY or FAILED state. The execution jumps to the next activity<br>
   * If successful, this operation changes task state from READY or FAILED to SKIPPED.<br>
   * 
   * @param activityInstanceUUID
   *          the activity instance UUID.
   * @param variablesToUpdate
   *          the variables to be updated while skipping activity
   * @throws ActivityNotFoundException
   *           if the activity has not been found.
   * @throws IllegalTaskStateException
   *           if the state of the activity is not READY or FAILED.
   * @throws BonitaInternalException
   *           if an exception occurs.
   */
  void skip(ActivityInstanceUUID activityInstanceUUID, Map<String, Object> variablesToUpdate)
      throws ActivityNotFoundException, IllegalTaskStateException;

  /**
   * Executes the event.
   * 
   * @param eventUUID
   *          the eventUUID to execute
   * @throws EventNotFoundException
   *           if the event does not exist
   */
  void executeEvent(CatchingEventUUID eventUUID) throws EventNotFoundException;

  /**
   * Deletes the event.
   * 
   * @param eventUUID
   *          the eventUUID to delete
   * @throws EventNotFoundException
   *           if the event does not exist
   */
  void deleteEvent(CatchingEventUUID eventUUID) throws EventNotFoundException;

  /**
   * Deletes events
   * 
   * @param eventUUIDs
   *          the eventUUIDs of events to delete
   * @throws EventNotFoundException
   *           if an event does not exist
   */
  void deleteEvents(Collection<CatchingEventUUID> eventUUIDs) throws EventNotFoundException;

  /**
   * Updates the expiration date of an event.
   * 
   * @param eventUUID
   *          the eventUUID to update
   * @param expiration
   *          the new expiration date
   * @throws EventNotFoundException
   *           if the event does not exist
   */
  void updateExpirationDate(CatchingEventUUID eventUUID, Date expiration) throws EventNotFoundException;

  /**
   * Gets the modified java object based on the given variable expression, initial variable value and attribute value
   * 
   * @param processUUID
   *          the ProcessDefinitionUUID
   * @param variableExpression
   *          the variable expression
   * @param variableValue
   *          initial variable value
   * @param attributeValue
   *          attribute to be set by the variableExpression
   * @return
   */
  Object getModifiedJavaObject(ProcessDefinitionUUID processUUID, String variableExpression, Object variableValue,
      Object attributeValue);

  /**
   * Updates the expected date when the activity should finish.
   * 
   * @param activityUUID
   *          the activity instance UUID
   * @param expectedEndDate
   *          the new value of the expected end date of the activity
   * @throws ActivityNotFoundException
   *           if the activity has not been found.
   */
  void updateActivityExpectedEndDate(final ActivityInstanceUUID activityUUID, Date expectedEndDate)
      throws ActivityNotFoundException;

  /**
   * Create a document associated with a process definition
   * 
   * @param name
   *          the name of the document
   * @param processDefinitionUUID
   *          the {@link ProcessDefinitionUUID}
   * @param fileName
   *          the filename
   * @param mimeType
   *          the mime type of the file
   * @param content
   *          the content of the file
   * @return a {@link Document}
   * @throws DocumentationCreationException
   * @throws InstanceNotFoundException
   */
  Document createDocument(final String name, final ProcessDefinitionUUID processDefinitionUUID, final String fileName,
      final String mimeType, final byte[] content) throws DocumentationCreationException, ProcessNotFoundException;

  /**
   * Create a document associated with a process instance
   * 
   * @param name
   *          the name of the document
   * @param instanceUUID
   *          the {@link ProcessInstanceUUID}
   * @param fileName
   *          the filename
   * @param mimeType
   *          the mime type of the file
   * @param content
   *          the content of the file
   * @return a {@link Document}
   * @throws DocumentationCreationException
   * @throws InstanceNotFoundException
   */
  Document createDocument(final String name, final ProcessInstanceUUID instanceUUID, final String fileName,
      final String mimeType, final byte[] content) throws DocumentationCreationException, InstanceNotFoundException;

  /**
   * add a document version
   * 
   * @param documentUUID
   *          the document UUID
   * @param isMajorVersion
   *          indicate if the document is a major version
   * @param fileName
   *          the filename
   * @param mimeType
   *          the mime type of the file
   * @param content
   *          the content of the file
   * @return a {@link Document}
   * @throws DocumentationCreationException
   * @throws InstanceNotFoundException
   */
  Document addDocumentVersion(final DocumentUUID documentUUID, final boolean isMajorVersion, final String fileName,
      final String mimeType, final byte[] content) throws DocumentationCreationException;

  /**
   * Delete a collection of documents
   * 
   * @param allVersions
   *          indicates if all the versions of the documents should be deleted or only the last
   * @param documentUUIDs
   *          the UUIDs of the documents to delete
   * @throws DocumentNotFoundException
   */
  void deleteDocuments(final boolean allVersions, final DocumentUUID... documentUUIDs) throws DocumentNotFoundException;

}
