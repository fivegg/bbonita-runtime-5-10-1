/**
 * Copyright (C) 2010  BonitaSoft S.A.
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
package org.ow2.bonita.facade.runtime.impl;

import java.util.Date;
import java.util.logging.Logger;

import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.Update;
import org.ow2.bonita.util.Misc;

public abstract class UpdateImpl implements Update {

  private static final long serialVersionUID = -7976662433188108616L;
  protected long dbid;
  protected long date;
  /**
   * date at update
   */
  protected ActivityState state;
  /**
   * User that performs the update
   */
  protected String updateUserId;

  protected static final Logger LOG = Logger.getLogger(UpdateImpl.class.getName());

  protected UpdateImpl() {
  }

  public UpdateImpl(final Date date, final ActivityState state, final String updateUserId) {
    this.date = Misc.getTime(date);
    this.state = state;
    this.updateUserId = updateUserId;
  }

  public UpdateImpl(final Update src) {
    date = Misc.getTime(src.getUpdatedDate());
    state = src.getActivityState();
    updateUserId = src.getUpdatedBy();
  }

  @Override
  public Date getUpdatedDate() {
    return Misc.getDate(date);
  }

  @Override
  public ActivityState getActivityState() {
    return state;
  }

  @Override
  public String getUpdatedBy() {
    return updateUserId;
  }

  public static void logClockInconsistency() {
    LOG.warning("This update date is before the last update date. The recorded entries may be inconsistent. Please check your clock synchronization.");
  }
}
