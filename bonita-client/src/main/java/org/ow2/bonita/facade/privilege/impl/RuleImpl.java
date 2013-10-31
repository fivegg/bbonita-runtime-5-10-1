/**
 * Copyright (C) 2010-2012 BonitaSoft S.A.
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
package org.ow2.bonita.facade.privilege.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.ow2.bonita.facade.def.majorElement.impl.DescriptionElementImpl;
import org.ow2.bonita.facade.privilege.Rule;
import org.ow2.bonita.facade.uuid.AbstractUUID;
import org.ow2.bonita.util.CopyTool;
import org.ow2.bonita.util.Misc;

/**
 * @author Nicolas Chabanoles
 * 
 */
public class RuleImpl extends DescriptionElementImpl implements Rule {

  private static final long serialVersionUID = -5403223858809095337L;

  protected long dbid;

  protected String uuid;

  protected String name;

  protected String label;

  protected Set<String> exceptions;

  protected String type;

  protected Set<String> entities;

  protected Set<String> users;

  protected Set<String> roles;

  protected Set<String> groups;

  protected Set<String> memberships;

  protected RuleImpl() {
    super();
  }

  protected RuleImpl(final RuleType type) {
    super();
    Misc.checkArgsNotNull(type);
    uuid = UUID.randomUUID().toString();
    this.type = type.name();
  }

  protected RuleImpl(final String name, final String label, final String description, final RuleType type,
      final Set<? extends AbstractUUID> items) {
    super();
    Misc.checkArgsNotNull(name, type);
    uuid = UUID.randomUUID().toString();
    this.name = name;
    this.label = label;
    if (items != null) {
      exceptions = new HashSet<String>();
      for (final AbstractUUID abstractUUID : items) {
        exceptions.add(abstractUUID.getValue());
      }

    } else {
      exceptions = null;
    }

    this.type = type.name();
    setDescription(description);
  }

  protected RuleImpl(final Rule src) {
    super(src);
    dbid = src.getId();
    uuid = src.getUUID();
    name = src.getName();
    label = src.getLabel();
    exceptions = new HashSet<String>();
    final Set<String> items_ = src.getItems();
    for (final String item : items_) {
      exceptions.add(item);
    }
    type = src.getType().name();
    entities = CopyTool.copy(src.getEntities());
    users = CopyTool.copy(src.getUsers());
    roles = CopyTool.copy(src.getRoles());
    groups = CopyTool.copy(src.getGroups());
    memberships = CopyTool.copy(src.getMemberships());
  }

  @Override
  @Deprecated
  public long getId() {
    return dbid;
  }

  @Override
  public String getUUID() {
    return uuid;
  }

  @Override
  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  @Override
  public String getLabel() {
    return label;
  }

  public void setLabel(final String label) {
    this.label = label;
  }

  @Override
  public Set<String> getItems() {
    if (exceptions == null) {
      exceptions = new HashSet<String>();
    }
    return exceptions;
  }

  public void setItems(final Set<String> items) {
    Misc.checkArgsNotNull(items);
    exceptions = items;
  }

  @Override
  public RuleType getType() {
    return RuleType.valueOf(type);
  }

  /**
   * @param exceptions
   *          must be not null
   */
  protected <E extends AbstractUUID> void addExceptions(final Collection<E> exceptions) {
    Misc.checkArgsNotNull(exceptions);
    if (this.exceptions == null) {
      this.exceptions = new HashSet<String>();
    }
    for (final AbstractUUID exception : exceptions) {
      this.exceptions.add(exception.getValue());
    }
  }

  /**
   * @param exceptions
   *          must be not null
   */
  protected <E extends AbstractUUID> void removeExceptions(final Collection<E> exceptions) {
    Misc.checkArgsNotNull(exceptions);
    if (this.exceptions != null && !this.exceptions.isEmpty()) {
      for (final AbstractUUID exception : exceptions) {
        this.exceptions.remove(exception.getValue());
      }
    }
  }

