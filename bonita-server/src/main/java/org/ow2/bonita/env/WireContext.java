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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ow2.bonita.definition.activity.ExternalActivity;
import org.ow2.bonita.env.descriptor.AbstractDescriptor;
import org.ow2.bonita.env.descriptor.ObjectDescriptor;
import org.ow2.bonita.env.operation.FieldOperation;
import org.ow2.bonita.env.operation.InvokeOperation;
import org.ow2.bonita.env.operation.Operation;
import org.ow2.bonita.env.operation.PropertyOperation;
import org.ow2.bonita.env.operation.SubscribeOperation;
import org.ow2.bonita.env.xml.WireParser;
import org.ow2.bonita.util.Closable;
import org.ow2.bonita.util.DefaultObservable;
import org.ow2.bonita.util.ExceptionManager;
import org.ow2.bonita.util.Observable;

/**
 * object factory that creates, initializes, wires and caches objects based on {@link Descriptor descriptors} (aka IoC
 * container).
 * 
 * <h3>General principle</h3>
 * 
 * <p>
 * As input, a WireContext takes a {@link WireDefinition}. The WireDefinition contains named {@link Descriptor}s that
 * know how to create objects and wire them together. Each object has a name. The WireContext will maintain a cache
 * (map) of the created objects. So that upon subsequent requests, the same object can be given from the cache.
 * </p>
 * 
 * <center><img src="wirescope.gif"/></center>
 * 
 * <h3>Purpose</h3>
 * 
 * <p>
 * A WireContext is used often in combination with {@link Environment} to decouple the processDefinition virtual machine
 * from its environment. In the {@link PvmEnvironmentFactory}, both the environment-factory context and the environment
 * contexts are WireContexts. The PVM will use the persistence service, asynchronous message service, timer service and
 * other services through specified abstractions in the environment.
 * </p>
 * 
 * <p>
 * Another usage of the WireContext is construction and configuration of user code objects in a persistable way.
 * {@link ExternalActivity}s and {@link org.ow2.bonita.definition.activity.ExternalActivity} and other user code can be
 * instantiated with a WireContext. That way, they can be persisted in a fixed schema.
 * </p>
 * 
 * <h3>Xml</h3>
 * <p>
 * Mostly often, {@link Descriptor}s and WireContext's are not used directly. Instead, the wire XML is used in a
 * configuration file. The {@link WireParser wire XML parser} contains the documentation on the XML grammer. The
 * {@link WireParser} will produce a {@link WireDefinition} with a bunch of {@link Descriptor}s in it.
 * 
 * <h3 id="lifecycle">Object lifecycle</h3>
 * 
 * <p>
 * Objects are build in 2 phases: construction and initialization. The motivation for splitting these phases is to
 * resolve many of the circular dependencies. Imagine 2 objects that have a bidirectional reference. By splitting the
 * construction from the initialization phase, the objects can both be constructed first, and then during
 * initialization, they will be injected into each other.
 * </p>
 * 
 * <h3>Construction</h3>
 * <p>
 * Construction of the object is all that needs to be done until a reference to the object is available.
 * </p>
 * 
 * <p>
 * In the case of dynamically created objects ({@link ObjectDescriptor}), the simplest case this is accomplished with a
 * constructor. But also static or non-static factory methods can be used to obtain a reference to an object.
 * </p>
 * 
 * <p>
 * In case of immutable objects, the descriptor can just provide a reference to a singleton object.
 * </p>
 * 
 * <h3>Initialization</h3>
 * <p>
 * Initialization is optional and it is comprised of everything that needs to be done with an object after a reference
 * to the object is available. {@link AbstractDescriptor} contains an empty default initialization method.
 * </p>
 * 
 * <p>
 * For objects {@link ObjectDescriptor}s, this means that a a sequence of {@link Operation}s can be applied to the
 * object. Following operations implementations are already available and can be applied to an object during
 * initialization:
 * </p>
 * 
 * <ul>
 * <li><b>{@link FieldOperation}</b>: injects another object into a field</li>
 * <li><b>{@link PropertyOperation}</b>: injects another object with a setter method.</li>
 * <li><b>{@link InvokeOperation}</b>: invokes a method.</li>
 * <li><b>{@link SubscribeOperation}</b>: subscribes to an {@link Observable observable}.</li>
 * </ul>
 * 
 * <h3>Environment</h3>
 * 
 * <p>
 * When an environment is injected into a WireContext, lookup of all referenced object names will be done first in this
 * WireContext, but if the object name is not defined there, the environment will be searched in the environment's
 * default search order.
 * </p>
 * 
 * <h3>Events</h3>
 * <p>
 * Several objects will fire events to which can be subscribed:
 * </p>
 * 
 * <p>
 * The WireContext itself fires the {@link #EVENT_OPEN} and {@link #EVENT_OPEN} events.
 * </p>
 * 
 * <p>
 * The {@link Descriptor}s will fire the events {@link Descriptor#EVENT_CONSTRUCTING},
 * {@link Descriptor#EVENT_INITIALIZING}, {@link Descriptor#EVENT_CONSTRUCTED}, {@link Descriptor#EVENT_SET} and
 * {@link Descriptor#EVENT_REMOVE}.
 * </p>
 * 
 * <p>
 * And last but not least, the objects created by the WireContext can be {@link Observable} themselves.
 * </p>
 * 
 * <h3>Eager initialization</h3>
 * 
 * <p>
 * By default, all objects in a WireContext are lazily constructued and initialized. Eager initialization is specified
 * on a named object and it means that the object is constructed and initialized during construction of the WireContext.
 * You an only specify eager initialization when the object has a name.
 * </p>
 * 
 * <h3>Specifying how an object should be initialized.</h3>
 * 
 * <p>
 * The initialization can be specified with the {@link AbstractDescriptor#setInit(char)} method.
 * </p>
 * The possible value for <code>init</code> parameter is one of :
 * <ul>
 * <li>{@link AbstractDescriptor#INIT_LAZY}: for lazy creation and delayed initialization</li>
 * <li>{@link AbstractDescriptor#INIT_REQUIRED}: for lazy creation and immediate initialization</li>
 * <li>{@link AbstractDescriptor#INIT_EAGER}: for eager creation and delayed initialization</li>
 * <li>{@link AbstractDescriptor#INIT_IMMEDIATE}: for eager creation and immediate initialization</li>
 * </ul>
 * 
 * @author Tom Baeyens
 * @author Guillaume Porcher (documentation)
 */
