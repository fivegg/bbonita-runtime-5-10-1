package org.ow2.bonita.event;

import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;

public class JobRetry {

  private final ProcessInstanceUUID instanceUUID;
  private final int retry;

  public JobRetry(final ProcessInstanceUUID instanceUUID, final int retry) {
    super();
    this.instanceUUID = instanceUUID;
    this.retry = retry;
  }

  public ProcessInstanceUUID getProcessInstanceUUID() {
    return instanceUUID;
  }

  public int getRetry() {
    return retry;
  }

}
