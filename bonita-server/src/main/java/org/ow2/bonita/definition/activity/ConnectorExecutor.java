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

import groovy.lang.Binding;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.hibernate.AssertionFailure;
import org.hibernate.StaleStateException;
import org.hibernate.exception.LockAcquisitionException;
import org.ow2.bonita.connector.core.Connector;
import org.ow2.bonita.connector.core.Filter;
import org.ow2.bonita.connector.core.MultiInstantiatorInstantiator;
import org.ow2.bonita.connector.core.MultiInstantiatorJoinChecker;
import org.ow2.bonita.connector.core.MultipleInstancesInstantiator;
import org.ow2.bonita.connector.core.MultipleInstancesJoinChecker;
import org.ow2.bonita.connector.core.PerformerAssignFilter;
import org.ow2.bonita.connector.core.ProcessConnector;
import org.ow2.bonita.connector.core.RoleResolver;
import org.ow2.bonita.connector.core.desc.Getter;
import org.ow2.bonita.connector.core.desc.Setter;
import org.ow2.bonita.definition.Hook;
import org.ow2.bonita.definition.MultiInstantiator;
import org.ow2.bonita.definition.MultiInstantiatorDescriptor;
import org.ow2.bonita.definition.PerformerAssign;
import org.ow2.bonita.definition.RoleMapper;
import org.ow2.bonita.definition.TxHook;
import org.ow2.bonita.facade.APIAccessor;
import org.ow2.bonita.facade.QueryDefinitionAPI;
import org.ow2.bonita.facade.QueryRuntimeAPI;
import org.ow2.bonita.facade.RuntimeAPI;
import org.ow2.bonita.facade.def.element.ConnectorDefinition;
import org.ow2.bonita.facade.def.element.HookDefinition;
import org.ow2.bonita.facade.def.element.HookDefinition.Event;
import org.ow2.bonita.facade.def.element.MultiInstantiationDefinition;
import org.ow2.bonita.facade.def.majorElement.ActivityDefinition;
import org.ow2.bonita.facade.def.majorElement.DataFieldDefinition;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.exception.ActivityNotFoundException;
import org.ow2.bonita.facade.exception.BonitaWrapperException;
import org.ow2.bonita.facade.exception.HookInvocationException;
import org.ow2.bonita.facade.exception.InstanceNotFoundException;
import org.ow2.bonita.facade.exception.UnRollbackableException;
import org.ow2.bonita.facade.impl.StandardAPIAccessorImpl;
import org.ow2.bonita.facade.impl.StandardQueryAPIAccessorImpl;
import org.ow2.bonita.facade.runtime.ActivityInstance;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.ProcessInstance;
import org.ow2.bonita.facade.runtime.TaskInstance;
import org.ow2.bonita.facade.runtime.impl.InternalActivityInstance;
import org.ow2.bonita.facade.runtime.impl.InternalProcessInstance;
import org.ow2.bonita.facade.uuid.ActivityDefinitionUUID;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.light.LightProcessDefinition.ProcessType;
import org.ow2.bonita.light.LightProcessInstance;
import org.ow2.bonita.runtime.ExtensionPointsPolicy;
import org.ow2.bonita.runtime.event.EventConstants;
import org.ow2.bonita.runtime.event.Job;
import org.ow2.bonita.runtime.event.JobBuilder;
import org.ow2.bonita.runtime.model.Execution;
import org.ow2.bonita.services.EventService;
import org.ow2.bonita.services.Recorder;
import org.ow2.bonita.util.AccessorUtil;
import org.ow2.bonita.util.BonitaConstants;
import org.ow2.bonita.util.BonitaException;
import org.ow2.bonita.util.BonitaRuntimeException;
import org.ow2.bonita.util.EnvTool;
import org.ow2.bonita.util.ExceptionManager;
import org.ow2.bonita.util.GroovyBindingBuilder;
import org.ow2.bonita.util.GroovyException;
import org.ow2.bonita.util.GroovyExpression;
import org.ow2.bonita.util.GroovyUtil;
import org.ow2.bonita.util.Misc;
import org.ow2.bonita.util.TransientData;

public final class ConnectorExecutor {

  private static final Logger LOG = Logger.getLogger(ConnectorExecutor.class.getName());

  private ConnectorExecutor() {
  }

  private static Binding getBinding(final Map<String, Object> extraParameters, final ProcessDefinitionUUID processUUID,
      final ActivityInstanceUUID activityInstanceUUID, final ProcessInstanceUUID instanceUUID,
      final boolean useCurrentVariableValue) throws ActivityNotFoundException, GroovyException,
      InstanceNotFoundException {
    boolean useActivityScope = false;
    boolean useInitValues = false;

    if (instanceUUID != null) {
      useInitValues = !useCurrentVariableValue;
    } else if (activityInstanceUUID != null) {
      useActivityScope = !useCurrentVariableValue;
    }
    final Map<String, Object> allVariables = GroovyBindingBuilder.getContext(extraParameters, processUUID,
        activityInstanceUUID, instanceUUID, useActivityScope, useInitValues);
    Binding binding = null;
    try {
      binding = GroovyBindingBuilder.getSimpleBinding(allVariables, processUUID, instanceUUID, activityInstanceUUID);
    } catch (final Exception e) {
      throw new BonitaRuntimeException(e);
    }
    return binding;
  }

