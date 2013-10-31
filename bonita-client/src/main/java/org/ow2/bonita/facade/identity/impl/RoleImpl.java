/**
 * Copyright (C) 2009  BonitaSoft S.A.
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
package org.ow2.bonita.facade.identity.impl;

import java.util.UUID;

import org.ow2.bonita.facade.def.majorElement.impl.DescriptionElementImpl;
import org.ow2.bonita.facade.identity.Role;
import org.ow2.bonita.util.Misc;

/**
 * @author Anthony Birembaut
 * 
 */
public class RoleImpl extends DescriptionElementImpl implements Role {

  private static final long serialVersionUID = 3045932457883692778L;

  protected long dbid;

  protected String uuid;

  protected String name;

  protected String label;

  protected RoleImpl() {
    super();
  };

  public RoleImpl(final String uuid, final String name) {
    super();
    Misc.checkArgsNotNull(uuid, name);
    this.uuid = uuid;
    this.name = name;
  }

  public RoleImpl(final String name) {
    super();
    Misc.checkArgsNotNull(name);
    this.name = name;
    uuid = UUID.randomUUID().toString();
  }

  public RoleImpl(final RoleImpl src) {
    super(src);
    uuid = src.getUUID();
    name = src.getName();
    label = src.getLabel();
  }

  public void setName(final String name) {
    this.name = name;
  }

  public void setLabel(final String label) {
    this.label = label;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getLabel() {
    return label;
  }

  @Override
  public String getUUID() {
    return uuid;
  }

}
