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
package org.ow2.bonita.services.impl;

import java.util.Date;

import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.services.Document;
import org.ow2.bonita.util.xml.XStreamUtil;

import com.thoughtworks.xstream.XStream;

/**
 * 
 * @author Matthieu Chaffotte
 * 
 */
public class DocumentImpl implements Document {

  private String id;
  private final String name;
  private String author;
  private Date creationDate;
  private String lastModifiedBy;
  private Date lastModificationDate;
  private boolean latestVersion;
  private boolean majorVersion;
  private String versionLabel;

  private String versionSeriesId;
  private String contentMimeType;
  private String contentFileName;
  private long contentSize;
  private ProcessInstanceUUID processInstanceUUID;
  private ProcessDefinitionUUID processDefinitionUUID;
  private String contentStorageId;

  public DocumentImpl(final String name, final String author, final Date creationDate, final Date lastModificationDate,
      final boolean latestVersion, final boolean majorVersion, final String versionLabel, final String versionSeriesId, final String contentFileName,
      final String contentMimeType, final long contentSize, final ProcessDefinitionUUID processDefinitionUUID, final ProcessInstanceUUID processInstanceUUID,
      final String contentStorageId) {
    this(name, author, creationDate, lastModificationDate, latestVersion, majorVersion, versionLabel, versionSeriesId, contentFileName, contentMimeType,
        contentSize, processDefinitionUUID, processInstanceUUID, contentStorageId, null);
  }

  public DocumentImpl(final String name, final String author, final Date creationDate, final Date lastModificationDate,
      final boolean latestVersion, final boolean majorVersion, final String versionLabel, final String versionSeriesId, final String contentFileName,
      final String contentMimeType, final long contentSize, final ProcessDefinitionUUID processDefinitionUUID, final ProcessInstanceUUID processInstanceUUID,
      final String contentStorageId, final String documentId) {
    this.name = name;
    this.author = author;
    this.creationDate = creationDate;
    this.lastModifiedBy = author;
    this.lastModificationDate = lastModificationDate;
    this.latestVersion = latestVersion;
    this.majorVersion = majorVersion;
    this.versionLabel = versionLabel;
    this.versionSeriesId = versionSeriesId;
    this.contentFileName = contentFileName;
    this.contentMimeType = contentMimeType;
    this.contentSize = contentSize;
    this.processInstanceUUID = processInstanceUUID;
    this.processDefinitionUUID = processDefinitionUUID;
    this.contentStorageId = contentStorageId;
    this.id = documentId;
  }

  public DocumentImpl(final String name, final String author, final long creationDate, final long lastModificationDate,
      final boolean majorVersion, final long versionLabel, final long versionSeriesId, final String contentFileName, final String contentMimeType,
      final long contentSize, final ProcessDefinitionUUID processDefinitionUUID, final ProcessInstanceUUID processInstanceUUID, final String contentStorageId,
      final long documentId) {
    this.name = name;
    this.author = author;
    this.creationDate = new Date(creationDate);
    this.lastModifiedBy = author;
    this.lastModificationDate = new Date(lastModificationDate);
    this.latestVersion = true;
    this.majorVersion = majorVersion;
    this.versionLabel = String.valueOf(versionLabel);
    this.versionSeriesId = String.valueOf(versionSeriesId);
    this.contentFileName = contentFileName;
    this.contentMimeType = contentMimeType;
    this.contentSize = contentSize;
    this.processInstanceUUID = processInstanceUUID;
    this.processDefinitionUUID = processDefinitionUUID;
    this.contentStorageId = contentStorageId;
    this.id = String.valueOf(documentId);
  }

  /**
   * @param name
   * @param versionLabel
   */
  public DocumentImpl(final String name) {
    this.name = name;
  }

  /**
   * @param name
   * @param contentFileName
   * @param contentMimeType
   * @param contentSize
   * @param versionLabel
   */
  public DocumentImpl(final String name, final String contentFileName, final String contentMimeType, final long contentSize) {
    this.name = name;
    this.contentFileName = contentFileName;
    this.contentMimeType = contentMimeType;
    this.contentSize = contentSize;

  }

  @Override
  public String getId() {
    return id;
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
  public String getAuthor() {
    return author;
  }

  @Override
  public Date getCreationDate() {
    return creationDate;
  }

  @Override
  public String getLastModifiedBy() {
    return lastModifiedBy;
  }

  @Override
  public Date getLastModificationDate() {
    return lastModificationDate;
  }

  @Override
  public boolean isLatestVersion() {
    return latestVersion;
  }

  @Override
  public boolean isMajorVersion() {
    return majorVersion;
  }

  @Override
  public String getVersionLabel() {
    return versionLabel;
  }

  @Override
  public String getVersionSeriesId() {
    return versionSeriesId;
  }

  @Override
  public String getContentMimeType() {
    return contentMimeType;
  }

  @Override
  public String getContentFileName() {
    return contentFileName;
  }

  @Override
  public long getContentSize() {
    return contentSize;
  }

  /**
   * @return the processInstanceUUID
   */
  @Override
  public ProcessInstanceUUID getProcessInstanceUUID() {
    return processInstanceUUID;
  }

  /**
   * @return the processDefinitionUUID
   */
  @Override
  public ProcessDefinitionUUID getProcessDefinitionUUID() {
    return processDefinitionUUID;
  }

  @Override
  public void setId(final String id) {
    this.id = id;
  }

  @Override
  public String getContentStorageId() {
    return contentStorageId;
  }

  public void setContentStorageId(final String contentStorageId) {
    this.contentStorageId = contentStorageId;
  }

  @Override
  public String toString() {
    final XStream xstream = XStreamUtil.getDefaultXstream();
    return xstream.toXML(this);
  }

}
