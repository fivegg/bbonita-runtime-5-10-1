/**
 * Copyright (C) 2010-2013  BonitaSoft S.A.
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

import org.ow2.bonita.definition.activity.ExternalActivity;
import org.ow2.bonita.facade.def.element.ConnectorDefinition;
import org.ow2.bonita.facade.def.element.DeadlineDefinition;
import org.ow2.bonita.facade.def.element.OutgoingEventDefinition;
import org.ow2.bonita.facade.def.element.impl.IncomingEventDefinitionImpl;
import org.ow2.bonita.facade.def.majorElement.ActivityDefinition;
import org.ow2.bonita.facade.def.majorElement.DataFieldDefinition;
import org.ow2.bonita.facade.def.majorElement.impl.ActivityDefinitionImpl;
import org.ow2.bonita.runtime.model.ObjectReference;
import org.ow2.bonita.util.BonitaRuntimeException;
import org.ow2.bonita.util.ExceptionManager;

/**
 * 
 * @author Matthieu Chaffotte
 * 
 */
public class InternalActivityDefinition extends ActivityDefinitionImpl {

  private static final long serialVersionUID = 7575413369114996514L;

  public ObjectReference<ExternalActivity> behaviourReference;

  // mandatory for hibernate
  protected InternalActivityDefinition() {
    super();
  }

  public InternalActivityDefinition(final ActivityDefinition activity, final ExternalActivity behaviour) {
    super(activity);
    behaviourReference = new ObjectReference<ExternalActivity>(behaviour);

    deadlines = null;
    for (final DeadlineDefinition deadline : activity.getDeadlines()) {
      addDeadline(new InternalConnectorDefinition(deadline, activity.getProcessDefinitionUUID()));
    }

    dataFields = null;
    for (final DataFieldDefinition dataField : activity.getDataFields()) {
      addData(new InternalDataFieldDefinition(dataField, activity.getProcessDefinitionUUID()));
    }

    connectors = null;
    for (final ConnectorDefinition connector : activity.getConnectors()) {
      addConnector(new InternalConnectorDefinition(connector, activity.getProcessDefinitionUUID()));
    }

    if (activity.getIncomingEvent() != null) {
      incomingEvent = new IncomingEventDefinitionImpl(activity.getIncomingEvent());
      for (final ConnectorDefinition connector : activity.getIncomingEvent().getConnectors()) {
        ((IncomingEventDefinitionImpl) incomingEvent).addConnector(new InternalConnectorDefinition(connector, activity
            .getProcessDefinitionUUID()));
      }
    }

    outgoingEvents = null;
    for (final OutgoingEventDefinition outgoing : activity.getOutgoingEvents()) {
      addOutgoingEvent(new InternalOutgoingEventDefinition(outgoing, activity.getProcessDefinitionUUID()));
    }

    if (activity.getMultiInstantiationDefinition() != null) {
      setMultiInstanciation(new InternalConnectorDefinition(activity.getMultiInstantiationDefinition(),
          activity.getProcessDefinitionUUID()));
    }
    if (activity.getFilter() != null) {
      setFilter(new InternalConnectorDefinition(activity.getFilter(), activity.getProcessDefinitionUUID()));
    }
    if (activity.getMultipleInstancesInstantiator() != null) {
      setMultipleInstancesInstantiator(new InternalConnectorDefinition(activity.getMultipleInstancesInstantiator(),
          activity.getProcessDefinitionUUID()));
    }
    if (activity.getMultipleInstancesJoinChecker() != null) {
      setMultipleInstancesJoinChecker(new InternalConnectorDefinition(activity.getMultipleInstancesJoinChecker(),
          activity.getProcessDefinitionUUID()));
    }

  }

  public ExternalActivity getBehaviour() {
    final ExternalActivity behaviour = behaviourReference != null ? behaviourReference.get() : null;
    if (behaviour == null) {
      final String message = ExceptionManager.getInstance().getFullMessage("bp_NI_1", this);
      throw new BonitaRuntimeException(message);
    }
    return behaviour;
  }

}
