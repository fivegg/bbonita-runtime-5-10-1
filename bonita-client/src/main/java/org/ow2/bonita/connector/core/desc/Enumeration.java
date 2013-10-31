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
 * 
 * Modified by Matthieu Chaffotte - BonitaSoft S.A.
 **/
package org.ow2.bonita.connector.core.desc;

import java.util.Arrays;
import java.util.Map;

/**
 * 
 * @author Matthieu Chaffotte
 * 
 */
public class Enumeration extends Widget {

  public static enum Selection {
    SINGLE, MUTLI
  };

  private final Map<String, String> values;
  private final int[] selectedIndices;
  private final int lines;
  private final Selection selection;

  public Enumeration(final String labelId, final Setter setter, final Map<String, String> values,
      final int[] selectedIndices, final int lines, final Selection selection) {
    super(labelId, setter);
    this.values = values;
    this.selectedIndices = selectedIndices;
    this.lines = lines;
    this.selection = selection;
  }

  public Map<String, String> getValues() {
    return values;
  }

  public int[] getSelectedIndices() {
    return selectedIndices;
  }

  public int getLines() {
    return lines;
  }

  public Selection getSelection() {
    return selection;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + lines;
    result = prime * result + Arrays.hashCode(selectedIndices);
    result = prime * result + (selection == null ? 0 : selection.hashCode());
    result = prime * result + (values == null ? 0 : values.hashCode());
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
    final Enumeration other = (Enumeration) obj;
    if (lines != other.lines) {
      return false;
    }
    if (!Arrays.equals(selectedIndices, other.selectedIndices)) {
      return false;
    }
    if (selection != other.selection) {
      return false;
    }
    if (values == null) {
      if (other.values != null) {
        return false;
      }
    } else if (!values.equals(other.values)) {
      return false;
    }
    return true;
  }

}
