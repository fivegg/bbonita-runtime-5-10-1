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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ow2.bonita.util.BonitaRuntimeException;
import org.ow2.bonita.util.ExceptionManager;

/**
 * @author Tom Baeyens
 */
public class BasicEnvironment extends Environment {

  private static final long serialVersionUID = 1L;

  private static final Logger LOG = Logger.getLogger(BasicEnvironment.class.getName());

  protected String userId;
  protected Map<String, Context> contexts;
  protected ArrayList<String> defaultSearchOrderList;
  protected String[] defaultSearchOrder;
  protected Throwable exception;
  protected boolean restrictedAccess;

  protected transient ClassLoader classLoader;

  public BasicEnvironment() {
    contexts = new HashMap<String, Context>();
    defaultSearchOrderList = new ArrayList<String>();
    defaultSearchOrder = null;
    pushEnvironment(this);
  }

  // context methods
  // ////////////////////////////////////////////////////////////

  public Context getContext(String contextName) {
    return contexts.get(contextName);
  }

  public void addContext(Context context) {
    String key = context.getName();
    contexts.put(key, context);
    defaultSearchOrderList.add(key);
    defaultSearchOrder = null;
  }

  public void removeContext(Context context) {
    String contextName = context.getName();
    Context removedContext = contexts.remove(contextName);
    if (removedContext != null) {
      defaultSearchOrderList.remove(contextName);
      defaultSearchOrder = null;
    }
  }

  public Context getEnvironmentFactoryContext() {
    return getContext(Context.CONTEXTNAME_ENVIRONMENT_FACTORY);
  }

  public Context getEnvironmentContext() {
    return getContext(Context.CONTEXTNAME_ENVIRONMENT);
  }

  // userId methods ///////////////////////////////////////////////////////////

  public String getUserId() {
    // if the authenticated user was explicitely set
    if (userId != null) {
      // return that one
      return userId;
    }

    // if an Authentication was specified
    Authentication authentication = get(Authentication.class);
    if (authentication != null) {
      // let the authentication do the work
      return authentication.getUserId();
    }

    return null;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  // classloader methods //////////////////////////////////////////////////////

  public ClassLoader getClassLoader() {
    return classLoader;
  }

  public void setClassLoader(ClassLoader classLoader) {
    this.classLoader = classLoader;
  }

  // search methods ///////////////////////////////////////////////////////////

  public Object get(String name) {
    return get(name, null);
  }

  public Object get(String name, String[] searchOrder) {
    if (searchOrder == null) {
      searchOrder = getDefaultSearchOrder();
    }
    for (int i = 0; i < searchOrder.length; i++) {
      Context context = contexts.get(searchOrder[i]);
      if (context.has(name)) {
        return context.get(name);
      }
    }
    return null;
  }

  public <T> T get(Class<T> type) {
    return get(type, null);
  }

  public <T> T get(Class<T> type, String[] searchOrder) {
    if (searchOrder == null) {
      searchOrder = getDefaultSearchOrder();
    }
    for (int i = 0; i < searchOrder.length; i++) {
      Context context = contexts.get(searchOrder[i]);
      T o = context.get(type);
      if (o != null) {
        return o;
      }
    }
    return null;
  }

  // close ////////////////////////////////////////////////////////////////////

  public void close() {
  	if (LOG.isLoggable(Level.FINE)) {
      LOG.fine("closing " + this);
  	}

    Environment popped = Environment.popEnvironment();
    if (this != popped) {
    	String message = ExceptionManager.getInstance().getFullMessage("bp_BE_1");
      throw new BonitaRuntimeException(message);
    }
  }

  // private methods //////////////////////////////////////////////////////////

  protected String[] getDefaultSearchOrder() {
    if (defaultSearchOrder == null) {
      int size = defaultSearchOrderList.size();
      defaultSearchOrder = new String[size];
      for (int i = 0; i < size; i++) {
        defaultSearchOrder[i] = defaultSearchOrderList.get(size - 1 - i);
      }
    }
    return defaultSearchOrder;
  }

}
