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

import org.ow2.bonita.env.WireContext;
import org.ow2.bonita.env.WireException;
import org.ow2.bonita.util.BonitaRuntimeException;
import org.ow2.bonita.util.ExceptionManager;
import org.ow2.bonita.util.ReflectUtil;

/**
 * loads the class with the specified class name using the WireContext class
 * loader.
 * 
 * @see WireContext#getClassLoader()
 * 
 * @author Tom Baeyens
 * @author Guillaume Porcher (documentation)
 */
public class ClassDescriptor extends AbstractDescriptor {

  private static final long serialVersionUID = 1L;

  String text;

  /**
   * loads the class from the class loader of the specified WireContext.
   * 
   * @throws WireException
   *           if the class could not be loaded.
   */
  public Object construct(WireContext wireContext) {
    ClassLoader classLoader = wireContext.getClassLoader();
    try {
      return ReflectUtil.loadClass(classLoader, text);
    } catch (BonitaRuntimeException e) {
      Throwable cause = e.getCause();
      String message = ExceptionManager.getInstance().getFullMessage(
      		"bp_CD_1", text, cause.getMessage());
      throw new WireException(message, cause);
    }
  }

  public void setClassName(String className) {
    this.text = className;
  }

  public void setClass(Class<?> clazz) {
    if (clazz == null) {
      text = null;
    } else {
      this.text = clazz.getName();
    }
  }
}
