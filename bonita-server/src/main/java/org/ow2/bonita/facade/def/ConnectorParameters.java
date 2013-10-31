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
package org.ow2.bonita.facade.def;

import java.util.ArrayList;
import java.util.List;

import org.ow2.bonita.type.Variable;

public class ConnectorParameters {

  protected String parameterName;
  protected List<Variable> variables;

  protected ConnectorParameters() { }
  
  public ConnectorParameters(final String parameterName, final List<Variable> variables) {
    super();
    this.parameterName = parameterName;
    this.variables = variables;
  }

  public String getParameterName() {
    return parameterName;
  }

  public List<Variable> getVariables() {
    return variables;
  }
  
  public Object[] getVariableValues() {
    List<Object> result = new ArrayList<Object>();
    if (variables == null) {
      return result.toArray();
    }
    for (Variable variable : variables) {
      result.add(variable.getValue());
    }
    return result.toArray();
  }
  
}
