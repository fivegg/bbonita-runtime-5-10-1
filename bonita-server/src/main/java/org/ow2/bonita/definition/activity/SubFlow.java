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
package org.ow2.bonita.definition.activity;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ow2.bonita.facade.QueryRuntimeAPI;
import org.ow2.bonita.facade.def.InternalActivityDefinition;
import org.ow2.bonita.facade.def.element.HookDefinition.Event;
import org.ow2.bonita.facade.def.element.SubflowParameterDefinition;
import org.ow2.bonita.facade.def.majorElement.DataFieldDefinition;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition.ProcessState;
import org.ow2.bonita.facade.exception.ActivityNotFoundException;
import org.ow2.bonita.facade.exception.InstanceNotFoundException;
import org.ow2.bonita.facade.exception.ProcessNotFoundException;
import org.ow2.bonita.facade.impl.StandardAPIAccessorImpl;
import org.ow2.bonita.facade.impl.StandardQueryAPIAccessorImpl;
import org.ow2.bonita.facade.runtime.AttachmentInstance;
import org.ow2.bonita.facade.runtime.InitialAttachment;
import org.ow2.bonita.facade.runtime.impl.InitialAttachmentImpl;
import org.ow2.bonita.facade.runtime.impl.InternalProcessInstance;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.runtime.model.Execution;
import org.ow2.bonita.services.Recorder;
import org.ow2.bonita.util.AccessorUtil;
import org.ow2.bonita.util.BonitaRuntimeException;
import org.ow2.bonita.util.EnvTool;
import org.ow2.bonita.util.ExceptionManager;
import org.ow2.bonita.util.GroovyException;
import org.ow2.bonita.util.GroovyUtil;
import org.ow2.bonita.util.Misc;
import org.ow2.bonita.util.ProcessUtil;

/**
 * @author Marc Blachon, Guillaume Porcher, Charles Souillard, Miguel Valdes, Pierre Vigneras
 */
public class SubFlow extends AbstractActivity {

  private static final long serialVersionUID = 477565487347215726L;

  protected static final Logger LOG = Logger.getLogger(SubFlow.class.getName());

  public static final String SUBFLOW_SIGNAL = "end_of_subflow";

  protected  SubFlow() {
    super();
  }

  public SubFlow(String activityName) {
    super(activityName);
  }

  @Override
  protected boolean bodyStartAutomatically() {
    return true;
  }

  @Override
  protected boolean executeBusinessLogic(final Execution execution) {
    final InternalActivityDefinition activityDefinition = execution.getNode();
    ConnectorExecutor.executeConnectors(activityDefinition, execution, Event.automaticOnEnter);
    String subflowProcessName = activityDefinition.getSubflowProcessName();
    String subflowProcessVersion = activityDefinition.getSubflowProcessVersion();
    if (Misc.isJustAGroovyExpression(subflowProcessName)) {
      try {
        subflowProcessName = (String) GroovyUtil.evaluate(subflowProcessName, null, execution.getActivityInstanceUUID(), false, false);
      } catch (GroovyException e) {
        // nothing
      }
    }
    if (subflowProcessVersion != null && Misc.isJustAGroovyExpression(subflowProcessVersion)) {
      try {
        subflowProcessVersion = (String) GroovyUtil.evaluate(subflowProcessVersion, null, execution.getActivityInstanceUUID(), false, false);
      } catch (GroovyException e) {
        // nothing
      }
    }
    ProcessDefinition subProcess = null;
    if (subflowProcessVersion == null) {
      subProcess = EnvTool.getJournalQueriers().getLastDeployedProcess(subflowProcessName, ProcessState.ENABLED);
    } else {
      subProcess = EnvTool.getJournalQueriers().getProcess(subflowProcessName, subflowProcessVersion);
    }
    if (subProcess == null) {
      String message = ExceptionManager.getInstance().getFullMessage("be_SF_1", subflowProcessName);
      throw new BonitaRuntimeException(message);
    }
    if (!ProcessState.ENABLED.equals(subProcess.getState())) {
      throw new BonitaRuntimeException("Subprocess: " + subProcess.getUUID() + " is not enabled. Can not use it as a subflow.");
    }
    if (LOG.isLoggable(Level.FINE)) {
      LOG.fine("Starting a new instance of process (as a subProcess) : " + subflowProcessName);
    }

    final InternalProcessInstance instance = execution.getInstance();
    final ProcessInstanceUUID instanceUUID = instance.getUUID();
    final ActivityInstanceUUID activityUUID = execution.getActivityInstanceUUID();

    try {
      final Map<String, Object> parameterValues = new HashMap<String, Object>();
      final Collection<InitialAttachment> attachments = new HashSet<InitialAttachment>();
      final Set<SubflowParameterDefinition> inParameters = activityDefinition.getSubflowInParameters();
      
      if (inParameters != null) {
        final StandardQueryAPIAccessorImpl accessor = new StandardQueryAPIAccessorImpl();
        final QueryRuntimeAPI queryRuntimeAPI = accessor.getQueryRuntimeAPI(AccessorUtil.QUERYLIST_JOURNAL_KEY);
        final Map<String, Object> variables = queryRuntimeAPI.getVariables(activityUUID);
        final Set<String> attachmentNames = queryRuntimeAPI.getAttachmentNames(instanceUUID);
        
        for (final SubflowParameterDefinition parameter : inParameters) {
          final String source = parameter.getSource();
          final String destination = parameter.getDestination();
          if (variables.containsKey(source)) {
            parameterValues.put(destination, variables.get(source));
          } else if (attachmentNames.contains(source)) {
            final AttachmentInstance attachmentInstance = queryRuntimeAPI.getLastAttachment(instanceUUID, source, activityUUID);
            final byte[] attachmentValue = queryRuntimeAPI.getAttachmentValue(attachmentInstance);
            final InitialAttachmentImpl initialAttachment = new InitialAttachmentImpl(destination, attachmentValue);
            initialAttachment.setDescription(attachmentInstance.getDescription());
            initialAttachment.setFileName(attachmentInstance.getFileName());
            initialAttachment.setLabel(attachmentInstance.getLabel());
            initialAttachment.setMetaData(attachmentInstance.getMetaData());
            attachments.add(initialAttachment);
          }          
        }
      }

      final ProcessInstanceUUID rootInstanceUUID = instance.getRootInstanceUUID();
      final ProcessDefinitionUUID subProcessUUID = subProcess.getUUID();
      final Execution subflowRootExecution = ProcessUtil.createProcessInstance(subProcessUUID, parameterValues, attachments, instanceUUID, rootInstanceUUID, null, activityUUID);
      
      if (LOG.isLoggable(Level.FINE)) {
        LOG.fine("Started subprocess instance : " + instance);
      }
      
      final InternalProcessInstance subflowInstance = subflowRootExecution.getInstance();
      ProcessUtil.startEventSubProcesses(subflowInstance);
      execution.getActivityInstance().setSubflowProcessInstanceUUID(subflowInstance.getProcessInstanceUUID());
      subflowInstance.begin(null);
    } catch (ProcessNotFoundException e) {
      throw new BonitaRuntimeException(e);
    } catch (ActivityNotFoundException e2) {
      throw new BonitaRuntimeException(e2);
    } catch (InstanceNotFoundException e3) {
      throw new BonitaRuntimeException(e3);
    }
    return false;
  }

