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
package org.ow2.bonita.search;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.ow2.bonita.search.index.DocumentIndex;
import org.ow2.bonita.util.xml.XStreamUtil;

import com.thoughtworks.xstream.XStream;

/**
 * 
 * @author Baptiste Mesta
 *
 */
public class DocumentSearchBuilder implements Serializable {

  private static final long serialVersionUID = 6226365334784520445L;

  private List<Object> query;
  private boolean searchAllVersions = false;

  public DocumentSearchBuilder() {
    query = new ArrayList<Object>();
  }

  public DocumentCriterion criterion(DocumentIndex index) {
    DocumentCriterion criterion = new DocumentCriterion(index, this);
    query.add(criterion);
    return criterion;
  }

  public DocumentSearchBuilder leftParenthesis() {
    query.add(QueryFormatter.L_PARENTHESIS);
    return this;
  }

  public DocumentSearchBuilder rightParenthesis() {
    query.add(QueryFormatter.R_PARENTHESIS);
    return this;
  }

  public DocumentSearchBuilder or() {
    query.add(QueryFormatter.OR);
    return this;
  }

  public DocumentSearchBuilder and() {
    query.add(QueryFormatter.AND);
    return this;
  }

  public List<Object> getQuery() {
    return query;
  }

  public DocumentSearchBuilder allVersion() {
    this.searchAllVersions = true;
    return this;
  }
  
  public DocumentSearchBuilder latestVersion() {
    this.searchAllVersions = false;
    return this;
  }

  /**
   * @return
   */
  public boolean isSearchAllVersions() {
    return searchAllVersions;
  }
  
  public static DocumentSearchBuilder valueOf(String xmlDocumentSearchBuilder){
    XStream xstream = XStreamUtil.getDefaultXstream();
    return (DocumentSearchBuilder)xstream.fromXML(xmlDocumentSearchBuilder);
  }
  
  @Override
  public String toString() {
    XStream xstream = XStreamUtil.getDefaultXstream();
    return xstream.toXML(this);
  }
}
