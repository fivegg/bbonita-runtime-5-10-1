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
package org.ow2.bonita.facade.identity.impl;

import java.util.UUID;

import org.ow2.bonita.facade.identity.Group;
import org.ow2.bonita.facade.identity.Membership;
import org.ow2.bonita.facade.identity.Role;

/**
 * @author Anthony Birembaut
 *
 */
public class MembershipImpl implements Membership {

  /**
   * UID
   */
  private static final long serialVersionUID = -4436298174915288366L;

  protected long dbid;
  
  protected String uuid;
  
  protected Role role;
  
  protected Group group;
  
  public MembershipImpl() {
    this.uuid = UUID.randomUUID().toString();
  }
  
  public MembershipImpl(MembershipImpl src) {
    this.uuid = src.getUUID();
    this.role = new RoleImpl((RoleImpl)src.getRole());
    this.group = new GroupImpl((GroupImpl)src.getGroup());
  }

  public Group getGroup() {
    return group;
  }
  
  public void setGroup(Group group) {
    this.group = group;
  }

  public Role getRole() {
    return role;
  }

  public void setRole(Role role) {
    this.role = role;
  }

  public String getUUID() {
    return uuid;
  }

}
