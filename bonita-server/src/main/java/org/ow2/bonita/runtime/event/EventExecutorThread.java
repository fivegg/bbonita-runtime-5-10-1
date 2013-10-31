/**
 * Copyright (C) 2102-2013 BonitaSoft S.A.
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

import org.ow2.bonita.services.CommandService;

/**
 * 
 * @author Matthieu Chaffotte
 * 
 */
public abstract class EventExecutorThread extends Thread {

  static final Logger LOG = Logger.getLogger(EventExecutorThread.class.getName());

  protected volatile boolean isActive = true;

  private final EventExecutor executor;

  private boolean refresh;

  private int currentIdleInterval;

  private int minimumInterval;

  private final Object semaphore = new Object();

  EventExecutorThread(final EventExecutor executor, final String name) {
    super(name);
    this.executor = executor;
  }

  @Override
  public void run() {
    if (LOG.isLoggable(Level.INFO)) {
      LOG.info("Starting...");
    }
    currentIdleInterval = executor.getIdleMillis();
    minimumInterval = executor.getMinimumInterval();
    try {
      activate();
      while (isActive()) {
        try {
          // refresh is set to true in refresh() below
          refresh = false;
          currentIdleInterval = executor.getIdleMillis();
          
          execute();
          if (isActive()) {
            final long waitPeriod = getWaitPeriod();
            if (waitPeriod > 0) {
              synchronized (semaphore) {
                if (!refresh) {
                  if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine(getName() + " will wait for max " + waitPeriod + "ms on " + executor);
                  }
                  semaphore.wait(waitPeriod);
                  if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine(getName() + " woke up, refresh=" + refresh);
                  }
                } else {
                  if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("skipped wait because new message arrived");
                  }
                }
              }
            }
          }
        } catch (final InterruptedException e) {
          LOG.info((isActive() ? "active" : "inactive") + getJobExecutorName() + " thread '" + getName()
              + "' got interrupted");
        } catch (final Exception e) {
          if (LOG.isLoggable(Level.SEVERE)) {
            LOG.log(Level.SEVERE, "exception in " + getJobExecutorName() + " thread. waiting " + currentIdleInterval
                + " milliseconds: " + e.getMessage(), e);
          }
          try {
            synchronized (semaphore) {
              semaphore.wait(currentIdleInterval);
            }
          } catch (final InterruptedException e2) {
            if (LOG.isLoggable(Level.FINE)) {
              LOG.fine("delay after exception got interrupted: " + e2);
            }
          }
        }
      }
    } catch (final Throwable t) {
      if (LOG.isLoggable(Level.WARNING)) {
        LOG.log(Level.WARNING, t.getMessage(), t);
      }
    } finally {
      if (LOG.isLoggable(Level.INFO)) {
        LOG.info(getName() + " leaves cyberspace");
      }
    }
  }

  protected abstract void activate();

  public void refresh() {
    if (LOG.isLoggable(Level.FINE)) {
      LOG.fine("Notifying " + getJobExecutorName() + " thread of new Event");
    }
    synchronized (semaphore) {
      refresh = true;
      semaphore.notify();
    }
  }
  
  public void deactivate(final boolean join) {
    if (isActive()) {
      if (LOG.isLoggable(Level.INFO)) {
        LOG.info("deactivating " + getName());
      }
      setIsActive(false);
      interrupt();
      if (join) {
        try {
          if (LOG.isLoggable(Level.INFO)) {
            LOG.info("joining " + getName());
          }
          join(60000); // a minute
        } catch (final InterruptedException e) {
          LOG.severe("joining " + getName() + " got interrupted");
        }
      }
    } else {
      if (LOG.isLoggable(Level.INFO)) {
        LOG.info("ignoring deactivate: " + getName() + " is not active");
      }
    }
    if (LOG.isLoggable(Level.INFO)) {
      LOG.info(getJobExecutorName() + " thread: " + getName() + " deactivated");
    }
  }

  private synchronized void setIsActive(final boolean value) {
    isActive = value;
  }

  public synchronized boolean isActive() {
    return isActive;
  }

  protected CommandService getCommandService() {
    return executor.getCommandService();
  }

  protected EventExecutor getEventExecutor() {
    return executor;
  }

  private long getWaitPeriod() {
    long interval = executor.getIdleMillis();
    final Long nextDueDate = getNextDueDate();
    if (nextDueDate != null) {
      final long currentTimeMillis = System.currentTimeMillis();
      if (nextDueDate < currentTimeMillis + currentIdleInterval) {
        interval = nextDueDate - currentTimeMillis;
      }
    }
    if (interval <= minimumInterval) {
      interval = minimumInterval;
    }
    return interval;
  }

  protected abstract void execute() throws InterruptedException;

  protected abstract String getJobExecutorName();

  protected abstract Long getNextDueDate();

}
