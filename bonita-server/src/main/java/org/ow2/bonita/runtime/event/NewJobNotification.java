/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
package org.ow2.bonita.runtime.event;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.transaction.Synchronization;

/**
 * 
 * @author Matthieu Chaffotte
 * 
 */
public class NewJobNotification implements Synchronization {

  static final Logger LOG = Logger.getLogger(NewJobNotification.class.getName());

  private final EventExecutor eventExecutor;

  public NewJobNotification(final EventExecutor jobExecutor) {
    eventExecutor = jobExecutor;
  }

  @Override
  public void afterCompletion(final int arg0) {
    if (LOG.isLoggable(Level.FINE)) {
      LOG.fine("Notifying job executor of the arrival of a new event");
    }
    eventExecutor.internalJobExecutorRefresh();
  }

  @Override
  public void beforeCompletion() {
    // Nothing to do
  }

  @Override
  public String toString() {
    return "new-job-notification";
  }

}
