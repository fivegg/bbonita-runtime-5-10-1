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
package org.ow2.bonita.facade.def.element.impl;

import org.ow2.bonita.facade.def.element.SignalEvent;
import org.ow2.bonita.facade.def.majorElement.TransitionDefinition;
import org.ow2.bonita.facade.uuid.ActivityDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;

/**
 * Engine implementation of a signal boundary event.
 * 
 * @author Matthieu Chaffotte
 * 
 */
public class SignalBoundaryEventImpl extends BoundaryEventImpl implements SignalEvent {

  private static final long serialVersionUID = -1775037491326816797L;

  protected String signalCode;

  protected SignalBoundaryEventImpl() {
    super();
  }

  public SignalBoundaryEventImpl(final String eventName, final ProcessDefinitionUUID processUUID,
      final ActivityDefinitionUUID activityUUID, final TransitionDefinition exceptionTransition, final String signalCode) {
    super(eventName, processUUID, activityUUID, exceptionTransition);
    this.signalCode = signalCode;
  }

  public SignalBoundaryEventImpl(final SignalBoundaryEventImpl src) {
    super(src);
    signalCode = src.getSignalCode();
  }

  @Override
  public String getSignalCode() {
    return signalCode;
  }

}
