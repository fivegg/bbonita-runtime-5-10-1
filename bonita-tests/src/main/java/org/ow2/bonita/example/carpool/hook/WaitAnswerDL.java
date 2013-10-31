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
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.TaskInstance;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.util.BonitaRuntimeException;
import org.ow2.bonita.util.ExceptionManager;

public class WaitAnswerDL implements TxHook {

  private static final Logger LOG = Logger.getLogger(WaitAnswerDL.class.getName());

  public void execute(final APIAccessor apiAccessor, final ActivityInstance activity) throws Exception {
    if (LOG.isLoggable(Level.INFO)) {
      LOG.info("\n***** Executing Hook: " + this.getClass().getName() + " *****\n");
    }

    final String activityId = activity.getActivityName();
    TaskInstance t = activity.getTask();
    final ActivityInstanceUUID taskUUID = t.getUUID();
    final RuntimeAPI runtimeAPI = apiAccessor.getRuntimeAPI();

    if (LOG.isLoggable(Level.INFO)) {
      LOG.info("\n***** Starting task associated to activity: " + activityId + " *****\n");
    }
    runtimeAPI.startTask(taskUUID, true);

    final TaskInstance activity2 = apiAccessor.getQueryRuntimeAPI().getTask(taskUUID);
    if (activity2 == null) {
    	String message = ExceptionManager.getInstance().getFullMessage("bex_WADL_1");
      throw new BonitaRuntimeException(message);
    }
    t = activity2.getTask();
    if (!ActivityState.EXECUTING.equals(t.getState())) {
    	String message = ExceptionManager.getInstance().getFullMessage(
    			"bex_WADL_2", ActivityState.EXECUTING, t.getState());
      throw new BonitaRuntimeException(message);
    }

    if (LOG.isLoggable(Level.INFO)) {
      LOG.info("\n***** Finishing task associated to activity: " + activityId + " *****\n");
    }
    runtimeAPI.finishTask(taskUUID, true);
  }

}
