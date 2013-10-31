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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ow2.bonita.facade.exception.DocumentAlreadyExistsException;
import org.ow2.bonita.facade.exception.DocumentNotFoundException;
import org.ow2.bonita.facade.exception.DocumentationCreationException;
import org.ow2.bonita.facade.impl.SearchResult;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.persistence.DocumentDbSession;
import org.ow2.bonita.search.DocumentSearchBuilder;
import org.ow2.bonita.services.Document;
import org.ow2.bonita.services.DocumentDescriptor;
import org.ow2.bonita.services.DocumentDescriptorMapping;
import org.ow2.bonita.services.DocumentStorageService;
import org.ow2.bonita.services.DocumentVersion;
import org.ow2.bonita.services.DocumentationManager;
import org.ow2.bonita.util.EnvTool;

/**
 * @author Elias Ricken de Medeiros
 * 
 */
public class DocumentManagerImpl implements DocumentationManager {

  private static final int MAX_RESULTS = 100;
  private final String persistenceServiceName;
  private final DocumentStorageService documentStorageService;

  private final Map<Long, Object> documentsLock = new HashMap<Long, Object>(3);

  private final Map<Long, Long> documentsLockCount = new HashMap<Long, Long>();

  private final Object lock = new Object();

  public DocumentManagerImpl(final String persistenceServiceName) {
    documentStorageService = EnvTool.getDocumentStorageService();
    this.persistenceServiceName = persistenceServiceName;
  }

  public DocumentDbSession getDbSession() {
    return EnvTool.getDocumentDbSession(persistenceServiceName);
  }

  public void ensureHasLockEntry(final Long documentDescId) {
    synchronized (lock) {
      if (!documentsLock.containsKey(documentDescId)) {
        documentsLock.put(documentDescId, new Object());
        documentsLockCount.put(documentDescId, 1L);
      } else {
        documentsLockCount.put(documentDescId, documentsLockCount.get(documentDescId) + 1);
      }
    }
  }

  public void removeLockEntryIfPossible(final Long documentDescId) {
    synchronized (lock) {
      final Long count = documentsLockCount.get(documentDescId);
      if (count == 1) {
        documentsLockCount.remove(documentDescId);
        documentsLock.remove(documentDescId);
      } else {
        documentsLockCount.put(documentDescId, count - 1);
      }
    }
  }

  @Override
  public Document createDocument(final String name, final ProcessDefinitionUUID definitionUUID)
      throws DocumentationCreationException, DocumentAlreadyExistsException {
    return createDocument(name, definitionUUID, null, null, null, null);
  }

  @Override
  public Document createDocument(final String name, final ProcessDefinitionUUID definitionUUID,
      final ProcessInstanceUUID instanceUUID) throws DocumentationCreationException, DocumentAlreadyExistsException {
    return createDocument(name, definitionUUID, instanceUUID, null, null, null);
  }

  @Override
  public Document createDocument(final String name, final ProcessDefinitionUUID definitionUUID, final String fileName,
      final String contentMimeType, final byte[] fileContent) throws DocumentationCreationException,
      DocumentAlreadyExistsException {
    return createDocument(name, definitionUUID, null, fileName, contentMimeType, fileContent);
  }

  @Override
  public Document createDocument(final String name, final ProcessDefinitionUUID definitionUUID,
      final ProcessInstanceUUID instanceUUID, final String fileName, final String contentMimeType,
      final byte[] fileContent) throws DocumentationCreationException, DocumentAlreadyExistsException {
    return createDocument(name, definitionUUID, instanceUUID, fileName, contentMimeType, fileContent, false);
  }

  @Override
  public Document createDocument(final String name, final ProcessDefinitionUUID definitionUUID,
      final ProcessInstanceUUID instanceUUID, final String fileName, final String contentMimeType,
      final byte[] fileContent, final String author, final Date creationDate, final Date lastModificationDate)
      throws DocumentationCreationException, DocumentAlreadyExistsException {
    return createDocument(name, definitionUUID, instanceUUID, fileName, contentMimeType, fileContent, false, author,
        creationDate, lastModificationDate);
  }

  private Document createDocument(final String name, final ProcessDefinitionUUID definitionUUID,
      final ProcessInstanceUUID instanceUUID, final String fileName, final String contentMimeType,
      final byte[] fileContent, final boolean metaDocument) throws DocumentationCreationException,
      DocumentAlreadyExistsException {
    final Date now = new Date();
    return createDocument(name, definitionUUID, instanceUUID, fileName, contentMimeType, fileContent, metaDocument,
        EnvTool.getUserId(), now, now);
  }

