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

import org.ow2.bonita.env.Descriptor;
import org.ow2.bonita.env.WireContext;

/**
 * @author Tom Baeyens
 * @author Guillaume Porcher (documentation)
 */
public class FloatDescriptor extends AbstractDescriptor implements Descriptor {

  private static final long serialVersionUID = 1L;

  Double doubleVal;

  public FloatDescriptor() {
    super();
  }

  public FloatDescriptor(final Float value) {
    super();
    setValue(value);
  }

  @Override
  public Object construct(final WireContext factory) {
    if (doubleVal == null) {
      return null;
    }
    return new Float(doubleVal.floatValue());
  }

  public void setValue(final Float value) {
    if (value == null) {
      doubleVal = null;
    } else {
      doubleVal = new Double(value);
    }
  }

}
