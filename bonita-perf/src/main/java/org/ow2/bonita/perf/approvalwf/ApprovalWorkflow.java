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
package org.ow2.bonita.perf.approvalwf;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.ow2.bonita.definition.InstanceInitiator;
import org.ow2.bonita.facade.QueryRuntimeAPI;
import org.ow2.bonita.facade.RuntimeAPI;
import org.ow2.bonita.facade.def.element.BusinessArchive;
import org.ow2.bonita.facade.def.element.HookDefinition.Event;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.def.majorElement.ActivityDefinition.JoinType;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.TaskInstance;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.perf.AbstractPerftestCase;
import org.ow2.bonita.perf.approvalwf.hook.Accept;
import org.ow2.bonita.perf.approvalwf.hook.Reject;
import org.ow2.bonita.perf.approvalwf.mapper.ApprovalMapper;
import org.ow2.bonita.util.AccessorUtil;
import org.ow2.bonita.util.BonitaRuntimeException;
import org.ow2.bonita.util.BusinessArchiveFactory;
import org.ow2.bonita.util.ExceptionManager;
import org.ow2.bonita.util.ProcessBuilder;


public class ApprovalWorkflow extends AbstractPerftestCase  {

  public String getProcessName() {
    return "ApprovalWorkflow";
  }

  public BusinessArchive getBusinessArchive() throws Exception {
    Set<String> applications = new HashSet<String>();
    applications.add("Word");
    applications.add("Excel");
    applications.add("MailReader");
    applications.add("WebBrowser");

    ProcessDefinition process =
      ProcessBuilder.createProcess("ApprovalWorkflow", null)

      .addEnumData("Applications", applications, "Word")

      .addGroup("User")
      .addGroupResolver(InstanceInitiator.class.getName())
      .addGroup("Administrator")
      .addGroupResolver(ApprovalMapper.class.getName())

      .addSystemTask("BonitaStart")
      .addSystemTask("BonitaEnd")
      .addJoinType(JoinType.XOR)
      .addHumanTask("Request", "User")
      .addSystemTask("Reject")
      .addConnector(Event.automaticOnEnter, Reject.class.getName(), false)
      .addSystemTask("Accept")
      .addConnector(Event.automaticOnEnter, Accept.class.getName(), false)
      .addHumanTask("Approval", "Administrator")
      .addBooleanData("isGranted", false)

      .addTransition("Request_Approval", "Request", "Approval")
      .addTransition("Reject_End", "Reject", "BonitaEnd")
      .addTransition("Accept_End", "Accept", "BonitaEnd")
      .addTransition("Approval_Reject", "Approval", "Reject")
      .addCondition("isGranted.compareTo(Boolean.valueOf(\"false\")) == 0")
      .addTransition("Approval_Accept", "Approval", "Accept")
      .addCondition("isGranted.compareTo(Boolean.valueOf(\"true\")) == 0")
      .addTransition("Start_Request", "BonitaStart", "Request")
      .done();

    BusinessArchive businessArchive = BusinessArchiveFactory.getBusinessArchive(process, ApprovalMapper.class, Accept.class, Reject.class);
    return businessArchive;
  }

  public long launch() {
    try {
      final ProcessDefinitionUUID processUUID = AccessorUtil.getAPIAccessor().getQueryDefinitionAPI().getLastProcess(getProcessName()).getUUID();
      final RuntimeAPI runtimeAPI = AccessorUtil.getAPIAccessor().getRuntimeAPI();
      final QueryRuntimeAPI queryRuntimeAPI = AccessorUtil.getAPIAccessor().getQueryRuntimeAPI();
      final ProcessInstanceUUID instanceUUID = runtimeAPI.instantiateProcess(processUUID);
      //tasks execution
      Collection<TaskInstance> activities = AccessorUtil.getQueryRuntimeAPI().getTaskList(instanceUUID, ActivityState.READY);
      if (activities.isEmpty()) {
        String msg = ExceptionManager.getInstance().getFullMessage("bex_AW_3");
        throw new BonitaRuntimeException(msg);
      }
      while (!activities.isEmpty()) {     
        for (TaskInstance activity : activities) {
          final ActivityInstanceUUID taskUUID = activity.getUUID();
          final String activityId = activity.getActivityName();
          runtimeAPI.startTask(taskUUID, true);
          if (activityId.equals("Approval")) {
            runtimeAPI.setActivityInstanceVariable(activity.getUUID(), "isGranted", true);
          }
          runtimeAPI.finishTask(taskUUID, true);
        }
        activities = queryRuntimeAPI.getTaskList(instanceUUID, ActivityState.READY);
      }
      return System.currentTimeMillis();
    } catch (Exception e) {
      throw new BonitaRuntimeException(e);
    }
  }

}