public class WireContext extends DefaultObservable implements Context, Closable, Serializable {

  private static final long serialVersionUID = 1L;
  private static final Logger LOG = Logger.getLogger(WireContext.class.getName());

  // events ///////////////////////////////////////////////////////////////////

  /**
   * is fired when a new wiring environment is being opened. No event info provided.
   */
  public static final String EVENT_OPEN = "open";
  /**
   * is fired when the wiring environment is being closed. No event info provided.
   */
  public static final String EVENT_CLOSE = "close";

  // member fields ////////////////////////////////////////////////////////////

  protected String name = "wire-context";
  protected transient ClassLoader classLoader;
  protected WireDefinition wireDefinition;

  /** objects that are being instantiated or constructed */
  Set<String> underConstruction = null;

  /**
   * objects that are constructed, but waiting for the initialization operations (like e.g. injections) to be performed
   */
  Map<String, PendingInitialization> pendingInitializations = null;

  /**
   * objects on which the initialization operations (like e.g. injections) are being performed
   */
  Map<String, Object> underInitialization = null;

  /** fully created and initialized objects */
  Map<String, Object> cache = null;

  /** exceptions throw by descriptor invocations */
  Map<String, Exception> exceptions = null;

  public WireContext() {
    super();
  }

  public WireContext(final WireDefinition wireDefinition) {
    this(wireDefinition, null, false);
  }

  /**
   * when this {@link Context} is used in an {@link Environment}, it needs a name.
   */
  public WireContext(final WireDefinition wireDefinition, final String name) {
    super();
    this.wireDefinition = wireDefinition;
    this.name = name;
    classLoader = wireDefinition != null ? wireDefinition.getClassLoader() : null;
    create();
  }