  public static void executeConnector(final TxHook connector, final Map<String, Object[]> connectorParameters,
      final ProcessInstanceUUID instanceUUID, final ActivityInstance activityInst,
      final Map<String, Object> extraParameters) throws Exception {
    final ProcessInstance instance = EnvTool.getJournalQueriers().getProcessInstance(instanceUUID);
    final ProcessDefinitionUUID processUUID = instance.getProcessDefinitionUUID();
    ActivityInstanceUUID activityInstanceUUID = null;
    if (activityInst != null) {
      activityInstanceUUID = activityInst.getUUID();
    }
    Map<String, Object[]> inputs = new HashMap<String, Object[]>();
    if (connectorParameters != null) {
      inputs = getInputs(connectorParameters);
    }
    final APIAccessor accessor = new StandardAPIAccessorImpl();
    if (connector instanceof ProcessConnector) {
      ((ProcessConnector) connector).setApiAccessor(accessor);
      ((ProcessConnector) connector).setProcessDefinitionUUID(processUUID);
      ((ProcessConnector) connector).setProcessInstanceUUID(instanceUUID);
      if (activityInst != null) {
        ((ProcessConnector) connector).setActivityInstanceUUID(activityInstanceUUID);
      }
    }
    final ClassLoader baseClassLoader = Thread.currentThread().getContextClassLoader();
    try {
      final ClassLoader processClassLoader = EnvTool.getClassDataLoader().getProcessClassLoader(processUUID);
      Thread.currentThread().setContextClassLoader(processClassLoader);

      final Binding binding = getBinding(extraParameters, processUUID, activityInstanceUUID, instanceUUID, true);

      if (!inputs.isEmpty()) {
        setParameters(binding, inputs, connector);
      }
      connector.execute(accessor, activityInst);
      if (connectorParameters != null) {
        final Map<String, Object[]> outputs = getOuputs(connectorParameters);
        if (activityInst != null) {
          setProcessOrActivityVariables(binding, outputs, connector, activityInstanceUUID, instanceUUID);
        } else {
          setProcessOrActivityVariables(binding, outputs, connector, null, instanceUUID);
        }
      }
    } finally {
      Thread.currentThread().setContextClassLoader(baseClassLoader);
    }
  }

  public static Map<String, Object> executeConnector(final TxHook connector,
      final Map<String, Object[]> connectorParameters, final ProcessDefinitionUUID processDefinitionUUID,
      final Map<String, Object> extraParameters) throws Exception {
    Map<String, Object[]> inputs = new HashMap<String, Object[]>();
    if (connectorParameters != null) {
      inputs = getInputs(connectorParameters);
    }
    final APIAccessor accessor = new StandardAPIAccessorImpl();
    if (connector instanceof ProcessConnector) {
      ((ProcessConnector) connector).setApiAccessor(accessor);
      ((ProcessConnector) connector).setProcessDefinitionUUID(processDefinitionUUID);
    }
    final ClassLoader baseClassLoader = Thread.currentThread().getContextClassLoader();
    Map<String, Object> newVariableValues = new HashMap<String, Object>();
    try {
      final ClassLoader processClassLoader = EnvTool.getClassDataLoader().getProcessClassLoader(processDefinitionUUID);
      Thread.currentThread().setContextClassLoader(processClassLoader);
      if (!inputs.isEmpty()) {
        final Binding binding = getBinding(extraParameters, processDefinitionUUID, null, null, true);
        setParameters(binding, inputs, connector);
      }
      connector.execute(accessor, null);
      if (connectorParameters != null) {
        final Map<String, Object[]> outputs = getOuputs(connectorParameters);
        newVariableValues = getProcessVariables(outputs, connector, processDefinitionUUID);
      }
    } finally {
      Thread.currentThread().setContextClassLoader(baseClassLoader);
    }
    return newVariableValues;
  }

  private static Map<String, Object> getGetterValues(final Connector connector) {
    final List<Getter> getters = connector.getGetters();
    final Map<String, Object> values = new HashMap<String, Object>();
    if (getters != null) {
      for (final Getter getter : getters) {
        try {
          final String getterName = Connector.getGetterName(getter.getName());
          final Method m = connector.getClass().getMethod(getterName, new Class[0]);
          final Object variableValue = m.invoke(connector, new Object[0]);
          values.put(getter.getName(), variableValue);
        } catch (final Exception e) {
          throw new BonitaRuntimeException(e.getMessage(), e);
        }
      }
    }
    return values;
  }

