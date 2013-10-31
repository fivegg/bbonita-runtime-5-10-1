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
 **/
package org.ow2.bonita.example.carpool.hook;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.ow2.bonita.definition.TxHook;
import org.ow2.bonita.facade.APIAccessor;
import org.ow2.bonita.facade.RuntimeAPI;
import org.ow2.bonita.facade.runtime.ActivityInstance;
import org.ow2.bonita.facade.runtime.TaskInstance;

public class CancelRequestDL implements TxHook {

  private static final Logger LOG = Logger.getLogger(CancelRequestDL.class.getName());

  public void execute(final APIAccessor apiAccessor, final ActivityInstance activity) throws Exception {
    if (LOG.isLoggable(Level.INFO)) {
      LOG.info("\n***** Executing Hook: " + this.getClass().getName() + " *****\n");
    }
    final TaskInstance t = activity.getTask();
    final RuntimeAPI runtimeAPI = apiAccessor.getRuntimeAPI();
    if (LOG.isLoggable(Level.INFO)) {
      LOG.info("\n***** Starting task associated to activity: " + activity.getActivityName() + " *****\n");
    }
    runtimeAPI.startTask(t.getUUID(), true);
    if (LOG.isLoggable(Level.INFO)) {
      LOG.info("\n***** Finishing task associated to activity: " + activity.getActivityName() + " *****\n");
    }
    runtimeAPI.finishTask(t.getUUID(), true);
  }

}
