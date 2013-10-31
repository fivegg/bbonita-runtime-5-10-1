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
package org.ow2.bonita.env.binding;

import org.ow2.bonita.env.descriptor.AbstractDescriptor;
import org.ow2.bonita.env.descriptor.ByteDescriptor;
import org.ow2.bonita.util.xml.Parse;
import org.w3c.dom.Element;

/**
 * parses a descriptor for creating {@link Byte}s.
 * 
 * See schema docs for more details.
 * 
 * @author Tom Baeyens
 * @author Guillaume Porcher (documentation)
 */
public class ByteBinding extends BasicTypeBinding {

  public ByteBinding() {
    super("byte");
  }

  protected AbstractDescriptor createDescriptor(String value, Element element,
      Parse parse) {
    ByteDescriptor byteDescriptor = new ByteDescriptor();
    Byte byteValue;
    try {
      byteValue = Byte.parseByte(value);
      byteDescriptor.setValue(byteValue);
      return byteDescriptor;

    } catch (NumberFormatException e) {
      parse.addProblem(createValueExceptionMessage("'" + value
          + "' cannot be parsed to a byte", element));
    }
    return null;
  }

}