/**
 * Copyright (C) 2009-2012 BonitaSoft S.A.
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

import java.util.Collections;
import java.util.Map;

import org.ow2.bonita.facade.def.element.OutgoingEventDefinition;

/**
 * 
 * @author Matthieu Chaffotte
 *
 */
public class OutgoingEventDefinitionImpl extends EventDefinitionImpl implements OutgoingEventDefinition {

  private static final long serialVersionUID = -2408886793315753184L;

  protected Map<String, Object> clientParameters;

  protected String toProcessName;

  protected String toActivityName;

  protected long timeToLive;

  public OutgoingEventDefinitionImpl() {
    super();
  }

  public OutgoingEventDefinitionImpl(final String eventName, final String toProcessName, final String toActivityName, final Map<String, Object> parameters, final long timeToLive) {
    super(eventName);
    this.toProcessName = toProcessName;
    this.toActivityName = toActivityName;
    this.clientParameters = parameters;
    this.timeToLive = timeToLive;
  }

  public OutgoingEventDefinitionImpl(final OutgoingEventDefinition src) {
    super(src);
    this.toProcessName = src.getToProcessName();
    this.toActivityName = src.getToActivityName();
    this.clientParameters = src.getParameters();
    this.timeToLive = src.getTimeToLive();
  }

  public Map<String, Object> getParameters() {
    if (getClientParameters() != null) {
      return getClientParameters();
    }
    return Collections.emptyMap();
  }

	public String getToActivityName() {
		return toActivityName;
	}

	public String getToProcessName() {
		return toProcessName;
	}
  
  public Map<String, Object> getClientParameters() {
    return clientParameters;
  }

  public long getTimeToLive() {
    return timeToLive;
  }

}
