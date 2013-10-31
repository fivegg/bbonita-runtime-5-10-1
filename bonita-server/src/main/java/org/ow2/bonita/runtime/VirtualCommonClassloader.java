package org.ow2.bonita.runtime;



public class VirtualCommonClassloader extends ClassLoader {

  private CommonClassLoader commonClassloader = new CommonClassLoader();
  
  public VirtualCommonClassloader() {
    super(VirtualCommonClassloader.class.getClassLoader());
  }

  protected synchronized Class< ? > loadClass(String name, boolean resolve) throws ClassNotFoundException {
    return commonClassloader.loadClass(name, resolve);
  }
  
  public CommonClassLoader getCommonClassLoader() {
    initCommonClassloader();
    return commonClassloader;
  }

  public void cleanCommonClassLoader() {
    if (commonClassloader != null) {
      commonClassloader.release();
      commonClassloader = null;
    }
  }

  public synchronized void resetCommonClassloader() {
    cleanCommonClassLoader();
    initCommonClassloader();
  }
  
  private synchronized void initCommonClassloader() {
    if (commonClassloader == null) {
      commonClassloader = new CommonClassLoader();
    }
  }
}
