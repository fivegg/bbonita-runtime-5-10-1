/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.ow2.bonita.services.impl;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.hibernate.AssertionFailure;
import org.hibernate.StaleStateException;
import org.hibernate.exception.LockAcquisitionException;
import org.ow2.bonita.util.BonitaRuntimeException;
import org.ow2.bonita.util.Command;
import org.ow2.bonita.util.ExceptionManager;

/**
 * retries the command execution in case hibernate throws optimistic locking
 * (StaleObjectException) exceptions.
 * 
 * @author Tom Baeyens
 */
public class RetryInterceptor extends Interceptor {

  static final Logger LOG = Logger.getLogger(RetryInterceptor.class.getName());

  int retries = 3;
  long delay = 50;
  long delayFactor = 4;

  public <T> T execute(Command<T> command) {
    int attempt = 1;
    long sleepTime = delay;
    while (attempt <= retries) {
      if (attempt > 1) {
        if (LOG.isLoggable(Level.INFO)) {
          LOG.info("retrying...");
        }
      }
      try {

        return next.execute(command);

      } catch (StaleStateException e) {
        manageException(attempt, sleepTime, e);
      } catch (AssertionFailure e) {
        manageException(attempt, sleepTime, e);
      } catch (LockAcquisitionException e) {
        manageException(attempt, sleepTime, e);
      }
    }
    if (LOG.isLoggable(Level.INFO)) {
      LOG.info("gave up after "+attempt+" attempts");
    }
    String message = ExceptionManager.getInstance().getFullMessage("bp_RI_1", attempt);
    throw new BonitaRuntimeException(message);
  }

  private void manageException(int attempt, long sleepTime, Exception e) {
    attempt++;
    if (LOG.isLoggable(Level.INFO)) {
      LOG.info("optimistic locking failed: " + e);
      LOG.info("waiting " + sleepTime + " millis");
    }
    try {
      Thread.sleep(sleepTime);
    } catch (InterruptedException e1) {
      if (LOG.isLoggable(Level.FINE)) {
        LOG.fine("retry sleeping got interrupted");
      }
    }
    sleepTime *= delayFactor;
  }

  public int getRetries() {
    return retries;
  }

  public void setRetries(int retries) {
    this.retries = retries;
  }

  public long getDelay() {
    return delay;
  }

  public void setDelay(long delay) {
    this.delay = delay;
  }

  public long getDelayFactor() {
    return delayFactor;
  }

  public void setDelayFactor(long delayFactor) {
    this.delayFactor = delayFactor;
  }
}
