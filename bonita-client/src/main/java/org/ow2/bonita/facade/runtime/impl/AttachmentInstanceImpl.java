/**
 * Copyright (C) 2010-2011 BonitaSoft S.A.
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
package org.ow2.bonita.facade.runtime.impl;

import java.util.Collections;
import java.util.Date;
import java.util.Map;

import org.ow2.bonita.facade.runtime.AttachmentInstance;
import org.ow2.bonita.facade.runtime.InitialAttachment;
import org.ow2.bonita.facade.uuid.DocumentUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.CopyTool;
import org.ow2.bonita.util.Misc;
import org.ow2.bonita.util.xml.XStreamUtil;

import com.thoughtworks.xstream.XStream;

public class AttachmentInstanceImpl implements AttachmentInstance {

  private static final long serialVersionUID = -4911584710221345476L;
  protected long dbid;

  private DocumentUUID attachmentUUID;
  protected String name;
  protected String label;
  protected String description;
  protected String fileName;
  protected Map<String, String> metaData;
  protected ProcessInstanceUUID processInstanceUUID;
  protected String author;
  protected long versionDate;

  public AttachmentInstanceImpl() {
    // hibernate use only
  }

  public AttachmentInstanceImpl(final DocumentUUID attachmentUUID, InitialAttachment attachment,
      ProcessInstanceUUID processInstanceUUID, String author, Date versionDate) {
    Misc.checkArgsNotNull(attachment.getName(), processInstanceUUID, author,
        versionDate);
    this.attachmentUUID = attachmentUUID;
    this.name = attachment.getName();
    this.processInstanceUUID = processInstanceUUID;
    this.author = author;
    this.versionDate = Misc.getTime(versionDate);
    this.label = attachment.getLabel();
    this.description = attachment.getDescription();
    this.fileName = attachment.getFileName();
    this.metaData = attachment.getMetaData();
  }

  public AttachmentInstanceImpl(final DocumentUUID attachmentUUID, String name,
      ProcessInstanceUUID processInstanceUUID, String author,
      Date versionDate) {
    Misc.checkArgsNotNull(name, processInstanceUUID, author, versionDate);
    this.attachmentUUID = attachmentUUID;
    this.name = name;
    this.processInstanceUUID = processInstanceUUID;
    this.author = author;
    this.versionDate = Misc.getTime(versionDate);
  }

  @Deprecated
  public AttachmentInstanceImpl(String name, ProcessInstanceUUID processInstanceUUID, String author, Date versionDate) {
    Misc.checkArgsNotNull(name, processInstanceUUID, author, versionDate);
    this.attachmentUUID = null;
    this.name = name;
    this.processInstanceUUID = processInstanceUUID;
    this.author = author;
    this.versionDate = Misc.getTime(versionDate);
  }

  public AttachmentInstanceImpl(AttachmentInstance src) {
    this.attachmentUUID = src.getUUID();
    this.name = src.getName();
    this.processInstanceUUID = src.getProcessInstanceUUID();
    this.author = src.getAuthor();
    this.versionDate = Misc.getTime(src.getVersionDate());
    this.label = src.getLabel();
    this.description = src.getDescription();
    this.fileName = src.getFileName();
    this.metaData = CopyTool.copy(src.getMetaData());
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public void setMetaData(Map<String, String> metaData) {
    this.metaData = metaData;
  }

  public DocumentUUID getUUID() {
    return attachmentUUID;
  }

  public ProcessInstanceUUID getProcessInstanceUUID() {
    return processInstanceUUID;
  }
  
  public String getAuthor() {
    return author;
  }

  public Date getVersionDate() {
    return Misc.getDate(versionDate);
  }

  public String getDescription() {
    return description;
  }

  public String getFileName() {
    return fileName;
  }

  public String getLabel() {
    return label;
  }

  public Map<String, String> getMetaData() {
    if (metaData == null) {
      return Collections.emptyMap();
    }
    return metaData;
  }

  public String getName() {
    return name;
  }

  @Override
	public String toString() {
		XStream xstream = XStreamUtil.getDefaultXstream();
	  return xstream.toXML(this);
	}

}
