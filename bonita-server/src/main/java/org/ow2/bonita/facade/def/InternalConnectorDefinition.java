/**
 * Copyright (C) 2009-2013 BonitaSoft S.A.
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
package org.ow2.bonita.facade.def;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ow2.bonita.facade.def.element.ConnectorDefinition;
import org.ow2.bonita.facade.def.element.impl.ConnectorDefinitionImpl;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.type.Variable;
import org.ow2.bonita.util.VariableUtil;

/**
 * 
 * @author Matthieu Chaffotte
 * 
 */
public class InternalConnectorDefinition extends ConnectorDefinitionImpl {

  protected long dbid;

  private static final long serialVersionUID = 1L;

  protected Map<String, ConnectorParameters> variableParameters;

  protected InternalConnectorDefinition() {
    super();
  }

  public InternalConnectorDefinition(final ConnectorDefinition src, final ProcessDefinitionUUID processUUID) {
    super(src);
    for (final Map.Entry<String, Object[]> entries : src.getParameters().entrySet()) {
      final String key = entries.getKey();
      final Object[] parameters = entries.getValue();
      final List<Variable> variables = new ArrayList<Variable>();
      for (final Object parameter : parameters) {
        variables.add(VariableUtil.createVariable(processUUID, key, parameter));
      }
      addParameter(key, new ConnectorParameters(key, variables));
    }
    clientParameters = null;
  }

  private void addParameter(final String key, final ConnectorParameters value) {
    if (variableParameters == null) {
      variableParameters = new HashMap<String, ConnectorParameters>();
    }
    variableParameters.put(key, value);
  }

  private Map<String, Object[]> getVariableParameters() {
    if (variableParameters == null) {
      return null;
    }
    final Map<String, Object[]> result = new HashMap<String, Object[]>();
    for (final Map.Entry<String, ConnectorParameters> entry : variableParameters.entrySet()) {
      result.put(entry.getKey(), entry.getValue().getVariableValues());
    }
    return result;
  }

  @Override
  public Map<String, Object[]> getParameters() {
    if (getVariableParameters() != null) {
      return getVariableParameters();
    }
    return Collections.emptyMap();
  }

  public long getDbid() {
    return dbid;
  }

}
