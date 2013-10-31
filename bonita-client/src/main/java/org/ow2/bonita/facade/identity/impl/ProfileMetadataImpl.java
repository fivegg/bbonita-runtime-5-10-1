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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.ow2.bonita.facade.identity.ProfileMetadata;
import org.ow2.bonita.facade.identity.User;
import org.ow2.bonita.util.Misc;

/**
 * @author Anthony Birembaut, Matthieu Chaffotte
 *
 */
public class ProfileMetadataImpl implements ProfileMetadata {

  /**
   * UIDMa
   */
  private static final long serialVersionUID = -7010280535895797596L;

  protected long dbid;
  protected String uuid;
  protected String name;
  protected String label;
  protected Map<User, String> users;

  protected ProfileMetadataImpl() {}

  public ProfileMetadataImpl(String name) {
    Misc.checkArgsNotNull(name);
    this.uuid = UUID.randomUUID().toString();
    this.name = name;
  }

  public ProfileMetadataImpl(ProfileMetadataImpl src) {
    this.uuid = src.getUUID();
    this.name = src.getName();
    this.label = src.getLabel();
    // the users map is voluntarily not copied
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public String getUUID() {
    return uuid;
  }

  public Map<User, String> getUsers() {
    if (users == null) {
      users = new HashMap<User, String>();
    }
    return users;
  }

  public void setUsers(Map<User, String> users) {
    this.users = users;
  }

  public void removeUser(User user) {
    this.users.remove(user);
  }

}
