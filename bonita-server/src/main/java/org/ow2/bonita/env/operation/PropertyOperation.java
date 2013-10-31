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
package org.ow2.bonita.env.operation;

import java.lang.reflect.Method;

import org.ow2.bonita.env.Descriptor;
import org.ow2.bonita.env.WireContext;
import org.ow2.bonita.env.WireException;
import org.ow2.bonita.util.ExceptionManager;
import org.ow2.bonita.util.ReflectUtil;
import org.ow2.bonita.util.ReflectUtilDescriptor;

/**
 * injects another object with a setter method.
 * 
 * @author Tom Baeyens
 * @author Guillaume Porcher (documentation)
 * 
 */
public class PropertyOperation extends AbstractOperation {

  private static final long serialVersionUID = 1L;

  String setterName = null;
  /* the method will be searched by reflection on the runtime value */
  Descriptor descriptor = null;

  public void apply(Object target, WireContext wireContext) {
    // create the value to assign to the property
    Object value = wireContext.create(descriptor, true);
    Method method = null;
    Class<?> clazz = target.getClass();
    Object[] args = new Object[] { value };
    method = ReflectUtilDescriptor.findMethod(clazz, setterName, null, args);
    if (method == null) {
    	String message = ExceptionManager.getInstance().getFullMessage(
    			"bp_PO_1", setterName, value);
      throw new WireException(message);
    }
    ReflectUtil.invoke(method, target, args);
  }

  /**
   * Sets the name of the property that should be updated by this operation. If
   * propertyName is <code>foo</code>, the method used to set the value will be
   * <code>setFoo</code>.
   * 
   * @param propertyName
   */
  public void setPropertyName(String propertyName) {
    this.setterName = "set" + propertyName.substring(0, 1).toUpperCase()
        + propertyName.substring(1);
  }

  /**
   * Gets the name of the setter method used to inject the property value.
   * 
   * @return name of the setter method used to inject the property value.
   */
  public String getSetterName() {
    return setterName;
  }

  /**
   * Sets the name of the setter method to use to inject the property value.
   * 
   * @param setterName
   */
  public void setSetterName(String setterName) {
    this.setterName = setterName;
  }

  /**
   * Gets the descriptor used to create the field's value.
   */
  public Descriptor getDescriptor() {
    return descriptor;
  }

  /**
   * Sets the descriptor used to create the field's value
   * 
   * @param valueDescriptor
   */
  public void setDescriptor(Descriptor valueDescriptor) {
    this.descriptor = valueDescriptor;
  }
}
