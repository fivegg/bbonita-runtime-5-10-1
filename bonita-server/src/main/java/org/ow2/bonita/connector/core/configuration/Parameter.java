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

/**
 * 
 * @author Matthieu Chaffotte
 *
 */
public class Parameter {

  private String name;
  private String value;
  private String typeClassName;

  public Parameter(String name, String value, String typeClassName) {
    if (name == null || name.length() == 0) {
      throw new IllegalArgumentException("The name cannot be null or empty");
    } else if (value == null) {
      throw new IllegalArgumentException("The value caoont be null");
    } else if (typeClassName == null) {
      throw new IllegalArgumentException("The typeClassName cannot be null");
    }
    this.name = name;
    this.value = value;
    this.typeClassName = typeClassName;
  }

  public String getName() {
    return name;
  }

  public String getValue() {
    return value;
  }

  public String getTypeClassName() {
    return typeClassName;
  }

  public void setValue(String value) {
    this.value = value;
  }

  @Override
  public boolean equals(Object o) {
    boolean equals = false;
    if (o instanceof Parameter) {
      Parameter temp = (Parameter) o;
      if (temp.getName().equals(this.getName())
          && temp.getValue().equals(this.getValue())
          && temp.typeClassName.equals(this.typeClassName)) {
        equals = true;
      }
    }
    return equals;
  }

  @Override
  public int hashCode() {
    StringBuilder hash = new StringBuilder(name);
    hash.append(value).append(typeClassName);
    return hash.toString().hashCode();
  }

}
