/**
 * Copyright (C) 2010  BonitaSoft S.A.
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
package org.ow2.bonita.facade.runtime.impl;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.Serializable;
import java.util.Arrays;

import org.ow2.bonita.util.Misc;

public class ObjectVariable implements Serializable {

  private static final long serialVersionUID = 233822925089125972L;
  private byte[] value;

  public ObjectVariable(Object value) throws IOException, ClassNotFoundException, NotSerializableException {
    setValue(value);
  }

  public void setValue(Object value) throws IOException, ClassNotFoundException {
    if (value == null) {
      return;
    }
    if (! (value instanceof Serializable)) {
      throw new NotSerializableException(value.getClass().getName());
    }
    this.value = Misc.serialize((Serializable) value);
  }
  
  public Serializable getValue() throws IOException, ClassNotFoundException {
    if (value == null) {
      return null;
    }
    return Misc.deserialize(value);
  }
  
  @Override
  public String toString() {
    if (value == null) {
      return this.getClass().getSimpleName() + " is null";
    }
    return this.getClass().getSimpleName() + ": " + value;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (! (obj instanceof ObjectVariable)) {
      return false;
    }
    ObjectVariable other = (ObjectVariable) obj;
    return Arrays.equals(value, other.value);
  }
  
}
