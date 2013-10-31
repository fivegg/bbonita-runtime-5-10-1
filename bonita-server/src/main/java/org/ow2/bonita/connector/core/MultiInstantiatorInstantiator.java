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
package org.ow2.bonita.connector.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author Matthieu Chaffotte
 *
 */
public class MultiInstantiatorInstantiator extends MultipleInstancesInstantiator {

  private MultiInstantiator instantiator;
  private Map<String, Object[]> instantiatorParameters;
  private String className;
  private String variableName;

  public Map<String, Object[]> getInstantiatorParameters() {
    return instantiatorParameters;
  }

  public void setInstantiator(MultiInstantiator instantiator) {
    this.instantiator = instantiator;
  }

  public void setInstantiatorParameters(Map<String, Object[]> instantiatorParameters) {
    this.instantiatorParameters = instantiatorParameters;
  }

  public void setInstantiatorParameters(List<List<Object>> instantiatorParameters) {
    this.instantiatorParameters = bonitaListToArrayMap(instantiatorParameters, String.class, Object.class);
  }
  
  public void setClassName(String className) {
    this.className = className;
  }

  public String getClassName() {
    return this.className;
  }

  public void setVariableName(String variableName) {
    this.variableName = variableName;
  }

  @Override
  protected List<Map<String, Object>> defineActivitiesContext() throws Exception {
    List<Map<String, Object>> contexts = new ArrayList<Map<String,Object>>();
    instantiator.execute();
    List<Object> values = instantiator.getVariableValues();
    for (Object value : values) {
      Map<String, Object> context = new HashMap<String, Object>();
      context.put(variableName, value);
      contexts.add(context);
    }
    return contexts;
  }

}
