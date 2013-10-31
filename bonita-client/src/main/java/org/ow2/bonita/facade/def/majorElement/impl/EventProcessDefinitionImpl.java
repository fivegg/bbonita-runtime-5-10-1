/**
 * Copyright (C) 2010-2013 BonitaSoft S.A.
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
package org.ow2.bonita.facade.def.majorElement.impl;

import org.ow2.bonita.facade.def.majorElement.EventProcessDefinition;

/**
 * 
 * @author Matthieu Chaffotte
 * 
 */
public class EventProcessDefinitionImpl extends NamedElementImpl implements EventProcessDefinition {

  private static final long serialVersionUID = 8299254081863899167L;

  protected String version;

  protected EventProcessDefinitionImpl() {
    super();
  }

  public EventProcessDefinitionImpl(final String name, final String version) {
    super(name);
    this.version = version;
  }

  public EventProcessDefinitionImpl(final EventProcessDefinition src) {
    super(src);
    version = src.getVersion();
  }

  @Override
  public String getVersion() {
    return version;
  }

}
