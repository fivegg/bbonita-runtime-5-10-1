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
import java.util.Stack;

import org.ow2.bonita.util.BonitaRuntimeException;
import org.ow2.bonita.util.ExceptionManager;

/**
 * maintains contextual information for a thread in a set of {@link Context}s.
 * 
 * <h3>Introduction</h3>
 * 
 * <p>
 * Objects have different lifecycles and different context's (aka scopes). An
 * environment provides the structure to easily manage objects with different
 * contexts.
 * </p>
 * 
 * <p>
 * Examples of contexts are:
 * <ul>
 * <li><b>environment-factory</b>: The environment-factory context is used to
 * store e.g. data sources, session factories and other static resources needed
 * by an application. The environment-factory context lives for the complete
 * duration of the EnvironmentFactory. So if the
 * EnvironmentFactory is maintained in a static member field, the
 * environment-factory context lives for the duration of the application. The
 * same environment-factory context is shared for all the Environments produced
 * by one EnvironmentFactory.</li>
 * <li><b>environment</b>: The environment context is used for e.g. a
 * transaction and transactional resources, user authentication. This results in
 * an efficient and configurable use of transactional resources that need to be
 * lazily initialized.</li>
 * <li>The environment can accomodate other contexts as well. They can be added
 * and removed dynamically. Examples of other potential contexts are
 * web-request, web-session, web-application, business processDefinition, ...</li>
 * </ul>
 * 
 * <center><img src="environment.gif"/></center>
 * 
 * <p>
 * An environment is typically installed like this
 * </p>
 * 
 * <b>
 * 
 * <pre>
 * static EnvironmentFactory environmentFactory = new DefaultEnvironmentFactory();
 * 
 * ...
 * 
 * Environment environment = environmentFactory.openEnvironment();
 * try {
 * 
 *   ... everything available in this block ... 
 * 
 * } finally {
 *   environment.close();
 * }
 * </pre>
 * 
 * </b>
 * 
 * <h3>Purpose</h3>
 * 
 * <p>
 * The first purpose of the environment is to separate the application from the
 * environment. Standard Java and Enterprise Java are quite different and an
 * environment abstraction like this allows for the development of applications
 * that can run in both Standard and Enterprise environments. Also test
 * environments are easier to tweak this way.
 * </p>
 * 
 * <p>
 * A second purpose of the environment is to enable specific to global searching
 * of resources. E.g. you could search for an 'adminEmailAddress' in the
 * contexts 'processDefinition-execution', 'processDefinition-definition' and
 * 'environment-factory' in the given order. That way, a global
 * adminEmailAddress can be specified in the environment-factory context and it
 * can be refined in more specific contexts processDefinition-definition or
 * processDefinition-execution.
 * </p>
 * 
 * <h3>Search order</h3>
 * 
 * <p>
 * To find an object in the environment, a searchOrder can be specified. A
 * search order is an sequence that specifies the order in which the contexts
 * should be searched.
 * </p>
 * 
 * <p>
 * The default search order is the inverse sequence of how the contexts are
 * added to the environment. This is because in general, we can assume that the
 * more recent a context was added, the more specific it is.
 * </p>
 * 
 * <h3>Transaction, username and classloader</h3>
 * 
 * <p>
 * Three objects are used so frequently in an environment that they get special
 * treatment:
 * </p>
 * 
 * <ul>
 * <li><b>Transaction</b>: an abstraction for marking a transaction with
 * setRollbackOnly.</li>
 * <li><b>Classloader</b>: the current class loader.</li>
 * <li><b>Username</b>: the name of the currently authenticated user.</li>
 * </ul>
 * 
 * <p>
 * For these special properties, setters are also available. That is to support
 * programmatic injection into the environment. Alternatively, they can be
 * configured in one of the contexts.
 * </p>
 * 
 * 
 * @author Tom Baeyens
 */
