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
package org.ow2.bonita.facade.runtime;

import java.io.Serializable;
import java.util.Date;

/**
 * Interface to get recorded (runtime) information on Hook in order to debug.
 * @author Pierre Vigneras
 */
public interface HookInstance extends Serializable {
  /**
   * Returns the date recorded when the hook is started.
   * @return The date recorded when the hook is started.
   */
  Date getStartedDate();
  /**
   * Returns the date recorded when the hook is finished.
   * @return The date recorded when the hook is finished.
   */
  Date getFinishedDate();
  /**
   * Get the recorded exception thrown by the hook.
   * @return The exception thrown by the hook.
   */
  Exception getExceptionThrown();
  /**
   * Returns a string representation of the java hook recorded before
   * the hook is executed.
   * For debugging purpose this method could be useful to implement.
   * @return The string representation of the java hook before the hook is executed.
   */
  String getToStringBeforeStarted();
  /**
   * Returns a string representation of the java hook recorded after
   * the hook is executed.
   * For debugging purpose this method could be useful to implement.
   * @return The string representation of the java hook after the hook is executed.
   */
  String getToStringAfterStarted();
}
