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
package org.ow2.bonita.facade.internal;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

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
import org.ow2.bonita.facade.uuid.ActivityDefinitionUUID;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.CatchingEventUUID;
import org.ow2.bonita.facade.uuid.DocumentUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.BonitaConstants;
import org.ow2.bonita.util.GroovyException;

/**
 * To manage process definition, process instance and task life cycle operations as well as to set/add/update
 * variables within activity or instance.
 *
 * Default states for process, processes instances, tasks (aka manual activities) are:
 * <ul>
 * <li>{@link org.ow2.bonita.facade.def.majorElement.ProcessDefinition.ProcessState States for process}: UNDEPLOYED, DEPLOYED</li>
 * <li>{@link org.ow2.bonita.facade.runtime.InstanceState States for process instance}: INITIAL, STARTED, FINISHED</li>
 * <li>{@link org.ow2.bonita.facade.runtime.ActivityState States for task}: INITIAL, READY, EXECUTING, SUSPENDED, FINISHED</li>
 * </ul>
 * @author Elias Ricken de Medeiros, Matthieu Chaffotte
 *
 */
@Path("/API/runtimeAPI/")
@Produces({"text/*","application/xml"})
@Consumes({"application/x-www-form-urlencoded", "application/xml" })
public interface AbstractRemoteRuntimeAPI extends Remote {

  /**
   * Creates an instance of the specified process and start the execution.
   * returned instance has STARTED state.
   * If the first activity has StartMode=manual then a task has been created.
   * If the first activity has StartMode=automatic then the automatic behavior
   * of the activity has been started.
   * @param processUUID the process UUID.
   * @param options the options map (domain, queryList, user)
   * @return the UUID of the created instance.
   * @throws ProcessNotFoundException
   * @throws RemoteException
   */
  @POST @Path("instantiateProcess/{uuid}")
  ProcessInstanceUUID instantiateProcess(
      @PathParam("uuid") ProcessDefinitionUUID processUUID, 
      @FormParam("options") final Map<String, String> options)
  throws ProcessNotFoundException, RemoteException;

  /**
   * Creates an instance of the specified process and start the execution at the specified activity. 
   * Specified activity must be a start activity (no incoming transitions).
   * returned instance has STARTED state.
   * If the first activity has StartMode=manual then a task has been created.
   * If the first activity has StartMode=automatic then the automatic behavior
   * of the activity has been started.
   * @param processUUID the process UUID.
   * @param activityUUI the start activity UUID.
   * @param options the options map (domain, queryList, user)
   * @return the UUID of the created instance.
   * @throws ProcessNotFoundException if the process has not been found.
   * @throws RemoteException
   */
  @POST	@Path("instantiateProcess/{processUUID}/{activityUUID}")
  ProcessInstanceUUID instantiateProcess(
      @PathParam("processUUID") ProcessDefinitionUUID processUUID, 
      @PathParam("activityUUID") final ActivityDefinitionUUID activityUUID,
      @FormParam("options") final Map<String, String> options)
  throws ProcessNotFoundException, RemoteException; 

  /**
   * Creates an instance of the specified process with the added variable map
   * and start the execution.
   * returned instance has STARTED state.
   * If the first activity has StartMode=manual then a task has been created.
   * If the first activity has StartMode=automatic then the automatic behavior
   * of the activity has been started.
   * @param processUUID the process UUID.
   * @param variables variables added to the variables already set within the process definition
   * the variable object can be: a plain {@link String}, a {@link Boolean}, a {@link Date}, a
   * {@link Long} or a {@link Double}.
   * @param options the options map (domain, queryList, user)
   * @return the UUID of the created instance.
   * @throws ProcessNotFoundException
   * @throws RemoteException
   * @throws VariableNotFoundException
   */
  @POST @Path("instantiateProcessWithVariables/{uuid}")
  ProcessInstanceUUID instantiateProcess(
      @PathParam("uuid") ProcessDefinitionUUID processUUID, 
      @FormParam("variables") Map<String, Object> variables, 
      @FormParam ("options") final Map<String, String> options)
  throws ProcessNotFoundException, RemoteException, VariableNotFoundException;

  /**
   * Starts the task. If successful, this operation changes task state from READY to EXECUTING.<br>
   * If the boolean assignTask is true the task is also assigned to the logged user
   * otherwise the assignment of the task is not affected by this operation.
   * @param taskUUID the task UUID.
   * @param assignTask true to assign the task to the logged user; false to don't assign the task.
   * @param options the options map (domain, queryList, user)
   * @throws TaskNotFoundException
   * @throws IllegalTaskStateException
   * @throws RemoteException
   */
  @POST	@Path("startTask/{taskUUID}/{assignTask}")
  void startTask(
      @PathParam("taskUUID") ActivityInstanceUUID taskUUID,
      @PathParam("assignTask") boolean assignTask, 
      @FormParam("options") final Map<String, String> options)
  throws TaskNotFoundException, IllegalTaskStateException, RemoteException;

  /**
   * Finishes the task. If successful, this operation changes task state from EXECUTING to FINISHED.<br>
   * If the boolean assignTask is true the task is also assigned to the logged user
   * otherwise the assignment of the task is not affected by this operation.
   * @param taskUUID the task UUID.
   * @param taskAssign true to assign the task to the logged user; false to don't assign the task.
   * @param options the options map (domain, queryList, user)
   * @throws TaskNotFoundException
   * @throws IllegalTaskStateException
   * @throws RemoteException
   */
  @POST @Path("finishTask/{taskUUID}/{taskAssign}")
  void finishTask(
      @PathParam("taskUUID") ActivityInstanceUUID taskUUID,
      @PathParam("taskAssign") boolean taskAssign, 
      @FormParam("options") final Map<String, String> options) 
  throws TaskNotFoundException, IllegalTaskStateException, RemoteException;

  /**
   * Executes the given task. It is equivalent to call startFinish and then finishTask.
   * Only one things differs: start and finish are executed in the same transaction.
   * @param taskUUID the activity instance UUID
   * @param assignTask true to assign the task to the logged user; false to don't assign the task.
   * @param options the options map (domain, queryList, user)
   * @throws TaskNotFoundException
   * @throws IllegalTaskStateException
   * @throws RemoteException
   */
  @POST @Path("executeTask/{taskUUID}/{assignTask}") 
  void executeTask(
      @PathParam("taskUUID") ActivityInstanceUUID taskUUID,
      @PathParam("assignTask") boolean assignTask, 
      @FormParam("options") final Map<String, String> options)
  throws TaskNotFoundException, IllegalTaskStateException, RemoteException;

  /**
   * Suspends the task if the task has EXECUTING state.<br>
   * If successful, this operation changes task state from EXECUTING to SUSPENDED.<br>
   * If the boolean assignTask is true the task is also assigned to the logged user
   * otherwise the assignment of the task is not affected by this operation.
   * @param taskUUID the task UUID.
   * @param assignTask true to assign the task to the logged user; false to don't assign the task.
   * @param options the options map (domain, queryList, user)
   * @throws TaskNotFoundException
   * @throws IllegalTaskStateException
   * @throws RemoteException
   */
  @POST @Path("suspendTask/{taskUUID}/{assignTask}")
  void suspendTask(
      @PathParam("taskUUID") ActivityInstanceUUID taskUUID,
      @PathParam("assignTask") boolean assignTask, 
      @FormParam("options") final Map<String, String> options)
  throws TaskNotFoundException, IllegalTaskStateException, RemoteException;

