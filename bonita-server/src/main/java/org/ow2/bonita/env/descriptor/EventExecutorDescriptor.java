/**
 * Copyright (C) 2007  Bull S. A. S.
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
package org.ow2.bonita.env.descriptor;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.ow2.bonita.env.WireContext;
import org.ow2.bonita.runtime.event.EventExecutor;
import org.ow2.bonita.util.BonitaRuntimeException;
import org.ow2.bonita.util.ExceptionManager;
import org.ow2.bonita.util.Listener;

public class EventExecutorDescriptor extends ObjectDescriptor {
	private static final long serialVersionUID = 1L;
  private static final Logger LOG = Logger.getLogger(EventExecutorDescriptor.class.getName());

  private boolean autoStart = false;

  public EventExecutorDescriptor() {
    super(EventExecutor.class.getName());
  }

  public Object construct(WireContext wireContext) {
  	EventExecutor eventExecutor = (EventExecutor) super.construct(wireContext);
    if (autoStart) {
      wireContext.addListener(new EventExecutorStopper(eventExecutor));
    }
    return eventExecutor;
  }

  public void setAutoStart(boolean autoStart) {
    this.autoStart = autoStart;
  }

  public static class EventExecutorStopper implements Listener {
  	EventExecutor eventExecutor;

    public EventExecutorStopper(EventExecutor eventExecutor) {
      this.eventExecutor = eventExecutor;
    }

    public void event(Object source, String eventName, Object info) {
      if (WireContext.EVENT_CLOSE.equals(eventName)) {
      	if (LOG.isLoggable(Level.INFO)) {
          LOG.info("stopping EventExecutor");
      	}
        // wait to prevent from calling stop before the run method (activation)
        // has been called (after a system context switching)
        // TODO: do not wait
        try {
          Thread.sleep(200);
        } catch (InterruptedException e) {
          e.printStackTrace();
          String message = ExceptionManager.getInstance().getFullMessage("bp_JED_1");
          throw new BonitaRuntimeException(message);
        }
        eventExecutor.stop(true);
      }
    }
  }

}
