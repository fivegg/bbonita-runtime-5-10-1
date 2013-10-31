/**
 * Copyright (C) 2009  BonitaSoft S.A..
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
package org.ow2.bonita.connector.core.desc;

import java.util.List;

/**
 * 
 * @author Matthieu Chaffotte
 * 
 */
public class Array extends Widget {

  private final int cols;
  private final int rows;
  private final boolean fixedCols;
  private final boolean fixedRows;
  private final List<String> colsCaptions;

  public Array(final String labelId, final Setter setter, final int cols, final int rows, final boolean fixedCols,
      final boolean fixedRows, final List<String> colsCaptions) {
    super(labelId, setter);
    this.cols = cols;
    this.rows = rows;
    this.fixedCols = fixedCols;
    this.fixedRows = fixedRows;
    this.colsCaptions = colsCaptions;
  }

  public int getCols() {
    return cols;
  }

  public int getRows() {
    return rows;
  }

  public boolean isFixedCols() {
    return fixedCols;
  }

  public boolean isFixedRows() {
    return fixedRows;
  }

  public List<String> getColsCaptions() {
    return colsCaptions;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + cols;
    result = prime * result + (colsCaptions == null ? 0 : colsCaptions.hashCode());
    result = prime * result + (fixedCols ? 1231 : 1237);
    result = prime * result + (fixedRows ? 1231 : 1237);
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
    final Array other = (Array) obj;
    if (cols != other.cols) {
      return false;
    }
    if (colsCaptions == null) {
      if (other.colsCaptions != null) {
        return false;
      }
    } else if (!colsCaptions.equals(other.colsCaptions)) {
      return false;
    }
    if (fixedCols != other.fixedCols) {
      return false;
    }
    if (fixedRows != other.fixedRows) {
      return false;
    }
    if (rows != other.rows) {
      return false;
    }
    return true;
  }

}
