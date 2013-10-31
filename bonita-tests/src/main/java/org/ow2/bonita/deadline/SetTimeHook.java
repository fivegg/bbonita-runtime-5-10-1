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
package org.ow2.bonita.deadline;

import org.ow2.bonita.definition.Hook;
import org.ow2.bonita.facade.APIAccessor;
import org.ow2.bonita.facade.QueryAPIAccessor;
import org.ow2.bonita.facade.impl.StandardAPIAccessorImpl;
import org.ow2.bonita.facade.runtime.ActivityInstance;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;

/**
 * @author Pascal Verdage
 */
public class SetTimeHook implements Hook {

  public void execute(QueryAPIAccessor accessor, ActivityInstance activityInstance) throws Exception {
    /*
     * With a Hook, no setter is available via QueryAPIAccessor.
     * For test purpose we want to check the execution time of a Hook
     * so we recreate an APIAccessor to be able to modify process variables.
     * You SHOULD NOT do that in a production environment.
     * If you need to set a variable, use a TxHook.
     */
    APIAccessor accessor2 = new StandardAPIAccessorImpl();
    String time = Long.toString(System.currentTimeMillis());
    ProcessInstanceUUID instanceUUID = activityInstance.getProcessInstanceUUID();
    accessor2.getRuntimeAPI().setProcessInstanceVariable(instanceUUID, "time", time);
  }

}


