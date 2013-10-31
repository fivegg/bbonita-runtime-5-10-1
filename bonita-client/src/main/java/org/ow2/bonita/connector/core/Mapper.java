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
package org.ow2.bonita.connector.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @author Matthieu Chaffotte
 *
 */
public abstract class Mapper extends ProcessConnector {

  private Set<String> members = new HashSet<String>();

  public Set<String> getMembers() {
    return members;
  }

  public void setMembers(Set<String> members) {
    this.members = members;
  }

  public String get(Set<String> set, int number) {
    if (set == null) {
      throw new IllegalArgumentException("The set cannot be null");
    } else if (number < 0) {
      throw new IllegalArgumentException("The number cannot be negative");
    } else if (set.size() < number) {
      throw new IllegalArgumentException("The number is greater than the set size");
    }
    Iterator<String> iterator = set.iterator();
    for (int i = 0; i < number; i++) {
      iterator.next();
    }
    return iterator.next();
  }

  @Override
  protected List<ConnectorError> validateValues() {
    return new ArrayList<ConnectorError>();
  }
}
