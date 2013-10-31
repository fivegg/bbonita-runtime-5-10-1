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
package org.ow2.bonita.facade.def.element.impl;

import org.ow2.bonita.facade.def.element.MetaData;

/**
 * 
 * @author Matthieu Chaffotte
 *
 */
public class MetaDataImpl implements MetaData {

  private static final long serialVersionUID = 8639036213681835900L;
  protected long dbid;
  protected String key;
  protected String value;

  protected MetaDataImpl() {
    super();
  }

  public MetaDataImpl(String key, String value) {
    this.key = key;
    this.value= value;
  }

  public MetaDataImpl(MetaData meta) {
    this.key = meta.getKey();
    this.value = meta.getValue();
  }

  public String getKey() {
    return key;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return "MetaDataImpl [key=" + key + ", value=" + value + "]";
  }
  
}