  /**
   * Resumes the task if the task has SUSPENDED state.
   * If successful, this operation changes task state from SUSPENDED to EXECUTING.<br>
   * If the boolean assignTask is true the task is also assigned to the logged user
   * otherwise the assignment of the task is not affected by this operation.
   * @param taskUUID the task UUID.
   * @param taskAssign true to assign the task to the logged user; false to don't assign the task.
   * @param options the options map (domain, queryList, user)
   * @throws TaskNotFoundException
   * @throws IllegalTaskStateException
   * @throws RemoteException
   */
  @POST @Path("resumeTask/{taskUUID}/{taskAssign}") 
  void resumeTask(
      @PathParam("taskUUID") ActivityInstanceUUID taskUUID,
      @PathParam("taskAssign") boolean taskAssign, 
      @FormParam("options") final Map<String, String> options)
  throws TaskNotFoundException, IllegalTaskStateException, RemoteException;

  /**
   * Searches for variable with id variableId within the given process instance
   * with ProcessInstanceUUID instanceUUID.
   * For XML types, see {@link #setVariable(ActivityInstanceUUID, String, Object)};
   * @param instanceUUID the instance UUID.
   * @param variableId the variable id.
   * @param variableValue the variable value (can be: a plain {@link String}, a {@link Boolean}, a {@link Date},
   * a {@link Long} or a {@link Double}).
   * @param options the options map (domain, queryList, user)
   * @throws InstanceNotFoundException
   * @throws RemoteException
   * @throws VariableNotFoundException
   */
  @POST @Path("setProcessInstanceVariable/{instanceUUID}") 
  void setProcessInstanceVariable(
      @PathParam("instanceUUID") ProcessInstanceUUID instanceUUID,
      @FormParam("variableId") String variableId, 
      @FormParam("variableValue") Object variableValue,
      @FormParam("options") final Map<String, String> options)
  throws InstanceNotFoundException, RemoteException, VariableNotFoundException;

  /**
   * Searches for variables with id variableId within the given process instance
   * with ProcessInstanceUUID instanceUUID.
   * For XML types, see {@link #setVariable(ActivityInstanceUUID, String, Object)};
   * @param instanceUUID the instance UUID.
   * @param variables the variables map.
   * @param options the options map (domain, queryList, user)
   * @throws InstanceNotFoundException
   * @throws RemoteException
   * @throws VariableNotFoundException
   */
  @POST
  @Path("setProcessInstanceVariables/{instanceUUID}")
  void setProcessInstanceVariables(
      @PathParam("instanceUUID") ProcessInstanceUUID instanceUUID,
      @FormParam("variables") Map<String, Object> variables,
      @FormParam("options") final Map<String, String> options)
  throws InstanceNotFoundException, RemoteException, VariableNotFoundException;

  /**
   * Searches for variable with the given activity UUID and variable Id.<br>
   * If the activity variable is found, the given value is set.<br>
   * For XML types, see {@link #setVariable(ActivityInstanceUUID, String, Object)}.
   * @param activityUUID the activity UUID.
   * @param variableId the variable id.
   * @param variableValue the variable value(can be: a plain {@link String}, a {@link Boolean}, a {@link Date},
   * a {@link Long} or a {@link Double}).
   * @param options the options map (domain, queryList, user)
   * @throws ActivityNotFoundException
   * @throws VariableNotFoundException
   * @throws RemoteException
   */
  @POST @Path("setActivityInstanceVariable/{activityUUID}") 
  void setActivityInstanceVariable(
      @PathParam("activityUUID") ActivityInstanceUUID activityUUID,
      @FormParam("variableId") String variableId,
      @FormParam("variableValue") Object variableValue,
      @FormParam("options") final Map<String, String> options)
  throws ActivityNotFoundException, VariableNotFoundException, RemoteException;

  /**
   * Searches for variables with the given activity UUID and variables Id.<br>
   * If the activity variables is found, the given value is set.<br>
   * For XML types, see {@link #setVariable(ActivityInstanceUUID, String, Object)}.
   * @param activityUUID the activity UUID.
   * @param variables the variables map.
   * @param options the options map (domain, queryList, user)
   * @throws ActivityNotFoundException
   * @throws VariableNotFoundException
   * @throws RemoteException
   */
  @POST
  @Path("setActivityInstanceVariables/{activityUUID}")
  void setActivityInstanceVariables(
      @PathParam("activityUUID") ActivityInstanceUUID activityUUID,
      @FormParam("variables") Map<String, Object> variables,
      @FormParam("options") final Map<String, String> options)
  throws ActivityNotFoundException, VariableNotFoundException, RemoteException;

  /**
   * Cancels the process instance with the given instance UUID.
   * If the instance represented by the given instanceUUID has a parentInstance,
   * then UncancellableInstanceException is thrown.
   * @param instanceUUID the instance UUID.
   * @param options the options map (domain, queryList, user)
   * @throws InstanceNotFoundException
   * @throws UncancellableInstanceException
   * @throws RemoteException
   */
  @POST @Path("cancelProcessInstance/{instanceUUID}") 
  void cancelProcessInstance(
      @PathParam("instanceUUID") ProcessInstanceUUID instanceUUID,
      @FormParam("options") final Map<String, String> options) 
  throws InstanceNotFoundException, UncancellableInstanceException, RemoteException;

  /**
   * @param instanceUUID
   * @param activityName
   * @param options the options map (domain, queryList, user)
   * @throws RemoteException
   */
  @POST @Path("enableEventsInFailure/{instanceUUID}/{activityName}")
  void enableEventsInFailure(
      @PathParam("instanceUUID") ProcessInstanceUUID instanceUUID,
      @PathParam("activityName") String activityName, 
      @FormParam("options") final Map<String, String> options)
  throws RemoteException;

  /**
   * @param activityUUID
   * @param options the options map (domain, queryList, user)
   * @throws RemoteException
   */
  @POST @Path("enableEventsInFailure/{activityUUID}") 
  void enableEventsInFailure(
      @PathParam("activityUUID") ActivityInstanceUUID activityUUID,
      @FormParam("options") final Map<String, String> options) 
  throws RemoteException;

  /**
   * @param activityUUID
   * @param options the options map (domain, queryList, user)
   * @throws RemoteException
   */
  @POST @Path("enablePermanentEventInFailure/{activityUUID}") 
  void enablePermanentEventInFailure(
      @PathParam("activityUUID") ActivityDefinitionUUID activityUUID, 
      @FormParam("options") final Map<String, String> options)
  throws RemoteException;

  /**
   * Deletes all runtime objects for the process instance with the given instance UUID
   * and delete also recorded data from the journal.
   * If this instance was not found in the journal, then the archived instance is deleted from history.
   * If the instance represented by the given instanceUUID has a parentInstance, then UndeletableInstanceException is thrown.
   * @param instanceUUID the instance UUID.
   * @param options the options map (domain, queryList, user)
   * @throws InstanceNotFoundException
   * @throws UndeletableInstanceException
   * @throws RemoteException
   */
  @POST @Path("deleteProcessInstance/{instanceUUID}") 
  void deleteProcessInstance(
      @PathParam("instanceUUID") ProcessInstanceUUID instanceUUID,
      @FormParam("options") final Map<String, String> options)
  throws InstanceNotFoundException, UndeletableInstanceException, RemoteException;

