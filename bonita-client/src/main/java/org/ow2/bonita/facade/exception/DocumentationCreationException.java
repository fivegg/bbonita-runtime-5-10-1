/**
 * Copyright (C) 2011  BonitaSoft S.A.
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

import org.ow2.bonita.util.BonitaException;

/**
 * Exception thrown when a document creation fails.
 * @author Nicolas Chabanoles
 *
 */
public class DocumentationCreationException extends BonitaException {

  private static final long serialVersionUID = -2543890613924795697L;

  public DocumentationCreationException(final String message) {
    super(message);
  }

  public DocumentationCreationException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public DocumentationCreationException(DocumentationCreationException e) {
    super(e.getMessage());
  }

}
