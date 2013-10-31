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
package org.ow2.bonita.env.operation;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.ow2.bonita.env.Transaction;
import org.ow2.bonita.env.WireContext;
import org.ow2.bonita.env.WireException;
import org.ow2.bonita.runtime.tx.StandardResource;
import org.ow2.bonita.runtime.tx.StandardTransaction;
import org.ow2.bonita.util.ExceptionManager;

/**
 * enlists this {@link StandardResource} with the current {@link Transaction}.
 * 
 * <p>
 * This {@link Operation} specifies that the object on which this operation is
 * applied should be added as a {@link StandardResource} to the specified
 * {@link Transaction}.
 * </p>
 * 
 * <p>
 * property transactionName refers to the objectName of the {@link Transaction}
 * and it may not be null.
 * </p>
 * 
 * @author Tom Baeyens
 * @author Guillaume Porcher (documentation)
 */
public class EnlistOperation implements Operation {

  private static final long serialVersionUID = 1L;
  private static final Logger LOG = Logger.getLogger(EnlistOperation.class.getName());

  String transactionName = null;

  /**
   * @throws WireException
   *           if this operation is applied on an object which is not a resource
   *           or if the specified transaction cannot be found.
   */
  public void apply(Object target, WireContext wireContext) {
    if (!(target instanceof StandardResource)) {
    	String message = ExceptionManager.getInstance().getFullMessage(
    			"bp_EO_1", StandardResource.class.getName(), target,
    			(target != null ? " (" + target.getClass().getName() + ")" : ""));
      throw new WireException(message);
    }

    Object object = null;
    if (transactionName != null) {
      object = wireContext.get(transactionName);
    } else {
      object = wireContext.get(Transaction.class);
    }

    if ((object == null) || (!(object instanceof StandardTransaction))) {
    	String message = ExceptionManager.getInstance().getFullMessage(
    			"bp_EO_2", StandardTransaction.class.getName(),
    			(transactionName != null ? "'" + transactionName + "'" : "by type"), target);
      throw new WireException(message);
    }

    StandardTransaction standardTransaction = (StandardTransaction) object;

    if (LOG.isLoggable(Level.FINE)) {
      LOG.fine("enlisting resource " + target + " with transaction");
    }
    standardTransaction.enlistResource((StandardResource) target);
  }

  /**
   * Gets the name of the transaction to which the object should be added.
   */
  public String getTransactionName() {
    return transactionName;
  }

  /**
   * Sets the name of the transaction to which the object should be added.
   * 
   * @param transactionName
   */
  public void setTransactionName(String transactionName) {
    this.transactionName = transactionName;
  }
}