  @Override
  public void signal(final Execution execution, final String signal, final Map<String, Object> signalParameters) {
    if (SUBFLOW_SIGNAL.equals(signal)) {
      final InternalActivityDefinition activityDefinition = execution.getNode();
      final Set<SubflowParameterDefinition> outParameters = activityDefinition.getSubflowOutParameters();
      if (outParameters != null) {
        final Recorder recorder = EnvTool.getRecorder();
        final ProcessInstanceUUID childInstanceUUID = (ProcessInstanceUUID) signalParameters.get("childInstanceUUID");
        final StandardAPIAccessorImpl accessor = new StandardAPIAccessorImpl();
        final QueryRuntimeAPI queryRuntimeAPI = accessor.getQueryRuntimeAPI(AccessorUtil.QUERYLIST_JOURNAL_KEY);
        Map<String, Object> childInstanceVariables = null;
        try {
          childInstanceVariables = queryRuntimeAPI.getProcessInstanceVariables(childInstanceUUID);
        } catch (InstanceNotFoundException e) {
          throw new BonitaRuntimeException(e);
        }
        final ActivityInstanceUUID activityUUID = execution.getActivityInstanceUUID();
        final ProcessInstanceUUID instanceUUID = execution.getInstance().getProcessInstanceUUID();
        final Set<String> activityVariableNames = new HashSet<String>();
        if (activityDefinition.getDataFields() != null) {
          for (DataFieldDefinition df : activityDefinition.getDataFields()) {
            activityVariableNames.add(df.getName());
          }
        }
        final Set<String> childInstanceAttachmentNames = queryRuntimeAPI.getAttachmentNames(childInstanceUUID);
        for (final SubflowParameterDefinition parameter : outParameters) {
          final String source = parameter.getSource();
          final String destination = parameter.getDestination();
          
          final Object variableValue = childInstanceVariables.get(source);
          
          if (activityVariableNames.contains(destination)) {
            recorder.recordActivityVariableUpdated(destination, variableValue, activityUUID, EnvTool.getUserId());
          } else if (childInstanceAttachmentNames.contains(source)) {
            final AttachmentInstance childAttachment = queryRuntimeAPI.getLastAttachment(childInstanceUUID, source);
            final byte[] childAttachmentValue = queryRuntimeAPI.getAttachmentValue(childAttachment);
            accessor.getRuntimeAPI().addAttachment(instanceUUID, destination, childAttachment.getLabel(), childAttachment.getDescription(), childAttachment.getFileName(), childAttachment.getMetaData(), childAttachmentValue);
          } else {
            recorder.recordInstanceVariableUpdated(destination, variableValue, instanceUUID, EnvTool.getUserId());
          }
        }
      }
      ConnectorExecutor.executeConnectors(activityDefinition, execution, Event.automaticOnExit);
      super.signal(execution, BODY_FINISHED, null);
    } else {
      super.signal(execution, signal, signalParameters);
    }
  }

}
