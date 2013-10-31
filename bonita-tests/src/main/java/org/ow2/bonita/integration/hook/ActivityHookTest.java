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
package org.ow2.bonita.integration.hook;

import java.net.URL;
import java.util.Set;

import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.exception.ActivityNotFoundException;
import org.ow2.bonita.facade.exception.DeploymentException;
import org.ow2.bonita.facade.exception.IllegalTaskStateException;
import org.ow2.bonita.facade.exception.InstanceNotFoundException;
import org.ow2.bonita.facade.exception.ProcessNotFoundException;
import org.ow2.bonita.facade.exception.TaskNotFoundException;
import org.ow2.bonita.facade.exception.UnAuthorizedUserException;
import org.ow2.bonita.facade.exception.UndeletableInstanceException;
import org.ow2.bonita.facade.exception.UndeletableProcessException;
import org.ow2.bonita.facade.runtime.TaskInstance;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.BonitaException;

/**
 * Test. Deploys a xpdl process.
 *
 * @author Miguel Valdes
 */
public class ActivityHookTest extends APITestCase {

  public void testActivityHookProEdV4() throws BonitaException {
    URL xpdlUrl = this.getClass().getResource("activityHook-v4.xpdl");
    activityHookTest(xpdlUrl);
  }

  private void activityHookTest(URL xpdlUrl) throws DeploymentException,
  ProcessNotFoundException,
  InstanceNotFoundException,
  ActivityNotFoundException,
  UndeletableProcessException, 
  UndeletableInstanceException {
    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl,
        HookBeforeTerminateCheckActVariable.class));
    
    ProcessDefinitionUUID processUUID = process.getUUID();

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    checkExecutedOnce(instanceUUID, new String[] {"a", "b", "initial"});
    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testOnReadyV4() throws BonitaException {
    URL xpdlUrl = this.getClass().getResource("onReadyHook-v4.xpdl");
    onReadyTest(xpdlUrl);
  }

  private void onReadyTest(final URL xpdlUrl) throws DeploymentException, ProcessNotFoundException,
  InstanceNotFoundException, ActivityNotFoundException,
  TaskNotFoundException, IllegalTaskStateException, 
  UndeletableInstanceException,
  UndeletableProcessException {

    final ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl,
        AbstractHook.class, OnReadyHook.class));
    final ProcessDefinitionUUID processUUID = process.getUUID();

    final ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    final Set<TaskInstance> tasks = getQueryRuntimeAPI().getTasks(instanceUUID);
    assertTrue(tasks.size() == 1);
    final TaskInstance task = tasks.iterator().next();
    getRuntimeAPI().startTask(task.getUUID(), true);
    getRuntimeAPI().finishTask(task.getUUID(), true);
    checkExecutedOnce(instanceUUID, new String[]{"firstActivity", "secondActivity"});
    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testOnSuspendV4() throws BonitaException {
    URL xpdlUrl = this.getClass().getResource("onSuspendHook-v4.xpdl");
    onSuspendTest(xpdlUrl);
  }

  private void onSuspendTest(URL xpdlUrl) throws DeploymentException, ProcessNotFoundException,
   InstanceNotFoundException, ActivityNotFoundException,
   TaskNotFoundException, IllegalTaskStateException, 
   UnAuthorizedUserException, UndeletableInstanceException, 
   UndeletableProcessException {

    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl,
        OnSuspendHook.class, AbstractHook.class));
    
    ProcessDefinitionUUID processUUID = process.getUUID();

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    final Set<TaskInstance> tasks = getQueryRuntimeAPI().getTasks(instanceUUID);
    assertTrue(tasks.size() == 1);
    final TaskInstance task = tasks.iterator().next();
    getRuntimeAPI().startTask(task.getUUID(), true);
    getRuntimeAPI().suspendTask(task.getUUID(), true);
    getRuntimeAPI().resumeTask(task.getUUID(), false);
    getRuntimeAPI().finishTask(task.getUUID(), true);
    checkExecutedOnce(instanceUUID, new String[]{"firstActivity", "secondActivity"});
    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testOnResumeV4() throws BonitaException {
    URL xpdlUrl = this.getClass().getResource("onResumeHook-v4.xpdl");
    onResumeTest(xpdlUrl);
  }

  private void onResumeTest(URL xpdlUrl) throws DeploymentException, ProcessNotFoundException,
   InstanceNotFoundException, ActivityNotFoundException, UndeletableInstanceException,
   TaskNotFoundException, IllegalTaskStateException, 
   UnAuthorizedUserException, UndeletableProcessException {

    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl,
        OnResumeHook.class, AbstractHook.class));
    
    ProcessDefinitionUUID processUUID = process.getUUID();

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    final Set<TaskInstance> tasks = getQueryRuntimeAPI().getTasks(instanceUUID);
    assertTrue(tasks.size() == 1);
    final TaskInstance task = tasks.iterator().next();
    getRuntimeAPI().startTask(task.getUUID(), true);
    getRuntimeAPI().suspendTask(task.getUUID(), true);
    getRuntimeAPI().resumeTask(task.getUUID(), false);
    getRuntimeAPI().finishTask(task.getUUID(), true);
    checkExecutedOnce(instanceUUID, new String[]{"firstActivity", "secondActivity"});
    getRuntimeAPI().deleteProcessInstance(instanceUUID);
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testOnStartV4() throws BonitaException {
    URL xpdlUrl = this.getClass().getResource("onStartHook-v4.xpdl");
    onStartTest(xpdlUrl, "onStartHook");
  }

  private void onStartTest(URL xpdlUrl, String processId) throws DeploymentException, ProcessNotFoundException,
  InstanceNotFoundException, ActivityNotFoundException,
  TaskNotFoundException, IllegalTaskStateException, 
  UndeletableProcessException, UndeletableInstanceException {

    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl,
        OnStartHook.class, AbstractHook.class));
    
    ProcessDefinitionUUID processUUID = process.getUUID();

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    final Set<TaskInstance> tasks = getQueryRuntimeAPI().getTasks(instanceUUID);
    assertTrue(tasks.size() == 1);
    final TaskInstance task = tasks.iterator().next();
    getRuntimeAPI().startTask(task.getUUID(), true);
    getRuntimeAPI().finishTask(task.getUUID(), true);
    checkExecutedOnce(instanceUUID, new String[]{"firstActivity", "secondActivity"});
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }

  public void testOnFinishV4() throws BonitaException {
    URL xpdlUrl = this.getClass().getResource("onFinishHook-v4.xpdl");
    onFinishHookTest(xpdlUrl);
  }

  private void onFinishHookTest(URL xpdlUrl) throws BonitaException {
    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl,
        OnFinishHook.class, AbstractHook.class));
    
    ProcessDefinitionUUID processUUID = process.getUUID();

    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);
    final Set<TaskInstance> tasks = getQueryRuntimeAPI().getTasks(instanceUUID);
    assertTrue(tasks.size() == 1);
    final TaskInstance task = tasks.iterator().next();
    getRuntimeAPI().startTask(task.getUUID(), true);
    getRuntimeAPI().finishTask(task.getUUID(), true);
    checkExecutedOnce(instanceUUID, new String[]{"firstActivity", "secondActivity"});
    getManagementAPI().disable(processUUID);
    getManagementAPI().deleteProcess(processUUID);
  }
}
