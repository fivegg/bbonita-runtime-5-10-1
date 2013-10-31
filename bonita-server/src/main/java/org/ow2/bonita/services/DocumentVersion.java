package org.ow2.bonita.services;

import java.io.Serializable;

public interface DocumentVersion extends Serializable {

  public long getId();

  public String getContentStorageId();

  public void setContentStorageId(String contentStorageId);

  public String getAuthor();

  public long getCreationDate();

  public String getLastModifiedBy();

  public long getLastModificationDate();

  public boolean isMajorVersion();

  public long getVersionSeriesId();

  public String getContentMimeType();

  public String getContentFileName();

  public long getContentSize();
  
  public long getVersionLabel();

}
