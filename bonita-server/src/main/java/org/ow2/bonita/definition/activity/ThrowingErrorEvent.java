/**
 * Copyright (C) 2010-2012 BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA  02110-1301, USA.
 **/
package org.ow2.bonita.definition.activity;

import org.ow2.bonita.facade.def.majorElement.ActivityDefinition;
import org.ow2.bonita.facade.runtime.impl.InternalProcessInstance;
import org.ow2.bonita.runtime.event.Job;
import org.ow2.bonita.runtime.model.Execution;
import org.ow2.bonita.services.EventService;
import org.ow2.bonita.services.Recorder;
import org.ow2.bonita.util.EnvTool;
import org.ow2.bonita.util.ProcessUtil;

/**
 * 
 * @author Matthieu Chaffotte
 *
 */
public class ThrowingErrorEvent extends AbstractActivity {

  private static final long serialVersionUID = -7250991226062578318L;

  protected ThrowingErrorEvent() {
    super();
  }

  public ThrowingErrorEvent(String eventName) {
    super(eventName);
  }

  @Override
  protected boolean bodyStartAutomatically() {
    return true;
  }

  @Override
  protected boolean executeBusinessLogic(Execution execution) {
	  final InternalProcessInstance instance = execution.getInstance();
	    final ActivityDefinition activity = execution.getNode();
	    Job errorJob = ActivityUtil.getErrorEventSubProcessJob(execution, activity.getTimerCondition());
	    if (errorJob == null) {
	      errorJob = ActivityUtil.getTargetErrorJob(execution);
	    }
	    execution.abort();
	    final Recorder recorder = EnvTool.getRecorder();
	    recorder.recordInstanceAborted(instance.getUUID(), EnvTool.getUserId());
	    if (errorJob != null) {
	      final EventService eventService = EnvTool.getEventService();
	      eventService.storeJob(errorJob);
	    } else {
	      ProcessUtil.removeInternalInstanceEvents(instance.getUUID());
	      instance.finish();
	    }
	    return false;
  }

}
