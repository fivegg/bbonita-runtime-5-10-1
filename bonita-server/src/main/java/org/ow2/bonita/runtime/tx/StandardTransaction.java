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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.transaction.Synchronization;

import org.ow2.bonita.env.Transaction;
import org.ow2.bonita.util.ExceptionManager;

/**
 * simple 2 phase commit transaction. no logging or recovery. non thread safe
 * (which is ok).
 * 
 * @author Tom Baeyens
 */
public class StandardTransaction implements Transaction, Serializable {

  private static final long serialVersionUID = 1L;
  private static final Logger LOG = Logger.getLogger(StandardTransaction.class.getName());

  enum State {
    CREATED, ACTIVE, ROLLBACKONLY, COMMITTED, ROLLEDBACK
  }

  protected List<StandardResource> resources;
  protected List<StandardSynchronization> synchronizations;
  protected State state = State.CREATED;

  // methods for interceptor //////////////////////////////////////////////////

  public void begin() {
  	if (LOG.isLoggable(Level.FINE)) {
      LOG.fine("beginning " + this);
  	}
    state = State.ACTIVE;
  }

  public void complete() {
    if (state == State.ACTIVE) {
      commit();
    } else if (state == State.ROLLBACKONLY) {
      rollback();
    } else {
    	String message = ExceptionManager.getInstance().getFullMessage("bp_ST_1", state);
      throw new TransactionException(message);
    }
  }

  // public tx methods ////////////////////////////////////////////////////////

  public void setRollbackOnly() {
    if (state != State.ACTIVE) {
    	String message = ExceptionManager.getInstance().getFullMessage("bp_ST_2", state);
      throw new TransactionException(message);
    }
    state = State.ROLLBACKONLY;
  }

  public boolean isRollbackOnly() {
    return ((state == State.ROLLBACKONLY) || (state == State.ROLLEDBACK));
  }

  // commit ///////////////////////////////////////////////////////////////////

  /** implements simplest two phase commit. */
  public void commit() {
    if (state != State.ACTIVE) {
    	String message = ExceptionManager.getInstance().getFullMessage("bp_ST_3", state);
      throw new TransactionException(message);
    }

    if (LOG.isLoggable(Level.FINE)) {
      LOG.fine("committing " + this);
    }

    try {
      beforeCompletion();

      if (resources != null) {
        // prepare
        // //////////////////////////////////////////////////////////////
        // the prepare loop will be skipped at the first exception
        for (StandardResource standardResource : resources) {
        	if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("preparing resource " + standardResource);
        	}
          standardResource.prepare();
        }
      }

      // for any exception in the prepare phase, we'll rollback
    } catch (Exception exception) {
      try {
      	if (LOG.isLoggable(Level.FINE)) {
          LOG.fine("resource threw exception in prepare.  rolling back.");
      	}
        rollbackResources();
      } catch (Exception rollbackException) {
      	if (LOG.isLoggable(Level.SEVERE)) {
          LOG.severe("rollback failed as well: " + rollbackException);
      	}
      }

      // rethrow
      if (exception instanceof RuntimeException) {
        throw (RuntimeException) exception;
      }
      String message = ExceptionManager.getInstance().getFullMessage("bp_ST_4");
      throw new TransactionException(message, exception);
    }

    // here is the point of no return :-)

    // commit ///////////////////////////////////////////////////////////////
    Throwable commitException = null;
    if (resources != null) {
      // The commit loop will try to send the commit to every resource,
      // No matter what it takes. If exceptions come out of resource.commit's
      // they will be suppressed and the first exception will be rethrown after
      // all the resources are commited
      for (StandardResource standardResource : resources) {
        try {
        	if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("committing resource " + standardResource);
        	}
          standardResource.commit();

          // Exceptions in the commit phase will not lead to rollback, since
          // some resources
          // might have committed and can't go back.
        } catch (Throwable t) {
          // TODO this should go to a special log for sys admin recovery
        	if (LOG.isLoggable(Level.SEVERE)) {
            LOG.severe("commit failed for resource " + standardResource + ": " + t);
        	}
          if (commitException == null) {
            commitException = t;
          }
        }
      }
    }

    state = State.COMMITTED;
    afterCompletion();
    if (LOG.isLoggable(Level.FINE)) {
      LOG.fine("committed " + this);
    }

    if (commitException != null) {
      if (commitException instanceof RuntimeException) {
        throw (RuntimeException) commitException;
      } else if (commitException instanceof Error) {
        throw (Error) commitException;
      }
      String message = ExceptionManager.getInstance().getFullMessage("bp_ST_5");
      throw new TransactionException(message, commitException);
    }
  }

  // rollback /////////////////////////////////////////////////////////////////

  public void rollback() {
    if ((state != State.ACTIVE) && (state != State.ROLLBACKONLY)) {
    	String message = ExceptionManager.getInstance().getFullMessage("bp_ST_6", state);
      throw new TransactionException(message);
    }

    if (LOG.isLoggable(Level.FINE)) {
      LOG.fine("rolling back " + this);
    }

    beforeCompletion();
    rollbackResources();
  }

  void rollbackResources() {
    if (resources != null) {
      for (StandardResource resource : resources) {
        try {
        	if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("rolling back resource " + resource);
        	}
          resource.rollback();
        } catch (Exception e) {
        	if (LOG.isLoggable(Level.SEVERE)) {
            LOG.severe("rollback failed for resource " + resource);
        	}
        }
      }
    }

    state = State.ROLLEDBACK;

    afterCompletion();

    if (LOG.isLoggable(Level.FINE)) {
      LOG.fine("rolled back");
    }
  }

  // synchronizations /////////////////////////////////////////////////////////

  public void registerSynchronization(Synchronization synchronization) {
    if (synchronizations == null) {
      synchronizations = new ArrayList<StandardSynchronization>();
    }
    synchronizations.add(new StandardSynchronization(synchronization));
  }

  public void afterCompletion() {
    if (synchronizations != null) {
      for (StandardSynchronization synchronization : synchronizations) {
        synchronization.afterCompletion(state);
      }
    }
  }

  public void beforeCompletion() {
    if (synchronizations != null) {
      for (StandardSynchronization synchronization : synchronizations) {
        synchronization.beforeCompletion();
      }
    }
  }

  // resource enlisting ///////////////////////////////////////////////////////

  public void enlistResource(StandardResource standardResource) {
    if (resources == null) {
      resources = new ArrayList<StandardResource>();
    }
    if (LOG.isLoggable(Level.FINE)) {
      LOG.fine("enlisting resource " + standardResource + " to standard transaction");
    }
    resources.add(standardResource);
  }

  List<StandardResource> getResources() {
    return resources;
  }

  public String toString() {
    return "StandardTransaction[" + System.identityHashCode(this) + "]";
  }
}
