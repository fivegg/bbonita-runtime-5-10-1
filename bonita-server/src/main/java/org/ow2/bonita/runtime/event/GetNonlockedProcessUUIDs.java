/**
 * Copyright (C) 2012-2013 BonitaSoft S.A.
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

import java.util.List;
import java.util.Set;
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
public class GetNonlockedProcessUUIDs implements Command<List<String>> {

  private static final long serialVersionUID = 117724435875640884L;

  static final Logger LOG = Logger.getLogger(GetNonlockedProcessUUIDs.class.getName());

  private final Set<String> lockedProcessUUIDs;

  private final int maxResult;

  public GetNonlockedProcessUUIDs(final Set<String> lockedProcessUUIDs, final int maxResult) {
    this.lockedProcessUUIDs = lockedProcessUUIDs;
    this.maxResult = maxResult;
  }

  @Override
  public List<String> execute(final Environment environment) throws Exception {
    final EventService eventService = EnvTool.getEventService();
    return eventService.getNonLockedProcessesHavingJobs(lockedProcessUUIDs, maxResult);
  }

}
