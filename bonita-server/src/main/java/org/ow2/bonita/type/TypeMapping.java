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
package org.ow2.bonita.type;

import java.io.Serializable;

public class TypeMapping implements Serializable {

  Matcher matcher;
  Type type;

  private static final long serialVersionUID = 1L;

  public boolean matches(String name, Object value) {
    return matcher.matches(name, value);
  }

  public String toString() {
    return "(" + matcher + "-->" + type + ")";
  }

  public void setMatcher(Matcher matcher) {
    this.matcher = matcher;
  }

  public Type getType() {
    return type;
  }

  public void setType(Type type) {
    this.type = type;
  }

  public Matcher getMatcher() {
    return matcher;
  }
}
