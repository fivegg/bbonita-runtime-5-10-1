/**
 * Copyright (C) 2006  Bull S. A. S.
 * Bull, Rue Jean Jaures, B.P.68, 78340, Les Clayes-sous-Bois
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA  02110-1301, USA.
 * 
 * Modified by Matthieu Chaffotte - BonitaSoft S.A.
 **/
package org.ow2.bonita.facade.def.element.impl;

import org.ow2.bonita.facade.def.element.AttachmentDefinition;
import org.ow2.bonita.facade.def.majorElement.impl.ProcessElementImpl;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;

public class AttachmentDefinitionImpl extends ProcessElementImpl implements AttachmentDefinition {

  private static final long serialVersionUID = 4707882598625841832L;
  protected String filePath;
  protected String fileName;

  protected AttachmentDefinitionImpl() {
    super();
  }

  public AttachmentDefinitionImpl(final ProcessDefinitionUUID processUUID, final String name) {
    super(name, processUUID);
  }

  public AttachmentDefinitionImpl(final AttachmentDefinition src) {
    super(src);
    fileName = src.getFileName();
    filePath = src.getFilePath();
  }

  @Override
  public String getFilePath() {
    return filePath;
  }

  public void setFilePath(final String filePath) {
    this.filePath = filePath;
  }

  @Override
  public String getFileName() {
    return fileName;
  }

  public void setFileName(final String fileName) {
    this.fileName = fileName;
  }

}