  protected <E extends AbstractUUID> void setExceptions(final Set<E> exceptions) {
    Misc.checkArgsNotNull(exceptions);
    final Set<String> exceptionValues = new HashSet<String>();
    for (final AbstractUUID exception : exceptions) {
      exceptionValues.add(exception.getValue());
    }
    this.exceptions = exceptionValues;
  }

  @Override
  public Set<String> getEntities() {
    if (entities == null) {
      entities = new HashSet<String>();
    }
    return entities;
  }

  /**
   * @param entities
   *          must be not null
   */
  public void addEntities(final Collection<String> entities) {
    Misc.checkArgsNotNull(entities);
    if (this.entities == null) {
      this.entities = new HashSet<String>();
    }
    this.entities.addAll(entities);
  }

  /**
   * @param entities
   *          must be not null
   */
  public void removeEntities(final Collection<String> entities) {
    Misc.checkArgsNotNull(entities);
    if (this.entities != null && !this.entities.isEmpty()) {
      this.entities.removeAll(entities);
    }
  }

  @Override
  public Set<String> getGroups() {
    if (groups == null) {
      groups = new HashSet<String>();
    }
    return groups;
  }

  /**
   * @param groups
   *          must be not null
   */
  public void addGroups(final Collection<String> groups) {
    Misc.checkArgsNotNull(groups);
    if (this.groups == null) {
      this.groups = new HashSet<String>();
    }
    this.groups.addAll(groups);
  }

  /**
   * @param groups
   *          must be not null
   */
  public void removeGroups(final Collection<String> groups) {
    Misc.checkArgsNotNull(groups);
    if (this.groups != null && !this.groups.isEmpty()) {
      this.groups.removeAll(groups);
    }
  }

  @Override
  public Set<String> getMemberships() {
    if (memberships == null) {
      memberships = new HashSet<String>();
    }
    return memberships;
  }

  /**
   * @param memberships
   *          must be not null
   */
  public void addMemberships(final Collection<String> memberships) {
    Misc.checkArgsNotNull(memberships);
    if (this.memberships == null) {
      this.memberships = new HashSet<String>();
    }
    this.memberships.addAll(memberships);
  }

  /**
   * @param memberships
   *          must be not null
   */
  public void removeMemberships(final Collection<String> memberships) {
    Misc.checkArgsNotNull(memberships);
    if (this.memberships != null && !this.memberships.isEmpty()) {
      this.memberships.removeAll(memberships);
    }
  }

  @Override
  public Set<String> getRoles() {
    if (roles == null) {
      roles = new HashSet<String>();
    }
    return roles;
  }

  /**
   * @param roles
   *          must be not null
   */
  public void addRoles(final Collection<String> roles) {
    Misc.checkArgsNotNull(roles);
    if (this.roles == null) {
      this.roles = new HashSet<String>();
    }
    this.roles.addAll(roles);
  }

  /**
   * @param roles
   *          must be not null
   */
  public void removeRoles(final Collection<String> roles) {
    Misc.checkArgsNotNull(roles);
    if (this.roles != null && !this.roles.isEmpty()) {
      this.roles.removeAll(roles);
    }
  }

  @Override
  public Set<String> getUsers() {
    if (users == null) {
      users = new HashSet<String>();
    }
    return users;
  }

  /**
   * @param users
   *          must be not null
   */
  public void addUsers(final Collection<String> users) {
    Misc.checkArgsNotNull(users);
    if (this.users == null) {
      this.users = new HashSet<String>();
    }
    this.users.addAll(users);
  }

  /**
   * @param users
   *          must be not null
   */
  public void removeUsers(final Collection<String> users) {
    Misc.checkArgsNotNull(users);
    if (this.users != null && !this.users.isEmpty()) {
      this.users.removeAll(users);
    }
  }

  @Override
  public String toString() {
    return name + " - " + label + " - " + type;
  }

  public static Rule createRule(final Rule source) {
    return new RuleImpl(source);
  }

  @Override
  public int compareTo(final Rule rule) {
    return getName().compareTo(rule.getName());
  }

}
