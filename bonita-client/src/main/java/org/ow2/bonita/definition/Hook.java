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
 **/
package org.ow2.bonita.definition;

import org.ow2.bonita.facade.QueryAPIAccessor;
import org.ow2.bonita.facade.runtime.ActivityInstance;

/**
 * Implementing this interface allows to execute not transactional hooks.
 * <p>
 * Hook and deadline features are requiring the implementation of TxHook or Hook interface.
 * Within xpdl definition, the hook feature is specified within external attributes of Activity element.<br>
 * There is two parameters defining the hook:
 * <ul>
 * <li>the event Name (ie. for hook on task: task:onReady, task:onStart, task:onFinish, task:onSuspend,
 * task:onResume and for hook on automatic activity: automatic:onEnter)</li>
 * <li>the name of the class that implements this interface</li>
 * </ul>
 * </p>
 * <p>
 * For deadline feature, name of the class that implements this interface is specified within XPDL Deadline element.<br>
 * The event name (internal to the engine) is ON_DEADLINE.<br>
 *  This Hook interface is intended to use APIs acceded by the the QueryAPIAccessor (provided
 *  into parameters of the execute() method) that should do read operations into the engine database:<br>
 *  <ul>
 *  <li>{@link org.ow2.bonita.facade.QueryRuntimeAPI QueryRuntimeAPI},</li>
 *  <li>{@link org.ow2.bonita.facade.QueryDefinitionAPI QueryDefinitionAPI}</li>
 *  </ul>
 *  If an exception occurs the exception is catched by the engine and no rollback is performed.
 *  Note: In the opposite if implementing {@link TxHook TxHook} interface the exception is not catched but
 *  raised by the engine and the transaction is not commited (rollback).
 *  </p>
 *  <p>
 *  This interface can be implemented for hook if at process definition (under proed)
 *  the designer has chosen:
 *  <ul>
 *  <li>either hook without rollback,</li>
 *  <li>or hook with rollback.</li>
 *  </ul>
 *  </p>
 */
public interface Hook {
  /**
   * Method of the interface to be implemented.<br>
   * Put in all your required user-defined operations.
   * @param accessor The QueryAPIAccessor interface to access: QueryRuntimeAPI or QueryDefinitionAPI.
   * @param activityInstance the record of the current activity.
   * @throws Exception If an Exception has occurred.
   */
  void execute(QueryAPIAccessor accessor, ActivityInstance activityInstance) throws Exception;

}


