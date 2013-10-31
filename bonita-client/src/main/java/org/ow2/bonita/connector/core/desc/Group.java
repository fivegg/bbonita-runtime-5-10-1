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

import java.util.List;

/**
 * @author Matthieu Chaffotte
 * 
 */
public class Group extends Component {

  private boolean optional;
  private List<WidgetComponent> widgets;

  // used by XStream
  protected Group() {
    super();
  }

  public Group(final String labelId, final boolean optional, final List<WidgetComponent> widgets) {
    super(labelId);
    this.optional = optional;
    this.widgets = widgets;
  }

  public boolean isOptional() {
    return optional;
  }

  public List<WidgetComponent> getWidgets() {
    return widgets;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + (optional ? 1231 : 1237);
    result = prime * result + (widgets == null ? 0 : widgets.hashCode());
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
    final Group other = (Group) obj;
    if (optional != other.optional) {
      return false;
    }
    if (widgets == null) {
      if (other.widgets != null) {
        return false;
      }
    } else if (!widgets.equals(other.widgets)) {
      return false;
    }
    return true;
  }

}