  /**
   * Deletes all runtime objects for all instances created with the given process UUID
   * and delete also all there recorded data from the journal.
   * If instances some instances of this process were not found in the journal,
   * then the archived instances are deleted from history.
   * @param processUUID the process UUID.
   * @param options the options map (domain, queryList, user)
   * @throws ProcessNotFoundException
   * @throws UndeletableInstanceException
   * @throws RemoteException
   */
  @POST @Path("deleteAllProcessInstances/{processUUID}") 
  void deleteAllProcessInstances(
      @PathParam("processUUID") ProcessDefinitionUUID processUUID,
      @FormParam("options") final Map<String, String> options) 
  throws ProcessNotFoundException, UndeletableInstanceException, RemoteException;

  /**
   * Launches the execution of both RoleResolver and Filters for the given task.<br>
   * If a RoleResolver has been defined within the participant referenced by the performer of the task,
   * it is executed.<br>
   * If Filters have been defined within the activity of the task they are also executed.
   * @param taskUUID the task UUID.
   * @param options the options map (domain, queryList, user)
   * @throws TaskNotFoundException
   * @throws RemoteException
   */
  @POST @Path("assignTask/{taskUUID}") 
  void assignTask(
      @PathParam("taskUUID") ActivityInstanceUUID taskUUID,
      @FormParam("options") final Map<String, String> options) 
  throws TaskNotFoundException, RemoteException;

  /**
   * Forces to assign the given task to the given actor id. If a set of candidates was already set, this method doesn't
   * update it.
   * @param taskUUID the task UUID.
   * @param options the options map (domain, queryList, user)
   * @throws TaskNotFoundException
   * @throws RemoteException
   */
  @POST @Path("assignTask/{taskUUID}/{actorId}") 
  void assignTask(
      @PathParam("taskUUID") ActivityInstanceUUID taskUUID,
      @PathParam("actorId") String actorId, 
      @FormParam("options") final Map<String, String> options)
  throws TaskNotFoundException, RemoteException;

  /**
   * Forces to replace the candidates set of the given task by the given candidates set.
   * If a userId was already set, this method doesn't update it.
   * @param taskUUID the task UUID.
   * @param candidates the set of candidate actors.
   * @param options the options map (domain, queryList, user)
   * @throws TaskNotFoundException
   * @throws RemoteException
   */
  @POST @Path("assignTaskWithCandidates/{taskUUID}") 
  void assignTask(
      @PathParam("taskUUID") ActivityInstanceUUID taskUUID,
      @FormParam("candidates") Set<String> candidates, 
      @FormParam("options") final Map<String, String> options)
  throws TaskNotFoundException, RemoteException;

  /**
   * If this task had a userId set, set it to null. If a set of candidates was already set,
   * this method doesn't update it.
   * @param taskUUID the task UUID.
   * @param options the options map (domain, queryList, user)
   * @throws TaskNotFoundException
   * @throws RemoteException
   */
  @POST @Path("unassignTask/{taskUUID}") 
  void unassignTask(
      @PathParam("taskUUID") ActivityInstanceUUID taskUUID,
      @FormParam("options") final Map<String, String> options) 
  throws TaskNotFoundException, RemoteException;

  /**
   * Starts the activity. If successful, this operation changes activity state from READY to EXECUTING.<br>
   * @param activityUUID the activity UUID.
   * @param options the options map (domain, queryList, user)
   * @throws ActivityNotFoundException
   * @throws RemoteException
   */
  @POST @Path("startActivity/{activityUUID}") 
  void startActivity(
      @PathParam("activityUUID") ActivityInstanceUUID activityUUID,
      @FormParam("options") final Map<String, String> options) 
  throws ActivityNotFoundException, RemoteException;

  /**
   * <p>
   * Searches for variable with id variableId within the given activity instance
   * with the given UUID.<br>
   * If the variable is found within the activity, the given value is set.<br>
   * If the variable is not found within the activity the search is performed
   * within the process instance.<br>
   * If the variable is found within the process instance, the given value is set.
   * </p>
   * <p>
   * <i>For XML data</i><br/>
   * Here is the effect of set variable for XML data:
   * <table border="1">
   * <tr><td></td><td>String</td><td>Document</td><td>Element</td><td>Attribute</td></tr>
   * <tr>
   * 	<td>"myXmlData"</td>
   * 	<td>Stores content of XML String into myXmlData</td>
   * 	<td>Stores a copy of Document in myXMLData</td>
   * 	<td>Not supported</td>
   * 	<td>Not supported</td>
   * </tr>
   * <tr>
   * 	<td>"myXmlData" + {@link BonitaConstants#XPATH_VAR_SEPARATOR} + "/root/node" (or any XPath expression resolving to an element</td>
   * 	<td>Creates Node from string, and replace node by new node</td>
   * 	<td>Not supported</td>
   * 	<td>Replace node by new element</td>
   * 	<td>Adds attribute to node</td>
   * </tr>
   * 	<td>"myXmlData" + {@link BonitaConstants#XPATH_VAR_SEPARATOR} + "/root/node/text()"</td>
   * 	<td>Sets text content of node</td>
   * 	<td>Not supported</td>
   * 	<td>Not supported</td>
   * 	<td>Not supported</td>
   * </tr>
   * <tr>
   * 	<td>"myXmlData" + {@link BonitaConstants#XPATH_VAR_SEPARATOR} + "/root/@attribute"</td>
   * 	<td>Sets the value of attribute. Create it if it does not exist</td>
   * 	<td>Not supported</td>
   * 	<td>Not supported</td>
   * 	<td>Sets the value of attribute to the value of passed attribute</td>
   * </tr>
   * <tr>
   * 	<td>"myXmlData" + {@link BonitaConstants#XPATH_VAR_SEPARATOR} + "/root/node" + {@link BonitaConstants#XPATH_VAR_SEPARATOR} + {@link BonitaConstants#XPATH_APPEND_FLAG}</td>
   * 	<td>Not supported</td>
   * 	<td>Not supported</td>
   * 	<td>Append a copy of element to node</td>
   * 	<td>Not supported</td>
   * </tr>
   * </table>
   * @param activityUUID the activity UUID.
   * @param variableId the variable id.
   * @param variableValue the variable value (can be: a plain {@link String}, a {@link Boolean},
   * a {@link Date}, a {@link Long}, a {@link Double}, any Java {@link Object}, or any {@link Document} element
   * according to the type of the variable).
   * @param options the options map (domain, queryList, user)
   * @throws ActivityNotFoundException
   * @throws VariableNotFoundException
   * @throws RemoteException
   */
  @POST @Path("setVariable/{activityUUID}") 
  void setVariable(
      @PathParam("activityUUID") ActivityInstanceUUID activityUUID,
      @FormParam("variableId") String variableId, 
      @FormParam("variableValue") Object variableValue,
      @FormParam("options") final Map<String, String> options)
  throws ActivityNotFoundException, VariableNotFoundException, RemoteException;

  /**
   * Adds a comment to the ProcessInstance feed. If the activtyInstance is null, it means that this comment is a process comment. 
   * @param instanceUUID the process instance UUID
   * @param activityUUID the activity UUID, can be null
   * @param message the comment
   * @param userId the userId
   * @param options the options map (domain, queryList, user)
   * @throws InstanceNotFoundException
   * @throws ActivityNotFoundException
   * @throws RemoteException
   */
  @Deprecated
  @POST @Path("addComment/{instanceUUID}/{activityUUID}/{userId}") 
  void addComment(
      @PathParam("instanceUUID") final ProcessInstanceUUID instanceUUID,
      @PathParam("activityUUID") ActivityInstanceUUID activityUUID, 
      @FormParam("message") String message,
      @PathParam("userId") String userId,
      @FormParam("options") final Map<String, String> options)
  throws InstanceNotFoundException, ActivityNotFoundException, RemoteException;

