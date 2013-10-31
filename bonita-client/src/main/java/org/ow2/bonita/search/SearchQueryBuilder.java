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
import java.util.ArrayList;
import java.util.List;

import org.ow2.bonita.search.index.Index;
import org.ow2.bonita.util.xml.XStreamUtil;

import com.thoughtworks.xstream.XStream;

/**
 * 
 * @author Matthieu Chaffotte, Elias Ricken de Medeiros
 *
 */
public class SearchQueryBuilder implements Serializable {

  private static final long serialVersionUID = -3830626827857893077L;
  private Index index;
  private List<Object> query;

  public SearchQueryBuilder(Index index) {
    this.index = index;
    query = new ArrayList<Object>();
  }

  public Criterion criterion() {
    Criterion criterion = new Criterion(this);
    query.add(criterion);
    return criterion;
  }

  public Criterion criterion(String fieldName) {
    Criterion criterion = new Criterion(fieldName, this);
    query.add(criterion);
    return criterion;
  }

  public Criteria criteria(List<String> fieldNames) {
    Criteria criteria = new Criteria(fieldNames, this);
    query.add(criteria);
    return criteria;
  }

  public SearchQueryBuilder leftParenthesis() {
    query.add(QueryFormatter.L_PARENTHESIS);
    return this;
  }

  public SearchQueryBuilder rightParenthesis() {
    query.add(QueryFormatter.R_PARENTHESIS);
    return this;
  }

  public SearchQueryBuilder or() {
    query.add(" " + QueryFormatter.OR + " ");
    return this;
  }

  public SearchQueryBuilder and() {
    query.add(" " + QueryFormatter.AND + " ");
    return this;
  }

  public Index getIndex() {
    return index;
  }

  public String getQuery() {
    StringBuilder builder = new StringBuilder();
    for (Object element : query) {
      builder.append(element.toString());
    }
    return builder.toString();
  }
  
  /**
   * Create a SearchQueryBuilder from its jsonRepresentation
   * @param jsonSearchQueryBuilder
   * @return SearchQueryBuilder instance
   */
  public static SearchQueryBuilder valueOf(String jsonSearchQueryBuilder){
  	XStream xstream = XStreamUtil.getDefaultXstream();
		return (SearchQueryBuilder)xstream.fromXML(jsonSearchQueryBuilder);
  }
  
  @Override
  public String toString() {
  	XStream xstream = XStreamUtil.getDefaultXstream();
		return xstream.toXML(this);
  }

}
