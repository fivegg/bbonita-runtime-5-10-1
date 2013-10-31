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
package org.ow2.bonita.persistence;

import java.util.List;

import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.search.DocumentSearchBuilder;
import org.ow2.bonita.services.Document;
import org.ow2.bonita.services.DocumentContent;
import org.ow2.bonita.services.DocumentDescriptor;
import org.ow2.bonita.services.DocumentDescriptorMapping;
import org.ow2.bonita.services.DocumentVersion;

/**
 * @author Elias Ricken de Medeiros
 * 
 */
public interface DocumentDbSession extends DbSession {

  DocumentDescriptor getDocumentDescriptor(long documentDescriptorId);

  DocumentVersion getDocumentVersion(long versionId);

  DocumentContent getDocumentContent(long contentStorageId);

  List<DocumentDescriptor> getDocumentDescriptors(int fromIndex, int maxResults);

  List<DocumentDescriptor> getDocumentDescriptors(ProcessDefinitionUUID processDefinitionUUID, int fromIndex, int maxResults);

  List<DocumentVersion> getDocumentVersions(int fromIndex, int maxResults, long versionSeriesId);

  List<DocumentVersion> getDocumentVersions(long versionSeriesId);

  List<Document> searchDocuments(DocumentSearchBuilder builder, int fromIndex, int maxResults);

  List<Document> getDocuments(ProcessInstanceUUID instanceUUID, int fromResult, int maxResults);

  List<Document> getDocuments(ProcessInstanceUUID instanceUUID, String documentName, int fromResult, int maxResults);

  List<Document> getDocuments(ProcessDefinitionUUID processDefUUID, String documentName, int fromResult, int maxResults);

  List<Document> getMetaDocumentOfProcessDefinition(ProcessDefinitionUUID processDefUUID, int fromResult, int maxResults);

  List<Document> getDocumentsOfProcessDefinitionWithoutInstances(ProcessDefinitionUUID processDefUUID, int fromResult, int maxResults);
  
  List<DocumentDescriptorMapping> getDocumentDescriptorMappings(long documentDescriptorId, int fromIndex, int maxResults);

  long getNbOfDocuments(ProcessInstanceUUID instanceUUID, String documentName);

  long hasDocuments(ProcessInstanceUUID instanceUUID, String documentName, boolean metaDocument);

  long hasDocuments(ProcessDefinitionUUID processUUID, String documentName, boolean metaDocument);

  long getNumberOfDocuments(DocumentSearchBuilder builder);

}
