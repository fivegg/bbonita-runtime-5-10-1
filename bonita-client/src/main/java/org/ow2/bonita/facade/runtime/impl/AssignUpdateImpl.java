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
 * 
 * Modified by Matthieu Chaffotte - BonitaSoft S.A.
 **/
package org.ow2.bonita.facade.runtime.impl;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.AssignUpdate;
import org.ow2.bonita.util.CopyTool;

public class AssignUpdateImpl extends UpdateImpl implements AssignUpdate {

  private static final long serialVersionUID = 8524430341951029186L;

  protected Set<String> candidates;

  protected String userId;

  protected AssignUpdateImpl() {
    super();
  }

  public AssignUpdateImpl(final Date date, final ActivityState taskState, final String loggedInUserId,
      final Set<String> candidates, final String assignedUserId) {
    super(date, taskState, loggedInUserId);
    if (candidates != null && !candidates.isEmpty()) {
      this.candidates = new HashSet<String>(candidates);
    }
    userId = assignedUserId;
  }

  public AssignUpdateImpl(final AssignUpdate assignUpdate) {
    super(assignUpdate);
    final Set<String> candidates = assignUpdate.getCandidates();
    if (candidates != null && !candidates.isEmpty()) {
      this.candidates = CopyTool.copy(candidates);

    }
    userId = assignUpdate.getAssignedUserId();
  }

  @Override
  public String getAssignedUserId() {
    return userId;
  }

  @Override
  public Set<String> getCandidates() {
    return candidates;
  }

}
