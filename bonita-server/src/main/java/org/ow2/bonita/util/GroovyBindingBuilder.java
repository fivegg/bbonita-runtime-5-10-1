/**
 * Copyright (C) 2009  BonitaSoft S.A.
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
package org.ow2.bonita.util;

import groovy.lang.Binding;
import groovy.lang.MissingPropertyException;

import java.io.IOException;
import java.io.NotSerializableException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ow2.bonita.facade.APIAccessor;
import org.ow2.bonita.facade.QueryDefinitionAPI;
import org.ow2.bonita.facade.QueryRuntimeAPI;
import org.ow2.bonita.facade.def.InternalProcessDefinition;
import org.ow2.bonita.facade.def.majorElement.DataFieldDefinition;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.def.majorElement.impl.ProcessDefinitionImpl;
import org.ow2.bonita.facade.exception.ActivityDefNotFoundException;
import org.ow2.bonita.facade.exception.ActivityNotFoundException;
import org.ow2.bonita.facade.exception.DataFieldNotFoundException;
import org.ow2.bonita.facade.exception.InstanceNotFoundException;
import org.ow2.bonita.facade.exception.ProcessNotFoundException;
import org.ow2.bonita.facade.impl.StandardAPIAccessorImpl;
import org.ow2.bonita.facade.impl.StandardQueryAPIAccessorImpl;
import org.ow2.bonita.facade.runtime.ActivityInstance;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.AttachmentInstance;
import org.ow2.bonita.facade.runtime.ProcessInstance;
import org.ow2.bonita.facade.runtime.VariableUpdate;
import org.ow2.bonita.facade.runtime.impl.ActivityInstanceImpl;
import org.ow2.bonita.facade.runtime.impl.InternalProcessInstance;
import org.ow2.bonita.facade.runtime.impl.ObjectVariable;
import org.ow2.bonita.facade.runtime.impl.ProcessInstanceImpl;
import org.ow2.bonita.facade.uuid.ActivityDefinitionUUID;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.services.DocumentationManager;

/**
 * 
 * @author Charles Souillard
 * 
 */
public class GroovyBindingBuilder {

  public static Binding getSimpleBinding(final Map<String, Object> allVariables,
      final ProcessDefinitionUUID processUUID, final ProcessInstanceUUID instanceUUID,
      final ActivityInstanceUUID activityUUID) throws IOException, ClassNotFoundException, GroovyException,
      InstanceNotFoundException, ActivityNotFoundException {

    return new SimpleBinding(allVariables, processUUID, instanceUUID, activityUUID);

  }

  public static Binding getPropagateBinding(final Map<String, Object> allVariables,
      final ProcessDefinitionUUID processUUID, final ProcessInstanceUUID instanceUUID,
      final ActivityInstanceUUID activityInstanceUUID, final Map<String, Object> context) throws IOException,
      ClassNotFoundException, GroovyException, InstanceNotFoundException, ActivityNotFoundException {

    ActivityDefinitionUUID activityUUID = null;
    if (activityInstanceUUID != null) {
      final ActivityInstance activityInstance = EnvTool.getAllQueriers().getActivityInstance(activityInstanceUUID);
      activityUUID = activityInstance.getActivityDefinitionUUID();
    }
    final Set<String> initialVariables = new HashSet<String>();
    initialVariables.addAll(allVariables.keySet());
    if (context != null) {
      initialVariables.removeAll(context.keySet());
    }
    return new PropagateBinding(processUUID, activityUUID, instanceUUID, activityInstanceUUID, allVariables,
        initialVariables);
  }

  public static Binding getSimpleBinding(final ProcessDefinitionUUID processUUID,
      final ProcessInstanceUUID instanceUUID, final ActivityInstanceUUID activityUUID,
      final Map<String, Object> context, final boolean useActivityScope, final boolean useInitialVariableValues)
      throws IOException, ClassNotFoundException, GroovyException, InstanceNotFoundException, ActivityNotFoundException {

    final Map<String, Object> allVariables = getContext(context, processUUID, activityUUID, instanceUUID,
        useActivityScope, useInitialVariableValues);
    return new SimpleBinding(allVariables, processUUID, instanceUUID, activityUUID);

  }

