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
 * 
 * Modified by Matthieu Chaffotte - BonitaSoft S.A.
 **/
package org.ow2.bonita.connector.core.desc;

/**
 * 
 * @author Matthieu Chaffotte
 * 
 */
public class Textarea extends Widget {

  private final int rows;
  private final int columns;
  private final int maxChar;
  private final int maxCharPerRow;

  public Textarea(final String labelId, final Setter setter, final int rows, final int columns, final int maxChar,
      final int maxCharPerRow) {
    super(labelId, setter);
    this.rows = rows;
    this.columns = columns;
    this.maxChar = maxChar;
    this.maxCharPerRow = maxCharPerRow;
  }

  public int getRows() {
    return rows;
  }

  public int getColumns() {
    return columns;
  }

  public int getMaxChar() {
    return maxChar;
  }

  public int getMaxCharPerRow() {
    return maxCharPerRow;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + columns;
    result = prime * result + maxChar;
    result = prime * result + maxCharPerRow;
    result = prime * result + rows;
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Textarea other = (Textarea) obj;
    if (columns != other.columns) {
      return false;
    }
    if (maxChar != other.maxChar) {
      return false;
    }
    if (maxCharPerRow != other.maxCharPerRow) {
      return false;
    }
    if (rows != other.rows) {
      return false;
    }
    return true;
  }

}