  /**
   * Adds a comment to the ProcessInstance feed.
   * @param instanceUUID the process instance UUID
   * @param message the comment
   * @param userId the userId
   * @param options the options map (domain, queryList, user)
   * @throws InstanceNotFoundException
   * @throws RemoteException
   */
  @POST @Path("addCommentToAProcessInstance/{instanceUUID}/{userId}") 
  void addComment(
      @PathParam("instanceUUID") final ProcessInstanceUUID instanceUUID,
      @FormParam("message") final String message, 
      @PathParam("userId") final String userId,
      @FormParam("options") final Map<String, String> options)
  throws InstanceNotFoundException, RemoteException;

  /**
   * Adds a comment to an ActivityInstance feed.
   * @param activityUUID the activity UUID
   * @param message the comment
   * @param userId the userId
   * @param options the options map (domain, queryList, user)
   * @throws ActivityNotFoundException
   * @throws InstanceNotFoundException
   * @throws RemoteException
   */
  @POST @Path("addCommentToAnActivityInstance/{activityUUID}/{userId}") 
  void addComment(
      @PathParam("activityUUID") final ActivityInstanceUUID activityUUID,
      @FormParam("message") final String message, 
      @PathParam("userId") final String userId,
      @FormParam("options") final Map<String, String> options)
  throws ActivityNotFoundException, InstanceNotFoundException, RemoteException;

  /**
   * Adds a process meta data.
   * @param uuid the process UUID.
   * @param key the key of the meta data
   * @param value the value of the meta data
   * @param options the options map (domain, queryList, user)
   * @throws ProcessNotFoundException
   * @throws RemoteException
   */
  @POST @Path("addProcessMetaData") 
  void addProcessMetaData(
      @QueryParam("uuid") ProcessDefinitionUUID uuid,
      @QueryParam("key") String key,
      @FormParam("value") String value, 
      @FormParam("options") final Map<String, String> options)
  throws ProcessNotFoundException, RemoteException;

  /**
   * Deletes a process meta data.
   * @param uuid the process UUID
   * @param key the key of the meta data
   * @param options the options map (domain, queryList, user)
   * @throws ProcessNotFoundException
   * @throws RemoteException
   */
  @POST @Path("deleteProcessMetaData") 
  void deleteProcessMetaData(
      @QueryParam("uuid") ProcessDefinitionUUID uuid,
      @QueryParam("key") String key, 
      @FormParam("options") final Map<String, String> options)
  throws ProcessNotFoundException, RemoteException;

  /**
   * Evaluates an expression using Groovy.
   * If more than one Groovy expressions are in the expression, they must start with ${
   * and finish with }. It returns an Object if the expression is an only Groovy one or a String
   * if the expression contains String and or more than one Groovy expression.
   * @param expression the expression
   * @param instanceUUID the process instance UUID
   * @param propagate true if true, the values modified by Groovy update Bonita variables
   * @param options the options map (domain, queryList, user)
   * @return either an Object if the expression is a Groovy one or a String
   * @throws InstanceNotFoundException
   * @throws GroovyException
   * @throws RemoteException
   */
  @POST @Path("evaluateGroovyExpression/{instanceUUID}/{propagate}")
  Object evaluateGroovyExpression(
      @FormParam("expression") String expression,
      @PathParam("instanceUUID") ProcessInstanceUUID instanceUUID, 
      @PathParam("propagate") boolean propagate,
      @FormParam("options") final Map<String, String> options)
  throws InstanceNotFoundException, GroovyException, RemoteException;

  /**
   * Evaluates an expression using Groovy.
   * If more than one Groovy expressions are in the expression, they must start with ${
   * and finish with }. It returns an Object if the expression is an only Groovy one or a String
   * if the expression contains String and or more than one Groovy expression.
   * @param expression the expression
   * @param processInstanceUUID the process instance UUID
   * @param context the extra variables added in the Groovy context
   * @param propagate if true, the values modified by Groovy update Bonita variables
   * @param options the options map (domain, queryList, user)
   * @return either an Object if the expression is a Groovy one or a String
   * @throws InstanceNotFoundException
   * @throws GroovyException
   * @throws RemoteException
   */
  @POST @Path("evaluateGroovyExpressionWithContext/{processInstanceUUID}/{propagate}")
  Object evaluateGroovyExpression(
      @FormParam("expression") String expression,
      @PathParam("processInstanceUUID") ProcessInstanceUUID processInstanceUUID, 
      @FormParam("context") Map<String, Object> context,
      @PathParam("propagate") boolean propagate,
      @FormParam("options") final Map<String, String> options)
  throws InstanceNotFoundException, GroovyException, RemoteException;

  /**
   * Evaluates an expression using Groovy.
   * If more than one Groovy expressions are in the expression, they must start with ${
   * and finish with }. It returns an Object if the expression is an only Groovy one or a String
   * if the expression contains String and or more than one Groovy expression.
   * @param expression the expression
   * @param processInstanceUUID the process instance UUID
   * @param context the extra variables added in the Groovy context
   * @param useInitialVariableValues if true, use the process variable values at instantiation as context
   * @param propagate if true, the values values modified by Groovy update Bonita variables
   * @param options the options map (domain, queryList, user)
   * @return
   * @throws InstanceNotFoundException
   * @throws GroovyException
   * @throws RemoteException
   */
  @POST @Path("evaluateGroovyExpressionWithContext/{processInstanceUUID}/{useInitialVariableValues}/{propagate}")
  Object evaluateGroovyExpression(
      @FormParam("expression") String expression,
      @PathParam("processInstanceUUID") ProcessInstanceUUID processInstanceUUID, 
      @FormParam("context") Map<String, Object> context,
      @PathParam("useInitialVariableValues") boolean useInitialVariableValues, 
      @PathParam("propagate") boolean propagate,
      @FormParam("options") final Map<String, String> options)
  throws InstanceNotFoundException, GroovyException, RemoteException;
  
  @POST @Path("evaluateGroovyExpressionsWithContext/{processInstanceUUID}/{useInitialVariableValues}/{propagate}")
  Map<String, Object> evaluateGroovyExpressions(
      @FormParam("expression") final Map<String, String> expression,
      @PathParam("processInstanceUUID") final ProcessInstanceUUID processInstanceUUID, 
      @FormParam("context") final Map<String, Object> context,
      @PathParam("useInitialVariableValues") final boolean useInitialVariableValues, 
      @PathParam("propagate") final boolean propagate,
      @FormParam("options") final Map<String, String> options)
  throws InstanceNotFoundException, GroovyException, RemoteException;

  /**
   * Evaluates an expression using Groovy.
   * If more than one Groovy expressions are in the expression, they must start with ${
   * and finish with }. It returns an Object if the expression is an only Groovy one or a String
   * if the expression contains String and or more than one Groovy expression.
   * @param expression the expression
   * @param activityUUID the activity UUID
   * @param useActivityScope
   * @param propagate if true, the values modified by Groovy update Bonita variables
   * @param options the options map (domain, queryList, user)
   * @return
   * @throws InstanceNotFoundException
   * @throws ActivityNotFoundException
   * @throws GroovyException
   * @throws RemoteException
   */
  @POST @Path("evaluateGroovyExpression/{activityUUID}/{useActivityScope}/{propagate}")
  Object evaluateGroovyExpression(
      @FormParam("expression") String expression,
      @PathParam("activityUUID") ActivityInstanceUUID activityUUID, 
      @PathParam("useActivityScope") boolean useActivityScope,
      @PathParam("propagate") boolean propagate, 
      @FormParam("options") final Map<String, String> options)
  throws InstanceNotFoundException, ActivityNotFoundException, GroovyException, RemoteException;

