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

public class MSSQLServerDescNullsFirstInterceptor extends DescNullFirstInterceptor {

  private static final long serialVersionUID = -1791670858498091038L;
  private final String[] keywords = new String[] {"limit"};

  @Override
  protected int getLast(final String query, final String end) {
    return query.indexOf(end);
  }

  @Override
  protected String getDescValue(final String current) {
    final StringBuilder builder = new StringBuilder("(case when ");
    builder.append(current).append(" is null then 0 else 1 end), ").append(current).append(" desc");
    return builder.toString();
  }

  @Override
  protected String getAscValue(final String current) {
    final StringBuilder builder = new StringBuilder("(case when ");
    builder.append(current).append(" is null then 1 else 0 end), ").append(current).append(" asc");
    return builder.toString();
  }

  @Override
  protected String formatSelectQuery(final String sql, final int orderByIndex, final String orderByContent, final int orderByEndIndex) {
    if (!sql.startsWith("select distinct ")) {
      return super.formatSelectQuery(sql, orderByIndex, orderByContent, orderByEndIndex);
    } else {
      return formatSelectDistinctQuery(sql, orderByIndex, orderByContent, orderByEndIndex);
    }
  }

  String formatSelectDistinctQuery(final String sql, final int orderByIndex, final String orderByContent, final int orderByEndIndex) {
    final int firstSelectedFieldIndex = 16;
    final int lastSelectedFieldIndex = sql.indexOf("from ");
    String selectedFields = sql.substring(firstSelectedFieldIndex, lastSelectedFieldIndex);
    selectedFields = selectedFields.replaceAll(" as [a-zA-Z_0-9]*", "");
    selectedFields = selectedFields.replaceAll("top [0-9]* ", "");

    final StringBuilder builder = new StringBuilder("select ");
    builder.append(sql.substring(16, orderByIndex)).append("group by ").append(selectedFields)
    .append("order by ").append(orderByContent).append(" ").append(sql.substring(orderByEndIndex));
    return builder.toString();
  }

  @Override
  protected String[] getKeyWordsAfterOrderBy() {
    return keywords;
  }

}
