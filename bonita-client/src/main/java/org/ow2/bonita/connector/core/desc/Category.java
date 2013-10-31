/**
 * Copyright (C) 2009-2012  BonitaSoft S.A..
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
package org.ow2.bonita.connector.core.desc;

import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author Matthieu Chaffotte
 * 
 */
public class Category {

  private static final Logger LOG = Logger.getLogger(Category.class.getName());

  private final String name;
  private final String icon;
  private final ClassLoader classLoader;

  public Category(final String name, final String icon, final ClassLoader classLoader) {
    this.name = name;
    this.icon = icon;
    this.classLoader = classLoader;
  }

  public String getName() {
    return name;
  }

  public String getIconPath() {
    return icon;
  }

  public InputStream getIcon() {
    if (icon != null && !"".equals(icon.trim())) {
      ClassLoader loader = classLoader;
      if (loader == null) {
        loader = Thread.currentThread().getContextClassLoader();
      }
      try {
        return loader.getResourceAsStream(icon);
      } catch (final RuntimeException e) {
        if (LOG.isLoggable(Level.WARNING)) {
          LOG.log(Level.WARNING, "Icon Of the category " + name + " cannot be loaded", e);
        }
        return null;
      }
    }
    return null;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (name == null ? 0 : name.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Category other = (Category) obj;
    if (name == null) {
      if (other.name != null) {
        return false;
      }
    } else if (!name.equals(other.name)) {
      return false;
    }
    return true;
  }

}