  /**
   * allows for postponing the creation of this wire context.
   * 
   * @param delayCreate
   *          specifies if creation should be postponed till {@link #create()} is called explicitly. If delayCreate is
   *          set to false, creation is done as part of the constructor. If delayCreate is set to true, the
   *          {@link #create()} method needs to be called explicitly by the client after construction is complete. The
   *          use case is creation of environment where the transactionName needs to be set and the scope needs to be
   *          added to the environment before the creation of this wire scope is done.
   * @see PvmEnvironmentFactory#openEnvironment()
   */
  public WireContext(final WireDefinition wireDefinition, final String name, final boolean delayCreate) {
    super();
    this.wireDefinition = wireDefinition;
    this.name = name;
    classLoader = wireDefinition != null ? wireDefinition.getClassLoader() : null;

    if (!delayCreate) {
      create();
    }
  }

  /** convenience method that wires the object for a given descriptor. */
  public static Object create(final Descriptor descriptor) {
    final WireContext wireContext = new WireContext();
    return wireContext.create(descriptor, false);
  }

  /**
   * initializes the eager objects and then fires the create event. This method only needs to be called explicitly in
   * case <code>delayCreate</code> is true in {@link #WireContext(WireDefinition, String, Environment, boolean)}.
   */
  public void create() {
    if (LOG.isLoggable(Level.FINE)) {
      LOG.fine("creating " + name);
    }
    initializeEagerObjects();
    fire(EVENT_OPEN, null);
  }

  /**
   * Initializes all the eager objects defined in the {@link #wireDefinition}.
   */
  void initializeEagerObjects() {
    if (wireDefinition != null) {
      final List<String> eagerInitObjectNames = wireDefinition.getEagerInitNames();
      if (eagerInitObjectNames != null) {
        for (final String eagerInitObjectName : eagerInitObjectNames) {
          final Descriptor descriptor = wireDefinition.getDescriptor(eagerInitObjectName);
          if (descriptor.isEagerInit()) {
            if (LOG.isLoggable(Level.FINE)) {
              LOG.fine("eagerly initializing " + eagerInitObjectName);
            }
            get(eagerInitObjectName, descriptor.isDelayable());
          }
        }
        while (!hasObjectUnderConstruction() && !hasObjectUnderInitialization() && hasPendingInitializations()) {
          processPendingInitializations();
        }
      }
    }
  }

  @Override
  public String toString() {
    return name != null ? name : super.toString();
  }

  // environment methods
  // //////////////////////////////////////////////////////////

  /**
   * the list of object names defined in this context. This means the union of the object names that are defined in the
   * {@link #wireDefinition} and the objects that are just {@link #set(String, Object)}. If there are no keys, an empty
   * set will be returned.
   */
  @Override
  public Set<String> keys() {
    final Set<String> keys = new HashSet<String>();
    if (cache != null) {
      keys.addAll(cache.keySet());
    }
    if (wireDefinition != null) {
      final Map<String, Descriptor> descriptors = wireDefinition.getDescriptors();
      if (descriptors != null) {
        keys.addAll(descriptors.keySet());
      }
    }
    return keys;
  }

  /**
   * checks if the given objectName is defined, either by means of a descriptor or by an explicit
   * {@link #set(String, Object)}.
   */
  @Override
  public boolean has(final String objectName) {
    return hasCached(objectName) || (wireDefinition != null ? wireDefinition.hasDescriptor(objectName) : false);
  }

  /**
   * retrieves the object for the given objectName, ensuring it is constructed and initialized.
   * 
   * @return the object found, or null if the object was not found.
   */
  @Override
  public Object get(final String objectName) {
    return get(objectName, false);
  }

  /**
   * adds an object to this context, which means storing it in the cache. This doesn't have to be an object that is
   * defined by the {@link WireDefinition}. If an object is set under a certain objectName that also is associated with
   * a descriptor, the object provided in this set invocation will be delivered upon subsequent {@link #get(String)}
   * requests.
   * 
   * @return previous value of the object with the name objectName in the {@link #cache}
   * @throws WireException
   *           when the objectName is null
   */
  @Override
  public synchronized Object set(final String objectName, final Object object) {
    if (objectName == null) {
      final String message = ExceptionManager.getInstance().getFullMessage("bp_WC_1");
      throw new WireException(message);
    }
    if (cache == null) {
      cache = new HashMap<String, Object>();
    }
    fireObjectEvent(Descriptor.EVENT_SET, objectName, object);
    return cache.put(objectName, object);
  }

