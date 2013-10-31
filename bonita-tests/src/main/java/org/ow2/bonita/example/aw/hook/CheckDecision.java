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
package org.ow2.bonita.example.aw.hook;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ow2.bonita.definition.TxHook;
import org.ow2.bonita.facade.APIAccessor;
import org.ow2.bonita.facade.runtime.ActivityInstance;

/**
 * @author "Pierre Vigneras"
 */
public class CheckDecision implements TxHook {

  private static final Logger LOG = Logger.getLogger(CheckDecision.class.getName());

  public void execute(final APIAccessor accessor, final ActivityInstance activityInstance) throws Exception {
    Set<ActivityInstance> activities = accessor.getQueryRuntimeAPI().getActivityInstances(activityInstance.getProcessInstanceUUID(), "Approval");

    int totalNumber = activities.size();
    int approved = 0;
    for (ActivityInstance act : activities) {
      if ("yes".equals(accessor.getQueryRuntimeAPI().getActivityInstanceVariable(act.getUUID(), "decisionAccepted"))) {
        approved++;
      }
    }
    // approve if more than 50% have approved
    boolean accept = 2 * approved > totalNumber;
    accessor.getRuntimeAPI().setActivityInstanceVariable(
        activityInstance.getUUID(), "decisionAccepted", accept ? "yes" : "no");

    if (LOG.isLoggable(Level.INFO)) {
      LOG.info("\n***** " + approved + "/" + totalNumber + " approved -> " + (accept ? "Accept" : "Reject") + "*****\n");
    }
  }
}
