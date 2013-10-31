/**
 * Copyright (C) 2009-2013 BonitaSoft S.A.
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
package org.ow2.bonita.runtime.event;

import java.io.Serializable;
import java.util.logging.Logger;

/**
 * 
 * @author Charles Souillard, Matthieu Chaffotte
 * 
 */
public class EventCoupleId implements Serializable {

  private static final long serialVersionUID = -511272148395080126L;

  static final Logger LOG = Logger.getLogger(EventCoupleId.class.getName());

  private long incoming;
  private long outgoing;

  public EventCoupleId() {
    super();
  }

  public EventCoupleId(final long incoming, final long outgoing) {
    this.incoming = incoming;
    this.outgoing = outgoing;
  }

  public long getIncoming() {
    return incoming;
  }

  public long getOutgoing() {
    return outgoing;
  }

  public void setIncoming(final long incoming) {
    this.incoming = incoming;
  }

  public void setOutgoing(final long outgoing) {
    this.outgoing = outgoing;
  }

  @Override
  public String toString() {
    final StringBuilder builder = new StringBuilder();
    builder.append("EventCoupleId [incoming=");
    builder.append(incoming);
    builder.append(", outgoing=");
    builder.append(outgoing);
    builder.append("]");
    return builder.toString();
  }

}
