/**
 * Copyright (C) 2010-2013 BonitaSoft S.A.
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
package org.ow2.bonita.facade.def;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.ow2.bonita.definition.activity.AbstractActivity;
import org.ow2.bonita.definition.activity.AutomaticActivity;
import org.ow2.bonita.definition.activity.CatchingErrorEvent;
import org.ow2.bonita.definition.activity.CatchingSignalEvent;
import org.ow2.bonita.definition.activity.ExternalActivity;
import org.ow2.bonita.definition.activity.ReceiveEvent;
import org.ow2.bonita.definition.activity.SendEvents;
import org.ow2.bonita.definition.activity.SubFlow;
import org.ow2.bonita.definition.activity.Task;
import org.ow2.bonita.definition.activity.ThrowingErrorEvent;
import org.ow2.bonita.definition.activity.ThrowingSignalEvent;
import org.ow2.bonita.definition.activity.Timer;
import org.ow2.bonita.facade.def.element.ConnectorDefinition;
import org.ow2.bonita.facade.def.majorElement.ActivityDefinition;
import org.ow2.bonita.facade.def.majorElement.DataFieldDefinition;
import org.ow2.bonita.facade.def.majorElement.ParticipantDefinition;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.def.majorElement.impl.ParticipantDefinitionImpl;
import org.ow2.bonita.facade.def.majorElement.impl.ProcessDefinitionImpl;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.util.EnvTool;

/**
 * 
 * @author Matthieu Chaffotte
 * 
 */
public class InternalProcessDefinition extends ProcessDefinitionImpl {

  private static final long serialVersionUID = 7746264410679050386L;

  protected long dbid;

  protected String labelOrName;

  protected int nbOfAttachments;

  // mandatory for hibernate
  protected InternalProcessDefinition() {
    super();
  }

  public InternalProcessDefinition(final ProcessDefinition process) {
    super(process);
    nbOfAttachments = 0;
    activities = new HashSet<ActivityDefinition>();
    for (final ActivityDefinition activity : process.getActivities()) {
      activities.add(new InternalActivityDefinition(activity, createBehaviour(activity)));
    }
    if (process.getLabel() != null) {
      labelOrName = process.getLabel();
    } else {
      labelOrName = process.getName();
    }
    connectors = null;
    for (final ConnectorDefinition connector : process.getConnectors()) {
      addConnector(new InternalConnectorDefinition(connector, process.getUUID()));
    }

    dataFields = null;
    for (final DataFieldDefinition dataField : process.getDataFields()) {
      addData(new InternalDataFieldDefinition(dataField, process.getUUID()));
    }

    for (final ParticipantDefinition participant : getParticipants()) {
      if (participant.getRoleMapper() != null) {
        ((ParticipantDefinitionImpl) participant).setResolver(new InternalConnectorDefinition(participant
            .getRoleMapper(), process.getUUID()));
      }
    }
  }

  public Long getDbid() {
    return dbid;
  }

  public String getLabelOrName() {
    return labelOrName;
  }

  @Override
  public InternalActivityDefinition getActivity(final String name) {
    return (InternalActivityDefinition) super.getActivity(name);
  }

  private static ExternalActivity createBehaviour(final ActivityDefinition activity) {
    final String activityName = activity.getName();
    AbstractActivity abstractActivity = null;
    if (activity.isSubflow()) {
      abstractActivity = new SubFlow(activityName);
    } else if (activity.isTimer()) {
      abstractActivity = new Timer(activityName);
    } else if (activity.isAutomatic()) {
      abstractActivity = new AutomaticActivity(activityName);
    } else if (activity.isTask()) {
      abstractActivity = new Task(activityName);
    } else if (activity.isSendEvents()) {
      abstractActivity = new SendEvents(activityName);
    } else if (activity.isReceiveEvent()) {
      abstractActivity = new ReceiveEvent(activityName);
    } else if (activity.isThrowingErrorEvent()) {
      abstractActivity = new ThrowingErrorEvent(activityName);
    } else if (activity.isThrowingSignalEvent()) {
      abstractActivity = new ThrowingSignalEvent(activityName);
    } else if (activity.isCatchingSignalEvent()) {
      abstractActivity = new CatchingSignalEvent(activityName);
    } else if (activity.isCatchingErrorEvent()) {
      abstractActivity = new CatchingErrorEvent(activityName);
    }
    return abstractActivity;
  }

  @Override
  protected ClassLoader getClassLoader(final ProcessDefinitionUUID processUUID) {
    return EnvTool.getClassDataLoader().getProcessClassLoader(processUUID);
  }

  public Map<String, InternalActivityDefinition> getInternalInitialActivities() {
    final Map<String, InternalActivityDefinition> result = new HashMap<String, InternalActivityDefinition>();
    for (final ActivityDefinition activity : getInitialActivities().values()) {
      result.put(activity.getName(), (InternalActivityDefinition) activity);
    }
    return result;
  }

  public int getNbOfAttachments() {
    return nbOfAttachments;
  }

  public void setNbOfAttachments(final int nbOfAttachments) {
    this.nbOfAttachments = nbOfAttachments;
  }

}
