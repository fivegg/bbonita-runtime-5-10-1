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
 * 
 * Modified by Matthieu Chaffotte - BonitaSoft S.A.
 **/
package org.ow2.bonita.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.ow2.bonita.facade.def.majorElement.DataFieldDefinition;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.type.Converter;
import org.ow2.bonita.type.Type;
import org.ow2.bonita.type.Variable;
import org.ow2.bonita.type.VariableTypeResolver;
import org.ow2.bonita.type.variable.NullVariable;
import org.w3c.dom.Document;

/**
 * @author Pierre Vigneras
 */
public abstract class VariableUtil {

  public static final String DEPLOYMENT_IDPREFIX = "deployment$";

  protected VariableUtil() {
  }

  public static Map<String, Variable> createVariableMap(final ProcessDefinitionUUID processUUID,
      final Map<String, Object> variables) {
    if (variables == null || variables.isEmpty()) {
      return null;
    }
    final Map<String, Variable> variableMap = new HashMap<String, Variable>();
    for (final Map.Entry<String, Object> e : variables.entrySet()) {
      final Object value = e.getValue();
      variableMap.put(e.getKey(), createVariable(processUUID, e.getKey(), value));
    }
    return variableMap;
  }

  public static Variable createVariable(final ProcessDefinitionUUID processUUID, final String key, final Object value) {
    Type type = null;
    final VariableTypeResolver variableTypeResolver = EnvTool.getVariableTypeResolver();
    if (variableTypeResolver != null) {
      type = variableTypeResolver.findTypeByMatch(key, value);
    }
    Variable variable = null;

    if (type != null) {
      final Class<?> variableClass = type.getVariableClass();
      try {
        variable = (Variable) variableClass.newInstance();
        variable.setProcessUUID(processUUID);
      } catch (final Exception e) {
        final String message = ExceptionManager.getInstance().getFullMessage("bp_EI_14", variableClass.getName());
        throw new BonitaRuntimeException(message);
      }
      final Converter converter = type.getConverter();
      variable.setConverter(converter);

    } else {
      if (value == null) {
        variable = new NullVariable();
      } else {
        throw new BonitaRuntimeException("The value of variable '" + key + "' of process " + processUUID
            + " is not Serializable");
      }
    }

    variable.setKey(key);
    variable.setValue(value);

    return variable;
  }

  public static Map<String, Variable> copyVariableMap(final ProcessDefinitionUUID processUUID,
      final Map<String, Variable> variables) {
    if (variables == null || variables.isEmpty()) {
      return null;
    }
    final Map<String, Variable> variableMap = new HashMap<String, Variable>();
    for (final Map.Entry<String, Variable> e : variables.entrySet()) {
      final Variable var = copyVariable(e.getValue());
      variableMap.put(var.getKey(), var);
    }
    return variableMap;
  }

  public static Variable copyVariable(final Variable var) {
    final Object value = var.getValue();
    final String key = var.getKey();
    return createVariable(var.getProcessUUID(), key, value);
  }

  public static Map<String, Variable> createVariables(final Collection<DataFieldDefinition> datafields,
      final ProcessInstanceUUID instanceUUID, final Map<String, Object> context) {
    if (datafields == null || datafields.isEmpty()) {
      return null;
    }
    final Map<String, Variable> variables = new HashMap<String, Variable>();
    for (final DataFieldDefinition df : datafields) {
      if (!df.isTransient()) {
        Object value = df.getInitialValue();
        value = VariableUtil.evaluateInitialValue(instanceUUID, df, value, context);
        variables.put(df.getName(), VariableUtil.createVariable(df.getProcessDefinitionUUID(), df.getName(), value));
      }
    }
    return variables;
  }

  private static Object evaluateInitialValue(final ProcessInstanceUUID instanceUUID, final DataFieldDefinition df,
      Object value, final Map<String, Object> context) {
    if (value == null) {
      final String script = df.getScriptingValue();
      if (script != null) {
        try {
          if (instanceUUID != null) {
            value = GroovyUtil.evaluate(script, context, instanceUUID, false, false);
          } else {
            value = GroovyUtil.evaluate(script, context, df.getProcessDefinitionUUID(), false);
          }
          if (df.getDataTypeClassName().equals(Document.class.getName()) && value instanceof String) {
            value = Misc.generateDocument((String) value);
          }
        } catch (final Exception e) {
          try {
            value = GroovyUtil.evaluate(script, context);
          } catch (final GroovyException e1) {
            final StringBuilder stb = new StringBuilder("Error while initializing variable: '");
            stb.append(df.getName());
            stb.append("'. ");
            throw new BonitaRuntimeException(stb.toString(), e);
          }
        }
      }
    }
    return value;
  }

  public static Map<String, Object> createTransientVariables(final Collection<DataFieldDefinition> datafields,
      final ProcessInstanceUUID instanceUUID) {
    if (datafields == null || datafields.isEmpty()) {
      return null;
    }
    final Map<String, Object> variables = new HashMap<String, Object>();
    for (final DataFieldDefinition df : datafields) {
      if (df.isTransient()) {
        Object value = df.getInitialValue();
        value = evaluateInitialValue(instanceUUID, df, value, null);
        variables.put(df.getName(), value);
      }
    }
    return variables;
  }

  public static Map<String, Object> getVariableValues(final Map<String, Variable> variableMap) {
    final Map<String, Object> res = new HashMap<String, Object>();
    if (variableMap != null) {
      for (final Variable var : variableMap.values()) {
        res.put(var.getKey(), var.getValue());
      }
    }
    return res;
  }

}
