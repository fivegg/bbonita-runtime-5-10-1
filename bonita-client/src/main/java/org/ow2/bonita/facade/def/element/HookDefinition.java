/**
 * Copyright (C) 2006  Bull S. A. S.
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
package org.ow2.bonita.facade.def.element;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Hook definition within Activity or ProcessInstance element.
 */
public interface HookDefinition extends ConnectorDefinition {

  /**
   * A Hook Event is used to define when a connector will be executed.
   */
  public static enum Event {
    taskOnReady, taskOnStart, taskOnFinish, taskOnSuspend, taskOnResume, taskOnCancel,
    automaticOnEnter, automaticOnExit, onTimer,
    instanceOnStart, instanceOnFinish, instanceOnCancel, instanceOnAbort, onEvent;

    /**
     * This list contains all the task (human activity) events. 
     */
    public static final List<Event> TASK_EVENTS;
    static {
      final List<Event> tmp = new ArrayList<Event>();
      tmp.add(taskOnReady);
      tmp.add(taskOnStart);
      tmp.add(taskOnFinish);
      tmp.add(taskOnSuspend);
      tmp.add(taskOnResume);
      tmp.add(taskOnCancel);
      TASK_EVENTS = Collections.unmodifiableList(tmp);
    }

    /**
     * This list contains all the process events.
     */
    public static final List<Event> PROCESS_EVENTS;
    static {
      final List<Event> tmp = new ArrayList<Event>();
      tmp.add(instanceOnStart);
      tmp.add(instanceOnFinish);
      tmp.add(instanceOnCancel);
      tmp.add(instanceOnAbort);
      PROCESS_EVENTS = Collections.unmodifiableList(tmp);
    }

    /**
     * Formats the event name i.e. taskOnReady becomes task:onReady, 
     * automaticOnEnter becomes automatic:onEnter, ...
     * @return the formatted event name
     */
    public String format() {
      final StringBuilder result = new StringBuilder(super.toString());
      int i = 0;
      char c = '$';
      while (i < result.length()) {
        c = result.charAt(i);
        if (Character.isUpperCase(c)) {
          break;
        }
        i++;
      }
      if (i < result.length()) {
        result.insert(i, ':');
        result.setCharAt(i + 1, Character.toLowerCase(c));
      }
      return result.toString();
    }
  }

  /**
   * Gets the Hook event.
   * @return the Hook event
   */
  Event getEvent();
}
