/**
 * Copyright (C) 2009-2011 BonitaSoft S.A.
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
package org.ow2.bonita.facade.runtime.impl;

import org.ow2.bonita.facade.runtime.Case;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;

/**
 * 
 * @author Charles Souillard, Matthieu Chaffotte
 *
 */
public class CaseImpl implements Case, Comparable<CaseImpl> {

  private static final long serialVersionUID = -1171017304614068835L;

  protected long dbid;
  protected ProcessInstanceUUID uuid;
  protected String ownerName;
  protected String labelName;
  protected long lastUpdate;

  protected String label;

  protected CaseImpl() { }

  public CaseImpl(ProcessInstanceUUID uuid, String ownerName, String labelName) {
    super();
    this.uuid = uuid;
    this.labelName = labelName;
    this.ownerName = ownerName;

    this.label = buildLabel(this.ownerName, this.labelName);
  }

  public CaseImpl(Case cas) {
    super();
    this.uuid = cas.getUUID();
    this.labelName = cas.getLabelName();
    this.ownerName = cas.getOwnerName();
    this.lastUpdate = cas.getLastUpdate();

    this.label = buildLabel(this.ownerName, this.labelName);
  }

  public static String buildLabel(final String ownerName, final String labelName) {
    return ownerName + "--" + labelName;
  }

  public long getLastUpdate() {
    return lastUpdate;
  }

  public ProcessInstanceUUID getUUID() {
    return uuid;
  }

  public String getLabelName() {
    return labelName;
  }

  public String getOwnerName() {
    return this.ownerName;
  }

  @Override
  public int hashCode() {
    return (uuid + labelName + ownerName).hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }
    if (!(o instanceof CaseImpl)) {
      return false;
    }
    CaseImpl other = (CaseImpl) o;
    return other.getUUID().equals(getUUID()) && other.getLabelName().equals(labelName) && other.getOwnerName().equals(ownerName);
  }

  public void setLabelName(String labelName) {
    this.labelName = labelName;
    this.label = buildLabel(ownerName, labelName);
  }

  @Override
  public String toString() {
    return "CaseImpl [dbid=" + dbid + ", labelName=" + labelName
    + ", ownerName=" + ownerName + ", uuid=" + uuid + "]";
  }

  public void setLastUpdate(long lastUpdate) {
    this.lastUpdate = lastUpdate;    
  }

  public String getLabel() {
    return label;
  }

  public int compareTo(CaseImpl c) {
    if (c == null) {
      return 1;
    }
    final Long my = Long.valueOf(this.lastUpdate);
    final Long other = Long.valueOf(c.getLastUpdate());
    return my.compareTo(other);
  }

}