  /**
   * removes an object from the context and fires the remove event.
   * 
   * @return previous object associated with the given name, or null if there was no mapping for this name.
   */
  public Object remove(final String objectName) {
    Object removed = null;
    if (cache != null) {
      removed = cache.remove(objectName);
      fireObjectEvent(Descriptor.EVENT_REMOVE, objectName, removed);
    }
    return removed;
  }

  /** clears the {@link #cache}. */
  public synchronized void clear() {
    if (cache != null) {
      final Set<String> objectsInCache = new HashSet<String>(cache.keySet());
      for (final String object : objectsInCache) {
        remove(object);
      }
    }
  }

  /**
   * fires the close event then removes the listeners, and cleans up the constructed objects of the context (cleans up
   * the object in the cache and the object in construction).
   * 
   * @see #EVENT_CLOSE
   */
  @Override
  public synchronized void close() {
    if (LOG.isLoggable(Level.FINE)) {
      LOG.fine("closing " + name + "...");
    }

    // fire the close event
    fire(EVENT_CLOSE, null);
  }

  // object access helper methods /////////////////////////////////////////////

  /**
   * gets the object having the name <code>objectName</code> in this context.
   * 
   * @param isDelayable
   *          indicates wether initialization is delayable. When isDelayable is set to false the returned object will be
   *          constructed and initialized. When isDelayable is set to true, the returned object will be constructed, but
   *          not necessarily initialized.
   * @return the object found or created, or null if the object was not found and cannot be created.
   * @throws WireException
   *           if a circular dependency was found during the object creation.
   */
  public synchronized Object get(final String objectName, final boolean isDelayable) {
    if (hasException(objectName)) {
      final String message = ExceptionManager.getInstance().getFullMessage("bp_WC_2", objectName);
      throw new WireException(message, exceptions.get(objectName));
    }

    // first check if the object is in the cache
    if (hasCached(objectName)) {
      final Object object = cache.get(objectName);
      if (LOG.isLoggable(Level.FINE)) {
        LOG.fine("delivering " + objectName);
      }
      return object;
    }

    // then check if it is constructed, but not yet in the cache (pending or
    // under initialization)
    final Object constructed = getConstructed(objectName);
    if (isDelayable && null != constructed) {
      final Object object = constructed;
      if (LOG.isLoggable(Level.FINE)) {
        LOG.fine("providing already constructed " + objectName);
      }
      return object;
    }

    // then check if we can create the object from a descriptor
    if (wireDefinition.hasDescriptor(objectName)) {

      if (isUnderConstruction(objectName) || isUnderInitialization(objectName)) {
        final String message = ExceptionManager.getInstance().getFullMessage("bp_WC_3", objectName);
        throw new WireException(message);
      }

      return create(objectName, isDelayable);
    }

    // then check if we can find it in the environment (if one is available)
    final Environment environment = Environment.getCurrent();
    if (environment != null) {
      if (LOG.isLoggable(Level.FINE)) {
        LOG.fine("delivering " + objectName + " from environment");
      }
      return environment.get(objectName);
    }

    if (LOG.isLoggable(Level.FINE)) {
      LOG.fine("delivering null for undefined object " + objectName);
    }
    return null;
  }

  /**
   * creates a new object for the given objectName as defined in the {@link #wireDefinition}.
   * 
   * @param isDelayable
   *          indicates wether initialization is delayable. When isDelayable is set to false the returned object will be
   *          constructed and initialized. When isDelayable is set to true, the returned object will be constructed, but
   *          not necessarily initialized.
   */
  protected Object create(final String objectName, final boolean isDelayable) {
    final Descriptor descriptor = wireDefinition.getDescriptor(objectName);
    return create(descriptor, isDelayable);
  }

  /**
   * creates a new object for the given descriptor.
   * 
   * @param isDelayable
   *          indicates wether initialization is delayable. When isDelayable is set to false the returned object will be
   *          constructed and initialized. When isDelayable is set to true, the returned object will be constructed, but
   *          not necessarily initialized.
   */
  public Object create(final Descriptor descriptor, final boolean isDelayable) {
    Object object = null;

    object = construct(descriptor);
    initialize(object, descriptor, isDelayable);
    processPendingInitializations();

    return object;
  }

