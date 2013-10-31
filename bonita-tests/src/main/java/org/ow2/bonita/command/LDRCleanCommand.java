package org.ow2.bonita.command;

import org.ow2.bonita.env.Environment;
import org.ow2.bonita.services.LargeDataRepository;
import org.ow2.bonita.util.Command;
import org.ow2.bonita.util.EnvTool;

public class LDRCleanCommand implements Command<Object> {

  private static final long serialVersionUID = -2948658366426093737L;

  public Object execute(Environment environment) throws Exception {
    LargeDataRepository ldr = EnvTool.getLargeDataRepository();
    ldr.clean();
    return null;
  }

}
