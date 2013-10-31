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
package org.ow2.bonita.services;

import java.util.Date;
import java.util.List;

import org.ow2.bonita.facade.exception.DocumentAlreadyExistsException;
import org.ow2.bonita.facade.exception.DocumentNotFoundException;
import org.ow2.bonita.facade.exception.DocumentationCreationException;
import org.ow2.bonita.facade.impl.SearchResult;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.search.DocumentSearchBuilder;

/**
 * 
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Elias Ricken de Medeiros
 * 
 */
public interface DocumentationManager {

  /**
   * Creates a document with an empty content. This document will be attached to
   * a process definition.
   * 
   * @param name
   *          the document name
   * @param definitionUUID
   *          the process definition UUID
   * @return the created document
   * @throws DocumentAlreadyExistsException
   *           if the document already exists.
   * @throws DocumentationCreationException
   *           for other any document creation exception
   */
  Document createDocument(final String name, final ProcessDefinitionUUID definitionUUID) throws DocumentationCreationException, DocumentAlreadyExistsException;

  /**
   * Creates a document with an empty content. This document will be attached to
   * a process definition and the process instance.
   * 
   * @param name
   *          the document name
   * @param definitionUUID
   *          the process definition UUID
   * @param instanceUUID
   *          the process instance UUID
   * @return the created document
   * @throws DocumentAlreadyExistsException
   *           if the document already exists.
   * @throws DocumentationCreationException
   *           for other any document creation exception
   */
  Document createDocument(final String name, final ProcessDefinitionUUID definitionUUID, final ProcessInstanceUUID instanceUUID) throws DocumentationCreationException,
      DocumentAlreadyExistsException;

  /**
   * Creates a document and stores the file data.
   * 
   * @param name
   *          the document name
   * @param definitionUUID
   *          the process definition UUID
   * @param fileName
   *          the file name
   * @param contentMimeType
   *          the content type of the file
   * @param fileContent
   *          the content of the file
   * @return the created document
   * @throws DocumentAlreadyExistsException
   *           if the document already exists
   * @throws DocumentationCreationException
   *           for other any document creation exception
   */
  Document createDocument(final String name, final ProcessDefinitionUUID definitionUUID, String fileName, String contentMimeType, final byte[] fileContent)
      throws DocumentationCreationException, DocumentAlreadyExistsException;

  /**
   * Creates a document and stores the file data.
   * 
   * @param name
   *          the document name
   * @param definitionUUID
   *          the process definition UUID
   * @param instanceUUID
   *          the process instance UUID
   * @param fileName
   *          the file name
   * @param contentMimeType
   *          the content type of the file
   * @param fileContent
   *          the content of the file
   * @return the created document
   * @throws DocumentAlreadyExistsException
   *           if the document already exists
   * @throws DocumentationCreationException
   *           for other any document creation exception
   */
  Document createDocument(final String name, final ProcessDefinitionUUID definitionUUID, final ProcessInstanceUUID instanceUUID, String fileName, String contentMimeType,
      final byte[] fileContent) throws DocumentationCreationException, DocumentAlreadyExistsException;

  /**
   * Creates a document and stores the file data.
   * 
   * @param name
   *          the document name
   * @param definitionUUID
   *          the process definition UUID
   * @param instanceUUID
   *          the process instance UUID
   * @param fileName
   *          the file name
   * @param contentMimeType
   *          the content type of the file
   * @param fileContent
   *          the content of the file
   * @param author
   *          the author of document
   * @param creationDate
   *          the creation date
   * @param lastModificationDate
   *          the last modification Date
   * @return the created document
   * @throws DocumentAlreadyExistsException
   *           if the document already exists
   * @throws DocumentationCreationException
   *           for other any document creation exception
   */
  Document createDocument(final String name, final ProcessDefinitionUUID definitionUUID, final ProcessInstanceUUID instanceUUID, String fileName, String contentMimeType,
      final byte[] fileContent, String author, Date creationDate, Date lastModificationDate) throws DocumentationCreationException, DocumentAlreadyExistsException;

  /**
   * Create a meta document. This document only will be retrieved when using the method getMetaDocuments.
   * 
   * @param definitionUUID
   *          the process definition UUID
   * @param name
   *          the document name
   * @param fileName
   *          the file name
   * @param contentMimeType
   *          the content type of the file
   * @param fileContent
   *          the content of the file
   * @return the created document
   * @throws DocumentAlreadyExistsException
   *           if the document already exists
   * @throws DocumentationCreationException
   *           for other any document creation exception
   */
  Document createMetaDocument(ProcessDefinitionUUID definitionUUID, String name, String fileName, String contentMimeType, byte[] fileContent)
      throws DocumentationCreationException, DocumentAlreadyExistsException;

  Document createMetaDocument(ProcessDefinitionUUID definitionUUID, String name, String fileName, String contentMimeType, byte[] fileContent, String author, Date creationDate, Date lastModificationDate)
      throws DocumentationCreationException, DocumentAlreadyExistsException;

  /**
   * Gets the document according to its identifier.
   * 
   * @param documentId
   *          the document identifier
   * @return the document
   * @throws DocumentNotFoundException
   *           if the document is not found
   */
  Document getDocument(final String documentId) throws DocumentNotFoundException;

  /**
   * Deletes a document and its version if allVersions is true.
   * 
   * @param documentId
   *          the document identifier
   * @param allVersions
   *          true to delete all versions of the given document.
   * @throws DocumentNotFoundException
   *           if the document is not found
   */
  void deleteDocument(final String documentId, boolean allVersions) throws DocumentNotFoundException;

