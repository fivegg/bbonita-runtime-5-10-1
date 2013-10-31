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
package org.ow2.bonita.type;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ow2.bonita.env.Environment;
import org.ow2.bonita.env.EnvironmentFactory;
import org.ow2.bonita.env.GlobalEnvironmentFactory;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.util.BonitaRuntimeException;
import org.ow2.bonita.util.ExceptionManager;

/**
 * is a bonita-internal class that serves as a base class for classes that store
 * variable values in the database.
 */
public abstract class Variable implements Serializable {

  protected static final Logger LOG = Logger.getLogger(Variable.class.getName());
  private static final long serialVersionUID = 1L;

  protected long dbid = -1;
  protected int dbversion = 0;

  protected String key = null;
  protected Converter converter = null;
  protected ProcessDefinitionUUID processUUID;
  private static final String REINDEX_DOMAIN_PROPERTY = "org.ow2.bonita.reindex.domain";

  /**
   * is true if this variable-instance supports the given value, false
   * otherwise.
   */
  public abstract boolean isStorable(Object value);

  /**
   * is the value, stored by this variable instance.
   */
  protected abstract Object getObject();

  /**
   * stores the value in this variable instance.
   */
  protected abstract void setObject(Object value);

  // variable management //////////////////////////////////////////////////////

  public void setProcessUUID(ProcessDefinitionUUID processUUID) {
    this.processUUID = processUUID;
  }

  protected ClassLoader getClassLoader() {
    return Thread.currentThread().getContextClassLoader();
  }

  public boolean supports(Object value) {
    final ClassLoader classLoaderToUse = getClassLoader();
    final ClassLoader ori = Thread.currentThread().getContextClassLoader();
    try {
      if (classLoaderToUse != ori) {
        Thread.currentThread().setContextClassLoader(classLoaderToUse);
      }
      if (converter != null) {
        return converter.supports(value);
      }
      return isStorable(value);
    } finally {
      setClassLoader(classLoaderToUse, ori);
    }
  }

  public void setValue(Object value) {
    final ClassLoader classLoaderToUse = getClassLoader();
    final ClassLoader ori = Thread.currentThread().getContextClassLoader();
    try {
      if (classLoaderToUse != ori) {
        Thread.currentThread().setContextClassLoader(classLoaderToUse);
      }
      if (converter != null) {
        if (!converter.supports(value)) {
          String message = ExceptionManager.getInstance().getFullMessage(
              "bp_V_1", converter.getClass().getName(), this.getClass().getName(), value.getClass().getName());
          throw new BonitaRuntimeException(message);
        }
        value = converter.convert(value);
      }
      if ((value != null) && (!this.isStorable(value))) {
        String message = ExceptionManager.getInstance().getFullMessage(
            "bp_V_2", this.getClass().getName(), value.getClass().getName());
        throw new BonitaRuntimeException(message);
      }
      setObject(value);

    } finally {
      setClassLoader(classLoaderToUse, ori);
    }
  }

  public Object getValue() {
    //used by IndexTool
    final String domain = System.getProperty(REINDEX_DOMAIN_PROPERTY);

    EnvironmentFactory environmentFactory = null;
    Environment environment = null;
    if(domain != null){
      try {
        environmentFactory = GlobalEnvironmentFactory.getEnvironmentFactory(domain);
        environment = environmentFactory.openEnvironment();
      } catch (Exception e) {
        if(LOG.isLoggable(Level.SEVERE)) {
          LOG.log(Level.SEVERE, "Impossible to open environment: ", e);
        }
      }
    }
    
    ClassLoader classLoaderToUse = null;
    ClassLoader ori = null;
    try {
      classLoaderToUse = getClassLoader();
      ori = Thread.currentThread().getContextClassLoader();
      if (classLoaderToUse != ori) {
        Thread.currentThread().setContextClassLoader(classLoaderToUse);
      }
      Object value = getObject();
      if ((value != null) && (converter != null)) {
        value = converter.revert(value);
      }
      return value;
    } finally {
      closeEnvironment(environment);
      setClassLoader(classLoaderToUse, ori);
    }
  }

  private void setClassLoader(ClassLoader classLoaderToUse, ClassLoader ori) {
    try {
      if (ori != null && classLoaderToUse != ori) {
        Thread.currentThread().setContextClassLoader(ori);
      }
    } catch(Exception e) {
      if(LOG.isLoggable(Level.SEVERE)) {
        LOG.log(Level.SEVERE, "Impossible set ClassLoader: ", e);
      }
    }
  }

  private void closeEnvironment(Environment environment) {
    try {
      if(environment != null) {
        environment.close();
      }
    } catch(Exception e) {
      if(LOG.isLoggable(Level.SEVERE)) {
        LOG.log(Level.SEVERE, "Impossible to open environment: ", e);
      }
    }
  }

  // utility methods /////////////////////////////////////////////////////////

  public String toString() {
    return "${" + key + "}";
  }

  public Type getType() {
    Type type = new Type();
    type.setConverter(converter);
    type.setVariableClass(getClass());
    return type;
  }

  // getters and setters //////////////////////////////////////////////////////

  public String getKey() {
    return key;
  }

  public Converter getConverter() {
    return converter;
  }

  public void setConverter(Converter converter) {
    this.converter = converter;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public ProcessDefinitionUUID getProcessUUID() {
    return this.processUUID;
  }

}
