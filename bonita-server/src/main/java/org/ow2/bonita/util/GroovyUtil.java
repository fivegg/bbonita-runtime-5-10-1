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

import static org.ow2.bonita.util.GroovyExpression.END_DELIMITER;
import static org.ow2.bonita.util.GroovyExpression.START_DELIMITER;
import groovy.lang.Binding;
import groovy.lang.MissingPropertyException;
import groovy.lang.Script;

import java.io.IOException;
import java.io.NotSerializableException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.ow2.bonita.facade.RuntimeAPI;
import org.ow2.bonita.facade.exception.ActivityDefNotFoundException;
import org.ow2.bonita.facade.exception.DataFieldNotFoundException;
import org.ow2.bonita.facade.exception.ProcessNotFoundException;
import org.ow2.bonita.facade.impl.StandardAPIAccessorImpl;
import org.ow2.bonita.facade.runtime.ActivityInstance;
import org.ow2.bonita.facade.runtime.ProcessInstance;
import org.ow2.bonita.facade.runtime.impl.ObjectVariable;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.GroovyBindingBuilder.PropagateBinding;

/**
 * 
 * @author Matthieu Chaffotte
 * 
 */
public class GroovyUtil {

  public static Object evaluate(final String expression, final Map<String, Object> variables,
      final ClassLoader classLoader) throws GroovyException {
    return evaluate(expression, variables, null, null, null, false, false, false, classLoader);
  }

  public static Object evaluate(final String expression, final Map<String, Object> variables) throws GroovyException {
    return evaluate(expression, variables, null, null, null, false, false, false, null);
  }

  public static Object evaluate(final String expression, final Map<String, Object> context,
      final ActivityInstanceUUID activityUUID, final boolean useActivityScope, final boolean propagate)
      throws GroovyException {
    final ActivityInstance activity = EnvTool.getAllQueriers().getActivityInstance(activityUUID);
    final ProcessInstanceUUID processInstanceUUID = activity.getProcessInstanceUUID();
    final ProcessDefinitionUUID processDefinitionUUID = activity.getProcessDefinitionUUID();
    return evaluate(expression, context, processDefinitionUUID, activityUUID, processInstanceUUID, useActivityScope,
        false, propagate, null);
  }

  public static Object evaluate(final String expression, final Map<String, Object> context,
      final ProcessInstanceUUID instanceUUID, final boolean useInitialVariableValues, final boolean propagate)
      throws GroovyException {
    ProcessDefinitionUUID processDefinitionUUID = null;
    boolean archived = false;
    if (instanceUUID != null) {
      final ProcessInstance instance = EnvTool.getAllQueriers().getProcessInstance(instanceUUID);
      archived = instance.isArchived();
      processDefinitionUUID = instance.getProcessDefinitionUUID();
    }
    return evaluate(expression, context, processDefinitionUUID, null, instanceUUID, false, useInitialVariableValues,
        propagate && !archived, null);
  }

  public static Object evaluate(final String expression, final Map<String, Object> context,
      final ProcessDefinitionUUID processUUID, final boolean propagate) throws GroovyException {
    return evaluate(expression, context, processUUID, null, null, false, false, propagate, null);
  }

