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

import org.ow2.bonita.facade.APIAccessor;
import org.ow2.bonita.facade.runtime.ActivityInstance;

/**
 * Implementing this interface allows to execute transactional hooks.
 * <p>
 * Hook and deadline features are requiring the implementation of TxHook or Hook interface.
 * Within xpdl definition, the hook feature is specified within
 * external attributes of Activity element.<br>
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
 * This TxHook interface is intended to use APIs acceded by the APIAccessor(provided
 * into parameters of the execute() method) that can do write operations into the engine database:<br>
 * <ul>
 * <li>{@link org.ow2.bonita.facade.RuntimeAPI RuntimeAPI},</li>
 * <li>{@link org.ow2.bonita.facade.ManagementAPI ManagementAPI},</li>
 * <li>{@link org.ow2.bonita.facade.CommandAPI CommandAPI}.</li>
 * </ul>
 * If an exception occurs the exception is raised by the engine and the transaction is not committed (rollback).<br>
 * Note: In the opposite if implementing {@link Hook Hook} interface the exception is caught by the engine
 * and no rollback is performed.
 * </p>
 * <p>
 * If at process definition (with proEd) the designer has chosen: hook with rollback,<br>
 *  implementing this interface for the hook is well suited.<br>
 * If the designer has chosen: hook without rollback,<br>implementing this interface for the hook is
 * not authorized by the engine (will generate an exception).
 * </p>
 */

public interface TxHook {

  /**
   * Method of the interface to be implemented.<br>
   * Put in all your required user-defined operations.
   * @param accessor The APIAccessor interface to access: RuntimeAPI, ManagementAPI, DefinitionAPI, CommandAPI.
   * @param activityInstance the record of the current activity.
   * @throws Exception If an Exception has occurred.
   */
  void execute(APIAccessor accessor, ActivityInstance activityInstance) throws Exception;

}
