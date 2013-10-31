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

import org.ow2.bonita.facade.def.element.BoundaryEvent;
import org.ow2.bonita.facade.def.majorElement.TransitionDefinition;
import org.ow2.bonita.facade.def.majorElement.impl.TransitionDefinitionImpl;
import org.ow2.bonita.facade.uuid.ActivityDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;

/**
 * Engine implementation of a boundary event.
 * 
 * @author Matthieu Chaffotte
 * 
 */
public abstract class BoundaryEventImpl extends EventImpl implements BoundaryEvent {

  private static final long serialVersionUID = 1440528386603185867L;

  protected TransitionDefinition exceptionTransition;

  public BoundaryEventImpl() {
    super();
  }

  public BoundaryEventImpl(final String eventName, final ProcessDefinitionUUID processUUID,
      final ActivityDefinitionUUID activityUUID, final TransitionDefinition exceptionTransition) {
    super(eventName, processUUID, activityUUID);
    this.exceptionTransition = exceptionTransition;
  }

  public BoundaryEventImpl(final BoundaryEvent src) {
    super(src);
    exceptionTransition = new TransitionDefinitionImpl(src.getTransition());
  }

  @Override
  public TransitionDefinition getTransition() {
    return exceptionTransition;
  }

  public void setExceptionTransition(final TransitionDefinition transition) {
    exceptionTransition = transition;
  }

}
