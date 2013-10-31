package org.ow2.bonita.facade.runtime;

import java.io.Serializable;
import java.util.Date;

import org.ow2.bonita.facade.uuid.DocumentUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;

public interface Document extends Serializable {

  public DocumentUUID getUUID();

  public String getName();

  public ProcessDefinitionUUID getProcessDefinitionUUID();

  public ProcessInstanceUUID getProcessInstanceUUID();

  public String getAuthor();

  public Date getCreationDate();

  public String getLastModifiedBy();

  public Date getLastModificationDate();

  public boolean isLatestVersion();

  public boolean isMajorVersion();

  public String getVersionLabel();

  public String getVersionSeriesId();

  public String getContentMimeType();

  public String getContentFileName();

  public long getContentSize();

}
