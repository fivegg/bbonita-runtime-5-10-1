/**
 * Copyright (C) 2007  Bull S. A. S.
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
 **/
package org.ow2.bonita.env.operation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ow2.bonita.env.Context;
import org.ow2.bonita.env.Descriptor;
import org.ow2.bonita.env.Environment;
import org.ow2.bonita.env.WireContext;
import org.ow2.bonita.env.WireDefinition;
import org.ow2.bonita.env.WireException;
import org.ow2.bonita.env.descriptor.ArgDescriptor;
import org.ow2.bonita.util.ExceptionManager;
import org.ow2.bonita.util.FilterListener;
import org.ow2.bonita.util.Listener;
import org.ow2.bonita.util.Observable;

/**
 * subscribes to an {@link Observable observable}.
 * 
 * <p>
 * The target object can be a {@link Listener} or a specific method to call can
 * be specified (by {@link #setMethodName(String)})
 * </p>
 * 
 * <p>
 * The event can be filtered by specifying a {@link Context} (with
 * {@link #setContextName(String)}), objects to observe (with
 * {@link #setObjectNames(List)}) and events to observe (with
 * {@link #setEventNames(List)}). If the objects or events are not specified,
 * then all objects and events are observed.
 * </p>
 * 
 * <p>
 * The {@link #setWireEvents(boolean)} specifies if the object or the
 * {@link Descriptor} events should be observed.
 * </p>
 * 
 * @author Tom Baeyens
 * @author Guillaume Porcher (documentation)
 * 
 */
public class SubscribeOperation implements Operation {

  private static final long serialVersionUID = 1L;
  private static final Logger LOG = Logger.getLogger(SubscribeOperation.class.getName());

  String contextName = null;
  List<String> eventNames = null;

  boolean wireEvents = false;
  List<String> objectNames = null;
  String methodName = null;
  List<ArgDescriptor> argDescriptors = null;

  public void apply(Object target, WireContext targetWireContext) {
    Listener listener = null;

    // if a method has to be invoked, rather then using the observable interface
    if (methodName != null) {
      listener = new MethodInvokerListener(methodName, argDescriptors,
          targetWireContext, target);
    } else {
      try {
        listener = (Listener) target;
      } catch (ClassCastException e) {
      	String message = ExceptionManager.getInstance().getFullMessage(
      			"bp_SO_1", target);
        throw new WireException(message);
      }
    }

    // if there is a filter specified on the event names
    if ((eventNames != null) && (!eventNames.isEmpty())) {
      listener = new FilterListener(listener, eventNames);
    }

    // identify the wireContext
    WireContext wireContext = null;
    if (contextName != null) {
      Environment environment = Environment.getCurrent();
      if (environment != null) {
        try {
          wireContext = (WireContext) environment.getContext(contextName);
          if (wireContext == null) {
          	String message = ExceptionManager.getInstance().getFullMessage(
          			"bp_SO_2", contextName);
            throw new WireException(message);
          }
        } catch (ClassCastException e) {
        	String message = ExceptionManager.getInstance().getFullMessage(
        			"bp_SO_3", contextName);
          throw new WireException(message, e);
        }
      } else {
      	String message = ExceptionManager.getInstance().getFullMessage(
      			"bp_SO_4", contextName, targetWireContext);
        throw new WireException(message);
      }
    } else {
      wireContext = targetWireContext;
    }

    if (wireEvents) {
      WireDefinition wireDefinition = wireContext.getWireDefinition();

      // if there are objectNames specified
      if (objectNames != null) {
        // subscribe to the descriptors for the all objectNames
        for (String objectName : objectNames) {
          Descriptor descriptor = wireDefinition.getDescriptor(objectName);
          subscribe(listener, descriptor);
        }

        // if no objectNames are specified, subscribe to all the descriptors
      } else {
        Set<Descriptor> descriptors = new HashSet<Descriptor>(wireDefinition
            .getDescriptors().values());
        for (Descriptor descriptor : descriptors) {
          subscribe(listener, descriptor);
        }
      }

    } else if ((objectNames != null) && (!objectNames.isEmpty())) {
      // for every objectName
      for (String objectName : objectNames) {
        // subscribe to the objects themselves
        Object object = wireContext.get(objectName);
        if (object == null) {
        	String message = ExceptionManager.getInstance().getFullMessage(
        			"bp_SO_5",  wireContext.getName(), objectName);
          throw new WireException(message);
        }
        if (!(object instanceof Observable)) {
        	String message = ExceptionManager.getInstance().getFullMessage(
        			"bp_SO_6",  wireContext.getName(), objectName, object.getClass().getName(), Observable.class.getName());
          throw new WireException(message);
        }
        subscribe(listener, (Observable) object);
      }

    } else {
      // subscribe to the context
      subscribe(listener, wireContext);
    }
  }

  void subscribe(Listener listener, Observable observable) {
  	if (LOG.isLoggable(Level.FINE)) {
      LOG.fine("adding " + listener + " as listener to " + observable);
  	}
    observable.addListener(listener);
  }

  /**
   * Gets the list of argDescriptor used to create the arguments given to the
   * method (only if a specific method has to be called).
   */
  public List<ArgDescriptor> getArgDescriptors() {
    return argDescriptors;
  }

  /**
   * Sets the list of argDescriptor used to create the arguments given to the
   * method.
   */
  public void setArgDescriptors(List<ArgDescriptor> argDescriptors) {
    this.argDescriptors = argDescriptors;
  }

  /**
   * Gets the list of events to listen to.
   */
  public List<String> getEventNames() {
    return eventNames;
  }

  /**
   * Sets the list of events to listen to.
   */
  public void setEventNames(List<String> eventNames) {
    this.eventNames = eventNames;
  }

  /**
   * Gets the name of the method to invoke when an event is received.
   */
  public String getMethodName() {
    return methodName;
  }

  /**
   * Sets the name of the method to invoke when an event is received.
   */
  public void setMethodName(String methodName) {
    this.methodName = methodName;
  }

  /**
   * Gets the name of the WireContext where the Observable should be found.
   */
  public String getContextName() {
    return contextName;
  }

  /**
   * Sets the name of the WireContext where the Observable should be found.
   */
  public void setContextName(String contextName) {
    this.contextName = contextName;
  }

  /**
   * Gets the list of name of the Observable objects to observe.
   */
  public List<String> getObjectNames() {
    return objectNames;
  }

  /**
   * Sets the list of name of the Observable objects to observe.
   */
  public void setObjectNames(List<String> objectNames) {
    this.objectNames = objectNames;
  }

  /**
   * <p>
   * <code>true</code> if the target object will listen to Descriptor related
   * events.
   * </p>
   * <p>
   * <code>false</code> if the target object will listen to the object instance
   * events.
   * </p>
   */
  public boolean isWireEvents() {
    return wireEvents;
  }

  /**
   * Sets if the object should listen to descriptor events or to events fired by
   * the named object.
   * <p>
   * <code>true</code> if the target object will listen to Descriptor related
   * events.
   * </p>
   * <p>
   * <code>false</code> if the target object will listen to the object instance
   * events.
   * </p>
   */
  public void setWireEvents(boolean wireEvents) {
    this.wireEvents = wireEvents;
  }
}
