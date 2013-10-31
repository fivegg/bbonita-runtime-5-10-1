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
package org.ow2.bonita.env.interceptor;

import org.hibernate.EmptyInterceptor;

public abstract class DescNullFirstInterceptor extends EmptyInterceptor {

  private static final long serialVersionUID = -7671847499846010891L;
  final static String ORDER_BY = "order by";

  public String onPrepareStatement(String sql) {
    String query = sql.toLowerCase();
    int orderByIndex = query.indexOf(ORDER_BY);
    if (orderByIndex > -1) {
      int orderByEndIndex = getLast(query, getKeyWordsAfterOrderBy());
      if (orderByEndIndex == -1) {
        orderByEndIndex = query.length();
      }
      String orderByContent = sql.substring(orderByIndex + 9, orderByEndIndex);
      String[] orderByNames = orderByContent.split("\\,");
      for (int i=0; i<orderByNames.length; i++) {
        String current = orderByNames[i];
        String currentTrim = current.trim();
        if (currentTrim.length() > 0) {
          String currentLC = currentTrim.toLowerCase();
          if (currentLC.endsWith(" desc")) {
            current = current.substring(0, current.length() - 5);
            orderByNames[i] = getDescValue(current);
          } else if (currentLC.endsWith(" asc")) {
            current = current.substring(0, current.length() - 4);
            orderByNames[i] = getAscValue(current);
          } else {
            current = current.substring(0, current.length());
            orderByNames[i] = getAscValue(current);
          }
        }
      }
      orderByContent = join(orderByNames, ",");
      sql = formatSelectQuery(sql, orderByIndex, orderByContent, orderByEndIndex);
    }
    return super.onPrepareStatement(sql);
  }

  protected abstract String getDescValue(String current);
  protected abstract String getAscValue(String current);
  protected abstract String[] getKeyWordsAfterOrderBy();

  protected int getLast(final String query, String... list) {
    int result = -1;
    int i = 0;
    int size = list.length;
    while (result == -1 && i < size) {
      result = getLast(query, list[i]);
      i++;
    }
    return result;
  }

  protected int getLast(final String query, String end) {
    return query.indexOf(end);
  }

  protected String formatSelectQuery(String sql, int orderByIndex, String orderByContent, int orderByEndIndex) {
    StringBuilder builder = new StringBuilder(sql.substring(0, orderByIndex));
    builder.append("order by ").append(orderByContent).append(" ").append(sql.substring(orderByEndIndex));
    return builder.toString();
  }

  protected static String join(String[] elements, String join) {
    StringBuilder joinBuilder = new StringBuilder();
    int penultimateIndex = elements.length - 1;
    for (int i = 0; i < penultimateIndex; i++) {
      joinBuilder.append(elements[i]).append(",");
    }
    joinBuilder.append(elements[penultimateIndex]);
    return joinBuilder.toString();
  }

}
