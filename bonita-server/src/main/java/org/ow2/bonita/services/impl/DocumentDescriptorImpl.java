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
import org.ow2.bonita.services.DocumentDescriptor;

/**
 * @author Elias Ricken de Medeiros
 * 
 */
public class DocumentDescriptorImpl implements DocumentDescriptor {

  private static final long serialVersionUID = -6490700300202238464L;

  protected long dbid;

  protected long lastVersionId;

  private String name;
  
  protected boolean metaDocument;

  private ProcessInstanceUUID processInstanceUUID;
  
  private ProcessDefinitionUUID processDefinitionUUID;
  
  private long lastVersionLabel;

  public DocumentDescriptorImpl() {}

  public DocumentDescriptorImpl(final String name, final ProcessDefinitionUUID processDefinitionUUID, final ProcessInstanceUUID processInstanceUUID, final boolean metaDocument, final long lastVersionLabel) {
    this.name = name;
    this.processInstanceUUID = processInstanceUUID;
    this.processDefinitionUUID = processDefinitionUUID;
    this.metaDocument = metaDocument;
    this.lastVersionLabel = lastVersionLabel;
  }

  @Override
  public long getId() {
    return dbid;
  }

  @Override
  public long getLastVersionId() {
    return lastVersionId;
  }

  @Override
  public void setLastVersionId(final long lastVersionId) {
    this.lastVersionId = lastVersionId;
  }
  
  @Override
  public long getLastVersionLabel() {
    return lastVersionLabel;
  }
  
  @Override
  public void setLastVersionLabel(final long lastVersionLabel) {
    this.lastVersionLabel = lastVersionLabel;
  }

  @Override
  public String getName() {
    return name;
  }

  public void setProcessInstanceUUID(final ProcessInstanceUUID processInstanceUUID) {
    this.processInstanceUUID = processInstanceUUID;
  }

  public void setProcessDefinitionUUID(final ProcessDefinitionUUID processDefinitionUUID) {
    this.processDefinitionUUID = processDefinitionUUID;
  }

  @Override
  public ProcessInstanceUUID getProcessInstanceUUID() {
    return processInstanceUUID;
  }

  @Override
  public ProcessDefinitionUUID getProcessDefinitionUUID() {
    return processDefinitionUUID;
  }

  public boolean isMetaDocument() {
    return metaDocument;
  }

  public void setMetaDocument(final boolean metaDocument) {
    this.metaDocument = metaDocument;
  }

}