  /**
   * Evaluates an expression using Groovy.
   * If more than one Groovy expressions are in the expression, they must start with ${
   * and finish with }. It returns an Object if the expression is an only Groovy one or a String
   * if the expression contains String and or more than one Groovy expression.
   * @param expression the expression
   * @param activityInstanceUUID the activity UUID
   * @param context the extra variables added in the Groovy context
   * @param useActivityScope
   * @param propagate propagate if true, the values modified by Groovy update Bonita variables
   * @param options the options map (domain, queryList, user)
   * @return
   * @throws InstanceNotFoundException
   * @throws ActivityNotFoundException
   * @throws GroovyException
   * @throws RemoteException
   */
  @POST @Path("evaluateGroovyExpressionInActivityInstance/{activityInstanceUUID}/{useActivityScope}/{propagate}")
  Object evaluateGroovyExpression(
      @FormParam("expression") String expression,
      @PathParam("activityInstanceUUID") ActivityInstanceUUID activityInstanceUUID, 
      @FormParam("context") Map<String, Object> context,
      @PathParam("useActivityScope") boolean useActivityScope,
      @PathParam("propagate") boolean propagate, 
      @FormParam("options") final Map<String, String> options)
  throws InstanceNotFoundException, ActivityNotFoundException, GroovyException, RemoteException;

  /**
   * Evaluates an expression using Groovy.
   * If more than one Groovy expressions are in the expression, they must start with ${
   * and finish with }. It returns an Object if the expression is an only Groovy one or a String
   * if the expression contains String and or more than one Groovy expression.
   * @param expression the expression
   * @param processDefinitionUUID the process definition UUID
   * @param options the options map (domain, queryList, user)
   * @return
   * @throws ProcessNotFoundException
   * @throws GroovyException
   * @throws RemoteException
   */
  @POST @Path("evaluateGroovyExpression/{processDefinitionUUID}")
  Object evaluateGroovyExpression(
      @FormParam("expression") String expression,
      @PathParam("processDefinitionUUID") ProcessDefinitionUUID processDefinitionUUID, 
      @FormParam("options") final Map<String, String> options)
  throws ProcessNotFoundException, GroovyException, RemoteException;

  /**
   * Evaluates an expression using Groovy.
   * If more than one Groovy expressions are in the expression, they must start with ${
   * and finish with }. It returns an Object if the expression is an only Groovy one or a String
   * if the expression contains String and or more than one Groovy expression.
   * @param expression the expression
   * @param processDefinitionUUID the process definition UUID
   * @param context the extra variables added in the Groovy context
   * @param options the options map (domain, queryList, user)
   * @return
   * @throws ProcessNotFoundException
   * @throws GroovyException
   * @throws RemoteException
   */
  @POST @Path("evaluateGroovyExpressionWithContext/{processDefinitionUUID}")
  Object evaluateGroovyExpression(
      @FormParam("expression") String expression,
      @PathParam("processDefinitionUUID") ProcessDefinitionUUID processDefinitionUUID, 
      @FormParam("context") Map<String, Object> context,
      @FormParam("options") final Map<String, String> options)
  throws ProcessNotFoundException, GroovyException, RemoteException;

  /**
   * Evaluates expressions using Groovy.
   * @param expressions the expressions
   * @param activityInstanceUUID the activity UUID
   * @param useActivityScope
   * @param propagate propagate if true, the values modified by Groovy update Bonita variables
   * @param options the options map (domain, queryList, user)
   * @return
   * @throws InstanceNotFoundException
   * @throws ActivityNotFoundException
   * @throws GroovyException
   * @throws RemoteException
   */
  @POST @Path("evaluateGroovyExpressions/{activityUUID}/{useActivityScope}/{propagate}")
  Map<String, Object> evaluateGroovyExpressions(
      @FormParam("expressions") final Map<String, String> expressions,
      @PathParam("activityUUID") final ActivityInstanceUUID activityUUID,
      @FormParam("context") final Map<String, Object> context,
      @PathParam("useActivityScope") final boolean useActivityScope,
      @PathParam("propagate") final boolean propagate,
      @FormParam("options") final Map<String, String> options)
      throws InstanceNotFoundException, ActivityNotFoundException, GroovyException, RemoteException;

  /**
   * Evaluates expressions using Groovy.
   * @param expressions the expressions
   * @param processDefinitionUUID the process definition UUID
   * @param context the extra variables added in the Groovy context
   * @param useInitialVariableValues if true, use the process variable values at instantiation as context
   * @param options the options map (domain, queryList, user)
   * @return
   * @throws InstanceNotFoundException
   * @throws ActivityNotFoundException
   * @throws GroovyException
   * @throws RemoteException
   */
  @POST @Path("evaluateGroovyExpressions/{processDefinitionUUID}")
  Map<String, Object> evaluateGroovyExpressions(
      @FormParam("expressions") final Map<String, String> expressions,
      @PathParam("processDefinitionUUID") final ProcessDefinitionUUID processDefinitionUUID,
      @FormParam("context") final Map<String, Object> context,
      @FormParam("options") final Map<String, String> options)
      throws InstanceNotFoundException, GroovyException, ProcessNotFoundException, RemoteException;

  /**
   * Add an attachment to a process instance.
   * @param instanceUUID the process instance UUID
   * @param name the attachment name
   * @param label the attachment label
   * @param description the attachment description
   * @param fileName the attachment file name
   * @param metadata the attachment meta data
   * @param value the attachment value
   * @param options the options map (domain, queryList, user)
   * @throws RemoteException
   */
  @Deprecated
  @POST @Path("addAttachmentWithDescription/{instanceUUID}/{name}") 
  void addAttachment(
      @PathParam("instanceUUID") ProcessInstanceUUID instanceUUID,
      @PathParam("name") String name,
      @QueryParam("label") String label, 
      @FormParam("description") String description,
      @FormParam("fileName") String fileName,
      @FormParam("metadata") Map<String, String> metadata, 
      @FormParam("value") byte[] value,
      @FormParam("options") final Map<String, String> options)
  throws RemoteException;

  /**
   * Add an attachment to a process instance. If you are using REST, use addAttachmentOctetStream instead.
   * @param instanceUUID the process instance UUID
   * @param name the attachment name
   * @param fileName the file name
   * @param value the content of the attachment
   * @param options the options map (domain, queryList, user)
   * @throws RemoteException
   */
  @Deprecated
  @POST @Path("addAttachment/{instanceUUID}/{name}") 
  void addAttachment(
      @PathParam("instanceUUID") ProcessInstanceUUID instanceUUID,
      @PathParam("name") String name,
      @FormParam("fileName") String fileName, 
      @FormParam("value") byte[] value,
      @FormParam("options") final Map<String, String> options)
  throws RemoteException;
  