  @SuppressWarnings("deprecation")
  private static void setProcessOrActivityVariables(final Binding binding, final Map<String, Object[]> outputs,
      final Object connector, final ActivityInstanceUUID activityInstanceUUID, final ProcessInstanceUUID instanceUUID)
      throws BonitaException {
    if (connector instanceof Connector) {
      final Map<String, Object> values = getGetterValues((Connector) connector);

      if (values != null) {
        for (final Map.Entry<String, Object> value : values.entrySet()) {
          binding.setVariable(value.getKey(), value.getValue());
        }
      }

      if (outputs.size() > 0) {
        final StandardAPIAccessorImpl accessor = new StandardAPIAccessorImpl();
        final RuntimeAPI runtime = accessor.getRuntimeAPI();
        for (final Entry<String, Object[]> output : outputs.entrySet()) {
          final String expression = (String) output.getValue()[0];
          final String variableName = output.getKey();
          Object variableValue;
          try {
            variableValue = GroovyUtil.evaluate(expression, binding);
          } catch (final Exception e) {
            throw new BonitaRuntimeException(e);
          }
          String dataTypeClassName = null;
          try {
            dataTypeClassName = getDataTypeClassName(variableName, instanceUUID, activityInstanceUUID);
          } catch (final BonitaRuntimeException e) {
            final ProcessDefinition process = accessor.getQueryDefinitionAPI().getProcess(
                instanceUUID.getProcessDefinitionUUID());
            if (ProcessType.EVENT_SUB_PROCESS == process.getType()) {
              final QueryRuntimeAPI queryRuntimeAPI = accessor.getQueryRuntimeAPI();
              final LightProcessInstance processInstance = queryRuntimeAPI.getLightProcessInstance(instanceUUID);
              dataTypeClassName = getDataTypeClassName(variableName, processInstance.getParentInstanceUUID(), null);
            }
          }
          final Object newValue = convertIfPossible(variableValue, dataTypeClassName);
          if (activityInstanceUUID != null) {
            runtime.setVariable(activityInstanceUUID, variableName, newValue);
          } else {
            runtime.setProcessInstanceVariable(instanceUUID, variableName, newValue);
          }
        }
      }
    }
  }

  private static Map<String, Object> getProcessVariables(final Map<String, Object[]> outputs, final Object connector,
      final ProcessDefinitionUUID processDefinitionUUID) throws BonitaException {
    final Map<String, Object> newVariableValues = new HashMap<String, Object>();
    if (connector instanceof Connector) {
      final Map<String, Object> values = getGetterValues((Connector) connector);
      for (final Entry<String, Object[]> output : outputs.entrySet()) {
        final String expression = (String) output.getValue()[0];
        final String variableName = output.getKey();
        final Object variableValue = GroovyUtil.evaluate(expression, values, processDefinitionUUID, false);
        final Object newValue = convertIfPossible(variableValue,
            getDataTypeClassName(variableName, processDefinitionUUID));
        newVariableValues.put(variableName, newValue);
      }
    }
    return newVariableValues;
  }

  private static String getDataTypeClassName(String variableName, final ProcessInstanceUUID instanceUUID,
      final ActivityInstanceUUID activityUUID) {
    if (isXPathVariableReference(variableName)) {
      return String.class.getName();
    }
    variableName = Misc.getVariableName(variableName);
    final StandardQueryAPIAccessorImpl accessor = new StandardQueryAPIAccessorImpl();
    final QueryDefinitionAPI queryDefinitionAPI = accessor.getQueryDefinitionAPI(AccessorUtil.QUERYLIST_JOURNAL_KEY);
    if (activityUUID != null) {
      try {
        final ActivityInstance activity = EnvTool.getJournalQueriers().getActivityInstance(activityUUID);
        return getActivityDataTypeClassName(queryDefinitionAPI, activity.getActivityDefinitionUUID(), variableName);
      } catch (final Exception e) {
        final ProcessInstance instance = EnvTool.getJournalQueriers().getProcessInstance(instanceUUID);
        return getProcessDataTypeClassName(queryDefinitionAPI, instance.getProcessDefinitionUUID(), variableName);
      }
    } else {
      final ProcessInstance instance = EnvTool.getJournalQueriers().getProcessInstance(instanceUUID);
      return getProcessDataTypeClassName(queryDefinitionAPI, instance.getProcessDefinitionUUID(), variableName);
    }
  }

  private static String getActivityDataTypeClassName(final QueryDefinitionAPI queryDefinitionAPI,
      final ActivityDefinitionUUID activityDefinitionUUID, final String variableName) {
    try {
      final DataFieldDefinition data = queryDefinitionAPI.getActivityDataField(activityDefinitionUUID, variableName);
      return data.getDataTypeClassName();
    } catch (final Exception e) {
      throw new BonitaRuntimeException(e.getMessage(), e);
    }
  }

  private static String getProcessDataTypeClassName(final QueryDefinitionAPI queryDefinitionAPI,
      final ProcessDefinitionUUID processDefinitionUUID, final String variableName) {
    try {
      final DataFieldDefinition data = queryDefinitionAPI.getProcessDataField(processDefinitionUUID, variableName);
      return data.getDataTypeClassName();
    } catch (final Exception e) {
      throw new BonitaRuntimeException(e.getMessage(), e);
    }
  }

  private static String getDataTypeClassName(String variableName, final ProcessDefinitionUUID processDefinitionUUID) {
    if (isXPathVariableReference(variableName)) {
      return String.class.getName();
    }
    variableName = Misc.getVariableName(variableName);
    final StandardQueryAPIAccessorImpl accessor = new StandardQueryAPIAccessorImpl();
    final QueryDefinitionAPI queryDefinitionAPI = accessor.getQueryDefinitionAPI(AccessorUtil.QUERYLIST_JOURNAL_KEY);
    try {
      final DataFieldDefinition data = queryDefinitionAPI.getProcessDataField(processDefinitionUUID, variableName);
      return data.getDataTypeClassName();
    } catch (final Exception a) {
      throw new BonitaRuntimeException(a.getMessage(), a);
    }
  }

