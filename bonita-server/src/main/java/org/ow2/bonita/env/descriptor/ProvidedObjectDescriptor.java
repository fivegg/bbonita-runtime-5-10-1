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
package org.ow2.bonita.env.descriptor;

import org.ow2.bonita.env.WireContext;
import org.ow2.bonita.env.WireDefinition;

/**
 * @author Tom Baeyens
 */
public class ProvidedObjectDescriptor extends AbstractDescriptor {

  private static final long serialVersionUID = 1L;

  Object providedObject;
  boolean exposeType = false;

  public ProvidedObjectDescriptor() {
    super();
  }

  public ProvidedObjectDescriptor(Object providedObject) {
    super();
    this.providedObject = providedObject;
  }

  public ProvidedObjectDescriptor(Object providedObject, boolean exposeType) {
    super();
    this.providedObject = providedObject;
    this.exposeType = exposeType;
  }

  public Object construct(WireContext wireContext) {
    return providedObject;
  }

  public Class<?> getType(WireDefinition wireDefinition) {
    if ((!exposeType) || (providedObject == null)) {
      return null;
    }
    return providedObject.getClass();
  }

  public Object getProvidedObject() {
    return providedObject;
  }

  public void setProvidedObject(Object providedObject) {
    this.providedObject = providedObject;
  }

  public boolean isExposeType() {
    return exposeType;
  }

  public void setExposeType(boolean exposeType) {
    this.exposeType = exposeType;
  }
}
