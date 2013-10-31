package org.ow2.bonita.command;

import org.ow2.bonita.env.Environment;
import org.ow2.bonita.services.LargeDataRepository;
import org.ow2.bonita.util.Command;
import org.ow2.bonita.util.EnvTool;

public class LDRIsEmptyCommand implements Command<Boolean> {

  private static final long serialVersionUID = -5508226143740469242L;

  public Boolean execute(Environment environment) throws Exception {
    LargeDataRepository ldr = EnvTool.getLargeDataRepository();
    return ldr.isEmpty();
  }

}
