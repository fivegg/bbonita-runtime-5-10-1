/**
 * Copyright (C) 2012 BonitaSoft S.A.
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

import org.ow2.bonita.services.DocumentVersion;

/**
 * 
 * @author Elias Ricken de Medeiros
 * 
 */
public class DocumentVersionImpl implements DocumentVersion {

  private static final long serialVersionUID = -8961816264977966202L;
  protected long dbid;

  private String author;
  protected long creationDate;
  private String lastModifiedBy;
  protected long lastModificationDate;
  private boolean majorVersion;
  protected long versionSeriesId;
  private String contentMimeType;
  private String contentFileName;
  protected long contentSize;

  protected String contentStorageId;
  private long versionLabel;

  public DocumentVersionImpl() {}

  public DocumentVersionImpl(final String author, final Date creationDate, final Date lastModificationDate, final boolean majorVersion,
      final long versionSeriesId, final String contentFileName, final String contentMimeType, final long contentSize, final long versionLabel) {

    this.author = author;
    this.versionLabel = versionLabel;
    this.creationDate = creationDate.getTime();
    this.lastModifiedBy = author;
    this.lastModificationDate = lastModificationDate.getTime();
    this.majorVersion = majorVersion;
    this.versionSeriesId = versionSeriesId;
    this.contentFileName = contentFileName;
    this.contentMimeType = contentMimeType;
    this.contentSize = contentSize;
  }

  @Override
  public long getId() {
    return dbid;
  }

  @Override
  public String getAuthor() {
    return author;
  }

  @Override
  public long getCreationDate() {
    return creationDate;
  }

  @Override
  public String getLastModifiedBy() {
    return lastModifiedBy;
  }

  @Override
  public long getLastModificationDate() {
    return lastModificationDate;
  }

  @Override
  public boolean isMajorVersion() {
    return majorVersion;
  }

  @Override
  public long getVersionSeriesId() {
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

  @Override
  public String getContentStorageId() {
    return contentStorageId;
  }

  @Override
  public void setContentStorageId(final String contentStorageId) {
    this.contentStorageId = contentStorageId;
  }
  
  public void setCreationDate(final long creationDate) {
    this.creationDate = creationDate;
  }
  
  public void setLastModificationDate(final long lastModificationDate) {
    this.lastModificationDate = lastModificationDate;
  }
  
  @Override
  public long getVersionLabel() {
    return versionLabel;
  }

}
