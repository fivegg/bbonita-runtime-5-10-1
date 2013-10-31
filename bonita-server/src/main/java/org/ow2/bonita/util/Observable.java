/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.ow2.bonita.util;

import java.util.List;

/**
 * dispatches events to which {@link Listener listeners} can subscribe. Aka
 * publish-subscribe.
 * 
 * @see DefaultObservable a default implementation of this interface
 * @author Tom Baeyens
 */
public interface Observable {

  /**
   * subscribes a listener to every event
   * 
   * @param listener
   *          is the object that will be notified on
   *          {@link #fire(String, Object) firing} of events.
   */
  void addListener(Listener listener);

  /**
   * removes a listener that was subscribed for every event
   */
  void removeListener(Listener listener);

  /**
   * subscribes the listener to receive event notifications only of the given
   * eventName. Events with different eventNames will not be dispatched to the
   * given listener.
   * 
   * @param listener
   *          is the object that will be notified on
   *          {@link #fire(String, Object) firing} of events.
   * @param eventName
   *          is the type of events the listener is interested in and this is
   *          mandatory.
   * @return the {@link FilterListener} that is created as a wrapper for the
   *         given listener. That handle might be necessary to remove the
   *         listener later on.
   * @throws NullPointerException
   *           in case listener or eventName is null.
   */
  Listener addListener(Listener listener, String eventName);

  /**
   * subscribes the listener to receive event notifications only if event
   * matches one of the given eventNames. Events with different eventNames will
   * not be dispatched to the given listener.
   * 
   * @param listener
   *          is the object that will be notified on
   *          {@link #fire(String, Object) firing} of events.
   * @param eventNames
   *          is the type of events the listener is interested in and this is
   *          mandatory.
   * @return the {@link FilterListener} that is created as a wrapper for the
   *         given listener. That handle might be necessary to remove the
   *         listener later on.
   * @throws NullPointerException
   *           in case listener or eventName is null.
   */
  Listener addListener(Listener listener, List<String> eventNames);

  /**
   * dispatches an event to the listeners.
   * 
   * @param eventName
   *          identifies the type of event and is allowed to be null.
   */
  void fire(String eventName);

  /**
   * dispatches an event to the listeners.
   * 
   * @param eventName
   *          identifies the type of event and is allowed to be null.
   * @param info
   *          is the optional information that the observable wants to pass to
   *          it's listeners. Each observable should indicate which type of info
   *          it's passing for each event.
   */
  void fire(String eventName, Object info);
}