  public static Binding getPropagateBinding(final ProcessDefinitionUUID processUUID,
      final ProcessInstanceUUID instanceUUID, final ActivityInstanceUUID activityInstanceUUID,
      final Map<String, Object> context, final boolean useActivityScope, final boolean useInitialVariableValues)
      throws IOException, ClassNotFoundException, GroovyException, InstanceNotFoundException, ActivityNotFoundException {

    final Map<String, Object> allVariables = getContext(context, processUUID, activityInstanceUUID, instanceUUID,
        useActivityScope, useInitialVariableValues);
    ActivityDefinitionUUID activityUUID = null;
    if (activityInstanceUUID != null) {
      final ActivityInstance activityInstance = EnvTool.getAllQueriers().getActivityInstance(activityInstanceUUID);
      activityUUID = activityInstance.getActivityDefinitionUUID();
    }
    final Set<String> initialVariables = new HashSet<String>();
    initialVariables.addAll(allVariables.keySet());
    if (context != null) {
      initialVariables.removeAll(context.keySet());
    }
    return new PropagateBinding(processUUID, activityUUID, instanceUUID, activityInstanceUUID, allVariables,
        initialVariables);
  }

  private static Map<String, Object> getActivityInstanceVariables(final ActivityInstanceUUID activityUUID)
      throws ActivityNotFoundException {
    final Map<String, Object> activityInstanceVariables = new HashMap<String, Object>();
    if (activityUUID != null) {
      final StandardQueryAPIAccessorImpl accessor = new StandardQueryAPIAccessorImpl();
      final QueryRuntimeAPI api = accessor.getQueryRuntimeAPI();
      activityInstanceVariables.putAll(api.getActivityInstanceVariables(activityUUID));
    }
    return activityInstanceVariables;
  }

  private static long getActivityScopeDate(final ActivityInstance activityInstance, final long defaultDate) {
    long maxDate = -1;
    if (!activityInstance.getState().equals(ActivityState.READY)
        && !activityInstance.getState().equals(ActivityState.SUSPENDED)
        && !activityInstance.getState().equals(ActivityState.EXECUTING)) {
      maxDate = activityInstance.getLastStateUpdate().getUpdatedDate().getTime();
    } else {
      maxDate = defaultDate;
    }
    return maxDate;
  }

  private static Map<String, Object> getProcessInstanceVariables(final ProcessInstanceUUID instanceUUID,
      final ActivityInstanceUUID activityUUID, final boolean useInitialVariableValues, final boolean useActivityScope)
      throws InstanceNotFoundException {
    final Map<String, Object> processInstanceVariables = new HashMap<String, Object>();
    if (instanceUUID != null) {
      final StandardQueryAPIAccessorImpl accessor = new StandardQueryAPIAccessorImpl();
      final QueryRuntimeAPI api = accessor.getQueryRuntimeAPI();

      final ProcessInstance instance = EnvTool.getAllQueriers().getProcessInstance(instanceUUID);
      if (useInitialVariableValues) {
        processInstanceVariables.putAll(instance.getInitialVariableValues());
      } else {
        processInstanceVariables.putAll(api.getProcessInstanceVariables(instanceUUID));
      }
    }
    if (activityUUID != null) {
      final ActivityInstance activityInstance = EnvTool.getAllQueriers().getActivityInstance(activityUUID);

      final ProcessInstance instance = EnvTool.getAllQueriers().getProcessInstance(
          activityInstance.getProcessInstanceUUID());
      long maxDate = new Date().getTime();
      if (useActivityScope) {
        maxDate = getActivityScopeDate(activityInstance, maxDate);
      }
      final List<VariableUpdate> instanceVarUpdates = instance.getVariableUpdates();
      final Map<String, VariableUpdate> tmp = new HashMap<String, VariableUpdate>();

      for (final VariableUpdate varUpdate : instanceVarUpdates) {
        if (varUpdate.getDate().getTime() <= maxDate) {
          tmp.put(varUpdate.getName(), varUpdate);
        }
      }
      processInstanceVariables.putAll(instance.getInitialVariableValues());
      for (final VariableUpdate varUpdate : tmp.values()) {
        processInstanceVariables.put(varUpdate.getName(), varUpdate.getValue());
      }
    }

    return processInstanceVariables;
  }