  private Document createDocument(final String name, final ProcessDefinitionUUID definitionUUID,
      final ProcessInstanceUUID instanceUUID, final String fileName, final String contentMimeType,
      final byte[] fileContent, final boolean metaDocument, final String author, final Date creationDate,
      final Date lastModificationDate) throws DocumentationCreationException, DocumentAlreadyExistsException {
    long nbOfDocuments = 0;
    if (instanceUUID != null) {
      nbOfDocuments = getDbSession().hasDocuments(instanceUUID, name, metaDocument);
    } else {
      nbOfDocuments = getDbSession().hasDocuments(definitionUUID, name, metaDocument);
    }
    if (nbOfDocuments > 0) {
      throw new DocumentAlreadyExistsException("DAEE", name);
    }
    final DocumentDescriptorImpl documentDescriptorImpl = new DocumentDescriptorImpl(name, definitionUUID,
        instanceUUID, metaDocument, 0);
    getDbSession().save(documentDescriptorImpl);

    // add mapping
    final DocumentDescriptorMappingImpl docDescMapping = new DocumentDescriptorMappingImpl(
        documentDescriptorImpl.getId(), definitionUUID, instanceUUID);
    getDbSession().save(docDescMapping);

    return createDocumentVersionUsingDescriptor(fileName, contentMimeType, fileContent, true, author, creationDate,
        lastModificationDate, documentDescriptorImpl);
  }

  private Document createDocumentVersionUsingDescriptor(final String fileName, final String contentMimeType,
      final byte[] fileContent, final boolean isMajorVersion, final String author, final Date creationDate,
      final Date lastModificationDate, final DocumentDescriptor documentDescriptor)
      throws DocumentationCreationException {
    final long length = fileContent == null ? 0 : fileContent.length;
    final long versionLabel = documentDescriptor.getLastVersionLabel() + 1;
    final DocumentVersionImpl documentVersionImpl = new DocumentVersionImpl(author, creationDate, lastModificationDate,
        isMajorVersion, documentDescriptor.getId(), fileName, contentMimeType, length, versionLabel);
    documentStorageService.storeDocumentContent(documentVersionImpl, fileContent);
    getDbSession().save(documentVersionImpl);
    documentDescriptor.setLastVersionId(Long.valueOf(documentVersionImpl.getId()));
    documentDescriptor.setLastVersionLabel(versionLabel);

    return toDocument(documentDescriptor, documentVersionImpl);
  }

  private Document toDocument(final DocumentDescriptor docDesc, final DocumentVersion docVersion) {
    final boolean isLatest = docDesc.getLastVersionId() == docVersion.getId();
    final DocumentImpl documentImpl = new DocumentImpl(docDesc.getName(), docVersion.getAuthor(), new Date(
        docVersion.getCreationDate()), new Date(docVersion.getLastModificationDate()), isLatest,
        docVersion.isMajorVersion(), String.valueOf(docVersion.getVersionLabel()), String.valueOf(docVersion
            .getVersionSeriesId()), docVersion.getContentFileName(), docVersion.getContentMimeType(),
        docVersion.getContentSize(), docDesc.getProcessDefinitionUUID(), docDesc.getProcessInstanceUUID(),
        docVersion.getContentStorageId());
    documentImpl.setId(String.valueOf(docVersion.getId()));
    return documentImpl;
  }

  @Override
  public Document getDocument(final String documentId) throws DocumentNotFoundException {
    final DocumentVersion documentVersion = getDbSession().getDocumentVersion(Long.valueOf(documentId));
    if (documentVersion == null) {
      throw new DocumentNotFoundException(documentId);
    }
    final DocumentDescriptor documentDescriptor = getDbSession().getDocumentDescriptor(
        Long.valueOf(documentVersion.getVersionSeriesId()));
    return toDocument(documentDescriptor, documentVersion);
  }

  @Override
  public void deleteDocument(final String documentId, final boolean allVersions) throws DocumentNotFoundException {
    final DocumentVersion documentVersion = getDbSession().getDocumentVersion(Long.valueOf(documentId));
    final DocumentDescriptor documentDescriptor = getDbSession().getDocumentDescriptor(
        Long.valueOf(documentVersion.getVersionSeriesId()));
    if (allVersions) {
      // delete all versions from descriptor
      deleteDocuments(documentDescriptor);
    } else {
      // delete version
      deleteVersionAndContent(documentVersion);
      final List<DocumentVersion> documentVersions = getDbSession().getDocumentVersions(documentDescriptor.getId());
      if (documentVersions.isEmpty()) {
        // if it was the last version also delete the descriptor
        getDbSession().delete(documentDescriptor);
      } else if (documentDescriptor.getLastVersionId() == documentVersion.getId()) {
        // if there are other version and the last version was deleted update descriptor
        documentDescriptor.setLastVersionId(documentVersions.get(0).getId());
      }
    }

  }

