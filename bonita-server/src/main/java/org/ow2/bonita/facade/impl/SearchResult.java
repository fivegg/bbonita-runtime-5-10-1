/**
 * Copyright (C) 2011 BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.ow2.bonita.facade.impl;

import java.util.List;

import org.ow2.bonita.services.Document;

/**
 * 
 * Represent a result od a document search
 * 
 * @author Baptiste Mesta
 *
 */
public class SearchResult {

  private final int pageNumItems;
  private final List<Document> documents;

  /**
   * @param documents
   * @param pageNumItems
   */
  public SearchResult(List<Document> documents, int pageNumItems) {
    this.documents = documents;
    this.pageNumItems = pageNumItems;
  }

  public List<Document> getDocuments() {
    return documents;
  }

  public int getCount() {
    return pageNumItems;
  }

}