  Object construct(final Descriptor descriptor) {
    Object object;

    final String objectName = descriptor.getName();
    if (objectName != null) {
      fireObjectEvent(Descriptor.EVENT_CONSTRUCTING, objectName, null);
      if (underConstruction == null) {
        underConstruction = new HashSet<String>();
      }
      underConstruction.add(objectName);
      if (LOG.isLoggable(Level.FINE)) {
        LOG.fine("constructing " + objectName);
      }
    }

    try {
      object = descriptor.construct(this);
    } catch (final RuntimeException e) {
      addException(descriptor, e);
      throw e;
    }

    if (objectName != null) {
      underConstruction.remove(objectName);
    }

    return object;
  }

  // initialization ///////////////////////////////////////////////////////////

  private enum InitializationType {
    NONE, IMMEDIATE, DELAYEBLE
  }

  void initialize(final Object object, final Descriptor descriptor, final boolean isDelayable) {

    final InitializationType initializationType = getInitializationType(object, descriptor, isDelayable);

    if (initializationType == InitializationType.IMMEDIATE) {
      performInitialization(object, descriptor);

    } else if (initializationType == InitializationType.DELAYEBLE) {
      addPendingInitialization(object, descriptor);

    } else {
      final String objectName = descriptor.getName();
      if (objectName != null) {
        set(objectName, object);
      }
    }
  }

  InitializationType getInitializationType(final Object object, final Descriptor descriptor, final boolean isDelayable) {
    if (object == null) {
      return InitializationType.NONE;
    }

    if (isDelayable && descriptor.isDelayable()) {
      return InitializationType.DELAYEBLE;
    }

    return InitializationType.IMMEDIATE;
  }

  void performInitialization(final Object object, final Descriptor descriptor) {
    final String objectName = descriptor.getName();

    if (objectName != null) {
      fireObjectEvent(Descriptor.EVENT_INITIALIZING, objectName, object);
      if (underInitialization == null) {
        underInitialization = new HashMap<String, Object>();
      }
      underInitialization.put(objectName, object);
      if (LOG.isLoggable(Level.FINE)) {
        LOG.fine("initializing " + objectName);
      }
    }

    try {
      descriptor.initialize(object, this);
    } catch (final RuntimeException e) {
      addException(descriptor, e);
      throw e;
    }

    if (objectName != null) {
      underInitialization.remove(objectName);
      // event constructed is fired before the object is put in the cache
      // because that generates a set event
      fireObjectEvent(Descriptor.EVENT_CONSTRUCTED, objectName, object);
      set(objectName, object);
    }
  }

  void addPendingInitialization(final Object object, final Descriptor descriptor) {
    if (pendingInitializations == null) {
      pendingInitializations = new HashMap<String, PendingInitialization>();
    }
    pendingInitializations.put(descriptor.getName(), new PendingInitialization(object, descriptor));
  }

  void processPendingInitializations() {
    if (pendingInitializations != null) {
      final Collection<PendingInitialization> pendingInitializationValues = new HashSet<PendingInitialization>(
          pendingInitializations.values());
      for (PendingInitialization pi : pendingInitializationValues) {
        // move pi from pending initializations to under initialization
        final String objectName = pi.initializable.getName();
        pi = pendingInitializations.remove(objectName);
        if (pi != null) {
          // initialize
          performInitialization(pi.object, pi.initializable);
        }
      }
    }
  }

  boolean hasPendingInitializations() {
    return pendingInitializations != null && !pendingInitializations.isEmpty();
  }

  /**
   * container for an storing waiting objects and their initializable in the list {@link #pendingInitializations}, while
   * waiting for initialization.
   */
  class PendingInitialization implements Serializable {
    private static final long serialVersionUID = 1L;
    Object object;
    Descriptor initializable;

    public PendingInitialization(final Object object, final Descriptor descriptor) {
      this.object = object;
      initializable = descriptor;
    }

    @Override
    public String toString() {
      final String objectName = initializable.getName();
      return "PendingInitialization[" + (objectName != null ? objectName + "|" : "") + object + "]";
    }
  }

