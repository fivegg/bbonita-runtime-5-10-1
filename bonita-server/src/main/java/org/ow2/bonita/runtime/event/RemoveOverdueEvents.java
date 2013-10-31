/**
 * Copyright (C) 2009-2012 BonitaSoft S.A.
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
package org.ow2.bonita.runtime.event;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.ow2.bonita.env.Environment;
import org.ow2.bonita.services.EventService;
import org.ow2.bonita.util.Command;
import org.ow2.bonita.util.EnvTool;

/**
 * 
 * @author Charles Souillard, Matthieu Chaffotte
 *
 */
public class RemoveOverdueEvents implements Command<Void> {
  
  private static final long serialVersionUID = -479276850307735480L;
  private static final Logger LOG = Logger.getLogger(RemoveOverdueEvents.class.getName());
  
  public Void execute(Environment environment) throws Exception {
    final EventService eventService = EnvTool.getEventService();
    for (EventInstance event : eventService.getOverdueEvents()) {
      if (LOG.isLoggable(Level.FINE)) {
        LOG.fine("Removing overdue event:" + event);
      }
      eventService.removeEvent(event);
    }
    return null;
  }

}
