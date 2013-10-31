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
package org.ow2.bonita.runtime.tx;

import javax.transaction.Status;
import javax.transaction.Synchronization;

import org.ow2.bonita.util.ExceptionManager;

/**
 * @author Tom Baeyens
 */
public class StandardSynchronization {

  Synchronization synchronization;

  public StandardSynchronization(Synchronization synchronization) {
    this.synchronization = synchronization;
  }

  public void afterCompletion(StandardTransaction.State transactionState) {
    if (transactionState == StandardTransaction.State.COMMITTED) {
      synchronization.afterCompletion(Status.STATUS_COMMITTED);

    } else if (transactionState == StandardTransaction.State.ROLLEDBACK) {
      synchronization.afterCompletion(Status.STATUS_ROLLEDBACK);

    } else {
    	String message = ExceptionManager.getInstance().getFullMessage("bp_SS_1", transactionState);
      throw new TransactionException(message);
    }
  }

  public void beforeCompletion() {
    synchronization.beforeCompletion();
  }
}