  private static Object evaluate(final String expression, final Map<String, Object> context,
      final ProcessDefinitionUUID processDefinitionUUID, final ActivityInstanceUUID activityUUID,
      final ProcessInstanceUUID instanceUUID, final boolean useActivityScope, final boolean useInitialVariableValues,
      final boolean propagate, final ClassLoader classLoader) throws GroovyException {

    if (expression == null || "".equals(expression.trim())) {
      final String message = getMessage(activityUUID, instanceUUID, processDefinitionUUID,
          "The expression is null or empty.");
      throw new GroovyException(message);
    } else {
      final int begin = expression.indexOf(START_DELIMITER);
      final int end = expression.indexOf(END_DELIMITER);
      if (begin >= end) {
        final String message = getMessage(activityUUID, instanceUUID, processDefinitionUUID,
            "The expression is not a Groovy one: " + expression + ".");
        throw new GroovyException(message);
      }
      final boolean oneUuidNotNull = processDefinitionUUID != null || instanceUUID != null || activityUUID != null;
      if (Misc.isJustAGroovyExpression(expression) && oneUuidNotNull) {
        final String insideExpression = expression.substring(begin + START_DELIMITER.length(), end).trim();
        if (Misc.isJavaIdentifier(insideExpression) && !"true".equals(insideExpression)
            && !"false".equals(insideExpression)) {
          try {
            final Object injectedVariable = GroovyBindingBuilder.getInjectedVariable(insideExpression,
                processDefinitionUUID, instanceUUID, activityUUID);
            if (injectedVariable != null) {
              return injectedVariable;
            }
            final Map<String, Object> allVariables = GroovyBindingBuilder.getContext(context, processDefinitionUUID,
                activityUUID, instanceUUID, useActivityScope, useInitialVariableValues);

            if (allVariables.containsKey(insideExpression)) {
              final Object result = allVariables.get(insideExpression);
              if (result instanceof ObjectVariable) {
                return ((ObjectVariable) result).getValue();
              }
              return result;
            }
          } catch (final Throwable t) {
          }
        }
      }
    }
    final ClassLoader ori = Thread.currentThread().getContextClassLoader();
    try {
      ProcessDefinitionUUID pUUID = processDefinitionUUID;
      boolean archived = false;
      ActivityInstance activity = null;
      if (pUUID == null && instanceUUID != null) {
        final ProcessInstance instance = EnvTool.getAllQueriers().getProcessInstance(instanceUUID);
        archived = instance.isArchived();
        pUUID = instance.getProcessDefinitionUUID();
      } else if (activityUUID != null) {
        activity = EnvTool.getJournalQueriers().getActivityInstance(activityUUID);
        if (activity == null) {
          archived = true;
          activity = EnvTool.getHistoryQueriers().getActivityInstance(activityUUID);
        }
        if (pUUID == null) {
          pUUID = activity.getProcessDefinitionUUID();
        }
      }

      if (pUUID != null && classLoader == null) {
        final ClassLoader processClassLoader = EnvTool.getClassDataLoader().getProcessClassLoader(pUUID);
        Thread.currentThread().setContextClassLoader(processClassLoader);
      } else if (classLoader != null) {
        Thread.currentThread().setContextClassLoader(classLoader);
      }
      final boolean propagateWhenInJournal = propagate && !archived;
      Binding binding = null;
      if (propagateWhenInJournal) {
        binding = GroovyBindingBuilder.getPropagateBinding(processDefinitionUUID, instanceUUID, activityUUID, context,
            useActivityScope, useInitialVariableValues);
      } else {
        binding = GroovyBindingBuilder.getSimpleBinding(processDefinitionUUID, instanceUUID, activityUUID, context,
            useActivityScope, useInitialVariableValues);
      }

      final Object result = evaluate(expression, binding);
      if (propagateWhenInJournal && instanceUUID != null) {
        propagateVariables(((PropagateBinding) binding).getVariablesToPropagate(), activityUUID, instanceUUID);
      }
      return result;
    } catch (final Exception e) {
      final String message = getMessage(activityUUID, instanceUUID, processDefinitionUUID,
          "Exception while evaluating expression.");
      throw new GroovyException(message, e);
    } finally {
      if (ori != null && ori != Thread.currentThread().getContextClassLoader()) {
        Thread.currentThread().setContextClassLoader(ori);
      }
    }
  }

  public static Object evaluate(final String expression, final Binding binding) throws GroovyException,
      NotSerializableException, ActivityDefNotFoundException, DataFieldNotFoundException, ProcessNotFoundException,
      IOException, ClassNotFoundException {
    String workingExpression = expression;
    Object result = null;
    if (Misc.isJustAGroovyExpression(workingExpression)) {
      workingExpression = workingExpression.substring(START_DELIMITER.length());
      workingExpression = workingExpression.substring(0, workingExpression.lastIndexOf(END_DELIMITER));
      if (Misc.isJavaIdentifier(workingExpression) && binding.getVariables().containsKey(workingExpression)) {
        result = binding.getVariable(workingExpression);
      } else {
        result = evaluateGroovyExpression(workingExpression, binding);
      }
    } else {
      result = evaluate(getExpressions(workingExpression), binding);
    }
    return result;
  }