  private static Map<String, Object> getProcessDatafieldsVariables(final ProcessDefinitionUUID processUUID,
      final Set<String> variablesToIgnore) throws InstanceNotFoundException, GroovyException {
    final Map<String, Object> processDatafieldsVariables = new HashMap<String, Object>();
    if (processUUID != null) {
      final InternalProcessDefinition process = EnvTool.getAllQueriers().getProcess(processUUID);
      final Set<DataFieldDefinition> datafields = process.getDataFields();
      processDatafieldsVariables.putAll(getMissingProcessDefinitionDataFields(datafields, variablesToIgnore,
          processUUID));
    }
    return processDatafieldsVariables;
  }

  private static Map<String, AttachmentInstance> getProcessInstanceAttachments(final ProcessInstanceUUID instanceUUID,
      final ActivityInstanceUUID activityInstanceUUID) {
    final Map<String, AttachmentInstance> processInstanceAttachments = new HashMap<String, AttachmentInstance>();
    // The history queriers are also needed because we want to be able to
    // evaluate expressions on terminated instances

    InternalProcessInstance instance = null;
    ProcessInstanceUUID processInstanceUUID = instanceUUID;
    if (instanceUUID != null) {
      instance = EnvTool.getAllQueriers().getProcessInstance(instanceUUID);
    } else if (activityInstanceUUID != null) {
      final ActivityInstance activityInstance = EnvTool.getAllQueriers().getActivityInstance(activityInstanceUUID);
      processInstanceUUID = activityInstance.getProcessInstanceUUID();
      instance = EnvTool.getAllQueriers().getProcessInstance(processInstanceUUID);
    }
    if (instance != null) {
      if (instance.getNbOfAttachments() > 0) {
        final DocumentationManager manager = EnvTool.getDocumentationManager();
        final List<AttachmentInstance> attachments = DocumentService.getLastAttachments(manager, processInstanceUUID);
        if (attachments != null) {
          for (final AttachmentInstance attachment : attachments) {
            processInstanceAttachments.put(attachment.getName(), attachment);
          }
        }
      }
    }
    return processInstanceAttachments;
  }

  public static Map<String, Object> getContext(final Map<String, Object> context,
      final ProcessDefinitionUUID processUUID, final ActivityInstanceUUID activityUUID,
      final ProcessInstanceUUID instanceUUID, final boolean useActivityScope, final boolean useInitialVariableValues)
      throws GroovyException, InstanceNotFoundException, ActivityNotFoundException {

    final Map<String, Object> allVariables = new HashMap<String, Object>();
    final Map<String, Object> processInstanceVariables = getProcessInstanceVariables(instanceUUID, activityUUID,
        useInitialVariableValues, useActivityScope);
    final Map<String, Object> activityInstanceVariables = getActivityInstanceVariables(activityUUID);

    allVariables.putAll(processInstanceVariables);
    allVariables.putAll(activityInstanceVariables);

    final Map<String, Object> processDatafieldsVariables = getProcessDatafieldsVariables(processUUID,
        allVariables.keySet());
    allVariables.putAll(processDatafieldsVariables);
    if (context != null) {
      allVariables.putAll(context);
    }
    return allVariables;
  }

