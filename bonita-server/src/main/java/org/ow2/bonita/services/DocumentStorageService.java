/**
 * Copyright (C) 2012  BonitaSoft S.A.
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
package org.ow2.bonita.services;

import org.ow2.bonita.facade.exception.DocumentNotFoundException;
import org.ow2.bonita.facade.exception.DocumentationCreationException;

/**
 * @author Elias Ricken de Medeiros
 * 
 */
public interface DocumentStorageService {

  /**
   * Get content for specific document
   * 
   * @param documentId
   *          Identifier of document
   * @return content of document, it is a byte array
   * @throws DocumentNotFoundException
   *           Error thrown if no document have id corresponding to the
   *           parameter.
   */
  byte[] getContent(String documentId) throws DocumentNotFoundException;

  /**
   * Store the document content
   * 
   * @param document
   * @param documentContent
   *          Content of document, it is a byte array
   * @return document object with storage id
   * @throws DocumentationCreationException
   *           Error thrown if has exception during the document content storage
   */
  DocumentVersion storeDocumentContent(DocumentVersion document, byte[] documentContent) throws DocumentationCreationException;
  
  /**
   * Delete the document content
   * @param documentId Identifier of document
   * @throws DocumentNotFoundException if no document have id corresponding to the
   *           parameter
   */
  void deleteContent(String documentId) throws DocumentNotFoundException;

}
