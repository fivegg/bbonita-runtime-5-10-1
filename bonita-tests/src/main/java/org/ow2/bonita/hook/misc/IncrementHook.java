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
package org.ow2.bonita.hook.misc;

import org.ow2.bonita.definition.TxHook;
import org.ow2.bonita.facade.APIAccessor;
import org.ow2.bonita.facade.runtime.ActivityInstance;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;

public class IncrementHook implements TxHook {

  @Override
  public void execute(final APIAccessor accessor, final ActivityInstance activityInstance) throws Exception {
    final ProcessInstanceUUID instanceUUID = activityInstance.getProcessInstanceUUID();
    final String counter = (String) accessor.getQueryRuntimeAPI().getProcessInstanceVariable(instanceUUID, "counter");
    final int intValue = Integer.valueOf(counter) + 1;
    accessor.getRuntimeAPI().setProcessInstanceVariable(instanceUUID, "counter", Integer.toString(intValue));
  }

}
