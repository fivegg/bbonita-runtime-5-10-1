package org.ow2.bonita.util;

import java.io.File;

public final class ServerConstants {
  
  private ServerConstants() {}

  private static final String getTenantServerFolder(String domain) {
    StringBuilder serverPath = new StringBuilder(BonitaConstants.getBonitaHomeFolder());
    serverPath.append(File.separator).append("server").append(File.separator).append(domain);
    return serverPath.toString();
  }

  public static final String getTenantConfigurationFolder(String domain) {
    StringBuilder tempPath = new StringBuilder(getTenantServerFolder(domain));
    tempPath.append(File.separator).append("conf");
    return tempPath.toString();
  }

  public static final String getTenantTemporaryFolder(String domain) {
    StringBuilder tempPath = new StringBuilder(getTenantServerFolder(domain));
    tempPath.append(File.separator).append("tmp");
    return tempPath.toString();
  }  

  public static final String getTenantWorkingFolder(String domain) {
    StringBuilder tempPath = new StringBuilder(getTenantServerFolder(domain));
    tempPath.append(File.separator).append("work");
    return tempPath.toString();
  }

}
