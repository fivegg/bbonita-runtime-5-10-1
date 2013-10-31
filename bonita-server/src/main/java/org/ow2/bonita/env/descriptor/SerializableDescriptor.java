/**
 * Copyright (C) 2009  BonitaSoft S.A.
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
package org.ow2.bonita.env.descriptor;

import java.io.Serializable;

import org.ow2.bonita.env.WireContext;

/**
 * @author Charles Souillard
 */
public class SerializableDescriptor extends AbstractDescriptor {

  private static final long serialVersionUID = 1L;

  protected Serializable ser;

  public SerializableDescriptor() {
    super();
  }

  public SerializableDescriptor(Serializable value) {
    super();
    setValue(value);
  }

  public Object construct(WireContext factory) {
    if (ser == null) {
      return null;
    }
    return ser;
  }

  public void setValue(Serializable value) {
    ser = value;
  }
  
}