  public static void propagateVariables(final Map<String, Object> variables, final ActivityInstanceUUID activityUUID,
      final ProcessInstanceUUID instanceUUID) throws GroovyException {
    final StandardAPIAccessorImpl accessor = new StandardAPIAccessorImpl();
    final RuntimeAPI runtime = accessor.getRuntimeAPI();
    if (variables != null) {
      for (final Entry<String, Object> variable : variables.entrySet()) {
        try {
          if (activityUUID != null) {
            runtime.setVariable(activityUUID, variable.getKey(), variable.getValue());
          } else {
            runtime.setProcessInstanceVariable(instanceUUID, variable.getKey(), variable.getValue());
          }
        } catch (final BonitaException e) {
          final String message = getMessage(activityUUID, instanceUUID, "Error while propagating variables.");
          throw new GroovyException(message, e);
        }
      }
    }
  }

  private static Object evaluateGroovyExpression(final String script, final Binding binding) throws GroovyException {
    final ClassLoader scriptClassLoader = Thread.currentThread().getContextClassLoader();
    final Script groovyScript = GroovyScriptBuilder.getScript(script, scriptClassLoader);
    groovyScript.setBinding(binding);
    Object result = null;
    try {
      result = groovyScript.run();
    } catch (final MissingPropertyException e) {
      final String lineSeparator = System.getProperty("line.separator", "\n");

      final StringBuilder stb = new StringBuilder();

      stb.append("Error in Groovy script: unable to use element \"" + e.getProperty() + "\"");
      stb.append(lineSeparator);
      stb.append(lineSeparator);
      stb.append("Possible cause:");
      stb.append(lineSeparator);
      stb.append("- missing import");
      stb.append(lineSeparator);
      stb.append("- variable not found (wrong name, undefined)");
      stb.append(lineSeparator);
      stb.append("- ...");
      stb.append(lineSeparator);
      stb.append(lineSeparator);
      stb.append("Script:");
      stb.append(lineSeparator);
      stb.append("\"" + script + "\"");
      stb.append(lineSeparator);

      throw new GroovyException(stb.toString(), e);
    }

    return result;
  }

  private static String evaluate(final List<String> expressions, final Binding binding) throws GroovyException {
    final StringBuilder builder = new StringBuilder();
    int i = 0;
    while (i < expressions.size()) {
      String expression = expressions.get(i);
      if (expression.equals(START_DELIMITER)) {
        expression = expressions.get(++i);
        builder.append(evaluateGroovyExpression(expression, binding));
      } else {
        builder.append(expression);
      }
      i++;
    }
    return builder.toString();
  }

  private static List<String> getExpressions(final String expression) {
    final List<String> expressions = new ArrayList<String>();
    String concat = expression;
    while (concat.contains(START_DELIMITER)) {
      final int index = concat.indexOf(START_DELIMITER);
      expressions.add(concat.substring(0, index));
      concat = concat.substring(index);
      expressions.add(START_DELIMITER);
      final int endGroovy = Misc.getGroovyExpressionEndIndex(concat);
      expressions.add(concat.substring(2, endGroovy - 1));
      concat = concat.substring(endGroovy);
    }
    expressions.add(concat);
    return expressions;
  }

  private static String getMessage(final ActivityInstanceUUID activityUUID, final ProcessInstanceUUID instanceUUID,
      final ProcessDefinitionUUID processDefUUID, final String message) {
    final String partialMessage = getMessage(activityUUID, instanceUUID, message);
    final StringBuilder stb = new StringBuilder(partialMessage);
    if (processDefUUID != null) {
      stb.append(" ProcessDefinitionUUID: '").append(processDefUUID).append("'.");
    }
    return stb.toString();
  }

  private static String getMessage(final ActivityInstanceUUID activityUUID, final ProcessInstanceUUID instanceUUID,
      final String message) {
    final StringBuilder stb = new StringBuilder(message);
    if (instanceUUID != null) {
      stb.append(" ProcessInstanceUUID: '").append(instanceUUID).append("'.");
    }
    if (activityUUID != null) {
      stb.append(" ActivityInstanceUUID: '").append(activityUUID).append("'.");
    }
    return stb.toString();
  }

}
