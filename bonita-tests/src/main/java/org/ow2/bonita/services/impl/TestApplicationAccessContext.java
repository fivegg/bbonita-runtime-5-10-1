package org.ow2.bonita.services.impl;

import org.ow2.bonita.facade.BonitaApplicationAccessContext;

public class TestApplicationAccessContext implements BonitaApplicationAccessContext {

  public static String applicationName;

  public String getApplicationName() {
    return applicationName;
  }

}
