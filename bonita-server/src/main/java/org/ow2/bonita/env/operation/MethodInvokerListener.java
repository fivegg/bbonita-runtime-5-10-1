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

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ow2.bonita.env.WireContext;
import org.ow2.bonita.env.WireException;
import org.ow2.bonita.env.descriptor.ArgDescriptor;
import org.ow2.bonita.env.descriptor.ObjectDescriptor;
import org.ow2.bonita.util.ExceptionManager;
import org.ow2.bonita.util.Listener;
import org.ow2.bonita.util.Observable;
import org.ow2.bonita.util.ReflectUtil;
import org.ow2.bonita.util.ReflectUtilDescriptor;

/**
 * Wrapper for the subscribe operation. This class will be used to call a
 * specified method on reception of an event. This class is used so that a non
 * {@link Listener} class can subscribe to an {@link Observable} object.
 * 
 * @see SubscribeOperation
 * 
 * @author Tom Baeyens
 * @author Guillaume Porcher (Documentation)
 */
public class MethodInvokerListener implements Listener, Serializable {

  private static final long serialVersionUID = 1L;
  private static final Logger LOG = Logger.getLogger(MethodInvokerListener.class.getName());

  String methodName;
  List<ArgDescriptor> argDescriptors = null;
  WireContext wireContext;
  Object target;

  transient Method method = null;

  /**
   * Creates a new Wrapper. When an event is received, the arguments
   * <code>args</code> are created from the list <code>argDescriptors</code>,
   * and <code>target.methodName(args)</code> is called.
   * 
   * @param methodName
   *          name of the method to call when an event is received.
   * @param argDescriptors
   *          list of descriptors of arguments given to the method.
   * @param wireContext
   *          context to use to create the arguments
   * @param target
   *          object on which the method will be called.
   */
  public MethodInvokerListener(String methodName,
      List<ArgDescriptor> argDescriptors, WireContext wireContext, Object target) {
    this.methodName = methodName;
    this.argDescriptors = argDescriptors;
    this.wireContext = wireContext;
    this.target = target;
  }

  public void event(Object source, String eventName, Object info) {
  	if (LOG.isLoggable(Level.FINE)) {
      LOG.fine("invoking " + methodName + " on " + target + " for event " + eventName);
  	}
    try {
      Object[] args = ObjectDescriptor.getArgs(wireContext, argDescriptors);
      Class<?> clazz = target.getClass();
      Method method = ReflectUtilDescriptor.findMethod(clazz, methodName, argDescriptors,
          args);
      if (method == null) {
      	String message = ExceptionManager.getInstance().getFullMessage(
      			"bp_MIL_1", ReflectUtilDescriptor.getSignature(methodName, argDescriptors, args), target);
        throw new WireException(message);
      }
      ReflectUtil.invoke(method, target, args);
    } catch (WireException e) {
      throw e;
    } catch (Exception e) {
    	String message = ExceptionManager.getInstance().getFullMessage(
    			"bp_MIL_2", methodName, e.getMessage());
      throw new WireException(message, e);
    }
  }
}