  private static Map<String, Object> getMissingProcessDefinitionDataFields(final Set<DataFieldDefinition> datafields,
      final Set<String> variablesToIgnore, final ProcessDefinitionUUID processDefinitionUUID) throws GroovyException {
    final Map<String, Object> processDatafieldsVariables = new HashMap<String, Object>();

    if (datafields != null) {
      final List<DataFieldDefinition> variables = new ArrayList<DataFieldDefinition>();
      for (final DataFieldDefinition datafield : datafields) {
        if (!variablesToIgnore.contains(datafield.getName())) {
          final Object value = datafield.getInitialValue();
          final String script = datafield.getScriptingValue();
          if (value == null && script != null) {
            variables.add(datafield);
          } else {
            processDatafieldsVariables.put(datafield.getName(), value);
          }
        }
      }

      int size = variables.size();
      int index = 0;
      int errors = 0;
      while (size > 0 && index < size) {
        final DataFieldDefinition variable = variables.get(index);
        try {
          final Object value = GroovyUtil.evaluate(variable.getScriptingValue(), processDatafieldsVariables);
          processDatafieldsVariables.put(variable.getName(), value);
          variables.remove(index);
          size = variables.size();
          index = 0;
          errors = 0;
        } catch (final GroovyException e) {
          errors++;
          if (errors == size) {
            final StringBuilder stb = new StringBuilder("Unable to evaluate: '");
            stb.append(variable.getScriptingValue());
            stb.append("' on variable: '");
            stb.append(variable.getName());
            stb.append("' in process: '");
            stb.append(processDefinitionUUID);
            stb.append("' ");

            throw new GroovyException(stb.toString(), e);
          }
          index++;
        }
      }
    }

    return processDatafieldsVariables;
  }

  public static class SimpleBinding extends Binding {
    Map<String, Object> allVariables = new HashMap<String, Object>();

    private ProcessDefinitionUUID processUUID;
    private ProcessDefinition clientProcessDefinition;
    private ProcessInstanceUUID instanceUUID;
    private ProcessInstance clientProcessInstance;
    private final ActivityInstanceUUID activityInstanceUUID;
    private ActivityInstance clientActivityInstance;
    private APIAccessor apiAccessor;
    private String initiator;
    private boolean attachmentsWasLoaded;

    public SimpleBinding(final Map<String, Object> variables, final ProcessDefinitionUUID processUUID,
        final ProcessInstanceUUID instanceUUID, final ActivityInstanceUUID activityInstanceUUID) throws IOException,
        ClassNotFoundException {
      super();
      attachmentsWasLoaded = false;
      if (variables != null) {
        for (final Map.Entry<String, Object> variable : variables.entrySet()) {
          Object value = variable.getValue();
          if (value instanceof ObjectVariable) {
            value = ((ObjectVariable) value).getValue();
          }
          allVariables.put(variable.getKey(), value);
        }
      }
      if (processUUID != null) {
        this.processUUID = processUUID;
      } else if (instanceUUID != null) {
        this.processUUID = EnvTool.getAllQueriers().getProcessInstance(instanceUUID).getProcessDefinitionUUID();
      } else if (activityInstanceUUID != null) {
        this.processUUID = EnvTool.getAllQueriers().getActivityInstance(activityInstanceUUID)
            .getProcessDefinitionUUID();
      }

      if (instanceUUID != null) {
        this.instanceUUID = instanceUUID;
      } else if (activityInstanceUUID != null) {
        this.instanceUUID = EnvTool.getAllQueriers().getActivityInstance(activityInstanceUUID).getProcessInstanceUUID();
      }
      this.activityInstanceUUID = activityInstanceUUID;
    }

