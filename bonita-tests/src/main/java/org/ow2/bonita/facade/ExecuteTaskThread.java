package org.ow2.bonita.facade;

import javax.security.auth.login.LoginContext;

import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.util.AccessorUtil;
import org.ow2.bonita.util.SimpleCallbackHandler;

public class ExecuteTaskThread extends Thread {

  private RuntimeAPI rAPI = null;
  private ActivityInstanceUUID aIUUID = null;
  private LoginContext context = null;

  private Exception exception = null;

  public ExecuteTaskThread(final ActivityInstanceUUID aIUUID) {

    try {
      context = new LoginContext("BonitaStore", new SimpleCallbackHandler("admin", ""));
    } catch (final Exception e) {
      exception = e;
      e.printStackTrace();
    }

    rAPI = AccessorUtil.getRuntimeAPI();
    this.aIUUID = aIUUID;
  }

  @Override
  public void run() {
    try {
      context.login();
      rAPI.executeTask(aIUUID, true);
      context.logout();
    } catch (final Exception e) {
      exception = e;
      e.printStackTrace();
    }
  }

  public Exception getException() {
    return exception;
  }

}
