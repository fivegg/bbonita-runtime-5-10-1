/**
 * Copyright (C) 2009  BonitaSoft S.A.
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
package org.ow2.bonita.connector.core;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ow2.bonita.definition.PerformerAssign;
import org.ow2.bonita.facade.runtime.ActivityInstance;
import org.ow2.bonita.util.AccessorUtil;

/**
 * 
 * @author Matthieu Chaffotte, Mickael Istria
 *
 */
public final class PerformerAssignFilter extends Filter {

  private String className;
  private PerformerAssign performerAssign;

  public void setClassName(String className) {
    this.className = className;
  }

  public void setPerformerAssign(PerformerAssign performerAssign) {
    this.performerAssign = performerAssign;
  }

  @Override
  protected List<ConnectorError> validateValues() {
    return null;
  }

  @Override
  protected Set<String> getCandidates(Set<String> members) throws Exception {
    if (performerAssign == null && className != null) {
      performerAssign = (PerformerAssign) Class.forName(className).newInstance();
    }
    ActivityInstance activityInstance = getApiAccessor().getQueryRuntimeAPI(AccessorUtil.QUERYLIST_JOURNAL_KEY).getActivityInstance(getActivityInstanceUUID());
    String user = performerAssign.selectUser(getApiAccessor(), activityInstance, getMembers());
    Set<String> users = new HashSet<String>();
    users.add(user);
    return users;
  }

}