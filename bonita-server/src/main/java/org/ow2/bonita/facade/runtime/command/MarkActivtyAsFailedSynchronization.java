/**
 * Copyright (C) 2012  BonitaSoft S.A.
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

import javax.transaction.Synchronization;

import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.util.EnvTool;

/**
 * @author Elias Ricken de Medeiros
 *
 */
public class MarkActivtyAsFailedSynchronization implements Synchronization {

  private final ActivityInstanceUUID activityInstUUID;
  private final int transactionStatus;

  public MarkActivtyAsFailedSynchronization(final ActivityInstanceUUID activityInstUUID, final int transactionStatus) {
    this.activityInstUUID = activityInstUUID;
    this.transactionStatus = transactionStatus;
  }
  
  @Override
  public void afterCompletion(final int transactionStatus) {
    if (this.transactionStatus == transactionStatus) {
      EnvTool.getCommandService().execute(new MarkActivityAsFailed(activityInstUUID));
    }

  }

  @Override
  public void beforeCompletion() {
    //nothing to do
  }

}
