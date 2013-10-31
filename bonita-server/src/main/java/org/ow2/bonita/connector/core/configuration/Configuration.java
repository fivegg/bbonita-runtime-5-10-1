/**
 * Copyright (C) 2009  Bull S. A. S.
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
package org.ow2.bonita.connector.core.configuration;

import java.util.ArrayList;
import java.util.List;

import org.ow2.bonita.connector.core.ConnectorDescription;

/**
 * 
 * @author Matthieu Chaffotte
 * 
 */
public class Configuration {

  private String id;
  private final List<Parameter> parameters;
  private final Configuration parent;
  private final List<Configuration> children;
  private final ConnectorDescription connector;

  public Configuration(final String name, final ConnectorDescription connector) {
    checkConfigurationName(name);
    if (connector == null) {
      throw new IllegalArgumentException("The connector cannot be null!");
    }
    id = name;
    parent = null;
    parameters = new ArrayList<Parameter>();
    children = new ArrayList<Configuration>();
    this.connector = connector;
  }

  public Configuration(final String name, final Configuration c) {
    if (c == null) {
      throw new IllegalArgumentException("The configuration cannot be null");
    }
    checkConfigurationName(name);
    if (exists(name, c.getRoot())) {
      throw new IllegalArgumentException("The configuration name has already been taken");
    }
    id = name;
    parameters = new ArrayList<Parameter>();
    parent = c;
    children = new ArrayList<Configuration>();
    c.children.add(this);
    connector = parent.connector;
  }

  public void checkConfigurationName(final String name) {
    if (name == null) {
      throw new IllegalArgumentException("The configutration name cannot be null");
    } else if (name.length() == 0) {
      throw new IllegalArgumentException("The configutration name cannot be empty");
    }
  }

  private boolean exists(final String name, final Configuration c) {
    if (name.equals(c.getId())) {
      return true;
    }
    for (final Configuration child : c.children) {
      if (exists(name, child)) {
        return true;
      }
    }
    return false;
  }

  public String getId() {
    return id;
  }

  public void setId(final String id) {
    if (exists(id, this.getRoot())) {
      throw new IllegalArgumentException("The configuration name has already been taken");
    }
    this.id = id;
  }

  public void addParameter(final Parameter element) {
    if (element == null) {
      throw new IllegalArgumentException("The parameter cannot be null!");
    }
    final int index = containsParameter(element);
    if (index == -1) {
      parameters.add(element);
    } else {
      parameters.set(index, element);
    }
  }

  private int containsParameter(final Parameter element) {
    return containsParameter(element, parameters);
  }

  private int containsParameter(final Parameter element, final List<Parameter> list) {
    final int size = list.size();
    int index = -1;
    for (int i = 0; i < size; i++) {
      final Parameter temp = list.get(i);
      if (temp.getName().equals(element.getName())) {
        index = i;
        break;
      }
    }
    return index;
  }

  public void removeParameter(final Parameter element) {
    final int index = containsParameter(element);
    if (index > -1) {
      parameters.remove(index);
    }
  }

  public List<Parameter> getLocalParameters() {
    return parameters;
  }

  public Parameter getParameter(final String parameterName) {
    return getParameter(this, parameterName);
  }

  public Parameter getParameter(final Configuration c, final String parameterName) {
    Parameter param = null;
    for (final Parameter parameter : c.parameters) {
      if (parameterName.equals(parameter.getName())) {
        param = parameter;
        break;
      }
    }
    if (param == null && c.hasParent()) {
      param = getParameter(c.getParent(), parameterName);
    }
    return param;
  }

  public List<Parameter> getParameters() {
    return getParameters(this, new ArrayList<Parameter>());
  }

  private List<Parameter> getParameters(final Configuration c, List<Parameter> list) {
    final List<Parameter> parentParams = c.parameters;
    for (final Parameter temp : parentParams) {
      if (containsParameter(temp, list) == -1) {
        list.add(temp);
      }
    }
    if (c.hasParent()) {
      list = getParameters(c.getParent(), list);
    }
    return list;
  }

  private boolean hasParent() {
    boolean has = true;
    if (getParent() == null) {
      has = false;
    }
    return has;
  }

  public Configuration getParent() {
    return parent;
  }

  private Configuration getRoot() {
    return getRoot(this);
  }

  private Configuration getRoot(final Configuration c) {
    if (c.hasParent()) {
      return getRoot(c.getParent());
    } else {
      return c;
    }
  }

  public ConnectorDescription getConnectorDescription() {
    return connector;
  }

  @Override
  public boolean equals(final Object object) {
    boolean equals = false;
    if (object instanceof Configuration) {
      final Configuration temp = (Configuration) object;
      if (temp.getId().equals(getId())) {
        equals = true;
      }
    }
    return equals;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (id == null ? 0 : id.hashCode());
    return result;
  }

}
