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
import org.ow2.bonita.runtime.ExtensionPointsPolicy;

/**
 * @author Elias Ricken de Medeiros
 */
public class ExtensionPointsDescriptor extends AbstractDescriptor {

  private static final long serialVersionUID = 2024770116457524323L;

  private ExtensionPointsPolicy policy;

  @Override
  public Object construct(final WireContext wireContext) {
    return policy;
  }

  @Override
  public Class<?> getType(final WireDefinition wireDefinition) {
    return ExtensionPointsPolicy.class;
  }

  public void setThrowExceptionOnFail(final boolean throwExceptionOnFail) {
    if (throwExceptionOnFail) {
      policy = ExtensionPointsPolicy.THROW_EXCPTION_ON_FAIL;
    } else {
      policy = ExtensionPointsPolicy.CATCH_EXCEPTION_ON_FAIL;
    }
  }

}