public abstract class Environment implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 4160822335317326182L;

  /**
   * searches a named object in all the contexts in the default search order.
   * 
   * @return the object if it exists in the environment, <code>null</code> if
   *         there is no object with the given name in the environment.
   */
  public abstract Object get(String name);

  /**
   * searches a named object in all the contexts in the given search order. The
   * given search order doesn't have to include all contexts. It can be a subset
   * of the contexts available.
   * 
   * @param searchOrder
   *          list of contexts names. The object will be searched in these
   *          contexts, in the given order.
   * @return the object if it exists in the environment, <code>null</code> if
   *         there is no object with the given name in the specified searchOrder
   *         contexts.
   */
  public abstract Object get(String name, String[] searchOrder);

  /**
   * searches an object based on type. The search does not take superclasses of the
   * context elements into account.
   * 
   * @return the first object of the given type or null in case no such element
   *         was found.
   */
  public abstract <T> T get(Class<T> type);

  /**
   * searches an object based on type. The search does not take superclasses of the
   * context elements into account.
   * 
   * @return the first object of the given type or null in case no such element
   *         was found.
   */
  public abstract <T> T get(Class<T> type, String[] searchOrder);

  /** get the authenticated user id */
  public abstract String getUserId();

  /** set the authenticated user id */
  public abstract void setUserId(String userId);

  /**
   * closes the Environment by removing all its contexts.
   */
  public abstract void close();

  public abstract Context getContext(String contextName);

  public abstract void addContext(Context context);

  public abstract void removeContext(Context context);

  public abstract ClassLoader getClassLoader();

  public abstract void setClassLoader(ClassLoader classLoader);

  // current environment //////////////////////////////////////////////////////
  /**
   * the current environment is maintained in the currentEnvironment thread
   * local
   */
  static ThreadLocal<Environment> currentEnvironment = new ThreadLocal<Environment>();

  /**
   * in case of nested environments, the current environment stack maintains the
   * outer environments
   */
  static ThreadLocal<Stack<Environment>> currentEnvironmentStack = new ThreadLocal<Stack<Environment>>();

  /** gets the most inner open environment. */
  public static Environment getCurrent() {
    return currentEnvironment.get();
  }

  public static <T> T getFromCurrent(Class<T> type) {
    return getFromCurrent(type, true);
  }

  public static <T> T getFromCurrent(Class<T> type, boolean required) {
    Environment environment = getCurrent();
    if (environment == null) {
      if (required) {
      	String message = ExceptionManager.getInstance().getFullMessage(
      			"bp_E_1", type.getName());
        throw new BonitaRuntimeException(message);
      }
      return null;
    }
    T object = environment.get(type);
    if (object == null) {
      if (required) {
      	String message = ExceptionManager.getInstance().getFullMessage("bp_E_2", type.getName());
        throw new BonitaRuntimeException(message);
      }
      return null;
    }
    return object;
  }

  public static Object getFromCurrent(String name) {
    return getFromCurrent(name, true);
  }

  public static Object getFromCurrent(String name, boolean required) {
    Environment environment = getCurrent();
    if (environment == null) {
      if (required) {
      	String message = ExceptionManager.getInstance().getFullMessage("bp_E_3", name);
        throw new BonitaRuntimeException(message);
      }
      return null;
    }
    Object object = environment.get(name);
    if (object == null) {
      if (required) {
      	String message = ExceptionManager.getInstance().getFullMessage("bp_E_4", name);
        throw new BonitaRuntimeException(message);
      }
      return null;
    }
    return object;
  }

  static Stack<Environment> getStack() {
    // lazy initialize the current environment stack
    Stack<Environment> stack = currentEnvironmentStack.get();
    if (stack == null) {
      stack = new Stack<Environment>();
      currentEnvironmentStack.set(stack);
    }
    return stack;
  }

  /**
   * pops the closing context from the stack of current contexts. This is the
   * first thing that needs to be done when an environment is closed.
   * 
   */
  protected static synchronized Environment popEnvironment() {
    Environment popped = currentEnvironment.get();
    currentEnvironment.set(null);
    Stack<Environment> stack = currentEnvironmentStack.get();
    if ((stack != null) && (!stack.isEmpty())) {
      currentEnvironment.set(stack.pop());
    }
    return popped;
  }

  /**
   * after opening of a new environment succeeded, the environment must be
   * pushed in the stack of current environments.
   * 
   */
  protected static synchronized void pushEnvironment(Environment environment) {
    Environment current = currentEnvironment.get();
    if (current != null) {
      getStack().push(current);
    }
    currentEnvironment.set(environment);
  }

}
