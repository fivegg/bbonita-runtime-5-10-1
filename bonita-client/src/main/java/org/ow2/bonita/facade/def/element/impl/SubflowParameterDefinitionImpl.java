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
package org.ow2.bonita.facade.def.element.impl;

import org.ow2.bonita.facade.def.element.SubflowParameterDefinition;

public class SubflowParameterDefinitionImpl implements SubflowParameterDefinition {

  private static final long serialVersionUID = 1L;

  protected SubflowParameterDefinitionImpl() { }

  protected String source;
  protected String destination;

  public SubflowParameterDefinitionImpl(final String source, final String destination) {
    super();
    this.source = source;
    this.destination = destination;
  }

  public SubflowParameterDefinitionImpl(final SubflowParameterDefinition src) {
    this.source = src.getSource();
    this.destination = src.getDestination();
  }
  
  public String getDestination() {
    return destination;
  }

  public String getSource() {
    return source;
  }

}
