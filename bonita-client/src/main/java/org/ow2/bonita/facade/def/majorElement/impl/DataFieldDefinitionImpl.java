/**
 * Copyright (C) 2006  Bull S. A. S.
 * Bull, Rue Jean Jaures, B.P.68, 78340, Les Clayes-sous-Bois
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA  02110-1301, USA.
 * 
 * Modified by Matthieu Chaffotte, Elias Ricken de Medeiros - BonitaSoft S.A.
 **/
package org.ow2.bonita.facade.def.majorElement.impl;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.ow2.bonita.facade.def.majorElement.DataFieldDefinition;
import org.ow2.bonita.facade.uuid.ActivityDefinitionUUID;
import org.ow2.bonita.facade.uuid.DataFieldDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;

public class DataFieldDefinitionImpl extends ProcessElementImpl implements DataFieldDefinition {

  private static final long serialVersionUID = 3520847216843051032L;

  protected DataFieldDefinitionUUID uuid;

  protected String dataTypeClassName;

  protected String scriptingValue;

  protected String enumerationValues;

  protected Serializable clientInitialValue;

  private boolean isTransient;

  protected DataFieldDefinitionImpl() {
    super();
  }

  public DataFieldDefinitionImpl(final ProcessDefinitionUUID processUUID, final String name,
      final String dataTypeClassName) {
    super(name, processUUID);
    uuid = new DataFieldDefinitionUUID(processUUID, name);
    this.dataTypeClassName = dataTypeClassName;
    isTransient = false;
  }

  public DataFieldDefinitionImpl(final ProcessDefinitionUUID processUUID, final ActivityDefinitionUUID activityUUID,
      final String name, final String dataTypeClassName) {
    super(name, processUUID);
    uuid = new DataFieldDefinitionUUID(activityUUID, name);
    this.dataTypeClassName = dataTypeClassName;
    isTransient = false;
  }

  public DataFieldDefinitionImpl(final DataFieldDefinition src) {
    super(src);
    uuid = new DataFieldDefinitionUUID(src.getUUID());
    dataTypeClassName = src.getDataTypeClassName();

    final DataFieldDefinitionImpl srcImpl = (DataFieldDefinitionImpl) src;
    enumerationValues = srcImpl.enumerationValues;
    clientInitialValue = src.getInitialValue();
    scriptingValue = src.getScriptingValue();
    isTransient = src.isTransient();
  }

  @Override
  public String getDataTypeClassName() {
    return dataTypeClassName;
  }

  @Override
  public Serializable getInitialValue() {
    return getClientInitialValue();
  }

  @Override
  public DataFieldDefinitionUUID getUUID() {
    return uuid;
  }

  @Override
  public Set<String> getEnumerationValues() {
    if (enumerationValues == null) {
      return Collections.emptySet();
    }
    final String[] values = enumerationValues.split(",");
    final Set<String> result = new HashSet<String>();
    for (final String s : values) {
      result.add(s);
    }
    return result;
  }

  public void setEnumerationValues(final Set<String> enumerationValues) {
    if (enumerationValues != null) {
      this.enumerationValues = "";
      final Iterator<String> it = enumerationValues.iterator();
      while (it.hasNext()) {
        this.enumerationValues += it.next();
        if (it.hasNext()) {
          this.enumerationValues += ",";
        }
      }
    }
  }

  public void setInitialValue(final Serializable initialValue) {
    clientInitialValue = initialValue;
  }

  @Override
  public boolean isEnumeration() {
    return dataTypeClassName.equals(String.class.getName()) && enumerationValues != null;
  }

  private Serializable getClientInitialValue() {
    return clientInitialValue;
  }

  @Override
  public boolean equals(final Object o) {
    if (o == null) {
      return false;
    }
    if (!o.getClass().equals(this.getClass())) {
      return false;
    }
    final DataFieldDefinitionImpl other = (DataFieldDefinitionImpl) o;
    return other.getUUID().equals(getUUID());
  }

  public void setScriptingValue(final String scriptingValue) {
    this.scriptingValue = scriptingValue;
  }

  @Override
  public String getScriptingValue() {
    return scriptingValue;
  }

  @Override
  public boolean isTransient() {
    return isTransient;
  }

  public void setTransient(final boolean isTransient) {
    this.isTransient = isTransient;
  }

}