  /**
   * Get the content of a document.
   * 
   * @param document
   *          the document
   * @return the content of the document
   * @throws DocumentNotFoundException
   *           if the document is not found
   */
  byte[] getContent(final Document document) throws DocumentNotFoundException;

  /**
   * Returns the meta documents of the given Process Definition UUID.
   * 
   * @param processDefinitionUUID the Process Definition UUID
   *          the folder identifier.
   * @return he meta documents of the given Process Definition UUID.
   */
  List<Document> getMetaDocuments(ProcessDefinitionUUID processDefinitionUUID);

  /**
   * Gets all the versions of the given document.
   * 
   * @param documentId
   *          the document identifier
   * @return all the versions of a document.
   * @throws DocumentNotFoundException
   *           if the document is not found
   */
  List<Document> getVersionsOfDocument(String documentId) throws DocumentNotFoundException;

  /**
   * Create a new version of a document. It creates a new copy of the document
   * and define whether the new version is a major one.
   * 
   * @param documentId
   *          the document identifier
   * @param isMajorVersion
   *          true to make the new version a major one
   * @return the new document version
   * @throws DocumentationCreationException
   *           for any document creation exception
   */
  Document createVersion(String documentId, boolean isMajorVersion) throws DocumentationCreationException;

  /**
   * Create a new version of a document. It creates a new copy of the document
   * and define whether the new version is a major one.
   * 
   * @param documentId
   *          the document identifier
   * @param isMajorVersion
   *          true to make the new version a major one
   * @param fileName
   *          the file name
   * @param mimeType
   *          the content type of the file
   * @param content
   *          the content of the file
   * @return the new document version
   * @throws DocumentationCreationException
   *           for any document creation exception
   */
  Document createVersion(String documentId, boolean isMajorVersion, String fileName, String mimeType, byte[] content) throws DocumentationCreationException;

  Document createVersion(String documentId, boolean isMajorVersion, String fileName, String mimeType, byte[] content, String author, Date creationDate, Date lastModificationDate) throws DocumentationCreationException;

  /**
   * Get process instance's documents
   * 
   * @param instanceUUID
   *          the process instance UUID
   * @param fromResult
   *          the first document to retrieve
   * @param maxResults
   *          the number of documents to retrieve
   * @return the list of documents associated to the given process instance
   */
  List<Document> getDocuments(ProcessInstanceUUID instanceUUID, int fromResult, int maxResults);

  /**
   * Get process instance's documents having the given document name
   * 
   * @param instanceUUID
   *          the process instance UUID
   * @param documentName
   *          the document name
   * @param fromResult
   *          the first document to retrieve
   * @param maxResults
   *          the number of documents to retrieve
   * @return the list of documents matching with the given parameters
   */
  List<Document> getDocuments(ProcessInstanceUUID instanceUUID, String documentName, int fromResult, int maxResults);

  /**
   * Get the number process instance's documents having the given document name
   * 
   * @param instanceUUID
   *          the process instance UUID
   * @param documentName
   *          the document name
   * @return the number of documents matching with the given parameters
   */
  long getNbOfDocuments(ProcessInstanceUUID instanceUUID, String documentName);

  /**
   * Get process definition's documents having the given document name
   * 
   * @param processDefUUID
   *          the process definition UUID
   * @param documentName
   *          the document name
   * @param fromResult
   *          the first document to retrieve
   * @param maxResults
   *          the number of documents to retrieve
   * @return the list of documents matching with the given parameters
   */
  List<Document> getDocuments(ProcessDefinitionUUID processDefUUID, String documentName, int fromResult, int maxResults);

  /**
   * Get the process defintion's documents that are not associated to any
   * process instance
   * 
   * @param processDefUUID
   *          the process definition UUID
   * @param fromResult
   *          the first document to retrieve
   * @param maxResults
   *          the number of documents to retrieve
   * @return the list of documents matching with the given parameters
   */
  List<Document> getDocumentsOfProcessDefinitionWithoutInstances(ProcessDefinitionUUID processDefUUID, int fromResult, int maxResults);

  /**
   * Searches documents according to the query given by the builder.
   * 
   * @param builder
   *          the builder which contains the query
   * @param fromResult
   *          the first document to retrieve
   * @param maxResults
   *          the number of documents to retrieve
   * @return the found documents according to the query
   */
  SearchResult search(DocumentSearchBuilder builder, int fromResult, int maxResults);

  /**
   * Clear all the repository: folders and documents.
   * 
   * @throws DocumentNotFoundException
   *           if a document is not found during deletion
   */
  void clear() throws DocumentNotFoundException;

  /**
   * Attaches a document to a process definition UUID and a process instance
   * UUID.
   * 
   * @param processDefinitionUUID
   *          the process definition UUID
   * @param processInstanceUUID
   *          the process instance UUID
   * @param documentId
   *          the document identifier
   * @throws DocumentNotFoundException
   *           if the document is not found
   */
  public void attachDocumentTo(final ProcessDefinitionUUID processDefinitionUUID, final ProcessInstanceUUID processInstanceUUID, final String documentId)
      throws DocumentNotFoundException;

  /**
   * Delete all documents and meta documents for the given process definition UUID
   * @param definitionUUID the Process Definition UUID
   */
  void deleteDocuments(ProcessDefinitionUUID definitionUUID) throws DocumentNotFoundException;

}
