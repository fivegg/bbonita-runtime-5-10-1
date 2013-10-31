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

import java.util.ArrayList;
import java.util.List;

/**
 * default implementation of the {@link Observable} interface.
 * 
 * @author Tom Baeyens
 */
public class DefaultObservable implements Observable {

  protected List<Listener> listeners = null;

  public void addListener(Listener listener) {
    if (listener == null) {
    	String message = ExceptionManager.getInstance().getFullMessage("bp_DO_1");
      throw new BonitaRuntimeException(message);
    }
    if (listeners == null) {
      listeners = new ArrayList<Listener>();
    }
    listeners.add(listener);
  }

  public void removeListener(Listener listener) {
    if (listener == null) {
    	String message = ExceptionManager.getInstance().getFullMessage("bp_DO_2");
      throw new BonitaRuntimeException(message);
    }
    if (listeners != null) {
      listeners.remove(listener);
    }
  }

  public Listener addListener(Listener listener, String eventName) {
    if (eventName == null) {
    	String message = ExceptionManager.getInstance().getFullMessage("bp_DO_3");
      throw new BonitaRuntimeException(message);
    }

    List<String> eventNames = new ArrayList<String>();
    eventNames.add(eventName);

    return addListener(listener, eventNames);
  }

  public Listener addListener(Listener listener, List<String> eventNames) {
    if (listener == null) {
    	String message = ExceptionManager.getInstance().getFullMessage("bp_DO_4");
      throw new BonitaRuntimeException(message);
    }
    if (eventNames == null) {
    	String message = ExceptionManager.getInstance().getFullMessage("bp_DO_5");
      throw new BonitaRuntimeException(message);
    }
    FilterListener filterListener = new FilterListener(listener, eventNames);
    addListener(filterListener);
    return filterListener;
  }

  public void fire(String eventName) {
    fire(eventName, null);
  }

  public void fire(String eventName, Object info) {
    if (listeners != null) {
      for (Listener listener : listeners) {
        listener.event(this, eventName, info);
      }
    }
  }

  public List<Listener> getListeners() {
    return listeners;
  }
}
