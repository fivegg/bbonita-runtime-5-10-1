/**
 * Copyright (C) 2009-2013 BonitaSoft S.A.
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
package org.ow2.bonita.facade.def;

import java.io.Serializable;

import org.ow2.bonita.facade.def.majorElement.DataFieldDefinition;
import org.ow2.bonita.facade.def.majorElement.impl.DataFieldDefinitionImpl;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.type.Variable;
import org.ow2.bonita.util.VariableUtil;

/**
 * 
 * @author Matthieu Chaffotte
 * 
 */
public class InternalDataFieldDefinition extends DataFieldDefinitionImpl {

  private static final long serialVersionUID = 1L;

  protected Variable initialValueVariable;

  protected InternalDataFieldDefinition() {
    super();
  }

  public InternalDataFieldDefinition(final DataFieldDefinition src, final ProcessDefinitionUUID processUUID) {
    super(src);
    setInitialValue(VariableUtil.createVariable(processUUID, src.getName(), src.getInitialValue()));
  }

  public void setInitialValue(final Variable initialValue) {
    initialValueVariable = initialValue;
    clientInitialValue = null;
  }

  private Variable getInitialValueVariable() {
    return initialValueVariable;
  }

  @Override
  public Serializable getInitialValue() {
    if (initialValueVariable == null) {
      return null;
    }
    return (Serializable) getInitialValueVariable().getValue();
  }

}
