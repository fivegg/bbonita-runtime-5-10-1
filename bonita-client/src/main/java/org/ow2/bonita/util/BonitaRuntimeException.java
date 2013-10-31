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
package org.ow2.bonita.util;

/**
 * Parent class for runtime exception.
 * @author Marc Blachon, Guillaume Porcher, Charles Souillard, Miguel Valdes, Pierre Vigneras.
 *
 */
public class BonitaRuntimeException extends RuntimeException {

  private static final long serialVersionUID = 3256294177735076391L;
  
  private String wrappedBy = null;

  public BonitaRuntimeException(final String message, final Throwable cause) {
    super(message, cause);
  }
  
  public BonitaRuntimeException(final String message, final Throwable cause, final String wrappedBy) {
    super(message, cause);
    this.wrappedBy = wrappedBy;
  }

  public BonitaRuntimeException(final Throwable cause) {
    super(cause);
  }

  public BonitaRuntimeException(final String message) {
    super(message);
  }
  
  public String getWrappedBy() {
    return this.wrappedBy;
  }
  
}
