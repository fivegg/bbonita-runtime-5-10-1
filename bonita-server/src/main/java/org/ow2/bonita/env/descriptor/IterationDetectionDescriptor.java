/**
 * Copyright (C) 2012 BonitaSoft S.A.
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

import org.ow2.bonita.env.WireContext;
import org.ow2.bonita.env.WireDefinition;
import org.ow2.bonita.runtime.IterationDetectionPolicy;

/**
 * @author Matthieu Chaffotte
 */
public class IterationDetectionDescriptor extends AbstractDescriptor {

  private static final long serialVersionUID = 8782028918933847934L;

  private IterationDetectionPolicy policy;

  public Object construct(WireContext wireContext) {
    return policy;
  }

  public Class<?> getType(WireDefinition wireDefinition) {
    return IterationDetectionPolicy.class;
  }

  public void setDisable(final boolean disable) {
    if (disable) {
      policy = IterationDetectionPolicy.DISABLE;
    } else {
      policy = IterationDetectionPolicy.ENABLE;
    }
  }

}
