package org.ow2.bonita.event;

import org.ow2.bonita.env.Environment;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.services.EventService;
import org.ow2.bonita.util.Command;
import org.ow2.bonita.util.EnvTool;

public class RemoveInstanceOutgoingEventInstancesCommand implements Command<Void> {
  private static final long serialVersionUID = 1L;

  private ProcessInstanceUUID instanceUUID;
  
  public RemoveInstanceOutgoingEventInstancesCommand(ProcessInstanceUUID instanceUUID) {
    this.instanceUUID = instanceUUID;
  }
  
	public Void execute(Environment environment) throws Exception {
	  final EventService eventService = EnvTool.getEventService();
	  eventService.removeFiredEvents(instanceUUID);
	  return null;
	}
}