  private static boolean isXPathVariableReference(final String variableName) {
    return variableName.contains(BonitaConstants.XPATH_VAR_SEPARATOR);
  }

  private static Map<String, Object[]> getOuputs(final Map<String, Object[]> connectorParameters) {
    final Map<String, Object[]> outputs = new HashMap<String, Object[]>();
    for (final Entry<String, Object[]> parameter : connectorParameters.entrySet()) {
      final String methodName = parameter.getKey();
      if (!Misc.isSetter(methodName)) {
        outputs.put(methodName, parameter.getValue());
      }
    }
    return outputs;
  }

  private static Map<String, Object[]> getInputs(final Map<String, Object[]> connectorParameters) {
    final Map<String, Object[]> inputs = new HashMap<String, Object[]>();
    for (final Entry<String, Object[]> parameter : connectorParameters.entrySet()) {
      final String methodName = parameter.getKey();
      if (Misc.isSetter(methodName)) {
        inputs.put(methodName, parameter.getValue());
      }
    }
    return inputs;
  }

  private static Setter getSetter(final List<Setter> inputs, final String parameterName) {
    if (inputs != null) {
      for (final Setter setter : inputs) {
        if (setter.getSetterName().equals(parameterName)) {
          return setter;
        }
      }
    }
    return null;
  }

  private static Object convertIfPossible(final Object variableValue, final String dataTypeClassName) {
    try {
      return Misc.convertIfPossible("", variableValue, dataTypeClassName);
    } catch (final Exception e) {
      return variableValue;
    }
  }

