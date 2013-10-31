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
package org.ow2.bonita.facade.def.majorElement.impl;

import org.ow2.bonita.facade.def.majorElement.TransitionDefinition;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.TransitionDefinitionUUID;

public class TransitionDefinitionImpl extends ProcessElementImpl implements TransitionDefinition {

  private static final long serialVersionUID = 3286234222544905107L;
  protected TransitionDefinitionUUID uuid;
  protected String condition;
  protected String from;
  protected String fromBoundaryEvent;
  protected String to;
  protected boolean isDefault;

  protected TransitionDefinitionImpl() {
    super();
  }

  public TransitionDefinitionImpl(final ProcessDefinitionUUID processUUID, final String name, final String from,
      final String to) {
    super(name, processUUID);
    uuid = new TransitionDefinitionUUID(processUUID, name);
    this.from = from;
    this.to = to;
  }

  public TransitionDefinitionImpl(final TransitionDefinition src) {
    super(src);
    uuid = src.getUUID();
    condition = src.getCondition();
    from = src.getFrom();
    fromBoundaryEvent = src.getFromBoundaryEvent();
    to = src.getTo();
    isDefault = src.isDefault();
  }

  @Override
  public String toString() {
    return getUUID().toString();
  }

  @Override
  public String getCondition() {
    return condition;
  }

  @Override
  public String getFrom() {
    return from;
  }

  @Override
  public String getTo() {
    return to;
  }

  @Override
  public boolean isDefault() {
    return isDefault;
  }

  @Override
  public TransitionDefinitionUUID getUUID() {
    return uuid;
  }

  @Override
  public String getFromBoundaryEvent() {
    return fromBoundaryEvent;
  }

  public void setCondition(final String condition) {
    this.condition = condition;
  }

  public void setDefault(final boolean isDefault) {
    this.isDefault = isDefault;
  }

  public void setFromBoundaryEvent(final String fromBoundaryEvent) {
    this.fromBoundaryEvent = fromBoundaryEvent;
  }

}
