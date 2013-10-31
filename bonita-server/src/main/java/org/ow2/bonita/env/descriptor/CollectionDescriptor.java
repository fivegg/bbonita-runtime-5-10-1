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
 * 
 * Modified by Matthieu Chaffotte - BonitaSoft S.A.
 */
package org.ow2.bonita.env.descriptor;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ow2.bonita.env.Descriptor;
import org.ow2.bonita.env.WireContext;
import org.ow2.bonita.env.WireException;
import org.ow2.bonita.util.ExceptionManager;
import org.ow2.bonita.util.ReflectUtil;

/**
 * @author Tom Baeyens
 */
public class CollectionDescriptor extends AbstractDescriptor implements Descriptor {

  private static final long serialVersionUID = 1L;

  private static final Logger LOG = Logger.getLogger(CollectionDescriptor.class.getName());

  protected String className;
  protected List<Descriptor> valueDescriptors;
  protected boolean isSynchronized;

  protected CollectionDescriptor() {
    super();
  }

  public CollectionDescriptor(final String defaultImplClassName) {
    super();
    className = defaultImplClassName;
  }

  @Override
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public Object construct(final WireContext wireContext) {
    Object object = null;
    try {
      // instantiate
      final ClassLoader classLoader = wireContext.getClassLoader();
      final Class<?> clazz = ReflectUtil.loadClass(classLoader, className);
      object = clazz.newInstance();

      if (isSynchronized) {
        if (object instanceof SortedSet) {
          object = Collections.synchronizedSortedSet((SortedSet) object);
        } else if (object instanceof SortedMap) {
          object = Collections.synchronizedSortedMap((SortedMap) object);
        } else if (object instanceof Set) {
          object = Collections.synchronizedSet((Set) object);
        } else if (object instanceof Map) {
          object = Collections.synchronizedMap((Map) object);
        } else if (object instanceof List) {
          object = Collections.synchronizedList((List) object);
        } else if (object instanceof Collection) {
          object = Collections.synchronizedCollection((Collection) object);
        }
      }

    } catch (final Exception e) {
      final String message = ExceptionManager.getInstance().getFullMessage("bp_CollD_1",
          name != null ? name : className, e.getMessage());
      throw new WireException(message, e);
    }
    return object;
  }

  @Override
  @SuppressWarnings("unchecked")
  public void initialize(final Object object, final WireContext wireContext) {
    final Collection<Object> collection = (Collection<Object>) object;
    try {
      if (valueDescriptors != null) {
        for (final Descriptor descriptor : valueDescriptors) {
          final Object element = wireContext.create(descriptor, true);
          if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("adding element " + element + " to collection");
          }
          collection.add(element);
        }
      }
    } catch (final WireException e) {
      throw e;
    } catch (final Exception e) {
      final String message = ExceptionManager.getInstance().getFullMessage("bp_CollD_2",
          name != null ? name : className);
      throw new WireException(message, e);
    }
  }

  public String getClassName() {
    return className;
  }

  public void setClassName(final String className) {
    this.className = className;
  }

  public List<Descriptor> getValueDescriptors() {
    return valueDescriptors;
  }

  public void setValueDescriptors(final List<Descriptor> valueDescriptors) {
    this.valueDescriptors = valueDescriptors;
  }

  public boolean isSynchronized() {
    return isSynchronized;
  }

  public void setSynchronized(final boolean isSynchronized) {
    this.isSynchronized = isSynchronized;
  }

}
