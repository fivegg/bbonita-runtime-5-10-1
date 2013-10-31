/**
 * Copyright (C) 2010  BonitaSoft S.A.
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

import org.ow2.bonita.facade.def.element.Event;
import org.ow2.bonita.facade.def.majorElement.impl.ProcessElementImpl;
import org.ow2.bonita.facade.uuid.ActivityDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;

/**
 * Engine implementation of an event.
 * 
 * @author Matthieu Chaffotte
 * 
 */
public abstract class EventImpl extends ProcessElementImpl implements Event {

  private static final long serialVersionUID = 6573426169759017444L;
  protected ActivityDefinitionUUID activityDefinitionUUID;

  public EventImpl() {
    super();
  }

  public EventImpl(final String eventName, final ProcessDefinitionUUID processUUID,
      final ActivityDefinitionUUID activityUUID) {
    super(eventName, processUUID);
    activityDefinitionUUID = activityUUID;
  }

  public EventImpl(final Event src) {
    super(src);
    activityDefinitionUUID = new ActivityDefinitionUUID(src.getActivityDefinitionUUID());
  }

  @Override
  public ActivityDefinitionUUID getActivityDefinitionUUID() {
    return activityDefinitionUUID;
  }

}