  /**
   * If there are no documents with the given name for this process instance, create a document a new document (same behavior as 
   * {@link #createDocument(String, ProcessInstanceUUID, String, String, byte[])}), otherwise add a new document version to the 
   * existent one (same behavior as {@link #addDocumentVersion(DocumentUUID, boolean, String, String, byte[])}). 
   * 
   * @param name the name of the document
   * @param instanceUUID the {@link ProcessInstanceUUID}
   * @param fileName the filename
   * @param mimeType the mime type of the file
   * @param content the content of the file
   * @return a {@link Document}
   * @throws DocumentationCreationException
   * @throws InstanceNotFoundException
   */
  @Consumes("application/octet-stream")
  @POST @Path("createDocumentOrAddDocumentVersion/{instanceUUID}")
  void createDocumentOrAddDocumentVersion(
      @PathParam("instanceUUID") ProcessInstanceUUID instanceUUID, 
      @QueryParam("name") String name, 
      @QueryParam("fileName") String fileName, 
      @QueryParam("mimeType") String mimeType, 
      byte[] value,
      @HeaderParam("options") final Map<String, String> options) throws RemoteException;

  /**
   * Add all attachments to the process instance defined in the attachment instance.
   * @param attachments the attachment instances
   * @param options the options map (domain, queryList, user)
   * @throws RemoteException
   */
  @Deprecated
  @POST @Path("addAttachments") 
  void addAttachments(
      @FormParam("attachments") Map<AttachmentInstance, byte[]> attachments,
      @FormParam("options") final Map<String, String> options)
  throws RemoteException;

  /**
   * Removes all versions of an attachment according to its name.
   * @param instanceUUID the process instance UUID
   * @param name the attachment name
   * @param options the options map (domain, queryList, user)
   * @throws RemoteException
   * @throws InstanceNotFoundException
   */
  @Deprecated
  @POST @Path("removeAttachment/{instanceUUID}/{name}")
  public void removeAttachment(
      @PathParam("instanceUUID") ProcessInstanceUUID instanceUUID,
      @PathParam("name") String name,
      @FormParam("options") final Map<String, String> options)
  throws RemoteException, InstanceNotFoundException;

  /**
   * set an activity instance priority
   * @param activityInstanceUUID the activity instance UUID
   * @param priority the priority
   * @param options the options map (domain, queryList, user)
   * @throws ActivityNotFoundException
   * @throws RemoteException
   */
  @POST @Path("setActivityInstancePriority/{activityInstanceUUID}/{priority}") 
  void setActivityInstancePriority(
      @PathParam("activityInstanceUUID") ActivityInstanceUUID activityInstanceUUID, 
      @PathParam("priority") int priority,
      @FormParam("options") final Map<String, String> options)
  throws ActivityNotFoundException, RemoteException;

  /**
   * @param eventName
   * @param toProcessName
   * @param toActivityName
   * @param activityUUID
   * @param options the options map (domain, queryList, user)
   * @throws RemoteException
   */
  @POST @Path("deleteEvents") 
  void deleteEvents(
      @QueryParam("eventName") final String eventName,
      @QueryParam("toProcessName") final String toProcessName, 
      @QueryParam("toActivityName") final String toActivityName,
      @QueryParam("activityUUID") final ActivityInstanceUUID activityUUID, 
      @FormParam("options") final Map<String, String> options)
  throws RemoteException;

  /**
   * Executes a Connector.
   * @param connectorClassName the Connector class name
   * @param parameters the input parameters to set the connector.
   * @param options the options map (domain, queryList, user)
   * @return the result of the connector execution
   * @throws RemoteException
   * @throws Exception
   */
  @POST @Path("executeConnector")
  Map<String, Object> executeConnector(
      @FormParam("connectorClassName") String connectorClassName,
      @FormParam("parameters") Map<String, Object[]> parameters, 
      @FormParam("options") final Map<String, String> options)
      throws RemoteException, Exception;

  /**
   * Executes a Connector in the context of a process. 
   * The ClassLoader uses the process UUID to load the connectors' class and the process data's initial values are added to the groovy evaluation context.
   * @param connectorClassName the Connector class name
   * @param parameters the input parameters to set the connector.
   * @param definitionUUID the process definition UUID
   * @param options the options map (domain, queryList, user)
   * @return the result of the connector execution
   * @throws RemoteException
   * @throws Exception
   */
  @POST @Path("executeConnector/{definitionUUID}")
  Map<String, Object> executeConnector(
      @FormParam("connectorClassName") String connectorClassName,
      @FormParam("parameters") Map<String, Object[]> parameters, 
      @PathParam("definitionUUID") ProcessDefinitionUUID definitionUUID,
      @FormParam("options") final Map<String, String> options)
      throws RemoteException, Exception;

  /**
   * Executes a Connector in the context of a process. 
   * The ClassLoader uses the process UUID to load the connectors' class and the process data's initial values are added to the groovy evaluation context.
   * @param connectorClassName the Connector class name
   * @param parameters the input parameters to set the connector.
   * @param definitionUUID the process definition UUID
   * @param context additional context for input parameters expressions evaluation
   * @param options the options map (domain, queryList, user)
   * @return the result of the connector execution
   * @throws RemoteException
   * @throws Exception
   */
  @POST @Path("executeConnectorWithContext/{definitionUUID}")
  Map<String, Object> executeConnector(
      @FormParam("connectorClassName") String connectorClassName,
      @FormParam("parameters") Map<String, Object[]> parameters, 
      @PathParam("definitionUUID") ProcessDefinitionUUID definitionUUID,
      @FormParam("context") Map<String, Object> context, 
      @FormParam("options") final Map<String, String> options)
      throws RemoteException, Exception;

  /**
   * Executes a Connector in the context of a process instance. 
   * The ClassLoader uses the process UUID to load the connectors' class and the process data's values are added to the groovy evaluation context.
   * @param connectorClassName the Connector class name
   * @param parameters the input parameters to set the connector
   * @param processInstanceUUID the process instance UUID
   * @param context additional context for input parameters expressions evaluation
   * @param useCurrentVariableValues if true the current variable values should be used for expression evaluation, otherwise the values at process instantiation are used
   * @param options the options map (domain, queryList, user) 
   * @return the result of the connector execution
   * @throws RemoteException
   * @throws Exception
   */
  @POST @Path("executeConnector/{processInstanceUUID}/{useCurrentVariableValues}")
  Map<String, Object> executeConnector(
      @FormParam("connectorClassName") String connectorClassName,
      @FormParam("parameters") Map<String, Object[]> parameters, 
      @PathParam("processInstanceUUID") ProcessInstanceUUID processInstanceUUID,
      @FormParam("context") Map<String, Object> context, 
      @PathParam("useCurrentVariableValues") boolean useCurrentVariableValues,
      @FormParam("options") final Map<String, String> options)
      throws RemoteException, Exception;

  /**
   * Executes a Connector in the context of an activity . 
   * The ClassLoader uses the process UUID to load the connectors' class and the process and activity data's values are added to the groovy evaluation context.
   * @param connectorClassName the Connector class name
   * @param parameters the input parameters to set the connector.
   * @param activityInstanceUUID the activity instance UUID
   * @param context additional context for input parameters expressions evaluation
   * @param useCurrentVariableValues if true the current variable values should be used for expression evaluation, otherwise the values at step end are used
   * @param options the options map (domain, queryList, user)
   * @return the result of the connector execution
   * @throws RemoteException
   * @throws Exception
   */
  @POST @Path("executeConnectorInActivityInstance/{activityInstanceUUID}/{useCurrentVariableValues}")
  Map<String, Object> executeConnector(
      @FormParam("connectorClassName") String connectorClassName,
      @FormParam("parameters") Map<String, Object[]> parameters, 
      @PathParam("activityInstanceUUID") ActivityInstanceUUID activityInstanceUUID,
      @FormParam("context") Map<String, Object> context, 
      @PathParam("useCurrentVariableValues") boolean useCurrentVariableValues,
      @FormParam("options") final Map<String, String> options) 
      throws RemoteException, Exception;

