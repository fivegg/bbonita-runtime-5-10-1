package org.ow2.bonita.services.impl;

import org.ow2.bonita.env.Environment;
import org.ow2.bonita.util.Command;
import org.ow2.bonita.util.EnvTool;

public class StopEventExecutorCommand implements Command<Void>{

  private static final long serialVersionUID = 1L;

  public Void execute(Environment environment) throws Exception {
    EnvTool.getEventExecutor().stop();
    return null;
  }
}

