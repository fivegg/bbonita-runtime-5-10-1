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
 * 
 * Modified by Matthieu Chaffotte - BonitaSoft S.A.
 **/
package org.ow2.bonita.connector.core.desc;

import java.util.Map;

/**
 * 
 * @author Matthieu Chaffotte
 * 
 */
public class Select extends Widget {

  private final Map<String, String> values;
  private final boolean editable;
  private final Option top;

  public Select(final String labelId, final Setter setter, final Map<String, String> values, final boolean editable,
      final Option top) {
    super(labelId, setter);
    this.values = values;
    this.editable = editable;
    this.top = top;
  }

  public Map<String, String> getValues() {
    return values;
  }

  public boolean isEditable() {
    return editable;
  }

  public Option getTop() {
    return top;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + (editable ? 1231 : 1237);
    result = prime * result + (top == null ? 0 : top.hashCode());
    result = prime * result + (values == null ? 0 : values.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Select other = (Select) obj;
    if (editable != other.editable) {
      return false;
    }
    if (top == null) {
      if (other.top != null) {
        return false;
      }
    } else if (!top.equals(other.top)) {
      return false;
    }
    if (values == null) {
      if (other.values != null) {
        return false;
      }
    } else if (!values.equals(other.values)) {
      return false;
    }
    return true;
  }

}
