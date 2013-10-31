package org.ow2.bonita.cmd;

import org.ow2.bonita.env.Environment;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.util.Command;
import org.ow2.bonita.util.EnvTool;

public class RemoveProcessClassloaderCommand implements Command<Void> {
  private static final long serialVersionUID = 1L;

  private ProcessDefinitionUUID processUUID;
  
  public RemoveProcessClassloaderCommand(final ProcessDefinitionUUID processUUID) {
    this.processUUID = processUUID;
  }
  
	public Void execute(Environment environment) throws Exception {
	  EnvTool.getClassDataLoader().removeProcessClassLoader(processUUID);
    return null;
	}
	
}