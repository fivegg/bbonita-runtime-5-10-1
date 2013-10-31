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
package org.ow2.bonita.runtime.event;

import java.util.logging.Logger;

/**
 * 
 * @author Charles Souillard, Matthieu Chaffotte
 * 
 */
public class EventCouple {

  static final Logger LOG = Logger.getLogger(EventCouple.class.getName());

  private IncomingEventInstance incoming;
  private OutgoingEventInstance outgoing;

  public EventCouple() {
  }

  public void setIncoming(final IncomingEventInstance incoming) {
    this.incoming = incoming;
  }

  public void setOutgoing(final OutgoingEventInstance outgoing) {
    this.outgoing = outgoing;
  }

  public IncomingEventInstance getIncoming() {
    return incoming;
  }

  public OutgoingEventInstance getOutgoing() {
    return outgoing;
  }

  @Override
  public String toString() {
    return "EventCouple [incoming=" + incoming + ", outgoing=" + outgoing + "]";
  }
}
