package org.ow2.bonita.command;

import org.ow2.bonita.env.Environment;
import org.ow2.bonita.util.Command;
import org.ow2.bonita.util.EnvTool;

public class ResetCommonClassLoader implements Command<Object> {

  private static final long serialVersionUID = 8440838297683843899L;

  public Object execute(Environment environment) throws Exception {
    EnvTool.getClassDataLoader().resetCommonClassloader();
    return null;
  }

}
