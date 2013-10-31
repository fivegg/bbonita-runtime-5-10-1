package org.ow2.bonita.integration.connector.test;

import java.util.List;

import org.ow2.bonita.connector.core.ConnectorError;
import org.ow2.bonita.connector.core.ProcessConnector;
import org.ow2.bonita.facade.runtime.AttachmentInstance;

public class AttachmentConnector extends ProcessConnector {

  private AttachmentInstance attachment;
  private String attachmentName;

  public String getAttachmentName() {
    return attachmentName;
  }

  public void setAttachment(AttachmentInstance attachment) {
    this.attachment = attachment;
  }

  @Override
  protected void executeConnector() throws Exception {
    attachmentName = attachment.getName();
  }

  @Override
  protected List<ConnectorError> validateValues() {
    return null;
  }

}
