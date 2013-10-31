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
package org.ow2.bonita.util;

public class ArrayUtil {

  public static String toString(Object[] array) {
    if (array == null) {
      return "null";
    } else {
      StringBuffer buffer = new StringBuffer();
      buffer.append("[");
      for (int i = 0; i < array.length; i++) {
        Object o = array[i];
        if (o != null) {
          buffer.append(o);
        } else {
          buffer.append("null");
        }
        if (i != array.length - 1) {
          buffer.append("|");
        }
      }
      buffer.append("]");
      return buffer.toString();
    }
  }
}
