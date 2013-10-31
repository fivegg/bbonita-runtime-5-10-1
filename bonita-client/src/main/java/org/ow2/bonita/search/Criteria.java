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
package org.ow2.bonita.search;

import java.io.Serializable;
import java.util.List;

/**
 * 
 * @author Matthieu Chaffotte
 *
 */
public class Criteria implements Serializable {

  private static final long serialVersionUID = 3605699586653944501L;

  private SearchQueryBuilder builder;
  private List<String> fieldNames;
  private String value;
  private String operator;

  public Criteria(List<String> fieldNames, SearchQueryBuilder builder) {
    this.fieldNames = fieldNames;
    this.operator = QueryFormatter.OR;
    this.builder = builder;
  }

  public Criteria union() {
    operator = QueryFormatter.OR;
    return this;
  }

  public Criteria inclusion() {
    operator = QueryFormatter.AND;
    return this;
  }

  public Criteria startsWith(String value) {
    this.value = QueryFormatter.startsWith(value);
    return this;
  }

  public Criteria equalsTo(String value) {
    this.value = QueryFormatter.equals(value);
    return this;
  }

  public Criteria ranges(String from, String to, boolean exclusive) {
    this.value = QueryFormatter.ranges(from, to, exclusive);
    return this;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    int fields = fieldNames.size();
    for (int i = 0; i < fields - 1; i++) {
      String fieldName = fieldNames.get(i);
      String field = QueryFormatter.field(fieldName, value);
      builder.append(field).append(" ").append(operator).append(" ");
    }
    String lastFieldName = fieldNames.get(fields - 1);
    String lastField = QueryFormatter.field(lastFieldName, value);
    builder.append(lastField);
    return builder.toString();
  }

  public SearchQueryBuilder rightParenthesis() {
    builder.rightParenthesis();
    return builder;
  }

  public SearchQueryBuilder or() {
    builder.or();
    return builder;
  }

  public SearchQueryBuilder and() {
    builder.and();
    return builder;
  }

}