  @Override
  public byte[] getContent(final Document document) throws DocumentNotFoundException {
    return documentStorageService.getContent(document.getContentStorageId());
  }

  @Override
  public List<Document> getVersionsOfDocument(final String documentId) throws DocumentNotFoundException {
    final DocumentVersion currentDocumentVersion = getDbSession().getDocumentVersion(Long.valueOf(documentId));
    final DocumentDescriptor documentDescriptor = getDbSession().getDocumentDescriptor(
        Long.valueOf(currentDocumentVersion.getVersionSeriesId()));
    final List<DocumentVersion> documentVersions = getDbSession().getDocumentVersions(documentDescriptor.getId());
    final List<Document> documents = new ArrayList<Document>(documentVersions.size());
    for (final DocumentVersion documentVersion : documentVersions) {
      documents.add(toDocument(documentDescriptor, documentVersion));
    }
    return documents;
  }

  @Override
  public Document createVersion(final String documentId, final boolean isMajorVersion)
      throws DocumentationCreationException {
    return createVersion(documentId, isMajorVersion, null, "application/octet-stream", null);
  }

  @Override
  public Document createVersion(final String documentId, final boolean isMajorVersion, final String fileName,
      final String mimeType, final byte[] content) throws DocumentationCreationException {
    final Date now = new Date();
    return createVersion(documentId, isMajorVersion, fileName, mimeType, content, EnvTool.getUserId(), now, now);
  }

  @Override
  public Document createVersion(final String documentId, final boolean isMajorVersion, final String fileName,
      final String mimeType, final byte[] content, final String author, final Date creationDate,
      final Date lastModificationDate) throws DocumentationCreationException {
    final DocumentVersion lastVersion = getDbSession().getDocumentVersion(Long.valueOf(documentId));
    final Long documentDescriptorId = Long.valueOf(lastVersion.getVersionSeriesId());

    // ensure that exists a lock entry for the given descriptor id
    ensureHasLockEntry(documentDescriptorId);
    Document document = null;
    synchronized (documentsLock.get(documentDescriptorId)) {
      final DocumentDescriptor documentDescriptor = getDbSession().getDocumentDescriptor(documentDescriptorId);
      document = createDocumentVersionUsingDescriptor(fileName, mimeType, content, isMajorVersion, author,
          creationDate, lastModificationDate, documentDescriptor);
    }

    // remove the lock entry if it isn't used anymore
    removeLockEntryIfPossible(documentDescriptorId);
    return document;
  }

  private int toInt(final long longValue) {
    if (longValue > Integer.MAX_VALUE) {
      throw new IllegalArgumentException(longValue + " cannot be cast to int because it's too long");
    }
    return (int) longValue;
  }

  @Override
  public SearchResult search(final DocumentSearchBuilder builder, final int fromResult, final int maxResults) {
    final List<Document> documents = getDbSession().searchDocuments(builder, fromResult, maxResults);
    final long numberOfDocuments = getDbSession().getNumberOfDocuments(builder);
    return new SearchResult(documents, toInt(numberOfDocuments));
  }

  @Override
  public void clear() throws DocumentNotFoundException {
    List<DocumentDescriptor> documentDescriptors = Collections.emptyList();
    final DocumentDbSession dbSession = getDbSession();
    do {
      // as the document will be deleted we must keep the same value (zero) for
      // the start index
      documentDescriptors = dbSession.getDocumentDescriptors(0, MAX_RESULTS);
      deleteDocuments(documentDescriptors);
    } while (!documentDescriptors.isEmpty());

  }

  private void deleteDocuments(final List<DocumentDescriptor> documentDescriptors) throws DocumentNotFoundException {
    for (final DocumentDescriptor documentDesc : documentDescriptors) {
      deleteDocuments(documentDesc);
    }
  }

  private void deleteDocuments(final DocumentDescriptor documentDesc) throws DocumentNotFoundException {
    deleteVersions(documentDesc);
    deleteMappingDescriptors(documentDesc);
    getDbSession().delete(documentDesc);
  }