  @POST @Path("executeConnectorWithClassLoader")
  Map<String, Object> executeConnector(
      @FormParam("connectorClassName") String connectorClassName,
      @FormParam("parameters") Map<String, Object[]> parameters,
      ClassLoader classLoader, 
      @FormParam("options") final Map<String, String> options)
      throws RemoteException, Exception;
  
  /**
   * Execute a list of Connector in the context of a process. 
   * The ClassLoader uses the process UUID to load the connectors' class and the process data's initial values are added to the groovy evaluation context.
   * @param processDefinitionUUID the process definition UUID
   * @param connectorExecutionDescriptors the descriptor used for connector execution (class name, input and output parameters)
   * @param context additional context for input parameters expressions evaluation
   * @return the context updated based on output parameters of connectorExecutionDescriptors
   * @throws Exception if any exception occurs
   */
  @POST @Path("executeConnectors/{processDefinitionUUID}")
  Map<String, Object> executeConnectors(
      @PathParam("processDefinitionUUID") final ProcessDefinitionUUID processDefinitionUUID, 
      @FormParam("connectorExecutionDescriptors") final List<ConnectorExecutionDescriptor> connectorExecutionDescriptors, 
      @FormParam("context") final Map<String, Object> context, 
      @FormParam("options") final Map<String, String> options) 
    throws RemoteException, Exception;
  
  /**
   * Execute a list of Connector in the context of a process instance.
   * @param processInstanceUUID the process instance UUID
   * @param connectorExecutionDescriptors the descriptor used for connector execution (class name, input and output parameters)
   * @param context additional context for input parameters expressions evaluation
   * @param useCurrentVariableValues if true the current variable values should be used for expression evaluation, otherwise the values at process instantiation are used
   * @return the context updated based on output parameters of connectorExecutionDescriptors
   * @throws Exception if any exception occurs
   */
  @POST @Path("executeConnectors/{processInstanceUUID}/{useCurrentVariableValues}")
  Map<String, Object> executeConnectors(
      @PathParam("processInstanceUUID") final ProcessInstanceUUID processInstanceUUID, 
      @FormParam("connectorExecutionDescriptors") final List<ConnectorExecutionDescriptor> connectorExecutionDescriptors, 
      @FormParam("context") final Map<String, Object> context, 
      @PathParam("useCurrentVariableValues") boolean useCurrentVariableValues, 
      @FormParam("options") final Map<String, String> options) 
  throws RemoteException, Exception;
  
  /**
   * Execute a list of Connector in the context of an activity instance.
   * @param activityInstanceUUID the activity instance UUID
   * @param connectorExecutionDescriptors the descriptor used for connector execution (class name, input and output parameters)
   * @param context additional context for input parameters expressions evaluation
   * @param useCurrentVariableValues if true the current variable values should be used for expression evaluation, otherwise the values at step end are used
   * @return the context updated based on output parameters of connectorExecutionDescriptors
   * @throws Exception if any exception occurs
   */
  @POST @Path("executeConnectorsInActivityInstance/{activityInstanceUUID}/{useCurrentVariableValues}")
  Map<String, Object> executeConnectors(
      @PathParam("activityInstanceUUID") final ActivityInstanceUUID activityInstanceUUID, 
      @FormParam("connectorExecutionDescriptors") final List<ConnectorExecutionDescriptor> connectorExecutionDescriptors, 
      @FormParam("context") final Map<String, Object> context, 
      @PathParam("useCurrentVariableValues") boolean useCurrentVariableValues, 
      @FormParam("options") final Map<String, String> options) 
  throws RemoteException, Exception;


  /**
   * Executes a Filter.
   * @param connectorClassName the Filter class name
   * @param parameters the parameters to set the connector.
   * @param members the member set to filter
   * @param options the options map (domain, queryList, user)
   * @return the candidates filtered by the connector
   * @throws RemoteException
   * @throws Exception
   */
  @POST @Path("executeFilter")
  Set<String> executeFilter(
      @FormParam("connectorClassName") String connectorClassName,
      @FormParam("parameters") Map<String, Object[]> parameters, 
      @FormParam("members") Set<String> members,
      @FormParam("options") final Map<String, String> options)
      throws RemoteException, Exception;

  /**
   * Executes a Filter.
   * @param connectorClassName the Filter class name
   * @param parameters the parameters to set the connector.
   * @param members the member set to filter
   * @param definitionUUID the process definition UUID
   * @param options the options map (domain, queryList, user)
   * @return the candidates filtered by the connector
   * @throws RemoteException
   * @throws Exception
   */
  @POST @Path("executeFilter/{definitionUUID}")
  Set<String> executeFilter(
      @FormParam("connectorClassName") String connectorClassName,
      @FormParam("parameters") Map<String, Object[]> parameters, 
      @FormParam("members") Set<String> members,
      @PathParam("definitionUUID") ProcessDefinitionUUID definitionUUID, 
      @FormParam("options") final Map<String, String> options)
      throws RemoteException, Exception;

  @POST	@Path("executeFilterWithClassLoader")
  Set<String> executeFilter(
      @FormParam("connectorClassName") String connectorClassName,
      @FormParam("parameters") Map<String, Object[]> parameters, 
      @FormParam("members") Set<String> members,
      ClassLoader classLoader,
      @FormParam("options") final Map<String, String> options)
      throws RemoteException, Exception;

  /**
   * Executes a RoleResolver.
   * @param connectorClassName the RoleResolver class name
   * @param parameters the parameters to set the connector.
   * @param definitionUUID the process definition UUID
   * @param options the options map (domain, queryList, user)
   * @return the members found by the RoleResolver execution
   * @throws RemoteException
   * @throws Exception
   */
  @POST @Path("executeRoleResolver/{definitionUUID}")
  Set<String> executeRoleResolver(
      @FormParam("connectorClassName") String connectorClassName,
      @FormParam("parameters") Map<String, Object[]> parameters, 
      @PathParam("definitionUUID") ProcessDefinitionUUID definitionUUID,
      @FormParam("options") final Map<String, String> options)
      throws RemoteException, Exception;

  /**
   * Executes a RoleResolver.
   * @param connectorClassName the RoleResolver class name
   * @param parameters the parameters to set the connector.
   * @param options the options map (domain, queryList, user)
   * @return the members found by the RoleResolver execution
   * @throws RemoteException
   * @throws Exception
   */
  @POST	@Path("executeRoleResolver")
  Set<String> executeRoleResolver(
      @FormParam("connectorClassName") String connectorClassName,
      @FormParam("parameters") Map<String, Object[]> parameters, 
      @FormParam("options") final Map<String, String> options)
      throws RemoteException, Exception;

  /**
   * @param connectorClassName
   * @param parameters
   * @param classLoader
   * @param options the options map (domain, queryList, user)
   * @return the members found by the RoleResolver execution
   * @throws RemoteException
   * @throws Exception
   */
  @POST @Path("executeRoleResolverWithClassLoader")
  Set<String> executeRoleResolver(
      @FormParam("connectorClassName") String connectorClassName,
      @FormParam("parameters") Map<String, Object[]> parameters,
      ClassLoader classLoader,
      @FormParam("options") final Map<String, String> options)
      throws RemoteException, Exception;

