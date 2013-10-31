/**
 * Copyright (C) 2010  BonitaSoft S.A.
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
package org.ow2.bonita.facade.exception;

import org.ow2.bonita.util.BonitaRuntimeException;
/**
 *
 * Wrapper exception <b>used internally</b> by the engine to wrap standard runtime exception or child classes
 * of BonitaRuntimeException. This wrapping allows to throw the original runtime exception to the end user
 * thanks to an API intercepter.
 */
public class BonitaWrapperException extends BonitaRuntimeException {

  private static final long serialVersionUID = 220147110980619393L;

  /**
   * Constructs a BonitaWrapperException with the throwable cause.
   * @param cause exception causing the abort.
   */
  public BonitaWrapperException(final Throwable cause) {
    super(cause);
  }
  
}
