package org.ow2.bonita.facade;

import org.ow2.bonita.env.Environment;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.exception.ProcessNotFoundException;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.util.AccessorUtil;
import org.ow2.bonita.util.Command;

public class GetProcessIdCommand implements Command<String> {
  /**
   * 
   */
  private static final long serialVersionUID = -4801840420489826816L;
  private ProcessDefinitionUUID processUUID;
  public GetProcessIdCommand(ProcessDefinitionUUID processUUID) {
    this.processUUID = processUUID;
  }
  public String execute(Environment arg0) throws ProcessNotFoundException {
    ProcessDefinition process = AccessorUtil.getAPIAccessor().getQueryDefinitionAPI().getProcess(processUUID);
    return process.getName();
  }
  
}
