package org.ow2.bonita.facade.runtime.impl;

import java.util.Date;

import org.ow2.bonita.facade.runtime.Document;
import org.ow2.bonita.facade.uuid.DocumentUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;

public class DocumentImpl implements Document {

  private static final long serialVersionUID = 2790236426040604303L;

  private final DocumentUUID uuid;
  private final String name;
  private final ProcessDefinitionUUID processDefinitionUUID;
  private final ProcessInstanceUUID processInstanceUUID;
  private final String author;
  private final Date creationDate;
  private final String lastModifiedBy;
  private final Date lastModificationDate;
  private final boolean isLatestVersion;
  private final boolean isMajorVersion;
  private final String versionLabel;
  private final String versionSeriesId;
  public final String contentMimeType;
  public final String contentFileName;
  public final long contentSize;

  public DocumentImpl(final DocumentUUID uuid, final String name, final ProcessDefinitionUUID processDefinitionUUID,
      final ProcessInstanceUUID processInstanceUUID, final String author, final Date creationDate,
      final String lastModifiedBy, final Date lastModificationDate, final boolean isLatestVersion,
      final boolean isMajorVersion, final String versionLabel, final String versionSeriesId,
      final String contentFileName, final String contentMimeType, final long contentSize) {
    this.uuid = uuid;
    this.name = name;
    this.processDefinitionUUID = processDefinitionUUID;
    this.processInstanceUUID = processInstanceUUID;
    this.author = author;
    this.creationDate = new Date(creationDate.getTime());
    this.lastModifiedBy = lastModifiedBy;
    this.lastModificationDate = new Date(lastModificationDate.getTime());
    this.isLatestVersion = isLatestVersion;
    this.isMajorVersion = isMajorVersion;
    this.versionLabel = versionLabel;
    this.versionSeriesId = versionSeriesId;
    this.contentFileName = contentFileName;
    this.contentMimeType = contentMimeType;
    this.contentSize = contentSize;
  }

  @Override
  public DocumentUUID getUUID() {
    return uuid;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public ProcessDefinitionUUID getProcessDefinitionUUID() {
    return processDefinitionUUID;
  }

  @Override
  public ProcessInstanceUUID getProcessInstanceUUID() {
    return processInstanceUUID;
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
    return isLatestVersion;
  }

  @Override
  public boolean isMajorVersion() {
    return isMajorVersion;
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

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (author == null ? 0 : author.hashCode());
    result = prime * result + (contentFileName == null ? 0 : contentFileName.hashCode());
    result = prime * result + (contentMimeType == null ? 0 : contentMimeType.hashCode());
    result = prime * result + (int) (contentSize ^ contentSize >>> 32);
    result = prime * result + (creationDate == null ? 0 : creationDate.hashCode());
    result = prime * result + (isLatestVersion ? 1231 : 1237);
    result = prime * result + (isMajorVersion ? 1231 : 1237);
    result = prime * result + (lastModificationDate == null ? 0 : lastModificationDate.hashCode());
    result = prime * result + (lastModifiedBy == null ? 0 : lastModifiedBy.hashCode());
    result = prime * result + (name == null ? 0 : name.hashCode());
    result = prime * result + (processDefinitionUUID == null ? 0 : processDefinitionUUID.hashCode());
    result = prime * result + (processInstanceUUID == null ? 0 : processInstanceUUID.hashCode());
    result = prime * result + (uuid == null ? 0 : uuid.hashCode());
    result = prime * result + (versionLabel == null ? 0 : versionLabel.hashCode());
    result = prime * result + (versionSeriesId == null ? 0 : versionSeriesId.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final DocumentImpl other = (DocumentImpl) obj;
    if (author == null) {
      if (other.author != null) {
        return false;
      }
    } else if (!author.equals(other.author)) {
      return false;
    }
    if (contentFileName == null) {
      if (other.contentFileName != null) {
        return false;
      }
    } else if (!contentFileName.equals(other.contentFileName)) {
      return false;
    }
    if (contentMimeType == null) {
      if (other.contentMimeType != null) {
        return false;
      }
    } else if (!contentMimeType.equals(other.contentMimeType)) {
      return false;
    }
    if (contentSize != other.contentSize) {
      return false;
    }
    if (creationDate == null) {
      if (other.creationDate != null) {
        return false;
      }
    } else if (!creationDate.equals(other.creationDate)) {
      return false;
    }
    if (isLatestVersion != other.isLatestVersion) {
      return false;
    }
    if (isMajorVersion != other.isMajorVersion) {
      return false;
    }
    if (lastModificationDate == null) {
      if (other.lastModificationDate != null) {
        return false;
      }
    } else if (!lastModificationDate.equals(other.lastModificationDate)) {
      return false;
    }
    if (lastModifiedBy == null) {
      if (other.lastModifiedBy != null) {
        return false;
      }
    } else if (!lastModifiedBy.equals(other.lastModifiedBy)) {
      return false;
    }
    if (name == null) {
      if (other.name != null) {
        return false;
      }
    } else if (!name.equals(other.name)) {
      return false;
    }
    if (processDefinitionUUID == null) {
      if (other.processDefinitionUUID != null) {
        return false;
      }
    } else if (!processDefinitionUUID.equals(other.processDefinitionUUID)) {
      return false;
    }
    if (processInstanceUUID == null) {
      if (other.processInstanceUUID != null) {
        return false;
      }
    } else if (!processInstanceUUID.equals(other.processInstanceUUID)) {
      return false;
    }
    if (uuid == null) {
      if (other.uuid != null) {
        return false;
      }
    } else if (!uuid.equals(other.uuid)) {
      return false;
    }
    if (versionLabel == null) {
      if (other.versionLabel != null) {
        return false;
      }
    } else if (!versionLabel.equals(other.versionLabel)) {
      return false;
    }
    if (versionSeriesId == null) {
      if (other.versionSeriesId != null) {
        return false;
      }
    } else if (!versionSeriesId.equals(other.versionSeriesId)) {
      return false;
    }
    return true;
  }

}
