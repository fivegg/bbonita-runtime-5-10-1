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
package org.ow2.bonita.services;

import org.ow2.bonita.util.BonitaRuntimeException;
import org.ow2.bonita.util.Command;

/**
 * abstract extensible session facade. Developers can use this directly or
 * extend one of the implementations with custom methods. Developers should be
 * encouraged to use this interface as it will be kept more stable then direct
 * usage of the API (which is still allowed). All the method implementations
 * should be based on commands. Each of the method implementations will have a
 * environment block. Then the command is executed and the environment is passed
 * into the command.
 */
public interface CommandService {

  /**
   * @throws BonitaRuntimeException
   *           if command throws an exception.
   */
  <T> T execute(Command<T> command);
}