  /**
   * Skips the task if the task has READY state. The execution jumps to the next activity<br>
   * If successful, this operation changes task state from READY to SKIPPED.<br>
   * @param taskUUID the task UUID.
   * @param variablesToUpdate the variables to be updated while skipping task
   * @param options the options map (domain, queryList, user)
   * @throws TaskNotFoundException if the task has not been found.
   * @throws IllegalTaskStateException if the state of the task has not READY state.
   * @throws RemoteException
   */
  @POST @Path("skipTask/{taskUUID}")
  void skipTask(
      @PathParam("taskUUID") ActivityInstanceUUID taskUUID,
      @FormParam("variablesToUpdate") Map<String, Object> variablesToUpdate,
      @FormParam("options") final Map<String, String> options)
  throws TaskNotFoundException, IllegalTaskStateException, RemoteException;
  
  /**
   * Skips the activity if the activity has READY or FAILED state. The execution jumps to the next activity<br>
   * If successful, this operation changes task state from READY or FAILED to SKIPPED.<br>
   * @param activityInstanceUUID the activity instance UUID.
   * @param variablesToUpdate the variables to be updated while skipping activity
   * @throws ActivityNotFoundException if the activity has not been found.
   * @throws IllegalTaskStateException if the state of the activity is not READY or FAILED.
   * @throws BonitaInternalException if an exception occurs.
   * @throws RemoteException
   */
  @POST @Path("skip/{activityInstanceUUID}")
  void skip(
      @PathParam("activityInstanceUUID") ActivityInstanceUUID activityInstanceUUID, 
      @FormParam("variablesToUpdate") Map<String, Object> variablesToUpdate,
      @FormParam("options") final Map<String, String> options)
  throws ActivityNotFoundException, IllegalTaskStateException, RemoteException;

  /**
   * Executes the event.
   * @param eventUUID the eventUUID to execute
   * @param options the options map (domain, queryList, user)
   * @throws EventNotFoundException if the event does not exist
   */
  @POST @Path("executeEvent/{eventUUID}")
  void executeEvent(
      @PathParam("eventUUID") CatchingEventUUID eventUUID,
      @FormParam("options") final Map<String, String> options)
  throws EventNotFoundException, RemoteException;

  /**
   * Deletes the event.
   * @param eventUUID the eventUUID to delete
   * @param options the options map (domain, queryList, user)
   * @throws EventNotFoundException if the event does not exist
   */
  @POST @Path("deleteEvent/{eventUUID}")
  public void deleteEvent(
      @PathParam("eventUUID") CatchingEventUUID eventUUID,
      @FormParam("options") final Map<String, String> options)
  throws EventNotFoundException, RemoteException;

  /**
   * Updates the expiration date of an event.
   * @param eventUUID the eventUUID to update
   * @param expiration the new expiration date
   * @param options the options map (domain, queryList, user)
   * @throws EventNotFoundException if the event does not exist
   */
  @POST @Path("updateExpirationDate/{eventUUID}")
  public void updateExpirationDate(
      @PathParam("eventUUID") CatchingEventUUID eventUUID,
      @FormParam("expiration") Date expiration,
      @FormParam("options") final Map<String, String> options)
  throws EventNotFoundException, RemoteException;

  /**
   * Gets the modified java object based on the given variable expression, initial variable value and attribute value
   * @param processUUID the ProcessDefinitionUUID
   * @param variableExpression the variable expression
   * @param variableValue initial variable value
   * @param attributeValue attribute to be set by the variableExpression
   * @param options the options map (domain, queryList, user)
   * @return
   */
  @POST @Path("getModifiedJavaObject/{processUUID}")
  Object getModifiedJavaObject(
      @PathParam("processUUID") ProcessDefinitionUUID processUUID,
      @FormParam("variableExpression")String variableExpression, 
      @FormParam("variableValue") Object variableValue,
      @FormParam("attributeValue") Object attributeValue,
      @FormParam("options") final Map<String, String> options)
  throws RemoteException;

  /**
   * Updates the expected date when the activity should finish.
   * @param activityUUID the activity instance UUID
   * @param expectedEndDate the new value of the expected end date of the activity
   * @param options the options map (domain, queryList, user)
   * @throws ActivityNotFoundException if the activity has not been found.
   */
  @POST @Path("updateActivityExpectedEndDate/{activityUUID}")
  void updateActivityExpectedEndDate(
      @PathParam("activityUUID") ActivityInstanceUUID activityUUID,
      @FormParam("expectedEndDate") Date expectedEndDate,
      @FormParam("options") final Map<String, String> options)
  throws RemoteException, ActivityNotFoundException;

  /**
   * Create a document associated with a process definition. If you are using REST, use createDocumentOctetStream instead.
   * @param name the name of the document
   * @param processDefinitionUUID the {@link ProcessDefinitionUUID}
   * @param fileName the filename
   * @param mimeType the mime type of the file
   * @param content the content of the file
   * @return a {@link Document}
   * @throws DocumentationCreationException
   * @throws InstanceNotFoundException
   */
  @POST @Path("createProcessDocument")
  Document createDocument(
      @FormParam("name") final String name, 
      @FormParam("processUUID") final ProcessDefinitionUUID processDefinitionUUID, 
      @FormParam("fileName") final String fileName, 
      @FormParam("mimeType") final String mimeType,
      @FormParam("content") final byte[] content,
      @FormParam("options") final Map<String, String> options)
  throws RemoteException, DocumentationCreationException, ProcessNotFoundException;
  
  /**
   * Create a document associated with a process instance. If you are using REST, use createDocumentOctetStream instead.
   * @param name the name of the document
   * @param instanceUUID the {@link ProcessInstanceUUID}
   * @param fileName the filename
   * @param mimeType the mime type of the file
   * @param content the content of the file
   * @return a {@link Document}
   * @throws DocumentationCreationException
   * @throws InstanceNotFoundException
   */
  @POST @Path("createDocument")
  Document createDocument(
      @FormParam("name") final String name,
      @FormParam("instanceUUID") final ProcessInstanceUUID instanceUUID,
      @FormParam("fileName") final String fileName,
      @FormParam("mimeType") final String mimeType,
      @FormParam("content") final byte[] content,
      @FormParam("options") final Map<String, String> options)
  throws RemoteException, DocumentationCreationException, InstanceNotFoundException;

  /**
   * add a document version. If you are using REST, use addDocumentVersionOctetStream instead.
   * @param documentUUID the document UUID
   * @param isMajorVersion indicate if the document is a major version
   * @param fileName the filename
   * @param mimeType the mime type of the file
   * @param content the content of the file
   * @return a {@link Document}
   * @throws DocumentationCreationException
   * @throws InstanceNotFoundException
   */
  @POST @Path("addDocumentVersion/{documentUUID}")
  Document addDocumentVersion(
      @PathParam("documentUUID") final DocumentUUID documentUUID,
      @FormParam("isMajorVersion") final boolean isMajorVersion,
      @FormParam("fileName") final String fileName,
      @FormParam("mimeType") final String mimeType,
      @FormParam("content") final byte[] content,
      @FormParam("options") final Map<String, String> options)
  throws RemoteException, DocumentationCreationException;

  /**
   * Delete a collection of documents
   * @param allVersions indicates if all the versions of the documents should be deleted or only the last
   * @param documentUUIDs the UUIDs of the documents to delete
   * @throws DocumentNotFoundException
   */
  @POST @Path("deleteDocuments")
  void deleteDocuments(
      @FormParam("allVersions") final boolean allVersions,
      @FormParam("documentUUIDs") final DocumentUUID[] documentUUIDs,
      @FormParam("options") final Map<String, String> options)
  throws RemoteException, DocumentNotFoundException;

}
