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
package org.ow2.bonita.util;

import java.util.ArrayList;
import java.util.List;

/**
 * listener that only delegates events to a given listener if they pass the
 * filter based on event names.
 */
public class FilterListener implements Listener {

  protected Listener listener;
  protected List<String> eventNames;

  public FilterListener(Listener listener, String eventName) {
    if (listener == null) {
    	String message = ExceptionManager.getInstance().getFullMessage("bp_FL_1");
    	throw new BonitaRuntimeException(message);
    }
    this.listener = listener;
    if (eventName == null) {
    	String message = ExceptionManager.getInstance().getFullMessage("bp_FL_2");
    	throw new BonitaRuntimeException(message);
    }
    this.eventNames = new ArrayList<String>();
    this.eventNames.add(eventName);
  }

  public FilterListener(Listener listener, List<String> eventNames) {
    if (listener == null) {
    	String message = ExceptionManager.getInstance().getFullMessage("bp_FL_3");
    	 throw new BonitaRuntimeException(message);
    }
    this.listener = listener;
    if (eventNames == null) {
    	String message = ExceptionManager.getInstance().getFullMessage("bp_FL_4");
    	throw new BonitaRuntimeException(message);
    }
    this.eventNames = eventNames;
  }

  public void event(Object source, String name, Object info) {
    if (!isFiltered(name)) {
      listener.event(source, name, info);
    }
  }

  public boolean isFiltered(String eventName) {
    if (eventNames.contains(eventName)) {
      return false;
    }
    return true;
  }

  public boolean equals(Object object) {
    if (object == null)
      return false;
    if (object == this)
      return true;
    if ((object instanceof Listener) && (listener.equals(object))) {
      return true;
    }
    return false;
  }

  public int hashCode() {
    return 17 + listener.hashCode();
  }
}