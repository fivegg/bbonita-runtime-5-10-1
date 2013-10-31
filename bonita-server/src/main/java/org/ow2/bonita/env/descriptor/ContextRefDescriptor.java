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
package org.ow2.bonita.env.descriptor;

import org.ow2.bonita.env.Descriptor;
import org.ow2.bonita.env.Environment;
import org.ow2.bonita.env.WireContext;
import org.ow2.bonita.env.WireException;
import org.ow2.bonita.util.ExceptionManager;

public class ContextRefDescriptor extends AbstractDescriptor implements
    Descriptor {

  private static final long serialVersionUID = 1L;

  String contextName;

  public Object construct(WireContext wireContext) {
    if (contextName == null) {
      return wireContext;
    }
    Environment environment = Environment.getCurrent();
    if (environment == null) {
    	String message = ExceptionManager.getInstance().getFullMessage("bp_CRD_1", contextName);
      throw new WireException(message);
    }
    return environment.getContext(contextName);
  }

  public String getContextName() {
    return contextName;
  }

  public void setContextName(String contextName) {
    this.contextName = contextName;
  }
}