  /**
   * checks if the given objectName is available in the cache, which means it already has been constructed from a wire
   * definition or it has been {@link #set(String, Object)} explicitely.
   */
  public boolean hasCached(final String objectName) {
    return cache != null && cache.containsKey(objectName);
  }

  /** finds the object in all stages after construction. */
  Object getConstructed(final String objectName) {
    Object constructed = null;
    if (pendingInitializations != null && pendingInitializations.containsKey(objectName)) {
      constructed = pendingInitializations.get(objectName).object;
    } else if (underInitialization != null && underInitialization.containsKey(objectName)) {
      constructed = underInitialization.get(objectName);
    }
    return constructed;
  }

  /** fires a {@link WireObjectEventInfo}. */
  protected void fireObjectEvent(final String eventName, final String objectName, final Object object) {
    WireObjectEventInfo wireEvent = null;

    // first fire the event on the descriptor for object specific listeners
    if (wireDefinition != null) {
      final Map<String, Descriptor> descriptors = wireDefinition.getDescriptors();
      if (descriptors != null) {
        final Descriptor descriptor = descriptors.get(objectName);
        if (descriptor != null) {
          wireEvent = new WireObjectEventInfo(eventName, objectName, object);
          descriptor.fire(eventName, wireEvent);
        }
      }
    }

    // then fire the event on this wiring environment for global listeners
    if (listeners != null && wireEvent == null) {
      wireEvent = new WireObjectEventInfo(eventName, objectName, object);
    }

    fire(eventName, wireEvent);
  }

  boolean hasObjectUnderConstruction() {
    return underConstruction != null && !underConstruction.isEmpty();
  }

  boolean hasObjectUnderInitialization() {
    return underInitialization != null && !underInitialization.isEmpty();
  }

  boolean isUnderConstruction(final String objectName) {
    return underConstruction != null && underConstruction.contains(objectName);
  }

  boolean isUnderInitialization(final String objectName) {
    return underInitialization != null && underInitialization.containsKey(objectName);
  }

  /**
   * the class loader to use to create objects or if none was explicitely set in this wire context, the default context
   * class loader for the current thread.
   */
  public ClassLoader getClassLoader() {
    // if there is a specific classloader specified
    if (classLoader != null) {
      return classLoader;
    }
    // otherwise, use the current environment classloader
    return Thread.currentThread().getContextClassLoader();
  }

  public boolean hasClassLoader() {
    return classLoader != null;
  }

  // search by class //////////////////////////////////////////////////////////

  /**
   * searches for the first descriptor that defines an object of the given type. In case of multiple objects of the same
   * type, the first object that is declared of the given type will be found. Also super classes and interfaces are
   * taken into account. Not all descriptor types will be type sensitive, only:
   * 
   * <pre>
   * | ObjectDescriptor                       | object            |
   * | HibernatePersistenceServiceDescriptor  | business-calendar |
   * | TransactionDescriptor                  | transaction       |
   * | PropertiesDescriptor                   | properties        |
   * | BusinessCalendarDescriptor             | business-calendar |
   * &lt;/ul&gt;
   * </pre>
   */
  @Override
  @SuppressWarnings("unchecked")
  public <T> T get(final Class<T> type) {
    if (wireDefinition != null) {
      final String name = wireDefinition.getDescriptorName(type);
      if (name != null) {
        return (T) get(name);
      }
    }
    return null;
  }

  protected boolean hasException(final String objectName) {
    return exceptions != null && exceptions.containsKey(objectName);
  }

  protected void addException(final Descriptor descriptor, final Exception exception) {
    if (exceptions == null) {
      exceptions = new HashMap<String, Exception>();
    }
    exceptions.put(descriptor.getName(), exception);
  }

  // getters and setters //////////////////////////////////////////////////////

  @Override
  public String getName() {
    return name;
  }

  public void setClassLoader(final ClassLoader classLoader) {
    this.classLoader = classLoader;
  }

  public WireDefinition getWireDefinition() {
    return wireDefinition;
  }

  public void setWireDefinition(final WireDefinition wireDefinition) {
    this.wireDefinition = wireDefinition;
  }
}
