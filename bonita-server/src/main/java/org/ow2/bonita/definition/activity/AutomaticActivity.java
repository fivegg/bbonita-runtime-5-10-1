/**
 * Copyright (C) 2006  Bull S. A. S.
 * Bull, Rue Jean Jaures, B.P.68, 78340, Les Clayes-sous-Bois
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA  02110-1301, USA.
 * 
 * Modified by Matthieu Chaffotte - BonitaSoft S.A.
 **/
package org.ow2.bonita.definition.activity;

import java.util.Iterator;
import java.util.List;

import org.ow2.bonita.facade.def.InternalActivityDefinition;
import org.ow2.bonita.facade.def.element.HookDefinition;
import org.ow2.bonita.facade.def.element.HookDefinition.Event;
import org.ow2.bonita.facade.runtime.ActivityInstance;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.runtime.event.Job;
import org.ow2.bonita.runtime.event.JobBuilder;
import org.ow2.bonita.runtime.model.Execution;
import org.ow2.bonita.services.EventService;
import org.ow2.bonita.util.BonitaConstants;
import org.ow2.bonita.util.EnvTool;

public class AutomaticActivity extends AbstractActivity {

  private static final long serialVersionUID = -6392840287035678094L;

  protected AutomaticActivity() {
    super();
  }

  public AutomaticActivity(final String activityName) {
    super(activityName);
  }

  @Override
  protected boolean executeBusinessLogic(final Execution execution) {
    final InternalActivityDefinition activityDefinition = execution.getNode();
    final ActivityInstance activity = execution.getActivityInstance();
    if (activityDefinition.isAsynchronous()) {
      if (hasConnectorsAutomaticOnEnter(activityDefinition)) {
        if (!ActivityState.ABORTED.equals(activity.getState())) {
          final EventService eventService = EnvTool.getEventService();
          final String eventName = BonitaConstants.CONNECTOR_AUTOMATIC_ON_ENTER_PREFIX + activity.getUUID();
          final Job job = JobBuilder.connectorsAutomaticOnEnterJob(eventName, execution.getInstance()
              .getRootInstanceUUID(), execution.getEventUUID(), execution.getInstance().getProcessInstanceUUID());
          eventService.storeJob(job);
        }
        // if it has connector OnEnter the execution must stop
        return false;
      }
    } else {
      ConnectorExecutor.executeConnectors(activityDefinition, execution, Event.automaticOnEnter);
    }
    ConnectorExecutor.executeConnectors(activityDefinition, execution, Event.automaticOnExit);
    return !ActivityState.ABORTED.equals(activity.getState());

  }

  private boolean hasConnectorsAutomaticOnEnter(final InternalActivityDefinition activityDefinition) {
    final List<HookDefinition> connectors = activityDefinition.getConnectors();
    final Iterator<HookDefinition> iterator = connectors.iterator();
    boolean hasConnectorsOnEnter = false;
    while (!hasConnectorsOnEnter && iterator.hasNext()) {
      final HookDefinition connector = iterator.next();
      if (Event.automaticOnEnter.equals(connector.getEvent())) {
        hasConnectorsOnEnter = true;
      }
    }
    return hasConnectorsOnEnter;
  }

  @Override
  protected boolean bodyStartAutomatically() {
    return true;
  }

}
