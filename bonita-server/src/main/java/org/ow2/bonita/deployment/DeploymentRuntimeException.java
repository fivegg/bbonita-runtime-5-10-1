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
package org.ow2.bonita.deployment;

import org.ow2.bonita.util.BonitaRuntimeException;

public class DeploymentRuntimeException extends BonitaRuntimeException {

  private static final long serialVersionUID = 3181476298112460242L;

  public DeploymentRuntimeException(String message) {
    super(message);
  }

  public DeploymentRuntimeException(String message, Throwable throwable) {
    super(message, throwable);
  }

}