  private static void setParameters(final Binding binding, final Map<String, Object[]> parameters,
      final Object connector) throws BonitaException {
    if (parameters != null) {
      for (final Entry<String, Object[]> parameter : parameters.entrySet()) {
        final String methodName = parameter.getKey();
        final Object[] methodParameters = evaluateParametersWithGroovy(binding, parameter.getValue(), connector);

        Setter setter = null;
        if (connector instanceof Connector) {
          final List<Setter> setters = ((Connector) connector).getSetters();
          if (setters != null) {
            setter = getSetter(setters, methodName);
            if (setter != null) {
              final Object[] setterParameters = setter.getParameters();
              for (int i = 0; i < setterParameters.length; i++) {
                final Object setterParameter = setterParameters[i];
                final Class<?> setterParameterClass = setterParameter.getClass();
                if (setterParameterClass != null && methodParameters[i] != null
                    && !setterParameterClass.equals(methodParameters[i].getClass())) {
                  final Class<?> methodParameterClass = methodParameters[i].getClass();
                  if (methodParameterClass.equals(BigDecimal.class) || methodParameterClass.equals(BigInteger.class)) {
                    methodParameters[i] = convertIfPossible(methodParameters[i].toString(),
                        setterParameterClass.getName());
                  } else if (methodParameterClass.equals(String.class)) {
                    methodParameters[i] = convertIfPossible(methodParameters[i], setterParameterClass.getName());
                  }
                }
              }
            }
          }
        }

        Object[] setterParameters = null;
        if (setter != null) {
          setterParameters = setter.getParameters();
        }
        final Class<?>[] parameterClasses = new Class[methodParameters.length];
        for (int i = 0; i < parameterClasses.length; i++) {
          if (methodParameters[i] == null) {
            parameterClasses[i] = setterParameters[i].getClass();
          } else {
            parameterClasses[i] = methodParameters[i].getClass();
          }
        }

        try {
          final Method m = Connector.getMethod(connector.getClass(), methodName, parameterClasses);
          if (m == null) {
            final StringBuilder parambuilder = new StringBuilder();
            if (parameterClasses != null) {
              parambuilder.append("[");
              for (int i = 0; i < parameterClasses.length; i++) {
                final Class<?> class1 = parameterClasses[i];
                parambuilder.append(class1);
                parambuilder.append("=");
                final Object parameterValue = methodParameters[i];
                if (parameterValue == null) {
                  parambuilder.append("null");
                } else {
                  parambuilder.append(parameterValue);
                }
                parambuilder.append(",");
              }
              parambuilder.insert(parambuilder.length() - 1, "").append("]");
            }
            throw new BonitaRuntimeException("Unable to find a method with name: " + methodName + " and parameters: "
                + parambuilder.toString() + " in connector: " + connector.getClass());
          }
          m.invoke(connector, methodParameters);
        } catch (final Exception e) {
          throw new BonitaRuntimeException(e.getMessage(), e);
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  private static Object[] evaluateParametersWithGroovy(final Binding binding, final Object[] parameters,
      final Object connector) {
    final Object[] methodParameters = parameters;
    for (int i = 0; i < parameters.length; i++) {
      final Object methodParameter = methodParameters[i];
      if (methodParameter instanceof List<?>) {
        final List<Object> temp = (List<Object>) methodParameter;
        for (int j = 0; j < temp.size(); j++) {
          final Object tempRow = temp.get(j);
          if (tempRow instanceof List<?>) {
            final List<Object> row = (List<Object>) tempRow;
            for (int k = 0; k < row.size(); k++) {
              row.set(k, getEvaluatedExpression(binding, row.get(k), connector));
            }
            temp.set(j, row);
          } else {
            temp.set(j, getEvaluatedExpression(binding, temp.get(j), connector));
          }
        }
        methodParameters[i] = temp;
      } else {
        methodParameters[i] = getEvaluatedExpression(binding, methodParameter, connector);
      }
    }
    return methodParameters;
  }

  private static Object getEvaluatedExpression(final Binding binding, final Object parameterMethod,
      final Object connector) {
    if (parameterMethod instanceof String) {
      final String expression = (String) parameterMethod;
      if (GroovyExpression.isGroovyExpression(expression)) {
        try {
          return GroovyUtil.evaluate(expression, binding);
        } catch (final Exception e) {
          final StringBuilder stb = new StringBuilder("Error while executing connector: '");
          stb.append(connector.getClass().getName());
          stb.append("'. ");
          throw new BonitaRuntimeException(stb.toString(), e);
        }
      }
    }
    return parameterMethod;
  }

  private static void executeConnector(final Execution execution, final String activityName,
      final ConnectorDefinition connector, final Map<String, Object> parameters) {
    if (connector != null) {
      final InternalProcessInstance instance = execution.getInstance();
      final ProcessInstanceUUID instanceUUID = instance.getUUID();
      final ActivityInstanceUUID activityInstanceUUID = execution.getActivityInstanceUUID();
      final ProcessDefinitionUUID processUUID = instance.getProcessDefinitionUUID();
      final boolean throwException = connector.isThrowingException();
      try {
        final Object hookInstance = EnvTool.getClassDataLoader().getInstance(Hook.class,
            instance.getProcessDefinitionUUID(), connector);
        if (LOG.isLoggable(Level.FINE)) {
          if (activityInstanceUUID != null) {
            LOG.fine("Starting connector (instance=" + instanceUUID + ", process=" + processUUID + ", activityId="
                + activityName + ") : " + connector);
          } else {
            LOG.fine("Starting connector (instance=" + instanceUUID + ", process=" + processUUID + ") : " + connector);
          }
        }
        if (activityInstanceUUID == null) {
          executeConnector((TxHook) hookInstance, connector.getParameters(), instanceUUID, null, parameters);
        } else {
          final ActivityInstance activityInst = EnvTool.getJournalQueriers().getActivityInstance(activityInstanceUUID);
          if (hookInstance instanceof Hook) {
            final Hook hook = (Hook) hookInstance;
            hook.execute(new StandardQueryAPIAccessorImpl(), activityInst);
          } else if (hookInstance instanceof TxHook) {
            final TxHook txHook = (TxHook) hookInstance;
            executeConnector(txHook, connector.getParameters(), instanceUUID, activityInst, parameters);
          } else {
            final String message = ExceptionManager.getInstance().getFullMessage("bsi_HEI_2", Hook.class.getName(),
                TxHook.class.getName());
            throw new BonitaRuntimeException(message);
          }
          if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Finished connector (instance=" + instanceUUID + ", process=" + processUUID + ", activityId="
                + activityName + ") : " + connector);
          }
        }
      } catch (final StaleStateException sse) {
        throw sse;
      } catch (final AssertionFailure af) {
        throw af;
      } catch (final LockAcquisitionException lae) {
        throw lae;
      } catch (final Exception e) {
        String message = "null";
        if (e != null) {
          message = e.getMessage();
        }
        if (LOG.isLoggable(Level.SEVERE)) {
          LOG.severe("Exception caught while executing connector (instance=" + instanceUUID + ", process="
              + processUUID + ", activityId=" + activityName + ") : " + connector.getClassName() + " - Exception : "
              + message + " " + Misc.getStackTraceFrom(e));
        }

        final String errorCode = connector.getErrorCode();
        if (errorCode != null) {
          if (LOG.isLoggable(Level.SEVERE)) {
            LOG.severe("This exception raised an error event with code: " + errorCode);
          }
          final ActivityDefinition activityDefinition = execution.getNode();
          final String eventName = ActivityUtil.getErrorEventName(activityDefinition, errorCode);
          final Recorder recorder = EnvTool.getRecorder();
          if (eventName != null) {
            recorder.recordBodyAborted(execution.getActivityInstance());
            TransientData.removeTransientData(execution.getActivityInstanceUUID());
            final int indexOf = eventName.indexOf(EventConstants.SEPARATOR);
            final Job boundaryError = JobBuilder.boundaryErrorJob(eventName.substring(0, indexOf), execution
                .getInstance().getRootInstanceUUID(), execution.getEventUUID(), execution.getInstance()
                .getProcessInstanceUUID());
            EnvTool.getEventService().storeJob(boundaryError);
          } else {
            final Job errorJob = ActivityUtil.getErrorEventSubProcessJob(execution, errorCode);
            if (errorJob != null) {
              execution.abort();
              recorder.recordInstanceAborted(instance.getUUID(), EnvTool.getUserId());
              final EventService eventService = EnvTool.getEventService();
              eventService.storeJob(errorJob);
            } else {
              final ActivityInstanceUUID parentActivityUUID = instance.getParentActivityUUID();
              if (parentActivityUUID != null) {
                recorder.recordBodyAborted(execution.getActivityInstance());
                final ActivityInstance parentActivity = EnvTool.getJournalQueriers().getActivityInstance(
                    parentActivityUUID);
                final ActivityDefinition parentActivityDefinition = EnvTool.getJournalQueriers().getActivity(
                    parentActivity.getActivityDefinitionUUID());
                final String parentEventName = ActivityUtil.getErrorEventName(parentActivityDefinition, errorCode);
                final Execution parentExecution = EnvTool.getJournal().getExecutionOnActivity(
                    parentActivity.getProcessInstanceUUID(), parentActivity.getUUID());
                final int indexOf = parentEventName.indexOf(EventConstants.SEPARATOR);
                final Job boundaryError = JobBuilder.boundaryErrorJob(parentEventName.substring(0, indexOf),
                    parentExecution.getInstance().getRootInstanceUUID(), parentExecution.getEventUUID(),
                    parentExecution.getInstance().getProcessInstanceUUID());
                EnvTool.getEventService().storeJob(boundaryError);
                final Set<Execution> executions = EnvTool.getJournal().getExecutions(instanceUUID);
                executions.iterator().next().abort();
              } else {
                final InternalActivityInstance activityInstance = execution.getActivityInstance();
                if (activityInstance != null) {
                  recorder.recordActivityFailed(activityInstance);
                  if (ExtensionPointsPolicy.THROW_EXCPTION_ON_FAIL.equals(EnvTool.getExtensionPointsPolicy())) {
                    throw new UnRollbackableException("Error while executing connector", e);
                  }
                } else { // if don't find activity instance abort it
                  execution.abort();
                  recorder.recordInstanceAborted(instanceUUID, BonitaConstants.SYSTEM_USER);
                  instance.finish();
                }
              }
            }
          }
        } else if (throwException) {
          throw new BonitaWrapperException(new HookInvocationException("bsi_HEI_3", connector.getClassName()
              + " because " + message, e));
        } else if (LOG.isLoggable(Level.SEVERE)) {
          LOG.severe("This exception was ignored in order to continue the process execution");
        }
      }
    }
  }

  public static void executeConnectors(final ActivityDefinition activityDef, final Execution execution,
      final Event event, final Map<String, Object> parameters) {
    final List<HookDefinition> hooks = activityDef.getConnectors();
    if (hooks != null) {
      for (final HookDefinition hook : hooks) {
        if (hook.getEvent() != null && hook.getEvent().equals(event)) {
          if (!ActivityState.ABORTED.equals(execution.getActivityInstance().getState())) {
            executeConnector(execution, activityDef.getName(), hook, parameters);
          }
        }
      }
    }
  }

  public static void executeConnector(final Execution execution, final String activityName,
      final ConnectorDefinition connector) {
    executeConnector(execution, activityName, connector, null);
  }

  public static void executeConnectors(final Execution execution, final Event event) {
    final InternalProcessInstance instance = execution.getInstance();
    final ProcessDefinitionUUID processUUID = instance.getProcessDefinitionUUID();
    final ProcessDefinition definition = EnvTool.getJournalQueriers().getProcess(processUUID);
    final List<HookDefinition> connectors = definition.getConnectors();
    if (connectors != null) {
      for (final HookDefinition connector : connectors) {
        if (connector.getEvent() != null && event.equals(connector.getEvent())) {
          executeConnector(execution, null, connector);
        }
      }
    }
  }

  private static Map<String, Object[]> formatParameters(final Map<String, Object[]> parameters) {
    final Map<String, Object[]> formattedParameters = new HashMap<String, Object[]>();
    if (parameters != null) {
      for (final Entry<String, Object[]> parameter : parameters.entrySet()) {
        String parameterName = parameter.getKey();
        if (!parameterName.startsWith("set")) {
          final StringBuilder builder = new StringBuilder("set");
          builder.append(String.valueOf(parameterName.charAt(0)).toUpperCase());
          builder.append(parameterName.substring(1));
          parameterName = builder.toString();
        }
        formattedParameters.put(parameterName, parameter.getValue());
      }
    }
    return formattedParameters;
  }

  public static Map<String, Object> executeConnector(final Connector connector, final Map<String, Object[]> parameters)
      throws Exception {
    final Binding binding = getBinding(null, null, null, null, true);
    setParameters(binding, formatParameters(parameters), connector);
    connector.execute();
    return getGetterValues(connector);
  }

  public static Map<String, Object> executeConnector(final Connector connector,
      final ProcessDefinitionUUID processUUID, final ProcessInstanceUUID instanceUUID,
      final ActivityInstanceUUID activityInstanceUUID, final Map<String, Object[]> parameters,
      final Map<String, Object> extraParameters, final boolean useCurrentVariableValue) throws Exception {
    final Map<String, Object[]> inputs = formatParameters(parameters);
    if (connector instanceof ProcessConnector) {
      ((ProcessConnector) connector).setApiAccessor(new StandardAPIAccessorImpl());
      ((ProcessConnector) connector).setActivityInstanceUUID(activityInstanceUUID);
      ((ProcessConnector) connector).setProcessInstanceUUID(instanceUUID);
      ((ProcessConnector) connector).setProcessDefinitionUUID(processUUID);
    }
    final Binding binding = getBinding(extraParameters, processUUID, activityInstanceUUID, instanceUUID,
        useCurrentVariableValue);
    setParameters(binding, inputs, connector);
    connector.execute();
    return getGetterValues(connector);
  }

  public static Set<String> executeFilter(final Filter filter, Map<String, Object[]> parameters,
      final Set<String> members) throws Exception {
    Misc.checkArgsNotNull(members);
    if (parameters == null) {
      parameters = new HashMap<String, Object[]>();
    }
    filter.setMembers(members);
    filter.setApiAccessor(new StandardAPIAccessorImpl());
    final Binding binding = getBinding(null, null, null, null, true);
    setParameters(binding, formatParameters(parameters), filter);
    filter.execute();
    return filter.getCandidates();
  }

  public static Set<String> executeRoleResolver(final RoleResolver resolver, final Map<String, Object[]> parameters)
      throws Exception {
    final Binding binding = getBinding(null, null, null, null, true);
    setParameters(binding, formatParameters(parameters), resolver);
    return resolver.searchMembers(null, null, "test");
  }

  public static void executeConnectors(final ActivityDefinition activityDef, final Execution execution,
      final Event event) {
    executeConnectors(activityDef, execution, event, null);
  }

  public static MultiInstantiatorDescriptor executeMultiInstantiator(final Execution execution,
      final String activityId, final MultiInstantiator actInstantiator, final Map<String, Object[]> parameters)
      throws Exception {
    final ProcessInstanceUUID instanceUUID = execution.getInstance().getUUID();
    if (parameters != null) {
      final Map<String, Object[]> inputs = getInputs(parameters);
      final Binding binding = getBinding(null, null, null, instanceUUID, true);
      setParameters(binding, inputs, actInstantiator);
    }
    return actInstantiator.execute(new StandardQueryAPIAccessorImpl(), instanceUUID, activityId,
        execution.getIterationId());
  }

  public static Set<String> executeRoleMapper(final RoleMapper roleMapper, final TaskInstance task,
      final String roleId, final Map<String, Object[]> parameters) throws Exception {
    final ProcessInstanceUUID processInstanceUUID = task.getProcessInstanceUUID();
    if (parameters != null) {
      final Map<String, Object[]> inputs = getInputs(parameters);
      final ActivityInstanceUUID activityInstanceUUID = task.getUUID();
      final Binding binding = getBinding(null, null, activityInstanceUUID, processInstanceUUID, true);
      setParameters(binding, inputs, roleMapper);
    }
    return roleMapper.searchMembers(new StandardQueryAPIAccessorImpl(), processInstanceUUID, roleId);
  }

  public static Set<String> executeFilter(final Filter filter, final PerformerAssign performerAssign,
      final ActivityInstance activityInstance, final Set<String> candidates, final Map<String, Object[]> parameters)
      throws Exception {
    final ClassLoader baseClassLoader = Thread.currentThread().getContextClassLoader();
    try {
      final ClassLoader processClassLoader = EnvTool.getClassDataLoader().getProcessClassLoader(
          activityInstance.getProcessDefinitionUUID());
      Thread.currentThread().setContextClassLoader(processClassLoader);
      Map<String, Object[]> inputs = new HashMap<String, Object[]>();
      if (filter instanceof PerformerAssignFilter) {
        ((PerformerAssignFilter) filter).setPerformerAssign(performerAssign);
        if (parameters != null) {
          final Binding binding = getBinding(null, null, activityInstance.getUUID(), null, true);
          setParameters(binding, parameters, performerAssign);
        }
        ((PerformerAssignFilter) filter).setClassName(performerAssign.getClass().getName());
      } else if (parameters != null) {
        inputs = getInputs(parameters);
      }
      filter.setActivityInstanceUUID(activityInstance.getUUID());
      filter.setApiAccessor(new StandardAPIAccessorImpl());
      filter.setMembers(candidates);
      filter.setProcessDefinitionUUID(activityInstance.getProcessDefinitionUUID());
      filter.setProcessInstanceUUID(activityInstance.getProcessInstanceUUID());
      if (!inputs.isEmpty()) {
        final Binding binding = getBinding(null, null, activityInstance.getUUID(), null, true);
        setParameters(binding, inputs, filter);
      }
      filter.execute();
      return filter.getCandidates();
    } finally {
      Thread.currentThread().setContextClassLoader(baseClassLoader);
    }
  }

  public static List<Map<String, Object>> executeMultipleInstancesInstantiatior(
      final MultiInstantiationDefinition instantiator, final ProcessInstanceUUID instanceUUID,
      final String activityName, final String iterationId) throws Exception {
    final ProcessInstance instance = EnvTool.getJournalQueriers().getProcessInstance(instanceUUID);
    final ProcessDefinitionUUID definitionUUID = instance.getProcessDefinitionUUID();

    final ClassLoader baseClassLoader = Thread.currentThread().getContextClassLoader();
    try {
      final ClassLoader processClassLoader = EnvTool.getClassDataLoader().getProcessClassLoader(definitionUUID);
      Thread.currentThread().setContextClassLoader(processClassLoader);
      final MultipleInstancesInstantiator activityinstantiator = EnvTool.getClassDataLoader().getInstance(
          MultipleInstancesInstantiator.class, definitionUUID, instantiator);

      final Map<String, Object[]> parameters = instantiator.getParameters();
      Map<String, Object[]> inputs = new HashMap<String, Object[]>();

      if (parameters != null) {
        inputs = getInputs(parameters);
      }
      activityinstantiator.setActivityInstanceUUID(null);
      activityinstantiator.setActivityName(activityName);
      activityinstantiator.setApiAccessor(new StandardAPIAccessorImpl());
      activityinstantiator.setIterationId(iterationId);
      activityinstantiator.setProcessDefinitionUUID(definitionUUID);
      activityinstantiator.setProcessInstanceUUID(instanceUUID);

      final Binding binding = getBinding(null, null, null, instanceUUID, true);
      setParameters(binding, inputs, activityinstantiator);

      if (activityinstantiator.getClass().getName().equals(MultiInstantiatorInstantiator.class.getName())) {
        final String className = ((MultiInstantiatorInstantiator) activityinstantiator).getClassName();
        final org.ow2.bonita.connector.core.MultiInstantiator multiInstantiator = (org.ow2.bonita.connector.core.MultiInstantiator) EnvTool
            .getClassDataLoader().getInstance(definitionUUID, className);
        final Map<String, Object[]> temp = ((MultiInstantiatorInstantiator) activityinstantiator)
            .getInstantiatorParameters();
        if (temp != null) {
          ((MultiInstantiatorInstantiator) activityinstantiator).setActivityName(activityName);
          ((MultiInstantiatorInstantiator) activityinstantiator).setIterationId(iterationId);
          ((MultiInstantiatorInstantiator) activityinstantiator).setProcessInstanceUUID(instanceUUID);
          setParameters(binding, temp, multiInstantiator);
        }
        ((MultiInstantiatorInstantiator) activityinstantiator).setInstantiator(multiInstantiator);
      }

      activityinstantiator.execute();
      return activityinstantiator.getActivitiesContext();
    } finally {
      Thread.currentThread().setContextClassLoader(baseClassLoader);
    }
  }

  public static boolean executeMultipleInstancesJoinChecker(final MultiInstantiationDefinition joinChecker,
      final ActivityInstanceUUID activityUUID) throws Exception {
    final ActivityInstance activity = EnvTool.getJournalQueriers().getActivityInstance(activityUUID);
    final ProcessInstanceUUID instanceUUID = activity.getProcessInstanceUUID();
    final ProcessDefinitionUUID definitionUUID = activity.getProcessDefinitionUUID();

    final ClassLoader baseClassLoader = Thread.currentThread().getContextClassLoader();
    try {
      final ClassLoader processClassLoader = EnvTool.getClassDataLoader().getProcessClassLoader(definitionUUID);
      Thread.currentThread().setContextClassLoader(processClassLoader);
      final MultipleInstancesJoinChecker checker = EnvTool.getClassDataLoader().getInstance(
          MultipleInstancesJoinChecker.class, definitionUUID, joinChecker);
      final Map<String, Object[]> parameters = joinChecker.getParameters();
      Map<String, Object[]> inputs = new HashMap<String, Object[]>();
      if (parameters != null) {
        inputs = getInputs(parameters);
      }
      checker.setActivityInstanceUUID(activityUUID);
      checker.setActivityName(activity.getActivityName());
      checker.setApiAccessor(new StandardAPIAccessorImpl());
      checker.setIterationId(activity.getIterationId());
      checker.setProcessDefinitionUUID(definitionUUID);
      checker.setProcessInstanceUUID(instanceUUID);

      final Binding binding = getBinding(null, null, null, instanceUUID, true);
      setParameters(binding, inputs, checker);

      if (checker.getClass().getName().equals(MultiInstantiatorJoinChecker.class.getName())) {
        final String className = ((MultiInstantiatorJoinChecker) checker).getClassName();
        final org.ow2.bonita.connector.core.MultiInstantiator multiInstantiator = (org.ow2.bonita.connector.core.MultiInstantiator) EnvTool
            .getClassDataLoader().getInstance(definitionUUID, className);
        final Map<String, Object[]> temp = ((MultiInstantiatorJoinChecker) checker).getInstantiatorParameters();
        if (temp != null) {
          multiInstantiator.setActivityId(activity.getActivityName());
          multiInstantiator.setIterationId(activity.getIterationId());
          multiInstantiator.setProcessInstanceUUID(instanceUUID);
          setParameters(binding, temp, multiInstantiator);
        }
        ((MultiInstantiatorJoinChecker) checker).setInstantiator(multiInstantiator);
      }

      checker.execute();
      return checker.isJoinable();
    } catch (final Exception e) {
      final StringBuilder builder = new StringBuilder(
          "An Exception occurs during join checking of mutliple instances activity: '");
      builder.append(activity.getActivityName()).append("' of instance: ").append(instanceUUID).append(" of process: ")
          .append(definitionUUID).append(".\r\n Exception : ").append(e.getMessage());
      throw new BonitaRuntimeException(builder.toString());
    } finally {
      Thread.currentThread().setContextClassLoader(baseClassLoader);
    }
  }

}
