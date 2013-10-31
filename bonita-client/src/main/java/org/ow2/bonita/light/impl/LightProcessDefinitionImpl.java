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
package org.ow2.bonita.light.impl;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition.ProcessState;
import org.ow2.bonita.facade.def.majorElement.impl.NamedElementImpl;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.light.LightProcessDefinition;
import org.ow2.bonita.util.Misc;

public class LightProcessDefinitionImpl extends NamedElementImpl implements LightProcessDefinition {

  private static final long serialVersionUID = -572795239631090498L;

  protected ProcessDefinitionUUID uuid;
  protected String version;
  protected ProcessState state;
  protected ProcessType type;
  protected long deployedDate;
  protected long undeployedDate;
  protected String deployedBy;
  protected String undeployedBy;
  protected Set<String> categories;
  protected long migrationDate;

  protected LightProcessDefinitionImpl() {
    super();
  }

  public LightProcessDefinitionImpl(final ProcessDefinition src) {
    super(src);
    uuid = new ProcessDefinitionUUID(src.getUUID());

    state = src.getState();
    type = src.getType();
    version = src.getVersion();

    deployedDate = Misc.getTime(src.getDeployedDate());
    undeployedDate = Misc.getTime(src.getUndeployedDate());
    deployedBy = src.getDeployedBy();
    undeployedBy = src.getUndeployedBy();
    categories = null;
    if (src.getCategoryNames() != null) {
      categories = new HashSet<String>(src.getCategoryNames());
    }
    migrationDate = Misc.getTime(src.getMigrationDate());
  }

  protected LightProcessDefinitionImpl(final String name, final String version) {
    super(name);
    Misc.checkArgsNotNull(name, version);
    this.version = version;
    if (this.version == null) {
      this.version = "1.0";
    }
    uuid = new ProcessDefinitionUUID(name, version);
    type = ProcessType.PROCESS;
  }

  @Override
  public String toString() {
    String st = this.getClass().getName() + "[uuid: " + getUUID() + ", name:" + getName() + ", description:"
        + getDescription() + ", version:" + getVersion();
    st += "]";
    return st;
  }

  @Override
  public ProcessState getState() {
    return state;
  }

  @Override
  public String getVersion() {
    return version;
  }

  @Override
  public ProcessDefinitionUUID getUUID() {
    return uuid;
  }

  @Override
  public Date getDeployedDate() {
    return Misc.getDate(deployedDate);
  }

  @Override
  public String getDeployedBy() {
    return deployedBy;
  }

  @Override
  public Date getUndeployedDate() {
    return Misc.getDate(undeployedDate);
  }

  @Override
  public String getUndeployedBy() {
    return undeployedBy;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == null) {
      return false;
    }
    if (!obj.getClass().equals(this.getClass())) {
      return false;
    }
    final LightProcessDefinitionImpl other = (LightProcessDefinitionImpl) obj;
    return other.getUUID().equals(uuid);
  }

  @Override
  public int hashCode() {
    return uuid.hashCode();
  }

  @Override
  public Set<String> getCategoryNames() {
    if (categories == null) {
      return Collections.emptySet();
    }
    return categories;
  }

  @Override
  public ProcessType getType() {
    return type;
  }

  @Override
  public Date getMigrationDate() {
    return Misc.getDate(migrationDate);
  }

  public void setMigrationDate(final Date migrationDate) {
    this.migrationDate = Misc.getTime(migrationDate);
  }

}
