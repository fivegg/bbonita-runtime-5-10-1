/**
 * Copyright (C) 2010-2012 BonitaSoft S.A.
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

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ow2.bonita.env.Environment;
import org.ow2.bonita.services.EventService;
import org.ow2.bonita.util.Command;
import org.ow2.bonita.util.EnvTool;

/**
 * 
 * @author Matthieu Chaffotte
 * 
 */
public class GetNextJobDueDate implements Command<Long> {

  private static final long serialVersionUID = 1L;

  private static final Logger LOG = Logger.getLogger(GetNextJobDueDate.class.getName());

  private final Set<String> processUUIDsToExclude;
  
  public GetNextJobDueDate(final Set<String> processUUIDsToExclude) {
	super();
	this.processUUIDsToExclude = processUUIDsToExclude;
  }

@Override
  public Long execute(final Environment environment) throws Exception {
    Long nextDueDate = null;
    if (LOG.isLoggable(Level.FINE)) {
      LOG.fine("Getting next due date...");
    }
    final EventService eventService = EnvTool.getEventService();
    nextDueDate = eventService.getNextJobDueDate(this.processUUIDsToExclude);
    if (LOG.isLoggable(Level.FINE)) {
      LOG.fine("Next due date is " + nextDueDate);
    }
    return nextDueDate;
  }

}
