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
package org.ow2.bonita.example.aw.performer;

import java.util.Set;

import org.ow2.bonita.definition.PerformerAssign;
import org.ow2.bonita.facade.QueryAPIAccessor;
import org.ow2.bonita.facade.runtime.ActivityInstance;

public class UserPerformerAssign implements PerformerAssign {

  public String selectUser(QueryAPIAccessor accessor, ActivityInstance activityInstance, Set<String> candidates) 
    throws Exception {
    String user = (String) accessor.getQueryRuntimeAPI().getActivityInstanceVariable(activityInstance.getUUID(), "performer");
    return user;
  }
}
