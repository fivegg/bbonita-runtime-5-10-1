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
import java.util.ArrayList;
import java.util.List;

import org.ow2.bonita.env.WireContext;
import org.ow2.bonita.env.WireException;
import org.ow2.bonita.env.descriptor.ArgDescriptor;
import org.ow2.bonita.env.descriptor.ObjectDescriptor;
import org.ow2.bonita.util.ExceptionManager;
import org.ow2.bonita.util.ReflectUtil;
import org.ow2.bonita.util.ReflectUtilDescriptor;

/**
 * invokes a method.
 * 
 * @author Tom Baeyens
 * @author Guillaume Porcher (documentation)
 * @see ArgDescriptor
 * @see ObjectDescriptor
 */

public class InvokeOperation extends AbstractOperation {

  private static final long serialVersionUID = 1L;

  /** name of the method to invoke. */
  String methodName = null;
  /** list of descriptors for creating arguments supplied to the method */
  List<ArgDescriptor> argDescriptors = null;

  public void apply(Object target, WireContext wireContext) {
    try {
      Object[] args = ObjectDescriptor.getArgs(wireContext, argDescriptors);
      Class<?> clazz = target.getClass();
      Method method = ReflectUtilDescriptor.findMethod(clazz, methodName, argDescriptors,
          args);
      if (method == null) {
      	String message = ExceptionManager.getInstance().getFullMessage(
      			"bp_IO_1", ReflectUtilDescriptor.getSignature(methodName, argDescriptors, args));
        throw new WireException(message);
      }
      ReflectUtil.invoke(method, target, args);
    } catch (WireException e) {
      throw e;
    } catch (Exception e) {
    	String message = ExceptionManager.getInstance().getFullMessage(
    			"bp_IO_2", methodName, e.getMessage());
      throw new WireException(message, e);
    }
  }

  /**
   * Adds a descriptor to the list of arguments descriptors.
   */
  public void addArgDescriptor(ArgDescriptor argDescriptor) {
    if (argDescriptors == null) {
      argDescriptors = new ArrayList<ArgDescriptor>();
    }
    argDescriptors.add(argDescriptor);
  }

  /**
   * Gets the name of the method to invoke.
   */
  public String getMethodName() {
    return methodName;
  }

  /**
   * Sets the name of the method to invoke.
   * 
   * @param methodName
   *          the name of the method to invoke.
   */
  public synchronized void setMethodName(String methodName) {
    this.methodName = methodName;
  }

  /**
   * Gets the list of descriptor to create arguments supplied to the method .
   */
  public List<ArgDescriptor> getArgDescriptors() {
    return argDescriptors;
  }

  /**
   * Sets the list of descriptor to create arguments supplied to the method .
   */
  public void setArgDescriptors(List<ArgDescriptor> argDescriptors) {
    this.argDescriptors = argDescriptors;
  }
}
