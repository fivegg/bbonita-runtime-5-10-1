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
package org.ow2.bonita.services.impl;

import org.ow2.bonita.facade.exception.DocumentNotFoundException;
import org.ow2.bonita.facade.exception.DocumentationCreationException;
import org.ow2.bonita.persistence.DocumentDbSession;
import org.ow2.bonita.runtime.CommonClassLoader;
import org.ow2.bonita.services.DocumentContent;
import org.ow2.bonita.services.DocumentStorageService;
import org.ow2.bonita.services.DocumentVersion;
import org.ow2.bonita.util.EnvTool;

/**
 * @author Elias Ricken de Medeiros
 * 
 */
public class DocumentStorageServiceImpl implements DocumentStorageService {

  private final String persistenceServiceName;

  public DocumentStorageServiceImpl(final String persistenceServiceName) {
    this.persistenceServiceName = persistenceServiceName;
  }

  public DocumentDbSession getDbSession() {
    return EnvTool.getDocumentDbSession(persistenceServiceName);
  }

  @Override
  public byte[] getContent(final String documentId) throws DocumentNotFoundException {
    final DocumentContent documentContent = getDocumentContent(documentId);
    return documentContent.getContent();
  }

  private DocumentContent getDocumentContent(final String documentId) throws DocumentNotFoundException {
    DocumentContent documentContent = null;
    final ClassLoader ori = Thread.currentThread().getContextClassLoader();
    try {
      // it's necessary to set the Classloader to avoid exception while using
      // the Studio
      // java.lang.IllegalArgumentException: interface
      // org.hibernate.engine.jdbc.WrappedBlob is not visible from class loader
      final CommonClassLoader commonClassLoader = EnvTool.getClassDataLoader().getCommonClassLoader();
      Thread.currentThread().setContextClassLoader(commonClassLoader);
      documentContent = getDbSession().getDocumentContent(Long.valueOf(documentId));
    } finally {
      if (ori != null) {
        Thread.currentThread().setContextClassLoader(ori);
      }
    }
    if (documentContent == null) {
      throw new DocumentNotFoundException(documentId);
    }
    return documentContent;

  }

  @Override
  public DocumentVersion storeDocumentContent(final DocumentVersion document, final byte[] documentContent) throws DocumentationCreationException {
    final ClassLoader ori = Thread.currentThread().getContextClassLoader();
    try {
      // it's necessary to set the class loader to avoid exception while using the Studio
      // java.lang.IllegalArgumentException: interface org.hibernate.engine.jdbc.BlobImplementer is not visible from class loader
      final CommonClassLoader commonClassLoader = EnvTool.getClassDataLoader().getCommonClassLoader();
      Thread.currentThread().setContextClassLoader(commonClassLoader);
      final DocumentContentImpl content = new DocumentContentImpl(documentContent);
      getDbSession().save(content);
      document.setContentStorageId(content.getId());
      return document;
    } finally {
      if (ori != null) {
        Thread.currentThread().setContextClassLoader(ori);
      }
    }

  }

  @Override
  public void deleteContent(final String documentId) throws DocumentNotFoundException {
    final DocumentContent documentContent = getDocumentContent(documentId);
    getDbSession().delete(documentContent);
  }

}
