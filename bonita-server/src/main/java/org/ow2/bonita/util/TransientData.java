/**
 * Copyright (C) 2010-2012 BonitaSoft S.A.
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

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ow2.bonita.facade.def.InternalActivityDefinition;
import org.ow2.bonita.facade.exception.VariableNotFoundException;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.impl.InternalActivityInstance;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;

/**
 * @author Elias Ricken de Medeiros
 * 
 */
public final class TransientData {

  static final Logger LOG = Logger.getLogger(TransientData.class.getName());

  private static Map<ActivityInstanceUUID, Map<String, Object>> transientData;

  private TransientData() {
  }

  private static Map<ActivityInstanceUUID, Map<String, Object>> getTransientData() {
    if (transientData == null) {
      transientData = new HashMap<ActivityInstanceUUID, Map<String, Object>>();
    }
    return transientData;
  }

  public static void addTransientVariable(final ActivityInstanceUUID activityInstanceUUID, final String variableName,
      final Object variableValue) {
    Misc.checkArgsNotNull(activityInstanceUUID, variableName);
    final Map<ActivityInstanceUUID, Map<String, Object>> data = getTransientData();
    if (!data.containsKey(activityInstanceUUID)) {
      data.put(activityInstanceUUID, new HashMap<String, Object>());
    }
    data.get(activityInstanceUUID).put(variableName, variableValue);
  }

  public static void addTransientVariables(final ActivityInstanceUUID activityInstanceUUID,
      final Map<String, Object> variables) {
    Misc.checkArgsNotNull(activityInstanceUUID);
    if (variables != null) {
      final Map<ActivityInstanceUUID, Map<String, Object>> data = getTransientData();
      if (!data.containsKey(activityInstanceUUID)) {
        data.put(activityInstanceUUID, new HashMap<String, Object>());
      }
      data.get(activityInstanceUUID).putAll(variables);
    }
  }

  public static void removeTransientData(final ActivityInstanceUUID activityInstanceUUID) {
    Misc.checkArgsNotNull(activityInstanceUUID);
    getTransientData().remove(activityInstanceUUID);
  }

  public static Map<String, Object> getActivityTransientVariables(final ActivityInstanceUUID activityInstanceUUID) {
    Misc.checkArgsNotNull(activityInstanceUUID);
    ensureTransientVariablesCreated(activityInstanceUUID);
    Map<String, Object> variables = getTransientData().get(activityInstanceUUID);
    // to avoid original map by doing getActivityTransientVariables(uuid).add, remove
    if (variables != null) {
      variables = new HashMap<String, Object>(variables);
    } else {
      variables = new HashMap<String, Object>();
    }
    return variables;
  }

  public static Object getActivityTransientVariableValue(final ActivityInstanceUUID activityInstanceUUID,
      final String variableName) throws VariableNotFoundException {
    Misc.checkArgsNotNull(activityInstanceUUID, variableName);
    ensureTransientVariablesCreated(activityInstanceUUID);
    final Map<String, Object> variables = getActivityTransientVariables(activityInstanceUUID);
    if (variables == null || !variables.containsKey(variableName)) {
      throw new VariableNotFoundException("bai_QRAPII_12", activityInstanceUUID, variableName);
    }

    return variables.get(variableName);
  }

  public static void updateActivityTransientVariableValue(final ActivityInstanceUUID activityInstanceUUID,
      final String variableName, final Object value) throws VariableNotFoundException {
    Misc.checkArgsNotNull(activityInstanceUUID, variableName);
    ensureTransientVariablesCreated(activityInstanceUUID);
    final Map<String, Object> variables = getTransientData().get(activityInstanceUUID);
    if (variables == null || !variables.containsKey(variableName)) {
      throw new VariableNotFoundException("bai_QRAPII_12", activityInstanceUUID, variableName);
    }
    variables.put(variableName, value);
  }

  private static void ensureTransientVariablesCreated(final ActivityInstanceUUID activityInstanceUUID) {
    final Map<ActivityInstanceUUID, Map<String, Object>> data = getTransientData();
    if (!data.containsKey(activityInstanceUUID)) {
      restoreTransientVariablesIfNecessary(activityInstanceUUID);
    }
  }

  private static void restoreTransientVariablesIfNecessary(final ActivityInstanceUUID activityInstanceUUID) {
    final InternalActivityInstance activityInstance = EnvTool.getJournalQueriers().getActivityInstance(
        activityInstanceUUID);
    if (activityInstance != null && needRestore(activityInstance.getState())) {
      final InternalActivityDefinition activityDefinition = EnvTool.getJournalQueriers().getActivity(
          activityInstance.getActivityDefinitionUUID());
      final Map<String, Object> transientVariables = VariableUtil.createTransientVariables(
          activityDefinition.getDataFields(), activityInstance.getProcessInstanceUUID());
      if (transientVariables != null && !transientVariables.isEmpty()) {
        addTransientVariables(activityInstanceUUID, transientVariables);
        if (LOG.isLoggable(Level.WARNING)) {
          final StringBuilder stb = new StringBuilder("Restoring transient varaibles for activity ");
          stb.append(activityInstanceUUID);
          stb.append(": the system probably was stopped during the variable life cycle. All transient variables for this activity will be restored to their default value.");
          LOG.warning(stb.toString());
        }
      }
    }
  }

  private static boolean needRestore(final ActivityState state) {
    return ActivityState.EXECUTING.equals(state) || ActivityState.READY.equals(state)
        || ActivityState.SUSPENDED.equals(state);
  }
}
