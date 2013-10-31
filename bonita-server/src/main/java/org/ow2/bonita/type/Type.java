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

import org.ow2.bonita.util.ReflectUtil;

/**
 * a variable type.
 * 
 * @author Tom Baeyens
 */
public class Type implements Serializable {

  private static final long serialVersionUID = 1L;

  protected String name;
  protected Converter converter;
  protected Class<?> variableClass;

  public String toString() {
    if (name != null) {
      return name;
    }
    StringBuffer buffer = new StringBuffer();
    if (converter != null) {
      buffer.append(converter.toString());
      buffer.append("-->");
    }
    if (variableClass != null) {
      buffer.append(ReflectUtil.getUnqualifiedClassName(variableClass));
    } else {
      buffer.append("undefined");
    }
    return buffer.toString();
  }

  public Converter getConverter() {
    return converter;
  }

  public void setConverter(Converter converter) {
    this.converter = converter;
  }

  public Class<?> getVariableClass() {
    return variableClass;
  }

  public void setVariableClass(Class<?> variableClass) {
    this.variableClass = variableClass;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
