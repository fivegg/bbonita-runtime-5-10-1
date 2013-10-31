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
package org.ow2.bonita.env;

import java.io.Serializable;

import org.ow2.bonita.util.Observable;

/**
 * knows how an object can be created.
 * 
 * <p>
 * A {@link WireDefinition} contains a map of {@link Descriptor}s. They are used
 * by the {@link WireContext} that will build and cache the objects.
 * </p>
 * 
 * @author Tom Baeyens
 * @author Guillaume Porcher (documentation)
 * 
 */
public interface Descriptor extends Observable, Serializable {

  /**
   * is fired when construction of a wired object starts. The provided event is
   * a {@link WireObjectEventInfo}.
   * 
   * @see WireContext
   */
  String EVENT_CONSTRUCTING = "constructing";
  /**
   * is fired when initialization of a wired object starts (after construction).
   * The provided event is a {@link WireObjectEventInfo}.
   * 
   * @see WireContext
   */
  String EVENT_INITIALIZING = "initializing";
  /**
   * is fired when object construction is completed. The provided event is a
   * {@link WireObjectEventInfo}.
   * 
   * @see WireContext
   */
  String EVENT_CONSTRUCTED = "constructed";
  /**
   * is fired when an object is being set into the cache of this wire context
   * with {@link WireContext#set(String, Object)}. The provided event is a
   * {@link WireObjectEventInfo}.
   * 
   * @see WireContext
   */
  String EVENT_SET = "set";
  /**
   * is fired when an object is being removed from the cache of the wire context
   * with {@link WireContext#remove(String)}. The provided event is a
   * {@link WireObjectEventInfo}.
   * 
   * @see WireContext
   */
  String EVENT_REMOVE = "remove";

  /** the name of this descriptor */
  String getName();

  /** the type of the produced object or null if that is not available */
  Class<?> getType(WireDefinition wireDefinition);

  /**
   * if eager initialization is set, it means that an object must be created
   * immediately during construction of the {@link WireContext}.
   */
  boolean isEagerInit();

  /**
   * constructs the object.
   * 
   * @param wireContext
   *          {@link WireContext} in which the object is created. This is also
   *          the {@link WireContext} where the object will search for other
   *          object that may be needed during the initialization phase.
   * @return the constructed object.
   */
  Object construct(WireContext wireContext);

  /**
   * Checks if the initialization should be done during the construction phase
   * or if it can be done later.
   * 
   * @return <code>false</code> if the initialization should be done during the
   *         creation phase, <code>true</code> if it can be done later.
   */
  boolean isDelayable();

  /**
   * called by the WireContext to initialize the specified object. For more
   * information about initialization, see {@link WireContext} section
   * lifecycle.
   * 
   * @param object
   *          object to initialize.
   * @param wireContext
   *          the context in which the object will be initialized.
   */
  void initialize(Object object, WireContext wireContext);
}
