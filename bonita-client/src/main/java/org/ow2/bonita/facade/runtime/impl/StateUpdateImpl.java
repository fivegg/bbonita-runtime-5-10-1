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

import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.StateUpdate;

public class StateUpdateImpl extends UpdateImpl implements StateUpdate {

  private static final long serialVersionUID = 1056111609014986295L;
  protected ActivityState initialState;

  protected StateUpdateImpl() {
    super();
  }

  public StateUpdateImpl(final Date date, final ActivityState finalState, final ActivityState initialState,
      final String updateUserId) {
    super(date, finalState, updateUserId);
    this.initialState = initialState;
  }

  public StateUpdateImpl(final StateUpdate src) {
    super(src);
    initialState = src.getInitialState();
  }

  public ActivityState getFinalState() {
    return state;
  }

  @Override
  public ActivityState getInitialState() {
    return initialState;
  }

}
