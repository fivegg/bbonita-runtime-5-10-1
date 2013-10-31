/**
 * Copyright (C) 2009  Bull S. A. S.
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
package org.ow2.bonita.connector.core;

/**
 * Describes what kind of Exception was raised on a field.
 * @author Matthieu Chaffotte
 *
 */
public class ConnectorError {

  /**
   * The field name.
   */
  private String field;

  /**
   * The exception description.
   */
  private Exception error;

  /**
   * Default constructor.
   * @param field the field name
   * @param error the field exception
   */
  public ConnectorError(final String field, final Exception error) {
    this.field = field;
    this.error = error;
  }

  /**
   * Returns the field name.
   * @return the field name
   */
  public final String getField() {
    return field;
  }

  /**
   * Returns the field exception.
   * @return the the field exception
   */
  public final Exception getError() {
    return error;
  }

  /**
   * Compares this object against the specified object. The result is true
   * if and only if the argument represents the same ConnectorError as this
   * object.
   * @param obj the object to compare against.
   * @return if the objects are the same; false otherwise.
   */
  @Override
  public boolean equals(final Object obj) {
    boolean equals = false;
    if (obj instanceof ConnectorError) {
      ConnectorError c = (ConnectorError) obj;
      if (this.getField().equals(c.getField())
          && this.getError().getMessage().equals(c.getError().getMessage())) {
        equals = true;
      }
    }
    return equals;
  }
}