    @Override
    public Object getVariable(final String name) {
    	
    //add by robin
        Logger LOGGER = Logger.getLogger(getClass().getName());

        LOGGER.log(Level.WARNING, "***********getVariable allVariables********" + allVariables.toString());
        LOGGER.log(Level.WARNING, "***********getVariable name********" + name);
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();

        for(int i=0; i<elements.length; i++) {
        	LOGGER.log(Level.WARNING, "***********call stack ********" + elements[i].toString());
        }
      //end add 
        
        
      if (BonitaConstants.PROCESS_DEFINITION.equals(name)) {
        if (clientProcessDefinition == null && processUUID != null) {
          clientProcessDefinition = new ProcessDefinitionImpl(EnvTool.getAllQueriers().getProcess(processUUID));
        }
        return clientProcessDefinition;
      }
      if (BonitaConstants.PROCESS_INSTANCE.equals(name)) {
        if (clientProcessInstance == null && instanceUUID != null) {
          clientProcessInstance = new ProcessInstanceImpl(EnvTool.getAllQueriers().getProcessInstance(instanceUUID));
        }
        return clientProcessInstance;
      }
      if (BonitaConstants.ACTIVITY_INSTANCE.equals(name)) {
        if (clientActivityInstance == null && activityInstanceUUID != null) {
          clientActivityInstance = new ActivityInstanceImpl(EnvTool.getAllQueriers().getActivityInstance(
              activityInstanceUUID));
        }
        return clientActivityInstance;
      }
      if (BonitaConstants.LOGGED_USER.equals(name)) {
        try {
          return EnvTool.getUserId();
        } catch (final Throwable e) {
          return null;
        }
      }
      if (BonitaConstants.API_ACCESSOR.equals(name)) {
        if (apiAccessor == null) {
          apiAccessor = new StandardAPIAccessorImpl();
        }
        return apiAccessor;
      }
      if (BonitaConstants.PROCESS_INSTANCE_INITIATOR.equals(name)) {
        if (initiator == null) {
          if (instanceUUID != null) {
            final InternalProcessInstance processInstance = EnvTool.getAllQueriers().getProcessInstance(instanceUUID);
            initiator = processInstance.getStartedBy();
          }
        }
        return initiator;
      }
      if (allVariables.containsKey(name)) {
        return allVariables.get(name);
      }
      try {
        return super.getVariable(name);
      } catch (final MissingPropertyException e) {
        if (!attachmentsWasLoaded) {
          final Map<String, AttachmentInstance> attachments = getProcessInstanceAttachments(instanceUUID,
              activityInstanceUUID);
          allVariables.putAll(attachments);
          attachmentsWasLoaded = true;
          if (allVariables.containsKey(name)) {
            return allVariables.get(name);
          } else {
            throw e;
          }
        } else {
          throw e;
        }

      }
    }

    @Override
    public void setVariable(final String name, final Object value) {
      allVariables.put(name, value);
    }

    @Override
    public Map<String, Object> getVariables() {
      return allVariables;
    }
  }

  public static Object getInjectedVariable(final String variable, final ProcessDefinitionUUID processUUID,
      final ProcessInstanceUUID instanceUUID, final ActivityInstanceUUID activityInstanceUUID) {
    if (BonitaConstants.PROCESS_DEFINITION.equals(variable) && processUUID != null) {
      return new ProcessDefinitionImpl(EnvTool.getAllQueriers().getProcess(processUUID));
    }
    if (BonitaConstants.PROCESS_INSTANCE.equals(variable) && instanceUUID != null) {
      return new ProcessInstanceImpl(EnvTool.getAllQueriers().getProcessInstance(instanceUUID));
    }
    if (BonitaConstants.ACTIVITY_INSTANCE.equals(variable) && activityInstanceUUID != null) {
      return new ActivityInstanceImpl(EnvTool.getAllQueriers().getActivityInstance(activityInstanceUUID));
    }
    if (BonitaConstants.LOGGED_USER.equals(variable)) {
      try {
        return EnvTool.getUserId();
      } catch (final Throwable e) {
        return null;
      }
    }
    if (BonitaConstants.API_ACCESSOR.equals(variable)) {
      return new StandardAPIAccessorImpl();
    }
    if (BonitaConstants.PROCESS_INSTANCE_INITIATOR.equals(variable)) {
      if (instanceUUID != null) {
        final InternalProcessInstance processInstance = EnvTool.getAllQueriers().getProcessInstance(instanceUUID);
        return processInstance.getStartedBy();
      }
    }

    return null;
  }

