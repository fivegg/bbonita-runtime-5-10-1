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

import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.services.DocumentDescriptorMapping;

/**
 * @author Elias Ricken de Medeiros
 *
 */
public class DocumentDescriptorMappingImpl implements DocumentDescriptorMapping {
  
  private static final long serialVersionUID = 4061401068833087786L;

  protected long dbid;

  protected long documentDescriptorId;

  protected ProcessInstanceUUID processInstanceUUID;
  
  protected ProcessDefinitionUUID processDefinitionUUID;

  
  public DocumentDescriptorMappingImpl() {
  }
  
  public DocumentDescriptorMappingImpl(final long documentDescriptorId, final ProcessDefinitionUUID processDefinitionUUID, final ProcessInstanceUUID processInstanceUUID) {
    super();
    this.documentDescriptorId = documentDescriptorId;
    this.processInstanceUUID = processInstanceUUID;
    this.processDefinitionUUID = processDefinitionUUID;
  }

  @Override
  public long getDocumentDescriptorId() {
    return documentDescriptorId;
  }


  public void setDocumentDescriptorId(final long documentDescriptorId) {
    this.documentDescriptorId = documentDescriptorId;
  }


  @Override
  public ProcessInstanceUUID getProcessInstanceUUID() {
    return processInstanceUUID;
  }


  public void setProcessInstanceUUID(final ProcessInstanceUUID processInstanceUUID) {
    this.processInstanceUUID = processInstanceUUID;
  }


  @Override
  public ProcessDefinitionUUID getProcessDefinitionUUID() {
    return processDefinitionUUID;
  }


  public void setProcessDefinitionUUID(final ProcessDefinitionUUID processDefinitionUUID) {
    this.processDefinitionUUID = processDefinitionUUID;
  }


}
