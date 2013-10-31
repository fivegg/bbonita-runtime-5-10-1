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
package org.ow2.bonita.facade;

import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.util.Command;

/**
 * To allow developers to write and execute its own commands packaged within its application.
 * @see org.ow2.bonita.definition.TxHook
 */
public interface CommandAPI {

  /**
   * Executes a command deployed at common level or one available in bonita distribution.
   * @param command Command to execute
   * @return the result of the command
   * @throws Exception if an exception occurs during the command execution
   */
  <T> T execute(Command<T> command) throws Exception;

  /**
   * Executes a command deployed at package level.
   * @param command Command to execute
   * @param processUUID process UUID in which the command was deployed (result of bar deployment)
   * @return the result of the command
   * @throws Exception if an exception occurs during the command execution
   */
  <T> T execute(Command<T> command, ProcessDefinitionUUID processUUID) throws Exception;

}
