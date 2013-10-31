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
package org.ow2.bonita.connector.core.desc;

import java.util.Arrays;

/**
 * 
 * @author Matthieu Chaffotte
 * 
 */
public class Setter {

  private final String setterName;
  private final String required;
  private final String forbidden;
  private final Object[] parameters;

  public Setter(final String setterName, final String required, final String forbidden, final Object[] hostParameter) {
    super();
    this.setterName = setterName;
    this.required = required;
    this.forbidden = forbidden;
    parameters = hostParameter;
  }

  public String getSetterName() {
    return setterName;
  }

  public String getRequired() {
    return required;
  }

  public String getForbidden() {
    return forbidden;
  }

  public Object[] getParameters() {
    return parameters;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (forbidden == null ? 0 : forbidden.hashCode());
    result = prime * result + Arrays.hashCode(parameters);
    result = prime * result + (required == null ? 0 : required.hashCode());
    result = prime * result + (setterName == null ? 0 : setterName.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Setter other = (Setter) obj;
    if (forbidden == null) {
      if (other.forbidden != null) {
        return false;
      }
    } else if (!forbidden.equals(other.forbidden)) {
      return false;
    }
    if (!Arrays.equals(parameters, other.parameters)) {
      return false;
    }
    if (required == null) {
      if (other.required != null) {
        return false;
      }
    } else if (!required.equals(other.required)) {
      return false;
    }
    if (setterName == null) {
      if (other.setterName != null) {
        return false;
      }
    } else if (!setterName.equals(other.setterName)) {
      return false;
    }
    return true;
  }

}
