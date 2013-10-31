/**
 * Copyright (C) 2007  Bull S. A. S.
 * Bull, Rue Jean Jaures, B.P.68, 78340, Les Clayes-sous-Bois
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

import org.ow2.bonita.env.Descriptor;

/**
 * <p>
 * This class specifies an argument to be supplied to a method.
 * </p>
 * 
 * @author Tom Baeyens
 * @author Guillaume Porcher (documentation)
 * 
 * @see ObjectDescriptor
 * @see InvokeOperation
 * @see SubscribeOperation
 */
public class ArgDescriptor implements Serializable {

  private static final long serialVersionUID = 1L;

  long dbid;
  int dbversion;
  String typeName;
  Descriptor descriptor;

  public ArgDescriptor() {
  }

  /**
   * Gets the Descriptor used to construct the value given to the argument.
   */
  public Descriptor getDescriptor() {
    return descriptor;
  }

  /**
   * Sets the Descriptor used to construct the value given to the argument.
   * 
   * @param descriptor
   */
  public void setDescriptor(Descriptor descriptor) {
    this.descriptor = descriptor;
  }

  /**
   * Gets the name of the type of this argument if it is defined. If the type
   * name is not defined, the type of the Descriptor is used.
   */
  public String getTypeName() {
    return typeName;
  }

  /**
   * Sets the name of the type of this argument.
   * 
   * @param typeName
   */
  public void setTypeName(String typeName) {
    this.typeName = typeName;
  }
}