  public static class PropagateBinding extends SimpleBinding {
    private final Collection<String> varToPropagate = new HashSet<String>();
    private final Collection<String> initialProcessVariableKeys = new HashSet<String>();
    private final Collection<String> initialObjectVariables = new HashSet<String>();
    private final Collection<String> initialNullVariables = new HashSet<String>();
    private final ActivityDefinitionUUID activityUUID;
    private final ProcessDefinitionUUID processUUID;

    public PropagateBinding(final ProcessDefinitionUUID processUUID, final ActivityDefinitionUUID activityUUID,
        final ProcessInstanceUUID instanceUUID, final ActivityInstanceUUID activityInstanceUUID,
        final Map<String, Object> variables, final Set<String> initialProcessVariableKeys) throws IOException,
        ClassNotFoundException {

      super(variables, processUUID, instanceUUID, activityInstanceUUID);

      this.initialProcessVariableKeys.addAll(initialProcessVariableKeys);
      if (variables != null) {
        for (final Map.Entry<String, Object> variable : variables.entrySet()) {
          Object value = variable.getValue();
          final String varName = variable.getKey();
          if (value instanceof ObjectVariable && initialProcessVariableKeys.contains(varName)) {
            value = ((ObjectVariable) value).getValue();
            initialObjectVariables.add(varName);
          }
          if (value == null) {
            initialNullVariables.add(varName);
          }
        }
      }
      this.activityUUID = activityUUID;
      this.processUUID = processUUID;
    }

    @Override
    public Object getVariable(final String name) {
      varToPropagate.add(name);
      return super.getVariable(name);
    }

    @Override
    public void setVariable(final String name, final Object value) {
      super.setVariable(name, value);
      if (initialProcessVariableKeys.contains(name)) {
        varToPropagate.add(name);
      }
    }

    public Map<String, Object> getVariablesToPropagate() throws NotSerializableException, IOException,
        ClassNotFoundException, ActivityDefNotFoundException, DataFieldNotFoundException, ProcessNotFoundException {
      final Map<String, Object> variablesToPropagate = new HashMap<String, Object>();
      for (final String varName : varToPropagate) {
        if (initialProcessVariableKeys.contains(varName)) {
          Object value = allVariables.get(varName);
          if (initialObjectVariables.contains(varName)) {
            value = new ObjectVariable(value);
          } else if (initialNullVariables.contains(varName)) {
            // try to get the type from definition
            final StandardQueryAPIAccessorImpl accessor = new StandardQueryAPIAccessorImpl();
            final QueryDefinitionAPI queryDefinitionAPI = accessor
                .getQueryDefinitionAPI(AccessorUtil.QUERYLIST_JOURNAL_KEY);
            DataFieldDefinition datafield = null;
            if (activityUUID != null) {
              try {
                datafield = queryDefinitionAPI.getActivityDataField(activityUUID, varName);
              } catch (final DataFieldNotFoundException e) {
                datafield = queryDefinitionAPI.getProcessDataField(processUUID, varName);
              }
            }
            if (datafield == null && processUUID != null) {
              datafield = queryDefinitionAPI.getProcessDataField(processUUID, varName);
            }
            if (datafield != null && datafield.getDataTypeClassName().equals(ObjectVariable.class.getName())) {
              value = new ObjectVariable(value);
            }
          }
          variablesToPropagate.put(varName, value);
        }
      }
      return variablesToPropagate;
    }
  }

}
