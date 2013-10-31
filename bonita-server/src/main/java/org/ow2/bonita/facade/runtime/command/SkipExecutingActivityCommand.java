/**
 * Copyright (C) 2011  BonitaSoft S.A.
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
package org.ow2.bonita.facade.runtime.command;

import org.ow2.bonita.env.Environment;
import org.ow2.bonita.facade.exception.ActivityNotFoundException;
import org.ow2.bonita.facade.runtime.ActivityInstance;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.impl.InternalActivityInstance;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.runtime.ActivityManager;
import org.ow2.bonita.runtime.model.Execution;
import org.ow2.bonita.util.BonitaConstants;
import org.ow2.bonita.util.Command;
import org.ow2.bonita.util.EnvTool;

public class SkipExecutingActivityCommand implements Command<Boolean> {

  private static final long serialVersionUID = -2748779080782099313L;

  protected ActivityInstanceUUID myActivityUUID;

  public SkipExecutingActivityCommand(final ActivityInstanceUUID activityUUID) {
    myActivityUUID = activityUUID;
  }

  @Override
  public Boolean execute(final Environment env) throws Exception {
    final InternalActivityInstance activityInstance = EnvTool.getJournalQueriers().getActivityInstance(myActivityUUID);
    if (activityInstance == null) {
      throw new ActivityNotFoundException("R1", myActivityUUID);
    }
    if (activityInstance.getState() == ActivityState.EXECUTING) {
      // Set activityInstance state to READY
      activityInstance.setActivityState(ActivityState.READY, BonitaConstants.SYSTEM_USER);
      // Skip the activity Instance
      ActivityManager.skip(myActivityUUID, null);
      return true;
    }
    return false;
  }

  protected static Execution getExecution(final ActivityInstance activityInstance) throws ActivityNotFoundException {
    return EnvTool.getJournalQueriers().getExecutionOnActivity(activityInstance.getProcessInstanceUUID(),
        activityInstance.getUUID());
  }

}