  private void deleteMappingDescriptors(final DocumentDescriptor documentDesc) {
    List<DocumentDescriptorMapping> mappings = Collections.emptyList();
    do {
      mappings = getDbSession().getDocumentDescriptorMappings(documentDesc.getId(), 0, MAX_RESULTS);
      for (final DocumentDescriptorMapping mapping : mappings) {
        getDbSession().delete(mapping);
      }
    } while (!mappings.isEmpty());

  }

  private void deleteVersions(final DocumentDescriptor documentDesc) throws DocumentNotFoundException {
    List<DocumentVersion> documentVersions = Collections.emptyList();
    do {
      documentVersions = getDbSession().getDocumentVersions(0, MAX_RESULTS, documentDesc.getId());
      for (final DocumentVersion documentVersion : documentVersions) {
        deleteVersionAndContent(documentVersion);
      }
    } while (!documentVersions.isEmpty());
  }

  private void deleteVersionAndContent(final DocumentVersion documentVersion) throws DocumentNotFoundException {
    documentStorageService.deleteContent(documentVersion.getContentStorageId());
    getDbSession().delete(documentVersion);
  }

  @Override
  public void attachDocumentTo(final ProcessDefinitionUUID processDefinitionUUID,
      final ProcessInstanceUUID processInstanceUUID, final String documentId) throws DocumentNotFoundException {
    final DocumentVersion documentVersion = getDbSession().getDocumentVersion(Long.valueOf(documentId));
    final DocumentDescriptorMappingImpl docDescMapping = new DocumentDescriptorMappingImpl(
        documentVersion.getVersionSeriesId(), processDefinitionUUID, processInstanceUUID);
    getDbSession().save(docDescMapping);
  }

  @Override
  public List<Document> getDocuments(final ProcessInstanceUUID instanceUUID, final int fromResult, final int maxResults) {
    return getDbSession().getDocuments(instanceUUID, fromResult, maxResults);
  }

  @Override
  public List<Document> getDocuments(final ProcessInstanceUUID instanceUUID, final String documentName,
      final int fromResult, final int maxResults) {
    return getDbSession().getDocuments(instanceUUID, documentName, fromResult, maxResults);
  }

  @Override
  public List<Document> getDocuments(final ProcessDefinitionUUID processDefUUID, final String documentName,
      final int fromResult, final int maxResults) {
    return getDbSession().getDocuments(processDefUUID, documentName, fromResult, maxResults);
  }

  @Override
  public List<Document> getDocumentsOfProcessDefinitionWithoutInstances(final ProcessDefinitionUUID processDefUUID,
      final int fromResult, final int maxResults) {
    return getDbSession().getDocumentsOfProcessDefinitionWithoutInstances(processDefUUID, fromResult, maxResults);
  }

  @Override
  public long getNbOfDocuments(final ProcessInstanceUUID instanceUUID, final String documentName) {
    return getDbSession().getNbOfDocuments(instanceUUID, documentName);
  }

  @Override
  public Document createMetaDocument(final ProcessDefinitionUUID definitionUUID, final String name,
      final String fileName, final String contentMimeType, final byte[] fileContent)
      throws DocumentationCreationException, DocumentAlreadyExistsException {
    return createDocument(name, definitionUUID, null, fileName, contentMimeType, fileContent, true);
  }

  @Override
  public Document createMetaDocument(final ProcessDefinitionUUID definitionUUID, final String name,
      final String fileName, final String contentMimeType, final byte[] fileContent, final String author,
      final Date creationDate, final Date lastModificationDate) throws DocumentationCreationException,
      DocumentAlreadyExistsException {
    return createDocument(name, definitionUUID, null, fileName, contentMimeType, fileContent, true, author,
        creationDate, lastModificationDate);
  }

  @Override
  public List<Document> getMetaDocuments(final ProcessDefinitionUUID processDefinitionUUID) {
    return getDbSession().getMetaDocumentOfProcessDefinition(processDefinitionUUID, 0, Integer.MAX_VALUE);
  }

  @Override
  public void deleteDocuments(final ProcessDefinitionUUID definitionUUID) throws DocumentNotFoundException {
    List<DocumentDescriptor> documentDescriptors = Collections.emptyList();
    do {
      documentDescriptors = getDbSession().getDocumentDescriptors(definitionUUID, 0, MAX_RESULTS);
      deleteDocuments(documentDescriptors);
    } while (!documentDescriptors.isEmpty());

  }

}
