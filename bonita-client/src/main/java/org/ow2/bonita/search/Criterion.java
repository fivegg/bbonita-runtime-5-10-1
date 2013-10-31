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

/**
 * 
 * @author Matthieu Chaffotte
 *
 */
public class Criterion implements Serializable {

  private static final long serialVersionUID = 3605699586653944501L;

  private SearchQueryBuilder builder;
  private String fieldName;
  private String value;

  public Criterion(String fieldName, SearchQueryBuilder builder) {
    this.fieldName = fieldName;
    this.builder = builder;
  }

  public Criterion(SearchQueryBuilder builder) {
    this.builder = builder;
  }

  public Criterion startsWith(String value) {
    this.value = QueryFormatter.startsWith(value);
    return this;
  }

  public Criterion equalsTo(String value) {
    this.value = QueryFormatter.equals(value);
    return this;
  }

  public Criterion ranges(String from, String to, boolean exclusive) {
    this.value = QueryFormatter.ranges(from, to, exclusive);
    return this;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    if(fieldName != null && !"".equals(fieldName.trim())) {
      builder.append(fieldName).append(":");
    }
    builder.append(value);
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
