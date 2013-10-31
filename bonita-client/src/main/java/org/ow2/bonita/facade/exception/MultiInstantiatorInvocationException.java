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
package org.ow2.bonita.facade.exception;

import org.ow2.bonita.util.BonitaRuntimeException;
import org.ow2.bonita.util.ExceptionManager;


/**
 * Thrown if an exception occurs during the execution of the multi-instantiation of the activity by the engine.
 * @author Guillaume Porcher
 *
 */
public class MultiInstantiatorInvocationException extends BonitaRuntimeException {

  /**
   * 
   */
  private static final long serialVersionUID = 1076451893210902522L;

  public MultiInstantiatorInvocationException(String id, String multiInstantiatorInfo, Throwable cause) {
    super(ExceptionManager.getInstance().getIdMessage(id)
    		+ ExceptionManager.getInstance().getMessage("MIIE1", multiInstantiatorInfo), cause);
  }

}
