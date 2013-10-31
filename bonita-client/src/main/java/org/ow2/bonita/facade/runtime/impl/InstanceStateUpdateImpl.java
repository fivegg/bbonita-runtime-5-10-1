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
package org.ow2.bonita.facade.runtime.impl;

import java.util.Date;

import org.ow2.bonita.facade.runtime.InstanceState;
import org.ow2.bonita.facade.runtime.InstanceStateUpdate;
import org.ow2.bonita.util.Misc;


/**
 * @author Guillaume Porcher
 *
 */
public class InstanceStateUpdateImpl implements InstanceStateUpdate {

  /**
   * 
   */
  private static final long serialVersionUID = -3185545147156677746L;
  protected long dbid;
  /**
   * date at update
   */
  protected long date;

  /**
   * User that performs the update
   */
  protected String updateUserId;

  protected InstanceState initialState;
  protected InstanceState currentState;

  protected InstanceStateUpdateImpl() {
  }

  public InstanceStateUpdateImpl(final Date date, final String updateUserId, final InstanceState initialState, final InstanceState currentState) {
    this.date = Misc.getTime(date);
    this.updateUserId = updateUserId;
    this.initialState = initialState;
    this.currentState = currentState;
  }

  public InstanceStateUpdateImpl(final InstanceStateUpdate src) {
    this.date = Misc.getTime(src.getUpdatedDate());
    this.updateUserId = src.getUpdatedBy();
    this.initialState = src.getInitialInstanceState();
    this.currentState = src.getInstanceState();
  }

  public InstanceState getInitialInstanceState() {
    return this.initialState;
  }
  public InstanceState getInstanceState() {
    return this.currentState;
  }
  public String getUpdatedBy() {
    return this.updateUserId;
  }
  public Date getUpdatedDate() {
    return Misc.getDate(this.date);
  }

}
